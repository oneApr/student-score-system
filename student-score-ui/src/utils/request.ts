import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import router from '@/router'

// 创建 axios 实例
const service = axios.create({
    baseURL: '/api', // 所有的请求都会加上 /api 前缀，在 vite.config.ts 中代理到真实后端
    timeout: 10000
})

// 请求拦截器
service.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers['Authorization'] = 'Bearer ' + token
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// 响应拦截器
service.interceptors.response.use(
    response => {
        const res = response.data

        // 如果状态码不是 200，则报错
        if (res.code !== 200) {
            // 优先使用后端返回的 message
            const errorMsg = res.message || '操作失败'

            // 401: Token 过期或无效，弹窗提示 
            if (res.code === 401) {
                ElMessageBox.confirm('登录状态已失效，请您重新登录', '提示', {
                    confirmButtonText: '重新登录',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    localStorage.removeItem('token')
                    localStorage.removeItem('role')
                    router.push('/login')
                })
            }
            // 抛出错误，让调用方捕获并处理
            return Promise.reject(new Error(errorMsg))
        } else {
            // 返回完整的 res，包括 code 字段，让页面可以判断
            return res
        }
    },
    error => {
        // 403: 无权限
        if (error.response?.status === 403) {
            ElMessage({
                message: '无权限访问',
                type: 'error',
                duration: 3 * 1000
            })
        } else if (error.response?.status === 401) {
            ElMessageBox.confirm('登录状态已失效，请您重新登录', '提示', {
                confirmButtonText: '重新登录',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                localStorage.removeItem('token')
                localStorage.removeItem('role')
                router.push('/login')
            })
        } else if (error.response?.status === 400) {
            ElMessage({
                message: error.response?.data?.message || '请求参数错误',
                type: 'error',
                duration: 3 * 1000
            })
        } else if (error.code === 'ECONNABORTED') {
            ElMessage({
                message: '请求超时，请稍后重试',
                type: 'error',
                duration: 3 * 1000
            })
        } else {
            ElMessage({
                message: error.message || '网络错误',
                type: 'error',
                duration: 3 * 1000
            })
        }
        return Promise.reject(error)
    }
)

export default service
