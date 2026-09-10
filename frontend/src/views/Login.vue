<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 class="title">台球厅会员管理系统</h2>
      <el-tabs v-model="activeTab" stretch>
        <el-tab-pane label="登录" name="login">
          <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="onLogin">
            <el-form-item prop="username">
              <el-input v-model="form.username" placeholder="用户名或手机号" size="large" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="form.password" type="password" placeholder="密码" show-password size="large"
                        @keyup.enter="onLogin" />
            </el-form-item>
            <el-button type="primary" size="large" style="width: 100%" :loading="loading" native-type="submit">登 录</el-button>
          </el-form>
          <p class="hint">管理员：admin / 123456　前台：cashier / 123456<br>用户：13900000000 / 123456</p>
        </el-tab-pane>
        <el-tab-pane label="用户注册" name="register">
          <el-form ref="registerRef" :model="registerForm" :rules="registerRules" @submit.prevent="onRegister">
            <el-form-item prop="realName"><el-input v-model="registerForm.realName" maxlength="50" placeholder="姓名" size="large" /></el-form-item>
            <el-form-item prop="phone"><el-input v-model="registerForm.phone" maxlength="11" placeholder="11位手机号（登录账号）" size="large" /></el-form-item>
            <el-form-item prop="password"><el-input v-model="registerForm.password" type="password" show-password placeholder="密码（6到32位）" size="large" /></el-form-item>
            <el-form-item prop="confirmPassword"><el-input v-model="registerForm.confirmPassword" type="password" show-password placeholder="再次输入密码" size="large" @keyup.enter="onRegister" /></el-form-item>
            <el-button type="success" size="large" style="width: 100%" :loading="loading" native-type="submit">注册并登录</el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
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
const registerRef = ref()
const loading = ref(false)
const activeTab = ref('login')
const form = reactive({ username: 'admin', password: '123456' })
const registerForm = reactive({ realName: '', phone: '', password: '', confirmPassword: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}
const registerRules = {
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '请输入正确的11位手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度应为6到32位', trigger: 'blur' }
  ],
  confirmPassword: [{
    validator: (_rule, value, callback) => value === registerForm.password ? callback() : callback(new Error('两次输入的密码不一致')),
    trigger: 'blur'
  }]
}

async function onLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    await auth.login(form)
    ElMessage.success('登录成功')
    router.replace(router.currentRoute.value.query.redirect || auth.defaultPath)
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

async function onRegister() {
  await registerRef.value.validate()
  loading.value = true
  try {
    const { realName, phone, password } = registerForm
    await auth.register({ realName, phone, password })
    ElMessage.success('注册成功，已自动登录')
    router.replace('/user/home')
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
  width: 390px;
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
