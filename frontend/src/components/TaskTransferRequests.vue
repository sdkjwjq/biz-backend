<template>
<el-badge :is-dot="transferPending.length>0" style="margin-right:8px"><el-button :loading="transferPendingLoading" @click="openPendingTransfers">{{pendingError?'移交待办加载失败，点击重试':`待我确认移交${transferPending.length?`（${transferPending.length}）`:''}`}}</el-button></el-badge>
<el-button @click="openSentTransfers">我发起的移交</el-button>
<el-dialog v-model="sentVisible" title="我发起的移交" width="min(980px, calc(100vw - 32px))" append-to-body>
  <el-alert v-if="sentError" :title="sentError" type="error" :closable="false"><el-button @click="loadSentTransfers">重试</el-button></el-alert>
  <el-table :data="sentTransfers" v-loading="sentLoading" border size="small" max-height="520" empty-text="暂无移交申请">
    <el-table-column prop="requestId" label="申请编号" width="95" />
    <el-table-column label="任务" min-width="240"><template #default="{row}">[{{ row.taskCode }}] {{ row.taskName }}</template></el-table-column>
    <el-table-column prop="fieldLabel" label="移交职责" width="120" />
    <el-table-column prop="toName" label="接收人" width="110" />
    <el-table-column label="申请时间" width="170"><template #default="{row}">{{ formatDateTime(row.createTime) }}</template></el-table-column>
    <el-table-column label="状态" width="105"><template #default="{row}">{{ {PENDING:'待确认',ACCEPTED:'已接受',REJECTED:'已拒绝',EXPIRED:'已失效'}[row.status] || row.status }}</template></el-table-column>
    <el-table-column label="原因" min-width="200"><template #default="{row}">{{ row.handleRemark || row.reason || '—' }}</template></el-table-column>
    <el-table-column label="操作" width="100"><template #default="{row}"><el-button link type="primary" @click="openTransferLogs(row)">移交日志</el-button></template></el-table-column>
  </el-table>
