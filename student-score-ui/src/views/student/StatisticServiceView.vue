<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { getMyScores } from '@/api/score'

const barChartRef = ref<HTMLElement | null>(null)
const lineChartRef = ref<HTMLElement | null>(null)
const pieChartRef = ref<HTMLElement | null>(null)
const loading = ref(false)
const tableData = ref<any[]>([])

let barChart: echarts.ECharts | null = null
let lineChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

// 基础统计数据（全部课程）
const stats = computed(() => {
  const courses = tableData.value.length
  const credits = tableData.value.reduce((acc, cur) => acc + (Number(cur.credit) || 0), 0)
  const totalScore = tableData.value.reduce((acc, cur) => acc + (cur.totalScore || 0), 0)
  const avgScore = courses > 0 ? (totalScore / courses).toFixed(1) : '0'
  const totalGpa = tableData.value.reduce((acc, cur) => acc + (cur.gpa || 0), 0)
  const avgGpa = courses > 0 ? (totalGpa / courses).toFixed(2) : '0'
  return { courses, credits, avgScore, avgGpa }
})

// 各学期 GPA 趋势（fold by term）
const termGpaData = computed(() => {
  const map = new Map<string, { totalGpa: number; count: number }>()
  for (const item of tableData.value) {
    if (!item.term || !item.gpa) continue
    if (!map.has(item.term)) map.set(item.term, { totalGpa: 0, count: 0 })
    const entry = map.get(item.term)!
    entry.totalGpa += item.gpa
    entry.count++
  }
  const terms = [...map.keys()].sort()
  return {
    terms,
    gpaList: terms.map(t => +(map.get(t)!.totalGpa / map.get(t)!.count).toFixed(2))
  }
})

// 各学期平均分
const termScoreData = computed(() => {
  const map = new Map<string, { total: number; count: number }>()
  for (const item of tableData.value) {
    if (!item.term || !item.totalScore) continue
    if (!map.has(item.term)) map.set(item.term, { total: 0, count: 0 })
    const entry = map.get(item.term)!
    entry.total += item.totalScore
    entry.count++
  }
  const terms = [...map.keys()].sort()
  return {
    terms,
    scoreList: terms.map(t => +(map.get(t)!.total / map.get(t)!.count).toFixed(1))
  }
})

// 成绩等级分布
const gradeDistribution = computed(() => {
  const dist: Record<string, number> = { '优秀': 0, '良好': 0, '中等': 0, '及格': 0, '不及格': 0 }
  for (const item of tableData.value) {
    const level = item.gradeLevel as string
    if (level && dist[level] !== undefined) {
      dist[level]++
    }
  }
  return Object.entries(dist).filter(([, v]) => v > 0).map(([name, value]) => ({ name, value }))
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMyScores() as any
    if (res.code === 200) {
      tableData.value = res.data?.list || res.data?.records || []
      nextTick(() => initCharts())
    } else {
      ElMessage.error(res.message || '获取成绩失败')
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '获取成绩失败')
  } finally {
    loading.value = false
  }
}

