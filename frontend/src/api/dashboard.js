/**
 * 数据大屏相关API
 */
import { get, post } from '@/utils/request';

/**
 * 获取大屏数据汇总
 * GET /dashboard/summary
 */
export const getDashboardSummary = () => {
  return get('/dashboard/summary');
};

/**
 * 获取所有任务完成率
 * GET /dashboard/completion/overall
 */
export const getAllTaskCompletionRate = () => {
  return get('/dashboard/completion/overall');
};

/**
 * 获取本年度任务完成率
 * GET /dashboard/completion/year?year=2026
 */
export const getYearTaskCompletionRate = (year) => {
  return get('/dashboard/completion/year', { year });
};

/**
 * 获取中期任务完成率
 * GET /dashboard/completion/midterm?year=2028
 */
export const getMidTermTaskCompletionRate = (year) => {
  return get('/dashboard/completion/midterm', { year });
};

/**
 * 获取一级任务完成率
 * GET /dashboard/completion/first-level
 */
export const getFirstLevelTaskCompletionRate = () => {
  return get('/dashboard/completion/first-level');
};

/**
 * 获取各部门整体任务完成率
 * GET /dashboard/dept/overall
 */
export const getDeptTaskCompletionRates = (config = {}) => {
  return get('/dashboard/dept/overall', {}, config);
};

/**
 * 获取各部门本年度任务完成率
 * GET /dashboard/dept/year?year=2026
 */
export const getDeptYearTaskCompletionRates = (year, config = {}) => {
  return get('/dashboard/dept/year', { year }, config);
};

/**
 * 获取各部门中期任务完成率
 * GET /dashboard/dept/midterm?endYear=2028
 */
export const getDeptMidTermTaskCompletionRates = (endYear, config = {}) => {
  return get('/dashboard/dept/midterm', { endYear }, config);
};

/**
 * 获取一级任务详细情况
 * GET /dashboard/tasks/first-level
 */
export const getFirstLevelTaskDetails = (config = {}) => {
  return get('/dashboard/tasks/first-level', {}, config);
};

/**
 * 获取单个部门详细统计信息
 * GET /dashboard/dept/{deptId}
 */
export const getDeptStatsDetail = (deptId) => {
  return get(`/dashboard/dept/${deptId}`);
};

/**
 * 批量获取部门详细统计信息
 * GET /dashboard/dept/batch?deptIds=1,2,3
 */
export const getDeptStatsBatch = (deptIds) => {
  const value = Array.isArray(deptIds) ? deptIds.join(',') : deptIds;
  return get('/dashboard/dept/batch', { deptIds: value });
};

/**
 * 获取完成率对比
 * GET /dashboard/comparison/{dimension}
 * dimension: dept | year | level
 */
export const getCompletionComparison = (dimension) => {
  return get(`/dashboard/comparison/${dimension}`);
};

/**
 * 获取指定年份的每日趋势数据（三级任务完成率）
 * - GET /dashboard/trend/{year}
 * - 不传 year 时，后端默认当前年份：GET /dashboard/trend/
 */
export const getTrendData = (year, config = {}) => {
  if (year) {
    return get(`/dashboard/trend/${year}`, {}, config);
  }
  return get('/dashboard/trend/', {}, config);
};

/**
 * 手动记录当天趋势数据
 * POST /dashboard/trend/record
 */
export const recordTrendData = () => {
  return post('/dashboard/trend/record');
};

/**
 * 获取全量任务详细情况（所有层级，不按角色过滤，用于大屏统计）
 * GET /dashboard/tasks/all_level
 */
export const getAllTaskDetails = (config = {}) => {
  return get('/dashboard/tasks/all_level', {}, config);
};
