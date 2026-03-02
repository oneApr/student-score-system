<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { Document, Position } from '@element-plus/icons-vue'
import { getTeacherScoreList, batchInputScore, getMyPlans, updatePlanWeights } from '@/api/score'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const selectedCourse = ref<number | null>(null)
const coursePlans = ref<any[]>([])
const loading = ref(false)
const subLoading = ref(false)

const weights = ref({
  usual: 0,
  homework: 0,
  midterm: 0,
  final: 0
})

const originalWeights = ref({
  usual: 0,
  homework: 0,
  midterm: 0,
  final: 0
})

const students = ref<any[]>([])
const originalStudents = ref<any[]>([])

onMounted(async () => {
  await fetchMyPlans()
})

// 获取当前教师的课程列表，注意 getMyPlans 返回 { code, data }
const fetchMyPlans = async () => {
  try {
    const res = await getMyPlans({})
    if (res.code === 200) {
      coursePlans.value = Array.isArray(res.data) ? res.data : (res.data?.list || [])
      if (coursePlans.value.length > 0) {
        selectedCourse.value = coursePlans.value[0].planId
        updateWeights(coursePlans.value[0])
      }
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '获取课程计划失败')
  }
}

const updateWeights = (plan: any) => {
  weights.value.usual = plan.dailyWeight || 0
  weights.value.homework = plan.homeworkWeight || 0
  weights.value.midterm = plan.midWeight || 0
  weights.value.final = plan.finalWeight || 0

  originalWeights.value = { ...weights.value }
}

watch(selectedCourse, (newVal) => {
  if (newVal) {
    const plan = coursePlans.value.find(p => p.planId === newVal)
    if (plan) {
      updateWeights(plan)
      fetchStudents()
      fetchBatchStatus()
    }
  }
})

const batchStatus = ref<string>('未提交')

const fetchBatchStatus = async () => {
  if (!selectedCourse.value) return
  batchStatus.value = '获取中...'
  try {
    const res = await request({
      url: '/teacher/score/submit/list',
      method: 'get',
      params: { pageNum: 1, pageSize: 1000 }
    })
    if (res.code === 200 && res.data?.list) {
      const match = res.data.list.find((b: any) => b.planId === selectedCourse.value)
      batchStatus.value = match ? match.status : '未提交'
    } else {
      batchStatus.value = '未提交'
    }
  } catch (e) {
    batchStatus.value = '未提交'
  }
}

const isCourseLocked = computed(() => {
  return batchStatus.value === '通过' || (students.value.length > 0 && students.value[0].isLocked === 1)
})

const getStatusTag = (status: string) => {
  if (status === '通过') return 'success'
  if (status === '待审核') return 'warning'
  if (status === '驳回') return 'danger'
  return 'info'
}

