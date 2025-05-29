from flask import Flask, request, jsonify
import os
import torch
import numpy as np
import pandas as pd
from torch.utils.data import Dataset, DataLoader
import model_list  # 确保此模块中包含 CNN_LSTM 和 CNN_Transformer
from werkzeug.utils import secure_filename
import tempfile
import h5py
import warnings
import random


app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = 16 * 1024 * 1024  # 限制上传文件大小为16MB

# 判断设备（GPU / CPU）
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
warnings.filterwarnings('ignore')

# 预定义的均值和标准差（从训练数据中计算得到）
FEATURE_MEANS = {
    'T24': 563.024898, 'T30': 1323.801706, 'T48': 1642.810916, 'T50': 1113.662715,
    'P15': 11.520329, 'P2': 8.851444, 'P21': 11.695766, 'P24': 14.386256,
    'Ps30': 217.774155, 'P40': 221.550853, 'P50': 8.651160, 'Nf': 1998.137013,
    'Nc': 8218.573059, 'Wf': 2.345737, 'T40': 2542.433162, 'P30': 231.990422,
    'P45': 41.184797, 'W21': 1808.173724, 'W22': 158.947076, 'W25': 158.946923,
    'W31': 18.284355, 'W32': 10.970613, 'W48': 148.620510, 'W50': 157.362797,
    'SmFan': 19.331695, 'SmLPC': 8.085485, 'SmHPC': 28.067971, 'phi': 38.495293,
    'HPT_eff_mod': -0.003271, 'LPT_eff_mod': -0.001792, 'LPT_flow_mod': -0.002024
}

FEATURE_STDS = {
    'T24': 18.303730, 'T30': 55.677034, 'T48': 99.883238, 'T50': 52.041587,
    'P15': 2.299463, 'P2': 1.916357, 'P21': 2.334480, 'P24': 2.775538,
    'Ps30': 45.544128, 'P40': 46.230581, 'P50': 2.038587, 'Nf': 142.164579,
    'Nc': 184.868923, 'Wf': 0.584898, 'T40': 144.017769, 'P30': 48.408985,
    'P45': 8.696471, 'W21': 311.812302, 'W22': 28.975486, 'W25': 28.975412,
    'W31': 3.422720, 'W32': 2.053632, 'W48': 27.942588, 'W50': 29.536117,
    'SmFan': 1.428708, 'SmLPC': 0.917600, 'SmHPC': 2.070840, 'phi': 2.295575,
    'HPT_eff_mod': 0.003450, 'LPT_eff_mod': 0.003149, 'LPT_flow_mod': 0.003206
}


class EngineDataset(Dataset):
    """数据集类，用于处理输入数据"""

    def __init__(self, data, sequence_length=30):
        self.sequence_length = sequence_length
        self.data = data
        self.samples = []

        # 假设输入文件只有一组特征值（一个发动机）
        engine_data = self.data

        # 检查数据是否足够
        if len(engine_data) >= self.sequence_length:
            print("数据足够")
            # 取最后30行数据
            self.samples.append(engine_data.iloc[-self.sequence_length:, 8:].values)
        else:
            print("数据不足，使用相同数据填充")

            # 获取现有的特征数据（第8列之后）
            existing_data = engine_data.iloc[:, 8:].values

            # 计算需要填充的行数
            padding_rows = self.sequence_length - len(engine_data)

            # 方法1: 用最后一行数据重复填充
            if len(existing_data) > 0:
                # 取最后一行数据进行重复
                last_row = existing_data[-1:, :]  # 保持二维形状 (1, n_features)
                padding = np.tile(last_row, (padding_rows, 1))  # 重复 padding_rows 次

                # 将填充数据放在前面，原数据放在后面
                padded_data = np.vstack((padding, existing_data))
            else:
                # 如果没有数据，创建全零数据
                padded_data = np.zeros((self.sequence_length, engine_data.shape[1] - 8))

            self.samples.append(padded_data)
            print(self.samples)

            print(f"原始数据行数: {len(engine_data)}")
            print(f"填充行数: {padding_rows}")
            print(f"最终数据形状: {padded_data.shape}")

    def __len__(self):
        return len(self.samples)

    def __getitem__(self, idx):
        return torch.tensor(self.samples[idx], dtype=torch.float)


