<template>
  <section class="query-filter-bar" :aria-label="`${title}筛选`" @keyup.enter="submitFromKeyboard">
    <p v-if="loading || background && error" class="query-status" role="status">{{ error ? '后台更新失败，当前显示上次结果' : !initialized || !retainPrevious ? '正在加载筛选与数据…' : background ? '正在后台更新…' : '正在查询，当前显示上次结果…' }}<el-button v-if="background && error" link type="primary" @click="refresh({background:true})">重试</el-button></p>
    <div class="basic-fields">
      <FilterField v-for="field in basicFields" :key="field.key" :field="field" :model="draft" />
      <div class="query-actions"><slot name="before-reset" /><el-button @click="reset()">重置全部</el-button><el-button v-if="groups.length" :aria-expanded="expanded" @click="expanded=!expanded;persist()">{{expanded?'收起高级筛选':'高级筛选'}}<span v-if="advancedCount">（{{advancedCount}}）</span></el-button><DetailedExportButton v-if="exportable" :domain="domain" :context="context" /><DetailedExportButton v-if="exportable && !['tasks','budget','admin-operations'].includes(domain)" :domain="domain" scope="filtered" :filters="applied" :context="context" :disabled="!initialized || loading" /><slot name="actions" /></div>
    </div>
    <div v-if="expanded" class="advanced-groups">
      <div v-for="group in groups" :key="group" class="query-group"><h5>{{group}}</h5><div class="group-fields"><FilterField v-for="field in advancedFields.filter(f=>f.group===group)" :key="field.key" :field="field" :model="draft" /></div></div>
      <div class="advanced-footer"><span>基础条件自动生效；高级条件选择完成后统一应用。同一条件多选满足任一项。</span><div><el-button link type="primary" @click="clearAdvanced">清空高级条件</el-button><el-button type="primary" :loading="loading && !background" @click="applyAdvanced">应用高级筛选</el-button></div></div>
    </div>
    <div class="applied-tags" v-if="tags.length"><span>已生效：</span><el-tag v-for="tag in tags" :key="tag.key" :closable="!requiredKeys.includes(tag.key)" @close="remove(tag.key)">{{tag.label}}</el-tag></div>
    <p v-if="adjustment" class="pending-change">{{adjustment}}<el-button v-if="adjustment.startsWith('筛选选项加载失败')" link type="primary" @click="loadOptions">重试选项</el-button></p>
    <p v-if="advancedDirty" class="pending-change">高级条件已修改，点击“应用高级筛选”生效</p>
    <el-alert v-if="error && !background" :title="error" type="error" :closable="false"><el-button link type="primary" @click="retryQuery">重新查询</el-button></el-alert>
  </section>
