<template>
  <el-dialog v-model="visible" title="移交审核职责" width="min(760px, calc(100vw - 32px))" append-to-body :close-on-click-modal="false" :show-close="!busy" :close-on-press-escape="!busy">
    <div v-loading="loading" style="min-height:120px"><template v-if="data"><el-alert title="这是业务职责变更，不是批准或退回。原始提交人、材料、审核结论和历史保持不变。" type="warning" show-icon :closable="false" /><p>业务对象：{{ data.target.objectId }}；当前阶段：{{ stageName }}；本次将同步同一业务对象、同一阶段的 {{ data.affected.length }} 条待办。</p><p>审核阶段同时修改后续提交使用的审核人配置；归档阶段仅移交这些待办，接收人必须为正常管理员。</p><el-form label-position="top"><el-form-item label="新审核处理人"><RemoteCandidateSelect v-model="handlerId" endpoint="/admin/permissions/candidates" :params="{domain:'active-users'}" aria-label="新审核处理人" :disabled="busy" style="width:100%" /></el-form-item><el-form-item label="移交原因"><el-input v-model="reason" type="textarea" :rows="3" maxlength="500" show-word-limit aria-label="审核职责移交原因" :disabled="busy" /></el-form-item></el-form><el-table :data="data.affected" max-height="220"><el-table-column prop="id" label="审核单" /><el-table-column prop="handler" label="原处理人" /><el-table-column label="新处理人"><template #default>{{ handlerId || '待选择' }}</template></el-table-column></el-table></template><el-alert v-if="error" :title="error" type="error" :closable="false" /></div>
    <template #footer><el-button :disabled="busy" @click="visible=false">取消</el-button><el-button type="primary" :loading="busy" :disabled="loading || !data || !handlerId || !reason.trim()" @click="save">确认移交</el-button></template>
  </el-dialog>
</template>
<script setup>
import RemoteCandidateSelect from '../RemoteCandidateSelect.vue'
import {computed,onUnmounted,ref} from 'vue'
import {ElMessage,ElMessageBox} from 'element-plus'
import {previewDutyTransfer,saveDutyTransfer} from '@/api/permissions'
const emit=defineEmits(['transferred']),visible=ref(false),data=ref(null),handlerId=ref(null),reason=ref(''),loading=ref(false),busy=ref(false),error=ref('')
const stageName=computed(()=>data.value?.target.archive?'待归档':data.value?.target.domain==='core'?'待归口审核（同时修改对应绩效 principalId，已提交报告的审核人快照保留）':data.value?.target.domain==='tasks'&&Number(data.value.target.stage)===10?'待专业群审核':'待归口审核')
let seq=0
async function open(row){const current=++seq;visible.value=true;data.value=null;handlerId.value=null;reason.value='';error.value='';loading.value=true;try{const result=await previewDutyTransfer({domain:row.domain,id:row.id,separateOptions:true});if(current===seq)data.value=result}catch(e){if(current===seq)error.value=e.message}finally{if(current===seq)loading.value=false}}
async function save(){if(busy.value)return;busy.value=true;error.value='';try{await ElMessageBox.confirm('确认同步变更上述审核职责和在途处理人？请核对接收账号和影响范围。','确认审核职责移交',{confirmButtonText:'确认变更',cancelButtonText:'返回核对',type:'warning',closeOnClickModal:false});const result=await saveDutyTransfer({domain:data.value.target.domain,id:data.value.target.id,handlerId:handlerId.value,token:data.value.token,reason:reason.value});ElMessage.success(result.message);visible.value=false;emit('transferred')}catch(e){if(e instanceof Error)error.value=e.message+'；填写内容仍保留，请重新打开以获取最新待办。'}finally{busy.value=false}}
onUnmounted(()=>seq++)
defineExpose({open,isBusy:busy})
</script>
