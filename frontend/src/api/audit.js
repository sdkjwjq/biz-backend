/**
 * 审核相关API
 */
import { get, post } from '@/utils/request';

// 统一处理后端可能返回的 ErrorVO，确保逻辑错误能被前端 try...catch 正确捕获
const unwrap = (data) => {
  if (data && typeof data === 'object' && 'code' in data && data.code !== null && data.code !== undefined) {
    const code = Number(data.code);
    if (!Number.isNaN(code) && code >= 400) {
      throw new Error(data.message || '请求失败');
    }
  }
  return data;
};

/**
 * 获取审批单（根据任务ID和当前用户）
 */
export const getAudit = (taskId) => {
  return get(`/biz/audit/${taskId}`).then(unwrap);
};

/**
 * 获取某任务全部审批单（用于任务详情抽屉展示完整流程）
 */
export const getAuditByTaskId = (taskId) => {
  return get(`/biz/audit/task/${taskId}`).then(unwrap);
};

/**
 * 审批任务
 */
export const auditTask = (data) => {
  return post('/biz/audit', data).then(unwrap);
};

/**
 * 获取审批操作日志（biz_audit_log）
 */
export const getAuditLogs = (subId) => {
  return get(`/biz/audit/logs/${subId}`).then(unwrap);
};

/**
 * 获取任务最近一次审批通过的文件
 * GET /biz/audit/file/{taskId}
 */
export const getApprovedAuditFile = (taskId) => {
  return get(`/biz/audit/file/${taskId}`).then(unwrap);
};

/**
 * 获取“待我审批”的审批单列表（按 current_handler_id 查询）
 * GET /biz/audit/todo
 */
export const getTodoAudits = () => {
  return get('/biz/audit/todo').then(unwrap);
};

/**
 * 退回后重新提交材料（复用原审批单）
 * POST /biz/resub
 */
export const reSubmitMaterial = (data) => {
  return post('/biz/resub', data).then(unwrap);
};

/**
 * 撤回最新提交
 * POST /biz/drawback/{taskId}
 */
export const drawbackTask = (taskId) => {
  return post(`/biz/drawback/${taskId}`).then(unwrap);
};

// Badge count shares the list endpoint permissions and business predicates.
export const getTodoAuditCount = () => get('/biz/audit/todo', { countOnly: true }).then(unwrap);
