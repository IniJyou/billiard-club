<template>
  <div class="page-stack">
    <el-alert title="预约仅限今天，请选择晚于当前时间的时段；每次可预约1至4小时。" type="info" :closable="false" />
    <el-card shadow="never">
      <template #header><div class="card-head"><strong>今日球桌状态</strong><span class="subtle">预约时段对所有登录用户公开</span></div></template>
      <div v-loading="loading" class="availability-grid">
        <div v-for="table in tables" :key="table.id" class="availability-card" :class="`status-${table.status}`">
          <div class="availability-head">
            <div><strong>{{ table.tableNo }}</strong><small>{{ table.tableType }}</small></div>
            <el-tag :type="tableStatusTag(table.status)">{{ tableStatusText(table.status) }}</el-tag>
          </div>
          <div v-if="table.status === 2" class="table-note">{{ table.remark || '维护中，暂不可预约' }}</div>
          <div v-else-if="table.todayReservations?.length" class="reservation-ranges">
            <span>今日已预约</span>
            <el-tag v-for="(reservation, index) in table.todayReservations" :key="index" type="warning" effect="plain">
              {{ reservationTimeRange(reservation) }}
            </el-tag>
          </div>
          <div v-else class="table-note">今天暂无预约</div>
        </div>
      </div>
    </el-card>
    <el-card shadow="never">
      <template #header><strong>预约球桌</strong></template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" class="reservation-form">
        <el-form-item label="选择球桌" prop="tableId">
          <el-select v-model="form.tableId" placeholder="请选择空闲球桌" style="width: 100%" @change="form.startTime = ''">
            <el-option v-for="table in tables" :key="table.id" :value="table.id" :disabled="table.status !== 0" :label="`${table.tableNo} · ${table.tableType} · ${tableStatusText(table.status)} · ￥${money(table.pricePerHour)}/小时`" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <div class="time-picker">
            <div v-if="!selectedTable" class="time-placeholder">请先选择球桌</div>
            <template v-else>
              <div class="time-grid">
                <el-tooltip v-for="slot in startSlots" :key="slot.value" :content="slot.reason" :disabled="!slot.disabled">
                  <button type="button" class="time-slot" :class="{ selected: form.startTime === slot.value }" :disabled="slot.disabled" @click="form.startTime = slot.value">{{ slot.label }}</button>
                </el-tooltip>
              </div>
              <div class="time-legend">灰色时段表示已过、已有预约，或按当前时长无法预约。</div>
            </template>
          </div>
        </el-form-item>
        <el-form-item label="预约时长" prop="durationHours"><el-radio-group v-model="form.durationHours" @change="ensureSelectedTimeAvailable"><el-radio-button v-for="hour in 4" :key="hour" :value="hour">{{ hour }}小时</el-radio-button></el-radio-group></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" maxlength="200" show-word-limit /></el-form-item>
        <el-form-item><el-button type="primary" :loading="saving" @click="submit">提交预约</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card shadow="never">
      <template #header><div class="card-head"><strong>我的预约</strong><el-button :loading="loading" @click="load">刷新</el-button></div></template>
      <el-table v-loading="loading" :data="reservations" stripe>
        <el-table-column prop="reservationNo" label="预约单号" min-width="180" />
        <el-table-column label="球桌" min-width="130"><template #default="{ row }">{{ row.tableNo }} · {{ row.tableType }}</template></el-table-column>
        <el-table-column label="预约时段" min-width="210"><template #default="{ row }">{{ dateTime(row.startTime) }} 至 {{ timeOnly(row.endTime) }}</template></el-table-column>
        <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag></template></el-table-column>
        <el-table-column prop="remark" label="备注" min-width="130" show-overflow-tooltip />
        <el-table-column label="操作" width="100"><template #default="{ row }"><el-button v-if="row.status === 0" link type="danger" @click="cancel(row)">取消</el-button><span v-else>--</span></template></el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTables } from '../api/table'
import { cancelMyReservation, createReservation, getMyReservations } from '../api/reservation'
import { buildTodayStartSlots, reservationTimeRange } from '../utils/reservationSlots'

