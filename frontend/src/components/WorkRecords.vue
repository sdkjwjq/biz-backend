<template>
  <div ref="pageRoot" class="work-records" v-loading="initialLoading">
    <template v-if="!detail && !opening">
      <div class="page-heading"><div><h2>工作纪实</h2><p>按月汇总任务进展。专业群可填报全校任务；归口部门可填报涉及本部门的一级改革任务及其全部下级任务。</p></div>
        <div class="heading-actions"><el-button v-if="capabilities.canExport" :disabled="busy || !exportSelection.length" :loading="operation === 'export'" @click="download(exportSelection.map(row => row.recordId))">导出所选（{{ exportSelection.length }}）</el-button><el-button v-if="showDeleteSelection" type="danger" plain :disabled="busy || !deleteSelection.length" @click="showDelete(deleteSelection)">删除所选（{{ deleteSelection.length }}）</el-button><el-button v-if="capabilities.canCreate" type="primary" :disabled="busy || !availableYears.length" @click="showCreate">新建纪实</el-button></div>
      </div>
      <el-alert v-if="initialError" :title="initialError" type="error" :closable="false" show-icon>
        <el-button link type="primary" @click="initialize">重新加载</el-button>
      </el-alert>
      <template v-else-if="hasAccess">
        <div class="list-card">
          <QueryFilterBar ref="queryBar" domain="records" title="纪实" :defaults="{year:String(defaultYear)}" @result="receiveQuery" @state="queryState = $event" />
          <el-alert v-if="listError" :title="listError" type="error" :closable="false" show-icon />
          <el-table ref="recordTable" :data="rows" v-loading="listLoading || queryState.loading" row-key="recordId" border :empty-text="!queryState.initialized ? (queryState.error ? '加载失败，请重试' : '正在加载工作纪实…') : '暂无工作纪实'" @selection-change="selectedRecords = $event">
            <el-table-column v-if="showSelection" type="selection" width="50" :reserve-selection="true" :selectable="row => !busy && (canDeleteRecord(row) || (capabilities.canExport && row.status === 1))" />
            <el-table-column label="纪实月份" min-width="150"><template #default="{ row }">{{ row.recordYear }}年{{ row.recordMonth }}月</template></el-table-column>
            <el-table-column prop="ownerName" label="填报人" min-width="150" />
            <el-table-column label="状态" width="110"><template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '已提交' : '草稿' }}</el-tag></template></el-table-column>
            <el-table-column label="更新时间" min-width="175"><template #default="{ row }">{{ formatDate(row.updateTime) }}</template></el-table-column>
            <el-table-column label="提交时间" min-width="175"><template #default="{ row }">{{ formatDate(row.submitTime) }}</template></el-table-column>
            <el-table-column label="操作" :width="capabilities.canExport ? 210 : 160" fixed="right"><template #default="{ row }"><el-button link type="primary" :disabled="busy" @click="openRecord(row.recordId)">{{ row.status === 0 ? '打开草稿' : '查看' }}</el-button><el-button v-if="capabilities.canExport && row.status === 1" link type="primary" :disabled="busy" @click="download([row.recordId])">导出</el-button><el-button v-if="canDeleteRecord(row)" link type="danger" :disabled="busy" @click="showDelete(row)">删除</el-button></template></el-table-column>
          </el-table>
          <el-pagination v-if="queryState.initialized" v-model:current-page="page" :page-size="10" :total="total" layout="total, prev, pager, next" @current-change="loadList" />
        </div>
      </template>
      <el-empty v-else-if="!initialLoading" description="您目前没有工作纪实填报或查阅权限" />
    </template>

    <div v-if="opening" class="opening" v-loading="true">正在加载纪实…</div>
    <template v-if="detail && !opening">
      <div class="page-heading detail-heading">
        <div><h2>{{ detail.record.recordYear }}年{{ detail.record.recordMonth }}月工作纪实</h2><p>填报人：{{ detail.record.ownerName }} <el-tag :type="submitted ? 'success' : 'info'">{{ submitted ? '已提交' : '草稿' }}</el-tag></p></div>
        <div class="heading-actions"><el-button v-if="capabilities.canExport && submitted" :disabled="busy" :loading="operation === 'export'" @click="download([detail.record.recordId])">导出 Word</el-button><el-button v-if="canDeleteRecord(detail.record)" type="danger" plain :disabled="busy" @click="showDelete(detail.record)">{{ submitted ? '删除纪实' : '删除草稿' }}</el-button><el-button :disabled="busy" @click="backToList">返回列表</el-button></div>
      </div>
      <el-alert v-if="!editable" :title="submitted ? '已提交纪实不可修改，以下内容与统计为提交时保存的版本。' : '您已不具备该年度填报资格，当前草稿仅可查看。'" type="info" :closable="false" show-icon />
      <el-alert v-if="operationError" :title="operationError" type="error" :closable="false" show-icon>
        <el-button link type="primary" :disabled="busy" @click="reloadDetail">重新加载纪实</el-button>
      </el-alert>
      <section class="record-section">
        <div class="section-heading"><h3>一、基本情况</h3><el-button v-if="editable" :disabled="busy" @click="refreshStatistics">刷新统计</el-button></div>
        <p class="muted">{{ submitted ? '展示提交时保存的任务范围和统计结果，后续任务或权限调整不改变本份纪实。' : '按一级改革任务确定填报范围，汇总范围内全部三级任务；四级目标归并到所属三级任务，完成情况以整个三级任务达标并最终归档为准。' }}</p>
        <p class="summary"><b>{{ detail.record.recordMonth }}</b>月新增完成任务 <b>{{ detail.statistics.newCompleted }}</b> 条；累计完成 <b>{{ detail.statistics.cumulativeCompleted }}</b> 条，占全年 <b>{{ detail.statistics.totalTasks }}</b> 条任务的 <b>{{ detail.statistics.completionRate == null ? '—' : `${detail.statistics.completionRate}%` }}</b>。</p>
        <p class="muted">统计截止：{{ formatDate(detail.statistics.cutoffAt) }} 生成时间：{{ formatDate(detail.statistics.generatedAt) }}</p>
        <el-alert :title="detail.statistics.notice" :type="detail.statistics.unverifiedTasks ? 'warning' : 'info'" :closable="false" />
        <el-table :data="detail.statistics.tasks" row-key="taskId" border max-height="330" empty-text="该年度暂无任务" class="task-summary-table">
          <el-table-column type="index" label="序号" width="60" /><el-table-column prop="taskCode" label="三级任务编号" width="140" />
          <el-table-column prop="taskName" label="三级任务名称" min-width="240" /><el-table-column prop="leaderName" label="负责人" width="120" />
          <el-table-column label="平台填报审核情况" min-width="210"><template #default="{ row }">{{ row.auditState }}<div v-if="row.verificationReason" class="verification">{{ row.verificationReason }}</div></template></el-table-column>
          <el-table-column prop="other" label="其它" width="90" />
        </el-table>
      </section>
      <section class="record-section">
        <h3>二、主要工作亮点和成果</h3>
        <p class="muted">从可填报的一级改革任务中逐项添加，分别记录关键进展、阶段性成果和典型做法。每项最多300字；添加项目不改变上方统计范围。</p>
        <div v-if="editable" class="reform-picker">
          <el-select v-model="pendingReform" filterable clearable aria-label="选择改革任务" placeholder="选择要添加的一级改革任务" :disabled="busy">
            <el-option v-for="reform in reformOptions" :key="reform.taskId" :label="reform.taskName" :value="reform.taskId" :disabled="reform.unavailable || form.entries.some(entry => entry.reformTaskId === reform.taskId)" />
          </el-select>
          <el-button type="primary" :disabled="busy || !pendingReform" @click="addReform">添加任务</el-button>
        </div>
        <el-empty v-if="!form.entries.length" :description="editable ? '请选择需要填报的改革任务' : '未填写改革任务内容'" :image-size="65" />
        <div v-for="entry in form.entries" :key="entry.reformTaskId" class="reform-entry">
          <div class="reform-heading"><h4>{{ entry.reformTaskName }}</h4><el-button v-if="editable" type="danger" link :disabled="busy" @click="changeSelection(form.entries.filter(item => item.reformTaskId !== entry.reformTaskId).map(item => item.reformTaskId))">移除任务</el-button></div>
          <div class="entry-texts">
            <WorkRecordText v-model="entry.keyProgress" label="关键进展" :readonly="!editable" :disabled="busy" />
            <WorkRecordText v-model="entry.stageResults" label="阶段性成果" :readonly="!editable" :disabled="busy" />
            <WorkRecordText v-model="entry.typicalPractices" label="典型做法" :readonly="!editable" :disabled="busy" />
          </div>
        </div>
      </section>
      <section class="record-section"><h3>三、存在问题</h3><p class="muted">说明本月任务推进中遇到的困难、主要原因，以及需要协调解决的问题。最多300字，无相关内容可留空。</p><WorkRecordText v-model="form.problems" label="存在问题" :readonly="!editable" :disabled="busy" /></section>
      <section class="record-section"><h3>四、下阶段工作重点</h3><p class="muted">梳理下一阶段的重点任务、推进措施和预期目标，为后续工作安排提供参考。最多300字，无相关内容可留空。</p><WorkRecordText v-model="form.nextFocus" label="下阶段工作重点" :readonly="!editable" :disabled="busy" /></section>
      <section class="record-section"><h3>五、其它事项</h3><p class="muted">补充以上部分未涵盖、需要说明的工作情况或相关事项。最多300字，无相关内容可留空。</p><WorkRecordText v-model="form.otherMatters" label="其它事项" :readonly="!editable" :disabled="busy" /></section>
      <div class="detail-actions">
        <span v-if="editable" class="muted">{{ dirty ? '有未保存的内容' : '内容已保存' }}</span>
        <el-button :disabled="busy" @click="backToList">返回列表</el-button>
        <template v-if="editable"><el-button :loading="operation === 'save'" :disabled="busy" @click="save(false)">保存草稿</el-button><el-button type="primary" :loading="operation === 'submit'" :disabled="busy" @click="save(true)">提交纪实</el-button></template>
      </div>
    </template>

    <el-dialog v-model="deleteVisible" title="删除工作纪实" width="min(480px, 92vw)" :close-on-click-modal="false" :close-on-press-escape="!busy" :show-close="!busy" :before-close="closeDelete">
      <template v-if="deleteTargets.length">
        <p v-if="deleteTargets.length === 1">确认删除 {{ deleteTargets[0].ownerName }} 的 {{ deleteTargets[0].recordYear }}年{{ deleteTargets[0].recordMonth }}月{{ deleteTargets[0].status === 0 ? '草稿' : '工作纪实' }}？</p>
        <p v-else>确认删除选中的 {{ deleteTargets.length }} 份工作纪实？</p>
        <el-alert title="删除后不再显示；后台保留原文和删除记录，填报人可重新填报对应月份。" type="warning" :closable="false" show-icon />
        <el-form v-if="deleteNeedsReason" label-position="top" style="margin-top: 16px"><el-form-item label="删除已提交纪实的原因" required>
          <el-input v-model="deleteReason" aria-label="删除原因" type="textarea" :rows="3" maxlength="300" show-word-limit :disabled="busy" placeholder="请填写删除原因" />
        </el-form-item></el-form>
        <el-alert v-if="deleteError" :title="deleteError" type="error" :closable="false" show-icon />
      </template>
      <template #footer><el-button :disabled="busy" @click="closeDelete()">取消</el-button><el-button type="danger" :disabled="busy" :loading="operation === 'delete'" @click="confirmDelete">确认删除</el-button></template>
    </el-dialog>
    <el-dialog v-model="createVisible" title="新建工作纪实" width="min(440px, 92vw)" :close-on-click-modal="false" :before-close="closeCreate">
      <p class="muted">每人每月一份；已有纪实将直接打开。</p>
      <el-form label-width="70px"><el-form-item label="年度"><el-select v-model="createYear" aria-label="新建纪实年度" :disabled="busy"><el-option v-for="year in availableYears" :key="year" :label="`${year}年`" :value="year" /></el-select></el-form-item>
        <el-form-item label="月份"><el-select v-model="createMonth" aria-label="新建纪实月份" :disabled="busy"><el-option v-for="month in 12" :key="month" :label="`${month}月`" :value="month" :disabled="isFuture(createYear, month)" /></el-select></el-form-item></el-form>
      <template #footer><el-button :disabled="busy" @click="createVisible = false">取消</el-button><el-button type="primary" :loading="operation === 'create'" :disabled="busy || !createYear || isFuture(createYear, createMonth)" @click="create">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
