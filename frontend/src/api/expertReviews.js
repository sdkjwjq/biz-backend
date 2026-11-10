import { get, post } from '@/utils/request'
const clean = params => Object.fromEntries(Object.entries(params || {}).filter(([, value]) => value !== '' && value != null))
export const getExpertObjects = (params, config) => get('/expert-reviews', clean(params), config)
export const getExpertDetail = (taskId, params, config) => get(`/expert-reviews/tasks/${taskId}`, clean(params), config)
export const getExpertHistory = (taskId, params, config) => get(`/expert-reviews/tasks/${taskId}/history`, clean(params), config)
// 专家可评审任务的目录结构（一级→二级→三级）
export const getScopeTree = (year, config) => get('/expert-reviews/scope-tree', clean({ year }), config)
export const saveExpertDraft = body => post('/expert-reviews/draft', body)
export const submitExpertOpinion = body => post('/expert-reviews/submit', body)
export const getExpertAccounts = config => get('/expert-reviews/admin/experts', {}, config)
// 发起评审任务
export const getReviewTasks = (year, config) => get('/expert-reviews/admin/review-tasks', { year }, config)
export const getReviewTaskExperts = (taskId, year, config) => get(`/expert-reviews/admin/review-tasks/${taskId}/experts`, { year }, config)
export const getReviewMembers = (keyword, page = 1, config) => get('/expert-reviews/admin/members', clean({ keyword, page }), config)
export const getAdminExpertOpinions = (params, config) => get('/expert-reviews/admin', clean(params), config)
export const getAdminExpertHistory = (id, params, config) => get(`/expert-reviews/admin/${id}/history`, clean(params), config)
export const exportExpertOpinions = body => post('/expert-reviews/admin/export', body, { responseType: 'blob' })
// 导出专家可评审任务树：一级/二级/三级编号 + 评语
export const exportExpertTree = body => post('/expert-reviews/tree-export', body || {}, { responseType: 'blob' })
