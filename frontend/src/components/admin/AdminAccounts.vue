<template>
  <div class="admin-page">
    <div class="page-head"><div><h2>账号与组织</h2><p>维护账号、角色和部门负责人。停用前会核对当前业务职责。</p></div></div>
    <el-tabs v-model="tab" class="admin-tabs">
      <el-tab-pane label="用户管理" name="users">
        <QueryFilterBar ref="userQuery" domain="admin-users" title="账号" :context="{page,pageSize}" :defaults="{status:String(route.query.status||'')}" @state="state => { loading = state.loading; usersReady = state.initialized; error = state.error }" @result="receiveUsers"><template #actions>
          <!-- 新增专家账号入口暂时隐藏：改由「专家评审 → 发起评审任务」添加。对话框与逻辑保留，需要时把 v-if 去掉即可。 -->
          <el-button v-if="false" @click="openExpert">新增专家账号</el-button>
          <el-button type="primary" @click="openUser()">新增账号</el-button></template></QueryFilterBar>
        <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" class="notice" />
        <el-table :data="users" v-loading="loading" :empty-text="!usersReady ? (error ? '加载失败，请重试' : '正在加载账号…') : '无匹配账号'" stripe>
          <el-table-column prop="userId" label="工号" width="120" />
          <el-table-column prop="nickName" label="姓名" width="120" />
          <el-table-column prop="userName" label="账号名称" min-width="150" show-overflow-tooltip />
          <el-table-column prop="deptName" label="部门" min-width="160" show-overflow-tooltip />
          <el-table-column label="角色" width="110"><template #default="scope">{{ roleName(scope.row.role) }}</template></el-table-column>
          <el-table-column label="状态" width="90"><template #default="scope"><el-tag :type="scope.row.status === '1' ? 'success' : 'info'">{{ scope.row.status === '1' ? '正常' : '停用' }}</el-tag></template></el-table-column>
          <el-table-column label="派生权限" min-width="170"><template #default="scope"><el-tag v-if="scope.row.canViewAchievement" size="small" effect="plain">成果查阅</el-tag><el-tag v-if="scope.row.canUploadAchievement" size="small" effect="plain" type="warning">成果报送</el-tag><span v-if="!scope.row.canViewAchievement && !scope.row.canUploadAchievement" class="muted">—</span></template></el-table-column>
          <el-table-column label="领导查阅" width="110"><template #default="scope"><el-button link type="primary" @click="leadershipDialog.open(scope.row)">查阅范围</el-button></template></el-table-column>
          <el-table-column label="操作" width="320" fixed="right"><template #default="scope"><el-button link type="primary" @click="router.push({ path: '/home/admin/permissions', query: { userId: scope.row.userId } })">配置权限</el-button><el-button link type="primary" @click="openUser(scope.row)">编辑</el-button><el-button link type="warning" @click="openReset(scope.row)">重置密码</el-button><el-button link :type="scope.row.status === '1' ? 'danger' : 'success'" @click="openStatus(scope.row)">{{ scope.row.status === '1' ? '停用' : '启用' }}</el-button></template></el-table-column>
        </el-table>
        <div v-if="usersReady" class="pager"><el-pagination v-model:current-page="page" v-model:page-size="pageSize" layout="total, prev, pager, next" :total="total" @current-change="loadUsers" /></div>
      </el-tab-pane>

      <el-tab-pane label="部门管理" name="departments" lazy>
        <el-alert title="部门名称和编号由基础数据维护，本页仅调整负责人和启停状态。更换负责人不会自动改写历史任务或绩效。" type="info" show-icon :closable="false" class="notice" />
        <QueryFilterBar ref="departmentQuery" domain="admin-departments" title="部门" @result="result => departmentIds = new Set(result.ids.map(String))" />
        <el-table :data="departments.filter(item => departmentIds.has(String(item.deptId)))" v-loading="deptLoading" stripe>
          <el-table-column prop="deptId" label="部门编号" width="120" />
          <el-table-column prop="deptName" label="部门名称" min-width="200" />
          <el-table-column prop="leaderName" label="负责人" width="130"><template #default="scope">{{ scope.row.leaderName || '未设置' }}</template></el-table-column>
          <el-table-column label="状态" width="90"><template #default="scope"><el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag></template></el-table-column>
          <el-table-column label="当前关联" min-width="260"><template #default="scope"><span>正常账号 {{ scope.row.activeUsers || 0 }}，任务 {{ scope.row.activeTasks || 0 }}，绩效 {{ scope.row.activePerformances || 0 }}</span></template></el-table-column>
          <el-table-column label="负责人不一致" width="150"><template #default="scope"><el-tag v-if="Number(scope.row.taskLeaderMismatches || 0) + Number(scope.row.performanceLeaderMismatches || 0)" type="warning" effect="plain">任务 {{ scope.row.taskLeaderMismatches || 0 }} / 绩效 {{ scope.row.performanceLeaderMismatches || 0 }}</el-tag><span v-else class="muted">无</span></template></el-table-column>
          <el-table-column label="操作" width="100" fixed="right"><template #default="scope"><el-button link type="primary" @click="openDepartment(scope.row)">维护</el-button></template></el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="userDialog" :title="userForm.editing ? '编辑账号' : '新增账号'" width="600px" destroy-on-close :close-on-click-modal="false">
      <el-form label-position="top" @submit.prevent>
        <div class="form-grid">
          <el-form-item label="工号" :error="formErrors.userId"><el-input v-model="userForm.userId" :disabled="userForm.editing" maxlength="20" /></el-form-item>
          <el-form-item label="姓名" :error="formErrors.nickName"><el-input v-model="userForm.nickName" maxlength="100" /></el-form-item>
          <el-form-item label="账号名称" :error="formErrors.userName"><el-input v-model="userForm.userName" maxlength="100" /></el-form-item>
          <el-form-item label="邮箱" :error="formErrors.email"><el-input v-model="userForm.email" maxlength="50" /></el-form-item>
          <el-form-item label="部门" :error="formErrors.deptId"><el-select v-model="userForm.deptId" filterable :clearable="userForm.externalExpert" :placeholder="userForm.externalExpert ? '外部专家可不绑定部门' : '请选择部门'"><el-option v-for="item in activeDepartments" :key="item.deptId" :label="item.deptName" :value="item.deptId" /></el-select></el-form-item>
          <el-form-item label="角色" :error="formErrors.role"><el-select v-model="userForm.role"><el-option label="管理员" value="0"/><el-option label="普通用户" value="1"/><el-option label="负责人" value="2"/></el-select></el-form-item>
        </div>
        <el-form-item v-if="userForm.editing" label="修改原因" :error="formErrors.reason"><el-input v-model="userForm.reason" type="textarea" :rows="3" maxlength="500" show-word-limit /></el-form-item>
        <el-alert v-else title="初始密码为工号，首次登录后必须修改密码。" type="info" show-icon :closable="false" />
      </el-form>
      <template #footer><el-button @click="userDialog=false">取消</el-button><el-button type="primary" :loading="submitting" @click="saveUser">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="expertDialog" title="新增专家账号" width="min(560px, calc(100vw - 32px))" :close-on-click-modal="false" :show-close="!expertBusy" :close-on-press-escape="!expertBusy">
      <el-alert title="初始密码为账号编号，首次登录必须修改。默认仅开放专家评审和任务、材料、审核历史查阅；创建后可继续限定任务范围。" type="info" :closable="false" show-icon />
      <el-form label-position="top"><el-form-item label="数字账号编号"><el-input v-model="expertForm.userId" aria-label="专家账号编号" maxlength="15" /></el-form-item><el-form-item label="姓名"><el-input v-model="expertForm.nickName" aria-label="专家姓名" maxlength="100" /></el-form-item><el-form-item label="账号名称（选填）"><el-input v-model="expertForm.userName" maxlength="100" /></el-form-item><el-form-item label="邮箱（选填）"><el-input v-model="expertForm.email" maxlength="50" /></el-form-item></el-form>
      <el-alert v-if="expertError" :title="expertError" type="error" :closable="false" />
      <template #footer><el-button :disabled="expertBusy" @click="expertDialog=false">取消</el-button><el-button type="primary" :loading="expertBusy" @click="saveExpert">创建并配置权限</el-button></template>
    </el-dialog>

    <LeadershipAccessDialog ref="leadershipDialog" :departments="departments" />
    <el-dialog v-model="actionDialog" :title="action.title" width="520px" :close-on-click-modal="false">
      <el-alert v-if="action.impact" :title="impactText(action.impact)" :type="action.kind === 'disable' && action.impact.blockingCount ? 'error' : 'warning'" show-icon :closable="false" class="notice" />
      <p>{{ action.message }}</p>
      <el-form label-position="top"><el-form-item label="操作原因" :error="action.error"><el-input v-model="action.reason" type="textarea" :rows="3" maxlength="500" show-word-limit /></el-form-item></el-form>
      <template #footer><el-button @click="actionDialog=false">取消</el-button><el-button :type="action.kind === 'disable' ? 'danger' : 'primary'" :loading="submitting" :disabled="action.kind === 'disable' && Number(action.impact?.blockingCount || 0) > 0" @click="submitAction">确认</el-button></template>
    </el-dialog>

    <el-dialog v-model="deptDialog" title="维护部门" width="520px" :close-on-click-modal="false">
      <el-alert v-if="deptForm.impact" :title="deptImpactText(deptForm.impact)" type="warning" show-icon :closable="false" class="notice" />
      <el-form label-position="top"><el-form-item label="部门"><el-input :model-value="deptForm.deptName" disabled /></el-form-item><el-form-item label="负责人"><el-select v-model="deptForm.leaderId" clearable filterable remote :remote-method="loadDepartmentCandidates" :loading="candidateLoading" @visible-change="open => open && loadDepartmentCandidates()" placeholder="请选择本部门正常账号"><el-option v-for="item in departmentCandidates" :key="item.value" :label="item.label" :value="Number(item.value)" /><template v-if="candidatePage * 50 < candidateTotal" #footer><el-button text :loading="candidateLoading" @click="nextDepartmentCandidates">加载更多</el-button></template></el-select></el-form-item><el-form-item label="状态"><el-radio-group v-model="deptForm.status"><el-radio value="0">正常</el-radio><el-radio value="1">停用</el-radio></el-radio-group></el-form-item><el-form-item label="修改原因" :error="deptForm.error"><el-input v-model="deptForm.reason" type="textarea" :rows="3" maxlength="500" show-word-limit /></el-form-item></el-form>
      <template #footer><el-button @click="deptDialog=false">取消</el-button><el-button type="primary" :loading="submitting" :disabled="deptForm.status === '1' && Number(deptForm.impact?.blockingCount || 0) > 0" @click="saveDepartment">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createExpertAccount } from '@/api/permissions'
