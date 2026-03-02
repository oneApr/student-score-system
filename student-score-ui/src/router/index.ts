import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

// 提前导入所有组件（解决 Vite 动态导入变量问题）
import OrgView from '../views/admin/OrgView.vue'
import CourseInfoView from '../views/admin/CourseInfoView.vue'
import CoursePlanView from '../views/admin/CoursePlanView.vue'
import StudentsView from '../views/admin/StudentsView.vue'
import CourseSelectionView from '../views/admin/CourseSelectionView.vue'
import AuditView from '../views/admin/AuditView.vue'
import DashboardView from '../views/admin/DashboardView.vue'
import ReportsView from '../views/admin/ReportsView.vue'
import PermissionsView from '../views/admin/PermissionsView.vue'
import TeacherCoursesView from '../views/teacher/CoursesView.vue'
import TeacherEntryView from '../views/teacher/EntryView.vue'
import TeacherStatusView from '../views/teacher/StatusView.vue'
import TeacherStatView from '../views/teacher/TeacherStatView.vue'
import StudentQueryView from '../views/student/QueryView.vue'
import StudentStatisticView from '../views/student/StatisticServiceView.vue'
import StudentCertView from '../views/student/CertView.vue'

// 登录页路由
const loginRoute = {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue')
}

// Layout 布局路由（父路由）
const layoutRoute = {
    path: '/',
    name: 'layout',
    component: () => import('../views/LayoutView.vue'),
    redirect: '',
    meta: { requiresAuth: true },
    children: []
}

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [loginRoute, layoutRoute, {
        path: '/:pathMatch(.*)*',
        redirect: '/login'
    }]
})

// 组件映射表：根据后端返回的 component 字段匹配前端组件
// 数据库菜单 component 字段映射：
// admin/AcademicStructure -> 学术组织架构
// admin/CourseManagement -> 课程信息管理
// admin/CoursePlanManagement -> 开课计划管理
// admin/StudentInfoManagement -> 学生信息管理
// admin/CourseSelectionManagement -> 学生选课管理
// admin/GradeAuditManagement -> 成绩审核流程
// admin/GradeStatistics -> 成绩查询与统计
// admin/ReportCenter -> 报表中心
// admin/PermissionManagement -> 系统权限管理
// teacher/xxx -> 教师相关页面
// student/xxx -> 学生相关页面
const getComponent = (componentPath: string) => {
    const map: Record<string, any> = {
        // 管理员菜单
        'admin/AcademicStructure': OrgView,
        'admin/CourseManagement': CourseInfoView,
        'admin/CoursePlanManagement': CoursePlanView,
        'admin/StudentInfoManagement': StudentsView,
        'admin/CourseSelectionManagement': CourseSelectionView,
        'admin/GradeAuditManagement': AuditView,
        'admin/GradeStatistics': DashboardView,
        'admin/ReportCenter': ReportsView,
        'admin/PermissionManagement': PermissionsView,
        // 教师菜单
        'teacher/TeacherCourseManagement': TeacherCoursesView,
        'teacher/GradeEntry': TeacherEntryView,
        'teacher/GradeSubmitStatus': TeacherStatusView,
        'teacher/TeacherGradeQuery': TeacherStatView,
        // 学生菜单
        'student/StudentGradeQuery': StudentQueryView,
        'student/StatisticService': StudentStatisticView,
        'student/CertificateApplication': StudentCertView,
    }
    return map[componentPath]
}

// 动态添加路由
export function addDynamicRoutes(menuList: any[]) {
    if (!menuList || menuList.length === 0) return

    menuList.forEach(menu => {
        if (menu.children && menu.children.length > 0) {
            menu.children.forEach((child: any) => {
                if (!child.component) return

                // 根据后端 component 字段获取组件
                const component = getComponent(child.component)
                if (!component) {
                    console.warn(`未找到组件映射: ${child.component}`)
                    return
                }

                const routeConfig: any = {
                    path: child.path,
                    name: child.path.replace(/\//g, '-'),
                    component: component,
                    meta: {
                        requiresAuth: true,
                        title: child.menuName,
                        perms: child.perms
                    }
                }

                const exists = router.getRoutes().some(r => r.path === '/' + child.path)
                if (!exists) {
                    router.addRoute('layout', routeConfig)
                }
            })
        }
    })
}

// 获取用户第一个有权限的菜单路径
export function getFirstMenuPath(menuList: any[]): string {
    if (!menuList || menuList.length === 0) return '/dashboard'

    for (const menu of menuList) {
        if (menu.children && menu.children.length > 0) {
            const firstChild = menu.children[0]
            if (firstChild && firstChild.path) {
                return '/' + firstChild.path
            }
        }
    }
    return '/dashboard'
}

// 标记动态路由是否已添加（避免重复添加导致死循环）
let routesAdded = false

export function resetRoutesAdded() {
    routesAdded = false
}

// 路由守卫
router.beforeEach(async (to, _from, next) => {
    const userStore = useUserStore()
    const token = localStorage.getItem('token')

    // 无 token 且不在登录页 → 跳到登录
    if (!token && to.path !== '/login') {
        next('/login')
        return
    }

    // 无 token 直接放行（去登录页）
    if (!token) {
        next()
        return
    }

    // 有 token 但还没获取用户信息 → 先获取
    if (!userStore.userInfo) {
        try {
            const userInfo = await userStore.getUserInfo()
            if (userInfo && userInfo.menuList) {
                addDynamicRoutes(userInfo.menuList)
                routesAdded = true
                next({ ...to, replace: true })
                return
            }
        } catch (error) {
            localStorage.removeItem('token')
            routesAdded = false
            next('/login')
            return
        }
    }

    // 有 token 有用户信息，但动态路由尚未添加（如页面刷新后 Pinia 恢复了 state 但路由丢失）
    if (!routesAdded && userStore.menuList.length > 0) {
        addDynamicRoutes(userStore.menuList)
        routesAdded = true
        next({ ...to, replace: true })
        return
    }

    // 已登录用户访问 login 页 → 重定向到首页
    if (to.path === '/login') {
        const redirectPath = getFirstMenuPath(userStore.menuList)
        next(redirectPath)
        return
    }

    next()
})

export default router
