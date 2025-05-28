import request from '@/utils/request';

/**
 * 获取用户今日警报统计
 * @returns {Promise} 包含今日已确认和未确认警报数量的Promise
 */
export function getTodayAlertStats() {
  return request({
    url: '/api/reports/alerts/getTodayAlertStats',
    method: 'get'
  });
}

/**
 * 获取用户所有警报统计（全局）
 * @returns {Promise} 包含总警报数、已确认和未确认警报数量的Promise
 */
export function getAllAlertStats() {
  return request({
    url: '/api/reports/alerts/getAllAlertStats',
    method: 'get'
  });
}

/**
 * 获取本周警报统计（按天分组）
 * @returns {Promise} 包含本周警报统计数据的Promise
 */
export function getWeeklyAlertStats() {
  return request({
    url: '/api/reports/alerts/getWeeklyAlertStats',
    method: 'get'
  });
}

/**
 * 获取设备列表
 * @returns {Promise} 包含设备列表的Promise
 */
export function getDeviceList() {
  return request({
    url: '/api/components/user/devices',
    method: 'get'
  });
}

/**
 * 获取设备指定属性值(8个)
 * @param {number} deviceId - 设备ID
 * @returns {Promise} 包含设备属性值的Promise
 */
export function getDeviceAttributes(deviceId) {
  return request({
    url: '/api/reports/devices/getDeviceAttributes',
    method: 'get',
    params: { deviceId }
  });
}

/**
 * 将设备属性值导出为excel文件
 * @param {number} deviceId - 设备ID
 * @returns {Promise} 包含文件数据的Promise
 */
export function exportDeviceAttributes(deviceId) {
  return request({
    url: '/api/reports/devices/exportDeviceAttributes',
    method: 'get',
    params: { deviceId },
    responseType: 'blob' // 指定响应类型为blob，用于文件下载
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
  getTodayAlertStats,
  getAllAlertStats,
  getWeeklyAlertStats,
  getDeviceList,
  getDeviceAttributes,
  exportDeviceAttributes,
  getDeviceById
};