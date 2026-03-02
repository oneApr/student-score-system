<script setup lang="ts">
import { nextTick, ref, onMounted, reactive } from 'vue'
import { Plus, Search, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoleList, addRole, updateRole, deleteRole } from '@/api/role'
import { getMenuTree, addMenu, updateMenu, deleteMenu } from '@/api/menu'

const activeTab = ref('role')
const loading = ref(false)

const roleList = ref<any[]>([])
const rolePagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const roleDialogVisible = ref(false)
const roleDialogTitle = ref('')
const roleFormRef = ref()
const roleForm = ref({
  roleId: undefined as number | undefined,
  roleName: '',
  roleKey: '',
  roleDesc: '',
  status: 1
})
const roleRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleKey: [{ required: true, message: '请输入角色标识', trigger: 'blur' }]
}

// 菜单管理状态
const menuData = ref<any[]>([])
const menuDialogVisible = ref(false)
const menuDialogTitle = ref('')
const menuFormRef = ref()
const menuForm = ref({
  menuId: undefined as number | undefined,
  parentId: 0,
  menuName: '',
  perms: '',
  path: '',
  component: '',
  menuType: 'C',
  icon: '',
  sortOrder: 0,
  status: 1
})
const menuRules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }]
}
const defaultMenuProps = { children: 'children', label: 'menuName' }

// 角色菜单分配相关状态
// 角色菜单分配相关状态
// (保留挂载占位，如有需要后续再启用)


// 角色处理函数
const fetchRoles = async () => {
  loading.value = true
  try {
    const res = await getRoleList({
      pageNum: rolePagination.pageNum,
      pageSize: rolePagination.pageSize
    }) as any
    if (res.code === 200) {
      roleList.value = res.data?.list || []
      rolePagination.total = res.data?.total || 0
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '获取角色列表失败')
  } finally {
    loading.value = false
  }
}

const handleAddRole = () => {
  roleForm.value = { roleId: undefined, roleName: '', roleKey: '', roleDesc: '', status: 1 }
  roleDialogTitle.value = '新增角色'
  roleDialogVisible.value = true
}

const handleEditRole = (row: any) => {
  roleForm.value = { ...row }
  roleDialogTitle.value = '编辑角色'
  roleDialogVisible.value = true
}

const handleDeleteRole = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认删除该角色？', '提示', { type: 'warning' })
    await deleteRole(row.roleId)
    ElMessage.success('删除成功')
    fetchRoles()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error.message || '删除失败')
  }
}

const handleSubmitRole = async () => {
  if (!roleFormRef.value) return
  try {
    await roleFormRef.value.validate()
    if (roleForm.value.roleId) {
      await updateRole(roleForm.value)
      ElMessage.success('修改成功')
    } else {
      await addRole(roleForm.value)
      ElMessage.success('新增成功')
    }
    roleDialogVisible.value = false
    await nextTick()
    await fetchRoles()
  } catch (error: any) {
    if (error !== false) {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

// 菜单处理函数
const fetchMenus = async () => {
  try {
    const res = await getMenuTree() as any
    if (res.code === 200) {
      menuData.value = res.data || []
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '获取菜单失败')
  }
}

const handleAddMenu = () => {
  menuForm.value = {
    menuId: undefined, parentId: 0, menuName: '', perms: '', path: '',
    component: '', menuType: 'C', icon: '', sortOrder: 0, status: 1
  }
  menuDialogTitle.value = '新增菜单'
  menuDialogVisible.value = true
}

const handleEditMenu = (row: any) => {
  menuForm.value = { ...row }
  menuDialogTitle.value = '编辑菜单'
  menuDialogVisible.value = true
}

const handleDeleteMenu = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认删除该菜单？', '提示', { type: 'warning' })
    await deleteMenu(row.menuId)
    ElMessage.success('删除成功')
    fetchMenus()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error.message || '删除失败')
  }
}

