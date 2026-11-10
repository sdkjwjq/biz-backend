<template>
  <el-dialog v-model="visible" title="新增三级建设任务" width="760px" class="create-task-dialog"
    :close-on-click-modal="false" :close-on-press-escape="!saving" :show-close="!saving" :before-close="close">
    <div ref="contentRef" v-loading="loading">
      <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />
      <el-button v-if="!options && !loading" @click="loadOptions">重新加载选项</el-button>
      <el-form v-if="options && !preview" ref="formRef" :model="form" :rules="rules" label-position="top" :disabled="saving">
        <div class="task-fields">
          <el-form-item label="年度" prop="phase"><el-select v-model="form.phase" aria-label="年度"><el-option v-for="year in years" :key="year" :label="`${year}年`" :value="year" /></el-select></el-form-item>
          <el-form-item label="二级目录" prop="parentId"><RemoteCandidateSelect v-model="form.parentId" endpoint="/biz/tasks/manage/candidates" :params="{domain: 'directories'}" aria-label="二级目录" @loaded="rememberDirectories" /></el-form-item>
          <el-form-item label="任务编号" prop="taskCode"><el-input v-model="form.taskCode" maxlength="64" aria-label="任务编号" /></el-form-item>
          <el-form-item label="任务名称" prop="taskName"><el-input v-model="form.taskName" maxlength="500" aria-label="任务名称" /></el-form-item>
          <el-form-item label="归口部门" prop="deptId"><el-select v-model="form.deptId" filterable aria-label="归口部门"><el-option v-for="item in options.departments" :key="item.deptId" :label="item.deptName" :value="item.deptId" /></el-select></el-form-item>
          <el-form-item v-for="field in peopleFields" :key="field.key" :label="field.label" :prop="field.key"><RemoteCandidateSelect v-model="form[field.key]" endpoint="/biz/tasks/manage/candidates" :params="{domain: 'users'}" :aria-label="field.label" @loaded="rememberUsers" /></el-form-item>
          <el-form-item label="数据类型" prop="dataType"><el-select v-model="form.dataType" aria-label="数据类型"><el-option v-for="item in dataTypes" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
          <el-form-item label="目标值" prop="targetValue"><el-input v-model="form.targetValue" inputmode="decimal" aria-label="目标值" placeholder="非负数，最多4位小数" /></el-form-item>
          <el-form-item label="预期成果级别"><el-input v-model="form.expLevel" maxlength="20" aria-label="预期成果级别" /></el-form-item>
          <el-form-item v-for="field in textFields" :key="field.key" :label="field.label" class="full-width"><el-input v-model="form[field.key]" type="textarea" :rows="2" :maxlength="field.max" show-word-limit :aria-label="field.label" /></el-form-item>
        </div>
      </el-form>
      <div v-if="preview" class="task-preview">
        <el-alert title="将新增以下三级任务，请核对后确认保存" type="info" :closable="false" />
        <el-descriptions :column="1" border>
          <el-descriptions-item v-for="item in previewRows" :key="item.label" :label="item.label">{{ item.value || '—' }}</el-descriptions-item>
        </el-descriptions>
        <p>新增前：无此任务。新增后：未开始，完成值和进度均为 0。不会自动关联绩效，也不会修改目录和已有任务。</p>
      </div>
    </div>
    <template #footer>
      <el-button :disabled="saving" @click="close">取消</el-button>
      <el-button v-if="preview" :disabled="saving" @click="preview = false">返回修改</el-button>
      <el-button v-if="preview" type="primary" :loading="saving" @click="save">确认保存</el-button>
      <el-button v-else type="primary" :disabled="loading || !options" @click="review">预览并确认</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import RemoteCandidateSelect from './RemoteCandidateSelect.vue';
import { computed, nextTick, onBeforeUnmount, ref } from 'vue';
import { onBeforeRouteLeave } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { get, post } from '@/utils/request';
const props = defineProps({ year: { type: [String, Number], default: 2026 } });
const emit = defineEmits(['created']);
const visible = ref(false), loading = ref(false), saving = ref(false), preview = ref(false), error = ref('');
const formRef = ref(null), contentRef = ref(null), form = ref({}), options = ref(null);
let disposed = false, requestVersion = 0, initial = '';
const years = [2025, 2026, 2027, 2028, 2029];
const peopleFields = [{ key: 'leaderId', label: '责任人' }, { key: 'auditorId', label: '专业群审核人' }, { key: 'principalId', label: '归口审核人' }];
const textFields = [{ key: 'comment', label: '任务描述', max: 500 }, { key: 'expTarget', label: '预期达成情况', max: 10000 }, { key: 'expEffect', label: '预期效果', max: 10000 }, { key: 'expMaterialDesc', label: '材料要求', max: 10000 }];
const dataTypes = [{ value: '0', label: '对指标无影响' }, { value: '1', label: '数值（累加）' }, { value: '2', label: '百分比（取大）' }];


