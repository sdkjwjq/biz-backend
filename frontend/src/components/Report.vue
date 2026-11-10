<template>
  <div class="report-container">
    <el-card class="box-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="title">督办中心</span>
            <el-tag type="info" size="small" class="ml-2">支持工作督办提醒</el-tag>
          </div>
          <div class="header-right">
            <!-- 可以在这里添加操作按钮 -->
          </div>
        </div>
      </template>

      <!-- 主要内容区域：表单 -->
      <div class="form-wrapper">
        <el-alert title="说明：请选择相关联的任务，并向指定人员发送督办提醒。" type="info" show-icon :closable="false" class="mb-4" />

        <el-form ref="reportFormRef" :model="reportForm" :rules="rules" label-width="100px" class="report-form"
          size="default">
          <QueryFilterBar response-mode="tree" domain="supervision" title="督办任务" :defaults="{year:'2026'}" @result="receiveTasks" @state="taskQueryState=$event" />
          <!-- 关联任务 -->
          <el-form-item label="关联任务" prop="taskId">
            <el-select v-model="reportForm.taskId" placeholder="请选择关联的任务" filterable :loading="taskQueryState.loading" style="width: 100%"
              @change="handleTaskChange">
              <el-option v-for="task in taskList" :key="task.id" :label="task.name" :value="task.id">
                <span style="float: left">{{ task.name }}</span>
                <!-- 下拉选项不显示“项目ID:x” -->
              </el-option>
            </el-select>
          </el-form-item>

          <!-- 任务详情展示 (只读) -->
          <div v-if="currentTask" class="task-info-box">
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="当前进度">
                <el-progress :percentage="currentTask.progress" :stroke-width="10" />
              </el-descriptions-item>
              <el-descriptions-item label="截止时间">{{ currentTask.deadline }}</el-descriptions-item>
              <el-descriptions-item label="当前状态">
                <el-tag size="small">{{ currentTask.status }}</el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- 督办对象：任务负责人 / 归口负责人 -->
          <el-form-item label="督办对象" prop="targetType">
            <div style="display:flex; align-items:center; gap: 14px; flex-wrap: wrap;">
              <el-radio-group v-model="reportForm.targetType">
                <el-radio-button label="leader">负责人</el-radio-button>
                <el-radio-button label="principal">归口审核人</el-radio-button>
              </el-radio-group>
              <span style="color:#606266;">
                当前选择：<b>{{ targetUserDisplay || '—' }}</b>
              </span>
            </div>
          </el-form-item>

          <!-- 标题/主题 -->
          <el-form-item label="主题摘要" prop="title">
            <el-input v-model="reportForm.title" placeholder="请输入简要说明 (如：关于xxx任务的阶段性材料)" />
          </el-form-item>

          <!-- 详细内容 -->
          <el-form-item label="详细说明" prop="content">
            <el-input v-model="reportForm.content" type="textarea" :rows="4" placeholder="请输入督办要求和整改建议..." />
          </el-form-item>

          <!-- 提交按钮 -->
          <el-form-item>
            <el-button type="primary" :icon="Position" @click="submitForm" :loading="submitting">
              发送督办
            </el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import QueryFilterBar from './QueryFilterBar.vue'
const taskQueryState=ref({loading:true,initialized:false,error:''});
const receiveTasks=result=>{const ids=new Set((result.matchedIds||[]).map(String));return loadBaseData((result.items||[]).filter(t=>ids.has(String(t.taskId))))}
import { ref, reactive, computed, onUnmounted, watch } from 'vue'
import { Position } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getAllUsers } from '@/api/user'
import { sendNotice } from '@/api/report'
import { getDeptById } from '@/api/dept'

// --- 状态与数据 ---
const reportFormRef = ref(null)
const submitting = ref(false)
const currentTask = ref(null)

const reportForm = reactive({
  taskId: '',
  targetType: 'leader', // leader | principal
  title: '',
  content: '',
})

let reportVersion = 0
let reportDisposed = false
watch(reportForm, () => { reportVersion++ }, { deep: true, flush: 'sync' })
onUnmounted(() => { reportDisposed = true })
// 真实任务/用户列表（由后端拉取）
const taskList = ref([])
const userList = ref([])
const deptMap = ref(new Map()) // deptId -> deptName

// 获取部门名
const getDeptName = (deptId) => {
  if (deptId === undefined || deptId === null) return ''
  const id = String(deptId)
  const name = deptMap.value.get(id)
  // 如果映射中没有，返回空字符串（不返回"部门${deptId}"，由调用方处理）
  return name || ''
}

// 表单校验规则
const rules = computed(() => {
  const base = {
  taskId: [{ required: true, message: '请选择关联任务', trigger: 'change' }],
    targetType: [{ required: true, message: '请选择督办对象', trigger: 'change' }],
  title: [{ required: true, message: '请输入主题摘要', trigger: 'blur' }],
    content: [{ required: true, message: '请输入详细说明', trigger: 'blur' }],
  }
  return base
})