const loading = ref(false)
const saving = ref(false)
const formRef = ref()
const tables = ref([])
const reservations = ref([])
const form = reactive({ tableId: null, startTime: '', durationHours: 1, remark: '' })
const rules = { tableId: [{ required: true, message: '请选择球桌', trigger: 'change' }], startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }] }
const selectedTable = computed(() => tables.value.find((table) => table.id === form.tableId))
const startSlots = computed(() => buildTodayStartSlots(selectedTable.value, form.durationHours))

onMounted(load)
async function load() {
  loading.value = true
  try {
    const [tableResponse, reservationResponse] = await Promise.all([getTables(), getMyReservations()])
    tables.value = tableResponse.data
    reservations.value = reservationResponse.data
    ensureSelectedTimeAvailable()
  } catch (error) { ElMessage.error(error.message) } finally { loading.value = false }
}
async function submit() {
  await formRef.value.validate()
  saving.value = true
  try {
    await createReservation(form)
    ElMessage.success('预约提交成功，请按预约时间到店')
    form.startTime = ''; form.remark = ''
    await load()
  } catch (error) { ElMessage.error(error.message) } finally { saving.value = false }
}
function ensureSelectedTimeAvailable() {
  if (form.startTime && startSlots.value.find((slot) => slot.value === form.startTime)?.disabled) form.startTime = ''
}
async function cancel(row) {
  try {
    await ElMessageBox.confirm(`确定取消 ${row.tableNo} 的本次预约吗？`, '取消预约', { type: 'warning' })
    await cancelMyReservation(row.id)
    ElMessage.success('预约已取消')
    await load()
  } catch (error) { if (error !== 'cancel' && error !== 'close') ElMessage.error(error.message) }
}
const money = (value) => Number(value || 0).toFixed(2)
const dateTime = (value) => value ? value.replace('T', ' ').slice(0, 16) : '--'
const timeOnly = (value) => value ? value.slice(11, 16) : '--'
const statusText = (status) => ['待处理', '已开台', '已完成', '已取消'][status] || '未知'
const statusTag = (status) => ['warning', 'primary', 'success', 'info'][status] || 'info'
const tableStatusText = (status) => ['空闲', '使用中', '维修中'][status] || '未知'
const tableStatusTag = (status) => ['success', 'danger', 'warning'][status] || 'info'
</script>

<style scoped>
.page-stack { display: grid; gap: 18px; }
.reservation-form { max-width: 650px; }
.card-head { display: flex; align-items: center; justify-content: space-between; }
.subtle, .table-note, .time-legend { color: #89948e; font-size: 13px; }
.availability-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(235px, 1fr)); gap: 14px; min-height: 80px; }
.availability-card { padding: 15px; border: 1px solid #e4e9e6; border-top: 4px solid #67c23a; border-radius: 9px; }
.availability-card.status-1 { border-top-color: #f56c6c; }
.availability-card.status-2 { border-top-color: #e6a23c; }
.availability-head { display: flex; justify-content: space-between; gap: 12px; margin-bottom: 13px; }
.availability-head strong, .availability-head small { display: block; }
.availability-head strong { font-size: 20px; }
.availability-head small { margin-top: 3px; color: #89948e; }
.reservation-ranges { display: flex; align-items: center; flex-wrap: wrap; gap: 7px; }
.reservation-ranges > span { width: 100%; color: #89948e; font-size: 13px; }
.time-picker { width: 100%; }
.time-placeholder { padding: 13px; color: #89948e; background: #f5f7f6; border-radius: 7px; }
.time-grid { display: grid; grid-template-columns: repeat(8, minmax(52px, 1fr)); gap: 7px; }
.time-slot { padding: 8px 4px; color: #356b58; background: #f1f8f5; border: 1px solid #b9d6cb; border-radius: 6px; cursor: pointer; }
.time-slot:hover:not(:disabled), .time-slot.selected { color: #fff; background: #14865f; border-color: #14865f; }
.time-slot:disabled { color: #a8afb0; background: #ecefee; border-color: #e1e5e3; cursor: not-allowed; }
.time-legend { margin-top: 9px; }
@media (max-width: 760px) { .time-grid { grid-template-columns: repeat(4, minmax(52px, 1fr)); } }
</style>
