import request from '@/utils/request'

export function getHerbPage(params) {
  return request.get('/herb/page', { params })
}
export function getHerb(id) {
  return request.get(`/herb/${id}`)
}
export function createHerb(data) {
  return request.post('/herb', data)
}
export function updateHerb(id, data) {
  return request.put(`/herb/${id}`, data)
}
export function deleteHerb(id) {
  return request.delete(`/herb/${id}`)
}

// 斗柜
export function getCabinetList(storeId) {
  return request.get('/herb-cabinet/list', { params: { storeId } })
}
export function getCabinetCells(id) {
  return request.get(`/herb-cabinet/${id}/cells`)
}
export function createCabinet(data) {
  return request.post('/herb-cabinet', data)
}
export function updateCabinet(id, data) {
  return request.put(`/herb-cabinet/${id}`, data)
}
export function deleteCabinet(id) {
  return request.delete(`/herb-cabinet/${id}`)
}
export function getBoundCabinetIds() {
  return request.get('/herb-cabinet/bound-ids')
}
export function assignCabinetCell(cellId, data) {
  return request.put(`/herb-cabinet/cell/${cellId}/assign`, data)
}
export function refillCabinetCell(cellId, data) {
  return request.put(`/herb-cabinet/cell/${cellId}/refill`, data)
}
export function cleanCabinetCell(cellId) {
  return request.put(`/herb-cabinet/cell/${cellId}/clean`)
}
export function getAssignedHerbIds() {
  return request.get('/herb-cabinet/assigned-herb-ids')
}
export function getHerbDrugList() {
  return request.get('/herb-cabinet/herb-drug-list')
}
export function downloadCabinetImportTemplate(cabinetId) {
  return request.get(`/herb-cabinet/${cabinetId}/import-template`, { responseType: 'blob' })
}
export function batchImportCabinetAssignment(cabinetId, formData) {
  return request.post(`/herb-cabinet/${cabinetId}/batch-import`, formData)
}

// GSP记录
export function createFillRecord(data) {
  return request.post('/herb-gsp/fill-record', data)
}
export function getFillRecords(params) {
  return request.get('/herb-gsp/fill-records', { params })
}
export function createCleanRecord(data) {
  return request.post('/herb-gsp/clean-record', data)
}
export function getCleanRecords(params) {
  return request.get('/herb-gsp/clean-records', { params })
}
export function createAcceptance(data) {
  return request.post('/herb-gsp/acceptance', data)
}
export function getAcceptances(params) {
  return request.get('/herb-gsp/acceptances', { params })
}
export function createMaintenance(data) {
  return request.post('/herb-gsp/maintenance', data)
}
export function getMaintenances(params) {
  return request.get('/herb-gsp/maintenances', { params })
}

// 处方
export function getPrescriptionPage(params) {
  return request.get('/herb-prescription/page', { params })
}
export function getPrescription(id) {
  return request.get(`/herb-prescription/${id}`)
}
export function createPrescription(data) {
  return request.post('/herb-prescription', data)
}
export function updatePrescriptionStatus(id, status) {
  return request.put(`/herb-prescription/${id}/status`, null, { params: { status } })
}

// 获取中药列表
export function getHerbList() {
  return request.get('/herb/list')
}

// ========== 配伍禁忌 ==========
export function getIncompatibilityPage(params) {
  return request.get('/herb-incompatibility/page', { params })
}
export function createIncompatibility(data) {
  return request.post('/herb-incompatibility', data)
}
export function updateIncompatibility(id, data) {
  return request.put(`/herb-incompatibility/${id}`, data)
}
export function deleteIncompatibility(id) {
  return request.delete(`/herb-incompatibility/${id}`)
}
export function checkHerbIncompatibility(herb1Id, herb2Id) {
  return request.get('/herb-incompatibility/check', { params: { herb1Id, herb2Id } })
}

// ========== 装斗记录 ==========
export function getHerbFillLogPage(params) {
  return request.get('/herb-fill-log/page', { params })
}
export function getHerbFillLogList(params) {
  return request.get('/herb-fill-log/list', { params })
}
export function updateHerbFillLogStatus(id, data) {
  return request.put(`/herb-fill-log/${id}/status`, data)
}

// ========== 清斗记录 ==========
export function getHerbCleanLogPage(params) {
  return request.get('/herb-clean-log/page', { params })
}
export function getHerbCleanLogList(params) {
  return request.get('/herb-clean-log/list', { params })
}
export function updateHerbCleanLogStatus(id, data) {
  return request.put(`/herb-clean-log/${id}/status`, data)
}
