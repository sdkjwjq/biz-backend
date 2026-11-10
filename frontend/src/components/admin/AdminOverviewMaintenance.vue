<template>
  <div class="admin-page" v-loading="loading">
    <el-alert v-if="error" :title="error" type="error" show-icon :closable="false"><template #default><el-button link type="primary" @click="load">重新加载</el-button></template></el-alert>
    <el-alert v-if="data.readOnly" :title="data.scope?.scope === 'DEPARTMENTS' ? '只读概览：任务、绩效及其待办异常仅统计分管归口部门，其余卡片为全校数据。' : '全校只读概览：管理操作由管理员处理。'" type="info" :closable="false" class="scope-notice" />
    <div v-if="!error" class="metric-grid">
      <button class="metric" :disabled="data.readOnly" @click="go('/home/admin/accounts', { status: '1' })"><span class="label">正常账号</span><strong>{{ data.activeUsers ?? '—' }}</strong><small>{{ data.readOnly ? '只读统计' : '查看账号' }}</small></button>
      <button class="metric muted" :disabled="data.readOnly" @click="go('/home/admin/accounts', { status: '0' })"><span class="label">停用账号</span><strong>{{ data.disabledUsers ?? '—' }}</strong><small>{{ data.readOnly ? '只读统计' : '查看停用账号' }}</small></button>
      <button class="metric" :class="{ danger: data.taskIssueCount }" @click="go('/home/works', { year })"><span class="label">任务配置异常</span><strong>{{ data.taskIssueCount ?? '—' }}</strong><small>定位任务</small></button>
      <button class="metric" :class="{ danger: data.performanceIssueCount }" @click="go('/home/works/performance', { year })"><span class="label">绩效配置异常</span><strong>{{ data.performanceIssueCount ?? '—' }}</strong><small>定位绩效</small></button>
      <button class="metric" :disabled="data.readOnly" :class="{ danger: data.invalidPendingCount }" @click="go('/home/audit')"><span class="label">待办处理人异常</span><strong>{{ data.invalidPendingCount ?? '—' }}</strong><small>{{ data.readOnly ? '只读统计' : '查看审核中心' }}</small></button>
      <button class="metric" @click="go('/home/works/budget', { year, month })"><span class="label">预算最新版本</span><strong>{{ data.budget?.versionCount ?? 0 }}</strong><small>{{ formatTime(data.budget?.importTime) || '本月尚未导入' }}</small></button>
    </div>

    <div v-if="!error" class="issue-grid">
      <section class="panel">
        <div class="panel-title"><div><h3>任务配置异常</h3><p>最多展示100条，点击任务进入原详情页处理。</p></div><el-tag :type="data.taskIssueCount ? 'danger' : 'success'">{{ data.taskIssueCount || 0 }} 条</el-tag></div>
        <el-table :data="data.taskIssues || []" height="330" empty-text="未发现异常">
          <el-table-column prop="taskCode" label="编号" width="120" />
          <el-table-column prop="taskName" label="任务名称" min-width="220" show-overflow-tooltip />
          <el-table-column prop="issue" label="异常" min-width="180"><template #default="scope"><el-tag type="danger" effect="plain">{{ scope.row.issue }}</el-tag></template></el-table-column>
          <el-table-column label="操作" width="80"><template #default="scope"><el-button link type="primary" @click="openTask(scope.row)">查看</el-button></template></el-table-column>
        </el-table>
      </section>
      <section class="panel">
        <div class="panel-title"><div><h3>绩效配置异常</h3><p>含责任人、审核人失效和自动指标无关联任务。</p></div><el-tag :type="data.performanceIssueCount ? 'danger' : 'success'">{{ data.performanceIssueCount || 0 }} 条</el-tag></div>
        <el-table :data="data.performanceIssues || []" height="330" empty-text="未发现异常">
          <el-table-column prop="perfCode" label="编号" width="120" />
          <el-table-column prop="perfName" label="绩效名称" min-width="220" show-overflow-tooltip />
          <el-table-column prop="issue" label="异常" min-width="180"><template #default="scope"><el-tag type="danger" effect="plain">{{ scope.row.issue }}</el-tag></template></el-table-column>
          <el-table-column label="操作" width="80"><template #default="scope"><el-button link type="primary" @click="openPerformance(scope.row)">查看</el-button></template></el-table-column>
        </el-table>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getAdminOverview } from '@/api/admin'

const router = useRouter()
const props = defineProps({ year: Number, month: Number })
const year = computed(() => props.year), month = computed(() => props.month)
let controller, sequence = 0
const data = ref({})
const loading = ref(false)
const error = ref('')
const load = async () => {
  const version = ++sequence; controller?.abort(); controller = new AbortController()
  loading.value = true; error.value = ''; data.value = {}
  try { const result = await getAdminOverview({ year: year.value, month: month.value }, { signal: controller.signal }); if (version === sequence) data.value = result }
  catch (e) { if (version === sequence && e.name !== 'AbortError') error.value = e.message || '运行维护加载失败' }
  finally { if (version === sequence) loading.value = false }
}
const go = (path, query = {}) => router.push({ path, query })
const openTask = row => router.push({ path: '/home/works', query: { taskId: row.taskId, year: year.value } })
const openPerformance = row => router.push({ path: '/home/works/performance', query: { perfId: row.perfId, year: year.value } })
const formatTime = value => value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : ''
watch([year, month], load, { immediate: true })
onBeforeUnmount(() => { sequence++; controller?.abort() })
</script>

<style scoped>
.scope-notice{margin-bottom:16px}.metric:disabled{cursor:default}.metric:disabled:hover{transform:none;border-color:#e5eaf2}
.admin-page{padding:4px;color:#1f2937;min-width:0}.page-head{display:flex;justify-content:space-between;gap:20px;align-items:flex-start;margin-bottom:20px}.page-head h2{margin:0 0 7px;font-size:24px}.page-head p,.panel-title p{margin:0;color:#7b8794;font-size:13px}.period{display:flex;gap:10px}.period .el-select{width:112px}.metric-grid{display:grid;grid-template-columns:repeat(6,minmax(145px,1fr));gap:14px;margin-bottom:18px}.metric{border:1px solid #e5eaf2;background:#fff;border-radius:10px;padding:18px;text-align:left;cursor:pointer;box-shadow:0 2px 10px rgba(31,45,61,.04)}.metric:hover{border-color:#409eff;transform:translateY(-1px)}.metric .label{display:block;color:#667085;font-size:13px}.metric strong{display:block;margin:9px 0 5px;font-size:28px;color:#1d4ed8}.metric small{display:block;color:#98a2b3;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}.metric.danger strong{color:#dc2626}.metric.muted strong{color:#64748b}.metric.wide{grid-column:span 2}.issue-grid{display:grid;grid-template-columns:1fr 1fr;gap:18px}.panel{background:#fff;border:1px solid #e5eaf2;border-radius:10px;padding:18px}.panel-title{display:flex;align-items:flex-start;justify-content:space-between;margin-bottom:14px}.panel-title h3{margin:0 0 5px;font-size:17px}@media(max-width:1280px){.metric-grid{grid-template-columns:repeat(3,1fr)}}@media(max-width:900px){.page-head,.period{flex-wrap:wrap}.metric-grid,.issue-grid{grid-template-columns:1fr 1fr}.metric.wide{grid-column:span 1}}@media(max-width:640px){.admin-page{padding:14px}.metric-grid,.issue-grid{grid-template-columns:1fr}.period .el-select{width:100px}}
</style>