const fetchStudents = async () => {
  if (!selectedCourse.value) return
  loading.value = true
  try {
    const res = await getTeacherScoreList(selectedCourse.value, { pageNum: 1, pageSize: 1000 })
    if (res.code === 200) {
      students.value = (res.data?.list || res.data?.records || []).map((r: any) => ({
        ...r,
        scoreId: r.scoreId,
        studentId: r.studentId,
        usual: r.dailyScore,
        homework: r.homeworkScore,
        midterm: r.midScore,
        final: r.finalScore
      }))
      // 深度拷贝以供重置
      originalStudents.value = JSON.parse(JSON.stringify(students.value))
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '获取学生成绩失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  // 恢复权重
  weights.value = { ...originalWeights.value }
  // 恢复学生成绩
  students.value = JSON.parse(JSON.stringify(originalStudents.value))
  ElMessage.success('已恢复原始成绩和权重数据')
}

const handleBatchSubmit = async () => {
  if (!selectedCourse.value) return
  subLoading.value = true
  try {
    // 1. 先保存权重设置
    if (totalWeight.value !== 100) {
      ElMessage.warning('权重总和必须为100%才能提交')
      subLoading.value = false
      return
    }
    await updatePlanWeights(selectedCourse.value, {
      usual: weights.value.usual,
      homework: weights.value.homework,
      midterm: weights.value.midterm,
      finalScore: weights.value.final // 传给后端叫 finalScore
    })

    // 2. 批量提交成绩
    const dtos = students.value.map(s => ({
      scoreId: s.scoreId,
      studentId: s.studentId,
      planId: selectedCourse.value,
      dailyScore: Number(s.usual) || 0,
      homeworkScore: Number(s.homework) || 0,
      midScore: Number(s.midterm) || 0,
      finalScore: Number(s.final) || 0
    }))
    const res = await batchInputScore(dtos)
    if (res.code === 200) {
      ElMessage.success('成绩与权重设置已提交审核')
      fetchStudents()
      fetchBatchStatus()
    } else {
      ElMessage.error(res.message || '成绩提交失败')
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '批量提交失败')
  } finally {
    subLoading.value = false
  }
}

const calculateTotal = (row: any) => {
  const total = (
    (Number(row.usual) || 0) * (weights.value.usual / 100) +
    (Number(row.homework) || 0) * (weights.value.homework / 100) +
    (Number(row.midterm) || 0) * (weights.value.midterm / 100) +
    (Number(row.final) || 0) * (weights.value.final / 100)
  )
  return total.toFixed(1)
}

const getTotalColor = (totalStr: string) => {
  const total = parseFloat(totalStr)
  if (total >= 90) return '#16a34a'
  if (total >= 80) return '#2563eb'
  if (total < 60) return '#dc2626'
  return '#ea580c'
}

const totalWeight = computed(() => {
  return (weights.value.usual || 0) + (weights.value.homework || 0) + (weights.value.midterm || 0) + (weights.value.final || 0)
})
</script>

<template>
  <div class="page-container" style="background: transparent; box-shadow: none; padding: 0;">

    <div style="background: white; padding: 24px; border-radius: 8px; margin-bottom: 20px;">
      <div class="page-header" style="margin-bottom: 20px;">
        <h2>成绩录入</h2>
        <p>对学生成绩进行录入</p>
      </div>
      <div>
        <div style="font-size: 13px; color: #64748b; margin-bottom: 8px;">选择课程</div>
        <div style="display: flex; align-items: center; gap: 16px;">
          <el-select v-model="selectedCourse" style="width: 420px;" placeholder="请选择课程">
            <el-option v-for="item in coursePlans" :key="item.planId"
              :label="`${item.courseCode || ''} - ${item.courseName || ''} - ${item.className || ''}`"
              :value="item.planId" />
          </el-select>
          <el-tag v-if="batchStatus" :type="getStatusTag(batchStatus)" size="large" effect="light"
            style="font-size: 14px; padding: 0 12px; height: 32px; line-height: 30px;">当前状态：{{ batchStatus }}</el-tag>
          <el-alert v-if="isCourseLocked" title="该条成绩已被锁定归档，禁止修改" type="error" :closable="false" show-icon
            style="padding: 4px 12px; border-radius: 4px; display: inline-flex; width: auto;" />
        </div>
      </div>
    </div>


    <div
      style="background: #f8fafc; border: 1px solid #e2e8f0; padding: 24px; border-radius: 8px; margin-bottom: 20px;">
      <div style="font-size: 15px; font-weight: 600; color: #1e293b; margin-bottom: 20px;">成绩权重设置</div>
      <div style="display: flex; gap: 40px; align-items: flex-end;">
        <div class="weight-item">
          <div class="label">平时成绩</div>
          <el-input-number v-model="weights.usual" :min="0" :max="100" controls-position="right" style="width: 100px;"
            :disabled="isCourseLocked" /> <span class="percent">%</span>
        </div>
        <div class="weight-item">
          <div class="label">作业成绩</div>
          <el-input-number v-model="weights.homework" :min="0" :max="100" controls-position="right"
            style="width: 100px;" :disabled="isCourseLocked" /> <span class="percent">%</span>
        </div>
        <div class="weight-item">
          <div class="label">期中成绩</div>
          <el-input-number v-model="weights.midterm" :min="0" :max="100" controls-position="right" style="width: 100px;"
            :disabled="isCourseLocked" /> <span class="percent">%</span>
        </div>
        <div class="weight-item">
          <div class="label">期末成绩</div>
          <el-input-number v-model="weights.final" :min="0" :max="100" controls-position="right" style="width: 100px;"
            :disabled="isCourseLocked" /> <span class="percent">%</span>
        </div>
        <div class="weight-total" style="margin-left: auto; text-align: right;">
          <div class="label">总计</div>
          <div style="font-size: 24px; font-weight: bold;"
            :style="{ color: totalWeight === 100 ? '#16a34a' : '#ef4444' }">
            {{ totalWeight }}%
          </div>
        </div>
      </div>
    </div>


    <div style="background: white; padding: 24px; border-radius: 8px; border: 1px solid #e2e8f0; margin-bottom: 20px;">
      <el-table v-loading="loading" :data="students" class="custom-table edit-table" style="width: 100%">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="studentNo" label="学号" />
        <el-table-column prop="studentName" label="姓名" />
        <el-table-column align="center" label="平时成绩">
          <template #header>平时成绩<br><span style="font-size:12px;color:#94a3b8;font-weight:normal;">({{ weights.usual
              }}%)</span></template>
          <template #default="scope"><el-input v-model="scope.row.usual" size="small" style="width: 80px;"
              :disabled="isCourseLocked" /></template>
        </el-table-column>
        <el-table-column align="center" label="作业成绩">
          <template #header>作业成绩<br><span style="font-size:12px;color:#94a3b8;font-weight:normal;">({{ weights.homework
              }}%)</span></template>
          <template #default="scope"><el-input v-model="scope.row.homework" size="small" style="width: 80px;"
              :disabled="isCourseLocked" /></template>
        </el-table-column>
        <el-table-column align="center" label="期中成绩">
          <template #header>期中成绩<br><span style="font-size:12px;color:#94a3b8;font-weight:normal;">({{ weights.midterm
              }}%)</span></template>
          <template #default="scope"><el-input v-model="scope.row.midterm" size="small" style="width: 80px;"
              :disabled="isCourseLocked" /></template>
        </el-table-column>
        <el-table-column align="center" label="期末成绩">
          <template #header>期末成绩<br><span style="font-size:12px;color:#94a3b8;font-weight:normal;">({{ weights.final
              }}%)</span></template>
          <template #default="scope"><el-input v-model="scope.row.final" size="small" style="width: 80px;"
              :disabled="isCourseLocked" /></template>
        </el-table-column>
        <el-table-column align="center" label="总评成绩">
          <template #default="scope">
            <span style="font-size: 15px; font-weight: bold;"
              :style="{ color: getTotalColor(calculateTotal(scope.row)) }">
              {{ calculateTotal(scope.row) }}
            </span>
          </template>
        </el-table-column>
      </el-table>
    </div>


    <div style="display: flex; justify-content: flex-end; gap: 16px;">
      <div style="margin-top: 24px; text-align: right;">
        <el-button @click="handleReset" :disabled="isCourseLocked">重置部分更改</el-button>
        <el-button type="primary" :loading="subLoading" @click="handleBatchSubmit" :disabled="isCourseLocked">
          <el-icon class="el-icon--left">
            <Position />
          </el-icon>
          提交审核
        </el-button>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.weight-item {
  display: flex;
  flex-direction: column;
  gap: 8px;

  .label {
    font-size: 13px;
    color: #475569;
  }

  .percent {
    color: #64748b;
    margin-left: 8px;
  }
}

.weight-total {
  .label {
    font-size: 13px;
    color: #475569;
    margin-bottom: 8px;
  }
}

.edit-table {
  :deep(.el-input__inner) {
    text-align: center;
  }
}
</style>
