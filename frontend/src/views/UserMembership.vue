<template>
  <div class="page-stack" v-loading="loading">
    <el-card v-if="profile.hasMember" shadow="never" class="member-card" :class="cardClass(profile.levelId)">
      <div class="card-top">
        <div class="card-label">我的会员卡</div>
        <span class="card-type">{{ profile.levelName || '会员卡' }}</span>
      </div>
      <div class="card-number">{{ profile.cardNo }}</div>
      <div class="card-details">
        <div><span>持卡人</span><strong>{{ profile.realName }}</strong></div>
        <div><span>当前余额</span><strong>￥{{ money(profile.balance) }}</strong></div>
        <div><span>消费折扣</span><strong>{{ discountText(profile.discount) }}</strong></div>
      </div>
      <el-button type="success" size="large" @click="rechargeDialog = true">在线充值</el-button>
    </el-card>
    <el-card v-else shadow="never">
      <el-result icon="info" title="在线办理会员" sub-title="办理完成后会立即生成会员卡号，可使用在线充值和当天预约功能。">
        <template #extra><el-button type="primary" size="large" @click="memberDialog = true">开始办理</el-button></template>
      </el-result>
    </el-card>

    <el-dialog v-model="memberDialog" title="确认会员资料" width="580px">
      <el-alert title="手机号使用当前登录账号，办理后不可自行更换。请选择适合自己的会员卡类别。" type="info" :closable="false" class="dialog-alert" />
      <el-form ref="memberFormRef" :model="memberForm" :rules="memberRules" label-width="95px">
        <el-form-item label="登录手机号"><el-input :model-value="profile.phone" disabled /></el-form-item>
        <el-form-item label="会员姓名" prop="name"><el-input v-model="memberForm.name" maxlength="50" /></el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="memberForm.gender"><el-radio :value="1">男</el-radio><el-radio :value="2">女</el-radio><el-radio :value="0">保密</el-radio></el-radio-group>
        </el-form-item>
        <el-form-item label="出生日期" prop="birthday">
          <el-date-picker v-model="memberForm.birthday" type="date" value-format="YYYY-MM-DD" format="YYYY-MM-DD" :disabled-date="disabledBirthday" placeholder="请选择出生日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="会员卡类别" prop="levelId">
          <div class="level-options">
            <button v-for="option in options" :key="option.id" type="button" class="level-option"
                    :class="[{ selected: memberForm.levelId === option.id }, cardClass(option.id)]"
                    @click="memberForm.levelId = option.id">
              <strong>{{ option.name }}</strong><span>{{ discountText(option.discount) }}</span>
            </button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="memberDialog = false">取消</el-button><el-button type="primary" :loading="saving" @click="submitMembership">确认办理</el-button></template>
    </el-dialog>

    <el-dialog v-model="rechargeDialog" title="在线充值（模拟支付）" width="460px">
      <el-alert title="课程演示环境不会发起真实扣款，确认后余额将直接到账。" type="info" :closable="false" class="dialog-alert" />
      <el-form ref="rechargeFormRef" :model="rechargeForm" :rules="rechargeRules" label-width="90px">
        <el-form-item label="充值金额" prop="amount"><el-input-number v-model="rechargeForm.amount" :min="0.01" :max="99999.99" :precision="2" :step="50" style="width: 100%" /></el-form-item>
        <el-form-item label="支付方式" prop="payWay"><el-radio-group v-model="rechargeForm.payWay"><el-radio-button :value="2">微信支付</el-radio-button><el-radio-button :value="3">支付宝</el-radio-button></el-radio-group></el-form-item>
      </el-form>
      <template #footer><el-button @click="rechargeDialog = false">取消</el-button><el-button type="success" :loading="saving" @click="submitRecharge">模拟支付并到账</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { applyMembership, getMembershipOptions, getUserProfile, onlineRecharge } from '../api/user'

