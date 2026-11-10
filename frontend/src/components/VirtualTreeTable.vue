<script>
import { defineComponent, h, ref, shallowRef, computed, watch, onMounted, onBeforeUnmount, nextTick, Fragment, Comment } from 'vue'
import { ElTableV2, ElCheckbox, ElTooltip, ElIcon } from 'element-plus'
import { ArrowRight } from '@element-plus/icons-vue'

const flattenColumns = nodes => (nodes || []).flatMap(node => {
  if (!node || node.type === Comment) return []
  if (Array.isArray(node)) return flattenColumns(node)
  if (node.type === Fragment) return flattenColumns(node.children)
  return node.props && (node.props.label || node.props.type === 'selection') ? [node] : []
})
const prop = (node, key) => node.props?.[key] ?? node.props?.[key.replace(/[A-Z]/g, c => '-' + c.toLowerCase())]
const OverflowCell = defineComponent({
  props: { content: String },
  setup(props, { slots }) {
    const element = ref(), clipped = ref(false)
    return () => h(ElTooltip, { content: props.content, disabled: !clipped.value, showAfter: 400 }, {
      default: () => h('div', { ref: element, class: 'virtual-overflow-cell', onMouseenter: () => { clipped.value = element.value?.scrollWidth > element.value?.clientWidth } }, slots.default?.())
    })
  }
})

