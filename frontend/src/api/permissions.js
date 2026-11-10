import { get, post, put, del } from '@/utils/request'
const base = '/admin/permissions'
export const permissionCatalog = config => get(`${base}/catalog`, {}, config)
export const permissionOptions = config => get(`${base}/options`, { separateOptions: true }, config)
export const userPermission = (id, config) => get(`${base}/users/${id}`, {}, config)
export const previewUserPermission = (id, body) => post(`${base}/users/${id}/preview`, body)
export const saveUserPermission = (id, body) => put(`${base}/users/${id}`, body)
export const previewPermissionRole = body => post(`${base}/roles/preview`, body)
export const savePermissionRole = body => post(`${base}/roles`, body)
export const deletePermissionRole = (code, params) => del(`${base}/roles/${encodeURIComponent(code)}?${new URLSearchParams(params)}`)
export const createExpertAccount = body => post(`${base}/experts`, body)
export const previewExpertTasks = (id, body) => post(`${base}/users/${id}/expert-tasks/preview`, body)
export const saveExpertTasks = (id, body) => post(`${base}/users/${id}/expert-tasks`, body)
export const previewDutyTransfer = params => get(`${base}/duties/preview`, params)
export const saveDutyTransfer = body => post(`${base}/duties/transfer`, body)

export const batchExpertTasks = body => post('/admin/permissions/expert-tasks/batch', body)
