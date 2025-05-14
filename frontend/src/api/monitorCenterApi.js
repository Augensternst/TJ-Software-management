import axios from 'axios';

const API_BASE_URL = 'https://your-api-endpoint.com/api';

/**
 * 获取设备的健康数据
 * @param {number} deviceId - 设备 ID
 * @returns {Promise<{ labels: string[], values: number[] }>}
 */
export const getDeviceHealthData = async (deviceId) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/devices/${deviceId}/health`);
    return response.data;
  } catch (error) {
    console.error('Error fetching device health data:', error);
    throw error;
  }
};

/**
 * 获取设备的能耗数据
 * @param {number} deviceId - 设备 ID
 * @returns {Promise<{ labels: string[], values: number[], energyCost: number }>}
 */
export const getDeviceEnergyData = async (deviceId) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/devices/${deviceId}/energy`);
    return response.data;
  } catch (error) {
    console.error('Error fetching device energy data:', error);
    throw error;
  }
};

/**
 * 获取设备的卡片数据（就是各项指标 health如果为-1则不会绘制或者展示健康指数）
 * @param {number} deviceId - 设备 ID
 * @param {number} page - 当前页码
 * @param {number} pageSize - 每页大小
 * @returns {Promise<{ items: { name: string, value: number, unit: string, health: number }[], totalPages: number }>}
 */
export const getDeviceCards = async (deviceId, page, pageSize) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/devices/${deviceId}/cards`, {
      params: {
        page,
        pageSize,
      },
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching device cards:', error);
    throw error;
  }
};