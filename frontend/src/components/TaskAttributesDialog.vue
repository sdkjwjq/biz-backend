<template>
  <el-dialog v-model="visible" title="任务管理属性" width="520px" :close-on-click-modal="!saving" :close-on-press-escape="!saving" :show-close="!saving">
    <el-form label-width="110px" :disabled="saving">
      <el-form-item label="任务类型"><el-checkbox v-model="form.priority">重点</el-checkbox><el-checkbox v-model="form.difficult">困难</el-checkbox></el-form-item>
      <el-form-item label="计划完成日期"><el-date-picker v-model="form.plannedCompletionDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" /></el-form-item>
      <el-form-item label="变更原因"><el-input v-model="form.reason" type="textarea" maxlength="200" show-word-limit /></el-form-item>
    </el-form>
    <template #footer><el-button :disabled="saving" @click="visible=false">取消</el-button><el-button type="primary" :loading="saving" @click="save">预览并保存</el-button></template>
  </el-dialog>
</template>
<script setup>
import { ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { put } from '@/utils/request';
const emit=defineEmits(['saved']);
const visible=ref(false), saving=ref(false), form=ref({});
let task;
const kind=value=>[value.priority?'重点':'',value.difficult?'困难':''].filter(Boolean).join('、')||'一般';
const open=value=>{ task=value; form.value={priority:!!value.isPriority,difficult:!!value.isDifficult,plannedCompletionDate:value.plannedCompletionDate||'',version:value.managementVersion,reason:''}; visible.value=true; };
const save=async()=>{
  if(saving.value)return;
  if(!form.value.plannedCompletionDate||!form.value.reason.trim()){ElMessage.warning('请选择计划日期并填写变更原因');return;}
  saving.value=true;
  try {
    // 避免双击的第二次点击落到刚出现的确认框取消按钮。
    await new Promise(resolve=>setTimeout(resolve,250));
    await ElMessageBox.confirm(`任务类型：${kind({priority:task.isPriority,difficult:task.isDifficult})} → ${kind(form.value)}；计划完成日期：${task.plannedCompletionDate||'未设置'} → ${form.value.plannedCompletionDate}。原结束日期和逾期规则保持不变。`,'确认变更',{confirmButtonText:'确认保存',cancelButtonText:'取消',closeOnClickModal:false});
    await put(`/manage/tasks/${task.taskId}/attributes`,form.value);
    visible.value=false; ElMessage.success('任务管理属性已保存'); emit('saved',task.taskId);
  }catch(error){if(error!=='cancel'&&error!=='close')ElMessage.error(error.message||'保存失败，请重试');}
  finally{saving.value=false;}
};
defineExpose({open});
</script>
