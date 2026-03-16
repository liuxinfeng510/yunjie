import request from '@/utils/request'

export function getMemberPage(params) {
  return request.get('/member/page', { params })
}
export const pageMemberList = getMemberPage // 别名

export function searchMembers(keyword) {
  return request.get('/member/search', { params: { keyword } })
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

// 批量导入
export function parseMemberImport(formData) {
  return request.post('/member/import/parse', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 60000
  })
}
export function executeMemberImport(data) {
  return request.post('/member/import/execute', data, { timeout: 120000 })
}

// 会员等级
export function getMemberLevelPage(params) {
  return request.get('/member-level/page', { params })
}
export function getMemberLevelList() {
  return request.get('/member-level/list')
}
export function getMemberLevel(id) {
  return request.get(`/member-level/${id}`)
}
export function createMemberLevel(data) {
  return request.post('/member-level/', data)
}
export function updateMemberLevel(id, data) {
  return request.put(`/member-level/${id}`, data)
}
export function deleteMemberLevel(id) {
  return request.delete(`/member-level/${id}`)
}