// --- 方法 ---

const userMap = computed(() => {
  const m = new Map()
    ; (Array.isArray(userList.value) ? userList.value : []).forEach(u => {
      if (u?.id !== undefined && u?.id !== null) m.set(String(u.id), u)
    })
  return m
})

const parseJwtPayload = (token) => {
  try {
    if (!token || typeof token !== 'string') return null
    const parts = token.split('.')
    if (parts.length < 2) return null
    let payload = parts[1].replace(/-/g, '+').replace(/_/g, '/')
    const pad = payload.length % 4
    if (pad) payload += '='.repeat(4 - pad)
    const json = atob(payload)
    return JSON.parse(json)
  } catch (e) { // eslint-disable-line no-unused-vars
     
    return null
  }
}

const getCurrentUserId = () => {
  const token = localStorage.getItem('token') || ''
  const payload = parseJwtPayload(token)
  const id = payload?.id !== undefined && payload?.id !== null ? Number(payload.id) : null
  return id && !Number.isNaN(id) ? id : null
}

const clampPercent = (n) => Math.max(0, Math.min(100, Number.isFinite(Number(n)) ? Number(n) : 0))
const mapStatus = (status) => {
  const s = status !== undefined && status !== null ? String(status) : ''
  const m = { '0': '未开始', '1': '进行中', '2': '审核中', '3': '已完成' }
  return m[s] || (s || '未开始')
}
const getDeadlineByPhase = (t) => {
  const y = t?.phase !== undefined && t?.phase !== null ? Number(t.phase) : NaN
  if (Number.isFinite(y) && y > 1900) return `${y}-12-31`
  return ''
}

const targetUserId = computed(() => {
  const t = currentTask.value?._raw
  if (!t) return null
  if (reportForm.targetType === 'principal') {
    return t.principalId !== undefined && t.principalId !== null ? Number(t.principalId) : null
  }
  return t.leaderId !== undefined && t.leaderId !== null ? Number(t.leaderId) : null
})

const targetUserDisplay = computed(() => {
  const id = targetUserId.value
  if (!id) return ''
  const u = userMap.value.get(String(id))
  if (!u) return `用户${id}`
  const deptName = getDeptName(u._raw?.deptId)
  // 如果 getDeptName 返回空字符串，说明没有找到部门信息，使用"未知部门"
  const deptDisplay = deptName || '未知部门'
  return `${u.name}（${deptDisplay}）`
})

const handleTaskChange = async (val) => {
  const task = taskList.value.find(t => t.id === val)
  currentTask.value = task
  if (task) {
    // 自动填充标题
    reportForm.title = `督办提醒：${task.name}`
  }
}

