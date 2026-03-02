import request from '@/utils/request'

export function getCoursePlanPage(params: any) {
    return request({
        url: '/admin/plan/list',
        method: 'get',
        params
    })
}

export function saveCoursePlan(data: any) {
    return request({
        url: '/admin/plan/save',
        method: 'post',
        data
    })
}

export function updateCoursePlan(data: any) {
    return request({
        url: '/admin/plan/update',
        method: 'put',
        data
    })
}

export function lockCoursePlan(id: number | string) {
    return request({
        url: `/admin/plan/lock/${id}`,
        method: 'put'
    })
}

export function getTeachers() {
    return request({
        url: '/admin/plan/teachers',
        method: 'get'
    })
}

export function getCourses() {
    return request({
        url: '/admin/plan/courses',
        method: 'get'
    })
}
