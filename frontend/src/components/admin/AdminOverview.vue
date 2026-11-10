<template>
  <div class="overview-page">
    <header class="dashboard-head">
      <div><p class="eyebrow">建设运行 · 综合数据</p><h2>管理概览</h2><p class="subtitle">按业务分类查看进展，点击统计数字分析明细。</p></div>
      <div class="dashboard-controls">
        <label>年度<el-select v-model="year" aria-label="看板年度" @change="reload"><el-option v-for="y in years" :key="y" :value="y" :label="`${y}年`" /></el-select></label>
        <label>月份<el-select v-model="month" aria-label="看板月份" @change="reload"><el-option v-for="m in 12" :key="m" :value="m" :label="`${m}月`" /></el-select></label>
        <el-button :icon="Refresh" :loading="loading" @click="reload">刷新</el-button>
      </div>
    </header>
    <div class="dashboard-context"><span><i class="scope-dot" />{{ scopeLabel }}</span><span>更新时间：{{ updatedAt }}</span></div>
    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon><el-button link @click="reload">重新加载</el-button></el-alert>
    <div class="business-grid">
      <section v-for="(label, key) in titles" :key="key" class="business-panel" :data-module="key" :class="`panel-${key}`" v-loading="pending[key]">
        <header><div class="panel-heading"><span class="panel-marker" /><h3>{{ label }}</h3></div><el-tag effect="plain" type="info" size="small">{{ sections[key]?.period || periods[key] }}</el-tag></header>
        <template v-if="sections[key]?.status === 'ok'">
          <div v-if="sections[key].refreshError" class="module-warning" role="alert">上次结果尚未更新：{{ sections[key].refreshError }} <el-button link :disabled="pending[key]" @click="load(key)">重试本区</el-button></div>
          <div v-if="sections[key].extra?.warning" class="module-warning" role="status">{{ sections[key].extra.warning }}</div>
          <div class="business-metrics">
            <button v-for="metric in sections[key].metrics" :key="metric.key" :data-metric="`${key}.${metric.key}`" :disabled="pending[key]" @click="analyze(key, metric)">
              <span class="metric-label">{{ metric.label }}</span><strong>{{ display(metric.value) }}<small>{{ metric.value == null ? '' : metric.unit }}</small></strong>
              <span class="metric-link">查看分析 <span aria-hidden="true">↗</span></span>
            </button>
          </div>
          <p class="module-note">{{ sections[key].note }}</p>
          <small v-if="key === 'budget' && !sections[key].extra.missing" class="budget-month">数据所属：{{ year }}年{{ sections[key].extra.dataMonth }}月 · 万元</small>
        </template>
        <div v-else-if="sections[key]?.status === 'error'" class="module-error" role="alert"><p>{{ sections[key].message }}</p><el-button @click="load(key)">重试本区</el-button></div>
        <div v-else-if="sections[key]?.status === 'forbidden'" class="module-placeholder">{{ sections[key].message || '未获准查阅此模块' }}</div>
        <div v-else class="module-placeholder">{{ pending[key] ? '正在读取统计…' : '统计尚未加载' }}</div>
      </section>
    </div>
    <el-collapse v-model="maintenanceOpen" class="maintenance"><el-collapse-item name="maintenance"><template #title><div><strong>运行维护</strong><span>账号、配置异常与数据版本</span></div></template><AdminOverviewMaintenance v-if="maintenanceOpen.includes('maintenance')" :year="year" :month="month" /></el-collapse-item></el-collapse>
    <OverviewAnalysisDrawer v-model="drawerVisible" :metric="activeMetric" :year="year" :month="month" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { getOverviewSummary } from '@/api/admin'
import { createRequestPool } from '@/utils/requestPool'
import AdminOverviewMaintenance from './AdminOverviewMaintenance.vue'
import OverviewAnalysisDrawer from './OverviewAnalysisDrawer.vue'

