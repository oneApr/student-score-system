<script setup lang="ts">
import { reactive, ref } from 'vue'
import { resetPassword, verifyUsername } from '@/api/auth'
import { ElMessage } from 'element-plus'
import { User, Lock, Key } from '@element-plus/icons-vue'

const emit = defineEmits(['goToLogin'])

const formData = reactive({
  username: '',
  newPassword: '',
  confirmPassword: ''
})

const loading = ref(false)
const step = ref(1)

const handleVerify = async () => {
  if (!formData.username) {
    ElMessage.warning('请输入需要找回密码的账号')
    return
  }

  loading.value = true
  try {
    const res: any = await verifyUsername(formData.username)
    if (res.code === 200 && res.data) {
      ElMessage.success('账号验证通过，请设置新密码')
      step.value = 2
    } else {
      ElMessage.warning(res.message || '该用户不存在')
    }
  } catch (err: any) {
    ElMessage.error(err.message || '账号验证失败')
  } finally {
    loading.value = false
  }
}

const handleReset = async () => {
  if (!formData.newPassword || !formData.confirmPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  if (formData.newPassword !== formData.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  if (formData.newPassword.length < 6) {
    ElMessage.warning('密码至少6位')
    return
  }

  loading.value = true
  try {
    await resetPassword({
      username: formData.username,
      newPassword: formData.newPassword
    })
    ElMessage.success('密码重置成功，请使用新密码重新登录')
    emit('goToLogin')
  } catch (err: any) {
    ElMessage.error(err.message || '重置失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-form-container">
    <div style="margin-bottom: 24px; text-align: center; font-size: 15px; color: #666; font-weight: bold;">
      {{ step === 1 ? '第一步：验证账号存在性' : '第二步：为账号设置新密码' }}
    </div>

    <div class="input-group" v-if="step === 1">
      <div class="custom-input">
        <el-icon class="icon">
          <User />
        </el-icon>
        <input v-model="formData.username" placeholder="请输入您的账号" @keyup.enter="handleVerify" />
      </div>

      <button class="submit-btn" :disabled="loading" @click="handleVerify" style="margin-top: 20px;">
        {{ loading ? '验证中...' : '下一步' }}
      </button>
    </div>

    <div class="input-group" v-else>
      <div class="custom-input disabled">
        <el-icon class="icon">
          <User />
        </el-icon>
        <input v-model="formData.username" disabled />
      </div>
      <div class="custom-input">
        <el-icon class="icon">
          <Key />
        </el-icon>
        <input type="password" v-model="formData.newPassword" placeholder="请输入新密码 (至少6位)" @keyup.enter="handleReset" />
      </div>
      <div class="custom-input">
        <el-icon class="icon">
          <Lock />
        </el-icon>
        <input type="password" v-model="formData.confirmPassword" placeholder="请确认新密码" @keyup.enter="handleReset" />
      </div>

      <button class="submit-btn" :disabled="loading" @click="handleReset" style="margin-top: 10px;">
        {{ loading ? '处理中...' : '确认重置' }}
      </button>
    </div>

    <div class="footer-links" style="margin-top: 30px;">
      <span class="register-link" @click="emit('goToLogin')">返回登录</span>
    </div>
  </div>
</template>

<style scoped lang="scss">
@import '@/assets/styles/login-forms.scss';
</style>