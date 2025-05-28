import torch
import torch.nn as nn
import torch.nn.functional as F
import math
import copy
import numpy as np


class ConfigTrans(object):
    """配置参数"""

    def __init__(self):
        self.model_name = 'Transformer'  # 模型名称，这里指定为 'Transformer'
        self.dropout = 0.0  # dropout比率，用于减少过拟合，值为0.5表示一半的节点将被随机丢弃
        self.pad_size = 48  # 序列填充的大小，用于确保所有序列长度一致
        self.embed = 92  # 嵌入向量的维度
        self.dim_model = 92  # Transformer模型中特征的维度
        self.hidden = 1024  # 隐藏层的大小
        self.last_hidden = 512  # 最后一个隐藏层的大小
        self.num_head = 4  # Transformer中的多头注意力机制的头数
        self.num_encoder = 2  # Transformer模型中编码器层的数量
        self.device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')  # 指定模型运行的设备


config = ConfigTrans()


class CNN_Transformer(nn.Module):
    def __init__(self):
        super(CNN_Transformer, self).__init__()

        self.conv1 = nn.Sequential(
            nn.Conv2d(1, 16, kernel_size=3, stride=1, padding=1),
            nn.BatchNorm2d(16),
            nn.ReLU(),
            nn.MaxPool2d(kernel_size=(1, 2)),
        )

        self.conv2 = nn.Sequential(
            nn.Conv2d(16, 32, kernel_size=3, stride=1, padding=1),
            nn.BatchNorm2d(32),
            nn.ReLU(),
            nn.MaxPool2d(kernel_size=(1, 2)),
        )

        self.conv3 = nn.Sequential(
            nn.Conv2d(32, 48, kernel_size=3, stride=1, padding=1),
            nn.BatchNorm2d(48),
            nn.ReLU(),
            nn.MaxPool2d(kernel_size=(1, 2)),
        )

        self.postion_embedding = Positional_Encoding(config.embed, config.pad_size, config.dropout, config.device)
        self.encoder = Encoder(config.dim_model, config.num_head, config.hidden, config.dropout)
        self.encoders = nn.ModuleList([
            copy.deepcopy(self.encoder)
            for _ in range(config.num_encoder)])

        self.fc = nn.Linear(config.pad_size * config.dim_model, 1)

        # self.fc = nn.Sequential(
        #     nn.Linear(config.pad_size * config.dim_model, 256),
        #     nn.ReLU(inplace=True),
        #     nn.Linear(256, 1)
        # )

    def forward(self, x):
        x = x.permute(0, 2, 1).unsqueeze(1)  # torch.Size([96, 1, 31, 30])
        x = self.conv1(x)  # torch.Size([96, 16, 31, 15])
        x = self.conv2(x)  # torch.Size([96, 32, 31, 7])
        x = self.conv3(x)  # torch.Size([96, 48, 31, 3])
        x = x.reshape(x.size(0), x.size(1), -1)[:, :, :-1]  # torch.Size([96, 48, 93])
        x = self.postion_embedding(x)
        for encoder in self.encoders:
            x = encoder(x)
        x = x.view(x.size(0), -1)
        feature = x.reshape(x.shape[0], -1)
        x = self.fc(x)
        return feature, x


class Encoder(nn.Module):
    def __init__(self, dim_model, num_head, hidden, dropout):
        super(Encoder, self).__init__()
        self.attention = Multi_Head_Attention(dim_model, num_head, dropout)
        self.feed_forward = Position_wise_Feed_Forward(dim_model, hidden, dropout)

    def forward(self, x):
        out = self.attention(x)
        out = self.feed_forward(out)
        return out


