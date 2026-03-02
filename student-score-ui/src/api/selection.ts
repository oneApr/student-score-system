import request from '@/utils/request'

// 选课管理 - 分页查询
export function getStudentCourseList(params: any) {
  return request({
    url: '/admin/selection/page',
    method: 'get',
    params
  })
}

// 手动添加选课
export function addStudentCourse(data: any) {
  return request({
    url: '/admin/selection',
    method: 'post',
    data
  })
}

// 删除学生选课
export function deleteStudentCourse(id: number) {
  return request({
    url: `/admin/selection/${id}`,
    method: 'delete'
  })
}