const userLabel = item => `${item.nickName || '未命名'}（${item.userId}）`;
const required = label => [{ required: true, whitespace: true, message: `请填写或选择${label}`, trigger: 'change' }];
const rules = Object.fromEntries([['phase', '年度'], ['parentId', '二级目录'], ['taskCode', '任务编号'], ['taskName', '任务名称'], ['deptId', '归口部门'], ...peopleFields.map(item => [item.key, item.label]), ['dataType', '数据类型']].map(([key, label]) => [key, required(label)]));
for (const key of ['phase', 'parentId', 'deptId', ...peopleFields.map(item => item.key)]) rules[key][0].type = 'number';
rules.targetValue = [{ required: true, validator: (_rule, value, done) => done(/^(?:0|[1-9]\d{0,15})(?:\.\d{1,4})?$/.test(String(value).trim()) ? undefined : new Error('请输入非负数，最多16位整数和4位小数')), trigger: 'blur' }];
const previewRows = computed(() => [
  { label: '年度', value: form.value.phase }, { label: '二级目录', value: (options.value?.directories || []).find(item => item.taskId === form.value.parentId)?.taskName },
  { label: '任务编号', value: form.value.taskCode.trim() }, { label: '任务名称', value: form.value.taskName.trim() },
  { label: '归口部门', value: options.value?.departments.find(item => item.deptId === form.value.deptId)?.deptName },
  ...peopleFields.map(field => ({ label: field.label, value: options.value?.users.find(item => item.userId === form.value[field.key]) && userLabel(options.value.users.find(item => item.userId === form.value[field.key])) })),
  { label: '数据类型', value: dataTypes.find(item => item.value === form.value.dataType)?.label }, { label: '目标值', value: form.value.targetValue },
  { label: '预期成果级别', value: form.value.expLevel }, ...textFields.map(field => ({ label: field.label, value: form.value[field.key] }))
]);
function rememberDirectories(items) { if(options.value) options.value.directories = [...new Map([...(options.value.directories || []), ...items.map(item=>item.item).filter(Boolean)].map(item=>[item.taskId,item])).values()]; }
function rememberUsers(items) { if(options.value) options.value.users = [...new Map([...(options.value.users || []), ...items.map(item=>item.item).filter(Boolean)].map(item=>[item.userId,item])).values()]; }
async function loadOptions() {
  const version = ++requestVersion; loading.value = true; error.value = '';
  try { const data = await get('/biz/tasks/manage/options', { separateOptions: true }); if (!disposed && version === requestVersion) options.value = data; }
  catch (e) { if (!disposed && version === requestVersion) error.value = e.message || '选项加载失败'; }
  finally { if (!disposed && version === requestVersion) loading.value = false; }
}
function open() {
  form.value = { projectId: 1, level: 3, phase: years.includes(Number(props.year)) ? Number(props.year) : 2026, parentId: null, taskCode: '', taskName: '', deptId: null, leaderId: null, auditorId: null, principalId: null, dataType: '', targetValue: '', comment: '', expLevel: '', expTarget: '', expEffect: '', expMaterialDesc: '' };
  initial = JSON.stringify(form.value); preview.value = false; options.value = null; error.value = ''; visible.value = true; loadOptions();
}
async function close() {
  if (saving.value) return false;
  if (initial !== JSON.stringify(form.value)) {
    try { await ElMessageBox.confirm('填写内容尚未保存，确定关闭？', '离开确认', { type: 'warning', confirmButtonText: '放弃填写', cancelButtonText: '继续填写' }); } catch { return false; }
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
    const result = await post('/biz/tasks/manage/add?returnDetail=true', { ...form.value, taskCode: form.value.taskCode.trim(), taskName: form.value.taskName.trim(), targetValue: String(form.value.targetValue).trim() });
    if (disposed) return;
    visible.value = false; ElMessage.success('任务添加成功'); emit('created', result.taskId);
  } catch (e) {
    if (!disposed) { error.value = e.message || '新增任务失败，请重试'; await nextTick(); contentRef.value?.parentElement?.scrollTo({ top: 0 }); }
  }
  finally { if (!disposed) saving.value = false; }
}
onBeforeUnmount(() => { disposed = true; requestVersion++; });
onBeforeRouteLeave(() => visible.value ? close() : true);
defineExpose({ open });
</script>

<style scoped>
.task-fields { display: grid; grid-template-columns: minmax(0, 1fr) minmax(0, 1fr); gap: 0 20px; }
.full-width { grid-column: 1 / -1; }
.task-fields .el-select { width: 100%; }
.task-preview { overflow-wrap: anywhere; }
.task-preview p { color: #606266; line-height: 1.6; }
@media (max-width: 600px) { .task-fields { grid-template-columns: minmax(0, 1fr); } }
</style>
<style>
.create-task-dialog { max-width: calc(100vw - 32px); }
.create-task-dialog .el-dialog__body { max-height: 65vh; overflow-y: auto; }
</style>
