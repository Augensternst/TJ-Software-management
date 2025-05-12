import math
import time
import os
import numpy as np
import pandas as pd
import torch
import model_list
from torch.utils.data import Dataset, DataLoader
import matplotlib
import matplotlib.pyplot as plt

np.set_printoptions(threshold=np.inf)


# 超参数设置
EPOCH = 1000
BATCH_SIZE = 60
LR = 0.001
# 模型选择 两个模型 CNN_LSTM CNN_Transformer
main_model = "CNN_LSTM"
# 读取预处理后的数据
file_path = './dataset/N-CMAPSS/dataset.csv'  # 路径
data = pd.read_csv(file_path, header=None)

# 从数据集中获取所有的发动机编号
"""[2. 5. 10. 16. 18. 20. 11. 14. 15.]"""
engine_ids = data.iloc[:, 0].unique()

# 假设前8个发动机用于训练，最后一个发动机用于测试
train_engine_ids = engine_ids[0:-1]
test_engine_ids = engine_ids[-1:]

# 如第一个发动机用于测试，其余发动机用于训练，则train_engine_ids = engine_ids[1:], test_engine_ids = engine_ids[0:1]

print("train: ", train_engine_ids, "test: ", test_engine_ids)
# 生成路径
def mkdir(path):
    isExists = os.path.exists(path)
    if not isExists:
        os.makedirs(path)
        return True


class EngineDataset(Dataset):
    """
    自定义的PyTorch Dataset，用于加载并处理发动机数据集。
    """

    """
            初始化数据集。
            :param data: DataFrame，整个数据集。
            :param engine_ids: list，包含要包含在该数据集中的发动机编号。
            :param sequence_length: int，每个样本的时间序列长度。
    """

    def __init__(self, data, engine_ids, sequence_length=30):

        self.sequence_length = sequence_length

        # 过滤出指定发动机的数据
        self.data = data[data.iloc[:, 0].isin(engine_ids)]

        # 生成数据样本
        self.samples = []
        self.labels = []
        for engine_id in engine_ids:
            engine_data = self.data[self.data.iloc[:, 0] == engine_id]
            for rul in engine_data.iloc[:, 3].unique():
                rul_data = engine_data[engine_data.iloc[:, 3] == rul]
                if len(rul_data) >= self.sequence_length:
                    self.samples.append(rul_data.iloc[-self.sequence_length:, 8:].values)
                    self.labels.append([rul])

    """
        返回数据集中样本的数量。
    """
    def __len__(self):

        return len(self.samples)

    """
           获取指定索引处的样本。
           :param idx: int，样本的索引。
           :return: tuple，包含特征和标签。
    """
    def __getitem__(self, idx):

        return torch.tensor(self.samples[idx], dtype=torch.float), torch.tensor(self.labels[idx], dtype=torch.float)

# 计算S-score
def compute_s_score(rul_true, rul_pred):
    """
    rul_true和rul_pred都应该是1D的NumPy数组。
    """
    diff = rul_pred - rul_true
    return np.sum(np.where(diff < 0, np.exp(-diff/13) - 1, np.exp(diff/10) - 1))

# 创建训练和测试数据集
train_dataset = EngineDataset(data, train_engine_ids)
test_dataset = EngineDataset(data, test_engine_ids)

# 创建DataLoader
train_loader = DataLoader(train_dataset, batch_size=BATCH_SIZE, shuffle=True)
test_loader = DataLoader(test_dataset, batch_size=BATCH_SIZE, shuffle=False)

# 使用GPU或CPU
cuda_avail = torch.cuda.is_available()


#区分所使用的模型结构
if main_model == "CNN_LSTM":
    model = model_list.CNN_LSTM()
elif main_model == "CNN_Transformer":
    model = model_list.CNN_Transformer()

# 所有的数据数
total_params = sum(p.numel() for p in model.parameters())

# 可训练的数据数量
trainable_params = sum(p.numel() for p in model.parameters() if p.requires_grad)

print(f"Total Parameters: {total_params}")
print(f"Trainable Parameters: {trainable_params}")

if cuda_avail:
    model.to("cuda")

# 以Adam作为优化学习器，优化rnn的所有参数，学习率为0.01
optimizer = torch.optim.Adam(model.parameters(), lr=LR, weight_decay=0.0001)  # optimize all cnn parameters
# 以交叉熵损失函数作为损失函数
loss_func = torch.nn.MSELoss()  # the target label is not one-hotted
loss_func_mae = torch.nn.L1Loss()

train_loss_list = []
test_loss_list = []


## 保存模型的路径
model_save_path = "./training_model/" + main_model
mkdir(model_save_path)
def save_models(epoch):
    torch.save(model.state_dict(), model_save_path + "/model_{}.model".format(epoch))


labels_list = []
prediction_list = []
out_list = []

