<template>
  <div class="notice-container">
    <el-card class="box-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="title">消息中心</span>
            <el-badge :value="unreadCount" :max="99" class="unread-badge" type="danger" v-if="unreadCount > 0" />
          </div>
          <div class="header-right">
            <el-button plain size="small" @click="markAllRead" :disabled="unreadCount === 0">
              <el-icon class="mr-1">
                <Check />
              </el-icon>全部已读
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 标签页筛选 -->
      <QueryFilterBar ref="queryBar" response-mode="page" domain="notices" title="消息" @result="receiveNotices" @state="queryState = $event" />
      <el-alert v-if="listError" :title="listError" type="error" :closable="false"><el-button link type="primary" @click="loadNotices">重新加载消息</el-button></el-alert>

      <!-- 消息列表 -->
      <div v-loading="loading || queryState.loading" class="notice-list">
        <template v-if="filterList.length > 0">
          <div v-for="item in pagedList" :key="item.notice_id" class="notice-item"
            :class="{ 'is-read': item.is_read === '1' }" @click="viewDetail(item)">
            <div class="notice-icon">
              <el-icon :class="getIconClass(item.type)" :size="20">
                <component :is="getIconComponent(item.type)" />
              </el-icon>
            </div>
            <div class="notice-content">
              <div class="notice-title-row">
                <span class="notice-title">{{ item.title }}</span>
                <el-tag size="small" :type="getTypeTag(item.type)" effect="plain" class="type-tag">
                  {{ getTypeLabel(item.type) }}
                </el-tag>
              </div>
              <p class="notice-desc">{{ item.content }}</p>
              <div class="notice-time">{{ item.create_time }}</div>
            </div>
            <div class="notice-action" v-if="item.is_read === '0'">
              <div class="red-dot"></div>
            </div>
          </div>
        </template>
        <p v-else-if="loading || queryState.loading || !noticeInitialized || !queryState.initialized" role="status">{{ listError || queryState.error ? '消息加载失败，请重试' : '正在加载消息…' }}</p>
        <p v-else-if="listError || queryState.error" role="status">消息加载失败，请重试</p>
        <el-empty v-else description="暂无消息" :image-size="100"></el-empty>
      </div>

      <!-- 分页 -->
      <div class="pagination-container" v-if="filterList.length > 0">
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="pageSize"
          v-model:current-page="currentPage" @current-change="changePage" />
      </div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="消息详情" width="500px" destroy-on-close>
      <div v-if="currentNotice" class="detail-wrapper">
        <h3 class="detail-title">{{ currentNotice.title }}</h3>
        <div class="detail-meta">
          <span class="time">{{ currentNotice.create_time }}</span>
          <span class="sender">发送人: {{ getSenderName(currentNotice) }}</span>
        </div>
        <div class="detail-body">
          {{ currentNotice.content }}
        </div>
        <div class="detail-footer" v-if="currentNotice.source_id && !accessState.isLeader">
          <el-button type="primary" link @click="handleJump(currentNotice)">
            前往处理业务 <el-icon>
              <ArrowRight />
            </el-icon>
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import QueryFilterBar from './QueryFilterBar.vue'
const queryBar=ref(null)
import { accessState } from '@/utils/accessState'
import { ref, computed, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
// 修正：将 chatLineRound 改为 ChatLineRound (PascalCase)
import { Check, ArrowRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getNotices, setNoticeRead } from '@/api/notice'

// --- 向父组件（如 Home 布局）上报未读数量 ---
const emit = defineEmits(['update-unread'])
const router = useRouter()

// --- 状态定义 ---
const loading = ref(false)
const listError = ref(''), noticeInitialized = ref(false), senderLoading = ref(false)
const queryState = ref({ loading: true, initialized: false, error: '' })
const detailVisible = ref(false)
const currentNotice = ref(null)
const pageSize = 10
const currentPage = ref(1)

// --- 通知列表数据 ---
const rawList = ref([])
const senderNames = ref(new Map())
const getSenderName = (notice) => {
  const id = notice?.from_user_id
  if (id === null || id === undefined || id === '') return '未知发送人'
  return senderNames.value.get(String(id)) || (senderLoading.value ? '发送人信息加载中…' : `用户${id}`)
}

/**
 * 格式化后端数据为前端需要的格式
 */
const formatNotice = (notice) => {
  // 确保 is_read 是字符串类型
  const isRead = notice.isRead !== undefined ? String(notice.isRead) : '0'
  
  // 格式化时间
  let formattedTime = ''
  if (notice.createTime) {
    try {
      formattedTime = new Date(notice.createTime).toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      }).replace(/\//g, '-')
    } catch (e) { // eslint-disable-line no-unused-vars
       
      formattedTime = String(notice.createTime)
    }
  }
  
  return {
    notice_id: notice.noticeId || notice.notice_id,
    from_user_id: notice.fromUserId ?? notice.from_user_id,
    title: notice.title || '',
    content: notice.content || '',
    type: notice.type || '1',
    trigger_event: notice.triggerEvent || notice.trigger_event || '',
    source_type: notice.sourceType || notice.source_type || '',
    source_id: notice.sourceId || notice.source_id ? String(notice.sourceId || notice.source_id) : null,
    is_read: isRead,
    create_time: formattedTime
  }
}

