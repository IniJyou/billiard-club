<template>
  <el-container class="app-shell">
    <el-aside width="220px" class="desktop-aside"><SidebarMenu /></el-aside>
    <el-container>
      <el-header class="app-header">
        <div class="header-title">
          <el-button class="mobile-menu" text aria-label="打开导航菜单" @click="drawerOpen = true">
            <el-icon size="22"><MenuIcon /></el-icon>
          </el-button>
          <div><strong>{{ pageTitle }}</strong><span>台球厅会员管理系统</span></div>
        </div>
        <div class="user-area">
          <el-tag :type="auth.isAdmin ? 'danger' : 'info'" effect="plain">
            {{ auth.isAdmin ? '管理员' : '前台' }}
          </el-tag>
          <span class="user-name">{{ auth.user?.realName || auth.user?.username }}</span>
          <el-button link type="primary" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="app-main"><router-view /></el-main>
    </el-container>
    <el-drawer v-model="drawerOpen" direction="ltr" :with-header="false" size="220px" class="nav-drawer">
      <SidebarMenu @navigate="drawerOpen = false" />
    </el-drawer>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { Menu as MenuIcon } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import SidebarMenu from '../components/SidebarMenu.vue'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const drawerOpen = ref(false)
const titles = {
  '/home': '工作台', '/members': '会员与充值', '/tables': '球桌与结账',
  '/records': '业务流水', '/reports': '经营报表', '/forbidden': '访问受限'
}
const pageTitle = computed(() => titles[route.path] || '台球厅管理')

async function handleLogout() {
  await auth.logout()
  ElMessage.success('已退出登录')
  router.replace('/login')
}
</script>

<style scoped>
.app-shell { min-height: 100vh; background: #f2f6f3; }
.desktop-aside { background: #14251f; box-shadow: 5px 0 22px rgba(18,42,33,.08); }
.app-header { height: 72px; display: flex; align-items: center; justify-content: space-between; gap: 16px; background: rgba(255,255,255,.96); border-bottom: 1px solid #e4ebe7; }
.header-title { display: flex; align-items: center; gap: 4px; min-width: 0; }
.header-title strong { color: #193a2e; font-size: 17px; }
.header-title span { margin-left: 12px; color: #8a958f; font-size: 13px; }
.mobile-menu { display: none; }
.user-area { display: flex; align-items: center; gap: 12px; white-space: nowrap; }
.app-main { padding: 24px; overflow-x: hidden; }
:global(.nav-drawer .el-drawer__body) { padding: 0; background: #14251f; }
@media (max-width: 800px) {
  .desktop-aside { display: none; }
  .mobile-menu { display: inline-flex; }
  .header-title span, .user-name { display: none; }
  .app-main { padding: 16px; }
}
</style>
