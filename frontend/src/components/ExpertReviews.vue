<template>
  <div class="expert-page">
    <div class="page-head"><div><h2>专家评审</h2><p>对一级任务形成总体意见，对三级任务填写具体评语。意见仅本人和管理员可见，不改变任务审核状态和完成值。</p></div><div class="actions"><el-button :disabled="busy" @click="refresh">刷新</el-button></div></div>
    <el-tabs v-if="accessState.isAdmin" v-model="mode" :before-leave="switchMode"><el-tab-pane label="发起评审任务" name="launch" /><el-tab-pane label="正式意见查询" name="admin" /><el-tab-pane label="评审任务" name="tasks" /></el-tabs>
    <section v-if="mode === 'launch' && accessState.isAdmin" class="panel launch">
      <el-alert v-if="launchError" :title="launchError" type="error" :closable="false" />
      <div class="section-title">
        <div class="filters"><span>任务清单</span><el-select v-model="launchYear" aria-label="评审年度" :disabled="busy" @change="loadAll"><el-option v-for="y in years" :key="y" :label="`${y}年`" :value="y" /></el-select><span class="muted">共 {{ launchTasks.length }} 条 · 已选 {{ assignTaskIds.length }} 条</span></div>
        <div class="actions">
          <el-button type="primary" :disabled="busy || !assignTaskIds.length" @click="openAssign()">分配评审专家</el-button>
          <el-button :loading="launchLoading" :disabled="busy" @click="loadAll">刷新</el-button>
        </div>
      </div>
      <el-table :data="visibleLaunchRows" v-loading="launchLoading" border empty-text="当前年度没有任务" @row-click="toggleLaunchRow">
        <el-table-column width="52" align="center">
          <template #default="{row}"><el-checkbox :model-value="leafChecked(row).checked" :indeterminate="leafChecked(row).indeterminate" @change="value => toggleCheck(row, value)" @click.stop /></template>
        </el-table-column>
        <el-table-column label="类型" width="110"><template #default="{row}"><el-tag :type="row.level === 1 ? 'primary' : row.level === 2 ? 'warning' : 'info'" effect="plain">{{ row.level === 1 ? '一级任务' : row.level === 2 ? '二级目录' : '三级任务' }}</el-tag></template></el-table-column>
        <el-table-column label="任务名称" min-width="320">
          <template #default="{row}">
            <span :style="{ paddingLeft: (row.depth * 20) + 'px' }" class="tree-label">
              <span v-if="row.hasChildren" class="tree-caret">{{ expandedKeys.includes(row.taskId) ? '▾' : '▸' }}</span>
              {{ row.level === 3 ? row.taskCode + ' ' : '' }}{{ row.taskName }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="deptName" label="归口部门" min-width="180" show-overflow-tooltip />
        <el-table-column label="已配专家" width="100" align="center"><template #default="{row}">{{ row.expertCount || 0 }}</template></el-table-column>
        <el-table-column label="操作" width="150" fixed="right"><template #default="{row}"><el-button v-if="row.level === 3" link type="primary" :disabled="busy" @click.stop="openAssign(row)">分配专家</el-button><span v-else class="muted">目录</span></template></el-table-column>
      </el-table>
      <div class="expert-block">
        <div class="section-title">
          <div><strong>评审专家</strong><span class="muted"> · 共 {{ launchExpertsPool.length }} 人</span></div>
          <el-dropdown trigger="click" :disabled="busy" @command="openAdd"><el-button type="primary" :disabled="busy">增加</el-button>
            <template #dropdown><el-dropdown-menu><el-dropdown-item command="member">添加校内账号</el-dropdown-item><el-dropdown-item command="account" divided>添加新的专家账号</el-dropdown-item></el-dropdown-menu></template>
          </el-dropdown>
        </div>
      </div>
      <el-table :data="launchExpertsPool" v-loading="launchLoading" border :empty-text="launchLoading ? '正在加载专家…' : launchError ? '加载失败，请重试' : '还没有可评审的专家，请点击“增加”'">
        <el-table-column prop="name" label="专家" width="160" />
        <el-table-column prop="userId" label="账号" width="150" />
        <el-table-column label="可评审任务数" width="130" align="center"><template #default="{row}">{{ row.taskCount ?? 0 }}</template></el-table-column>
        <el-table-column label="填报权限" width="120"><template #default="{row}"><el-tag :type="row.canWrite ? 'success' : 'info'" size="small">{{ row.canWrite ? '可填报' : '只读' }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="170" fixed="right"><template #default="{row}"><el-button link type="primary" :disabled="busy" @click="editScope(row)">编辑评审范围</el-button></template></el-table-column>
      </el-table>
      <div class="history-block">
        <el-alert v-if="launchHistoryError" :title="launchHistoryError" type="error" :closable="false" />
        <div class="section-title"><div><strong>历史评审数据</strong><span class="muted"> · 全部正式评语，按年度筛选</span></div><span class="muted">共 {{ launchHistoryTotal }} 条</span></div>
        <el-table :data="launchHistory" v-loading="launchHistoryLoading" border empty-text="暂无正式评语">
          <el-table-column prop="expertName" label="专家" width="130" />
          <el-table-column prop="year" label="年度" width="85" />
          <el-table-column label="评审任务" min-width="300"><template #default="{row}"><div class="task-name">{{ row.taskCode }} {{ row.taskName }}</div><small class="muted">{{ row.level === 1 ? '一级总体意见' : '三级任务意见' }}</small></template></el-table-column>
          <el-table-column label="评语" min-width="280"><template #default="{row}"><div class="opinion-summary">{{ row.text }}</div></template></el-table-column>
          <el-table-column label="提交时间" width="165"><template #default="{row}">{{ time(row.createTime) }}</template></el-table-column>
        </el-table>
      </div>
      <el-dialog v-model="assignVisible" title="分配评审专家" width="min(620px, calc(100vw - 32px))" append-to-body :close-on-click-modal="false" :show-close="!assignBusy" :close-on-press-escape="!assignBusy">
        <el-alert v-if="assignError" :title="assignError" type="error" :closable="false" /><el-input v-model="assignReason" placeholder="请说明分配评审任务的原因" maxlength="500" :disabled="assignBusy" /><p class="muted">将为下列 {{ assignRows.length }} 条任务追加所选专家，已有范围不会丢失。</p>
        <el-select v-model="assignExpertIds" multiple filterable placeholder="选择评审专家" style="width:100%">
          <el-option v-for="expert in launchExpertsPool" :key="expert.userId" :label="`${expert.name}（${expert.userId}）`" :value="expert.userId" />
        </el-select>
        <template #footer><el-button :disabled="assignBusy" @click="assignVisible = false">取消</el-button><el-button type="primary" :loading="assignBusy" :disabled="!assignExpertIds.length || !assignReason.trim() || !assignVersions.length" @click="confirmAssign">确定分配</el-button></template>
      </el-dialog>
    </section>
    <section v-if="mode === 'tasks'" class="panel">
      <div v-if="accessState.isAdmin" class="filters"><span>评审身份</span><el-select :model-value="selectedExpert" aria-label="代填专家" filterable :disabled="busy" @change="changeExpert"><el-option v-for="user in experts.filter(u => u.active && u.canWrite)" :key="user.userId" :label="`${user.name}（${user.userId}）`" :value="user.userId" /></el-select><el-tag v-if="Number(selectedExpert) !== Number(accessState.userId)" type="warning">管理员代填</el-tag></div>
      <QueryFilterBar ref="expertFilters" domain="expert-reviews" title="专家评审" endpoint="/expert-reviews" :defaults="{year:String(year)}" :context="{year,pageSize:100,responseMode:'tree', ...authorParams()}" @result="receiveExpertQuery" @state="expertQueryState=$event"><template #actions><el-button v-if="hasPermission('tasks.export')" :loading="exportBusy" :disabled="exportBusy" @click="exportTree">导出</el-button></template></QueryFilterBar>
      <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />
      <el-table :data="scopeRows" v-loading="loading || expertQueryState.loading || scopeTreeLoading" border @row-click="toggleScopeRow" :empty-text="expertQueryState.loading || !expertQueryState.initialized ? '正在加载评审任务…' : expertQueryState.error ? '评审任务加载失败，请重试' : '当前年度没有获准评审的任务'">
        <el-table-column label="任务" min-width="420">
          <template #default="{row}"><span :style="{ paddingLeft: (row.depth * 20) + 'px' }" class="tree-label"><span v-if="row.hasChildren" class="tree-caret">{{ scopeExpanded.includes(row.taskId) ? '▾' : '▸' }}</span>{{ row.taskCode }} {{ row.taskName }}</span></template>
        </el-table-column>
        <el-table-column label="类型" width="110"><template #default="{row}"><el-tag :type="row.level === 1 ? 'primary' : row.level === 2 ? 'warning' : 'info'" effect="plain">{{ row.level === 1 ? '一级任务' : row.level === 2 ? '二级目录' : '三级任务' }}</el-tag></template></el-table-column>
        <el-table-column label="本人评审状态" width="150"><template #default="{row}"><el-tag v-if="row.level === 3" :type="row.reviewStatus === 'submitted' ? 'success' : row.reviewStatus === 'draft' ? 'warning' : 'info'">{{ statusName(row.reviewStatus) }}</el-tag></template></el-table-column>
        <el-table-column label="最近正式提交" min-width="165"><template #default="{row}">{{ row.level === 3 ? time(row.submittedAt) : '' }}</template></el-table-column>
        <el-table-column label="操作" width="200"><template #default="{row}"><template v-if="[1,3].includes(row.level)"><el-button link type="primary" :disabled="busy || !canLoadTasks" @click.stop="open(row.taskId, true)">{{ row.level === 1 ? '总体评语' : '专家评语' }}</el-button><el-button link type="primary" :disabled="busy" @click.stop="open(row.taskId, false)">任务详情</el-button></template><span v-else class="muted">目录</span></template></el-table-column>
      </el-table>
    </section>
    <section v-if="mode === 'admin' && accessState.isAdmin" class="panel">
      <QueryFilterBar ref="formalFilters" domain="expert-formal" title="正式意见" endpoint="/expert-reviews/admin" :defaults="{year:String(year)}" :context="{filterMode:'advanced',taskId:adminFilters.taskId}" @result="receiveFormalQuery" @state="formalQueryState=$event"><template #actions><el-checkbox v-model="includeHistory">导出包含修订历史</el-checkbox><el-button type="success" :disabled="busy || !selection.length" :loading="exporting" @click="download">导出所选（{{selection.length}}）</el-button></template></QueryFilterBar>
      <el-alert v-if="adminFilters.taskId" title="已按业务详情链接限定任务。点击“重置”恢复全部任务。" type="info" :closable="false" />
      <el-alert v-if="adminError" :title="adminError" type="error" :closable="false" show-icon />
      <el-table ref="adminTable" :data="adminList.items || []" :row-key="r => `${r.reviewId}:${r.year}`" v-loading="adminLoading || formalQueryState.loading" @selection-change="selection=$event" :empty-text="formalQueryState.loading || !formalQueryState.initialized ? '正在加载正式评语…' : formalQueryState.error ? '正式评语加载失败，请重试' : '暂无正式评语；草稿仅专家本人可见'">
        <el-table-column type="selection" width="48" :reserve-selection="true" /><el-table-column prop="expertName" label="专家" width="125" /><el-table-column prop="year" label="年度" width="85" /><el-table-column label="评审任务" min-width="290"><template #default="{row}">{{ row.taskCode }} {{ row.taskName }}<div class="muted">{{ row.level === 1 ? '一级总体意见' : '三级任务意见' }} <el-tag v-if="row.scope.partial" size="small" type="warning">部分范围</el-tag></div></template></el-table-column><el-table-column label="意见内容" min-width="260"><template #default="{row}"><div class="opinion-summary">{{ row.text }}</div></template></el-table-column><el-table-column label="提交时间" width="165"><template #default="{row}">{{ time(row.createTime) }}</template></el-table-column><el-table-column label="操作" width="120"><template #default="{row}"><el-button link type="primary" @click="showAdminHistory(row)">意见及修订</el-button></template></el-table-column>
      </el-table><div v-if="formalQueryState.initialized" class="pager"><span class="muted">按年度展示最近正式版本，完整保留历次正式提交。</span><el-pagination v-model:current-page="adminPage" :page-size="10" :total="adminList.total || 0" layout="total, prev, pager, next" @current-change="loadAdmin" /></div>
    </section>

    <el-dialog v-model="pickVisible" title="添加校内账号" width="min(780px, calc(100vw - 32px))" :close-on-click-modal="false">
      <el-input v-model="pickKeyword" clearable placeholder="按姓名或账号搜索" class="picker-search" @keyup.enter="searchPick" />
      <el-table :data="pickRows" v-loading="pickLoading" border height="360" row-key="userId" highlight-current-row @current-change="pickSelection = $event ? [$event] : []">
        <el-table-column prop="name" label="姓名" width="150" />
        <el-table-column prop="userId" label="账号" width="140" />
        <el-table-column prop="deptName" label="部门" min-width="200" show-overflow-tooltip />
      </el-table>
      <el-alert v-if="pickError" :title="pickError" type="error" :closable="false" /><el-pagination v-model:current-page="pickPage" :total="pickTotal" :page-size="50" layout="total, prev, pager, next" @current-change="loadPick" /><p class="muted picker-tip">点击选择一位账号，配置并确认后才授予评审权限，原有填报、审核职责保留。</p>
      <template #footer><el-button :disabled="pickBusy" @click="pickVisible = false">取消</el-button><el-button type="primary" :loading="pickBusy" :disabled="!pickSelection.length" @click="confirmAdd">添加并配置评审范围</el-button></template>
    </el-dialog>

    <el-dialog v-model="accountVisible" title="新增专家账号" width="min(560px, calc(100vw - 32px))" :close-on-click-modal="false">
      <el-alert title="账号创建后立即弹出“选择可评审的任务”，为这位专家设定评审范围。" type="info" :closable="false" />
      <el-form label-position="top" class="account-form"><el-form-item label="数字账号编号"><el-input v-model="accountForm.userId" maxlength="15" /></el-form-item><el-form-item label="姓名"><el-input v-model="accountForm.nickName" maxlength="100" /></el-form-item><el-form-item label="账号名称（选填）"><el-input v-model="accountForm.userName" maxlength="100" /></el-form-item><el-form-item label="邮箱（选填）"><el-input v-model="accountForm.email" maxlength="50" /></el-form-item></el-form>
      <el-alert v-if="accountError" :title="accountError" type="error" :closable="false" />
      <template #footer><el-button :disabled="accountBusy" @click="accountVisible = false">取消</el-button><el-button type="primary" :loading="accountBusy" @click="createAccount">创建并配置权限</el-button></template>
    </el-dialog>

    <el-dialog v-model="scopeVisible" title="选择可评审的任务" width="min(920px, calc(100vw - 32px))" top="5vh" :close-on-click-modal="false" :before-close="closeScope">
      <p class="muted">为 <strong>{{ scopeExpert?.name }}</strong>（{{ scopeExpert?.userId }}）选择 {{ scopeYear }} 年度可评审的三级任务；一级总评随其下级任务自动获得。其他年度评审及原有业务职责保留。</p>
      <el-input v-model="scopeKeyword" clearable placeholder="搜索任务编号或名称" class="picker-search" />
      <el-form-item label="变更原因（必填）"><el-input v-model="scopeReason" maxlength="500" placeholder="请说明调整评审范围的原因" /></el-form-item><el-alert v-if="scopeError" :title="scopeError" type="error" :closable="false" /><el-checkbox-group v-model="scopeTasks" class="scope-body">
        <div v-for="group in scopeGroups" :key="group.rootTaskId" class="scope-group">
          <div class="scope-group-head"><span>{{ group.rootCode }} {{ group.rootName }}</span><el-button link type="primary" @click="toggleGroup(group, !group.checked)">{{ group.checked ? '取消全选' : `全选（${group.tasks.length}）` }}</el-button></div>
          <el-checkbox v-for="task in group.tasks" :key="task.taskId" :value="task.taskId" class="scope-item">{{ task.taskCode }} {{ task.taskName }}</el-checkbox>
        </div>
        <el-empty v-if="!scopeGroups.length" description="没有匹配的任务" />
      </el-checkbox-group>
      <template #footer><span class="muted">已选 {{ scopeTasks.length }} 条</span><el-button :disabled="scopeBusy" @click="closeScope()">取消</el-button><el-button type="primary" :loading="scopeBusy" :disabled="!scopeReason.trim()" @click="saveScope">预览并确认</el-button></template>
    </el-dialog>

    <el-dialog v-model="scopePreviewVisible" title="评审范围变更确认" width="min(640px, calc(100vw - 32px))" :close-on-click-modal="false" :close-on-press-escape="!scopeBusy" :show-close="!scopeBusy">
      <p>专家：{{ scopeExpert?.name }} · {{ scopeYear }}年度 · 选中 {{ scopeTasks.length }} 条三级任务</p><p>只调整专家评审范围；其他年度、原有任务填报和审核资格保留。保存后立即生效。</p>
      <el-alert v-if="scopePreview?.blocked?.length" title="存在受影响待办，请先在管理中心移交后再调整。" type="error" :closable="false" /><p v-if="scopePreview && !scopePreview.after?.permissions?.includes('expert.write')" class="muted">该账号的有效权限当前为只读；个人禁止项或停用的角色模板仍会生效。</p>
      <p>原因：{{ scopeReason }}</p><template #footer><el-button :disabled="scopeBusy" @click="scopePreviewVisible=false">返回修改</el-button><el-button type="primary" :loading="scopeBusy" :disabled="!!scopePreview?.blocked?.length" @click="confirmScope">确认保存</el-button></template>
    </el-dialog>
    <Works ref="taskDetails" detail-only :review-expert-id="accessState.isAdmin ? selectedExpert : null" @expert-saved="loadList" />
    <el-dialog v-model="historyVisible" title="正式评语及修订历史" width="min(1100px, calc(100vw - 32px))" top="5vh" class="expert-dialog">
      <div v-loading="historyLoading" class="history-body"><el-empty v-if="!historyLoading && !historyRows.length" description="当前可查阅范围内暂无正式提交历史" /><article v-for="r in historyRows" :key="r.revisionId" class="history-item"><h3>第 {{ r.revisionNo }} 版 · {{ r.year }}年度 · {{ r.expertName }}</h3><p>{{ r.taskCode }} {{ r.taskName }}</p><pre>{{ r.text }}</pre><p class="muted">{{ time(r.createTime) }} · {{ r.delegated ? `管理员 ${r.operatorName} 代填` : `${r.operatorName} 提交` }}</p><el-tag :type="r.scope.partial ? 'warning' : 'info'">{{ r.scope.partial ? '仅针对提交时获准的部分任务' : '提交时获准范围' }} · {{ r.scope.tasks.length }} 条</el-tag><el-collapse><el-collapse-item title="提交时任务范围"><p v-for="t in r.scope.tasks" :key="t.taskId">{{ t.taskCode }} {{ t.taskName }}</p></el-collapse-item></el-collapse></article></div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { onBeforeRouteLeave, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { accessState, hasPermission } from '@/utils/accessState'
import Works from '@/components/Works.vue'
import QueryFilterBar from './QueryFilterBar.vue'
import { getExpertObjects, getExpertAccounts, getAdminExpertOpinions, getAdminExpertHistory, exportExpertOpinions, getReviewTasks, getReviewMembers, exportExpertTree } from '@/api/expertReviews'
import { createExpertAccount, saveExpertTasks, previewExpertTasks, userPermission, batchExpertTasks } from '@/api/permissions'
const route = useRoute(), years = [2025, 2026, 2027, 2028, 2029], defaultYear = Math.min(2029, Math.max(2025, new Date().getFullYear()))
const mode = ref(accessState.isAdmin && route.query.view !== 'tasks' ? (route.query.view === 'admin' ? 'admin' : 'launch') : 'tasks'), year = ref(years.includes(Number(route.query.year)) ? Number(route.query.year) : defaultYear)
const expertQueryState=ref({loading:true,initialized:false,error:''}),formalQueryState=ref({loading:true,initialized:false,error:''})
const expertFilters=ref(null), appliedExpertFilters=ref({})
// 评审任务树：一级/二级只作目录，三级才是可评价任务
const scopeTree=ref([]),scopeTreeLoading=ref(false),scopeExpanded=ref([])
const scopeStatus=computed(()=>{const map={};for(const item of list.value?.items||[])map[String(item.taskId)]=item;return map})
const scopeRows=computed(()=>{
  const out=[]
  const walk=(nodes,depth)=>{for(const node of nodes||[]){
    const status=scopeStatus.value[String(node.taskId)]||node
    out.push({...node,depth,hasChildren:(node.children||[]).length>0,reviewStatus:status.reviewStatus,submittedAt:status.submittedAt})
    if(scopeExpanded.value.includes(node.taskId))walk(node.children,depth+1)
  }}
  walk(scopeTree.value,0)
  return out
})
function toggleScopeRow(row){
  if(!row?.hasChildren)return
  const key=row.taskId
  scopeExpanded.value=scopeExpanded.value.includes(key)?scopeExpanded.value.filter(item=>item!==key):[...scopeExpanded.value,key]
}
const receiveExpertQuery=result=>{listSeq++;listController?.abort();loading.value=false;error.value='';appliedExpertFilters.value=result.query;list.value=result;page.value=result.page;scopeTree.value=result.tree||[];if(!scopeExpanded.value.length){const expand=nodes=>{for(const n of nodes){if(n.children?.length){scopeExpanded.value.push(n.taskId);expand(n.children)}}};expand(scopeTree.value)}}
const filters = reactive({rootId:'',deptId:'',keyword:'',status:''}), page = ref(1), list = ref({}), loading = ref(false), error = ref('')
const adminFilters = reactive({year:year.value,expertId:'',keyword:'',taskId:accessState.isAdmin&&route.query.view==='admin'?Number(route.query.taskId)||'':''})
const formalFilters=ref(null),appliedFormalFilters=ref({})
const receiveFormalQuery=result=>{adminSeq++;adminController?.abort();adminLoading.value=false;adminError.value='';appliedFormalFilters.value=result.query;adminList.value=result;adminPage.value=result.page;selection.value=[];adminTable.value?.clearSelection()}
const adminPage=ref(1),adminList=ref({}),adminLoading=ref(false),adminError=ref(''),adminTable=ref(null),selection=ref([]),includeHistory=ref(true),exporting=ref(false)
const experts=ref([{userId:accessState.userId,name:'本人',active:true,canWrite:true}]),selectedExpert=ref(accessState.userId),taskDetails=ref(null),opening=ref(false)
const busy=computed(()=>exporting.value||opening.value||assignBusy.value||taskDetails.value?.busy),canLoadTasks=computed(()=>!accessState.isAdmin||selectedExpert.value!=null)
// 导出本专家可评审任务树：一级/二级/三级编号 + 评语，评审结果列暂空。
const exportBusy=ref(false)
async function exportTree(){
 if(exportBusy.value)return
 exportBusy.value=true
 try{const blob=await exportExpertTree({});const year2=((list.value?.items||[]).find(i=>Number(i.year))||{}).year||new Date().getFullYear();const url=URL.createObjectURL(blob),link=document.createElement('a');link.href=url;link.download=`${year2}年评审结果.xlsx`;link.click();setTimeout(()=>URL.revokeObjectURL(url),1000)}
 catch(e){ElMessage.error(e.message||'导出失败')}finally{exportBusy.value=false}
}
const historyVisible=ref(false),historyRows=ref([]),historyLoading=ref(false)
const time=value=>value?new Date(value).toLocaleString('zh-CN',{hour12:false}):'—',statusName=value=>({draft:'草稿／修订中',submitted:'已提交',unreviewed:'未评审'})[value]||'—'
let disposed=false,listSeq=0,adminSeq=0,historySeq=0,initializing=true,listController,adminController,historyController,lastYear=year.value
const accessDenied=code=>[401,403,428].includes(Number(code))
const clearFormal=()=>{adminList.value={};selection.value=[];historyVisible.value=false;historyRows.value=[];adminTable.value?.clearSelection()}
watch(()=>expertQueryState.value.responseCode,code=>{if(accessDenied(code)){list.value={};scopeTree.value=[];loading.value=false}})
watch(()=>formalQueryState.value.responseCode,code=>{if(accessDenied(code)){clearFormal();adminLoading.value=false}})
const authorParams=()=>accessState.isAdmin?{expertId:selectedExpert.value}:{}
async function loadList(){const seq=++listSeq;listController?.abort();if(!canLoadTasks.value)return;listController=new AbortController();loading.value=true;error.value='';try{const data=await getExpertObjects({responseMode:'tree',year:year.value,...Object.fromEntries(Object.entries(appliedExpertFilters.value).map(([k,v])=>['f.'+k,v])),...filters,page:page.value,pageSize:20,...authorParams()},{signal:listController.signal});if(disposed||seq!==listSeq)return;list.value=data;scopeTree.value=data.tree||[];page.value=data.page;expertFilters.value?.rememberPage(data.page)}catch(e){if(seq===listSeq&&e.name!=='AbortError'){error.value=e.message;if(accessDenied(e.responseCode))list.value={}}}finally{if(seq===listSeq)loading.value=false}}
const search=()=>{page.value=1;expertFilters.value?.refresh()}
async function changeExpert(value){if(busy.value)return;const previous=list.value;list.value={items:[],total:0};loading.value=true;if(!await taskDetails.value.closeDetails()){list.value=previous;loading.value=false;return}selectedExpert.value=value;scopeTree.value=[];await nextTick();search()}
async function switchMode(){if(busy.value)return false;return taskDetails.value?.closeDetails() ?? true}
async function open(id,review=true){if(busy.value||!canLoadTasks.value)return;opening.value=true;try{await taskDetails.value.openTaskDetail(id,{year:Number(appliedExpertFilters.value.year)||year.value,review})}finally{opening.value=false}}
async function loadAdmin(){if(!accessState.isAdmin)return;const seq=++adminSeq;adminController?.abort();adminController=new AbortController();adminLoading.value=true;adminError.value='';try{const data=await getAdminExpertOpinions({filterMode:'advanced',taskId:adminFilters.taskId,...Object.fromEntries(Object.entries(appliedFormalFilters.value).map(([key,value])=>['f.'+key,value])),page:adminPage.value,pageSize:10},{signal:adminController.signal});if(disposed||seq!==adminSeq)return;adminList.value=data;adminPage.value=data.page;formalFilters.value?.rememberPage(data.page)}catch(e){if(seq===adminSeq&&e.name!=='AbortError'){adminError.value=e.message;if(accessDenied(e.responseCode))clearFormal()}}finally{if(seq===adminSeq)adminLoading.value=false}}
async function showAdminHistory(row){const seq=++historySeq;historyController?.abort();historyController=new AbortController();historyVisible.value=true;historyLoading.value=true;historyRows.value=[];try{const rows=await getAdminExpertHistory(row.reviewId,{year:row.year},{signal:historyController.signal});if(!disposed&&seq===historySeq&&historyVisible.value)historyRows.value=rows}catch(e){if(e.name!=='AbortError')ElMessage.error(e.message)}finally{if(seq===historySeq)historyLoading.value=false}}
async function download(){if(busy.value||!selection.value.length)return;if(selection.value.length>500)return ElMessage.warning('请分批导出，每次最多500份正式意见');exporting.value=true;try{const blob=await exportExpertOpinions({revisionIds:selection.value.map(r=>r.revisionId),includeHistory:includeHistory.value});const url=URL.createObjectURL(blob),link=document.createElement('a');link.href=url;link.download='专家正式评语及修订.xlsx';link.click();setTimeout(()=>URL.revokeObjectURL(url),1000)}catch(e){ElMessage.error(e.message)}finally{exporting.value=false}}
// —— 发起评审任务：全局专家名单 + 历史评审数据 ——
const launchYear=ref(year.value),launchTasks=ref([]),launchExpertsPool=ref([]),launchLoading=ref(false)
const launchHistory=ref([]),launchHistoryTotal=ref(0),launchHistoryLoading=ref(false),launchHistoryPage=ref(1)
const pickVisible=ref(false),pickKeyword=ref(''),pickRows=ref([]),pickSelection=ref([]),pickLoading=ref(false),pickBusy=ref(false)
const accountVisible=ref(false),accountBusy=ref(false),accountError=ref(''),accountForm=reactive({userId:'',nickName:'',userName:'',email:''})
const scopeVisible=ref(false),scopeBusy=ref(false),scopeKeyword=ref(''),scopeTasks=ref([]),scopeExpert=ref(null)
const scopeYear=ref(year.value),scopeReason=ref(''),scopeError=ref(''),scopeConfig=ref(null),scopePreviewVisible=ref(false),scopePreview=ref(null)
const launchError=ref(''),launchHistoryError=ref(''),pickError=ref(''),pickPage=ref(1),pickTotal=ref(0)
let launchSeq=0,launchHistorySeq=0,pickSeq=0,scopeSeq=0,scopeBaseline='',scopePayload,launchController,launchHistoryController,pickController
// 任务清单里勾选/单行分配评审专家
const assignTaskIds=ref([]),assignRows=ref([]),assignVisible=ref(false),assignExpertIds=ref([]),assignBusy=ref(false)
// 一级任务只是目录，三级任务才是评审对象；按一级分组展示成树
const launchTree=computed(()=>{
 const nodes=new Map(launchTasks.value.map(t=>[t.taskId,{...t,children:[]}]))
 const roots=[];for(const node of nodes.values()){const parent=nodes.get(node.parentId);if(parent)parent.children.push(node);else roots.push(node)}return roots.filter(r=>r.level===1)
})
// 一级/二级目录可展开；勾选目录时级联其下全部三级任务
const expandedKeys=ref([])
const checkedLeafIds=ref([])
const visibleLaunchRows=computed(()=>{
  const rows=[]
  for(const root of launchTree.value){
    rows.push({...root,depth:0,hasChildren:(root.children||[]).length>0})
    if(!expandedKeys.value.includes(root.taskId))continue
    for(const mid of root.children||[]){
      rows.push({...mid,depth:1,hasChildren:(mid.children||[]).length>0})
      if(mid.level===3||!expandedKeys.value.includes(mid.taskId))continue
      for(const leaf of mid.children||[])rows.push({...leaf,depth:2,hasChildren:false})
    }
  }
  return rows
})
const leavesOf=row=>row.level===3?[row.taskId]:(row.children||[]).flatMap(leavesOf)
function leafChecked(row){
  const leaves=leavesOf(row)
  const checked=leaves.filter(id=>checkedLeafIds.value.includes(id)).length
  return {checked:leaves.length>0&&checked===leaves.length,indeterminate:checked>0&&checked<leaves.length}
}
function toggleCheck(row,value){
  const set=new Set(checkedLeafIds.value)
  for(const id of leavesOf(row)){if(value)set.add(id);else set.delete(id)}
  checkedLeafIds.value=[...set]
  assignTaskIds.value=[...set]
}
function toggleLaunchRow(row){
  if(!row||!row.hasChildren)return
  const key=row.taskId
  expandedKeys.value=expandedKeys.value.includes(key)?expandedKeys.value.filter(item=>item!==key):[...expandedKeys.value,key]
}
// 默认折叠；点击一级/二级行整行展开或收起

const assignReason=ref(''),assignError=ref(''),assignVersions=ref([])
async function openAssign(row){
 assignRows.value=row?[row]:launchTasks.value.filter(task=>assignTaskIds.value.includes(task.taskId));if(!assignRows.value.length)return;
 assignExpertIds.value=[];assignReason.value='';assignError.value='';assignVisible.value=true;assignVersions.value=[];
 try{assignVersions.value=await Promise.all(launchExpertsPool.value.map(e=>userPermission(e.userId)))}catch(e){assignError.value=e.message}
}
async function confirmAssign(){
 if(assignBusy.value||!assignExpertIds.value.length||!assignReason.value.trim()||!assignVersions.value.length)return;assignBusy.value=true;assignError.value='';
 try{const members=assignExpertIds.value.map(id=>{const c=assignVersions.value.find(c=>Number(c.userId)===Number(id));if(!c)throw new Error('专家配置已变化，请重新打开');return {expertId:id,version:c.policy.version}});
 await batchExpertTasks({year:launchYear.value,taskIds:assignRows.value.map(t=>t.taskId),members,revision:assignVersions.value[0].revision,reason:assignReason.value.trim()});assignVisible.value=false;checkedLeafIds.value=[];assignTaskIds.value=[];ElMessage.success('已分配评审专家');await loadLaunch();
 }catch(e){assignError.value=e.message||'分配失败'}finally{assignBusy.value=false}
}
const catalogTaskIds=()=>new Set(launchTasks.value.filter(t=>t.level===3).map(t=>t.taskId))
async function loadLaunch(){
 const seq=++launchSeq;launchController?.abort();launchController=new AbortController();launchLoading.value=true;launchError.value=''
 try{const data=await getReviewTasks(launchYear.value,{signal:launchController.signal});if(disposed||seq!==launchSeq)return;launchTasks.value=data.items||[];checkedLeafIds.value=[];assignTaskIds.value=[];launchExpertsPool.value=data.experts||[]}
 catch(e){if(seq===launchSeq&&e.name!=='AbortError')launchError.value=e.message}finally{if(seq===launchSeq)launchLoading.value=false}
}
async function loadHistory(){
 const seq=++launchHistorySeq;launchHistoryController?.abort();launchHistoryController=new AbortController();launchHistoryLoading.value=true;launchHistoryError.value=''
 try{const data=await getAdminExpertOpinions({year:launchYear.value,page:launchHistoryPage.value,pageSize:10},{signal:launchHistoryController.signal});if(disposed||seq!==launchHistorySeq)return;launchHistory.value=data.items||[];launchHistoryTotal.value=data.total||0;launchHistoryPage.value=data.page||1}
 catch(e){if(seq===launchHistorySeq&&e.name!=='AbortError')launchHistoryError.value=e.message}finally{if(seq===launchHistorySeq)launchHistoryLoading.value=false}
}
function loadAll(){launchHistoryPage.value=1;return Promise.all([loadLaunch(),loadHistory()])}
const scopeSnapshot=()=>JSON.stringify([...scopeTasks.value].sort((a,b)=>a-b))
async function editScope(row){
 const seq=++scopeSeq;scopeBusy.value=true
 try{const config=await userPermission(row.userId);if(disposed||seq!==scopeSeq)return;scopeConfig.value=config;scopeExpert.value=row;scopeYear.value=launchYear.value;scopeKeyword.value='';scopeReason.value='';scopeError.value='';const rule=config.policy.scopes?.expert,canReview=config.effective.permissions?.includes('expert.read');const selected=!canReview?[]:rule?rule.years?.length&&!rule.years.includes(scopeYear.value)?[]:rule.mode==='ALL'?[...catalogTaskIds()]:rule.mode==='CUSTOM'?rule.items||[]:[]:config.effective.taskIds||[];scopeTasks.value=selected.filter(id=>catalogTaskIds().has(id));scopeBaseline=scopeSnapshot();scopeVisible.value=true}
 catch(e){ElMessage.error(e.message)}finally{if(seq===scopeSeq)scopeBusy.value=false}
}
async function closeScope(done){if(scopeBusy.value)return;let allowed=scopeSnapshot()===scopeBaseline&&!scopeReason.value.trim();if(!allowed){try{await ElMessageBox.confirm('尚未保存的评审范围将丢失，是否放弃？','未保存的评审范围',{confirmButtonText:'放弃修改',cancelButtonText:'继续配置'});allowed=true}catch{allowed=false}}if(allowed){scopeVisible.value=false;scopePreviewVisible.value=false;if(typeof done==='function')done()}return allowed}
function openAdd(kind){
  if(kind==='account'){Object.assign(accountForm,{userId:'',nickName:'',userName:'',email:''});accountError.value='';accountVisible.value=true;return}
  pickKeyword.value='';pickSelection.value=[];pickPage.value=1;pickVisible.value=true;loadPick()
}
function searchPick(){pickPage.value=1;loadPick()}
async function loadPick(){
 const seq=++pickSeq;pickController?.abort();pickController=new AbortController();pickLoading.value=true;pickError.value='';pickSelection.value=[]
 try{const data=await getReviewMembers(pickKeyword.value,pickPage.value,{signal:pickController.signal});if(disposed||seq!==pickSeq)return;pickRows.value=data.items||[];pickTotal.value=data.total||0}
 catch(e){if(seq===pickSeq&&e.name!=='AbortError')pickError.value=e.message}finally{if(seq===pickSeq)pickLoading.value=false}
}
async function confirmAdd(){if(pickSelection.value.length!==1||pickBusy.value)return;pickVisible.value=false;await editScope(pickSelection.value[0])}
async function createAccount(){
  if(accountBusy.value)return
  if(!/^[1-9]\d{0,14}$/.test(accountForm.userId)||!accountForm.nickName.trim()){accountError.value='请输入有效的数字账号编号及姓名';return}
  if(accountForm.email&&!/^\S+@\S+\.\S+$/.test(accountForm.email)){accountError.value='邮箱格式不正确';return}
  accountBusy.value=true;accountError.value=''
  try{const result=await createExpertAccount({...accountForm,userId:Number(accountForm.userId),configureTasks:true})
    accountVisible.value=false;ElMessage.success('专家账号已创建，请配置可评审任务')
    await loadLaunch();await editScope(launchExpertsPool.value.find(e=>e.userId===result.userId)||{userId:result.userId,name:accountForm.nickName,taskIds:[]})}
  catch(e){if(e instanceof Error)accountError.value=e.message}finally{accountBusy.value=false}
}
const scopeGroups=computed(()=>{
  const term=scopeKeyword.value.trim().toLowerCase(),groups=new Map()
  for(const task of launchTasks.value){if(task.level!==3)continue
    if(term&&!`${task.taskCode} ${task.taskName}`.toLowerCase().includes(term))continue
    if(!groups.has(task.rootTaskId))groups.set(task.rootTaskId,{rootTaskId:task.rootTaskId,rootCode:task.rootCode,rootName:task.rootName,tasks:[]})
    groups.get(task.rootTaskId).tasks.push(task)}
  return [...groups.values()].map(group=>{const selected=group.tasks.filter(t=>scopeTasks.value.includes(t.taskId)).length
    return {...group,checked:selected>0&&selected===group.tasks.length}})
})
function toggleGroup(group,checked){
  const ids=group.tasks.map(t=>t.taskId),set=new Set(scopeTasks.value)
  if(checked)ids.forEach(id=>set.add(id));else ids.forEach(id=>set.delete(id))
  scopeTasks.value=[...set]
}
async function saveScope(){
 if(!scopeExpert.value||scopeBusy.value||!scopeReason.value.trim())return;scopeBusy.value=true;scopeError.value=''
 try{scopePayload={year:scopeYear.value,taskIds:[...scopeTasks.value],version:scopeConfig.value.policy.version,revision:scopeConfig.value.revision,reason:scopeReason.value.trim()};scopePreview.value=await previewExpertTasks(scopeExpert.value.userId,scopePayload);scopePreviewVisible.value=true}
 catch(e){scopeError.value=e.message}finally{scopeBusy.value=false}
}
async function confirmScope(){
 if(scopeBusy.value||!scopePayload)return;scopeBusy.value=true;scopeError.value=''
 try{await saveExpertTasks(scopeExpert.value.userId,scopePayload);scopePreviewVisible.value=false;scopeVisible.value=false;ElMessage.success('评审范围已保存');await loadLaunch()}
 catch(e){scopePreviewVisible.value=false;scopeError.value=e.message}finally{scopeBusy.value=false}
}
const refresh=()=>mode.value==='launch'?loadAll():mode.value==='admin'?formalFilters.value?.refresh():expertFilters.value?.refresh()
watch(year,async value=>{if(initializing||value===lastYear)return;if(!await taskDetails.value.closeDetails()){year.value=lastYear;return}lastYear=value;page.value=1;loadList()})
watch(mode,value=>{if(initializing)return;if(value==='launch')loadAll();else refresh()})
watch(historyVisible,value=>{if(!value){historySeq++;historyController?.abort()}})
onBeforeRouteLeave(async()=>!busy.value&&!scopeBusy.value&&!accountBusy.value&&( !scopeVisible.value||await closeScope())&&await taskDetails.value.closeDetails())
onMounted(async()=>{
  initializing=false
  if(mode.value==='launch')loadAll()
  // A large account directory must not block the task list or opinion query.
  if(accessState.isAdmin)getExpertAccounts().then(rows=>{if(!disposed)experts.value=rows}).catch(e=>{if(!disposed)ElMessage.warning(`专家账号列表加载失败：${e.message}；仍可使用本人身份评审`)})
  // Each filter bar owns its initial request and restores applied conditions.
  if(route.query.taskId&&mode.value==='tasks')await open(Number(route.query.taskId))
})
onUnmounted(()=>{disposed=true;listSeq++;adminSeq++;historySeq++;launchSeq++;launchHistorySeq++;pickSeq++;scopeSeq++;for(const c of [listController,adminController,historyController,launchController,launchHistoryController,pickController])c?.abort()})
</script>
<style scoped>
.expert-page{height:100%;box-sizing:border-box;overflow:auto;padding:24px;background:#f3f6fa;min-width:0}.page-head,.section-title,.pager{display:flex;justify-content:space-between;align-items:center;gap:16px}.page-head{margin-bottom:20px}.page-head h2{margin:0 0 8px}.page-head p,.muted{color:#7b8794;font-size:13px;line-height:1.7;margin:0}.actions,.filters{display:flex;flex-wrap:wrap;align-items:center;gap:10px}.panel{padding:18px;background:white;border:1px solid #e4eaf3;border-radius:10px}.filters{margin-bottom:16px}.filters>.el-select{width:170px}.filters>.el-input{width:240px}.task-name{white-space:normal;overflow-wrap:anywhere;line-height:1.6}.pager{margin-top:18px;flex-wrap:wrap}.opinion-summary{display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden;white-space:pre-wrap;overflow-wrap:anywhere}.expert-page :deep(.expert-dialog .el-dialog__body){max-height:calc(90dvh - 150px);overflow:auto}.formal,.history-item{border:1px solid #e2e9f3;border-radius:8px;padding:16px;margin:18px 0;background:#f8fafc}.formal h3,.history-item h3{margin:0 0 10px;font-size:16px}pre{font-family:inherit;white-space:pre-wrap;overflow-wrap:anywhere;line-height:1.8;margin:12px 0;color:#344054}.word-count{text-align:right;width:100%;font-size:12px;color:#8a94a4;margin-top:5px}.editor-body>.el-alert{margin:14px 0}.expert-page h3{overflow-wrap:anywhere}.launch .section-title{margin-bottom:12px}.launch .section-title .filters>.el-select{width:130px}.history-block{margin-top:24px;border-top:1px dashed #e2e9f3;padding-top:16px}.pick-search,.picker-search{max-width:320px;margin-bottom:12px}.picker-tip{margin:10px 0 0}.account-form{margin-top:12px}.scope-body{max-height:52vh;overflow:auto;padding-right:6px;display:block}.scope-group+.scope-group{margin-top:14px}.scope-group-head{display:flex;align-items:center;justify-content:space-between;gap:12px;padding:6px 10px;background:#f4f7fb;border-radius:6px;font-weight:600;color:#3b4a5f}.scope-item{display:flex;width:100%;margin:8px 0 0;padding-left:10px;line-height:1.6;white-space:normal;height:auto}.scope-item :deep(.el-checkbox__label){white-space:normal;overflow-wrap:anywhere}.section-title{align-items:flex-start;flex-wrap:wrap}@media(max-width:900px){.expert-page{padding:14px}.page-head{align-items:flex-start;flex-direction:column}.filters>.el-select,.filters>.el-input{max-width:100%}.section-title{gap:8px}.pager{align-items:flex-start}.panel{padding:12px}}
</style>
