<template>
  <div>
    <PageHeader title="业务流水" description="按时间、支付方式和操作员追溯每笔充值与消费" />
    <el-card shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="充值流水" name="recharge">
          <QueryPanel>
            <el-input v-model="recharge.filters.keyword" clearable placeholder="会员、卡号或充值单号"
                      class="keyword-input" @keyup.enter="search('recharge')" />
            <el-date-picker v-model="recharge.filters.dates" type="daterange" unlink-panels
                            value-format="YYYY-MM-DD" start-placeholder="开始日期" end-placeholder="结束日期" />
            <el-select v-model="recharge.filters.payWay" clearable placeholder="支付方式" class="short-select">
              <el-option v-for="item in rechargePayOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-select v-model="recharge.filters.operatorId" clearable filterable placeholder="操作员" class="operator-select">
              <el-option v-for="item in operators" :key="item.id" :value="item.id"
                         :label="`${item.realName || item.username}${item.status === 0 ? '（已停用）' : ''}`" />
            </el-select>
            <template #actions>
              <el-button @click="reset('recharge')">重置</el-button>
              <el-button type="primary" @click="search('recharge')">查询</el-button>
              <el-button :loading="recharge.exporting" @click="exportRows('recharge')">导出 CSV</el-button>
            </template>
          </QueryPanel>

          <el-table v-loading="recharge.loading" :data="recharge.rows" stripe empty-text="暂无充值流水">
            <el-table-column prop="recordNo" label="充值单号" min-width="180" />
            <el-table-column label="会员" min-width="150">
              <template #default="{ row }">{{ row.memberName }}<small>{{ row.memberCardNo }}</small></template>
            </el-table-column>
            <el-table-column label="充值金额" width="120"><template #default="{ row }">￥{{ money(row.amount) }}</template></el-table-column>
            <el-table-column label="赠送" width="100"><template #default="{ row }">￥{{ money(row.giftAmount) }}</template></el-table-column>
            <el-table-column label="支付" width="100"><template #default="{ row }">{{ rechargePayWay(row.payWay) }}</template></el-table-column>
            <el-table-column prop="operatorName" label="操作员" width="110" />
            <el-table-column prop="createTime" label="时间" width="175" />
          </el-table>
          <PaginationRow :state="recharge" @change="loadRecharge" />
        </el-tab-pane>

        <el-tab-pane label="消费流水" name="consumption">
          <QueryPanel>
            <el-input v-model="consumption.filters.keyword" clearable placeholder="会员、卡号、账单号或项目"
                      class="keyword-input" @keyup.enter="search('consumption')" />
            <el-date-picker v-model="consumption.filters.dates" type="daterange" unlink-panels
                            value-format="YYYY-MM-DD" start-placeholder="开始日期" end-placeholder="结束日期" />
            <el-select v-model="consumption.filters.payWay" clearable placeholder="支付方式" class="short-select">
              <el-option v-for="item in consumptionPayOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-select v-model="consumption.filters.operatorId" clearable filterable placeholder="操作员" class="operator-select">
              <el-option v-for="item in operators" :key="item.id" :value="item.id"
                         :label="`${item.realName || item.username}${item.status === 0 ? '（已停用）' : ''}`" />
            </el-select>
            <template #actions>
              <el-button @click="reset('consumption')">重置</el-button>
              <el-button type="primary" @click="search('consumption')">查询</el-button>
              <el-button :loading="consumption.exporting" @click="exportRows('consumption')">导出 CSV</el-button>
            </template>
          </QueryPanel>

          <el-table v-loading="consumption.loading" :data="consumption.rows" stripe empty-text="暂无消费流水">
            <el-table-column prop="billNo" label="账单号" min-width="180" />
            <el-table-column label="顾客" min-width="150">
              <template #default="{ row }">{{ row.memberName || '散客' }}<small v-if="row.memberCardNo">{{ row.memberCardNo }}</small></template>
            </el-table-column>
            <el-table-column prop="itemName" label="消费项目" min-width="130" />
            <el-table-column label="金额" width="120"><template #default="{ row }"><strong class="amount">￥{{ money(row.amount) }}</strong></template></el-table-column>
            <el-table-column label="支付" width="100"><template #default="{ row }">{{ consumptionPayWay(row.payWay) }}</template></el-table-column>
            <el-table-column prop="operatorName" label="操作员" width="110" />
            <el-table-column prop="createTime" label="时间" width="175" />
          </el-table>
          <PaginationRow :state="consumption" @change="loadConsumption" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { defineComponent, h, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElPagination } from 'element-plus'
