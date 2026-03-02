import request from '@/utils/request'

// ========== 系统配置 ==========

/** 获取所有配置（按分组） */
export function getAllConfig() {
  return request.get('/sys/config')
}

/** 按分组获取配置 */
export function getConfigByGroup(group) {
  return request.get(`/sys/config/group/${group}`)
}

/** 获取单个配置值 */
export function getConfigValue(key) {
  return request.get(`/sys/config/key/${key}`)
}

/** 设置单个配置 */
export function setConfigValue(data) {
  return request.post('/sys/config', null, { params: data })
}

/** 批量更新配置 */
export function batchUpdateConfig(configs) {
  return request.put('/sys/config/batch', configs)
}

// ========== 快速配置 ==========

/** 获取初始化配置状态 */
export function getSetupStatus() {
  return request.get('/setup/status')
}

/** 执行快速配置 */
export function quickSetup(data) {
  return request.post('/setup', data)
}

/** 获取功能开关 */
export function getFeatures() {
  return request.get('/setup/features')
}

/** 更新功能开关 */
export function updateFeatures(features) {
  return request.put('/setup/features', features)
}

// ========== 门店管理 ==========

/** 分页查询门店 */
export function getStorePage(params) {
  return request.get('/store/page', { params })
}

/** 获取所有门店列表 */
export function getStoreList() {
  return request.get('/store/list')
}

/** 获取门店详情 */
export function getStore(id) {
  return request.get(`/store/${id}`)
}

/** 创建门店 */
export function createStore(data) {
  return request.post('/store', data)
}

/** 更新门店 */
export function updateStore(id, data) {
  return request.put(`/store/${id}`, data)
}

/** 删除门店 */
export function deleteStore(id) {
  return request.delete(`/store/${id}`)
}

// ========== 数据迁移 ==========

/** 分页查询迁移任务 */
export function getMigrationPage(params) {
  return request.get('/migration/page', { params })
}

/** 获取迁移任务详情 */
export function getMigrationTask(id) {
  return request.get(`/migration/${id}`)
}

/** 上传文件创建迁移任务 */
export function uploadMigrationFile(formData) {
  return request.post('/migration/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/** 执行迁移任务 */
export function executeMigration(id) {
  return request.post(`/migration/${id}/execute`)
}

/** 删除迁移任务 */
export function deleteMigration(id) {
  return request.delete(`/migration/${id}`)
}
