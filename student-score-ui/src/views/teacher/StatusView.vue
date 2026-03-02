<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { Search, View, RefreshLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const term = ref('')
const formatTime = (val: string) => val ? val.replace('T', ' ').substring(0, 19) : '-'
const searchQuery = ref('')
const statusFilter = ref('')
const loading = ref(false)
const tableData = ref<any[]>([])
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const getStatusTag = (status: string) => {
  if (status === '通过') return 'tag-success'
  if (status === '待审核') return 'tag-warning'
  if (status === '驳回') return 'tag-danger'
  return 'tag-info'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await request({
      url: '/teacher/score/submit/list',
      method: 'get',
      params: {
        term: term.value,
        keyword: searchQuery.value,
        status: statusFilter.value || undefined,
        pageNum: pagination.pageNum,
        pageSize: pagination.pageSize
      }
    })
    if (res.code === 200) {
      tableData.value = res.data?.list || []
      pagination.total = res.data?.total || 0
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '获取提交列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  fetchData()
}

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<any[]>([])
const detailPagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const currentPlanId = ref<number | null>(null)

const fetchDetailData = async () => {
  if (!currentPlanId.value) return
  detailLoading.value = true
  try {
    const res: any = await request({
      url: `/teacher/score/plan/${currentPlanId.value}`,
      method: 'get',
      params: {
        pageNum: detailPagination.pageNum,
        pageSize: detailPagination.pageSize
      }
    })
    if (res.code === 200) {
      detailData.value = res.data?.list || []
      detailPagination.total = res.data?.total || 0
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '获取详情失败')
  } finally {
    detailLoading.value = false
  }
}

const handleViewDetail = (row: any) => {
  currentPlanId.value = row.planId
  detailPagination.pageNum = 1
  detailVisible.value = true
  fetchDetailData()
}

const handleWithdraw = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认撤回该提交？', '提示', { type: 'warning' })
    await request({
      url: `/teacher/score/submit/${row.submitId}/withdraw`,
      method: 'put'
    })
    ElMessage.success('撤回成功')
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error.message || '撤回失败')
  }
}


onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>成绩验证与提交</h2>
      <p>查看本人成绩录入单审核进度</p>
    </div>

    <div class="action-bar">
      <div class="filter-group">
        <el-select v-model="term" placeholder="选择学期" style="width: 200px;" @change="handleSearch">
          <el-option label="全部学期" value="" />
          <el-option label="2025-2026第二学期" value="2025-2026第二学期" />
          <el-option label="202599" value="202599" />
        </el-select>
        <el-input v-model="searchQuery" placeholder="搜索提交单号或课程..." :prefix-icon="Search" style="width: 280px;" clearable
          @keyup.enter="handleSearch" />
        <el-select v-model="statusFilter" placeholder="全部状态" style="width: 150px;" @change="handleSearch">
          <el-option label="全部状态" value="" />
          <el-option label="待审核" value="待审核" />
          <el-option label="通过" value="通过" />
          <el-option label="驳回" value="驳回" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>
      <div class="action-group">
      </div>
    </div>

    <el-table :data="tableData" v-loading="loading" class="custom-table" style="width: 100%">
      <el-table-column type="index" label="序号" width="70" align="center" />

      <el-table-column align="center" prop="batchNo" label="提交单号" />

      <el-table-column prop="courseName" align="center" label="相关课程" />

      <el-table-column align="center" label="提交时间">
        <template #default="{ row }">
          {{ formatTime(row.submitTime) }}
        </template>
      </el-table-column>

      <el-table-column prop="teacherName" align="center" label="当前处理人" />

      <el-table-column align="center" label="处理阶段">

        <template #default="scope">
          <span class="custom-tag" :class="getStatusTag(scope.row.status)">
            {{ scope.row.status }}
          </span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="操作" fixed="right">

        <template #default="scope">
          <el-link type="primary" :icon="View" :underline="false" @click="handleViewDetail(scope.row)">详情</el-link>
          <el-link v-if="scope.row.status === 'PENDING'" type="danger" :icon="RefreshLeft" :underline="false"
            @click="handleWithdraw(scope.row)" style="margin-left: 12px;">撤回</el-link>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="pagination.pageNum" v-model:page-size="pagination.pageSize"
      :total="pagination.total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
      @current-change="fetchData" @size-change="fetchData" style="margin-top: 16px; justify-content: flex-end;" />


    <el-dialog v-model="detailVisible" title="成绩单详情" width="900px" destroy-on-close align-center>
      <el-table :data="detailData" v-loading="detailLoading" border stripe size="small"
        style="width: 100%; margin-bottom: 20px;">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="studentName" label="姓名" width="100" />
        <el-table-column prop="className" label="班级" min-width="120" />
        <el-table-column align="center" label="各项成绩">
          <el-table-column prop="dailyScore" label="平时" width="80" align="center" />
          <el-table-column prop="homeworkScore" label="作业" width="80" align="center" />
          <el-table-column prop="midScore" label="期中" width="80" align="center" />
          <el-table-column prop="finalScore" label="期末" width="80" align="center" />
          <el-table-column prop="makeupScore" label="补考" width="80" align="center" />
        </el-table-column>
        <el-table-column prop="totalScore" label="综合分" width="90" align="center" fixed="right">
          <template #default="{ row }">
            <span style="font-weight: 600; color: #409EFF;">{{ row.totalScore || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="gpa" label="绩点" width="70" align="center" fixed="right" />
        <el-table-column prop="gradeLevel" label="等级" width="70" align="center" fixed="right">
          <template #default="{ row }">
            <el-tag :type="row.totalScore >= 60 ? 'success' : 'danger'" size="small">
              {{ row.gradeLevel || '-' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div class="flex-row" style="justify-content: flex-end;">
        <el-pagination v-model:current-page="detailPagination.pageNum" v-model:page-size="detailPagination.pageSize"
          :total="detailPagination.total" layout="total, prev, pager, next" @current-change="fetchDetailData" />
      </div>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss"></style>
