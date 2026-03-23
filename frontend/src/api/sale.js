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
export function getRefundDetails(id) {
  return request.get(`/refund/${id}/details`)
}
export function getOrderDetailsForRefund(saleOrderId) {
  return request.get(`/refund/order/${saleOrderId}/details`)
}
export function createRefund(data) {
  return request.post('/refund/', data)
}
export function createPartialRefund(data) {
  return request.post('/refund/partial', data)
}
export function approveRefund(id, data) {
  return request.put(`/refund/${id}/approve`, data)
}
export function rejectRefund(id, data) {
  return request.put(`/refund/${id}/reject`, data)
}

// ========== 缺货登记 ==========
export function getOutOfStockPage(params) {
  return request.get('/out-of-stock/page', { params })
}
export function createOutOfStock(data) {
  return request.post('/out-of-stock', data)
}
export function startPurchase(id) {
  return request.post(`/out-of-stock/${id}/start-purchase`)
}
export function confirmArrived(id) {
  return request.post(`/out-of-stock/${id}/arrived`)
}
export function notifyMember(id) {
  return request.post(`/out-of-stock/${id}/notify`)
}

// ========== 促销管理 ==========
export function getPromotionPage(params) {
  return request.get('/promotion/page', { params })
}
export function createPromotion(data) {
  return request.post('/promotion', data)
}
export function updatePromotion(id, data) {
  return request.put(`/promotion/${id}`, data)
}
export function activatePromotion(id) {
  return request.post(`/promotion/${id}/activate`)
}
export function pausePromotion(id) {
  return request.post(`/promotion/${id}/pause`)
}
export function endPromotion(id) {
  return request.post(`/promotion/${id}/end`)
}

// ========== 药品组合推荐 ==========
export function getCombinationPage(params) {
  return request.get('/drug-combination/page', { params })
}
export function createCombination(data) {
  return request.post('/drug-combination', data)
}
export function updateCombination(id, data) {
  return request.put(`/drug-combination/${id}`, data)
}
export function deleteCombination(id) {
  return request.delete(`/drug-combination/${id}`)
}
export function enableCombination(id) {
  return request.post(`/drug-combination/${id}/enable`)
}
export function disableCombination(id) {
  return request.post(`/drug-combination/${id}/disable`)
}

// ========== 挂单管理 ==========
export function createSuspendedOrder(data) {
  return request.post('/suspended-order', data)
}
export function getSuspendedOrderList(params) {
  return request.get('/suspended-order/list', { params })
}
export function getSuspendedOrder(id) {
  return request.get(`/suspended-order/${id}`)
}
export function retrieveSuspendedOrder(id) {
  return request.put(`/suspended-order/${id}/retrieve`)
}
export function deleteSuspendedOrder(id) {
  return request.delete(`/suspended-order/${id}`)
}
export function getSuspendedOrderExpireMinutes() {
  return request.get('/suspended-order/config/expire-minutes')
}
export function updateSuspendedOrderExpireMinutes(minutes) {
  return request.put('/suspended-order/config/expire-minutes', { minutes })
}

// ========== 对账单 ==========
export function getReconciliationPreview(params) {
  return request.get('/reconciliation/preview', { params })
}
export function submitReconciliation(data) {
  return request.post('/reconciliation', data)
}
export function getReconciliationPage(params) {
  return request.get('/reconciliation/page', { params })
}
export function getReconciliationDetail(id) {
  return request.get(`/reconciliation/${id}`)
}
export function getProfitReport(params) {
  return request.get('/sale/profit-report', { params })
}
