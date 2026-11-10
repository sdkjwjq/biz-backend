import { adminTestFetch } from '@/utils/adminTest';
/**
 * 预算相关API
 */
import { get, post } from '@/utils/request';
import { readDownloadResponse } from '@/utils/fileResponses.mjs';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';
export const saveBudget = data => post('/budget/save', data);
export const getBudgetTrends = (year, taskIndex = 0, config = {}) => get('/budget/trends', { year, taskIndex }, config);

export const getBudget = (year, month, section = 'annual') => {
  return get('/budget', { year, month, section });
};

export const importBudget = (year, month, file, section = 'annual') => {
  const formData = new FormData();
  formData.append('year', year);
  formData.append('month', month);
  formData.append('section', section);
  formData.append('file', file);
  return post('/budget/import', formData);
};

export const updateBudgetLock = (year, month, locked, section = 'annual') => {
  const params = new URLSearchParams();
  params.append('year', year);
  params.append('month', month);
  params.append('section', section);
  params.append('locked', String(locked));
  return post('/budget/lock', params);
};

export const getBudgetVersions = (year, month, section = 'annual') => {
  return get('/budget/versions', { year, month, section });
};

export const copyBudgetSection = (year, month, from = 'annual', to = 'execution') => {
  const params = new URLSearchParams();
  params.append('year', year);
  params.append('month', month);
  params.append('from', from);
  params.append('to', to);
  return post('/budget/copy-section', params);
};

export const getBudgetVersion = (versionId) => {
  return get(`/budget/versions/${versionId}`);
};

export const restoreBudgetVersion = (versionId, remark) => {
  return post(`/budget/versions/${versionId}/restore`, { remark });
};

export const exportBudget = async (year, month, section = 'annual') => {
  const token = localStorage.getItem('token') || '';
  const query = new URLSearchParams({ year: String(year), month: String(month), section: String(section) }).toString();
  const response = await adminTestFetch(`${API_BASE_URL}/budget/export?${query}`, {
    method: 'GET',
    headers: {
      ...(token && { Authorization: token })
    }
  });

  return readDownloadResponse(response, `${year}年${section === 'execution' ? '年度预算执行' : '年度预算'}.xlsx`);
};

export const exportBudgetTemplate = async (year, section = 'execution') => {
  const token = localStorage.getItem('token') || '';
  const query = new URLSearchParams({ year: String(year), month: '5', section: String(section) }).toString();
  const response = await adminTestFetch(`${API_BASE_URL}/budget/template?${query}`, {
    method: 'GET',
    headers: {
      ...(token && { Authorization: token })
    }
  });

  return readDownloadResponse(response, `${year}年${section === 'execution' ? '年度预算执行模板' : '年度预算模板'}.xlsx`);
};
