<template>
  <div class="adjacent-details">
    <span v-if="index >= 0">{{ index + 1 }} / {{ items.length }}</span>
    <el-button :disabled="busy || switching || index <= 0" @click="move(-1)">上一个</el-button>
    <el-button :disabled="busy || switching || index < 0 || index >= items.length - 1" @click="move(1)">下一个</el-button>
  </div>
</template>
<script setup>
import { ref, computed, onBeforeUnmount } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
const props = defineProps({ items: Array, currentKey: String, rowKey: Function, busy: Boolean, dirty: Function, open: Function })
const switching = ref(false)
let disposed = false
onBeforeUnmount(() => { disposed = true })
const index = computed(() => props.items.findIndex(row => String(props.rowKey(row)) === props.currentKey))
const move = async delta => {
  if (props.busy || switching.value || index.value < 0) return
  const row = props.items[index.value + delta]
  if (!row) return
  const key = props.currentKey
  switching.value = true
  try {
    if (props.dirty()) await ElMessageBox.confirm('当前详情有未提交的内容，切换将放弃这些内容。是否继续？', '切换详情', {
      confirmButtonText: '放弃并切换', cancelButtonText: '继续编辑', type: 'warning'
    })
    if (disposed || props.busy || key !== props.currentKey || !props.items.some(item => props.rowKey(item) === props.rowKey(row))) return
    await props.open(row)
  } catch (e) { if (e !== 'cancel' && e !== 'close') ElMessage.error(e?.message || '切换失败，请重试') }
  finally { switching.value = false }
}
</script>
<style scoped>
.adjacent-details { display: flex; align-items: center; justify-content: flex-end; flex-wrap: wrap; gap: 8px; margin-bottom: 12px; }
.adjacent-details .el-button + .el-button { margin-left: 0; }
</style>
