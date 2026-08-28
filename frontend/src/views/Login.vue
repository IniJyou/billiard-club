<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 class="title">台球厅会员管理系统</h2>
      <el-form :model="form" @submit.prevent>
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" show-password size="large" />
        </el-form-item>
        <el-button type="primary" size="large" style="width: 100%" @click="onLogin">
          登 录
        </el-button>
      </el-form>
      <p class="hint">默认账号 admin / 123456（待对接后端登录接口）</p>
    </el-card>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const form = reactive({ username: '', password: '' })

function onLogin() {
  // TODO: 对接后端 /api/auth/login，校验通过后写入 token 并跳转
  router.push('/home')
}
</script>

<style scoped>
.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: linear-gradient(135deg, #1f2d3d 0%, #2c3e50 100%);
}
.login-card {
  width: 360px;
  padding: 8px 8px 16px;
}
.title {
  text-align: center;
  margin-bottom: 20px;
  color: #303133;
}
.hint {
  margin-top: 12px;
  font-size: 12px;
  color: #909399;
  text-align: center;
}
</style>