const queryBar=ref(null),appliedRecordQuery=ref({});
const queryState=ref({loading:false,initialized:false,error:''});
function receiveQuery(data){listVersion++;listController?.abort();listLoading.value=false;listError.value='';appliedRecordQuery.value=data.query;page.value=data.page;rows.value=data.items;total.value=data.total;recordTable.value?.clearSelection();selectedRecords.value=[];}
import { ref, reactive, computed, watch, nextTick, onMounted, onBeforeUnmount } from 'vue';
import { onBeforeRouteLeave, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import QueryFilterBar from './QueryFilterBar.vue';
import { get } from '@/utils/request';
import WorkRecordText from './WorkRecordText.vue';
import { getWorkRecordCapabilities, getWorkRecord, createWorkRecord,
  saveWorkRecord, submitWorkRecord, refreshWorkRecordStatistics, exportWorkRecords, deleteWorkRecord, deleteWorkRecords } from '@/api/workRecords';

const years = [2025, 2026, 2027, 2028, 2029];
const route = useRoute();
const pageRoot = ref(null);
const recordTable = ref(null), selectedRecords = ref([]);
const canDeleteRecord = row => row?.status === 0 ? row.ownerId === capabilities.value.currentUserId : row?.status === 1 && !!capabilities.value.canDelete;
const showSelection = computed(() => capabilities.value.canExport || capabilities.value.canDelete || rows.value.some(canDeleteRecord) || selectedRecords.value.some(canDeleteRecord));
const exportSelection = computed(() => selectedRecords.value.filter(row => row.status === 1 && capabilities.value.canExport));
const deleteSelection = computed(() => selectedRecords.value.filter(canDeleteRecord));
const showDeleteSelection = computed(() => capabilities.value.canDelete || rows.value.some(canDeleteRecord) || deleteSelection.value.length > 0);
let exportController;
async function scrollToStart() {
  await nextTick();
  if (alive()) pageRoot.value?.scrollTo({ top: 0 });
}
const today = new Intl.DateTimeFormat('en-CA', { timeZone: 'Asia/Shanghai', year: 'numeric', month: '2-digit' }).formatToParts(new Date());
const currentYear = Number(today.find(part => part.type === 'year').value);
const currentMonth = Number(today.find(part => part.type === 'month').value);
const defaultYear = years.includes(currentYear) ? currentYear : '';
const capabilities = ref({ fillableYears: [] });
const hasAccess = computed(() => capabilities.value.canCreate || capabilities.value.canViewAll || capabilities.value.canViewOwnRecords);
const availableYears = computed(() => (capabilities.value.fillableYears || []).filter(year => year <= currentYear));
const initialYear = years.includes(Number(route.query.year)) ? Number(route.query.year) : defaultYear;
const initialMonth = Number(route.query.month);
const filters = reactive({ year: initialYear, month: initialMonth >= 1 && initialMonth <= 12 ? initialMonth : '', ownerId: '', status: route.query.status === '1' ? 1 : '' });
const rows = ref([]), page = ref(1), total = ref(0);
const listLoading = ref(false), initialLoading = ref(false), opening = ref(false);
const initialError = ref(''), listError = ref(''), operationError = ref('');
const detail = ref(null), pendingReform = ref(null), operation = ref('');
const busy = computed(() => !!operation.value);
const editable = computed(() => detail.value?.editable === true);
const submitted = computed(() => detail.value?.record.status === 1);
const form = reactive({ entries: [], problems: '', nextFocus: '', otherMatters: '' });
const fingerprint = () => JSON.stringify(form);
const savedFingerprint = ref('');
const dirty = computed(() => editable.value && fingerprint() !== savedFingerprint.value);
const createVisible = ref(false), createYear = ref(null), createMonth = ref(currentMonth);
const deleteVisible = ref(false), deleteTargets = ref([]), deleteReason = ref(''), deleteError = ref('');
const deleteNeedsReason = computed(() => deleteTargets.value.some(row => row.status === 1));
function showDelete(records) {
  const targets = (Array.isArray(records) ? records : [records]).filter(canDeleteRecord);
  if (busy.value || !targets.length) return;
  deleteTargets.value = targets.map(row => ({ ...row })); deleteReason.value = ''; deleteError.value = ''; deleteVisible.value = true;
}
function closeDelete(done) {
  if (busy.value) return;
  deleteVisible.value = false; deleteTargets.value = []; deleteReason.value = ''; deleteError.value = '';
  if (typeof done === 'function') done();
}
async function confirmDelete() {
  if (busy.value || !deleteTargets.value.length) return;
  const reason = deleteReason.value.trim();
  if (deleteNeedsReason.value && (!reason || Array.from(reason).length > 300)) { deleteError.value = '请填写1～300字的删除原因'; return; }
  const targets = deleteTargets.value.map(row => ({ ...row }));
  operation.value = 'delete'; deleteError.value = '';
  let deleted = false;
  try {
    if (targets.length === 1) await deleteWorkRecord(targets[0].recordId, targets[0].version, reason);
    else await deleteWorkRecords(targets.map(row => ({ recordId: row.recordId, version: row.version })), reason);
    if (!alive()) return;
    deleted = true; deleteVisible.value = false; deleteTargets.value = []; deleteReason.value = '';
    ++listVersion; listController?.abort(); ++detailVersion; detailController?.abort();
    if (targets.some(row => row.recordId === detail.value?.record.recordId)) detail.value = null;
    recordTable.value?.clearSelection(); selectedRecords.value = [];
    const deletedIds = new Set(targets.map(row => row.recordId));
    rows.value = rows.value.filter(row => !deletedIds.has(row.recordId));
    ElMessage.success(targets.length === 1 ? '纪实已删除，可重新填报该月份' : `已删除${targets.length}份工作纪实`);
    await loadList();
    if (listError.value) ElMessage.warning('删除已成功，列表刷新失败，请点击刷新');
  } catch (error) {
    if (alive()) {
      if (deleted) ElMessage.warning('删除已成功，列表信息刷新失败，请点击刷新');
      else deleteError.value = error.message || '删除失败，请重试';
    }
  } finally { if (alive()) operation.value = ''; }
}
const token = localStorage.getItem('token');
let disposed = false, listVersion = 0, detailVersion = 0, initialVersion = 0;
let listController, detailController, initialController, leavePromise;
const alive = () => !disposed && localStorage.getItem('token') === token;
const reformOptions = computed(() => {
  const options = [...(detail.value?.statistics.reformTasks || [])];
  form.entries.forEach(entry => {
    if (!options.some(item => item.taskId === entry.reformTaskId)) options.push({ taskId: entry.reformTaskId, taskName: `${entry.reformTaskName}（已不在填报范围）`, unavailable: true });
  });
  return options;
});
const formatDate = value => value ? new Date(value).toLocaleString('zh-CN', { timeZone: 'Asia/Shanghai', hour12: false }) : '—';
const isFuture = (year, month) => year > currentYear || (year === currentYear && month > currentMonth);

async function loadList() {
  const version = ++listVersion;
  listController?.abort(); listController = new AbortController();
  listLoading.value = true; listError.value = '';
  try {
    const params = Object.fromEntries(Object.entries(appliedRecordQuery.value).map(([key,value])=>['f.'+key,value]));
    const data = await get('/list-filters/records', { ...params, page: page.value, pageSize: 10, responseMode: 'page', includeFields: false }, { signal: listController.signal });
    if (!alive() || version !== listVersion) return;
    total.value = data.total;
    const last = Math.max(1, Math.ceil(data.total / 10));
    if (page.value > last) { page.value = last; return loadList(); }
    rows.value = data.items;queryBar.value?.rememberPage(page.value);
  } catch (error) {
    if (alive() && version === listVersion && error.name !== 'AbortError') { listError.value = error.message || '纪实列表加载失败'; }
  } finally { if (alive() && version === listVersion) listLoading.value = false; }
}
async function initialize() {
  const version = ++initialVersion;
  initialController?.abort(); initialController = new AbortController();
  initialLoading.value = true; initialError.value = '';
  try {
    const access = await getWorkRecordCapabilities({ signal: initialController.signal });
    if (!alive() || version !== initialVersion) return;
    capabilities.value = access;
    if (hasAccess.value) {
      await nextTick();
      await queryBar.value?.refresh();
    }
  } catch (error) { if (alive() && version === initialVersion && error.name !== 'AbortError') initialError.value = error.message || '纪实权限加载失败'; }
  finally { if (alive() && version === initialVersion) initialLoading.value = false; }
}
watch(filters, () => { recordTable.value?.clearSelection(); selectedRecords.value = []; page.value = 1; if (hasAccess.value) loadList(); });

function applyDetail(data) {
  detail.value = data;
  form.entries = (data.entries || []).map(item => ({ reformTaskId: item.reformTaskId, reformTaskName: item.reformTaskName,
    keyProgress: item.keyProgress || '', stageResults: item.stageResults || '', typicalPractices: item.typicalPractices || '' }));
  form.problems = data.record.problems || ''; form.nextFocus = data.record.nextFocus || ''; form.otherMatters = data.record.otherMatters || '';
  pendingReform.value = null;
  savedFingerprint.value = fingerprint(); operationError.value = '';
}
async function mayLeave() {
  if (busy.value) { ElMessage.warning('操作进行中，请稍候'); return false; }
  if (!dirty.value) return true;
  if (!leavePromise) leavePromise = ElMessageBox.confirm('有未保存的填报内容，离开后将丢失。是否继续？', '未保存提醒',
    { confirmButtonText: '离开', cancelButtonText: '继续填写', type: 'warning' }).then(() => true).catch(() => false).finally(() => { leavePromise = null; });
  return leavePromise;
}
async function openRecord(id) {
  if (!await mayLeave() || !alive()) return;
  const version = ++detailVersion;
  detailController?.abort(); detailController = new AbortController();
  selectedRecords.value = []; detail.value = null; operationError.value = ''; opening.value = true;
  try {
    const data = await getWorkRecord(id, { signal: detailController.signal });
    if (alive() && version === detailVersion) { applyDetail(data); scrollToStart(); }
  } catch (error) { if (alive() && version === detailVersion && error.name !== 'AbortError') ElMessage.error(error.message || '纪实加载失败'); }
  finally { if (alive() && version === detailVersion) opening.value = false; }
}
async function backToList() {
  if (!await mayLeave() || !alive()) return;
  ++detailVersion; detailController?.abort(); detail.value = null; opening.value = false; scrollToStart(); await initialize();
}
async function reloadDetail() { if (detail.value) await openRecord(detail.value.record.recordId); }
function showCreate() {
  createYear.value = availableYears.value.includes(currentYear) ? currentYear : availableYears.value.at(-1);
  createMonth.value = createYear.value === currentYear ? currentMonth : 12; createVisible.value = true;
}
function closeCreate(done) { if (!busy.value) done(); }
async function create() {
  if (busy.value || !createYear.value || isFuture(createYear.value, createMonth.value)) return;
  operation.value = 'create'; const version = ++detailVersion;
  try {
    const data = await createWorkRecord(createYear.value, createMonth.value);
    if (alive() && version === detailVersion) { createVisible.value = false; applyDetail(data); scrollToStart(); }
  } catch (error) { if (alive() && version === detailVersion) ElMessage.error(error.message || '新建失败'); }
  finally { if (alive()) operation.value = ''; }
}
function addReform() {
  if (busy.value || !editable.value) return;
  const reform = reformOptions.value.find(item => item.taskId === pendingReform.value && !item.unavailable);
  if (!reform || form.entries.some(item => item.reformTaskId === reform.taskId)) return;
  form.entries.push({ reformTaskId: reform.taskId, reformTaskName: reform.taskName, keyProgress: '', stageResults: '', typicalPractices: '' });
  pendingReform.value = null;
}
async function changeSelection(ids) {
  if (busy.value || !editable.value) return;
  const version = detailVersion;
  const removed = form.entries.filter(item => !ids.includes(item.reformTaskId));
  if (removed.some(item => item.keyProgress || item.stageResults || item.typicalPractices)) {
    operation.value = 'selection';
    try { await ElMessageBox.confirm('移除改革任务会清除该项尚未提交的文字，是否继续？', '移除填报项目', { confirmButtonText: '确认移除', cancelButtonText: '取消', type: 'warning' }); }
    catch { return; }
    finally { if (alive()) operation.value = ''; }
  }
  if (!alive() || version !== detailVersion) return;
  form.entries = ids.map(id => form.entries.find(item => item.reformTaskId === id) || { reformTaskId: id,
    reformTaskName: reformOptions.value.find(item => item.taskId === id)?.taskName || '', keyProgress: '', stageResults: '', typicalPractices: '' });
}
function valid(submit) {
  const texts = [form.problems, form.nextFocus, form.otherMatters, ...form.entries.flatMap(item => [item.keyProgress, item.stageResults, item.typicalPractices])];
  if (texts.some(text => Array.from(text).length > 300)) { ElMessage.warning('每项填报内容不得超过300字'); return false; }
  if (submit && !form.entries.some(item => [item.keyProgress, item.stageResults, item.typicalPractices].some(text => text.trim()))) {
    ElMessage.warning('请至少选择一项改革任务并填写有效内容'); return false;
  }
  return true;
}
async function save(submit) {
  if (busy.value || !editable.value || !valid(submit)) return;
  const id = detail.value.record.recordId, version = detailVersion;
  operation.value = submit ? 'submit' : 'save';
  try {
    if (submit) await ElMessageBox.confirm('提交后不可修改。提交时将重新计算并固定统计数据，确认提交？', '提交工作纪实', { confirmButtonText: '确认提交', cancelButtonText: '继续填写', type: 'warning' });
    if (!alive() || version !== detailVersion) return;
    const body = { ...JSON.parse(fingerprint()), version: detail.value.record.version };
    const result = await (submit ? submitWorkRecord(id, body) : saveWorkRecord(id, body));
    if (alive() && version === detailVersion) { applyDetail(result); ElMessage.success(submit ? '工作纪实已提交' : '草稿已保存'); }
  } catch (error) {
    if (alive() && version === detailVersion && error !== 'cancel' && error !== 'close') operationError.value = error.message || '保存失败，当前填报内容已保留';
  } finally { if (alive()) operation.value = ''; }
}
async function refreshStatistics() {
  if (busy.value || !editable.value) return;
  const id = detail.value.record.recordId, version = detailVersion;
  operation.value = 'refresh';
  try {
    const result = await refreshWorkRecordStatistics(id, detail.value.record.version);
    if (alive() && version === detailVersion) { detail.value = result; operationError.value = ''; ElMessage.success('统计已刷新，当前填写内容保留'); }
  } catch (error) { if (alive() && version === detailVersion) operationError.value = error.message || '刷新统计失败'; }
  finally { if (alive()) operation.value = ''; }
}
const beforeUnload = event => { if (dirty.value || busy.value) { event.preventDefault(); event.returnValue = ''; } };
async function download(ids) {
  if (busy.value || !ids.length) return;
  operation.value = 'export'; exportController = new AbortController();
  try {
    const blob = await exportWorkRecords([...ids], { signal: exportController.signal });
    if (!alive()) return;
    if (!(blob instanceof Blob) || !blob.size) throw new Error('导出文件为空，请重试');
    const url = URL.createObjectURL(blob);
    try {
      const link = document.createElement('a'); link.href = url; link.download = ids.length > 1 ? '工作纪实-合并导出.docx' : '工作纪实.docx';
      document.body.appendChild(link); link.click(); link.remove();
    } finally { URL.revokeObjectURL(url); }
    ElMessage.success('纪实导出成功');
  } catch (error) { if (alive() && error.name !== 'AbortError') ElMessage.error(error.message || '纪实导出失败，请重试'); }
  finally { if (alive()) operation.value = ''; }
}
onBeforeRouteLeave(() => mayLeave());
onMounted(async () => { window.addEventListener('beforeunload', beforeUnload); await initialize(); if (alive() && hasAccess.value && /^\d+$/.test(String(route.query.recordId || ''))) await openRecord(route.query.recordId); });
onBeforeUnmount(() => { disposed = true; ++detailVersion; ++listVersion; ++initialVersion; exportController?.abort(); listController?.abort(); detailController?.abort(); initialController?.abort(); window.removeEventListener('beforeunload', beforeUnload); });
</script>

<style scoped>
.work-records { height: 100%; overflow-y: auto; box-sizing: border-box; padding: 24px; min-width: 0; color: #303133; }
.page-heading, .section-heading { display: flex; align-items: center; justify-content: space-between; gap: 16px; }
.page-heading { margin-bottom: 20px; }
.heading-actions { display: flex; gap: 8px; flex-wrap: wrap; } .heading-actions .el-button { margin-left: 0; }
h2 { font-size: 22px; margin: 0 0 8px; } h3 { font-size: 17px; margin: 0 0 16px; } h4 { font-size: 15px; margin: 0 0 16px; }
.page-heading p, .muted { font-size: 13px; color: #909399; line-height: 1.7; }
.list-card, .record-section { background: white; padding: 20px; border-radius: 8px; border: 1px solid #ebeef5; margin-bottom: 16px; }
.record-filters, .record-filters .record-filter-item { display: flex; align-items: center; gap: 12px; }
.record-filters { flex-wrap: wrap; margin-bottom: 16px; font-size: 14px; }
.record-filters .record-filter-item { gap: 8px; } .record-filters .el-select { width: 140px; }
.record-filters :deep(.el-button) { margin: 0; height: 32px; }
.el-pagination { margin-top: 16px; justify-content: flex-end; }
.summary { line-height: 1.8; } .summary b { color: #409eff; }
.task-summary-table { margin-top: 16px; } .verification { color: #b88230; font-size: 12px; }
.reform-picker { display: flex; gap: 12px; width: 100%; margin-bottom: 16px; }
.reform-picker .el-select { flex: 1; min-width: 0; }
.reform-heading { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.reform-heading h4 { min-width: 0; overflow-wrap: anywhere; }
.reform-heading .el-button { flex-shrink: 0; }
.reform-entry { padding: 20px 0; border-top: 1px solid #ebeef5; }
.entry-texts { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; }
.detail-actions { position: sticky; bottom: 0; background: white; padding: 16px; border: 1px solid #ebeef5; display: flex; justify-content: flex-end; align-items: center; gap: 12px; flex-wrap: wrap; z-index: 2; }
.detail-actions .el-button { margin-left: 0; } .detail-actions .muted { margin-right: auto; }
.opening { min-height: 300px; text-align: center; padding-top: 60px; }
.work-records > .el-alert { margin-bottom: 16px; }
@media (max-width: 1100px) { .entry-texts { grid-template-columns: 1fr; } }
@media (max-width: 768px) { .work-records { padding: 12px; } .page-heading { flex-wrap: wrap; } .record-section, .list-card { padding: 12px; } .record-filters .record-filter-item { flex: 1 1 180px; } .record-filters .el-select { flex: 1; min-width: 100px; } }
</style>
