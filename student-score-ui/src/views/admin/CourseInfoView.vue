<script setup lang="ts">
import { nextTick, ref, onMounted, reactive } from 'vue'
import { Plus, Search, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCoursePage, addCourse, updateCourse, deleteCourse } from '@/api/course'
import { getOrgTree } from '@/api/org'

const loading = ref(false)
const tableKey = ref(0)
const searchQuery = ref('')
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
  courseId: undefined as number | undefined,
  courseCode: '',
  courseName: '',
  credit: 0,
  hours: 0,
  orgId: undefined as number | undefined,
  courseType: ''
})

// 学院列表（用于开课院系选择）
const orgOptions = ref<{ orgId: number; orgName: string }[]>([])

const rules = {
  courseCode: [{ required: true, message: '请输入课程代码', trigger: 'blur' }],
  courseName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  credit: [{ required: true, message: '请输入学分', trigger: 'blur' }],
  hours: [{ required: true, message: '请输入学时', trigger: 'blur' }],
  orgId: [{ required: true, message: '请选择开课院系', trigger: 'change' }],
  courseType: [{ required: true, message: '请选择课程类型', trigger: 'change' }],
}

const courseTypeOptions = [
  { label: '专业核心课', value: '专业核心课' },
  { label: '专业选修课', value: '专业选修课' },
  { label: '公共基础课', value: '公共基础课' },
  { label: '公共选修课', value: '公共选修课' }
]

// 从组织树中提取学院列表
const extractColleges = (nodes: any[]): { orgId: number; orgName: string }[] => {
  const result: { orgId: number; orgName: string }[] = []
  const dfs = (list: any[]) => {
    list.forEach(item => {
      if (item.orgType === '学院') {
        result.push({ orgId: item.orgId, orgName: item.orgName })
      }
      if (item.children && item.children.length) {
        dfs(item.children)
      }
    })
  }
  dfs(nodes)
  return result
}

// 获取课程列表
const fetchCourseList = async () => {
  loading.value = true
  try {
    const res = await getCoursePage({
      keyword: searchQuery.value,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }) as any
    if (res.code === 200) {
      tableData.value = res.data?.list || []
      pagination.total = res.data?.total || 0
      tableKey.value++ // 强制刷新表格
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '获取课程列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  fetchCourseList()
}

// 分页变化
const handlePageChange = (page: number) => {
  pagination.pageNum = page
  fetchCourseList()
}

const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  fetchCourseList()
}

// 新增课程
const handleAdd = () => {
  form.value = {
    courseId: undefined,
    courseCode: '',
    courseName: '',
    credit: 0,
    hours: 0,
    orgId: undefined,
    courseType: ''
  }
  dialogTitle.value = '新增课程'
  dialogVisible.value = true
}

// 编辑课程
const handleEdit = (row: any) => {
  form.value = {
    courseId: row.courseId,
    courseCode: row.courseCode,
    courseName: row.courseName,
    credit: row.credit,
    hours: row.hours,
    orgId: row.orgId,
    courseType: row.courseType
  }
  dialogTitle.value = '编辑课程'
  dialogVisible.value = true
}

// 删除课程
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认删除该课程？', '提示', { type: 'warning' })
    await deleteCourse(row.courseId)
    ElMessage.success('删除成功')
    fetchCourseList()
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
    if (form.value.courseId) {
      await updateCourse(form.value)
      ElMessage.success('修改成功')
    } else {
      await addCourse(form.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await nextTick()
    await fetchCourseList()
  } catch (error: any) {
    if (error !== false) {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const getTypeTag = (type: string) => {
  if (type === '专业核心课') return 'tag-danger'
  if (type === '专业选修课') return 'tag-primary'
  if (type === '公共基础课') return 'tag-success'
  if (type === '公共选修课') return 'tag-warning'
  return 'tag-warning'
}

// 根据 orgId 获取学院名称
const getOrgName = (orgId: number) => {
  if (!orgId) return '-'
  const found = orgOptions.value.find(o => o.orgId === orgId)
  return found ? found.orgName : '未知'
}

onMounted(async () => {
  // 先加载学院列表，再加载课程列表
  try {
    const res = await getOrgTree() as any
    if (res.code === 200 && res.data) {
      orgOptions.value = extractColleges(res.data)
    }
  } catch (error) {
    console.error('获取学院列表失败', error)
  }
  fetchCourseList()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>课程信息管理</h2>
      <p>维护课程基础信息</p>
    </div>

    <div class="action-bar">
      <div class="filter-group" style="flex: 1; max-width: 600px;">
        <el-input v-model="searchQuery" placeholder="搜索课程代码..." :prefix-icon="Search" clearable
          @keyup.enter="handleSearch" @clear="handleSearch" />
      </div>
      <div class="action-group">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增课程</el-button>
      </div>
    </div>

    <el-table :data="tableData" class="custom-table" style="width: 100%" v-loading="loading" :key="tableKey">
      <el-table-column type="index" label="序号" width="70" align="center" />

      <el-table-column prop="courseCode" label="课程代码" />

      <el-table-column prop="courseName" label="课程名称" />

      <el-table-column label="开课院系">

        <template #default="scope">
          {{ getOrgName(scope.row.orgId) }}
        </template>
      </el-table-column>
      <el-table-column align="center" prop="credit" label="学分" />

      <el-table-column align="center" prop="hours" label="学时" />

      <el-table-column align="center" label="课程类型">

        <template #default="scope">
          <span class="custom-tag" :class="getTypeTag(scope.row.courseType)">
            {{ scope.row.courseType }}
          </span>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="课程代码" prop="courseCode">
          <el-input v-model="form.courseCode" placeholder="如: CS101" />
        </el-form-item>
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="form.courseName" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="学分" prop="credit">
          <el-input-number v-model="form.credit" :min="0" :max="20" />
        </el-form-item>
        <el-form-item label="学时" prop="hours">
          <el-input-number v-model="form.hours" :min="0" :max="200" />
        </el-form-item>
        <el-form-item label="开课院系" prop="orgId">
          <el-select v-model="form.orgId" placeholder="请选择开课院系">
            <el-option v-for="item in orgOptions" :key="item.orgId" :label="item.orgName" :value="item.orgId" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程类型">
          <el-select v-model="form.courseType" placeholder="请选择课程类型">
            <el-option v-for="item in courseTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
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
