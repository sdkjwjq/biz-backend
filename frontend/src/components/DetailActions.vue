<template>
  <div class="detail-actions">
    <el-button size="small" type="primary" plain :icon="Back" :loading="locating"
      :disabled="busy || locating" @click="returnToList">返回列表并定位</el-button>
    <el-button size="small" :icon="CopyDocument" :disabled="busy" @click="copyLink">复制详情链接</el-button>
    <el-dialog v-model="linkVisible" title="手动复制详情链接" append-to-body width="520px" style="max-width: calc(100vw - 32px)">
      <p>自动复制不可用，请选中以下链接复制。</p>
      <el-input :model-value="link" readonly aria-label="详情链接" @focus="event => event.target.select()" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { Back, CopyDocument } from '@element-plus/icons-vue'
const props = defineProps({ busy: Boolean, link: String, locate: Function })
const linkVisible = ref(false)
const locating = ref(false)
let disposed = false
const copyLink = async () => {
  try {
    await navigator.clipboard.writeText(props.link)
    if (!disposed) ElMessage.success('详情链接已复制')
  } catch { if (!disposed) linkVisible.value = true }
}
const returnToList = async () => {
  if (props.busy || locating.value) return
  locating.value = true
  try { await props.locate() } catch (e) { if (!disposed) ElMessage.error(e?.message || '定位失败，请重试') }
  finally { locating.value = false }
}
onBeforeUnmount(() => { disposed = true })
</script>

<style scoped>
.detail-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 6px;
}

.detail-actions .el-button { margin: 0; }
.detail-actions .el-button + .el-button { margin-left: 0; }
</style>
<style>
.detail-located-row { animation: detail-locate-highlight 3s ease-out; }
@keyframes detail-locate-highlight { from { background-color: #c6e2ff; } to { background-color: transparent; } }
.el-table .detail-located-row > td.el-table__cell { background-color: inherit; }
</style>