const loading = ref(false)
const saving = ref(false)
const profile = reactive({ hasMember: false })
const options = ref([])
const memberDialog = ref(false)
const rechargeDialog = ref(false)
const memberFormRef = ref()
const rechargeFormRef = ref()
const memberForm = reactive({ name: '', gender: 0, birthday: '', levelId: null })
const rechargeForm = reactive({ amount: 100, payWay: 2 })
const memberRules = {
  name: [{ required: true, message: '请输入会员姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  birthday: [{ required: true, message: '请选择出生日期', trigger: 'change' }],
  levelId: [{ required: true, message: '请选择会员卡类别', trigger: 'change' }]
}
const rechargeRules = { amount: [{ required: true, message: '请输入充值金额', trigger: 'change' }], payWay: [{ required: true, message: '请选择支付方式', trigger: 'change' }] }

onMounted(load)
async function load() {
  loading.value = true
  try {
    const [profileResponse, optionsResponse] = await Promise.all([getUserProfile(), getMembershipOptions()])
    Object.assign(profile, profileResponse.data)
    options.value = optionsResponse.data
    memberForm.name = profile.realName || ''
    if (!memberForm.levelId && options.value.length) memberForm.levelId = options.value[0].id
  } catch (error) { ElMessage.error(error.message) } finally { loading.value = false }
}
async function submitMembership() {
  await memberFormRef.value.validate()
  saving.value = true
  try {
    const response = await applyMembership(memberForm)
    Object.assign(profile, response.data)
    memberDialog.value = false
    ElMessage.success(`会员办理成功，卡号：${profile.cardNo}`)
  } catch (error) { ElMessage.error(error.message) } finally { saving.value = false }
}
async function submitRecharge() {
  await rechargeFormRef.value.validate()
  saving.value = true
  try {
    const response = await onlineRecharge({ ...rechargeForm, giftAmount: 0 })
    profile.balance = response.data.newBalance
    rechargeDialog.value = false
    ElMessage.success(`充值成功，当前余额 ￥${money(profile.balance)}`)
  } catch (error) { ElMessage.error(error.message) } finally { saving.value = false }
}
const money = (value) => Number(value || 0).toFixed(2)
const discountText = (value) => `${Number((Number(value || 1) * 10).toFixed(1))}折`
const cardClass = (levelId) => ({ 1: 'card-standard', 2: 'card-silver', 3: 'card-gold', 4: 'card-diamond' }[levelId] || 'card-standard')
const disabledBirthday = (date) => date.getTime() >= new Date().setHours(0, 0, 0, 0)
</script>

<style scoped>
.page-stack { display: grid; gap: 18px; }
.member-card { max-width: 820px; color: #fff; border: 0; }
.card-standard { background: linear-gradient(135deg, #123a2d, #23835f); }
.card-silver { background: linear-gradient(135deg, #46505a, #9aa5ad); }
.card-gold { background: linear-gradient(135deg, #6f4a13, #d5a63f); }
.card-diamond { background: linear-gradient(135deg, #243765, #6750a4 55%, #318aa5); }
.card-top { display: flex; align-items: center; justify-content: space-between; gap: 20px; }
.card-label { color: #b9dbcc; letter-spacing: 2px; }
.card-type { padding: 6px 13px; border: 1px solid rgba(255,255,255,.45); border-radius: 20px; background: rgba(255,255,255,.12); }
.card-number { margin: 28px 0 34px; font-size: 30px; letter-spacing: 3px; }
.card-details { display: flex; flex-wrap: wrap; gap: 55px; margin-bottom: 28px; }
.card-details span, .card-details strong { display: block; }
.card-details span { margin-bottom: 6px; color: #b9dbcc; }
.card-details strong { font-size: 22px; }
.dialog-alert { margin-bottom: 20px; }
.level-options { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; width: 100%; }
.level-option { min-height: 68px; padding: 12px 16px; border: 2px solid transparent; border-radius: 9px; color: #fff; text-align: left; cursor: pointer; opacity: .68; }
.level-option strong, .level-option span { display: block; }
.level-option span { margin-top: 5px; font-size: 13px; opacity: .88; }
.level-option.selected { border-color: #17c98a; box-shadow: 0 0 0 2px rgba(23,201,138,.2); opacity: 1; }
@media (max-width: 600px) { .level-options { grid-template-columns: 1fr; } }
</style>