import QueryFilterBar from '../QueryFilterBar.vue'
const departmentQuery=ref(null),departmentIds=ref(new Set())
import LeadershipAccessDialog from './LeadershipAccessDialog.vue'
const leadershipDialog = ref(null)
import { createAdminUser, getAdminDepartmentImpact, getAdminDepartments, getAdminUserImpact, getDepartmentLeaderCandidates, resetAdminUserPassword, updateAdminDepartment, updateAdminUser, updateAdminUserStatus } from '@/api/admin'

const route = useRoute(), router = useRouter()
const tab = ref('users'), loading = ref(false), deptLoading = ref(false), submitting = ref(false), error = ref('')
const users = ref([]), departments = ref([]), page = ref(1), pageSize = ref(10), total = ref(0)
const usersReady=ref(false)
const userQuery=ref(null)
const receiveUsers=result=>{users.value=result.items;total.value=result.total;page.value=result.page;loading.value=false}
const activeDepartments = computed(() => departments.value.filter(item => item.status === '0'))
const roleName = role => ({ 0: '管理员', 1: '普通用户', 2: '负责人' })[String(role)] || '未知'
const loadDepartments = async () => { deptLoading.value = true; try { departments.value = await getAdminDepartments();  } catch (e) { ElMessage.error(e.message || '部门加载失败') } finally { deptLoading.value = false } }
const loadUsers=()=>userQuery.value?.refresh()

