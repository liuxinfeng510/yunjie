import request from '@/utils/request'

export function createAcceptance(data) {
  return request.post('/gsp/acceptance', data)
}
export function getAcceptances(params) {
  return request.get('/gsp/acceptances', { params })
}
export function createMaintenance(data) {
  return request.post('/gsp/maintenance', data)
}
export function getMaintenances(params) {
  return request.get('/gsp/maintenances', { params })
}
export function getTempHumidityLogs(params) {
  return request.get('/gsp/temp-humidity-logs', { params })
}

// ========== 不良品管理 ==========
export function getDefectiveDrugPage(params) {
  return request.get('/defective-drug/page', { params })
}
export function registerDefectiveDrug(data) {
  return request.post('/defective-drug', data)
}
export function processDefectiveDrug(id) {
  return request.post(`/defective-drug/${id}/process`)
}
export function completeDefectiveDrug(id) {
  return request.post(`/defective-drug/${id}/complete`)
}

// ========== 药品销毁 ==========
export function getDestructionPage(params) {
  return request.get('/drug-destruction/page', { params })
}
export function createDestructionApplication(data) {
  return request.post('/drug-destruction', data)
}
export function approveDestruction(id) {
  return request.post(`/drug-destruction/${id}/approve`)
}
export function rejectDestruction(id) {
  return request.post(`/drug-destruction/${id}/reject`)
}
export function executeDestruction(id) {
  return request.post(`/drug-destruction/${id}/execute`)
}

// ========== 员工培训 ==========
export function getTrainingPage(params) {
  return request.get('/staff-training/page', { params })
}
export function createTraining(data) {
  return request.post('/staff-training', data)
}
export function updateTraining(id, data) {
  return request.put(`/staff-training/${id}`, data)
}

// ========== GSP报表 ==========
export function exportAcceptanceReport(params) {
  return request.get('/gsp-report/acceptance', { params })
}
export function exportMaintenanceReport(params) {
  return request.get('/gsp-report/maintenance', { params })
}
export function exportTemperatureReport(params) {
  return request.get('/gsp-report/temperature', { params })
}
export function exportTrainingReport(params) {
  return request.get('/gsp-report/training', { params })
}
