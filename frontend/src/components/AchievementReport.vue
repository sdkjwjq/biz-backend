<template>
  <el-dialog title="成果统计与导出" width="90%" :model-value="modelValue" @update:model-value="value => emit('update:modelValue', value)">
    <el-form inline class="report-filters">
      <el-form-item label="时间口径"><el-select v-model="timeField" aria-label="统计时间口径" style="width:150px">
        <el-option label="成果取得时间" value="gotTime" /><el-option label="最近提交时间" value="submittedTime" /><el-option label="当前归档时间" value="archivedTime" />
      </el-select></el-form-item>
      <el-form-item label="时间范围"><el-date-picker v-model="dates" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始日期" end-placeholder="结束日期" /></el-form-item>
      <el-form-item label="状态"><el-select v-model="status" placeholder="全部" aria-label="统计状态" style="width:130px"><el-option label="全部" value="" /><el-option v-for="(label, value) in reportStatusNames" :key="value" :label="label" :value="value" /></el-select></el-form-item>
      <el-form-item label="报送部门"><el-select v-model="department" placeholder="全部" aria-label="统计部门" style="width:180px" filterable><el-option label="全部" value="" /><el-option v-for="item in departments" :key="item.id" :label="item.name" :value="item.id" /></el-select></el-form-item>
      <el-form-item label="类别"><el-select v-model="category" placeholder="全部" aria-label="统计类别" style="width:210px"><el-option label="全部" value="" /><el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" /></el-select></el-form-item>
      <el-form-item label="成果名称"><el-input v-model="keyword" placeholder="筛选成果名称" clearable /></el-form-item>
      <el-form-item><el-button @click="reset">重置统计条件</el-button></el-form-item>
    </el-form>
    <el-alert type="info" :closable="false" show-icon title="统计范围为列表已生效条件下的成果，可在此进一步筛选；成果条数与奖项数量分别计算。无时间证据的记录不纳入指定日期范围，专业群审核情况暂标记为未知。" />
    <div class="report-summary" aria-label="成果统计汇总">
      <el-tag>成果 {{ filtered.length }} 条</el-tag><el-tag type="success">已归档 {{ totals.archived }} 条</el-tag>
      <el-tag type="warning">待归档 {{ totals.pending }} 条</el-tag><el-tag type="danger">已退回 {{ totals.returned }} 条</el-tag>
      <el-tag type="info">状态未知 {{ totals.unknown }} 条</el-tag><el-tag>奖项数量 {{ totals.awards }}</el-tag>
    </div>
    <el-radio-group v-model="groupBy"><el-radio-button label="department">按部门汇总</el-radio-button><el-radio-button label="category">按类别汇总</el-radio-button></el-radio-group>
    <el-table :data="groups" max-height="380" empty-text="当前条件下暂无成果">
      <el-table-column prop="name" :label="groupBy === 'department' ? '报送部门' : '成果类别'" min-width="210" />
      <el-table-column prop="count" label="成果条数" /><el-table-column prop="archived" label="已归档" /><el-table-column prop="pending" label="待归档" />
      <el-table-column prop="returned" label="已退回" /><el-table-column prop="unknown" label="状态未知" /><el-table-column prop="awards" label="奖项数量" />
    </el-table>
    <template #footer><el-button @click="emit('update:modelValue', false)">关闭</el-button><el-button :loading="exporting" :disabled="!filtered.length||exporting" @click="exportSummary">导出统计 CSV</el-button><el-button type="primary" :loading="exporting" :disabled="!filtered.length||exporting" @click="exportDetails">导出明细 CSV</el-button></template>
  </el-dialog>
</template>
<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { reportDate, reportStatus, reportStatusNames, awardQuantity, downloadReport } from '@/utils/achievementReport'
const props = defineProps({ modelValue: Boolean, refreshRows: Function, rows: { type: Array, default: () => [] }, deptName: Function, categoryName: Function })
const freshRows=ref(null),exporting=ref(false)
const sourceRows=computed(()=>freshRows.value||props.rows)
const emit = defineEmits(['update:modelValue'])
const timeField = ref('gotTime'), dates = ref(null), status = ref(''), department = ref(''), category = ref(''), keyword = ref(''), groupBy = ref('department')
const reset = () => { timeField.value = 'gotTime'; dates.value = null; status.value = ''; department.value = ''; category.value = ''; keyword.value = '' }
const departments = computed(() => [...new Set(sourceRows.value.map(row => String(row.deptId ?? '')))].map(id => ({ id, name: props.deptName(id) || '部门未知' })))
const categories = computed(() => [...new Set(sourceRows.value.map(row => String(row.category ?? '')))].map(id => ({ id, name: props.categoryName(id) || '类别未知' })))
const filtered = computed(() => sourceRows.value.filter(row => {
  if (status.value && reportStatus(row) !== status.value) return false
  if (department.value && String(row.deptId) !== department.value) return false
  if (category.value && String(row.category) !== category.value) return false
  if (keyword.value.trim() && !String(row.achName || '').toLowerCase().includes(keyword.value.trim().toLowerCase())) return false
  if (dates.value?.length === 2) {
    const date = reportDate(row[timeField.value])
    if (!date || date < dates.value[0] || date > dates.value[1]) return false
  }
  return true
}))
const empty = () => ({ count: 0, archived: 0, pending: 0, returned: 0, unknown: 0, awards: 0 })
const accumulate = (total, row) => { total.count++; total[reportStatus(row)]++; total.awards += awardQuantity(row); return total }
const totals = computed(() => filtered.value.reduce(accumulate, empty()))
const groups = computed(() => {
  const result = new Map()
  filtered.value.forEach(row => {
    const id = groupBy.value === 'department' ? row.deptId : row.category
    if (!result.has(id)) result.set(id, { ...empty(), name: (groupBy.value === 'department' ? props.deptName(id) : props.categoryName(id)) || '未知' })
    accumulate(result.get(id), row)
  })
  return [...result.values()]
})
const writeSummary = () => downloadReport([
  [groupBy.value === 'department' ? '报送部门' : '成果类别', '成果条数', '已归档', '待归档', '已退回', '状态未知', '奖项数量'],
  ...groups.value.map(row => [row.name, row.count, row.archived, row.pending, row.returned, row.unknown, row.awards])
], '成果统计.csv')
const writeDetails = () => downloadReport([
  ['成果ID', '成果名称', '报送部门', '成果类别', '级别', '状态', '取得时间', '最近提交时间', '当前归档时间', '奖项数量', '专业群审核情况'],
  ...filtered.value.map(row => [row.achId, row.achName, props.deptName(row.deptId), props.categoryName(row.category), row.level,
    reportStatusNames[reportStatus(row)], reportDate(row.gotTime), reportDate(row.submittedTime), reportDate(row.archivedTime), awardQuantity(row), '审核情况未知'])
], '成果明细.csv')
async function exportCurrent(writer){
 if(exporting.value)return
 exporting.value=true
 try{if(props.refreshRows)freshRows.value=await props.refreshRows();writer()}catch(e){ElMessage.error(e.message||'导出失败，请重试')}finally{exporting.value=false}
}
const exportSummary=()=>exportCurrent(writeSummary),exportDetails=()=>exportCurrent(writeDetails)
</script>
<style scoped>
.report-summary { display: flex; gap: 12px; flex-wrap: wrap; margin: 16px 0; }
.report-filters { display: flex; flex-wrap: wrap; }
</style>
