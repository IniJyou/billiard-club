<template>
  <div class="table-page">
    <div class="table-toolbar">
      <div>
        <h2>球桌实时看板</h2>
        <p>结账采用整小时进位，不足一小时按一小时计算。</p>
      </div>
      <el-button :loading="loading" @click="load">刷新状态</el-button>
    </div>

    <div v-loading="loading" class="table-grid">
      <el-card v-for="table in tables" :key="table.id" shadow="hover" class="table-card"
               :class="statusClass(table.status)">
        <div class="card-head">
          <div>
            <strong>{{ table.tableNo }}</strong>
            <span>{{ table.tableType }}</span>
          </div>
          <el-tag :type="statusTag(table.status)">{{ statusText(table.status) }}</el-tag>
        </div>
        <div class="price">￥{{ money(table.pricePerHour) }} <small>/ 小时</small></div>

        <div v-if="table.status === 1" class="session-info">
          <div><span>顾客</span><strong>{{ table.memberName || '散客' }}</strong></div>
          <div><span>开台时间</span><strong>{{ table.startTime }}</strong></div>
          <div><span>已使用</span><strong>{{ elapsed(table.startTime) }}</strong></div>
        </div>
        <div v-else-if="table.status === 2" class="empty-info">
          {{ table.remark || '球桌维护中，暂不可开台' }}
        </div>
        <div v-else class="empty-info">当前空闲，可为会员或散客开台</div>

        <div class="card-actions">
          <template v-if="table.status === 0">
            <el-button type="primary" @click="showOpen(table)">开台</el-button>
            <el-button v-if="auth.isAdmin" @click="setMaintenance(table)">设为维护</el-button>
          </template>
          <template v-else-if="table.status === 1">
            <el-button type="success" @click="showCheckout(table)">结账</el-button>
            <el-button type="danger" plain @click="cancel(table)">取消开台</el-button>
          </template>
          <el-button v-else-if="auth.isAdmin" type="warning" @click="restore(table)">恢复空闲</el-button>
        </div>
      </el-card>
    </div>

    <el-dialog v-model="openDialog" :title="`${selectedTable?.tableNo} 开台`" width="460px">
      <el-alert title="不选择会员即按散客开台，散客只能使用现金结账。" type="info"
                :closable="false" class="dialog-alert" />
      <el-form label-width="85px">
        <el-form-item label="球桌">
          <el-input :model-value="`${selectedTable?.tableNo} / ${selectedTable?.tableType}`" disabled />
        </el-form-item>
        <el-form-item label="绑定会员">
          <el-select v-model="openForm.memberId" filterable style="width: 100%">
            <el-option label="散客（不绑定会员）" :value="0" />
            <el-option v-for="member in members" :key="member.id"
                       :label="`${member.name} / ${member.cardNo} / 余额￥${money(member.balance)}`"
                       :value="member.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="openDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitOpen">确认开台</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="checkoutDialog" :title="`${selectedTable?.tableNo} 结账`" width="480px">
      <div class="checkout-summary">
        <div><span>顾客</span><strong>{{ selectedTable?.memberName || '散客' }}</strong></div>
        <div><span>开台时间</span><strong>{{ selectedTable?.startTime }}</strong></div>
        <div><span>当前时长</span><strong>{{ elapsed(selectedTable?.startTime) }}</strong></div>
      </div>
      <el-form label-width="90px">
        <el-form-item label="支付方式">
          <el-radio-group v-model="checkoutForm.payWay">
            <el-radio-button :value="1">现金</el-radio-button>
            <el-radio-button :value="2" :disabled="!selectedTable?.memberId">会员余额</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="checkoutDialog = false">取消</el-button>
        <el-button type="success" :loading="saving" @click="submitCheckout">确认结账</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMembers } from '../api/member'
import { cancelSession, checkoutSession, getTables, openTable, updateTableStatus } from '../api/table'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const loading = ref(false)
const saving = ref(false)
const tables = ref([])
const members = ref([])
const clock = ref(Date.now())
let timer

const selectedTable = ref(null)
const openDialog = ref(false)
const openForm = reactive({ memberId: 0 })
const checkoutDialog = ref(false)
const checkoutForm = reactive({ payWay: 1 })

onMounted(async () => {
  timer = window.setInterval(() => { clock.value = Date.now() }, 30000)
  await Promise.all([load(), loadMembers()])
})
onUnmounted(() => window.clearInterval(timer))

