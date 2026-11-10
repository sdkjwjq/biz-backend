<template>
  <div class="chart-container"><div ref="element" class="overview-chart" :aria-label="`${data.label}图表`" role="img" /><p v-if="failed" class="chart-failure">图表加载失败，仍可使用下方分组和明细。<el-button link @click="render">重试图表</el-button></p></div>
</template>
<script setup>
import { ref, watch, nextTick, onMounted, onBeforeUnmount } from 'vue'
const props = defineProps({ data: { type: Object, required: true }, trendView: String }), emit = defineEmits(['select'])
const element = ref(), failed = ref(false); let chart, observer, version = 0, disposed = false
async function render() {
  const current = ++version; failed.value = false
  try {
    const echarts = await import('echarts'); await nextTick()
    if (disposed || version !== current || !element.value) return
    if (!chart) { chart = echarts.init(element.value); chart.on('click', event => { const row = props.data.chart[event.dataIndex]; if (row && props.data.groupable) emit('select', row) }) }
    const d = props.data, trend = d.dimension === 'trend', pie = ['status', 'structure'].includes(d.dimension)
    const names = d.chart.map(r => trend ? `${r.month}月` : r.name), values = d.chart.map(r => trend ? r[props.trendView] : r.value)
    const palette = ['#4b89dc', '#64ac9a', '#e4b45c', '#a28abd', '#e38d87', '#7fa9b2']
    const option = { color: palette, tooltip: { trigger: pie ? 'item' : 'axis', confine: true, renderMode: 'richText' }, grid: { left: 12, right: 24, top: 28, bottom: 25, containLabel: true }, animation: false }
    if (pie) option.series = [{ type: 'pie', stillShowZeroSum: false, radius: ['38%', '64%'], center: ['50%', '48%'], label: { formatter: '{b}: {c}' }, data: d.chart.filter(r => r.value != null).map(r => ({ name: r.name, value: r.value, originalKey: r.key })) }]
    else if (trend) {
      option.xAxis = { type: 'category', data: names }; option.yAxis = { type: 'value', name: props.trendView === 'executionRate' ? '%' : '万元' }
      option.series = [{ type: 'line', connectNulls: false, smooth: false, symbolSize: 7, data: values }]
    } else {
      option.xAxis = { type: 'value', name: d.chartUnit || (d.module === 'core' ? '%' : d.module === 'budget' ? '万元' : '数量') }
      option.yAxis = { type: 'category', inverse: true, data: names, axisLabel: { width: 170, overflow: 'truncate' } }
      option.series = d.module === 'records' && d.dimension === 'department'
        ? [{ name: '已提交', type: 'bar', stack: 'records', data: d.chart.map(r => r.submitted) }, { name: '未提交', type: 'bar', stack: 'records', data: d.chart.map(r => r.missing) }]
        : [{ type: 'bar', barMaxWidth: 24, data: values }]
      if (d.module === 'records') option.legend = { top: 0 }
    }
    chart.off('click'); chart.on('click', event => { const row = pie ? d.chart.find(r => r.key === event.data.originalKey) : d.chart[event.dataIndex]; if (row && d.groupable) emit('select', row) })
    chart.setOption(option, true); chart.resize()
  } catch { if (!disposed && current === version) failed.value = true }
}
onMounted(() => { observer = new ResizeObserver(() => chart?.resize()); observer.observe(element.value); render() })
watch(() => [props.data, props.trendView], render)
onBeforeUnmount(() => { disposed = true; version++; observer?.disconnect(); chart?.dispose(); chart = null })
</script>
<style scoped>
.chart-container{width:100%;min-width:0}.overview-chart{width:100%;height:340px;min-width:0}.chart-failure{font-size:12px;color:#a7772a}@media(max-width:600px){.overview-chart{height:300px}}
</style>
