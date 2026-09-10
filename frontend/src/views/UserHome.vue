<template>
  <div class="page-stack">
    <PageHeader :title="`你好，${profile.realName || auth.user?.realName || ''}`" description="在这里办理会员、充值并预约今天的球桌">
      <template #actions><el-button type="primary" @click="router.push('/user/reservations')">立即预约</el-button></template>
    </PageHeader>
    <el-row :gutter="18" v-loading="loading">
      <el-col :xs="24" :sm="8"><el-card shadow="never" class="summary-card"><span>会员卡号</span><strong>{{ profile.hasMember ? profile.cardNo : '尚未办理' }}</strong></el-card></el-col>
      <el-col :xs="24" :sm="8"><el-card shadow="never" class="summary-card"><span>账户余额</span><strong>{{ profile.hasMember ? `￥${money(profile.balance)}` : '--' }}</strong></el-card></el-col>
      <el-col :xs="24" :sm="8"><el-card shadow="never" class="summary-card"><span>当前折扣</span><strong>{{ profile.hasMember ? discountText(profile.discount) : '--' }}</strong></el-card></el-col>
    </el-row>
    <el-card v-if="!loading && !profile.hasMember" shadow="never">
      <el-result icon="info" title="你还没有会员卡" sub-title="办理后可查看卡号、余额与折扣，并使用在线充值和球桌预约功能。">
        <template #extra><el-button type="success" @click="router.push('/user/membership')">在线办理会员</el-button></template>
      </el-result>
    </el-card>
    <el-card v-else shadow="never">
      <template #header><strong>我的当天预约</strong></template>
      <el-empty v-if="!reservations.length" description="今天还没有预约" />
      <el-table v-else :data="reservations.slice(0, 5)" stripe>
        <el-table-column prop="reservationNo" label="预约单号" min-width="180" />
        <el-table-column prop="tableNo" label="球桌" width="100" />
        <el-table-column label="预约时间" min-width="210"><template #default="{ row }">{{ dateTime(row.startTime) }} 至 {{ timeOnly(row.endTime) }}</template></el-table-column>
        <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag></template></el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeader from '../components/PageHeader.vue'
import { getUserProfile } from '../api/user'
import { getMyReservations } from '../api/reservation'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const profile = reactive({ hasMember: false })
const reservations = ref([])

onMounted(load)
async function load() {
  loading.value = true
  try {
    const [profileResponse, reservationResponse] = await Promise.all([getUserProfile(), getMyReservations()])
    Object.assign(profile, profileResponse.data)
    reservations.value = reservationResponse.data
  } catch (error) { ElMessage.error(error.message) } finally { loading.value = false }
}
const money = (value) => Number(value || 0).toFixed(2)
const discountText = (value) => `${Number(value || 1) * 10}折`
const dateTime = (value) => value ? value.replace('T', ' ').slice(0, 16) : '--'
const timeOnly = (value) => value ? value.slice(11, 16) : '--'
const statusText = (status) => ['待处理', '已开台', '已完成', '已取消'][status] || '未知'
const statusTag = (status) => ['warning', 'primary', 'success', 'info'][status] || 'info'
</script>

<style scoped>
.page-stack { display: grid; gap: 18px; }
.summary-card { margin-bottom: 18px; }
.summary-card span { display: block; margin-bottom: 14px; color: #849089; }
.summary-card strong { color: #1d5e47; font-size: 24px; }
</style>
