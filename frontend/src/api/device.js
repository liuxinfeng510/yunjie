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
