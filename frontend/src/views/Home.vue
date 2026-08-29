<template>
  <div>
    <div class="welcome-card">
      <div>
        <p class="eyebrow">今日工作台</p>
        <h1>欢迎回来，{{ auth.user?.realName }}</h1>
        <p>系统已经接入真实会员、充值、开台、结账与流水数据。</p>
      </div>
      <el-button type="primary" size="large" @click="router.push('/tables')">进入球桌看板</el-button>
    </div>
    <div class="quick-grid">
      <button class="quick-card" @click="router.push('/members')">
        <strong>会员建档与充值</strong>
        <span>新增会员、调整资料、充值余额</span>
      </button>
      <button class="quick-card" @click="router.push('/tables')">
        <strong>开台与结账</strong>
        <span>实时查看球桌、整小时进位结算</span>
      </button>
      <button class="quick-card" @click="router.push('/records')">
        <strong>业务流水</strong>
        <span>查询充值单和台费消费记录</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
</script>

<style scoped>
.welcome-card {
  min-height: 190px;
  padding: 36px;
  border-radius: 14px;
  color: #fff;
  background: linear-gradient(120deg, #176c4c, #2d8e68);
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 12px 30px rgba(31, 112, 81, 0.18);
}
.welcome-card h1 { margin: 8px 0 12px; }
.welcome-card p { color: rgba(255, 255, 255, 0.82); }
.eyebrow { font-size: 13px; letter-spacing: 2px; }
.quick-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
  margin-top: 20px;
}
.quick-card {
  padding: 26px;
  border: 1px solid #e6e9ed;
  border-radius: 12px;
  background: #fff;
  text-align: left;
  cursor: pointer;
  transition: transform .2s, box-shadow .2s;
}
.quick-card:hover { transform: translateY(-2px); box-shadow: 0 8px 22px rgba(31,45,61,.08); }
.quick-card strong { display: block; color: #26333d; font-size: 17px; margin-bottom: 10px; }
.quick-card span { color: #7d8994; }
@media (max-width: 900px) {
  .quick-grid { grid-template-columns: 1fr; }
}
</style>
