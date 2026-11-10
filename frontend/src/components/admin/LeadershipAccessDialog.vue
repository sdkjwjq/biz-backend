<template>
  <el-dialog v-model="visible" title="校级领导查阅范围" width="560px" :close-on-click-modal="false" :close-on-press-escape="!saving" :show-close="!saving">
    <div v-loading="loading">
      <p>{{ user?.nickName }}（{{ user?.userId }}）</p>
      <el-alert title="仅增加查阅能力，不授予管理员或业务填报、审核权限。" type="info" :closable="false" />
      <el-form label-position="top" :disabled="saving || loading" @submit.prevent>
        <el-form-item label="查阅范围"><el-select v-model="form.scope" @change="form.departmentIds=[]"><el-option label="取消领导查阅配置" value="NONE"/><el-option label="全校" value="ALL"/><el-option label="指定归口部门" value="DEPARTMENTS"/></el-select></el-form-item>
        <el-form-item v-if="form.scope==='DEPARTMENTS'" label="任务及绩效归口部门"><el-select v-model="form.departmentIds" multiple filterable placeholder="请选择归口部门"><el-option v-for="dept in departments.filter(item=>item.status==='0')" :key="dept.deptId" :label="dept.deptName" :value="Number(dept.deptId)"/></el-select></el-form-item>
        <p v-if="form.scope!=='NONE'">成果、预算及已提交工作纪实可全校查阅；管理概览只读。</p>
        <el-form-item label="变更原因"><el-input v-model="form.reason" type="textarea" maxlength="500" show-word-limit/></el-form-item>
      </el-form>
      <el-alert v-if="error" :title="error" type="error" :closable="false"/>
    </div>
    <template #footer><el-button :disabled="saving" @click="visible=false">取消</el-button><el-button type="primary" :disabled="loading || !loaded" :loading="saving" @click="save">保存</el-button></template>
  </el-dialog>
</template>
<script setup>
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getLeadershipAccess, saveLeadershipAccess } from '@/api/admin'
const props = defineProps({ departments: { type: Array, default: () => [] } })
const visible = ref(false), loading = ref(false), saving = ref(false), error = ref(''), user = ref(null)
const form = reactive({ scope: 'NONE', departmentIds: [], version: 0, reason: '' })
const loaded = ref(false)
let generation = 0
let before = { scope: 'NONE', departmentIds: [] }
const label = value => ({ NONE: '无领导查阅权限', ALL: '全校', DEPARTMENTS: '指定归口部门' })[value]
const summary = value => value.scope === 'DEPARTMENTS' ? `${label(value.scope)}：${value.departmentIds.map(id => props.departments.find(dept => Number(dept.deptId) === Number(id))?.deptName || id).join('、')}` : label(value.scope)
async function open(row) {
  const version = ++generation
  user.value = row; visible.value = true; loading.value = true; loaded.value = false; error.value = ''
  Object.assign(form, { scope: 'NONE', departmentIds: [], version: 0, reason: '' })
  try {
    const result = await getLeadershipAccess(row.userId)
    if (version !== generation) return
    Object.assign(form, result); before = { scope: result.scope, departmentIds: [...result.departmentIds] }; loaded.value = true
  } catch (exception) { if (version === generation) error.value = exception.message || '加载失败' }
  finally { if (version === generation) loading.value = false }
}
async function save() {
  if (saving.value || loading.value || !loaded.value) return
  if (!form.reason.trim() || (form.scope === 'DEPARTMENTS' && !form.departmentIds.length)) { error.value = '请填写变更原因并选择有效范围'; return }
  saving.value = true; error.value = ''
  try {
    await ElMessageBox.confirm(`查阅范围由“${summary(before)}”改为“${summary(form)}”。保存后在后续请求中生效，是否确认？`, '确认变更', { confirmButtonText: '确认保存', cancelButtonText: '取消', closeOnClickModal: false })
    await saveLeadershipAccess(user.value.userId, { ...form, departmentIds: [...form.departmentIds] })
    visible.value = false; ElMessage.success('查阅范围已更新')
  } catch (exception) { if (exception !== 'cancel' && exception !== 'close') error.value = exception.message || '保存失败，请重新加载后重试' }
  finally { saving.value = false }
}
defineExpose({ open })
</script>
<style scoped>
.el-select{width:100%}.el-form{margin-top:18px}p{font-size:13px;color:#606266}
</style>