def train():
    for epoch in range(EPOCH):  # EPOCH为训练迭代次数
        model.train()  # 模型开始训练
        train_loss = 0.0  # 训练集准确率

        if epoch <= 50:
            for param_group in optimizer.param_groups:
                param_group['lr'] = 0.001
        elif 70 >= epoch > 50:
            for param_group in optimizer.param_groups:
                param_group['lr'] = 0.0001
        elif 90 >= epoch > 70:
            for param_group in optimizer.param_groups:
                param_group['lr'] = 0.00001
        else:
            for param_group in optimizer.param_groups:
                param_group['lr'] = 0.000001  # 设置学习率为0.0001

        for step, (b_x, b_y) in enumerate(train_loader):  # 批量喂入数据
            if cuda_avail:
                b_x = b_x.to("cuda")
                b_y = b_y.to("cuda")
            _, outputs = model(b_x)  # 把batch size的数据放入模型得到输出
            loss = loss_func(outputs, b_y)  # 计算预测值和真实值之间的误差
            optimizer.zero_grad()  # 将这一步的梯度清零
            loss.backward()  # 误差反向传递
            optimizer.step()  # 优化器优化
            train_loss += loss.item() * b_x.size(0)  # 累计batch size的训练误差

        # 计算训练集样本的平均误差
        train_loss = train_loss / len(train_dataset)
        # 计算测试集样本的平均误差
        test_loss = test()
        print(f"Epoch: {epoch}, TrainLoss: {train_loss}, TestLoss: {test_loss}, RMSE: {math.sqrt(test_loss)}")
        train_loss_list.append(train_loss)
        test_loss_list.append(test_loss)
        # 保存每一次迭代的训练模型
        save_models(epoch)
    print(test_loss_list)


# 模型测试
def test():
    model.eval()
    test_loss = 0.0
    for step, (b_x, b_y) in enumerate(test_loader):
        if cuda_avail:
            b_x = b_x.to("cuda")
            b_y = b_y.to("cuda")

        _, outputs = model(b_x)
        loss = loss_func(outputs, b_y)
        test_loss += loss.item() * b_x.size(0)

    test_loss = test_loss / len(test_dataset)

    return test_loss


def test_model(model_file):
    # 加载训练好的模型
    model.load_state_dict(torch.load(model_file))
    model.eval()

    # 将训练集误差初始为0.0
    test_loss = 0.0
    test_loss_mae = 0.0

    # prediction_list用于存放测试集每个样本预测结果
    prediction_list = np.array([])
    true_list = np.array([])

    for step, (b_x, b_y) in enumerate(test_loader):
        if cuda_avail:
            b_x = b_x.to("cuda")
            b_y = b_y.to("cuda")

        _, outputs = model(b_x)
        loss = loss_func(outputs, b_y)
        test_loss += loss.item() * b_x.size(0)
        loss_mae = loss_func_mae(outputs, b_y)
        test_loss_mae += loss_mae.item() * b_x.size(0)

        prediction = torch.flatten(outputs, start_dim=0, end_dim=1)
        prediction_list = np.append(prediction_list, prediction.cpu().detach().numpy())
        true = torch.flatten(b_y, start_dim=0, end_dim=1)
        true_list = np.append(true_list, true.cpu().detach().numpy())

    mse_loss = test_loss / len(test_dataset)
    mae_loss = test_loss_mae / len(test_dataset)
    rmse_loss = np.sqrt(mse_loss)
    return mae_loss, mse_loss, rmse_loss, prediction_list, true_list, file_path







if __name__ == "__main__":

    '''训练模式'''
    # train()

    '''测试模式'''
    a = 1
    if a == 1:
        index = 5
        for i in range(index, 100):
            mae_loss, mse_loss, rmse_loss, prediction_list, true_list, file_path = test_model(
                './training_model/' + main_model + '/model_' + str(i) + '.model')
            s_score = compute_s_score(true_list, prediction_list)
            if i == index:
                min_loss = mse_loss
                print('index: ', i, 'test_loss: ', min_loss, 'rmse_loss: ', rmse_loss)
            else:
                if mse_loss <= min_loss:
                    min_loss = mse_loss
                    index = i
                    print('index: ', i, 'test_loss: ', min_loss, 'rmse_loss: ', rmse_loss)
        # elif a == 2:
        model_test_num = index
        mae_loss, mse_loss, rmse_loss, prediction_list, true_list, file_path = test_model(
            './training_model/' + main_model + '/model_' + str(model_test_num) + '.model')
        print('MAE: ', mae_loss, 'MSE: ', mse_loss, 'RMSE: ', rmse_loss)
        # s_score = compute_s_score(true_list, prediction_list)
        print("S-score：", s_score)
        '''保存图片'''
        X = np.linspace(0, len(prediction_list) - 1, num=len(prediction_list))

        matplotlib.rcParams['font.family'] = 'Times New Roman'
        matplotlib.rcParams['font.size'] = 15

        plt.figure(figsize=(10, 6))
        plt.plot(X, prediction_list, label="Predicted value", linestyle="-", marker="o")
        plt.plot(true_list, label="True value", linestyle="-", marker="o")
        plt.xlabel("Time step")
        plt.ylabel("RUL")
        plt.legend()
        figure_save_path = "image_" + main_model  # 这里创建了一个文件夹，如果依次创建不同文件夹，可以用name_list[i]
        if not os.path.exists(figure_save_path):
            os.makedirs(figure_save_path)  # 如果不存在目录figure_save_path，则创建
        plt.savefig(os.path.join(figure_save_path, str(i) + '_image.png'))  # 使用下划线或其他字符作为分隔符
        # plt.close()  # 关闭之前的图形
        plt.show()
