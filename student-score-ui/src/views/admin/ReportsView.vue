<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { Search, View, Download, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const searchQuery = ref('')
const typeFilter = ref('')
const loading = ref(false)
const tableData = ref<any[]>([])
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

// 创建报表弹窗
const createDialogVisible = ref(false)
const createForm = ref({
  reportName: '',
  reportDesc: '',
  reportType: '综合报表',
  frequency: '手动',
  planId: null as number | null
})
const planList = ref<any[]>([])

const getTypeTag = (type: string) => {
  if (type === '综合报表') return 'tag-primary'
  if (type === '分析报表') return 'tag-success'
  if (type === '预警报表') return 'tag-danger'
  return 'tag-info'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request({
      url: '/admin/report/list',
      method: 'get',
      params: {
        keyword: searchQuery.value || undefined,
        type: typeFilter.value || undefined,
        pageNum: pagination.pageNum,
        pageSize: pagination.pageSize
      }
    }) as any
    if (res.code === 200) {
      tableData.value = res.data?.list || []
      pagination.total = res.data?.total || 0
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '获取报表列表失败')
  } finally {
    loading.value = false
  }
}

const fetchPlans = async () => {
  try {
    const res = await request({ url: '/admin/plan/list', method: 'get', params: { pageNum: 1, pageSize: 100 } }) as any
    if (res.code === 200) planList.value = res.data?.list || []
  } catch (e: any) { /** 静默 */ }
}

const handleSearch = () => {
  pagination.pageNum = 1
  fetchData()
}

const handlePreview = (row: any) => {
  // 在新标签页中打开 HTML 预览
  const token = localStorage.getItem('token') || ''
  window.open(`/api/admin/report/preview/${row.reportId}?token=${token}`, '_blank')
}

const handleExport = (row: any) => {
  // 直接下载 PDF
  const token = localStorage.getItem('token')
  const link = document.createElement('a')
  link.href = `/api/admin/report/export/${row.reportId}`
  // 附加 token
  const url = new URL(link.href, window.location.origin)
  link.href = url.href
  // 用 fetch 带 token 下载
  fetch(link.href, {
    headers: { 'Authorization': 'Bearer ' + token }
  }).then(res => res.blob()).then(blob => {
    const blobUrl = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = blobUrl
    a.download = (row.reportName || '报表') + '.pdf'
    a.click()
    URL.revokeObjectURL(blobUrl)
  }).catch(() => {
    ElMessage.error('导出失败')
  })
}

const handleCreate = () => {
  createForm.value = { reportName: '', reportDesc: '', reportType: '综合报表', frequency: '手动', planId: null }
  createDialogVisible.value = true
}

const submitCreate = async () => {
  if (!createForm.value.reportName) {
    ElMessage.warning('请输入报表名称')
    return
  }
  try {
    const res = await request({
      url: '/admin/report',
      method: 'post',
      data: createForm.value
    }) as any
    if (res.code === 200) {
      ElMessage.success('报表创建成功')
      createDialogVisible.value = false
      fetchData()
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '创建失败')
  }
}

const formatTime = (time: string) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

onMounted(() => {
  fetchData()
  fetchPlans()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>报表中心</h2>
      <p>导出各类成绩与统计分析报表</p>
    </div>

    <div class="action-bar">
      <div class="filter-group">
        <el-input v-model="searchQuery" placeholder="搜索报表名称..." :prefix-icon="Search" style="width: 240px;" clearable
          @keyup.enter="handleSearch" />
        <el-select v-model="typeFilter" placeholder="全部类型" style="width: 150px;" clearable @change="handleSearch">
          <el-option label="全部类型" value="" />
          <el-option label="综合报表" value="综合报表" />
          <el-option label="分析报表" value="分析报表" />
          <el-option label="预警报表" value="预警报表" />
        </el-select>
      </div>
      <div class="action-group">
        <el-button type="primary" :icon="Plus" @click="handleCreate">创建报表</el-button>
      </div>
    </div>

    <el-table :data="tableData" v-loading="loading" class="custom-table" style="width: 100%" row-key="reportId">
      <el-table-column type="index" label="序号" width="70" align="center" />
      <el-table-column prop="reportName" label="报表名称">
        <template #default="scope">
          <div class="flex-column" style="gap: 4px;">
            <span style="font-weight: 500;">{{ scope.row.reportName }}</span>
            <span style="font-size: 12px; color: #94a3b8;">{{ scope.row.reportDesc }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column align="center" label="报表类型">
        <template #default="scope">
          <span class="custom-tag" :class="getTypeTag(scope.row.reportType)">{{ scope.row.reportType }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="frequency" label="更新频率" align="center" />
      <el-table-column prop="term" label="关联学期" align="center" />
      <el-table-column label="最后更新时间" align="center">
        <template #default="scope">{{ formatTime(scope.row.updateTime) }}</template>
      </el-table-column>
      <el-table-column align="center" label="操作" fixed="right">
        <template #default="scope">
          <div class="flex-row" style="gap: 16px; height: 100%; align-items: center; justify-content: center;">
            <el-link type="primary" :icon="View" :underline="false" @click="handlePreview(scope.row)">预览</el-link>
            <el-link type="success" :icon="Download" :underline="false" @click="handleExport(scope.row)">导出</el-link>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="pagination.pageNum" v-model:page-size="pagination.pageSize"
      :total="pagination.total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
      @current-change="fetchData" @size-change="fetchData" style="margin-top: 16px; justify-content: flex-end;" />


    <el-dialog v-model="createDialogVisible" title="创建报表" width="550px" destroy-on-close>
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="报表名称" required>
          <el-input v-model="createForm.reportName" placeholder="请输入报表名称" />
        </el-form-item>
        <el-form-item label="报表描述">
          <el-input v-model="createForm.reportDesc" type="textarea" :rows="3" placeholder="请输入报表描述" />
        </el-form-item>
        <el-form-item label="报表类型">
          <el-select v-model="createForm.reportType" style="width: 100%;">
            <el-option label="综合报表" value="综合报表" />
            <el-option label="分析报表" value="分析报表" />
            <el-option label="预警报表" value="预警报表" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联计划">
          <el-select v-model="createForm.planId" placeholder="选择开课计划" clearable style="width: 100%;">
            <el-option v-for="p in planList" :key="p.planId"
              :label="`${p.term} - ${p.courseName || ''} (${p.className || ''})`" :value="p.planId" />
          </el-select>
        </el-form-item>
        <el-form-item label="更新频率">
          <el-select v-model="createForm.frequency" style="width: 100%;">
            <el-option label="手动" value="手动" />
            <el-option label="每日" value="每日" />
            <el-option label="每周" value="每周" />
            <el-option label="每月" value="每月" />
            <el-option label="每学期" value="每学期" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">确定创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss"></style>
