<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input v-model="keyword" clearable placeholder="会员、卡号或业务单号"
                style="width: 300px" @keyup.enter="search" @clear="search" />
      <el-button type="primary" @click="search">查询</el-button>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="充值流水" name="recharge">
        <el-table v-loading="loading" :data="recharge.rows" stripe>
          <el-table-column prop="recordNo" label="充值单号" min-width="180" />
          <el-table-column label="会员" min-width="160">
            <template #default="{ row }">{{ row.memberName }}<small>{{ row.memberCardNo }}</small></template>
          </el-table-column>
          <el-table-column label="充值金额" width="120">
            <template #default="{ row }">￥{{ money(row.amount) }}</template>
          </el-table-column>
          <el-table-column label="赠送" width="100">
            <template #default="{ row }">￥{{ money(row.giftAmount) }}</template>
          </el-table-column>
          <el-table-column label="支付" width="100">
            <template #default="{ row }">{{ rechargePayWay(row.payWay) }}</template>
          </el-table-column>
          <el-table-column prop="operatorName" label="操作员" width="110" />
          <el-table-column prop="createTime" label="时间" width="175" />
        </el-table>
        <div class="pagination-row">
          <el-pagination v-model:current-page="recharge.page" :page-size="pageSize"
                         layout="total, prev, pager, next" :total="recharge.total" @current-change="loadRecharge" />
        </div>
      </el-tab-pane>

      <el-tab-pane label="消费流水" name="consumption">
        <el-table v-loading="loading" :data="consumption.rows" stripe>
          <el-table-column prop="billNo" label="账单号" min-width="180" />
          <el-table-column label="顾客" min-width="160">
            <template #default="{ row }">
              {{ row.memberName || '散客' }}<small v-if="row.memberCardNo">{{ row.memberCardNo }}</small>
            </template>
          </el-table-column>
          <el-table-column prop="itemName" label="消费项目" min-width="130" />
          <el-table-column label="金额" width="120">
            <template #default="{ row }"><strong class="amount">￥{{ money(row.amount) }}</strong></template>
          </el-table-column>
          <el-table-column prop="operatorName" label="操作员" width="110" />
          <el-table-column prop="createTime" label="时间" width="175" />
        </el-table>
        <div class="pagination-row">
          <el-pagination v-model:current-page="consumption.page" :page-size="pageSize"
                         layout="total, prev, pager, next" :total="consumption.total"
                         @current-change="loadConsumption" />
        </div>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getConsumptionRecords, getRechargeRecords } from '../api/record'

const activeTab = ref('recharge')
const loading = ref(false)
const keyword = ref('')
const pageSize = 10
const recharge = reactive({ page: 1, total: 0, rows: [] })
const consumption = reactive({ page: 1, total: 0, rows: [] })

onMounted(loadRecharge)

async function loadRecharge() {
  loading.value = true
  try {
    const response = await getRechargeRecords({ page: recharge.page, pageSize, keyword: keyword.value })
    recharge.rows = response.data.records
    recharge.total = response.data.total
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

async function loadConsumption() {
  loading.value = true
  try {
    const response = await getConsumptionRecords({ page: consumption.page, pageSize, keyword: keyword.value })
    consumption.rows = response.data.records
    consumption.total = response.data.total
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  if (activeTab.value === 'recharge') loadRecharge()
  else loadConsumption()
}

function search() {
  if (activeTab.value === 'recharge') {
    recharge.page = 1
    loadRecharge()
  } else {
    consumption.page = 1
    loadConsumption()
  }
}

function rechargePayWay(value) {
  return ['', '现金', '微信', '支付宝', '银行卡'][value] || '未知'
}

function money(value) {
  return Number(value || 0).toFixed(2)
}
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 12px; }
.pagination-row { display: flex; justify-content: flex-end; margin-top: 18px; }
small { display: block; color: #919ba4; margin-top: 3px; }
.amount { color: #d45d45; }
</style>
