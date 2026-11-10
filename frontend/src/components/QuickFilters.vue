<template>
  <div class="quick-filters" aria-label="快捷筛选">
    <el-radio-group :model-value="modelValue" :disabled="loading" @change="value => emit('update:modelValue', value)">
      <el-radio-button label="all">全部</el-radio-button>
      <el-radio-button label="mine">{{ mineLabel }}</el-radio-button>
      <el-radio-button label="todo">待我审核</el-radio-button>
      <el-radio-button label="returned">已退回</el-radio-button>
    </el-radio-group>
    <el-button :disabled="loading" @click="emit('reset')">重置筛选</el-button>
    <span v-if="error" role="alert">{{ error }} <el-button link type="primary" @click="emit('retry')">重试</el-button></span>
  </div>
</template>
<script setup>
defineProps({ modelValue: String, loading: Boolean, error: String, mineLabel: { type: String, default: '我负责的' } })
const emit = defineEmits(['update:modelValue', 'reset', 'retry'])
</script>
<style scoped>
.quick-filters { display: flex; flex-wrap: wrap; align-items: center; gap: 12px; margin: 0 20px 16px; }
.quick-filters span { color: #e6a23c; font-size: 13px; }
</style>
