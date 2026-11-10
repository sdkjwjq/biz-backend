/**
 * 成果相关API
 */
import { get, post } from '@/utils/request';

/**
 * 获取全量成果数据
 */
export const getAllAchievements = (config = {}) => {
  return get('/achievement/', {}, config);
};

/**
 * 根据ID获取成果
 */
export const getAchievementById = (achId) => {
  return get(`/achievement/${achId}`);
};

/**
 * 新增成果
 */
export const addAchievement = (achievement) => {
  return post('/achievement/add', achievement);
};

/**
 * 更新成果
 */
export const updateAchievement = (id, achievement) => {
  return post(`/achievement/update/${id}`, achievement);
};

/**
 * 删除成果
 */
export const deleteAchievement = (achId) => {
  return post(`/achievement/delete/${achId}`);
};

export const getTodoAchievementAudits = (config = {}) => {
  return get('/achievement/audit/todo', {}, config);
};

export const getAchievementAuditRecords = () => {
  return get('/achievement/audit/records');
};

export const getAchievementAuditsByAchId = (achId) => {
  return get(`/achievement/audit/achievement/${achId}`);
};

export const auditAchievement = (data) => {
  return post('/achievement/audit', data);
};

export const getAchievementAuditLogs = (subId) => {
  return get(`/achievement/audit/logs/${subId}`);
};


// Badge count shares the list endpoint permissions and business predicates.
export const getTodoAchievementAuditCount = () => get('/achievement/audit/todo', { countOnly: true });

export const resubmitAchievement = achId => post(`/achievement/${achId}/resubmit`);
