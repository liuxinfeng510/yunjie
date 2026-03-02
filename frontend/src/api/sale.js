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