/**
 * 加载通知数据
 */
const noticeToken = localStorage.getItem('token')
let noticeDisposed = false
const noticeAlive = () => !noticeDisposed && noticeToken === localStorage.getItem('token')
onUnmounted(() => { noticeDisposed = true })
const unreadCount = ref(0), total = ref(0)
let loadedNoticePage=1
const receiveNotices = result => {
  rawList.value = (result.items || []).map(formatNotice)
  senderNames.value = new Map((result.items || []).filter(n => n.senderName).map(n => [String(n.fromUserId),n.senderName]))
  currentPage.value = Number(result.page) || 1
  loadedNoticePage=currentPage.value
  total.value = Number(result.total) || 0
  unreadCount.value = Number(result.summary?.unreadCount) || 0
  noticeInitialized.value = true
}
const loadNotices = () => queryBar.value?.refresh()
const changePage = page => {if(page!==loadedNoticePage)return queryBar.value?.setPage(page)}
watch(()=>queryState.value.responseCode,code=>{if([401,403,428].includes(Number(code))){rawList.value=[];senderNames.value=new Map();total.value=0;unreadCount.value=0;noticeInitialized.value=false}})

// 监听未读数变化，实时通知父组件更新顶部铃铛的数字
watch(
  [unreadCount, noticeInitialized],
  ([val, ready]) => {
    if (ready) emit('update-unread', val)
  },
  { immediate: true }
)

const filterList = computed(() => rawList.value)
const pagedList = computed(() => rawList.value)

const markAllRead = async () => {
  loading.value = true
  try {
    // 批量设置所有未读通知为已读
    const unreadNotices = (await getNotices()).filter(item => String(item.isRead) === '0' && String(item.sourceType) !== '5').map(formatNotice)
    if (!unreadNotices.length) { ElMessage.info('平台通知请打开正文后单独确认已阅'); return }
    await Promise.all(unreadNotices.map(item => setNoticeRead(item.notice_id)))
    if (!noticeAlive()) return
    
    // 更新本地状态
    const readIds = new Set(unreadNotices.map(item => String(item.notice_id)))
    rawList.value.forEach(item => {
      if (item.is_read === '0' && readIds.has(String(item.notice_id))) {
        item.is_read = '1'
      }
    })
    void loadNotices()
    ElMessage.success('业务消息已标记为已读，平台通知请单独阅读')
  } catch (error) {
    if (noticeAlive()) ElMessage.error(error.message || '操作失败，请稍后重试')
  } finally {
    if (noticeAlive()) loading.value = false
  }
}

const viewDetail = async (item) => {
  if (item.source_type === '5' && item.source_id) { router.push(`/home/platform-notices/${item.source_id}`); return }
  currentNotice.value = item
  detailVisible.value = true
  // 如果是未读，点击详情后自动标记为已读
  if (item.is_read === '0') {
    try {
      await setNoticeRead(item.notice_id)
      if (noticeAlive()) {
        item.is_read = '1'
        void loadNotices()
      }
    } catch (error) { // eslint-disable-line no-unused-vars
       
      // ignore
    }
  }
}

