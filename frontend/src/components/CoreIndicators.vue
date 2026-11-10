<template>
 <div class="core-page">
  <header><div><h2>核心指标</h2><p>专业群、管理员及双高办填报，归口部门审核、双高办归档；与绩效完成值分别保存。</p></div></header>
  <QueryFilterBar ref="queryBar" domain="core" title="核心指标" :required-keys="['year','month']" :defaults="{year:String(now.getFullYear()),month:String(now.getMonth()+1)}" @result="receiveQuery">
   <template #actions><el-button :loading="exporting" :disabled="exporting||!filteredIndicators.length" @click="exportIndicators">导出汇总表</el-button></template>
  </QueryFilterBar>
  <el-alert v-if="error" :title="error" type="error" :closable="false" />
  <section class="indicator-panel">
   <div class="table-caption"><span>核心产出指标</span><span class="table-count">共 {{filteredIndicators.length}} / {{rows.length}} 项</span></div>
   <el-table v-loading="loading" :data="filteredIndicators" row-key="indicatorId" class="indicator-table" :row-class-name="()=> 'indicator-row'" border empty-text="暂无核心指标" @row-click="openRowDetail">
    <el-table-column type="expand" width="48">
     <template #default="{row:item}">
      <div class="recent-reports">
       <div class="recent-heading">最近两次正式填报 <span>跨月份展示，完整记录可在“历史记录”中查看。</span></div>
       <el-table :data="item.recentReports || []" border empty-text="暂无正式填报">
        <el-table-column label="填报月份" width="115"><template #default="{row}">{{row.year}}年{{row.month}}月</template></el-table-column>
        <el-table-column prop="submitName" label="填报人" min-width="130" />
        <el-table-column label="截至上年年末累计值" min-width="160"><template #default="{row}">{{display(row.previousYearValue)}}</template></el-table-column>
        <el-table-column label="截至本次累计值" min-width="140"><template #default="{row}">{{display(row.actualValue)}}</template></el-table-column>
        <el-table-column label="状态" width="145"><template #default="{row}"><el-tag :type="statusType(row.flowStatus)" size="small">{{status(row.flowStatus)}}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="150"><template #default="{row}"><el-button link type="primary" @click="view(row.reportId)">查看</el-button><el-button v-if="canUsePermission('core.withdraw')&&(isAdminTestEnabled()||String(row.submitBy)===String(accessState.userId))&&row.flowStatus===10" link type="warning" :disabled="busy" @click="withdraw(row)">撤回</el-button></template></el-table-column>
       </el-table>
      </div>
     </template>
    </el-table-column>
    <el-table-column prop="sortOrder" label="序号" width="60" align="center" />
    <el-table-column prop="indicatorCode" label="指标编号" width="105" />
    <el-table-column label="核心指标名称" min-width="300"><template #default="{row}"><div class="indicator-name">{{row.indicatorName || '映射待核实'}}</div><div v-if="row.configurationError" class="configuration-error">{{row.configurationError}}</div></template></el-table-column>
    <el-table-column label="项目期满目标值" width="140" align="center"><template #default="{row}">{{display(row.targetValue)}}</template></el-table-column>
    <el-table-column label="当前累计归档完成值" width="170" align="center"><template #default="{row}"><strong class="archived-value">{{display(row.effectiveResult?.actualValue)}}</strong><div class="cell-note">{{row.effectiveResult?`${row.effectiveResult.year}年${row.effectiveResult.month}月`:'尚未归档'}}</div></template></el-table-column>
    <el-table-column label="本年度归档完成值" width="155" align="center"><template #default="{row}"><template v-if="yearArchivedOf(row)!=null"><span>{{display(yearArchivedOf(row))}}</span></template><span v-else class="cell-note">暂无归档</span></template></el-table-column>
    <el-table-column label="最近正式填报" min-width="190"><template #default="{row}"><template v-if="latest(row)"><el-tag :type="statusType(latest(row).flowStatus)" size="small">{{status(latest(row).flowStatus)}}</el-tag><div class="cell-note">{{latest(row).year}}年{{latest(row).month}}月 · {{latest(row).submitName || '—'}}</div></template><span v-else class="cell-note">暂无正式填报</span></template></el-table-column>
    <el-table-column label="归口部门" min-width="180" show-overflow-tooltip><template #default="{row}"><span :class="{deptEmpty:!row.deptName}">{{row.deptName || '—'}}</span></template></el-table-column>
    <el-table-column label="归口审核人" width="145" align="center"><template #default="{row}"><span :class="{deptEmpty:!row.principalName}">{{row.principalName || '—'}}</span></template></el-table-column>
    <el-table-column label="操作" width="200" fixed="right"><template #default="{row}"><div class="row-actions" @click.stop><el-button v-if="canFill" link type="primary" :disabled="!!row.configurationError||busy||future(year,month)" @click="edit(row)" title="填报或继续本人草稿">填报</el-button><el-button v-if="canUsePermission('core.history')" link type="primary" @click="history(row)">历史记录</el-button></div></template></el-table-column>
   </el-table>
   <p class="table-hint">点击行首箭头查看最近两次正式填报；退回后重新填报仍需经过完整审核流程。</p>
  </section>
  <el-dialog v-model="visible" :title="editable?'核心指标填报':'核心指标详情'" width="min(760px, 94vw)" :before-close="close" :close-on-click-modal="false" destroy-on-close>
   <template v-if="report">
    <el-alert v-if="report.mappingObsolete" title="此记录为修正前的旧关联记录，仅供查阅，不计入当前指标完成值。请重新填报当前指标。" type="warning" :closable="false" />
    <el-alert v-if="editable && report.correctionSource" type="warning" :closable="false" class="correction-alert">
     <template #title>上次填报已退回，请按审核意见补正后重新提交</template>
     <div>退回人：{{report.correctionSource.returnedBy || '—'}} · 时间：{{report.correctionSource.returnedAt || '—'}}</div>
     <div>退回原因：{{report.correctionSource.reason || '未填写'}}</div>
     <el-button link type="primary" @click="view(report.correctionSource.reportId)">查看原退回单</el-button>
    </el-alert>
    <h3>{{report.indicatorName}}</h3><p>{{report.year}}年{{report.month}}月 · 项目期满目标值 {{display(report.targetValue)}} · {{status(report.flowStatus)}}</p>
    <el-form label-position="top" :disabled="busy||!editable">
     <el-form-item label="截至上年年末累计完成值"><el-input v-model="form.previousYearValue" inputmode="decimal" placeholder="非负数，最多4位小数" /></el-form-item>
     <el-form-item label="截至本次填报累计完成值"><el-input v-model="form.actualValue" inputmode="decimal" placeholder="非负数，允许超额完成" /></el-form-item>
     <el-form-item label="工作说明"><el-input v-model="form.comment" type="textarea" :rows="5" maxlength="500" show-word-limit /></el-form-item>
    </el-form>
    <el-table v-if="report.logs?.length" :data="report.logs" border><el-table-column prop="action" label="操作" width="80"/><el-table-column prop="operatorName" label="操作人" width="120"/><el-table-column prop="comment" label="意见"/><el-table-column prop="createTime" label="时间" min-width="160"/></el-table>
   </template>
   <template #footer><el-button :disabled="busy" @click="close(()=>visible=false)">返回</el-button><template v-if="editable"><el-button :loading="busy" @click="save(false)">保存草稿</el-button><el-button type="primary" :loading="busy" @click="save(true)">提交审核</el-button></template></template>
  </el-dialog>
  <el-dialog v-model="historyVisible" title="核心指标历史记录" width="min(1000px, 94vw)">
   <div class="filters"><el-select v-model="historyYear" clearable placeholder="全部年度" @change="loadHistory"><el-option v-for="y in years" :key="y" :label="`${y}年`" :value="y" /></el-select><el-select v-model="historyMonth" clearable placeholder="全部月份" @change="loadHistory"><el-option v-for="m in 12" :key="m" :label="`${m}月`" :value="m" /></el-select></div>
   <el-table v-loading="historyLoading" :data="historyRows" border><el-table-column prop="year" label="年度" width="80"/><el-table-column prop="month" label="月份" width="70"/><el-table-column prop="submitName" label="填报人"/><el-table-column prop="indicatorName" label="填报时指标名称" min-width="180"/><el-table-column prop="actualValue" label="完成值"/><el-table-column label="状态"><template #default="{row}">{{status(row.flowStatus)}}<el-tag v-if="row.mappingObsolete" type="warning" size="small">旧关联记录</el-tag></template></el-table-column><el-table-column label="操作"><template #default="{row}"><el-button link type="primary" @click="view(row.reportId)">查看</el-button></template></el-table-column></el-table>
  </el-dialog>
 </div>
