<template>
  <el-dialog v-model="visible" :title="`管理员${noun}移交`" width="720px" class="transfer-dialog"
    :close-on-click-modal="false" :close-on-press-escape="!saving" :show-close="!saving" :before-close="close">
    <div ref="contentRef" v-loading="loading">
      <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />
      <el-alert :title="summary" type="info" :closable="false" />
      <p v-if="targetType === 'PERFORMANCE'" class="transfer-flow-note">绩效仍按“专业群审核→双高办归档”处理；归口部门审核人配置供对应核心指标的归口审核使用。</p>
      <el-button v-if="!options && !loading" @click="loadOptions">重新加载选项</el-button>
      <el-form v-if="options && !preview" ref="formRef" :model="form" :rules="rules" label-position="top" :disabled="saving">
        <div class="transfer-fields"><el-form-item label="目标归口部门">
            <el-input class="picker-input" :model-value="deptLabel" readonly placeholder="尚未选择目标归口部门" aria-label="目标归口部门">
              <template #append><el-button :disabled="saving" @click="openDeptPicker">选择</el-button></template>
            </el-input>
          </el-form-item>
          <el-form-item label="目标归口部门审核人" prop="principalId">
            <el-input class="picker-input" :model-value="chosenLabel.principalId" readonly placeholder="尚未选择目标归口部门审核人" aria-label="目标归口部门审核人">
              <template #append><el-button :disabled="saving" @click.stop="openPicker('principalId', '目标归口部门审核人')">选择</el-button></template>
            </el-input>
          </el-form-item>
          <el-form-item label="目标专业群审核人" prop="auditorId">
            <el-input class="picker-input" :model-value="chosenLabel.auditorId" readonly placeholder="尚未选择目标专业群审核人" aria-label="目标专业群审核人">
              <template #append><el-button :disabled="saving" @click.stop="openPicker('auditorId', '目标专业群审核人')">选择</el-button></template>
            </el-input>
          </el-form-item>
          <el-form-item :label="leaderLabel" prop="leaderId">
            <el-input class="picker-input" :model-value="chosenLabel.leaderId" readonly :placeholder="`尚未选择${leaderLabel}`" :aria-label="leaderLabel">
              <template #append><el-button :disabled="saving" @click.stop="openPicker('leaderId', leaderLabel)">选择</el-button></template>
            </el-input>
          </el-form-item>
          <el-form-item label="移交原因" prop="reason" class="full-width">
            <el-input v-model="form.reason" type="textarea" :rows="3" maxlength="200" show-word-limit
              aria-label="移交原因" placeholder="请说明移交原因，例如人员调整或岗位变更" />
          </el-form-item>
        </div>
      </el-form>
      <el-alert v-if="options && !preview && hasPrefill && !pendingChange" class="transfer-keep" type="info" :closable="false"
        title="已带出当前归口部门及三类人员；各岗位独立配置，只修改需要调整的项。" />
      <el-alert v-if="duplicatePeople" :title="duplicatePeople" type="warning" :closable="false" show-icon class="transfer-keep" />
      <el-alert v-if="deptConflict" title="请选择目标归口部门，确认预览后统一移交。" type="warning" :closable="false" show-icon class="transfer-keep" />
      <div v-if="preview" class="transfer-preview">
        <el-alert title="将按下表变更归属，请核对后确认提交" type="info" :closable="false" />
        <el-descriptions :column="1" border>
          <el-descriptions-item v-for="row in previewRows" :key="row.label" :label="row.label">
            {{ row.before }} → {{ row.after }}
          </el-descriptions-item>
        </el-descriptions>
        <p>
          共 {{ rows.length }} 条{{ noun }}：归口部门、负责人、专业群审核人与归口部门审核人按预览独立变更，部门完成率归属、可见范围与填报权限随之调整。存在审核中单据的记录会被整批拒绝，不会部分成功。
        </p>
        <p class="transfer-reason">移交原因：{{ form.reason.trim() }}</p>
      </div>
    </div>
    <template #footer>
      <el-button :disabled="saving" @click="close">取消</el-button>
      <el-button v-if="preview" :disabled="saving" @click="preview = false">返回修改</el-button>
      <el-button v-if="preview" type="primary" :loading="saving" @click="save">确认提交</el-button>
      <el-button v-else type="primary" :disabled="loading || !options || !pendingChange || deptConflict" @click="review">预览并确认</el-button>
    </template>
  </el-dialog>
  <el-dialog v-model="picker.visible" :title="picker.title" width="min(560px, calc(100vw - 32px))" append-to-body :close-on-click-modal="false">
    <el-input class="picker-chosen" :model-value="picker.selected?.label || ''" readonly :placeholder="picker.mode === 'dept' ? '尚未选择部门' : '尚未选择人员'" aria-label="已选" />
    <el-input v-model="picker.keyword" clearable :placeholder="picker.mode === 'dept' ? '按部门名称搜索' : '按姓名或工号搜索'" @input="onPickerSearch" />
    <el-table :data="picker.rows" height="320" v-loading="picker.loading" :row-key="row => row.value" :row-class-name="pickerRowClass" class="picker-table" :empty-text="picker.mode === 'dept' ? '没有匹配的部门' : '没有匹配的人员'" @row-click="row => picker.selected = row">
      <el-table-column prop="label" :label="picker.mode === 'dept' ? '部门' : '人员'" min-width="260" />
    </el-table>
    <div v-if="picker.mode !== 'dept' && picker.total > picker.page * 50" class="picker-more"><el-button link type="primary" :loading="picker.loading" @click="loadPicker(picker.page + 1)">加载更多</el-button></div>
    <template #footer><el-button @click="picker.visible = false">取消</el-button><el-button type="primary" :disabled="!picker.selected" @click="confirmPicker">确定</el-button></template>
  </el-dialog>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, ref } from 'vue';
