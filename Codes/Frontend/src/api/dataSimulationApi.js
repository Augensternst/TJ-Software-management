import axios from 'axios';

const API_BASE_URL = 'https://your-api-endpoint.com/api';

/**
 * 获取模型列表（支持分页和模糊搜索）
 * @param {number} page - 当前页码
 * @param {number} pageSize - 每页大小
 * @param {string} searchQuery - 搜索关键字
 * @returns {Promise<{ models: { id: number, name: string }[], total: number }>}
 */
export const getModels = async (page = 1, pageSize = 10, searchQuery = '') => {
  try {
    const response = await axios.get(`${API_BASE_URL}/models`, {
      params: {
        page,
        pageSize,
        searchQuery,
      },
    });
    return {
      models: response.data.models,
      total: response.data.total,
    };
  } catch (error) {
    console.error('Error fetching models:', error);
    throw error;
  }
};

/**
 * 获取设备列表（支持分页和模糊搜索）
 * @param {number} page - 当前页码
 * @param {number} pageSize - 每页大小
 * @param {string} searchQuery - 搜索关键字
 * @returns {Promise<{ devices: { id: number, name: string }[], total: number }>}
 */
export const getDevices = async (page = 1, pageSize = 10, searchQuery = '') => {
  try {
    const response = await axios.get(`${API_BASE_URL}/devices`, {
      params: {
        page,
        pageSize,
        searchQuery,
      },
    });
    return {
      devices: response.data.devices,
      total: response.data.total,
    };
  } catch (error) {
    console.error('Error fetching devices:', error);
    throw error;
  }
};

/**
 * 提交模拟任务
 * @param {number} modelId - 模型 ID
 * @param {number} deviceId - 设备 ID
 * @param {File} file - 上传的文件
 * @returns {Promise<{ taskId: string }>} - 返回任务 ID
 */
export const submitSimulationTask = async (modelId, deviceId, file) => {
  try {
    const formData = new FormData();
    formData.append('modelId', modelId);
    formData.append('deviceId', deviceId);
    formData.append('file', file);

    const response = await axios.post(`${API_BASE_URL}/simulate`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  } catch (error) {
    console.error('Error submitting simulation task:', error);
    throw error;
  }
};

/**
 * 获取模拟结果
 * @param {string} taskId - 任务 ID
 * @returns {Promise<{ imageUrl: string, damageLocation: string, lifespan: number, healthIndex: number }>}
 * healthIndex是健康指数
 */
export const getSimulationResult = async (taskId) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/simulate/${taskId}/result`);
    return response.data;
  } catch (error) {
    console.error('Error fetching simulation result:', error);
    throw error;
  }
};