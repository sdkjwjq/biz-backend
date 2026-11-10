<template>
 <div class="task-workspace" :class="{'is-review-workspace':kind!=='business'}">
  <div v-if="kind!=='business'" class="round-strip">
   <strong>{{kind==='reviewHistory'?'历史评审':'专家评审'}}</strong>
   <el-select :model-value="selected?.round_id||''" :loading="loading" placeholder="选择评审任务" @change="selectRound"><el-option v-for="r in choices" :key="r.round_id" :label="r.name" :value="r.round_id"/></el-select>
   <template v-if="selected"><span>{{selected.review_year?`${selected.review_year}年度`:'2025—2029全年度'}}</span><span>{{time(selected.startsAt)}} 至 {{time(selected.endsAt)}}</span><el-tag :type="readOnly?'info':'success'">{{readOnly?'只读查阅':'可评审'}}</el-tag></template>
   <el-button :loading="loading" @click="refresh">刷新轮次</el-button>
   <el-button v-if="kind!=='reviewHistory'" link type="primary" @click="router.push({path:'/home/works',query:{reviewView:'history'}})">历史评审</el-button>
   <el-button v-else link type="primary" @click="router.push('/home/works')">返回任务</el-button>
   <el-button v-if="kind==='reviewHistory'" link @click="openLegacy">旧版历史意见</el-button>
  </div>
  <el-alert v-if="error" :title="error" type="error" :closable="false"/>
  <Works v-if="kind==='business'||selected" :key="workspaceKey" ref="works" :workspace="context" @round-updated="updateRound"/>
  <div v-else class="round-empty" v-loading="loading"><el-empty :description="loading?'正在加载评审任务…':kind==='reviewHistory'?'暂无可查阅的已结束评审任务':'暂无可参加的有效评审任务'"/></div>
  <el-drawer v-model="legacyVisible" title="旧版正式评审意见（只读）" size="min(1000px,100vw)"><p>以下是原正式版本，不代表当前任务授权。</p><el-table :data="legacyRows" v-loading="legacyLoading"><el-table-column prop="task_code" label="任务编号" width="150"/><el-table-column prop="task_name" label="任务名称" min-width="250"/><el-table-column prop="review_year" label="年度" width="90"/><el-table-column prop="review_text" label="正式意见" min-width="320"/><el-table-column prop="revision_no" label="修订版" width="90"/></el-table><el-pagination :current-page="legacyPage" :page-size="50" :total="legacyTotal" layout="total,prev,pager,next" @current-change="loadLegacy"/></el-drawer>
 </div>
</template>
<script setup>
import {ref,computed,watch,onUnmounted} from 'vue'
import {onBeforeRouteUpdate,useRoute,useRouter} from 'vue-router'
import {get} from '@/utils/request'
import {accessState} from '@/utils/accessState'
import Works from './Works.vue'
const route=useRoute(),router=useRouter(),works=ref(null),rounds=ref([]),selected=ref(null),loading=ref(false),error=ref('')
const kind=computed(()=>route.query.reviewView==='history'?'reviewHistory':accessState.experience==='EXPERT'?'review':'business')
const choices=computed(()=>kind.value==='reviewHistory'||accessState.isAdmin?rounds.value:rounds.value.filter(r=>r.status==='ACTIVE'||r.round_id===selected.value?.round_id))
const readOnly=computed(()=>kind.value==='reviewHistory'||!selected.value?.canWrite)
const context=computed(()=>({kind:kind.value,roundId:selected.value?.round_id||null,round:selected.value,readOnly:readOnly.value}))
const workspaceKey=computed(()=>`${kind.value}:${selected.value?.round_id||0}`)
const time=v=>(v||'').replace('T',' ')
let sequence=0,controller,disposed=false,legacySeq=0
async function load(){const n=++sequence;controller?.abort();if(kind.value==='business'){rounds.value=[];selected.value=null;error.value='';loading.value=false;return}loading.value=true;error.value='';selected.value=null;controller=new AbortController();try{const values=await get(kind.value==='reviewHistory'?'/review-history/rounds':'/review-work',{},{signal:controller.signal});if(disposed||n!==sequence)return;rounds.value=values;const id=route.query.reviewRoundId;if(id!=null&&(!/^\d+$/.test(String(id))||Number(id)<=0))throw Error('评审任务编号不正确');let chosen=id?values.find(r=>r.round_id===Number(id)):null;if(id&&!chosen)throw Error('该评审任务不存在、尚未开始或权限已失效');if(!chosen){const active=values.filter(r=>r.status==='ACTIVE').sort((a,b)=>b.startsAt.localeCompare(a.startsAt));chosen=active[0]||(kind.value==='reviewHistory'||accessState.isAdmin?[...values].sort((a,b)=>b.round_id-a.round_id)[0]:null)}selected.value=chosen||null}catch(e){if(e.name!=='AbortError'&&n===sequence)error.value=e.message}finally{if(n===sequence)loading.value=false}}
async function selectRound(id){await router.replace({path:'/home/works',query:{...(kind.value==='reviewHistory'?{reviewView:'history'}:{}),reviewRoundId:String(id)}})}
async function refresh(){if(works.value&&!await works.value.prepareContextChange())return;await load()}
function updateRound(value){if(value?.round_id===selected.value?.round_id)selected.value=value}
async function openLegacy(){legacyVisible.value=true;await loadLegacy(1)}
const legacyVisible=ref(false),legacyRows=ref([]),legacyTotal=ref(0),legacyPage=ref(1),legacyLoading=ref(false)
async function loadLegacy(page){const n=++legacySeq;legacyLoading.value=true;try{const result=await get('/review-history',{page});if(!disposed&&n===legacySeq){legacyRows.value=result.items;legacyTotal.value=result.total;legacyPage.value=page}}catch(e){if(n===legacySeq)error.value=e.message}finally{if(n===legacySeq)legacyLoading.value=false}}
onBeforeRouteUpdate(async(to,from)=>{if(to.query.reviewRoundId!==from.query.reviewRoundId||to.query.reviewView!==from.query.reviewView)return !works.value||await works.value.prepareContextChange()})
watch(()=>[kind.value,route.query.reviewRoundId],load,{immediate:true});onUnmounted(()=>{disposed=true;sequence++;legacySeq++;controller?.abort()})
</script>
<style scoped>
.is-review-workspace { overflow-y:auto; scrollbar-gutter:stable; }
.is-review-workspace :deep(.works-container) { height:auto; min-height:620px; flex:1 0 auto; }
.is-review-workspace :deep(.table-card) { min-height:auto; }
.is-review-workspace :deep(.table-wrapper) { min-height:280px; }
.is-review-workspace :deep(.review-task-counts) { padding-bottom:12px; color:#64748b; font-size:13px; line-height:1.7; }
.is-review-workspace :deep(.readonly-task-value) { font-weight:600; color:#334155; }
</style>
<style scoped>.task-workspace{height:100%;min-height:0;display:flex;flex-direction:column;min-width:0}.task-workspace :deep(.works-container){flex:1;min-height:0}.round-strip{display:flex;align-items:center;gap:12px;flex-wrap:wrap;padding:12px 16px;margin:0 0 12px;background:#fff;border-radius:6px;font-size:13px;color:#64748b}.round-strip .el-select{width:270px;max-width:100%}.round-empty{flex:1;overflow:auto;background:white}.task-workspace>.el-alert{flex-shrink:0}</style>
