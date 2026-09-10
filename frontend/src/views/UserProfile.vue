<template>
  <div class="profile-grid" v-loading="loading">
    <el-card shadow="never">
      <template #header><strong>基本资料</strong></template>
      <el-form ref="profileRef" :model="profileForm" :rules="profileRules" label-width="90px">
        <el-form-item label="手机号"><el-input :model-value="profile.phone" disabled /></el-form-item>
        <el-form-item label="姓名" prop="realName"><el-input v-model="profileForm.realName" maxlength="50" /></el-form-item>
        <el-form-item><el-button type="primary" :loading="savingProfile" @click="saveProfile">保存资料</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card shadow="never">
      <template #header><strong>修改密码</strong></template>
      <el-form ref="passwordRef" :model="passwordForm" :rules="passwordRules" label-width="90px">
        <el-form-item label="原密码" prop="oldPassword"><el-input v-model="passwordForm.oldPassword" type="password" show-password /></el-form-item>
        <el-form-item label="新密码" prop="newPassword"><el-input v-model="passwordForm.newPassword" type="password" show-password /></el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword"><el-input v-model="passwordForm.confirmPassword" type="password" show-password /></el-form-item>
        <el-form-item><el-button type="primary" :loading="savingPassword" @click="savePassword">更新密码</el-button></el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { changePassword } from '../api/auth'
import { getUserProfile, updateUserProfile } from '../api/user'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const loading = ref(false)
const savingProfile = ref(false)
const savingPassword = ref(false)
const profileRef = ref()
const passwordRef = ref()
const profile = reactive({})
const profileForm = reactive({ realName: '' })
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const profileRules = { realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }] }
const validateConfirm = (_rule, value, callback) => value === passwordForm.newPassword ? callback() : callback(new Error('两次输入的新密码不一致'))
const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, max: 32, message: '密码长度应为6到32位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请再次输入新密码', trigger: 'blur' }, { validator: validateConfirm, trigger: 'blur' }]
}

onMounted(load)
async function load() {
  loading.value = true
  try {
    const response = await getUserProfile()
    Object.assign(profile, response.data)
    profileForm.realName = profile.realName
  } catch (error) { ElMessage.error(error.message) } finally { loading.value = false }
}
async function saveProfile() {
  await profileRef.value.validate()
  savingProfile.value = true
  try {
    const response = await updateUserProfile(profileForm)
    Object.assign(profile, response.data)
    if (auth.user) auth.user.realName = profile.realName
    ElMessage.success('个人资料已更新')
  } catch (error) { ElMessage.error(error.message) } finally { savingProfile.value = false }
}
async function savePassword() {
  await passwordRef.value.validate()
  savingPassword.value = true
  try {
    await changePassword({ oldPassword: passwordForm.oldPassword, newPassword: passwordForm.newPassword })
    passwordForm.oldPassword = ''; passwordForm.newPassword = ''; passwordForm.confirmPassword = ''
    passwordRef.value.clearValidate()
    ElMessage.success('密码修改成功')
  } catch (error) { ElMessage.error(error.message) } finally { savingPassword.value = false }
}
</script>

<style scoped>
.profile-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 18px; }
@media (max-width: 850px) { .profile-grid { grid-template-columns: 1fr; } }
</style>