const handleJump = (item) => {
  detailVisible.value = false
  if (item?.source_id) {
    const sourceType = String(item.source_type || '').toLowerCase()
    if (sourceType === '4') {
      const pending = item.trigger_event === 'CORE_TODO'
      router.push(pending
        ? { path: '/home/audit', query: { type: 'core', subId: String(item.source_id) } }
        : { path: '/home/core-indicators', query: { reportId: String(item.source_id) } })
      return
    }
    const explicitType = { '0': 'task', '1': 'task', task: 'task', '2': 'performance', performance: 'performance', '3': 'achievement', achievement: 'achievement' }[sourceType]
    if (explicitType === 'task') {
      const taskAudit = String(item.trigger_event || '').includes('任务审核') || String(item.title || '').includes('任务审核')
      router.push({ path: taskAudit ? '/home/audit' : '/home/works', query: { taskId: String(item.source_id) } })
      return
    }
    if (explicitType) {
      router.push({ path: '/home/audit', query: { type: explicitType, subId: String(item.source_id) } })
      return
    }
    const isPerformanceAudit =
      sourceType === 'performance' ||
      String(item.title || '').includes('绩效') ||
      String(item.trigger_event || '').includes('绩效') ||
      String(item.title || '').toLowerCase().includes('performance') ||
      String(item.trigger_event || '').toLowerCase().includes('performance')

    if (isPerformanceAudit) {
      router.push({
        path: '/home/audit',
        query: { type: 'performance', subId: String(item.source_id) }
      })
      return
    }

    const isAchievementAudit =
      sourceType === 'achievement' ||
      sourceType === '3' ||
      String(item.title || '').includes('成果') ||
      String(item.trigger_event || '').includes('成果')

    if (isAchievementAudit) {
      router.push({
        path: '/home/audit',
        query: { type: 'achievement', subId: String(item.source_id) }
      })
      return
    }

    const isTaskAudit =
      String(item.title || '').includes('任务审核') ||
      String(item.trigger_event || '').includes('任务审核')

    // “任务审核”消息：跳转审核中心并自动打开对应任务的审批界面
    if (isTaskAudit) {
      router.push({
        path: '/home/audit',
        query: { taskId: String(item.source_id) }
      })
      return
    }

    // 其他消息：保持原逻辑（跳工作中心并定位任务）
    router.push({
      path: '/home/works',
      query: { taskId: String(item.source_id) }
    })
  } else {
    ElMessage.info('暂无关联业务可跳转')
  }
}

// --- 样式/图标辅助 ---
const getTypeTag = (type) => {
  const map = { '1': 'primary', '2': 'danger', '3': 'warning' }
  return map[type] || 'info'
}

const getTypeLabel = (type) => {
  const map = { '1': '系统通知', '2': '预警/驳回', '3': '待办事项' }
  return map[type] || '通知'
}

const getIconComponent = (type) => {
  // 修正：将 chatLineRound 改为 ChatLineRound
  const map = { '1': 'Bell', '2': 'Warning', '3': 'ChatLineRound' }
  return map[type] || 'Bell'
}

const getIconClass = (type) => {
  const map = { '1': 'icon-primary', '2': 'icon-danger', '3': 'icon-warning' }
  return map[type] || ''
}


</script>

<style scoped>
.notice-container {
  padding: 0;
  height: 100%;
}

.box-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* 强制让 el-card__body 占满剩余高度 */
:deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0;
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
  gap: 8px;
}

.title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.notice-tabs {
  padding: 0 20px;
  background-color: #fff;
  border-bottom: 1px solid #f0f2f5;
}

.notice-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 20px;
}

.notice-item {
  display: flex;
  padding: 16px 0;
  border-bottom: 1px solid #f0f2f5;
  cursor: pointer;
  transition: background-color 0.2s;
}

.notice-item:hover {
  background-color: #f9fafc;
}

.notice-item.is-read .notice-title {
  color: #909399;
  font-weight: normal;
}

.notice-item.is-read .notice-desc {
  color: #c0c4cc;
}

.notice-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #f2f6fc;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  flex-shrink: 0;
}

.icon-primary {
  color: #409eff;
  background-color: #ecf5ff;
}

.icon-danger {
  color: #f56c6c;
  background-color: #fef0f0;
}

.icon-warning {
  color: #e6a23c;
  background-color: #fdf6ec;
}

.notice-content {
  flex: 1;
  min-width: 0;
  /* 防止文本溢出 */
}

.notice-title-row {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
  gap: 8px;
}

.notice-title {
  font-size: 15px;
  color: #303133;
  font-weight: 500;
}

.notice-desc {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
  margin: 0 0 4px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.notice-time {
  font-size: 12px;
  color: #909399;
}

.notice-action {
  width: 30px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding-top: 10px;
}

.red-dot {
  width: 8px;
  height: 8px;
  background-color: #f56c6c;
  border-radius: 50%;
}

.pagination-container {
  padding: 16px 20px;
  border-top: 1px solid #f0f2f5;
  display: flex;
  justify-content: flex-end;
}

/* 详情弹窗样式 */
.detail-wrapper {
  padding: 0 10px;
}

.detail-title {
  margin: 0 0 12px 0;
  font-size: 18px;
  color: #303133;
}

.detail-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 16px;
}

.detail-body {
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
  min-height: 100px;
}

.detail-footer {
  margin-top: 20px;
  text-align: right;
  border-top: 1px solid #ebeef5;
  padding-top: 12px;
}

.mr-1 {
  margin-right: 4px;
}
</style>
