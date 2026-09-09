import request from './request'

export function getReportOverview(params) {
  return request.get('/reports/overview', { params })
}
