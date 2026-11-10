/**
 * 管理员归属移交相关API（任务与绩效共用）
 */
import { get, post } from '@/utils/request';

/** 是否具备移交权限（管理员） */
export const getTransferCapabilities = (config = {}) => {
  return get('/manage/transfer/capabilities', {}, config);
};

/** 移交任务：{ ids, deptId, leaderId, auditorId, principalId, reason } */
export const transferTasks = (data) => {
  return post('/manage/transfer/tasks', data);
};

/** 移交绩效：{ ids, deptId, leaderId, auditorId, principalId, reason } */
export const transferPerformances = (data) => {
  return post('/manage/transfer/performances', data);
};

/** 归属变更记录：targetType = TASK | PERFORMANCE */
export const getTransferLogs = (targetType, targetId, config = {}) => {
  return get('/manage/transfer/logs', { targetType, targetId }, config);
};
