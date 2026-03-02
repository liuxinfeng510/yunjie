import request from '@/utils/request'

// 电子签名API

/**
 * 创建电子签名
 */
export function createSignature(data) {
  return request({
    url: '/signature',
    method: 'post',
    data
  })
}

/**
 * 验证签名
 */
export function verifySignature(id) {
  return request({
    url: `/signature/${id}/verify`,
    method: 'get'
  })
}

/**
 * 获取业务签名
 */
export function getBusinessSignatures(businessType, businessId) {
  return request({
    url: '/signature/business',
    method: 'get',
    params: { businessType, businessId }
  })
}

// GSP自动化API

/**
 * 执行合规检查
 */
export function executeGspCheck(data) {
  return request({
    url: '/gsp-automation/check',
    method: 'post',
    data
  })
}

/**
 * 分页查询检查记录
 */
export function pageGspChecks(params) {
  return request({
    url: '/gsp-automation/checks',
    method: 'get',
    params
  })
}

/**
 * 获取合规统计
 */
export function getGspStatistics(params) {
  return request({
    url: '/gsp-automation/statistics',
    method: 'get',
    params
  })
}

/**
 * 更新整改状态
 */
export function updateCorrectionStatus(id, data) {
  return request({
    url: `/gsp-automation/checks/${id}/correction`,
    method: 'post',
    data
  })
}
