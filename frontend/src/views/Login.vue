<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 class="title">台球厅会员管理系统</h2>
      <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="onLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" show-password size="large"
                    @keyup.enter="onLogin" />
        </el-form-item>
        <el-button type="primary" size="large" style="width: 100%" :loading="loading" native-type="submit">
          登 录
        </el-button>
      </el-form>
      <p class="hint">演示账号：admin / 123456　前台：cashier / 123456</p>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: 'admin', password: '123456' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function onLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    await auth.login(form)
    ElMessage.success('登录成功')
    router.replace(router.currentRoute.value.query.redirect || '/home')
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: radial-gradient(circle at 25% 20%, #2c7658 0%, #18372e 35%, #121d23 100%);
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