</template>
<script setup>
import QueryFilterBar from './QueryFilterBar.vue';
import { isAdminTestEnabled } from '@/utils/adminTest';
import {ref,computed,watch,onMounted,onBeforeUnmount} from 'vue';
import {onBeforeRouteLeave,useRoute} from 'vue-router';
import {ElMessage,ElMessageBox} from 'element-plus';
import {accessState,canUsePermission} from '@/utils/accessState';
import {getCoreCapabilities,getCoreIndicators,createCoreDraft,getCoreReport,saveCoreDraft,submitCoreDraft,withdrawCoreReport,getCoreHistory,exportCoreIndicators} from '@/api/coreIndicators';
const now=new Date(),years=[2025,2026,2027,2028,2029],year=ref(Math.min(2029,Math.max(2025,now.getFullYear()))),month=ref(now.getMonth()+1);
const route=useRoute(),rows=ref([]),loading=ref(false),error=ref(''),cap=ref({}),busy=ref(false),visible=ref(false),report=ref(null),form=ref({}),saved=ref(''),exporting=ref(false);
const historyVisible=ref(false),historyRows=ref([]),historyYear=ref(''),historyMonth=ref(''),historyLoading=ref(false);let historyId,loadVersion=0,detailVersion=0,historyVersion=0,disposed=false;
const canFill=computed(()=>canUsePermission('core.fill')&&cap.value.fillableYears?.includes(year.value));
const editable=computed(()=>canUsePermission('core.fill')&&report.value?.flowStatus===0&&!report.value?.mappingObsolete);
const dirty=computed(()=>editable.value&&JSON.stringify(form.value)!==saved.value);
const display=value=>value===null||value===undefined||value===''?'—':value;
const status=value=>({0:'草稿',10:'待归口审核',20:'待双高办归档',30:'已归档','-10':'归口退回','-20':'归档退回','-1':'已撤回','-2':'草稿已提交'})[value]||'待核实';
const latest=item=>item.recentReports?.[0];
// 本年度归档完成值 = 当前累计归档完成值 - 上年末归档完成值
const yearArchivedOf=item=>{
 const current=item?.effectiveResult?.actualValue;
 if(current==null)return null;
 const previous=item?.previousYearResult?.actualValue??0;
 return Number(current)-Number(previous);
};
const queryBar=ref(null),queryIds=ref(new Set());
// 月份已移入高级筛选：进入页面默认当月；切换到往年时默认取 12 月（年末），手动改过月份则不再覆盖。
let patchedYear=null;
const receiveQuery=async result=>{
 const queryYear=Number(result.query.year||now.getFullYear());
 const queryMonth=Number(result.query.month||now.getMonth()+1);
 const desiredMonth=queryYear<now.getFullYear()?12:now.getMonth()+1;
 if(queryYear!==patchedYear&&queryMonth!==desiredMonth){
  patchedYear=queryYear;
  queryBar.value?.patch({month:String(desiredMonth)});
  return;
 }
 patchedYear=queryYear;
 queryIds.value=new Set(result.ids.map(String));
 year.value=queryYear;
 month.value=queryMonth;
 await load();
};

