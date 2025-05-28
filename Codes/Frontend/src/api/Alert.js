import request from '@/utils/request';


//获取未确认的警报数
export function getUnconfirmedAlerts() {
    return request.get('/api/alerts/getUnconfirmedAlerts');
  }


// 导出报表
export function exportAlerts() {
  return request.get('/api/alerts/exportAlertsToXLSX', {
    responseType: 'blob', // 必须指定响应类型为blob
    timeout: 30000 // 适当延长超时时间
  });
}

// 确认警报
export function confirmAlerts(alertIds) {
  return request.post('/api/alerts/confirmAlert', {
    alertIds: alertIds.map(id => Number(id)) // 确保转换为数字
  }, {
    headers: {
      'Content-Type': 'application/json' // 明确指定JSON格式
    }
  })
}
