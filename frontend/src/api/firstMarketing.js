import request from '@/utils/request'

// ========== 首营企业 ==========
export function getFirstMarketingSupplierPage(params) {
  return request.get('/first-marketing/supplier/page', { params })
}
export function getFirstMarketingSupplier(id) {
  return request.get(`/first-marketing/supplier/${id}`)
}
export function createFirstMarketingSupplier(data) {
  return request.post('/first-marketing/supplier', data)
}
export function updateFirstMarketingSupplier(id, data) {
  return request.put(`/first-marketing/supplier/${id}`, data)
}
export function deleteFirstMarketingSupplier(id) {
  return request.delete(`/first-marketing/supplier/${id}`)
}
export function submitFirstMarketingSupplier(id) {
  return request.post(`/first-marketing/supplier/${id}/submit`)
}
export function approveFirstMarketingSupplierFirst(id, data) {
  return request.post(`/first-marketing/supplier/${id}/approve-first`, data)
}
export function approveFirstMarketingSupplierSecond(id, data) {
  return request.post(`/first-marketing/supplier/${id}/approve-second`, data)
}
export function getApprovalLevel() {
  return request.get('/first-marketing/supplier/approval-level')
}

// ========== 首营品种 ==========
export function getFirstMarketingDrugPage(params) {
  return request.get('/first-marketing/drug/page', { params })
}
export function getFirstMarketingDrug(id) {
  return request.get(`/first-marketing/drug/${id}`)
}
export function createFirstMarketingDrug(data) {
  return request.post('/first-marketing/drug', data)
}
export function updateFirstMarketingDrug(id, data) {
  return request.put(`/first-marketing/drug/${id}`, data)
}
export function deleteFirstMarketingDrug(id) {
  return request.delete(`/first-marketing/drug/${id}`)
}
export function submitFirstMarketingDrug(id) {
  return request.post(`/first-marketing/drug/${id}/submit`)
}
export function approveFirstMarketingDrugFirst(id, data) {
  return request.post(`/first-marketing/drug/${id}/approve-first`, data)
}
export function approveFirstMarketingDrugSecond(id, data) {
  return request.post(`/first-marketing/drug/${id}/approve-second`, data)
}

// ========== 文件上传 ==========
export function uploadFile(formData) {
  return request.post('/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// ========== AI识别 ==========
export function aiRecognizeSupplierDocs(data) {
  return request.post('/ai/first-marketing/recognize', data, { timeout: 120000 })
}