</template>
<script setup>
import { computed, defineComponent, h, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { ElButton, ElInput, ElSelect, ElOption, ElDatePicker } from 'element-plus'
import { get } from '@/utils/request'
import { createWorksCache } from '@/utils/worksCache'
import { accessState } from '@/utils/accessState'
import { queryFingerprint } from '@/utils/queryPresentation'
import DetailedExportButton from './DetailedExportButton.vue'
import { basicViews, advancedViews, basicQuery, clearAdvancedQuery, filterKeys, asMany, asSingle, createFilterTrigger } from '@/utils/filterInteraction.mjs'
import '@/styles/queryFilters.css'
const props=defineProps({exportable:{type:Boolean,default:true},retainPrevious:{type:Boolean,default:true},remember:{type:Boolean,default:true},beforeApply:Function,responseMode:{type:String,default:''},endpoint:String,optionsEndpoint:String,optionLabels:{type:Object,default:()=>({})},cacheKey:String,requiredKeys:{type:Array,default:()=>[]},context:{type:Object,default:()=>({})},domain:{type:String,required:true},title:{type:String,required:true},defaults:{type:Object,default:()=>({})},hiddenKeys:{type:Array,default:()=>[]},visibleKeys:{type:Array,default:()=>[]}})
const emit=defineEmits(['result','error','state'])
const cache=createWorksCache(),key=`works_query_v1_${props.cacheKey||props.domain}`
let saved;try{saved=props.remember?JSON.parse(cache.getItem(key)||'null'):null}catch{/* invalid stored filters */}
const copy=v=>JSON.parse(JSON.stringify(v)),draft=ref(copy(saved?.applied||props.defaults)),applied=ref({}),expanded=ref(!!saved?.expanded),fields=ref(props.domain==='admin-users'?[{key:'keyword',label:'姓名／工号',type:'text',basic:true,options:[]},{key:'deptId',label:'部门',type:'multi',basic:true,options:[]},{key:'roles',label:'角色模板',type:'multi',basic:true,options:[]},{key:'status',label:'启停状态',type:'select',basic:true,options:[{value:'1',label:'正常'},{value:'0',label:'停用'}]}]:[]),loading=ref(false),error=ref('')
const clean=v=>Object.fromEntries(Object.entries(v).filter(([,x])=>Array.isArray(x)?x.length:x!==''&&x!=null))
const adjustment=ref('')
const advancedDraftKeys=reactive(new Set())
const advancedDirty=computed(()=>initialized.value && fields.value.some(f=>(!f.basic||advancedDraftKeys.has(f.key))&&filterKeys(f.key).some(k=>queryFingerprint({v:draft.value[k]})!==queryFingerprint({v:applied.value[k]}))))
const basicFields=computed(()=>basicViews(visibleFields.value)),advancedFields=computed(()=>advancedViews(visibleFields.value))
const groups=computed(()=>[...new Set(advancedFields.value.map(f=>f.group))])
// hiddenKeys 用于对特定账号隐藏筛选字段（例如专家账号不显示年度）。
const visibleFields=computed(()=>fields.value.filter(f=>!props.hiddenKeys.includes(f.key)&&(!props.visibleKeys.length||props.visibleKeys.includes(f.key))))
const active=f=>[f.key,f.key+'From',f.key+'To'].some(k=>Object.hasOwn(clean(applied.value),k))
const advancedCount=computed(()=>fields.value.filter(f=>(!f.basic||f.type==='multi'&&asMany(applied.value[f.key]).length>1)&&active(f)).length)
const optionLabel=(f,value)=>props.optionLabels[f.key]?.[String(value)]||f.options.find(o=>String(o.value)===String(value))?.label||(value==='missing'?'未设置':props.domain==='admin-users'&&f.key==='roles'?'角色名称待加载':value)
const tags=computed(()=>visibleFields.value.filter(active).map(f=>{const value=applied.value[f.key];return {key:f.key,label:`${f.label}：${value!=null&&value!==''?(Array.isArray(value)?value.map(v=>optionLabel(f,v)).join('、'):optionLabel(f,value)):`${applied.value[f.key+'From']||'不限'} 至 ${applied.value[f.key+'To']||'不限'}`}`}}))
let rememberedPage=Number(saved?.page)||1
const persist=()=>props.remember&&cache.setItem(key,JSON.stringify({applied:applied.value,expanded:expanded.value,page:rememberedPage}))
function rememberPage(page){rememberedPage=Math.max(1,Number(page)||1);persist()}
let seq=0,controller,disposed=false
const initialized=ref(false),responseCode=ref(null),background=ref(false),activeQuery=ref({})
watch([loading,error,initialized,responseCode,background,activeQuery],()=>emit('state',{loading:loading.value,background:background.value,initialized:initialized.value,error:error.value,responseCode:responseCode.value,query:activeQuery.value}),{immediate:true,flush:'sync'})
let guardPromise=null;
async function guardedRun(source,preservePage,options){
 if(guardPromise)return guardPromise;
 const selected=copy(source);
 guardPromise=Promise.resolve(props.beforeApply(selected,copy(applied.value)));
 let approved;
 try{approved=await guardPromise}finally{guardPromise=null}
 if(approved===false){basicTrigger.cancel();advancedDraftKeys.clear();draft.value=copy(applied.value);return}
 if(disposed)return;
 return runApproved(selected,preservePage,options);
}
let pendingKey='',pendingPromise=null
function run(source=draft.value,preservePage=false,options={}){return props.beforeApply?guardedRun(source,preservePage,options):runApproved(source,preservePage,options)}
function runApproved(source,preservePage,options={}){
 if(adjustment.value.startsWith('筛选选项加载失败'))loadOptions();
 const selected=copy(source),quiet=!!(options.background&&initialized.value&&preservePage&&queryFingerprint(selected)===queryFingerprint(applied.value)),identity=JSON.stringify([clean(selected),clean(props.context),preservePage,rememberedPage,quiet]);
 if(!options.force&&pendingPromise&&pendingKey===identity)return pendingPromise;
 pendingKey=identity;
 const promise=execute(selected,preservePage,quiet);pendingPromise=promise;
 promise.finally(()=>{if(pendingPromise===promise){pendingPromise=null;pendingKey=''}});
 return promise;
}
async function execute(source,preservePage,quiet){const query=copy(clean(source)),current=++seq;controller?.abort();controller=new AbortController();activeQuery.value=query;background.value=quiet;loading.value=true;error.value='';responseCode.value=null;const params=new URLSearchParams(clean(props.context));params.set('page',String(preservePage?(initialized.value?(props.context.page||rememberedPage):rememberedPage):1));if(props.responseMode){params.set('responseMode',props.responseMode);params.set('includeFields','false');params.set('separateOptions','true')}if(props.domain==='admin-users'||(props.domain==='admin-operations'&&preservePage&&initialized.value)){params.set('responseMode','page');params.set('includeFields','false')}for(const [k,value]of Object.entries(query))for(const v of Array.isArray(value)?value:[value])params.append(`f.${k}`,v)
 try{const result=await get(`${props.endpoint||'/list-filters/'+props.domain}?${params}`,{},{signal:controller.signal});if(disposed||current!==seq||!cache.isCurrent())return;if(result.fields)fields.value=result.fields;
 if(preservePage&&result.fields){let changed=false;for(const f of fields.value.filter(f=>f.type==='multi'))if(Array.isArray(query[f.key])){const valid=query[f.key].filter(v=>f.options.some(o=>String(o.value)===String(v)));if(valid.length!==query[f.key].length){changed=true;if(valid.length){query[f.key]=valid;draft.value[f.key]=valid}else{delete query[f.key];delete draft.value[f.key]}}}if(changed){adjustment.value='授权范围或可选项已变化，已移除失效条件。';return run(query,false)}}
 applied.value=query;for(const key of advancedDraftKeys)if(queryFingerprint({v:query[key]})===queryFingerprint({v:draft.value[key]}))advancedDraftKeys.delete(key);initialized.value=true;rememberedPage=Number(result.page)||1;persist();emit('result',{...result,query,background:quiet});return result}catch(e){if(!disposed&&current===seq&&e.name!=='AbortError'){responseCode.value=e.responseCode||null;error.value=([401,403,428].includes(Number(responseCode.value))?'':initialized.value&&props.retainPrevious?'更新失败，当前保留上次查询结果：':'加载失败：')+(e.message||'请重试');emit('error')}}finally{if(current===seq)loading.value=false}}
const basicTrigger=createFilterTrigger(()=>applyBasic())
function applyBasic(){basicTrigger.cancel();if(disposed)return;return run(basicQuery(initialized.value?applied.value:saved?.applied||props.defaults,draft.value,fields.value,advancedDraftKeys))}
function applyAdvanced(){basicTrigger.cancel();return run()}
function retryQuery(){basicTrigger.cancel();return run(copy(activeQuery.value))}
function submitFromKeyboard(event){if(event.isComposing||event.target.closest('.el-select'))return;if(event.target.closest('.advanced-groups'))applyAdvanced();else basicTrigger.flush()}
function refresh(options={}){if(options.background&&loading.value&&!options.force)return pendingPromise;return run(initialized.value?applied.value:draft.value,true,options)}
function setPage(page){rememberedPage=Math.max(1,Number(page)||1);return refresh()}
function reset(){basicTrigger.cancel();advancedDraftKeys.clear();draft.value=copy(props.defaults);return run()}
function replace(query){basicTrigger.cancel();advancedDraftKeys.clear();draft.value=copy(query);return run()}
function patch(values){basicTrigger.cancel();for(const key of Object.keys(values))advancedDraftKeys.delete(key);const next={...applied.value,...values};Object.assign(draft.value,values);return run(next)}
function clearAdvanced(){basicTrigger.cancel();const next=clearAdvancedQuery(draft.value,applied.value,fields.value.filter(f=>!props.requiredKeys.includes(f.key)));advancedDraftKeys.clear();draft.value=next;return run()}
function remove(key){basicTrigger.cancel();advancedDraftKeys.delete(key);const next=copy(applied.value);for(const k of filterKeys(key)){delete draft.value[k];delete next[k]}run(next)}
const FilterField=defineComponent({props:['field','model'],setup(p){return()=>{const f=p.field,origin=f.source||f,model=p.model,update=(key,value)=>{model[key]=f.basic&&origin.type==='multi'?asMany(value):value;if(value&&(key.endsWith('From')||key.endsWith('To')))delete model[key.replace(/(From|To)$/,'')];if(f.basic){advancedDraftKeys.delete(f.key);basicTrigger.schedule(f.type==='text'||f.type==='number')}else advancedDraftKeys.add(f.key)},aria=`${props.title}${f.label}`;let control
 if(f.type==='date'||f.type==='number'){const date=f.type==='date';control=h('div',{class:'query-range'},[h(date?ElDatePicker:ElInput,{modelValue:model[f.key+'From'],'onUpdate:modelValue':v=>update(f.key+'From',v),...(date?{type:'date',valueFormat:'YYYY-MM-DD'}:{type:'number'}),placeholder:'起始','aria-label':aria+'起始',clearable:true}),h('span','至'),h(date?ElDatePicker:ElInput,{modelValue:model[f.key+'To'],'onUpdate:modelValue':v=>update(f.key+'To',v),...(date?{type:'date',valueFormat:'YYYY-MM-DD'}:{type:'number'}),placeholder:'结束','aria-label':aria+'结束',clearable:true}),...(date?[h(ElSelect,{modelValue:model[f.key],'onUpdate:modelValue':v=>{update(f.key,v);if(v){delete model[f.key+'From'];delete model[f.key+'To']}},clearable:true,placeholder:'含已设置','aria-label':aria+'缺失'},{default:()=>h(ElOption,{label:'未设置',value:'missing'})})]:[])])}
 else if(f.type==='text')control=h(ElInput,{modelValue:model[f.key],'onUpdate:modelValue':v=>update(f.key,v),clearable:true,maxlength:300,placeholder:`搜索${f.label}`,'aria-label':aria})
 else {
  const remote=origin.remote?remoteState(origin):null,more=f.basic&&origin.type==='multi'&&asMany(model[f.key]).length>1;
  const roleOptionsPending=props.domain==='admin-users'&&f.key==='roles'&&!origin.options.length
  const selectedRolePlaceholders=props.domain==='admin-users'&&f.key==='roles'?asMany(model[f.key]).filter(v=>!origin.options.some(o=>String(o.value)===String(v))).map(v=>h(ElOption,{key:`pending-${v}`,value:String(v),label:'角色名称待加载',disabled:true})):[]
  control=h(ElSelect,{modelValue:f.type==='multi'?asMany(model[f.key]).map(String):String(asSingle(model[f.key])),'onUpdate:modelValue':v=>update(f.key,v),multiple:f.type==='multi',filterable:true,collapseTags:true,collapseTagsTooltip:true,clearable:!props.requiredKeys.includes(f.key),loading:roleOptionsPending,disabled:roleOptionsPending,placeholder:more?`高级已选${asMany(model[f.key]).length}项`:roleOptionsPending?'正在加载角色':props.requiredKeys.includes(f.key)?'请选择':'全部','aria-label':aria,
   ...(remote?{remote:true,loading:remote.loading,remoteMethod:q=>scheduleRemote(origin,q),'onVisibleChange':visible=>{if(visible&&!remote.loaded)loadRemote(origin,'',1)}}:{})},
   {default:()=>[...origin.options.map(o=>h(ElOption,{key:o.value,value:String(o.value),label:optionLabel(origin,o.value)})),...selectedRolePlaceholders],...(remote?{footer:()=>remote.error?h(ElButton,{link:true,type:'primary',onClick:()=>loadRemote(origin,remote.keyword,remote.page||1)},()=> '选项加载失败，点击重试'):remote.total>remote.page*50?h(ElButton,{link:true,type:'primary',loading:remote.loading,onClick:()=>loadRemote(origin,remote.keyword,remote.page+1)},()=> '加载更多'):null}:{})})
 }

 return h('div',{class:['query-field',f.type==='text'?'query-keyword':'',f.type==='date'||f.type==='number'?'range-field':'']},[h('label',f.label),control])}}})
watch(()=>JSON.stringify(clean(props.context)),refresh);
let optionsController, adjustmentTimer, prunedQuery
const optionDomain=computed(()=>props.domain.startsWith('audits-')?'audits':props.domain)
const remoteStates=reactive({}),remoteControllers=new Map(),remoteTimers=new Map()
const remoteState=f=>remoteStates[f.key]||(remoteStates[f.key]={loading:false,loaded:false,keyword:'',page:0,total:0,error:''})
const optionContext=()=>JSON.stringify([clean(props.context),draft.value.year||'',draft.value.month||'',accessState.permissionVersion])
function scheduleRemote(field,keyword){clearTimeout(remoteTimers.get(field.key));remoteTimers.set(field.key,setTimeout(()=>loadRemote(field,keyword,1),250))}
async function loadRemote(field,keyword='',page=1,validate=false){
 const state=remoteState(field),context=optionContext(),sourceQuery=copy(draft.value),value=draft.value[field.key],ids=(Array.isArray(value)?value:value?[value]:[]).map(String)
 const identity=JSON.stringify([keyword,page,context,ids]);if(state.loading&&state.identity===identity)return
 remoteControllers.get(field.key)?.abort();const controller=new AbortController();remoteControllers.set(field.key,controller)
 state.loading=true;state.error='';state.identity=identity;state.keyword=keyword
 const params=new URLSearchParams(clean(props.context));params.set('field',field.key);params.set('keyword',keyword);params.set('page',String(page));params.set('pageSize','50')
 for(const key of ['year','month'])if(sourceQuery[key])params.set('f.'+key,sourceQuery[key]);for(const id of ids)params.append('ids',id)
 try{
  const result=await get(`${props.optionsEndpoint||'/list-filters/'+optionDomain.value+'/filter-options'}?${params}`,{},{signal:controller.signal})
  if(disposed||!cache.isCurrent()||context!==optionContext()||remoteControllers.get(field.key)!==controller||!fields.value.includes(field))return
  const merged=new Map((page>1?field.options:[]).map(o=>[String(o.value),o]));for(const o of [...result.selected,...result.items])merged.set(String(o.value),o);field.options=[...merged.values()]
  Object.assign(state,{page:result.page,total:result.total,loaded:true})
  if(validate&&JSON.stringify(draft.value[field.key])===JSON.stringify(value)){
   const allowed=new Set(result.selected.map(o=>String(o.value))),valid=ids.filter(id=>allowed.has(id))
   if(valid.length!==ids.length){
    if(valid.length)draft.value[field.key]=Array.isArray(value)?valid:valid[0];else delete draft.value[field.key]
    prunedQuery=prunedQuery||copy(initialized.value?applied.value:sourceQuery)
    if(valid.length)prunedQuery[field.key]=Array.isArray(value)?valid:valid[0];else delete prunedQuery[field.key]
    adjustment.value='授权范围或可选项已变化，已移除失效条件。';clearTimeout(adjustmentTimer)
    adjustmentTimer=setTimeout(()=>{const query=prunedQuery;prunedQuery=null;run(query,false)},50)
   }
  }
 }catch(error){if(!disposed&&error.name!=='AbortError'&&remoteControllers.get(field.key)===controller)state.error=error.message||'加载失败'}
 finally{if(remoteControllers.get(field.key)===controller)state.loading=false}
}
async function loadOptions(){
 if(!props.responseMode&&props.domain!=='admin-users')return
 optionsController?.abort();optionsController=new AbortController()
 const params=new URLSearchParams(clean(props.context));if(props.responseMode)params.set('definitions','true')
 try{
  const result=await get(`${props.optionsEndpoint||'/list-filters/'+optionDomain.value+'/filter-options'}?${params}`,{},{signal:optionsController.signal})
  if(!disposed&&cache.isCurrent()){
   fields.value=result.fields;if(adjustment.value.startsWith('筛选选项加载失败'))adjustment.value=''
   for(const f of fields.value)if(f.remote&&draft.value[f.key]?.length)void loadRemote(f,'',1,true)
  }
 }catch(e){if(!disposed&&e.name!=='AbortError')adjustment.value='筛选选项加载失败，请点击“重试选项”；已有条件保持不变。'}
}
watch(()=>JSON.stringify([draft.value.year||'',draft.value.month||'',clean(props.context)]),()=>{if(!props.responseMode)return;for(const field of fields.value.filter(f=>f.remote)){remoteControllers.get(field.key)?.abort();clearTimeout(remoteTimers.get(field.key));Object.assign(remoteState(field),{loaded:false,loading:false,page:0,total:0,error:''});if(draft.value[field.key]?.length)void loadRemote(field,'',1,false);else field.options=[]}})
watch(()=>accessState.permissionVersion,(next,previous)=>{if(next!==previous&&initialized.value&&props.responseMode){for(const controller of remoteControllers.values())controller.abort();loadOptions();refresh()}})
onMounted(()=>{loadOptions();run(draft.value,true)});onUnmounted(()=>{disposed=true;basicTrigger.cancel();seq++;controller?.abort();optionsController?.abort();clearTimeout(adjustmentTimer);for(const timer of remoteTimers.values())clearTimeout(timer);for(const controller of remoteControllers.values())controller.abort()})
defineExpose({refresh,reset,replace,patch,rememberPage,setPage,applied})
</script>
<style scoped>
.query-filter-bar{position:relative}.query-status{position:absolute;right:0;top:-18px;margin:0;line-height:16px;font-size:12px;color:#909399;pointer-events:none}.query-status .el-button{pointer-events:auto;padding:0 4px}.query-submit{width:80px}
.query-filter-bar{min-width:0;margin-bottom:16px}.basic-fields,.group-fields,.query-actions,.applied-tags{display:flex;flex-wrap:wrap;align-items:center;gap:10px}.query-actions{margin-left:auto}.query-actions :deep(.el-button){margin-left:0}.query-filter-bar :deep(.query-field){display:flex;align-items:center;gap:8px;font-size:13px;color:#606266;max-width:100%}.query-filter-bar :deep(.query-field>label){white-space:nowrap}.query-filter-bar :deep(.query-field>.el-select){width:170px}.query-filter-bar :deep(.query-keyword>.el-input){width:250px;max-width:100%}.query-filter-bar :deep(.query-range){display:flex;align-items:center;gap:6px;flex-wrap:wrap}.query-filter-bar :deep(.query-range>.el-input){width:140px}.query-filter-bar :deep(.query-range>.el-select){width:110px}.advanced-groups{margin-top:12px;padding:14px;background:#f7f9fc;border:1px solid #e7edf5;border-radius:6px}.query-group+.query-group{margin-top:14px}.query-group h5{margin:0 0 10px;color:#59677b;font-size:13px}.advanced-footer{display:flex;justify-content:space-between;flex-wrap:wrap;gap:8px;font-size:12px;color:#909399;margin-top:12px}.applied-tags{margin-top:10px;font-size:12px;color:#7b8794}.applied-tags :deep(.el-tag){height:auto;min-height:24px;white-space:normal;max-width:100%;padding:4px 8px}.pending-change{margin:8px 0 0;color:#b7791f;font-size:12px}@media(max-width:900px){.query-actions{margin-left:0}.query-filter-bar :deep(.query-field){flex-wrap:wrap}}
</style>