def manual_normalize(df, feature_means=FEATURE_MEANS, feature_stds=FEATURE_STDS):
    """使用预定义的均值和标准差对数据进行手动Z-Score归一化"""
    normalized_df = df.copy()

    # 对每个特征列应用Z-Score归一化
    for col in df.columns[8:]:
        if col in feature_means and col in feature_stds:
            normalized_df[col] = (df[col] - feature_means[col]) / feature_stds[col]
        else:
            print(f"警告: 缺少特征列 {col} 的均值或标准差，该列未归一化")
    print(normalized_df)

    return normalized_df


def preprocess_data(file_path):
    """数据预处理流程"""
    try:
        # 读取带表头的CSV文件
        raw_data = pd.read_csv(file_path)

        # 特征列 - 这些是模型实际需要的列
        feature_columns = ['T24', 'T30', 'T48', 'T50', 'P15', 'P2', 'P21', 'P24',
                           'Ps30', 'P40', 'P50', 'Nf', 'Nc', 'Wf', 'T40', 'P30',
                           'P45', 'W21', 'W22', 'W25', 'W31', 'W32', 'W48', 'W50',
                           'SmFan', 'SmLPC', 'SmHPC', 'phi', 'HPT_eff_mod',
                           'LPT_eff_mod', 'LPT_flow_mod']

        # 检查必要的特征列是否存在
        missing_cols = set(feature_columns) - set(raw_data.columns)
        if missing_cols:
            raise ValueError(f"缺少必要的特征列: {missing_cols}")

        # 添加非特征列（如果不存在则创建填充值）
        metadata_columns = ['unit', 'cycle', 'hs', 'RUL', 'alt', 'Mach', 'TRA', 'T2']
        for col in metadata_columns:
            if col not in raw_data.columns:
                # 为缺失的非特征列创建默认值
                if col == 'unit':
                    raw_data[col] = 1  # 默认为第1个发动机
                elif col == 'cycle':
                    raw_data[col] = range(1, len(raw_data) + 1)  # 顺序循环
                elif col == 'RUL':
                    raw_data[col] = 0  # 默认RUL为0，将由模型预测
                else:
                    raw_data[col] = 0  # 其他列默认为0

        # 确保所有列的顺序与原始代码一致
        selected_cols = ['unit', 'cycle', 'hs', 'RUL', 'alt', 'Mach', 'TRA', 'T2'] + feature_columns
        data = raw_data[selected_cols]

        # 使用预定义的均值和标准差进行手动归一化，而不是使用StandardScaler
        result_df = manual_normalize(data)

        # 保存原始数据用于分析损伤位置
        result_df.raw_data = data.copy()

        # result_df.to_csv('normalized_data.csv', index=False)

        return result_df

    except Exception as e:
        raise ValueError(f"数据预处理失败: {str(e)}")


