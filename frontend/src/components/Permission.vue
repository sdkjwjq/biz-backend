<template>
  <div class="permission-container">
    <el-card class="box-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="title">任务权限管理</span>
            <el-tag type="info" effect="plain" class="ml-2">共 {{ myTasks.length }} 项负责任务</el-tag>
          </div>
          <div class="header-right">
            <el-input 
              v-model="searchText" 
              placeholder="搜索任务名称" 
              style="width: 200px" 
              class="mr-2"
              clearable
            >
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
      </template>

      <!-- 任务列表表格 -->
      <el-table 
        :data="filteredTasks" 
        style="width: 100%" 
        border 
        stripe 
        :header-cell-style="{background:'#f8f9fa', color:'#606266'}"
      >
        <el-table-column prop="name" label="任务名称" min-width="220" show-overflow-tooltip />
        <el-table-column prop="project" label="所属项目" width="180" show-overflow-tooltip />
        <el-table-column prop="deadline" label="截止时间" width="120" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="scope">
            <el-tooltip content="将此任务权限转交给其他人员" placement="top" :disabled="scope.row.status === '已完成'">
              <el-button 
                type="primary" 
                link 
                size="small" 
                @click="openTransferDialog(scope.row)"
                :disabled="scope.row.status === '已完成'"
              >
                <el-icon class="mr-1"><Switch /></el-icon>转交
              </el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 任务转交弹窗 -->
    <el-dialog 
      v-model="transferVisible" 
      title="权限转交" 
      width="450px" 
      destroy-on-close
    >
      <div class="transfer-content" v-if="currentTask">
        <el-alert
          type="warning"
          :closable="false"
          class="mb-3"
          show-icon
        >
          <template #title>
            注意：转交后您将失去对任务 <span class="font-bold">“{{ currentTask.name }}”</span> 的管理权限。
          </template>
        </el-alert>
        
        <el-form :model="transferForm" label-width="80px" class="mt-4">
          <el-form-item label="当前状态">
            <el-tag size="small">{{ currentTask.status }}</el-tag>
          </el-form-item>
          <el-form-item label="接收人员" required>
            <el-select 
              v-model="transferForm.targetUser" 
              placeholder="请搜索并选择接收人" 
              filterable 
              style="width: 100%"
            >
              <el-option 
                v-for="user in userList" 
                :key="user.id" 
                :label="user.name + ' (' + user.dept + ')'" 
                :value="user.id" 
              />
            </el-select>
          </el-form-item>
          <el-form-item label="转交原因">
            <el-input 
              v-model="transferForm.reason" 
              type="textarea" 
              :rows="3" 
              placeholder="请输入转交原因及交接事项说明..." 
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="transferVisible = false">取消</el-button>
          <el-button type="primary" @click="submitTransfer" :loading="transferring">
            确认转交
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { Switch, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// --- 模拟数据 ---
const myTasks = ref([
  { id: 1, name: '1.2.3 实施师德师风行为准则考核', project: '国双高项目', deadline: '2025-12-31', status: '进行中' },
  { id: 2, name: '2.1.2 建立校企定期需求对接机制', project: '国双高项目', deadline: '2025-12-31', status: '进行中' },
  { id: 3, name: '年度绩效数据填报', project: '日常工作', deadline: '2025-06-30', status: '未开始' },
  { id: 4, name: '实训室安全检查报告', project: '安全专项', deadline: '2025-05-20', status: '已完成' },
  { id: 5, name: '省级教学成果奖申报材料汇总', project: '重大成果培育', deadline: '2025-09-10', status: '进行中' },
])

const userList = [
  { id: 'u001', name: '张老师', dept: '教务处' },
  { id: 'u002', name: '李主任', dept: '科研处' },
  { id: 'u003', name: '王科长', dept: '人事处' },
  { id: 'u004', name: '赵助理', dept: '“双高”建设办公室' },
  { id: 'u005', name: '陈院长', dept: '机电学院' },
]

// --- 状态与逻辑 ---
const searchText = ref('')
const transferVisible = ref(false)
const transferring = ref(false)
const currentTask = ref(null)
const transferForm = reactive({
  targetUser: '',
  reason: ''
})

// 计算属性：前端搜索
const filteredTasks = computed(() => {
  if (!searchText.value) return myTasks.value
  return myTasks.value.filter(task => 
    task.name.toLowerCase().includes(searchText.value.toLowerCase()) || 
    task.project.toLowerCase().includes(searchText.value.toLowerCase())
  )
})

const getStatusType = (status) => {
  if (status === '已完成') return 'success'
  if (status === '进行中') return 'primary'
  if (status === '未开始') return 'info'
  return 'warning'
}

const openTransferDialog = (row) => {
  currentTask.value = row
  transferForm.targetUser = ''
  transferForm.reason = ''
  transferVisible.value = true
}

const submitTransfer = () => {
  if(!transferForm.targetUser) {
    ElMessage.warning('请选择接收人')
    return
  }
  
  transferring.value = true
  // 模拟 API 请求延迟
  setTimeout(() => {
    const targetUser = userList.find(u => u.id === transferForm.targetUser)
    ElMessage.success(`任务 "${currentTask.value.name}" 已成功转交给 ${targetUser.name}`)
    
    // 模拟从列表中移除该任务
    const idx = myTasks.value.findIndex(t => t.id === currentTask.value.id)
    if(idx > -1) myTasks.value.splice(idx, 1)
    
    transferring.value = false
    transferVisible.value = false
  }, 800)
}
</script>

<style scoped>
.permission-container {
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

.font-bold {
  font-weight: bold;
}

.ml-2 { margin-left: 8px; }
.mr-1 { margin-right: 4px; }
.mr-2 { margin-right: 8px; }
.mb-3 { margin-bottom: 12px; }
.mt-4 { margin-top: 16px; }
</style>