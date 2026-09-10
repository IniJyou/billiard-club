import request from './request'

export function getUserProfile() {
  return request.get('/user/profile')
}

export function getMembershipOptions() {
  return request.get('/user/membership-options')
}

export function updateUserProfile(payload) {
  return request.put('/user/profile', payload)
}

export function applyMembership(payload) {
  return request.post('/user/membership', payload)
}

export function onlineRecharge(payload) {
  return request.post('/user/recharges', payload)
}

export function getMyRechargeRecords() {
  return request.get('/user/records/recharges')
}

export function getMyConsumptionRecords() {
  return request.get('/user/records/consumptions')
}