def determine_damage_location(raw_data):
    """根据各字段的值判断损伤位置，使用正常均值和标准差评估"""
    # 提取最后一行数据进行分析
    latest_data = raw_data.iloc[-1] if len(raw_data) > 0 else None

    if latest_data is None:
        return "无法确定损伤位置，数据为空"

    # 初始化分析结果
    damages = []
    health_index = 100  # 初始健康指数为100

    # 使用均值和标准差定义的正常范围
    feature_means = {
        'T24': 561.734307, 'T30': 1320.850085, 'T48': 1630.231993, 'T50': 1104.318060,
        'P15': 11.409782, 'P2': 8.780057, 'P21': 11.583535, 'P24': 14.234296,
        'Ps30': 215.225091, 'P40': 219.019148, 'P50': 8.548206, 'Nf': 1993.529530,
        'Nc': 8211.029946, 'Wf': 2.300304, 'T40': 2527.272002, 'P30': 229.339422,
        'P45': 40.602641, 'W21': 1794.094218, 'W22': 157.625671, 'W25': 157.625378,
        'W31': 18.128248, 'W32': 10.876949, 'W48': 147.326124, 'W50': 155.995802,
        'SmFan': 19.497370, 'SmLPC': 8.224322, 'SmHPC': 28.382235, 'phi': 38.184013,
        'HPT_eff_mod': -0.000920, 'LPT_eff_mod': -0.000097, 'LPT_flow_mod': -0.000077
    }

    feature_stds = {
        'T24': 19.197751, 'T30': 57.766587, 'T48': 101.321474, 'T50': 52.922060,
        'P15': 2.312136, 'P2': 1.916450, 'P21': 2.347347, 'P24': 2.792789,
        'Ps30': 45.830745, 'P40': 46.537956, 'P50': 2.034312, 'Nf': 141.865723,
        'Nc': 192.280626, 'Wf': 0.581590, 'T40': 146.892788, 'P30': 48.730844,
        'P45': 8.721765, 'W21': 314.120672, 'W22': 29.337796, 'W25': 29.337905,
        'W31': 3.465540, 'W32': 2.079324, 'W48': 28.282172, 'W50': 29.895559,
        'SmFan': 1.727403, 'SmLPC': 1.084494, 'SmHPC': 2.149762, 'phi': 2.312139,
        'HPT_eff_mod': 0.000230, 'LPT_eff_mod': 0.000224, 'LPT_flow_mod': 0.000186
    }

    # 按参数组评估
    params_groups = {
        'temperature': ['T24', 'T30', 'T48', 'T50', 'T40'],
        'pressure': ['P15', 'P2', 'P21', 'P24', 'Ps30', 'P40', 'P50', 'P30', 'P45'],
        'flow': ['W21', 'W22', 'W25', 'W31', 'W32', 'W48', 'W50'],
        'efficiency': ['HPT_eff_mod', 'LPT_eff_mod', 'LPT_flow_mod'],
        'speed': ['Nf', 'Nc'],
        'fuel': ['Wf']
    }

    group_descriptions = {
        'temperature': '热区',
        'pressure': '压力系统',
        'flow': '流量通道',
        'efficiency': '涡轮效率',
        'speed': '轴承或转子系统',
        'fuel': '燃烧室'
    }

    # 使用Z分数来评估参数异常程度
    for group_name, params in params_groups.items():
        for param in params:
            if param in latest_data and param in feature_means and param in feature_stds:
                value = latest_data[param]
                mean = feature_means[param]
                std = feature_stds[param]

                # 计算Z分数 (标准化分数)
                z_score = abs((value - mean) / std) if std > 0 else 0

                if z_score > 3.0:  # 超过3个标准差，严重异常
                    damages.append(f"{param}异常（{value:.1f}），可能导致{group_descriptions[group_name]}严重损伤")
                    health_index -= 25 if group_name in ['temperature', 'speed'] else 20
                elif z_score > 2.0:  # 超过2个标准差，中度异常
                    damages.append(f"{param}异常（{value:.1f}），{group_descriptions[group_name]}存在中度损伤")
                    health_index -= 15 if group_name in ['temperature', 'speed'] else 12
                elif z_score > 1.5:  # 超过1.5个标准差，轻微异常
                    damages.append(f"{param}偏离正常值（{value:.1f}），{group_descriptions[group_name]}存在轻微异常")
                    health_index -= 5 if group_name in ['temperature', 'speed'] else 3

                # 为效率参数特殊处理
                if group_name == 'efficiency':
                    # 对于效率修正系数，使用更严格的标准，因为其标准差很小
                    if z_score > 2.5:
                        damages.append(f"{param}严重偏离正常值（{value:.5f}），涡轮效率显著下降")
                        health_index -= 20
                    elif z_score > 1.5:
                        damages.append(f"{param}异常（{value:.5f}），涡轮效率中度下降")
                        health_index -= 15
                    elif z_score > 1.0:
                        damages.append(f"{param}偏离正常值（{value:.5f}），涡轮效率轻微下降")
                        health_index -= 5

    # 确保健康指数在0-100之间
    health_index = max(0, min(100, health_index))

    # 如果没有检测到具体损伤，根据健康指数给出综合评估
    if not damages:
        if health_index >= 90:
            return "设备状态良好，未检测到明显损伤"
        elif health_index >= 80:
            return "设备运行正常，部分参数略有波动"
        elif health_index >= 70:
            return "设备整体状态可接受，建议定期检查"
        elif health_index >= 60:
            return "设备存在轻微异常，建议关注温度和压力参数"
        elif health_index >= 50:
            return "设备多个系统参数偏离正常值，建议进行维护检查"
        else:
            return "设备状态异常，多处可能存在损伤，建议立即检修"

    # 限制返回的损伤信息数量，避免过长
    if len(damages) > 3:
        main_damages = damages[:3]
        return f"{', '.join(main_damages)}等多处异常"
    else:
        return f"{', '.join(damages)}"


