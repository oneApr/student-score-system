<script setup lang="ts">
import { useRoute } from 'vue-router'
import { onMounted, computed, onUnmounted } from 'vue'
import { useUserStore } from '@/store/user'
import {
  Setting,
  User,
  Document,
  Calendar,
  DataAnalysis,
  Tickets,
  DataLine,
  SwitchButton,
  Collection,
  OfficeBuilding,
  Reading,
  UserFilled,
  Mouse,
  Stamp,
  PieChart,
  Briefcase,
  EditPen,
  CircleCheck,
  Search,
  Avatar,
  TrendCharts,
  Coordinate,
  Menu,
  Lock
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()

const userStore = useUserStore()

// 映射图标字符串到组件
const iconMap: Record<string, any> = {
  'Setting': Setting,
  'User': User,
  'Document': Document,
  'Calendar': Calendar,
  'DataAnalysis': DataAnalysis,
  'Tickets': Tickets,
  'DataLine': DataLine,
  'SwitchButton': SwitchButton,
  'Collection': Collection,
  'OfficeBuilding': OfficeBuilding,
  'Reading': Reading,
  'UserFilled': UserFilled,
  'Mouse': Mouse,
  'Stamp': Stamp,
  'PieChart': PieChart,
  'Briefcase': Briefcase,
  'EditPen': EditPen,
  'CircleCheck': CircleCheck,
  'Search': Search,
  'Avatar': Avatar,
  'TrendCharts': TrendCharts,
  'Coordinate': Coordinate,
  'Menu': Menu,
  'Lock': Lock
}

// 直接使用后端返回的菜单（后端已根据用户角色过滤）
const menuItems = computed(() => {
  return userStore.menuList || []
})

// 扁平化菜单用于 el-menu-item
const flatMenuItems = computed(() => {
  const result: any[] = []

  const processMenu = (menu: any) => {
    if (menu.children && menu.children.length > 0) {
      menu.children.forEach((child: any) => {
        result.push({
          index: '/' + child.path,
          title: child.menuName,
          icon: iconMap[child.icon] || Document,
          parentPath: menu.path
        })
      })
    }
  }

  menuItems.value.forEach(processMenu)
  return result
})



const handleLogout = async () => {
  await userStore.logout()
  ElMessage.success('已安全退出')
}

// 展示用用户信息
const userInfo = computed(() => ({
  name: userStore.userInfo?.nickname || '未命名',
  subtitle: userStore.roles.includes('admin') ? '管理员' :
    (userStore.roles.includes('teacher') ? '教师' : '学生')
}))

const onSidebarClick = (e: MouseEvent) => {
  const target = (e.target as HTMLElement).closest('.el-menu-item')
  if (!target) return
  const rect = target.getBoundingClientRect()
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top
  for (let i = 0; i < 6; i++) {
    const dot = document.createElement('span')
    dot.className = 'ripple-particle'
    dot.style.left = x + 'px'
    dot.style.top = y + 'px'
    const angle = Math.random() * Math.PI * 2
    const dist = 20 + Math.random() * 30
    dot.style.setProperty('--tx', Math.cos(angle) * dist + 'px')
    dot.style.setProperty('--ty', Math.sin(angle) * dist + 'px')
    target.appendChild(dot)
    setTimeout(() => dot.remove(), 600)
  }
}

onMounted(async () => {
  if (!userStore.userInfo) {
    try {
      await userStore.getUserInfo()
    } catch (error) {
      // getUserInfo 失败会在 request.ts 拦截跳转到登录
    }
  }
  const sidebar = document.querySelector('.sidebar-menu')
  sidebar?.addEventListener('click', onSidebarClick as EventListener)
})

onUnmounted(() => {
  const sidebar = document.querySelector('.sidebar-menu')
  sidebar?.removeEventListener('click', onSidebarClick as EventListener)
})
</script>

<template>
  <el-container class="layout-container">
    <el-header class="nav-header flex-row align-center">
      <div class="header-left flex-row align-center">
        <img src="/logo.svg" class="header-logo" alt="Logo" />
        <span class="header-title">高校学生成绩管理系统</span>
      </div>

      <div class="header-right flex-row align-center">
        <div class="user-profile flex-row align-center">
          <el-icon class="user-icon">
            <User />
          </el-icon>
          <div class="user-info flex-column">
            <span class="user-name">{{ userInfo.name }}</span>
            <span class="user-role">{{ userInfo.subtitle }}</span>
          </div>
        </div>
        <div class="logout-btn flex-row align-center" @click="handleLogout">
          <el-icon>
            <SwitchButton />
          </el-icon>
          <span>退出</span>
        </div>
      </div>
    </el-header>

    <el-container class="main-body">
      <el-aside width="240px" class="layout-sidebar">
        <el-menu :default-active="route.path" class="sidebar-menu" router>
          <el-menu-item v-for="item in flatMenuItems" :key="item.index" :index="item.index">
            <el-icon>
              <component :is="item.icon" />
            </el-icon>
            <span>{{ item.title }}</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-main class="layout-main">
        <router-view></router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

/* --- 顶部导航栏样式 --- */
.nav-header {
  height: 60px !important;
  background-color: #fff;
  border-bottom: 1px solid #e2e8f0;
  justify-content: space-between;
  padding: 0 30px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
  z-index: 10;

  .header-left {
    gap: 12px;

    .header-logo {
      width: 24px;
      height: 24px;
      color: #3b82f6;
      /* 蓝色匹配 Figma */
    }

    .header-title {
      font-size: 18px;
      font-weight: 700;
      color: #2563eb;
      letter-spacing: 1px;
    }
  }

  .header-right {
    gap: 24px;

    .user-profile {
      gap: 10px;
      cursor: pointer;
      padding: 4px 12px;
      border-radius: 4px;
      transition: background-color 0.2s;

      &:hover {
        background-color: #f1f5f9;
      }

      .user-icon {
        font-size: 20px;
        color: #64748b;
        background-color: #f1f5f9;
        padding: 6px;
        border-radius: 50%;
      }

      .user-info {
        .user-name {
          font-size: 13px;
          font-weight: 600;
          color: #1e293b;
        }

        .user-role {
          font-size: 11px;
          color: #94a3b8;
        }
      }
    }

    .logout-btn {
      gap: 6px;
      color: #64748b;
      font-size: 14px;
      cursor: pointer;
      transition: color 0.2s;
      padding-left: 20px;
      border-left: 1px solid #e2e8f0;

      &:hover {
        color: #ef4444;
      }
    }
  }
}

/* --- 主体部分样式 --- */
.main-body {
  flex: 1;
  overflow: hidden;
  background-color: #f3f5f8; // 匹配 Figma 背景色
}

/* --- 侧边栏菜单样式 --- */
.layout-sidebar {
  background-color: #f8fafc;
  border-right: 1px solid #e2e8f0;
  height: 100%;

  .sidebar-menu {
    border-right: none;
    background-color: transparent;
    padding-top: 20px;

    :deep(.el-menu-item) {
      height: 50px;
      line-height: 50px;
      margin-bottom: 4px;
      margin-left: 12px;
      margin-right: 12px;
      border-radius: 8px;
      color: #475569;
      font-weight: 500;
      font-size: 14px;
      position: relative;
      overflow: hidden;

      &:hover {
        background-color: #f1f5f9;
      }

      &.is-active {
        background-color: #3b82f6; // 蓝色精准匹配
        color: white;

        .el-icon {
          color: white;
        }
      }
    }
  }
}

/* --- 主内容区域样式 --- */
.layout-main {
  padding: 24px;
  overflow-y: auto;
  position: relative;
}

/* Flex 布局工具类继承于全局，以防未导入提供封装 */
.flex-column {
  display: flex;
  flex-direction: column;
}

.flex-row {
  display: flex;
  flex-direction: row;
}

.align-center {
  align-items: center;
}

@keyframes ripple-burst {
  0% {
    transform: translate(-50%, -50%) scale(0);
    opacity: 1;
  }

  100% {
    transform: translate(calc(-50% + var(--tx)), calc(-50% + var(--ty))) scale(1);
    opacity: 0;
  }
}

:deep(.ripple-particle) {
  position: absolute;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgba(59, 130, 246, 0.7);
  pointer-events: none;
  animation: ripple-burst 0.6s ease-out forwards;
  z-index: 100;
}
</style>