const filteredIndicators=computed(()=>rows.value.filter(item=>queryIds.value.has(String(item.indicatorId))));
// 导出当前筛选结果：本年归档完成值 = 累计归档完成值 - 上年末归档完成值，工作说明取本年最近一次填报。
const exportIndicators=async()=>{
 if(exporting.value||!filteredIndicators.value.length)return;
 exporting.value=true;
 try{
  const ids=filteredIndicators.value.map(item=>item.indicatorId);
  const blob=await exportCoreIndicators({year:year.value,month:month.value,ids});
  const url=URL.createObjectURL(blob),link=document.createElement('a');
  link.href=url;link.download=`${year.value}年核心指标.xlsx`;link.click();
  setTimeout(()=>URL.revokeObjectURL(url),1000);
 }catch(e){ElMessage.error(e.message||'导出失败');}finally{exporting.value=false;}
};
const statusType=value=>value===30?'success':value===10||value===20?'warning':value===-10||value===-20?'danger':'info';
const future=(y,m)=>y>now.getFullYear()||(y===now.getFullYear()&&m>now.getMonth()+1);
let firstDataLoaded=false,pendingInitialLoad=null,initialLinkPending=true;
const load=()=>{
 const requestedYear=year.value,requestedMonth=month.value,key=`${requestedYear}:${requestedMonth}`;
 if(!firstDataLoaded&&pendingInitialLoad?.key===key)return pendingInitialLoad.promise;
 const version=++loadVersion;loading.value=true;error.value='';
 const promise=(async()=>{try{
  const [data,permissions]=await Promise.all([getCoreIndicators({year:requestedYear,month:requestedMonth}),getCoreCapabilities()]);
  if(disposed||version!==loadVersion)return;
  rows.value=data;cap.value=permissions;firstDataLoaded=true;
 }catch(e){if(version===loadVersion&&!disposed){rows.value=[];error.value=e.message||'加载失败，请重试';}}
 finally{if(version===loadVersion&&!disposed)loading.value=false;}})();
 pendingInitialLoad={key,promise};
 promise.finally(()=>{if(pendingInitialLoad?.promise===promise)pendingInitialLoad=null;});
 return promise;
};
const assign=data=>{report.value=data;form.value={previousYearValue:data.previousYearValue==null?'':String(data.previousYearValue),actualValue:data.actualValue==null?'':String(data.actualValue),comment:data.comment||''};saved.value=JSON.stringify(form.value);};
const confirmLeave=async()=>{if(busy.value)return false;if(!dirty.value)return true;try{await ElMessageBox.confirm('有未保存的填报内容，是否离开？','离开确认',{confirmButtonText:'离开',cancelButtonText:'继续填写'});return true;}catch{return false;}};
const close=async done=>{if(await confirmLeave()){detailVersion++;visible.value=false;report.value=null;done?.();}};
const edit=async item=>{if(busy.value||!await confirmLeave())return;busy.value=true;const version=++detailVersion;try{const data=await createCoreDraft(item.indicatorId,{year:year.value,month:month.value});if(disposed||version!==detailVersion)return;assign(data);visible.value=true;}catch(e){ElMessage.error(e.message||'草稿打开失败');}finally{busy.value=false;}};
const view=async id=>{if(busy.value||!await confirmLeave())return;const version=++detailVersion;try{const data=await getCoreReport(id);if(disposed||version!==detailVersion)return;assign(data);visible.value=true;}catch(e){ElMessage.error(e.message||'详情加载失败');}};
// 点击数据行直接打开该指标最近一次正式填报的详情，与“历史记录 → 查看”是同一个对话框。
const openRowDetail=(row,column,event)=>{
 if(event?.target?.closest('button,a,.el-table__expand-icon'))return;
 if(column?.type==='expand')return;
 const target=latest(row)||row.effectiveResult;
 if(!target){ElMessage.info('该指标暂无填报记录，可点击“历史记录”查看');return;}
 view(target.reportId);
};
const save=async submit=>{if(busy.value)return;for(const value of [form.value.previousYearValue,form.value.actualValue]){if((submit&&!value.trim())||(value.trim()&&!/^\d{1,16}(\.\d{1,4})?$/.test(value.trim()))){ElMessage.warning('请填写非负完成值，最多16位整数及4位小数');return;}}
 busy.value=true;try{if(submit)await ElMessageBox.confirm('提交后文字和目标、审核人将冻结；归口审核后由双高办归档。是否提交？','提交确认',{confirmButtonText:'确认提交',cancelButtonText:'取消',closeOnClickModal:false});const data=await saveCoreDraft(report.value.reportId,{version:report.value.version,previousYearValue:form.value.previousYearValue.trim()||null,actualValue:form.value.actualValue.trim()||null,comment:form.value.comment});assign(data);if(submit){assign(await submitCoreDraft(data.reportId,data.version));window.dispatchEvent(new Event('audit-count-updated'));}ElMessage.success(submit?'已提交审核':'草稿已保存');await queryBar.value?.refresh();}catch(e){if(e!=='cancel'&&e!=='close')ElMessage.error(e.message||'操作失败，内容已保留');}finally{busy.value=false;}};
