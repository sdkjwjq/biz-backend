<template>
  <div class="advanced-filters">
    <div class="filter-basic">
      <slot name="basic" />
      <el-button class="advanced-toggle" :aria-expanded="expanded" :aria-controls="panelId" @click="expanded = !expanded">
        {{ expanded ? '收起高级筛选' : '高级筛选' }}<span v-if="activeCount" class="active-count">{{ activeCount }}</span>
      </el-button>
      <slot name="actions" />
    </div>
    <div :id="panelId" v-show="expanded" class="filter-advanced" role="group" :aria-label="`${title}高级筛选`">
      <slot />
      <p class="filter-help">多个条件同时满足才显示结果；收起后已选条件仍然生效。</p>
    </div>
  </div>
</template>
<script setup>
import { ref, useId } from 'vue';
defineProps({ title: { type: String, default: '' }, activeCount: { type: Number, default: 0 } });
const expanded = ref(false);
const panelId = `advanced-filters-${useId()}`;
</script>
<style scoped>
.advanced-filters { min-width: 0; width: 100%; }
.filter-basic, .filter-advanced { display: flex; flex-wrap: wrap; align-items: center; gap: 12px; min-width: 0; }
.filter-advanced { margin-top: 12px; padding: 14px; background: #f7f9fc; border: 1px solid #e7edf5; border-radius: 6px; }
.advanced-toggle { margin-left: 0; color: #337ecc; flex-shrink: 0; }
.active-count { margin-left: 6px; padding: 1px 6px; background: #ecf5ff; border-radius: 10px; font-size: 12px; }
.filter-help { flex-basis: 100%; margin: 0; color: #909399; font-size: 12px; line-height: 1.5; }
@media (max-width: 700px) { .filter-basic, .filter-advanced { gap: 8px; } }
/* 窄屏展开时允许整张列表卡片滚动，避免筛选项把表格压缩为零高。 */
@media (max-width: 1100px) {
  :global(.table-card:has(.advanced-toggle[aria-expanded="true"])) { overflow-y: auto; }
  :global(.table-card:has(.advanced-toggle[aria-expanded="true"]) .table-wrapper) { flex-shrink: 0; min-height: 260px; }
}
</style>
