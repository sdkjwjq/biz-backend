/**
 * 定时任务手动触发API
 */
import { post } from '@/utils/request';

/**
 * 手动触发每月部门负责人提醒
 */
export const triggerMonthLeaderNotice = () => {
  return post('/scheduled/month_leader_trigger');
};

/**
 * 手动触发每月审核人提醒
 */
export const triggerMonthAuditorNotice = () => {
  return post('/scheduled/month_auditor_trigger');
};

/**
 * 手动触发年度提醒
 */
export const triggerYearNotice = () => {
  return post('/scheduled/year_trigger');
};

/**
 * 手动更新任务状态
 */
export const updateTaskStatus = () => {
  return post('/scheduled/update_task_status');
};
