<script setup lang="ts">
import { reactive, ref } from 'vue'
import { register } from '@/api/auth'
import { ElMessage } from 'element-plus'
import { User, Lock, Avatar } from '@element-plus/icons-vue'

const props = defineProps(['activeRole'])
const emit = defineEmits(['update:activeRole', 'goToLogin'])

const registerData = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})

const loading = ref(false)

const roles = [
  { label: '学生', value: 'student' },
  { label: '教师', value: 'teacher' },
  { label: '管理员', value: 'admin' }
]

const setRole = (val: string) => {
  emit('update:activeRole', val)
}

const handleRegister = async () => {
  if (!registerData.username || !registerData.password) {
    ElMessage.warning('请输入完整注册信息')
    return
  }
  if (registerData.password !== registerData.confirmPassword) {
    ElMessage.warning('两次输入密码不一致')
    return
  }
  if (registerData.password.length < 6) {
    ElMessage.warning('密码至少6位')
    return
  }

  loading.value = true
  try {
    await register({
      username: registerData.username,
      nickname: registerData.nickname || registerData.username,
      password: registerData.password,
      roleKey: props.activeRole
    })
    ElMessage.success('注册成功，请使用新账号登录')
    emit('goToLogin')
  } catch (err: any) {
    ElMessage.error(err.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-form-container">
    <div class="role-selector">
      <div v-for="role in roles" :key="role.value" class="role-item" :class="{ active: activeRole === role.value }"
        @click="setRole(role.value)">
        {{ role.label }}
      </div>
    </div>

    <div class="input-group">
      <div class="custom-input">
        <el-icon class="icon">
          <User />
        </el-icon>
        <input v-model="registerData.username" placeholder="请输入注册账号" @keyup.enter="handleRegister" />
      </div>
      <div class="custom-input">
        <el-icon class="icon">
          <Avatar />
        </el-icon>
        <input v-model="registerData.nickname" placeholder="请输入您的姓名" @keyup.enter="handleRegister" />
      </div>
      <div class="custom-input">
        <el-icon class="icon">
          <Lock />
        </el-icon>
        <input type="password" v-model="registerData.password" placeholder="请输入密码 (至少6位)"
          @keyup.enter="handleRegister" />
      </div>
      <div class="custom-input">
        <el-icon class="icon">
          <Lock />
        </el-icon>
        <input type="password" v-model="registerData.confirmPassword" placeholder="请再次确认密码"
          @keyup.enter="handleRegister" />
      </div>
    </div>

    <button class="submit-btn" :disabled="loading" @click="handleRegister">
      {{ loading ? '注册中...' : '注 册' }}
    </button>

    <div class="footer-links">
      已有账号？<span class="register-link" @click="emit('goToLogin')">直接登录</span>
    </div>
  </div>
</template>

<style scoped lang="scss">
@import '@/assets/styles/login-forms.scss';

.custom-input {
  margin-bottom: 15px;
  height: 50px;
}
</style>
