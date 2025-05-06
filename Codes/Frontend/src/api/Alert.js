import request from '@/utils/request';

// 获取未确认警报列表（添加筛选参数）
export function getUnconfirmedAlerts(params) {
  return request({
    url: '/api/alerts/getUnconfirmedAlerts',
    method: 'post',
    data: params || {
      page: 1,
      pageSize: 10
    }
  });
}

// 导出报表（添加筛选参数）
export function exportAlerts(params) {
  return request({
    url: '/api/alerts/exportAlertsToXLSX',
    method: 'get',
    params: params, // 添加筛选参数
    responseType: 'blob',
    timeout: 30000
  });
}

// 确认警报
export function confirmAlerts(alertIds) {
  return request.put('/api/alerts/confirmAlert', {
    alertIds: alertIds.map(id => Number(id))
  }, {
    headers: {
      'Content-Type': 'application/json'
    }
  });
}