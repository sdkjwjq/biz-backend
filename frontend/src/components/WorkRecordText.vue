<template>
  <div class="record-text">
    <label :for="inputId">{{ label }}</label>
    <template v-if="!readonly">
      <el-input :id="inputId" v-model="value" type="textarea" :rows="4" :disabled="disabled" :aria-label="label" :aria-invalid="count > 300" />
      <div class="text-count" :class="{ invalid: count > 300 }"><span v-if="count > 300" role="alert">不得超过300字 </span>{{ count }}/300</div>
    </template>
    <p v-else class="readonly-text">{{ value || '未填写' }}</p>
  </div>
</template>
<script setup>
import { computed, useId } from 'vue';
defineProps({ label: String, readonly: Boolean, disabled: Boolean });
const value = defineModel({ default: '' });
const count = computed(() => Array.from(value.value || '').length);
const inputId = `work-record-text-${useId()}`;
</script>
<style scoped>
.record-text { min-width: 0; }
label { display: block; margin-bottom: 8px; font-size: 14px; color: #606266; }
.text-count { text-align: right; color: #909399; font-size: 12px; margin-top: 4px; }
.invalid { color: #f56c6c; }
.readonly-text { white-space: pre-wrap; overflow-wrap: anywhere; line-height: 1.8; margin: 0; }
</style>
