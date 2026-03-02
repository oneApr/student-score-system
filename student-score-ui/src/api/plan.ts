import request from '@/utils/request'

// 分页获取排课计划列表
export function getCoursePlanPage(params: any) {
  return request({
    url: '/admin/plan/list',
    method: 'get',
    params
  })
}

// 创建排课计划
export function addCoursePlan(data: any) {
  return request({
    url: '/admin/plan',
    method: 'post',
    data
  })
}

// 更新排课计划
export function updateCoursePlan(data: any) {
  return request({
    url: '/admin/plan',
    method: 'put',
    data
  })
}

// 删除排课计划
export function deleteCoursePlan(id: number) {
  return request({
    url: `/admin/plan/${id}`,
    method: 'delete'
  })
}
