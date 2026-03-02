import request from '@/utils/request'

export function getAdminStat(params: any) {
    return request({
        url: '/stat/admin',
        method: 'get',
        params
    })
}

export function getTeacherStat(params: any) {
    return request({
        url: '/stat/teacher',
        method: 'get',
        params
    })
}

export function getStudentStat() {
    return request({
        url: '/stat/student',
        method: 'get'
    })
}
