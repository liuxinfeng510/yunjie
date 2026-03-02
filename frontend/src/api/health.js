import request from '@/utils/request'

// 会员健康画像API

/**
 * 获取会员健康画像
 */
export function getHealthProfile(memberId) {
  return request({
    url: `/member-health/profile/${memberId}`,
    method: 'get'
  })
}

/**
 * 更新健康画像
 */
export function updateHealthProfile(data) {
  return request({
    url: '/member-health/profile',
    method: 'put',
    data
  })
}

/**
 * 获取健康分析
 */
export function getHealthAnalysis(memberId) {
  return request({
    url: `/member-health/analysis/${memberId}`,
    method: 'get'
  })
}

/**
 * 获取用药建议
 */
export function getMedicationAdvice(memberId) {
  return request({
    url: `/member-health/advice/${memberId}`,
    method: 'get'
  })
}

// 慢病管理API

/**
 * 创建慢病记录
 */
export function createDiseaseRecord(data) {
  return request({
    url: '/chronic-disease',
    method: 'post',
    data
  })
}

/**
 * 更新慢病记录
 */
export function updateDiseaseRecord(data) {
  return request({
    url: '/chronic-disease',
    method: 'put',
    data
  })
}

/**
 * 获取会员慢病列表
 */
export function getMemberDiseases(memberId) {
  return request({
    url: `/chronic-disease/member/${memberId}`,
    method: 'get'
  })
}

/**
 * 分页查询慢病记录
 */
export function pageDiseaseRecords(params) {
  return request({
    url: '/chronic-disease/page',
    method: 'get',
    params
  })
}

/**
 * 获取待复查列表
 */
export function getPendingFollowUps(days = 7) {
  return request({
    url: '/chronic-disease/pending-followups',
    method: 'get',
    params: { days }
  })
}

/**
 * 记录检查结果
 */
export function recordCheckResult(recordId, data) {
  return request({
    url: `/chronic-disease/${recordId}/check-result`,
    method: 'post',
    data
  })
}

/**
 * 获取慢病统计
 */
export function getDiseaseStatistics() {
  return request({
    url: '/chronic-disease/statistics',
    method: 'get'
  })
}

/**
 * 为慢病创建用药提醒
 */
export function createDiseaseReminder(recordId, data) {
  return request({
    url: `/chronic-disease/${recordId}/reminder`,
    method: 'post',
    data
  })
}

// 用药提醒API

/**
 * 创建用药提醒
 */
export function createReminder(data) {
  return request({
    url: '/medication-reminder',
    method: 'post',
    data
  })
}

/**
 * 更新用药提醒
 */
export function updateReminder(data) {
  return request({
    url: '/medication-reminder',
    method: 'put',
    data
  })
}

/**
 * 暂停提醒
 */
export function pauseReminder(id) {
  return request({
    url: `/medication-reminder/${id}/pause`,
    method: 'post'
  })
}

/**
 * 恢复提醒
 */
export function resumeReminder(id) {
  return request({
    url: `/medication-reminder/${id}/resume`,
    method: 'post'
  })
}

/**
 * 完成提醒
 */
export function completeReminder(id) {
  return request({
    url: `/medication-reminder/${id}/complete`,
    method: 'post'
  })
}

/**
 * 获取会员的用药提醒
 */
export function getMemberReminders(memberId, status) {
  return request({
    url: `/medication-reminder/member/${memberId}`,
    method: 'get',
    params: { status }
  })
}

/**
 * 分页查询提醒
 */
export function pageReminders(params) {
  return request({
    url: '/medication-reminder/page',
    method: 'get',
    params
  })
}

/**
 * 获取提醒统计
 */
export function getReminderStatistics() {
  return request({
    url: '/medication-reminder/statistics',
    method: 'get'
  })
}