export default defineComponent({
  name: 'VirtualTreeTable', inheritAttrs: false,
  props: {
    data: { type: Array, default: () => [] }, rowKey: { type: String, default: 'id' },
    expandRowKeys: { type: Array, default: () => [] }, emptyText: { type: String, default: '暂无数据' },
    rowClassName: Function, headerCellStyle: Object, treeProps: Object, border: Boolean, selectionVisible: Boolean, preserveSelection: Boolean
  },
  emits: ['expand-change', 'row-click', 'selection-change'],
  setup(props, { attrs, slots, emit, expose }) {
    const host = ref(), table = ref(), dimensions = shallowRef({ width: 900, height: 400 }), selected = shallowRef(new Set()), current = ref(''), layoutEpoch = ref(0)
    const selectedRows = computed(() => leaves.value.filter(row => selected.value.has(key(row))))
    const key = row => String(row[props.rowKey])
    const children = row => row[props.treeProps?.children || 'children'] || []
    const all = computed(() => { const list = []; const walk = rows => rows.forEach(row => { list.push(row); walk(children(row)) }); walk(props.data); return list })
    const depths = computed(() => { const result = new Map(); const walk = (rows, depth) => rows.forEach(row => { result.set(key(row), depth); walk(children(row), depth + 1) }); walk(props.data, 0); return result })
    const leaves = computed(() => all.value.filter(row => Number(row.level) === 3))
    const visible = computed(() => { const expanded = new Set(props.expandRowKeys.map(String)), list = []; const walk = rows => rows.forEach(row => { list.push(row); if (expanded.has(key(row))) walk(children(row)) }); walk(props.data); return list })
    const heights = new Map(), rowElements = new Set()
    let observer, rowObserver, resizeFrame, measureFrame, disposed = false, lastWidth = 0, restoreVersion = 0
    const scroll = { scrollTop: 0, scrollLeft: 0 }
    const descendantKeys = row => { const ids = []; const walk = node => { if (Number(node.level) === 3) ids.push(key(node)); else children(node).forEach(walk) }; walk(row); return ids }
    const check = ids => ({ checked: ids.length > 0 && ids.every(id => selected.value.has(id)), partial: ids.some(id => selected.value.has(id)) && !ids.every(id => selected.value.has(id)) })
    const publish = () => emit('selection-change', selectedRows.value)
    const toggle = (row, value) => { const ids = row ? descendantKeys(row) : leaves.value.map(key), next = new Set(selected.value); ids.forEach(id => value ? next.add(id) : next.delete(id)); selected.value = next; publish() }
    const clearSelection = () => { selected.value = new Set(); publish() }
    watch(() => props.data, () => {
      if (!props.preserveSelection) return clearSelection()
      const valid = new Set(leaves.value.map(key))
      selected.value = new Set([...selected.value].filter(id => valid.has(id)))
      publish()
    })
    const checkbox = (row, header = false) => {
      const ids = row ? descendantKeys(row) : leaves.value.map(key), status = check(ids)
      return h('div', { class: 'virtual-checkbox-cell', onClick: event => event.stopPropagation() }, [h(ElCheckbox, { modelValue: status.checked, indeterminate: status.partial, disabled: !ids.length, 'aria-label': header ? '全选当前筛选结果' : `选择${row.name || ''}`, onChange: value => toggle(row, value) })])
    }
    const rowClass = ({ rowData, rowIndex }) => ['el-table__row', props.rowClassName?.({ row: rowData, rowIndex }), key(rowData) === current.value && 'is-current'].filter(Boolean).join(' ')
    const updateDimensions = () => {
      if (disposed || !host.value?.parentElement) return
      const parent = host.value.parentElement, width = Math.max(1, parent.clientWidth)
      const available = Math.max(180, window.innerHeight - host.value.getBoundingClientRect().top - 24)
      const anchor = lastWidth && lastWidth !== width ? getScrollState() : null
      if (anchor) layoutEpoch.value += 1
      lastWidth = width
      const content = 41 + visible.value.reduce((sum, row) => sum + (heights.get(key(row)) || 64), 0) + 10
      const height = props.data.length ? Math.min(available, Math.max(100, content)) : Math.min(available, 180)
      if (dimensions.value.width !== width || Math.abs(dimensions.value.height - height) >= 4) dimensions.value = { width, height }
      if (anchor) nextTick(() => restoreScrollState(anchor))
    }
    const scheduleResize = () => { cancelAnimationFrame(resizeFrame); resizeFrame = requestAnimationFrame(updateDimensions) }
    const measureRows = () => {
      cancelAnimationFrame(measureFrame)
      measureFrame = requestAnimationFrame(() => {
        if (disposed) return
        const next = new Set(host.value?.querySelectorAll('.el-table-v2__main .el-table-v2__row') || [])
        for (const element of rowElements) if (!next.has(element)) { rowObserver?.unobserve(element); rowElements.delete(element) }
        for (const element of next) { if (!rowElements.has(element)) { rowElements.add(element); rowObserver?.observe(element) } const id = element.dataset.rowKey; if (id) heights.set(id, element.getBoundingClientRect().height) }
        updateDimensions()
      })
    }
    const frame = () => new Promise(resolve => requestAnimationFrame(resolve))
    const ensureRowVisible = async (id, align = 'center', valid = () => true) => {
      await nextTick()
      // Expansion, variable row measurement and a width remount can complete in
      // different animation frames. Retry the requested row after those updates.
      for (let attempt = 0; attempt < 8 && !disposed && valid(); attempt++) {
        const index = visible.value.findIndex(row => key(row) === String(id)); if (index < 0) return
        table.value?.scrollToRow(index, align)
        await nextTick(); await frame(); await frame(); await nextTick()
        const row = [...(host.value?.querySelectorAll('.el-table-v2__main .el-table-v2__row') || [])].find(el => el.dataset.rowKey === String(id))
        const bounds = row?.getBoundingClientRect(), viewport = host.value?.getBoundingClientRect()
        if (bounds && viewport && bounds.bottom > viewport.top + 40 && bounds.top < viewport.bottom) return
      }
    }
    const getScrollState = () => {
      const top = host.value?.getBoundingClientRect().top || 0
      const anchor = [...(host.value?.querySelectorAll('.el-table-v2__main .el-table-v2__row') || [])].find(el => el.getBoundingClientRect().bottom > top + 40)
      return { ...scroll, rowKey: anchor?.dataset.rowKey, anchor: anchor ? 'detail-row-' + anchor.dataset.rowKey : '', offset: anchor ? anchor.getBoundingClientRect().top - top : 0 }
    }
    const restoreScrollState = async saved => {
      const version = ++restoreVersion
      const valid = () => !disposed && version === restoreVersion
      if (!saved || !visible.value.length) return false
      await nextTick(); await frame()
      if (!valid()) return false
      const body = host.value?.querySelector('.el-table-v2__main .el-table-v2__body > div')
      if (!body?.clientHeight) return false
      const nonnegative = value => Number.isFinite(Number(value)) ? Math.max(0, Number(value)) : 0
      const boundedScroll = (top, left) => table.value?.scrollTo({
        scrollTop: Math.min(nonnegative(top), Math.max(0, body.scrollHeight - body.clientHeight)),
        scrollLeft: Math.min(nonnegative(left), Math.max(0, body.scrollWidth - body.clientWidth))
      })
      const id = saved.rowKey || saved.anchor?.replace(/^detail-row-/, '')
      if (id && visible.value.some(row => key(row) === String(id))) {
        await ensureRowVisible(id, 'start', valid)
        if (!valid()) return false
        const element = host.value?.querySelector('.el-table-v2__main .' + CSS.escape('detail-row-' + id))
        const delta = element ? element.getBoundingClientRect().top - host.value.getBoundingClientRect().top - (Number(saved.offset) || 0) : 0
        boundedScroll(scroll.scrollTop + delta, saved.scrollLeft)
      } else boundedScroll(id ? 0 : saved.scrollTop, saved.scrollLeft)
      return true
    }
    expose({ get $el() { return host.value }, clearSelection, getSelectionRows: () => selectedRows.value, toggleRowSelection: (row, value) => toggle(row, value ?? !check(descendantKeys(row)).checked), setCurrentRow: row => { current.value = row ? key(row) : '' }, ensureRowVisible, getScrollState, restoreScrollState, scrollToTop: value => table.value?.scrollToTop(value) })
    watch(visible, () => { scheduleResize(); measureRows() }, { flush: 'post' })
    onMounted(() => {
      observer = new ResizeObserver(scheduleResize); observer.observe(host.value.parentElement)
      rowObserver = new ResizeObserver(entries => { let changed=false; for (const entry of entries) { const id=entry.target.dataset.rowKey; if(!id)continue;const height=Math.round(entry.target.getBoundingClientRect().height);if(Math.abs((heights.get(id)||0)-height)>=1){heights.set(id,height);changed=true} } if(changed)scheduleResize() })
      updateDimensions()
    })
    onBeforeUnmount(() => { disposed = true; restoreVersion++; observer?.disconnect(); rowObserver?.disconnect(); cancelAnimationFrame(resizeFrame); cancelAnimationFrame(measureFrame) })
    return () => {
      const specifications = flattenColumns(slots.default?.()).filter(node => prop(node, 'type') !== 'selection' || props.selectionVisible)
      const widths = specifications.map(node => Number(prop(node, 'width') || prop(node, 'minWidth') || 100))
      const flexible = specifications.map((node, i) => !prop(node, 'width') ? i : -1).filter(i => i >= 0), remaining = Math.max(0, dimensions.value.width - widths.reduce((a, b) => a + b, 0))
      for (const index of flexible) widths[index] += remaining / Math.max(1, flexible.length)
      const treeIndex = specifications.findIndex(node => prop(node, 'type') !== 'selection')
      const columns = specifications.map((node, index) => {
        const selection = prop(node, 'type') === 'selection', column = { key: selection ? '__selection' : String(prop(node, 'prop') || prop(node, 'label') || index), dataKey: prop(node, 'prop'), title: prop(node, 'label') || '', width: widths[index], fixed: prop(node, 'fixed') === true ? 'left' : prop(node, 'fixed'), align: prop(node, 'align') || 'left', class: prop(node, 'className') }
        if (selection) { column.headerCellRenderer = () => checkbox(null, true); column.cellRenderer = ({ rowData }) => checkbox(rowData) }
        else column.cellRenderer = ({ rowData, rowIndex }) => {
          const content = () => node.children?.default?.({ row: rowData, $index: rowIndex, column }) || String(rowData[column.dataKey] ?? '')
          const cell = prop(node, 'showOverflowTooltip') !== undefined && prop(node, 'showOverflowTooltip') !== false ? h(OverflowCell, { content: String(rowData[column.dataKey] ?? '') }, { default: content }) : h('div', { class: 'virtual-cell-content' }, content())
          if (index !== treeIndex) return cell
          const expanded = props.expandRowKeys.map(String).includes(key(rowData))
          return h('div', { class: 'virtual-tree-cell' }, [
            children(rowData).length ? h('button', { type: 'button', class: ['el-table-v2__expand-icon', expanded && 'is-expanded'], style: { marginInlineStart: (depths.value.get(key(rowData)) || 0) * 16 + 'px' }, 'aria-label': `${expanded ? '收起' : '展开'}${rowData.name || ''}`, 'aria-expanded': expanded, onClick: event => { event.stopPropagation(); emit('expand-change', rowData, !expanded) } }, [h(ElIcon, { size: 12 }, { default: () => h(ArrowRight) })]) : h('span', { class: 'virtual-tree-spacer', style: { marginInlineStart: (depths.value.get(key(rowData)) || 0) * 16 + 'px' } }), cell
          ])
        }
        return column
      })
      return h('div', { ...attrs, ref: host, class: ['virtual-tree-table el-table el-table--border', attrs.class], style: [attrs.style, { width: '100%', height: dimensions.value.height + 'px' }], role: 'treegrid', 'data-model-row-count': all.value.length }, [h(ElTableV2, {
        key: layoutEpoch.value, ref: table, fixed: true, columns, data: visible.value, rowKey: props.rowKey, width: dimensions.value.width, height: dimensions.value.height, headerHeight: 40, rowHeight: 64, estimatedRowHeight: 64,
        scrollbarAlwaysOn: true, cache: Math.max(3, Math.ceil(dimensions.value.height / 64)),
        rowClass, rowProps: ({ rowData }) => ({ 'data-row-key': key(rowData), role: 'row', 'aria-level': rowData.level }), headerCellProps: { style: props.headerCellStyle },
        rowEventHandlers: { onClick: ({ rowData, event }) => { if (!event.target?.closest('.el-table-v2__expand-icon')) emit('row-click', rowData, null, event) } }, onRowExpand: ({ rowData, expanded }) => emit('expand-change', rowData, expanded),
        onScroll: value => { Object.assign(scroll, value); host.value?.dispatchEvent(new CustomEvent('virtual-scroll', { bubbles: true })); measureRows() }, onRowsRendered: measureRows
      }, { empty: () => h('span', { class: 'el-table__empty-text' }, props.emptyText) })])
    }
  }
})
</script>

