<template>
  <div>
    <PageHeader title="经营报表" description="消费、充值与球桌使用情况按统一口径汇总">
      <template #actions>
        <el-button-group>
          <el-button v-for="preset in presets" :key="preset.key" :type="activePreset === preset.key ? 'primary' : ''"
                     @click="applyPreset(preset.key)">{{ preset.label }}</el-button>
        </el-button-group>
      </template>
    </PageHeader>

    <QueryPanel>
      <el-date-picker v-model="dates" type="daterange" unlink-panels value-format="YYYY-MM-DD"
                      start-placeholder="开始日期" end-placeholder="结束日期" @change="activePreset = 'custom'" />
      <template #actions><el-button type="primary" :loading="loading" @click="loadReport">生成报表</el-button></template>
    </QueryPanel>

    <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" class="error-alert">
      <template #default><el-button link type="primary" @click="loadReport">重新加载</el-button></template>
    </el-alert>

    <div class="metric-grid">
      <MetricCard label="消费收入" :value="`￥${money(summary.consumptionRevenue)}`" hint="按已结账账单实收金额" />
      <MetricCard label="充值实收" :value="`￥${money(summary.rechargePrincipal)}`" hint="不含充值赠送金额" tone="blue" />
      <MetricCard label="结账单数" :value="summary.orderCount || 0" :hint="`优惠 ￥${money(summary.discountAmount)}`" tone="gold" />
      <MetricCard label="球桌利用率" :value="`${money(summary.tableUtilizationRate)}%`" hint="按实际占用分钟计算" tone="coral" />
    </div>

    <div class="chart-grid">
      <el-card shadow="never" class="chart-card chart-wide">
        <template #header><ChartTitle title="每日经营趋势" note="消费收入与充值实收分别统计，避免重复计算" /></template>
        <BaseChart :option="trendOption" :loading="loading" :empty="trendEmpty" />
      </el-card>
      <el-card shadow="never" class="chart-card">
        <template #header><ChartTitle title="消费支付方式" note="按已结账金额汇总" /></template>
        <BaseChart :option="payOption" :loading="loading" :empty="payEmpty" />
      </el-card>
      <el-card shadow="never" class="chart-card">
        <template #header><ChartTitle title="顾客类型" note="会员与散客结账单数" /></template>
        <BaseChart :option="customerOption" :loading="loading" :empty="customerEmpty" />
      </el-card>
      <el-card shadow="never" class="chart-card chart-wide">
        <template #header><ChartTitle title="球桌使用时长" note="维护历史未记录，利用率分母按全部球桌计算" /></template>
        <BaseChart :option="tableOption" :loading="loading" :empty="tableEmpty" />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed, defineComponent, h, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import BaseChart from '../components/BaseChart.vue'
import MetricCard from '../components/MetricCard.vue'
import PageHeader from '../components/PageHeader.vue'
import QueryPanel from '../components/QueryPanel.vue'
import { getReportOverview } from '../api/report'
import { money } from '../utils/formatters'

const ChartTitle = defineComponent({
  props: { title: String, note: String },
  setup(props) { return () => h('div', { class: 'chart-title' }, [h('strong', props.title), h('span', props.note)]) }
})
const presets = [
  { key: 'today', label: '今天' }, { key: '7days', label: '近7天' },
  { key: '30days', label: '近30天' }, { key: 'month', label: '本月' }
]
const activePreset = ref('month')
const dates = ref(monthRange())
const loading = ref(false)
const error = ref('')
const data = ref({ dailyTrend: [], consumptionPayBreakdown: [], customerBreakdown: [], tableUsage: [], summary: {} })
const summary = computed(() => data.value.summary || {})

function formatDate(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}
function rangeFromDays(days) {
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - days + 1)
  return [formatDate(start), formatDate(end)]
}
function monthRange() {
  const now = new Date()
  return [formatDate(new Date(now.getFullYear(), now.getMonth(), 1)), formatDate(now)]
}
function applyPreset(key) {
  activePreset.value = key
  dates.value = key === 'today' ? rangeFromDays(1) : key === '7days' ? rangeFromDays(7)
    : key === '30days' ? rangeFromDays(30) : monthRange()
  loadReport()
}