</el-dialog>
    <!-- 任务移交申请（仅本人负责的字段） -->
    <el-dialog v-model="transferApplyVisible" title="任务移交申请" width="min(560px, calc(100vw - 32px))" append-to-body :close-on-click-modal="false" :before-close="closeApply">
      <p class="muted">{{ transferApplyTaskName }}</p>
      <div v-if="transferApplyTasks.length>1" class="transfer-apply-tasks">
        <div v-for="task in transferApplyTasks" :key="task?._raw?.taskId||task?.taskId">[{{ task?._raw?.taskCode||'—' }}] {{ task?._raw?.taskName||task?.name }}</div>
      </div>
      <el-alert title="仅移交所选职责，接收人确认后生效。审核中的任务需先完成审核。" type="info" :closable="false" />
      <el-form label-width="90px">
        <el-form-item label="移交类型">
          <el-radio-group v-model="transferApplyForm.fieldType" @change="onTransferTypeChange">
            <el-radio v-for="f in transferApplyFields" :key="f.key" :value="f.key">{{ f.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="接收人">
          <div class="receiver-row">
            <el-select v-model="transferApplyForm.toUserId" filterable remote :remote-method="searchTransferUsers" :loading="transferUserLoading" placeholder="请选择接收人，或点右侧「查找」" style="flex:1" @visible-change="onTransferSelectVisible">
              <el-option v-for="u in transferUserOptions" :key="u.userId" :label="`${u.name}（${u.userId}）`" :value="u.userId" />
            </el-select>
            <el-button :disabled="!transferApplyForm.fieldType" @click="openUserPicker">查找</el-button>
          </div>
        </el-form-item>
        <el-form-item label="移交原因">
          <el-input v-model="transferApplyForm.reason" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <el-alert v-if="transferApplyError" :title="transferApplyError" type="error" :closable="false" />
      <template #footer><el-button :disabled="transferApplyBusy" @click="closeApply(()=>transferApplyVisible=false)">取消</el-button><el-button type="primary" :loading="transferApplyBusy" :disabled="!transferApplyForm.fieldType || !transferApplyForm.toUserId || !transferApplyForm.reason.trim()" @click="submitTransferApply">提交申请</el-button></template>
    </el-dialog>

    <!-- 查找接收人：显示全部在用账号，不受“已承担同类职责”限制 -->
    <el-dialog v-model="userPickerVisible" title="查找接收人" width="min(820px, calc(100vw - 32px))" append-to-body :close-on-click-modal="false">
      <el-alert type="info" :closable="false" title="这里列出全部在用账号；刚新建、暂未承担该职责的账号也能选中并移交。没有对应填报/审核权限的账号提交时会提示分配角色。" />
      <div class="picker-search">
        <el-input v-model="userPickerKeyword" placeholder="输入姓名 / 账号 / 工号搜索" clearable @keyup.enter="searchPicker(1)" @clear="searchPicker(1)">
          <template #append><el-button @click="searchPicker(1)">搜索</el-button></template>
        </el-input>
      </div>
      <el-table ref="pickerTableRef" v-loading="userPickerLoading" :data="userPickerRows" size="small" border height="340" highlight-current-row row-key="userId" @current-change="onPickerCurrentChange">
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="userId" label="账号" width="110" />
        <el-table-column prop="deptName" label="部门" min-width="160" show-overflow-tooltip />
        <el-table-column prop="roles" label="角色" min-width="130" show-overflow-tooltip>
          <template #default="{ row }">{{ roleLabel(row) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="130">
          <template #default="{ row }">
            <el-tag v-if="row.self" type="info" size="small">本人</el-tag>
            <el-tag v-else-if="!row.available" type="warning" size="small">暂无对应权限</el-tag>
            <el-tag v-else type="success" size="small">可选</el-tag>
          </template>
        </el-table-column>
        <template #empty><span class="muted">没有匹配的账号</span></template>
      </el-table>
      <div class="picker-pager">
        <span class="muted">共 {{ userPickerTotal }} 个账号，第 {{ userPickerPage }} 页</span>
        <div>
          <el-button size="small" :disabled="userPickerPage<=1||userPickerLoading" @click="searchPicker(userPickerPage-1)">上一页</el-button>
          <el-button size="small" :disabled="userPickerPage*userPickerSize>=userPickerTotal||userPickerLoading" @click="searchPicker(userPickerPage+1)">下一页</el-button>
        </div>
      </div>
      <el-alert v-if="userPickerError" :title="userPickerError" type="error" :closable="false" />
      <template #footer>
        <el-button @click="userPickerVisible=false">取消</el-button>
        <el-button type="primary" :disabled="!userPickerSelected||userPickerSelected.self" @click="confirmUserPicker">选择该账号</el-button>
      </template>
    </el-dialog>

    <!-- 移交日志 -->
    <el-dialog v-model="transferLogVisible" title="移交日志" width="min(760px, calc(100vw - 32px))" append-to-body>
      <el-alert v-if="lastLogTask?.requestId" :title="lastLogTask.status === 'ACCEPTED' ? `申请 #${lastLogTask.requestId} 已接收；下方记录含该申请对应的职责变化` : `申请 #${lastLogTask.requestId} 尚未生效；下方仅为该任务其他已生效的移交记录`" type="info" :closable="false" style="margin-bottom:10px" />
      <el-alert v-if="logError" :title="logError" type="error" :closable="false"><el-button @click="reloadLogs">重试</el-button></el-alert>
      <el-table :data="transferLogs" v-loading="transferLogsLoading" size="small" border empty-text="暂无移交记录">
        <el-table-column v-if="lastLogTask?.requestId" label="对应申请" width="100"><template #default="{row}">{{ String(row.reason || '').includes(`#${lastLogTask.requestId}：`) ? `#${lastLogTask.requestId}` : '—' }}</template></el-table-column>
        <el-table-column label="时间" width="160"><template #default="{row}">{{ formatDateTime(row.createTime || row.create_time) }}</template></el-table-column>
        <el-table-column prop="reason" label="说明" min-width="260" show-overflow-tooltip />
        <el-table-column label="操作人" width="120"><template #default="{row}">{{ row.operatorName || row.operator_id || '—' }}</template></el-table-column>
      </el-table>
    </el-dialog>

    <!-- 待我确认的移交 -->
    <el-dialog v-model="transferPendingVisible" title="待我确认的移交" width="min(1180px, calc(100vw - 32px))" append-to-body :close-on-click-modal="false">
      <el-alert v-if="pendingError" :title="pendingError" type="error" :closable="false"><el-button @click="loadPendingTransfers">重试</el-button></el-alert>
<el-table ref="pendingTableRef" row-key="requestId" :data="transferPending" v-loading="transferPendingLoading" size="small" border max-height="520" empty-text="没有待确认的移交" @selection-change="onPendingSelectionChange">
        <el-table-column type="selection" width="42" reserve-selection />
        <el-table-column label="任务" min-width="300"><template #default="{row}"><div>{{ row.taskCode }} {{ row.taskName }}</div></template></el-table-column>
        <el-table-column prop="fieldLabel" label="移交类型" width="120" />
        <el-table-column prop="fromName" label="申请人" width="110" show-overflow-tooltip />
        <el-table-column label="原因" min-width="220"><template #default="{row}"><span>{{ row.reason }}</span></template></el-table-column>
        <el-table-column label="申请时间" width="150"><template #default="{row}">{{ formatDateTime(row.createTime) }}</template></el-table-column>
        <el-table-column label="操作" width="270" fixed="right"><template #default="{row}"><el-button link type="primary" :disabled="handling" @click="openTaskDetail(row)">查看任务详情</el-button><el-button link type="primary" :disabled="handling" @click="acceptTransfer(row)">确认接收</el-button><el-button link type="danger" :disabled="handling" @click="rejectTransfer(row)">拒绝</el-button></template></el-table-column>
      </el-table>
      <template #footer>
        <div class="pending-footer">
          <span class="muted">已选 {{ pendingSelection.length }} 条</span>
          <div>
            <el-button :disabled="!pendingSelection.length || handling" @click="batchReject">批量拒绝</el-button>
            <el-button type="primary" :disabled="!pendingSelection.length || handling" @click="batchAccept">批量同意</el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- 批量拒绝：独立大对话框，完整覆盖“待我确认的移交”，避免回车/误触把下层勾选状态刷掉 -->
    <el-dialog v-model="rejectVisible" title="批量拒绝移交" width="min(1180px, calc(100vw - 32px))" top="6vh" append-to-body :close-on-click-modal="false" modal-class="reject-overlay" class="batch-reject-dialog">
      <div class="reject-head">将拒绝以下 <b>{{ rejectRows.length }}</b> 条移交申请，拒绝后对应职责仍保留在申请人名下：</div>
      <div class="reject-tasks">
        <div v-for="row in rejectRows" :key="row.requestId" class="reject-task">{{ row.taskCode }} {{ row.taskName }}<span class="muted">（{{ row.fieldLabel }} · 申请人 {{ row.fromName }}）</span></div>
      </div>
      <el-input v-model="rejectRemark" type="textarea" :rows="6" maxlength="500" show-word-limit placeholder="请填写拒绝理由（必填，500 字以内）；在本框内按回车是换行，Ctrl+回车 可直接提交" @keydown.ctrl.enter.prevent="submitBatchReject" />
      <el-alert v-if="rejectError" :title="rejectError" type="error" :closable="false" class="reject-error" />
      <template #footer>
        <el-button @click="rejectVisible=false">取消</el-button>
        <el-button type="danger" :loading="handling" @click="submitBatchReject">确认拒绝（{{ rejectRows.length }} 条）</el-button>
      </template>
    </el-dialog>

    <!-- 任务详情：内容与任务列表「查看」抽屉中的“基本信息与完成情况”一致 -->
    <el-dialog v-model="detailVisible" title="任务详情" width="min(760px, calc(100vw - 32px))" append-to-body>
      <div v-loading="detailLoading" class="transfer-detail">
        <el-alert v-if="detailError" :title="detailError" type="error" :closable="false" />
        <TaskBasicInfo v-if="detailTask" :task="detailTask" />
      </div>
    </el-dialog>

</template>
<script setup>
import {ref,reactive,computed,onMounted,onBeforeUnmount,nextTick,watch} from 'vue'
import {ElMessage,ElMessageBox} from 'element-plus'
import {accessState} from '@/utils/accessState'
import {getTransferUsers,getAllTransferUsers,getPendingTransfers,getSentTransfers,createTransferRequest,createTransferBatch,acceptTransferRequest,rejectTransferRequest,batchAcceptTransfers,batchRejectTransfers,getTaskTransferLogs} from '@/api/taskTransfer'
import {get} from '@/utils/request'
import TaskBasicInfo from './TaskBasicInfo.vue'
const props=defineProps({beforeAccept:Function}),emit=defineEmits(['changed','applied'])
const transferLogs=ref([]),transferLogsLoading=ref(false),pendingError=ref(''),logError=ref(''),handling=ref(false)
const sentVisible=ref(false),sentTransfers=ref([]),sentLoading=ref(false),sentError=ref('')
const loadSentTransfers=async()=>{sentLoading.value=true;sentError.value='';try{sentTransfers.value=await getSentTransfers()}catch(e){sentError.value=e.message||'移交申请加载失败'}finally{sentLoading.value=false}}
const openSentTransfers=()=>{sentVisible.value=true;void loadSentTransfers()}
let disposed=false,pendingController=null,userController=null,logController=null,poll=null
const formatDateTime=v=>String(v||'—').replace('T',' ').slice(0,19)
// —— 任务移交申请：只能移交本人负责的字段，接收人确认后生效；支持一次勾选多个任务批量申请 ——
const transferApplyVisible=ref(false),transferApplyTasks=ref([]),transferApplyFields=ref([]),transferApplyError=ref(''),transferApplyBusy=ref(false)
const transferUserOptions=ref([]),transferUserLoading=ref(false),lastLogTask=ref(null)
const userPickerVisible=ref(false),userPickerKeyword=ref(''),userPickerRows=ref([]),userPickerLoading=ref(false)
const userPickerSelected=ref(null),userPickerTotal=ref(0),userPickerPage=ref(1),userPickerError=ref('')
const userPickerSize=20
const transferApplyForm=reactive({taskIds:[],fieldType:'',toUserId:null,reason:''})
const transferLogVisible=ref(false),transferPendingVisible=ref(false),transferPending=ref([]),transferPendingLoading=ref(false)
const transferApplyTaskName=computed(()=>{
  const rows=transferApplyTasks.value
  if(!rows.length)return ''
  if(rows.length===1)return rows[0]?._raw?.taskName||rows[0]?.name||rows[0]?.taskName||''
  return `共 ${rows.length} 个任务，将按同一职责、同一接收人批量发起移交申请`
})
const transferFieldsOf=(row)=>{
  const uid=Number(accessState.currentUser?.userId||accessState.userId);const raw=row?._raw||row||{};const fields=[]
  const pick=(key,id,label)=>{if(Number(id)===uid)fields.push({key,label})}
  pick('leader',raw.leaderId??raw.leader_id,'专业群负责人')
  pick('auditor',raw.auditorId??raw.auditor_id??raw.auditId,'专业群审核人')
  pick('principal',raw.principalId??raw.principal_id,'归口部门审核人')
  return fields
}
const openApplyMany=(rows)=>{
  const list=(Array.isArray(rows)?rows:[rows]).filter(Boolean)
  if(!list.length)return
  const taskIds=list.map(row=>row?._raw?.taskId||row?.taskId).filter(Boolean)
  // 批量移交要求所有任务都存在同一项本人职责，取交集作为可选移交类型
  const common=transferFieldsOf(list[0]).filter(field=>list.every(row=>transferFieldsOf(row).some(item=>item.key===field.key)))
  transferApplyTasks.value=list
  transferApplyFields.value=common
  transferApplyForm.taskIds=taskIds
  transferApplyForm.fieldType=common[0]?.key||''
  transferApplyForm.toUserId=null;transferApplyForm.reason=''
  transferApplyError.value=common.length?'':'所选任务没有共同的本人职责，请分开移交'
  transferUserOptions.value=[];transferApplyVisible.value=true;userPickerVisible.value=false
  if(common.length)searchTransferUsers('')
}
const openTransferApply=row=>openApplyMany([row])
const searchTransferUsers=async(keyword)=>{
  if(!transferApplyForm.taskIds.length)return
  userController?.abort();const request=userController=new AbortController()
  transferUserLoading.value=true
  try{const rows=await getTransferUsers(transferApplyForm.taskIds,keyword||'',transferApplyForm.fieldType,{signal:request.signal});if(!disposed&&request===userController){transferUserOptions.value=rows;transferApplyError.value=''}}catch(e){if(!disposed&&request===userController&&e.name!=='AbortError')transferApplyError.value=e.message||'接收人候选加载失败，请重新搜索'}finally{if(request===userController)transferUserLoading.value=false}
}
const onTransferSelectVisible=(visible)=>{if(visible)searchTransferUsers('')}
const onTransferTypeChange=()=>{transferApplyForm.toUserId=null;searchTransferUsers('');if(userPickerVisible.value)searchPicker(1)}
// —— 查找接收人：全部在用账号（含刚新建、尚未承担该职责的账号） ——
let pickerSeq=0
const openUserPicker=()=>{
  if(!transferApplyForm.fieldType)return
  userPickerVisible.value=true;userPickerKeyword.value='';userPickerSelected.value=null;userPickerRows.value=[];userPickerError.value=''
  searchPicker(1)
}
const searchPicker=async(page=1)=>{
  const seq=++pickerSeq
  userPickerLoading.value=true;userPickerError.value='';userPickerSelected.value=null
  try{
    const data=await getAllTransferUsers({fieldType:transferApplyForm.fieldType,keyword:userPickerKeyword.value.trim(),page,size:userPickerSize})
    if(disposed||seq!==pickerSeq)return
    userPickerRows.value=Array.isArray(data?.rows)?data.rows:[]
    userPickerTotal.value=Number(data?.total||0)
    userPickerPage.value=Number(data?.page||page)
  }catch(e){
    if(!disposed&&seq===pickerSeq)userPickerError.value=e.message||'账号列表加载失败'
  }finally{if(seq===pickerSeq)userPickerLoading.value=false}
}
const onPickerCurrentChange=row=>{userPickerSelected.value=row||null}
const ROLE_NAMES={0:'管理员',1:'普通用户',2:'负责人'}
const roleLabel=row=>`${row?.roles||''}`.trim()||ROLE_NAMES[String(row?.role)]||'—'
const confirmUserPicker=()=>{
  const user=userPickerSelected.value
  if(!user||user.self)return
  if(!transferUserOptions.value.some(item=>item.userId===user.userId))
    transferUserOptions.value=[...transferUserOptions.value,{userId:user.userId,name:user.name}]
  transferApplyForm.toUserId=user.userId
  userPickerVisible.value=false
  if(!user.available)ElMessage.warning(`${user.name||user.userId} 暂无该职责的填报/审核权限，提交时若被拒绝，请先在「组织与账号」中为其分配角色`)
}
const submitTransferApply=async()=>{
  if(transferApplyBusy.value)return
  transferApplyBusy.value=true;transferApplyError.value=''
  const taskIds=[...transferApplyForm.taskIds]
  try{
    if(taskIds.length>1)await createTransferBatch({taskIds,fieldType:transferApplyForm.fieldType,toUserId:transferApplyForm.toUserId,reason:transferApplyForm.reason})
    else await createTransferRequest({taskId:taskIds[0],fieldType:transferApplyForm.fieldType,toUserId:transferApplyForm.toUserId,reason:transferApplyForm.reason})
    ElMessage.success(taskIds.length>1?`已提交 ${taskIds.length} 个任务的移交申请，已通知接收人与管理员`:'移交申请已提交，已通知接收人与管理员')
    transferApplyVisible.value=false
    if(sentVisible.value)await loadSentTransfers()
    await loadPendingTransfers()
    emit('applied')
  }catch(e){transferApplyError.value=e.message||'提交失败'}finally{transferApplyBusy.value=false}
}
const openTransferLogs=async(row)=>{lastLogTask.value=row;await reloadLogs()}
const reloadLogs=async()=>{
  logController?.abort();const request=logController=new AbortController();logError.value=''
  transferLogVisible.value=true;transferLogsLoading.value=true;transferLogs.value=[]
  const row=lastLogTask.value
  try{const rows=await getTaskTransferLogs(row?._raw?.taskId||row?.taskId,{signal:request.signal});if(!disposed&&request===logController)transferLogs.value=rows}catch(e){if(!disposed&&e.name!=='AbortError')logError.value=e.message||'移交日志加载失败'}finally{if(request===logController)transferLogsLoading.value=false}
}
const loadPendingTransfers=async()=>{if(disposed||transferPendingLoading.value)return;transferPendingLoading.value=true;const request=pendingController=new AbortController();try{const rows=await getPendingTransfers({signal:request.signal});if(!disposed){transferPending.value=rows;pendingError.value='';void prunePendingSelection()}}catch(e){if(!disposed&&e.name!=='AbortError')pendingError.value=e.message||'待办加载失败'}finally{transferPendingLoading.value=false}}

/** 轮询刷新后按 requestId 保留勾选；已被他人处理掉的单据要从勾选里剔除，避免批量提交时报“已处理”。 */
const prunePendingSelection=async()=>{
  await nextTick()
  const ids=new Set(transferPending.value.map(row=>row.requestId))
  const kept=pendingSelection.value.filter(row=>ids.has(row.requestId))
  if(kept.length===pendingSelection.value.length)return
  pendingTableRef.value?.clearSelection()
  kept.forEach(row=>{const hit=transferPending.value.find(item=>item.requestId===row.requestId);if(hit)pendingTableRef.value?.toggleRowSelection(hit,true)})
}
const openPendingTransfers=async()=>{transferPendingVisible.value=true;await loadPendingTransfers()}
watch(transferPendingVisible,visible=>{if(!visible){rejectVisible.value=false;pendingSelection.value=[];pendingTableRef.value?.clearSelection()}})
const acceptTransfer=async(row)=>{
  if(handling.value)return;handling.value=true;try{
  try{await ElMessageBox.confirm(`确认接收任务《${row.taskName}》的「${row.fieldLabel}」？确认后该字段将转到你的名下。`,'确认接收移交',{confirmButtonText:'确认接收',cancelButtonText:'再想想',type:'warning'})}catch{return}
  try{if(props.beforeAccept&&!await props.beforeAccept())return;await acceptTransferRequest(row.requestId);ElMessage.success('已确认接收');await loadPendingTransfers();emit('changed',row)}catch(e){ElMessage.error(e.message||'确认失败')}
  }finally{handling.value=false}
}
const rejectTransfer=async(row)=>{
  if(handling.value)return;handling.value=true;try{
  let remark=''
  try{const result=await ElMessageBox.prompt('请填写拒绝理由','拒绝移交',{inputType:'textarea',inputValidator:v=>!!v?.trim()&&v.length<=500||'请填写500字以内的拒绝原因',confirmButtonText:'拒绝',cancelButtonText:'取消'});remark=result.value||''}catch{return}
  try{await rejectTransferRequest(row.requestId,remark);ElMessage.success('已拒绝该移交');await loadPendingTransfers()}catch(e){ElMessage.error(e.message||'操作失败')}
  }finally{handling.value=false}
}

// —— 待确认移交：查看任务详情，内容与任务列表「查看」抽屉的“基本信息与完成情况”一致 ——
const detailVisible=ref(false),detailLoading=ref(false),detailError=ref(''),detailTask=ref(null)
let detailSeq=0
const transformDetail=(raw)=>({
  id:String(raw.taskId),code:raw.taskCode||'',name:raw.taskName||'',
  level:Number(raw.level)||3,progress:Number(raw.progress)||0,
  dept:(raw.deptName&&String(raw.deptName).trim())||raw.dept||'—',_raw:raw
})
const openTaskDetail=async(row)=>{
  const seq=++detailSeq
  detailVisible.value=true;detailLoading.value=true;detailError.value='';detailTask.value=null
  try{
    const body=await get(`/task-transfer/${row.requestId}/task`)
    if(disposed||seq!==detailSeq)return
    detailTask.value=transformDetail(body?.task||body)
  }catch(e){if(!disposed&&seq===detailSeq)detailError.value=e.message||'任务详情加载失败'}
  finally{if(seq===detailSeq)detailLoading.value=false}
}

// —— 待确认移交：批量同意 / 批量拒绝 ——
const pendingTableRef=ref(null),pendingSelection=ref([])
const rejectVisible=ref(false),rejectRows=ref([]),rejectRemark=ref(''),rejectError=ref('')
const onPendingSelectionChange=rows=>{pendingSelection.value=Array.isArray(rows)?rows:[]}
const batchAccept=async()=>{
  const rows=[...pendingSelection.value]
  if(!rows.length||handling.value)return
  handling.value=true
  try{
    try{await ElMessageBox.confirm(`确认同意选中的 ${rows.length} 条移交申请？确认后对应职责将转到你的名下。`,'批量同意移交',{confirmButtonText:'全部同意',cancelButtonText:'再想想',type:'warning'})}catch{return}
    try{if(props.beforeAccept&&!await props.beforeAccept())return}catch{return}
    const result=await batchAcceptTransfers(rows.map(row=>row.requestId))
    const done=(result?.done||[]).length,failed=result?.failed||[]
    if(failed.length)ElMessage.warning(`成功 ${done} 条，失败 ${failed.length} 条：${failed.map(item=>item.message).join('；')}`)
    else ElMessage.success(`已同意 ${done} 条移交申请`)
    pendingSelection.value=[];pendingTableRef.value?.clearSelection()
    await loadPendingTransfers();emit('changed',result)
  }catch(e){ElMessage.error(e.message||'批量同意失败')}finally{handling.value=false}
}
const batchReject=()=>{
  const rows=[...pendingSelection.value]
  if(!rows.length||handling.value)return
  rejectRows.value=rows;rejectRemark.value='';rejectError.value='';rejectVisible.value=true
}
const submitBatchReject=async()=>{
  const rows=[...rejectRows.value]
  if(!rows.length||handling.value)return
  const remark=rejectRemark.value.trim()
  if(!remark){rejectError.value='请填写拒绝原因';return}
  if(remark.length>500){rejectError.value='拒绝原因不能超过500字';return}
  rejectError.value=''
  handling.value=true
  try{
    const result=await batchRejectTransfers(rows.map(row=>row.requestId),remark)
    const done=(result?.done||[]).length,failed=result?.failed||[]
    if(failed.length)ElMessage.warning(`成功 ${done} 条，失败 ${failed.length} 条：${failed.map(item=>item.message).join('；')}`)
    else ElMessage.success(`已拒绝 ${done} 条移交申请`)
    rejectVisible.value=false
    pendingSelection.value=[];pendingTableRef.value?.clearSelection()
    await loadPendingTransfers()
  }catch(e){rejectError.value=e.message||'批量拒绝失败'}finally{handling.value=false}
}

const visibleRefresh=()=>{if(document.hidden||rejectVisible.value||handling.value)return;void loadPendingTransfers()}
const confirmLeave=async()=>{if(transferApplyBusy.value||handling.value)return false;if(transferApplyVisible.value&&(transferApplyForm.reason.trim()||transferApplyForm.toUserId)){try{await ElMessageBox.confirm('尚未提交移交申请，确定放弃本次填写？','离开任务页',{confirmButtonText:'放弃',cancelButtonText:'继续填写',type:'warning'})}catch{return false}}transferApplyVisible.value=false;return true}
const closeApply=async done=>{if(await confirmLeave()){userPickerVisible.value=false;done()}}
onMounted(()=>{void loadPendingTransfers();poll=setInterval(visibleRefresh,15000);document.addEventListener('visibilitychange',visibleRefresh)})
onBeforeUnmount(()=>{disposed=true;clearInterval(poll);pendingController?.abort();userController?.abort();logController?.abort();document.removeEventListener('visibilitychange',visibleRefresh)})
defineExpose({openApply:openTransferApply,openApplyMany,openLogs:openTransferLogs,ownFields:transferFieldsOf,refresh:loadPendingTransfers,confirmLeave})
</script>
<style scoped>
.transfer-apply-tasks{max-height:132px;overflow:auto;margin:0 0 10px;padding:8px 10px;background:#f7f9fc;border:1px solid #e4e8ef;border-radius:6px;font-size:13px;color:#5b6472;line-height:22px}
.receiver-row{display:flex;gap:8px;width:100%;align-items:center}
.picker-search{margin:12px 0 10px}
.picker-pager{display:flex;align-items:center;justify-content:space-between;margin-top:10px}
.picker-pager .muted{color:#909399;font-size:13px}
.transfer-detail{min-height:120px}
.pending-footer{display:flex;align-items:center;justify-content:space-between;gap:12px}
.pending-footer .muted{color:#909399;font-size:13px}
.reject-head{margin-bottom:8px;font-size:14px;color:#303133}
.reject-tasks{max-height:180px;overflow:auto;margin-bottom:12px;padding:8px 12px;background:#f7f9fc;border:1px solid #e4e8ef;border-radius:6px}
.reject-task{font-size:13px;line-height:24px;color:#4b5563}
.reject-task .muted{color:#909399;margin-left:4px}
.reject-error{margin-top:10px}
</style>
<style>
/* 让批量拒绝的遮罩压住下层“待我确认的移交”对话框，避免视觉上还能点到复选框 */
.reject-overlay{background-color:rgba(0,0,0,.75)!important}
</style>
