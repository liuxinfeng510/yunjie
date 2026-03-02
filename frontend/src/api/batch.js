import request from '@/utils/request'

// 追溯码
export function getTraceCodePage(params) {
  return request.get('/trace-code/page', { params })
}

export function queryTraceCode(traceCode) {
  return request.get('/trace-code/query', { params: { traceCode } })
}

export function traceCode(traceCode) {
  return request.get('/trace-code/trace', { params: { traceCode } })
}

export function createTraceCode(data) {
  return request.post('/trace-code', data)
}

export function batchCreateTraceCodes(data) {
  return request.post('/trace-code/batch', data)
}

// 批次
export function getBatchPage(params) {
  return request.get('/drug-batch/page', { params })
}

export function getBatch(id) {
  return request.get(`/drug-batch/${id}`)
}

export function getBatchesByDrug(drugId) {
  return request.get(`/drug-batch/drug/${drugId}`)
}

export function getValidBatches(drugId) {
  return request.get(`/drug-batch/drug/${drugId}/valid`)
}

export function createBatch(data) {
  return request.post('/drug-batch', data)
}

export function updateBatch(id, data) {
  return request.put(`/drug-batch/${id}`, data)
}

export function getNearExpiryBatches(days = 90) {
  return request.get('/drug-batch/near-expiry', { params: { days } })
}

export function getExpiredBatches() {
  return request.get('/drug-batch/expired')
}

// 效期预警
export function getExpiryWarningSummary(storeId) {
  return request.get('/expiry-warning/summary', { params: { storeId } })
}

export function triggerExpiryCheck() {
  return request.post('/expiry-warning/check')
}
