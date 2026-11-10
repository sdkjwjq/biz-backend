<template>
  <main class="admin-notices">
    <header class="page-head"><div><h2>通知管理</h2><p>发布平台通知，查询接收人员及浏览记录。任务督办继续在督办中心发送。</p></div><div><DetailedExportButton domain="platform-notices" /><el-button type="primary" @click="edit()">发送通知</el-button></div></header>
    <section class="panel">
      <div class="filters"><el-input v-model="keyword" aria-label="通知标题" placeholder="搜索通知标题" clearable @input="autoQuery.schedule(true)" @keyup.enter="autoQuery.flush()" /><el-select v-model="status" aria-label="通知状态" clearable placeholder="全部状态" @change="autoQuery.flush()"><el-option label="草稿" value="DRAFT" /><el-option label="已发布" value="PUBLISHED" /><el-option label="已撤回" value="WITHDRAWN" /></el-select><el-button @click="reset">重置</el-button></div>
      <el-alert v-if="error" :title="error" type="error" :closable="false"><el-button link @click="load">重新加载</el-button></el-alert>
      <el-table :data="rows" v-loading="loading" :empty-text="ready ? '暂无平台通知' : '正在加载通知…'">
        <el-table-column label="通知标题" min-width="260"><template #default="{ row }"><el-button link type="primary" class="title-link" @click="view(row)">{{ row.title }}</el-button></template></el-table-column>
        <el-table-column label="级别" width="90"><template #default="{ row }"><el-tag :type="Number(row.important) ? 'danger' : 'info'">{{ Number(row.important) ? '重要' : '普通' }}</el-tag></template></el-table-column>
        <el-table-column label="状态" width="100"><template #default="{ row }">{{ states[row.status] }}</template></el-table-column>
        <el-table-column label="发布时间" width="175"><template #default="{ row }">{{ time(row.publishedAt) }}</template></el-table-column>
        <el-table-column label="确认 / 接收" width="115"><template #default="{ row }">{{ row.confirmedCount }} / {{ row.recipientCount }}</template></el-table-column>
        <el-table-column label="操作" width="230" fixed="right"><template #default="{ row }"><el-button v-if="row.status === 'DRAFT'" link type="primary" @click="edit(row)">编辑 / 发布</el-button><template v-else><el-button link type="primary" @click="receipts(row)">浏览记录</el-button><el-button link type="primary" @click="edit(row, true)">更正发布</el-button><el-button v-if="row.status === 'PUBLISHED'" link type="danger" @click="withdraw(row)">撤回</el-button></template></template></el-table-column>
      </el-table>
      <div class="pager"><el-pagination v-model:current-page="page" :total="total" :page-size="10" layout="total, prev, pager, next" @current-change="load" /></div>
    </section>
    <el-dialog v-model="editing" :title="form.id ? '编辑通知草稿' : form.revisionOf ? '更正发布通知' : '发送平台通知'" width="850px" class="notice-editor" :close-on-click-modal="false" :before-close="closeEditor">
      <el-form label-position="top" @submit.prevent>
        <el-form-item label="标题" required><el-input v-model="form.title" maxlength="100" show-word-limit placeholder="请输入通知标题" /></el-form-item>
        <div class="form-grid"><el-form-item label="通知级别"><el-radio-group v-model="form.important"><el-radio :value="false">普通通知</el-radio><el-radio :value="true">重要通知（登录后必读）</el-radio></el-radio-group></el-form-item><el-form-item label="有效截止时间（可不填）"><el-date-picker v-model="form.expiresAt" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="不填则持续有效，直至撤回" /></el-form-item></div>
        <el-form-item label="接收范围" required><el-radio-group v-model="form.audienceType" @change="form.audienceIds = []"><el-radio value="ALL">所有正常账号</el-radio><el-radio value="DEPARTMENTS">指定部门</el-radio><el-radio value="USERS">指定人员</el-radio></el-radio-group></el-form-item>
        <el-form-item v-if="form.audienceType === 'DEPARTMENTS'" label="接收部门"><el-select v-model="form.audienceIds" multiple filterable placeholder="请选择部门"><el-option v-for="item in departments" :key="item.id" :label="item.name" :value="item.id" /></el-select></el-form-item>
        <el-form-item v-if="form.audienceType === 'USERS'" label="接收人员"><el-select v-model="form.audienceIds" multiple filterable remote :remote-method="searchUsers" :loading="searching" placeholder="输入姓名或工号搜索"><el-option v-for="item in users" :key="item.id" :label="`${item.name}（${item.id}）`" :value="item.id" /></el-select></el-form-item>
        <el-form-item label="通知正文" required><el-input v-model="form.content" type="textarea" :rows="9" maxlength="20000" show-word-limit placeholder="请输入完整通知内容" /></el-form-item>
        <el-form-item label="附件"><div><el-upload :show-file-list="false" :http-request="uploadFile" accept=".pdf,.doc,.docx,.xls,.xlsx,.png,.jpg,.jpeg" :disabled="uploading || files.length >= 10"><el-button :loading="uploading">上传附件</el-button></el-upload><p class="muted">最多10份，单份20MB，支持PDF、Word、Excel和图片。</p><div v-for="file in files" :key="file.id" class="file"><span>{{ file.name }}</span><el-button link type="danger" @click="removeFile(file)">移除</el-button></div></div></el-form-item>
        <el-alert v-if="form.important" title="重要通知将在接收用户下次登录时优先展示，全部确认已阅后才能进入业务页面。当前在线用户的编辑不会被打断。" type="warning" :closable="false" />
      </el-form>
      <template #footer><el-button @click="closeEditor()">取消</el-button><el-button :loading="saving" :disabled="uploading" @click="save()">保存草稿</el-button><el-button type="primary" :loading="saving" :disabled="uploading" @click="preparePublish">预览并发布</el-button></template>
    </el-dialog>
    <el-dialog v-model="previewing" title="发布预览" width="800px" :close-on-click-modal="false"><template v-if="preview"><h2>{{ form.title }}</h2><p>将发送给 <strong>{{ preview.total }}</strong> 个正常账号 · {{ form.important ? '重要通知，登录后必读' : '普通通知' }}</p><div class="preview-body">{{ form.content }}</div><p v-if="files.length">附件：{{ files.map(f => f.name).join('、') }}</p><el-collapse><el-collapse-item title="查看接收人员（预览最多显示100人）"><p v-for="user in preview.recipients" :key="user.id">{{ user.name }}（{{ user.id }}） {{ user.deptName }}</p></el-collapse-item></el-collapse></template><template #footer><el-button :disabled="publishing" @click="previewing = false">返回编辑</el-button><el-button type="primary" :loading="publishing" @click="publish">确认发布</el-button></template></el-dialog>
    <el-dialog v-model="viewing" title="通知内容" width="800px"><template v-if="viewed"><h2>{{ viewed.title }}</h2><p>{{ states[viewed.status] }} · 第{{ viewed.contentVersion }}版</p><div class="preview-body">{{ viewed.content }}</div><p v-if="viewed.withdrawReason">撤回原因：{{ viewed.withdrawReason }}</p></template></el-dialog>
    <el-dialog v-model="readingVisible" title="通知浏览记录" width="1100px"><template v-if="selected"><h3>{{ selected.title }}</h3><p>应收 {{ readSummary.total || 0 }} 人，打开过 {{ readSummary.opened || 0 }} 人，确认已阅 {{ readSummary.confirmed || 0 }} 人。部门以发布时信息为准。</p><div class="filters"><el-input v-model="readKeyword" placeholder="搜索姓名、工号或部门" @input="autoReceipts.schedule(true)" @keyup.enter="autoReceipts.flush()" /><el-select v-model="readState" placeholder="全部确认状态" clearable @change="autoReceipts.flush()"><el-option label="已确认" value="confirmed" /><el-option label="未确认" value="unconfirmed" /></el-select><el-button :loading="exporting" @click="exportReads">导出全部浏览记录</el-button></div><el-table :data="readRows" v-loading="readLoading"><el-table-column prop="userId" label="工号" width="95" /><el-table-column prop="userName" label="姓名" width="100" /><el-table-column prop="deptName" label="发布时所属部门" min-width="150" /><el-table-column label="首次打开" width="175"><template #default="{ row }">{{ time(row.firstOpenedAt) }}</template></el-table-column><el-table-column label="最近打开" width="175"><template #default="{ row }">{{ time(row.lastOpenedAt) }}</template></el-table-column><el-table-column prop="openCount" label="查看次数" width="90" /><el-table-column label="确认已阅" width="175"><template #default="{ row }">{{ time(row.confirmedAt) }}</template></el-table-column><el-table-column label="日志" width="70"><template #default="{ row }"><el-button link @click="showEvents(row)">查看</el-button></template></el-table-column></el-table><div class="pager"><el-pagination v-model:current-page="readPage" :page-size="20" :total="readTotal" layout="total, prev, pager, next" @current-change="loadReceipts" /></div></template></el-dialog>
    <el-dialog v-model="eventsVisible" title="最近浏览日志（最多200条）" width="600px"><el-table :data="eventRows"><el-table-column label="操作"><template #default="{ row }">{{ row.action === 'OPEN' ? '打开正文' : '确认已阅' }}</template></el-table-column><el-table-column prop="contentVersion" label="通知版本" /><el-table-column label="时间" width="200"><template #default="{ row }">{{ time(row.createTime) }}</template></el-table-column></el-table></el-dialog>
  </main>
