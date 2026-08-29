import request from './request'

export function getRechargeRecords(params) {
  return request.get('/records/recharges', { params })
}

export function getConsumptionRecords(params) {
  return request.get('/records/consumptions', { params })
}
