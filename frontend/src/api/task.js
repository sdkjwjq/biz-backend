import { adminTestFetch } from '@/utils/adminTest';
/**
 * 任务相关API
 */
import { get, post } from '@/utils/request';
import { readDownloadResponse } from '@/utils/fileResponses.mjs';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

/**
 * 获取全量任务数据
 */
export const getAllTasks = () => {
  return get('/biz/tasks');
};

/**
 * 根据ID获取任务
 */
export const getTaskById = (taskId) => {
  return get(`/biz/tasks/${taskId}`);
};

/**
 * 获取当前任务的所有子任务
 */
export const getChildrenTasks = (taskId) => {
  return get('/biz/tasks/children', { task_id: taskId });
};

/**
 * 获取当前任务的父任务
 */
export const getParentTask = (taskId) => {
  return get('/biz/tasks/parent', { task_id: taskId });
};

/**
 * 根据部门ID获取任务
 */
export const getTasksByDeptId = (deptId) => {
  return get('/biz/tasks/dept', { dept_id: deptId });
};

/**
 * 获取四级任务（根据父任务ID/三级任务ID）
 */
export const getForthTasks = (parentId) => {
  return get('/biz/tasks/forth', { parent_id: parentId });
};

/**
 * 添加三级任务
 * POST /biz/tasks/manage/add
 */
export const addManagedTask = (task) => {
  return post('/biz/tasks/manage/add', task);
};

/**
 * 更新任务
 * POST /biz/tasks/manage/update
 */
export const updateManagedTask = (task) => {
  return post('/biz/tasks/manage/update', task);
};

/**
 * 管理员完成任务
 * POST /biz/tasks/manage/finish/{taskId}
 */
export const finishManagedTask = (taskId) => {
  return post(`/biz/tasks/manage/finish/${taskId}`);
};

/**
 * 导出任务明细（Excel）
 * POST /biz/tasks/export  body: { ids: [...] }
 * 后端按本人可见范围二次校验后返回 xlsx
 */
export const exportTasks = async (ids, config = {}) => {
  const token = localStorage.getItem('token') || '';
  const params=new URLSearchParams();for(const [key,value] of Object.entries(config.filters||{}))for(const item of Array.isArray(value)?value:[value])if(item!==''&&item!=null)params.append('f.'+key,item);
  const response = await adminTestFetch(config.filters?`${API_BASE_URL}/list-filters/tasks/export?${params}`:`${API_BASE_URL}/biz/tasks/export`, {
    method: config.filters?'GET':'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token && { Authorization: token })
    },
    body: config.filters?undefined:JSON.stringify({ ids }),
    signal: config.signal
  });

  return readDownloadResponse(response, '任务明细.xlsx');
};
