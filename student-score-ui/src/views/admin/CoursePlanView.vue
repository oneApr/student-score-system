<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from 'vue'
import { Plus, Search, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCoursePlanPage, addCoursePlan, updateCoursePlan, deleteCoursePlan } from '@/api/plan'
import { getCoursePage } from '@/api/course'
import request from '@/utils/request'

const searchQuery = ref('')
const loading = ref(false)
const tableKey = ref(0)

const tableData = ref<any[]>([])
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

// 课程选项
const courseOptions = ref<{ courseId: number; courseName: string; courseCode: string }[]>([])
// 教师选项
const teacherOptions = ref<{ teacherId: number; teacherName: string }[]>([])

const form = ref({
  planId: undefined as number | undefined,
  courseId: undefined as number | undefined,
  teacherId: undefined as number | undefined,
  term: '',
  className: '',
  scheduleTime: '',
  location: '',
  dailyWeight: 10,
  homeworkWeight: 20,
  midWeight: 20,
  finalWeight: 50,
  capacity: 50,
  status: '进行中'
})

const rules = {
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  teacherId: [{ required: true, message: '请选择教师', trigger: 'change' }],
  term: [{ required: true, message: '请输入学期', trigger: 'blur' }],
  className: [{ required: true, message: '请输入班级', trigger: 'blur' }],
  capacity: [{ required: true, message: '请输入容量', trigger: 'blur' }]
}

// 获取课程列表（下拉框用）
const fetchCourseOptions = async () => {
  try {
    const res = await getCoursePage({ pageNum: 1, pageSize: 1000 }) as any
    if (res.code === 200 && res.data?.list) {
      courseOptions.value = res.data.list.map((c: any) => ({
        courseId: c.courseId,
        courseName: c.courseName,
        courseCode: c.courseCode
      }))
    }
  } catch (error) {
    console.error('获取课程列表失败', error)
  }
}

// 获取教师列表（下拉框用）
const fetchTeacherOptions = async () => {
  try {
    const res = await request({ url: '/admin/teacher/list', method: 'get' }) as any
    if (res.code === 200 && res.data) {
      teacherOptions.value = res.data.map((t: any) => ({
        teacherId: t.teacherId,
        teacherName: t.teacherName
      }))
    }
  } catch (error) {
    console.error('获取教师列表失败', error)
  }
}

// 获取开课计划列表
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getCoursePlanPage({
      keyword: searchQuery.value,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }) as any
    if (res.code === 200) {
      tableData.value = res.data?.list || []
      pagination.total = res.data?.total || 0
      tableKey.value++
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '获取开课计划失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  fetchData()
}

const handlePageChange = (page: number) => {
  pagination.pageNum = page
  fetchData()
}

const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  fetchData()
}

// 新增
const handleAdd = () => {
  form.value = {
    planId: undefined,
    courseId: undefined,
    teacherId: undefined,
    term: '',
    className: '',
    scheduleTime: '',
    location: '',
    dailyWeight: 10,
    homeworkWeight: 20,
    midWeight: 20,
    finalWeight: 50,
    capacity: 50,
    status: '进行中'
  }
  dialogTitle.value = '新增开课计划'
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: any) => {
  form.value = {
    planId: row.planId,
    courseId: row.courseId,
    teacherId: row.teacherId,
    term: row.term,
    className: row.className,
    scheduleTime: row.scheduleTime,
    location: row.location,
    dailyWeight: row.dailyWeight,
    homeworkWeight: row.homeworkWeight,
    midWeight: row.midWeight,
    finalWeight: row.finalWeight,
    capacity: row.capacity,
    status: row.status || '进行中'
  }
  dialogTitle.value = '编辑开课计划'
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认删除该开课计划？', '提示', { type: 'warning' })
    await deleteCoursePlan(row.planId)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    // 检查权重总和
    const totalWeight = form.value.dailyWeight + form.value.homeworkWeight +
      form.value.midWeight + form.value.finalWeight
    if (totalWeight !== 100) {
      ElMessage.error('四项成绩权重总和必须等于100！')
      return
    }
    if (form.value.planId) {
      await updateCoursePlan(form.value)
      ElMessage.success('修改成功')
    } else {
      await addCoursePlan(form.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await nextTick()
    await fetchData()
  } catch (error: any) {
    if (error !== false) { // validate 失败时不显示错误
      ElMessage.error(error.message || '操作失败')
    }
  }
}

onMounted(() => {
  fetchData()
  fetchCourseOptions()
  fetchTeacherOptions()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>开课计划管理</h2>
      <p>管理各学期开课安排</p>
    </div>

    <div class="action-bar">
      <div class="filter-group">
        <el-input v-model="searchQuery" placeholder="搜索课程名称..." :prefix-icon="Search" style="width: 280px;" clearable
          @keyup.enter="handleSearch" @clear="handleSearch" />
      </div>
      <div class="action-group">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增开课计划</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="tableData" class="custom-table" style="width: 100%" :key="tableKey">
      <el-table-column type="index" label="序号" width="70" align="center" />


      <el-table-column prop="courseCode" label="课程代码" />

      <el-table-column prop="courseName" label="课程名称" />

      <el-table-column prop="teacherName" label="授课教师" />

      <el-table-column prop="term" label="学期" />

      <el-table-column prop="className" label="授课班级" />

      <el-table-column prop="location" label="教室" />

      <el-table-column label="容量">

        <template #default="{ row }">
          {{ row.enrolled || 0 }}/{{ row.capacity }}
        </template>
      </el-table-column>
      <el-table-column align="center" prop="status" label="状态">

        <template #default="{ row }">
          <el-tag :type="row.status === '进行中' ? 'success' : 'info'">
            {{ row.status }}
          </el-tag>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="课程" prop="courseId">
          <el-select v-model="form.courseId" placeholder="请选择课程" style="width: 100%;">
            <el-option v-for="item in courseOptions" :key="item.courseId"
              :label="`${item.courseName} (${item.courseCode})`" :value="item.courseId" />
          </el-select>
        </el-form-item>
        <el-form-item label="教师" prop="teacherId">
          <el-select v-model="form.teacherId" placeholder="请选择教师" style="width: 100%;">
            <el-option v-for="item in teacherOptions" :key="item.teacherId" :label="item.teacherName"
              :value="item.teacherId" />
          </el-select>
        </el-form-item>
        <el-form-item label="学期" prop="term">
          <el-input v-model="form.term" placeholder="如: 2024-2025第一学期" />
        </el-form-item>
        <el-form-item label="班级" prop="className">
          <el-input v-model="form.className" placeholder="如: 软件工程21-1班" />
        </el-form-item>
        <el-form-item label="上课时间">
          <el-input v-model="form.scheduleTime" placeholder="如: 周一 3-4节" />
        </el-form-item>
        <el-form-item label="教室">
          <el-input v-model="form.location" placeholder="如: A301" />
        </el-form-item>
        <el-form-item label="容量" prop="capacity">
          <el-input-number v-model="form.capacity" :min="1" :max="500" />
        </el-form-item>
        <el-divider>成绩权重（必须等于100）</el-divider>
        <el-form-item label="平时成绩权重">
          <el-input-number v-model="form.dailyWeight" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="作业成绩权重">
          <el-input-number v-model="form.homeworkWeight" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="期中成绩权重">
          <el-input-number v-model="form.midWeight" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="期末成绩权重">
          <el-input-number v-model="form.finalWeight" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="进行中">进行中</el-radio>
            <el-radio value="结课">结课</el-radio>
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
