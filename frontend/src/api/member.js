import request from './request'

export function getMembers(params) {
  return request.get('/members', { params })
}

export function createMember(payload) {
  return request.post('/members', payload)
}

export function updateMember(id, payload) {
  return request.put(`/members/${id}`, payload)
}

export function updateMemberStatus(id, status) {
  return request.patch(`/members/${id}/status`, { status })
}

export function rechargeMember(id, payload) {
  return request.post(`/members/${id}/recharges`, payload)
}

export function getMemberLevels() {
  return request.get('/member-levels')
}
