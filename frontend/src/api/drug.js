import request from '@/utils/request'

export function getDrugPage(params) {
  return request.get('/drug/page', { params })
}
export function getDrugList(params) {
  return request.get('/drug/list', { params })
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

// 字典项
export function getDictList(type) {
  return request.get(`/sys-dict/${type}/list`)
}
export function createDictItem(data) {
  return request.post('/sys-dict', data)
}
export function updateDictItem(id, data) {
  return request.put(`/sys-dict/${id}`, data)
}
export function deleteDictItem(id) {
  return request.delete(`/sys-dict/${id}`)
}

// 生产企业
export function searchManufacturer(keyword) {
  return request.get('/drug-manufacturer/search', { params: { keyword } })
}
export function getManufacturerPage(params) {
  return request.get('/drug-manufacturer/page', { params })
}
export function createManufacturer(data) {
  return request.post('/drug-manufacturer', data)
}
export function updateManufacturer(id, data) {
  return request.put(`/drug-manufacturer/${id}`, data)
}
export function deleteManufacturer(id) {
  return request.delete(`/drug-manufacturer/${id}`)
}
export function getOrCreateManufacturer(name) {
  return request.post('/drug-manufacturer/get-or-create', null, { params: { name } })
}

// 条形码
export function getDrugBarcodes(drugId) {
  return request.get(`/drug-barcode/drug/${drugId}`)
}
export function findByBarcode(barcode) {
  return request.get('/drug-barcode/find', { params: { barcode } })
}
export function checkBarcodeDuplicate(barcode, excludeDrugId) {
  return request.get('/drug-barcode/check-duplicate', { params: { barcode, excludeDrugId } })
}
export function deleteBarcode(id) {
  return request.delete(`/drug-barcode/${id}`)
}

// 分类
export function getCategoryTree() {
  return request.get('/drug-category/tree')
}
export function initHerbCategories() {
  return request.post('/drug-category/init-herb')
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
export function searchSupplier(keyword) {
  return request.get('/supplier/search', { params: { keyword } })
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
