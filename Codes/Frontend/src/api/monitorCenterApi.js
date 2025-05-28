import request from '@/utils/request';

//获取设备名字
export function getDeviceName  (deviceId) {
  return request.get('/api/reports/devices/exportDeviceAttributes', null, {
    params: deviceId});
}


//获取设备一周健康数据
export const getDeviceHealthData = (deviceId) => {
  return request.get(`/api/data/monitor/${deviceId}/health`, {
    validateStatus: function (status) {
      return status === 200; // 严格校验HTTP状态码
    }
  });
};


export const getDeviceEnergyData = (deviceId) => {
  return request.get(`/api/monitor/${deviceId}/energy`);
};


export const getDeviceCards = (deviceId, page, pageSize) => {
  return request.get(`/api/monitor/${deviceId}/cards`, {
    params: {
      page,
      pageSize
    }
  });
};