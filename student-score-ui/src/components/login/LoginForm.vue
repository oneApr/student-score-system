<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { addDynamicRoutes, getFirstMenuPath, setRoutesAdded } from '@/router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const props = defineProps(['activeRole'])
const emit = defineEmits(['update:activeRole', 'goToRegister', 'goToForgot'])

const router = useRouter()
const userStore = useUserStore()

const loginData = reactive({
  account: '',
  password: '',
  remember: false
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

const handleLogin = async () => {
  if (!loginData.account || !loginData.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    await userStore.login({
      username: loginData.account,
      password: loginData.password,
      role: props.activeRole
    })

    // 先注册动态路由并同步标记，再跳转，避免守卫因路由未注册而重定向回登录页
    addDynamicRoutes(userStore.menuList)
    setRoutesAdded()
    ElMessage.success('登录成功')
    const targetPath = getFirstMenuPath(userStore.menuList)
    router.push(targetPath)
  } catch (err: any) {
    ElMessage.error(err.message || '登录失败，请检查账号密码')
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
        <input v-model="loginData.account" placeholder="请输入账号" @keyup.enter="handleLogin" />
      </div>
      <div class="custom-input">
        <el-icon class="icon">
          <Lock />
        </el-icon>
        <input type="password" v-model="loginData.password" placeholder="请输入密码" @keyup.enter="handleLogin" />
      </div>
    </div>

    <div class="options">
      <label class="remember">
        <input type="checkbox" v-model="loginData.remember" />
        <span class="checkbox-text">记住密码</span>
      </label>
      <span class="forgot-btn" @click="emit('goToForgot')">忘记密码？</span>
    </div>

    <button class="submit-btn" :disabled="loading" @click="handleLogin">
      {{ loading ? '登录中...' : '登 录' }}
    </button>

    <div class="footer-links">
      还没有账号？<span class="register-link" @click="emit('goToRegister')">注册账号</span>
    </div>
  </div>
</template>

<style scoped lang="scss">
@import '@/assets/styles/login-forms.scss';
</style>