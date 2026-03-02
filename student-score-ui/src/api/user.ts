import request from '@/utils/request'

// 分页查询用户列表
export function getUserPage(params: any) {
  return request({
    url: '/admin/user/page',
    method: 'get',
    params
  })
}

// 新增用户
export function addUser(data: any) {
  return request({
    url: '/admin/user',
    method: 'post',
    data
  })
}

// 修改用户
export function updateUser(data: any) {
  return request({
    url: '/admin/user',
    method: 'put',
    data
  })
}

// 删除用户
export function deleteUser(id: number) {
  return request({
    url: `/admin/user/${id}`,
    method: 'delete'
  })
}

// 更新用户状态
export function updateUserStatus(id: number, status: string) {
  return request({
    url: `/admin/user/${id}/status/${status}`,
    method: 'put'
  })
}