const submitForm = async () => {
  if (!reportFormRef.value || submitting.value || reportDisposed) return
  const version = reportVersion
  const snapshot = { ...reportForm }
  const recipient = targetUserId.value
  submitting.value = true
  
  try {
    const valid = await reportFormRef.value.validate().catch(() => false)
    if (!valid || reportDisposed || version !== reportVersion) return

    const taskId = snapshot.taskId ? Number(snapshot.taskId) : null
    const toUserId = recipient ? Number(recipient) : null

    if (!taskId) {
      ElMessage.warning('请选择关联任务')
      return
    }

    if (!toUserId) {
      ElMessage.warning('未找到督办对象，请先选择任务并确认负责人或归口审核人')
      return
    }

    const myId = getCurrentUserId()
    if (myId && Number(myId) === Number(toUserId)) {
      ElMessage.warning('不能督办自己')
      return
    }

    await sendNotice({
      to_user_id: toUserId,
      title: snapshot.title,
      content: snapshot.content,
      source_id: taskId,
      type: '3',
      trigger_event: 'TASK',
    })

    if (reportDisposed) return
    ElMessage.success('督办信息已发送')
    if (version === reportVersion) resetForm()
  } catch (e) {
    if (!reportDisposed && version === reportVersion) ElMessage.error(e?.message || '提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  if (!reportFormRef.value) return
  reportFormRef.value.resetFields()
  currentTask.value = null
}

/**
 * 加载基础数据：任务/用户
 */
const loadBaseData = async (tasks) => {
  try {
    const list = Array.isArray(tasks) ? tasks : []

    // 只允许选择"当前用户权限下"的三级任务：过滤掉一级/二级
    taskList.value = list
      .filter(t => Number(t?.level) === 3)
      .map(t => ({
        id: String(t.taskId),
        name: t.taskName || `任务${t.taskId}`,
        deadline: getDeadlineByPhase(t),
        status: mapStatus(t.status),
        progress: (() => {
          const cur = Number(t.currentValue ?? t.current_value)
          const target = Number(t.targetValue ?? t.target_value)
          if (Number.isFinite(cur) && Number.isFinite(target) && target > 0) {
            return clampPercent(Math.round((cur / target) * 100))
          }
          return clampPercent(Number(t.progress))
        })(),
        _raw: t,
      }))


    // 先尝试获取用户列表，如果失败则从任务数据中提取
    let users = []
    try {
      users = await getAllUsers()
    } catch (e) { // eslint-disable-line no-unused-vars

      // 如果获取用户列表失败（权限不足），尝试从缓存读取
      try {
        const cached = sessionStorage.getItem('works_users_cache_v1')
        if (cached) {
          users = JSON.parse(cached)
        }
      } catch (e2) { // eslint-disable-line no-unused-vars

        // 忽略缓存读取错误
      }
    }

    // 建立用户列表，兼容字段名
    const userMapTemp = new Map()
    // 从用户列表建立映射
    ;(Array.isArray(users) ? users : []).forEach(u => {
      const uid = u?.userId ?? u?.user_id
      if (uid !== undefined && uid !== null) {
        const name = u?.nickName ?? u?.nick_name ?? u?.userName ?? u?.user_name
        if (name) {
          userMapTemp.set(String(uid), {
            id: String(uid),
            name: name,
            _raw: u,
          })
        }
      }
    })
    
    // 从任务数据中提取用户信息，补充到用户映射中
    list.forEach(task => {
      if (task) {
        // 任务负责人
        const leaderId = task?.leaderId ?? task?.leader_id
        const leaderName = task?.leaderName ?? task?.leader_name
        if (leaderId && leaderName && !userMapTemp.has(String(leaderId))) {
          userMapTemp.set(String(leaderId), {
            id: String(leaderId),
            name: leaderName,
            _raw: { userId: leaderId, nickName: leaderName },
          })
        }
        // 归口负责人
        const principalId = task?.principalId ?? task?.principal_id
        const principalName = task?.principalName ?? task?.principal_name
        if (principalId && principalName && !userMapTemp.has(String(principalId))) {
          userMapTemp.set(String(principalId), {
            id: String(principalId),
            name: principalName,
            _raw: { userId: principalId, nickName: principalName },
          })
        }
        // 审核人
        const auditorId = task?.auditorId ?? task?.auditor_id ?? task?.auditId ?? task?.audit_id
        const auditorName = task?.auditorName ?? task?.auditName
        if (auditorId && auditorName && auditorName !== '无' && !userMapTemp.has(String(auditorId))) {
          userMapTemp.set(String(auditorId), {
            id: String(auditorId),
            name: auditorName,
            _raw: { userId: auditorId, nickName: auditorName },
          })
        }
      }
    })

    userList.value = Array.from(userMapTemp.values())

    // 收集所有用户的部门ID并建立映射
    const allDeptIds = new Set()
    userList.value.forEach(u => {
      if (u?._raw?.deptId !== undefined && u?._raw?.deptId !== null) {
        allDeptIds.add(u._raw.deptId)
      }
    })

    // 批量获取部门信息并建立映射
    const newDeptMap = new Map()
    
    // 先从任务数据中提取部门信息
    list.forEach(task => {
      const deptId = task?.deptId ?? task?.dept_id
      const deptName = task?.deptName ?? task?.dept_name
      if (deptId && deptName) {
        newDeptMap.set(String(deptId), deptName)
      }
    })
    
    // 批量获取缺失的部门信息
    const missingDeptIds = Array.from(allDeptIds).filter(deptId => !newDeptMap.has(String(deptId)))
    if (missingDeptIds.length > 0) {
      await Promise.all(missingDeptIds.map(deptId =>
        getDeptById(deptId)
          .then(dept => {
            if (dept?.deptName) {
              newDeptMap.set(String(deptId), dept.deptName)
            } else {
              // 如果获取失败，使用兜底值
              newDeptMap.set(String(deptId), `部门${deptId}`)
            }
          })
          .catch(() => {
            // 如果获取失败，使用兜底值
            newDeptMap.set(String(deptId), `部门${deptId}`)
          })
      ))
    }
    deptMap.value = newDeptMap
  } catch (e) {
    ElMessage.error(e?.message || '加载任务/用户失败')
    taskList.value = []
    userList.value = []
}
}


</script>

<style scoped>
.report-container {
  height: 100%;
}

.box-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

:deep(.el-card__body) {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
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

/* 表单区域 */
.form-wrapper {
  max-width: 800px;
  margin: 0 auto;
  padding-top: 20px;
}

.task-info-box {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 22px;
  border: 1px solid #ebeef5;
}

.ml-2 {
  margin-left: 8px;
}

.mb-4 {
  margin-bottom: 16px;
}
</style>
