<template>
  <div class="data-center-container">
    <el-card class="box-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="title">数据中心</span>
            <el-tag type="success" effect="plain" class="ml-2">数据已更新至 {{ lastUpdateTime }}</el-tag>
          </div>
          <div class="header-right">
            <el-date-picker
              v-model="year"
              type="year"
              placeholder="选择年份"
              style="width: 120px"
              class="mr-2"
              @change="fetchData"
            />
            <el-button type="primary" :icon="Download" @click="handleBatchExport">批量导出</el-button>
          </div>
        </div>
      </template>

      <!-- 顶部统计卡片 -->
      <el-row :gutter="20" class="mb-4">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-title">年度总任务数</div>
            <div class="stat-value">4,321</div>
            <div class="stat-desc text-green">同比增长 12% <el-icon><Top /></el-icon></div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-title">年度资金预算</div>
            <div class="stat-value">1,200 <span class="unit">万元</span></div>
            <div class="stat-desc text-blue">执行率 45%</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-title">核心绩效指标</div>
            <div class="stat-value">128 <span class="unit">项</span></div>
            <div class="stat-desc text-orange">达标率 88%</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-title">生成报表数</div>
            <div class="stat-value">56 <span class="unit">份</span></div>
            <div class="stat-desc">最近生成：2小时前</div>
          </div>
        </el-col>
      </el-row>

      <!-- 报表列表 -->
      <el-tabs v-model="activeTab" class="report-tabs">
        <el-tab-pane label="年度报表" name="year">
          <el-table :data="yearReports" style="width: 100%" border stripe v-loading="loading">
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column prop="name" label="报表名称" min-width="250">
              <template #default="scope">
                <div class="report-name">
                  <el-icon class="file-icon"><Document /></el-icon>
                  {{ scope.row.name }}
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="type" label="类型" width="120" align="center">
              <template #default="scope">
                <el-tag size="small">{{ scope.row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="size" label="大小" width="100" align="center" />
            <el-table-column prop="createTime" label="生成时间" width="180" align="center" />
            <el-table-column label="操作" width="180" align="center">
              <template #default="scope">
                <el-button link type="primary" :icon="View" @click="handlePreview(scope.row)">预览</el-button>
                <el-button link type="primary" :icon="Download" @click="handleExport(scope.row)">下载</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="季度报表" name="quarter">
          <el-table :data="quarterReports" style="width: 100%" border stripe v-loading="loading">
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column prop="name" label="报表名称" min-width="250">
              <template #default="scope">
                <div class="report-name">
                  <el-icon class="file-icon"><Document /></el-icon>
                  {{ scope.row.name }}
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="quarter" label="季度" width="100" align="center">
              <template #default="scope">
                <el-tag type="warning" size="small">{{ scope.row.quarter }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="scope">
                <el-tag v-if="scope.row.status === '已归档'" type="success" size="small">已归档</el-tag>
                <el-tag v-else type="info" size="small">草稿</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="生成时间" width="180" align="center" />
            <el-table-column label="操作" width="180" align="center">
              <template #default="scope">
                <el-button link type="primary" :icon="View" @click="handlePreview(scope.row)">预览</el-button>
                <el-button link type="primary" :icon="Download" @click="handleExport(scope.row)">下载</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Download, View, Document, Top } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const year = ref('2025')
const activeTab = ref('year')

// 动态获取当天日期并设置为 00:00:00
const now = new Date()
const yearStr = now.getFullYear()
const monthStr = String(now.getMonth() + 1).padStart(2, '0')
const dayStr = String(now.getDate()).padStart(2, '0')
const lastUpdateTime = ref(`${yearStr}-${monthStr}-${dayStr} 00:00:00`)

// --- 模拟数据 ---
const yearReports = ref([
  { id: 1, name: '2025年度双高建设任务完成情况总表', type: '综合报表', size: '2.5MB', createTime: '2025-12-31 10:00' },
  { id: 2, name: '2025年度资金预算执行决算表', type: '财务报表', size: '1.8MB', createTime: '2025-12-30 15:30' },
  { id: 3, name: '2025年度核心绩效指标监测报告', type: '绩效报表', size: '3.2MB', createTime: '2025-12-28 09:15' },
  { id: 4, name: '2025年度标志性成果汇编', type: '成果汇编', size: '15.6MB', createTime: '2025-12-25 14:00' },
])

const quarterReports = ref([
  { id: 11, name: '2025年第一季度任务进度监测表', quarter: 'Q1', status: '已归档', createTime: '2025-04-05 10:00' },
  { id: 12, name: '2025年第二季度任务进度监测表', quarter: 'Q2', status: '已归档', createTime: '2025-07-05 10:00' },
  { id: 13, name: '2025年第三季度任务进度监测表', quarter: 'Q3', status: '已归档', createTime: '2025-10-08 10:00' },
  { id: 14, name: '2025年第四季度任务进度监测表', quarter: 'Q4', status: '草稿', createTime: '2025-12-15 10:00' },
])

// --- 方法 ---
const fetchData = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    // 已移除成功提示消息
  }, 500)
}

const handleExport = (row) => {
  ElMessage.success(`正在下载：${row.name}`)
}

const handleBatchExport = () => {
  ElMessage.success('正在打包下载选中的报表...')
}

const handlePreview = (row) => {
  ElMessage.info(`预览报表：${row.name}`)
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.data-center-container {
  height: 100%;
}
.box-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}
:deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 20px;
  overflow: hidden; 
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-left {
  display: flex;
  align-items: center;
}
.title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

/* 统计卡片 */
.stat-card {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  text-align: center;
  border: 1px solid #ebeef5;
}
.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
  font-family: 'DIN Alternate', sans-serif;
}
.unit {
  font-size: 12px;
  font-weight: normal;
  color: #909399;
}
.stat-desc {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}
.text-green { color: #67c23a; }
.text-blue { color: #409eff; }
.text-orange { color: #e6a23c; }

/* 报表列表 */
.report-tabs {
  margin-top: 10px;
}
.report-name {
  display: flex;
  align-items: center;
  font-weight: 500;
  color: #303133;
}
.file-icon {
  margin-right: 8px;
  font-size: 16px;
  color: #409eff;
}

.ml-2 { margin-left: 8px; }
.mr-2 { margin-right: 8px; }
.mb-4 { margin-bottom: 16px; }
</style>