import { onBeforeRouteLeave } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { get } from '@/utils/request';
import { transferPerformances, transferTasks } from '@/api/transfer';

const props = defineProps({
  targetType: { type: String, default: 'TASK' },
  items: { type: Array, default: () => [] }
});
const emit = defineEmits(['transferred']);

const visible = ref(false), loading = ref(false), saving = ref(false), preview = ref(false), error = ref('');
const formRef = ref(null), contentRef = ref(null), options = ref(null);
const form = ref({ deptId:null, leaderId: null, auditorId: null, principalId: null, reason: '' });
const chosenLabel = ref({ leaderId: '', auditorId: '', principalId: '' });
const picker = ref({ visible: false, mode: 'user', title: '', field: '', keyword: '', rows: [], loading: false, page: 1, total: 0, selected: null });
let pickerTimer, pickerSeq = 0;
let disposed = false, requestVersion = 0, initial = '';

const noun = computed(() => props.targetType === 'PERFORMANCE' ? '绩效指标' : '任务');
const rows = computed(() => props.items || []);
const leaderLabel = computed(() => props.targetType === 'TASK' ? '目标专业群负责人（填报人）' : '目标负责人（填报人）');
const summary = computed(() => rows.value.length > 1
  ? `本次将对 ${rows.value.length} 条${noun.value}统一变更归属，提交前请先预览。`
  : `将变更${noun.value}「${rows.value[0]?.name || '—'}」的归属，提交前请先预览。`);
// 目标归口部门改为输入框展示：优先用已加载的部门选项，其次用记录上的名称快照
const deptLabel = computed(() => {
  const id = form.value.deptId;
  if (id == null) return '';
  const fromOptions = options.value?.departments?.find(d => String(d.deptId) === String(id))?.deptName;
  if (fromOptions) return fromOptions;
  return commonValue('deptId') !== null && String(commonValue('deptId')) === String(id) ? (rows.value[0]?.deptName || '') : '';
});
const rules = {
  leaderId: [{ required: true, type: 'number', message: '请选择目标责任人', trigger: 'change' }],
  auditorId: [{ validator: (_rule, value, done) => { if (typeof value === 'number' || canKeepReviewers.value) done(); else done(new Error('请选择目标专业群审核人')); }, trigger: 'change' }],
  principalId: [{ required: true, type: 'number', message: '请选择目标归口部门审核人', trigger: 'change' }],
  reason: [{ required: true, whitespace: true, message: '请填写移交原因', trigger: 'blur' }]
};
// 多条任务当前值一致时直接带出该值，只有需要调整的字段才重新选择
const commonValue = (key) => {
  if (!rows.value.length) return null;
  const first = rows.value[0][key];
  if (first === null || first === undefined) return null;
  return rows.value.every(row => String(row[key]) === String(first)) ? first : null;
};
const canKeepReviewers = computed(() => rows.value.length > 1 && rows.value.every(row => row.auditorId != null));
const hasPrefill = computed(() => ['deptId', 'leaderId', 'auditorId', 'principalId']
  .some(key => commonValue(key) !== null));
