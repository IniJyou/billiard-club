<template>
  <el-card shadow="never">
    <template #header><div class="card-head"><div><strong>今天的球桌预约</strong><p>到店后可将待处理预约直接转为开台计费。</p></div><el-button :loading="loading" @click="load">刷新</el-button></div></template>
    <el-table v-loading="loading" :data="rows" stripe>
      <el-table-column prop="reservationNo" label="预约单号" min-width="180" />
      <el-table-column prop="memberName" label="会员" width="110" />
      <el-table-column label="球桌" min-width="130"><template #default="{ row }">{{ row.tableNo }} · {{ row.tableType }}</template></el-table-column>
      <el-table-column label="预约时段" min-width="210"><template #default="{ row }">{{ dateTime(row.startTime) }} 至 {{ timeOnly(row.endTime) }}</template></el-table-column>
      <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag></template></el-table-column>
      <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
      <el-table-column label="操作" width="180" fixed="right"><template #default="{ row }"><template v-if="row.status === 0"><el-button link type="primary" @click="open(row)">转为开台</el-button><el-button link type="danger" @click="cancel(row)">取消</el-button></template><span v-else>--</span></template></el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cancelReservationByStaff, getTodayReservations, openReservation } from '../api/reservation'

const loading = ref(false)
const rows = ref([])
onMounted(load)
async function load() {
  loading.value = true
  try { rows.value = (await getTodayReservations()).data } catch (error) { ElMessage.error(error.message) } finally { loading.value = false }
}
async function open(row) {
  try {
    await ElMessageBox.confirm(`确认会员已到店，并为 ${row.tableNo} 开台吗？`, '预约转开台', { type: 'info' })
    await openReservation(row.id)
    ElMessage.success('已开台并开始计费')
    await load()
  } catch (error) { if (error !== 'cancel' && error !== 'close') ElMessage.error(error.message) }
}
async function cancel(row) {
  try {
    await ElMessageBox.confirm(`确定取消预约单 ${row.reservationNo} 吗？`, '取消预约', { type: 'warning' })
    await cancelReservationByStaff(row.id)
    ElMessage.success('预约已取消')
    await load()
  } catch (error) { if (error !== 'cancel' && error !== 'close') ElMessage.error(error.message) }
}
const dateTime = (value) => value ? value.replace('T', ' ').slice(0, 16) : '--'
const timeOnly = (value) => value ? value.slice(11, 16) : '--'
const statusText = (status) => ['待处理', '已开台', '已完成', '已取消'][status] || '未知'
const statusTag = (status) => ['warning', 'primary', 'success', 'info'][status] || 'info'
</script>

<style scoped>
.card-head { display: flex; align-items: center; justify-content: space-between; }
.card-head p { margin-top: 6px; color: #89948e; font-size: 13px; }
</style>
