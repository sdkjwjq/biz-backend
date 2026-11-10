<template>
  <el-drawer :model-value="modelValue" :title="`${titles[module] || '业务'} · ${data?.label || '统计分析'}`" size="min(1120px, calc(100vw - 24px))" destroy-on-close class="overview-drawer" @update:model-value="emit('update:modelValue', $event)">
    <div class="analysis-body" v-loading="loading">
      <div class="analysis-heading"><strong>{{ year }}年{{ month }}月</strong><el-tag type="info" effect="plain">{{ data?.period }}</el-tag><span v-if="data">{{ data.filterApplied ? '筛选前统计 · ' : '' }}{{ data.label }}：{{ display(data.value) }}{{ data.value == null ? '' : data.unit }}</span></div>
      <p class="analysis-note">{{ data?.note }}</p>
      <el-alert v-if="error" type="error" :title="error" :closable="false" show-icon><el-button link @click="load">重试分析</el-button></el-alert>
      <QueryFilterBar v-if="modelValue" :key="metric" ref="analysisQuery" :domain="`overview-${metric}`" endpoint="/admin/overview/analysis" title="统计分析" :context="{year,month,metric,dimension:dimension||undefined,groupKey:groupKey||undefined,page,pageSize:10}" @result="receiveAnalysis" />
      <template v-if="data && !error">
        <el-alert v-if="data.extra?.warning" :title="data.extra.warning" type="warning" :closable="false" />
        <div class="analysis-controls">
          <label>分析维度<el-select v-model="dimension" aria-label="分析维度" @change="changeDimension"><el-option v-for="d in data.dimensions" :key="d" :value="d" :label="dimensionNames[d]" /></el-select></label>
          <el-select v-if="dimension === 'trend'" v-model="trendView" aria-label="预算趋势视图"><el-option value="cumulative" label="累计支出金额" /><el-option value="incremental" label="当月新增支出" /><el-option value="executionRate" label="年度预算执行率" /></el-select>
          <el-tag v-if="groupKey" closable @close="clearGroup">{{ groupName }}</el-tag>
        </div>
        <OverviewChart v-if="data.chart.length" :data="data" :trend-view="trendView" @select="selectGroup" />
        <el-empty v-else description="此指标暂无可分析数据" :image-size="64" />
        <div v-if="data.groupable && data.chart.length" class="chart-groups" aria-label="图表分组筛选">
          <span>筛选明细：</span><el-button v-for="g in data.chart" :key="g.key" size="small" :type="groupKey === g.key ? 'primary' : 'default'" @click="selectGroup(g)">{{ g.name }}（{{ display(g.value) }}）</el-button>
        </div>
        <p v-if="dimension === 'trend' && data.chart.some(p => p.adjustment)" class="adjustment-note">存在负增长，可能为财务调账；缺月和缺值保持空白。</p>
        <div class="detail-heading"><h3>数据明细</h3><span>共 {{ data.total }} {{ module === 'records' ? '人' : '条' }}<template v-if="groupKey"> · 已按图表分组筛选</template></span></div>
        <el-table :data="data.rows" border class="analysis-table" empty-text="暂无符合条件的记录">
          <el-table-column v-if="module !== 'budget'" prop="code" :label="module === 'records' ? '工号' : '编号'" width="110" />
          <el-table-column prop="name" :label="module === 'records' ? '填报人' : '名称'" min-width="220"><template #default="{ row }"><span class="detail-name">{{ row.name }}</span></template></el-table-column>
          <el-table-column v-if="['tasks', 'performance', 'achievements', 'records'].includes(module)" prop="department" :label="module === 'achievements' ? '报送部门' : '部门'" min-width="150" />
          <el-table-column v-if="module !== 'budget'" prop="status" label="状态" min-width="110" />
          <el-table-column v-if="module === 'tasks'" label="推进进度" width="100"><template #default="{ row }">{{ display(row.progress) }}%</template></el-table-column>
          <el-table-column v-if="module === 'tasks'" label="标记" min-width="120"><template #default="{ row }"><el-tag v-if="row.priority" type="warning" size="small">重点</el-tag> <el-tag v-if="row.difficult" type="danger" size="small">困难</el-tag> <el-tag v-if="row.overdue" type="danger" size="small">逾期</el-tag></template></el-table-column>
          <template v-if="['performance', 'core', 'budget'].includes(module)">
            <el-table-column :label="module === 'budget' ? '分项预算' : module === 'core' ? '期满目标' : '年度目标'" width="110"><template #default="{ row }">{{ display(row.target) }}</template></el-table-column>
            <el-table-column :label="module === 'budget' ? '累计支出' : module === 'core' ? '归档完成值' : '年度完成值'" width="110"><template #default="{ row }">{{ display(row.value) }}</template></el-table-column>
          </template>
          <el-table-column v-if="['core', 'budget'].includes(module)" label="达成比例" width="110"><template #default="{ row }">{{ row.ratio == null ? '—' : `${display(row.ratio)}%` }}</template></el-table-column>
          <el-table-column v-if="module === 'core'" label="归档所属月份" width="110"><template #default="{ row }">{{ row.month == null ? '—' : `${row.month}月` }}</template></el-table-column>
          <el-table-column v-if="module === 'achievements'" prop="quantity" label="奖项数量" width="90" />
          <el-table-column v-if="module === 'achievements'" prop="reason" label="驳回原因" min-width="180" />
          <el-table-column label="查看" width="90"><template #default="{ row }"><el-button v-if="module !== 'records' || row.recordId" link type="primary" @click="open(row)">{{ module === 'core' ? '历史' : '详情' }}</el-button><span v-else>—</span></template></el-table-column>
        </el-table>
        <el-pagination v-model:current-page="page" :page-size="10" :total="data.total" :pager-count="5" layout="prev, pager, next, total" @current-change="load" />
      </template>
    </div>
    <template #footer><el-button @click="emit('update:modelValue', false)">关闭分析</el-button></template>
  </el-drawer>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import QueryFilterBar from '../QueryFilterBar.vue'
