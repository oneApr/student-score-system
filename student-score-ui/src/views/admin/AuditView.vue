<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { Search, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAuditList, auditPass, auditReject } from '@/api/audit'
import request from '@/utils/request'

const searchQuery = ref('')
const statusFilter = ref('')
const loading = ref(false)
const tableData = ref<any[]>([])
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const detailDialogVisible = ref(false)
const detailData = ref<any[]>([])
const rejectDialogVisible = ref(false)
const rejectForm = ref({ id: 0, reason: '' })

const getStatusTag = (status: string) => {
  if (status === '通过' || status === '已通过') return 'tag-success'
  if (status === '待审核') return 'tag-warning'
  if (status === '驳回' || status === '已退回') return 'tag-danger'
  return 'tag-info'
}

const getStatusText = (status: string) => {
  if (status === '通过' || status === '已通过') return '已通过'
  if (status === '待审核') return '待审核'
  if (status === '驳回' || status === '已退回') return '已驳回'
  return status
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getAuditList({
      keyword: searchQuery.value,
      status: statusFilter.value || undefined,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }) as any
    if (res.code === 200) {
      tableData.value = res.data?.list || []
      pagination.total = res.data?.total || 0
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '获取审核列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  fetchData()
}

const handleAuditPass = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认审核通过该成绩？', '提示', { type: 'warning' })
    // 后端路径 /admin/audit/pass/{batchId}，传 batchId 而非 auditId
    await auditPass(row.batchId)
    ElMessage.success('审核通过')
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error.message || '操作失败')
  }
}

const handleAuditReject = (row: any) => {
  rejectForm.value = { id: row.batchId, reason: '' }
  rejectDialogVisible.value = true
}

const submitReject = async () => {
  try {
    await auditReject(rejectForm.value.id, rejectForm.value.reason)
    ElMessage.success('已退回')
    rejectDialogVisible.value = false
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleViewDetail = async (row: any) => {
  try {
    // 调用后端 /admin/audit/batch/{batchId} 获取真实成绩明细
    const res = await request({
      url: `/admin/audit/batch/${row.batchId}`,
      method: 'get'
    })
    detailData.value = (res as any).data || res || []
  } catch (error: any) {
    ElMessage.error(error?.message || '获取明细失败')
  }
  detailDialogVisible.value = true
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>成绩审核</h2>
      <p>管理教师提交成绩批次与系统教务复核</p>
    </div>

    <div class="action-bar">
      <div class="filter-group">
        <el-input v-model="searchQuery" placeholder="搜索单号、课程或教师..." :prefix-icon="Search" style="width: 280px;"
          clearable @keyup.enter="handleSearch" />
        <el-select v-model="statusFilter" placeholder="审核状态" style="width: 150px;" @change="handleSearch">
          <el-option label="全部状态" value="" />
          <el-option label="待审核" value="待审核" />
          <el-option label="已通过" value="通过" />
          <el-option label="已退回" value="驳回" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>
      <div class="action-group">
      </div>
    </div>

    <el-table :data="tableData" v-loading="loading" class="custom-table" style="width: 100%" row-key="batchId">
      <el-table-column type="index" label="序号" width="70" align="center" />

      <el-table-column align="center" prop="batchNo" label="提交单号" />

      <el-table-column prop="term" label="学期" />

      <el-table-column prop="courseCode" label="课程代码" />

      <el-table-column prop="courseName" label="课程名称" />

      <el-table-column prop="teacherName" label="提交教师" />

      <el-table-column align="center" label="成绩概况">

        <template #default="scope">
          <span style="color: #64748b; font-size: 13px;">
            平均分 {{ scope.row.avgScore }}，及格率 {{ scope.row.passRate }}
          </span>
        </template>
      </el-table-column>
      <el-table-column align="center" prop="submitTime" label="提交时间" />

      <el-table-column align="center" label="审核状态">

        <template #default="scope">
          <span class="custom-tag" :class="getStatusTag(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="操作" fixed="right">
        <template #default="scope">
          <div class="flex-row" style="gap: 12px; height: 100%; align-items: center; justify-content: center;">
            <el-link v-if="scope.row.status === '待审核'" type="success" :underline="false"
              @click="handleAuditPass(scope.row)">通过</el-link>
            <el-link v-if="scope.row.status === '待审核'" type="danger" :underline="false"
              @click="handleAuditReject(scope.row)">退回</el-link>
            <el-link type="info" :icon="View" :underline="false" @click="handleViewDetail(scope.row)">明细</el-link>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="pagination.pageNum" v-model:page-size="pagination.pageSize"
      :total="pagination.total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
      @current-change="fetchData" @size-change="fetchData" style="margin-top: 16px; justify-content: flex-end;" />


    <el-dialog v-model="detailDialogVisible" title="成绩明细" width="800px">
      <el-table :data="detailData">
        <el-table-column type="index" label="序号" width="70" align="center" />

        <el-table-column prop="studentNo" label="学号" />

        <el-table-column prop="studentName" label="姓名" />

        <el-table-column align="center" prop="dailyScore" label="平时成绩" />

        <el-table-column align="center" prop="homeworkScore" label="作业成绩" />

        <el-table-column align="center" prop="midScore" label="期中成绩" />

        <el-table-column align="center" prop="finalScore" label="期末成绩" />

        <el-table-column align="center" prop="totalScore" label="总评成绩" />

      </el-table>
    </el-dialog>


    <el-dialog v-model="rejectDialogVisible" title="退回原因" width="400px">
      <el-input v-model="rejectForm.reason" type="textarea" placeholder="请输入退回原因" :rows="4" />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReject">确定退回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss"></style>
