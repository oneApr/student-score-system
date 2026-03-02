import request from '@/utils/request'

// 获取组织架构树
export function getOrgTree() {
  return request({
    url: '/admin/org/tree',
    method: 'get'
  })
}

// 新增组织节点
export function addOrg(data: any) {
  return request({
    url: '/admin/org',
    method: 'post',
    data
  })
}

// 修改组织节点
export function updateOrg(data: any) {
  return request({
    url: '/admin/org',
    method: 'put',
    data
  })
}

// 删除组织节点
export function deleteOrg(id: number) {
  return request({
    url: `/admin/org/${id}`,
    method: 'delete'
  })
}
