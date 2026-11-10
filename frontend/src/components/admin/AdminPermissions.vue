<template>
  <div class="permissions-page" v-loading="loading">
    <el-alert title="专家评审已按评审任务独立授权，请到管理中心的“评审任务管理”添加专家和配置范围；旧专家模板不再自动产生新轮次权限。" type="info" :closable="false" />
    <div class="page-head"><div><h2>角色与权限</h2><p>配置可用功能和查阅范围；填报人、审核人仍在业务配置与移交中指定。</p></div><el-button :disabled="busy" @click="reload">刷新配置</el-button></div>
    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />
    <el-tabs v-model="tab" class="panel">
      <el-tab-pane label="角色模板" name="roles">
        <div class="toolbar"><span class="muted">管理员固定为全权限，其他模板可以调整。已有人员使用的角色不能删除。</span><el-button type="primary" @click="openRole()">新增角色</el-button></div>
        <QueryFilterBar ref="roleQuery" domain="admin-roles" title="角色" @result="result=>roleIds=new Set(result.ids)" />
        <el-table :data="catalog.roles.filter(role=>roleIds.has(role.code))" stripe>
          <el-table-column prop="name" label="角色" min-width="160" />
          <el-table-column label="来源" width="120"><template #default="{row}">{{ row.builtin ? (row.dynamic ? '业务资格模板' : '内置模板') : '自定义' }}</template></el-table-column>
          <el-table-column label="功能数" width="90"><template #default="{row}">{{ row.permissions.length }}</template></el-table-column>
          <el-table-column label="状态" width="95"><template #default="{row}"><el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '启用' : '停用' }}</el-tag></template></el-table-column>
          <el-table-column label="操作" min-width="240"><template #default="{row}"><el-button link type="primary" :disabled="row.code === 'ADMIN'" @click="openRole(row)">查看／修改</el-button><el-button link type="primary" @click="openRole(row, true)">复制</el-button><el-button v-if="!row.builtin" link type="danger" @click="removeRole(row)">删除</el-button></template></el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="个人授权" name="users">
        <div class="toolbar user-picker"><RemoteCandidateSelect v-model="selectedUser" aria-label="选择权限账号" endpoint="/admin/permissions/candidates" :params="{domain: 'users'}" placeholder="搜索姓名、工号或账号" :disabled="busy" @change="selectUser" /><el-button :disabled="!selectedUser || busy" @click="selectUser(selectedUser)">重新加载该账号</el-button></div>
        <template v-if="userData">
          <el-alert v-if="isAdminUser" title="此账号是数据库当前管理员，权限固定为全权限。如需改变身份，请先在账号管理中调整角色。" type="info" :closable="false" />
          <template v-else>
            <el-form label-position="top" class="user-form">
              <el-form-item label="基础权限来源"><el-radio-group v-model="userForm.mode"><el-radio value="LEGACY">保留现有业务资格</el-radio><el-radio value="CUSTOM">仅使用指定角色</el-radio></el-radio-group><p class="muted">这里维护信息填报角色的业务权限；评审资格和任务范围统一在“评审任务管理”设置，校内账号原业务资格可继续保留。</p></el-form-item>
              <el-form-item label="指定角色"><el-select v-model="userForm.roles" multiple filterable aria-label="个人指定角色"><el-option v-for="r in catalog.roles.filter(r => r.code !== 'ADMIN')" :key="r.code" :label="`${r.name}${r.enabled ? '' : '（已停用）'}`" :value="r.code" :disabled="!r.enabled && !userForm.roles.includes(r.code)" /></el-select></el-form-item>
            </el-form>
            <h3>个人功能设置</h3><p class="muted">角色允许项合并，个人“允许”或“禁止”覆盖角色。没有模块查阅权限时，该模块的其他功能一并关闭。</p>
            <el-collapse><el-collapse-item v-for="(label, module) in catalog.modules" :key="module" :name="module" :title="label"><div class="override-grid"><div v-for="item in functionsFor(module)" :key="item.code" class="override-item"><span>{{ item.label }}</span><el-select :aria-label="`${label}：${item.label}`" :model-value="overrideValue(item.code)" @update:model-value="setOverride(item.code, $event)"><el-option label="继承角色" value="inherit" /><el-option label="允许" value="allow" /><el-option label="禁止" value="deny" /></el-select></div></div></el-collapse-item></el-collapse>
            <h3>个人数据范围</h3><PermissionScopes v-model="userForm.scopes" :options="options" personal />
            <el-form label-position="top"><el-form-item label="变更原因"><el-input v-model="userForm.reason" aria-label="个人权限变更原因" type="textarea" maxlength="500" show-word-limit :rows="2" /></el-form-item></el-form>
            <el-button type="primary" :loading="busy" :disabled="userLoading" @click="previewUser">预览并保存个人权限</el-button>
          </template>
          <div class="effective"><h3>当前有效权限</h3><p class="muted">来源：{{ (userData.effective.roles || []).map(r => r.name).join('、') || '无' }}。可查阅三级任务 {{ userData.effective.taskCount || 0 }} 条。</p><el-tooltip v-for="code in userData.effective.permissions || []" :key="code" :content="permissionSource(code)"><el-tag effect="plain">{{ functionName(code) }}</el-tag></el-tooltip><p class="muted">{{ userData.effective.notice }}</p><el-collapse><el-collapse-item title="已有业务职责"><p v-for="d in userData.effective.duties?.tasks || []" :key="`t${d.id}`">任务：{{ d.name }}（{{ d.year }}）</p><p v-for="d in userData.effective.duties?.performance || []" :key="`p${d.id}`">绩效：{{ d.name }}</p></el-collapse-item></el-collapse></div>
        </template>
      </el-tab-pane>
    </el-tabs>

    <el-dialog top="6vh" v-model="roleDialog" title="角色模板" width="min(1060px, calc(100vw - 32px))" :close-on-click-modal="false" :before-close="closeRole" class="permission-dialog">
      <el-form label-position="top"><div class="role-fields"><el-form-item label="角色名称"><el-input v-model="roleForm.name" maxlength="100" /></el-form-item><el-form-item label="状态"><el-switch v-model="roleForm.enabled" active-text="启用" inactive-text="停用" /></el-form-item></div></el-form>
      <h3>功能权限</h3><el-collapse><el-collapse-item v-for="(label, module) in catalog.modules" :key="module" :name="module" :title="label"><el-checkbox-group v-model="roleForm.permissions"><el-checkbox v-for="item in functionsFor(module)" :key="item.code" :value="item.code">{{ item.label }}</el-checkbox></el-checkbox-group></el-collapse-item></el-collapse>
      <h3>数据范围</h3><PermissionScopes v-model="roleForm.scopes" :options="options" />
      <el-form label-position="top"><el-form-item label="变更原因"><el-input v-model="roleForm.reason" aria-label="角色变更原因" type="textarea" maxlength="500" show-word-limit :rows="2" /></el-form-item></el-form>
      <template #footer><el-button :disabled="busy" @click="closeRole(() => { roleDialog=false })">取消</el-button><el-button type="primary" :loading="busy" @click="previewRole">预览影响</el-button></template>
    </el-dialog>

    <el-dialog top="6vh" v-model="previewDialog" title="确认权限变更" width="min(1000px, calc(100vw - 32px))" :close-on-click-modal="false" :show-close="!busy" :close-on-press-escape="!busy" class="permission-dialog">
      <template v-if="preview">
        <el-alert :title="preview.blocked?.length ? `有 ${preview.blocked.length} 条待办会失去处理权限，请先移交。当前变更不会保存。` : '请核对变更和影响范围，确认后保存。'" :type="preview.blocked?.length ? 'error' : 'warning'" show-icon :closable="false" />
        <p>新增功能：{{ addedFunctions.map(functionName).join('、') || '无' }}</p><p>移除功能：{{ removedFunctions.map(functionName).join('、') || '无' }}</p>
        <template v-if="previewKind === 'user'"><p>可查阅三级任务：{{ preview.before.taskCount || 0 }} → {{ preview.after.taskCount || 0 }}</p><el-collapse><el-collapse-item title="变更后可见任务清单"><p v-for="id in preview.after.taskIds || []" :key="id">{{ taskLabel(id) }}</p></el-collapse-item></el-collapse></template>
        <el-table v-else :data="preview.impacts || []" max-height="260"><el-table-column prop="name" label="受影响账号" min-width="160" /><el-table-column label="可见三级任务" width="170"><template #default="{row}">{{ row.beforeTasks }} → {{ row.afterTasks }}</template></el-table-column><el-table-column label="受阻待办" width="110"><template #default="{row}">{{ row.blocked.length }}</template></el-table-column></el-table>
        <el-table v-if="preview.blocked?.length" :data="preview.blocked" max-height="240"><el-table-column label="业务类型" width="130"><template #default="{row}">{{ catalog.modules[row.domain] }}</template></el-table-column><el-table-column prop="id" label="审核单" /><el-table-column prop="objectId" label="业务对象" /><el-table-column label="处理入口" width="220"><template #default="{row}"><el-button link type="primary" @click="dutyTransfer.open(row)">移交审核职责</el-button><el-button link type="primary" @click="router.push('/home/audit')">审核中心</el-button></template></el-table-column></el-table>
        <el-alert v-if="saveError" :title="saveError" type="error" :closable="false" show-icon />
      </template>
      <template #footer><el-button :disabled="busy" @click="previewDialog=false">返回修改</el-button><el-button type="primary" :loading="busy" :disabled="!!preview?.blocked?.length" @click="confirmSave">确认保存</el-button></template>
    </el-dialog>
    <PermissionDutyTransfer ref="dutyTransfer" @transferred="dutyTransferred" />
  </div>
