import request from '@/utils/request'

export function getMemberPage(params) {
  return request.get('/member/page', { params })
}
export function getMember(id) {
  return request.get(`/member/${id}`)
}
export function createMember(data) {
  return request.post('/member', data)
}
export function updateMember(id, data) {
  return request.put(`/member/${id}`, data)
}
export function getMemberPointsLog(id, params) {
  return request.get(`/member/${id}/points-log`, { params })
}
export function addPoints(id, data) {
  return request.post(`/member/${id}/add-points`, data)
}
export function deductPoints(id, data) {
  return request.post(`/member/${id}/deduct-points`, data)
}
