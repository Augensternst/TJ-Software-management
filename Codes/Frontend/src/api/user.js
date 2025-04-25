// src/api/user.js
import request from '@/utils/request';

// 登录
export function loginUser(username, password) {
  return request.post('/api/user/account/token/', null, {
    params: { username, password }
  });
}

// 注册
export function registerUser(data) {
  return request.post('/api/user/account/register/', null, {
    params: data
  });
}

// 登录信息
export function getUserInfo(token) {
  return request.get('/api/user/account/info/',null,{params:token});
}

// 用户设备总数
export function getUserDeviceCount() {
  return request.get('/api/devices/count');
}

// 获取用户设备列表
export function getUserDevices() {
  return request({
    url: '/api/devices',
    method: 'post',
    data: {
      page: 1,
      pageSize: 10
    }
  });
}

// 获取用户设备状态统计
export function getUserDeviceStatusSummary() {
  return request.get('/api/devices/status-summary');
}

// 获取用户缺陷设备信息
export function getUserDefectiveDevices() {
  return request.get('/api/devices/defective');
}

// 获取用户预警状态分布
export function getAlertStatusSummary() {
  return request.get('/api/alerts/status-summary');
}