const handleSubmitMenu = async () => {
  if (!menuFormRef.value) return
  try {
    await menuFormRef.value.validate()
    if (menuForm.value.menuId) {
      await updateMenu(menuForm.value)
      ElMessage.success('修改成功')
    } else {
      await addMenu(menuForm.value)
      ElMessage.success('新增成功')
    }
    menuDialogVisible.value = false
    await nextTick()
    await fetchMenus()
  } catch (error: any) {
    if (error !== false) {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

onMounted(() => {
  fetchRoles()
  fetchMenus()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>系统权限管理</h2>
      <p>基于 RBAC 的权限配置</p>
    </div>

    <el-tabs v-model="activeTab" class="perm-tabs">
      <el-tab-pane label="角色管理" name="role">
        <div class="action-bar">
          <div class="filter-group">
            <el-input placeholder="搜索角色..." style="width: 200px;" :prefix-icon="Search" clearable />
          </div>
          <div class="action-group">
            <el-button type="primary" :icon="Plus" @click="handleAddRole">新增角色</el-button>
          </div>
        </div>
        <el-table :data="roleList" v-loading="loading" row-key="roleId">
          <el-table-column type="index" label="序号" width="70" align="center" />

          <el-table-column align="center" prop="roleName" label="角色名称" />

          <el-table-column align="center" prop="roleKey" label="角色标识" />

          <el-table-column prop="roleDesc" label="描述" />

          <el-table-column align="center" prop="status" label="状态">

            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                {{ row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column align="center" label="操作">

            <template #default="scope">
              <el-link type="primary" :icon="Edit" @click="handleEditRole(scope.row)" />
              <el-link type="danger" :icon="Delete" @click="handleDeleteRole(scope.row)" style="margin-left: 12px;" />
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="菜单管理" name="menu">
        <div class="action-bar">
          <div class="filter-group"></div>
          <div class="action-group">
            <el-button type="primary" :icon="Plus" @click="handleAddMenu">新增菜单</el-button>
          </div>
        </div>
        <el-tree :data="menuData" :props="defaultMenuProps" default-expand-all>
          <template #default="{ data }">
            <span class="menu-tree-node">
              <span>{{ data.menuName }}</span>
              <span class="menu-perms">{{ data.perms }}</span>
              <span class="menu-actions">
                <el-link type="primary" :icon="Edit" @click.stop="handleEditMenu(data)" />
                <el-link type="danger" :icon="Delete" @click.stop="handleDeleteMenu(data)" style="margin-left: 8px;" />
              </span>
            </span>
          </template>
        </el-tree>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="roleDialogVisible" :title="roleDialogTitle" width="500px">
      <el-form ref="roleFormRef" :model="roleForm" :rules="roleRules" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="roleForm.roleName" />
        </el-form-item>
        <el-form-item label="角色标识" prop="roleKey">
          <el-input v-model="roleForm.roleKey" />
        </el-form-item>
        <el-form-item label="描述" prop="roleDesc">
          <el-input v-model="roleForm.roleDesc" type="textarea" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="roleForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitRole">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="menuDialogVisible" :title="menuDialogTitle" width="600px">
      <el-form ref="menuFormRef" :model="menuForm" :rules="menuRules" label-width="100px">
        <el-form-item label="上级菜单">
          <el-tree-select v-model="menuForm.parentId" :data="menuData" :props="defaultMenuProps" check-strictly />
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="menuForm.menuName" />
        </el-form-item>
        <el-form-item label="权限标识">
          <el-input v-model="menuForm.perms" placeholder="如: edu:course:list" />
        </el-form-item>
        <el-form-item label="路由路径">
          <el-input v-model="menuForm.path" />
        </el-form-item>
        <el-form-item label="组件路径">
          <el-input v-model="menuForm.component" />
        </el-form-item>
        <el-form-item label="菜单类型">
          <el-radio-group v-model="menuForm.menuType">
            <el-radio value="C">目录</el-radio>
            <el-radio value="M">菜单</el-radio>
            <el-radio value="B">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="menuForm.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="menuForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="menuDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitMenu">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.perm-tabs {
  background: white;
  padding: 20px;
  border-radius: 6px;
}

.menu-tree-node {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  padding-right: 8px;

  .menu-perms {
    font-size: 12px;
    color: #94a3b8;
  }

  .menu-actions {
    margin-left: auto;
  }
}
</style>