const pendingChange = computed(() => ['deptId', 'leaderId', 'auditorId', 'principalId']
  .some(key => String(form.value[key] ?? '') !== String(commonValue(key) ?? '')));
// 优先使用所选部门；批量记录当前部门不一致时，必须先明确选择目标部门。
const keptDeptId = computed(() => form.value.deptId ?? commonValue('deptId'));
const deptConflict = computed(() => keptDeptId.value == null);
const userLabel = item => `${item.nickName || '未命名'}（${item.userId}）`;
const deptName=id=>options.value?.departments?.find(d=>String(d.deptId)===String(id))?.deptName||'—';
const userName = id => {
  const user = options.value?.users?.find(item => item.userId === id);
  return user ? userLabel(user) : id == null ? '—' : `账号（${id}）`;
};
const previewRows = computed(() => {
  // 变更前取值：多条任务当前值不一致时说明清楚，避免按第一条误导
  const beforeLabel = (key, nameKey) => {
    if (commonValue(key) !== null) return rows.value[0]?.[nameKey] || (key === 'deptId' ? deptName(commonValue(key)) : userName(commonValue(key)));
    return rows.value.length > 1 ? '多个任务当前值不一致' : '—';
  };
  return [
    {label:'归口部门',before:beforeLabel('deptId','deptName'),after:deptName(keptDeptId.value)},
    { label: props.targetType === 'TASK' ? '专业群负责人（填报人）' : '负责人（填报人）', before: beforeLabel('leaderId', 'leaderName'), after: userName(form.value.leaderId) },
    { label: '专业群审核人', before: beforeLabel('auditorId', 'auditorName'), after: form.value.auditorId == null && canKeepReviewers.value ? '保留各记录当前审核人' : userName(form.value.auditorId) },
    { label: '归口部门审核人', before: beforeLabel('principalId', 'principalName'), after: userName(form.value.principalId) }
  ];
});

const duplicatePeople = computed(() => {
  const roles = [[form.value.leaderId, props.targetType === 'TASK' ? '专业群负责人' : '负责人'], [form.value.auditorId, '专业群审核人'], [form.value.principalId, '归口部门审核人']];
  const grouped = new Map();
  for (const [id, role] of roles) if (id != null) grouped.set(String(id), [...(grouped.get(String(id)) || []), role]);
  const duplicates = [...grouped].filter(([, names]) => names.length > 1);
  return duplicates.length ? `同一账号兼任${duplicates.map(([id, names]) => `${names.join('、')}（${userName(Number(id))}）`).join('；')}，请核对是否符合实际分工。` : '';
});

