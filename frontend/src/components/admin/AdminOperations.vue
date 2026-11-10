<template>
  <div class="admin-page">
    <div class="page-head"><div><h2>操作记录</h2><p>统一查询管理员操作及既有任务移交、绩效关联、预算版本和纪实删除记录。</p></div><el-button type="success" :loading="exporting" @click="exportRows">导出筛选结果</el-button></div>
    <section class="panel">
      <QueryFilterBar ref="queryBar" domain="admin-operations" title="操作记录" :context="{page,pageSize:10}" :option-labels="{module:names}" @result="receiveQuery" />
      <el-table :data="rows" v-loading="loading" stripe empty-text="暂无操作记录">
        <el-table-column label="时间" width="175"><template #default="scope">{{ formatTime(scope.row.createTime) }}</template></el-table-column><el-table-column label="模块" width="135"><template #default="scope">{{ moduleName(scope.row.module) }}</template></el-table-column><el-table-column prop="action" label="动作" width="130"/><el-table-column prop="targetName" label="对象" min-width="210" show-overflow-tooltip/><el-table-column prop="targetId" label="对象ID" width="115"/><el-table-column prop="operatorName" label="操作人" width="110"/><el-table-column prop="reason" label="原因" min-width="180" show-overflow-tooltip/><el-table-column label="操作" width="80" fixed="right"><template #default="scope"><el-button link type="primary" @click="showDetail(scope.row)">详情</el-button></template></el-table-column>
      </el-table>
      <div class="pager"><el-pagination v-model:current-page="page" layout="total, prev, pager, next" :total="total"/></div>
    </section>
    <el-dialog v-model="detailVisible" title="操作详情" width="700px"><el-descriptions v-if="detail" :column="2" border><el-descriptions-item label="模块">{{ moduleName(detail.module) }}</el-descriptions-item><el-descriptions-item label="动作">{{ detail.action }}</el-descriptions-item><el-descriptions-item label="对象">{{ detail.targetName || '—' }}</el-descriptions-item><el-descriptions-item label="对象ID">{{ detail.targetId || '—' }}</el-descriptions-item><el-descriptions-item label="操作人">{{ detail.operatorName || detail.operatorId }}</el-descriptions-item><el-descriptions-item label="时间">{{ formatTime(detail.createTime) }}</el-descriptions-item><el-descriptions-item label="原因" :span="2">{{ detail.reason || '—' }}</el-descriptions-item><el-descriptions-item v-if="detail.beforeJson" label="变更前" :span="2"><pre>{{ pretty(detail.beforeJson) }}</pre></el-descriptions-item><el-descriptions-item v-if="detail.afterJson" label="变更后" :span="2"><pre>{{ pretty(detail.afterJson) }}</pre></el-descriptions-item></el-descriptions></el-dialog>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { exportAdminOperations, getAdminOperation } from '@/api/admin'
import QueryFilterBar from '../QueryFilterBar.vue'
const queryBar=ref(null),applied=ref({})
const receiveQuery=result=>{applied.value=result.query;rows.value=result.items;total.value=result.total;page.value=result.page}
const modules=[{label:'角色与权限',value:'PERMISSION'},{label:'管理员操作',value:'ADMIN_OPERATION'},{label:'历史管理员测试',value:'ADMIN_TEST'},{label:'专家评审',value:'EXPERT_REVIEW'},{label:'账号',value:'USER'},{label:'部门',value:'DEPARTMENT'},{label:'任务/绩效移交',value:'OWNERSHIP'},{label:'绩效关联',value:'PERFORMANCE_RELATION'},{label:'预算',value:'BUDGET'},{label:'工作纪实',value:'WORK_RECORD'},{label:'任务',value:'TASK'}]
const names=Object.fromEntries(modules.map(item=>[item.value,item.label])), rows=ref([]), page=ref(1), total=ref(0), loading=ref(false), exporting=ref(false), detailVisible=ref(false), detail=ref(null)
const params=()=>Object.fromEntries(Object.entries(applied.value).map(([key,value])=>['f.'+key,value]))
const showDetail=async row=>{try{detail.value=await getAdminOperation(row.recordType,row.recordId);detailVisible.value=true}catch(e){ElMessage.error(e.message||'详情加载失败')}}
const exportRows=async()=>{if(exporting.value)return;exporting.value=true;try{const blob=await exportAdminOperations(params());const url=URL.createObjectURL(blob),link=document.createElement('a');link.href=url;link.download='管理员操作记录.xlsx';link.click();setTimeout(()=>URL.revokeObjectURL(url),1000)}catch(e){ElMessage.error(e.message||'导出失败')}finally{exporting.value=false}}
const formatTime=value=>value?new Date(value).toLocaleString('zh-CN',{hour12:false}):'—',moduleName=value=>names[value]||value||'—'
const pretty=value=>{try{return JSON.stringify(JSON.parse(value),null,2)}catch{return value}}
</script>
<style scoped>
.admin-page{padding:24px;min-height:100%;background:#f3f6fa}.page-head{display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:18px}.page-head h2{margin:0 0 7px;font-size:24px}.page-head p{margin:0;color:#7b8794;font-size:13px}.panel{background:#fff;border:1px solid #e5eaf2;border-radius:10px;padding:18px}.filters{display:flex;flex-wrap:wrap;gap:10px;margin-bottom:16px}.filters .el-select{width:155px}.filters .el-input{width:165px}.filters .el-input:nth-of-type(2){width:220px}.pager{display:flex;justify-content:flex-end;margin-top:18px}pre{margin:0;white-space:pre-wrap;word-break:break-all;font-family:ui-monospace,monospace;font-size:12px;color:#475467}@media(max-width:700px){.admin-page{padding:14px}.page-head{gap:12px}}
</style>
