import request from '@/utils/request'

export function getSaleOrderPage(params) {
  return request.get('/sale/page', { params })
}
export function getSaleOrder(id) {
  return request.get(`/sale/${id}`)
}
export function createSaleOrder(data) {
  return request.post('/sale', data)
}
export function getDailyStats(params) {
  return request.get('/sale/daily-stats', { params })
}

// 退货
export function getRefundPage(params) {
  return request.get('/refund/page', { params })
}
export function getRefund(id) {
  return request.get(`/refund/${id}`)
}
export function createRefund(data) {
  return request.post('/refund/', data)
}
export function approveRefund(id, data) {
  return request.put(`/refund/${id}/approve`, data)
}
export function rejectRefund(id, data) {
  return request.put(`/refund/${id}/reject`, data)
}
