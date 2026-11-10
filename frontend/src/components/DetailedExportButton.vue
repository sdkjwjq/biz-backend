<template>
  <el-button v-if="allowed" :loading="busy" :disabled="disabled || busy" :title="hint" @click="download">
    {{ scope === 'all' ? '导出全量明细' : '导出筛选明细' }}
  </el-button>
</template>
<script setup>
import { computed, onUnmounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { get } from '@/utils/request'
import { accessState, hasPermission } from '@/utils/accessState'
import { dataExportUrl, exportDomain, exportModules } from '@/utils/dataExports.mjs'
const props = defineProps({ domain: { type: String, required: true }, scope: { type: String, default: 'all' }, filters: { type: Object, default: () => ({}) }, context: { type: Object, default: () => ({}) }, disabled: Boolean })
const busy = ref(false)
let controller, disposed = false
const module = computed(() => exportModules[exportDomain(props.domain)])
const allowed = computed(() => {
  if (accessState.experience !== 'BUSINESS' || !module.value) return false
  const permission = module.value[1]
  return permission === 'admin' ? accessState.isAdmin : hasPermission(permission) && (exportDomain(props.domain) !== 'budget-versions' || hasPermission('budget.versions'))
})
const hint = computed(() => props.scope === 'all' ? '导出当前账号获准查看的全部已保存数据，跨年度、不限当前页；遵守草稿和历史记录权限。' : '导出已生效筛选条件下的全部明细，不限当前页；尚未保存的修改不在导出内。')
async function download() {
  if (busy.value || !allowed.value) return
  const token = localStorage.getItem('token')
  const url = dataExportUrl(props.domain, props.scope, props.filters, props.context)
  busy.value = true; controller = new AbortController()
  try {
    const blob = await get(url, {}, { responseType: 'blob', signal: controller.signal })
    if (disposed || token !== localStorage.getItem('token')) return
    const href = URL.createObjectURL(blob), link = document.createElement('a')
    link.href = href; link.download = `${module.value[0]}-${props.scope === 'all' ? '全量明细' : '筛选明细'}-${new Date().toLocaleDateString('sv-SE')}.xlsx`
    document.body.appendChild(link); link.click(); link.remove()
    setTimeout(() => URL.revokeObjectURL(href), 1000)
    ElMessage.success('明细已导出')
  } catch (e) { if (!disposed && e.name !== 'AbortError') ElMessage.error(e.message || '导出失败，请重试') }
  finally { busy.value = false }
}
onUnmounted(() => { disposed = true; controller?.abort() })
</script>
