import request from '@/utils/request';

/**
 * 获取设备健康数据（七天内）
 * @param {number} deviceId - 设备ID
 * @returns {Promise} - 返回包含健康指数数组的Promise
 */
export function getDeviceHealthData(deviceId) {
  return request({
    url: `/api/monitor/${deviceId}/health`,
    method: 'get'
  });
}

/**
 * 获取设备能耗数据（七天内）
 * @param {number} deviceId - 设备ID
 * @returns {Promise} - 返回包含能耗数据和成本的Promise
 */
export function getDeviceEnergyData(deviceId) {
  return request({
    url: `/api/monitor/${deviceId}/energy`,
    method: 'get'
  });
}

/**
 * 获取设备指标卡片数据（分页）
 * @param {number} deviceId - 设备ID
 * @param {number} page - 页码，从1开始
 * @param {number} pageSize - 每页数量
 * @returns {Promise} - 返回包含指标卡片数据的Promise
 */
export function getDeviceMetricCards(deviceId, page, pageSize) {
  return request({
    url: `/api/monitor/${deviceId}/cards`,
    method: 'get',
    params: {
      page,
      pageSize
    }
  });
}

/**
 * 根据设备ID获取设备基本信息
 * @param {number} deviceId - 设备ID
 * @returns {Promise} - 返回包含设备基本信息的Promise
 */
export function getDeviceById(deviceId) {
  return request({
    url: '/api/components/getdevice',
    method: 'get',
    params: {
      deviceId
    }
  });
}

export default {
  getDeviceHealthData,
  getDeviceEnergyData,
  getDeviceMetricCards,
  getDeviceById
};