import { reactive } from 'vue'
import { noticeStatus } from '@/api/platformNotices'
export const platformNoticeState = reactive({ token: '', checked: false, pending: [], error: '' })
let inflight
export async function checkPlatformNotices(force = false) {
  const token = localStorage.getItem('token') || ''
  if (platformNoticeState.token !== token) { Object.assign(platformNoticeState, { token, checked: false, pending: [], error: '' }); inflight = null }
  if (!token) return []
  if (!force && platformNoticeState.checked) return platformNoticeState.pending
  if (inflight) return inflight
  const request = noticeStatus().then(data => {
    if (localStorage.getItem('token') !== token) return []
    Object.assign(platformNoticeState, { checked: true, pending: data.pending || [], error: '' }); return platformNoticeState.pending
  }).catch(error => {
    if (localStorage.getItem('token') === token) { platformNoticeState.checked = false; platformNoticeState.error = error.message || '重要通知加载失败' }
    throw error
  }).finally(() => { if (inflight === request) inflight = null })
  inflight = request; return request
}
