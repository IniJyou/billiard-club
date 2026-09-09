import { flushPromises, shallowMount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import ElementPlus from 'element-plus'

const mocks = vi.hoisted(() => ({
  getRechargeRecords: vi.fn(), getConsumptionRecords: vi.fn(), getRecordOperators: vi.fn(),
  exportRechargeRecords: vi.fn(), exportConsumptionRecords: vi.fn()
}))
vi.mock('../api/record', () => mocks)

import Record from './Record.vue'

function mountRecord() {
  return shallowMount(Record, { global: { plugins: [ElementPlus], stubs: { 'el-table-column': true } } })
}

describe('Record view', () => {
  beforeEach(() => {
    mocks.getRecordOperators.mockResolvedValue({ data: [{ id: 1, username: 'admin', realName: '管理员', status: 1 }] })
    mocks.getRechargeRecords.mockResolvedValue({ data: { records: [], total: 0 } })
    mocks.getConsumptionRecords.mockResolvedValue({ data: { records: [], total: 0 } })
    mocks.exportRechargeRecords.mockResolvedValue({ data: new Blob(['csv']) })
    vi.stubGlobal('URL', { createObjectURL: vi.fn(() => 'blob:test'), revokeObjectURL: vi.fn() })
    vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => {})
  })

  it('loads operators and the recharge tab independently', async () => {
    mountRecord()
    await flushPromises()
    expect(mocks.getRecordOperators).toHaveBeenCalledOnce()
    expect(mocks.getRechargeRecords).toHaveBeenCalledWith(expect.objectContaining({ page: 1, pageSize: 10 }))
    expect(mocks.getConsumptionRecords).not.toHaveBeenCalled()
  })

  it('exports the active recharge filters as a CSV download', async () => {
    const wrapper = mountRecord()
    await flushPromises()
    await wrapper.vm.exportRows('recharge')
    await flushPromises()
    expect(mocks.exportRechargeRecords).toHaveBeenCalledWith(expect.not.objectContaining({ page: expect.anything() }))
    expect(URL.createObjectURL).toHaveBeenCalled()
  })
})
