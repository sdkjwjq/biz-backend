import { watch, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { createWorksCache } from './worksCache'

/** 仅保存当前登录会话的界面状态，不保存业务数据和表单。 */
export const useBrowseMemory = (name, { fields, expanded, table, skip }) => {
  const cache = createWorksCache()
  const key = `works_browse_v1_${name}`
  let saved = null
  try { saved = JSON.parse(cache.getItem(key) || 'null') } catch { /* 损坏缓存忽略 */ }
  if (!skip() && saved?.fields) {
    Object.entries(fields).forEach(([key, value]) => {
      const text = saved.fields[key]
      if (typeof text === 'string' && text.length <= 256) value.value = text
    })
    if (fields.quick && !['all', 'mine', 'todo', 'returned'].includes(fields.quick.value)) fields.quick.value = 'all'
  }
  let restored = false, restoring = false, disposed = false, timer
  const scrollHost = () => {
    for (let el = table.value?.$el.parentElement; el; el = el.parentElement) {
      if (/(auto|scroll)/.test(getComputedStyle(el).overflowY) && el.scrollHeight > el.clientHeight) return el
    }
    return document.scrollingElement
  }
  const save = () => {
    if (!restored || restoring || disposed) return
    if (table.value?.getScrollState) {
      cache.setItem(key, JSON.stringify({ fields: Object.fromEntries(Object.entries(fields).map(([key, value]) => [key, value.value])), expanded: expanded.value, ...table.value.getScrollState() }))
      return
    }
    const host = scrollHost()
    const top = host?.getBoundingClientRect().top || 0
    const rows = [...(table.value?.$el.querySelectorAll('.el-table__row') || [])]
    const anchor = rows.find(el => el.getClientRects().length && el.getBoundingClientRect().bottom > top)
    cache.setItem(key, JSON.stringify({ fields: Object.fromEntries(Object.entries(fields).map(([key, value]) => [key, value.value])),
      expanded: expanded.value, scrollTop: host?.scrollTop || 0,
      anchor: anchor ? [...anchor.classList].find(name => name.startsWith('detail-row-')) : '',
      offset: anchor ? anchor.getBoundingClientRect().top - top : 0 }))
  }
  const restoreTree = async tree => {
    // Capabilities/shortcut requests may finish before the first list request.
    // Restoring against that temporary empty tree loses the expansion keys and
    // puts the virtual grid at a scroll offset with no corresponding rows.
    if (restored || restoring || disposed || !Array.isArray(tree) || !tree.length) return
    restoring = true
    try {
    const ids = new Set()
    const walk = rows => (rows || []).forEach(row => { ids.add(String(row.id)); walk(row.children) })
    walk(tree)
    if (!skip() && Array.isArray(saved?.expanded)) expanded.value = saved.expanded.map(String).filter(id => ids.has(id))
    await nextTick()
    if (disposed) return
    if (!skip() && saved) {
      if (table.value?.restoreScrollState) {
        if (await table.value.restoreScrollState(saved) === false) return
      }
      else {
      const host = scrollHost()
      const anchor = typeof saved.anchor === 'string' && saved.anchor ? table.value?.$el.querySelector(`.${CSS.escape(saved.anchor)}`) : null
      if (host && anchor?.getClientRects().length) host.scrollTop += anchor.getBoundingClientRect().top - host.getBoundingClientRect().top - (Number(saved.offset) || 0)
      else if (host) host.scrollTop = Math.max(0, Number(saved.scrollTop) || 0)
      }
    }
    restored = true
    } finally { restoring = false }
    save()
  }
  const resetPosition = () => { if (table.value?.scrollToTop) table.value.scrollToTop(0); else scrollHost()?.scrollTo({ top: 0 }); save() }
  const onScroll = event => {
    const host = scrollHost()
    if (event.type === 'virtual-scroll' ? event.target !== table.value?.$el : event.target !== host && event.target !== document) return
    clearTimeout(timer)
    timer = setTimeout(save, 100)
  }
  watch([...Object.values(fields), expanded], save, { deep: true, flush: 'post' })
  onMounted(() => { document.addEventListener('scroll', onScroll, true); document.addEventListener('virtual-scroll', onScroll, true) })
  onBeforeUnmount(() => { save(); disposed = true; clearTimeout(timer); document.removeEventListener('scroll', onScroll, true); document.removeEventListener('virtual-scroll', onScroll, true) })
  return { restoreTree, resetPosition, hasSaved: !!saved }
}
