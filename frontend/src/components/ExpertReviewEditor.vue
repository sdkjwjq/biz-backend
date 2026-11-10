<template>
  <section v-if="allowed" ref="section" class="expert-review-section" data-detail-section="专家评语">
    <div class="section-heading"><div><h4>专家评语</h4><p v-if="detail" class="review-identity">{{detail.round?.name||`${detail.year}年度`}} · {{detail.expertName||accessState.currentUser?.nickName||'本人'}} <el-tag v-if="detail.delegated" type="warning" size="small">管理员代填</el-tag></p></div><el-button link type="primary" @click="showTask">查看任务完成情况 ↑</el-button></div>
    <p v-if="loading">正在读取评语…</p>
    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />
    <template v-if="detail">
      <el-alert v-if="detail.scope.partial" title="本次总体评语仅针对下方获准任务，不代表整个一级任务。" type="warning" :closable="false" show-icon />
      <el-alert v-if="detail.draftRestricted" title="原稿涉及当前无权查阅的任务，已隐藏；可按当前范围重新填写。" type="warning" :closable="false" />
      <el-alert v-if="detail.otherYearDraft" :title="`已有${detail.draftYear}年度草稿；保存将替换该草稿，正式历史保留。`" type="warning" :closable="false" />
      <div class="review-result-row"><span class="review-result-label">评审结果</span><el-radio-group v-model="reviewResult" :disabled="busy || !writable"><el-radio value="pass" class="review-result-pass">通过</el-radio><el-radio value="reject" class="review-result-reject">不通过</el-radio></el-radio-group></div>
      <el-form class="review-form" label-position="top" @submit.prevent><el-form-item :label="writable ? '填写评语' : '当前仅有查阅权限'">
        <el-input ref="input" :model-value="text" @update:model-value="text = [...$event].slice(0, 2000).join('')" type="textarea" :rows="7" aria-label="专家评语正文" :disabled="busy || !writable" placeholder="请结合上方任务完成情况填写评语" />
        <div class="word-count">{{ [...text].length }}/2000 字</div>
      </el-form-item></el-form>

    </template>
    <div class="review-footer"><p class="save-hint"><span :class="{ unsaved: dirty }">{{ dirty ? '有未保存的修改' : '保存草稿后可继续填写' }}</span><span>正式提交后保留修订记录</span></p><div class="review-actions"><el-button :disabled="busy || loading" @click="reload">重新加载</el-button><el-button v-if="writable" :disabled="busy || loading" :loading="operation === 'draft'" @click="save(false)">保存草稿</el-button><el-button v-if="writable" type="primary" :disabled="busy || loading || !text.trim() || (text === detail.formal?.text && reviewResult === (detail.formal?.result || ''))" :loading="operation === 'submit'" @click="save(true)">提交评语</el-button></div></div>
    <div v-if="detail" class="review-reference"><div class="reference-heading">评审参考与记录</div>
      <el-collapse><el-collapse-item :title="`本次评审范围（${detail.scope.tasks.length} 条三级任务）`" name="scope">
        <p class="muted">提交时保存实际获准范围，列表搜索和部门筛选不改变此范围。</p>
        <el-table :data="detail.scope.tasks" max-height="230" size="small"><el-table-column prop="taskCode" label="三级任务编号" width="145" /><el-table-column prop="taskName" label="三级任务名称" min-width="260" /></el-table>
      </el-collapse-item></el-collapse>
      <div v-if="detail.formal" class="formal"><strong>当前正式意见 · 第 {{ detail.formal.revisionNo }} 版</strong> <el-tag size="small" :type="resultType(detail.formal.result)">{{ resultLabel(detail.formal.result) }}</el-tag><pre>{{ detail.formal.text }}</pre><p class="muted">{{ time(detail.formal.createTime) }} · {{ detail.formal.delegated ? `管理员 ${detail.formal.operatorName} 代填` : `${detail.formal.expertName} 提交` }}</p></div>
      <el-collapse v-model="historyExpanded" @change="loadHistory"><el-collapse-item title="正式提交与修订历史" name="history"><div v-loading="historyLoading">
        <el-alert v-if="historyError" :title="historyError" type="warning" :closable="false" />
        <el-empty v-else-if="!historyLoading && !historyRows.length" description="暂无正式提交历史" />
        <article v-for="row in historyRows" :key="row.revisionId" class="formal"><strong>第 {{ row.revisionNo }} 版 · {{ row.year }}年度</strong> <el-tag size="small" :type="resultType(row.result)">{{ resultLabel(row.result) }}</el-tag><pre>{{ row.text }}</pre><p class="muted">{{ time(row.createTime) }} · {{ row.delegated ? `管理员 ${row.operatorName} 代填` : `${row.expertName} 提交` }}</p><el-collapse><el-collapse-item :title="`提交时范围（${row.scope.tasks.length} 条）`"><p v-for="task in row.scope.tasks" :key="task.taskId">{{ task.taskCode }} {{ task.taskName }}</p></el-collapse-item></el-collapse></article>
      </div></el-collapse-item></el-collapse>
    </div>
  </section>
