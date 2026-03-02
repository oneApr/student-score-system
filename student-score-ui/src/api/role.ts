import request from '@/utils/request'

// 获取角色列表
export function getRoleList(params: any) {
  return request({
    url: '/admin/role/page',
    method: 'get',
    params
  })
}

// 获取角色下拉列表
export function getAllRoles() {
  return request({
    url: '/admin/role/list',
    method: 'get'
  })
}

// 新增角色
export function addRole(data: any) {
  return request({
    url: '/admin/role',
    method: 'post',
    data
  })
}

// 修改角色
export function updateRole(data: any) {
  return request({
    url: '/admin/role',
    method: 'put',
    data
  })
}

// 删除角色
export function deleteRole(id: number) {
  return request({
    url: `/admin/role/${id}`,
    method: 'delete'
  })
}