const now = new Date(), years = [2025, 2026, 2027, 2028, 2029]
const year = ref(Math.max(2025, Math.min(2029, now.getFullYear()))), month = ref(now.getMonth() + 1)
const titles = { tasks: '任务推进', performance: '绩效达成', core: '核心指标', achievements: '建设成果', budget: '预算执行', records: '工作纪实' }
const periods = { tasks: '年度当前状态', performance: '年度当前状态', core: '截至所选月份', achievements: '取得年份／审核为当前状态', budget: '所选月份', records: '所选月份／当前填报资格' }
const sections = ref({}), metadata = ref({}), error = ref(''), pending = reactive({}), maintenanceOpen = ref([])
const loading = computed(() => Object.values(pending).some(Boolean))
const drawerVisible = ref(false), activeMetric = ref('')
const controllers = new Map(), pool = createRequestPool(2); let generation = 0, disposed = false, renderedKey = ''
const contextKey = () => `${year.value}:${month.value}:${localStorage.getItem('token') || ''}`
const scopeLabel = computed(() => !metadata.value.scope ? '正在核实查阅范围…' : metadata.value.scope.scope === 'DEPARTMENTS' ? '任务、绩效：分管部门；其余：全校 · 只读' : metadata.value.readOnly ? '查阅范围：全校 · 领导只读' : '查阅范围：全校 · 管理员')
const updatedAt = computed(() => metadata.value.generatedAt ? new Date(metadata.value.generatedAt).toLocaleString('zh-CN', { hour12: false }) : '—')
const display = value => value == null ? '—' : new Intl.NumberFormat('zh-CN', { maximumFractionDigits: 2 }).format(value)
function applySection(module, section) {
  const previous = sections.value[module]
  sections.value[module] = section.status === 'error' && !section.accessDenied && previous?.status === 'ok'
    ? { ...previous, refreshError: section.message || '统计加载失败' } : section
}
function invalidate(message) {
  generation++; controllers.forEach(c => c.abort()); controllers.clear(); pool.clearQueued()
  Object.keys(pending).forEach(key => { pending[key] = false })
  sections.value = {}; metadata.value = {}; drawerVisible.value = false; error.value = message
}
function load(module) {
  if (disposed) return Promise.resolve()
  if (renderedKey !== contextKey()) return reload()
  const current = generation, key = renderedKey, params = { year: year.value, month: month.value, module }
  const valid = () => !disposed && current === generation && key === contextKey()
  pending[module] = true
  return pool.run(`${current}:${module}`, async () => {
    if (!valid()) return
    const controller = new AbortController(); controllers.set(module, controller)
    try {
      const result = await getOverviewSummary(params, { signal: controller.signal })
      if (!disposed && current === generation && key !== contextKey()) invalidate('登录状态已变化，请刷新')
      if (!valid()) return
      if (!result.modules?.[module]) throw new Error('统计结果不完整，请重试')
      applySection(module, result.modules[module]); metadata.value = result; error.value = ''
    } catch (e) {
      if (!disposed && current === generation && key !== contextKey()) invalidate('登录状态已变化，请刷新')
      if (!valid() || e.name === 'AbortError') return
      if ([401, 403, 428].includes(e.responseCode)) {
        invalidate(e.message || '查阅权限已失效，请刷新')
      } else applySection(module, { status: 'error', message: e.message || '统计加载失败' })
    } finally {
      if (valid()) { pending[module] = false; if (controllers.get(module) === controller) controllers.delete(module) }
    }
  })
}
function reload() {
  if (disposed) return Promise.resolve()
  generation++; controllers.forEach(c => c.abort()); controllers.clear(); pool.clearQueued()
  const nextKey = contextKey()
  if (renderedKey !== nextKey) { sections.value = {}; metadata.value = {} }
  renderedKey = nextKey; error.value = ''; drawerVisible.value = false
  return Promise.all(Object.keys(titles).map(key => load(key)))
}
function analyze(module, metric) { activeMetric.value = `${module}.${metric.key}`; drawerVisible.value = true }
onMounted(reload)
onBeforeUnmount(() => { disposed = true; generation++; controllers.forEach(c => c.abort()); pool.dispose() })
</script>