@app.route('/predict', methods=['POST'])
def predict_rul():
    """
    预测设备剩余寿命(RUL)
    接收JSON格式参数:
    {
        "model_type": "CNN_LSTM",  # 或 "CNN_Transformer"
        "model_path": "./training_model/CNN_LSTM/model_127.model",
        "file_path": "/path/to/data.csv",
        "sequence_length": 30
    }
    或者表单格式上传文件
    """
    try:
        # 获取请求参数
        if request.content_type == 'application/json':
            data = request.get_json()
            model_type = data.get('model_type', 'CNN_LSTM')
            model_path = data.get('model_path', './training_model/CNN_LSTM/model_127.model')
            file_path = data.get('file_path','./dataset/N-CMAPSS/test_dataset.csv')
            sequence_length = data.get('sequence_length', 30)
        else:
            model_type = request.form.get('model_type', 'CNN_LSTM')
            model_path = request.form.get('model_path', './training_model/CNN_LSTM/model_127.model')
            file_path = None
            sequence_length = int(request.form.get('sequence_length', 30))

            # 处理文件上传
            if 'file' in request.files:
                file = request.files['file']
                if file.filename == '':
                    return jsonify({"status": "error", "message": "未选择文件"}), 400

                # 保存临时文件
                temp_dir = tempfile.mkdtemp()
                file_path = os.path.join(temp_dir, secure_filename(file.filename))
                file.save(file_path)

        # 参数验证
        if not file_path:
            return jsonify({"status": "error", "message": "必须提供文件路径或上传文件"}), 400

        if not os.path.exists(file_path):
            return jsonify({"status": "error", "message": f"文件不存在: {file_path}"}), 400

        if model_type not in ["CNN_LSTM", "CNN_Transformer"]:
            return jsonify({"status": "error", "message": "不支持的模型类型"}), 400

        # 数据预处理
        try:
            processed_data = preprocess_data(file_path)
            raw_data = processed_data.raw_data  # 获取原始数据用于分析损伤位置
        except Exception as e:
            return jsonify({"status": "error", "message": str(e)}), 400

        # 加载模型
        model = model_list.CNN_LSTM() if model_type == "CNN_LSTM" else model_list.CNN_Transformer()
        model.load_state_dict(torch.load(model_path, map_location=device))
        model.to(device)
        model.eval()

        print("Processed Data:")
        for i, item in enumerate(processed_data):
            print(f"{i}: {item}")

        # 预测
        test_dataset = EngineDataset(processed_data, sequence_length=sequence_length)
        test_loader = DataLoader(test_dataset, batch_size=1, shuffle=False)

        with torch.no_grad():
            for b_x in test_loader:
                b_x = b_x.to(device)
                _, outputs = model(b_x)
                prediction = outputs.cpu().numpy().flatten()[0]

        # 分析损伤位置
        damage_location = determine_damage_location(raw_data)

        # 计算健康指数 (基于预测的RUL值)
        # 假设最大RUL是300，可以根据实际情况调整

        max_rul = 70
        health_index = min(100, max(0, (prediction / max_rul) * 100))

        # 清理临时文件
        if 'temp_dir' in locals():
            import shutil
            shutil.rmtree(temp_dir)

        return jsonify({
            "status": "success",
            "predicted_rul": float(prediction),
            "model_used": model_type,
            "sequence_length": sequence_length,
            "damage_location": damage_location,
            "health_index": int(health_index),
            "message": "预测成功"
        })

    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500


@app.route('/')
def home():
    return jsonify({
        "message": "设备剩余寿命预测服务",
        "endpoints": {
            "/predict": "POST方法，接收预测请求"
        },
        "data_requirements": {
            "required_columns": [
                "T24", "T30", "T48", "T50", "P15", "P2", "P21", "P24",
                "Ps30", "P40", "P50", "Nf", "Nc", "Wf", "T40", "P30",
                "P45", "W21", "W22", "W25", "W31", "W32", "W48", "W50",
                "SmFan", "SmLPC", "SmHPC", "phi", "HPT_eff_mod",
                "LPT_eff_mod", "LPT_flow_mod"
            ],
            "note": "输入文件必须包含以上所有特征列，其他非特征列（unit, cycle, hs, RUL, alt, Mach, TRA, T2）如不提供将自动填充默认值"
        }
    })


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)