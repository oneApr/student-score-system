<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { Search, View } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getMyScores } from '@/api/score'

const termFilter = ref('')
const searchQuery = ref('')
const loading = ref(false)
const tableData = ref<any[]>([])
const termOptions = ref<string[]>([])

const detailVisible = ref(false)
const detailRow = ref<any>(null)

const filteredData = computed(() => {
  return tableData.value.filter(item => {
    const matchTerm = !termFilter.value || item.term === termFilter.value
    const matchSearch = !searchQuery.value ||
      item.courseName?.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      item.courseCode?.toLowerCase().includes(searchQuery.value.toLowerCase())
    return matchTerm && matchSearch
  })
})

const stats = computed(() => {
  const data = filteredData.value.length > 0 ? filteredData.value : tableData.value
  const courses = data.length
  const credits = data.reduce((acc, cur) => acc + (Number(cur.credit) || 2), 0)
  const totalScore = data.reduce((acc, cur) => acc + (cur.totalScore || 0), 0)
  const avgScore = courses > 0 ? (totalScore / courses).toFixed(1) : 0
  const totalGpa = data.reduce((acc, cur) => acc + (cur.gpa || 0), 0)
  const avgGpa = courses > 0 ? (totalGpa / courses).toFixed(2) : 0

  return [
    { label: '已修课程', value: courses, type: 'blue' },
    { label: '已获学分', value: credits, type: 'green' },
    { label: '平均分', value: avgScore, type: 'purple' },
    { label: '平均GPA', value: avgGpa, type: 'orange' }
  ]
})

onMounted(() => {
  fetchData()
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMyScores() as any
    if (res.code === 200) {
      tableData.value = res.data?.list || res.data?.records || []
      // 从成绩数据中提取不重复的学期
      const terms = [...new Set(tableData.value.map((item: any) => item.term).filter(Boolean))] as string[]
      termOptions.value = terms.sort()
    } else {
      ElMessage.error(res.message || '获取成绩失败')
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '获取成绩失败')
  } finally {
    loading.value = false
  }
}

const handleViewDetail = (row: any) => {
  detailRow.value = row
  detailVisible.value = true
}

const getLevelTag = (level: string) => {
  if (level === '优秀') return 'tag-success'
  if (level === '良好') return 'tag-primary'
  if (level === '中等') return 'tag-warning'
  if (level === '及格') return 'tag-info'
  return 'tag-danger'
}

const getScoreColor = (score: number) => {
  if (!score) return '#94a3b8'
  if (score >= 90) return '#22c55e'
  if (score >= 80) return '#3b82f6'
  if (score >= 70) return '#f59e0b'
  if (score >= 60) return '#f97316'
  return '#ef4444'
}
</script>

