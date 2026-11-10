<template>
  <div class="business-filters">
    <AdvancedFilters :title="title" :active-count="advancedCount">
      <template #basic>
      <div class="filter-item">
        <span>年度</span>
        <el-select v-model="year" :aria-label="`${title}年度`" placeholder="全部年度" @change="emit('year-change')">
          <el-option label="全部年度" value="" />
          <el-option v-for="item in yearOptions" :key="item" :label="`${item}年`" :value="String(item)" />
        </el-select>
      </div>
      <div v-for="field in fields.filter(item => item.basic)" :key="field.key" class="filter-item" :class="{ 'wide-filter': field.wide || field.department }">
        <span>{{ field.label }}</span>
        <el-select :model-value="filters[field.key]" :aria-label="`${title}${field.label}`" :placeholder="field.options[0].label"
          :filterable="field.filterable || field.department" :disabled="loading" @update:model-value="value => filters = { ...filters, [field.key]: value }">
          <el-option v-for="option in field.options" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
      </div>
      <div class="filter-search">
        <el-input v-model="search" :aria-label="`${title}搜索`" :placeholder="placeholder" clearable @clear="emit('clear')" @keyup.enter="emit('search')">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="emit('search')">查询</el-button>
        <el-button :disabled="loading" @click="emit('reset')">重置</el-button>
      </div>
      </template>
      <template #actions><div v-if="$slots.actions" class="filter-actions"><slot name="actions" /></div></template>
      <div class="filter-item">
        <span>{{ title }}范围</span>
        <el-select v-model="scope" :aria-label="`${title}范围`" :disabled="loading">
          <el-option label="全部" value="all" /><el-option :label="mineLabel" value="mine" />
          <el-option label="待我审核" value="todo" /><el-option label="已退回" value="returned" />
        </el-select>
      </div>
      <div v-for="field in fields.filter(item => !item.basic)" :key="field.key" class="filter-item" :class="{ 'wide-filter': field.wide || field.department }">
        <span>{{ field.label }}</span>
        <el-select :model-value="filters[field.key]" :aria-label="`${title}${field.label}`" :placeholder="field.options[0].label"
          :filterable="field.filterable || field.department" :disabled="loading" @update:model-value="value => filters = { ...filters, [field.key]: value }">
          <el-option v-for="option in field.options" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
      </div>
    </AdvancedFilters>
    <div v-if="error" class="filter-error" role="alert">{{ error }} <el-button link type="primary" @click="emit('retry')">重试</el-button></div>
  </div>
</template>
<script setup>
import { computed } from 'vue';
import AdvancedFilters from './AdvancedFilters.vue';
import { Search } from '@element-plus/icons-vue';
const props = defineProps({ title: String, yearOptions: Array, fields: Array, loading: Boolean, error: String, placeholder: String,
  mineLabel: { type: String, default: '我负责的' } });
const year = defineModel('year');
const scope = defineModel('scope');
const search = defineModel('search');
const filters = defineModel('filters');
const advancedCount = computed(() => (scope.value && scope.value !== 'all' ? 1 : 0) + props.fields.filter(field => !field.basic && filters.value[field.key] !== '' && filters.value[field.key] != null).length);
const emit = defineEmits(['year-change', 'search', 'clear', 'reset', 'retry']);
</script>
<style scoped>
.business-filters { container-type: inline-size; margin-bottom: 16px; }
.filter-bar, .filter-item, .filter-search, .filter-actions { display: flex; align-items: center; gap: 12px; }
.filter-bar { flex-wrap: wrap; }
.filter-item { flex: 0 0 auto; gap: 8px; color: #606266; font-size: 14px; }
.filter-item > span { white-space: nowrap; }
.filter-item .el-select { width: 104px; }
.wide-filter .el-select { width: 160px; }
.filter-search { flex: 1; min-width: 280px; }
.filter-search .el-input { flex: 1; min-width: 120px; }
.filter-bar :deep(.el-button) { height: 32px; margin-left: 0; padding: 8px 12px; }
.filter-bar :deep(.el-select__wrapper), .filter-bar :deep(.el-input__wrapper) { min-height: 32px; box-sizing: border-box; }
.filter-error { margin-top: 8px; color: #e6a23c; font-size: 13px; }
@container (max-width: 950px) { .filter-search { flex-basis: 100%; } }
@container (max-width: 440px) {
  .filter-item { flex: 1 1 220px; }
  .filter-item > span { min-width: 84px; }
  .filter-item .el-select { flex: 1; min-width: 80px; }
}
</style>
