<template>
  <section class="budget-trend" v-loading="loading">
    <div class="trend-toolbar"><h3>预算执行逐月趋势</h3>
      <el-select v-model="taskIndex" aria-label="趋势改革任务"><el-option label="全校合计" :value="0"/><el-option v-for="n in 10" :key="n" :label="`${n}. ${tasks.find(t=>Number(t.index)===n)?.name || '改革任务'}`" :value="n"/></el-select>
      <el-select v-model="metric" aria-label="趋势统计口径"><el-option label="累计支出金额" value="cumulative"/><el-option label="当月新增支出" value="incremental"/><el-option label="年度预算执行率" value="executionRate"/></el-select>
      <el-button @click="load">刷新趋势</el-button>
    </div>
    <p>金额单位：万元。缺月或缺值留空；新增支出只与紧邻上月比较，不跨月补算。</p>
    <el-alert v-if="error" :title="error" type="error" :closable="false"/>
    <el-alert v-if="points.some(p=>p.adjustment)" title="部分月份支出减少，可能存在财务调账，请结合原始数据核实。" type="warning" :closable="false"/>
    <div ref="chartElement" class="trend-chart" role="img" :aria-label="`${year}年预算执行趋势`"/>
    <el-empty v-if="!loading&&!error&&!points.some(p=>p[metric]!=null)" description="所选年度与口径暂无可展示数据"/>
  </section>
</template>
<script setup>
import {ref,watch,onMounted,onBeforeUnmount,nextTick} from 'vue';
import * as echarts from 'echarts';
import {getBudgetTrends} from '@/api/budget';
const props=defineProps({year:{type:[String,Number],required:true},revision:{type:Number,default:0},tasks:{type:Array,default:()=>[]}});
const taskIndex=ref(0),metric=ref('cumulative'),points=ref([]),loading=ref(false),error=ref(''),chartElement=ref(null);
let chart,observer,controller,sequence=0,disposed=false;
const draw=async()=>{await nextTick();if(disposed||!chartElement.value)return;
  chart ||= echarts.init(chartElement.value);
  chart.setOption({animation:false,tooltip:{trigger:'axis',valueFormatter:value=>value==null?'—':`${value}${metric.value==='executionRate'?'%':'万元'}`},grid:{left:65,right:25,top:35,bottom:35},xAxis:{type:'category',data:Array.from({length:12},(_,i)=>`${i+1}月`)},yAxis:{type:'value',name:metric.value==='executionRate'?'%':'万元'},series:[{type:'line',connectNulls:false,smooth:false,symbolSize:7,data:points.value.map(p=>p[metric.value]),lineStyle:{color:'#409eff'},itemStyle:{color:'#409eff'}}]},true);
};
const load=async()=>{const current=++sequence;controller?.abort();controller=new AbortController();loading.value=true;error.value='';points.value=[];
 try{const data=await getBudgetTrends(props.year,taskIndex.value,{signal:controller.signal});if(disposed||current!==sequence)return;points.value=data.points||[];await draw();}
 catch(e){if(disposed||current!==sequence||e.name==='AbortError')return;error.value=e.message||'趋势加载失败，请重试';await draw();}
 finally{if(!disposed&&current===sequence)loading.value=false;}
};
watch(()=>[props.year,props.revision,taskIndex.value],load);watch(metric,draw);
onMounted(()=>{observer=new ResizeObserver(()=>chart?.resize());observer.observe(chartElement.value);load();});
onBeforeUnmount(()=>{disposed=true;sequence++;controller?.abort();observer?.disconnect();chart?.dispose();});
</script>
<style scoped>
.budget-trend{background:#fff;border:1px solid #e5eaf2;border-radius:8px;padding:16px;margin-bottom:16px;flex-shrink:0;min-width:0}.trend-toolbar{display:flex;align-items:center;gap:12px;flex-wrap:wrap}.trend-toolbar h3{margin:0 auto 0 0}.trend-toolbar .el-select{width:175px}.budget-trend p{color:#8791a1;font-size:13px}.trend-chart{height:300px;width:100%;min-width:0}
</style>
