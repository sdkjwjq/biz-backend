<template>
  <div class="task-filters">
    <AdvancedFilters title="任务" :active-count="advancedCount">
      <template #basic>
      <div class="filter-item">
        <span class="filter-label">年度</span>
        <el-select v-model="year" aria-label="任务年度" placeholder="全部年度">
          <el-option label="全部年度" value="" />
          <el-option v-for="item in yearOptions" :key="item" :label="`${item}年`" :value="String(item)" />
        </el-select>
      </div>
      <div class="filter-item">
        <span class="filter-label">任务状态</span>
        <el-select v-model="status" aria-label="任务状态" placeholder="全部状态">
          <el-option label="全部状态" value="" /><el-option label="进行中" value="ing" />
          <el-option label="审核中" value="reviewing" /><el-option label="未开始" value="not_started" />
          <el-option label="已完成" value="done" /><el-option label="逾期" value="overdue" />
        </el-select>
      </div>
      <div class="filter-search">
        <el-input v-model="search" aria-label="任务搜索" placeholder="搜索任务编号或名称" clearable @clear="emit('clear')" @keyup.enter="emit('search')">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="emit('search')">查询</el-button>
        <el-button :disabled="loading" @click="emit('reset')">重置</el-button>
      </div>
      </template>
      <div class="filter-item">
        <span class="filter-label">任务范围</span>
        <el-select v-model="scope" aria-label="任务范围" :disabled="loading">
          <el-option label="全部" value="all" /><el-option label="我负责的" value="mine" />
          <el-option label="待我审核" value="todo" /><el-option label="已退回" value="returned" />
        </el-select>
      </div>
      <div class="filter-item person-filter">
        <span class="filter-label">任务类型</span>
        <el-select v-model="kind" aria-label="任务类型">
          <el-option label="全部类型" value="" /><el-option label="一般" value="normal" />
          <el-option label="重点" value="priority" /><el-option label="困难" value="difficult" /><el-option label="重点且困难" value="both" />
        </el-select>
      </div>
      <div class="filter-item person-filter">
        <span class="filter-label">责任人</span>
        <el-select v-model="leader" aria-label="任务责任人" placeholder="全部人员" filterable :disabled="loading">
          <el-option label="全部人员" value="" />
          <el-option v-for="item in leaders" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </div>
      <div class="department-filters"><AuditConditions /></div>
    </AdvancedFilters>
    <div v-if="error" class="filter-error" role="alert">{{ error }} <el-button link type="primary" @click="emit('retry')">重试</el-button></div>
  </div>
</template>

<script setup>
import { h, computed } from 'vue';
import AdvancedFilters from './AdvancedFilters.vue';
import { ElSelect, ElOption } from 'element-plus';
import { Search } from '@element-plus/icons-vue';

const props = defineProps({ yearOptions: Array, departments: Array, owningDepartments: Array, leaders: Array, loading: Boolean, error: String });
const year = defineModel('year');
const scope = defineModel('scope');
const status = defineModel('status');
const leader = defineModel('leader');
const kind = defineModel('kind');
const stage = defineModel('stage');
const department = defineModel('department');
const owningDepartment = defineModel('owningDepartment');
const search = defineModel('search');
const advancedCount = computed(() => [scope.value !== 'all' ? scope.value : '', kind.value, leader.value, stage.value, department.value, owningDepartment.value].filter(value => value !== '' && value != null).length);
const emit = defineEmits(['search', 'clear', 'reset', 'retry']);
const AuditConditions = () => [
  { label: '归口部门', aria: '任务归口部门', model: owningDepartment, filterable: true, options: [{ name: '全部部门', id: '' }, ...(props.owningDepartments || [])] },
  { label: '当前处理部门', aria: '任务当前处理部门', model: department, filterable: true, options: [{ name: '全部部门', id: '' }, ...(props.departments || [])] },
  { label: '待审阶段', aria: '任务待审阶段', model: stage, options: [
    { name: '全部阶段', id: '' }, { name: '待专业群审核', id: '10' },
    { name: '待归口部门审核', id: '20' }, { name: '待双高办归档', id: '30' }
  ] }
].map(item => h('div', { class: 'filter-item audit-condition' }, [
  h('span', { class: 'filter-label' }, item.label),
  h(ElSelect, {
    modelValue: item.model.value, 'onUpdate:modelValue': value => { item.model.value = value; },
    'aria-label': item.aria, placeholder: item.options[0].name, disabled: props.loading, filterable: item.filterable
  }, { default: () => item.options.map(option => h(ElOption, { key: option.id, label: option.name, value: option.id })) })
]));
</script>

<style scoped>
.task-filters { container-type: inline-size; margin-bottom: 16px; }
.filter-bar, .filter-item, .department-filters, .filter-search { display: flex; align-items: center; gap: 12px; }
.filter-bar { min-height: 32px; flex-wrap: wrap; }
.filter-item { flex: 0 0 auto; gap: 8px; }
.filter-label, :deep(.filter-label) { color: #606266; font-size: 14px; white-space: nowrap; }
.filter-item .el-select { width: 104px; }
.person-filter .el-select { width: 140px; }
.department-filters { flex-wrap: wrap; }
.department-filters :deep(.filter-item) { display: flex; align-items: center; gap: 8px; }
.department-filters :deep(.el-select) { width: 160px; }
.filter-search {
  flex: 1 1 360px;
  width: 100%;
  min-width: 320px;
  max-width: 480px;
  margin-left: auto;
}
.filter-search .el-input { flex: 1; min-width: 120px; }
.filter-bar :deep(.el-button) { height: 32px; margin-left: 0; padding: 8px 12px; }
.task-filters :deep(.el-select__wrapper), .task-filters :deep(.el-input__wrapper) { min-height: 32px; box-sizing: border-box; }
.filter-error { margin-top: 8px; color: #e6a23c; font-size: 13px; }
@container (max-width: 2600px) {
  .filter-bar { gap: 8px; }
  .filter-item,
  .department-filters,
  .department-filters :deep(.filter-item),
  .filter-search { gap: 6px; }
  .filter-item .el-select { width: 96px; }
  .person-filter .el-select { width: 120px; }
  .department-filters :deep(.el-select) { width: 126px; }
  .filter-search { flex-basis: 320px; min-width: 300px; }
}
@container (max-width: 800px) {
  .filter-search {
    flex-basis: 100%;
    order: 2;
    min-width: 0;
    max-width: none;
    margin-left: 0;
  }
}
@container (max-width: 440px) {
  .filter-item { flex: 1 1 140px; justify-content: space-between; }
  .filter-item .el-select { flex: 1; min-width: 80px; }
}
</style>
