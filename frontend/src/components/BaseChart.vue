<template>
  <div class="chart-wrap" v-loading="loading">
    <div v-show="!empty" ref="chartRef" class="chart" />
    <el-empty v-if="empty && !loading" description="当前时间范围暂无数据" :image-size="72" />
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
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

function render() {
  if (!chartRef.value || props.empty) return
  chart ||= echarts.init(chartRef.value)
  chart.setOption({ aria: { enabled: true }, ...props.option }, true)
}

function resize() { chart?.resize() }

onMounted(() => {
  render()
  window.addEventListener('resize', resize)
})
watch(() => props.option, render, { deep: true })
watch(() => props.empty, (empty) => { if (!empty) setTimeout(render) })
onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  chart?.dispose()
})
</script>

<style scoped>
.chart-wrap { min-height: 320px; display: grid; place-items: stretch; }
.chart { width: 100%; height: 320px; }
.chart-wrap :deep(.el-empty) { min-height: 300px; }
</style>