// 人员选择：点击按钮打开弹窗，搜索勾选后回填到对应字段
let candidateCache = null;
function pickerRowClass({ row }) { return picker.value.selected && String(picker.value.selected.value) === String(row.value) ? 'row-picked' : ''; }
// 打开移交对话框时预取第一页人员，弹窗打开即可直接选择，避免等待
async function prefetchCandidates() {
  if (candidateCache) return;
  try {
    const result = await get('/biz/tasks/manage/candidates', { domain: 'users', keyword: '', page: 1, pageSize: 50 });
    candidateCache = { rows: result.items || [], page: result.page || 1, total: result.total || 0 };
  } catch { /* 预取失败不提示，弹窗内会重试 */ }
}
function openPicker(field, title) {
  clearTimeout(pickerTimer); pickerSeq++;
  // 打开时带上该岗位当前已选人员，方便核对与改选
  const current = form.value[field];
  const cached = (candidateCache?.rows || []).find(row => String(row.value) === String(current));
  picker.value = {
    visible: true, mode: 'user', title: `选择${title}`, field, keyword: '',
    rows: candidateCache?.rows || [], loading: false,
    page: candidateCache?.page || 1, total: candidateCache?.total || 0,
    selected: current == null ? null : (cached || { value: current, label: chosenLabel.value[field] || `账号（${current}）` })
  };
  if (!candidateCache) loadPicker(1);
}
function schedulePickerSearch() { clearTimeout(pickerTimer); pickerSeq++; pickerTimer = setTimeout(() => loadPicker(1), 250); }
// 部门候选：本地过滤选项即可，不分页、不走后端搜索
function deptCandidates(keyword) {
  const kw = (keyword || '').trim().toLowerCase();
  return (options.value?.departments || [])
    .filter(d => !kw || String(d.deptName || '').toLowerCase().includes(kw) || String(d.deptId).includes(kw))
    .map(d => ({ value: d.deptId, label: d.deptName }));
}
function openDeptPicker() {
  clearTimeout(pickerTimer); pickerSeq++;
  const list = deptCandidates('');
  picker.value = {
    visible: true, mode: 'dept', title: '选择目标归口部门', field: 'deptId', keyword: '',
    rows: list, loading: false, page: 1, total: list.length,
    selected: list.find(row => String(row.value) === String(form.value.deptId)) || null
  };
}
function onPickerSearch() {
  if (picker.value.mode !== 'dept') { schedulePickerSearch(); return; }
  const list = deptCandidates(picker.value.keyword);
  picker.value.rows = list;
  picker.value.total = list.length;
}
async function loadPicker(page = 1) {
  const seq = ++pickerSeq;
  picker.value.loading = true;
  try {
    const result = await get('/biz/tasks/manage/candidates', { domain: 'users', keyword: picker.value.keyword, page, pageSize: 50 });
    if (disposed || seq !== pickerSeq || !picker.value.visible || picker.value.mode !== 'user') return;
    const merged = new Map((page > 1 ? picker.value.rows : []).map(item => [String(item.value), item]));
    for (const item of [...(result.items || []), ...(result.selected || [])]) merged.set(String(item.value), item);
    picker.value.rows = [...merged.values()];
    picker.value.page = result.page || page;
    picker.value.total = result.total || 0;
    if (!picker.value.keyword && page === 1) candidateCache = { rows: picker.value.rows, page: picker.value.page, total: picker.value.total };
  } catch (e) {
    if (!disposed && seq === pickerSeq && picker.value.visible && picker.value.mode === 'user') ElMessage.error(e.message || '人员加载失败');
  } finally {
    if (seq === pickerSeq) picker.value.loading = false;
  }
}
function confirmPicker() {
  const item = picker.value.selected;
  if (!item) return;
  const field = picker.value.field;
  form.value[field] = Number(item.value);
  if (field === 'deptId') {
    picker.value.visible = false;
    return;
  }
  chosenLabel.value[field] = item.label;
  picker.value.visible = false;
  // 只读输入框不会触发 change，选中后主动重跑该字段校验，避免旧的红色提示残留
  nextTick(() => formRef.value?.validateField(field).catch(() => {}));
}
async function loadOptions() {
  const version = ++requestVersion;
  loading.value = true; error.value = '';
  try {
    const data = await get('/biz/tasks/manage/options', { separateOptions: true });
    if (!disposed && version === requestVersion) options.value = data;
  } catch (e) {
    if (!disposed && version === requestVersion) error.value = e.message || '选项加载失败';
  } finally {
    if (!disposed && version === requestVersion) loading.value = false;
  }
}

async function open() {
  preview.value = false; options.value = null; error.value = ''; visible.value = true;
  // 等父组件把选中记录传进来，否则读不到当前值（默认值会全部为空）
  await nextTick();
  // 默认带出当前值：单条即该记录的值，多条取各字段一致的值，不一致的字段留空由人工选择
  form.value = {
    deptId: commonValue('deptId'),
    leaderId: commonValue('leaderId'),
    auditorId: commonValue('auditorId'),
    principalId: commonValue('principalId'),
    reason: ''
  };
  // 带出当前人员姓名：优先用记录上的名称快照，其次回查选项
  const nameOf = (key, nameKey) => commonValue(key) === null ? '' : (rows.value[0]?.[nameKey] || userName(commonValue(key)));
  chosenLabel.value = { leaderId: nameOf('leaderId', 'leaderName'), auditorId: nameOf('auditorId', 'auditorName'), principalId: nameOf('principalId', 'principalName') };
  initial = JSON.stringify(form.value);
  loadOptions();
  prefetchCandidates();
}

