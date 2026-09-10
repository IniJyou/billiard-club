<template>
  <el-card shadow="never">
    <el-tabs v-model="activeTab" @tab-change="load">
      <el-tab-pane label="充值记录" name="recharge">
        <el-table v-loading="loading" :data="recharges" stripe>
          <el-table-column prop="recordNo" label="充值单号" min-width="190" />
          <el-table-column label="充值金额" width="130"><template #default="{ row }"><strong class="income">+￥{{ money(row.amount) }}</strong></template></el-table-column>
          <el-table-column label="支付方式" width="120"><template #default="{ row }">{{ payWayText(row.payWay) }}</template></el-table-column>
          <el-table-column label="充值时间" min-width="170"><template #default="{ row }">{{ dateTime(row.createTime) }}</template></el-table-column>
          <el-table-column prop="remark" label="备注" min-width="150" />
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="消费记录" name="consumption">
        <el-table v-loading="loading" :data="consumptions" stripe>
          <el-table-column prop="billNo" label="账单号" min-width="190" />
          <el-table-column prop="itemName" label="消费项目" min-width="150" />
          <el-table-column label="消费金额" width="130"><template #default="{ row }"><strong class="expense">-￥{{ money(row.amount) }}</strong></template></el-table-column>
          <el-table-column label="支付方式" width="120"><template #default="{ row }">{{ payWayText(row.payWay) }}</template></el-table-column>
          <el-table-column label="消费时间" min-width="170"><template #default="{ row }">{{ dateTime(row.createTime) }}</template></el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyConsumptionRecords, getMyRechargeRecords } from '../api/user'

const activeTab = ref('recharge')
const loading = ref(false)
const recharges = ref([])
const consumptions = ref([])
onMounted(load)
async function load() {
  loading.value = true
  try {
    if (activeTab.value === 'recharge') recharges.value = (await getMyRechargeRecords()).data
    else consumptions.value = (await getMyConsumptionRecords()).data
  } catch (error) { ElMessage.error(error.message) } finally { loading.value = false }
}
const money = (value) => Number(value || 0).toFixed(2)
const dateTime = (value) => value ? value.replace('T', ' ').slice(0, 19) : '--'
const payWayText = (value) => ({ 1: '现金', 2: '微信支付', 3: '支付宝', 4: '银行卡' }[value] || '未知')
</script>

<style scoped>
.income { color: #16865f; }
.expense { color: #d05b52; }
</style>