import PageHeader from '../components/PageHeader.vue'
import QueryPanel from '../components/QueryPanel.vue'
import {
  exportConsumptionRecords, exportRechargeRecords, getConsumptionRecords,
  getRechargeRecords, getRecordOperators
} from '../api/record'
import { consumptionPayWay, money, rechargePayWay } from '../utils/formatters'

const pageSize = 10
const activeTab = ref('recharge')
const operators = ref([])
const rechargePayOptions = [1, 2, 3, 4].map(value => ({ value, label: rechargePayWay(value) }))
const consumptionPayOptions = [1, 2, 3].map(value => ({ value, label: consumptionPayWay(value) }))

function createState() {
  return reactive({ page: 1, total: 0, rows: [], loading: false, exporting: false, loaded: false,
    filters: { keyword: '', dates: [], payWay: null, operatorId: null } })
}
const recharge = createState()
const consumption = createState()

const PaginationRow = defineComponent({
  props: { state: { type: Object, required: true } },
  emits: ['change'],
  setup(props, { emit }) {
    return () => h('div', { class: 'pagination-row' }, [h(ElPagination, {
      currentPage: props.state.page, pageSize, layout: 'total, prev, pager, next', total: props.state.total,
      'onUpdate:currentPage': value => { props.state.page = value },
      onCurrentChange: () => emit('change')
    })])
  }
})

onMounted(async () => {
  try { operators.value = (await getRecordOperators()).data } catch (error) { ElMessage.error(error.message) }
  loadRecharge()
})

function params(state, paged = true) {
  const [startDate, endDate] = state.filters.dates || []
  return {
    ...(paged ? { page: state.page, pageSize } : {}), keyword: state.filters.keyword || undefined,
    startDate, endDate, payWay: state.filters.payWay || undefined, operatorId: state.filters.operatorId || undefined
  }
}

async function loadRecharge() { await load(recharge, getRechargeRecords) }
async function loadConsumption() { await load(consumption, getConsumptionRecords) }
async function load(state, api) {
  state.loading = true
  try {
    const response = await api(params(state))
    state.rows = response.data.records
    state.total = response.data.total
    state.loaded = true
  } catch (error) { ElMessage.error(error.message) } finally { state.loading = false }
}

function handleTabChange(name) {
  const state = name === 'recharge' ? recharge : consumption
  if (!state.loaded) (name === 'recharge' ? loadRecharge : loadConsumption)()
}

function search(type) {
  const state = type === 'recharge' ? recharge : consumption
  state.page = 1
  return (type === 'recharge' ? loadRecharge : loadConsumption)()
}

function reset(type) {
  const state = type === 'recharge' ? recharge : consumption
  Object.assign(state.filters, { keyword: '', dates: [], payWay: null, operatorId: null })
  search(type)
}

async function exportRows(type) {
  const state = type === 'recharge' ? recharge : consumption
  state.exporting = true
  try {
    const response = await (type === 'recharge' ? exportRechargeRecords : exportConsumptionRecords)(params(state, false))
    const url = URL.createObjectURL(response.data)
    const link = document.createElement('a')
    link.href = url
    link.download = type === 'recharge' ? '充值流水.csv' : '消费流水.csv'
    link.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出完成')
  } catch (error) { ElMessage.error(error.message) } finally { state.exporting = false }
}
</script>

<style scoped>
.keyword-input { width: 260px; }
.short-select { width: 130px; }
.operator-select { width: 150px; }
.pagination-row { display: flex; justify-content: flex-end; margin-top: 18px; }
small { display: block; color: #87948e; margin-top: 3px; }
.amount { color: #c95643; }
@media (max-width: 640px) {
  .keyword-input, .short-select, .operator-select { width: 100%; }
  :deep(.el-date-editor) { width: 100%; }
  .pagination-row { overflow-x: auto; justify-content: flex-start; }
}
</style>
