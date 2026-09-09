import { mount } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const mocks = vi.hoisted(() => {
  const chart = { setOption: vi.fn(), resize: vi.fn(), dispose: vi.fn() }
  return { chart, init: vi.fn(() => chart), use: vi.fn() }
})

vi.mock('echarts/core', () => ({ init: mocks.init, use: mocks.use }))
vi.mock('echarts/charts', () => ({ BarChart: {}, LineChart: {}, PieChart: {} }))
vi.mock('echarts/components', () => ({
  AriaComponent: {}, GridComponent: {}, LegendComponent: {}, TooltipComponent: {}
}))
vi.mock('echarts/renderers', () => ({ CanvasRenderer: {} }))

import BaseChart from './BaseChart.vue'

let frameCallbacks
let nextFrameId
let observerCallback
let observe
let disconnect

function mountChart(props = {}) {
  return mount(BaseChart, {
    props: { option: { series: [] }, ...props },
    global: {
      directives: { loading: () => {} },
      stubs: { ElEmpty: true }
    }
  })
}

async function flushFrames() {
  await Promise.resolve()
  const callbacks = frameCallbacks.splice(0)
  callbacks.forEach(({ callback }) => callback())
  await Promise.resolve()
}

describe('BaseChart', () => {
  beforeEach(() => {
    frameCallbacks = []
    nextFrameId = 1
    observerCallback = undefined
    observe = vi.fn()
    disconnect = vi.fn()
    mocks.init.mockClear()
    mocks.chart.setOption.mockClear()
    mocks.chart.resize.mockClear()
    mocks.chart.dispose.mockClear()

    vi.stubGlobal('requestAnimationFrame', vi.fn(callback => {
      const id = nextFrameId++
      frameCallbacks.push({ id, callback })
      return id
    }))
    vi.stubGlobal('cancelAnimationFrame', vi.fn(id => {
      frameCallbacks = frameCallbacks.filter(frame => frame.id !== id)
    }))
    vi.stubGlobal('ResizeObserver', class {
      constructor(callback) { observerCallback = callback }
      observe = observe
      disconnect = disconnect
    })
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('resizes when its own container changes size', async () => {
    const wrapper = mountChart()
    await flushFrames()
    mocks.chart.resize.mockClear()

    observerCallback()
    await flushFrames()

    expect(observe).toHaveBeenCalledWith(wrapper.find('.chart').element)
    expect(mocks.chart.resize).toHaveBeenCalledOnce()
    wrapper.unmount()
  })

  it('renders after changing from empty to populated', async () => {
    const wrapper = mountChart({ empty: true })
    await flushFrames()
    expect(mocks.init).not.toHaveBeenCalled()

    await wrapper.setProps({ empty: false })
    await flushFrames()

    expect(mocks.init).toHaveBeenCalledWith(wrapper.find('.chart').element)
    expect(mocks.chart.setOption).toHaveBeenCalledOnce()
    wrapper.unmount()
  })

  it('disconnects observers and disposes the chart on unmount', async () => {
    const wrapper = mountChart()
    await flushFrames()

    wrapper.unmount()

    expect(disconnect).toHaveBeenCalledOnce()
    expect(mocks.chart.dispose).toHaveBeenCalledOnce()
  })
})