class Positional_Encoding(nn.Module):
    def __init__(self, embed, pad_size, dropout, device):
        super(Positional_Encoding, self).__init__()
        self.device = device
        self.pe = torch.tensor(
            [[pos / (10000.0 ** (i // 2 * 2.0 / embed)) for i in range(embed)] for pos in range(pad_size)])
        self.pe[:, 0::2] = np.sin(self.pe[:, 0::2])
        self.pe[:, 1::2] = np.cos(self.pe[:, 1::2])
        self.dropout = nn.Dropout(dropout)

    def forward(self, x):
        # print(x.shape)
        out = x + nn.Parameter(self.pe, requires_grad=False).to(self.device)
        out = self.dropout(out)
        return out


class Scaled_Dot_Product_Attention(nn.Module):
    '''Scaled Dot-Product Attention '''

    def __init__(self):
        super(Scaled_Dot_Product_Attention, self).__init__()

    def forward(self, Q, K, V, scale=None):
        '''
        Args:
            Q: [batch_size, len_Q, dim_Q]
            K: [batch_size, len_K, dim_K]
            V: [batch_size, len_V, dim_V]
            scale: 缩放因子 论文为根号dim_K
        Return:
            self-attention后的张量，以及attention张量
        '''
        attention = torch.matmul(Q, K.permute(0, 2, 1))
        if scale:
            attention = attention * scale
        # if mask:  # TODO change this
        #     attention = attention.masked_fill_(mask == 0, -1e9)
        attention = F.softmax(attention, dim=-1)
        context = torch.matmul(attention, V)
        return context


class Multi_Head_Attention(nn.Module):
    def __init__(self, dim_model, num_head, dropout=0.0):
        super(Multi_Head_Attention, self).__init__()
        self.num_head = num_head
        assert dim_model % num_head == 0
        self.dim_head = dim_model // self.num_head
        self.fc_Q = nn.Linear(dim_model, num_head * self.dim_head)
        self.fc_K = nn.Linear(dim_model, num_head * self.dim_head)
        self.fc_V = nn.Linear(dim_model, num_head * self.dim_head)
        self.attention = Scaled_Dot_Product_Attention()
        self.fc = nn.Linear(num_head * self.dim_head, dim_model)
        self.dropout = nn.Dropout(dropout)
        self.layer_norm = nn.LayerNorm(dim_model)

    def forward(self, x):
        batch_size = x.size(0)
        Q = self.fc_Q(x)
        K = self.fc_K(x)
        V = self.fc_V(x)
        Q = Q.view(batch_size * self.num_head, -1, self.dim_head)
        K = K.view(batch_size * self.num_head, -1, self.dim_head)
        V = V.view(batch_size * self.num_head, -1, self.dim_head)
        # if mask:  # TODO
        #     mask = mask.repeat(self.num_head, 1, 1)  # TODO change this
        scale = K.size(-1) ** -0.5  # 缩放因子
        context = self.attention(Q, K, V, scale)

        context = context.view(batch_size, -1, self.dim_head * self.num_head)
        out = self.fc(context)
        out = self.dropout(out)
        out = out + x  # 残差连接
        out = self.layer_norm(out)
        return out


class Position_wise_Feed_Forward(nn.Module):
    def __init__(self, dim_model, hidden, dropout=0.0):
        super(Position_wise_Feed_Forward, self).__init__()
        self.fc1 = nn.Linear(dim_model, hidden)
        self.fc2 = nn.Linear(hidden, dim_model)
        self.dropout = nn.Dropout(dropout)
        self.layer_norm = nn.LayerNorm(dim_model)

    def forward(self, x):
        out = self.fc1(x)
        out = F.relu(out)
        out = self.fc2(out)
        out = self.dropout(out)
        out = out + x  # 残差连接
        out = self.layer_norm(out)
        return out


class CNN_LSTM(nn.Module):
    def __init__(self):
        super(CNN_LSTM, self).__init__()

        self.layer1 = nn.Sequential(
            nn.Conv2d(1, 16, kernel_size=3, stride=1, padding=1),
            nn.BatchNorm2d(16),
            nn.ReLU(),
            nn.MaxPool2d(kernel_size=(1, 2)),
        )

        self.layer2 = nn.Sequential(
            nn.Conv2d(16, 32, kernel_size=3, stride=1, padding=1),
            nn.BatchNorm2d(32),
            nn.ReLU(),
            nn.MaxPool2d(kernel_size=(1, 2)),
        )

        self.layer3 = nn.Sequential(
            nn.Conv2d(32, 48, kernel_size=3, stride=1, padding=1),
            nn.BatchNorm2d(48),
            nn.ReLU(),
            nn.MaxPool2d(kernel_size=(1, 2)),
        )

        self.lstm_layer = nn.LSTM(
            input_size=93,
            hidden_size=128,
            num_layers=2,
            batch_first=True,
            bidirectional=True)

        # Softmax
        self.fc = nn.Sequential(
            nn.Linear(256, 64),
            nn.ReLU(inplace=True),
            nn.Linear(64, 1)
        )

    def forward(self, x):  # torch.Size([96, 30, 31])
        x = x.permute(0, 2, 1).unsqueeze(1)  # torch.Size([96, 1, 31, 30])
        x = self.layer1(x)  # torch.Size([96, 16, 31, 15])
        x = self.layer2(x)  # torch.Size([96, 32, 31, 7])
        x = self.layer3(x)  # torch.Size([96, 48, 31, 3])
        x = x.reshape(x.size(0), x.size(1), -1)  # torch.Size([96, 48, 93])
        x, _ = self.lstm_layer(x)
        x = x[:, -1, :]
        feature = x.view(x.shape[0], -1)
        x = x.view(x.shape[0], -1)
        x = self.fc(x)
        return feature, x
