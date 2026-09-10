import request from './request'

export function createReservation(payload) {
  return request.post('/reservations', payload)
}

export function getMyReservations() {
  return request.get('/reservations/mine')
}

export function cancelMyReservation(id) {
  return request.post(`/reservations/${id}/cancel-mine`)
}

export function getTodayReservations() {
  return request.get('/reservations/today')
}

export function openReservation(id) {
  return request.post(`/reservations/${id}/open`)
}

export function cancelReservationByStaff(id) {
  return request.post(`/reservations/${id}/cancel`)
}
