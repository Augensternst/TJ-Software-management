import pandas as pd
import numpy as np

# 读取CSV文件
data = pd.read_csv('selected_data.csv')

# 筛选发动机寿命RUL（第四列）大于等于70的行
filtered_data = data[data.iloc[:, 3] >= 70]
print(f"RUL大于等于70的行数: {filtered_data.shape[0]}")

# 获取第九列及之后的所有列
columns_of_interest = filtered_data.iloc[:, 8:]

# 计算平均值
means = columns_of_interest.mean()

# 计算标准差
stds = columns_of_interest.std()

# 打印结果
print("\n平均值:")
for i, mean_val in enumerate(means):
    col_index = i + 9
    col_name = data.columns[i + 8] if i + 8 < len(data.columns) else f"列{col_index}"
    print(f"{col_index}: {mean_val:.6f}")

print("\n标准差:")
for i, std_val in enumerate(stds):
    col_index = i + 9
    col_name = data.columns[i + 8] if i + 8 < len(data.columns) else f"列{col_index}"
    print(f"{col_index}: {std_val:.6f}")