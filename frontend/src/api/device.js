import request from '@/utils/request'

// 获取设备列表
export function getDeviceList() {
  return request.get('/device/list')
}

// 连接模拟电子秤
export function connectSimulatedScale(config = {}) {
  return request.post('/device/scale/connect-sim', config)
}

// 连接模拟摄像头
export function connectSimulatedCamera(config = {}) {
  return request.post('/device/camera/connect-sim', config)
}

// 断开设备
export function disconnectDevice(deviceId) {
  return request.post(`/device/disconnect/${deviceId}`)
}

// ========== 电子秤设备管理 ==========
export function getScaleDevicePage(params) {
  return request.get('/scale-device/page', { params })
}
export function createScaleDevice(data) {
  return request.post('/scale-device', data)
}
export function updateScaleDevice(id, data) {
  return request.put(`/scale-device/${id}`, data)
}
export function deleteScaleDevice(id) {
  return request.delete(`/scale-device/${id}`)
}
export function testScaleDevice(id) {
  return request.post(`/scale-device/${id}/test`)
}
export function calibrateScaleDevice(id) {
  return request.post(`/scale-device/${id}/calibrate`)
}