</template>

<script setup>
import RemoteCandidateSelect from '../RemoteCandidateSelect.vue'
import QueryFilterBar from '../QueryFilterBar.vue'

import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PermissionScopes from './PermissionScopes.vue'
import PermissionDutyTransfer from './PermissionDutyTransfer.vue'
import { get } from '@/utils/request'
import { permissionCatalog, permissionOptions, userPermission, previewUserPermission, saveUserPermission, previewPermissionRole, savePermissionRole, deletePermissionRole } from '@/api/permissions'
import { loadCurrentAccess } from '@/utils/accessState'
import { permissionOverrideValue } from '@/utils/permissionOverrides.mjs'
const roleQuery=ref(null),roleIds=ref(new Set())
const route = useRoute(), router = useRouter(), clone = value => JSON.parse(JSON.stringify(value))
const catalog = reactive({ modules: {}, permissions: {}, roles: [], revision: 0 }), options = ref({}), tab = ref(route.query.userId ? 'users' : 'roles')
const loading = ref(false), busy = ref(false), error = ref(''), saveError = ref(''), userLoading = ref(false)
const selectedUser = ref(null), userData = ref(null), userForm = reactive({ mode: 'LEGACY', roles: [], overrides: {}, scopes: {}, reason: '' })
const userBaseline = ref(''), roleBaseline = ref(''), roleDialog = ref(false), editingRole = ref(false)
const emptyRole = () => ({ code: '', name: '', permissions: [], scopes: {}, enabled: true, version: 0, reason: '' })
const roleForm = reactive(emptyRole()), previewDialog = ref(false), preview = ref(null), previewKind = ref(''), payload = ref(null)
const dutyTransfer=ref(null)
async function dutyTransferred(){previewDialog.value=false;busy.value=true;try{const [c,u]=await Promise.all([permissionCatalog(),selectedUser.value?userPermission(selectedUser.value):Promise.resolve(null)]);Object.assign(catalog,c);if(u)userData.value=u;window.dispatchEvent(new Event('audit-count-updated'));ElMessage.info('原权限填写内容已保留，请重新预览影响后保存')}catch(e){error.value=`职责已移交，权限信息刷新失败：${e.message}；请重新加载后预览。填写内容仍保留。`}finally{busy.value=false}}
const isAdminUser = computed(() => userData.value?.effective.roles?.some(r => r.code === 'ADMIN'))
const functionsFor = module => Object.entries(catalog.permissions).filter(([code]) => code.startsWith(module + '.')).map(([code, label]) => ({ code, label }))
const functionName = code => `${catalog.modules[code.split('.')[0]] || ''}：${catalog.permissions[code] || code}`
const addedFunctions = computed(() => { const a = preview.value?.after?.permissions || [], b = preview.value?.before?.permissions || []; return a.filter(k => !b.includes(k)) })
const removedFunctions = computed(() => { const a = preview.value?.after?.permissions || [], b = preview.value?.before?.permissions || []; return b.filter(k => !a.includes(k)) })
const permissionSource = code => userData.value?.policy.overrides?.[code] === true ? '来源：个人明确允许' : '来源：' + (userData.value?.effective.roles || []).filter(r => r.permissions.includes(code)).map(r => r.name).join('、')
const taskLabel = id => { const t = options.value.tasks?.find(t => Number(t.id) === Number(id)); return t ? `${t.code} ${t.name}（${t.year}）` : String(id) }
const overrideValue = code => permissionOverrideValue(userForm.overrides, code)
const setOverride = (code, value) => { if (value === 'inherit') delete userForm.overrides[code]; else userForm.overrides[code] = value === 'allow' }
let userSequence = 0
const aborter = new AbortController()
const confirmDiscard = async () => { try { await ElMessageBox.confirm('有未保存的权限设置，确认放弃？', '未保存的修改', { type: 'warning', confirmButtonText: '放弃修改', cancelButtonText: '继续编辑' }); return true } catch { return false } }
const dirty = () => userData.value && !isAdminUser.value && JSON.stringify(userForm) !== userBaseline.value || roleDialog.value && JSON.stringify(roleForm) !== roleBaseline.value
async function reload() { if (busy.value || dirty() && !await confirmDiscard()) return; loading.value = true; error.value = ''; try { const [c, o] = await Promise.all([permissionCatalog({ signal: aborter.signal }), permissionOptions({ signal: aborter.signal })]); Object.assign(catalog, c); await roleQuery.value?.refresh(); options.value = o; if (selectedUser.value) await selectUser(selectedUser.value, true) } catch (e) { if (e.name !== 'AbortError') error.value = e.message } finally { loading.value = false } }

