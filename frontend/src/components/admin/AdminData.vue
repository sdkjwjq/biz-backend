<template>
  <div class="admin-data">
    <div class="admin-banner"><div><h2>数据维护</h2><p>集中维护预算版本并查询全校已提交、已删除的工作纪实。</p></div><el-segmented :key="tabSelectionVersion" :model-value="tab" :options="options" :disabled="changingTab" @update:model-value="changeTab" /></div>
    <Budget ref="budgetRef" v-if="tab === 'budget'" />
    <WorkRecords v-else-if="tab === 'records'" />
    <section v-else class="deleted-panel">
      <div class="filters"><el-select v-model="filters.year" @change="autoSearch.flush()" clearable placeholder="全部年度"><el-option v-for="item in years" :key="item" :label="`${item}年`" :value="item" /></el-select><el-select v-model="filters.month" @change="autoSearch.flush()" clearable placeholder="全部月份"><el-option v-for="item in 12" :key="item" :label="`${item}月`" :value="item" /></el-select><el-input v-model="filters.keyword" clearable placeholder="填报人姓名或工号" @input="autoSearch.schedule(true)" @keyup.enter="autoSearch.flush()"/></div>
      <el-alert title="已删除纪实仅供追溯，首版不提供恢复。原填报人可以重新填报同一月份。" type="info" show-icon :closable="false" />
      <el-table :data="rows" v-loading="loading" stripe empty-text="暂无已删除记录">
        <el-table-column prop="recordId" label="记录ID" width="100"/><el-table-column prop="ownerName" label="填报人" width="120"/><el-table-column prop="ownerId" label="工号" width="120"/><el-table-column label="纪实月份" width="120"><template #default="scope">{{ scope.row.recordYear }}年{{ scope.row.recordMonth }}月</template></el-table-column><el-table-column prop="submitTime" label="原提交时间" width="180"><template #default="scope">{{ formatTime(scope.row.submitTime) }}</template></el-table-column><el-table-column prop="deletedByName" label="删除人" width="120"/><el-table-column prop="deletedTime" label="删除时间" width="180"><template #default="scope">{{ formatTime(scope.row.deletedTime) }}</template></el-table-column><el-table-column prop="deleteReason" label="删除原因" min-width="220" show-overflow-tooltip/>
      </el-table>
      <div class="pager"><el-pagination v-model:current-page="page" layout="total, prev, pager, next" :total="total" @current-change="load" /></div>
    </section>
  </div>
</template>
<script setup>
import { onMounted, onBeforeUnmount, reactive, ref, watch } from 'vue'
import { onBeforeRouteUpdate, useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import Budget from '@/components/Budget.vue'
import WorkRecords from '@/components/WorkRecords.vue'
import { getDeletedWorkRecords } from '@/api/admin'
import { createFilterTrigger } from '@/utils/filterInteraction.mjs'
const route = useRoute(), router = useRouter(), years = [2025, 2026, 2027, 2028, 2029]
const options = [{ label: '预算版本', value: 'budget' }, { label: '工作纪实', value: 'records' }, { label: '已删除纪实', value: 'deleted' }]
const validTab = value => options.some(item => item.value === value) ? value : 'budget'
const tab = ref(validTab(route.query.tab)), filters = reactive({ year: route.query.year ? Number(route.query.year) : '', month: route.query.month ? Number(route.query.month) : '', keyword: '' })
const budgetRef=ref(null);
const tabSelectionVersion=ref(0),changingTab=ref(false);
const changeTab=async value=>{
  if(changingTab.value||value===tab.value)return;
  changingTab.value=true;
  try{if(tab.value==='budget'&&await budgetRef.value?.confirmLeave()===false)return;tab.value=value}
  finally{changingTab.value=false;tabSelectionVersion.value++}
};
onBeforeRouteUpdate(async to=>{
  if(tab.value==='budget'&&validTab(to.query.tab)!=='budget')return await budgetRef.value?.confirmLeave()!==false;
});
const rows = ref([]), page = ref(1), total = ref(0), loading = ref(false)
let requestVersion=0,disposed=false
onBeforeUnmount(()=>{disposed=true;requestVersion++})
const load = async () => { const request=++requestVersion; if (tab.value !== 'deleted') return; loading.value = true; try { const result = await getDeletedWorkRecords({ ...filters, page: page.value, pageSize: 10 }); if(!disposed&&request===requestVersion){rows.value = result.items || []; total.value = result.total || 0} } catch (e) { if(!disposed&&request===requestVersion)ElMessage.error(e.message || '已删除纪实加载失败') } finally { if(request===requestVersion)loading.value = false } }
const search = () => { page.value = 1; return load() }
const autoSearch = createFilterTrigger(search)
onBeforeUnmount(() => autoSearch.cancel())
const formatTime = value => value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : '—'
watch(tab, value => { autoSearch.cancel();requestVersion++;router.replace({ path: route.path, query: { ...route.query, tab: value } }); if (value === 'deleted') load() })
watch(() => route.query.tab, value => { tab.value = validTab(value) })
onMounted(load)
</script>
<style scoped>
.admin-data{height:100%;min-height:0;min-width:0;overflow-y:auto;overflow-x:hidden;box-sizing:border-box;background:#f3f6fa}
.admin-data > :deep(.works-container){height:auto;min-height:0;overflow:visible}
.admin-banner{display:flex;justify-content:space-between;align-items:center;gap:20px;padding:22px 24px 16px}.admin-banner h2{margin:0 0 7px;font-size:24px}.admin-banner p{margin:0;color:#7b8794;font-size:13px}.deleted-panel{margin:0 24px 24px;padding:18px;background:#fff;border:1px solid #e5eaf2;border-radius:10px}.filters{display:flex;gap:10px;flex-wrap:wrap;margin-bottom:14px}.filters .el-select{width:130px}.filters .el-input{width:220px}.deleted-panel>.el-alert{margin-bottom:14px}.pager{display:flex;justify-content:flex-end;margin-top:18px}@media(max-width:700px){.admin-banner{align-items:flex-start;flex-direction:column;padding:16px 14px}.deleted-panel{margin:0 14px 14px}}
</style>
