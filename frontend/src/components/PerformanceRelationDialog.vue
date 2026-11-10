<template>
  <el-dialog v-model="visible" title="编辑关联任务" width="760px" class="relation-dialog"
    :close-on-click-modal="false" :close-on-press-escape="!saving" :show-close="!saving" :before-close="close">
    <div ref="contentRef" v-loading="loading">
      <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />
      <el-alert :title="summary" type="info" :closable="false" />
      <el-button v-if="!loaded && !loading" @click="loadCandidates">重新加载任务</el-button>

      <!-- 第一步：勾选需要关联的任务 -->
      <template v-if="loaded && step === 'pick'">
        <div class="pick-toolbar">
          <el-input v-model="search" clearable placeholder="搜索任务编号或名称" class="pick-search" />
          <el-select v-model="linkedFilter" style="width: 130px">
            <el-option label="全部任务" value="all" />
            <el-option label="已关联" value="linked" />
            <el-option label="未关联" value="unlinked" />
          </el-select>
          <el-checkbox :model-value="allPicked" :indeterminate="partPicked" :disabled="!filteredRows.length"
            @change="toggleAll">全选当前 {{ filteredRows.length }} 条</el-checkbox>
        </div>
        <div class="pick-list">
          <el-checkbox-group v-model="checked">
            <el-checkbox v-for="item in filteredRows" :key="item.taskId" :value="item.taskId" class="pick-item">
              <span v-if="item.taskCode" class="pick-code">{{ `[${item.taskCode}]` }}</span>{{ item.taskName }}
              <el-tag v-if="item.linked && !checked.includes(item.taskId)" size="small" type="danger" effect="plain"
                class="pick-tag">待解除</el-tag>
              <el-tag v-else-if="!item.linked && checked.includes(item.taskId)" size="small" type="primary"
                effect="plain" class="pick-tag">待新增</el-tag>
              <el-tag v-else-if="item.linked" size="small" type="success" effect="plain" class="pick-tag">已关联</el-tag>
              <span v-if="item.skipReason" class="pick-note">{{ item.skipReason }}</span>
            </el-checkbox>
          </el-checkbox-group>
          <p v-if="!filteredRows.length" class="pick-empty">没有符合条件的任务</p>
        </div>
      </template>

      <!-- 第二步：预览与变更原因 -->
      <div v-if="loaded && step === 'preview'" class="relation-preview">
        <el-alert :title="previewLead" type="info" :closable="false" />
        <div class="preview-block" v-if="addedRows.length">
          <div class="preview-title">新增关联 {{ addedRows.length }} 条</div>
          <div v-for="item in addedRows" :key="`add-${item.taskId}`" class="preview-row">
            <span class="pick-code">{{ item.taskCode ? `[${item.taskCode}]` : '' }}</span>{{ item.taskName }}
          </div>
        </div>
        <div class="preview-block" v-if="removedRows.length">
          <div class="preview-title">解除关联 {{ removedRows.length }} 条</div>
          <div v-for="item in removedRows" :key="`remove-${item.taskId}`" class="preview-row">
            <span class="pick-code">{{ item.taskCode ? `[${item.taskCode}]` : '' }}</span>{{ item.taskName }}
          </div>
        </div>
        <p class="preview-note">
          保存后会立即重算该指标的完成值；只有三级、未删除、数据类型不为 0 且年度与指标年度一致的任务会计入汇总。
          解除关联采用软删除留痕，历史记录可在详情中查看。
        </p>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <el-form-item label="变更原因" prop="reason">
            <el-input v-model="form.reason" type="textarea" :rows="3" maxlength="200" show-word-limit
              aria-label="变更原因" placeholder="请说明变更原因，例如任务调整或数据修正" />
          </el-form-item>
        </el-form>
      </div>
    </div>
    <template #footer>
      <el-button :disabled="saving" @click="close">取消</el-button>
      <template v-if="step === 'pick'">
        <el-button type="primary" :disabled="!changedCount" @click="step = 'preview'">
          下一步{{ changedCount ? `（新增 ${addedRows.length}、解除 ${removedRows.length}）` : '' }}
        </el-button>
      </template>
      <template v-else>
        <el-button :disabled="saving" @click="step = 'pick'">返回修改</el-button>
        <el-button type="primary" :loading="saving" @click="save">确认提交</el-button>
      </template>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, ref } from 'vue';
import { onBeforeRouteLeave } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getPerformanceRelationCandidates, savePerformanceRelation } from '@/api/performance';

const emit = defineEmits(['saved']);

// step：pick 勾选任务 → preview 预览并填写原因
const visible = ref(false), loading = ref(false), saving = ref(false), loaded = ref(false);
const step = ref('pick'), error = ref('');
const contentRef = ref(null), formRef = ref(null);
const rows = ref([]), checked = ref([]), search = ref(''), linkedFilter = ref('all');
const target = ref({ perfId: null, year: null, code: '', name: '' });
const form = ref({ reason: '' });
let disposed = false, requestVersion = 0, linkedIds = [];

const rules = {
  reason: [{ required: true, whitespace: true, message: '请填写变更原因', trigger: 'blur' }]
};
const summary = computed(() => `指标：${target.value.code ? `${target.value.code} ` : ''}${target.value.name || '—'}，`
  + `年度：${target.value.year || '—'}（关联按年度维护，年度不可修改）`);
