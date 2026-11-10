<template>
  <section class="notice-reading" v-loading="loading">
    <el-alert v-if="error" :title="error" type="error" :closable="false"><el-button @click="load" link>重新加载通知</el-button></el-alert>
    <template v-if="notice">
      <h2>{{ notice.title }}</h2>
      <div class="meta"><el-tag v-if="Number(notice.important)" type="danger" size="small">重要通知</el-tag><span>{{ time(notice.publishedAt) }}</span><span>第{{ notice.contentVersion }}版</span></div>
      <div ref="body" class="notice-body" tabindex="0" aria-label="通知正文" @scroll="measure">{{ notice.content }}</div>
      <div v-if="notice.attachments?.length" class="attachments"><strong>附件</strong><el-button v-for="file in notice.attachments" :key="file.id" link type="primary" @click="download(file)">{{ file.name }}</el-button></div>
      <div class="read-footer"><span>{{ notice.confirmedAt ? '已确认阅读' : atBottom ? '阅读后请确认已阅' : '请向下阅读完整通知正文' }}</span><el-button type="primary" :disabled="!atBottom || Boolean(error)" :loading="saving" @click="confirm">{{ buttonText }}</el-button></div>
    </template>
    <p v-else-if="loading" role="status">正在加载通知正文…</p>
  </section>
</template>
<script setup>
import { ref, watch, nextTick, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { openPlatformNotice, confirmPlatformNotice, downloadPlatformAttachment } from '@/api/platformNotices'
const props = defineProps({ id: { type: [String, Number], required: true }, buttonText: { type: String, default: '确认已阅' } })
const emit = defineEmits(['confirmed', 'unavailable'])
const notice = ref(null), loading = ref(false), saving = ref(false), error = ref(''), body = ref(null), atBottom = ref(false)
let version = 0, disposed = false, observer
const time = value => value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : ''
const measure = () => { const el = body.value; if (el) atBottom.value = el.scrollHeight <= el.clientHeight + el.scrollTop + 3 }
async function load() {
  const request = ++version, token = localStorage.getItem('token'); loading.value = true; error.value = ''; notice.value = null; atBottom.value = false
  try {
    const requestId = globalThis.crypto?.randomUUID?.() || `${Date.now()}-${Math.random().toString(36).slice(2)}-${Math.random().toString(36).slice(2)}`
    const value = await openPlatformNotice(props.id, requestId)
    if (disposed || request !== version || token !== localStorage.getItem('token')) return
    notice.value = value; await nextTick(); observer?.disconnect(); observer = new ResizeObserver(measure); if (body.value) observer.observe(body.value); measure()
  } catch (e) { if (!disposed && request === version) { error.value = e.message || '通知加载失败'; if ([404, 409].includes(e.responseCode)) emit('unavailable') } }
  finally { if (!disposed && request === version) loading.value = false }
}
async function confirm() {
  if (saving.value || !notice.value || !atBottom.value) return
  saving.value = true
  try { await confirmPlatformNotice(notice.value); window.dispatchEvent(new Event('platform-notice-read')); emit('confirmed', notice.value) }
  catch (e) { error.value = e.message || '确认失败，请重试' }
  finally { saving.value = false }
}
async function download(file) {
  try { const blob = await downloadPlatformAttachment(notice.value.id, file.id), url = URL.createObjectURL(blob), link = document.createElement('a'); link.href = url; link.download = file.name; link.click(); setTimeout(() => URL.revokeObjectURL(url), 1000) }
  catch (e) { ElMessage.error(e.message || '附件下载失败') }
}
watch(() => props.id, load, { immediate: true })
onUnmounted(() => { disposed = true; version++; observer?.disconnect() })
</script>
<style scoped>
.notice-reading{min-height:180px}h2{font-size:22px;line-height:1.5;margin:12px 0;overflow-wrap:anywhere}.meta{display:flex;gap:12px;align-items:center;flex-wrap:wrap;color:#7a8798;font-size:13px;margin-bottom:20px}.notice-body{white-space:pre-wrap;overflow-wrap:anywhere;line-height:1.9;font-size:16px;max-height:min(52vh,550px);min-height:100px;overflow:auto;border:1px solid #e4e9f1;padding:20px;border-radius:6px;color:#303d4b;background:#fbfcfe}.attachments{display:flex;flex-direction:column;align-items:flex-start;gap:8px;margin-top:16px}.attachments .el-button{white-space:normal;height:auto;text-align:left;overflow-wrap:anywhere;margin-left:0}.read-footer{display:flex;justify-content:space-between;gap:15px;align-items:center;margin-top:22px}.read-footer span{color:#7a8798;font-size:13px}@media(max-width:600px){.read-footer{flex-wrap:wrap}.notice-body{padding:14px;max-height:48vh}}
</style>
