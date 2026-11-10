<template>
 <section class="round-report">
  <div class="report-heading"><div><h3>{{ overview?.round.name || '评审详情' }}</h3><p v-if="overview">{{ overview.round.review_year || '2025—2029全年度' }} · {{ time(overview.round.startsAt) }} 至 {{ time(overview.round.endsAt) }} <el-tag size="small">{{ roundState(overview.round.status) }}</el-tag></p></div><el-button :loading="loading.overview" @click="refresh">刷新详情</el-button></div>
  <el-alert v-if="errors.overview" :title="errors.overview" type="error" :closable="false" />
  <el-tabs :model-value="tab" @update:model-value="$emit('update:tab',$event)">
   <el-tab-pane label="评审概况" name="overview">
    <el-skeleton v-if="!overview && loading.overview" :rows="5" animated />
    <template v-if="overview">
     <div class="report-stats"><div v-for="metric in metrics" :key="metric.key"><span>{{ metric.label }}</span><strong>{{ overview.summary[metric.key] }}</strong></div></div>
     <p class="note">正式意见按每名专家、每项任务的最新提交统计，一级总体评语与三级评语分别计算。修订记录共 {{ overview.summary.revisionCount }} 条；草稿不对管理员开放。</p>
     <p v-if="overview.round.description" class="description">{{ overview.round.description }}</p>
     <h4>专家参与情况</h4>
     <el-table :data="overview.experts" border empty-text="尚未添加专家或提交正式意见">
      <el-table-column label="专家" min-width="150"><template #default="{row}">{{ row.expertName }}<br><small>{{ row.expertId }}</small></template></el-table-column>
      <el-table-column label="参与状态" width="125"><template #default="{row}"><el-tag :type="row.administrator ? 'primary' : row.active ? 'success' : 'info'">{{ row.administrator ? '管理员评审' : row.active ? '本轮专家' : '已移除' }}</el-tag></template></el-table-column>
      <el-table-column label="当前分配三级任务" min-width="150"><template #default="{row}">{{ row.administrator ? '本轮全部有效任务' : row.taskCount }}</template></el-table-column>
      <el-table-column prop="leafCount" label="正式三级评语" width="120" /><el-table-column prop="overallCount" label="一级总体评语" width="120" />
      <el-table-column label="最近提交" min-width="165"><template #default="{row}">{{ time(row.lastSubmitted) }}</template></el-table-column>
      <el-table-column label="操作" width="110"><template #default="{row}"><el-button link type="primary" @click="expertOpinions(row.expertId)">查看评语</el-button></template></el-table-column>
     </el-table>
     <p class="note">已移除专家和管理员的正式意见仍保留。当前分配范围可能调整，不能用历史评语条数推断当前完成比例。</p>
    </template>
   </el-tab-pane>
   <el-tab-pane label="专家与范围" name="members"><slot name="members" /></el-tab-pane>
   <el-tab-pane label="正式评语" name="opinions">
    <div class="report-filters">
     <el-select v-model="filters.expert" @change="autoOpinions.flush()" clearable filterable placeholder="全部专家" aria-label="评语专家"><el-option v-for="e in overview?.experts || []" :key="e.expertId" :label="`${e.expertName}（${e.expertId}）`" :value="e.expertId" /></el-select>
     <el-select v-model="filters.level" @change="autoOpinions.flush()" clearable placeholder="全部任务层级" aria-label="评语任务层级"><el-option label="一级总体评语" :value="1" /><el-option label="三级任务评语" :value="3" /></el-select>
     <el-select v-model="filters.result" @change="autoOpinions.flush()" clearable placeholder="全部结论" aria-label="评语结论"><el-option label="通过" value="pass" /><el-option label="不通过" value="reject" /></el-select>
     <el-input v-model="filters.keyword" placeholder="任务编号、名称或评语关键词" aria-label="评语关键词" clearable @input="autoOpinions.schedule(true)" @keyup.enter="autoOpinions.flush()" />
     <el-checkbox v-model="filters.allVersions" @change="autoOpinions.flush()">包含历次修订</el-checkbox><el-button @click="resetOpinions">重置</el-button>
     <el-button :loading="exporting" @click="download">导出本轮全部意见及修订</el-button>
    </div>
    <p class="note">默认每名专家、每项任务只显示最新正式意见。查看详情可切换历次修订及提交时范围；导出包含本轮全部正式版本。</p>
    <el-alert v-if="errors.opinions" :title="errors.opinions" type="error" :closable="false" /><el-button v-if="errors.opinions" @click="loadOpinions">重新加载</el-button>
    <el-table :data="opinionRows" v-loading="loading.opinions" border :empty-text="empty('opinions')">
     <el-table-column prop="expert_name" label="专家" width="110" />
     <el-table-column label="任务" min-width="235"><template #default="{row}"><el-tag size="small" type="info">{{ row.task_level===1 ? '一级总体评语' : '三级任务评语' }}</el-tag><div class="task-label">{{ row.task_code }} {{ row.task_name }}</div></template></el-table-column>
     <el-table-column label="结论" width="90"><template #default="{row}"><el-tag :type="resultType(row.review_result)">{{ resultText(row.review_result) }}</el-tag></template></el-table-column>
     <el-table-column label="评语摘要" min-width="245"><template #default="{row}"><div class="preview">{{ row.preview }}</div></template></el-table-column>
     <el-table-column label="版本" width="105"><template #default="{row}">V{{ row.revision_no }}<br><small>{{ row.currentVersion ? '最新正式版' : '历史修订版' }}</small></template></el-table-column>
     <el-table-column label="提交时间" min-width="170"><template #default="{row}">{{ time(row.create_time) }}</template></el-table-column>
     <el-table-column label="操作" width="95" fixed="right"><template #default="{row}"><el-button link type="primary" @click="openOpinion(row.revision_id)">查看详情</el-button></template></el-table-column>
    </el-table>
    <el-pagination v-if="loaded.opinions" :current-page="opinionPage" :page-size="20" :total="opinionTotal" layout="total,prev,pager,next" @current-change="p=>{opinionPage=p;loadOpinions()}" />
   </el-tab-pane>
   <el-tab-pane label="操作日志" name="logs">
    <div class="report-filters"><el-select v-model="logType" @change="logPage=1;loadLogs()" placeholder="全部日志" aria-label="评审日志类型" clearable><el-option value="management" label="任务配置变更" /><el-option value="submission" label="正式提交与修订" /></el-select></div>
    <p class="note">展示系统已保存的任务配置变更、正式评语提交和修订。历史上未记录的浏览、草稿编辑不补造日志。</p>
    <el-alert v-if="errors.logs" :title="errors.logs" type="error" :closable="false" /><el-button v-if="errors.logs" @click="loadLogs">重新加载</el-button>
    <el-table :data="logRows" v-loading="loading.logs" border :empty-text="empty('logs')"><el-table-column label="时间" min-width="170"><template #default="{row}">{{ time(row.create_time) }}</template></el-table-column><el-table-column prop="operatorName" label="操作人" width="115" /><el-table-column label="操作" width="140"><template #default="{row}">{{ actionText(row.action) }}</template></el-table-column><el-table-column label="对象" min-width="210"><template #default="{row}">{{ row.task_code }} {{ row.task_name }}</template></el-table-column><el-table-column prop="reason" label="原因／版本" min-width="200" /><el-table-column label="操作" width="95" fixed="right"><template #default="{row}"><el-button link type="primary" @click="openLog(row)">查看详情</el-button></template></el-table-column></el-table>
    <el-pagination v-if="loaded.logs" :current-page="logPage" :page-size="20" :total="logTotal" layout="total,prev,pager,next" @current-change="p=>{logPage=p;loadLogs()}" />
   </el-tab-pane>
  </el-tabs>
  <el-drawer v-model="opinionVisible" title="正式评语详情" size="min(960px,100vw)" append-to-body @closed="cancelDetail">
   <div v-loading="loading.detail"><el-alert v-if="errors.detail" :title="errors.detail" type="error" :closable="false" />
    <template v-if="opinionDetail">
     <h3>{{ opinionDetail.task_code }} {{ opinionDetail.task_name }}</h3>
     <el-descriptions :column="2" border><el-descriptions-item label="专家">{{ opinionDetail.expert_name }}（{{ opinionDetail.expert_id }}）</el-descriptions-item><el-descriptions-item label="评语类型">{{ opinionDetail.task_level===1 ? '一级总体评语' : '三级任务评语' }}</el-descriptions-item><el-descriptions-item label="提交时间">{{ time(opinionDetail.create_time) }}</el-descriptions-item><el-descriptions-item label="实际操作者">{{ opinionDetail.operatorName }}</el-descriptions-item></el-descriptions>
     <div class="revision-picker"><span>正式版本</span><el-select :model-value="opinionDetail.revision_id" aria-label="正式评语版本" @change="openOpinion"><el-option v-for="r in opinionDetail.revisions" :key="r.revision_id" :value="r.revision_id" :label="`V${r.revision_no} · ${time(r.create_time)}`" /></el-select><el-tag :type="resultType(opinionDetail.review_result)">{{ resultText(opinionDetail.review_result) }}</el-tag></div>
     <h4>专家评语</h4><div class="formal-text">{{ opinionDetail.review_text }}</div>
     <h4>提交时评审范围（{{ scopeTasks.length }} 条三级任务）</h4><p class="note">{{ opinionDetail.scope?.partial ? '本次为部分范围评审，意见仅针对下列任务。' : '以下为本版本提交时保存的评审范围及完成情况。' }} 此处为历史快照，范围和完成值不会随当前配置更新。</p>
     <el-table :data="scopePageRows" border><el-table-column prop="taskCode" label="编号" min-width="140" /><el-table-column prop="taskName" label="任务名称" min-width="260" /><el-table-column prop="year" label="年度" width="85" /><el-table-column prop="targetValue" label="目标值" width="100" /><el-table-column prop="currentValue" label="完成值" width="100" /><el-table-column label="进度" width="90"><template #default="{row}">{{ row.progress == null ? '—' : `${row.progress}%` }}</template></el-table-column></el-table>
     <el-pagination :current-page="scopePage" :page-size="20" :total="scopeTasks.length" layout="total,prev,pager,next" @current-change="scopePage=$event" />
    </template>
   </div>
  </el-drawer>
  <el-drawer v-model="logVisible" title="配置变更详情" size="min(800px,100vw)" append-to-body @closed="cancelLogDetail"><div v-loading="loading.logDetail"><el-alert v-if="errors.logDetail" :title="errors.logDetail" type="error" :closable="false" /><template v-if="logDetail"><el-descriptions :column="1" border><el-descriptions-item label="操作">{{ actionText(logDetail.action) }}</el-descriptions-item><el-descriptions-item label="操作人">{{ logDetail.operatorName }}</el-descriptions-item><el-descriptions-item label="时间">{{ time(logDetail.create_time) }}</el-descriptions-item><el-descriptions-item label="原因">{{ logDetail.reason || '—' }}</el-descriptions-item></el-descriptions><h4>变更前</h4><pre class="change-text">{{ changeText(logDetail.before_json) }}</pre><h4>变更后</h4><pre class="change-text">{{ changeText(logDetail.after_json) }}</pre></template></div></el-drawer>
 </section>
