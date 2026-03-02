<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Reading, User, Calendar, View } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getMyPlans, getTeacherScoreList } from '@/api/score'

const loading = ref(false)
const statList = ref([
  { id: 1, label: '本学期课程', value: 0, icon: Reading, color: 'blue' },
  { id: 2, label: '授课学生', value: 0, icon: User, color: 'green' },
  { id: 3, label: '教学班级', value: 0, icon: Calendar, color: 'purple' }
])
const courseList = ref<any[]>([])

// 学生名单弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogLoading = ref(false)
const studentList = ref<any[]>([])

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await getMyPlans({})
    if (res.code === 200) {
      courseList.value = Array.isArray(res.data) ? res.data : (res.data?.list || [])
      if (statList.value[0]) statList.value[0].value = courseList.value.length
      if (statList.value[1]) statList.value[1].value = courseList.value.reduce((sum: number, c: any) => sum + (c.enrolled || 0), 0)
      if (statList.value[2]) statList.value[2].value = courseList.value.length
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '获取课程列表失败')
  } finally {
    loading.value = false
  }
}

const handleViewStudents = async (course: any) => {
  dialogTitle.value = `${course.courseName} - ${course.className} 学生名单`
  dialogVisible.value = true
  dialogLoading.value = true
  try {
    const res = await getTeacherScoreList(course.planId, { pageNum: 1, pageSize: 1000 })
    if (res.code === 200) {
      studentList.value = res.data?.list || res.data?.records || []
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '获取学生名单失败')
  } finally {
    dialogLoading.value = false
  }
}

const getScoreColor = (score: number | null) => {
  if (score == null) return '#94a3b8'
  if (score >= 90) return '#16a34a'
  if (score >= 80) return '#2563eb'
  if (score >= 60) return '#ea580c'
  return '#dc2626'
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="page-container" style="background: transparent; box-shadow: none; padding: 0;">
    <div class="page-header" style="background: white; padding: 24px; border-radius: 8px; margin-bottom: 20px;">
      <h2>教师课程管理</h2>
      <p>查看并管理本人授课课程</p>
    </div>

    <div class="stats-row" style="display: flex; gap: 20px; margin-bottom: 24px;">
      <div v-for="stat in statList" :key="stat.id" class="stat-card" :class="stat.color"
        style="flex: 1; display: flex; flex-direction: row; align-items: center; justify-content: flex-start; gap: 16px; padding: 24px; background: white;">
        <el-icon :size="32" :class="['custom-icon-' + stat.color]">
          <component :is="stat.icon" />
        </el-icon>
        <div class="flex-column" style="gap: 4px;">
          <div style="font-size: 13px; color: #64748b;">{{ stat.label }}</div>
          <div style="font-size: 28px; font-weight: 700; line-height: 1; color: #1e293b;"
            :class="['text-' + stat.color]">{{ stat.value }}</div>
        </div>
      </div>
    </div>

    <div class="course-list flex-column" style="gap: 16px;" v-loading="loading">
      <div v-for="course in courseList" :key="course.planId" class="course-card"
        style="background: white; border-radius: 8px; padding: 24px; border: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: flex-start;">
        <div class="course-info flex-column" style="flex: 1; gap: 16px;">
          <div class="flex-row" style="align-items: center; gap: 12px;">
            <span style="font-size: 18px; font-weight: 600; color: #1e293b;">{{ course.courseName }}</span>
            <span class="custom-tag tag-primary">{{ course.courseCode }}</span>
            <span class="custom-tag tag-info">{{ course.term }}</span>
          </div>
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
            <div class="detail-item">
              <span class="detail-label">授课班级:</span>
              <span class="detail-value">{{ course.className }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">学生人数:</span>
              <span class="detail-value">{{ course.enrolled || 0 }} 人</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">上课时间:</span>
              <span class="detail-value">{{ course.scheduleTime }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">教室:</span>
              <span class="detail-value">{{ course.location }}</span>
            </div>
          </div>
        </div>
        <div class="course-actions flex-column" style="gap: 12px; min-width: 140px;">
          <el-button type="primary" :icon="View" style="width: 100%;" @click="handleViewStudents(course)">查看学生名单
          </el-button>
        </div>
      </div>
    </div>


    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" destroy-on-close>
      <el-table :data="studentList" v-loading="dialogLoading" class="custom-table" style="width: 100%;">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="studentNo" label="学号" align="center" />
        <el-table-column prop="studentName" label="姓名" align="center" />
        <el-table-column label="总评成绩" align="center">
          <template #default="{ row }">
            <span :style="{ color: getScoreColor(row.totalScore), fontWeight: 'bold' }">
              {{ row.totalScore != null ? row.totalScore : '-' }}
            </span>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <span style="font-size: 13px; color: #64748b;">共 {{ studentList.length }} 名学生</span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.stat-card {
  border-radius: 8px;
  border: 1px solid transparent;

  &.blue {
    background-color: #eff6ff !important;
    border-color: #bfdbfe !important;
  }

  &.green {
    background-color: #f0fdf4 !important;
    border-color: #bbf7d0 !important;
  }

  &.purple {
    background-color: #f5f3ff !important;
    border-color: #ddd6fe !important;
  }
}

.custom-icon-blue {
  color: #2563eb;
}

.custom-icon-green {
  color: #16a34a;
}

.custom-icon-purple {
  color: #7c3aed;
}

.text-blue {
  color: #2563eb !important;
}

.text-green {
  color: #16a34a !important;
}

.text-purple {
  color: #7c3aed !important;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;

  .detail-label {
    color: #64748b;
  }

  .detail-value {
    color: #334155;
  }
}

.course-card:hover {
  border-color: #cbd5e1;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
  transition: all 0.2s;
}
</style>