const withdraw=async row=>{if(busy.value)return;busy.value=true;try{await ElMessageBox.confirm('撤回后本次记录仍保留，可重新填写并提交。','撤回确认',{confirmButtonText:'确认撤回',cancelButtonText:'取消',closeOnClickModal:false});await withdrawCoreReport(row.reportId,row.version);window.dispatchEvent(new Event('audit-count-updated'));await queryBar.value?.refresh();}catch(e){if(e!=='cancel'&&e!=='close')ElMessage.error(e.message||'撤回失败');}finally{busy.value=false;}};
const loadHistory=async()=>{const version=++historyVersion;historyLoading.value=true;try{const params={};if(historyYear.value)params.year=historyYear.value;if(historyMonth.value)params.month=historyMonth.value;const data=await getCoreHistory(historyId,params);if(!disposed&&version===historyVersion)historyRows.value=data;}catch(e){if(version===historyVersion)ElMessage.error(e.message||'历史加载失败');}finally{if(version===historyVersion)historyLoading.value=false;}};
const history=item=>{historyId=item.indicatorId;historyRows.value=[];historyYear.value='';historyMonth.value='';historyVisible.value=true;loadHistory();};
watch(year,load);watch(historyVisible,value=>{if(!value)historyVersion++;});
watch(()=>route.query.reportId,id=>{if(id)view(id);});
watch([loading,rows],()=>{
 if(!initialLinkPending||loading.value||disposed)return;
 initialLinkPending=false;
 if(route.query.reportId)view(route.query.reportId);
 else if(route.query.indicatorId){const item=rows.value.find(r=>String(r.indicatorId)===String(route.query.indicatorId));if(item)history(item);}
},{flush:'post'});
onBeforeRouteLeave(confirmLeave);onBeforeUnmount(()=>{disposed=true;loadVersion++;detailVersion++;historyVersion++;});onMounted(()=>{load();});
</script>
<style scoped>
.core-page{padding:24px;overflow:auto;height:100%;min-width:0;box-sizing:border-box;background:#f3f6fa}
.core-page h2{margin:0 0 10px;font-size:24px;color:#24354b}
.correction-alert{margin-bottom:12px}
.core-page header p{margin:0 0 20px;color:#738092;line-height:1.6}
.filters{display:flex;align-items:center;gap:12px;flex-wrap:wrap;margin-bottom:20px}
.filters .el-select{width:130px}
.core-filter-item{display:flex;align-items:center;gap:8px}
.indicator-panel{background:#fff;border:1px solid #e5eaf2;border-radius:8px;padding:20px;min-width:0}
.table-caption{display:flex;align-items:center;justify-content:space-between;margin-bottom:16px;font-size:16px;font-weight:600;color:#344256}
.table-count{font-size:13px;font-weight:400;color:#8390a2}
.indicator-table{width:100%;--el-table-header-bg-color:#f6f8fb;--el-table-header-text-color:#536278;--el-table-row-hover-bg-color:#f4f8ff}
.indicator-table :deep(.indicator-row .el-table__cell){padding:14px 0}
.indicator-table :deep(.indicator-row){cursor:pointer}
.indicator-name{white-space:normal;overflow-wrap:anywhere;line-height:1.6;color:#27384e;font-weight:500}
.archived-value{color:#2876cf;font-size:16px;font-variant-numeric:tabular-nums}
.cell-note{font-size:12px;color:#8491a3;line-height:1.6;margin-top:4px;overflow-wrap:anywhere}
.deptEmpty{color:#a8b2c0}
.row-actions{display:flex;gap:12px;flex-wrap:wrap;align-items:center}
.row-actions .el-button+.el-button{margin-left:0}
.configuration-error{font-size:12px;color:#d97816;line-height:1.5;margin-top:4px}
.table-hint{margin:14px 0 0;color:#8491a3;font-size:12px;line-height:1.6}
.recent-reports{padding:16px 24px 20px;background:#f7f9fc;overflow:auto}
.recent-heading{margin-bottom:12px;font-size:14px;font-weight:600;color:#46556b}
.recent-heading span{margin-left:12px;font-size:12px;font-weight:400;color:#8491a3}
@media(max-width:1000px){.core-page{padding:16px}.indicator-panel{padding:14px}.recent-reports{padding:14px}.row-actions{gap:6px}}
</style>
