<template>
  <el-container class="app-shell">
    <el-aside width="220px" class="app-aside">
      <div class="brand">
        <div class="brand-mark">B</div>
        <div>
          <strong>台球厅管理</strong>
          <small>Billiard Club</small>
        </div>
      </div>
      <el-menu :default-active="route.path" router class="app-menu">
        <el-menu-item index="/home">工作台</el-menu-item>
        <el-menu-item index="/members">会员与充值</el-menu-item>
        <el-menu-item index="/tables">球桌与结账</el-menu-item>
        <el-menu-item index="/records">业务流水</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="app-header">
        <div>
          <strong>{{ pageTitle }}</strong>
          <span class="header-subtitle">台球厅会员管理系统</span>
        </div>
        <div class="user-area">
          <el-tag :type="auth.isAdmin ? 'danger' : 'info'" effect="plain">
            {{ auth.isAdmin ? '管理员' : '前台' }}
          </el-tag>
          <span>{{ auth.user?.realName || auth.user?.username }}</span>
          <el-button link type="primary" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const titles = {
  '/home': '工作台',
  '/members': '会员与充值',
  '/tables': '球桌与结账',
  '/records': '业务流水'
}
const pageTitle = computed(() => titles[route.path] || '台球厅管理')

async function handleLogout() {
  await auth.logout()
  ElMessage.success('已退出登录')
  router.replace('/login')
}
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  background: #f4f6f8;
}
.app-aside {
  background: #17212b;
  color: #fff;
}
.brand {
  height: 72px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.brand-mark {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: #2f9e6f;
  display: grid;
  place-items: center;
  font-size: 20px;
  font-weight: 700;
}
.brand small {
  display: block;
  margin-top: 3px;
  color: #91a0ae;
}
.app-menu {
  border-right: 0;
  background: transparent;
}
.app-menu :deep(.el-menu-item) {
  color: #bdc7d0;
}
.app-menu :deep(.el-menu-item:hover),
.app-menu :deep(.el-menu-item.is-active) {
  color: #fff;
  background: #243442;
}
.app-header {
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e8ebef;
}
.header-subtitle {
  margin-left: 12px;
  color: #909399;
  font-size: 13px;
}
.user-area {
  display: flex;
  align-items: center;
  gap: 12px;
}
.app-main {
  padding: 24px;
}
@media (max-width: 800px) {
  .app-aside { width: 160px !important; }
  .brand { padding: 0 12px; }
  .header-subtitle { display: none; }
}
</style>
