import os
import torch
import numpy as np
import pandas as pd
import model_list  # 确保此模块中包含 CNN_LSTM 和 CNN_Transformer
from torch.utils.data import Dataset, DataLoader

# 使用的模型类型
main_model = "CNN_LSTM"  # 也可设为 "CNN_Transformer"

# 加载预处理后的完整数据（或新的测试数据）
file_path = './dataset/N-CMAPSS/dataset.csv'
data = pd.read_csv(file_path, header=None)

# 设置要进行寿命预测的发动机编号
target_engine_ids = [2]  # 替换为你想预测的发动机编号

# 模型超参数
SEQUENCE_LENGTH = 30
BATCH_SIZE = 1

# 判断设备（GPU / CPU）
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")


# 定义Dataset类（修改为只取第一个样本）
class EngineDataset(Dataset):
    """
        修改后的类，只提取第一个样本序列
    """

    def __init__(self, data, engine_ids, sequence_length=30):
        self.sequence_length = sequence_length
        self.data = data[data.iloc[:, 0].isin(engine_ids)]
        self.samples = []
        self.labels = []

        for engine_id in engine_ids:
            engine_data = self.data[self.data.iloc[:, 0] == engine_id]
            if len(engine_data) >= self.sequence_length:
                # 只取第一个符合条件的序列
                first_rul = engine_data.iloc[0, 3]
                rul_data = engine_data[engine_data.iloc[:, 3] == first_rul]
                if len(rul_data) >= self.sequence_length:
                    self.samples.append(rul_data.iloc[:self.sequence_length, 8:].values)  # 取前30行
                    self.labels.append([first_rul])
                    break  # 只取第一个样本

    def __len__(self):
        return len(self.samples)

    def __getitem__(self, idx):
        return torch.tensor(self.samples[idx], dtype=torch.float), torch.tensor(self.labels[idx], dtype=torch.float)


# 加载模型
if main_model == "CNN_LSTM":
    model = model_list.CNN_LSTM()
elif main_model == "CNN_Transformer":
    model = model_list.CNN_Transformer()

# 加载训练好的模型参数
model_path = f'./training_model/{main_model}/model_127.model'
model.load_state_dict(torch.load(model_path, map_location=device))
model.to(device)
model.eval()

# 构建数据集和加载器（现在只会包含一个样本）
test_dataset = EngineDataset(data, target_engine_ids, sequence_length=SEQUENCE_LENGTH)
test_loader = DataLoader(test_dataset, batch_size=BATCH_SIZE, shuffle=False)

# 推理并输出预测结果（现在只会预测第一个样本）
with torch.no_grad():
    for b_x, b_y in test_loader:
        b_x = b_x.to(device)
        _, outputs = model(b_x)
        prediction = outputs.cpu().numpy().flatten()[0]
        true_value = b_y.numpy().flatten()[0]
        print(f"第一个样本的预测结果: 真实 RUL = {true_value:.2f}, 预测 RUL = {prediction:.2f}")
        break  # 只处理第一个批次

# 如果没有找到任何样本
if len(test_dataset) == 0:
    print("警告: 没有找到符合条件的样本进行预测")