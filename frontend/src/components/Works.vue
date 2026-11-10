<template>
  <div class="works-container" :class="{ 'detail-only': props.detailOnly }">
    <template v-if="!props.detailOnly">
    <div class="notice-bar">
      <div class="notice-bar-inner">
        <div class="notice-bar-label">提示</div>
        <div class="notice-bar-content">
          <div class="notice-bar-marquee">
            今天是{{ todayDate }}
          </div>
        </div>
      </div>
    </div>

    <div class="project-card">
      <div class="project-info">
        <h2 class="project-title">徐州工业职业技术学院“双高建设计划”任务与绩效目标管理</h2>
        <div class="project-meta">
          <div class="meta-item">
            <el-icon>
              <Calendar />
            </el-icon>
            <span>周期: 2025-01-01 至 2029-12-31</span>
          </div>
          <div class="meta-item">
            <el-icon>
              <User />
            </el-icon>
            <span>责任部门：“双高”建设办公室</span>
          </div>
        </div>
      </div>
      <div class="project-actions">
        <el-tag size="small" effect="plain" type="info"> {{ taskQueryState.initialized ? `共 ${visibleNodeCount} 条` : taskQueryState.error ? '任务加载失败' : '正在加载任务…' }} </el-tag>
        <div v-if="!isReview" class="toggle-group">
          <button v-if="canUsePermission('tasks.read')" class="toggle-btn" :class="{ active: currentTab === 'task' }"
            @click="handleTabSwitch('task')">任务</button>
          <button v-if="canUsePermission('performance.read')" class="toggle-btn" :class="{ active: currentTab === 'performance' }"
            @click="handleTabSwitch('performance')">绩效</button>
          <button v-if="canUsePermission('achievements.read')" class="toggle-btn" :class="{ active: currentTab === 'achievement' }"
            @click="handleTabSwitch('achievement')">成果</button>
          <button v-if="canUsePermission('budget.read')" class="toggle-btn" :class="{ active: currentTab === 'budget' }"
            @click="handleTabSwitch('budget')">预算</button>
        </div>
        <span v-if="canTransfer && transferMode && !transferSelection.length" class="transfer-hint">请在列表中勾选需要移交的三级任务</span>
        <el-button v-if="canTransfer && !transferMode" class="transfer-btn" @click="enterTransferMode">批量移交</el-button>
        <el-button v-if="canTransfer && transferMode" class="transfer-btn" @click="exitTransferMode">退出批量移交</el-button>
        <el-button v-if="canTransfer && transferMode && transferSelection.length" class="transfer-btn"
          @click="openTransfer(transferSelection)">确认移交（{{ transferSelection.length }}）</el-button>
        <TaskTransferRequests v-if="!isReview" ref="taskTransferRequests" :before-accept="()=>confirmTaskLeave(true)" @changed="handleRequestTransferred" @applied="exitApplyTransfer" />
        <el-button v-if="!isReview && canUsePermission('tasks.export')" class="export-btn" :loading="exporting" :disabled="exporting" @click="exportCurrentTasks">导出筛选结果</el-button>
        <el-button type="primary" class="create-btn" v-if="canCreateTask" @click="createTaskRef.open()">
          <el-icon class="mr-1">
            <Plus />
          </el-icon>
          新增任务
        </el-button>
      </div>
    </div>

    <div class="table-card">
      <QueryFilterBar v-if="!props.detailOnly" :exportable="!isReview" ref="queryBar" response-mode="tree" :endpoint="source.queryEndpoint" :options-endpoint="source.optionsEndpoint" :cache-key="queryMemoryKey" :required-keys="taskRequiredKeys" :before-apply="isReview?guardQueryChange:undefined" domain="tasks" title="任务" :defaults="taskDefaults" :hidden-keys="!isReview&&expertOnly?['year']:[]" @result="receiveQuery" @state="taskQueryState = $event">
        <template #before-reset>
          <el-button v-if="canApplyTransfer && !applyTransferMode" class="transfer-btn" @click.stop="enterApplyTransfer">移交</el-button>
          <template v-else-if="canApplyTransfer">
            <el-button class="transfer-btn" @click.stop="exitApplyTransfer">退出移交</el-button>
            <el-button v-if="transferSelection.length" type="primary" class="transfer-btn" @click.stop="submitApplyTransfer">确认移交（{{ transferSelection.length }}）</el-button>
          </template>
        </template>
      </QueryFilterBar>
      <div class="task-stats-wrap" :style="{visibility:taskQueryState.initialized?'visible':'hidden'}">
        <div class="task-stats" aria-label="任务统计">
          <span class="stat-item" data-stat="total">
            <span class="stat-label">任务总数</span>
            <span class="stat-value">{{ taskStats.total }}</span>
          </span>
          <span class="stat-item" data-stat="rate">
            <span class="stat-label">{{isReview?'当前范围平均进度':'当前完成率'}}</span>
            <span class="stat-value stat-value--rate">{{ taskStats.avgProgress === null ? '—' : taskStats.avgProgress + '%' }}</span>
          </span>
          <button type="button" class="stat-item stat-item--clickable" data-stat="done"
            :class="{ active: queryBar?.applied.status === '3' }" @click="applyTaskStatFilter('done')">
            <span class="stat-label">已完成</span>
            <span class="stat-value stat-value--done">{{ taskStats.completed }}</span>
            <span class="stat-note">{{ taskStats.completedRate === null ? '—' : taskStats.completedRate + '%' }}</span>
          </button>
          <button type="button" class="stat-item stat-item--clickable" data-stat="reviewing"
            :class="{ active: queryBar?.applied.status === '2' }" @click="applyTaskStatFilter('reviewing')">
            <span class="stat-label">审核中</span>
            <span class="stat-value stat-value--reviewing">{{ taskStats.reviewing }}</span>
          </button>
          <button type="button" class="stat-item stat-item--clickable" data-stat="overdue"
            :class="{ active: queryBar?.applied.overdue === 'yes' }" @click="applyTaskStatFilter('overdue')">
            <span class="stat-label">逾期</span>
            <span class="stat-value stat-value--overdue">{{ taskStats.overdue }}</span>
          </button>
          <span v-if="!isReview" class="stat-item stat-item--clickable" data-stat="todo" role="button" tabindex="0"
            :class="{ active: queryBar?.applied.scope === 'todo' }" @click="applyTaskStatFilter('todo')"
            @keydown.enter="applyTaskStatFilter('todo')" @keydown.space.prevent="applyTaskStatFilter('todo')">
            <span class="stat-label">我的待办</span>
            <span class="stat-value stat-value--todo">{{ taskStats.todo === null ? '—' : taskStats.todo }}</span>
            <span class="stat-note">已退回
              <button v-if="taskStats.returned !== null" type="button" class="stat-link" data-stat="returned"
                :class="{ active: queryBar?.applied.scope === 'returned' }" @click.stop="applyTaskStatFilter('returned')">{{ taskStats.returned }}</button>
              <span v-else>—</span>
            </span>
          </span>
        </div>
      </div>
      <div v-if="isReview" class="review-task-counts">三级评语：未评审 {{reviewCounts.unreviewed}} · 草稿／修订中 {{reviewCounts.draft}} · 已提交 {{reviewCounts.submitted}}。一级总评状态单独列示。</div>
      <div v-else class="review-task-counts">一级、二级目录的进度按当前筛选命中的三级任务计算；筛选导出另列全部下级任务进度。</div>
      <div class="table-wrapper" v-loading="loading || taskQueryState.loading && !taskQueryState.background"
        :class="{ 'transfer-capable': canTransfer || canApplyTransfer, 'transfer-selecting': transferMode || applyTransferMode }">
        <!-- v-memo：表格只跟随数据与展开态更新，勾选与批量移交模式切换都不再触发整表重渲染 -->
        <VirtualTreeTable :selection-visible="transferMode || applyTransferMode" :preserve-selection="taskQueryState.background" ref="taskTableRef" v-memo="[taskTreeData, expandedRowKeys, canTransfer, canApplyTransfer, transferMode, applyTransferMode, taskQueryState.background, !taskTreeData.length && taskQueryState.loading, !taskTreeData.length && taskQueryState.error, !taskTreeData.length && taskQueryState.initialized]"
          :data="taskTreeData" :empty-text="taskQueryState.loading ? '正在加载任务…' : taskQueryState.error ? '任务加载失败，请重试' : !taskQueryState.initialized ? '正在加载任务…' : '暂无匹配任务'" style="width: 100%" row-key="id" border
          :expand-row-keys="expandedRowKeys" @expand-change="handleExpandChange" @row-click="handleRowClick"
          @selection-change="handleTransferSelectionChange"
          :row-class-name="context => `${rowClassName(context) || ''} ${detailRowClass(context.row)}`" :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
          :header-cell-style="{ background: '#f8f9fa', color: '#606266', fontWeight: 'bold' }">
          <!-- 勾选一级/二级会由表格自身级联勾选其下全部三级任务，提交时只取三级。
               该列常驻挂载，平时整列收成 0 宽（见样式），进入移交模式后整表右移让出勾选列 -->
          <el-table-column v-if="canTransfer || canApplyTransfer" type="selection" width="46"
            class-name="transfer-select-cell" label-class-name="transfer-select-cell" />
          <el-table-column prop="name" label="任务名称" min-width="400">
            <template #default="scope">
              <span class="task-name-cell" :class="getLevelClass(scope.row.level)">
                <span v-if="scope.row.code" class="task-code">[{{ scope.row.code }}]</span>
                <span class="task-name-text">{{ scope.row.name }}</span>
                <el-tag v-if="scope.row._raw?.isPriority" type="danger" size="small">重点</el-tag>
                <el-tag v-if="scope.row._raw?.isDifficult" type="warning" size="small">困难</el-tag>
                <span v-if="(scope.row.children?.length || 0) > 0" class="child-count">（{{ scope.row.children.length
                  }}）</span>
              </span>
            </template>
          </el-table-column>
          <el-table-column label="进度" width="150">
            <template #default="scope">
              <el-progress :percentage="scope.row.progress" :status="getProgressStatus(scope.row)"
                :stroke-width="8"></el-progress>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="scope">
              <el-tag :type="getStatusTag(scope.row.status)" effect="plain" size="small">
                {{ scope.row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="dept" label="归口部门" width="150" show-overflow-tooltip></el-table-column>
          <el-table-column prop="principal" label="负责人" width="120">
            <template #default="scope">
              <div class="principal-cell" v-if="scope.row.principal">
                <div class="avatar-circle">
                  {{ scope.row.principal.charAt(0) }}
                </div>
                {{ scope.row.principal }}
              </div>
              <div v-else class="text-gray-400">-</div>
            </template>
          </el-table-column>
          <el-table-column label="起止时间" width="180" align="center">
            <template #default="scope">
              <div class="date-cell">
                {{ scope.row.startDate }}<br />{{ scope.row.endDate }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="计划／实际完成" width="160" align="center">
            <template #default="scope"><div v-if="scope.row.level === 3" class="date-cell">计划：{{ scope.row._raw?.plannedCompletionDate || '—' }}<br />实际：{{ scope.row._raw?.actualCompletionDate ? formatDate(scope.row._raw.actualCompletionDate) : '—' }}</div></template>
          </el-table-column>
          <el-table-column v-if="isReview" label="本轮本人评审" width="145"><template #default="{row}"><template v-if="row.level!==2"><el-tag size="small" effect="plain">{{reviewStatusLabel(row._raw?.reviewStatus)}}</el-tag><div v-if="reviewResultLabel(row._raw?.reviewResult)" class="review-result-tag"><el-tag size="small" :type="row._raw.reviewResult==='pass'?'success':'danger'" effect="light">{{reviewResultLabel(row._raw.reviewResult)}}</el-tag></div></template></template></el-table-column>
          <el-table-column label="操作" :width="isReview?200:280" fixed="right" align="center">
            <template #default="scope">
              <div v-if="scope.row.level === 3">
                <el-button link type="primary" size="small" @click="handleViewDetail(scope.row)">
                  <el-icon class="mr-1">
                    <View />
                  </el-icon>查看
                </el-button>
                <el-button v-if="isReview" link type="primary" size="small" @click.stop="openTaskDetail(scope.row._raw.taskId,{review:true})">专家评语</el-button>
                <el-button v-if="canAuditTask(scope.row)" link type="success" size="small"
                  @click="handleAuditTask(scope.row)">
                  <el-icon class="mr-1">
                    <Document />
                  </el-icon>审核
                </el-button>
                <el-button v-if="canWithdrawTask(scope.row)" link type="warning" size="small"
                  @click.stop="handleWithdrawTask(scope.row)">
                  <el-icon class="mr-1">
                    <RefreshLeft />
                  </el-icon>撤回
                </el-button>
                <el-button v-if="!isReview && scope.row._raw?.canSupervise === true" link type="danger" size="small" @click="handleSupervise(scope.row)">
                  <el-icon class="mr-1">
                    <Bell />
                  </el-icon>督办
                </el-button>
                <el-button v-if="!isReview&&taskTransferRequests?.ownFields(scope.row).length" link type="warning" size="small" @click.stop="taskTransferRequests.openApply(scope.row)">移交申请</el-button>
                <el-button v-if="!isReview" link type="info" size="small" @click.stop="taskTransferRequests.openLogs(scope.row)">移交日志</el-button>
              </div>
              <el-button v-else-if="scope.row.level === 1 && canReviewTaskObject(scope.row)" link type="primary" size="small" @click.stop="openTaskDetail(scope.row._raw.taskId, {year: filterYear || 2026, review: true})">总体评语</el-button>
              <div v-else style="height: 24px;"></div>
            </template>
          </el-table-column>
        </VirtualTreeTable>
      </div>
    </div>

    </template>
    <el-drawer v-model="drawerVisible" title="任务详情与反馈" size="min(1200px, calc(100vw - 32px))" :destroy-on-close="true" :before-close="closeTaskDetail" @closed="handleDrawerClosed">
      <template #header>
        <div class="task-detail-header">
          <span>{{currentTask?.level===1?'一级任务详情与总体评语':isReview?'任务详情与专家评语':'任务详情与反馈'}}</span>
          <el-button v-if="canReviewTaskObject(currentTask)" link type="primary" :disabled="detailNavigationBusy" @click="expertReviewRef?.focusEditor()">专家评语 ↓</el-button>
          <div v-if="!props.detailOnly && currentTask?.level === 3" class="task-detail-navigation">
            <span v-if="detailTaskIndex >= 0">{{ detailTaskIndex + 1 }} / {{ detailTasks.length }}</span>
            <el-button :disabled="detailNavigationBusy || detailTaskIndex <= 0" @click="navigateDetail(-1)">上一个</el-button>
            <el-button :disabled="detailNavigationBusy || detailTaskIndex < 0 || detailTaskIndex >= detailTasks.length - 1"
              @click="navigateDetail(1)">下一个</el-button>
          </div>
        </div>
      </template>
      <div v-if="currentTask" ref="detailContentRef" class="drawer-content" v-loading="drawerLoading">
        <el-alert v-if="reviewDetailError" :title="reviewDetailError" type="error" :closable="false"/>
        <template v-if="!reviewAccessLost">
        <DetailActions v-if="!props.detailOnly" :busy="detailNavigationBusy" :link="taskDetailLink" :locate="locateCurrentTask" />
        <div v-if="canTransfer && currentTask?.level === 3" class="owner-actions">
          <el-button size="small" type="primary" plain :icon="Switch"
            @click="openTransfer([currentTask])">移交</el-button>
        </div>
        <TaskBasicInfo :task="currentTask" :editable="canUsePermission('tasks.attributes', canTransfer)" @attributes="taskAttributesDialog.open(currentTask._raw)" />
        <el-alert v-if="liveWorkflowError" :title="liveWorkflowError" type="warning" :closable="false" show-icon>
          <el-button link type="primary" @click="refreshCurrentTaskWorkflow(detailContext())">重新核对审核状态</el-button>
        </el-alert>

        <template v-if="currentTask.level === 3">
        <div class="step-section mb-6">
          <el-steps :active="stepActive" finish-status="success" align-center class="custom-steps">
            <el-step title="材料提交" :description="stepDesc.submit"></el-step>
            <el-step title="专业群审核" :description="stepDesc.dept"></el-step>
            <el-step title="归口审核" :description="stepDesc.principal"></el-step>
            <el-step title="完结归档" :description="stepDesc.admin"></el-step>
          </el-steps>
        </div>
        
        <div class="section-title mt-6">{{isReview?'任务完成情况与材料':'进度反馈与材料上报'}}</div>
        <el-form label-position="top" class="feedback-form">
          
          <template v-if="forthTasks && forthTasks.length > 0">
            <el-form-item 
              v-for="task in forthTasks"
              :key="task.taskId" 
              :label="task.taskName"
            >
              <div class="progress-input-wrapper">
                <span v-if="isReview" class="readonly-task-value">{{formatTaskNumber(task.currentValue)}}</span>
                <el-input-number v-else
                  v-model="forthFeedbackData[task.taskId].progress" 
                  :min="0" 
                  :max="task.dataType === '2' ? 100 : 999999999" 
                  :step="1"
                  :precision="0" 
                  controls-position="right" 
                  style="width: 240px" 
                  :disabled="!canSubmitMaterial" 
                />
                <span v-if="task.dataType === '2'" class="percent-suffix">%</span>
                <div class="level4-rate-summary">
                  <span>目标值 {{ formatTaskNumber(task.targetValue ?? task.target_value) }}</span>
                  <span>实际完成率 {{ formatRate(level4LiveRate(task).actual) }}</span>
                  <span>计入汇总 {{ formatRate(level4LiveRate(task).counted) }}</span>
                </div>
              </div>
            </el-form-item>
            <el-alert class="level4-aggregate-preview" type="info" :closable="false" show-icon>
              <template #title>
                预计三级任务完成率：{{ formatRate(level4AggregatePreview) }}；各目标等权，单项目标最高按100%计入。
              </template>
            </el-alert>
          </template>

          <template v-else>
            <el-form-item :label="isPercentTask ? '本次进度更新(%)' : '本次填报值'">
              <div class="progress-input-wrapper" :title="isPercentTask ? '可使用鼠标滚轮增减（每次±1）' : ''"
                @wheel="handleProgressWheel">
                <span v-if="isReview" class="readonly-task-value">{{formatTaskNumber(currentTask._raw?.currentValue)}}</span><el-input-number v-else v-model="feedbackForm.progress" :min="0" :max="isPercentTask ? 100 : 999999999" :step="1"
                  :precision="0" controls-position="right" style="width: 240px" :disabled="!canSubmitMaterial" />
                <span v-if="isPercentTask" class="percent-suffix">%</span>
                <span v-if="isPercentTask" class="wheel-tip">滚轮可增减</span>
              </div>
            </el-form-item>
          </template>

          <el-form-item label="完成情况" v-if="isReview || canSubmitMaterial || isAnyAuditor">
            <el-input v-model="feedbackForm.comment" type="textarea" :rows="2" :maxlength="50"
              :show-word-limit="canSubmitMaterial" :disabled="!canSubmitMaterial"
              :placeholder="canSubmitMaterial ? '请输入完成情况（50字以内）' : '暂无完成情况'" />
          </el-form-item>
          
          <el-form-item label="佐证材料上传" v-if="canSubmitMaterial" data-detail-section="材料">
            <el-upload class="upload-area" :class="{ 'hide-upload-trigger': fileList.length >= 1 }" drag action="#"
              :limit="1" :on-exceed="handleExceed" :on-change="handleFileChange" :on-remove="handleRemove"
              :on-preview="handlePreview" :file-list="fileList" :auto-upload="false" accept=".doc,.docx,.pdf">
              <el-icon class="el-icon--upload">
                <UploadFilled />
              </el-icon>
              <div class="el-upload__text">
                拖拽文件到此处或 <em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  <el-icon class="mr-1 warning-icon">
                    <Warning />
                  </el-icon>
                  仅允许上传 1 个文件，支持 doc/docx/pdf 格式，文件大小不超过 50MB
                </div>
              </template>
            </el-upload>
          </el-form-item>
          
          <div v-if="!isReview" class="form-actions text-right mb-4">
            <el-button type="primary" @click="submitFeedback" :disabled="!canSubmitMaterial || drawerLoading || auditLoading" :loading="feedbackSubmitting">提交反馈</el-button>
            <el-button v-if="canWithdrawCurrentTask" type="warning" :icon="RefreshLeft"
              @click="handleWithdrawTask(currentTask)">
              撤回提交
            </el-button>
          </div>
        </el-form>

        <div v-if="canAudit && latestAudit" ref="auditSectionRef" class="audit-section mt-6">
          <h4 class="section-title">审核处理</h4>
          <el-form label-position="top" class="audit-form">
            <el-form-item label="审核结果">
              <el-radio-group v-model="auditForm.result">
                <el-radio :label="1" border class="radio-success">通过</el-radio>
                <el-radio :label="0" border class="radio-danger">驳回</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="审批意见">
              <el-input v-model="auditForm.comment" type="textarea" :rows="3" placeholder="请输入审批意见（必填）..."></el-input>
            </el-form-item>
            <el-form-item label="佐证材料" v-if="latestAudit.fileId || latestAudit.file_id">
              <div class="file-link" @click="previewMaterialFile(latestAudit)">
                <el-icon>
                  <Document />
                </el-icon>
                <span>{{ latestAudit.filename || getFileName(latestAudit.fileId || latestAudit.file_id) }}</span>
              </div>
            </el-form-item>
            <div class="form-actions text-right mt-4">
              <el-button type="primary" @click="submitAudit" :loading="auditSubmitting" :disabled="drawerLoading || auditLoading">确认提交</el-button>
            </div>
          </el-form>
        </div>

        <div class="log-section mt-6">
          <h4 class="section-title">操作日志与审核记录</h4>
          <div class="log-timeline-box">
            <div class="sub-title">操作日志</div>
            <template v-if="auditLogs && auditLogs.length">
            <el-timeline>
                <el-timeline-item v-for="log in auditLogs" :key="log.logId || (log.subId + '_' + log.createTime)"
                  :timestamp="formatDateTime(log.createTime)" placement="top">
                <el-card shadow="never" class="log-card-item">
                  <div class="log-header">
                      <span class="log-user font-bold">{{ log.operatorName||getUserName(log.operatorId) }}</span>
                      <el-tag size="small" type="info">{{ log.actionType || '操作' }}</el-tag>
                    </div>
                    <div class="log-content mt-2">
                      状态：{{ getFlowStatusDesc(log.preStatus) }} → {{ taskAuditPostStatus(log, getFlowStatusDesc(log.postStatus)) }}
                    </div>
                    <div class="log-content mt-1" v-if="log.comment">
                      意见：{{ log.comment }}
                    </div>
                  </el-card>
                </el-timeline-item>
              </el-timeline>
            </template>
            <template v-else-if="noticeLogs && noticeLogs.length">
              <el-timeline>
                <el-timeline-item v-for="log in noticeLogs" :key="log.id || (log.operatorId + '_' + log.createTime)"
                  :timestamp="formatDateTime(log.createTime)" placement="top">
                  <el-card shadow="never" class="log-card-item">
                    <div class="log-header">
                      <span class="log-user font-bold">{{ log.operatorName||getUserName(log.operatorId) }}</span>
                      <el-tag size="small" type="info">{{ log.actionType || '通知' }}</el-tag>
                    </div>
                    <div class="log-content mt-2" v-if="log.comment">
                      {{ log.comment }}
                    </div>
                  </el-card>
                </el-timeline-item>
              </el-timeline>
            </template>
            <el-empty v-else description="暂无操作日志" :image-size="90" />

            <div class="sub-title mt-4">审批记录</div>
            <template v-if="auditList && auditList.length">
              <el-timeline>
                <el-timeline-item v-for="item in auditList" :key="item.subId || item.sub_id || item.submitTime"
                  :timestamp="formatDateTime(item.submitTime)" placement="top">
                  <el-card shadow="never" class="log-card-item">
                    <div class="log-header">
                      <span class="log-user font-bold">审批单 {{ item.subId || item.sub_id }}</span>
                      <el-tag size="small" type="info">{{ taskSubmissionStatus(item, auditLogs, getFlowStatusDesc(item.flowStatus ?? item.flow_status)) }}</el-tag>
                    </div>
                    
                    <div class="log-content mt-2">
                      <template v-if="item.subList && item.subList.length > 0">
                        <div v-for="sub in item.subList" :key="sub.task_id" style="margin-bottom: 4px;">
                          <span style="color: #606266;">{{ sub.task_name ?? sub.taskName ?? getForthTaskName(sub.task_id) }}：</span>
                          <span style="color: #409eff; font-weight: 500;">
                            {{ sub.reported_value ?? sub.reportedValue ?? '-' }}
                          </span>
                          <span class="audit-level4-rate">
                            （目标 {{ formatTaskNumber(sub.target_value ?? sub.targetValue) }}，实际
                            {{ formatRate(sub.actual_progress ?? sub.actualProgress) }}，计入
                            {{ formatRate(sub.counted_progress ?? sub.countedProgress) }}）
                          </span>
                        </div>
                      </template>
                      <template v-else-if="item.historicalDetailsAvailable === false">
                        计入汇总值：{{ item.reportedValue ?? item.reported_value ?? '—' }}；当时四级原始值未留存。
                      </template>
                      <template v-else>
                        计入汇总值：{{ item.reportedValue ?? item.reported_value ?? '-' }}，
                        数据类型：{{ item.dataType ?? item.data_type ?? '-' }}
                      </template>
                      <div v-if="item.comment" class="mt-2">完成情况：{{ item.comment }}</div>
                    </div>

                    <div class="log-content mt-1 text-gray-400">
                      提交人：{{ getUserName(item.submitBy ?? item.submit_by) }}，当前处理人：{{ getUserName(item.currentHandlerId ?? item.current_handler_id) }}
                    </div>
                    
                    <div class="log-file mt-2" v-if="item.fileId || item.file_id">
                      <el-icon><Document /></el-icon>
                      <span class="file-name">{{ item.filename || getFileName(item.fileId || item.file_id) }}</span>
                      <span class="preview-text" @click.stop="previewMaterialFile(item)">点击预览</span>
                      <span class="preview-text" @click.stop="downloadByFileId(item.fileId || item.file_id, item.fileSuffix || item.file_suffix)">点击下载</span>
                    </div>

                    <div class="mt-2">
                      <el-button link type="primary" size="small" @click.stop="toggleAuditItemLogs(item)">
                        {{ auditItemLogVisible[getAuditSubKey(item)] ? '收起审批动作' : '查看审批动作' }}
                      </el-button>
                    </div>
                    <div class="mt-2" v-if="auditItemLogVisible[getAuditSubKey(item)]">
                      <div v-if="auditItemLogLoading[getAuditSubKey(item)]" class="text-gray-400">加载中...</div>
                      <template v-else>
                        <el-timeline
                          v-if="auditItemLogList[getAuditSubKey(item)] && auditItemLogList[getAuditSubKey(item)].length">
                          <el-timeline-item v-for="log in auditItemLogList[getAuditSubKey(item)]"
                            :key="log.logId || (log.subId + '_' + log.createTime)"
                            :timestamp="formatDateTime(log.createTime)" placement="top">
                            <el-card shadow="never" class="log-card-item">
                              <div class="log-header">
                                <span class="log-user font-bold">{{ log.operatorName||getUserName(log.operatorId) }}</span>
                                <el-tag size="small" type="info">{{ log.actionType || '操作' }}</el-tag>
                              </div>
                              <div class="log-content mt-2">
                                状态：{{ getFlowStatusDesc(log.preStatus) }} → {{ taskAuditPostStatus(log, getFlowStatusDesc(log.postStatus)) }}
                              </div>
                              <div class="log-content mt-1" v-if="log.comment">
                                意见：{{ log.comment }}
                              </div>
                            </el-card>
                          </el-timeline-item>
                        </el-timeline>
                        <el-empty v-else description="暂无审批动作日志" :image-size="70" />
                      </template>
                    </div>
                  </el-card>
                </el-timeline-item>
              </el-timeline>
            </template>
            <el-empty v-else description="暂无审批记录" :image-size="90" />
          </div>
        </div>

        <div v-if="canTransfer && transferLogs.length" class="log-section mt-6">
          <h4 class="section-title">归属变更记录</h4>
          <el-table :data="transferLogs" size="small" border v-loading="transferLogsLoading"
            empty-text="暂无归属变更记录">
            <el-table-column label="时间" width="160">
              <template #default="scope">{{ formatDateTime(scope.row.createTime) }}</template>
            </el-table-column>
            <el-table-column prop="operatorName" label="操作人" width="120" show-overflow-tooltip />
            <el-table-column label="归口部门" min-width="200" show-overflow-tooltip>
              <template #default="scope">
                {{ scope.row.deptBeforeName || '—' }} → {{ scope.row.deptAfterName || '—' }}
              </template>
            </el-table-column>
            <el-table-column label="责任人" min-width="180" show-overflow-tooltip>
              <template #default="scope">
                {{ scope.row.leaderBeforeName || '—' }} → {{ scope.row.leaderAfterName || '—' }}
              </template>
            </el-table-column>
            <el-table-column label="专业群审核人" min-width="180" show-overflow-tooltip>
              <template #default="scope">{{ scope.row.auditorBeforeName || '历史未记录' }} → {{ scope.row.auditorAfterName || '历史未记录' }}</template>
            </el-table-column>
            <el-table-column label="归口部门审核人" min-width="180" show-overflow-tooltip>
              <template #default="scope">
                {{ scope.row.principalBeforeName || '—' }} → {{ scope.row.principalAfterName || '—' }}
              </template>
            </el-table-column>
            <el-table-column prop="reason" label="原因" min-width="180" show-overflow-tooltip />
          </el-table>
        </div>

        </template>
        <section v-if="currentTask.level === 1" class="mt-6">
          <h4>本次获准评审的三级任务完成情况</h4>
          <el-table :data="reviewChildren" border max-height="380" empty-text="暂无获准任务">
            <el-table-column prop="code" label="任务编号" width="145" />
            <el-table-column prop="name" label="任务名称" min-width="260" />
            <el-table-column label="进度" width="145"><template #default="{row}"><el-progress :percentage="row.progress" /></template></el-table-column>
            <el-table-column prop="status" label="状态" width="100" />
            <el-table-column label="操作" width="90"><template #default="{row}"><el-button link type="primary" @click="openTaskDetail(row._raw.taskId, {year: reviewYear})">查看</el-button></template></el-table-column>
          </el-table>
        </section>
        </template>
        <ExpertReviewEditor v-if="currentTask?._raw?.taskId && canReviewTaskObject(currentTask) && (!isReview||reviewDetail)" :key="`${currentTask._raw.taskId}:${reviewYear}:${props.reviewExpertId || ''}`" ref="expertReviewRef" :task-id="currentTask._raw.taskId" :year="(currentTask.level === 3 ? currentTask._raw.phase : reviewYear) || filterYear || 2026" :expert-id="props.reviewExpertId" :round-id="isReview?workspace.roundId:null" :read-only="isReview&&workspace.readOnly" :history-mode="workspace.kind==='reviewHistory'" :initial-detail="isReview?reviewDetail:null" @saved="handleExpertSaved" @reload-requested="reloadReviewDetail" @access-lost="handleReviewAccessLost" />
      </div>
    </el-drawer>

    <el-dialog v-model="previewVisible" title="文件预览" width="60%" destroy-on-close @closed="cleanupPreviewUrl">
      <div class="preview-container">
        <div v-if="previewType === 'pdf'" class="pdf-viewer">
          <div class="pdf-viewer-header">
            <span class="file-name-display">{{ previewFileName }}</span>
            <el-button type="primary" :icon="Download" @click="downloadPreviewFile" :disabled="!previewUrl">
              下载文件
            </el-button>
          </div>
          <iframe :src="previewUrl" width="100%" height="600px" style="border:none;" allow="fullscreen"></iframe>
        </div>
        <div v-else class="doc-viewer">
          <div class="doc-icon">
            <el-icon :size="60" color="#409eff">
              <Document />
            </el-icon>
          </div>
          <h3 class="mt-4">{{ previewFileName }}</h3>
          <p class="text-gray-500 mt-2">
            该文件为 Word 文档 (.doc/.docx)，暂不支持在线直接渲染。<br>
            请下载文件后查看。
          </p>
          <el-button type="primary" class="mt-4" @click="previewFileBlob?downloadPreviewFile():downloadSelectedLocalFile()">下载当前选择文件</el-button>
        </div>
      </div>
    </el-dialog>

    <CreateTaskDialog v-if="!isReview" ref="createTaskRef" :year="filterYear" @created="handleTaskCreated" />
    <TaskAttributesDialog v-if="!isReview" ref="taskAttributesDialog" @saved="refreshTasksSilently" />
    <TransferDialog v-if="!isReview" ref="transferRef" target-type="TASK" :items="transferRows" @transferred="handleTransferred" />
  </div>
</template>

<script setup>

const queryBar = ref(null), queryIds = ref(new Set());
const taskQueryState=ref({loading:true,initialized:false,error:''});

watch(()=>taskQueryState.value.responseCode,code=>{if([401,403,428].includes(Number(code))){drawerVisible.value=false;allTaskTreeData.value=[];queryIds.value=new Set();applyFilters();}})
watch(()=>taskQueryState.value.error,error=>{if(error)loading.value=false})
let taskRenderPromise=Promise.resolve(),taskRenderVersion=0;
const receiveQuery = result => {taskRenderPromise=renderTaskQuery(result);};
const renderTaskQuery = async result => {
  const version=++taskRenderVersion;
  loading.value=!result.background;
  const tasks=isReview.value?(result.items||[]):await supplementParentTasks(result.items||[]);
  if(isReview.value&&result.round)emit('round-updated',result.round);
  if(!isCurrentSession()||version!==taskRenderVersion)return;
  const names=new Map(userMap.value),departments=new Map(deptMap.value);
  for(const item of tasks){if(item.deptName)departments.set(String(item.deptId),item.deptName);for(const role of ['leader','auditor','principal'])if(item[role+'Name'])names.set(String(item[role+'Id']),item[role+'Name']);}
  userMap.value=names;deptMap.value=departments;allTaskTreeData.value=buildTaskTree(tasks);
  if(!isReview.value)void loadUserAndDeptMaps(tasks).then(()=>{if(!isCurrentSession()||version!==taskRenderVersion)return;const refresh=nodes=>{for(const node of nodes){const display=transformTask(node._raw);node.dept=display.dept;node.principal=display.principal;if(node.children)refresh(node.children)}};refresh(taskTreeData.value);});
  queryIds.value = new Set((result.matchedIds||[]).map(String));
  const nextYear = result.query.year === 'missing' ? '' : String(result.query.year || '');
  const changed = filterYear.value !== nextYear;
  filterYear.value = nextYear;
  void changed;
  if(!result.background){filterStatus.value = ''; taskKindFilter.value = ''; quickFilter.value = 'all'; leaderFilter.value = ''; auditStageFilter.value = ''; auditDeptFilter.value = ''; owningDeptFilter.value = ''; actualSearchText.value = ''; searchText.value = '';}
  loading.value=false;applyFilters();
  if(route.query.taskId && String(currentTask.value?._raw?.taskId || '') !== String(route.query.taskId))await openTaskFromQuery(route.query.taskId);
};
import { isAdminTestEnabled } from '@/utils/adminTest';
import { adminTestFetch } from '@/utils/adminTest';

import { accessState, canUsePermission as businessPermission, hasPermission } from '@/utils/accessState';
import ExpertReviewEditor from './ExpertReviewEditor.vue';
import {createTaskPageSource,taskPageCapabilities} from '@/utils/taskPageSource';
import {normalizeRoundReview} from '@/api/roundReviews';
import {queryFingerprint} from '@/utils/queryPresentation';

function canReviewTaskObject(row) {
  if(isReview.value)return !!row&&[1,3].includes(Number(row.level));
  if (accessState.reviewRoundsEnabled || !hasPermission('expert.read') || !row) return false;
  if (accessState.isAdmin) return true;
  const rule = accessState.dataScopes.expert?.[0];
  if (!rule) return true;
  const year = Number(row._raw?.phase || reviewYear.value || filterYear.value || 2026);
  if (rule.years?.length && !rule.years.map(Number).includes(year)) return false;
  if (rule.mode === 'ALL') return true;
  if (rule.mode !== 'CUSTOM') return false;
  const selected = new Set((rule.items || []).map(String));
  if (Number(row.level) === 3) return selected.has(String(row._raw?.taskId));
  if (props.detailOnly) return selected.size > 0;
  const contains = node => Number(node.level) === 3 ? selected.has(String(node._raw?.taskId)) : (node.children || []).some(contains);
  return contains(row);
}

import { getExpertDetail } from '@/api/expertReviews';
import { createWorksCache } from '@/utils/worksCache';
import { ref, reactive, onMounted, onUnmounted, watch, computed, nextTick } from 'vue';
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router';
import { 
  Calendar, User, Plus, Download, UploadFilled, View, Bell, Document, Warning, RefreshLeft, Switch
} from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import DetailActions from './DetailActions.vue';
import QueryFilterBar from './QueryFilterBar.vue';
import VirtualTreeTable from './VirtualTreeTable.vue';
import CreateTaskDialog from './CreateTaskDialog.vue';
import TransferDialog from './TransferDialog.vue';
import TaskTransferRequests from './TaskTransferRequests.vue';
import TaskBasicInfo from './TaskBasicInfo.vue';
import { useBrowseMemory } from '@/utils/browseMemory';
import { get } from '@/utils/request';
import { detailRowClass, locateDetailRow, confirmDetailLeave } from '@/utils/detailNavigation';
import { taskStageDescription, currentTaskSubmission, canProcessTaskSubmission, taskWorkflowStep, taskSubmissionStatus, taskAuditPostStatus } from '@/utils/taskAuditTimeline.mjs';
import { getAllTasks, getTaskById, getForthTasks, exportTasks } from '@/api/task'; 
import TaskAttributesDialog from './TaskAttributesDialog.vue';
import { getAllUsers } from '@/api/user';
import { getAudit, getAuditByTaskId, getAuditLogs, reSubmitMaterial, auditTask, getTodoAudits, drawbackTask } from '@/api/audit';
import { uploadFile, submitMaterial, batchSubmitMaterial, sendNotice } from '@/api/report';
import { getNotices } from '@/api/notice';
import { getDeptById } from '@/api/dept';
import { getTransferCapabilities, getTransferLogs } from '@/api/transfer';

const props = defineProps({ workspace:{type:Object,default:()=>({kind:'business',roundId:null,round:null,readOnly:false})},detailOnly: Boolean, reviewExpertId: { type: [Number, String], default: null } });
const emit = defineEmits(['detail-closed', 'expert-saved','round-updated']);
const expertReviewRef = ref(null);
const isReview=computed(()=>props.workspace.kind!=='business');
const pageCapabilities=computed(()=>taskPageCapabilities(props.workspace));
const source=computed(()=>createTaskPageSource(props.workspace));
const queryMemoryKey=computed(()=>isReview.value?`tasks:${props.workspace.kind}:${props.workspace.roundId}`:'tasks');
const taskDefaults=computed(()=>isReview.value?(props.workspace.round?.review_year?{year:String(props.workspace.round.review_year)}:{}):expertOnly.value?{}:{year:'2026'});
const taskRequiredKeys=computed(()=>isReview.value&&props.workspace.round?.review_year?['year']:[]);
const canUsePermission=(code,legacy=true)=>pageCapabilities.value.business&&businessPermission(code,legacy);
const reviewDetail=ref(null),reviewDetailError=ref(''),reviewAccessLost=ref(false);
let reviewDetailController;
const reviewStatusLabel=value=>({unreviewed:'未评审',draft:'草稿／修订中',submitted:'已提交'})[value]||'未评审';
const reviewResultLabel=value=>({pass:'通过',reject:'不通过'})[value]||'';
async function guardQueryChange(next,current){if(queryFingerprint(next)===queryFingerprint(current))return true;if(!await confirmTaskLeave())return false;drawerVisible.value=false;return true;}
async function prepareContextChange(){if(!await confirmTaskLeave())return false;drawerVisible.value=false;return true;}
async function handleExpertSaved(result){emit('expert-saved',result);if(isReview.value)await queryBar.value?.refresh({background:true});}
function handleReviewAccessLost(message){if(!isReview.value)return;reviewAccessLost.value=true;reviewDetailError.value=message;clearDetailData();reviewChildren.value=[];}

// 保留旧账号识别，仅影响年度筛选；业务导出统一使用任务明细接口。
const expertOnly = computed(() => !accessState.isAdmin && accessState.permissionRoles?.every(r => r.code === 'EXPERT'));
const reviewChildren = ref([]);
const reviewYear = ref(null);
// --- 状态管理 ---
const route = useRoute();
const router = useRouter();
const canCreateTask = ref(false);
const taskTransferRequests = ref(null);
const handleRequestTransferred = async () => { await refreshTasksSilently(); await loadTaskShortcuts(); };
const canTransfer = ref(false);
const createTaskRef = ref(null);
const loadTaskManagementPermission = async () => {
  try {
    const [capability, transfer] = await Promise.all([
      get('/biz/tasks/manage/capabilities'),
      getTransferCapabilities()
    ]);
    if (isCurrentSession()) {
      canCreateTask.value = canUsePermission('tasks.create', capability.canCreate === true);
      canTransfer.value = canUsePermission('tasks.transfer', transfer.canTransfer === true);
    }
  } catch {
    if (isCurrentSession()) {
      canCreateTask.value = false;
      canTransfer.value = false;
    }
  }
};
// --- 管理员归属移交 ---
const transferRef = ref(null);
const transferRows = ref([]);
// 批量移交模式：默认隐藏勾选列，点“批量移交”后才出现，退出时清空已选
const transferMode = ref(false);
const transferSelection = ref([]);
// 本人职责批量移交：仅给了专业群负责人/审核人/归口部门审核人职责的账号可见
const applyTransferMode = ref(false);
const ownTransferFields = row => taskTransferRequests.value?.ownFields(row) || [];
const canApplyTransfer = computed(() => {
  if (isReview.value) return false;
  const hasOwnField = nodes => nodes.some(node =>
    (Number(node.level) === 3 && ownTransferFields(node).length > 0) ||
    (node.children?.length > 0 && hasOwnField(node.children)));
  return hasOwnField(allTaskTreeData.value);
});
const transferLogs = ref([]);
const transferLogsLoading = ref(false);
const handleTransferSelectionChange = (rows) => {
  transferSelection.value = (rows || []).filter(row => Number(row?.level) === 3);
};
const toTransferRow = (row) => {
  const raw = row._raw || {};
  return {
    id: Number(raw.taskId),
    code: raw.taskCode || '',
    name: raw.taskName || '',
    deptId: raw.deptId ?? null,
    deptName: raw.deptName || row.dept || '',
    leaderId: raw.leaderId ?? null,
    leaderName: raw.leaderName || row.principal || '',
    auditorId: raw.auditorId ?? raw.auditId ?? null,
    auditorName: raw.auditorName || '',
    principalId: raw.principalId ?? null,
    principalName: raw.principalName || ''
  };
};
const openTransfer = (rows) => {
  const list = (Array.isArray(rows) ? rows : [rows])
    .filter(Boolean)
    .map(toTransferRow)
    .filter(row => Number.isFinite(row.id) && row.id > 0);
  if (!list.length) {
    ElMessage.warning('请选择需要移交的三级任务');
    return;
  }
  transferRows.value = list;
  transferRef.value?.open();
};
const enterTransferMode = () => {
  applyTransferMode.value = false;
  transferMode.value = true;
  transferSelection.value = [];
};
const exitTransferMode = () => {
  transferMode.value = false;
  transferSelection.value = [];
  taskTableRef.value?.clearSelection();
};
// 本人职责批量移交：进入后列表出现勾选列，只提交本人承担职责的三级任务
const enterApplyTransfer = () => {
  transferMode.value = false;
  applyTransferMode.value = true;
  transferSelection.value = [];
  taskTableRef.value?.clearSelection();
};
const exitApplyTransfer = () => {
  applyTransferMode.value = false;
  transferSelection.value = [];
  taskTableRef.value?.clearSelection();
};
const submitApplyTransfer = () => {
  const rows = transferSelection.value.filter(row => ownTransferFields(row).length > 0);
  if (!rows.length) {
    ElMessage.warning('请勾选本人负责的三级任务');
    return;
  }
  const skipped = transferSelection.value.length - rows.length;
  if (skipped > 0) ElMessage.info(`已忽略 ${skipped} 个本人不承担所选职责的任务`);
  taskTransferRequests.value?.openApplyMany(rows);
};
const loadTransferLogs = async (taskId) => {
  if (!canTransfer.value || !taskId) {
    transferLogs.value = [];
    return;
  }
  transferLogsLoading.value = true;
  try {
    const logs = await getTransferLogs('TASK', taskId);
    transferLogs.value = Array.isArray(logs) ? logs : [];
  } catch {
    transferLogs.value = [];
  } finally {
    transferLogsLoading.value = false;
  }
};
const handleTransferred = async () => {
  exitTransferMode();
  await refreshTasksSilently();
  await loadTransferLogs(currentTask.value?._raw?.taskId);
};
const handleTaskCreated = async taskId => {
  try {
    if (!expertOnly.value) await queryBar.value?.replace({year:String(filterYear.value||'2026')});
    await taskRenderPromise;
    if (!isCurrentSession()) return;
    const row = findTaskById(allTaskTreeData.value, taskId);
    if (!row) throw new Error('新任务暂不可见，请刷新列表');
    await handleViewDetail(row);
  } catch (error) {
    if (isCurrentSession()) ElMessage.warning(`任务已创建，${error.message || '列表刷新失败，请刷新后查看'}。任务ID：${taskId}`);
  }
};

// 展开动画：记录“刚展开出现”的子行（用 row-class-name 赋 class，避免 DOM 查询造成卡顿）
const animRowKeyMap = reactive({}); // rowKey -> true

const filterYear = ref('2026');
const filterStatus = ref('');
const taskKindFilter = ref('');
const taskAttributesDialog = ref(null);
const searchText = ref(''); // 搜索输入框的值
const actualSearchText = ref(''); // 实际用于搜索的值
const drawerVisible = ref(false);
const previewVisible = ref(false);
const currentTask = ref(null);
const detailContentRef = ref(null);
const fileList = ref([]);
const loading = ref(false);
const drawerLoading = ref(false);
// 是否需要定位到审核区域
const shouldScrollToAudit = ref(false);
// 展开状态：用户展开/收起后在刷新时保持
const expandedRowKeys = ref([]);
const expandedInitialized = ref(false);
const taskTableRef = ref(null);
const quickFilter = ref('all');
const auditStageFilter = ref('');
const auditDeptFilter = ref('');
const owningDeptFilter = ref('');
const leaderFilter = ref('');


const shortcutLoading = ref(false);
const shortcutError = ref('');
const taskShortcuts = ref({ returnedIds: [], todoIds: [] });

let shortcutRequest = 0;
const taskMemory = useBrowseMemory(isReview.value?queryMemoryKey.value:props.detailOnly?'expert-task-detail':'tasks', { fields: { year: filterYear, status: filterStatus, kind: taskKindFilter, search: actualSearchText, quick: quickFilter, leader: leaderFilter, auditStage: auditStageFilter, auditDept: auditDeptFilter, owningDept: owningDeptFilter },
  expanded: expandedRowKeys, table: taskTableRef, skip: () => !!route.query.taskId });
if (!route.query.taskId) searchText.value = actualSearchText.value;
const loadTaskShortcuts = async () => {
  const version = ++shortcutRequest;
  shortcutLoading.value = true;
  try {
    const result = await get('/biz/tasks/quick-filters');
    if (version !== shortcutRequest || !isCurrentSession()) return;
    taskShortcuts.value = result; shortcutError.value = '';
  } catch {
    if (version !== shortcutRequest || !isCurrentSession()) return;
    taskShortcuts.value = { returnedIds: [], todoIds: [] }; shortcutError.value = '快捷筛选加载失败';
  } finally {
    if (version === shortcutRequest && isCurrentSession()) { shortcutLoading.value = false;if(quickFilter.value==='todo'||quickFilter.value==='returned'||auditStageFilter.value||auditDeptFilter.value)applyFilters();else if(!loading.value)taskMemory.restoreTree(taskTreeData.value); }
  }
};
// 审核区域引用
const auditSectionRef = ref(null);

// --- 四级任务状态管理 ---
const forthTasks = ref([]);
const forthFeedbackData = reactive({});

// --- 辅助方法：获取四级子任务名称 ---
const getForthTaskName = (taskId) => {
  if (!taskId) return '未知子目标';
  const t = forthTasks.value.find(x => String(x.taskId) === String(taskId));
  return t ? (t.taskName || `子任务[${taskId}]`) : `子任务[${taskId}]`;
};

// 预览相关状态
const previewType = ref('');
const previewUrl = ref('');
const previewFileName = ref('');
const previewFileId = ref(null); // 当前预览的文件ID
const previewFileBlob = ref(null); // 当前预览的文件Blob（用于下载）
const selectedFileObjectUrl = ref('');

const feedbackForm = reactive({
  progress: 0,
  comment: '', // 完成情况（材料上传人可填写，50字内）
});

// 审核表单状态
const auditForm = reactive({
  result: 1, // 1=通过, 0=驳回
  comment: '',
});
const auditSubmitting = ref(false);
const feedbackSubmitting = ref(false);
const withdrawingTaskIds = reactive(new Set());
const auditLoading = ref(false);
const liveWorkflowError = ref('');
let liveWorkflowRefresh = null;
let detailVersion = 0;
let auditVersion = 0;
let detailDisposed = false;
const detailContext = () => ({ version: detailVersion, taskId: currentTask.value?._raw?.taskId });
const isCurrentDetail = (context) => !detailDisposed && drawerVisible.value &&
  context.version === detailVersion && context.taskId === currentTask.value?._raw?.taskId;
const clearDetailData = () => {
  liveWorkflowError.value = '';
  auditList.value = [];
  auditLogs.value = [];
  noticeLogs.value = [];
  forthTasks.value = [];
  Object.keys(forthFeedbackData).forEach(key => delete forthFeedbackData[key]);
  auditLoading.value = false;
};
watch(drawerVisible, visible => {
  if (!visible) {
    detailVersion++;
    auditVersion++;
    transferLogs.value = [];
    clearDetailData();
  }
}, { flush: 'sync' });

// 判断当前用户是否是审核人（当前审核人）
const canAudit = computed(() => {
  if (liveWorkflowError.value) return false;
  if (!canProcessTaskSubmission(latestAudit.value)) return false;
  const stage = Number(latestAudit.value?.flowStatus ?? latestAudit.value?.flow_status);
  if (isReview.value || props.detailOnly || !canUsePermission(stage === 30 ? 'tasks.archive' : 'tasks.audit')) return false;
  const auth = getCurrentAuthInfo();
  if (!auth || !latestAudit.value) return false;
  const currentHandlerId = latestAudit.value.currentHandlerId ?? latestAudit.value.current_handler_id;
  if (!currentHandlerId) return false;
  return isAdminTestEnabled() || Number(currentHandlerId) === Number(auth.userId);
});

// 当前标签页（根据路由判断）
const currentTab = computed(() => {
  const path = route.path;
  if (path.includes('/performance')) return 'performance';
  if (path.includes('/achievement')) return 'achievement';
  if (path.includes('/budget')) return 'budget';
  return 'task';
});

// 切换标签页
const handleTabSwitch = (tab) => {
  if(isReview.value)return;
  if (tab === 'task') {
    router.push('/home/works');
  } else if (tab === 'performance') {
    router.push('/home/works/performance');
  } else if (tab === 'achievement') {
    router.push('/home/works/achievement');
  } else if (tab === 'budget') {
    router.push('/home/works/budget');
  }
};

// 判断当前用户是否是任何审核人（包括当前审核人、历史审核人、未来审核人）
const isAnyAuditor = computed(() => {
  const auth = getCurrentAuthInfo();
  if (!auth) return false;
  const userId = Number(auth.userId);

  // 检查是否是管理员
  const userRole = auth.role;
  if (userRole === '0') return true;

  // 检查是否是任务的审核人（专业群审核人）
  const raw = currentTask.value?._raw;
  const auditorId = raw?.auditorId ?? raw?.auditor_id ?? raw?.auditId ?? raw?.audit_id ?? null;
  if (auditorId && Number(auditorId) === userId) return true;

  // 检查是否是任务的归口负责人
  const principalId = raw?.principalId ?? raw?.principal_id ?? null;
  if (principalId && Number(principalId) === userId) return true;

  // 检查是否是审批单的当前处理人
  if (latestAudit.value) {
    const currentHandlerId = latestAudit.value.currentHandlerId ?? latestAudit.value.current_handler_id;
    if (currentHandlerId && Number(currentHandlerId) === userId) return true;
  }

  // 检查是否是审批单历史中的处理人（遍历所有审批单）
  if (auditList.value && auditList.value.length > 0) {
    for (const audit of auditList.value) {
      const handlerId = audit.currentHandlerId ?? audit.current_handler_id;
      if (handlerId && Number(handlerId) === userId) return true;
    }
  }

  return false;
});

// 今天的日期（格式化）
const todayDate = computed(() => {
  const today = new Date();
  const year = today.getFullYear();
  const month = today.getMonth() + 1;
  const day = today.getDate();
  return `${year}年${month}月${day}日`;
});

// 年度选项：生成2025-2029年，确保包含当前年份


// --- 任务自动刷新（状态变化及时更新） ---
let tasksAutoRefreshTimer = null;

// 当前任务是否为“百分比”数据类型（后端：data_type=2）
const isPercentTask = computed(() => {
  const dt = currentTask.value?._raw?.dataType;
  return String(dt) === '2';
});

// 仅允许“任务负责人（leader_id）”提交材料
const canSubmitMaterial = computed(() => {
  if (isReview.value || props.detailOnly) return false;
  if (!canUsePermission('tasks.fill')) return false;
  const auth = getCurrentAuthInfo();
  if (!auth) return false;
  const leaderId = currentTask.value?._raw?.leaderId;
  if (isAdminTestEnabled()) return true;
  if (leaderId === undefined || leaderId === null) return false;
  return Number(leaderId) === Number(auth.userId);
});

// 百分比输入框滚轮增减（每次±1）
const handleProgressWheel = (e) => {
  if(isReview.value)return;
  if (!isPercentTask.value) return;
  e.preventDefault();
  const cur = Number(feedbackForm.progress);
  const base = Number.isFinite(cur) ? cur : 0;
  const next = base + (e.deltaY < 0 ? 1 : -1);
  feedbackForm.progress = Math.max(0, Math.min(100, Math.round(next)));
};

// --- 审批/上报信息（依赖后端接口 /biz/audit/{taskId}） ---
const auditList = ref([]);
// 待我审批的任务Map（taskId -> true）
const todoAuditTaskMap = ref(new Map());
// 操作日志（biz_audit_log）：需要后端提供查询接口
const auditLogs = ref([]);
// 兜底操作日志：用“当前用户收到的、与任务相关的通知”来展示时间线（/system/notice）
const noticeLogs = ref([]);
// 后端是否实现了 /biz/audit/logs/{subId}（未实现则只尝试一次，避免控制台刷屏）
const auditLogsApiEnabled = ref(true);

// 审批记录补全：每个审批单(subId)可展开查看对应的审批动作日志（来自 /biz/audit/logs/{subId}）
const auditItemLogVisible = reactive({}); // subId -> boolean
const auditItemLogLoading = reactive({}); // subId -> boolean
const auditItemLogList = reactive({}); // subId -> BizAuditLog[]

// --- 文件映射（用于显示文件名而不是文件ID） ---
const fileMap = ref(new Map()); // fileId -> fileName
const getFileName = (fileId) => {
  if (fileId === undefined || fileId === null) return '';
  const id = String(fileId);
  let fileName = fileMap.value.get(id);
  if (!fileName) return `文件ID: ${fileId}`;
  // 如果文件名包含URL编码，尝试解码
  if (fileName && fileName.includes('%') && !fileName.startsWith('文件ID:')) {
    try {
      fileName = decodeURIComponent(fileName);
    } catch (e) { // eslint-disable-line no-unused-vars
      // 解码失败，使用原始值
    }
  }
  return fileName;
};

// 从下载接口的响应头获取文件名
const fetchFileNameFromDownload = async (fileId) => {
  if(isReview.value)return fileMap.value.get(String(fileId))||null;
  if (!fileId) return null;
  const id = String(fileId);
  // 如果已经缓存了，直接返回
  if (fileMap.value.has(id)) {
    return fileMap.value.get(id);
  }

  try {
    const token = localStorage.getItem('token') || '';
    if (!token) return null;

    // 使用 HEAD 请求获取响应头（如果支持），否则使用 GET 但只读取响应头
    let resp;
    try {
      resp = await adminTestFetch(`/system/download/${fileId}`, {
        method: 'HEAD',
        headers: { Authorization: token }
      });
    } catch (e) { // eslint-disable-line no-unused-vars
      // HEAD 请求失败，尝试 GET 请求但只读取响应头
      resp = await adminTestFetch(`/system/download/${fileId}`, {
        method: 'GET',
        headers: { Authorization: token }
      });
      // 如果是 GET 请求，需要立即取消，只读取响应头
      if (resp.ok) {
        const cd = resp.headers.get('content-disposition') || '';
        const filename = parseFilenameFromContentDisposition(cd);
        if (filename) {
          fileMap.value.set(id, filename);
          return filename;
        }
      }
      return null;
    }

    if (resp.ok) {
      const cd = resp.headers.get('content-disposition') || '';
      const filename = parseFilenameFromContentDisposition(cd);
      if (filename) {
        fileMap.value.set(id, filename);
        return filename;
      }
    }
  } catch (e) { // eslint-disable-line no-unused-vars
    // 忽略错误，返回 null
  }
  return null;
};

const normalizeAuditItem = (it) => {
  if (!it || typeof it !== 'object') return null;
  return {
    subId: it.subId ?? it.sub_id ?? null,
    taskId: it.taskId ?? it.task_id ?? null,
    fileId: it.fileId ?? it.file_id ?? null,
    filename: it.filename ?? it.file_name ?? null, // 映射后端返回的文件名
    reportedValue: it.reportedValue ?? it.reported_value ?? null,
    dataType: it.dataType ?? it.data_type ?? null,
    submitBy: it.submitBy ?? it.submit_by ?? null,
    submitDeptId: it.submitDeptId ?? it.submit_dept_id ?? null,
    manageDeptId: it.manageDeptId ?? it.manage_dept_id ?? null,
    submitTime: it.submitTime ?? it.submit_time ?? null,
    fileSuffix: it.fileSuffix ?? it.file_suffix ?? null,
    flowStatus: it.flowStatus ?? it.flow_status ?? null,
    currentHandlerId: it.currentHandlerId ?? it.current_handler_id ?? null,
    isDelete: it.isDelete ?? it.is_delete ?? null,
    comment: it.comment ?? null, // 完成情况
    historicalDetailsAvailable: it.historicalDetailsAvailable ?? it.historical_details_available ?? null,
    subList: it.subList ?? it.sub_list ?? [], // 挂载四级任务的列表
    _raw: it,
  };
};

const getAuditSubId = (it) => (it ? (it.subId ?? it.sub_id ?? null) : null);
const getAuditSubKey = (it) => {
  const id = getAuditSubId(it);
  return id === undefined || id === null ? '' : String(id);
};

const toggleAuditItemLogs = async (item) => {
  const subId = getAuditSubId(item);
  if (!subId) return;
  const key = String(subId);
  auditItemLogVisible[key] = !auditItemLogVisible[key];
  if (!auditItemLogVisible[key]) return;

  // 已加载过就不重复拉
  if (Array.isArray(auditItemLogList[key]) && auditItemLogList[key].length > 0) return;
  if (!auditLogsApiEnabled.value) return;

  try {
    auditItemLogLoading[key] = true;
    const logs = isReview.value?auditLogs.value.filter(log=>Number(log.subId)===Number(subId)):await getAuditLogs(Number(subId));
    const arr = Array.isArray(logs) ? logs : [];
    arr.sort((a, b) => {
      const ta = a?.createTime ? new Date(a.createTime).getTime() : 0;
      const tb = b?.createTime ? new Date(b.createTime).getTime() : 0;
      return tb - ta;
    });
    auditItemLogList[key] = arr;
  } catch (e) {
    const msg = String(e?.message || '');
    if (msg.includes('No endpoint GET /biz/audit/logs') || msg.includes('/biz/audit/logs') || msg.includes('404')) {
      auditLogsApiEnabled.value = false;
    }
    auditItemLogList[key] = [];
  } finally {
    auditItemLogLoading[key] = false;
  }
};

// 部门信息缓存：deptId -> SysDept（含 leaderId）
const deptInfoCache = ref(new Map());

const ensureDeptInfo = async (deptId) => {
  if(isReview.value)return;
  if (deptId === undefined || deptId === null) return;
  const id = String(deptId);
  if (deptInfoCache.value.has(id)) return;
  try {
    const dept = await getDeptById(Number(deptId));
    if (dept && typeof dept === 'object') {
      deptInfoCache.value.set(id, dept);
    }
  } catch (e) { // eslint-disable-line no-unused-vars
    // 静默失败，交给兜底逻辑显示 —
  }
};

const formatDateTime = (val) => {
  if (!val) return '';
  try {
    return new Date(val).toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    }).replace(/\//g, '-');
  } catch (e) { // eslint-disable-line no-unused-vars
    return String(val);
  }
};

const getFlowStatusDesc = (status) => {
  const s = Number(status);
  if (s === 40) return '已归档/通过';
  if (s === 30) return '待双高办归档';
  if (s === 20) return '待归口审核';
  if (s === 10) return '待专业群审核';
  if (s < 0) return '已退回';
  return '历史状态未记录';
};

const latestAudit = computed(() => currentTaskSubmission(auditList.value));

// --- 审核人显示（方案一：通过 /system/dept/{deptId} 获取 leaderId，不再用 role=2 推断） ---
const findDeptLeaderName = (deptId) => { // eslint-disable-line no-unused-vars
  if (deptId === undefined || deptId === null) return '';
  const dept = deptInfoCache.value.get(String(deptId));
  const leaderId = dept?.leaderId ?? dept?.leader_id;
  return leaderId ? getUserName(leaderId) : '';
};

const findAdminName = () => {
  const users = Array.isArray(allUsers.value) ? allUsers.value : [];
  // 优先后端常用管理员ID（若存在）
  const hardcoded = users.find(u => Number(u?.userId) === 110228);
  if (hardcoded) return hardcoded.nickName || hardcoded.userName || getUserName(hardcoded.userId);
  const admin = users.find(u => String(u?.role) === '0');
  return admin ? (admin.nickName || admin.userName || getUserName(admin.userId)) : '';
};

const stepApprover = computed(() => {
  const raw = currentTask.value?._raw;
  // 提交人：对应任务的负责人（leader_id / leaderId）
  const leaderId = raw?.leaderId ?? raw?.leader_id ?? null;
  // 专业群审核人：对应任务的审核人（auditor_id / auditorId；兼容旧字段 auditId）
  // 后端已返回 auditorName：如果 auditorId 不为 null 则返回实际姓名，如果为 null 则返回 "无"
  const auditorName = raw?.auditorName ?? raw?.auditName ?? null;
  const auditorId = raw?.auditorId ?? raw?.auditor_id ?? raw?.auditId ?? raw?.audit_id ?? null;
  const principalId = raw?.principalId ?? raw?.principal_id ?? null;

  // 处理审核人姓名：如果后端返回 "无" 或空字符串，则显示为空（步骤条会显示 "—"）
  let deptLeaderName = '';
  if (auditorName && auditorName !== '无') {
    deptLeaderName = auditorName;
  } else if (!auditorName && auditorId) {
    // 兜底：如果后端未返回 auditorName 但有 auditorId，则通过用户映射获取
    deptLeaderName = getUserName(auditorId);
  }

  return {
    // 后端可能因为空指针异常导致某些字段为 null，前端需要做兜底处理
    submitterName: (raw?.leaderName && raw.leaderName.trim() !== '') ? raw.leaderName : (leaderId ? getUserName(leaderId) : ''),
    deptLeaderName: deptLeaderName,
    principalName: (raw?.principalName && raw.principalName.trim() !== '') ? raw.principalName : (principalId ? getUserName(principalId) : ''),
    adminName: findAdminName(),
  };
});

const stepActive = computed(() => {
  // 如果有 latestAudit，优先使用审批单的状态
  if (latestAudit.value) {
    const s = Number(latestAudit.value.flowStatus ?? latestAudit.value.flow_status);
    if (Number.isFinite(s)) return taskWorkflowStep(latestAudit.value, auditLogs.value);
  }

  // 如果没有 latestAudit，但任务状态是"审核中"（status === '2'），说明材料已提交但审批单可能未加载到
  // 此时应该显示步骤条，根据任务状态推断当前步骤
  const rawStatus = currentTask.value?._raw?.status !== undefined && currentTask.value?._raw?.status !== null
    ? String(currentTask.value._raw.status)
    : '';

  if (rawStatus === '2') {
    // 审核中：至少已提交，默认停在"专业群审核"（步骤2）
    // 但为了更准确，可以尝试从任务的其他信息推断，这里先返回2
    return 2;
  }
  if (rawStatus === '3') {
    // 已完成
    return 4;
  }

  // 如果任务状态不是"审核中"或"已完成"，且没有 latestAudit，说明材料未提交
  // 返回 0 表示所有步骤都不激活，材料提交步骤不会亮绿色
  return 0;
});

const stepDesc = computed(() => {
  const submission = latestAudit.value;
  const configured = stepApprover.value;
  if (!submission) return {
    submit: `提交人：${configured.submitterName || '—'}`,
    dept: `审核人：${configured.deptLeaderName || '—'}`,
    principal: `审核人：${configured.principalName || '—'}（待流转）`,
    admin: `审核人：${configured.adminName || '—'}（待流转）`,
  };
  const describe = (stage, name) => taskStageDescription(submission, auditLogs.value, stage, name, getUserName, formatDateTime);
  const submitter = submission.submitBy ?? submission.submit_by;
  return {
    submit: `提交人：${submitter ? getUserName(submitter) : configured.submitterName || '—'}（${formatDateTime(submission.submitTime ?? submission.submit_time) || '—'}）`,
    dept: describe('dept', configured.deptLeaderName),
    principal: describe('principal', configured.principalName),
    admin: describe('admin', configured.adminName),
  };
});

const toStr = (v) => (v === undefined || v === null ? '' : String(v));

/**
 * 兜底：用“通知”拼一个可读的操作日志
 * 注意：/system/notice 只返回“当前用户收到的通知”，因此只是一种前端兜底视角。
 */
const loadNoticeLogsForTask = async (taskId, isCurrent) => {
  if (!taskId) {
    if (isCurrent()) noticeLogs.value = [];
    return;
  }
  try {
    const list = await getNotices();
    if (!isCurrent()) return;
    const arr = Array.isArray(list) ? list : [];
    const filtered = arr
      .filter(n => toStr(n.sourceId ?? n.source_id) === toStr(taskId))
      .map(n => ({
        _type: 'notice',
        id: n.noticeId ?? n.notice_id,
        operatorId: n.fromUserId ?? n.from_user_id,
        actionType: n.title || n.triggerEvent || n.trigger_event || '通知',
        comment: n.content || '',
        createTime: n.createTime || n.create_time || '',
        raw: n
      }))
      .sort((a, b) => {
        const ta = a.createTime ? new Date(a.createTime).getTime() : 0;
        const tb = b.createTime ? new Date(b.createTime).getTime() : 0;
        return tb - ta;
      });
    noticeLogs.value = filtered;
  } catch (e) { // eslint-disable-line no-unused-vars
    if (isCurrent()) noticeLogs.value = [];
  }
};

// --- 任务树数据 ---
const allTaskTreeData = ref([]); // 保存所有原始数据
const taskTreeData = ref([]); // 显示的数据（经过筛选）

// 按当前筛选后的树顺序浏览三级任务，不受树节点展开/收起影响。
const detailTasks = computed(() => {
  const rows = [];
  const visit = nodes => nodes.forEach(node => {
    if (Number(node.level) === 3) rows.push(node);
    if (node.children?.length) visit(node.children);
  });
  visit(taskTreeData.value);
  return rows;
});
const detailTaskIndex = computed(() => detailTasks.value.findIndex(row =>
  String(row._raw?.taskId) === String(currentTask.value?._raw?.taskId)));
const detailSwitching = ref(false);
const detailOperationBusy = computed(() => expertReviewRef.value?.busy || loading.value || drawerLoading.value || auditLoading.value ||
  feedbackSubmitting.value || auditSubmitting.value || withdrawingTaskIds.size > 0);
const detailNavigationBusy = computed(() => detailSwitching.value || detailOperationBusy.value);
const detailDraft = () => JSON.stringify({ feedback: feedbackForm, audit: auditForm, forth: forthFeedbackData });
let savedDetailDraft = '';
const rememberDetailDraft = async (context) => {
  await nextTick();
  if (isCurrentDetail(context)) savedDetailDraft = detailDraft();
};
const navigateDetail = async direction => {
  if (detailNavigationBusy.value || detailTaskIndex.value < 0) return;
  const target = detailTasks.value[detailTaskIndex.value + direction];
  if (!target) return;
  const context = detailContext();
  detailSwitching.value = true;
  try {
    if (!await expertReviewRef.value?.confirmLeave() && expertReviewRef.value) return;
    if (!isReview.value&&(fileList.value.some(file => file.raw) || (savedDetailDraft && detailDraft() !== savedDetailDraft))) {
      await ElMessageBox.confirm('当前任务有未提交的内容，切换将放弃这些内容。是否继续？', '切换任务', {
        confirmButtonText: '放弃并切换', cancelButtonText: '继续编辑', type: 'warning',
      });
    }
    if (!isCurrentDetail(context) || detailOperationBusy.value) return;
    const row = detailTasks.value.find(item => String(item._raw?.taskId) === String(target._raw?.taskId));
    if (!row) return;
    previewVisible.value = false;
    shouldScrollToAudit.value = false;
    await handleViewDetail(row, true);
    await nextTick();
    if (drawerVisible.value && !detailDisposed) {
      detailContentRef.value?.closest('.el-drawer__body')?.scrollTo({ top: 0 });
    }
  } catch (e) {
    if (e !== 'cancel' && e !== 'close' && !detailDisposed) ElMessage.error('切换任务失败，请稍后重试');
  } finally {
    detailSwitching.value = false;
  }
};

const taskDetailLink = computed(() => `${window.location.origin}${router.resolve({ path: '/home/works', query: {
  ...(isReview.value?{reviewRoundId:String(props.workspace.roundId),...(props.workspace.kind==='reviewHistory'?{reviewView:'history'}:{})}:{}),
  taskId: String(currentTask.value?._raw?.taskId || ''), year: String(currentTask.value?._raw?.phase || '')
} }).href}`);
const locateCurrentTask = async () => {
  const context = detailContext();
  if (expertReviewRef.value && !await expertReviewRef.value.confirmLeave()) return;
  if (!isReview.value && !await confirmDetailLeave(fileList.value.some(file => file.raw) || (savedDetailDraft && detailDraft() !== savedDetailDraft))) return;
  if (!isCurrentDetail(context) || detailOperationBusy.value) return;
  await refreshTasksSilently();
  if (!findTaskById(allTaskTreeData.value, context.taskId)) { try { await source.value.loadDetail(context.taskId); } catch { ElMessage.info('记录已删除或无权访问，无法定位'); return; } }
  await locateDetailRow({ tree: () => taskTreeData.value,
    matches: row => String(row._raw?.taskId) === String(context.taskId),
    reset: async () => { filterYear.value = ''; filterStatus.value = ''; taskKindFilter.value = ''; quickFilter.value = 'all'; leaderFilter.value = ''; auditStageFilter.value = ''; auditDeptFilter.value = ''; owningDeptFilter.value = ''; searchText.value = ''; actualSearchText.value = ''; await queryBar.value?.replace({}); applyFilters(); },
    expanded: expandedRowKeys, table: taskTableRef, close: () => { drawerVisible.value = false; }, valid: () => isCurrentDetail(context) });
};

// 收集筛选结果中的三级任务（统计口径统一走这里，避免计数与汇总两套遍历）
const collectLevel3 = (nodes) => {
  const result = [];
  const walk = (list) => {
    if (!Array.isArray(list)) return;
    list.forEach(it => {
      if (it && Number(it.level) === 3) result.push(it);
      if (Array.isArray(it?.children) && it.children.length > 0) walk(it.children);
    });
  };
  walk(nodes);
  return result;
};

// 当前筛选结果下的“条目数量”（仅统计三级任务）
const visibleNodeCount = computed(() => collectLevel3(taskTreeData.value).length);

// 本地日期拼 YYYY-MM-DD，避免时区解析歧义
const localTodayKey = () => {
  const now = new Date();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${now.getFullYear()}-${month}-${day}`;
};

// 严格逾期：截止时间为任务所属年度年末，已过截止且未完成（status 原始值 3 为已完成）
const isTaskOverdue = (raw, todayKey) => {
  const y = raw?.phase !== undefined && raw?.phase !== null ? Number(raw.phase) : NaN;
  if (!Number.isFinite(y) || y <= 1900) return false;
  if (String(raw?.status) === '3') return false;
  return todayKey > `${y}-12-31`;
};

// 统计带：全部取自当前筛选后的三级任务，随筛选栏自动联动
const taskStats = computed(() => {
  const tasks = collectLevel3(taskTreeData.value);
  const total = tasks.length;
  const sum = tasks.reduce((acc, t) => acc + (Number.isFinite(Number(t.progress)) ? Number(t.progress) : 0), 0);
  const completed = tasks.filter(t => t.status === '已完成').length;
  const reviewing = tasks.filter(t => t.status === '审核中').length;
  const today = localTodayKey();
  const overdue = tasks.filter(t => isTaskOverdue(t._raw, today)).length;
  // 快捷筛选未就绪（加载中或加载失败）时不把缺失显示成 0
  const shortcutsReady = !shortcutLoading.value && !shortcutError.value;
  const todoIds = new Set((taskShortcuts.value.todoIds || []).map(String));
  const returnedIds = new Set((taskShortcuts.value.returnedIds || []).map(String));
  return {
    total,
    avgProgress: total === 0 ? null : Math.round(sum / total),
    completed,
    completedRate: total === 0 ? null : Math.round((completed * 1000) / total) / 10,
    reviewing,
    overdue,
    todo: shortcutsReady ? tasks.filter(t => todoIds.has(String(t.id))).length : null,
    returned: shortcutsReady ? tasks.filter(t => returnedIds.has(String(t.id))).length : null
  };
});

// 统计卡反向筛选：清空与之冲突的条件，保证“共 N 条”等于卡片数字
const applyTaskStatFilter = async (key) => {
  const current=queryBar.value?.applied||{};
  const field=key==='done'||key==='reviewing'?'status':key==='overdue'?'overdue':'scope';
  const value=key==='done'?'3':key==='reviewing'?'2':key==='overdue'?'yes':key;
  await queryBar.value?.patch({[field]:current[field]===value?'':value});
  taskMemory.resetPosition();
};

// 导出当前筛选结果（含一/二/三级行，权限由后端二次校验）
const exporting = ref(false);
const flattenTaskTree = (nodes) => {
  const result = [];
  const walk = (list) => {
    if (!Array.isArray(list)) return;
    list.forEach(node => {
      result.push(node);
      if (Array.isArray(node?.children) && node.children.length > 0) walk(node.children);
    });
  };
  walk(nodes);
  return result;
};
const exportCurrentTasks = async () => {
  if (exporting.value) return;
  const ids = [...new Set(flattenTaskTree(taskTreeData.value)
    .map(row => Number(row?._raw?.taskId))
    .filter(id => Number.isFinite(id) && id > 0))];
  if (ids.length === 0) {
    ElMessage.warning('当前没有可导出的任务');
    return;
  }
  exporting.value = true;
  try {
    const { blob, filename } = await exportTasks(ids, {filters:queryBar.value?.applied||{}});
    const url = URL.createObjectURL(blob);
    try {
      const link = document.createElement('a');
      link.href = url;
      link.download = filename;
      document.body.appendChild(link);
      link.click();
      link.remove();
    } finally {
      URL.revokeObjectURL(url);
    }
    ElMessage.success(`已导出 ${ids.length} 条任务`);
  } catch (e) {
    ElMessage.error(e?.message || '导出失败，请稍后重试');
  } finally {
    exporting.value = false;
  }
};

// --- 用户和部门映射 ---
const userMap = ref(new Map()); // userId -> nickName
const deptMap = ref(new Map()); // deptId -> deptName
const allUsers = ref([]); // 缓存全量用户，用于从token的userId反查deptId等

// --- 简单缓存：提升进入工作中心首屏速度（先渲染缓存，再静默刷新） ---
const worksCache = createWorksCache();
const isCurrentSession = () => worksCache.isCurrent() && !detailDisposed;
const USERS_CACHE_KEY = 'works_users_cache_v1';
const USERS_CACHE_TIME_KEY = 'works_users_cache_time_v1';
const DEPTS_CACHE_KEY = 'works_depts_cache_v1';
const DEPTS_CACHE_TIME_KEY = 'works_depts_cache_time_v1';
const EXPANDED_KEYS_CACHE_KEY = 'works_expanded_keys_v1'+(isReview.value?':'+queryMemoryKey.value:'');
const CACHE_TTL_MS = 60 * 1000; // 60s 内认为可用

const readJsonCache = (key) => {
  try {
    const raw = worksCache.getItem(key);
    if (!raw) return null;
    return JSON.parse(raw);
  } catch (e) { // eslint-disable-line no-unused-vars
    return null;
  }
};

const writeJsonCache = (key, val) => {
  try {
    worksCache.setItem(key, JSON.stringify(val));
  } catch (e) { // eslint-disable-line no-unused-vars
      // 忽略错误
    }
};

const isCacheFresh = (timeKey) => {
  try {
    const t = Number(worksCache.getItem(timeKey));
    if (!Number.isFinite(t) || t <= 0) return false;
    return Date.now() - t <= CACHE_TTL_MS;
  } catch (e) { // eslint-disable-line no-unused-vars
    return false;
  }
};

const collectTreeIds = (nodes, set) => {
  if (!Array.isArray(nodes)) return;
  nodes.forEach(n => {
    if (!n) return;
    set.add(String(n.id));
    if (Array.isArray(n.children) && n.children.length > 0) collectTreeIds(n.children, set);
  });
};

const countDescendants = (node) => {
  if (!node || !Array.isArray(node.children) || node.children.length === 0) return 0;
  let cnt = 0;
  const stack = [...node.children];
  while (stack.length) {
    const n = stack.pop();
    if (!n) continue;
    cnt += 1;
    if (Array.isArray(n.children) && n.children.length) {
      for (let i = 0; i < n.children.length; i++) stack.push(n.children[i]);
    }
  }
  return cnt;
};

const markChildrenAnimating = (row) => {
  try {
    const children = Array.isArray(row?.children) ? row.children : [];
    if (!children.length) return;
    // 只动画“刚出现的一层子节点”，并限制数量，避免大数据卡顿
    const ids = children
      .map(c => (c?.id !== undefined && c?.id !== null ? String(c.id) : ''))
      .filter(Boolean)
      .slice(0, 60);
    if (!ids.length) return;

    ids.forEach(id => {
      animRowKeyMap[id] = true;
      window.setTimeout(() => {
        try { delete animRowKeyMap[id]; } catch (e) { // eslint-disable-line no-unused-vars
          // 忽略删除错误
        }
      }, 220);
    });
  } catch (e) { // eslint-disable-line no-unused-vars
    // ignore
  }
};

const rowClassName = ({ row }) => {
  const id = row?.id !== undefined && row?.id !== null ? String(row.id) : '';
  return id && animRowKeyMap[id] ? 'row-expand-anim' : '';
};

const handleExpandChange = (row, expanded) => {
  const id = row?.id !== undefined && row?.id !== null ? String(row.id) : '';
  if (!id) return;
  const cur = new Set(Array.isArray(expandedRowKeys.value) ? expandedRowKeys.value.map(String) : []);
  if (expanded) cur.add(id);
  else cur.delete(id);
  expandedRowKeys.value = Array.from(cur);
  expandedInitialized.value = true;

  // 保存展开状态到 sessionStorage，以便切换页面后恢复
  try {
    worksCache.setItem(EXPANDED_KEYS_CACHE_KEY, JSON.stringify(Array.from(cur)));
  } catch (e) { // eslint-disable-line no-unused-vars
    // ignore
  }

  if (expanded) markChildrenAnimating(row);
};

// 点击整行/名称即可展开（无需点小箭头）；点击按钮等交互控件不触发
const isRowToggleBlocked = (evt) => {
  const el = evt?.target;
  if (!el || typeof el.closest !== 'function') return false;
  return !!el.closest('button, a, .el-button, .el-dropdown, .el-input, .el-select, .el-switch, .el-checkbox, .el-radio, .preview-text');
};

const toggleRowExpand = (row) => {
  if (!row || !Array.isArray(row.children) || row.children.length === 0) return;
  const id = row?.id !== undefined && row?.id !== null ? String(row.id) : '';
  if (!id) return;
  const cur = new Set(Array.isArray(expandedRowKeys.value) ? expandedRowKeys.value.map(String) : []);
  const expanded = cur.has(id);
  handleExpandChange(row, !expanded);
};

const handleRowClick = (row, _column, event) => {
  if (isRowToggleBlocked(event)) return;
  // 三级任务（最后一级）：点击打开任务详情抽屉
  if (row?.level === 3) {
    handleViewDetail(row);
    return;
  }
  // 一级、二级任务：点击展开/收起
  toggleRowExpand(row);
};

/**
 * 解析JWT（仅用于前端读payload做筛选；不做安全校验）
 */
const parseJwtPayload = (token) => {
  try {
    if (!token || typeof token !== 'string') return null;
    const parts = token.split('.');
    if (parts.length < 2) return null;
    let payload = parts[1];
    // base64url -> base64
    payload = payload.replace(/-/g, '+').replace(/_/g, '/');
    // pad
    const pad = payload.length % 4;
    if (pad) payload += '='.repeat(4 - pad);
    const json = atob(payload);
    return JSON.parse(json);
  } catch (e) { // eslint-disable-line no-unused-vars
    return null;
  }
};

/**
 * 从localStorage token中获取当前用户信息（userId/role）
 * token由后端JWTUtil生成：claim包含 id / role
 */
const getCurrentAuthInfo = () => {
  const token = localStorage.getItem('token') || '';
  const payload = parseJwtPayload(token);
  if (!payload) return null;
  const userId = payload.id !== undefined ? Number(payload.id) : null;
  const role = accessState.token === token ? accessState.role : payload.role !== undefined ? String(payload.role) : '';
  if (!userId || Number.isNaN(userId)) return null;
  return { userId, role };
};

/**
 * 按角色过滤任务，并补齐父节点，保证树结构完整
 * 角色约定（来自后端sys_user.role注释）：
 * - 0: admin
 * - 1: user
 * - 2: leader（部门负责人）
 */
const filterTasksByRole = (tasks, auth) => { // eslint-disable-line no-unused-vars
  if (!Array.isArray(tasks) || tasks.length === 0) return [];
  if (!auth) return [];

  const { userId, role } = auth;
  if (role === '0') return tasks; // 管理员看全部

  // 从用户列表中反查当前用户deptId（token里没有deptId）
  const me = Array.isArray(allUsers.value) ? allUsers.value.find(u => Number(u.userId) === Number(userId)) : null;
  const myDeptId = me?.deptId !== undefined && me?.deptId !== null ? Number(me.deptId) : null;

  // 命中条件：可按你们的业务口径调整这里
  const isMine = (t) => {
    const principalId = t?.principalId !== undefined ? Number(t.principalId) : null; // 归口负责人
    const leaderId = t?.leaderId !== undefined ? Number(t.leaderId) : null; // 任务负责人
    const deptId = t?.deptId !== undefined ? Number(t.deptId) : null; // 归口部门

    if (role === '2') {
      // 部门负责人：默认看本部门任务 + 自己是归口负责人的任务
      return (myDeptId !== null && deptId === myDeptId) || (principalId !== null && principalId === userId);
    }
    // 普通用户：默认看自己是任务负责人的任务（兼容部分任务未填leaderId时也按principalId匹配）
    return (leaderId !== null && leaderId === userId) || (principalId !== null && principalId === userId);
  };

  const taskMap = new Map();
  tasks.forEach(t => {
    if (t?.taskId !== undefined && t?.taskId !== null) taskMap.set(String(t.taskId), t);
  });

  const keepIds = new Set();
  const matched = tasks.filter(isMine);
  matched.forEach(t => keepIds.add(String(t.taskId)));

  // 补齐父节点链路
  const addParents = (t) => {
    let parentId = t?.parentId !== undefined && t?.parentId !== null ? String(t.parentId) : null;
    while (parentId && parentId !== '0' && !keepIds.has(parentId)) {
      keepIds.add(parentId);
      const parent = taskMap.get(parentId);
      if (!parent) break;
      parentId = parent?.parentId !== undefined && parent?.parentId !== null ? String(parent.parentId) : null;
    }
  };
  matched.forEach(addParents);

  return tasks.filter(t => keepIds.has(String(t.taskId)));
};

/**
 * 将后端状态码转换为前端状态文字
 */
const mapStatus = (status) => {
  const statusMap = {
    '0': '未开始',
    '1': '进行中',
    '2': '审核中',
    '3': '已完成'
  };
  return statusMap[status] || '未开始';
};

/**
 * 格式化日期（从Date对象或字符串转为YYYY-MM-DD）
 */
const formatDate = (date) => {
  if (!date) return '';
  const d = new Date(date);
  if (isNaN(d.getTime())) return '';
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

/**
 * 根据任务phase（年份）计算起止时间：默认 01-01 ~ 12-31
 */
const getPhaseDateRange = (task) => {
  // 一级/二级任务：统一展示项目周期 2025-01-01 ~ 2029-12-31
  const lvl = task?.level !== undefined && task?.level !== null ? Number(task.level) : NaN;
  if (Number.isFinite(lvl) && (lvl === 1 || lvl === 2)) {
    return {
      startDate: '2025-01-01',
      endDate: '2029-12-31',
    };
  }

  const y = task?.phase !== undefined && task?.phase !== null ? Number(task.phase) : NaN;
  if (Number.isFinite(y) && y > 1900) {
    return {
      startDate: `${y}-01-01`,
      endDate: `${y}-12-31`,
    };
  }
  return null;
};

/**
 * 加载用户和部门数据，建立映射关系
 */
const loadUserAndDeptMaps = async (tasks = []) => {
  if(isReview.value)return;
  try {
    // 优先读缓存，减少首次进入等待
    let users = null;
    if (isCacheFresh(USERS_CACHE_TIME_KEY)) {
      const cached = readJsonCache(USERS_CACHE_KEY);
      if (Array.isArray(cached)) users = cached;
    }
    if (!users) {
      try {
        users = await getAllUsers();
        if (Array.isArray(users)) {
          writeJsonCache(USERS_CACHE_KEY, users);
          worksCache.setItem(USERS_CACHE_TIME_KEY, String(Date.now()));
        }
      } catch (e) {
        // 如果获取用户列表失败（可能是权限不足），使用空数组，后续会使用缓存或降级处理
        // 403 权限错误是预期的（普通用户无法访问 /system/allUsers），不打印警告
        if (e?.isPermissionError || (e?.message && e.message.includes('无权限'))) {
          // 静默处理，不打印日志
        } else {
          console.warn('获取用户列表失败，使用缓存或降级处理:', e);
        }
        users = [];
      }
    }
    allUsers.value = Array.isArray(users) ? users : [];
    const newUserMap = new Map();
    const newDeptMap = new Map();
    // 部门名缓存：命中时不再逐个请求 /system/dept/{id}
    if (isCacheFresh(DEPTS_CACHE_TIME_KEY)) {
      const cachedDepts = readJsonCache(DEPTS_CACHE_KEY);
      if (cachedDepts && typeof cachedDepts === 'object') {
        Object.entries(cachedDepts).forEach(([id, name]) => {
          if (name) newDeptMap.set(String(id), String(name));
        });
      }
    }

    // 建立用户ID到用户名的映射
    // 兼容字段名：userId/user_id, nickName/nick_name, userName/user_name
    // 处理编码问题：确保用户名正确显示，避免乱码
    (Array.isArray(users) ? users : []).forEach(user => {
      const uid = user?.userId ?? user?.user_id;
      if (uid !== undefined && uid !== null) {
        let name = user?.nickName ?? user?.nick_name ?? user?.userName ?? user?.user_name;
        if (name) {
          // 处理可能的编码问题：移除BOM标记，但不强制重新编码（避免破坏正常的中文字符）
          try {
            if (typeof name === 'string') {
              // 移除可能的BOM标记
              name = name.replace(/^\uFEFF/, '');
              // 只处理明显的乱码情况，不强制重新编码正常的中文字符串
              // 检查是否包含乱码字符（如 或其他无效字符）
              if (name.includes('\uFFFD') || /[\uFFFD]/.test(name)) {
                // 如果包含乱码字符，尝试修复
                try {
                  name = decodeURIComponent(encodeURIComponent(name));
                } catch (e) { // eslint-disable-line no-unused-vars
                  // 编码修复失败，使用原始值
                }
              }
            }
          } catch (e) {
            // 如果编码修复失败，使用原始值
            console.warn('用户名编码处理失败:', e);
          }
          if (name && name.trim() !== '') {
            newUserMap.set(String(uid), name);
          }
        }
      }
    });

    // 从任务数据中提取用户信息，补充到用户映射中
    // 任务数据中可能包含 leaderName, principalName, auditorName 等信息
    // 后端可能因为空指针异常导致某些字段为 null，前端需要做兜底处理
    // 重要：一级二级任务可能没有 leaderName，需要确保所有级别的任务都能正确显示负责人
    tasks.forEach(task => {
      if (task) {
        // 任务负责人（所有级别的任务都需要处理）
        const leaderId = task?.leaderId ?? task?.leader_id;
        let leaderName = task?.leaderName ?? task?.leader_name;
        if (leaderId) {
          // 如果任务数据中有 leaderName，使用它（并处理编码）
          if (leaderName && leaderName.trim() !== '') {
            try {
              // 移除BOM标记
              leaderName = leaderName.replace(/^\uFEFF/, '');
              // 只处理明显的乱码情况
              if (leaderName.includes('\uFFFD') || /[\uFFFD]/.test(leaderName)) {
                try {
                  leaderName = decodeURIComponent(encodeURIComponent(leaderName));
                } catch (e) { // eslint-disable-line no-unused-vars
                  // 编码修复失败，使用原始值
                }
              }
            } catch (e) { // eslint-disable-line no-unused-vars
              // 编码处理失败，使用原始值
            }
            if (leaderName && leaderName.trim() !== '') {
              newUserMap.set(String(leaderId), leaderName);
            }
          }
          // 如果任务数据中没有 leaderName，但用户映射中也没有，尝试从用户列表中查找
          // 这确保一级二级任务也能显示负责人姓名
          // 注意：即使 allUsers 为空（403错误），也要尝试从缓存中查找
          if (!newUserMap.has(String(leaderId))) {
            // 先从 allUsers 中查找
            let user = allUsers.value.find(u => {
              const uid = u?.userId ?? u?.user_id;
              return uid !== undefined && uid !== null && Number(uid) === Number(leaderId);
            });
            // 如果 allUsers 为空，尝试从缓存中查找
            if (!user) {
              try {
                const cachedUsers = readJsonCache(USERS_CACHE_KEY);
                if (Array.isArray(cachedUsers)) {
                  user = cachedUsers.find(u => {
                    const uid = u?.userId ?? u?.user_id;
                    return uid !== undefined && uid !== null && Number(uid) === Number(leaderId);
                  });
                }
              } catch (e) { // eslint-disable-line no-unused-vars
                // 忽略缓存读取错误
              }
            }
            if (user) {
              let userName = user?.nickName ?? user?.nick_name ?? user?.userName ?? user?.user_name;
              if (userName) {
                try {
                  // 移除BOM标记
                  userName = userName.replace(/^\uFEFF/, '');
                  // 只处理明显的乱码情况
                  if (userName.includes('\uFFFD') || /[\uFFFD]/.test(userName)) {
                    try {
                      userName = decodeURIComponent(encodeURIComponent(userName));
                    } catch (e) { // eslint-disable-line no-unused-vars
                      // 编码修复失败，使用原始值
                    }
                  }
                } catch (e) { // eslint-disable-line no-unused-vars
                  // 编码处理失败，使用原始值
                }
                if (userName && userName.trim() !== '') {
                  newUserMap.set(String(leaderId), userName);
                }
              }
            }
          }
        }
        // 归口负责人
        const principalId = task?.principalId ?? task?.principal_id;
        let principalName = task?.principalName ?? task?.principal_name;
        if (principalId) {
          if (principalName && principalName.trim() !== '') {
            try {
              // 移除BOM标记
              principalName = principalName.replace(/^\uFEFF/, '');
              // 只处理明显的乱码情况
              if (principalName.includes('\uFFFD') || /[\uFFFD]/.test(principalName)) {
                try {
                  principalName = decodeURIComponent(encodeURIComponent(principalName));
                } catch (e) { // eslint-disable-line no-unused-vars
                  // 编码修复失败，使用原始值
                }
              }
            } catch (e) { // eslint-disable-line no-unused-vars
              // 编码处理失败，使用原始值
            }
            if (principalName && principalName.trim() !== '') {
              newUserMap.set(String(principalId), principalName);
            }
          }
          // 如果任务数据中没有 principalName，尝试从用户列表中查找
          // 注意：即使 allUsers 为空（403错误），也要尝试从缓存中查找
          if (!newUserMap.has(String(principalId))) {
            // 先从 allUsers 中查找
            let user = allUsers.value.find(u => {
              const uid = u?.userId ?? u?.user_id;
              return uid !== undefined && uid !== null && Number(uid) === Number(principalId);
            });
            // 如果 allUsers 为空，尝试从缓存中查找
            if (!user) {
              try {
                const cachedUsers = readJsonCache(USERS_CACHE_KEY);
                if (Array.isArray(cachedUsers)) {
                  user = cachedUsers.find(u => {
                    const uid = u?.userId ?? u?.user_id;
                    return uid !== undefined && uid !== null && Number(uid) === Number(principalId);
                  });
                }
              } catch (e) { // eslint-disable-line no-unused-vars
                // 忽略缓存读取错误
              }
            }
            if (user) {
              let userName = user?.nickName ?? user?.nick_name ?? user?.userName ?? user?.user_name;
              if (userName) {
                try {
                  // 移除BOM标记
                  userName = userName.replace(/^\uFEFF/, '');
                  // 只处理明显的乱码情况
                  if (userName.includes('\uFFFD') || /[\uFFFD]/.test(userName)) {
                    try {
                      userName = decodeURIComponent(encodeURIComponent(userName));
                    } catch (e) { // eslint-disable-line no-unused-vars
                      // 编码修复失败，使用原始值
                    }
                  }
                } catch (e) { // eslint-disable-line no-unused-vars
                  // 编码处理失败，使用原始值
                }
                if (userName && userName.trim() !== '') {
                  newUserMap.set(String(principalId), userName);
                }
              }
            }
          }
        }
        // 审核人
        const auditorId = task?.auditorId ?? task?.auditor_id ?? task?.auditId ?? task?.audit_id;
        let auditorName = task?.auditorName ?? task?.auditName;
        if (auditorId) {
          if (auditorName && auditorName.trim() !== '' && auditorName !== '无') {
            try {
              // 移除BOM标记
              auditorName = auditorName.replace(/^\uFEFF/, '');
              // 只处理明显的乱码情况
              if (auditorName.includes('\uFFFD') || /[\uFFFD]/.test(auditorName)) {
                try {
                  auditorName = decodeURIComponent(encodeURIComponent(auditorName));
                } catch (e) { // eslint-disable-line no-unused-vars
                  // 编码修复失败，使用原始值
                }
              }
            } catch (e) { // eslint-disable-line no-unused-vars
              // 编码处理失败，使用原始值
            }
            if (auditorName && auditorName.trim() !== '' && auditorName !== '无') {
              newUserMap.set(String(auditorId), auditorName);
            }
          }
        }
      }
    });

    // 从任务数据中提取部门ID，并优先使用后端 BizTaskVo 返回的 deptName
    const uniqueDeptIds = new Set();
    tasks.forEach(task => {
      if (task?.deptId) {
        const deptIdStr = String(task.deptId);
        uniqueDeptIds.add(deptIdStr);
        // 只有当 deptName 存在且不为空字符串时才使用
        if (task.deptName && task.deptName.trim() !== '' && !newDeptMap.has(deptIdStr)) {
          newDeptMap.set(deptIdStr, String(task.deptName));
        }
      }
    });

    // 由于后端没有提供获取所有部门的公开接口，我们尝试通过用户数据间接获取
    // 创建一个用户deptId到用户列表的映射，用于查找部门信息
    const deptIdToUsers = new Map();
    (Array.isArray(users) ? users : []).forEach(user => {
      if (user.deptId) {
        const deptIdStr = String(user.deptId);
        if (!deptIdToUsers.has(deptIdStr)) {
          deptIdToUsers.set(deptIdStr, []);
        }
        deptIdToUsers.get(deptIdStr).push(user);
      }
    });

    // 批量获取缺失的部门信息
    const missingDeptIds = Array.from(uniqueDeptIds).filter(deptId => !newDeptMap.has(deptId));
    // 先用已有映射出表，缺失的部门名在后台补齐（缓存调用方可不等该异步过程）
    userMap.value = newUserMap;
    deptMap.value = newDeptMap;
    if (missingDeptIds.length > 0) {
      await Promise.all(missingDeptIds.map(deptId =>
        getDeptById(Number(deptId))
          .then(dept => {
            if (dept?.deptName) {
              newDeptMap.set(String(deptId), String(dept.deptName));
            }
          })
          .catch(() => {
            // 如果获取失败，使用兜底值
            newDeptMap.set(String(deptId), `部门${deptId}`);
          })
      ));
    }

    userMap.value = newUserMap;
    deptMap.value = newDeptMap;
    writeJsonCache(DEPTS_CACHE_KEY, Object.fromEntries(newDeptMap));
    worksCache.setItem(DEPTS_CACHE_TIME_KEY, String(Date.now()));
  } catch (error) { // eslint-disable-line no-unused-vars
    // 如果加载失败，使用空映射，这样会显示ID
    userMap.value = new Map();
    deptMap.value = new Map();
    allUsers.value = [];
  }
};

/**
 * 根据部门ID获取部门名称
 * 如果映射中没有，尝试从后端获取
 */
const getDeptName = (deptId) => {
  if(isReview.value)return deptMap.value.get(String(deptId))||'—';
  if (!deptId) return '';
  const deptIdStr = String(deptId);
  return deptMap.value.get(deptIdStr) || `部门${deptId}`;
};

/**
 * 根据用户ID获取用户名称
 */
const getUserName = (userId) => {
  if(isReview.value)return userMap.value.get(String(userId))||'—';
  if (!userId) return '';
  const userIdStr = String(userId);
  let name = userMap.value.get(userIdStr);
  if (name) {
    // 处理可能的编码问题：只处理明显的乱码，不破坏正常的中文字符
    try {
      // 移除BOM标记
      name = name.replace(/^\uFEFF/, '');
      // 只处理明显的乱码情况
      if (name.includes('') || /[\uFFFD]/.test(name)) {
        try {
          name = decodeURIComponent(encodeURIComponent(name));
        } catch (e) { // eslint-disable-line no-unused-vars
          // 编码修复失败，使用原始值
        }
      }
    } catch (e) { // eslint-disable-line no-unused-vars
      // 编码处理失败，使用原始值
    }
    return name;
  }
  // 如果用户映射中没有，尝试从 allUsers 中查找（用于一级二级任务）
  let user = allUsers.value.find(u => {
    const uid = u?.userId ?? u?.user_id;
    return uid !== undefined && uid !== null && String(uid) === userIdStr;
  });
  // 如果 allUsers 为空，尝试从缓存中查找
  if (!user) {
    try {
      const cachedUsers = readJsonCache(USERS_CACHE_KEY);
      if (Array.isArray(cachedUsers)) {
        user = cachedUsers.find(u => {
          const uid = u?.userId ?? u?.user_id;
          return uid !== undefined && uid !== null && String(uid) === userIdStr;
        });
      }
    } catch (e) { // eslint-disable-line no-unused-vars
      // 忽略缓存读取错误
    }
  }
  if (user) {
    let userName = user?.nickName ?? user?.nick_name ?? user?.userName ?? user?.user_name;
    if (userName) {
      try {
        // 移除BOM标记
        userName = userName.replace(/^\uFEFF/, '');
        // 只处理明显的乱码情况
        if (userName.includes('') || /[\uFFFD]/.test(userName)) {
          try {
            userName = decodeURIComponent(encodeURIComponent(userName));
          } catch (e) { // eslint-disable-line no-unused-vars
            // 编码修复失败，使用原始值
          }
        }
      } catch (e) { // eslint-disable-line no-unused-vars
        // 编码处理失败，使用原始值
      }
      if (userName && userName.trim() !== '') {
        // 更新用户映射，避免下次重复查找
        userMap.value.set(userIdStr, userName);
        return userName;
      }
    }
  }
  return `用户${userId}`;
};

// --- 进度计算：统一按 current_value/target_value（父级任务按子孙汇总） ---
const toFiniteNumber = (val) => {
  const n = Number(val);
  return Number.isFinite(n) ? n : 0;
};

const clampPercent = (val) => {
  const n = toFiniteNumber(val);
  return Math.max(0, Math.min(100, n));
};

const getCurrentTarget = (raw) => {
  const cur = toFiniteNumber(raw?.currentValue ?? raw?.current_value);
  const target = toFiniteNumber(raw?.targetValue ?? raw?.target_value);
  return { cur, target };
};

const calcProgressByCurrentTarget = (raw) => {
  const { cur, target } = getCurrentTarget(raw);
  if (target <= 0) return 0;
  return clampPercent(Math.round((cur / target) * 100));
};

const formatTaskNumber = (value) => {
  const n = Number(value);
  if (!Number.isFinite(n)) return '—';
  return String(Number(n.toFixed(2)));
};

const formatRate = (value) => {
  const n = Number(value);
  if (!Number.isFinite(n)) return '—';
  return `${Number(n.toFixed(2))}%`;
};

const level4Rate = (task, currentValue) => {
  const target = toFiniteNumber(task?.targetValue ?? task?.target_value);
  const current = toFiniteNumber(currentValue);
  if (target <= 0) return { actual: 0, counted: 0 };
  const actual = Math.max(0, (current / target) * 100);
  return { actual, counted: Math.min(actual, 100) };
};

const level4LiveRate = (task) => level4Rate(task, forthFeedbackData[task.taskId]?.progress);

const level4AggregatePreview = computed(() => {
  if (!forthTasks.value.length) return 0;
  const total = forthTasks.value.reduce((sum, task) => sum + level4LiveRate(task).counted, 0);
  return total / forthTasks.value.length;
});

/**
 * 将后端任务数据转换为前端需要的格式
 */
const transformTask = (task) => {
  const phaseRange = getPhaseDateRange(task);
  // 修复：后端 level 可能是字符串（如 "3"），统一转成数字，避免三级任务进度被误判为0
  const level = Number(task.level) || 1;
  // 三级任务采用后端权威进度；含四级目标时，后端已按等权规则计算并向下取整。
  const storedProgress = Number(task.progress);
  const progress = (level === 3 || isReview.value) && Number.isFinite(storedProgress)
    ? clampPercent(Math.floor(storedProgress)) : calcProgressByCurrentTarget(task);
  return {
    id: String(task.taskId),
    code: task.taskCode || '',
    name: task.taskName || '',
    level,
    progress,
    status: mapStatus(task.status),
    // 对齐后端 BizTaskVo：优先显示 deptName / leaderName（页面"负责人"统一按 leader_id）
    // 后端可能因为空指针异常导致某些字段为 null，前端需要做兜底处理
    dept: (task.deptName && task.deptName.trim() !== '') ? task.deptName : getDeptName(task.deptId),
    // 负责人显示：优先使用任务数据中的 leaderName，如果没有则从用户映射中获取
    // 注意：这里需要确保 userMap 已经建立（loadUserAndDeptMaps 在 buildTaskTree 之前执行）
    principal: (() => {
      const leaderId = task?.leaderId ?? task?.leader_id;
      // 优先使用任务数据中的 leaderName（处理编码问题）
      if (task.leaderName && task.leaderName.trim() !== '') {
        let name = task.leaderName;
        try {
          name = name.replace(/^\uFEFF/, '');
          // 只处理明显的乱码情况
          if (name.includes('\uFFFD') || /[\uFFFD]/.test(name)) {
            try {
              name = decodeURIComponent(encodeURIComponent(name));
            } catch (e) { // eslint-disable-line no-unused-vars
              // 编码修复失败，使用原始值
            }
          }
        } catch (e) { // eslint-disable-line no-unused-vars
          // 编码处理失败，使用原始值
        }
        return name;
      }
      // 如果任务数据中没有 leaderName，从用户映射中获取
      if (leaderId) {
        return getUserName(leaderId);
      }
      return '';
    })(),
    startDate: phaseRange?.startDate || formatDate(task.createTime),
    endDate: phaseRange?.endDate || formatDate(task.updateTime) || formatDate(task.createTime), // 如果没有更新时间，用创建时间
    // 保留原始数据，方便后续使用
    _raw: task,
    children: []
  };
};

/**
 * 静默刷新任务列表（不闪 loading），用于任务状态变化后及时更新
 */
// 加载待我审批的任务列表
const loadTodoAudits = async () => {
  try {
    const list = await getTodoAudits();
    const arr = Array.isArray(list) ? list : [];
    const map = new Map();
    arr.forEach(item => {
      const taskId = item.task_id ?? item.taskId;
      if (taskId) {
        map.set(Number(taskId), true);
      }
    });
    todoAuditTaskMap.value = map;
  } catch (e) { // eslint-disable-line no-unused-vars
    // 静默失败，不影响主流程
    todoAuditTaskMap.value = new Map();
  }
};

const refreshTasksSilently = async (options = {}) => {
  if(!isCurrentSession())return;
  const context = detailContext();
  await loadTasks({background:true, force:options.force === true});
  if (!isReview.value && isCurrentDetail(context)) await refreshCurrentTaskWorkflow(context);
};

// Polling and returning to the tab must refresh the workflow, not just the task title/status.
// Read only the current submission's logs; older history is fetched when opening the drawer.
const refreshCurrentTaskWorkflow = (context) => {
  if (!isCurrentDetail(context) || !context.taskId || isReview.value || drawerLoading.value || auditLoading.value ||
      auditSubmitting.value || feedbackSubmitting.value || withdrawingTaskIds.size) return;
  if (liveWorkflowRefresh?.context.version === context.version && liveWorkflowRefresh?.context.taskId === context.taskId &&
      liveWorkflowRefresh.version === auditVersion) return liveWorkflowRefresh.promise;
  const version = ++auditVersion;
  const current = () => isCurrentSession() && isCurrentDetail(context) && version === auditVersion;
  const entry = { context, version };
  entry.promise = (async () => {
    try {
      const [task, records] = await Promise.all([getTaskById(context.taskId), getAuditByTaskId(context.taskId)]);
      if (!current()) return;
      if (!Array.isArray(records)) throw new Error('未取得有效审批单');
      const audits = records.map(normalizeAuditItem).filter(item => item && Number(item.taskId) === Number(context.taskId));
      audits.sort((a, b) => (Date.parse(b.submitTime) || 0) - (Date.parse(a.submitTime) || 0) || Number(b.subId) - Number(a.subId));
      const latest = currentTaskSubmission(audits), subId = latest?.subId;
      const logs = subId ? await getAuditLogs(Number(subId)) : [];
      if (!current()) return;
      if (!Array.isArray(logs)) throw new Error('未取得有效审核日志');
      const preserveDraft = fileList.value.some(file => file.raw) || (savedDetailDraft && detailDraft() !== savedDetailDraft);
      let goals;
      if (!preserveDraft && forthTasks.value.length) {
        goals = await getForthTasks(context.taskId);
        if (!current()) return;
      }
      currentTask.value = transformTask(task);
      auditList.value = audits;
      auditLogs.value = [...logs, ...auditLogs.value.filter(log => String(log.subId ?? log.sub_id) !== String(subId))];
      noticeLogs.value = [];
      liveWorkflowError.value = '';
      if (!preserveDraft) {
        feedbackForm.progress = Math.round(Number(task.currentValue ?? task.current_value) || 0);
        feedbackForm.comment = String(latest?.comment ?? task.comment ?? '');
        if (goals) {
          forthTasks.value = Array.isArray(goals) ? goals : goals.data || [];
          Object.keys(forthFeedbackData).forEach(key => delete forthFeedbackData[key]);
          for (const goal of forthTasks.value) forthFeedbackData[goal.taskId] = { progress: Math.round(Number(goal.currentValue ?? goal.current_value) || 0) };
        }
        if (latest && Number(latest.flowStatus) < 0) prefillRejectedSubmission(latest);
        await rememberDetailDraft(context);
      }
    } catch (error) {
      if (current()) {
        if ([403, 404].includes(Number(error.responseCode))) {
          clearDetailData(); drawerVisible.value = false; currentTask.value = null;
          ElMessage.warning('任务已失效或查阅范围已变化，请重新查询');
        } else liveWorkflowError.value = `审核状态刷新失败，当前显示上次记录：${error.message || '请重试'}`;
      }
    } finally {
      if (liveWorkflowRefresh === entry) liveWorkflowRefresh = null;
    }
  })();
  liveWorkflowRefresh = entry;
  return entry.promise;
};
/**
 * 补充缺失的父任务（如果三级任务的父任务不在列表中，需要从后端获取）
 */
const supplementParentTasks = async (tasks) => {
  if(isReview.value)return tasks;
  if (!Array.isArray(tasks) || tasks.length === 0) return tasks;

  // 创建任务ID集合，用于快速查找
  const taskIdSet = new Set(tasks.map(t => String(t?.taskId ?? t?.task_id ?? '')));

  // 收集所有缺失的父任务ID
  const missingParentIds = new Set();
  tasks.forEach(task => {
    const level = task?.level !== undefined && task?.level !== null ? Number(task.level) : NaN;
    // 只处理三级任务
    if (Number.isFinite(level) && level === 3) {
      const parentId = task?.parentId ?? task?.parent_id;
      if (parentId && parentId !== 0 && !taskIdSet.has(String(parentId))) {
        missingParentIds.add(Number(parentId));
      }
    }
  });

  // 如果没有缺失的父任务，直接返回
  if (missingParentIds.size === 0) return tasks;

  // 从后端获取缺失的父任务
  const parentTasks = [];
  const fetchedTaskIds = new Set();

  // 递归获取所有父任务（包括二级和一级）
  const fetchParentRecursively = async (taskId) => {
    if (fetchedTaskIds.has(taskId)) return; // 避免重复获取
    if (taskIdSet.has(String(taskId))) return; // 如果任务已在列表中，不需要获取
    fetchedTaskIds.add(taskId);

    try {
      const parentTask = await getTaskById(taskId);
      if (parentTask && parentTask.taskId) {
        parentTasks.push(parentTask);
        // 更新 taskIdSet，避免重复获取
        taskIdSet.add(String(parentTask.taskId));

        // 如果父任务还有父任务，继续获取
        const parentLevel = parentTask?.level !== undefined && parentTask?.level !== null ? Number(parentTask.level) : NaN;
        if (Number.isFinite(parentLevel) && parentLevel > 1) {
          const grandParentId = parentTask?.parentId ?? parentTask?.parent_id;
          if (grandParentId && grandParentId !== 0 && !taskIdSet.has(String(grandParentId))) {
            await fetchParentRecursively(Number(grandParentId));
          }
        }
      }
    } catch (e) { // eslint-disable-line no-unused-vars
      // 忽略获取失败的情况（可能是权限不足）
      // console.warn(`获取父任务 ${taskId} 失败:`, e);
    }
  };

  // 批量获取所有缺失的父任务
  await Promise.all(Array.from(missingParentIds).map(id => fetchParentRecursively(id)));

  // 将获取到的父任务添加到任务列表中
  return [...tasks, ...parentTasks];
};

/**
 * 将扁平的任务列表转换为树形结构
 */
const buildTaskTree = (tasks) => {
  if (!tasks || tasks.length === 0) return [];

  // 转换数据格式
  const transformedTasks = tasks.map(transformTask);

  // 创建ID映射
  const taskMap = new Map();
  transformedTasks.forEach(task => {
    taskMap.set(task.id, task);
  });

  // 构建树形结构
  const rootTasks = [];
  transformedTasks.forEach(task => {
    const parentId = task._raw.parentId ? String(task._raw.parentId) : null;

    if (!parentId || parentId === '0') {
      // 根节点
      rootTasks.push(task);
    } else {
      // 子节点，找到父节点并添加到children
      const parent = taskMap.get(parentId);
      if (parent) {
        if (!parent.children) {
          parent.children = [];
        }
        parent.children.push(task);
      } else {
        // 如果找不到父节点，也作为根节点处理（这种情况应该通过补充父任务来避免）
        rootTasks.push(task);
      }
    }
  });

  // 对每个节点的children按level和taskId排序
  const sortChildren = (nodes) => {
    nodes.forEach(node => {
      if (node.children && node.children.length > 0) {
        node.children.sort((a, b) => {
          const al = Number(a.level);
          const bl = Number(b.level);
          if (al !== bl) return al - bl;
          return a.id.localeCompare(b.id);
        });
        sortChildren(node.children);
      }
    });
  };

  sortChildren(rootTasks);

  // 递归重算：
  // - 三级：采用后端保存的权威进度（含四级目标时已按等权规则计算）
  // - 二级：按所有子任务（三级）的进度平均值
  // - 一级：按所有对应的三级任务的进度平均值（不包含二级任务的进度）
  const computeProgress = (node) => {
    if (!node) return;
    if (Array.isArray(node.children) && node.children.length > 0) {
      node.children.forEach(computeProgress);
    }

    const lvl = Number(node.level);
    if (lvl === 1) {
      // 一级任务：只计算对应的三级任务的进度平均值（不包含二级任务的进度）
      const getAllLevel3Progress = (n) => {
        const progresses = [];
        if (Array.isArray(n.children) && n.children.length > 0) {
          n.children.forEach(child => {
            const childLevel = Number(child.level);
            if (childLevel === 3) {
              // 如果是三级任务，直接收集其进度
              if (child.progress !== undefined && child.progress !== null) {
                progresses.push(Number(child.progress));
              }
            } else if (childLevel === 2) {
              // 如果是二级任务，递归查找其下的三级任务
              const level3Progresses = getAllLevel3Progress(child);
              progresses.push(...level3Progresses);
            }
            // 忽略其他级别的任务
          });
        }
        return progresses;
      };
      const allLevel3Progresses = getAllLevel3Progress(node);
      if (allLevel3Progresses.length > 0) {
        const sum = allLevel3Progresses.reduce((acc, p) => acc + p, 0);
        node.progress = clampPercent(Math.round(sum / allLevel3Progresses.length));
      } else {
        node.progress = 0;
      }
      return;
    }
    if (lvl === 2) {
      // 二级任务：计算所有子任务（三级）的进度平均值
      const kids = (Array.isArray(node.children) ? node.children : []).filter(c => Number(c.level) === 3);
      if (kids.length > 0) {
        const progresses = kids
          .map(k => Number(k.progress))
          .filter(p => Number.isFinite(p));
        if (progresses.length > 0) {
          const sum = progresses.reduce((acc, p) => acc + p, 0);
          node.progress = clampPercent(Math.round(sum / progresses.length));
        } else {
          node.progress = 0;
        }
      } else {
        node.progress = 0;
      }
      return;
    }

    // 三级任务已在 transformTask 中采用后端权威进度，不在浏览器中重复计算。
    if (lvl !== 3) {
      node.progress = calcProgressByCurrentTarget(node._raw);
    }
  };

  rootTasks.forEach(computeProgress);
  // 展开状态处理：
  // - 首次进入：尝试从 sessionStorage 恢复，否则默认展开一级节点（避免首次渲染大量行导致卡顿）
  // - 后续刷新：保留用户展开状态，只剔除不存在的 key
  const allIds = new Set();
  collectTreeIds(rootTasks, allIds);
  if (!expandedInitialized.value) {
    // 尝试从 sessionStorage 恢复展开状态
    let savedExpandedKeys = [];
    try {
      const saved = worksCache.getItem(EXPANDED_KEYS_CACHE_KEY);
      if (saved) {
        savedExpandedKeys = JSON.parse(saved);
        if (Array.isArray(savedExpandedKeys)) {
          // 过滤掉不存在的 key
          savedExpandedKeys = savedExpandedKeys.filter(id => allIds.has(String(id)));
        }
      }
    } catch (e) { // eslint-disable-line no-unused-vars
      // ignore
    }

    if (savedExpandedKeys.length > 0) {
      // 有保存的展开状态，直接使用
      expandedRowKeys.value = savedExpandedKeys.map(String);
    } else {
      // 没有保存的展开状态，使用默认逻辑
      // 子树过大时默认收起：避免一进来就渲染过多行导致卡顿
      const TOTAL_NODES_THRESHOLD = 260; // 总节点超过该阈值：默认全部收起
      const ROOT_DESC_THRESHOLD = 90; // 单个根节点子树超过该阈值：该根节点默认收起
      const total = allIds.size;

      if (total > TOTAL_NODES_THRESHOLD) {
        expandedRowKeys.value = [];
      } else {
        expandedRowKeys.value = rootTasks
          .filter(rt => countDescendants(rt) <= ROOT_DESC_THRESHOLD)
          .map(t => String(t.id));
      }
    }
    expandedInitialized.value = true;
  } else {
    // 后续刷新：保留用户展开状态，只剔除不存在的 key
    expandedRowKeys.value = (Array.isArray(expandedRowKeys.value) ? expandedRowKeys.value : [])
      .map(String)
      .filter(id => allIds.has(id));
    // 同步更新 sessionStorage
    try {
      worksCache.setItem(EXPANDED_KEYS_CACHE_KEY, JSON.stringify(expandedRowKeys.value));
    } catch (e) { // eslint-disable-line no-unused-vars
      // ignore
    }
  }
  return rootTasks;
};

/**
 * 加载任务数据
 */
const loadTasks = async (options={}) => {
  if(props.detailOnly||!isCurrentSession())return;
  await queryBar.value?.refresh(options);await taskRenderPromise;
};
/**
 * 递归过滤任务树
 */
const filterTaskTree = (nodes, yearFilter, statusFilter, searchKeyword, todayKey) => {
  if (!nodes || nodes.length === 0) return [];

  return nodes.map(node => {
    // 检查当前节点是否匹配筛选条件
    let matches = Number(node.level) === 3 && queryIds.value.has(String(node._raw?.taskId));
    if (taskKindFilter.value) {
      const priority = !!node._raw?.isPriority, difficult = !!node._raw?.isDifficult;
      matches = matches && Number(node.level) === 3 && ({ normal: !priority && !difficult, priority, difficult, both: priority && difficult })[taskKindFilter.value];
    }
    if (owningDeptFilter.value) {
      matches = matches && Number(node.level) === 3 && String(node._raw?.deptId) === owningDeptFilter.value;
    }
    if (leaderFilter.value) {
      const leaderId = node._raw?.leaderId ?? node._raw?.leader_id;
      matches = matches && Number(node.level) === 3 && String(leaderId) === leaderFilter.value;
    }
    if (quickFilter.value !== 'all') {
      const raw = node._raw || {};
      matches = matches && Number(node.level) === 3 && (quickFilter.value === 'mine'
        ? String(raw.leaderId) === String(getCurrentAuthInfo()?.userId)
        : (quickFilter.value === 'todo' ? taskShortcuts.value.todoIds : taskShortcuts.value.returnedIds).some(id => String(id) === String(raw.taskId)));
    }
    if (auditStageFilter.value || auditDeptFilter.value) {
      matches = matches && Number(node.level) === 3 && (taskShortcuts.value.pending || []).some(row =>
        String(row.taskId) === String(node._raw?.taskId) &&
        (!auditStageFilter.value || String(row.flowStatus) === auditStageFilter.value) &&
        (!auditDeptFilter.value || String(row.handlerDeptId) === auditDeptFilter.value));
    }

    // 年度筛选：根据任务的phase字段（所属年份）
    if (yearFilter) {
      const taskPhase = node._raw?.phase;
      // 如果phase字段存在，使用phase；否则从创建时间提取年份
      const taskYear = taskPhase ? String(taskPhase) : (node.startDate ? String(new Date(node.startDate).getFullYear()) : '');
      if (taskYear && taskYear !== String(yearFilter)) {
        matches = false;
      } else if (!taskYear && yearFilter) {
        // 如果没有年份信息，不匹配
        matches = false;
      }
    }

    // 状态筛选
    if (statusFilter) {
      const statusMap = {
        'ing': '进行中',
        'reviewing': '审核中',
        'not_started': '未开始',
        'done': '已完成',
        'overdue': '逾期'
      };
      const targetStatus = statusMap[statusFilter];
      if (statusFilter === 'ing') {
        // 进行中筛选：包含“进行中/审核中”
        if (node.status !== '进行中' && node.status !== '审核中') matches = false;
      } else if (statusFilter === 'not_started') {
        if (node.status !== '未开始') matches = false;
      } else if (statusFilter === 'overdue') {
        // 逾期按截止时间判定，不依赖任务状态文案
        if (!isTaskOverdue(node._raw, todayKey)) matches = false;
      } else if (node.status !== targetStatus) {
        matches = false;
      }
    }

    // 搜索筛选：任务编号或名称（支持模糊搜索）
    if (searchKeyword) {
      const keyword = searchKeyword.toLowerCase().trim();
      if (keyword) {
        const matchCode = node.code && node.code.toLowerCase().includes(keyword);
        const matchName = node.name && node.name.toLowerCase().includes(keyword);
        if (!matchCode && !matchName) {
          matches = false;
        }
      }
    }

    // 递归处理子节点
    let filteredChildren = [];
    if (node.children && node.children.length > 0) {
      filteredChildren = filterTaskTree(node.children, yearFilter, statusFilter, searchKeyword, todayKey);
    }

    // 如果当前节点匹配，或者有子节点匹配，则保留该节点
    if (matches || filteredChildren.length > 0) {
      return {
        ...node,
        children: filteredChildren
      };
    }

    return null;
  }).filter(node => node !== null);
};

/**
 * 在筛选后的树结构上重新计算进度
 */
const recomputeProgressForFilteredTree = (nodes) => {
  if (!nodes || nodes.length === 0) return;

  const computeProgressForNode = (node) => {
    if (!node) return;
    // 先递归处理子节点
    if (Array.isArray(node.children) && node.children.length > 0) {
      node.children.forEach(computeProgressForNode);
    }

    const lvl = Number(node.level);
    if (lvl === 1) {
      // 一级任务：只计算对应的三级任务的进度平均值
      const getAllLevel3Progress = (n) => {
        const progresses = [];
        if (Array.isArray(n.children) && n.children.length > 0) {
          n.children.forEach(child => {
            const childLevel = Number(child.level);
            if (childLevel === 3) {
              // 如果是三级任务，直接收集其进度
              if (child.progress !== undefined && child.progress !== null) {
                progresses.push(Number(child.progress));
              }
            } else if (childLevel === 2) {
              // 如果是二级任务，递归查找其下的三级任务
              const level3Progresses = getAllLevel3Progress(child);
              progresses.push(...level3Progresses);
            }
          });
        }
        return progresses;
      };
      const allLevel3Progresses = getAllLevel3Progress(node);
      if (allLevel3Progresses.length > 0) {
        const sum = allLevel3Progresses.reduce((acc, p) => acc + p, 0);
        node.progress = clampPercent(Math.round(sum / allLevel3Progresses.length));
      } else {
        node.progress = 0;
      }
      return;
    }
    if (lvl === 2) {
      // 二级任务：计算所有子任务（三级）的进度平均值
      const kids = (Array.isArray(node.children) ? node.children : []).filter(c => Number(c.level) === 3);
      if (kids.length > 0) {
        const progresses = kids
          .map(k => Number(k.progress))
          .filter(p => Number.isFinite(p));
        if (progresses.length > 0) {
          const sum = progresses.reduce((acc, p) => acc + p, 0);
          node.progress = clampPercent(Math.round(sum / progresses.length));
        } else {
          node.progress = 0;
        }
      } else {
        node.progress = 0;
      }
      return;
    }
    // 三级任务：保持原有进度（基于 current/target）
    // 不需要重新计算，因为三级任务的进度在 transformTask 时已经计算好了
  };

  nodes.forEach(computeProgressForNode);
};

/**
 * 应用筛选条件
 */
const applyFilters = () => {
  const yearFilter = filterYear.value || '';
  const statusFilter = filterStatus.value || '';
  const searchKeyword = actualSearchText.value.trim() || '';
  const todayKey = localTodayKey();

  taskTreeData.value = filterTaskTree(allTaskTreeData.value, yearFilter, statusFilter, searchKeyword, todayKey);
  // 筛选后重新计算一级和二级任务的进度（基于筛选后的三级任务）
  recomputeProgressForFilteredTree(taskTreeData.value);
  if (!loading.value && !shortcutLoading.value) taskMemory.restoreTree(taskTreeData.value);
};

watch(quickFilter, () => { applyFilters(); taskMemory.resetPosition(); });
watch(taskKindFilter, () => { applyFilters(); taskMemory.resetPosition(); });
watch([leaderFilter, auditStageFilter, auditDeptFilter, owningDeptFilter], () => { applyFilters(); taskMemory.resetPosition(); });
watch(loading, value => { if (!value) taskMemory.restoreTree(taskTreeData.value); });

/**
 * 处理搜索按钮点击
 */


/**
 * 取消搜索：清空关键字并恢复列表
 */


// 递归查找任务（支持树形结构）
const findTaskById = (list, targetId) => {
  const idStr = String(targetId);
  for (const item of list) {
    if (String(item.id) === idStr) {
      return item;
    }
    if (item.children && item.children.length) {
      const found = findTaskById(item.children, idStr);
      if (found) return found;
    }
  }
  return null;
};

// 判断任务是否显示审核按钮
const canAuditTask = (row) => {
  if (!canUsePermission('tasks.audit') && !canUsePermission('tasks.archive')) return false;
  if (!row || row.level !== 3) return false;
  if (row.status !== '审核中') return false;
  const taskId = row._raw?.taskId;
  if (!taskId) return false;
  return todoAuditTaskMap.value.has(Number(taskId));
};

// 判断任务是否显示撤回按钮：提交人本人 + 审核中 + 存在未删除的活跃审批单
const canWithdrawTask = (row) => {
  if (!canUsePermission('tasks.withdraw')) return false;
  if (!row || row.level !== 3) return false;
  const auth = getCurrentAuthInfo();
  if (!auth?.userId) return false;
  const raw = row._raw || {};
  const leaderId = raw.leaderId ?? raw.leader_id;
  const status = raw.status ?? row.status;
  if (!isAdminTestEnabled() && (!leaderId || Number(leaderId) !== Number(auth.userId))) return false;
  return String(status) === '2' || row.status === '审核中';
};

const canWithdrawCurrentTask = computed(() => {
  if (!canWithdrawTask(currentTask.value)) return false;
  if (!latestAudit.value) return false;
  const flowStatus = Number(latestAudit.value.flowStatus ?? latestAudit.value.flow_status);
  const isDelete = latestAudit.value.isDelete ?? latestAudit.value.is_delete;
  return [10, 20, 30].includes(flowStatus) && Number(isDelete) !== 1;
});

// 处理审核任务（打开任务详情抽屉）
const handleAuditTask = (row) => {
  shouldScrollToAudit.value = true;
  handleViewDetail(row);
};

const handleWithdrawTask = async (row) => {
  if (!canWithdrawTask(row)) {
    ElMessage.warning('仅任务提交人可撤回审核中的提交');
    return;
  }
  const taskId = row?._raw?.taskId ?? row?.id;
  if (!taskId) {
    ElMessage.error('未获取到任务ID，无法撤回');
    return;
  }

  const key = Number(taskId);
  if (withdrawingTaskIds.has(key)) return;
  const context = detailContext();
  const isCurrent = () => isCurrentDetail(context) && Number(context.taskId) === key;
  withdrawingTaskIds.add(key);
  try {
    await ElMessageBox.confirm(
      '撤回后，本次提交会作废，任务进度将恢复到提交前状态。确认撤回吗？',
      '撤回提交',
      {
        confirmButtonText: '确认撤回',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    if (isCurrent()) drawerLoading.value = true;
    auditVersion++;
    await drawbackTask(Number(taskId));
    if (detailDisposed) return;
    ElMessage.success('已撤回提交');

    if (isCurrent()) {
      fileList.value = [];
      feedbackForm.comment = '';
    }
    await refreshTasksSilently({force:true});

    if (isCurrent()) {
      const refreshed = findTaskById(taskTreeData.value, taskId) || findTaskById(allTaskTreeData.value, taskId);
      if (refreshed) {
        currentTask.value = refreshed;
      }
      await loadAuditForCurrentTask(context);
      await rememberDetailDraft(context);
    }
  } catch (e) {
    if (e === 'cancel' || e === 'close') return;
    if (!detailDisposed) ElMessage.error(e?.message || '撤回失败，请稍后重试');
  } finally {
    withdrawingTaskIds.delete(key);
    if (isCurrent()) drawerLoading.value = false;
  }
};

// --- 辅助方法 ---
const getLevelClass = (level) => {
  switch (level) {
    case 1: return 'font-bold text-gray-900 text-base';
    case 2: return 'font-medium text-gray-700';
    case 3: return 'text-gray-600 pl-2';
    default: return '';
  }
};

const getStatusTag = (status) => {
  const map = {
    '进行中': 'primary',
    '审核中': 'warning',
    '已完成': 'success',
    '未开始': 'info',
    '逾期': 'danger'
  };
  return map[status] || 'info';
};

const getProgressStatus = (row) => {
  if (row.status === '逾期') return 'exception';
  if (row.progress === 100) return 'success';
  return '';
};

// --- 请求四级任务数据 ---
const loadForthTasks = async (parentId, context) => {
  try {
    const data = await getForthTasks(parentId);
    if (!isCurrentDetail(context)) return;
    // 兼容可能存在的 data 层级包裹
    forthTasks.value = Array.isArray(data) ? data : (data.data || []);

    forthTasks.value.forEach(t => {
      const cv = t.currentValue ?? t.current_value;
      const n = Number(cv);
      forthFeedbackData[t.taskId] = {
        progress: Number.isFinite(n) ? Math.round(n) : 0,
      };
    });
  } catch (e) {
    if (!isCurrentDetail(context)) return;
    console.error(e);
    forthTasks.value = [];
  }
};

const prefillRejectedSubmission = (audit) => {
  if (!audit || Number(audit.flowStatus) >= 0) return;
  feedbackForm.comment = String(audit.comment ?? '');
  if (audit.fileId) fileList.value = [{ name: audit.filename || getFileName(audit.fileId) || '原佐证材料', fileId: audit.fileId, uid: `existing-${audit.fileId}` }];
  if (forthTasks.value.length) {
    if (audit.historicalDetailsAvailable === false) {
      for (const goal of forthTasks.value) if (forthFeedbackData[goal.taskId]) forthFeedbackData[goal.taskId].progress = null;
      ElMessage.warning('原审批单未留存四级目标原始值，请核对后重新填写');
      return;
    }
    for (const goal of audit.subList || []) {
      const id = goal.task_id ?? goal.taskId;
      if (forthFeedbackData[id]) forthFeedbackData[id].progress = Number(goal.reported_value ?? goal.reportedValue ?? 0);
    }
  } else if (audit.reportedValue != null) feedbackForm.progress = Number(audit.reportedValue);
};

// 查看详情（并发拉取审批流和四级任务拆分）
const handleViewDetail = async (row, confirmed = false, prefetched = null) => {
  if (!confirmed && drawerVisible.value && !await confirmTaskLeave()) return;
  savedDetailDraft = '';
  detailVersion++;
  auditVersion++;
  clearDetailData();
  reviewDetail.value=null;reviewDetailError.value='';reviewAccessLost.value=false;
  currentTask.value = row;
  const context = detailContext();
  // “本次填报值”默认回填任务当前值（current_value），且仅允许整数
  const cv = row?._raw?.currentValue ?? row?._raw?.current_value;
  const n = Number(cv);
  feedbackForm.progress = Number.isFinite(n) ? Math.round(n) : 0;
  fileList.value = []; // 清空文件列表
  // 初始化完成情况：先清空，等 loadAuditForCurrentTask 加载
  feedbackForm.comment = '';
  // 初始化审核表单：审核结果默认为"通过"，审批意见默认为"同意"
  auditForm.result = 1;
  auditForm.comment = '同意';

  drawerVisible.value = true;
  drawerLoading.value = true;
  if (row.level === 3) loadTransferLogs(row._raw?.taskId);

  try {
    const taskId = row._raw?.taskId;
    if(isReview.value){await loadReviewPageDetail(taskId,context,prefetched);return;}
    if (taskId && row.level === 3) {
      // 必须先等四级任务拉取完毕，才能基于四级任务ID去并行拉取审批单
      await loadForthTasks(taskId, context);
      if (!isCurrentDetail(context)) return;
      await loadAuditForCurrentTask(context);
      if (isCurrentDetail(context)) prefillRejectedSubmission(latestAudit.value);
    }
  } catch (e) {
    if (isCurrentDetail(context)) console.error("加载任务详情异常:", e);
  } finally {
    if (isCurrentDetail(context)) {
      drawerLoading.value = false;
      await nextTick();
      detailContentRef.value?.closest('.el-drawer__body')?.scrollTo({ top: 0 });
    }
    await rememberDetailDraft(context);
  }
};

async function loadReviewPageDetail(taskId,context,prefetched=null){
 reviewDetailController?.abort();reviewDetailController=new AbortController();
 try{const page=prefetched||await source.value.loadDetail(taskId,{signal:reviewDetailController.signal});if(!isCurrentDetail(context))return;
  for(const person of page.people||[])userMap.value.set(String(person.userId),person.nickName||'');
  for(const dept of page.departments||[]){deptMap.value.set(String(dept.deptId),dept.deptName||'');deptInfoCache.value.set(String(dept.deptId),dept)}
  currentTask.value=transformTask(page.task);forthTasks.value=page.level4Tasks||[];
  for(const task of forthTasks.value)forthFeedbackData[task.taskId]={progress:Number(task.currentValue)||0};
  auditList.value=(page.submissions||[]).map(normalizeAuditItem);auditLogs.value=page.auditLogs||[];noticeLogs.value=[];
  for(const item of auditList.value)if(item.fileId&&item.filename)fileMap.value.set(String(item.fileId),item.filename);
  reviewChildren.value=(page.children||[]).map(transformTask);reviewAccessLost.value=false;reviewDetail.value=normalizeRoundReview(page.review);
  feedbackForm.comment=page.task.comment||'';
 }catch(error){if(error.name!=='AbortError'&&isCurrentDetail(context)){reviewDetailError.value=error.message;if([403,404].includes(Number(error.responseCode))){reviewDetail.value=null;currentTask.value=null;drawerVisible.value=false;ElMessage.warning(error.message)}}}
}
async function reloadReviewDetail(){if(!await confirmTaskLeave())return;const context=detailContext();drawerLoading.value=true;try{await loadReviewPageDetail(context.taskId,context)}finally{if(isCurrentDetail(context))drawerLoading.value=false}}
const reviewCounts=computed(()=>{const result={unreviewed:0,draft:0,submitted:0};for(const task of collectLevel3(taskTreeData.value))result[task._raw?.reviewStatus||'unreviewed']++;return result});

// 发送督办
const handleSupervise = async (row) => {
  if(isReview.value)return;
  try {
    // 督办发送给“任务负责人”（leader_id）
    const toUserId = row?._raw?.leaderId !== undefined && row?._raw?.leaderId !== null ? Number(row._raw.leaderId) : null;
    const taskId = row?.id ? Number(row.id) : null;
    if (!toUserId) {
      ElMessage.warning('未找到任务负责人，无法发送督办');
      return;
    }
    const auth = getCurrentAuthInfo();
    if (auth?.userId && Number(auth.userId) === Number(toUserId)) {
      ElMessage.warning('不能督办自己');
      return;
    }
    await sendNotice({
      to_user_id: toUserId,
      title: `督办提醒：${row?.name || '任务'}`,
      content: '请尽快更新任务进度并按要求提交佐证材料。',
      source_id: taskId || undefined,
      type: '3',
      trigger_event: 'TASK',
    });
    ElMessage.success(`已向 ${getUserName(toUserId)} 发送督办提醒`);
  } catch (e) {
    ElMessage.error(e?.message || '发送督办失败，请稍后重试');
  }
};

// 文件上传相关
const handleExceed = () => {
  ElMessage.warning('只能上传 1 个文件，请先删除旧文件。');
};

const handleRemove = () => {
  fileList.value = [];
};

const handleFileChange = (file, list) => {
  // 校验文件类型
  const fileName = file.name.toLowerCase();
  const isExtAllowed = fileName.endsWith('.doc') || fileName.endsWith('.docx') || fileName.endsWith('.pdf');

  if (!isExtAllowed) {
    ElMessage.error('不支持的文件类型！仅支持 doc, docx, pdf');
    fileList.value = []; // 清空
    return;
  }

  if (file.raw?.size > 50 * 1024 * 1024) {
    ElMessage.error('文件超过 50MB，请压缩后重新选择');
    fileList.value = [];
    return;
  }

  // 更新列表
  fileList.value = list.slice(-1); // 确保只保留最后一个
};

// 预览逻辑
const openPreview = (name, url = '') => {
  previewFileName.value = name;
  const lowerName = name.toLowerCase();

  if (lowerName.endsWith('.pdf')) {
    previewType.value = 'pdf';
    previewUrl.value = url;
  } else {
    previewType.value = 'doc';
  }
  previewVisible.value = true;
};

// 点击上传列表中的文件预览
const handlePreview = (file) => {
  if (file?.fileId && !file.raw) {
    void previewMaterialFile({ fileId: file.fileId, filename: file.name });
    return;
  }
  // 本地预览：仅支持PDF；Word文档提示下载
  const raw = file?.raw;
  if (raw && (raw.type === 'application/pdf' || String(file.name || '').toLowerCase().endsWith('.pdf'))) {
    // 释放旧的 objectURL
    if (selectedFileObjectUrl.value) {
      try { URL.revokeObjectURL(selectedFileObjectUrl.value); } catch { /* noop */ }
    }
    selectedFileObjectUrl.value = URL.createObjectURL(raw);
    openPreview(file.name, selectedFileObjectUrl.value);
    return;
  }
  openPreview(file.name);
};

const downloadSelectedLocalFile = () => {
  const f = fileList.value?.[0];
  if (!f?.raw) {
    ElMessage.warning('暂无可下载的本地文件');
    return;
  }
  const url = URL.createObjectURL(f.raw);
  const a = document.createElement('a');
  a.href = url;
  a.download = f.name || 'download';
  document.body.appendChild(a);
  a.click();
  a.remove();
  setTimeout(() => {
    try { URL.revokeObjectURL(url); } catch { /* noop */ }
  }, 500);
};

/**
 * 提交反馈与材料上报（复用后端接口）
 * 1) /system/upload 上传文件 -> fileId
 * 2) /biz/sub 提交审批单（材料上报）
 */
const submitFeedback = async () => {
  if (feedbackSubmitting.value || drawerLoading.value || auditLoading.value) return;
  const context = detailContext();
  if (!isCurrentDetail(context)) return;
  if (!canSubmitMaterial.value) {
    ElMessage.warning('仅该任务负责人可提交材料');
    return;
  }
  const taskId = currentTask.value?._raw?.taskId;
  if (!taskId) {
    ElMessage.error('未获取到任务信息，无法提交');
    return;
  }
  if (!fileList.value || fileList.value.length === 0) {
    ElMessage.warning('请先选择佐证材料文件（doc/docx/pdf）');
    return;
  }
  const rawFile = fileList.value[0]?.raw;
  const existingFileId = Number(fileList.value[0]?.fileId || 0);
  if (!rawFile && !existingFileId) {
    ElMessage.error('未获取到上传文件，请重新选择');
    return;
  }

  // 检查审核人信息：优先用任务的 auditor_id（兼容旧字段 auditId）；若为空，尝试获取部门负责人作为备选
  const taskRaw = { ...currentTask.value._raw };
  const comment = String(feedbackForm.comment || '').trim();
  const rv = Number.isFinite(Number(feedbackForm.progress)) ? Math.round(Number(feedbackForm.progress)) : 0;
  const dataType = taskRaw?.dataType !== undefined && taskRaw?.dataType !== null ? String(taskRaw.dataType) : '2';
  if (forthTasks.value.some(task => forthFeedbackData[task.taskId]?.progress === null || forthFeedbackData[task.taskId]?.progress === '' ||
      !Number.isFinite(Number(forthFeedbackData[task.taskId]?.progress)))) {
    ElMessage.warning('请完整填写各四级目标的原始完成值');
    return;
  }
  const subList = forthTasks.value.map(task => ({
    task_id: Number(task.taskId),
    reported_value: Number.isFinite(Number(forthFeedbackData[task.taskId]?.progress))
      ? Math.round(Number(forthFeedbackData[task.taskId].progress)) : 0,
    data_type: Number(task.dataType ?? '2'),
  }));
  const audit = latestAudit.value;
  const flow = audit ? Number(audit.flowStatus ?? audit.flow_status) : NaN;
  const subId = audit?.subId ?? audit?.sub_id ?? null;
  const submitBy = audit?.submitBy ?? audit?.submit_by ?? null;
  const myId = getCurrentAuthInfo()?.userId ?? null;
  if (audit && Number(audit.taskId) !== Number(taskId)) return;
  feedbackSubmitting.value = true;
  auditVersion++;
  drawerLoading.value = true;
  try {
    let hasAuditor = false;
    if (taskRaw?.auditorId || taskRaw?.auditor_id || taskRaw?.auditId || taskRaw?.audit_id) {
      hasAuditor = true;
    } else if (taskRaw?.deptId) {
      // 尝试获取部门负责人
      try {
        const dept = await getDeptById(Number(taskRaw.deptId));
        if (dept?.leaderId) {
          hasAuditor = true;
        }
      } catch { /* noop */ }
    }

    if (!hasAuditor) {
      ElMessage.error({
        message: '该任务未设置审核人，无法提交材料。请联系管理员为任务设置审核人后再试。',
        duration: 5000,
        showClose: true
      });
      return;
    }

    // 校验完成情况字数（最多50字）
    if (comment.length > 50) {
      ElMessage.warning('完成情况最多50字');
      return;
    }

    // 1) 上传文件 (需要传递 taskId)    const uploaded = await uploadFile(rawFile, Number(taskId));
    const uploaded = rawFile ? await uploadFile(rawFile, Number(taskId)) : null;
    const fileId = rawFile ? (uploaded?.fileId ?? uploaded?.file_id) : existingFileId;
    if (!fileId) {
      ElMessage.error('上传失败：未拿到 fileId');
      return;
    }

    // 2) 提交材料上报（审批单）
    // 判断是否存在四级任务，如果有四级任务则走批量提交接口，否则按原本三级任务提报逻辑
    if (subList.length > 0) {
      const payload = {
        third_task_id: Number(taskId),
        file_id: Number(fileId),
        comment: comment, // 添加完成情况一起提交
        sub_list: subList
      };
      if (Number.isFinite(flow) && flow < 0 && subId) {
        if (submitBy && myId && Number(submitBy) !== Number(myId)) {
          ElMessage.warning('仅该审批单提交人可重新提交');
          return;
        }
        await reSubmitMaterial({ sub_id: Number(subId), file_id: Number(fileId), comment, sub_list: subList });
      } else await batchSubmitMaterial(payload);

    } else {
      if (Number.isFinite(flow) && flow < 0 && subId) {
        if (submitBy && myId && Number(submitBy) !== Number(myId)) {
          ElMessage.warning('仅该审批单提交人可重新提交');
          return;
        }
        await reSubmitMaterial({
          sub_id: Number(subId),
          file_id: Number(fileId),
          reported_value: rv,
          data_type: dataType,
          comment, // 完成情况
        });
      } else {
        await submitMaterial({
          task_id: Number(taskId),
          file_id: Number(fileId),
          reported_value: rv,
          data_type: dataType,
          comment, // 完成情况
        });
      }
    }

    if (!isCurrentDetail(context)) {
      if (!detailDisposed) await refreshTasksSilently();
      return;
    }
    ElMessage.success('材料已提交（已进入审核流程）');
    fileList.value = [];
    feedbackForm.comment = '';

    // The committed POST is authoritative; do not reuse a pre-submit polling request.
    await refreshTasksSilently({force:true});
    if (isCurrentDetail(context)) await loadAuditForCurrentTask(context);

    await rememberDetailDraft(context);

  } catch (e) {
    if (isCurrentDetail(context)) ElMessage.error(e?.message || '提交失败，请稍后重试');
  } finally {
    feedbackSubmitting.value = false;
    if (isCurrentDetail(context)) drawerLoading.value = false;
  }
};

/**
 * 加载当前任务的审批单信息
 * 注意：后端 /biz/audit/{taskId} 目前可能只返回“当前审批人=我”的审批单
 */
const loadAuditForCurrentTask = async (context = detailContext()) => {
  if (!isCurrentDetail(context)) return;
  const requestVersion = ++auditVersion;
  const isCurrent = () => isCurrentDetail(context) && requestVersion === auditVersion;
  const taskId = context.taskId;
  if (!taskId) {
    auditList.value = [];
    auditLogs.value = [];
    noticeLogs.value = [];
    return;
  }
  auditLoading.value = true;
  try {
    // 封装一个安全的获取单条任务审批单的方法
    const fetchTaskAudit = async (tId) => {
      try {
        if (typeof getAuditByTaskId === 'function') {
          return await getAuditByTaskId(Number(tId));
        } else if (typeof getAudit === 'function') {
          return await getAudit(Number(tId));
        }
      } catch (err) {
        console.warn(`获取任务 ${tId} 的审批单异常:`, err);
      }
      return [];
    };

    // 1. 仅获取三级任务的审批单
    let res = await fetchTaskAudit(taskId);
    if (!isCurrent()) return;
    let allAudits = Array.isArray(res) ? res.map(normalizeAuditItem)
      .filter(item => item && Number(item.taskId) === Number(taskId)) : [];

    if (allAudits.length > 0) {
      allAudits.sort((a, b) => {
        const ta = a.submitTime ? new Date(a.submitTime).getTime() : 0;
        const tb = b.submitTime ? new Date(b.submitTime).getTime() : 0;
        return tb - ta;
      });
      auditList.value = allAudits;

      // 批量获取文件名
      const fileIds = allAudits
        .map(item => item.fileId)
        .filter(id => id !== undefined && id !== null);
      if (fileIds.length > 0) {
        // 去重获取
        const uniqueFileIds = [...new Set(fileIds)];
        const filePromises = uniqueFileIds.map(fileId =>
          fetchFileNameFromDownload(fileId).catch(() => {})
        );
        await Promise.all(filePromises);
        if (!isCurrent()) return;
      }
    } else {
      auditList.value = [];
    }

    // 方案一：为步骤条显示“部门审核人”，按审批单里的 submitDeptId 拉取部门信息（含 leaderId）
    const submitDeptId = auditList.value?.[0]?.submitDeptId;
    if (submitDeptId) {
      await ensureDeptInfo(submitDeptId);
      if (!isCurrent()) return;
    }

    // 尝试加载"操作日志"（后端未实现时会失败，前端兜底为空）
    // 加载所有审批单的操作日志，合并后按时间排序
    const allLogs = [];
    if (auditLogsApiEnabled.value && auditList.value.length > 0) {
      // 遍历所有审批单，加载每个审批单的操作日志
      for (const auditItem of auditList.value) {
        const subId = auditItem.subId ?? auditItem.sub_id;
        if (subId) {
          try {
            const logs = isReview.value?auditLogs.value.filter(log=>Number(log.subId)===Number(subId)):await getAuditLogs(Number(subId));
            if (!isCurrent()) return;
            if (Array.isArray(logs) && logs.length > 0) {
              allLogs.push(...logs);
            }
          } catch (e) {
            if (!isCurrent()) return;
            // 识别"接口未实现"的场景：后续不再请求，避免控制台重复 404
            const msg = String(e?.message || '');
            if (msg.includes('No endpoint GET /biz/audit/logs') || msg.includes('/biz/audit/logs') || msg.includes('404')) {
              auditLogsApiEnabled.value = false;
              break; // 如果接口未实现，停止后续请求
            }
            // 单个审批单的日志加载失败，继续加载其他审批单的日志
          }
        }
      }
    }

    // 合并所有日志并按时间倒序展示
    if (allLogs.length > 0) {
      auditLogs.value = allLogs;
      auditLogs.value.sort((a, b) => {
        const ta = a?.createTime ? new Date(a.createTime).getTime() : 0;
        const tb = b?.createTime ? new Date(b.createTime).getTime() : 0;
        return tb - ta;
      });
    } else {
      auditLogs.value = [];
    }

    // 兜底：如果后端没提供日志接口，就用"通知"做可读时间线
    if (!auditLogs.value || auditLogs.value.length === 0) {
      await loadNoticeLogsForTask(taskId, isCurrent);
      if (!isCurrent()) return;
    } else {
      noticeLogs.value = [];
    }

    // 加载完成情况：从最新的审批单中获取
    if (auditList.value && auditList.value.length > 0) {
      const latest = auditList.value[0];
      const comment = latest.comment ?? latest._raw?.comment ?? null;
      if (comment) {
        feedbackForm.comment = String(comment);
      } else {
        // 如果没有审批单中的完成情况，尝试从任务数据中获取
        const taskComment = currentTask.value?._raw?.comment;
        if (taskComment) {
          feedbackForm.comment = String(taskComment);
        } else {
          feedbackForm.comment = '';
        }
      }
    } else {
      // 如果没有审批单，尝试从任务数据中获取完成情况
      const taskComment = currentTask.value?._raw?.comment;
      if (taskComment) {
        feedbackForm.comment = String(taskComment);
      } else {
        feedbackForm.comment = '';
      }
    }
  } catch (e) {
    if (!isCurrent()) return;
    console.error("整体处理审批日志失败: ", e);
    auditList.value = [];
    auditLogs.value = [];
    noticeLogs.value = [];
    feedbackForm.comment = '';
  } finally {
    if (isCurrent()) auditLoading.value = false;
  }
};

const parseFilenameFromContentDisposition = (cd) => {
  if (!cd) return '';

  // RFC5987: filename*=UTF-8''xxx 或 filename*=charset''xxx
  const mStar = cd.match(/filename\*\s*=\s*([^']*)''([^;]+)/i);
  if (mStar && mStar[2]) {
    try {
      return decodeURIComponent(mStar[2]);
    } catch {
      return mStar[2];
    }
  }

  // 普通格式: filename="xxx" 或 filename=xxx
  const m = cd.match(/filename\s*=\s*"([^";]+)"/i);
  if (m && m[1]) {
    let filename = m[1];
    // 检查是否是URL编码的字符串（包含%符号）
    if (filename.includes('%')) {
      try {
        // 尝试解码URL编码的文件名
        filename = decodeURIComponent(filename);
      } catch {
        // 如果解码失败，可能是部分编码，尝试其他方式
        try {
          // 处理可能的双重编码情况
          filename = decodeURIComponent(decodeURIComponent(filename));
        } catch { /* noop */ }
      }
    }
    return filename;
  }

  return '';
};

const ensureExt = (name, suffix) => {
  const n = String(name || '').trim();
  const s = String(suffix || '').trim().replace(/^\./, '').toLowerCase();
  if (!n) return s ? `file.${s}` : 'file';
  if (!s) return n;
  // 已有后缀就不重复加
  if (/\.[a-z0-9]+$/i.test(n)) return n;
  return `${n}.${s}`;
};

const downloadByFileId = async (fileId, fileSuffix) => {
  if (!fileId) return;
  const token = localStorage.getItem('token') || '';
  if (!token) {
    ElMessage.error('未登录或登录已过期，请先登录');
    try { router.push('/login'); } catch { /* noop */ }
    return;
  }

  try {
    const resp = await adminTestFetch(source.value.materialUrl(fileId), {
      method: 'GET',
      headers: { Authorization: token }
    });

    if (resp.status === 401) {
      // token 过期：清 token 并跳转登录页
      try { localStorage.removeItem('token'); } catch { /* noop */ }
      try {
        const full = `${window.location.pathname || ''}${window.location.search || ''}${window.location.hash || ''}`;
        window.location.replace(`/login?redirect=${encodeURIComponent(full)}`);
      } catch {
        try { router.push('/login'); } catch { /* noop */ }
      }
      return;
    }

    // 检查响应类型：如果是JSON错误，先尝试解析
    const contentType = resp.headers.get('content-type') || '';
    if (!resp.ok) {
      let err;
      if (contentType.includes('application/json')) {
        try { err = await resp.json(); } catch { /* noop */ }
      } else {
        // 如果不是JSON，尝试读取文本错误信息
        try { err = { message: await resp.text() }; } catch { /* noop */ }
      }
      throw new Error(err?.message || `下载失败(${resp.status})`);
    }

    // 检查响应是否为文件流（成功时应该是文件流，不是JSON）
    if (contentType.includes('application/json')) {
      // 如果返回的是JSON，可能是错误信息被包装了
      const jsonData = await resp.json();
      if (jsonData && (jsonData.code || jsonData.message)) {
        throw new Error(jsonData.message || '下载失败');
      }
    }

    const cd = resp.headers.get('content-disposition') || '';
    let filename = parseFilenameFromContentDisposition(cd);
    if (!filename) {
      filename = `file_${fileId}`;
    }
    filename = ensureExt(filename, fileSuffix);

    // 更新文件映射缓存
    if (filename && !filename.startsWith('file_')) {
      fileMap.value.set(String(fileId), filename);
    }

    const blob = await resp.blob();
    const url = URL.createObjectURL(blob);

    const a = document.createElement('a');
    a.href = url;
    // 确保文件名正确解码，避免下载时出现URL编码
    let downloadName = filename;
    if (downloadName.includes('%')) {
      try {
        downloadName = decodeURIComponent(downloadName);
      } catch { /* noop */ }
    }
    a.download = downloadName;
    document.body.appendChild(a);
    a.click();
    a.remove();
    setTimeout(() => {
      try { URL.revokeObjectURL(url); } catch { /* noop */ }
    }, 800);
  } catch (e) {
    ElMessage.error(e?.message || '下载失败，请稍后重试');
  }
};

// 预览佐证材料
const previewMaterialFile = async (auditItem) => {
  const fileId = auditItem?.fileId ?? auditItem?.file_id;
  if (!fileId) {
    ElMessage.warning('未找到文件ID，无法预览');
    return;
  }
  const token = localStorage.getItem('token') || '';
  if (!token) {
    ElMessage.error('未登录或登录已过期，请先登录');
    return;
  }

  // 清理之前的预览URL
  if (previewUrl.value) {
    try { URL.revokeObjectURL(previewUrl.value); } catch { /* noop */ }
  }
  if (selectedFileObjectUrl.value) {
    try { URL.revokeObjectURL(selectedFileObjectUrl.value); } catch { /* noop */ }
  }

  previewVisible.value = true;
  previewType.value = '';
  previewUrl.value = '';
  previewFileName.value = '';

  try {
    const resp = await adminTestFetch(source.value.materialUrl(fileId), {
      method: 'GET',
      headers: { Authorization: token }
    });

    if (resp.status === 401) {
      try { localStorage.removeItem('token'); } catch { /* noop */ }
      try {
        const full = `${window.location.pathname || ''}${window.location.search || ''}${window.location.hash || ''}`;
        window.location.replace(`/login?redirect=${encodeURIComponent(full)}`);
      } catch { /* noop */ }
      return;
    }

    // 检查响应状态
    if (!resp.ok) {
      // 响应失败，尝试解析错误信息
      const contentType = resp.headers.get('content-type') || '';
      let err;
      if (contentType.includes('application/json')) {
        try { err = await resp.json(); } catch { /* noop */ }
      } else {
        // 如果不是JSON，尝试读取文本错误信息
        try { err = { message: await resp.text() }; } catch { /* noop */ }
      }
      throw new Error(err?.message || `文件获取失败(${resp.status})`);
    }

    // 响应成功，获取文件流
    const cd = resp.headers.get('content-disposition') || '';
    const filename = parseFilenameFromContentDisposition(cd);
    previewFileName.value = filename || `file_${fileId}`;

    // 更新文件映射缓存
    if (filename) {
      fileMap.value.set(String(fileId), filename);
      // 如果当前项在审批记录中，更新其文件名显示
      const currentItem = auditList.value.find(item => String(item.fileId || item.file_id) === String(fileId));
      if (currentItem) {
        // 触发响应式更新
        currentItem.fileName = filename;
      }
    }

    const blob = await resp.blob();
    previewFileBlob.value = blob; // 保存blob用于下载
    const ext = getExtFromName(previewFileName.value);
    const isPdf = ext === 'pdf' || (await isPdfBlob(blob));

    if (isPdf) {
      previewType.value = 'pdf';
      const pdfBlob = blob.type === 'application/pdf' ? blob : new Blob([blob], { type: 'application/pdf' });
      previewUrl.value = URL.createObjectURL(pdfBlob);
    } else {
      previewType.value = 'doc';
      previewFileName.value = previewFileName.value || `file_${fileId}`;
    }
  } catch (e) {  
    previewVisible.value = false;
    ElMessage.error(e?.message || '预览失败，请稍后重试');
  }
};

// 下载预览中的文件
const downloadPreviewFile = () => {
  if (!previewFileBlob.value || !previewFileName.value) {
    ElMessage.warning('暂无可下载的文件');
    return;
  }

  try {
    const url = URL.createObjectURL(previewFileBlob.value);
    const a = document.createElement('a');
    a.href = url;
    // 确保文件名正确解码，避免下载时出现URL编码
    let downloadName = previewFileName.value;
    if (downloadName.includes('%')) {
      try {
        downloadName = decodeURIComponent(downloadName);
      } catch { /* noop */ }
    }
    a.download = downloadName;
    document.body.appendChild(a);
    a.click();
    a.remove();
    setTimeout(() => {
      try { URL.revokeObjectURL(url); } catch { /* noop */ }
    }, 500);
  } catch {
    ElMessage.error('下载失败，请稍后重试');
  }
};

// 清理预览URL和Blob
const cleanupPreviewUrl = () => {
  if (previewUrl.value) {
    try { URL.revokeObjectURL(previewUrl.value); } catch { /* noop */ }
    previewUrl.value = '';
  }
  if (previewFileBlob.value) {
    previewFileBlob.value = null;
  }
  previewFileId.value = null;
  previewFileName.value = '';
  previewType.value = '';
};

// 判断是否为PDF
const isPdfBlob = async (blob) => {
  try {
    const buf = await blob.slice(0, 5).arrayBuffer();
    const bytes = new Uint8Array(buf);
    return bytes[0] === 0x25 && bytes[1] === 0x50 && bytes[2] === 0x44 && bytes[3] === 0x46 && bytes[4] === 0x2D;
  } catch {
    return false;
  }
};

// 获取文件扩展名
const getExtFromName = (name) => {
  if (!name) return '';
  const m = name.match(/\.([a-z0-9]+)$/i);
  return m ? m[1].toLowerCase() : '';
};

// 提交审核
const submitAudit = async () => {
  if (auditSubmitting.value || drawerLoading.value || auditLoading.value) return;
  const context = detailContext();
  if (!isCurrentDetail(context) || !canAudit.value ||
      Number(latestAudit.value?.taskId) !== Number(context.taskId)) return;
  const result = auditForm.result;
  const comment = auditForm.comment;
  if (!auditForm.comment) {
    ElMessage.warning('请输入审批意见');
    return;
  }

  if (!latestAudit.value) {
    ElMessage.warning('未找到审批单信息');
    return;
  }

  const subId = latestAudit.value.subId ?? latestAudit.value.sub_id;
  if (!subId) {
    ElMessage.warning('未找到审批单ID');
    return;
  }

  auditSubmitting.value = true;
  auditVersion++;
  try {
    await auditTask({
      sub_id: Number(subId),
      is_pass: result === 1,
      title: comment,
      content: comment
    });

    if (!isCurrentDetail(context)) return;
    ElMessage.success(result === 1 ? '审批通过' : '已驳回');

    // 重置审核表单
    auditForm.result = 1;
    auditForm.comment = '';

    // Always start a post-commit query, even if an older polling query is in flight.
    await refreshTasksSilently({force:true});
    // 重新加载审批记录和操作日志（确保能看到最新的审批记录和所有操作日志）
    if (isCurrentDetail(context)) await loadAuditForCurrentTask(context);
    await rememberDetailDraft(context);
  } catch (e) {  
    if (isCurrentDetail(context)) ElMessage.error(e?.message || '审核提交失败，请稍后重试');
  } finally {
    auditSubmitting.value = false;
  }
};

// 根据路由参数自动打开对应任务详情
const openTaskFromQuery = async (taskId) => {
  let prefetched=null;
  if (!taskId || !isCurrentSession()) return;
  let target = findTaskById(allTaskTreeData.value, taskId);
  if(!target){try{const response=await source.value.loadDetail(taskId);prefetched=isReview.value?response:null;const raw=isReview.value?response.task:response;if(!isCurrentSession())return;target=transformTask(raw);}catch{/* preserve the existing inaccessible-record message */}}
  if (target && (Number(target.level)===3||isReview.value&&Number(target.level)===1)) {
    filterYear.value = String(target._raw?.phase || '');
    quickFilter.value = 'all';
    leaderFilter.value = ''; auditStageFilter.value = ''; auditDeptFilter.value = ''; owningDeptFilter.value = '';
    filterStatus.value = '';
    searchText.value = '';
    actualSearchText.value = '';
    applyFilters();
    handleViewDetail(target,false,prefetched);
  } else {
    drawerVisible.value = false;
    currentTask.value = null;
    ElMessage.info('任务不存在、已删除或无权访问');
  }
};

// 监听筛选条件变化（年度和状态实时筛选，搜索需要点击按钮）
watch([filterYear, filterStatus], () => {
  applyFilters();
}, { deep: true });

// 监听抽屉打开和审核区域，自动滚动到审核区域
watch([drawerVisible, shouldScrollToAudit, canAudit, latestAudit], async ([drawerOpen, needScroll, canAuditNow, hasAudit]) => {
  if (drawerOpen && needScroll && canAuditNow && hasAudit && auditSectionRef.value) {
    await nextTick();
    // 等待抽屉完全打开和审核数据加载完成
    setTimeout(() => {
      if (auditSectionRef.value) {
        auditSectionRef.value.scrollIntoView({ behavior: 'smooth', block: 'start' });
        shouldScrollToAudit.value = false; // 重置标志
      }
    }, 500);
  }
});

// 监听审核结果变化，自动设置审批意见
watch(() => auditForm.result, (val) => {
  if (val === 0) {
    auditForm.comment = '驳回';
  } else if (val === 1) {
    auditForm.comment = '同意';
  }
});

// 监听可审核状态和审批单，当可以审核时，初始化审批意见为"同意"
watch([canAudit, latestAudit], ([canAuditNow, hasAudit]) => {
  if (canAuditNow && hasAudit && auditForm.result === 1) {
    // 如果审核结果是"通过"且审批意见为空，则设置为"同意"
    if (!auditForm.comment || auditForm.comment.trim() === '') {
      auditForm.comment = '同意';
    }
  }
}, { immediate: true });

onMounted(() => {
  if (props.detailOnly) return;
  if(!isReview.value)loadTaskManagementPermission();
  // QueryFilterBar owns the initial tree read; independent metadata may finish later.
  if(!isReview.value){void loadTaskShortcuts();void loadTodoAudits();}

  // 自动刷新：页面可见时定时拉取任务，确保任务状态变更能及时更新
  if (tasksAutoRefreshTimer) clearInterval(tasksAutoRefreshTimer);
  tasksAutoRefreshTimer = setInterval(() => {
    if (!document.hidden) refreshTasksSilently();
  }, 15000);

  // 切回页面立即刷新一次
  try {
    const handler = () => {
      if (!document.hidden) refreshTasksSilently();
    };
    // 挂到 window 便于卸载（避免重复绑定）
    window.__worksVisibilityHandler = handler;
    document.addEventListener('visibilitychange', handler);
  } catch { /* noop */ }
});

onUnmounted(() => {
  reviewDetailController?.abort();
  detailDisposed = true;
  detailVersion++;
  auditVersion++;
  if (tasksAutoRefreshTimer) {
    clearInterval(tasksAutoRefreshTimer);
    tasksAutoRefreshTimer = null;
  }
  try {
    if (window.__worksVisibilityHandler) {
      document.removeEventListener('visibilitychange', window.__worksVisibilityHandler);
      delete window.__worksVisibilityHandler;
    }
  } catch { /* noop */ }
});

watch(
  () => route.query.taskId,
  (val) => {
    if (!props.detailOnly && val && taskTreeData.value.length > 0) {
      openTaskFromQuery(val);
    }
  }
);
const confirmTaskLeave = async (skipTransfer=false) => {
  if(!skipTransfer&&taskTransferRequests.value&&!await taskTransferRequests.value.confirmLeave())return false;
  if (detailOperationBusy.value) return false;
  if (expertReviewRef.value && !await expertReviewRef.value.confirmLeave()) return false;
  if(isReview.value)return true;
  // 详情抽屉未打开时不存在可放弃的编辑内容，避免关闭后再操作被误判为“有未提交内容”。
  if(!drawerVisible.value)return true;
  return confirmDetailLeave(fileList.value.some(file => file.raw) || !!(savedDetailDraft && detailDraft() !== savedDetailDraft));
};
const closeTaskDetail = async done => { if (await confirmTaskLeave()) done(); };
const handleDrawerClosed = () => { savedDetailDraft = ''; emit('detail-closed'); };
const closeDetails = async () => { if (!drawerVisible.value) return true; if (!await confirmTaskLeave()) return false; drawerVisible.value = false; return true; };
let externalSequence = 0;
const openTaskDetail = async (id, options = {}) => {
  if (!await closeDetails()) return false;
  const seq = ++externalSequence;
  try {
    const existing=isReview.value?(findTaskById(allTaskTreeData.value,id)||reviewChildren.value.find(row=>String(row._raw.taskId)===String(id))):null;
    const response=existing?null:await source.value.loadDetail(id);
    const raw=existing?existing._raw:isReview.value?response.task:response;
    if (detailDisposed || seq !== externalSequence) return false;
    reviewYear.value = Number(options.year || raw.phase || filterYear.value || 2026);
    reviewChildren.value = [];
    if (!isReview.value && Number(raw.level) === 1) {
      const [review, tasks] = await Promise.all([getExpertDetail(id, {year: reviewYear.value, ...(props.reviewExpertId ? {expertId: props.reviewExpertId} : {})}), getAllTasks()]);
      if (detailDisposed || seq !== externalSequence) return false;
      const ids = new Set(review.scope.tasks.map(task => Number(task.taskId)));
      reviewChildren.value = tasks.filter(task => ids.has(Number(task.taskId))).map(transformTask);
      raw.progress = reviewChildren.value.length ? Math.floor(reviewChildren.value.reduce((sum, task) => sum + task.progress, 0) / reviewChildren.value.length) : 0;
    }
    await handleViewDetail(transformTask(raw),false,isReview.value?response:null);
    if (detailDisposed || seq !== externalSequence || !drawerVisible.value) return false;
    await nextTick();
    if (options.review) await expertReviewRef.value?.focusEditor();
    return true;
  } catch (e) { if (!detailDisposed && seq === externalSequence) ElMessage.error(e.message || '任务详情加载失败'); return false; }
};
const detailBeforeUnload = event => {
  if (drawerVisible.value && (detailOperationBusy.value || expertReviewRef.value?.dirty || !isReview.value && (fileList.value.some(file => file.raw) || savedDetailDraft && detailDraft() !== savedDetailDraft))) { event.preventDefault(); event.returnValue = ''; }
};
onMounted(() => window.addEventListener('beforeunload', detailBeforeUnload));
onUnmounted(() => { externalSequence++; window.removeEventListener('beforeunload', detailBeforeUnload); });
onBeforeRouteLeave(async () => props.detailOnly || !drawerVisible.value || await confirmTaskLeave());
defineExpose({ openTaskDetail, closeDetails, prepareContextChange,confirmLeave: confirmTaskLeave, busy: detailOperationBusy });

</script>

<style scoped>
.review-result-tag { margin-top:4px; }
.task-detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  width: 100%;
}
.task-detail-navigation {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 14px;
}
.task-detail-navigation .el-button + .el-button { margin-left: 0; }
/* 容器样式 */
.works-container.detail-only { height: 0; gap: 0; }
.works-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
}

/* 顶部滚动信息条（预留） */
.notice-bar {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.notice-bar-inner {
  display: flex;
  align-items: center;
  min-height: 42px;
}

.notice-bar-label {
  padding: 0 18px;
  color: #fff;
  background: #409eff;
  align-self: stretch;
  display: flex;
  align-items: center;
  font-weight: 600;
}

.notice-bar-content {
  flex: 1;
  overflow: hidden;
  white-space: nowrap;
  padding: 0 16px;
  color: #606266;
}

.notice-bar-marquee {
  display: inline-block;
  padding-left: 100%;
  font-size: 13px;
  color: #606266;
  animation: works-notice-marquee 18s linear infinite;
}

@keyframes works-notice-marquee {
  0% {
    transform: translateX(0);
  }

  100% {
    transform: translateX(-100%);
  }
}

.sub-title {
  font-size: 14px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 8px;
}

/* 项目信息卡片 */
.project-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #ebeef5;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.project-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.project-title {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  margin: 0;
}

.project-meta {
  display: flex;
  gap: 24px;
  color: #909399;
  font-size: 14px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.project-actions {
  display: flex;
  gap: 16px;
  align-items: center;
}

.toggle-group {
  background: #f5f7fa;
  padding: 4px;
  border-radius: 4px;
  display: flex;
  gap: 4px;
}

.toggle-btn {
  padding: 6px 16px;
  border: none;
  background: transparent;
  color: #606266;
  cursor: pointer;
  border-radius: 4px;
  font-size: 14px;
  transition: all 0.3s;
}

.toggle-btn.active {
  background: #fff;
  color: #1890ff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  font-weight: bold;
}

.create-btn {
  background-color: #1890ff;
  border-color: #1890ff;
}

/* 表格卡片 */
.table-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #ebeef5;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  /* 防止溢出 */
}

/* 统计条：单行概览，弱化视觉重量，让任务表格保持主体地位 */
.task-stats-wrap {
  container-type: inline-size;
}

.task-stats {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 4px 24px;
  padding-bottom: 10px;
  margin-bottom: 12px;
  border-bottom: 1px solid #f0f2f5;
}

.stat-item {
  display: inline-flex;
  align-items: baseline;
  gap: 6px;
  padding: 0;
  border: 0;
  background: transparent;
  font: inherit;
  text-align: left;
  white-space: nowrap;
}

.stat-item--clickable {
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: border-color 0.15s;
}

.stat-item--clickable:hover .stat-value,
.stat-item--clickable:focus-visible .stat-value {
  text-decoration: underline;
}

.stat-item--clickable:focus-visible {
  outline: none;
}

.stat-item--clickable.active {
  border-bottom-color: #409eff;
}

.stat-label {
  color: #909399;
  font-size: 12px;
  white-space: nowrap;
}

.stat-value {
  font-size: 17px;
  font-weight: 600;
  line-height: 1.2;
  color: #303133;
  white-space: nowrap;
}

.stat-value--rate { color: #409eff; }
.stat-value--done { color: #67c23a; }
.stat-value--reviewing { color: #e6a23c; }
.stat-value--overdue { color: #f56c6c; }
.stat-value--todo { color: #1890ff; }

.stat-note {
  color: #909399;
  font-size: 12px;
  white-space: nowrap;
}

.stat-note::before {
  content: '·';
  margin-right: 4px;
  color: #c0c4cc;
}

.stat-link {
  border: none;
  background: transparent;
  padding: 0;
  font: inherit;
  color: #f56c6c;
  cursor: pointer;
}

.stat-link:hover,
.stat-link.active {
  text-decoration: underline;
}

.stat-link.active {
  font-weight: 700;
}

/* 表格样式 */
.table-wrapper {
  position: relative;
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  min-height: 0;
}

/* 展开子任务行的小动画（淡入 + 轻微上移） */
:deep(.el-table__body tr.row-expand-anim) {
  animation: works-row-expand 180ms ease-out both;
}

@keyframes works-row-expand {
  from {
    opacity: 0;
    transform: translateY(-4px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.task-code {
  color: #909399;
  font-family: monospace;
  margin-right: 6px;
  font-weight: 700;
  /* 任务编号如 [SG0101] 加粗显示 */
}

.task-name-cell {
  cursor: pointer;
}

.task-name-text:hover {
  text-decoration: underline;
}

.child-count {
  margin-left: 6px;
  color: #909399;
  font-size: 12px;
}

/* 树表：隐藏小箭头（改为点击整行/名称展开） */
::deep(.el-table__expand-icon) {
  display: none;
}

.principal-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.avatar-circle {
  width: 24px;
  height: 24px;
  background-color: #e6f7ff;
  color: #1890ff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
}

.date-cell {
  font-size: 12px;
  color: #909399;
  font-family: monospace;
}

.ml-2 {
  margin-left: 8px;
}

.mr-1 {
  margin-right: 4px;
}

.mb-4 {
  margin-bottom: 16px;
}

.mb-6 {
  margin-bottom: 24px;
}

.mt-4 {
  margin-top: 16px;
}

.mt-6 {
  margin-top: 24px;
}

/* 抽屉样式 */
.section-title {
  font-size: 15px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 12px;
  border-left: 3px solid #1890ff;
  padding-left: 8px;
}

.task-info-box {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 4px;
  margin-bottom: 24px;
  border: 1px solid #e4e7ed;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.info-row {
  display: flex;
}

.info-row--full {
  grid-column: 1 / -1;
  align-items: flex-start;
}

.info-row--full .label {
  /* 多行内容时，label 固定在第一行顶部，避免“预期效果”字样因换行产生错位 */
  align-self: flex-start;
  padding-top: 2px;
  white-space: nowrap;
}

.info-row .label {
  width: 92px;
  color: #909399;
  font-size: 14px;
  white-space: nowrap;
  flex-shrink: 0;
}

.info-row .value {
  color: #303133;
  font-weight: 500;
  font-size: 14px;
}

.info-row--full .value {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
}

/* 预期效果/材料清单等多行内容：自动换行展示 */

.slider-container {
  padding: 0 10px;
}

.progress-input-wrapper {
  display: inline-flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  user-select: none;
}

.level4-rate-summary {
  display: inline-flex;
  flex-wrap: wrap;
  gap: 6px 14px;
  color: #606266;
  font-size: 12px;
}

.level4-rate-summary span:last-child {
  color: #409eff;
  font-weight: 500;
}

.level4-aggregate-preview {
  margin: 4px 0 18px;
}

.audit-level4-rate {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
}

.percent-suffix {
  color: #606266;
  font-size: 14px;
}

.wheel-tip {
  color: #909399;
  font-size: 12px;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
}

.text-right {
  text-align: right;
}

/* 文件上传区域控制 */
/* 当存在文件时，隐藏上传触发器（拖拽框） */
.hide-upload-trigger :deep(.el-upload--text) {
  display: none;
}

/* 日志样式 */
.log-timeline-box {
  padding-left: 10px;
}

.log-card-item {
  border: 1px solid #ebeef5;
  background-color: #fff;
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.log-user {
  font-size: 14px;
  color: #303133;
}

.log-content {
  color: #606266;
  font-size: 13px;
}

.log-file {
  color: #1890ff;
  font-size: 13px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  background: #f0f9eb;
  padding: 4px 8px;
  border-radius: 4px;
  width: fit-content;
}

.log-file:hover {
  text-decoration: underline;
  background: #e1f3d8;
}

.preview-text {
  font-size: 12px;
  color: #909399;
  margin-left: 4px;
}

.pdf-viewer {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.pdf-viewer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.file-name-display {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 16px;
}

.doc-viewer {
  text-align: center;
  padding: 40px 0;
}

/* 审核表单样式 */
.audit-section {
  border-top: 1px solid #ebeef5;
  padding-top: 20px;
}

.audit-form {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
}

.audit-form :deep(.el-radio) {
  margin-right: 20px;
}

.radio-success :deep(.el-radio__label) {
  color: #67c23a;
}

.radio-danger :deep(.el-radio__label) {
  color: #f56c6c;
}

.file-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #409eff;
  cursor: pointer;
  font-size: 14px;
  padding: 8px 12px;
  background: #ecf5ff;
  border-radius: 4px;
  transition: all 0.3s;
}

.file-link:hover {
  background: #b3d8ff;
  text-decoration: underline;
}

/* 管理员归属移交 */
.transfer-hint {
  color: #909399;
  font-size: 13px;
  margin-right: 4px;
}

/* 批量移交的勾选列：常驻挂载，未进入移交模式时整列收成 0 宽。
   Element Plus 用 colgroup 的 col 设置列宽，必须覆盖 col 才不会留下空白 */
.table-wrapper.transfer-capable:not(.transfer-selecting) :deep(colgroup > col:first-child) {
  width: 0 !important;
}

.table-wrapper.transfer-capable:not(.transfer-selecting) :deep(.transfer-select-cell) {
  visibility: hidden;
  padding: 0 !important;
}

.owner-actions {
  display: flex;
  justify-content: flex-end;
  margin: -8px 0 12px;
}

.owner-actions .el-button + .el-button { margin-left: 0; }
</style>