const expertDialog = ref(false), expertBusy = ref(false), expertError = ref(''), expertForm = reactive({ userId: '', nickName: '', userName: '', email: '' })
const openExpert = () => { Object.assign(expertForm, { userId: '', nickName: '', userName: '', email: '' }); expertError.value = ''; expertDialog.value = true }
const saveExpert = async () => {
  if (expertBusy.value) return
  if (!/^[1-9]\d{0,14}$/.test(expertForm.userId) || !expertForm.nickName.trim()) { expertError.value = '请输入有效的数字账号编号及姓名'; return }
  if (expertForm.email && !/^\S+@\S+\.\S+$/.test(expertForm.email)) { expertError.value = '邮箱格式不正确'; return }
  expertBusy.value = true; expertError.value = ''
  try { await ElMessageBox.confirm(`为“${expertForm.nickName}（${expertForm.userId}）”创建专家账号？初始密码为账号编号，首次登录强制修改。`, '确认新增专家', { type: 'warning', confirmButtonText: '确认创建', cancelButtonText: '取消' }); const result = await createExpertAccount({ ...expertForm, userId: Number(expertForm.userId) }); expertDialog.value = false; await loadUsers(); ElMessage.success('专家账号已创建'); await router.push({ path: '/home/admin/permissions', query: { userId: result.userId } }) } catch (e) { if (e instanceof Error) expertError.value = e.message } finally { expertBusy.value = false }
}