async function loadReport() {
  if (!dates.value?.length) return ElMessage.warning('请选择报表日期范围')
  loading.value = true
  error.value = ''
  try {
    const response = await getReportOverview({ startDate: dates.value[0], endDate: dates.value[1] })
    data.value = response.data
  } catch (err) { error.value = err.message } finally { loading.value = false }
}

const trendEmpty = computed(() => !data.value.dailyTrend.some(item => Number(item.consumptionRevenue) || Number(item.rechargePrincipal)))
const payEmpty = computed(() => !data.value.consumptionPayBreakdown.length)
const customerEmpty = computed(() => !data.value.customerBreakdown.some(item => item.count))
const tableEmpty = computed(() => !data.value.tableUsage.some(item => item.usedMinutes))
const commonTooltip = { trigger: 'axis', backgroundColor: '#173b2f', borderWidth: 0, textStyle: { color: '#fff' } }

const trendOption = computed(() => ({
  tooltip: { ...commonTooltip, valueFormatter: value => `￥${money(value)}` },
  legend: { bottom: 0 }, grid: { left: 20, right: 20, top: 20, bottom: 48, containLabel: true },
  xAxis: { type: 'category', data: data.value.dailyTrend.map(item => item.date.slice(5)), axisLine: { lineStyle: { color: '#dce5e0' } } },
  yAxis: { type: 'value', axisLabel: { formatter: value => `￥${value}` }, splitLine: { lineStyle: { color: '#edf1ef' } } },
  series: [
    { name: '消费收入', type: 'line', smooth: true, symbolSize: 7, data: data.value.dailyTrend.map(item => item.consumptionRevenue), lineStyle: { width: 3, color: '#16865f' }, itemStyle: { color: '#16865f' }, areaStyle: { color: 'rgba(22,134,95,.10)' } },
    { name: '充值实收', type: 'line', smooth: true, symbolSize: 7, data: data.value.dailyTrend.map(item => item.rechargePrincipal), lineStyle: { width: 3, color: '#3f7cac' }, itemStyle: { color: '#3f7cac' } }
  ]
}))
const payOption = computed(() => pieOption(data.value.consumptionPayBreakdown.map(item => ({ name: item.name, value: item.amount })), ['#16865f', '#3f7cac', '#d79a32']))
const customerOption = computed(() => pieOption(data.value.customerBreakdown.map(item => ({ name: item.name, value: item.count })), ['#16865f', '#d9a441']))
function pieOption(seriesData, colors) {
  return { color: colors, tooltip: { trigger: 'item' }, legend: { bottom: 0 },
    series: [{ type: 'pie', radius: ['46%', '70%'], center: ['50%', '44%'], avoidLabelOverlap: true,
      label: { formatter: '{b}\n{d}%' }, data: seriesData }] }
}
const tableOption = computed(() => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, formatter: items => `${items[0].name}<br/>使用 ${items[0].value} 分钟` },
  grid: { left: 24, right: 30, top: 12, bottom: 22, containLabel: true },
  xAxis: { type: 'value', name: '分钟', splitLine: { lineStyle: { color: '#edf1ef' } } },
  yAxis: { type: 'category', data: data.value.tableUsage.map(item => item.tableNo) },
  series: [{ type: 'bar', data: data.value.tableUsage.map(item => item.usedMinutes), barMaxWidth: 26,
    itemStyle: { color: '#16865f', borderRadius: [0, 5, 5, 0] } }]
}))

onMounted(loadReport)
</script>

<style scoped>
.error-alert { margin-bottom: 16px; }
.metric-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 16px; margin-bottom: 16px; }
.chart-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; }
.chart-wide { grid-column: 1 / -1; }
.chart-card { border-color: #e4e9e6; }
:deep(.chart-title) { display: flex; align-items: baseline; justify-content: space-between; gap: 12px; }
:deep(.chart-title strong) { color: #203b31; font-size: 16px; }
:deep(.chart-title span) { color: #8a958f; font-size: 12px; }
@media (max-width: 1000px) { .metric-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 720px) {
  .metric-grid, .chart-grid { grid-template-columns: 1fr; }
  .chart-wide { grid-column: auto; }
  :deep(.chart-title) { align-items: flex-start; flex-direction: column; }
}
</style>
