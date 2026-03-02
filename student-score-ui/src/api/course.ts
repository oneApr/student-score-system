import request from '@/utils/request'

// 分页查询课程列表
export function getCoursePage(params: any) {
  return request({
    url: '/admin/course/page',
    method: 'get',
    params
  })
}

// 新增课程
export function addCourse(data: any) {
  return request({
    url: '/admin/course',
    method: 'post',
    data
  })
}

// 编辑课程
export function updateCourse(data: any) {
  return request({
    url: '/admin/course',
    method: 'put',
    data
  })
}

// 删除课程
export function deleteCourse(id: number) {
  return request({
    url: `/admin/course/${id}`,
    method: 'delete'
  })
}
