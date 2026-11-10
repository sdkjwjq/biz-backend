import { get, post } from '@/utils/request'

/** 任务移交申请：holder 只能移交自己对应的字段，接收人确认后生效 */
export const getPendingTransfers = (config = {}) => get('/task-transfer/pending', {}, config)
export const getTransferUsers = (taskIds, keyword, fieldType, config = {}) => get('/task-transfer/users', { taskIds: Array.isArray(taskIds) ? taskIds : [taskIds], keyword, fieldType }, config)
/** 查找接收人：全部在用账号，不受“已承担同类职责”限制 */
export const getAllTransferUsers = (params = {}, config = {}) => get('/task-transfer/all-users', params, config)
export const getTaskTransferLogs = (taskId, config = {}) => get('/task-transfer/logs', { taskId }, config)
export const getSentTransfers = (config = {}) => get('/task-transfer/sent', {}, config)
export const createTransferRequest = data => post('/task-transfer/request', data)
export const createTransferBatch = data => post('/task-transfer/batch', data)
export const acceptTransferRequest = id => post(`/task-transfer/${id}/accept`, {})
export const rejectTransferRequest = (id, remark) => post(`/task-transfer/${id}/reject`, { remark })
export const batchAcceptTransfers = requestIds => post('/task-transfer/batch-accept', { requestIds })
export const batchRejectTransfers = (requestIds, remark) => post('/task-transfer/batch-reject', { requestIds, remark })