const previewLead = computed(() => `本次将新增 ${addedRows.value.length} 条、解除 ${removedRows.value.length} 条关联，请核对后提交。`);
const keyword = computed(() => search.value.trim().toLowerCase());
const filteredRows = computed(() => rows.value.filter(item => {
  if (linkedFilter.value === 'linked' && !item.linked) return false;
  if (linkedFilter.value === 'unlinked' && item.linked) return false;
  const value = keyword.value;
  if (!value) return true;
  return `${item.taskCode || ''} ${item.taskName || ''}`.toLowerCase().includes(value);
}));
const allPicked = computed(() => filteredRows.value.length > 0 && filteredRows.value.every(item => checked.value.includes(item.taskId)));
const partPicked = computed(() => !allPicked.value && filteredRows.value.some(item => checked.value.includes(item.taskId)));
const addedRows = computed(() => rows.value.filter(item => checked.value.includes(item.taskId) && !linkedIds.includes(item.taskId)));
const removedRows = computed(() => rows.value.filter(item => !checked.value.includes(item.taskId) && linkedIds.includes(item.taskId)));
const changedCount = computed(() => addedRows.value.length + removedRows.value.length);

async function loadCandidates() {
  const version = ++requestVersion;
  loading.value = true; error.value = '';
  try {
    const data = await getPerformanceRelationCandidates(target.value.perfId, target.value.year);
    if (disposed || version !== requestVersion) return;
    rows.value = Array.isArray(data?.candidates) ? data.candidates.map(item => ({ ...item, taskId: Number(item.taskId) })) : [];
    linkedIds = rows.value.filter(item => Number(item.linked) === 1).map(item => item.taskId);
    checked.value = [...linkedIds];
    loaded.value = true;
  } catch (e) {
    if (!disposed && version === requestVersion) {
      loaded.value = false;
      error.value = e.message || '关联任务加载失败';
    }
  } finally {
    if (!disposed && version === requestVersion) loading.value = false;
  }
}

// 打开弹窗：年度跟随详情抽屉当前筛选年度
function open(perf, year) {
  target.value = {
    perfId: Number(perf?.perfId ?? perf?._raw?.perfId),
    year: Number(year) || null,
    code: perf?.code || perf?._raw?.perfCode || '',
    name: perf?.name || perf?._raw?.perfName || ''
  };
  rows.value = []; checked.value = []; linkedIds = [];
  search.value = ''; linkedFilter.value = 'all';
  form.value = { reason: '' };
  step.value = 'pick'; error.value = ''; loaded.value = false;
  visible.value = true;
  loadCandidates();
}

function toggleAll(value) {
  const ids = filteredRows.value.map(item => item.taskId);
  checked.value = value
    ? [...new Set([...checked.value, ...ids])]
    : checked.value.filter(id => !ids.includes(id));
}

async function close() {
  if (saving.value) return false;
  if (changedCount.value > 0 || form.value.reason.trim()) {
    try {
      await ElMessageBox.confirm('关联变更尚未提交，确定关闭？', '离开确认',
        { type: 'warning', confirmButtonText: '放弃填写', cancelButtonText: '继续修改' });
    } catch {
      return false;
    }
  }
  requestVersion++; visible.value = false;
  return true;
}

async function save() {
  if (saving.value) return;
  if (!await formRef.value?.validate().catch(() => false)) return;
  saving.value = true; error.value = '';
  try {
    const result = await savePerformanceRelation({
      perfId: target.value.perfId,
      year: target.value.year,
      addTaskIds: addedRows.value.map(item => item.taskId),
      removeTaskIds: removedRows.value.map(item => item.taskId),
      reason: form.value.reason.trim()
    });
    if (disposed) return;
    visible.value = false;
    const added = result?.added ?? addedRows.value.length;
    const removed = result?.removed ?? removedRows.value.length;
    ElMessage.success(`已更新关联：新增 ${added} 条、解除 ${removed} 条，完成值已重算`);
    emit('saved', result);
  } catch (e) {
    if (!disposed) {
      error.value = e.message || '保存失败，请重试';
      await nextTick(); contentRef.value?.parentElement?.scrollTo({ top: 0 });
    }
  } finally {
    if (!disposed) saving.value = false;
  }
}

onBeforeUnmount(() => { disposed = true; requestVersion++; });
onBeforeRouteLeave(() => visible.value ? close() : true);
defineExpose({ open });
</script>

<style scoped>
.pick-toolbar { display: flex; align-items: center; gap: 12px; margin: 12px 0; }
.pick-search { flex: 1; }
.pick-list { max-height: 320px; overflow-y: auto; border: 1px solid #ebeef5; border-radius: 6px; padding: 6px 12px; background: #fafafa; }
.pick-list .el-checkbox-group { display: flex; flex-direction: column; }
.pick-item { height: auto; margin-right: 0; padding: 6px 0; }
.pick-item .el-checkbox__label { white-space: normal; line-height: 1.6; }
.pick-code { color: #909399; margin-right: 6px; }
.pick-tag { margin-left: 8px; }
.pick-note { color: #e6a23c; margin-left: 8px; font-size: 12px; }
.pick-empty { color: #909399; text-align: center; margin: 14px 0; }
.relation-preview { margin-top: 12px; overflow-wrap: anywhere; }
.preview-block { margin-top: 12px; border: 1px solid #ebeef5; border-radius: 6px; padding: 8px 12px; background: #fafafa; }
.preview-title { color: #303133; font-weight: 600; margin-bottom: 6px; }
.preview-row { color: #606266; line-height: 1.9; }
.preview-note { color: #606266; line-height: 1.7; }
</style>
<style>
.relation-dialog { max-width: calc(100vw - 32px); }
.relation-dialog .el-dialog__body { max-height: 65vh; overflow-y: auto; }
</style>
