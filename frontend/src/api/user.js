import request from '@/utils/request'

// 员工/用户管理
export function getUserPage(params) {
  return request.get('/user/page', { params })
}

export function getUserList(params) {
  return request.get('/user/list', { params })
}

export function getUser(id) {
  return request.get(`/user/${id}`)
}

export function createUser(data) {
  return request.post('/user', data)
}

export function updateUser(id, data) {
  return request.put(`/user/${id}`, data)
}

export function deleteUser(id) {
  return request.delete(`/user/${id}`)
}

export function resetPassword(id) {
  return request.put(`/user/${id}/reset-password`)
}

export function updateUserStatus(id, status) {
  return request.put(`/user/${id}/status`, { status })
}

// 角色管理
export function getRoleList() {
  return request.get('/role/list')
}

export function getRole(id) {
  return request.get(`/role/${id}`)
}

export function createRole(data) {
  return request.post('/role', data)
}

export function updateRole(id, data) {
  return request.put(`/role/${id}`, data)
}

export function deleteRole(id) {
  return request.delete(`/role/${id}`)
}

export function getRolePermissions(id) {
  return request.get(`/role/${id}/permissions`)
}

export function updateRolePermissions(id, permissions) {
  return request.put(`/role/${id}/permissions`, { permissions })
}
