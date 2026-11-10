<template>
  <div class="audit-container">
    <el-card class="box-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="title">审核中心</span>
            <el-tag type="danger" effect="dark" class="ml-2" v-if="todoCount > 0">{{ todoCount }} 待我处理</el-tag>
          </div>
          <div class="header-right">
            <el-radio-group v-model="activeTab" size="small" @change="handleTabChange">
              <el-radio-button label="todo">待我处理</el-radio-button>
              <el-radio-button v-if="isAdmin" label="all">全部待审</el-radio-button>
              <el-radio-button label="done">审批记录</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>

      <QueryFilterBar :key="activeTab" ref="auditQuery" response-mode="page" :domain="`audits-${activeTab}`" endpoint="/list-filters/audits" title="审核" :context="{tab:activeTab}" @result="receiveAuditQuery" @state="auditQueryState = $event"><template #actions><el-button :icon="Refresh" circle @click="refreshData" /></template></QueryFilterBar>
      <p v-if="activeTab === 'all'" class="all-pending-note">全校待审记录供查看和代审，提醒数量仅统计待我处理。</p>
      <!-- 审批记录：不再展示“后端能力提示”（避免误导/噪音） -->

      <!-- 列表区域 -->
      <el-table :data="tableData" style="width: 100%" v-loading="loading || routeLoading || auditQueryState.loading" :empty-text="loading || routeLoading || auditQueryState.loading ? '正在加载审核记录…' : auditQueryState.error ? '审核记录加载失败，请重试' : '暂无匹配审核记录'"
        :header-cell-style="{ background: '#f8f9fa', color: '#606266' }">
        <el-table-column label="申请单号" width="100" align="center">
          <template #default="scope">
            {{ scope.row.sub_id }}
          </template>
        </el-table-column>
        <el-table-column prop="task_name" label="关联任务/项目" min-width="250" show-overflow-tooltip>
          <template #default="scope">
              <span class="task-name">{{ scope.row.task_name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100" align="center">
           <template #default="scope">
             <el-tag :type="getTypeInfo(scope.row.type).tag">{{ getTypeInfo(scope.row.type).label }}</el-tag>
           </template>
        </el-table-column>
        <el-table-column label="提交信息" width="200">
          <template #default="scope">
            <div class="submit-info">
              <span class="user">{{ scope.row.reporter_name }}</span>
              <span class="dept text-gray">{{ scope.row.dept_name }}</span>
              <span class="time text-gray">{{ scope.row.submit_time }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="reported_value" label="填报值／计入汇总值" width="145" align="center">
          <template #default="scope">
            <span class="highlight-value">{{ displayReportedValue(scope.row.reported_value) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="完成情况" min-width="200" show-overflow-tooltip>
          <template #default="scope">
            <span class="comment-text">{{ scope.row.comment || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="flow_status" label="当前状态" width="120" align="center">
           <template #default="scope">
              <span :class="getStatusClass(scope.row.flow_status)">
                {{ scope.row.status_desc }}
              </span>
           </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="scope">
            <el-button v-if="activeTab !== 'done'" type="primary" link @click="openAuditDialog(scope.row)">
              <el-icon class="mr-1">
                <Stamp />
              </el-icon>{{ activeTab === 'all' && !scope.row._raw.assignedToMe ? '代审' : '审批' }}
            </el-button>
            <el-button v-else type="info" link @click="openAuditDialog(scope.row, true)">
              <el-icon class="mr-1">
                <View />
              </el-icon>查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-alert v-if="refreshAfterAuditFailed" title="审核已成功，列表刷新失败，请刷新列表后继续" type="warning" :closable="false" show-icon>
        <el-button link type="primary" @click="refreshData" :loading="loading">刷新列表</el-button>
      </el-alert>
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination v-model:current-page="currentPage" background layout="total, prev, pager, next" :total="total" :page-size="pageSize" @current-change="handleAuditPage">
          <template #total="{ total }">
            <span>总计 {{ total }} 条</span>
          </template>
        </el-pagination>
      </div>
    </el-card>

    <!-- 审批弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isViewMode ? '详情查看' : '业务审批'" width="600px" destroy-on-close>
      <div v-if="currentItem" class="dialog-content">
        <!-- 基础信息展示区 -->
        <div class="info-section">
          <div class="section-title">申请详情</div>
          <el-alert v-if="currentItem.type === 'achievement' && currentItem._raw.historicalDetailsAvailable === false && currentItem.flow_status !== 10"
            :title="currentItem._raw.historicalCommentAvailable ? '该旧审批单正文取自当时提交日志；名称、其他字段和附件未完整留存' : '该旧审批单未保存提交时的成果正文和附件；当前成果可能已修改，历史原文无法准确还原'" type="warning" :closable="false" />
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="申请单号">{{ currentItem.sub_id }}</el-descriptions-item>
            <el-descriptions-item label="业务类型">{{ getTypeInfo(currentItem.type).label }}</el-descriptions-item>
            <el-descriptions-item label="提交人员">{{ currentItem.reporter_name }}</el-descriptions-item>
            <el-descriptions-item label="所属部门">{{ currentItem.dept_name }}</el-descriptions-item>
            <el-descriptions-item :label="currentItem.type === 'task' ? '计入汇总值' : '填报数值/金额'" :span="2">
              <span class="highlight-value">{{ displayReportedValue(currentItem.reported_value) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="完成情况" :span="2" v-if="currentItem.comment">
              <span class="comment-text">{{ currentItem.comment || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item v-if="currentItem.type === 'core'" label="项目期满目标" :span="2">{{ displayReportedValue(currentItem._raw.targetValue) }}</el-descriptions-item>
            <el-descriptions-item v-if="currentItem.type === 'core'" label="上年年末累计值" :span="2">{{ displayReportedValue(currentItem._raw.previousYearValue) }}</el-descriptions-item>
            <el-descriptions-item v-if="currentItem.type !== 'core'" label="佐证材料" :span="2">
              <div v-if="currentItem.file_id" class="file-link" @click="previewFile(currentItem)">
                <el-icon>
                  <Document />
                </el-icon> {{ currentItem.file_name }}
              </div><span v-else>—</span>
            </el-descriptions-item>
          </el-descriptions>
          <el-alert v-if="currentItem.type === 'task' && currentItem._raw.historicalDetailsAvailable === false"
            title="当时四级原始值未留存；此处仅显示审批单计入汇总值" type="info" :closable="false" />
          <el-table v-if="taskLevel4Rows(currentItem).length" :data="taskLevel4Rows(currentItem)"
            size="small" border class="level4-audit-table">
            <el-table-column prop="task_name" label="四级目标" min-width="190" show-overflow-tooltip />
            <el-table-column label="填报值" width="90" align="center">
              <template #default="scope">{{ displayReportedValue(scope.row.reported_value) }}</template>
            </el-table-column>
            <el-table-column label="目标值" width="90" align="center">
              <template #default="scope">{{ displayReportedValue(scope.row.target_value) }}</template>
            </el-table-column>
            <el-table-column label="实际完成率" width="110" align="center">
              <template #default="scope">{{ displayRate(scope.row.actual_progress) }}</template>
            </el-table-column>
            <el-table-column label="计入汇总" width="100" align="center">
              <template #default="scope">{{ displayRate(scope.row.counted_progress) }}</template>
            </el-table-column>
          </el-table>
        </div>

        <div v-if="currentItem.type === 'core' && currentItem._raw.logs?.length" class="audit-section mt-4">
          <div class="section-title">填报与审核记录</div>
          <el-table :data="currentItem._raw.logs" border>
            <el-table-column prop="action" label="操作" width="80" />
            <el-table-column prop="operatorName" label="操作人" width="120" />
            <el-table-column prop="comment" label="意见" />
            <el-table-column prop="createTime" label="时间" min-width="160" />
          </el-table>
        </div>
        <!-- 审批表单区 -->
        <div class="audit-section mt-4" v-if="!isViewMode">
          <div class="section-title">审批处理</div>
          <el-form :model="auditForm" label-position="top">
            <el-form-item label="审核结果">
              <el-radio-group v-model="auditForm.result">
                <el-radio :label="1" border class="radio-success">通过</el-radio>
                <el-radio :label="0" border class="radio-danger">驳回</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="审批意见">
              <el-input v-model="auditForm.comment" type="textarea" :rows="3" placeholder="请输入审批意见（必填）..."></el-input>
            </el-form-item>
          </el-form>
        </div>

        <!-- 审批历史记录（仅在查看模式显示） -->
        <div class="history-section mt-4" v-else>
          <div class="section-title">处理结果</div>
          <el-alert :title="currentItem.flow_status > 0 ? '已通过' : '已驳回'"
            :type="currentItem.flow_status > 0 ? 'success' : 'error'" :description="currentItem.status_desc || '已处理'"
            show-icon :closable="false" />
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button v-if="!isViewMode" type="primary" @click="submitAudit(false)" :loading="submitting" :disabled="auditCommitted">
            确认提交
          </el-button>
          <el-button v-if="!isViewMode && activeTab !== 'done'" type="primary" @click="submitAudit(true)" :loading="submitting" :disabled="auditCommitted">
            提交并审核下一条
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 文件预览弹窗 -->
    <el-dialog v-model="filePreviewVisible" title="文件预览" width="70%" destroy-on-close @closed="cleanupPreviewUrl">
      <div v-loading="filePreviewLoading" class="file-preview-wrapper">
        <template v-if="!filePreviewLoading">
          <div v-if="filePreviewType === 'pdf'" class="pdf-viewer">
            <div class="pdf-viewer-header">
              <span class="file-name-display">{{ filePreviewName }}</span>
              <el-button type="primary" :icon="Download" @click="downloadPreviewBlob" :disabled="!filePreviewBlob">
                下载文件
              </el-button>
            </div>
            <iframe :src="filePreviewUrl" width="100%" height="650px" style="border:none;" allow="fullscreen"></iframe>
          </div>
          <div v-else class="other-viewer">
            <div class="hint">该文件暂不支持在线预览，请下载后查看。</div>
            <el-button type="primary" :icon="Download" @click="downloadPreviewBlob" :disabled="!filePreviewBlob">
              下载文件
            </el-button>
          </div>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { adminTestFetch } from '@/utils/adminTest';

import { computed, ref, reactive, onMounted, onUnmounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import QueryFilterBar from './QueryFilterBar.vue';
import { Refresh, Stamp, View, Document, Download } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { get as queryGet } from '@/utils/request';
import { auditTask } from '@/api/audit';
import { auditPerformance } from '@/api/performance';
import { auditAchievement } from '@/api/achievement';
import { auditCoreReport, getCoreReport } from '@/api/coreIndicators';
import { accessState } from '@/utils/accessState';

// 进入审核中心不输出控制台日志（减少噪音）

// --- 状态管理 ---
const emit = defineEmits(['audit-completed']);
const activeTab = ref('todo');
const isAdmin = computed(() => accessState.isAdmin);
watch(isAdmin, value => { if (!value && activeTab.value === 'all') { activeTab.value = 'todo'; handleTabChange(); } });
const searchText = ref('');
const filterType = ref('');
const loading = ref(false);
const routeLoading = ref(false);
const dialogVisible = ref(false);
const isViewMode = ref(false);
const submitting = ref(false);
const currentItem = ref(null);
const auditCommitted = ref(false);
const refreshAfterAuditFailed = ref(false);
let filterVersion = 0;
const auditQuery=ref(null);
const auditQueryState=ref({loading:true,initialized:false,error:''});
watch(()=>auditQueryState.value.responseCode,code=>{if([401,403,428].includes(Number(code))){allAudits.value=[];tableData.value=[];total.value=0;todoCount.value=0;dialogVisible.value=false;}});
let appliedAuditSignature='',loadedAuditPage=1;
const receiveAuditQuery=result=>{
  const signature=JSON.stringify(Object.entries(result.query||{}).sort(([a],[b])=>a.localeCompare(b)));
  if(signature!==appliedAuditSignature){filterVersion++;appliedAuditSignature=signature;}
  allAudits.value=mapAuditItems(result.items||[]);tableData.value=allAudits.value;
  total.value=result.total;loadedAuditPage=result.page;currentPage.value=result.page;
  todoCount.value=Number(result.summary?.todoCount)||0;
};
const auditKey = item => `${item.type}:${item.sub_id}`;
watch([activeTab, searchText, filterType], () => { filterVersion++; }, { flush: 'sync' });
let dialogVersion = 0;
watch(dialogVisible, visible => {
  if (!visible) dialogVersion++;
}, { flush: 'sync' });

const route = useRoute();
const router = useRouter();

// --- 文件预览状态 ---
const filePreviewVisible = ref(false);
const filePreviewLoading = ref(false);
const filePreviewType = ref('other'); // pdf | other
const filePreviewUrl = ref('');
const filePreviewBlob = ref(null);
const filePreviewName = ref('');

const auditForm = reactive({
  result: 1,
  comment: ''
});

// --- 用户映射（用于"提交人员"显示姓名而不是ID） ---
const userMap = ref(new Map()); // userId -> nickName/userName
const getUserName = (userId) => {
  if (userId === undefined || userId === null) return '';
  const id = String(userId);
  return userMap.value.get(id) || `用户${userId}`;
};

// --- 部门映射（用于"所属部门"显示部门名而不是ID） ---
const deptMap = ref(new Map()); // deptId -> deptName
const getDeptName = (deptId) => {
  if (deptId === undefined || deptId === null) return '';
  const id = String(deptId);
  return deptMap.value.get(id) || '';
};

// --- 文件映射（用于显示文件名而不是文件ID） ---
const fileMap = ref(new Map()); // fileId -> fileName
const getFileName = (fileId) => {
  if (fileId === undefined || fileId === null) return '';
  const id = String(fileId);
  let fileName = fileMap.value.get(id) || `文件ID: ${fileId}`;
  // 如果文件名包含URL编码，尝试解码
  if (fileName && fileName.includes('%') && !fileName.startsWith('文件ID:')) {
    try {
      fileName = decodeURIComponent(fileName);
    } catch {
      // 解码失败，使用原始值
    }
  }
  return fileName;
};

// 从下载接口的响应头获取文件名
const fetchFileNameFromDownload = async (fileId) => {
  if (!fileId) return null;
  const id = String(fileId);
  // 如果已经缓存了，直接返回
  if (fileMap.value.has(id)) {
    return fileMap.value.get(id);
  }
  
  try {
    const token = localStorage.getItem('token') || '';
    if (!token) return null;
    
    // 使用 HEAD 请求获取响应头（如果支持），否则使用 GET 但只读取响应头
    // 注意：某些服务器可能不支持 HEAD 请求，如果失败则尝试 GET 请求但只读取响应头
    let resp;
    try {
      resp = await adminTestFetch(`/api/system/download/${fileId}`, {
        method: 'HEAD',
        headers: { Authorization: token }
      });
    } catch {
      // HEAD 请求失败，尝试 GET 请求但只读取响应头
      resp = await adminTestFetch(`/api/system/download/${fileId}`, {
        method: 'GET',
        headers: { Authorization: token }
      });
      // 如果是 GET 请求，需要立即取消，只读取响应头
      if (resp.ok) {
        const cd = resp.headers.get('content-disposition') || '';
        const filename = parseFilenameFromContentDisposition(cd);
        if (filename) {
          fileMap.value.set(id, filename);
          return filename;
        }
      }
      return null;
    }

    if (resp.ok) {
      const cd = resp.headers.get('content-disposition') || '';
      const filename = parseFilenameFromContentDisposition(cd);
      if (filename) {
        fileMap.value.set(id, filename);
        return filename;
      }
    }
  } catch {
    // 忽略错误，返回 null
  }
  return null;
};

// --- 审批单数据 ---
const allAudits = ref([]); // 所有审批单
const tableData = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = 10;

const displayReportedValue = (value) => value === null || value === undefined || value === '' ? '-' : value;
const displayRate = (value) => {
  if (value === null || value === undefined || value === '') return '-';
  const rate = Number(value);
  return Number.isFinite(rate) ? `${Number(rate.toFixed(2))}%` : '-';
};
const taskLevel4Rows = (item) => {
  if (!item || item.type !== 'task') return [];
  const rows = item._raw?.sub_list ?? item._raw?.subList;
  return Array.isArray(rows) ? rows : [];
};

// 不再展示“后端能力提示”，审批记录以现有接口能力尽量完整展示

// 待处理数量
const todoCount = ref(0);
let auditRequestId = 0;
let auditDisposed = false;
const isTodoAudit = (item) => {
  const status = Number(item.flow_status);
  if (item.type === 'achievement') return status === 10;
  if (item.type === 'performance') return status === 10 || status === 20;
  if (item.type === 'core') return status === 10 || status === 20;
  return [10, 20, 30].includes(status);
};

/**
 * 格式化审批单数据
 */
const formatAudit = (audit, task) => {
  // 根据flowStatus判断状态
  const getStatusDesc = (status) => {
    if (status === 40) return '已通过';
    if (status < 0) return '已驳回';
    if (status >= 30) return '待双高办归档';
    if (status >= 20) return '待归口部门审核';
    if (status >= 10) return '待专业群审核';
    return '待处理';
  };

  // 获取提交人姓名：优先从用户映射获取，如果映射中没有，尝试从任务数据获取
  const submitBy = audit?.submitBy ?? audit?.submit_by;
  let reporterName = getUserName(submitBy);
  // 如果 getUserName 返回的是 "用户${userId}"，说明映射中没有，尝试从任务数据获取
  if (reporterName.startsWith('用户') && task) {
    const leaderId = task?.leaderId ?? task?.leader_id;
    const leaderName = task?.leaderName ?? task?.leader_name;
    // 如果提交人是任务负责人，使用任务数据中的 leaderName
    if (leaderId && String(leaderId) === String(submitBy) && leaderName) {
      reporterName = leaderName;
    }
  }

  return {
    sub_id: audit.subId,
    task_id: audit.taskId,
    task_name: task ? task.taskName : `任务ID: ${audit.taskId}`,
    project_name: task ? `项目ID: ${task.projectId}` : '',
    type: 'task', // 后端目前只有任务类型
    reporter_name: reporterName,
    dept_name: (() => {
      const submitDeptId = audit?.submitDeptId ?? audit?.submit_dept_id;
      // 优先从部门映射获取
      let deptName = getDeptName(submitDeptId);
      // 如果返回的是 "部门${deptId}"，尝试从任务数据获取
      if (deptName.startsWith('部门') && task) {
        const taskDeptName = task?.deptName ?? task?.dept_name;
        if (taskDeptName) {
          deptName = taskDeptName;
        }
      }
      return deptName || '未知部门';
    })(),
    submit_time: audit.submitTime ? new Date(audit.submitTime).toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    }).replace(/\//g, '-') : '',
    status_desc: getStatusDesc(audit.flowStatus),
    flow_status: audit.flowStatus,
    reported_value: audit.reportedValue ?? audit.reported_value ?? '',
    // 审核必须显示当前审批单的提交快照；任务上的说明可能是退回前的旧值。
    comment: audit.comment ?? '',
    file_name: getFileName(audit.fileId),
    file_id: audit.fileId,
    data_type: audit.dataType,
    _raw: audit
  };
};

const formatPerformanceAudit = (audit) => {
  const status = Number(audit.flowStatus ?? audit.flow_status);
  const getStatusDesc = (s) => {
    if (s === 30) return '已完结归档';
    if (s === 20) return '待完结归档';
    if (s === 10) return '待专业群审核';
    if (s < 0) return '已退回';
    if (s === 0) return '已撤回';
    return '待处理';
  };

  return {
    sub_id: audit.subId ?? audit.sub_id,
    task_id: audit.perfId ?? audit.perf_id,
    task_name: audit.perfName ?? audit.perf_name ?? `绩效ID: ${audit.perfId ?? audit.perf_id}`,
    project_name: `年度: ${audit.year || '-'}`,
    type: 'performance',
    reporter_name: getUserName(audit.submitBy ?? audit.submit_by),
    dept_name: '',
    submit_time: audit.submitTime ? new Date(audit.submitTime).toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    }).replace(/\//g, '-') : '',
    status_desc: getStatusDesc(status),
    flow_status: status,
    reported_value: audit.actualValue ?? audit.actual_value ?? '',
    comment: audit.comment || '',
    file_name: '',
    file_id: null,
    data_type: '',
    _raw: audit
  };
};

const formatCoreAudit = audit => ({
  sub_id: audit.reportId, task_id: audit.indicatorId, task_name: audit.indicatorName,
  project_name: `${audit.year}年${audit.month}月`, type: 'core', reporter_name: audit.submitName || getUserName(audit.submitBy),
  dept_name: '', submit_time: audit.submitTime || '', status_desc: ({10:'待归口审核',20:'待双高办归档',30:'已归档','-10':'已退回','-20':'已退回','-1':'已撤回'})[audit.flowStatus] || '',
  flow_status: Number(audit.flowStatus), reported_value: audit.actualValue, comment: audit.comment || '', file_name: '', file_id: null, _raw: audit
});

const formatAchievementAudit = (audit) => {
  const status = Number(audit.flowStatus ?? audit.flow_status);
  const getStatusDesc = (s) => {
    if (s === 30) return '已完结归档';
    if (s === 10) return '待管理员归档';
    if (s < 0) return '已退回';
    return '待处理';
  };

  return {
    sub_id: audit.subId ?? audit.sub_id,
    task_id: audit.achId ?? audit.ach_id,
    task_name: audit.achName ?? audit.ach_name ?? `成果ID: ${audit.achId ?? audit.ach_id}`,
    project_name: '成果归档',
    type: 'achievement',
    reporter_name: getUserName(audit.submitBy ?? audit.submit_by),
    dept_name: getDeptName(audit.deptId ?? audit.dept_id) || '',
    submit_time: audit.submitTime ? new Date(audit.submitTime).toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    }).replace(/\//g, '-') : '',
    status_desc: getStatusDesc(status),
    flow_status: status,
    reported_value: audit.level || '',
    comment: audit.historicalDetailsAvailable === false && status !== 10 && !audit.historicalCommentAvailable ? '历史正文未留存' : audit.achievementComment ?? audit.achievement_comment ?? audit.comment ?? '',
    file_name: audit.historicalDetailsAvailable === false && status !== 10 ? '历史附件未留存' : getFileName(audit.fileId ?? audit.file_id),
    file_id: audit.historicalDetailsAvailable === false && status !== 10 ? null : audit.fileId ?? audit.file_id,
    data_type: '',
    _raw: audit
  };
};

/**
 * 加载审批单数据
 */
const mapAuditItems = items => items.map(audit => {
  if(audit._submitName)userMap.value.set(String(audit.submitBy),audit._submitName);
  if(audit._deptName){const id=audit.type==='achievement'?audit.deptId:audit.submitDeptId;if(id!=null)deptMap.value.set(String(id),audit._deptName);}
  if(audit.fileId!=null&&audit.filename)fileMap.value.set(String(audit.fileId),audit.filename);
  if(audit.type==='core')return formatCoreAudit(audit);
  if(audit.type==='performance')return formatPerformanceAudit(audit);
  if(audit.type==='achievement')return formatAchievementAudit(audit);
  return formatAudit(audit,audit._task);
});
const loadAudits = async ({quiet=false}={}) => {
  const requestId=++auditRequestId,tab=activeTab.value,token=localStorage.getItem('token');
  const current=()=>!auditDisposed&&requestId===auditRequestId&&tab===activeTab.value&&token===localStorage.getItem('token');
  loading.value=true;
  try{
    const result=await auditQuery.value?.refresh();if(!current())return null;
    if(!result){if(!quiet)ElMessage.error('审核列表更新失败，请重试');return false;}
    refreshAfterAuditFailed.value=false;return true;
  }finally{if(current())loading.value=false;}
};
const getFilteredAudits=()=>allAudits.value;
const filterTableData=()=>{tableData.value=allAudits.value;};
const handleAuditPage=page=>{if(Number(page)!==loadedAuditPage)void auditQuery.value?.setPage(page);};
// --- 方法 ---
const refreshData = () => {
  loadAudits();
};

const handleTabChange = () => {
  routeLookupVersion += 1;
  routeLoading.value = false;
  currentPage.value = 1;
  dialogVisible.value = false;
  allAudits.value = [];
  tableData.value = [];
  total.value = 0;
  loadAudits();
};

const getTypeInfo = (type) => {
  const map = {
    // 修复：task 类型如果不设置颜色，也需要给一个合法的 type 值，如 primary 或 info
    'task': { label: '任务佐证', tag: 'primary' },
    'fund': { label: '资金审批', tag: 'success' },
    'performance': { label: '绩效填报', tag: 'warning' },
    'achievement': { label: '成果归档', tag: 'success' },
    'core': { label: '核心指标', tag: 'warning' }
  };
  return map[type] || { label: '未知', tag: 'info' };
};

const getStatusClass = (status) => {
  if (status === 40) return 'status-success';
  if (status < 0) return 'status-error';
  return 'status-process';
};

const openAuditDialog = (row, viewOnly = false) => {
  dialogVersion++;
  auditCommitted.value = false;
  currentItem.value = row;
  isViewMode.value = viewOnly;
  auditForm.result = 1;
  auditForm.comment = viewOnly ? '' : '同意';
  dialogVisible.value = true;
  const version = dialogVersion;
  if (row.type === 'core') {
    getCoreReport(row.sub_id).then(detail => {
      if (!auditDisposed && version === dialogVersion && dialogVisible.value) currentItem.value._raw.logs = detail.logs;
    }).catch(() => { /* 审核提交仍由后端校验当前版本，不用历史加载失败覆盖表单。 */ });
  }
  if (row.file_id && !fileMap.value.has(String(row.file_id))) {
    fetchFileNameFromDownload(row.file_id).then(name => {
      if (name && !auditDisposed && version === dialogVersion && dialogVisible.value) currentItem.value.file_name = name;
    });
  }
};

// 监听审批结果变化，自动更新审批意见
watch(() => auditForm.result, (newVal) => {
  if (!isViewMode.value && dialogVisible.value) {
    if (newVal === 0) {
      auditForm.comment = '驳回';
    } else if (newVal === 1) {
      auditForm.comment = '同意';
    }
  }
});

/**
 * 从消息中心跳转过来时：
 * - /home/audit?taskId=xxx
 * - /home/audit?type=performance&subId=xxx
 * - /home/audit?type=achievement&subId=xxx
 * 自动定位并打开对应审批弹窗（若存在待我审批/历史记录）。
 */
let routeLookupVersion = 0;
const tryOpenAuditFromRoute = async () => {
  const version=++routeLookupVersion;
  const context={type:String(route.query.type||''),subId:String(route.query.subId||''),taskId:String(route.query.taskId||'')};
  if(!context.taskId&&!context.subId)return;
  const token=localStorage.getItem('token');
  const current=()=>!auditDisposed&&version===routeLookupVersion&&token===localStorage.getItem('token');
  routeLoading.value=true;
  try{
    const result=await queryGet('/list-filters/audits/locate',Object.fromEntries(Object.entries(context).filter(([,v])=>v)));
    if(!current())return;
    openAuditDialog(mapAuditItems([result.item])[0],result.isViewMode);
    if(result.isViewMode)ElMessage.info('该业务当前为只读，已打开处理结果');
  }catch(error){if(current())ElMessage.error(error.message||'对应业务加载失败，请重试');}
  finally{
    if(current()){
      routeLoading.value=false;const next={...route.query};delete next.type;delete next.subId;delete next.taskId;
      await router.replace({path:route.path,query:next});
    }
  }
};
const cleanupPreviewUrl = () => {
  if (filePreviewUrl.value) {
    try { URL.revokeObjectURL(filePreviewUrl.value); } catch {
      // 忽略撤销错误
    }
  }
  filePreviewUrl.value = '';
  filePreviewBlob.value = null;
  filePreviewType.value = 'other';
  filePreviewName.value = '';
};

const parseFilenameFromContentDisposition = (cd) => {
  if (!cd) return '';

  // RFC5987: filename*=UTF-8''xxx 或 filename*=charset''xxx
  const mStar = cd.match(/filename\*\s*=\s*([^']*)''([^;]+)/i);
  if (mStar && mStar[2]) {
    try {
      return decodeURIComponent(mStar[2]);
    } catch {
      return mStar[2];
    }
  }

  // 普通格式: filename="xxx" 或 filename=xxx
  const m = cd.match(/filename\s*=\s*"([^";]+)"?/i);
  if (m && m[1]) {
    let filename = m[1];
    // 检查是否是URL编码的字符串（包含%符号）
    if (filename.includes('%')) {
      try {
        // 尝试解码URL编码的文件名
        filename = decodeURIComponent(filename);
      } catch {
        // 如果解码失败，可能是部分编码，尝试其他方式
        try {
          // 处理可能的双重编码情况
          filename = decodeURIComponent(decodeURIComponent(filename));
        } catch {
          // 解码失败，返回原始值
        }
      }
    }
    return filename;
  }

  return '';
};

const getExtFromName = (name) => {
  const n = String(name || '');
  const idx = n.lastIndexOf('.');
  if (idx === -1) return '';
  return n.slice(idx + 1).toLowerCase();
};

const downloadPreviewBlob = () => {
  const blob = filePreviewBlob.value;
  if (!blob) return;
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  // 确保文件名正确解码，避免下载时出现URL编码
  let downloadName = filePreviewName.value || `file_${Date.now()}`;
  // 如果文件名包含URL编码，确保已解码
  if (downloadName.includes('%')) {
    try {
      downloadName = decodeURIComponent(downloadName);
    } catch {
      // 解码失败，使用原始值
    }
  }
  a.download = downloadName;
  document.body.appendChild(a);
  a.click();
  a.remove();
  setTimeout(() => {
    try { URL.revokeObjectURL(url); } catch {
      // 忽略撤销错误
    }
  }, 500);
};

const isPdfBlob = async (blob) => {
  try {
    const buf = await blob.slice(0, 5).arrayBuffer();
    const bytes = new Uint8Array(buf);
    // %PDF-
    return bytes[0] === 0x25 && bytes[1] === 0x50 && bytes[2] === 0x44 && bytes[3] === 0x46 && bytes[4] === 0x2D;
  } catch {
    return false;
  }
};

const previewFile = async (row) => {
  const fileId = row?.file_id;
  if (!fileId) {
    ElMessage.warning('未找到文件ID，无法预览');
    return;
  }
  const token = localStorage.getItem('token') || '';
  if (!token) {
    ElMessage.error('未登录或登录已过期，请先登录');
    return;
  }

  cleanupPreviewUrl();
  filePreviewVisible.value = true;
  filePreviewLoading.value = true;
  try {
    const resp = await adminTestFetch(`/api/system/download/${fileId}`, {
      method: 'GET',
      headers: { Authorization: token }
    });

    if (resp.status === 401) {
      try { localStorage.removeItem('token'); } catch {  
        // 忽略清理错误
      }
      try {
        const full = `${window.location.pathname || ''}${window.location.search || ''}${window.location.hash || ''}`;
        window.location.replace(`/login?redirect=${encodeURIComponent(full)}`);
      } catch {  
        // 忽略重定向错误
      }
      return;
    }

    if (!resp.ok) {
      // 尝试解析后端ErrorVO
      let err;
      try { err = await resp.json(); } catch {  
        // 忽略JSON解析错误
      }
      throw new Error(err?.message || `文件获取失败(${resp.status})`);
    }

    const cd = resp.headers.get('content-disposition') || '';
    const filename = parseFilenameFromContentDisposition(cd);
    filePreviewName.value = filename || `file_${fileId}`;
    
    // 更新文件映射缓存
    if (filename) {
      fileMap.value.set(String(fileId), filename);
      // 如果当前项在表格中，更新其文件名显示
      const currentRow = tableData.value.find(row => String(row.file_id) === String(fileId));
      if (currentRow) {
        currentRow.file_name = filename;
      }
    }

    const blob = await resp.blob();
    const ext = getExtFromName(filePreviewName.value);
    const isPdf = ext === 'pdf' || (await isPdfBlob(blob));

    if (isPdf) {
      filePreviewType.value = 'pdf';
      const pdfBlob = blob.type === 'application/pdf' ? blob : new Blob([blob], { type: 'application/pdf' });
      filePreviewBlob.value = pdfBlob;
      filePreviewUrl.value = URL.createObjectURL(pdfBlob);
    } else {
      filePreviewType.value = 'other';
      filePreviewBlob.value = blob;
    }
  } catch (e) {
    filePreviewVisible.value = false;
    ElMessage.error(e?.message || '预览失败，请稍后重试');
  } finally {
    filePreviewLoading.value = false;
  }
};

const submitAudit = async (next = false) => {
  if (submitting.value || auditCommitted.value || !dialogVisible.value || isViewMode.value || !currentItem.value) return;
  const version = dialogVersion;
  const filters = filterVersion;
  const token = localStorage.getItem('token');
  const item = { ...currentItem.value };
  const order = getFilteredAudits().map(auditKey);
  const position = order.indexOf(auditKey(item));
  const approvedPage=currentPage.value;
  const candidates = position < 0 ? [] : order.slice(position + 1);
  const result = auditForm.result;
  const comment = auditForm.comment;
  const isCurrent = () => !auditDisposed && token === localStorage.getItem('token') && dialogVisible.value && version === dialogVersion &&
    item.sub_id === currentItem.value?.sub_id && item.type === currentItem.value?.type;
  if (!auditForm.comment) {
    ElMessage.warning('请输入审批意见');
    return;
  }
  
  submitting.value = true;
  try {
    const payload = {
      sub_id: item.sub_id,
      is_pass: result === 1,
      title: comment,
      content: comment
    };
    if (item.type === 'core') {
      await auditCoreReport(item.sub_id, { version: item._raw.version, pass: result === 1, comment });
    } else if (item.type === 'performance') {
      await auditPerformance(payload);
    } else if (item.type === 'achievement') {
      await auditAchievement(payload);
    } else {
      await auditTask(payload);
    }
    
    if (auditDisposed || token !== localStorage.getItem('token')) return;
    emit('audit-completed');
    if (isCurrent()) {
      auditCommitted.value = true;
      if (!next) dialogVisible.value = false;
    }
    allAudits.value = allAudits.value.filter(row => auditKey(row) !== auditKey(item));
    if (item._raw.assignedToMe) todoCount.value = Math.max(0, todoCount.value - 1);
    total.value=Math.max(0,total.value-1);
    filterTableData();
    ElMessage.success(result === 1 ? '审批通过' : '已驳回');
    
    // 重新加载数据
    const refreshed = await loadAudits({ quiet: true });
    if (auditDisposed || token !== localStorage.getItem('token')) return;
    if (refreshed === false) {
      refreshAfterAuditFailed.value = true;
      if (isCurrent()) dialogVisible.value = false;
      return;
    }
    if (!isCurrent()) return;
    if (!refreshed || !next || filters !== filterVersion || activeTab.value === 'done') {
      dialogVisible.value = false;
      return;
    }
    const remaining=getFilteredAudits();
    let nextRow=remaining.find(row=>candidates.includes(auditKey(row)));
    if(!nextRow&&currentPage.value>=approvedPage){
      const offset=Math.max(0,position)+(remaining.some(row=>auditKey(row)===auditKey(item))?1:0);
      nextRow=remaining[offset];
      if(!nextRow&&total.value>currentPage.value*pageSize){
        await auditQuery.value?.setPage(currentPage.value+1);
        if(!isCurrent()||filters!==filterVersion)return;
        nextRow=allAudits.value.find(row=>auditKey(row)!==auditKey(item));
      }
    }
    if(nextRow&&isTodoAudit(nextRow)&&auditKey(nextRow)!==auditKey(item))openAuditDialog(nextRow);
    else{dialogVisible.value=false;ElMessage.info('后续待办已处理完');}
  } catch (error) {
    if (isCurrent()) ElMessage.error(error.message || '审批失败，请稍后重试');
  } finally {
    submitting.value = false;
  }
};

onMounted(async () => {
  if(route.query.taskId||route.query.subId)await tryOpenAuditFromRoute();
});

onUnmounted(() => {
  auditDisposed = true;
  auditRequestId += 1;
});

// 若用户在审核中心内路由 query 发生变化（或重复从通知跳转），也能自动打开
watch(
  () => `${route.query?.type || ''}:${route.query?.subId || ''}:${route.query?.taskId || ''}`,
  async (val, oldVal) => {
    if (val && val !== oldVal) {
      await tryOpenAuditFromRoute();
    }
  }
);

// 监听搜索和筛选变化
watch([searchText, filterType], () => {
  currentPage.value = 1;
  filterTableData();
});
</script>

<style scoped>
.all-pending-note {
  margin: 0 0 12px;
  color: #7a8493;
  font-size: 13px;
  line-height: 20px;
}

.audit-container {
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

/* 筛选栏 */
.filter-bar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.filter-left {
  display: flex;
  gap: 12px;
}

.search-input {
  width: 240px;
}

.w-150 {
  width: 150px;
}

/* 表格内容样式 */
.task-cell {
  display: flex;
  flex-direction: column;
}

.task-name {
  font-weight: 500;
  color: #303133;
}

.submit-info {
  display: flex;
  flex-direction: column;
  font-size: 13px;
}

.text-gray {
  color: #909399;
  font-size: 12px;
}

.status-process {
  color: #409eff;
}

.status-success {
  color: #67c23a;
}

.status-error {
  color: #f56c6c;
}

/* 弹窗样式 */
.section-title {
  font-size: 14px;
  font-weight: bold;
  border-left: 3px solid #1890ff;
  padding-left: 8px;
  margin-bottom: 12px;
  color: #303133;
}

.highlight-value {
  color: #1890ff;
  font-weight: bold;
  font-family: monospace;
}

.file-link {
  color: #1890ff;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}

.file-link:hover {
  text-decoration: underline;
}

.radio-success :deep(.el-radio__input.is-checked .el-radio__inner) {
  border-color: #67c23a;
  background: #67c23a;
}

.radio-success :deep(.el-radio__input.is-checked + .el-radio__label) {
  color: #67c23a;
}

.radio-danger :deep(.el-radio__input.is-checked .el-radio__inner) {
  border-color: #f56c6c;
  background: #f56c6c;
}

.radio-danger :deep(.el-radio__input.is-checked + .el-radio__label) {
  color: #f56c6c;
}

/* 辅助类 */
.ml-2 {
  margin-left: 8px;
}

.mr-1 {
  margin-right: 4px;
}

.mt-1 {
  margin-top: 4px;
}

.mt-4 {
  margin-top: 24px;
}

.pagination-container {
  margin-top: auto;
  padding-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 文件预览相关样式 */
.file-preview-wrapper {
  min-height: 400px;
}

.pdf-viewer {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.pdf-viewer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.file-name-display {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 16px;
}

.other-viewer {
  text-align: center;
  padding: 60px 20px;
}

.other-viewer .hint {
  color: #909399;
  font-size: 14px;
  margin-bottom: 20px;
}

.comment-text {
  color: #606266;
  font-size: 13px;
  line-height: 1.5;
}

.level4-audit-table {
  margin-top: 14px;
}
</style>