async function load() {
  loading.value = true
  try {
    const response = await getTables()
    tables.value = response.data
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

async function loadMembers() {
  const response = await getMembers({ page: 1, pageSize: 100 })
  members.value = response.data.records.filter((member) => member.status === 1)
}

function showOpen(table) {
  selectedTable.value = table
  openForm.memberId = 0
  openDialog.value = true
}

async function submitOpen() {
  saving.value = true
  try {
    await openTable({ tableId: selectedTable.value.id, memberId: openForm.memberId || null })
    ElMessage.success('开台成功，已经开始计时')
    openDialog.value = false
    await load()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    saving.value = false
  }
}

function showCheckout(table) {
  selectedTable.value = table
  checkoutForm.payWay = 1
  checkoutDialog.value = true
}

async function submitCheckout() {
  saving.value = true
  try {
    const response = await checkoutSession(selectedTable.value.activeSessionId, checkoutForm)
    const bill = response.data.bill
    checkoutDialog.value = false
    await ElMessageBox.alert(
      `计费 ${bill.durationHours} 小时，原价 ￥${money(bill.originalAmount)}，优惠 ￥${money(bill.discountAmount)}，实收 ￥${money(bill.finalAmount)}`,
      `结账成功 · ${bill.billNo}`,
      { confirmButtonText: '完成', type: 'success' }
    )
    await Promise.all([load(), loadMembers()])
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    saving.value = false
  }
}

async function cancel(table) {
  await ElMessageBox.confirm(`取消 ${table.tableNo} 的本次开台吗？取消后不会生成账单。`, '取消开台', { type: 'warning' })
  try {
    await cancelSession(table.activeSessionId)
    ElMessage.success('本次开台已取消')
    await load()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

async function setMaintenance(table) {
  try {
    const { value } = await ElMessageBox.prompt('请输入维护原因（可留空）', `设置 ${table.tableNo} 为维护`, {
      inputValue: table.remark || ''
    })
    await updateTableStatus(table.id, { status: 2, remark: value })
    ElMessage.success('球桌已进入维护状态')
    await load()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(error.message)
  }
}

async function restore(table) {
  try {
    await updateTableStatus(table.id, { status: 0, remark: '' })
    ElMessage.success('球桌已恢复空闲')
    await load()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

function elapsed(startTime) {
  clock.value
  if (!startTime) return '--'
  const milliseconds = Math.max(0, Date.now() - new Date(startTime.replace(' ', 'T')).getTime())
  const totalMinutes = Math.floor(milliseconds / 60000)
  return `${Math.floor(totalMinutes / 60)}小时 ${totalMinutes % 60}分钟`
}

function statusText(status) {
  return ['空闲', '使用中', '维护中'][status] || '未知'
}
function statusTag(status) {
  return status === 0 ? 'success' : status === 1 ? 'danger' : 'warning'
}
function statusClass(status) {
  return status === 0 ? 'is-idle' : status === 1 ? 'is-busy' : 'is-maintenance'
}
function money(value) {
  return Number(value || 0).toFixed(2)
}
</script>

<style scoped>
.table-page { display: grid; gap: 20px; }
.table-toolbar { display: flex; align-items: center; justify-content: space-between; }
.table-toolbar h2 { margin-bottom: 7px; color: #26333d; }
.table-toolbar p { color: #7e8993; }
.table-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 18px; min-height: 180px; }
.table-card { border-top: 4px solid #67c23a; }
.table-card.is-busy { border-top-color: #f56c6c; }
.table-card.is-maintenance { border-top-color: #e6a23c; }
.card-head { display: flex; justify-content: space-between; align-items: flex-start; }
.card-head strong { display: block; font-size: 26px; color: #26333d; }
.card-head span { display: block; color: #8a949d; margin-top: 4px; }
.price { margin: 20px 0; font-size: 24px; font-weight: 700; color: #25765a; }
.price small { font-size: 13px; color: #929ba3; font-weight: 400; }
.session-info { display: grid; gap: 9px; min-height: 92px; }
.session-info div, .checkout-summary div { display: flex; justify-content: space-between; gap: 15px; }
.session-info span, .checkout-summary span { color: #8a949d; }
.empty-info { min-height: 92px; color: #8a949d; line-height: 1.7; }
.card-actions { display: flex; margin-top: 18px; }
.dialog-alert { margin-bottom: 20px; }
.checkout-summary { display: grid; gap: 12px; padding: 16px; margin-bottom: 20px; background: #f6f8fa; border-radius: 8px; }
</style>