<style scoped>
.virtual-tree-table{position:relative;box-sizing:border-box;font-size:14px;color:#606266;background:white;border:1px solid #ebeef5;overflow:hidden}
.virtual-tree-table :deep(.el-table-v2__row){border-bottom:1px solid #ebeef5;box-sizing:border-box;min-height:54px}
.virtual-tree-table :deep(.el-table-v2__row-cell){padding:10px 12px;border-right:1px solid #ebeef5;white-space:normal;overflow-wrap:anywhere;line-height:23px;box-sizing:border-box}
.virtual-tree-table :deep(.el-table-v2__header-cell){border-right:1px solid #ebeef5;border-bottom:1px solid #ebeef5;background:#f8f9fa;color:#606266;font-weight:700;padding:0 12px}
.virtual-tree-table :deep(.el-table-v2__row.is-current){background:#ecf5ff}
.virtual-tree-table :deep(.el-table-v2__row:hover){background:#f5f7fa}
.virtual-tree-table :deep(.virtual-tree-cell){display:flex;align-items:center;width:100%;min-width:0}
.virtual-tree-table :deep(.virtual-tree-cell>.el-table-v2__expand-icon){flex:none;width:16px;height:18px;padding:0;border:0;background:transparent;cursor:pointer;color:#606266;display:flex;align-items:center;justify-content:center;margin-inline-end:4px}
.virtual-tree-table :deep(.virtual-tree-spacer){flex:none;width:16px;margin-inline-end:4px}
.virtual-tree-table :deep(.virtual-cell-content){width:100%;min-width:0;white-space:normal;overflow-wrap:anywhere}
.virtual-tree-table :deep(.virtual-checkbox-cell){display:flex;align-items:center;justify-content:center;width:100%}
.virtual-tree-table :deep(.virtual-overflow-cell){white-space:nowrap;text-overflow:ellipsis;overflow:hidden;width:100%}
.virtual-tree-table :deep(.el-table-v2__empty){display:flex;align-items:center;justify-content:center;color:#909399}
</style>
