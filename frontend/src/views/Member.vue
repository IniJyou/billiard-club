<template>
  <div class="page-stack">
    <el-card shadow="never">
      <div class="toolbar">
        <div class="search-row">
          <el-input v-model="query.keyword" clearable placeholder="姓名 / 手机号 / 卡号"
                    style="width: 280px" @keyup.enter="search" @clear="search" />
          <el-button type="primary" @click="search">查询</el-button>
        </div>
        <el-button type="success" @click="openCreate">新增会员</el-button>
      </div>

      <el-table v-loading="loading" :data="rows" stripe>
        <el-table-column prop="cardNo" label="会员卡号" min-width="150" />
        <el-table-column prop="name" label="姓名" width="110" />
        <el-table-column prop="phone" label="手机号" width="135" />
        <el-table-column label="等级" width="120">
          <template #default="{ row }"><el-tag effect="plain">{{ row.levelName }}</el-tag></template>
        </el-table-column>
        <el-table-column label="余额" width="120">
          <template #default="{ row }">￥{{ money(row.balance) }}</template>
        </el-table-column>
        <el-table-column prop="points" label="积分" width="90" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="230" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="success" :disabled="row.status !== 1" @click="openRecharge(row)">充值</el-button>
            <el-button link :type="row.status === 1 ? 'danger' : 'warning'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-row">
        <el-pagination v-model:current-page="query.page" v-model:page-size="query.pageSize"
                       layout="total, prev, pager, next" :total="total" @current-change="load" />
      </div>
    </el-card>

    <el-dialog v-model="memberDialog" :title="memberForm.id ? '编辑会员' : '新增会员'" width="460px">
      <el-form ref="memberFormRef" :model="memberForm" :rules="memberRules" label-width="85px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="memberForm.name" maxlength="50" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="memberForm.phone" maxlength="11" />
        </el-form-item>
        <el-form-item label="会员等级" prop="levelId">
          <el-select v-model="memberForm.levelId" style="width: 100%">
            <el-option v-for="level in levels" :key="level.id" :label="`${level.name}（${discountText(level.discount)}）`"
                       :value="level.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="memberDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveMember">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rechargeDialog" title="会员充值" width="480px">
      <el-alert :title="`${rechargeMemberRow?.name}　当前余额 ￥${money(rechargeMemberRow?.balance)}`"
                type="info" :closable="false" class="dialog-alert" />
      <el-form ref="rechargeFormRef" :model="rechargeForm" :rules="rechargeRules" label-width="90px">
        <el-form-item label="充值金额" prop="amount">
          <el-input-number v-model="rechargeForm.amount" :min="0.01" :precision="2" :step="50" style="width: 100%" />
        </el-form-item>
        <el-form-item label="赠送金额" prop="giftAmount">
          <el-input-number v-model="rechargeForm.giftAmount" :min="0" :precision="2" :step="10" style="width: 100%" />
        </el-form-item>
        <el-form-item label="支付方式" prop="payWay">
          <el-radio-group v-model="rechargeForm.payWay">
            <el-radio-button :value="1">现金</el-radio-button>
            <el-radio-button :value="2">微信</el-radio-button>
            <el-radio-button :value="3">支付宝</el-radio-button>
            <el-radio-button :value="4">银行卡</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="rechargeForm.remark" maxlength="200" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rechargeDialog = false">取消</el-button>
        <el-button type="success" :loading="saving" @click="submitRecharge">确认充值</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createMember,
  getMemberLevels,
  getMembers,
  rechargeMember,
  updateMember,
  updateMemberStatus
} from '../api/member'

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const levels = ref([])
const total = ref(0)
const query = reactive({ page: 1, pageSize: 10, keyword: '' })

const memberDialog = ref(false)
const memberFormRef = ref()
const memberForm = reactive({ id: null, name: '', phone: '', levelId: null })
const memberRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '请输入正确的11位手机号', trigger: 'blur' }
  ],
  levelId: [{ required: true, message: '请选择会员等级', trigger: 'change' }]
}

const rechargeDialog = ref(false)
const rechargeFormRef = ref()
const rechargeMemberRow = ref(null)
const rechargeForm = reactive({ amount: 100, giftAmount: 0, payWay: 1, remark: '' })
const rechargeRules = {
  amount: [{ required: true, message: '请输入充值金额', trigger: 'change' }],
  payWay: [{ required: true, message: '请选择支付方式', trigger: 'change' }]
}

onMounted(async () => {
  const levelResponse = await getMemberLevels()
  levels.value = levelResponse.data
  await load()
})

async function load() {
  loading.value = true
  try {
    const response = await getMembers(query)
    rows.value = response.data.records
    total.value = response.data.total
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

function search() {
  query.page = 1
  load()
}

function openCreate() {
  Object.assign(memberForm, { id: null, name: '', phone: '', levelId: levels.value[0]?.id })
  memberDialog.value = true
}

function openEdit(row) {
  Object.assign(memberForm, { id: row.id, name: row.name, phone: row.phone, levelId: row.levelId })
  memberDialog.value = true
}

async function saveMember() {
  const valid = await memberFormRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const payload = { name: memberForm.name, phone: memberForm.phone, levelId: memberForm.levelId }
    if (memberForm.id) await updateMember(memberForm.id, payload)
    else await createMember(payload)
    ElMessage.success(memberForm.id ? '会员资料已更新' : '会员建档成功')
    memberDialog.value = false
    await load()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row) {
  const nextStatus = row.status === 1 ? 0 : 1
  await ElMessageBox.confirm(`确定${nextStatus === 1 ? '启用' : '停用'}会员“${row.name}”吗？`, '状态确认')
  try {
    await updateMemberStatus(row.id, nextStatus)
    ElMessage.success('会员状态已更新')
    await load()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

function openRecharge(row) {
  rechargeMemberRow.value = row
  Object.assign(rechargeForm, { amount: 100, giftAmount: 0, payWay: 1, remark: '' })
  rechargeDialog.value = true
}

async function submitRecharge() {
  const valid = await rechargeFormRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const response = await rechargeMember(rechargeMemberRow.value.id, rechargeForm)
    ElMessage.success(`充值成功，新余额 ￥${money(response.data.newBalance)}`)
    rechargeDialog.value = false
    await load()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    saving.value = false
  }
}

function money(value) {
  return Number(value || 0).toFixed(2)
}

function discountText(value) {
  return Number(value) === 1 ? '无折扣' : `${Number(value) * 10}折`
}
</script>

<style scoped>
.page-stack { display: grid; gap: 18px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px; }
.search-row { display: flex; gap: 10px; }
.pagination-row { display: flex; justify-content: flex-end; margin-top: 18px; }
.dialog-alert { margin-bottom: 20px; }
</style>
