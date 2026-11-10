import { nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

export const findDetailPath = (nodes, matches, parents = []) => {
  for (const row of nodes || []) {
    const path = [...parents, row]
    if (matches(row)) return path
    const child = findDetailPath(row.children, matches, path)
    if (child) return child
  }
  return null
}
export const detailRowClass = row => `detail-row-${row.id}`
export const detailLeaves = (nodes, matches) => (nodes || []).flatMap(row => [
  ...(matches(row) ? [row] : []), ...detailLeaves(row.children, matches)
])
export const confirmDetailLeave = async dirty => {
  if (!dirty) return true
  try {
    await ElMessageBox.confirm('当前详情有未提交的内容，返回列表将放弃这些内容。是否继续？', '未提交内容', {
      confirmButtonText: '放弃并返回', cancelButtonText: '继续编辑', type: 'warning'
    })
    return true
  } catch { return false }
}
export const locateDetailRow = async ({ tree, matches, reset, expanded, table, close, valid }) => {
  let path = findDetailPath(tree(), matches)
  if (!path) {
    try {
      await ElMessageBox.confirm('当前记录不在筛选结果中。是否清除筛选并定位？', '定位记录', {
        confirmButtonText: '清除筛选并定位', cancelButtonText: '保留筛选', type: 'info'
      })
    } catch { return }
    if (!valid()) return
    await reset()
    if (!valid()) return
    path = findDetailPath(tree(), matches)
  }
  if (!path) { ElMessage.info('记录已删除或无权访问，无法定位'); return }
  if (!valid()) return
  expanded.value = [...new Set([...expanded.value.map(String), ...path.slice(0, -1).map(row => String(row.id))])]
  close()
  await nextTick()
  const row = path.at(-1)
  table.value?.setCurrentRow(row)
  await table.value?.ensureRowVisible?.(row.id, 'center')
  if (!valid()) return
  const element = table.value?.$el.querySelector(`.${CSS.escape(detailRowClass(row))}`)
  if (!table.value?.ensureRowVisible) element?.scrollIntoView({ block: 'center', behavior: 'auto' })
  element?.classList.add('detail-located-row')
  element?.addEventListener('animationend', () => element.classList.remove('detail-located-row'), { once: true })
}

// 只接受本站已匹配的业务路由，拒绝协议相对地址、外部地址和登录循环。
export const safeLoginRedirect = (router, value, fallback) => {
  if (typeof value !== 'string' || !value.startsWith('/') || value.startsWith('//') || /[\\\r\n]/.test(value)) return fallback
  try {
    const resolved = router.resolve(value)
    return resolved.matched.some(record => record.meta.requiresAuth) ? resolved.fullPath : fallback
  } catch { return fallback }
}
