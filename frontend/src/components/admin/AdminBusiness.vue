<template>
  <div class="admin-business">
    <div class="admin-banner">
      <div><h2>任务与绩效管理</h2><p>复用原业务页面完成新增、移交、完结和绩效关联维护，业务规则与原页面保持一致。</p></div>
      <el-segmented v-model="tab" :options="options" />
    </div>
    <section v-if="route.query.issue" class="issue-panel" v-loading="issueLoading">
      <div class="issue-head"><div><strong>{{ tab === 'task' ? '任务' : '绩效' }}配置异常</strong><span>点击记录进入原详情页处理</span></div><el-select v-model="issueYear" @change="loadIssues"><el-option v-for="item in years" :key="item" :label="`${item}年`" :value="item" /></el-select></div>
      <el-table :data="issueRows" max-height="260" empty-text="未发现异常"><el-table-column :prop="tab === 'task' ? 'taskCode' : 'perfCode'" label="编号" width="130"/><el-table-column :prop="tab === 'task' ? 'taskName' : 'perfName'" label="名称" min-width="260" show-overflow-tooltip/><el-table-column prop="issue" label="异常" min-width="220"/><el-table-column label="操作" width="90"><template #default="scope"><el-button link type="primary" @click="openIssue(scope.row)">查看</el-button></template></el-table-column></el-table>
    </section>
    <div class="business-content"><Works v-if="tab === 'task'" :key="`task-${route.fullPath}`"/><Performance v-else :key="`performance-${route.fullPath}`"/></div>
  </div>
</template>
<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Works from '@/components/Works.vue'
import Performance from '@/components/Performance.vue'
import { getAdminOverview } from '@/api/admin'
const route = useRoute(), router = useRouter()
const options = [{ label: '任务管理', value: 'task' }, { label: '绩效管理', value: 'performance' }]
const tab = ref(route.query.tab === 'performance' ? 'performance' : 'task')
const years = [2025, 2026, 2027, 2028, 2029], issueYear = ref(Number(route.query.year) || 2026), issueLoading = ref(false), issueData = ref({})
const issueRows = computed(() => tab.value === 'task' ? (issueData.value.taskIssues || []) : (issueData.value.performanceIssues || []))
const loadIssues = async () => { if (!route.query.issue) return; issueLoading.value = true; try { issueData.value = await getAdminOverview({ year: issueYear.value, month: new Date().getMonth() + 1 }) } finally { issueLoading.value = false } }
const openIssue = row => router.push({ path: tab.value === 'task' ? '/home/works' : '/home/works/performance', query: tab.value === 'task' ? { taskId: row.taskId, year: issueYear.value } : { perfId: row.perfId, year: issueYear.value } })
watch(tab, value => { router.replace({ path: route.path, query: { ...route.query, tab: value } }); loadIssues() })
watch(() => route.query.tab, value => { tab.value = value === 'performance' ? 'performance' : 'task' })
onMounted(loadIssues)
</script>
<style scoped>
.admin-business{min-height:100%;background:#f3f6fa}.admin-banner{display:flex;justify-content:space-between;align-items:center;gap:20px;padding:22px 24px 16px}.admin-banner h2{margin:0 0 7px;font-size:24px}.admin-banner p{margin:0;color:#7b8794;font-size:13px}.issue-panel{margin:0 24px 14px;padding:14px 18px;background:#fff7e6;border:1px solid #ffd591;border-radius:10px}.issue-head{display:flex;justify-content:space-between;align-items:center;margin-bottom:10px}.issue-head strong{margin-right:12px}.issue-head span{color:#8c6d1f;font-size:13px}.issue-head .el-select{width:115px}.business-content :deep(.works-container){padding-top:0}@media(max-width:700px){.admin-banner{align-items:flex-start;flex-direction:column;padding:16px 14px}.issue-panel{margin:0 14px 12px}}
</style>
