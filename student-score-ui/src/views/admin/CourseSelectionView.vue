<script setup lang="ts">
import { nextTick, ref, onMounted, reactive } from 'vue'
import { Plus, Search, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStudentCourseList, addStudentCourse, deleteStudentCourse } from '@/api/selection'
import request from '@/utils/request'

const term = ref('')
const termOptions = ref<{ label: string; value: string }[]>([])
const searchQuery = ref('')
const loading = ref(false)
const tableKey = ref(0)
const tableData = ref<any[]>([])
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const dialogVisible = ref(false)
const formRef = ref()
const form = ref({
  studentId: undefined as number | undefined,
  planId: undefined as number | undefined,
  courseType: '必修'
})
const rules = {
  studentId: [{ required: true, message: '请选择学生', trigger: 'blur' }],
  planId: [{ required: true, message: '请选择课程', trigger: 'blur' }]
}

// 获取学期列表
const fetchTerms = async () => {
  try {
    const res: any = await request({
      url: '/admin/plan/terms',
      method: 'get'
    })
    if (res.code === 200 && res.data) {
      termOptions.value = res.data.map((t: string) => ({
        label: formatTerm(t),
        value: t
      }))
    }
  } catch (error) {
    console.error('获取学期列表失败', error)
  }
}

const studentsOptions = ref<any[]>([])
const plansOptions = ref<any[]>([])

const fetchDictionaries = async () => {
  try {
    const [stuRes, planRes]: [any, any] = await Promise.all([
      request({ url: '/admin/user/page?pageNum=1&pageSize=9999', method: 'get' }),
      request({ url: '/admin/plan/page?pageNum=1&pageSize=9999', method: 'get' })
    ])
    if (stuRes.code === 200) {
      studentsOptions.value = stuRes.data?.list || []
    }
    if (planRes.code === 200) {
      plansOptions.value = planRes.data?.list || []
    }
  } catch (error) {
    console.error('获取字典失败', error)
  }
}

// 格式化学期显示
const formatTerm = (termStr: string) => {
  if (!termStr) return ''
  const match = termStr.match(/^(\d{4})-(\d{4})-([12])$/)
  if (match) {
    const year1 = match[1]
    const year2 = match[2]
    const sem = match[3] === '1' ? '第一学期' : '第二学期'
    return `${year1}-${year2}学年${sem}`
  }
  return termStr
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getStudentCourseList({
      term: term.value,
      keyword: searchQuery.value,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    })
    if (res.code === 200) {
      tableData.value = res.data?.list || []
      pagination.total = res.data?.total || 0
      tableKey.value++
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '获取选课列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  fetchData()
}

const handleAdd = () => {
  form.value = { studentId: undefined, planId: undefined, courseType: '必修' }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    await addStudentCourse(form.value)
    ElMessage.success('添加成功')
    dialogVisible.value = false
    await nextTick()
    await fetchData()
  } catch (error: any) {
    if (error !== false) {
      ElMessage.error(error.message || '添加失败')
    }
  }
}

const handleWithdraw = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认删除该生选课？', '提示', { type: 'warning' })
    await deleteStudentCourse(row.scoreId)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error.message || '删除失败')
  }
}

const handlePageChange = (page: number) => {
  pagination.pageNum = page
  fetchData()
}

const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  fetchData()
}

onMounted(async () => {
  await fetchTerms()
  fetchDictionaries()
  fetchData()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>学生选课管理</h2>
      <p>处理学生退补选与选课查询</p>
    </div>

    <div class="action-bar">
      <div class="filter-group">
        <el-select v-model="term" placeholder="选择学期" style="width: 200px;" @change="handleSearch">
          <el-option label="全部学期" value="" />
          <el-option v-for="item in termOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-input v-model="searchQuery" placeholder="搜索课程" :prefix-icon="Search" style="width: 280px;" clearable
          @keyup.enter="handleSearch" />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>
      <div class="action-group">
        <el-button type="primary" :icon="Plus" @click="handleAdd">手工调课</el-button>
      </div>
    </div>

    <el-table :data="tableData" v-loading="loading" class="custom-table" style="width: 100%" :key="tableKey"
      row-key="scoreId">
      <el-table-column type="index" label="序号" width="70" align="center" />

      <el-table-column prop="term" label="学期" />

      <el-table-column prop="studentNo" label="学号" />

      <el-table-column prop="studentName" label="姓名" />

      <el-table-column prop="courseCode" label="课程代码" />

      <el-table-column prop="courseName" label="课程名称" />

      <el-table-column align="center" label="状态">

        <template #default="scope">
          <el-tag :type="scope.row.status === '在修' ? 'success' : 'info'">
            {{ scope.row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="center" fixed="right">
        <template #default="scope">
          <el-link type="danger" :icon="Delete" :underline="false" @click="handleWithdraw(scope.row)">删除</el-link>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="pagination.pageNum" v-model:page-size="pagination.pageSize"
      :total="pagination.total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
      @current-change="handlePageChange" @size-change="handleSizeChange"
      style="margin-top: 16px; justify-content: flex-end;" />

    <el-dialog v-model="dialogVisible" title="手工调课" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="选择学生" prop="studentId">
          <el-select v-model="form.studentId" placeholder="请选择或搜索学生" filterable style="width: 100%;">
            <el-option v-for="stu in studentsOptions" :key="stu.studentId"
              :label="stu.studentNo + ' - ' + stu.studentName" :value="stu.studentId" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择课程" prop="planId">
          <el-select v-model="form.planId" placeholder="请选择或搜索排课计划" filterable style="width: 100%;">
            <el-option v-for="plan in plansOptions" :key="plan.planId"
              :label="(plan.term || '') + ' 学期 / ' + (plan.courseName || plan.courseCode) + ' (' + plan.teacherName + ')'"
              :value="plan.planId" />
          </el-select>
        </el-form-item>
        <el-form-item label="选课属性">
          <el-radio-group v-model="form.courseType">
            <el-radio value="必修">必修</el-radio>
            <el-radio value="选修">选修</el-radio>
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
