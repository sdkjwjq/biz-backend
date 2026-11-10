import { get, post, put } from '@/utils/request'

export const getAdminOverview = (params, config) => get('/admin/overview', params, config)
export const getOverviewSummary = (params, config) => get('/admin/overview/summary', params, config)
export const getOverviewAnalysis = (params, config) => get('/admin/overview/analysis', Object.fromEntries(Object.entries(params).filter(([, value]) => value != null && value !== '')), config)
export const getAdminUsers = params => get('/admin/users', params)
export const getLeadershipAccess = userId => get(`/admin/users/${userId}/leadership`)
export const saveLeadershipAccess = (userId, data) => put(`/admin/users/${userId}/leadership`, data)
export const createAdminUser = data => post('/admin/users', data)
export const updateAdminUser = (userId, data) => put(`/admin/users/${userId}`, data)
export const getAdminUserImpact = userId => get(`/admin/users/${userId}/impact`)
export const updateAdminUserStatus = (userId, data) => post(`/admin/users/${userId}/status`, data)
export const resetAdminUserPassword = (userId, data) => post(`/admin/users/${userId}/reset-password`, data)

export const getAdminDepartments = () => get('/admin/departments')
export const getAdminDepartmentImpact = deptId => get(`/admin/departments/${deptId}/impact`)
export const updateAdminDepartment = (deptId, data) => put(`/admin/departments/${deptId}`, data)

export const getAdminOperations = params => get('/admin/operations', params)
export const getAdminOperation = (type, id) => get(`/admin/operations/${type}/${id}`)
export const exportAdminOperations = params => {
  const query = new URLSearchParams()
  for (const [key,value] of Object.entries(params||{})) if(value!==''&&value!=null)for(const item of Array.isArray(value)?value:[value])query.append(key,item)
  return post(`/admin/operations/export?${query}`, {}, { responseType: 'blob' })
}
export const getDeletedWorkRecords = params => get('/admin/work-records/deleted', params)

export const getDepartmentLeaderCandidates = (deptId, params, config) => get(`/admin/departments/${deptId}/leader-candidates`, params, config)
