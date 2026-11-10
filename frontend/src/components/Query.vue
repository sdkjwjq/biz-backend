<template>
  <div class="query-container">
    <el-card class="box-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="title">查询中心</span>
            <el-tag type="info" size="small" class="ml-2">全量任务检索与详情查看</el-tag>
          </div>
        </div>
      </template>

      <!-- 搜索筛选区 -->
      <div class="filter-container">
        <el-form :inline="true" :model="filters" class="demo-form-inline" size="default">
          <el-form-item label="关键词">
            <el-input 
              v-model="filters.keyword" 
              placeholder="任务名称/编号" 
              clearable 
              style="width: 200px"
              @keyup.enter="handleSearch"
            >
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item label="所属项目">
            <el-select v-model="filters.project" placeholder="全部项目" clearable style="width: 180px">
              <el-option label="打造技术技能人才培养高地" value="双高-人才" />
              <el-option label="打造技术技能创新服务平台" value="双高-平台" />
              <el-option label="提升校企合作水平" value="双高-校企" />
              <el-option label="提升学校治理水平" value="双高-治理" />
              <el-option label="重大成果培育" value="重大成果" />
            </el-select>
          </el-form-item>
          <el-form-item label="负责部门">
            <el-select v-model="filters.dept" placeholder="全部部门" clearable style="width: 140px">
              <el-option label="教务处" value="教务处" />
              <el-option label="科研处" value="科研处" />
              <el-option label="学工处" value="学工处" />
              <el-option label="人事处" value="人事处" />
              <el-option label="“双高”建设办公室" value="“双高”建设办公室" />
              <el-option label="机电学院" value="机电学院" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="filters.status" placeholder="全部状态" clearable style="width: 120px">
              <el-option label="进行中" value="进行中" />
              <el-option label="已完成" value="已完成" />
              <el-option label="未开始" value="未开始" />
              <el-option label="逾期" value="逾期" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
            <el-button :icon="Refresh" @click="resetFilters">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 结果表格 -->
      <el-table 
        :data="paginatedData" 
        style="width: 100%" 
        border 
        stripe 
        v-loading="loading"
        :header-cell-style="{background:'#f8f9fa', color:'#606266'}"
      >
        <el-table-column prop="code" label="任务编号" width="120" align="center" show-overflow-tooltip />
        <el-table-column prop="name" label="任务名称" min-width="250" show-overflow-tooltip>
          <template #default="scope">
            <span class="task-link" @click="viewDetail(scope.row)">{{ scope.row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="project" label="所属项目" width="180" show-overflow-tooltip />
        <el-table-column prop="dept" label="负责部门" width="120" show-overflow-tooltip />
        <el-table-column prop="principal" label="负责人" width="100" align="center" />
        <el-table-column prop="progress" label="进度" width="160">
          <template #default="scope">
            <el-progress 
              :percentage="scope.row.progress" 
              :status="getProgressStatus(scope.row)" 
              :stroke-width="8"
            />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusTag(scope.row.status)" effect="plain" size="small">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" :icon="View" @click="viewDetail(scope.row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailVisible" title="任务详情档案" size="70%">
      <div v-if="currentTask" class="detail-content">
        <!-- 基础信息 -->
        <div class="detail-section">
          <div class="section-title">基础信息</div>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="任务编号">{{ currentTask.code }}</el-descriptions-item>
            <el-descriptions-item label="任务状态">
              <el-tag :type="getStatusTag(currentTask.status)" size="small">{{ currentTask.status }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="所属项目" :span="2">{{ currentTask.project }}</el-descriptions-item>
            <el-descriptions-item label="负责部门">{{ currentTask.dept }}</el-descriptions-item>
            <el-descriptions-item label="负责人">{{ currentTask.principal }}</el-descriptions-item>
            <el-descriptions-item label="起止时间" :span="2">{{ currentTask.startDate }} 至 {{ currentTask.endDate }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 任务描述 -->
        <div class="detail-section">
          <div class="section-title">任务描述</div>
          <div class="text-block">
            {{ currentTask.description || '暂无详细描述。' }}
          </div>
        </div>

        <!-- 预期成果 -->
        <div class="detail-section">
          <div class="section-title">预期成果</div>
          <div class="text-block">
            <ul class="result-list" v-if="currentTask.expectedResults && currentTask.expectedResults.length">
              <li v-for="(item, idx) in currentTask.expectedResults" :key="idx">{{ item }}</li>
            </ul>
            <span v-else class="text-gray-400">无明确预期成果描述</span>
          </div>
        </div>

        <!-- 资金情况 -->
        <div class="detail-section">
          <div class="section-title">资金预算</div>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="预算总额">50.00 万元</el-descriptions-item>
            <el-descriptions-item label="已执行">
              <span class="text-blue-500 font-bold">20.00 万元</span> (40%)
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 执行记录 -->
        <div class="detail-section">
          <div class="section-title">最新进展记录</div>
          <el-timeline>
            <el-timeline-item
              v-for="(activity, index) in currentTask.activities"
              :key="index"
              :timestamp="activity.timestamp"
              :type="index === 0 ? 'primary' : ''"
              :color="index === 0 ? '#409eff' : ''"
            >
              {{ activity.content }}
            </el-timeline-item>
            <el-timeline-item timestamp="2025-01-01" type="info">任务创建</el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, Refresh, View } from '@element-plus/icons-vue'

// --- 状态管理 ---
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const detailVisible = ref(false)
const currentTask = ref(null)

const filters = reactive({
  keyword: '',
  project: '',
  dept: '',
  status: ''
})

// --- 模拟数据 ---
const allTasks = [
  {
    id: 1,
    code: 'T-2025-001',
    name: '1.2.3 实施师德师风行为准则考核',
    project: '双高-人才',
    dept: '人事处',
    principal: '王科长',
    status: '进行中',
    progress: 45,
    startDate: '2025-01-01',
    endDate: '2025-12-31',
    description: '建立健全师德师风建设长效机制，实施师德师风行为准则考核，实行师德失范“一票否决”。',
    expectedResults: ['师德师风考核办法', '年度考核报告'],
    activities: [
      { content: '完成考核办法修订', timestamp: '2025-02-15' },
      { content: '启动上半年考核工作', timestamp: '2025-06-01' }
    ]
  },
  {
    id: 2,
    code: 'T-2025-002',
    name: '2.1.2 建立校企定期需求对接机制',
    project: '双高-校企',
    dept: '科研处',
    principal: '李主任',
    status: '进行中',
    progress: 30,
    startDate: '2025-01-01',
    endDate: '2025-12-31',
    description: '与行业龙头企业建立定期沟通机制，每季度召开一次校企合作对接会。',
    expectedResults: ['校企对接会议纪要', '人才需求分析报告'],
    activities: [
      { content: '召开第一季度对接会', timestamp: '2025-03-20' }
    ]
  },
  {
    id: 3,
    code: 'T-2025-003',
    name: '3.1 核心课程建设数量达标审核',
    project: '双高-人才',
    dept: '教务处',
    principal: '张老师',
    status: '未开始',
    progress: 0,
    startDate: '2025-06-01',
    endDate: '2025-12-31',
    description: '对照双高建设标准，审核核心课程建设数量及质量。',
    expectedResults: ['课程建设审核报告'],
    activities: []
  },
  {
    id: 4,
    code: 'T-2025-004',
    name: '实训基地设备采购第一期',
    project: '双高-平台',
    dept: '教务处',
    principal: '赵助理',
    status: '已完成',
    progress: 100,
    startDate: '2025-01-01',
    endDate: '2025-05-30',
    description: '完成智能制造实训基地第一期设备采购及安装调试。',
    expectedResults: ['设备采购合同', '验收报告'],
    activities: [
      { content: '设备招标', timestamp: '2025-02-10' },
      { content: '设备到货安装', timestamp: '2025-04-15' },
      { content: '验收通过', timestamp: '2025-05-28' }
    ]
  },
  {
    id: 5,
    code: 'T-2025-005',
    name: '省级教学成果奖申报材料汇总',
    project: '重大成果',
    dept: '“双高”建设办公室',
    principal: '孙主任',
    status: '进行中',
    progress: 60,
    startDate: '2025-03-01',
    endDate: '2025-09-10',
    description: '汇总整理全校申报省级教学成果奖的相关材料，组织专家打磨。',
    expectedResults: ['申报材料汇编', '专家指导记录'],
    activities: [
      { content: '收集各学院材料', timestamp: '2025-04-01' },
      { content: '第一轮专家评审', timestamp: '2025-05-20' }
    ]
  },
  {
    id: 6,
    code: 'T-2025-006',
    name: '国际交流合作项目备案',
    project: '双高-治理',
    dept: '机电学院',
    principal: '陈院长',
    status: '逾期',
    progress: 10,
    startDate: '2025-01-01',
    endDate: '2025-03-01',
    description: '完成年度新增国际交流项目的备案工作。',
    expectedResults: ['项目备案表'],
    activities: [
      { content: '启动备案通知', timestamp: '2025-01-05' }
    ]
  }
]

// 响应式数据
const displayList = ref([])

// --- 方法 ---

// 模拟后端查询
const performSearch = () => {
  loading.value = true
  
  // 模拟延迟
  setTimeout(() => {
    let results = allTasks.filter(item => {
      const matchKeyword = !filters.keyword || item.name.includes(filters.keyword) || item.code.includes(filters.keyword)
      const matchProject = !filters.project || item.project.includes(filters.project)
      const matchDept = !filters.dept || item.dept === filters.dept
      const matchStatus = !filters.status || item.status === filters.status
      return matchKeyword && matchProject && matchDept && matchStatus
    })
    
    total.value = results.length
    
    // 前端分页逻辑
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    displayList.value = results.slice(start, end)
    
    loading.value = false
  }, 300)
}

const handleSearch = () => {
  currentPage.value = 1
  performSearch()
}

const resetFilters = () => {
  filters.keyword = ''
  filters.project = ''
  filters.dept = ''
  filters.status = ''
  handleSearch()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  performSearch()
}

const handlePageChange = (val) => {
  currentPage.value = val
  performSearch()
}

const viewDetail = (row) => {
  currentTask.value = row
  detailVisible.value = true
}

// 辅助方法
const getStatusTag = (status) => {
  const map = { '进行中': 'primary', '已完成': 'success', '未开始': 'info', '逾期': 'danger' }
  return map[status] || ''
}

const getProgressStatus = (row) => {
  if (row.status === '逾期') return 'exception'
  if (row.progress === 100) return 'success'
  return ''
}

// 计算属性供表格使用
const paginatedData = computed(() => displayList.value)

onMounted(() => {
  handleSearch()
})
</script>

<style scoped>
.query-container {
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

.filter-container {
  margin-bottom: 10px;
  padding: 15px 15px 0 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.task-link {
  color: #409eff;
  cursor: pointer;
  font-weight: 500;
}
.task-link:hover {
  text-decoration: underline;
}

.pagination-container {
  margin-top: auto;
  padding-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 详情抽屉样式 */
.detail-content {
  padding: 0 20px;
}
.detail-section {
  margin-bottom: 24px;
}
.section-title {
  font-size: 15px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 12px;
  border-left: 4px solid #409eff;
  padding-left: 10px;
}
.text-block {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  background: #fcfcfc;
  padding: 10px;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}
.result-list {
  padding-left: 20px;
  margin: 0;
}
.ml-2 { margin-left: 8px; }
.text-blue-500 { color: #409eff; }
.font-bold { font-weight: bold; }
</style>
