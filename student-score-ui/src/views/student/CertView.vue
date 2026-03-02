<script setup lang="ts">
// 将后端 ISO 时间字符串转换为标准显示格式
const formatTime = (val: string) => val ? val.replace('T', ' ').substring(0, 19) : '-'
import { ref } from 'vue'
import { Document } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const certType = ref('zh')
const purpose = ref('study')
const previewLoading = ref(false)
const certRecords = ref<any[]>([])
const recordLoading = ref(false)

const purposeMap: Record<string, string> = {
  study: '留学申请', job: '求职应聘', postgrad: '保研推免', other: '其他'
}

const fetchCertRecords = async () => {
  recordLoading.value = true
  try {
    const res = await request({ url: '/student/cert/list', method: 'get' }) as any
    if (res.code === 200) {
      certRecords.value = res.data || []
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '获取申请记录失败')
  } finally {
    recordLoading.value = false
  }
}

// 调用后端 PDF 预览接口：后端直接返回 PDF 二进制流，在新标签页内展示
const handlePreview = async () => {
  previewLoading.value = true
  try {
    const token = localStorage.getItem('token') || ''
    const purposeLabel = purposeMap[purpose.value] || '其他'
    // 使用 <a> 在新窗口打开（浏览器内置 PDF 阅读器展示）
    const url = `/api/student/cert/preview?certType=${certType.value}&purpose=${encodeURIComponent(purposeLabel)}`
    const response = await fetch(url, {
      headers: { Authorization: 'Bearer ' + token }
    })
    if (!response.ok) {
      ElMessage.error('预览生成失败，请确认已有成绩数据')
      return
    }
    const blob = await response.blob()
    const objectUrl = URL.createObjectURL(blob)
    window.open(objectUrl, '_blank')
    // 刷新申请记录（PDF 预览会自动记录一条申请）
    fetchCertRecords()
    ElMessage.success('成绩单已生成，请在新标签页查看')
  } catch (error: any) {
    ElMessage.error(error?.message || '预览失败，请重试')
  } finally {
    previewLoading.value = false
  }
}

const getStatusTag = (status: string) => {
  if (status === '已完成') return 'tag-success'
  if (status === '处理中') return 'tag-warning'
  if (status === '已取消') return 'tag-danger'
  return 'tag-info'
}

fetchCertRecords()
</script>

<template>
  <div class="page-container" style="background: transparent; box-shadow: none; padding: 0;">
    <div style="background: white; padding: 24px; border-radius: 8px; margin-bottom: 20px;">
      <div class="page-header" style="margin-bottom: 20px;">
        <h2>成绩证明与打印</h2>
        <p>生成官方成绩单电子版 PDF</p>
      </div>

      <div class="config-section"
        style="display: flex; gap: 40px; align-items: flex-end; padding: 24px; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px;">
        <div class="form-group" style="flex: 1;">
          <div style="font-size: 13px; color: #64748b; margin-bottom: 8px;">成绩单类型</div>
          <el-radio-group v-model="certType">
            <el-radio-button label="zh">中文成绩单</el-radio-button>
            <el-radio-button label="en">英文成绩单</el-radio-button>
          </el-radio-group>
        </div>
        <div class="form-group" style="flex: 1;">
          <div style="font-size: 13px; color: #64748b; margin-bottom: 8px;">证明用途</div>
          <el-select v-model="purpose" style="width: 100%;">
            <el-option label="留学申请" value="study" />
            <el-option label="求职应聘" value="job" />
            <el-option label="保研推免" value="postgrad" />
            <el-option label="其他" value="other" />
          </el-select>
        </div>
        <el-button type="primary" :icon="Document" :loading="previewLoading" @click="handlePreview">
          点击预览并下载成绩单PDF
        </el-button>
      </div>

      <div
        style="margin-top: 16px; padding: 12px 16px; background: #eff6ff; border-radius: 6px; font-size: 13px; color: #3b82f6;">
        💡 点击「预览生成 PDF」将自动在新标签页中打开您的电子成绩单（浏览器内置 PDF 阅读器展示，可直接下载保存）。
      </div>
    </div>


    <div style="background: white; padding: 24px; border-radius: 8px; border: 1px solid #e2e8f0;"
      v-loading="recordLoading">
      <div style="font-size: 15px; font-weight: 600; color: #1e293b; margin-bottom: 16px;">申请记录</div>
      <el-table :data="certRecords" style="width: 100%;">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column align="center" prop="certType" label="证明类型" />
        <el-table-column align="center" label="申请时间">
          <template #default="{ row }">
            {{ formatTime(row.applyTime) }}
          </template>
        </el-table-column>
        <el-table-column align="center" prop="status" label="状态">
          <template #default="{ row }">
            <span class="custom-tag" :class="getStatusTag(row.status)">{{ row.status }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>
