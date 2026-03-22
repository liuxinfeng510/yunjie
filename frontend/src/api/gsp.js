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
export function getMaintenanceByDrug(drugId) {
  return request.get(`/gsp/maintenance/drug/${drugId}`)
}
export function getMaintenanceList(params) {
  return request.get('/gsp/maintenance/list', { params })
}
export function getTempHumidityLogs(params) {
  return request.get('/gsp/temp-humidity-logs', { params })
}

// ========== 不良品管理 ==========
export function getDefectiveDrugPage(params) {
  return request.get('/defective-drug/page', { params })
}
export function getDefectiveDrugList(params) {
  return request.get('/defective-drug/list', { params })
}
export function registerDefectiveDrug(data) {
  return request.post('/defective-drug', data)
}
export function disposeDefectiveDrug(id, data) {
  return request.post(`/defective-drug/${id}/dispose`, data)
}
export function processDefectiveDrug(id) {
  return request.post(`/defective-drug/${id}/process`)
}
export function completeDefectiveDrug(id) {
  return request.post(`/defective-drug/${id}/complete`)
}
export function getDefectiveStatistics(params) {
  return request.get('/defective-drug/statistics', { params })
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

// ========== 近效期催销 ==========
export function generateNearExpirySale(month) {
  return request.post('/near-expiry-sale/generate', null, { params: { month } })
}
export function getNearExpirySalePage(params) {
  return request.get('/near-expiry-sale/page', { params })
}
export function getNearExpirySaleList(params) {
  return request.get('/near-expiry-sale/list', { params })
}
export function updateNearExpirySaleMeasure(id, data) {
  return request.put(`/near-expiry-sale/${id}/measure`, data)
}
export function completeNearExpirySale(id, data) {
  return request.put(`/near-expiry-sale/${id}/complete`, data)
}

// ========== 养护设备管理 ==========
export function createEquipment(data) {
  return request.post('/maintenance-equipment', data)
}
export function updateEquipment(id, data) {
  return request.put(`/maintenance-equipment/${id}`, data)
}
export function retireEquipment(id) {
  return request.put(`/maintenance-equipment/${id}/retire`)
}
export function getEquipmentPage(params) {
  return request.get('/maintenance-equipment/page', { params })
}
export function getEquipmentList() {
  return request.get('/maintenance-equipment/list')
}
export function createInspection(data) {
  return request.post('/maintenance-equipment/inspection', data)
}
export function getInspectionPage(params) {
  return request.get('/maintenance-equipment/inspection/page', { params })
}
export function getInspectionList(params) {
  return request.get('/maintenance-equipment/inspection/list', { params })
}