<template>
  <div class="page-container" style="background: transparent; box-shadow: none; padding: 0;">
    <div style="background: white; padding: 24px; border-radius: 8px; margin-bottom: 20px;">
      <div class="page-header" style="margin-bottom: 20px;">
        <h2>成绩查询</h2>
        <p>查看个人已发布成绩</p>
      </div>

      <div class="action-bar" style="margin-bottom: 24px;">
        <div class="filter-group">
          <el-select v-model="termFilter" placeholder="全部学期" style="width: 220px;" clearable>
            <el-option label="全部学期" value="" />
            <el-option v-for="term in termOptions" :key="term" :label="term" :value="term" />
          </el-select>
          <el-input v-model="searchQuery" placeholder="搜索课程名称或代码..." :prefix-icon="Search" style="width: 280px;"
            clearable />
        </div>
      </div>


      <div class="stats-row" style="display: flex; gap: 20px; margin-bottom: 24px;">
        <div v-for="stat in stats" :key="stat.label" class="stat-card" :class="stat.type"
          style="flex: 1; padding: 24px;">
          <div class="stat-label">{{ stat.label }}</div>
          <div class="stat-value">{{ stat.value }}</div>
        </div>
      </div>


      <el-table v-loading="loading" :data="filteredData" class="custom-table" style="width: 100%">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="term" label="学期" />
        <el-table-column prop="courseCode" label="课程代码" />
        <el-table-column prop="courseName" label="课程名称" />
        <el-table-column align="center" prop="credit" label="学分" />
        <el-table-column prop="teacherName" label="授课教师" />
        <el-table-column align="center" prop="totalScore" label="总评成绩">
          <template #default="scope">
            <span :style="`font-weight: bold; color: ${getScoreColor(scope.row.totalScore)};`">
              {{ scope.row.totalScore }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="等级">
          <template #default="scope">
            <span class="custom-tag" :class="getLevelTag(scope.row.gradeLevel)">
              {{ scope.row.gradeLevel }}
            </span>
          </template>
        </el-table-column>
        <el-table-column align="center" prop="gpa" label="绩点">
          <template #default="scope">
            <span style="font-weight: 500; color: #3b82f6;">{{ scope.row.gpa?.toFixed(1) }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="操作" fixed="right">
          <template #default="scope">
            <div style="display: flex; justify-content: center;">
              <el-link type="primary" :icon="View" :underline="false" @click="handleViewDetail(scope.row)">详情</el-link>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>


    <el-dialog v-model="detailVisible" title="成绩详情" width="520px" :close-on-click-modal="true">
      <template v-if="detailRow">
        <div class="detail-header">
          <div class="detail-course-name">{{ detailRow.courseName }}</div>
          <div class="detail-course-code">{{ detailRow.courseCode }} · {{ detailRow.term }}</div>
        </div>

        <div class="detail-score-main">
          <div class="detail-total-score" :style="`color: ${getScoreColor(detailRow.totalScore)};`">
            {{ detailRow.totalScore ?? '-' }}
          </div>
          <div class="detail-total-label">综合总评</div>
          <span class="custom-tag" :class="getLevelTag(detailRow.gradeLevel)">{{ detailRow.gradeLevel }}</span>
        </div>

        <el-divider />

        <div class="detail-items">
          <div class="detail-item">
            <span class="detail-item-label">授课教师</span>
            <span class="detail-item-value">{{ detailRow.teacherName ?? '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-item-label">学分</span>
            <span class="detail-item-value">{{ detailRow.credit ?? '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-item-label">平时成绩</span>
            <span class="detail-item-value">{{ detailRow.dailyScore ?? '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-item-label">作业成绩</span>
            <span class="detail-item-value">{{ detailRow.homeworkScore ?? '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-item-label">期中成绩</span>
            <span class="detail-item-value">{{ detailRow.midScore ?? '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-item-label">期末成绩</span>
            <span class="detail-item-value">{{ detailRow.finalScore ?? '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-item-label">绩点 (GPA)</span>
            <span class="detail-item-value" style="color: #3b82f6; font-weight: 600;">{{ detailRow.gpa?.toFixed(2) ??
              '-' }}</span>
          </div>
          <div class="detail-item" v-if="detailRow.isRetake">
            <span class="detail-item-label">备注</span>
            <span class="detail-item-value" style="color: #f59e0b;">重修课程</span>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.stat-card {
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  gap: 8px;

  .stat-label {
    font-size: 14px;
    color: #475569;
  }

  .stat-value {
    font-size: 32px;
    font-weight: bold;
  }

  &.blue {
    background-color: #eff6ff;

    .stat-value {
      color: #2563eb;
    }
  }

  &.green {
    background-color: #f0fdf4;

    .stat-value {
      color: #16a34a;
    }
  }

  &.purple {
    background-color: #f5f3ff;

    .stat-value {
      color: #9333ea;
    }
  }

  &.orange {
    background-color: #fff7ed;

    .stat-value {
      color: #ea580c;
    }
  }
}

.detail-header {
  text-align: center;
  margin-bottom: 20px;

  .detail-course-name {
    font-size: 20px;
    font-weight: 600;
    color: #1e293b;
    margin-bottom: 6px;
  }

  .detail-course-code {
    font-size: 13px;
    color: #94a3b8;
  }
}

.detail-score-main {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px 0;

  .detail-total-score {
    font-size: 56px;
    font-weight: 700;
    line-height: 1;
  }

  .detail-total-label {
    font-size: 13px;
    color: #94a3b8;
  }
}

.detail-items {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  padding: 0 8px;

  .detail-item {
    display: flex;
    flex-direction: column;
    gap: 4px;

    .detail-item-label {
      font-size: 12px;
      color: #94a3b8;
    }

    .detail-item-value {
      font-size: 15px;
      font-weight: 500;
      color: #1e293b;
    }
  }
}
</style>
