<script setup lang="ts">
import { ref } from 'vue'
import LoginForm from '@/components/login/LoginForm.vue'
import RegisterForm from '@/components/login/RegisterForm.vue'
import ForgotForm from '@/components/login/ForgotForm.vue'

// 当前视图状态：'login' | 'register' | 'forgot'
const currentView = ref('login')

// 选中的角色状态
const activeRole = ref('student')

const goToLogin = () => { currentView.value = 'login' }
const goToRegister = () => { currentView.value = 'register' }
const goToForgot = () => { currentView.value = 'forgot' }
</script>

<template>
  <div class="login-page flex-center">
    <div class="login-card-wrapper flex-row">
      <div class="form-panel flex-column">
        <div class="brand-header flex-column align-center">
          <div class="logo-text">{{ currentView === 'forgot' ? '重置密码' : '学生成绩管理系统' }}</div>
          <div class="logo-subtext">
            {{ currentView === 'forgot' ? '找回您的账号访问权限' : 'STUDENT GRADE MANAGEMENT SYSTEM' }}
          </div>
        </div>
        <div class="form-content">
          <Transition name="fade-slide" mode="out-in">
            <LoginForm v-if="currentView === 'login'" v-model:activeRole="activeRole" @goToRegister="goToRegister"
              @goToForgot="goToForgot" />
            <RegisterForm v-else-if="currentView === 'register'" v-model:activeRole="activeRole"
              @goToLogin="goToLogin" />
            <ForgotForm v-else-if="currentView === 'forgot'" @goToLogin="goToLogin" />
          </Transition>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.login-page {
  width: 100vw;
  height: 100vh;
  background-image: url('/student_backgroud.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 0;
}

.login-card-wrapper {
  width: 100%;
  /* 增加卡片尺寸，使其更大气 */
  max-width: 540px;
  min-height: 580px;

  /* 核心修改：改为几乎不透明的纯白底色，彻底隔绝背景干扰 */
  background-color: rgba(255, 255, 255, 0.96);

  /* 核心修改：使用明显的黑色粗边框，强化卡通风格的轮廓 */
  border: 3px solid #1a1a1a;
  border-radius: 28px;

  /* 核心修改：经典的漫画风实体阴影（Solid Shadow） */
  box-shadow: 10px 10px 0px rgba(0, 0, 0, 0.15);
  position: relative;
  transition: all 0.4s ease;
}

.form-panel {
  width: 100%;
  color: #1a1a1a;
  /* 统一基础字体为近乎黑色的深灰 */

  .brand-header {
    margin-top: 50px;
    margin-bottom: 30px;
    text-align: center;

    .logo-text {
      font-size: 32px;
      font-weight: 900;
      letter-spacing: 2px;
      color: #000000;
      /* 标题纯黑 */
    }

    .logo-subtext {
      font-size: 13px;
      font-weight: bold;
      margin-top: 10px;
      letter-spacing: 1.5px;
      color: #666666;
      /* 副标题深灰，拉开层次 */
    }
  }

  .form-content {
    padding: 10px 60px 40px;
    display: flex;
    flex-direction: column;
    flex-grow: 1;
  }
}

/* 切换动效 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.4s cubic-bezier(0.25, 1, 0.5, 1);
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

/* Flex 工具类 */
.flex-column {
  display: flex;
  flex-direction: column;
}

.flex-row {
  display: flex;
  flex-direction: row;
}

.flex-center {
  display: flex;
  justify-content: center;
  align-items: center;
}

.align-center {
  align-items: center;
}
</style>