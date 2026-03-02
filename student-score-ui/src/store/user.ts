import { defineStore } from 'pinia'
import { login as loginApi, getInfo, logout as logoutApi } from '@/api/auth'
import router, { resetRoutesAdded } from '@/router'

export const useUserStore = defineStore('user', {
    state: () => ({
        token: localStorage.getItem('token') || '',
        userInfo: null as any,
        roles: [] as string[],
        menuList: [] as any[]
    }),
    actions: {
        // 登录
        async login(loginForm: { username: string; password: string; role?: string }) {
            const res = await loginApi(loginForm)

            // 登录接口返回 token
            const token = res.token || res.data?.token
            if (token) {
                this.token = token
                localStorage.setItem('token', token)
            }

            // 获取用户信息进行角色验证
            const infoRes = await getInfo()
            if (infoRes.code === 200) {
                const userRoles = infoRes.data.roles || []

                // 角色验证：检查用户角色是否匹配选择的角色
                if (loginForm.role) {
                    const roleKey = loginForm.role === 'admin' ? 'admin' :
                        loginForm.role === 'teacher' ? 'teacher' : 'student'
                    const hasRole = userRoles.some((r: string) => r.toLowerCase() === roleKey.toLowerCase())

                    if (!hasRole) {
                        // 角色不匹配，清除 token 并抛出错误
                        this.token = ''
                        localStorage.removeItem('token')
                        const roleName = loginForm.role === 'admin' ? '管理员' :
                            loginForm.role === 'teacher' ? '教师' : '学生'
                        throw new Error(`请使用${roleName}账号登录`)
                    }
                }

                // 保存用户信息
                this.userInfo = infoRes.data
                this.roles = userRoles
                this.menuList = infoRes.data.menuList || []
            }

            return res
        },
        // 获取用户信息
        async getUserInfo() {
            const res = await getInfo()
            if (res.code === 200) {
                this.userInfo = res.data
                this.roles = res.data.roles || []
                this.menuList = res.data.menuList || []
                return res.data
            }
            throw new Error(res.message || '获取用户信息失败')
        },
        // 登出
        async logout() {
            try {
                await logoutApi()
            } catch (e) {
                // 忽略登出接口错误
            }
            this.token = ''
            this.userInfo = null
            this.roles = []
            this.menuList = []
            localStorage.removeItem('token')
            localStorage.removeItem('role')
            resetRoutesAdded()
            router.push('/login')
        }
    }
})
