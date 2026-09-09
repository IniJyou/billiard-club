import request from './request'

export function getRechargeRecords(params) {
  return request.get('/records/recharges', { params })
}

export function getConsumptionRecords(params) {
  return request.get('/records/consumptions', { params })
}

export function getRecordOperators() {
  return request.get('/records/operators')
}

export function exportRechargeRecords(params) {
  return request.get('/records/recharges/export', { params, responseType: 'blob' })
}

export function exportConsumptionRecords(params) {
  return request.get('/records/consumptions/export', { params, responseType: 'blob' })
}