async function close() {
  if (saving.value) return false;
  if (initial !== JSON.stringify(form.value)) {
    try {
      await ElMessageBox.confirm('移交信息尚未提交，确定关闭？', '离开确认',
        { type: 'warning', confirmButtonText: '放弃填写', cancelButtonText: '继续修改' });
    } catch {
      return false;
    }
  }
  requestVersion++; visible.value = false;
  return true;
}

async function review() {
  if (!await formRef.value?.validate().catch(() => false)) return;
  error.value = ''; preview.value = true;
  await nextTick(); contentRef.value?.parentElement?.scrollTo({ top: 0 });
}

async function save() {
  if (saving.value) return;
  saving.value = true; error.value = '';
  try {
    const payload = {
      ids: rows.value.map(row => row.id),
      deptId: keptDeptId.value,
      leaderId: form.value.leaderId,
      auditorId: form.value.auditorId,
      principalId: form.value.principalId,
      reason: form.value.reason.trim(),
      expected: rows.value.map(row => ({ id: row.id, deptId: row.deptId, leaderId: row.leaderId, auditorId: row.auditorId, principalId: row.principalId }))
    };
    const result = await (props.targetType === 'PERFORMANCE'
      ? transferPerformances(payload) : transferTasks(payload));
    if (disposed) return;
    visible.value = false;
    ElMessage.success(`已移交 ${result?.updated ?? payload.ids.length} 条${noun.value}`);
    emit('transferred', result);
  } catch (e) {
    if (!disposed) {
      error.value = e.message || '移交失败，请重试';
      await nextTick(); contentRef.value?.parentElement?.scrollTo({ top: 0 });
    }
  } finally {
    if (!disposed) saving.value = false;
  }
}

onBeforeUnmount(() => { disposed = true; requestVersion++; pickerSeq++; clearTimeout(pickerTimer); });
onBeforeRouteLeave(() => visible.value ? close() : true);
defineExpose({ open });
</script>

<style scoped>
.transfer-fields { display: grid; grid-template-columns: minmax(0, 1fr); gap: 0; margin-top: 12px; }
.transfer-fields .full-width { grid-column: 1 / -1; }
.transfer-fields .el-select { width: 100%; }
.transfer-fields :deep(.el-form-item) { margin-bottom: 16px; }
.picker-input { width: 100%; }
.picker-input :deep(.el-input__inner) { cursor: default; }
.picker-input :deep(.el-input-group__append) { padding: 0 14px; }
.locked-input :deep(.el-input__wrapper) { background: #f5f7fa; }
.locked-input :deep(.el-input__inner) { color: #2b3a4d; }
.locked-input :deep(.el-input__inner::placeholder) { color: #a8b2c0; }
.transfer-fields .el-form-item:has(.picker-input) { margin-bottom: 12px; }
.picker-chosen { margin-bottom: 10px; }
.picker-table { margin-top: 12px; }
.picker-table :deep(.row-picked) { background: #e8f2ff; }
.picker-table :deep(.row-picked td) { color: #1f6feb; font-weight: 600; }
.picker-selected { margin-top: 10px; font-size: 13px; color: #3b4a5f; }
.picker-more { margin-top: 8px; text-align: center; }
.transfer-preview { margin-top: 12px; overflow-wrap: anywhere; }
.transfer-preview p { color: #606266; line-height: 1.7; }
.transfer-reason { color: #303133; }
.transfer-flow-note { color: #606266; font-size: 13px; line-height: 1.6; }
@media (max-width: 600px) { .transfer-fields { grid-template-columns: minmax(0, 1fr); } }
</style>
<style>
.transfer-dialog { max-width: calc(100vw - 32px); }
.transfer-dialog .el-dialog__body { max-height: 65vh; overflow-y: auto; }
</style>
