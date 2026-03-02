import request from '@/utils/request'

export function login(data: { username: string; password: string; role?: string }) {
    return request({
        url: '/auth/login',
        method: 'post',
        data
    })
}

export function getInfo() {
    return request({
        url: '/auth/info',
        method: 'get'
    })
}

export function logout() {
    return request({
        url: '/auth/logout',
        method: 'post'
    })
}

// 忘记密码重置（无需登录）
export function resetPassword(data: { username: string; newPassword: string }) {
    return request({
        url: '/auth/password/reset',
        method: 'post',
        data
    })
}

// 验证用户名是否存在（用于忘记密码第一步）
export function verifyUsername(username: string) {
    return request({
        url: '/auth/username/verify',
        method: 'get',
        params: { username }
    })
}

export function register(data: any) {
    return request({
        url: '/auth/register',
        method: 'post',
        data
    })
}