<style scoped>
.overview-page{height:100%;overflow:auto;min-width:0;min-height:0;box-sizing:border-box;padding:24px;background:#f3f6fa;color:#26364a}.dashboard-head{display:flex;align-items:center;justify-content:space-between;gap:20px;flex-wrap:wrap}.eyebrow{color:#4774ab;font-size:12px;letter-spacing:2px;margin:0 0 6px}.dashboard-head h2{font-size:25px;margin:0 0 8px}.subtitle{color:#758294;font-size:13px;margin:0}.dashboard-controls{display:flex;align-items:flex-end;gap:12px;flex-wrap:wrap}.dashboard-controls label{display:flex;flex-direction:column;gap:6px;font-size:12px;color:#68778b}.dashboard-controls .el-select{width:110px}.dashboard-context{display:flex;justify-content:space-between;flex-wrap:wrap;gap:8px;font-size:12px;color:#748195;padding:20px 0 16px}.scope-dot{display:inline-block;width:6px;height:6px;border-radius:50%;background:#409eff;margin-right:7px}.business-grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(min(100%,540px),1fr));gap:18px}.business-panel{padding:20px;background:white;border:1px solid #e5eaf2;border-radius:10px;min-width:0}.business-panel>header{display:flex;align-items:center;justify-content:space-between;gap:8px;flex-wrap:wrap;margin-bottom:18px}.panel-heading{display:flex;align-items:center;gap:9px}.panel-heading h3{font-size:17px;margin:0}.panel-marker{width:4px;height:17px;border-radius:2px;background:#409eff}.panel-performance .panel-marker{background:#4c9f8d}.panel-core .panel-marker{background:#8b78b7}.panel-achievements .panel-marker{background:#d69949}.panel-budget .panel-marker{background:#5e9eaa}.panel-records .panel-marker{background:#7a8eaa}.business-metrics{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:8px}.business-metrics button{font:inherit;border:1px solid transparent;border-radius:7px;background:#f7f9fc;text-align:left;padding:14px 12px;cursor:pointer;min-width:0;color:inherit}.business-metrics button:hover,.business-metrics button:focus-visible{border-color:#99c5ff;background:#eff6ff;outline:2px solid transparent}.metric-label{display:block;color:#6c7a8d;font-size:12px}.business-metrics strong{display:block;font-size:27px;font-weight:600;margin:7px 0;color:#244b7a;overflow-wrap:anywhere}.business-metrics strong small{font-size:11px;margin-left:5px;color:#8b98a9;font-weight:400}.metric-link{font-size:11px;color:#4978ad}.metric-link span{margin-left:4px}.module-note{font-size:12px;color:#8a95a4;line-height:1.65;margin:16px 0 0}.module-warning{padding:8px 10px;background:#fff7e8;color:#9a681d;font-size:12px;border-radius:5px;margin-bottom:12px}.module-error,.module-placeholder{min-height:140px;display:flex;align-items:center;justify-content:center;gap:10px;flex-direction:column;color:#8792a1;font-size:13px}.budget-month{display:block;margin-top:8px;color:#748195}.maintenance{margin-top:20px;background:white;border-radius:10px;padding:0 18px;border:1px solid #e5eaf2}.maintenance :deep(.el-collapse-item__header){min-height:56px;height:auto;line-height:1.6}.maintenance strong{color:#40516a;font-size:15px}.maintenance span{margin-left:14px;color:#8a95a4;font-size:12px}@media(max-width:700px){.overview-page{padding:14px}.business-metrics{grid-template-columns:repeat(2,minmax(0,1fr))}.business-panel{padding:15px}.maintenance span{display:block;margin-left:0}}
</style>
