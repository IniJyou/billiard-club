import request from './request'

export function getTables() {
  return request.get('/tables')
}

export function updateTableStatus(id, payload) {
  return request.patch(`/tables/${id}/status`, payload)
}

export function openTable(payload) {
  return request.post('/sessions', payload)
}

export function cancelSession(id) {
  return request.post(`/sessions/${id}/cancel`)
}

export function checkoutSession(id, payload) {
  return request.post(`/sessions/${id}/checkout`, payload)
}
