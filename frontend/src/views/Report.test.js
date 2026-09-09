import { flushPromises, shallowMount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import ElementPlus from 'element-plus'

const mocks = vi.hoisted(() => ({ getReportOverview: vi.fn() }))
vi.mock('../api/report', () => ({ getReportOverview: mocks.getReportOverview }))

import Report from './Report.vue'

const emptyReport = {
  summary: { consumptionRevenue: 0, rechargePrincipal: 0, discountAmount: 0, orderCount: 0, tableUtilizationRate: 0 },
  dailyTrend: [], consumptionPayBreakdown: [], customerBreakdown: [], tableUsage: []
}

describe('Report view', () => {
  it('loads the current-month overview and exposes empty chart states', async () => {
    mocks.getReportOverview.mockResolvedValue({ data: emptyReport })
    const wrapper = shallowMount(Report, { global: { plugins: [ElementPlus] } })
    await flushPromises()
    expect(mocks.getReportOverview).toHaveBeenCalledOnce()
    expect([wrapper.vm.trendEmpty, wrapper.vm.payEmpty, wrapper.vm.customerEmpty, wrapper.vm.tableEmpty])
      .toEqual([true, true, true, true])
  })

  it('shows an API failure without discarding the page', async () => {
    mocks.getReportOverview.mockRejectedValue(new Error('报表加载失败'))
    const wrapper = shallowMount(Report, { global: { plugins: [ElementPlus] } })
    await flushPromises()
    expect(wrapper.findComponent({ name: 'ElAlert' }).props('title')).toBe('报表加载失败')
  })
})