const userDialog = ref(false)
const emptyUser = () => ({ editing: false, externalExpert: false, userId: '', nickName: '', userName: '', email: '', deptId: '', role: '1', reason: '', expectedUpdateTime: null })
const userForm = reactive(emptyUser()), formErrors = reactive({})
const openUser = row => { if (!departments.value.length) loadDepartments(); Object.keys(formErrors).forEach(key => delete formErrors[key]); Object.assign(userForm, row ? { ...row, editing: true, userId: String(row.userId), deptId: row.deptId == null ? null : Number(row.deptId), externalExpert: row.deptId == null, expectedUpdateTime: row.updateTime, reason: '' } : emptyUser()); userDialog.value = true }
const validateUser = () => { Object.keys(formErrors).forEach(key => delete formErrors[key]); if (!/^\d+$/.test(String(userForm.userId))) formErrors.userId = '请输入数字工号'; if (!String(userForm.nickName).trim()) formErrors.nickName = '请输入姓名'; if (!String(userForm.userName).trim()) formErrors.userName = '请输入账号名称'; if (!userForm.deptId && !userForm.externalExpert) formErrors.deptId = '请选择部门'; if (!userForm.role) formErrors.role = '请选择角色'; if (userForm.email && !/^\S+@\S+\.\S+$/.test(userForm.email)) formErrors.email = '邮箱格式不正确'; if (userForm.editing && !String(userForm.reason).trim()) formErrors.reason = '请填写修改原因'; return !Object.keys(formErrors).length }
const saveUser = async () => { if (submitting.value || !validateUser()) return; submitting.value = true; try { const body = { userId: Number(userForm.userId), deptId: userForm.deptId || null, userName: userForm.userName, nickName: userForm.nickName, email: userForm.email, role: userForm.role, expectedUpdateTime: userForm.expectedUpdateTime, reason: userForm.reason }; if (userForm.editing) await updateAdminUser(body.userId, body); else await createAdminUser(body); ElMessage.success(userForm.editing ? '账号已更新' : '账号已创建'); userDialog.value = false; await loadUsers() } catch (e) { ElMessage.error(e.message || '保存失败') } finally { submitting.value = false } }

const actionDialog = ref(false), action = reactive({ kind: '', title: '', message: '', row: null, impact: null, reason: '', error: '' })
const openStatus = async row => { Object.assign(action, { kind: row.status === '1' ? 'disable' : 'enable', title: row.status === '1' ? '停用账号' : '启用账号', message: `确认${row.status === '1' ? '停用' : '启用'}“${row.nickName}（${row.userId}）”？`, row, impact: null, reason: '', error: '' }); actionDialog.value = true; try { action.impact = await getAdminUserImpact(row.userId) } catch (e) { action.error = e.message } }
const openReset = row => { Object.assign(action, { kind: 'reset', title: '重置密码', message: `将“${row.nickName}（${row.userId}）”的密码重置为工号，并要求下次登录修改。`, row, impact: null, reason: '', error: '' }); actionDialog.value = true }
const impactText = value => `部门负责人 ${value.departmentLeaderCount || 0}，任务职责 ${value.taskResponsibilityCount || 0}，绩效职责 ${value.performanceResponsibilityCount || 0}，活动待办 ${Number(value.taskPendingCount || 0) + Number(value.performancePendingCount || 0) + Number(value.achievementPendingCount || 0)}，纪实草稿 ${value.draftWorkRecordCount || 0}`
const submitAction = async () => { if (submitting.value) return; action.error = ''; if (!action.reason.trim()) { action.error = '请填写操作原因'; return } submitting.value = true; try { const body = { reason: action.reason, expectedUpdateTime: action.row.updateTime }; if (action.kind === 'reset') await resetAdminUserPassword(action.row.userId, body); else await updateAdminUserStatus(action.row.userId, { ...body, status: action.kind === 'disable' ? '0' : '1' }); ElMessage.success(action.kind === 'reset' ? '密码已重置为工号' : '账号状态已更新'); actionDialog.value = false; await loadUsers() } catch (e) { action.error = e.message || '操作失败' } finally { submitting.value = false } }

