import request from '@/utils/request'

// 教师端接口
export function getTeacherScoreList(planId: number, params: any) {
    return request({
        url: `/teacher/score/plan/${planId}`,
        method: 'get',
        params
    })
}

export function inputScore(data: any) {
    return request({
        url: '/teacher/score/input',
        method: 'post',
        data
    })
}

export function batchInputScore(data: any) {
    return request({
        url: '/teacher/score/batch',
        method: 'post',
        data
    })
}

export function updatePlanWeights(planId: number, data: any) {
    return request({
        url: `/teacher/score/plan/${planId}/weights`,
        method: 'put',
        data
    })
}

// 学生端接口
export function getMyScores() {
    return request({
        url: '/student/score/mine',
        method: 'get'
    })
}

// 教师端 - 获取我的课程列表
export function getMyPlans(params: any) {
    return request({
        url: '/teacher/plan/my',
        method: 'get',
        params
    })
}