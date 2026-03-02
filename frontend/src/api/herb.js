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
