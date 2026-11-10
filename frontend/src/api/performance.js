/**
 * 绩效指标相关API
 */
import { get, post } from '@/utils/request';

/**
 * 获取所有绩效指标
 * GET /performance
 */
export const getAllPerformance = () => {
    return get('/performance');
};

/**
 * 根据ID获取绩效指标详情
 * GET /performance/{perfId}
 */
export const getPerformanceById = (perfId) => {
    return get(`/performance/${perfId}`);
};

/**
 * 刷新绩效指标（后端当前接口会重新计算所有绩效）
 * POST /performance/refresh
 */
export const refreshPerformance = () => {
    return post('/performance/refresh');
};

/**
 * 刷新所有绩效指标
 * POST /performance/refresh
 */
export const refreshAllPerformance = () => {
    return post('/performance/refresh');
};

/**
 * 根据绩效指标ID获取关联的任务列表
 * GET /performance/task/{perfId}
 */
export const getTasksByPerfId = (perfId, year) => {
    const query = year ? `?year=${encodeURIComponent(year)}` : '';
    return get(`/performance/task/${perfId}${query}`);
};

/**
 * 按年份获取绩效指标
 * GET /performance/year/{year}
 */
export const getPerformanceByYear = (year) => {
    return get(`/performance/year/${year}`);
};

/**
 * 手动提交绩效指标（用于无关联任务的指标）
 * POST /performance/submit
 * 参数: pref_id, actual_value, year
 */
export const submitPerformanceManually = (prefId, actualValue, year) => {
    // 使用 URLSearchParams 来发送表单数据
    const params = new URLSearchParams();
    params.append('pref_id', prefId);
    params.append('actual_value', actualValue);
    params.append('year', year);

    return post('/performance/submit', params, {
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    });
};

export const getTodoPerformanceAudits = () => {
    return get('/performance/audit/todo');
};

export const getPerformanceAuditRecords = () => {
    return get('/performance/audit/records');
};

export const getPerformanceAuditsByPerfAndYear = (perfId, year) => {
    const query = year ? `?year=${encodeURIComponent(year)}` : '';
    return get(`/performance/audit/perf/${perfId}${query}`);
};

export const auditPerformance = (data) => {
    return post('/performance/audit', data);
};

export const withdrawPerformanceAudit = (subId) => {
    return post(`/performance/audit/withdraw/${subId}`);
};

export const getPerformanceAuditLogs = (subId) => {
    return get(`/performance/audit/logs/${subId}`);
};

/**
 * 管理员编辑绩效关联任务相关接口
 */
export const getPerformanceRelationCapabilities = () => {
    return get('/manage/performance-relation/capabilities');
};

export const getPerformanceRelationCandidates = (perfId, year) => {
    return get(`/manage/performance-relation/candidates?perfId=${encodeURIComponent(perfId)}&year=${encodeURIComponent(year)}`);
};

export const getPerformanceRelationCounts = (year) => {
    return get(`/manage/performance-relation/counts?year=${encodeURIComponent(year)}`);
};

export const getPerformanceRelationLogs = (perfId, year) => {
    return get(`/manage/performance-relation/logs?perfId=${encodeURIComponent(perfId)}&year=${encodeURIComponent(year)}`);
};

export const savePerformanceRelation = (data) => {
    return post('/manage/performance-relation', data);
};

// Badge count shares the list endpoint permissions and business predicates.
export const getTodoPerformanceAuditCount = () => get('/performance/audit/todo', { countOnly: true });
