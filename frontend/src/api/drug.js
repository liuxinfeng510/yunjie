import request from '@/utils/request'

export function getDrugPage(params) {
  return request.get('/drug/page', { params })
}
export function getDrug(id) {
  return request.get(`/drug/${id}`)
}
export function createDrug(data) {
  return request.post('/drug', data)
}
export function updateDrug(id, data) {
  return request.put(`/drug/${id}`, data)
}
export function deleteDrug(id) {
  return request.delete(`/drug/${id}`)
}

// 分类
export function getCategoryTree() {
  return request.get('/drug-category/tree')
}
export function createCategory(data) {
  return request.post('/drug-category', data)
}
export function updateCategory(id, data) {
  return request.put(`/drug-category/${id}`, data)
}
export function deleteCategory(id) {
  return request.delete(`/drug-category/${id}`)
}

// 供应商
export function getSupplierPage(params) {
  return request.get('/supplier/page', { params })
}
export function getSupplier(id) {
  return request.get(`/supplier/${id}`)
}
export function createSupplier(data) {
  return request.post('/supplier', data)
}
export function updateSupplier(id, data) {
  return request.put(`/supplier/${id}`, data)
}
export function deleteSupplier(id) {
  return request.delete(`/supplier/${id}`)
}
