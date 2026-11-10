import { adminTestFetch } from '@/utils/adminTest';
/**
 * 上报与督办中心相关API（复用后端已有接口）
 * - 上传文件：POST /system/upload
 * - 材料上报：POST /biz/sub
 * - 批量材料上报（四级任务）：POST /biz/sub
 * - 发送任务督办：POST /supervision/reminders
 */
import { post, upload } from '@/utils/request';

const BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

// 后端很多接口失败时会返回 ErrorVO：{ message, code }，但HTTP仍可能是200
const unwrap = (data) => {
  if (data && typeof data === 'object' && 'code' in data && data.code !== null && data.code !== undefined) {
    const code = Number(data.code);
    if (!Number.isNaN(code) && code >= 400) {
      throw new Error(data.message || '请求失败');
    }
  }
  return data;
};

export const uploadFile = (file, taskId) => {
  if (taskId === null || taskId === undefined) {
    return Promise.reject(new Error('任务ID不能为空'));
  }
  if (file?.size > 50 * 1024 * 1024) {
    return Promise.reject(new Error('文件超过 50MB，请压缩后重新选择'));
  }
  return upload(`/system/upload/${taskId}`, file).then(unwrap);
};

export const submitMaterial = (data) => {
  return post('/biz/sub', data).then(unwrap);
};

/**
 * 批量上报（用于将拆分的四级任务打包合并提交）
 * @param {Object} data 
 * @param {Number} data.third_task_id 三级任务ID
 * @param {Number} data.file_id 统一上传的佐证材料文件ID
 * @param {String} data.comment 完成情况说明
 * @param {Array} data.sub_list 四级任务填报数据列表 [{task_id, reported_value, data_type}]
 */
export const batchSubmitMaterial = (data) => {
  return post('/biz/sub', data).then(unwrap);
};

export const sendNotice = (data) => {
  return post('/supervision/reminders', {
    taskId: data.taskId ?? data.source_id,
    toUserId: data.toUserId ?? data.to_user_id,
    title: data.title,
    content: data.content,
  }).then(unwrap);
};

export const downloadFile = async (fileId) => {
  const token = localStorage.getItem('token') || '';
  const response = await adminTestFetch(`${BASE_URL}/system/download/${fileId}`, {
    method: 'GET',
    headers: {
      ...(token && { Authorization: token }),
    },
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: '下载失败' }));
    throw new Error(error.message || '下载失败');
  }

  return response;
};
