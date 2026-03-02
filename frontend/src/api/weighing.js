import request from '@/utils/request'

// 智能称重
export function smartWeighing(data) {
  return request.post('/smart-weighing/process', data)
}

// 配伍检查
export function checkCompatibility(herbs) {
  return request.post('/smart-weighing/check-compatibility', { herbs })
}

// 配伍禁忌检查
export function checkIncompatibility(herbs) {
  return request.post('/herb-incompatibility/check', { herbs })
}

// 检查新药材配伍
export function checkNewHerbIncompatibility(newHerb, existingHerbs) {
  return request.post('/herb-incompatibility/check-new', { newHerb, existingHerbs })
}

// 获取十八反
export function get18Oppose() {
  return request.get('/herb-incompatibility/18-oppose')
}

// 获取十九畏
export function get19Fear() {
  return request.get('/herb-incompatibility/19-fear')
}

// 电子秤设备列表
export function getScaleDevices(storeId) {
  return request.get('/scale/device/list', { params: { storeId } })
}

// 在线电子秤
export function getOnlineScales(storeId) {
  return request.get('/scale/device/online', { params: { storeId } })
}

// 称重记录
export function getWeighingLogs(params) {
  return request.get('/scale/log/page', { params })
}
