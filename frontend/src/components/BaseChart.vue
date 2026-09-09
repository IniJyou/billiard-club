<template>
  <div class="chart-wrap" v-loading="loading">
    <div v-show="!empty" ref="chartRef" class="chart" />
    <el-empty v-if="empty && !loading" description="当前时间范围暂无数据" :image-size="72" />
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts/core'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import { AriaComponent, GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([BarChart, LineChart, PieChart, AriaComponent, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer])

const props = defineProps({
  option: { type: Object, required: true },
  loading: { type: Boolean, default: false },
  empty: { type: Boolean, default: false }
})
const chartRef = ref()
let chart
let resizeObserver
let resizeFrame

const requestFrame = callback => window.requestAnimationFrame
  ? window.requestAnimationFrame(callback)
  : window.setTimeout(callback, 0)
const cancelFrame = frame => window.cancelAnimationFrame
  ? window.cancelAnimationFrame(frame)
  : window.clearTimeout(frame)

function render() {
  if (!chartRef.value || props.empty) return
  chart ||= echarts.init(chartRef.value)
  chart.setOption({ aria: { enabled: true }, ...props.option }, true)
  scheduleResize()
}

function resize() { chart?.resize() }

function scheduleResize() {
  if (resizeFrame != null) cancelFrame(resizeFrame)
  resizeFrame = requestFrame(() => {
    resizeFrame = null
    resize()
  })
}

onMounted(() => {
  nextTick(render)
  window.addEventListener('resize', scheduleResize)
  if (typeof ResizeObserver !== 'undefined') {
    resizeObserver = new ResizeObserver(scheduleResize)
    resizeObserver.observe(chartRef.value)
  }
})
watch(() => props.option, () => nextTick(render), { deep: true })
watch(() => props.empty, (empty) => { if (!empty) nextTick(render) })
onBeforeUnmount(() => {
  window.removeEventListener('resize', scheduleResize)
  resizeObserver?.disconnect()
  if (resizeFrame != null) cancelFrame(resizeFrame)
  chart?.dispose()
})
</script>

<style scoped>
.chart-wrap { min-height: 320px; display: grid; place-items: stretch; }
.chart { width: 100%; height: 320px; }
.chart-wrap :deep(.el-empty) { min-height: 300px; }
</style>
