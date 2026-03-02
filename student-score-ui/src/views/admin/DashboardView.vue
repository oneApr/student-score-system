<script setup lang="ts">
import { ref, onMounted, nextTick, watch, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)

// 筛选条件
const termList = ref<string[]>([])
const courseList = ref<any[]>([])
const selectedTerm = ref('')
const selectedCourseId = ref<number | string>('')

// 统计数据
const statData = ref<any>({})

// 成绩明细
const scoreDetails = ref<any[]>([])

// 图表
const barChartRef = ref<HTMLElement>()
const pieChartRef = ref<HTMLElement>()
let barChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

onMounted(async () => {
  await fetchFilters()
  await fetchStats()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  barChart?.dispose()
  pieChart?.dispose()
})

const handleResize = () => {
  barChart?.resize()
  pieChart?.resize()
}

// 加载筛选器的学期和课程数据
const fetchFilters = async () => {
  try {
    // 获取所有学期
    const termRes = await request({ url: '/admin/plan/terms', method: 'get' }) as any
    if (termRes.code === 200) {
      termList.value = termRes.data || []
    }
    // 获取所有课程
    const courseRes = await request({ url: '/admin/course/list', method: 'get' }) as any
    if (courseRes.code === 200) {
      courseList.value = Array.isArray(courseRes.data) ? courseRes.data : (courseRes.data?.list || [])
    }
  } catch (e: any) {
    // 静默失败，筛选器保持空
  }
}

// 获取统计数据（含成绩明细）
const fetchStats = async () => {
  loading.value = true
  try {
    const params: any = {}
    if (selectedTerm.value) params.term = selectedTerm.value
    if (selectedCourseId.value) params.courseId = selectedCourseId.value
    const res = await request({ url: '/stat/admin', method: 'get', params }) as any
    if (res.code === 200) {
      statData.value = res.data || {}
      // 成绩明细直接取自后端 latestScores
      scoreDetails.value = res.data?.latestScores || []
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '获取统计数据失败')
  } finally {
    loading.value = false
    nextTick(() => renderCharts())
  }
}

// 筛选变化
watch([selectedTerm, selectedCourseId], () => {
  fetchStats()
})

// ECharts 渲染
const renderCharts = () => {
  const dist = statData.value.distribution || []

  if (barChartRef.value) {
    if (!barChart) barChart = echarts.init(barChartRef.value)
    barChart.setOption({
      title: { text: '成绩分布 - 柱状图', left: 'center', textStyle: { fontSize: 15, color: '#1e293b' } },
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: 60, right: 30, top: 50, bottom: 30 },
      xAxis: { type: 'category', data: dist.map((d: any) => d.name), axisLabel: { color: '#64748b' } },
      yAxis: { type: 'value', name: '人数', minInterval: 1, axisLabel: { color: '#64748b' } },
      series: [{
        name: '人数', type: 'bar', data: dist.map((d: any) => d.value),
        barWidth: '50%',
        itemStyle: { color: '#3b82f6', borderRadius: [4, 4, 0, 0] }
      }]
    })
  }

  if (pieChartRef.value) {
    if (!pieChart) pieChart = echarts.init(pieChartRef.value)
    pieChart.setOption({
      title: { text: '成绩分布 - 饼图', left: 'center', textStyle: { fontSize: 15, color: '#1e293b' } },
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      color: ['#16a34a', '#3b82f6', '#f59e0b', '#ea580c', '#dc2626'],
      series: [{
        name: '成绩分布', type: 'pie', radius: ['35%', '60%'],
        data: dist.map((d: any) => ({ name: d.name, value: d.value })),
        label: { fontSize: 12 },
        emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.2)' } }
      }]
    })
  }
}

const getScoreColor = (score: number | null) => {
  if (score == null) return '#94a3b8'
  if (score >= 90) return '#16a34a'
  if (score >= 80) return '#2563eb'
  if (score >= 60) return '#ea580c'
  return '#dc2626'
}

const getGradeTag = (level: string) => {
  if (level === '优秀') return 'tag-success'
  if (level === '良好') return 'tag-primary'
  if (level === '中等') return 'tag-warning'
  if (level === '及格') return 'tag-info'
  return 'tag-danger'
}
</script>

