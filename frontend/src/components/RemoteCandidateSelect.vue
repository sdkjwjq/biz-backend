<template>
  <el-select v-bind="$attrs" :model-value="modelValue" :multiple="multiple" filterable remote clearable :remote-method="search" :loading="loading" @update:model-value="update" @change="value => emit('change', value)" @visible-change="open => open && !loaded && load('', 1)">
    <el-option v-for="item in choices" :key="item.value" :value="Number(item.value)" :label="item.label" />
    <template #footer><el-button v-if="error" link type="primary" @click="load(keyword, lastRequested)">选项加载失败，点击重试</el-button><el-button v-else-if="total > page * 50" link type="primary" :loading="loading" @click="load(keyword, page + 1)">加载更多</el-button></template>
  </el-select>
</template>
<script setup>
import { ref, watch, onBeforeUnmount } from 'vue'
import { get } from '@/utils/request'
defineOptions({ inheritAttrs: false })
const props=defineProps({ modelValue: { type: [Number, String, Array], default: null }, multiple: Boolean, endpoint: { type: String, required: true }, params: { type: Object, default: () => ({}) } })
const emit=defineEmits(['update:modelValue','change','loaded'])
const choices=ref([]),loading=ref(false),error=ref(''),loaded=ref(false),total=ref(0),page=ref(0),lastRequested=ref(1)
let controller,seq=0,timer,keyword='',disposed=false
const ids=()=> (Array.isArray(props.modelValue)?props.modelValue:props.modelValue==null?[]:[props.modelValue]).map(String)
const update=value=>emit('update:modelValue',Array.isArray(value)?value.map(Number):value==null||value===''?null:Number(value))
function search(value){clearTimeout(timer);timer=setTimeout(()=>load(value,1),250)}
async function load(term='',requested=1){
 controller?.abort();controller=new AbortController();const current=++seq,selected=ids(),owned=controller;keyword=term;lastRequested.value=requested;loading.value=true;error.value=''
 try{
  const params={...props.params,keyword:term,page:requested,pageSize:50,ids:selected.slice(0,100)}
  const result=await get(props.endpoint,params,{signal:owned.signal})
  if(disposed||current!==seq)return
  const extras=[];for(let start=100;start<selected.length;start+=100){const echo=await get(props.endpoint,{...params,page:1,pageSize:1,ids:selected.slice(start,start+100)},{signal:owned.signal});extras.push(...echo.selected)}
  if(disposed||current!==seq)return
  const kept=requested>1?choices.value:choices.value.filter(item=>selected.includes(String(item.value)))
  choices.value=[...new Map([...kept,...result.items,...result.selected,...extras].map(item=>[String(item.value),item])).values()]
  page.value=result.page;total.value=result.total;loaded.value=true;emit('loaded',choices.value)
 }catch(e){if(!disposed&&current===seq&&e.name!=='AbortError')error.value=e.message||'选项加载失败'}finally{if(current===seq)loading.value=false}
}
watch(()=>JSON.stringify([props.endpoint,props.params,ids()]),()=>{clearTimeout(timer);loaded.value=false;load('',1)},{immediate:true})
onBeforeUnmount(()=>{disposed=true;seq++;controller?.abort();clearTimeout(timer)})
</script>