</template>

<script setup>
import { computed, nextTick, onUnmounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { accessState, hasPermission } from '@/utils/accessState'
import {loadRoundReview,saveRoundReview,normalizeRoundReview} from '@/api/roundReviews'
import { getExpertDetail, getExpertHistory, saveExpertDraft, submitExpertOpinion } from '@/api/expertReviews'
// 评审结果：pass 通过 / reject 不通过 / 空 未选择
const reviewResult = ref(''), savedResult=ref('')
const props = defineProps({ roundId:{type:[Number,String],default:null},readOnly:Boolean,historyMode:Boolean,initialDetail:{type:Object,default:null},taskId: { type: [Number, String], required: true }, year: { type: [Number, String], required: true }, expertId: { type: [Number, String], default: null } })
const emit = defineEmits(['saved','reload-requested','access-lost'])
const allowed = computed(() => !!props.roundId || hasPermission('expert.read'))
const section = ref(null), input = ref(null), detail = ref(null), text = ref(''), savedText = ref(''), loading = ref(false), error = ref(''), operation = ref('')
const busy = computed(() => !!operation.value),clock=ref(Date.now())
const writable=computed(()=>!!detail.value?.canWrite&&!props.readOnly&&(!props.roundId||accessState.isAdmin||clock.value<new Date(detail.value.round.endsAt).getTime()&&clock.value>=new Date(detail.value.round.startsAt).getTime()))
const dirty=computed(()=>!!detail.value&&(text.value!==savedText.value||reviewResult.value!==savedResult.value))
const expiryTimer=setInterval(()=>clock.value=Date.now(),1000)

const historyExpanded = ref([]), historyRows = ref([]), historyLoading = ref(false), historyError = ref('')
let sequence = 0, historySequence = 0, controller, historyController, disposed = false, pendingFocus = false
const author = () => accessState.isAdmin && props.expertId ? { expertId: Number(props.expertId) } : {}
const time = value => value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : '—'
const resultLabel = value => ({pass:'通过',reject:'不通过'})[value] || '未记录结论'
const resultType = value => ({pass:'success',reject:'danger'})[value] || 'info'
const showTask = () => section.value?.closest('.el-drawer__body')?.scrollTo({ top: 0, behavior: 'smooth' })
async function confirmLeave() {
  if (busy.value) return false
  if (!dirty.value) return true
  try { await ElMessageBox.confirm('评语有未保存的修改，确认放弃？', '未保存的评语', { confirmButtonText: '放弃修改', cancelButtonText: '继续填写', type: 'warning' }); return true } catch { return false }
}
async function focusEditor() {
  pendingFocus = true
  await nextTick()
  const element = section.value, body = element?.closest('.el-drawer__body')
  if (!body) return
  const destination = element
  body.scrollTo({ top: body.scrollTop + destination.getBoundingClientRect().top - body.getBoundingClientRect().top - 20, behavior: 'auto' })
  if (detail.value && !loading.value) { input.value?.textarea?.focus({ preventScroll: true }); pendingFocus = false }
}
async function load() {
  const seq = ++sequence
  controller?.abort(); historyController?.abort(); historySequence++
  detail.value = null; error.value = ''; text.value = ''; savedText.value = ''; historyRows.value = []; historyExpanded.value = []; historyLoading.value = false
  if (!allowed.value) { loading.value = false; return }
  controller = new AbortController(); loading.value = true
  try {
    const result = props.roundId?(props.initialDetail?normalizeRoundReview(props.initialDetail):await loadRoundReview(props.roundId,props.taskId,props.historyMode,{signal:controller.signal})):await getExpertDetail(props.taskId, { year: props.year, ...author() }, { signal: controller.signal })
    if (disposed || seq !== sequence) return
    detail.value = result; text.value = result.draftText || ''; savedText.value = text.value; reviewResult.value = result.result || '';savedResult.value=reviewResult.value
  } catch (e) { if (seq === sequence && e.name !== 'AbortError') error.value = e.message }
  finally { if (seq === sequence) { loading.value = false; if (pendingFocus) await focusEditor() } }
}
async function reload() {if(props.roundId){emit('reload-requested');return}if(await confirmLeave())await load()}
async function save(submit) {
  if (busy.value || !writable.value) return
  if(submit&&(!text.value.trim()||!reviewResult.value)){ElMessage.warning('请填写评语并选择通过或不通过');return}
  operation.value = submit ? 'submit' : 'draft'; error.value = ''
  const current = detail.value, seq = sequence
  try {
    if (current.otherYearDraft) await ElMessageBox.confirm(`保存将替换${current.draftYear}年度草稿，正式历史保留。`, '确认年度变更', { confirmButtonText: '继续保存', cancelButtonText: '取消', type: 'warning', closeOnClickModal: false })
    if (submit) await ElMessageBox.confirm('提交后形成正式意见，后续修订完整留痕。', '确认提交评语', { confirmButtonText: '确认提交', cancelButtonText: '继续填写', closeOnClickModal: false })
    const body = { taskId: current.task.taskId, year: current.year, version: current.version, text: text.value, result: reviewResult.value || null, scopeToken: current.scopeToken, ...author() }
    const result=props.roundId?await saveRoundReview(props.roundId,props.taskId,{roundVersion:current.round.version,version:current.version,scopeToken:current.scopeToken,text:text.value,result:reviewResult.value||null,submit}):await (submit ? submitExpertOpinion(body) : saveExpertDraft(body))
    if (disposed || seq !== sequence) return
    detail.value = result; text.value = result.draftText || ''; savedText.value = text.value; reviewResult.value = result.result || '';savedResult.value=reviewResult.value; historyRows.value = []; historyExpanded.value = []
    emit('saved',result); ElMessage.success(submit ? '评语已正式提交' : '评语草稿已保存')
  } catch (e) { if (seq === sequence && e instanceof Error) {
    error.value = `${e.message}；当前文字仍保留。`
    if(props.roundId&&[403,404].includes(Number(e.responseCode))){detail.value={...detail.value,canWrite:false,scope:{tasks:[],partial:false},formal:null,history:[]};historyRows.value=[];emit('access-lost',e.message)}
  } }
  finally { operation.value = '' }
}
async function loadHistory() {
  if (!historyExpanded.value.includes('history') || !detail.value) return
  if(props.roundId){historyRows.value=detail.value.history||[];return}
  const seq = ++historySequence; historyController?.abort(); historyController = new AbortController(); historyLoading.value = true; historyError.value = ''
  try { const rows = await getExpertHistory(props.taskId, { year: props.year, ...author() }, { signal: historyController.signal }); if (!disposed && seq === historySequence) historyRows.value = rows }
  catch (e) { if (seq === historySequence && e.name !== 'AbortError') historyError.value = e.message }
  finally { if (seq === historySequence) historyLoading.value = false }
}
watch(() => [props.taskId, props.year, props.expertId,props.roundId,props.initialDetail,allowed.value], load, { immediate: true })
onUnmounted(() => { clearInterval(expiryTimer);disposed = true; sequence++; historySequence++; controller?.abort(); historyController?.abort() })
defineExpose({ dirty, busy, confirmLeave, focusEditor })
</script>
<style scoped>
.expert-review-section :deep(.review-result-pass) { --el-color-primary:var(--el-color-success); }
.expert-review-section :deep(.review-result-reject) { --el-color-primary:var(--el-color-danger); }
.expert-review-section :deep(.review-result-pass .el-radio__label) { color:var(--el-color-success); }
.expert-review-section :deep(.review-result-reject .el-radio__label) { color:var(--el-color-danger); }
.review-result-row { display: flex; align-items: center; gap: 12px; margin: 12px 0 4px; }.review-result-label { font-size: 14px; color: #3b4a5f; font-weight: 600; }

.expert-review-section { margin-top:24px; padding:20px; min-width:0; border:1px solid #e4e9f0; border-radius:10px; background:#fff; }
.section-heading { display:flex; justify-content:space-between; align-items:flex-start; gap:12px; flex-wrap:wrap; margin-bottom:20px; }
.section-heading h4 { margin:0; font-size:16px; line-height:24px; color:#253449; }
.review-identity { margin:6px 0 0; font-size:13px; color:#7b8794; line-height:22px; }
.review-form :deep(.el-form-item) { margin-bottom:0; }
.review-form :deep(.el-form-item__label) { font-weight:500; color:#475569; margin-bottom:8px; }
.expert-review-section :deep(.el-textarea__inner) { resize:vertical; padding:12px 14px; line-height:1.8; font-size:14px; }
.word-count { text-align:right; width:100%; font-size:12px; color:#8a94a4; line-height:24px; }
.review-footer { display:flex; align-items:center; justify-content:space-between; gap:16px; flex-wrap:wrap; padding:14px 0 20px; }
.save-hint { display:flex; flex-direction:column; gap:4px; margin:0; color:#8893a2; font-size:12px; line-height:18px; }
.save-hint > span:first-child { color:#64748b; }
.save-hint > span.unsaved { color:#b7791f; }
.review-actions { display:flex; align-items:center; gap:10px; flex-wrap:wrap; }
.review-actions :deep(.el-button) { margin-left:0; }
.review-reference { padding:14px 16px 0; background:#f7f9fc; border:1px solid #edf0f5; border-radius:8px; }
.reference-heading { font-size:12px; color:#8491a3; margin-bottom:4px; }
.review-reference :deep(.el-collapse) { border:0; --el-collapse-header-bg-color:transparent; --el-collapse-content-bg-color:transparent; }
.review-reference :deep(.el-collapse-item__header) { font-size:13px; color:#526277; min-height:42px; height:auto; line-height:22px; padding:10px 0; box-sizing:border-box; }
.review-reference :deep(.el-collapse-item__wrap) { border-bottom:0; }
.muted { color:#7b8794; font-size:13px; line-height:1.7; }
.formal { border:1px solid #e2e9f3; border-radius:6px; padding:14px; margin:12px 0; background:#fff; }
pre { font-family:inherit; white-space:pre-wrap; overflow-wrap:anywhere; line-height:1.8; }
.expert-review-section :deep(.el-alert) { margin:12px 0; }
@media (max-width:700px) {
  .expert-review-section { padding:14px; }
  .section-heading { margin-bottom:14px; }
  .review-footer { gap:12px; }
  .review-actions { width:100%; justify-content:flex-end; }
  .review-reference { padding:10px 12px 0; }
}
</style>
