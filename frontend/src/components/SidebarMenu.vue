<template>
  <div class="side-content">
    <div class="brand">
      <div class="brand-mark">B</div>
      <div><strong>台球厅管理</strong><small>Billiard Club</small></div>
    </div>
    <el-menu :default-active="route.path" router class="app-menu" @select="$emit('navigate')">
      <el-menu-item index="/home">工作台</el-menu-item>
      <el-menu-item index="/members">会员与充值</el-menu-item>
      <el-menu-item index="/tables">球桌与结账</el-menu-item>
      <el-menu-item index="/records">业务流水</el-menu-item>
      <el-menu-item v-if="auth.isAdmin" index="/reports">经营报表</el-menu-item>
    </el-menu>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'

defineEmits(['navigate'])
const route = useRoute()
const auth = useAuthStore()
</script>

<style scoped>
.side-content { min-height: 100%; background: #14251f; color: #fff; }
.brand { height: 72px; display: flex; align-items: center; gap: 12px; padding: 0 20px; border-bottom: 1px solid rgba(255,255,255,.08); }
.brand-mark { width: 38px; height: 38px; border-radius: 50%; display: grid; place-items: center; background: #27a777; font-size: 20px; font-weight: 700; box-shadow: inset 0 0 0 4px rgba(255,255,255,.13); }
.brand small { display: block; margin-top: 3px; color: #8fa79d; }
.app-menu { border-right: 0; background: transparent; padding: 10px; }
.app-menu :deep(.el-menu-item) { height: 48px; margin: 4px 0; border-radius: 9px; color: #b9cbc3; }
.app-menu :deep(.el-menu-item:hover), .app-menu :deep(.el-menu-item.is-active) { color: #fff; background: #23483a; }
</style>
