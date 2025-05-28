import request from '@/utils/request';


//获取未确认的警报数
export function getUnconfirmedAlerts() {
    return request.get('/api/alerts/getUnconfirmedAlerts');
  }


export function exportAlerts() {
  return request.get('/api/alerts/exportAlertsToXLSX', {
    responseType: 'blob', // 必须指定响应类型为blob
    timeout: 30000 // 适当延长超时时间
  });
}
