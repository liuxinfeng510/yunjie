import request from '@/utils/request'

// 库存
export function getInventoryPage(params) {
  return request.get('/inventory/page', { params })
}
export function getInventoryWarnings(params) {
  return request.get('/inventory/warnings', { params })
}

// 入库
export function getStockInPage(params) {
  return request.get('/stock-in/page', { params })
}
export function getStockIn(id) {
  return request.get(`/stock-in/${id}`)
}
export function createStockIn(data) {
  return request.post('/stock-in', data)
}
export function approveStockIn(id) {
  return request.put(`/stock-in/${id}/approve`)
}
export function completeStockIn(id) {
  return request.put(`/stock-in/${id}/complete`)
}