</template>
<script setup>
import DetailedExportButton from '../DetailedExportButton.vue'
import { reactive, ref, onMounted, onUnmounted } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as api from '@/api/platformNotices'
import { createFilterTrigger } from '@/utils/filterInteraction.mjs'
const rows = ref([]), total = ref(0), page = ref(1), keyword = ref(''), status = ref(''), loading = ref(false), error = ref(''), ready = ref(false)
const editing = ref(false), saving = ref(false), uploading = ref(false), publishing = ref(false), previewing = ref(false), preview = ref(null), files = ref([]), users = ref([]), departments = ref([]), searching = ref(false)
const empty = () => ({ id: null, version: null, title: '', content: '', important: false, audienceType: 'ALL', audienceIds: [], fileIds: [], expiresAt: null, revisionOf: null })
const form = reactive(empty()), viewing = ref(false), viewed = ref(null), readingVisible = ref(false), selected = ref(null), readPage = ref(1), readKeyword = ref(''), readState = ref(''), readRows = ref([]), readSummary = ref({}), readTotal = ref(0), readLoading = ref(false), exporting = ref(false), eventsVisible = ref(false), eventRows = ref([])
const draftPayload = () => ({ id: form.id, version: form.version, title: form.title, content: form.content, important: form.important, audienceType: form.audienceType, audienceIds: [...form.audienceIds], fileIds: files.value.map(file => file.id), expiresAt: form.expiresAt, revisionOf: form.revisionOf })
const states = { DRAFT: '草稿', PUBLISHED: '已发布', WITHDRAWN: '已撤回' }, time = value => value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : '—'
let disposed = false, request = 0, searchRequest = 0, readRequest = 0, saved = ''
const snapshot = () => JSON.stringify(draftPayload())
async function load() { const id = ++request; loading.value = true; error.value = ''; try { const data = await api.listPlatformNotices({ page: page.value, keyword: keyword.value, status: status.value }); if (!disposed && id === request) { rows.value = data.items; total.value = data.total; ready.value = true } } catch (e) { if (!disposed && id === request) error.value = e.message } finally { if (!disposed && id === request) loading.value = false } }
const autoQuery = createFilterTrigger(query), autoReceipts = createFilterTrigger(() => { if (readingVisible.value) { readPage.value = 1; return loadReceipts() } })
function query() { autoQuery.cancel(); page.value = 1; return load() }
function reset() { keyword.value = ''; status.value = ''; query() }
async function searchUsers(keyword = '') { const id = ++searchRequest; searching.value = true; try { const data = await api.platformNoticeOptions({ keyword, page: 1 }); if (disposed || id !== searchRequest) return; const options = new Map(users.value.filter(x => form.audienceIds.includes(x.id)).map(x => [x.id, x])); data.users.forEach(x => options.set(x.id, x)); users.value = [...options.values()]; departments.value = data.departments } catch (e) { ElMessage.error(e.message) } finally { if (id === searchRequest) searching.value = false } }
async function edit(row, revision = false) {
  try {
    const data = row ? await api.getPlatformNotice(row.id) : empty()
    files.value = data.attachments || []
    Object.assign(form, empty(), {
      id: revision ? null : data.id,
      version: revision ? null : data.version,
      title: data.title,
      content: data.content,
      important: Boolean(Number(data.important)),
      audienceType: data.audienceType,
      audienceIds: [...(data.audienceIds || [])],
      fileIds: [...(data.fileIds || [])],
      expiresAt: revision ? null : data.expiresAt ? String(data.expiresAt).replace(' ', 'T').slice(0, 19) : null,
      revisionOf: revision ? data.id : data.revisionOf
    })
    saved = snapshot()
    editing.value = true
    searchUsers()
  } catch (e) { ElMessage.error(e.message) }
}
async function save(silent = false) {
  if (saving.value || publishing.value || uploading.value) return null
  if (!form.title.trim() || !form.content.trim() || form.audienceType !== 'ALL' && !form.audienceIds.length) {
    ElMessage.warning('请填写标题、正文和接收范围')
    return null
  }
  saving.value = true
  try {
    const data = await api.savePlatformNotice(draftPayload())
    form.id = data.id
    form.version = data.version
    saved = snapshot()
    if (!silent) ElMessage.success('草稿已保存')
    load()
    return data
  } catch (e) { ElMessage.error(e.message); return null }
  finally { saving.value = false }
}
async function preparePublish() { if (!await save(true)) return; try { preview.value = await api.previewPlatformRecipients(draftPayload()); previewing.value = true } catch (e) { ElMessage.error(e.message) } }
async function publish() { if (publishing.value) return; publishing.value = true; try { await api.publishPlatformNotice(form); saved = snapshot(); previewing.value = false; editing.value = false; ElMessage.success('平台通知已发布'); load(); window.dispatchEvent(new Event('platform-notice-read')) } catch (e) { ElMessage.error(e.message) } finally { publishing.value = false } }
async function mayLeave() { if (saving.value || publishing.value || uploading.value) return false; if (!editing.value || saved === snapshot()) return true; try { await ElMessageBox.confirm('尚未保存的通知内容将丢失，确定离开吗？', '未保存的通知', { confirmButtonText: '放弃编辑', cancelButtonText: '继续编辑', type: 'warning' }); return true } catch { return false } }
async function closeEditor(done) { if (await mayLeave()) { editing.value = false; previewing.value = false; if (typeof done === 'function') done() } }
async function uploadFile({ file, onSuccess, onError }) { if (uploading.value || files.value.length >= 10) return; uploading.value = true; try { const result = await api.uploadPlatformAttachment(file); files.value.push(result); onSuccess(result) } catch (e) { ElMessage.error(e.message); onError(e) } finally { uploading.value = false } }
function removeFile(file) { files.value = files.value.filter(f => f.id !== file.id) }
async function view(row) { try { viewed.value = await api.getPlatformNotice(row.id); viewing.value = true } catch (e) { ElMessage.error(e.message) } }
async function withdraw(row) { try { const input = await ElMessageBox.prompt('撤回后停止登录必读提示，已产生的浏览记录继续保留。请填写原因：', '撤回通知', { inputValidator: v => Boolean(v?.trim()) || '请填写撤回原因', inputType: 'textarea' }); await api.withdrawPlatformNotice(row, input.value); ElMessage.success('通知已撤回'); load() } catch (e) { if (e !== 'cancel' && e !== 'close') ElMessage.error(e.message) } }
async function receipts(row) { autoReceipts.cancel(); selected.value = row; readPage.value = 1; readKeyword.value = ''; readState.value = ''; readingVisible.value = true; loadReceipts() }
async function loadReceipts() { const id = ++readRequest; readLoading.value = true; try { const data = await api.platformNoticeReceipts(selected.value.id, { page: readPage.value, keyword: readKeyword.value, state: readState.value }); if (disposed || id !== readRequest) return; readRows.value = data.items; readTotal.value = data.total; readSummary.value = data.summary } catch (e) { ElMessage.error(e.message) } finally { if (id === readRequest) readLoading.value = false } }
async function showEvents(row) { try { eventRows.value = await api.platformNoticeEvents(selected.value.id, row.userId); eventsVisible.value = true } catch (e) { ElMessage.error(e.message) } }
async function exportReads() { if (exporting.value) return; exporting.value = true; try { const blob = await api.exportPlatformNoticeReceipts(selected.value.id), url = URL.createObjectURL(blob), link = document.createElement('a'); link.href = url; link.download = '平台通知浏览记录.xlsx'; link.click(); setTimeout(() => URL.revokeObjectURL(url), 1000) } catch (e) { ElMessage.error(e.message) } finally { exporting.value = false } }
onBeforeRouteLeave(mayLeave)
onMounted(load)
onUnmounted(() => { autoQuery.cancel(); autoReceipts.cancel(); disposed = true; request++; readRequest++; searchRequest++ })
</script>
<style scoped>
.admin-notices{height:100%;overflow:auto;box-sizing:border-box;padding:24px;background:#f3f6fa}.page-head{display:flex;justify-content:space-between;gap:20px;align-items:flex-start;margin-bottom:18px}h2{margin:0 0 8px}.page-head p,.muted{color:#7b8794;font-size:13px}.panel{background:white;border:1px solid #e5eaf2;border-radius:8px;padding:18px}.filters{display:flex;flex-wrap:wrap;gap:10px;margin-bottom:16px}.filters>.el-input{width:260px}.filters>.el-select{width:160px}.pager{display:flex;justify-content:flex-end;margin-top:18px}.title-link{white-space:normal;text-align:left;height:auto;line-height:1.6;overflow-wrap:anywhere}.form-grid{display:grid;grid-template-columns:1fr 1fr;gap:15px}.el-form-item .el-select{width:100%}.file{display:flex;gap:12px;align-items:center;overflow-wrap:anywhere}.preview-body{white-space:pre-wrap;line-height:1.9;max-height:48vh;overflow:auto;padding:18px;background:#f6f8fb;overflow-wrap:anywhere}:deep(.el-dialog){max-width:calc(100vw - 32px)}:deep(.el-dialog__body){max-height:72vh;overflow:auto}@media(max-width:700px){.admin-notices{padding:14px}.form-grid{grid-template-columns:1fr}.page-head{flex-wrap:wrap}}
</style>
