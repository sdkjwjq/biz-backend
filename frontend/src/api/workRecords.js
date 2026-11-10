import { get, post } from '@/utils/request';

export const getWorkRecordCapabilities = config => get('/work-records/capabilities', {}, config);
export const getWorkRecordAuthors = config => get('/work-records/authors', {}, config);
export const listWorkRecords = (params, config) => get('/work-records', params, config);
export const getWorkRecord = (id, config) => get(`/work-records/${id}`, {}, config);
export const createWorkRecord = (year, month) => post('/work-records', { year, month });
export const saveWorkRecord = (id, data) => post(`/work-records/${id}/save`, data);
export const submitWorkRecord = (id, data) => post(`/work-records/${id}/submit`, data);
export const deleteWorkRecord = (id, version, reason) => post(`/work-records/${id}/delete`, { version, reason });
export const deleteWorkRecords = (items, reason) => post('/work-records/delete-batch', { items, reason });
export const refreshWorkRecordStatistics = (id, version) => post(`/work-records/${id}/statistics/refresh`, { version });
export const exportWorkRecords = (ids, config) => post('/work-records/export', { ids }, { ...config, responseType: 'blob' });
