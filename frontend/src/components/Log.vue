<template>
  <div class="system-log-container">
    <el-card class="box-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="title">系统操作日志</span>
            <el-tag type="info" size="small" class="ml-2">共 {{ total }} 条记录</el-tag>
          </div>
          <div class="header-right">
            <el-button type="primary" plain :icon="Download" @click="handleExport">导出日志</el-button>
          </div>
        </div>
      </template>

      <!-- 筛选区域 -->
      <div class="filter-container">
        <el-form :inline="true" :model="filters" class="demo-form-inline" size="default">
          <el-form-item label="操作人员">
            <el-input v-model="filters.user" placeholder="姓名/账号" clearable style="width: 150px" />
          </el-form-item>
          <el-form-item label="功能模块">
            <el-select v-model="filters.module" placeholder="全部模块" clearable style="width: 150px">
              <el-option label="系统登录" value="login" />
              <el-option label="任务中心" value="task" />
              <el-option label="审核中心" value="audit" />
              <el-option label="用户管理" value="user" />
              <el-option label="数据中心" value="data" />
            </el-select>
          </el-form-item>
          <el-form-item label="操作状态">
             <el-select v-model="filters.status" placeholder="全部" clearable style="width: 100px">
              <el-option label="成功" value="success" />
              <el-option label="失败" value="fail" />
            </el-select>
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="filters.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 240px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
            <el-button :icon="Refresh" @click="resetFilters">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 表格区域 -->
      <el-table 
        :data="tableData" 
        v-loading="loading" 
        style="width: 100%" 
        border 
        stripe
        :header-cell-style="{background:'#f8f9fa', color:'#606266'}"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="操作人员" width="120">
          <template #default="scope">
            <div class="user-cell">
              <el-avatar :size="24" class="mr-2">{{ scope.row.username.charAt(0) }}</el-avatar>
              <span>{{ scope.row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP地址" width="140" show-overflow-tooltip />
        <el-table-column prop="module" label="功能模块" width="120">
          <template #default="scope">
            <el-tag :type="getModuleTag(scope.row.moduleType)" size="small" effect="plain">
              {{ scope.row.module }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="action" label="操作内容" min-width="250" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'success' ? 'success' : 'danger'" size="small">
              <span class="dot" :class="scope.row.status"></span>
              {{ scope.row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="操作时间" width="180" sortable />
        <el-table-column prop="duration" label="耗时" width="100" align="right">
          <template #default="scope">
            <span :class="scope.row.duration > 500 ? 'text-warning' : 'text-gray'">
              {{ scope.row.duration }} ms
            </span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页区域 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// --- 状态定义 ---
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const filters = reactive({
  user: '',
  module: '',
  status: '',
  dateRange: []
})

// --- 模拟数据生成 ---
const generateMockData = () => {
  const modules = [
    { name: '系统登录', type: 'login' },
    { name: '任务中心', type: 'task' },
    { name: '审核中心', type: 'audit' },
    { name: '用户管理', type: 'user' },
    { name: '数据中心', type: 'data' }
  ]
  const users = ['admin', '张老师', '李主任', '王科长', '刘院长']
  const actions = [
    '登录系统', '修改了密码', '新增了任务 [SG-2025-001]', '审核通过了资金申请', 
    '导出了月度报表', '删除了临时文件', '查看了驾驶舱数据', '上传了佐证材料'
  ]
  const ips = ['192.168.1.10', '192.168.1.15', '10.0.0.5', '172.16.0.1']

  const data = []
  for (let i = 0; i < 15; i++) {
    const mod = modules[Math.floor(Math.random() * modules.length)]
    const isSuccess = Math.random() > 0.1
    
    data.push({
      id: 10000 + i,
      username: users[Math.floor(Math.random() * users.length)],
      ip: ips[Math.floor(Math.random() * ips.length)],
      module: mod.name,
      moduleType: mod.type,
      action: actions[Math.floor(Math.random() * actions.length)],
      status: isSuccess ? 'success' : 'fail',
      time: `2025-02-15 ${String(10 + i).padStart(2, '0')}:${String(Math.floor(Math.random()*60)).padStart(2, '0')}:00`,
      duration: Math.floor(Math.random() * 800) + 20
    })
  }
  return data
}

const tableData = ref([])

// --- 方法 ---
const fetchData = () => {
  loading.value = true
  // 模拟网络请求
  setTimeout(() => {
    tableData.value = generateMockData()
    total.value = 1254 // 模拟总数
    loading.value = false
  }, 500)
}

const handleSearch = () => {
  ElMessage.success('查询已提交')
  fetchData()
}

const resetFilters = () => {
  filters.user = ''
  filters.module = ''
  filters.status = ''
  filters.dateRange = []
  fetchData()
}

const handleExport = () => {
  ElMessage.success('正在导出 Excel 文件...')
}

const handleSizeChange = (val) => {
  console.log(`每页 ${val} 条`)
  fetchData()
}

const handleCurrentChange = (val) => {
  console.log(`当前页: ${val}`)
  fetchData()
}

const getModuleTag = (type) => {
  const map = {
    'login': 'info',
    'task': 'primary',
    'audit': 'warning',
    'user': 'success',
    'data': 'danger'
  }
  return map[type] || ''
}

// --- 生命周期 ---
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.system-log-container {
  height: 100%;
}
.box-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}
/* 强制卡片body占满剩余空间以支持内容滚动（如果有需要）或分页置底 */
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
.ml-2 { margin-left: 10px; }
.mr-2 { margin-right: 8px; }

.filter-container {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.user-cell {
  display: flex;
  align-items: center;
}

.dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 4px;
}
.dot.success { background-color: #67c23a; }
.dot.fail { background-color: #f56c6c; }

.text-gray { color: #909399; }
.text-warning { color: #e6a23c; font-weight: bold; }

.pagination-container {
  margin-top: auto; /* 推到底部 */
  padding-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>