<template>
  <div class="dashboard-container flex-column">
    <div class="page-header" style="margin-bottom: 24px;">
      <h2 style="font-size: 24px; color: #1e293b; margin: 0 0 8px;">成绩查询与统计</h2>
      <p style="color: #64748b; font-size: 14px; margin: 0;">全局成绩数据统计分析</p>
    </div>

    <div class="card-panel" style="margin-bottom: 24px;">
      <div class="flex-row" style="align-items: center; gap: 16px; flex-wrap: wrap;">
        <div style="display: flex; align-items: center; gap: 6px; color: #64748b; font-size: 14px;">
          <span>筛选条件</span>
        </div>
        <el-select v-model="selectedTerm" placeholder="全部学期" clearable style="width: 220px;">
          <el-option label="全部学期" value="" />
          <el-option v-for="t in termList" :key="t" :label="t" :value="t" />
        </el-select>
        <el-select v-model="selectedCourseId" placeholder="全部课程" clearable style="width: 260px;">
          <el-option label="全部课程" value="" />
          <el-option v-for="c in courseList" :key="c.courseId" :label="`${c.courseCode} - ${c.courseName}`"
            :value="c.courseId" />
        </el-select>
      </div>
    </div>

    <div v-loading="loading" style="display: flex; gap: 20px; margin-bottom: 24px;">
      <div class="stat-card blue" style="flex: 1;">
        <div class="stat-label">总人数</div>
        <div class="stat-value" style="color: #2563eb;">{{ statData.totalStudents || 0 }}</div>
      </div>
      <div class="stat-card green" style="flex: 1;">
        <div class="stat-label">平均分</div>
        <div class="stat-value" style="color: #16a34a;">{{ statData.avgScore || 0 }}</div>
      </div>
      <div class="stat-card purple" style="flex: 1;">
        <div class="stat-label">最高分</div>
        <div class="stat-value" style="color: #9333ea;">{{ statData.maxScore || 0 }}</div>
      </div>
      <div class="stat-card orange" style="flex: 1;">
        <div class="stat-label">及格率</div>
        <div class="stat-value" style="color: #ea580c;">{{ statData.passRate || '0%' }}</div>
      </div>
      <div class="stat-card red" style="flex: 1;">
        <div class="stat-label">不及格</div>
        <div class="stat-value" style="color: #dc2626;">{{ statData.failCount || 0 }}</div>
      </div>
    </div>

    <el-row :gutter="20" style="margin-bottom: 24px;">
      <el-col :span="12">
        <div class="card-panel">
          <div ref="barChartRef" style="width: 100%; height: 320px;"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="card-panel">
          <div ref="pieChartRef" style="width: 100%; height: 320px;"></div>
        </div>
      </el-col>
    </el-row>

    <div class="card-panel">
      <div style="font-size: 15px; font-weight: 600; color: #1e293b; margin-bottom: 16px;">成绩明细</div>
      <el-table :data="scoreDetails" v-loading="loading" class="custom-table" style="width: 100%">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="rank" label="排名" align="center" width="80" />
        <el-table-column prop="studentNo" label="学号" align="center" />
        <el-table-column prop="studentName" label="姓名" align="center" />
        <el-table-column prop="courseName" label="课程" />
        <el-table-column label="成绩" align="center">
          <template #default="{ row }">
            <span style="font-weight: bold;" :style="{ color: getScoreColor(row.totalScore) }">
              {{ row.totalScore != null ? row.totalScore : '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="等级" align="center">
          <template #default="{ row }">
            <span v-if="row.gradeLevel" class="custom-tag" :class="getGradeTag(row.gradeLevel)">
              {{ row.gradeLevel }}
            </span>
            <span v-else style="color:#94a3b8;">-</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<style scoped lang="scss">
.dashboard-container {
  gap: 0;
}

.card-panel {
  background-color: #fff;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  padding: 20px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.01);
}

.stat-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: center;

  .stat-label {
    font-size: 13px;
    color: #64748b;
    margin-bottom: 8px;
  }

  .stat-value {
    font-size: 28px;
    font-weight: 700;
  }

  &.blue {
    background-color: #eff6ff;
    border: 1px solid #bfdbfe;
  }

  &.green {
    background-color: #f0fdf4;
    border: 1px solid #bbf7d0;
  }

  &.purple {
    background-color: #f5f3ff;
    border: 1px solid #ddd6fe;
  }

  &.orange {
    background-color: #fff7ed;
    border: 1px solid #fed7aa;
  }

  &.red {
    background-color: #fef2f2;
    border: 1px solid #fecaca;
  }
}

.flex-column {
  display: flex;
  flex-direction: column;
}

.flex-row {
  display: flex;
  flex-direction: row;
}
</style>
