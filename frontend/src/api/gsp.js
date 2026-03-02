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
