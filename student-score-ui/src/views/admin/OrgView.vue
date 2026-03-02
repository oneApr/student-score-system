<script setup lang="ts">
import { nextTick, ref, onMounted, computed } from 'vue'
import { Plus, Delete, Edit } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrgTree, addOrg, updateOrg, deleteOrg } from '@/api/org'

interface OrgNode {
  orgId: number
  orgName: string
  orgType: string
  parentId: number
  sortOrder: number
  status: number
  children?: OrgNode[]
}

const loading = ref(false)
const orgData = ref<OrgNode[]>([])
const selectedNode = ref<OrgNode | null>(null)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const form = ref({
  orgId: undefined as number | undefined,
  orgName: '',
  orgType: '',
  parentId: 0,
  sortOrder: 0,
  status: 1
})

const rules = {
  orgType: [
    { required: true, message: '请选择组织类型', trigger: 'change' }
  ],
  orgName: [
    { required: true, message: '请输入组织名称', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (!value) {
          callback()
          return
        }
        const orgType = form.value.orgType
        if (orgType === '学院' && !value.endsWith('学院')) {
          callback(new Error('学院名称必须以"学院"结尾'))
        } else if (orgType === '专业' && !value.endsWith('专业')) {
          callback(new Error('专业名称必须以"专业"结尾'))
        } else if (orgType === '班级') {
          // 班级格式：xx202x级x班，如"软件工程2021级1班"
          const classPattern = /^[\u4e00-\u9fa5]+202\d级\d+班$/
          if (!classPattern.test(value)) {
            callback(new Error('班级格式应为"xxx202x级x班"，如"软件工程2021级1班"'))
          } else {
            callback()
          }
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 保存新增时的父节点信息（对话框打开时）
const parentOrgInfo = ref<{ orgId: number; orgName: string; orgType: string } | null>(null)

// 根据当前选中的节点，推断可以添加的组织类型
const availableAddTypes = computed(() => {
  const parent = selectedNode.value
  if (!parent) {
    // 没有选中任何节点，只能添加学校
    return ['学校']
  }
  // 根据父节点类型，返回可添加的子类型
  switch (parent.orgType) {
    case '学校':
      return ['学院']
    case '学院':
      return ['专业']
    case '专业':
      return ['班级']
    default:
      return []
  }
})

// 是否可以新增子节点
const canAddChild = computed(() => {
  return availableAddTypes.value.length > 0
})

const defaultProps = {
  children: 'children',
  label: 'orgName'
}

// 获取组织架构树
const fetchOrgTree = async () => {
  loading.value = true
  try {
    const res = await getOrgTree() as any
    if (res.code === 200) {
      orgData.value = res.data || []
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '获取组织架构失败')
  } finally {
    loading.value = false
  }
}

// 选择节点
const handleNodeClick = (data: OrgNode) => {
  selectedNode.value = data
}

// 新增节点
const handleAdd = () => {
  // 保存父节点信息（在对话框打开前保存）
  parentOrgInfo.value = selectedNode.value || null

  // 设置默认类型为可选类型中的第一个
  form.value = {
    orgId: undefined,
    orgName: '',
    orgType: availableAddTypes.value[0] || '',
    parentId: selectedNode.value?.orgId || 0,
    sortOrder: 0,
    status: 1
  }
  dialogTitle.value = '新增组织节点'
  dialogVisible.value = true
}

// 编辑节点
const handleEdit = () => {
  if (!selectedNode.value) return
  form.value = {
    orgId: selectedNode.value.orgId,
    orgName: selectedNode.value.orgName,
    orgType: selectedNode.value.orgType,
    parentId: selectedNode.value.parentId,
    sortOrder: selectedNode.value.sortOrder,
    status: selectedNode.value.status
  }
  dialogTitle.value = '编辑组织节点'
  dialogVisible.value = true
}

// 删除节点
const handleDelete = async () => {
  if (!selectedNode.value) return
  try {
    await ElMessageBox.confirm('确认删除该组织节点？', '提示', {
      type: 'warning'
    })
    await deleteOrg(selectedNode.value.orgId)
    ElMessage.success('删除成功')
    selectedNode.value = null
    fetchOrgTree()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    // 层级校验
    const parentType = parentOrgInfo.value?.orgType
    const childType = form.value.orgType

    // 顶级学校校验
    if (childType === '学校' && form.value.parentId !== 0) {
      ElMessage.error('学校只能作为顶级组织（上级组织为空）')
      return
    }

    // 学院必须在学校下
    if (childType === '学院' && parentType !== '学校' && form.value.parentId !== 0) {
      ElMessage.error('学院必须挂在学校下面')
      return
    }

    // 专业必须在学院下
    if (childType === '专业' && parentType !== '学院') {
      ElMessage.error('专业必须挂在学院下面')
      return
    }

    // 班级必须在专业下
    if (childType === '班级' && parentType !== '专业') {
      ElMessage.error('班级必须挂在专业下面')
      return
    }

    if (form.value.orgId) {
      await updateOrg(form.value)
      ElMessage.success('修改成功')
    } else {
      await addOrg(form.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await nextTick()
    await fetchOrgTree()
  } catch (error: any) {
    if (error !== false) {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

onMounted(() => {
  fetchOrgTree()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>学术组织架构管理</h2>
      <p>维护学校 / 学院 / 专业 / 班级的层级关系</p>
    </div>

    <div class="org-layout">
      <div class="org-tree-panel">
        <div class="panel-header">
          <span class="panel-title">组织架构树</span>
          <el-button type="primary" :icon="Plus" size="small" :disabled="!canAddChild" @click="handleAdd">新增</el-button>
        </div>
        <el-tree :data="orgData" :props="defaultProps" default-expand-all class="custom-tree" :loading="loading"
          @node-click="handleNodeClick">
          <template #default="{ node, data }">
            <span class="custom-tree-node">
              <span>{{ node.label }}</span>
              <span class="node-type">({{ data.orgType }})</span>
            </span>
          </template>
        </el-tree>
      </div>

      <div class="org-detail-panel">
        <template v-if="selectedNode">
          <div class="detail-header">
            <h3>节点详情</h3>
            <div class="detail-actions">
              <el-button type="primary" :icon="Edit" size="small" @click="handleEdit">编辑</el-button>
              <el-button type="danger" :icon="Delete" size="small" @click="handleDelete">删除</el-button>
            </div>
          </div>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="组织名称">{{ selectedNode.orgName }}</el-descriptions-item>
            <el-descriptions-item label="组织类型">{{ selectedNode.orgType }}</el-descriptions-item>
            <el-descriptions-item label="排序">{{ selectedNode.sortOrder }}</el-descriptions-item>
            <el-descriptions-item label="状态">{{ selectedNode.status === 1 ? '启用' : '禁用' }}</el-descriptions-item>
          </el-descriptions>
        </template>
        <div v-else class="empty-state">
          <span class="empty-text">请从左侧选择一个节点查看详情</span>
        </div>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="组织名称" prop="orgName">
          <el-input v-model="form.orgName" placeholder="请输入组织名称" />
        </el-form-item>
        <el-form-item label="组织类型" prop="orgType">
          <el-select v-model="form.orgType" placeholder="请选择组织类型">
            <el-option v-for="type in availableAddTypes" :key="type" :label="type" :value="type" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.org-layout {
  display: flex;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  height: calc(100vh - 240px);
}

.org-tree-panel {
  width: 320px;
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e2e8f0;

  .panel-title {
    font-size: 14px;
    font-weight: 600;
    color: #1e293b;
  }
}

.custom-tree {
  padding: 16px;
  overflow-y: auto;
  flex: 1;
  --el-tree-node-hover-bg-color: #f1f5f9;
}

.custom-tree-node {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #1e293b;

  .node-type {
    font-size: 12px;
    color: #94a3b8;
  }
}

.org-detail-panel {
  flex: 1;
  background-color: #f8fafc;
  padding: 20px;

  .detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h3 {
      margin: 0;
      font-size: 16px;
      color: #1e293b;
    }
  }

  .empty-state {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100%;

    .empty-text {
      color: #94a3b8;
      font-size: 14px;
    }
  }
}
</style>
