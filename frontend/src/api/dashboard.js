import request from '@/utils/request'

export function getDashboardOverview(params) {
  return request.get('/dashboard/overview', { params })
}

export function getSalesTrend(params) {
  return request.get('/dashboard/sales-trend', { params })
}

export function getDashboardTodos(params) {
  return request.get('/dashboard/todos', { params })
}

export function getExpiringDrugs(params) {
  return request.get('/dashboard/expiring-drugs', { params })
}

export function getTopDrugs(params) {
  return request.get('/dashboard/top-drugs', { params })
}
