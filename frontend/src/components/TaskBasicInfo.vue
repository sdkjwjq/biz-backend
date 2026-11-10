<template>
  <h4 class="section-title">基本信息与完成情况</h4>
  <div class="task-info-box" data-detail-section="基本信息">
    <div v-if="Number(task?.level) === 3" class="info-grid">
      <div class="info-row">
        <span class="label">任务类型：</span>
        <span class="value">{{ taskType }}</span>
        <el-button v-if="editable" link type="primary" @click="$emit('attributes')">修改管理属性</el-button>
      </div>
      <div class="info-row"><span class="label">计划完成日期：</span><span class="value">{{ raw.plannedCompletionDate || '—' }}</span></div>
      <div class="info-row"><span class="label">实际完成日期：</span><span class="value">{{ raw.actualCompletionDate ? formatDate(raw.actualCompletionDate) : '—' }}</span></div>
    </div>
    <div class="info-grid">
      <div class="info-row"><span class="label">任务编号：</span><span class="value">{{ task?.code || '—' }}</span></div>
      <div class="info-row"><span class="label">任务名称：</span><span class="value">{{ task?.name || '—' }}</span></div>
      <div class="info-row"><span class="label">当前进度：</span><span class="value">{{ Number(task?.progress) || 0 }}%</span></div>
      <div class="info-row"><span class="label">归口部门：</span><span class="value">{{ task?.dept || '—' }}</span></div>
      <div class="info-row"><span class="label">预期达成目标：</span><span class="value">{{ raw.expTarget || '—' }}</span></div>
      <div class="info-row info-row--full"><span class="label">预期成果成效：</span><span class="value">{{ raw.expEffect || '—' }}</span></div>
      <div class="info-row info-row--full"><span class="label">预期佐证清单：</span><span class="value">{{ raw.expMaterialDesc || '—' }}</span></div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  task: { type: Object, default: null },
  editable: { type: Boolean, default: false }
})
defineEmits(['attributes'])

const raw = computed(() => props.task?._raw || {})
const taskType = computed(() => [
  raw.value?.isPriority ? '重点' : '',
  raw.value?.isDifficult ? '困难' : ''
].filter(Boolean).join('、') || '一般')

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}
</script>

<style scoped>
.section-title {
  font-size: 15px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 12px;
  border-left: 3px solid #1890ff;
  padding-left: 8px;
}
.task-info-box {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 4px;
  margin-bottom: 24px;
  border: 1px solid #e4e7ed;
}
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.info-row {
  display: flex;
}
.info-row--full {
  grid-column: 1 / -1;
  align-items: flex-start;
}
.info-row--full .label {
  align-self: flex-start;
  padding-top: 2px;
  white-space: nowrap;
}
.info-row .label {
  width: 92px;
  color: #909399;
  font-size: 14px;
  white-space: nowrap;
  flex-shrink: 0;
}
.info-row .value {
  color: #303133;
  font-weight: 500;
  font-size: 14px;
}
.info-row--full .value {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
}
</style>
