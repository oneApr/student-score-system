<script setup lang="ts">
import { ref, computed, onMounted, watch, nextTick, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyPlans, getTeacherScoreList } from '@/api/score'
import * as echarts from 'echarts'

const selectedPlanId = ref<number | null>(null)
const coursePlans = ref<any[]>([])
const loading = ref(false)
const scores = ref<any[]>([])
const chartRef = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null

onMounted(async () => {
    await fetchPlans()
    window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
    window.removeEventListener('resize', handleResize)
    chartInstance?.dispose()
})

const handleResize = () => {
    chartInstance?.resize()
}

const fetchPlans = async () => {
    try {
        const res = await getMyPlans({})
        if (res.code === 200) {
            coursePlans.value = Array.isArray(res.data) ? res.data : (res.data?.list || [])
            if (coursePlans.value.length > 0) {
                selectedPlanId.value = coursePlans.value[0].planId
            }
        }
    } catch (error: any) {
        ElMessage.error(error?.message || '获取课程列表失败')
    }
}

watch(selectedPlanId, (val) => {
    if (val) fetchScores()
})

const fetchScores = async () => {
    if (!selectedPlanId.value) return
    loading.value = true
    try {
        const res = await getTeacherScoreList(selectedPlanId.value, { pageNum: 1, pageSize: 2000 })
        if (res.code === 200) {
            scores.value = (res.data?.list || res.data?.records || [])
        }
    } catch (error: any) {
        ElMessage.error(error?.message || '获取成绩数据失败')
    } finally {
        loading.value = false
        nextTick(() => renderChart())
    }
}

// 统计计算
const totalCount = computed(() => scores.value.length)

const validScores = computed(() => scores.value.filter(s => s.totalScore != null).map(s => Number(s.totalScore)))

const avgScore = computed(() => {
    if (validScores.value.length === 0) return 0
    const sum = validScores.value.reduce((a, b) => a + b, 0)
    return (sum / validScores.value.length).toFixed(1)
})

const maxScore = computed(() => {
    if (validScores.value.length === 0) return 0
    return Math.max(...validScores.value)
})

const minScore = computed(() => {
    if (validScores.value.length === 0) return 0
    return Math.min(...validScores.value)
})

const passRate = computed(() => {
    if (validScores.value.length === 0) return '0'
    const passed = validScores.value.filter(s => s >= 60).length
    return ((passed / validScores.value.length) * 100).toFixed(0)
})

const excellentRate = computed(() => {
    if (validScores.value.length === 0) return '0'
    const excellent = validScores.value.filter(s => s >= 90).length
    return ((excellent / validScores.value.length) * 100).toFixed(0)
})

// 分布区间
const distribution = computed(() => {
    const ranges = [
        { label: '90-100', min: 90, max: 100, count: 0 },
        { label: '80-89', min: 80, max: 89, count: 0 },
        { label: '70-79', min: 70, max: 79, count: 0 },
        { label: '60-69', min: 60, max: 69, count: 0 },
        { label: '<60', min: 0, max: 59, count: 0 }
    ]
    validScores.value.forEach(s => {
        for (const r of ranges) {
            if (s >= r.min && s <= r.max) { r.count++; break }
        }
    })
    return ranges
})

const renderChart = () => {
    if (!chartRef.value) return
    if (!chartInstance) {
        chartInstance = echarts.init(chartRef.value)
    }
    const dist = distribution.value
    chartInstance.setOption({
        title: { text: '成绩分布统计', left: 'center', textStyle: { fontSize: 16, color: '#1e293b' } },
        tooltip: {
            trigger: 'axis', axisPointer: { type: 'shadow' }, formatter: (params: any) => {
                const p = params[0]
                return `${p.name}<br/>人数 ${p.value}`
            }
        },
        grid: { left: 60, right: 40, top: 60, bottom: 40 },
        xAxis: { type: 'category', data: dist.map(d => d.label), axisLabel: { color: '#64748b' } },
        yAxis: { type: 'value', name: '人数', minInterval: 1, axisLabel: { color: '#64748b' } },
        series: [{
            name: '人数',
            type: 'bar',
            data: dist.map(d => d.count),
            barWidth: '50%',
            itemStyle: {
                color: '#3b82f6',
                borderRadius: [4, 4, 0, 0]
            }
        }]
    })
}

const statCards = computed(() => [
    { label: '总人数', value: totalCount.value, color: '#2563eb', bg: '#eff6ff' },
    { label: '平均分', value: avgScore.value, color: '#16a34a', bg: '#f0fdf4' },
    { label: '最高分', value: maxScore.value, color: '#7c3aed', bg: '#f5f3ff' },
    { label: '最低分', value: minScore.value, color: '#ea580c', bg: '#fff7ed' },
    { label: '及格率', value: passRate.value + '%', color: '#0891b2', bg: '#ecfeff' },
    { label: '优秀率', value: excellentRate.value + '%', color: '#be185d', bg: '#fdf2f8' }
])
</script>

<template>
    <div class="page-container" style="background: transparent; box-shadow: none; padding: 0;">
        <div style="background: white; padding: 24px; border-radius: 8px; margin-bottom: 20px;">
            <div class="page-header" style="margin-bottom: 20px;">
                <h2>教师成绩查询统计</h2>
                <p>查看本人课程成绩统计</p>
            </div>
            <el-select v-model="selectedPlanId" placeholder="选择课程" style="width: 360px;">
                <el-option v-for="item in coursePlans" :key="item.planId"
                    :label="`${item.courseCode || ''} - ${item.courseName || ''}`" :value="item.planId" />
            </el-select>
        </div>

        <div v-loading="loading">

            <div style="display: flex; gap: 16px; margin-bottom: 20px;">
                <div v-for="(card, idx) in statCards" :key="idx"
                    style="flex: 1; padding: 20px; border-radius: 8px; border: 1px solid #e2e8f0;"
                    :style="{ backgroundColor: card.bg }">
                    <div style="font-size: 13px; margin-bottom: 8px;" :style="{ color: card.color }">{{ card.label }}
                    </div>
                    <div style="font-size: 28px; font-weight: 700; line-height: 1;" :style="{ color: card.color }">
                        {{ card.value }}
                    </div>
                </div>
            </div>


            <div style="background: white; padding: 24px; border-radius: 8px; border: 1px solid #e2e8f0;">
                <div ref="chartRef" style="width: 100%; height: 360px;"></div>
            </div>
        </div>
    </div>
</template>

<style scoped lang="scss"></style>