</template>

<script setup>
import {ref,reactive,computed,watch,onBeforeUnmount} from 'vue'
import {ElMessage} from 'element-plus'
import {get} from '@/utils/request'
import {createFilterTrigger} from '@/utils/filterInteraction.mjs'
const props=defineProps({roundId:{type:Number,required:true},roundVersion:Number,tab:{type:String,default:'overview'}})
const emit=defineEmits(['update:tab','round-read'])
const base=computed(()=>`/admin/review-tasks/${props.roundId}`),overview=ref(null),loading=reactive({}),errors=reactive({}),loaded=reactive({})
const opinionRows=ref([]),opinionTotal=ref(0),opinionPage=ref(1),filters=reactive({expert:'',level:'',result:'',keyword:'',allVersions:false});let applied={}
const logRows=ref([]),logTotal=ref(0),logPage=ref(1),logType=ref(''),opinionVisible=ref(false),opinionDetail=ref(null),logVisible=ref(false),logDetail=ref(null),scopePage=ref(1),exporting=ref(false)
const scopeTasks=computed(()=>opinionDetail.value?.scope?.tasks || []),scopePageRows=computed(()=>scopeTasks.value.slice((scopePage.value-1)*20,scopePage.value*20))
const metrics=[{key:'activeExperts',label:'本轮在册专家'},{key:'assignedTasks',label:'当前分配三级任务（去重）'},{key:'leafOpinions',label:'最新正式三级评语'},{key:'overallOpinions',label:'最新一级总体评语'},{key:'passed',label:'通过意见'},{key:'rejected',label:'不通过意见'}]
const time=v=>{if(!v)return '—';const s=String(v).replace('T',' ').replace(/\.\d+(?:[+Z].*)?$/,'');const m=s.match(/^(\d{4})-(\d{1,2})-(\d{1,2})(?:\s+(\d{1,2}):(\d{1,2}))?/);if(!m)return s;const d=`${m[1]}年${Number(m[2])}月${Number(m[3])}日`;return m[4]?`${d} ${String(m[4]).padStart(2,'0')}:${String(m[5]).padStart(2,'0')}`:d}
const roundState=v=>({DRAFT:'草稿',UPCOMING:'未开始',ACTIVE:'进行中',ENDED:'已结束'})[v] || v
const resultType=v=>v==='pass'?'success':v==='reject'?'danger':'info',resultText=v=>v==='pass'?'通过':v==='reject'?'不通过':'未记录结论'
const actionText=v=>({ROUND_CREATE:'创建评审任务',ROUND_UPDATE:'修改任务信息',MEMBER_ADD:'添加专家',MEMBER_REMOVE:'移除专家',SCOPE_UPDATE:'调整评审范围',ROUND_PUBLISH:'发布评审任务',OPINION_SUBMIT:'提交正式评语',OPINION_RESUBMIT:'修订正式评语'})[v] || v
const empty=key=>loading[key]?'正在加载…':errors[key]?'加载失败，请重试':loaded[key]?'暂无匹配记录':'正在加载…'
let disposed=false;const requests=new Map()
async function fetchData(key,url,params,apply){requests.get(key)?.abort();const c=new AbortController();requests.set(key,c);loading[key]=true;errors[key]='';try{const data=await get(url,params,{signal:c.signal});if(!disposed&&requests.get(key)===c){apply(data);loaded[key]=true}}catch(e){if(!disposed&&requests.get(key)===c&&e.name!=='AbortError')errors[key]=e.message}finally{if(requests.get(key)===c)loading[key]=false}}
const loadOverview=()=>fetchData('overview',`${base.value}/overview`,{},data=>{overview.value=data;emit('round-read',data.round)})
const loadOpinions=()=>fetchData('opinions',`${base.value}/opinion-records`,{...applied,page:opinionPage.value,pageSize:20},r=>{opinionRows.value=r.items;opinionTotal.value=r.total})
const autoOpinions=createFilterTrigger(()=>{if(props.tab==='opinions')return queryOpinions()})
function queryOpinions(){autoOpinions.cancel();applied={...filters};for(const key of ['expert','level'])if(applied[key]==='')delete applied[key];opinionPage.value=1;return loadOpinions()}
function resetOpinions(){Object.assign(filters,{expert:'',level:'',result:'',keyword:'',allVersions:false});return queryOpinions()}
function expertOpinions(id){Object.assign(filters,{expert:id,level:'',result:'',keyword:'',allVersions:false});opinionPage.value=1;if(props.tab==='opinions')queryOpinions();else emit('update:tab','opinions')}
const loadLogs=()=>fetchData('logs',`${base.value}/activity`,{type:logType.value,page:logPage.value,pageSize:20},r=>{logRows.value=r.items;logTotal.value=r.total})
function openOpinion(id){opinionVisible.value=true;opinionDetail.value=null;scopePage.value=1;return fetchData('detail',`${base.value}/opinion-records/${id}`,{},r=>{opinionDetail.value=r})}
const cancelDetail=()=>{requests.get('detail')?.abort();requests.delete('detail')}
function openLog(row){if(row.type==='submission')return openOpinion(row.id);logVisible.value=true;logDetail.value=null;return fetchData('logDetail',`${base.value}/activity/management/${row.id}`,{},r=>{logDetail.value=r})}
const cancelLogDetail=()=>{requests.get('logDetail')?.abort();requests.delete('logDetail')}
const changeLabels={name:'评审名称',review_year:'建设年度',starts_at:'开始时间',ends_at:'结束时间',description:'评审说明',published:'发布标记',version:'配置版本',userIds:'专家账号',taskIds:'三级任务ID',reason:'调整原因',round_id:'评审任务ID',create_by:'创建人账号',update_by:'更新人账号',create_time:'创建时间',update_time:'更新时间'}
function changeText(value){if(value==null)return '无';try{const parsed=typeof value==='string'?JSON.parse(value):value;const transform=v=>Array.isArray(v)?v.map(transform):v&&typeof v==='object'?Object.fromEntries(Object.entries(v).map(([k,val])=>[changeLabels[k] || k,transform(val)])):v;return JSON.stringify(transform(parsed),null,2)}catch{return String(value)}}
async function download(){if(exporting.value)return;exporting.value=true;try{const blob=await get(`${base.value}/opinions/export`,{},{responseType:'blob'});if(disposed)return;const url=URL.createObjectURL(blob),a=document.createElement('a');a.href=url;a.download=`${overview.value?.round.name || '评审任务'}-全部正式意见及修订.xlsx`;a.click();setTimeout(()=>URL.revokeObjectURL(url),1000)}catch(e){if(!disposed)ElMessage.error(e.message)}finally{exporting.value=false}}
function refresh(){void loadOverview();if(props.tab==='opinions')void loadOpinions();if(props.tab==='logs')void loadLogs()}
watch(()=>props.roundVersion,loadOverview,{immediate:true})
watch(()=>props.tab,tab=>{autoOpinions.cancel();for(const key of ['opinions','logs'])if(key!==tab){requests.get(key)?.abort();requests.delete(key);loading[key]=false}if(tab==='opinions')void queryOpinions();if(tab==='logs')void loadLogs()},{immediate:true})
onBeforeUnmount(()=>{autoOpinions.cancel();disposed=true;for(const c of requests.values())c.abort()})
</script>

