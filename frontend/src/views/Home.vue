<template>
  <div>
    <PageHeader :title="`欢迎回来，${auth.user?.realName || auth.user?.username || ''}`"
                description="选择一项日常工作开始处理">
      <template #actions><el-button type="primary" size="large" @click="router.push('/tables')">进入球桌看板</el-button></template>
    </PageHeader>
    <div class="summary-strip">
      <span>当前身份</span><strong>{{ auth.isAdmin ? '管理员' : '前台操作员' }}</strong>
      <small>{{ auth.isAdmin ? '可查看经营报表并维护球桌状态' : '可处理会员、球桌与业务流水' }}</small>
    </div>
    <div class="quick-grid">
      <button class="quick-card" @click="router.push('/members')"><span>01</span><strong>会员建档与充值</strong><small>新增会员、调整资料、充值余额</small></button>
      <button class="quick-card" @click="router.push('/tables')"><span>02</span><strong>开台与结账</strong><small>实时查看球桌、整小时进位结算</small></button>
      <button class="quick-card" @click="router.push('/records')"><span>03</span><strong>业务流水</strong><small>筛选并导出充值和消费记录</small></button>
      <button v-if="auth.isAdmin" class="quick-card report-card" @click="router.push('/reports')"><span>04</span><strong>经营报表</strong><small>分析收入、客群和球桌利用率</small></button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import PageHeader from '../components/PageHeader.vue'
import { useAuthStore } from '../stores/auth'
const router = useRouter()
const auth = useAuthStore()
</script>

<style scoped>
.summary-strip { display: grid; grid-template-columns: auto auto 1fr; align-items: center; gap: 12px; padding: 18px 22px; margin-bottom: 18px; border-radius: 12px; color: #d7ebe2; background: linear-gradient(110deg, #153c2e, #1c6b4d); }
.summary-strip span { font-size: 13px; opacity: .75; }
.summary-strip strong { padding: 5px 10px; border-radius: 20px; background: rgba(255,255,255,.12); color: #fff; }
.summary-strip small { text-align: right; }
.quick-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 18px; }
.quick-card { min-height: 180px; padding: 26px; border: 1px solid #e1e9e5; border-radius: 14px; background: #fff; text-align: left; cursor: pointer; transition: transform .2s, box-shadow .2s, border-color .2s; }
.quick-card:hover { transform: translateY(-3px); border-color: #8cc8af; box-shadow: 0 12px 28px rgba(25,75,56,.09); }
.quick-card > span { display: block; color: #2d9d73; font-size: 13px; letter-spacing: 1px; }
.quick-card strong { display: block; margin: 30px 0 12px; color: #203a31; font-size: 18px; }
.quick-card small { color: #78867f; font-size: 14px; }
.report-card { background: linear-gradient(145deg, #fff, #f1faf6); }
@media (max-width: 900px) { .quick-grid { grid-template-columns: 1fr 1fr; } }
@media (max-width: 600px) {
  .quick-grid { grid-template-columns: 1fr; }
  .summary-strip { grid-template-columns: auto 1fr; }
  .summary-strip small { grid-column: 1 / -1; text-align: left; }
}
</style>
