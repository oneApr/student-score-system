import request from '@/utils/request'

// 获取菜单树
export function getMenuTree() {
  return request({
    url: '/admin/menu/tree',
    method: 'get'
  })
}

// 新增菜单
export function addMenu(data: any) {
  return request({
    url: '/admin/menu',
    method: 'post',
    data
  })
}

// 修改菜单
export function updateMenu(data: any) {
  return request({
    url: '/admin/menu',
    method: 'put',
    data
  })
}

// 删除菜单
export function deleteMenu(id: number) {
  return request({
    url: `/admin/menu/${id}`,
    method: 'delete'
  })
}