const initCharts = () => {
  if (barChartRef.value) {
    barChart = echarts.init(barChartRef.value)
    const data = termScoreData.value
    barChart.setOption({
      title: { text: '各学期平均分对比', textStyle: { fontSize: 14, fontWeight: 'normal', color: '#1e293b' } },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.terms, axisLabel: { rotate: 20, fontSize: 11 } },
      yAxis: { type: 'value', min: 0, max: 100, name: '分数' },
      series: [{
        name: '平均分',
        type: 'bar',
        data: data.scoreList,
        itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#3b82f6' }, { offset: 1, color: '#93c5fd' }]) },
        label: { show: true, position: 'top', fontSize: 11 },
        barMaxWidth: 50
      }],
      grid: { left: 40, right: 20, bottom: 50, top: 50 }
    })
    window.addEventListener('resize', () => barChart?.resize())
  }

  if (lineChartRef.value) {
    lineChart = echarts.init(lineChartRef.value)
    const data = termGpaData.value
    lineChart.setOption({
      title: { text: '历学期 GPA 变化趋势', textStyle: { fontSize: 14, fontWeight: 'normal', color: '#1e293b' } },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.terms, axisLabel: { rotate: 20, fontSize: 11 } },
      yAxis: { type: 'value', min: 0, max: 4.0, name: 'GPA' },
      series: [{
        name: 'GPA',
        type: 'line',
        data: data.gpaList,
        itemStyle: { color: '#8b5cf6' },
        lineStyle: { color: '#8b5cf6', width: 2 },
        areaStyle: { color: 'rgba(139,92,246,0.1)' },
        smooth: true,
        label: { show: true, fontSize: 11 }
      }],
      grid: { left: 40, right: 20, bottom: 50, top: 50 }
    })
    window.addEventListener('resize', () => lineChart?.resize())
  }

  if (pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value)
    const colors = { '优秀': '#22c55e', '良好': '#3b82f6', '中等': '#f59e0b', '及格': '#94a3b8', '不及格': '#ef4444' }
    pieChart.setOption({
      title: { text: '成绩等级分布', textStyle: { fontSize: 14, fontWeight: 'normal', color: '#1e293b' } },
      tooltip: { trigger: 'item', formatter: '{b}: {c}门 ({d}%)' },
      legend: { bottom: 0 },
      series: [{
        type: 'pie',
        radius: ['40%', '65%'],
        data: gradeDistribution.value.map(d => ({ ...d, itemStyle: { color: (colors as any)[d.name] } })),
        label: { show: true, formatter: '{b}\n{c}门' }
      }]
    })
    window.addEventListener('resize', () => pieChart?.resize())
  }
}

onMounted(() => {
  nextTick(() => fetchData())
})
</script>

<template>
  <div class="page-container" style="background: transparent; box-shadow: none; padding: 0;" v-loading="loading">
    <div style="background: white; padding: 24px; border-radius: 8px; margin-bottom: 20px;">
      <div class="page-header" style="margin-bottom: 20px;">
        <h2>成绩统计分析</h2>
        <p>个人成绩多维度可视化分析</p>
      </div>


      <div class="stats-row" style="display: flex; gap: 20px; margin-bottom: 4px;">
        <div class="stat-card st-blue">
          <div class="stat-label">已修课程</div>
          <div class="stat-value">{{ stats.courses }}</div>
        </div>
        <div class="stat-card st-green">
          <div class="stat-label">已获学分</div>
          <div class="stat-value">{{ stats.credits }}</div>
        </div>
        <div class="stat-card st-purple">
          <div class="stat-label">平均成绩</div>
          <div class="stat-value">{{ stats.avgScore }}</div>
        </div>
        <div class="stat-card st-orange">
          <div class="stat-label">平均 GPA</div>
          <div class="stat-value">{{ stats.avgGpa }}</div>
        </div>
      </div>
    </div>

    <div style="display: flex; gap: 20px; margin-bottom: 20px;">
      <div style="flex: 3; background: white; padding: 24px; border-radius: 8px; border: 1px solid #e2e8f0;">
        <div ref="barChartRef" style="width: 100%; height: 300px;"></div>
      </div>
      <div style="flex: 2; background: white; padding: 24px; border-radius: 8px; border: 1px solid #e2e8f0;">
        <div ref="pieChartRef" style="width: 100%; height: 300px;"></div>
      </div>
    </div>

    <div style="background: white; padding: 24px; border-radius: 8px; border: 1px solid #e2e8f0;">
      <div ref="lineChartRef" style="width: 100%; height: 280px;"></div>
    </div>


    <div v-if="!loading && tableData.length === 0"
      style="text-align: center; padding: 60px; color: #94a3b8; background: white; border-radius: 8px; margin-top: 20px;">
      暂无成绩数据，请先查询成绩
    </div>
  </div>
</template>

<style scoped lang="scss">
.stat-card {
  flex: 1;
  padding: 20px;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  gap: 6px;

  .stat-label {
    font-size: 13px;
    color: #64748b;
  }

  .stat-value {
    font-size: 28px;
    font-weight: 700;
  }

  &.st-blue {
    background: #eff6ff;

    .stat-value {
      color: #2563eb;
    }
  }

  &.st-green {
    background: #f0fdf4;

    .stat-value {
      color: #16a34a;
    }
  }

  &.st-purple {
    background: #f5f3ff;

    .stat-value {
      color: #9333ea;
    }
  }

  &.st-orange {
    background: #fff7ed;

    .stat-value {
      color: #ea580c;
    }
  }
}
</style>
