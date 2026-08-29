import request from './request'

export function login(payload) {
  return request.post('/auth/login', payload)
}

export function getCurrentUser() {
  return request.get('/auth/me')
}

export function logout() {
  return request.post('/auth/logout')
}
