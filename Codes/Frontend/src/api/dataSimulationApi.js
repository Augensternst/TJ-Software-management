import request from '@/utils/request';

/**
 * 获取模型列表
 * @param {number} page - 当前页码
 * @param {number} pageSize - 每页数量
 * @param {string} [searchQuery] - 搜索关键词(可选)
 * @returns {Promise} 包含模型列表的Promise
 */
export function getModels(page, pageSize, searchQuery) {
  return request({
    url: '/api/model/getModels',
    method: 'get',
    params: {
      page,
      pageSize,
      searchQuery
    }
  });
}

/**
 * 获取设备列表
 * @param {number} page - 当前页码
 * @param {number} pageSize - 每页数量
 * @param {string} [searchQuery] - 搜索关键词(可选)
 * @returns {Promise} 包含设备列表的Promise
 */
export function getDevices(page, pageSize, searchQuery) {
  return request({
    url: '/api/devices', // 使用设备列表API
    method: 'post',
    data: {  // 改用data而不是params，将参数放在请求体中
      page,
      pageSize,
      searchQuery
    }
  });
}

/**
 * 获取模拟结果
 * @param {number} modelId - 所选模型ID
 * @param {number} deviceId - 所选设备ID
 * @param {File} file - 上传的数据文件
 * @returns {Promise} 包含模拟结果的Promise
 */
export function getSimulationResult(modelId, deviceId, file) {
  // 创建FormData对象用于文件上传
  const formData = new FormData();
  formData.append('modelId', modelId);
  formData.append('deviceId', deviceId);
  formData.append('file', file);

  return request({
    url: '/api/simulation/getSimulationResult',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

export default {
  getModels,
  getDevices,
  getSimulationResult
};