import OverviewChart from './OverviewChart.vue'
const props = defineProps({ modelValue: Boolean, metric: String, year: Number, month: Number })
const emit = defineEmits(['update:modelValue']), router = useRouter()
const titles = { tasks: '任务推进', performance: '绩效达成', core: '核心指标', achievements: '建设成果', budget: '预算执行', records: '工作纪实' }
const dimensionNames = { status: '状态分布', reform: '十大改革任务对比', department: '部门对比', category: '类别对比', level: '级别对比', indicator: '十项指标达成比例', structure: '支出结构', trend: '逐月趋势' }
const module = computed(() => props.metric?.split('.')[0]), data = ref(null), error = ref(''), loading = ref(false)
const dimension = ref(''), groupKey = ref(''), groupName = ref(''), page = ref(1), trendView = ref('cumulative')
const analysisQuery=ref(null)
const display = value => value == null ? '—' : new Intl.NumberFormat('zh-CN', { maximumFractionDigits: 4 }).format(value)
function receiveAnalysis(result){data.value=result;dimension.value=result.dimension;page.value=result.page;error.value=''}
function load(){return analysisQuery.value?.refresh()}
function selectGroup(g) { if (!data.value?.groupable) return; groupKey.value = String(g.key); groupName.value = g.name; page.value = 1; load() }
function clearGroup() { groupKey.value = ''; groupName.value = ''; page.value = 1; load() }
function changeDimension() { groupKey.value = ''; groupName.value = ''; page.value = 1; load() }
function open(row) {
  const query = { year: props.year }, path = { task: '/home/works', performance: '/home/works/performance', achievement: '/home/works/achievement', core: '/home/core-indicators', budget: '/home/works/budget', record: '/home/work-records' }[row.kind]
  if (row.kind === 'task') query.taskId = row.id
  if (row.kind === 'performance') query.perfId = row.id
  if (row.kind === 'achievement') query.achId = row.id
  if (row.kind === 'core') query.indicatorId = row.id
  if (row.kind === 'budget' || row.kind === 'record') query.month = props.month
  if (row.kind === 'record') query.recordId = row.recordId
  emit('update:modelValue', false); router.push({ path, query })
}
watch(() => [props.modelValue, props.metric, props.year, props.month], () => {
  data.value = null; error.value = ''; loading.value = false
  if (props.modelValue) { dimension.value = ''; groupKey.value = ''; groupName.value = ''; page.value = 1; load() }
})
</script>

<style scoped>
.analysis-body{min-width:0;min-height:150px}.analysis-heading{display:flex;align-items:center;gap:12px;flex-wrap:wrap;color:#64748b;font-size:13px}.analysis-heading strong{color:#26364a;font-size:18px}.analysis-note{color:#8792a1;font-size:12px;line-height:1.7}.analysis-controls{display:flex;align-items:center;gap:12px;flex-wrap:wrap;margin:20px 0 10px}.analysis-controls label{display:flex;align-items:center;gap:10px;font-size:13px;flex-wrap:wrap}.analysis-controls .el-select{width:175px}.chart-groups{display:flex;gap:7px;align-items:center;flex-wrap:wrap;color:#8792a1;font-size:12px;margin:10px 0 20px}.chart-groups .el-button{margin-left:0}.detail-heading{display:flex;align-items:center;gap:12px;margin:20px 0 12px}.detail-heading h3{font-size:16px;margin:0}.detail-heading span{font-size:12px;color:#8792a1}.analysis-table{width:100%}.detail-name{white-space:normal;overflow-wrap:anywhere;line-height:1.6}.el-pagination{margin-top:16px;overflow-x:auto;padding-bottom:6px}.adjustment-note{color:#b48226;font-size:12px}
</style>
