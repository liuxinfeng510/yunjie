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

// 出库
export function getStockOutPage(params) {
  return request.get('/stock-out/page', { params })
}
export function getStockOut(id) {
  return request.get(`/stock-out/${id}`)
}
export function createStockOut(data) {
  return request.post('/stock-out/', data)
}
export function approveStockOut(id) {
  return request.put(`/stock-out/${id}/approve`)
}
export function completeStockOut(id) {
  return request.put(`/stock-out/${id}/complete`)
}

// 盘点
export function getStockCheckPage(params) {
  return request.get('/stock-check/page', { params })
}
export function getStockCheck(id) {
  return request.get(`/stock-check/${id}`)
}
export function createStockCheck(data) {
  return request.post('/stock-check/', data)
}
export function updateStockCheckDetail(detailId, actualQuantity, diffReason) {
  return request.put(`/stock-check/detail/${detailId}`, null, {
    params: { actualQuantity, diffReason }
  })
}
export function completeStockCheck(id) {
  return request.put(`/stock-check/${id}/complete`)
}
