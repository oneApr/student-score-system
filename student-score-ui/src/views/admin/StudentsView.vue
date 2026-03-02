<script setup lang="ts">
import { nextTick, ref, onMounted, reactive } from 'vue'
import { Plus, Search, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserPage, addUser, updateUser, deleteUser, updateUserStatus } from '@/api/user'

const loading = ref(false)
const tableKey = ref(0)
const searchQuery = ref('')
const statusFilter = ref('')
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const form = ref({
  userId: undefined as number | undefined,
  username: '',
  nickname: '',
  password: '',
  studentNo: '',
  gender: '0',
  grade: '',
  major: '',
  className: '',
  roleId: undefined as number | undefined,
  status: 1
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

// 获取学生列表
const fetchStudentList = async () => {
  loading.value = true
  try {
    const res = await getUserPage({
      keyword: searchQuery.value,
      status: statusFilter.value,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }) as any
    if (res.code === 200) {
      tableData.value = res.data?.list || []
      pagination.total = res.data?.total || 0
      tableKey.value++
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '获取学生列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  fetchStudentList()
}

// 分页变化
const handlePageChange = (page: number) => {
  pagination.pageNum = page
  fetchStudentList()
}

const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  fetchStudentList()
}

// 新增学生
const handleAdd = () => {
  form.value = {
    userId: undefined,
    username: '',
    nickname: '',
    password: '',
    studentNo: '',
    gender: '0',
    grade: '',
    major: '',
    className: '',
    roleId: 3, // 默认学生角色
    status: 1
  }
  dialogTitle.value = '新增学生'
  dialogVisible.value = true
}

// 编辑学生
const handleEdit = (row: any) => {
  form.value = {
    userId: row.userId,
    username: row.username,
    nickname: row.nickname,
    password: '',
    studentNo: row.studentNo,
    gender: row.gender || '0',
    grade: row.grade || '',
    major: row.major || '',
    className: row.className || '',
    roleId: 3, // 默认学生角色
    status: row.status
  }
  dialogTitle.value = '编辑学生'
  dialogVisible.value = true
}

// 删除学生
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认删除该学生？', '提示', { type: 'warning' })
    await deleteUser(row.userId)
    ElMessage.success('删除成功')
    fetchStudentList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 切换状态
const handleStatusChange = async (row: any, newStatus: number) => {
  const oldStatus = row.status
  try {
    const res = await updateUserStatus(row.userId, String(newStatus)) as any
    if (res.code !== 200) {
      throw new Error(res.message || '状态更新失败')
    }
    ElMessage.success('状态更新成功')
  } catch (error: any) {
    row.status = oldStatus
    ElMessage.error(error?.message || '状态更新失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    if (form.value.userId) {
      // 编辑时只传用户信息，不传角色ID，避免把角色删掉
      const editData = {
        userId: form.value.userId,
        username: form.value.username,
        nickname: form.value.nickname,
        status: form.value.status
      }
      await updateUser(editData)
      ElMessage.success('修改成功')
    } else {
      await addUser(form.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await nextTick()
    await fetchStudentList()
  } catch (error: any) {
    if (error !== false) {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

onMounted(() => {
  fetchStudentList()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>学生信息管理</h2>
      <p>查看与管理学生学籍信息</p>
    </div>

    <div class="action-bar">
      <div class="filter-group" style="display: flex; gap: 12px; width: 500px;">
        <el-input v-model="searchQuery" placeholder="搜索账号姓名..." :prefix-icon="Search" clearable
          @keyup.enter="handleSearch" @clear="handleSearch" style="flex: 1;" />
        <el-select v-model="statusFilter" placeholder="全部状态" style="width: 150px;" @change="handleSearch">
          <el-option label="全部状态" value="" />
          <el-option label="在读" value="1" />
          <el-option label="退学" value="0" />
        </el-select>
      </div>
      <div class="action-group">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增学生</el-button>
      </div>
    </div>

    <el-table :data="tableData" class="custom-table" style="width: 100%" v-loading="loading" :key="tableKey">
      <el-table-column type="index" label="序号" width="70" align="center" />

      <el-table-column prop="studentNo" label="学号" />

      <el-table-column prop="nickname" label="学生姓名" />

      <el-table-column prop="username" label="账号姓名" />

      <el-table-column align="center" prop="gender" label="性别">

        <template #default="scope">
          <span>{{ scope.row.gender === '0' ? '男' : (scope.row.gender === '1' ? '女' : '未知') }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" prop="grade" label="年级" />

      <el-table-column prop="major" label="专业" />

      <el-table-column prop="className" label="班级" />

      <el-table-column align="center" label="学籍状态">

        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '在读' : '退学' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="状态开关">

        <template #default="scope">
          <el-switch v-model="scope.row.status" :active-value="1" :inactive-value="0"
            @change="(val: number) => handleStatusChange(scope.row, val)" />
        </template>
      </el-table-column>
      <el-table-column align="center" label="操作" fixed="right">

        <template #default="scope">
          <div class="flex-row" style="gap: 12px; height: 100%; align-items: center; justify-content: center;">
            <el-link type="primary" :icon="Edit" :underline="false" @click="handleEdit(scope.row)"></el-link>
            <el-link type="danger" :icon="Delete" :underline="false" @click="handleDelete(scope.row)"></el-link>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="pagination.pageNum" v-model:page-size="pagination.pageSize"
      :page-sizes="[10, 20, 50, 100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange" @current-change="handlePageChange"
      style="margin-top: 20px; justify-content: flex-end;" />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="姓名" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!form.userId">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="学号">
          <el-input v-model="form.studentNo" placeholder="请输入学号" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio value="0">男</el-radio>
            <el-radio value="1">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="年级">
          <el-input v-model="form.grade" placeholder="如：2024" />
        </el-form-item>
        <el-form-item label="专业">
          <el-input v-model="form.major" placeholder="如：软件工程" />
        </el-form-item>
        <el-form-item label="班级">
          <el-input v-model="form.className" placeholder="如：软件工程1班" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">在读</el-radio>
            <el-radio :value="0">退学</el-radio>
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

<style scoped lang="scss"></style>