async function selectUser(id, force = false) { if (!force && dirty() && !await confirmDiscard()) { selectedUser.value = userData.value?.userId || null; return } const seq = ++userSequence; userData.value = null; if (!id) return; userLoading.value = true; try { const data = await userPermission(id, { signal: aborter.signal }); if (seq !== userSequence) return; userData.value = data; Object.assign(userForm, clone({ ...data.policy, reason: '' })); userBaseline.value = JSON.stringify(userForm) } catch (e) { if (e.name !== 'AbortError') ElMessage.error(e.message) } finally { if (seq === userSequence) userLoading.value = false } }
function openRole(role, copy = false) { editingRole.value = !!role && !copy; Object.assign(roleForm, role ? clone({ code: copy ? `ROLE_${Date.now()}` : role.code, name: copy ? `${role.name}副本` : role.name, permissions: role.permissions, scopes: role.scopes, enabled: role.enabled, version: copy ? 0 : role.version, reason: '' }) : { ...emptyRole(), code: `ROLE_${Date.now()}` }); roleBaseline.value = JSON.stringify(roleForm); roleDialog.value = true }
async function closeRole(done) { if (busy.value) return; if (JSON.stringify(roleForm) !== roleBaseline.value && !await confirmDiscard()) return; done() }
async function previewTaskNames(ids) { const items=[]; for(let start=0;start<ids.length;start+=100){const data=await get('/admin/permissions/candidates',{domain:'tasks',ids:ids.slice(start,start+100),pageSize:1},{signal:aborter.signal});items.push(...data.selected.map(row=>row.item).filter(Boolean))} options.value.tasks=items; }
async function previewUser() { if (busy.value || !selectedUser.value || userLoading.value) return; if (!userForm.reason.trim()) return ElMessage.warning('请填写变更原因'); busy.value = true; try { payload.value = { mode: userForm.mode, roles: clone(userForm.roles), overrides: clone(userForm.overrides), scopes: clone(userForm.scopes), version: userData.value.policy.version, revision: userData.value.revision, reason: userForm.reason.trim() }; preview.value = await previewUserPermission(selectedUser.value, payload.value); await previewTaskNames(preview.value.after.taskIds || []); previewKind.value = 'user'; saveError.value = ''; previewDialog.value = true } catch (e) { ElMessage.error(e.message) } finally { busy.value = false } }
async function previewRole() { if (busy.value) return; if (!/^[A-Z][A-Z0-9_]{1,63}$/.test(roleForm.code) || !roleForm.name.trim() || !roleForm.reason.trim()) return ElMessage.warning('请填写角色名称和变更原因'); busy.value = true; try { payload.value = { ...clone(roleForm), revision: catalog.revision }; preview.value = await previewPermissionRole(payload.value); previewKind.value = 'role'; saveError.value = ''; previewDialog.value = true } catch (e) { ElMessage.error(e.message) } finally { busy.value = false } }
async function confirmSave() { if (busy.value || preview.value?.blocked?.length) return; busy.value = true; saveError.value = ''; try { if (previewKind.value === 'user') { const data = await saveUserPermission(selectedUser.value, payload.value); userData.value = data; Object.assign(userForm, clone({ ...data.policy, reason: '' })); userBaseline.value = JSON.stringify(userForm) } else { await savePermissionRole(payload.value); roleDialog.value = false } Object.assign(catalog, await permissionCatalog()); await roleQuery.value?.refresh(); await loadCurrentAccess(); previewDialog.value = false; ElMessage.success('权限已保存，后续请求立即生效') } catch (e) { saveError.value = `${e.message}；填写内容仍保留。配置冲突时请返回并重新加载。` } finally { busy.value = false } }
async function removeRole(role) { if (busy.value) return; try { const result = await ElMessageBox.prompt(`删除未使用的角色“${role.name}”？请填写原因。`, '删除角色', { inputValidator: value => !!value?.trim() || '请填写原因', type: 'warning' }); busy.value = true; await deletePermissionRole(role.code, { version: role.version, revision: catalog.revision, reason: result.value }); Object.assign(catalog, await permissionCatalog()); await roleQuery.value?.refresh(); ElMessage.success('角色已删除') } catch (e) { if (e instanceof Error) ElMessage.error(e.message) } finally { busy.value = false } }
const beforeUnload = event => { if (dirty() || busy.value || dutyTransfer.value?.isBusy) { event.preventDefault(); event.returnValue = '' } }
onMounted(async () => { window.addEventListener('beforeunload', beforeUnload); await reload(); if (route.query.userId) { selectedUser.value = Number(route.query.userId); await selectUser(selectedUser.value, true) } })
onBeforeRouteLeave(async () => !busy.value && !dutyTransfer.value?.isBusy && (!dirty() || await confirmDiscard()))
onUnmounted(() => { window.removeEventListener('beforeunload', beforeUnload); userSequence++; aborter.abort() })
</script>
<style scoped>
.permissions-page{height:100%;overflow:auto;box-sizing:border-box;padding:24px;background:#f3f6fa;min-width:0}.page-head,.toolbar{display:flex;justify-content:space-between;gap:16px;align-items:center;margin-bottom:18px}.page-head h2{margin:0 0 8px}.page-head p,.muted{color:#778496;font-size:13px;line-height:1.7;margin:0}.panel{padding:12px 18px 22px;background:white;border-radius:10px}.user-picker .el-select{width:360px;max-width:100%}.user-form{margin:20px 0}.user-form .el-select{width:100%}.override-grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(250px,1fr));gap:12px}.override-item{display:flex;align-items:center;justify-content:space-between;gap:12px}.override-item .el-select{width:115px}.effective{margin-top:25px;border-top:1px solid #e7edf5;padding-top:12px}.effective .el-tag{margin:5px 8px 4px 0}.role-fields{display:grid;grid-template-columns:minmax(0,1fr) 160px;gap:18px}h3{font-size:16px;margin:22px 0 12px}.el-form{margin-top:18px}:deep(.permission-dialog .el-dialog__body){max-height:min(64vh,calc(100dvh - 200px));overflow:auto}.el-checkbox{margin-bottom:8px}@media(max-width:900px){.permissions-page{padding:14px}.toolbar,.page-head{align-items:flex-start;flex-direction:column}.role-fields{grid-template-columns:1fr}.override-grid{grid-template-columns:1fr}}
</style>