<style scoped>
.round-report{min-width:0}.report-heading{display:flex;align-items:flex-start;justify-content:space-between;gap:12px;flex-wrap:wrap}.report-heading h3{margin:0 0 10px}.report-heading p,.note{color:#738196;line-height:1.7;font-size:13px}.description,.formal-text{white-space:pre-wrap;overflow-wrap:anywhere;line-height:1.8}.description{padding:12px 16px;background:#f6f8fb;border-radius:6px}.report-stats{display:grid;grid-template-columns:repeat(auto-fit,minmax(145px,1fr));gap:12px;margin:16px 0}.report-stats>div{padding:16px;background:#f5f7fb;border:1px solid #e6ebf2;border-radius:6px}.report-stats span{display:block;font-size:13px;color:#738196}.report-stats strong{display:block;font-size:26px;margin-top:8px;color:#25364b}.report-filters,.revision-picker{display:flex;gap:10px;align-items:center;flex-wrap:wrap;margin:12px 0}.report-filters .el-select{width:175px}.report-filters .el-input{width:260px;max-width:100%}.round-report small{color:#8793a5;font-size:12px}.task-label{white-space:normal;overflow-wrap:anywhere;line-height:1.6;margin-top:5px}.preview{white-space:pre-wrap;overflow-wrap:anywhere;display:-webkit-box;-webkit-line-clamp:3;-webkit-box-orient:vertical;overflow:hidden}.round-report :deep(.el-pagination){justify-content:flex-end;margin:16px 0;flex-wrap:wrap}.formal-text,.change-text{padding:16px;border:1px solid #e5eaf0;background:#f7f9fc;border-radius:6px}.change-text{white-space:pre-wrap;overflow-wrap:anywhere;line-height:1.6}.revision-picker .el-select{width:280px;max-width:100%}
</style>