const deptDialog = ref(false), deptForm = reactive({ deptId: null, deptName: '', leaderId: null, status: '0', reason: '', expectedUpdateTime: null, impact: null, error: '' })
const departmentCandidates = ref([]), candidateLoading = ref(false), candidateTotal = ref(0), candidatePage = ref(1)
let candidateSequence = 0, candidateController, candidateKeyword = ''
const loadDepartmentCandidates = async (keyword = '', append = false) => {
  candidateController?.abort(); candidateController = new AbortController(); const seq = ++candidateSequence
  candidateLoading.value = true; if (!append) candidateKeyword = keyword
  const requestedPage = append ? candidatePage.value + 1 : 1
  try {
    const result = await getDepartmentLeaderCandidates(deptForm.deptId, { keyword: candidateKeyword, page: requestedPage, pageSize: 50, ids: deptForm.leaderId == null ? [] : [deptForm.leaderId] }, { signal: candidateController.signal })
    if (seq !== candidateSequence || !deptDialog.value) return
    const merged = [...(append ? departmentCandidates.value : []), ...(result.selected || []), ...result.items]
    departmentCandidates.value = [...new Map(merged.map(item => [item.value, item])).values()]; candidateTotal.value = result.total; candidatePage.value = result.page
  } catch (e) { if (seq === candidateSequence && e.name !== 'AbortError') deptForm.error = e.message || '负责人选项加载失败，请重新搜索' }
  finally { if (seq === candidateSequence) candidateLoading.value = false }
}
const nextDepartmentCandidates = () => loadDepartmentCandidates(candidateKeyword, true)
onBeforeUnmount(() => { candidateController?.abort(); candidateSequence += 1 })
watch(deptDialog, open => { if (!open) { candidateController?.abort(); candidateSequence += 1; candidateLoading.value = false } })
const openDepartment = async row => { Object.assign(deptForm, { ...row, expectedUpdateTime: row.updateTime, leaderId: row.leaderId == null ? null : Number(row.leaderId), reason: '', impact: null, error: '' }); deptDialog.value = true; departmentCandidates.value = row.leaderId == null ? [] : [{ value: String(row.leaderId), label: row.leaderName || String(row.leaderId) }]; loadDepartmentCandidates(); try { deptForm.impact = await getAdminDepartmentImpact(row.deptId) } catch (e) { deptForm.error = e.message } }
const deptImpactText = value => `正常账号 ${value.activeUsers || 0}，有效任务 ${value.activeTasks || 0}，有效绩效 ${value.activePerformances || 0}，活动待办 ${value.taskPendingCount || 0}`
const saveDepartment = async () => { if (submitting.value) return; deptForm.error = ''; if (!deptForm.reason.trim()) { deptForm.error = '请填写修改原因'; return } submitting.value = true; try { await updateAdminDepartment(deptForm.deptId, { leaderId: deptForm.leaderId, status: deptForm.status, reason: deptForm.reason, expectedUpdateTime: deptForm.expectedUpdateTime }); ElMessage.success('部门已更新'); deptDialog.value = false; await loadDepartments(); departmentQuery.value?.refresh() } catch (e) { deptForm.error = e.message || '保存失败' } finally { submitting.value = false } }
watch(tab, value => { if (value === 'departments') loadDepartments() })
</script>

<style scoped>
.admin-page{padding:24px;min-height:100%;background:#f3f6fa}.page-head{margin-bottom:18px}.page-head h2{margin:0 0 7px;font-size:24px}.page-head p,.muted{margin:0;color:#7b8794;font-size:13px}.admin-tabs{background:#fff;border:1px solid #e5eaf2;border-radius:10px;padding:4px 18px 18px}.account-actions{display:flex;gap:8px}.toolbar{display:flex;justify-content:space-between;gap:16px;margin:14px 0}.filters{display:flex;flex-wrap:wrap;gap:10px}.filters .el-input{width:220px}.filters .el-select{width:150px}.notice{margin:12px 0}.pager{display:flex;justify-content:flex-end;margin-top:18px}.form-grid{display:grid;grid-template-columns:1fr 1fr;gap:0 16px}.form-grid .el-select{width:100%}.el-tag+.el-tag{margin-left:5px}@media(max-width:900px){.toolbar{align-items:flex-start;flex-direction:column}.form-grid{grid-template-columns:1fr}.admin-page{padding:14px}}
</style>
