<template>
  <div class="works-container">
    <transition name="refresh-fade">
      <div v-if="refreshing" class="refresh-progress" role="status" aria-live="polite">
        <div class="refresh-progress-card">
          <div class="refresh-spinner">
            <el-icon><Refresh /></el-icon>
          </div>
          <div class="refresh-text">正在刷新绩效数据</div>
          <div class="refresh-subtext">正在重新计算关联任务与年度指标</div>
          <div class="refresh-bar"><span></span></div>
        </div>
      </div>
    </transition>

    <!-- 顶部滚动信息条（预留） -->
    <div class="notice-bar">
      <div class="notice-bar-inner">
        <div class="notice-bar-label">提示</div>
        <div class="notice-bar-content">
          <div class="notice-bar-marquee">
            绩效指标库 - 三级指标由任务自动汇总，一级/二级指标为分类展示
          </div>
        </div>
      </div>
    </div>

    <!-- 顶部项目信息卡片 -->
    <div class="project-card">
      <div class="project-info">
        <h2 class="project-title">徐州工业职业技术学院"双高建设计划"任务与绩效目标管理</h2>
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
            <span>责任部门："双高"建设办公室</span>
          </div>
        </div>
      </div>
      <div class="project-actions">
        <el-tag size="small" effect="plain" type="info"> {{ performanceQueryState.initialized ? `共 ${visibleNodeCount} 条` : performanceQueryState.error ? '绩效加载失败' : '正在加载绩效…' }} </el-tag>
        <div class="toggle-group">
          <button v-if="canUsePermission('tasks.read')" class="toggle-btn" :class="{ active: currentTab === 'task' }"
                  @click="handleTabSwitch('task')">任务</button>
          <button v-if="canUsePermission('performance.read')" class="toggle-btn" :class="{ active: currentTab === 'performance' }"
                  @click="handleTabSwitch('performance')">绩效</button>
          <button v-if="canUsePermission('achievements.read')" class="toggle-btn" :class="{ active: currentTab === 'achievement' }"
                  @click="handleTabSwitch('achievement')">成果</button>
          <button v-if="canUsePermission('budget.read')" class="toggle-btn" :class="{ active: currentTab === 'budget' }"
                  @click="handleTabSwitch('budget')">预算</button>
        </div>
        <span v-if="canTransfer && transferMode && !transferSelection.length" class="transfer-hint">请在列表中勾选需要移交的三级指标</span>
        <el-button v-if="canTransfer && !transferMode" class="transfer-btn" @click="enterTransferMode">批量移交</el-button>
        <el-button v-if="canTransfer && transferMode" class="transfer-btn" @click="exitTransferMode">退出批量移交</el-button>
        <el-button v-if="canTransfer && transferMode && transferSelection.length" class="transfer-btn"
          @click="openTransfer(transferSelection)">确认移交（{{ transferSelection.length }}）</el-button>
        <el-button type="primary" class="create-btn" v-if="false">
          <el-icon class="mr-1">
            <Plus />
          </el-icon>
          新建绩效
        </el-button>
      </div>
    </div>

    <!-- 筛选与表格区域 -->
    <div class="table-card">
      <!-- 筛选区 -->
      <QueryFilterBar ref="queryBar" response-mode="tree" domain="performance" title="绩效" :required-keys="['year']" :defaults="{year: getDefaultPerformanceYear()}" :remember="!route.query.perfId" :before-apply="beforePerformanceQuery" @result="receiveQuery" @state="performanceQueryState = $event" />
      <div class="table-wrapper" v-loading="loading || performanceQueryState.loading && !performanceQueryState.background"
        :class="{ 'transfer-capable': canTransfer, 'transfer-selecting': transferMode }">
        <!-- v-memo：表格只跟随数据与展开态更新，勾选与批量移交模式切换都不再触发整表重渲染 -->
        <VirtualTreeTable :selection-visible="transferMode" :preserve-selection="performanceQueryState.background" ref="performanceTableRef" v-memo="[performanceTreeData, expandedRowKeys, canTransfer, transferMode, performanceQueryState.background, !performanceTreeData.length && performanceQueryState.loading, !performanceTreeData.length && performanceQueryState.error, !performanceTreeData.length && performanceQueryState.initialized]"
                  :data="performanceTreeData" :empty-text="performanceQueryState.loading ? '正在加载绩效…' : performanceQueryState.error ? '绩效加载失败，请重试' : !performanceQueryState.initialized ? '正在加载绩效…' : '暂无匹配绩效'" style="width: 100%" row-key="id" border
                  :expand-row-keys="expandedRowKeys" @expand-change="handleExpandChange" @row-click="handleRowClick"
                  @selection-change="handleTransferSelectionChange"
                  :row-class-name="context => `${rowClassName(context) || ''} ${detailRowClass(context.row)}`" :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
                  :header-cell-style="{ background: '#f8f9fa', color: '#606266', fontWeight: 'bold' }">
          <!-- 勾选一级/二级会由表格自身级联勾选其下全部三级指标，提交时只取三级。
               该列常驻挂载，平时整列收成 0 宽（见样式），进入移交模式后整表右移让出勾选列 -->
          <el-table-column v-if="canTransfer" type="selection" width="46"
            class-name="transfer-select-cell" label-class-name="transfer-select-cell" />
          <el-table-column prop="name" label="绩效指标名称" min-width="350">
            <template #default="scope">
              <span class="task-name-cell" :class="getLevelClass(scope.row.level)">
                <span v-if="scope.row.code" class="task-code">[{{ scope.row.code }}]</span>
                <span class="task-name-text">{{ scope.row.name }}</span>
                <span v-if="(scope.row.children?.length || 0) > 0" class="child-count">（{{ scope.row.children.length
                  }}）</span>
              </span>
            </template>
          </el-table-column>
          <el-table-column label="目标值/完成值" width="180">
            <template #default="scope">
              <div v-if="scope.row.level === 3" class="progress-text">
                <span class="target-value">{{ formatNumber(scope.row.targetValue) }}</span>
                <span class="mx-1">/</span>
                <span class="current-value">{{ formatNumber(scope.row.currentValue) }}</span>
              </div>
              <div v-else class="text-gray-400 text-xs">—</div>
            </template>
          </el-table-column>
          <el-table-column label="完成率" width="150">
            <template #default="scope">
              <el-progress v-if="scope.row.level === 3" :percentage="scope.row.progress"
                           :status="getProgressStatus(scope.row)" :stroke-width="8"></el-progress>
              <span v-else class="text-gray-400 text-xs">—</span>
            </template>
          </el-table-column>
          <!-- 管理员可见：当前年度已关联的任务数，便于发现没有挂任务的指标 -->
          <el-table-column v-if="canManageRelation" label="关联任务数" width="110" align="center"
            class-name="relation-count-cell" label-class-name="relation-count-cell">
            <template #default="scope">
              <span v-if="scope.row.level === 3 && isAutoCode(scope.row.code)"
                class="relation-count">{{ relationCountOf(scope.row) }}</span>
              <span v-else class="text-gray-400 text-xs">—</span>
            </template>
          </el-table-column>
          <el-table-column prop="dataType" label="数据类型" width="120" align="center">
            <template #default="scope">
              <span v-if="scope.row.level === 3">
                <el-tag :type="getDataTypeTag(scope.row.dataType)" effect="plain" size="small">
                  {{ getDataTypeText(scope.row.dataType) }}
                </el-tag>
              </span>
              <span v-else class="text-gray-400 text-xs">—</span>
            </template>
          </el-table-column>
          <el-table-column prop="dept" label="归口部门" width="150" show-overflow-tooltip>
            <template #default="scope">
              <span v-if="scope.row.level === 3">{{ scope.row.dept || '-' }}</span>
              <span v-else class="text-gray-400 text-xs">—</span>
            </template>
          </el-table-column>
          <el-table-column prop="leaderName" label="专业群负责人" width="140">
            <template #default="scope">
              <div v-if="scope.row.level === 3" class="principal-cell">
                <div class="avatar-circle">
                  {{ scope.row.leaderName ? scope.row.leaderName.charAt(0) : '—' }}
                </div>
                {{ scope.row.leaderName || '—' }}
              </div>
              <span v-else class="text-gray-400 text-xs">—</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right" align="center">
            <template #default="scope">
              <!-- 三级指标可查看详情 -->
              <div v-if="scope.row.level === 3">
                <el-button link type="primary" size="small" @click="handleViewDetail(scope.row)">
                  <el-icon class="mr-1">
                    <View />
                  </el-icon>查看
                </el-button>
                <el-button v-if="canUsePermission('management.refresh', false)"
                    link
                    type="primary"
                    size="small"
                    :loading="refreshingPerfId === scope.row.perfId"
                    :disabled="refreshing && refreshingPerfId !== scope.row.perfId"
                    @click.stop="handleRefreshPerformance(scope.row)">
                  <el-icon v-if="refreshingPerfId !== scope.row.perfId" class="mr-1">
                    <Refresh />
                  </el-icon>{{ refreshingPerfId === scope.row.perfId ? '刷新中' : '刷新' }}
                </el-button>
                <el-button v-else link type="primary" size="small" @click.stop="reloadPerformanceData">刷新</el-button>
              </div>
              <!-- 一二级指标不展示操作，留白 -->
              <div v-else style="height: 24px;"></div>
            </template>
          </el-table-column>
        </VirtualTreeTable>
      </div>
    </div>

    <!-- 绩效详情抽屉 -->
    <el-drawer v-model="detailDrawerVisible" title="绩效指标详情" size="70%" :destroy-on-close="true">
      <div v-if="currentPerformance" ref="performanceContentRef" class="drawer-content" v-loading="detailLoading">
        <AdjacentDetails :items="performanceDetailItems" :current-key="String(currentPerformance.perfId)" :row-key="row => row.perfId"
          :busy="performanceToolsBusy" :dirty="() => performanceDraft() !== performanceSavedDraft" :open="openAdjacentPerformance" />
        <DetailActions :busy="performanceToolsBusy" :link="performanceDetailLink" :locate="locateCurrentPerformance" />
        <div v-if="canTransfer" class="owner-actions">
          <el-button size="small" type="primary" plain :icon="Switch"
            @click="openTransfer([currentPerformance])">移交</el-button>
        </div>
        <!-- 绩效基本信息 -->
        <div class="task-info-box" data-detail-section="基本信息">
          <div class="info-grid">
            <div class="info-row">
              <span class="label">指标编码：</span>
              <span class="value">{{ currentPerformance.code }}</span>
            </div>
            <div class="info-row">
              <span class="label">指标名称：</span>
              <span class="value">{{ currentPerformance.name }}</span>
            </div>
            <div class="info-row">
              <span class="label">目标值：</span>
              <span class="value">{{ formatNumber(currentPerformance.targetValue) }}</span>
            </div>
            <div class="info-row">
              <span class="label">当前完成值：</span>
              <span class="value">{{ formatNumber(currentPerformance.currentValue) }}</span>
            </div>
            <div class="info-row">
              <span class="label">完成率：</span>
              <span class="value">{{ currentPerformance.progress }}%</span>
            </div>
            <div class="info-row">
              <span class="label">数据类型：</span>
              <span class="value">
                <el-tag :type="getDataTypeTag(currentPerformance.dataType)" effect="plain" size="small">
                  {{ getDataTypeText(currentPerformance.dataType) }}
                </el-tag>
              </span>
            </div>
            <div class="info-row">
              <span class="label">归口部门：</span>
              <span class="value">{{ currentPerformance.dept || '—' }}</span>
            </div>
            <div class="info-row info-row--professional-owner">
              <span class="label">专业群负责人：</span>
              <span class="value">{{ currentPerformance.leaderName || '—' }}</span>
            </div>
          </div>
        </div>

        <!-- 根据指标类型显示不同的内容 -->
        <!-- 1.1.x 和 1.2.x 指标：显示关联任务列表 -->
        <div v-if="isAutoTaskPerformance">
          <div class="relation-title mt-6">
            <span class="section-title">关联任务列表</span>
            <el-button v-if="canManageRelation" size="small" type="primary" plain :icon="Switch"
              @click="openRelationDialog">编辑关联</el-button>
          </div>
          <el-table :data="relatedTasks" class="related-task-table" style="width: 100%" border stripe v-loading="tasksLoading"
                    :header-cell-style="{ background: '#f8f9fa', color: '#606266' }">
            <el-table-column prop="taskCode" label="任务编号" width="120" show-overflow-tooltip>
              <template #default="scope">
                <span class="font-mono">{{ scope.row.taskCode || '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="taskName" label="任务名称" min-width="200" show-overflow-tooltip>
              <template #default="scope">
                <span>{{ scope.row.taskName }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="phase" label="年度" width="80" align="center">
              <template #default="scope">
                {{ scope.row.phase || '—' }}
              </template>
            </el-table-column>
            <el-table-column label="贡献值" width="120" align="center">
              <template #default="scope">
                <span class="highlight-value">{{ formatNumber(scope.row.contributionValue || 0) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="weight" label="权重" width="80" align="center">
              <template #default="scope">
                {{ scope.row.weight || '1.00' }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="任务状态" width="100" align="center">
              <template #default="scope">
                <el-tag :type="getTaskStatusTag(scope.row.status)" size="small">
                  {{ mapTaskStatus(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template #default="scope">
                <el-button link type="primary" size="small" @click="jumpToTask(scope.row)">
                  查看
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <!-- 管理员可查看该指标本年度的关联变更记录 -->
          <div v-if="canManageRelation && relationLogs.length" class="relation-log mt-6">
            <div class="section-title">关联变更记录</div>
            <el-table :data="relationLogs" size="small" border v-loading="relationLogsLoading"
              empty-text="暂无关联变更记录">
              <el-table-column label="时间" width="160">
                <template #default="scope">{{ formatDateTime(scope.row.createTime) }}</template>
              </el-table-column>
              <el-table-column prop="operatorName" label="操作人" width="120" show-overflow-tooltip />
              <el-table-column label="动作" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.action === 'ADD' ? 'success' : 'danger'" size="small" effect="plain">
                    {{ scope.row.action === 'ADD' ? '新增关联' : '解除关联' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="任务" min-width="240" show-overflow-tooltip>
                <template #default="scope">
                  <span class="font-mono">{{ scope.row.taskCode }}</span> {{ scope.row.taskName }}
                </template>
              </el-table-column>
              <el-table-column prop="reason" label="原因" min-width="180" show-overflow-tooltip />
            </el-table>
          </div>
        </div>

        <!-- 其他指标（1.3.x, 2.x.x, 3.x.x）：显示手动提交表单 -->
        <!-- 其他指标（1.3.x, 2.x.x, 3.x.x）：显示手动提交表单 -->
        <div v-else>
          <div class="section-title mt-6">手动填报绩效</div>
          <div class="step-section mb-6" v-loading="performanceAuditLoading">
            <el-steps :active="performanceStepActive" finish-status="success" align-center class="custom-steps">
              <el-step title="绩效填报" :description="performanceStepDesc.submit"></el-step>
              <el-step title="专业群审核" :description="performanceStepDesc.auditor"></el-step>
              <el-step title="完结归档" :description="performanceStepDesc.archive"></el-step>
            </el-steps>
          </div>

          <div class="manual-submit-box">
            <!-- 年份未选择提示 -->
            <div v-if="!canSubmitManual" class="year-warning-tip">
              <el-alert
                  title="提示"
                  type="warning"
                  :closable="false"
                  show-icon
                  :description="manualSubmitDisabledReason">
                <template v-if="false" #default>
                  请先在上方筛选区选择具体年份，再进行填报
                </template>
              </el-alert>
            </div>

            <el-form :model="manualSubmitForm" :rules="manualSubmitRules" ref="manualSubmitFormRef" label-width="120px">
              <el-form-item label="当前年份" required>
                <el-input :value="currentYear" disabled style="width: 200px"></el-input>
              </el-form-item>
              <el-form-item label="目标值">
                <span class="target-value-display">{{ formatNumber(currentPerformance.targetValue) }}</span>
              </el-form-item>
              <el-form-item :label="manualValueLabel" prop="actualValue">
                <div class="progress-input-wrapper" @wheel="canSubmitManual ? handleManualProgressWheel : undefined">
                  <el-input-number
                      v-model="manualSubmitForm.actualValue"
                      :min="0"
                      :max="manualMaxValue"
                      :step="1"
                      :precision="0"
                      controls-position="right"
                      style="width: 240px"
                      placeholder="请输入完成值"
                      :disabled="!canSubmitManual"
                  />
                  <span v-if="manualIsPercent" class="percent-suffix">%</span>
                  <span class="wheel-tip">滚轮可增减</span>
                </div>
                <div class="form-tip">{{ manualValueTip }}</div>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="submitManualPerformance" :loading="manualSubmitting" :disabled="!canSubmitManual">
                  <el-icon class="mr-1"><UploadFilled /></el-icon>
                  提交填报
                </el-button>
                <el-button @click="resetManualForm" :disabled="!canSubmitManual">重置</el-button>
              </el-form-item>
            </el-form>

            <div class="performance-audit-box" v-if="currentManualAudit">
              <div class="section-title mt-6">绩效填报审核</div>
              <el-alert
                  :title="getManualAuditStatusText(currentManualAudit.status)"
                  :type="getManualAuditAlertType(currentManualAudit.status)"
                  :closable="false"
                  show-icon>
                <template #default>
                  <div>填报值：{{ currentManualAudit.actualValue }}%</div>
                  <div>填报人：{{ getUserName(currentManualAudit.submitBy) }}</div>
                  <div>填报时间：{{ formatDateTime(currentManualAudit.submitTime) }}</div>
                  <div v-if="currentManualAudit.auditComment">审核意见：{{ currentManualAudit.auditComment }}</div>
                </template>
              </el-alert>

              <div v-if="canAuditManualPerformance && currentManualAudit.status === 'pending'" class="audit-section mt-4">
                <el-form label-position="top" class="audit-form">
                  <el-form-item label="审核结果">
                    <el-radio-group v-model="manualAuditForm.result">
                      <el-radio :label="1" border class="radio-success">通过</el-radio>
                      <el-radio :label="0" border class="radio-danger">驳回</el-radio>
                    </el-radio-group>
                  </el-form-item>
                  <el-form-item label="审批意见">
                    <el-input v-model="manualAuditForm.comment" type="textarea" :rows="3" placeholder="请输入审批意见"></el-input>
                  </el-form-item>
                  <div class="form-actions text-right mt-4">
                    <el-button type="primary" @click="submitManualPerformanceAudit" :loading="manualAuditSubmitting">确认提交</el-button>
                  </div>
                </el-form>
              </div>
            </div>

            <!-- 提交记录提示 -->
            <div class="submit-history-tip" v-if="currentPerformance.submitHistory">
              <el-alert
                  title="上次提交记录"
                  type="info"
                  :closable="false"
                  show-icon>
                <template #default>
                  <div>提交时间：{{ currentPerformance.submitHistory.submitTime }}</div>
                  <div>提交值：{{ currentPerformance.submitHistory.actualValue }}%</div>
                  <div>提交人：{{ currentPerformance.submitHistory.submitter }}</div>
                </template>
              </el-alert>
            </div>
          </div>

          <el-alert
              v-if="latestPerformanceRejected"
              class="mt-4"
              title="已退回，可重新填报"
              type="warning"
              :closable="false"
              show-icon
              :description="latestPerformanceRejectReason"
          />

          <div v-if="performanceCanAudit && latestPerformanceAudit" class="audit-section mt-6">
            <h4 class="section-title">审核处理</h4>
            <el-form label-position="top" class="audit-form">
              <el-form-item label="审核结果">
                <el-radio-group v-model="performanceAuditForm.result">
                  <el-radio :label="1" border class="radio-success">通过</el-radio>
                  <el-radio :label="0" border class="radio-danger">驳回</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="审批意见">
                <el-input
                    v-model="performanceAuditForm.comment"
                    type="textarea"
                    :rows="3"
                    placeholder="请输入审批意见（必填）..."
                />
              </el-form-item>
              <div class="form-actions text-right mt-4">
                <el-button type="primary" @click="submitPerformanceAudit" :loading="performanceAuditSubmitting">确认提交</el-button>
              </div>
            </el-form>
          </div>

          <div class="log-section mt-6">
            <h4 class="section-title">绩效审核记录</h4>
            <template v-if="performanceAuditList && performanceAuditList.length">
              <el-timeline>
                <el-timeline-item
                    v-for="item in performanceAuditList"
                    :key="item.subId || item.sub_id"
                    :timestamp="formatDateTime(item.submitTime)"
                    placement="top">
                  <el-card shadow="never" class="log-card-item">
                    <div class="log-header">
                      <span class="log-user font-bold">审核单 {{ item.subId || item.sub_id }}</span>
                      <el-tag size="small" type="info">{{ getPerformanceFlowStatusDesc(item.flowStatus ?? item.flow_status) }}</el-tag>
                    </div>
                    <div class="log-content mt-2">
                      年度：{{ item.year || currentYear }}，
                      填报值：{{ formatNumber(item.actualValue ?? item.actual_value) }}
                    </div>
                    <div class="log-content mt-1 text-gray-400">
                      提交人：{{ getUserName(item.submitBy ?? item.submit_by) }}，
                      当前处理人：{{ getUserName(item.currentHandlerId ?? item.current_handler_id) || '—' }}
                    </div>
                    <div class="log-content mt-1" v-if="item.comment">
                      备注：{{ item.comment }}
                    </div>
                    <div class="mt-2">
                      <el-button link type="primary" size="small" @click.stop="togglePerformanceAuditItemLogs(item)">
                        {{ performanceAuditItemLogVisible[getPerformanceAuditSubKey(item)] ? '收起审批动作' : '查看审批动作' }}
                      </el-button>
                    </div>
                    <div class="mt-2" v-if="performanceAuditItemLogVisible[getPerformanceAuditSubKey(item)]">
                      <div v-if="performanceAuditItemLogLoading[getPerformanceAuditSubKey(item)]" class="text-gray-400">加载中...</div>
                      <template v-else>
                        <el-timeline v-if="performanceAuditItemLogList[getPerformanceAuditSubKey(item)] && performanceAuditItemLogList[getPerformanceAuditSubKey(item)].length">
                          <el-timeline-item
                              v-for="log in performanceAuditItemLogList[getPerformanceAuditSubKey(item)]"
                              :key="log.logId || (log.subId + '_' + log.createTime)"
                              :timestamp="formatDateTime(log.createTime)"
                              placement="top">
                            <el-card shadow="never" class="log-card-item">
                              <div class="log-header">
                                <span class="log-user font-bold">{{ getUserName(log.operatorId) }}</span>
                                <el-tag size="small" type="info">{{ getPerformanceAuditActionDesc(log.actionType) }}</el-tag>
                              </div>
                              <div class="log-content mt-2">
                                状态：{{ getPerformanceFlowStatusDesc(log.preStatus) }} → {{ getPerformanceFlowStatusDesc(log.postStatus) }}
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
            <el-empty v-else description="暂无绩效审核记录" :image-size="90" />
          </div>
        </div>

        <div class="drawer-footer mt-4" v-if="isAutoTaskPerformance && canUsePermission('management.refresh', false)">
          <el-button type="primary" @click="handleRefreshPerformance(currentPerformance)"
                     :loading="refreshingPerfId === currentPerformance?.perfId"
                     :disabled="refreshing && refreshingPerfId !== currentPerformance?.perfId">
            <el-icon v-if="refreshingPerfId !== currentPerformance?.perfId" class="mr-1"><Refresh /></el-icon>
            {{ refreshingPerfId === currentPerformance?.perfId ? '正在刷新' : '刷新指标' }}
          </el-button>
        </div>
        <div class="drawer-footer mt-4" v-else-if="isAutoTaskPerformance">
          <el-button @click="reloadPerformanceData">重新读取</el-button>
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
      </div>
    </el-drawer>

    <!-- 任务详情抽屉 -->
    <el-drawer v-model="taskDrawerVisible" size="70%" :destroy-on-close="true" @closed="handleTaskDrawerClosed">
      <template #header>
        <div class="drawer-header">
          <el-tooltip v-if="taskReturnToPerformance" content="返回绩效指标详情" placement="bottom">
            <el-button class="drawer-back-btn" text circle :icon="ArrowLeft" aria-label="返回绩效指标详情"
                       @click="handleBackToPerformanceDetail" />
          </el-tooltip>
          <span class="drawer-title">任务详情与反馈</span>
        </div>
      </template>
      <div v-if="currentTask" class="drawer-content" v-loading="taskDrawerLoading">
        <!-- 任务基本信息 -->
        <div class="task-info-box">
          <div class="info-grid">
            <div class="info-row">
              <span class="label">任务编号：</span>
              <span class="value">{{ currentTask.code }}</span>
            </div>
            <div class="info-row">
              <span class="label">任务名称：</span>
              <span class="value">{{ currentTask.name }}</span>
            </div>
            <div class="info-row">
              <span class="label">当前进度：</span>
              <span class="value">{{ currentTask.progress }}%</span>
            </div>
            <div class="info-row">
              <span class="label">归口部门：</span>
              <span class="value">{{ currentTask.dept }}</span>
            </div>
            <div class="info-row">
              <span class="label">预期达成目标：</span>
              <span class="value">{{ currentTask?._raw?.expTarget || '—' }}</span>
            </div>
            <div class="info-row info-row--full">
              <span class="label">预期成果成效：</span>
              <span class="value">{{ currentTask?._raw?.expEffect || '—' }}</span>
            </div>
            <div class="info-row info-row--full">
              <span class="label">预期佐证清单：</span>
              <span class="value">{{ currentTask?._raw?.expMaterialDesc || '—' }}</span>
            </div>
          </div>
        </div>

        <!-- 审核流程步骤条 -->
        <div class="step-section mb-6">
          <el-steps :active="taskStepActive" finish-status="success" align-center class="custom-steps">
            <el-step title="材料提交" :description="taskStepDesc.submit"></el-step>
            <el-step title="专业群审核" :description="taskStepDesc.dept"></el-step>
            <el-step title="归口审核" :description="taskStepDesc.principal"></el-step>
            <el-step title="完结归档" :description="taskStepDesc.admin"></el-step>
          </el-steps>
        </div>

        <!-- 反馈填报区域（仅负责人可见） -->
        <div class="section-title mt-6">进度反馈与材料上报</div>
        <el-form label-position="top" class="feedback-form">
          <template v-if="taskForthTasks && taskForthTasks.length > 0">
            <el-form-item
                v-for="task in taskForthTasks"
                :key="task.taskId ?? task.task_id"
                :label="task.taskName ?? task.task_name"
            >
              <div class="progress-input-wrapper">
                <el-input-number
                    v-model="taskForthFeedbackData[task.taskId ?? task.task_id].progress"
                    :min="0"
                    :max="String(task.dataType ?? task.data_type) === '2' ? 100 : 999999999"
                    :step="1"
                    :precision="0"
                    controls-position="right"
                    style="width: 240px"
                    :disabled="!taskCanSubmitMaterial"
                />
                <span v-if="String(task.dataType ?? task.data_type) === '2'" class="percent-suffix">%</span>
              </div>
            </el-form-item>
          </template>

          <template v-else>
            <el-form-item :label="taskIsPercent ? '本次进度更新(%)' : '本次填报值'">
              <div class="progress-input-wrapper" :title="taskIsPercent ? '可使用鼠标滚轮增减（每次±1）' : ''"
                   @wheel="handleTaskProgressWheel">
                <el-input-number v-model="taskFeedbackForm.progress" :min="0" :max="taskIsPercent ? 100 : 999999999" :step="1"
                                 :precision="0" controls-position="right" style="width: 240px" :disabled="!taskCanSubmitMaterial" />
                <span v-if="taskIsPercent" class="percent-suffix">%</span>
                <span v-if="taskIsPercent" class="wheel-tip">滚轮可增减</span>
              </div>
            </el-form-item>
          </template>

          <el-form-item label="完成情况" v-if="taskCanSubmitMaterial || taskIsAnyAuditor">
            <el-input v-model="taskFeedbackForm.comment" type="textarea" :rows="2" :maxlength="50"
                      :show-word-limit="taskCanSubmitMaterial" :disabled="!taskCanSubmitMaterial"
                      :placeholder="taskCanSubmitMaterial ? '请输入完成情况（50字以内）' : '暂无完成情况'" />
          </el-form-item>

          <el-form-item label="佐证材料上传" v-if="taskCanSubmitMaterial">
            <el-upload class="upload-area" :class="{ 'hide-upload-trigger': taskFileList.length >= 1 }" drag action="#"
                       :limit="1" :on-exceed="handleTaskExceed" :on-change="handleTaskFileChange" :on-remove="handleTaskRemove"
                       :on-preview="handleTaskPreview" :file-list="taskFileList" :auto-upload="false" accept=".doc,.docx,.pdf">
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

          <div class="form-actions text-right mb-4">
            <el-button type="primary" @click="submitTaskFeedback" :disabled="!taskCanSubmitMaterial || taskDrawerLoading" :loading="taskFeedbackSubmitting">提交反馈</el-button>
          </div>
        </el-form>

        <!-- 审核处理区域（仅审核人可见） -->
        <div v-if="taskCanAudit && taskLatestAudit" ref="taskAuditSectionRef" class="audit-section mt-6">
          <h4 class="section-title">审核处理</h4>
          <el-form label-position="top" class="audit-form">
            <el-form-item label="审核结果">
              <el-radio-group v-model="taskAuditForm.result">
                <el-radio :label="1" border class="radio-success">通过</el-radio>
                <el-radio :label="0" border class="radio-danger">驳回</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="审批意见">
              <el-input v-model="taskAuditForm.comment" type="textarea" :rows="3" placeholder="请输入审批意见（必填）..."></el-input>
            </el-form-item>
            <el-form-item label="佐证材料" v-if="taskLatestAudit.fileId || taskLatestAudit.file_id">
              <div class="file-link" @click="previewTaskMaterialFile(taskLatestAudit)">
                <el-icon>
                  <Document />
                </el-icon>
                <span>{{ getTaskFileName(taskLatestAudit.fileId || taskLatestAudit.file_id) }}</span>
              </div>
            </el-form-item>
            <div class="form-actions text-right mt-4">
              <el-button type="primary" @click="submitTaskAudit" :loading="taskAuditSubmitting" :disabled="taskDrawerLoading">确认提交</el-button>
            </div>
          </el-form>
        </div>

        <!-- 操作日志时间轴 -->
        <div class="log-section mt-6">
          <h4 class="section-title">操作日志与审核记录</h4>
          <div class="log-timeline-box">
            <div class="sub-title">操作日志</div>
            <template v-if="taskAuditLogs && taskAuditLogs.length">
              <el-timeline>
                <el-timeline-item v-for="log in taskAuditLogs" :key="log.logId || (log.subId + '_' + log.createTime)"
                                  :timestamp="formatDateTime(log.createTime)" placement="top">
                  <el-card shadow="never" class="log-card-item">
                    <div class="log-header">
                      <span class="log-user font-bold">{{ getUserName(log.operatorId) }}</span>
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
            <el-empty v-else description="暂无操作日志" :image-size="90" />

            <div class="sub-title mt-4">审批记录</div>
            <template v-if="taskAuditList && taskAuditList.length">
              <el-timeline>
                <el-timeline-item v-for="item in taskAuditList" :key="item.subId || item.sub_id || item.submitTime"
                                  :timestamp="formatDateTime(item.submitTime)" placement="top">
                  <el-card shadow="never" class="log-card-item">
                    <div class="log-header">
                      <span class="log-user font-bold">审批单 {{ item.subId || item.sub_id }}</span>
                      <el-tag size="small" type="info">{{ taskSubmissionStatus(item, taskAuditLogs, getFlowStatusDesc(item.flowStatus ?? item.flow_status)) }}</el-tag>
                    </div>
                    <div class="log-content mt-2">
                      本次填报值：{{ item.reportedValue ?? item.reported_value ?? '-' }}，
                      数据类型：{{ item.dataType ?? item.data_type ?? '-' }}
                    </div>
                    <div class="log-content mt-1 text-gray-400">
                      提交人：{{ getUserName(item.submitBy ?? item.submit_by) }}，
                      当前处理人：{{ getUserName(item.currentHandlerId ?? item.current_handler_id) }}
                    </div>
                    <div class="log-file mt-2" v-if="item.fileId || item.file_id">
                      <el-icon>
                        <Document />
                      </el-icon>
                      <span class="file-name">{{ getTaskFileName(item.fileId || item.file_id) }}</span>
                      <span class="preview-text" @click.stop="previewTaskMaterialFile(item)">点击预览</span>
                      <span class="preview-text" @click.stop="downloadTaskByFileId(item.fileId || item.file_id, item.fileSuffix || item.file_suffix)">点击下载</span>
                    </div>

                    <div class="mt-2">
                      <el-button link type="primary" size="small" @click.stop="toggleTaskAuditItemLogs(item)">
                        {{ taskAuditItemLogVisible[getTaskAuditSubKey(item)] ? '收起审批动作' : '查看审批动作' }}
                      </el-button>
                    </div>
                    <div class="mt-2" v-if="taskAuditItemLogVisible[getTaskAuditSubKey(item)]">
                      <div v-if="taskAuditItemLogLoading[getTaskAuditSubKey(item)]" class="text-gray-400">加载中...</div>
                      <template v-else>
                        <el-timeline v-if="taskAuditItemLogList[getTaskAuditSubKey(item)] && taskAuditItemLogList[getTaskAuditSubKey(item)].length">
                          <el-timeline-item v-for="log in taskAuditItemLogList[getTaskAuditSubKey(item)]"
                                            :key="log.logId || (log.subId + '_' + log.createTime)"
                                            :timestamp="formatDateTime(log.createTime)" placement="top">
                            <el-card shadow="never" class="log-card-item">
                              <div class="log-header">
                                <span class="log-user font-bold">{{ getUserName(log.operatorId) }}</span>
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
      </div>
    </el-drawer>

    <!-- 任务文件预览弹窗 -->
    <el-dialog v-model="taskPreviewVisible" title="文件预览" width="60%" destroy-on-close @closed="cleanupTaskPreviewUrl">
      <div class="preview-container">
        <div v-if="taskPreviewType === 'pdf'" class="pdf-viewer">
          <div class="pdf-viewer-header">
            <span class="file-name-display">{{ taskPreviewFileName }}</span>
            <el-button type="primary" :icon="Download" @click="downloadTaskPreviewFile" :disabled="!taskPreviewUrl">
              下载文件
            </el-button>
          </div>
          <iframe :src="taskPreviewUrl" width="100%" height="600px" style="border:none;" allow="fullscreen"></iframe>
        </div>
        <div v-else class="doc-viewer">
          <div class="doc-icon">
            <el-icon :size="60" color="#409eff">
              <Document />
            </el-icon>
          </div>
          <h3 class="mt-4">{{ taskPreviewFileName }}</h3>
          <p class="text-gray-500 mt-2">
            该文件为 Word 文档 (.doc/.docx)，暂不支持在线直接渲染。<br>
            在实际生产环境中，此处会集成 Office Web Apps 或 PDF 转换服务进行预览。
          </p>
          <el-button type="primary" class="mt-4" @click="downloadTaskPreviewFile">下载当前文件</el-button>
        </div>
      </div>
    </el-dialog>
    <TransferDialog ref="transferRef" target-type="PERFORMANCE" :items="transferRows" @transferred="handleTransferred" />
    <PerformanceRelationDialog ref="relationRef" @saved="handleRelationSaved" />
  </div>
</template>

<script setup>
import { taskStageDescription, currentTaskSubmission, canProcessTaskSubmission, taskWorkflowStep, taskSubmissionStatus, taskAuditPostStatus } from '@/utils/taskAuditTimeline.mjs';

const queryBar = ref(null), queryIds = ref(new Set());
const performanceQueryState=ref({loading:true,initialized:false,error:''});
let performanceListVersion=0;
const beforePerformanceQuery=async query=>{
  if(String(query.year||'')===String(filterYear.value||''))return true;
  if(detailDrawerVisible.value){
    if(performanceToolsBusy.value)return false;
    if(!await confirmDetailLeave(performanceDraft()!==performanceSavedDraft))return false;
    detailDrawerVisible.value=false;
  }
  invalidatePerformanceAudit();
  currentPerformance.value=null;
  return true;
};

watch(()=>performanceQueryState.value.responseCode,code=>{if([401,403,428].includes(Number(code))){detailDrawerVisible.value=false;allPerformances.value=[];queryIds.value=new Set();applyFilters();}})
watch(()=>performanceQueryState.value.error,error=>{if(error)loading.value=false})
const receiveQuery = async result => {
  const listVersion=++performanceListVersion;
  const items=result.items||[];
  const names=new Map(userMap.value),departments=new Map(deptMap.value);
  for(const item of items){if(item.deptName)departments.set(String(item.deptId),item.deptName);for(const role of ['leader','auditor','principal'])if(item[role+'Name'])names.set(String(item[role+'Id']),fixUserNameEncoding(item[role+'Name']));}
  userMap.value=names;deptMap.value=departments;allPerformances.value=items;
  void loadUserAndDeptMaps(items).then(()=>{if(performanceDetailDisposed||listVersion!==performanceListVersion)return;const refresh=nodes=>{for(const node of nodes){if(node.level===3){if(!node._raw.deptName)node.dept=getDeptName(node.deptId);if(!node._raw.principalName)node.principal=getUserName(node.principalId);if(!node._raw.auditorName)node.auditorName=getUserName(node.auditorId);if(!node._raw.leaderName)node.leaderName=getUserName(node.leaderId);}if(node.children)refresh(node.children)}};refresh(performanceTreeData.value);});
  queryIds.value = new Set((result.matchedIds||[]).map(String));
  const nextYear = result.query.year === 'missing' ? '' : String(result.query.year || '');
  const changed = filterYear.value !== nextYear;
  filterYear.value = nextYear;
  if(changed){detailDrawerVisible.value=false;currentPerformance.value=null;}
  if(!result.background){listFilters.value = { dataType: '', owner: '', leader: '', mode: '', auditor: '' }; quickFilter.value='all'; actualSearchText.value=''; searchText.value='';}applyFilters();loading.value=false;
  if(detailDrawerVisible.value&&currentPerformance.value){
    const fresh=performanceDetailItems.value.find(row=>String(row.perfId)===String(currentPerformance.value.perfId));
    if(fresh)currentPerformance.value=fresh;
  }
  await nextTick();
  if(performanceDetailDisposed||listVersion!==performanceListVersion)return;
  if(route.query.perfId && String(currentPerformance.value?.perfId || '') !== String(route.query.perfId))await openPerformanceLink();
  if(!expandedInitialized.value){expandedRowKeys.value=performanceTreeData.value.map(item=>String(item.id));expandedInitialized.value=true;}
  await performanceMemory.restoreTree(performanceTreeData.value);
};
import { isAdminTestEnabled } from '@/utils/adminTest';
import { adminTestFetch } from '@/utils/adminTest';

import { accessState, canUsePermission } from '@/utils/accessState';
import { ref, reactive, computed, onMounted, onUnmounted, watch, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import DetailActions from './DetailActions.vue';
import TransferDialog from './TransferDialog.vue';
import PerformanceRelationDialog from './PerformanceRelationDialog.vue';
import QueryFilterBar from './QueryFilterBar.vue';
import VirtualTreeTable from './VirtualTreeTable.vue';
import AdjacentDetails from './AdjacentDetails.vue';
import { useBrowseMemory } from '@/utils/browseMemory';
import { get } from '@/utils/request';
import { detailLeaves } from '@/utils/detailNavigation';
import { detailRowClass, locateDetailRow, confirmDetailLeave, findDetailPath } from '@/utils/detailNavigation';
import {
  Calendar, User, Plus, Download, View, Refresh, UploadFilled, Warning, Document, ArrowLeft, Switch
} from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { refreshPerformance, getTasksByPerfId, submitPerformanceManually, getPerformanceAuditsByPerfAndYear, auditPerformance, getPerformanceAuditLogs } from '@/api/performance';
import { getPerformanceRelationCapabilities, getPerformanceRelationCounts, getPerformanceRelationLogs } from '@/api/performance';
import { getTaskById, getForthTasks } from '@/api/task';
import { getAllUsers } from '@/api/user';
import { getTransferCapabilities, getTransferLogs } from '@/api/transfer';
import { getDeptById } from '@/api/dept';
import { getAudit, getAuditByTaskId, auditTask, getAuditLogs, reSubmitMaterial } from '@/api/audit';
import { uploadFile, submitMaterial, batchSubmitMaterial } from '@/api/report';

const route = useRoute();
const router = useRouter();

// --- 绩效相关状态管理 ---
const loading = ref(false);
const detailLoading = ref(false);
const tasksLoading = ref(false);
const refreshing = ref(false);
const refreshingPerfId = ref(null);
const getDefaultPerformanceYear = () => {
  const linkedYear=String(route.query.year||'');
  if(route.query.perfId&&/^(202[5-9])$/.test(linkedYear))return linkedYear;
  const current = new Date().getFullYear();
  return current >= 2025 && current <= 2029 ? String(current) : '2026';
};
const filterYear = ref(getDefaultPerformanceYear());
const listFilters = ref({ dataType: '', owner: '', leader: '', mode: '', auditor: '' });
const performanceMode = computed({ get: () => listFilters.value.mode, set: value => { listFilters.value.mode = value; } });
const performanceAuditor = computed({ get: () => listFilters.value.auditor, set: value => { listFilters.value.auditor = value; } });
const filterDataType = computed({ get: () => listFilters.value.dataType, set: value => { listFilters.value.dataType = value; } });
const ownerFilter = computed({ get: () => listFilters.value.owner, set: value => { listFilters.value.owner = value; } });
const leaderFilter = computed({ get: () => listFilters.value.leader, set: value => { listFilters.value.leader = value; } });

const searchText = ref('');
const actualSearchText = ref('');
const performanceTableRef = ref(null);
// --- 管理员归属移交 ---
const canTransfer = ref(false);
const canManageRelation = ref(false);
const transferRef = ref(null);
const transferRows = ref([]);
// 批量移交模式：默认隐藏勾选列，点“批量移交”后才出现，退出时清空已选
const transferMode = ref(false);
const transferSelection = ref([]);
const transferLogs = ref([]);
const transferLogsLoading = ref(false);
const loadTransferPermission = async () => {
  try {
    const [transfer, relation] = await Promise.all([
      getTransferCapabilities(),
      getPerformanceRelationCapabilities()
    ]);
    canTransfer.value = canUsePermission('performance.transfer', transfer?.canTransfer === true);
    canManageRelation.value = canUsePermission('performance.relations', relation?.canManageRelation === true);
  } catch {
    canTransfer.value = false;
    canManageRelation.value = false;
  }
};
// --- 管理员编辑绩效关联任务（按详情抽屉当前年度维护） ---
const relationRef = ref(null);
const relationLogs = ref([]);
const relationLogsLoading = ref(false);
const relationCounts = ref({});
const relationCountsLoaded = ref(false);
let relationCountRequest = 0;
const relationCountOf = row => {
  const count = relationCounts.value[String(row.perfId ?? row._raw?.perfId ?? '')];
  return count === undefined || count === null ? 0 : Number(count);
};
const isAutoCode = code => {
  const value = String(code || '');
  return value.startsWith('1.1.') || value.startsWith('1.2.');
};
const loadRelationCounts = async () => {
  if (!canManageRelation.value || !filterYear.value) return;
  const requestId = ++relationCountRequest;
  try {
    const data = await getPerformanceRelationCounts(filterYear.value);
    if (requestId === relationCountRequest) {
      relationCounts.value = data || {};
      relationCountsLoaded.value = true;
      // 数量只更新对应单元格；是否关联已由统一查询筛选，不再重建整棵树。
    }
  } catch {
    if (requestId === relationCountRequest) {
      relationCounts.value = {};
      relationCountsLoaded.value = false;
    }
  }
};
const loadRelationLogs = async (perfId) => {
  if (!canManageRelation.value || !perfId || !filterYear.value) {
    relationLogs.value = [];
    return;
  }
  relationLogsLoading.value = true;
  try {
    const logs = await getPerformanceRelationLogs(perfId, filterYear.value);
    relationLogs.value = Array.isArray(logs) ? logs : [];
  } catch {
    relationLogs.value = [];
  } finally {
    relationLogsLoading.value = false;
  }
};
const openRelationDialog = () => {
  if (!currentPerformance.value) return;
  relationRef.value?.open(currentPerformance.value, filterYear.value);
};
// 关联变化会重算完成值，列表、关联任务与变更记录都要刷新
const handleRelationSaved = async () => {
  await loadPerformances();
  await loadRelatedTasks(currentPerformance.value?.perfId ?? currentPerformance.value?._raw?.perfId, filterYear.value || null);
  await loadRelationLogs(currentPerformance.value?.perfId ?? currentPerformance.value?._raw?.perfId);
  await loadRelationCounts();
};
const handleTransferSelectionChange = (rows) => {
  transferSelection.value = (rows || []).filter(row => Number(row?.level) === 3);
};
const toTransferRow = (row) => ({
  id: Number(row.perfId ?? row._raw?.perfId),
  code: row.code || row._raw?.perfCode || '',
  name: row.name || row._raw?.perfName || '',
  deptId: row.deptId ?? row._raw?.deptId ?? null,
  deptName: row.dept || '',
  leaderId: row.leaderId ?? row._raw?.leaderId ?? null,
  leaderName: row.leaderName || '',
  auditorId: row.auditorId ?? row._raw?.auditorId ?? null,
  auditorName: row.auditorName || row._raw?.auditorName || '',
  principalId: row.principalId ?? row._raw?.principalId ?? null,
  principalName: row.principal || ''
});
const openTransfer = (rows) => {
  const list = (Array.isArray(rows) ? rows : [rows])
    .filter(Boolean)
    .map(toTransferRow)
    .filter(row => Number.isFinite(row.id) && row.id > 0);
  if (!list.length) {
    ElMessage.warning('请选择需要移交的三级指标');
    return;
  }
  transferRows.value = list;
  transferRef.value?.open();
};
const enterTransferMode = () => {
  transferMode.value = true;
  transferSelection.value = [];
};
const exitTransferMode = () => {
  transferMode.value = false;
  transferSelection.value = [];
  performanceTableRef.value?.clearSelection();
};
const loadTransferLogs = async (perfId) => {
  if (!canTransfer.value || !perfId) {
    transferLogs.value = [];
    return;
  }
  transferLogsLoading.value = true;
  try {
    const logs = await getTransferLogs('PERFORMANCE', perfId);
    transferLogs.value = Array.isArray(logs) ? logs : [];
  } catch {
    transferLogs.value = [];
  } finally {
    transferLogsLoading.value = false;
  }
};
const handleTransferred = async () => {
  exitTransferMode();
  await loadPerformances();
  await loadTransferLogs(currentPerformance.value?.perfId ?? currentPerformance.value?._raw?.perfId);
};
const expandedRowKeys = ref([]);
const quickFilter = ref('all');
const shortcutError = ref('');
const performanceShortcuts = ref({ returned: [], todo: [] });
const performanceContentRef = ref(null);
const performanceMemory = useBrowseMemory('performance', { fields: { year: filterYear, dataType: filterDataType, search: actualSearchText, quick: quickFilter, owner: ownerFilter, leader: leaderFilter, mode: performanceMode, auditor: performanceAuditor },
  expanded: expandedRowKeys, table: performanceTableRef, skip: () => !!route.query.perfId });
if (!route.query.perfId) searchText.value = actualSearchText.value;
const expandedInitialized = ref(false);
const detailDrawerVisible = ref(false);
const currentPerformance = ref(null);
const performanceDetailYear = ref(null);
const relatedTasks = ref([]);
const performanceAuditList = ref([]);
const performanceAuditLoading = ref(false);
const performanceAuditItemLogVisible = reactive({});
const performanceAuditItemLogLoading = reactive({});
const performanceAuditItemLogList = reactive({});
let performanceAuditRequestId = 0;
let performanceDetailVersion = 0;
let performanceDetailDisposed = false;

// --- 手动提交相关状态 ---
const manualSubmitting = ref(false);
const manualSubmitFormRef = ref(null);
const manualSubmitForm = reactive({
  actualValue: 0
});

const performanceAuditSubmitting = ref(false);
const performanceAuditForm = reactive({
  result: 1,
  comment: '同意'
});
const manualAuditSubmitting = ref(false);
const manualAuditForm = reactive({
  result: 1,
  comment: '同意'
});
const manualPerformanceAudits = ref([]);

// 表单验证规则
const manualSubmitRules = {
  actualValue: [
    { required: true, message: '请输入完成值', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value === undefined || value === null || value === '') {
          callback(new Error('请输入完成值'));
        } else if (value < 0 || value > manualMaxValue.value) {
          callback(new Error('完成值必须在0-100之间'));
        } else if (!Number.isInteger(value)) {
          callback(new Error('完成值必须为整数'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
};

const MANUAL_PERFORMANCE_AUDIT_KEY = 'manual_performance_audits';

const loadManualPerformanceAudits = () => {
  try {
    const raw = localStorage.getItem(MANUAL_PERFORMANCE_AUDIT_KEY);
    const list = raw ? JSON.parse(raw) : [];
    manualPerformanceAudits.value = Array.isArray(list) ? list : [];
  } catch (e) { // eslint-disable-line no-unused-vars
    manualPerformanceAudits.value = [];
  }
};

const saveManualPerformanceAudits = () => {
  localStorage.setItem(MANUAL_PERFORMANCE_AUDIT_KEY, JSON.stringify(manualPerformanceAudits.value));
};

const getManualAuditKey = (perfId, year) => `${perfId || ''}_${year || ''}`;

const currentManualAudit = computed(() => {
  const perfId = currentPerformance.value?.perfId;
  const year = currentYear.value;
  if (!perfId || !year) return null;
  const key = getManualAuditKey(perfId, year);
  return manualPerformanceAudits.value.find(item => item.key === key && item.status === 'pending')
      || manualPerformanceAudits.value.find(item => item.key === key)
      || null;
});

const canAuditManualPerformance = computed(() => {
  if (!canUsePermission('performance.audit') && !canUsePermission('performance.archive')) return false;
  const auth = getCurrentAuthInfo();
  if (!auth || !currentPerformance.value) return false;
  if (auth.role === '0') return true;
  const auditorId = currentPerformance.value.auditorId ?? currentPerformance.value._raw?.auditorId ?? currentPerformance.value._raw?.auditor_id;
  return auditorId !== undefined && auditorId !== null && Number(auditorId) === Number(auth.userId);
});

const getManualAuditStatusText = (status) => {
  if (status === 'approved') return '审核通过';
  if (status === 'rejected') return '已驳回';
  return '待审核';
};

const getManualAuditAlertType = (status) => {
  if (status === 'approved') return 'success';
  if (status === 'rejected') return 'error';
  return 'warning';
};

// 当前年份（从筛选器获取或默认当前年份）
const currentYear = computed(() => {
  if(currentPerformance.value&&performanceDetailYear.value)return performanceDetailYear.value;
  if (filterYear.value && filterYear.value !== '') {
    return filterYear.value;
  }
  return new Date().getFullYear().toString();
});

// 审核记录只属于打开时的绩效和年份，关闭后重开同一条也视为新的详情。
const getPerformanceAuditContext = () => ({
  perfId: Number(currentPerformance.value?.perfId ?? currentPerformance.value?._raw?.perfId),
  year: Number(currentYear.value),
  version: performanceDetailVersion
});

const isCurrentPerformanceAuditContext = (context) => {
  const current = getPerformanceAuditContext();
  return !performanceDetailDisposed && detailDrawerVisible.value
      && context.version === current.version
      && context.perfId === current.perfId && context.year === current.year;
};

const matchesPerformanceAuditContext = (audit, context) => {
  return Number(audit?.perfId ?? audit?.perf_id) === context.perfId
      && Number(audit?.year) === context.year;
};

const clearPerformanceAuditLogs = () => {
  [performanceAuditItemLogVisible, performanceAuditItemLogLoading, performanceAuditItemLogList].forEach(map => {
    Object.keys(map).forEach(key => { delete map[key]; });
  });
};

const invalidatePerformanceAudit = () => {
  performanceDetailVersion += 1;
  performanceAuditRequestId += 1;
  performanceAuditList.value = [];
  performanceAuditLoading.value = false;
  performanceAuditSubmitting.value = false;
  clearPerformanceAuditLogs();
};

// 判断是否为自动任务指标（1.1.x 和 1.2.x 从任务汇总）
const isAutoTaskPerformance = computed(() => {
  if (!currentPerformance.value) return false;
  // 获取编码，尝试多种来源
  const code = currentPerformance.value.code ||
      currentPerformance.value._raw?.perfCode ||
      '';

  // 1.1.x 和 1.2.x 的指标从任务汇总
  // 其他指标（1.3.x, 2.x.x, 3.x.x）手动提交
  const isAuto = code.startsWith('1.1.') || code.startsWith('1.2.');

  return isAuto;
});

const manualIsPercent = computed(() => String(currentPerformance.value?.dataType ?? currentPerformance.value?._raw?.dataType) === '2');
const manualMaxValue = computed(() => manualIsPercent.value ? 100 : 999999999);
const manualValueLabel = computed(() => manualIsPercent.value ? '当前完成值(%)' : '当前完成值');
const manualValueTip = computed(() => manualIsPercent.value ? '请输入0-100之间的整数百分比' : '请输入大于等于0的整数数值');
const manualSubmitDisabledReason = computed(() => {
  if (!filterYear.value || filterYear.value === '') {
    return '请先在上方筛选区选择具体年份，再进行填报';
  }
  const auth = getCurrentAuthInfo();
  if (!auth) {
    return '请先登录后再进行填报';
  }
  if (auth.role === '0') {
    return '';
  }
  const leaderId = currentPerformance.value?.leaderId ?? currentPerformance.value?._raw?.leaderId;
  if (leaderId !== undefined && leaderId !== null && Number(leaderId) === Number(auth.userId)) {
    return '';
  }
  return '当前账号不是该绩效的填报负责人，仅负责人或管理员可以填报';
});

const latestPerformanceAudit = computed(() => {
  if (!performanceAuditList.value || performanceAuditList.value.length === 0) return null;
  return performanceAuditList.value[0];
});

const latestPerformanceRejected = computed(() => {
  const audit = latestPerformanceAudit.value;
  if (!audit) return false;
  return Number(audit.flowStatus ?? audit.flow_status) < 0;
});

const latestPerformanceRejectReason = computed(() => {
  const audit = latestPerformanceAudit.value;
  const comment = audit?.comment ? `退回意见：${audit.comment}` : '请根据退回意见调整后重新填报。';
  return `${getPerformanceFlowStatusDesc(audit?.flowStatus ?? audit?.flow_status)}。${comment}`;
});

const performanceCanAudit = computed(() => {
  const stage = Number(latestPerformanceAudit.value?.flowStatus ?? latestPerformanceAudit.value?.flow_status);
  if (!canUsePermission(stage === 20 ? 'performance.archive' : 'performance.audit')) return false;
  const auth = getCurrentAuthInfo();
  const audit = latestPerformanceAudit.value;
  const context = getPerformanceAuditContext();
  if (!auth || !audit || performanceAuditLoading.value
      || !isCurrentPerformanceAuditContext(context)
      || !matchesPerformanceAuditContext(audit, context)) return false;
  const status = Number(audit.flowStatus ?? audit.flow_status);
  if (![10, 20].includes(status)) return false;
  if (auth.role === '0') return true;
  const handlerId = audit.currentHandlerId ?? audit.current_handler_id;
  return handlerId !== undefined && handlerId !== null && Number(handlerId) === Number(auth.userId);
});

const performanceStepActive = computed(() => {
  const audit = latestPerformanceAudit.value;
  if (!audit) return 0;
  const status = Number(audit.flowStatus ?? audit.flow_status);
  if (status === 10) return 1;
  if (status === 20) return 2;
  if (status === 30) return 3;
  if (status < 0) return 1;
  return 0;
});

const getPerformanceFlowStatusDesc = (status) => {
  const value = Number(status);
  const map = {
    10: '专业群审核中',
    20: '待完结归档',
    30: '已完结归档',
    '-10': '专业群退回',
    '-20': '归档退回',
    0: '已撤回'
  };
  return map[value] || '暂无提交';
};

const getPerformanceAuditActionDesc = (actionType) => {
  const map = {
    submit: '提交',
    pass: '通过',
    reject: '退回',
    withdraw: '撤回'
  };
  return map[actionType] || actionType || '操作';
};

const performanceStepDesc = computed(() => {
  const audit = latestPerformanceAudit.value;
  if (!audit) {
    return {
      submit: '暂无填报',
      auditor: `审核人：${currentPerformance.value?.auditorName || '—'}（待提交）`,
      archive: '归档人：管理员（待流转）'
    };
  }
  const status = Number(audit.flowStatus ?? audit.flow_status);
  const submitter = getUserName(audit.submitBy ?? audit.submit_by) || '—';
  const submitTime = formatDateTime(audit.submitTime);
  return {
    submit: `提交人：${submitter}${submitTime ? `（${submitTime}）` : ''}`,
    auditor: status === 10
        ? `审核人：${currentPerformance.value?.auditorName || '—'}（当前）`
        : status < 0
            ? getPerformanceFlowStatusDesc(status)
            : `审核人：${currentPerformance.value?.auditorName || '—'}（已通过）`,
    archive: status === 20
        ? `归档人：管理员（当前）`
        : status >= 30
            ? '归档人：管理员（已归档）'
            : status < 0
                ? getPerformanceFlowStatusDesc(status)
                : '归档人：管理员（待流转）'
  };
});

// 重置手动提交表单
const resetManualForm = () => {
  manualSubmitForm.actualValue = 0;
  if (manualSubmitFormRef.value) {
    manualSubmitFormRef.value.clearValidate();
  }
};

// 手动进度滚轮事件
const handleManualProgressWheel = (e) => {
  e.preventDefault();
  const cur = Number(manualSubmitForm.actualValue);
  const base = Number.isFinite(cur) ? cur : 0;
  const next = base + (e.deltaY < 0 ? 1 : -1);
  manualSubmitForm.actualValue = Math.max(0, Math.min(manualMaxValue.value, Math.round(next)));
};

// 提交手动绩效
const submitManualPerformance = async () => {
  if (manualSubmitting.value) return;
  const context = getPerformanceAuditContext();
  if (!isCurrentPerformanceAuditContext(context)) return;
  const perfId = context.perfId;
  const year = context.year;
  const actualValue = manualSubmitForm.actualValue;
  const name = currentPerformance.value.name;
  const isPercent = manualIsPercent.value;
  // 添加年份验证
  if (!canSubmitManual.value) {
    ElMessage.warning('请先在上方筛选区选择具体年份，再进行填报');
    return;
  }

  if (!manualSubmitFormRef.value) return;

  manualSubmitting.value = true;
  try {
    try {
      await manualSubmitFormRef.value.validate();
    } catch (e) { // eslint-disable-line no-unused-vars
      ElMessage.warning('请正确填写完成值（0-100的整数）');
      return;
    }

    if (!isCurrentPerformanceAuditContext(context)) return;
    if (actualValue !== manualSubmitForm.actualValue) return;

    if (!perfId) {
      ElMessage.error('未获取到绩效指标ID');
      return;
    }

    if (!year || isNaN(year)) {
      ElMessage.error('请先选择年份');
      return;
    }

    // 二次确认
    try {
      const valueLabel = isPercent ? '完成率' : '实际值';
      const displayValue = isPercent ? `${actualValue}%` : actualValue;
      await ElMessageBox.confirm(
          `确认提交「${name}」${year}年度${valueLabel}为 ${displayValue} 吗？`,
          '提交确认',
          {
            confirmButtonText: '确认提交',
            cancelButtonText: '取消',
            type: 'warning'
          }
      );
    } catch {
      return;
    }

    if (!isCurrentPerformanceAuditContext(context)) return;
    await submitPerformanceManually(perfId, actualValue, year);
    if (!isCurrentPerformanceAuditContext(context)) return;
    ElMessage.success('提交成功，已进入绩效审核流程');

    // 刷新数据
    await loadPerformances();
    if (!isCurrentPerformanceAuditContext(context)) return;
    await loadPerformanceAuditFlow(perfId, context);
    if (!isCurrentPerformanceAuditContext(context)) return;

    // 更新当前显示的绩效数据
    const updated = allPerformances.value.find(p => p.perfId === perfId);
    if (updated) {
      currentPerformance.value = {
        ...currentPerformance.value,
        targetValue: updated.targetValue,
        currentValue: updated.currentValue,
        progress: (() => {
          const target = Number(updated.targetValue || 0);
          const current = Number(updated.currentValue || 0);
          return target > 0 ? Math.min(100, Math.round((current / target) * 100)) : 0;
        })(),
        submitHistory: updated.submitHistory
      };
    }

    // 重置表单
    resetManualForm();

  } catch (e) {
    if (!isCurrentPerformanceAuditContext(context)) return;
    console.error('提交失败:', e);
    ElMessage.error(e?.message || '提交失败，请稍后重试');
  } finally {
    manualSubmitting.value = false;
  }
};

const submitPerformanceAudit = async () => {
  if (performanceAuditSubmitting.value) return;
  const context = getPerformanceAuditContext();
  const audit = latestPerformanceAudit.value;
  if (performanceAuditLoading.value || !isCurrentPerformanceAuditContext(context)
      || !matchesPerformanceAuditContext(audit, context) || !performanceCanAudit.value) {
    ElMessage.warning('当前绩效审核信息尚未就绪，请加载完成后重试');
    return;
  }
  if (!performanceAuditForm.comment) {
    ElMessage.warning('请输入审批意见');
    return;
  }

  const subId = audit?.subId ?? audit?.sub_id;
  if (!subId) {
    ElMessage.warning('未找到绩效审核单ID');
    return;
  }

  const payload = {
    sub_id: Number(subId),
    is_pass: performanceAuditForm.result === 1,
    title: performanceAuditForm.comment,
    content: performanceAuditForm.comment
  };
  performanceAuditSubmitting.value = true;
  try {
    await auditPerformance(payload);
    if (!isCurrentPerformanceAuditContext(context)) return;

    ElMessage.success(payload.is_pass ? '审核通过' : '已退回');
    performanceAuditForm.result = 1;
    performanceAuditForm.comment = '同意';

    await loadPerformanceAuditFlow(context.perfId, context);
    if (isCurrentPerformanceAuditContext(context)) {
      await loadPerformances();
    }
  } catch (e) {
    if (isCurrentPerformanceAuditContext(context)) {
      ElMessage.error(e?.message || '绩效审核提交失败，请稍后重试');
    }
  } finally {
    if (isCurrentPerformanceAuditContext(context)) performanceAuditSubmitting.value = false;
  }
};

// 展开动画
const animRowKeyMap = reactive({});

// 当前标签页
const currentTab = computed(() => {
  const path = route.path;
  if (path.includes('/performance')) return 'performance';
  if (path.includes('/achievement')) return 'achievement';
  if (path.includes('/budget')) return 'budget';
  return 'task';
});

// 切换标签页
const handleTabSwitch = (tab) => {
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

const submitManualPerformanceAudit = async () => {
  if (!currentManualAudit.value) {
    ElMessage.warning('未找到待审核记录');
    return;
  }
  if (!canAuditManualPerformance.value) {
    ElMessage.warning('当前账号无权审核该绩效填报');
    return;
  }
  if (!manualAuditForm.comment) {
    ElMessage.warning('请输入审批意见');
    return;
  }

  manualAuditSubmitting.value = true;
  try {
    const auditItem = currentManualAudit.value;
    const auth = getCurrentAuthInfo();

    if (manualAuditForm.result === 1) {
      await submitPerformanceManually(auditItem.perfId, auditItem.actualValue, auditItem.year);
    }

    const index = manualPerformanceAudits.value.findIndex(item => item.key === auditItem.key);
    if (index >= 0) {
      manualPerformanceAudits.value.splice(index, 1, {
        ...auditItem,
        status: manualAuditForm.result === 1 ? 'approved' : 'rejected',
        auditBy: auth?.userId || null,
        auditTime: new Date().toISOString(),
        auditComment: manualAuditForm.comment
      });
      saveManualPerformanceAudits();
    }

    ElMessage.success(manualAuditForm.result === 1 ? '审核通过，指标已更新' : '已驳回');
    manualAuditForm.result = 1;
    manualAuditForm.comment = '同意';

    await loadPerformances();
    const updated = allPerformances.value.find(p => p.perfId === auditItem.perfId);
    if (updated && currentPerformance.value?.perfId === auditItem.perfId) {
      currentPerformance.value = {
        ...currentPerformance.value,
        targetValue: updated.targetValue,
        currentValue: updated.currentValue,
        progress: (() => {
          const target = Number(updated.targetValue || 0);
          const current = Number(updated.currentValue || 0);
          return target > 0 ? Math.min(100, Math.round((current / target) * 100)) : 0;
        })(),
        submitHistory: updated.submitHistory
      };
    }
  } catch (e) {
    console.error('绩效审核失败:', e);
    ElMessage.error(e?.message || '审核提交失败，请稍后重试');
  } finally {
    manualAuditSubmitting.value = false;
  }
};

// --- 用户和部门映射 ---
const userMap = ref(new Map());
const deptMap = ref(new Map());
const allUsers = ref([]);
// 首屏秒开缓存：按登录会话隔离（sessionStorage + token 后缀），命中后先出表再后台刷新
const USERS_CACHE_KEY = 'works_users_cache_v1';
const DEPTS_CACHE_KEY = 'works_depts_cache_v1';
const cacheSuffix = () => (localStorage.getItem('token') || '').split('.')[2] || 'anonymous';

const writeJsonCache = (key, value) => {
  try {
    sessionStorage.setItem(`${key}:${cacheSuffix()}`, JSON.stringify(value));
  } catch (e) { // eslint-disable-line no-unused-vars
    // 缓存不可用不影响业务
  }
};

// 读取缓存
const readJsonCache = (key) => {
  try {
    const raw = sessionStorage.getItem(`${key}:${cacheSuffix()}`);
    if (!raw) return null;
    return JSON.parse(raw);
  } catch (e) { // eslint-disable-line no-unused-vars
    return null;
  }
};

// 处理用户名编码
const fixUserNameEncoding = (name) => {
  if (!name || typeof name !== 'string') return name;
  try {
    let fixed = name.replace(/^\uFEFF/, '').trim();
    if (!fixed) return name;
    if (fixed.includes('\uFFFD') || /[\uFFFD]/.test(fixed)) {
      try {
        fixed = decodeURIComponent(encodeURIComponent(fixed));
      } catch (e) { // eslint-disable-line no-unused-vars
        // 忽略编码错误
      }
    }
    return fixed;
  } catch (e) { // eslint-disable-line no-unused-vars
    return name;
  }
};

// 加载用户和部门数据
const loadUserAndDeptMaps = async (performances = []) => {
  try {
    // 优先读缓存，未命中再请求，减少进入页面的等待
    let users = readJsonCache(USERS_CACHE_KEY);
    if (!Array.isArray(users)) {
      try {
        users = await getAllUsers();
        if (Array.isArray(users)) writeJsonCache(USERS_CACHE_KEY, users);
      } catch (e) { // eslint-disable-line no-unused-vars
        users = [];
      }
    }
    allUsers.value = Array.isArray(users) ? users : [];

    const newUserMap = new Map();
    const newDeptMap = new Map();
    // 部门名缓存：命中时不再逐个请求 /system/dept/{id}
    const cachedDepts = readJsonCache(DEPTS_CACHE_KEY);
    if (cachedDepts && typeof cachedDepts === 'object') {
      Object.entries(cachedDepts).forEach(([id, name]) => {
        if (name) newDeptMap.set(String(id), String(name));
      });
    }

    allUsers.value.forEach(user => {
      const uid = user?.userId ?? user?.user_id;
      if (uid) {
        let name = user?.nickName ?? user?.nick_name ?? user?.userName ?? user?.user_name;
        if (name) {
          name = fixUserNameEncoding(name);
          if (name && name.trim() !== '') {
            newUserMap.set(String(uid), name);
          }
        }
      }
    });

    const uniqueDeptIds = new Set();
    const uniqueAuditorIds = new Set();
    performances.forEach(p => {
      if (p?.deptId) uniqueDeptIds.add(String(p.deptId));
      if (p?.auditorId) uniqueAuditorIds.add(String(p.auditorId));
    });

    uniqueAuditorIds.forEach(auditorId => {
      if (!newUserMap.has(String(auditorId))) {
        const user = allUsers.value.find(u => {
          const uid = u?.userId ?? u?.user_id;
          return uid !== undefined && uid !== null && String(uid) === String(auditorId);
        });
        if (user) {
          let name = user?.nickName ?? user?.nick_name ?? user?.userName ?? user?.user_name;
          if (name) {
            name = fixUserNameEncoding(name);
            if (name && name.trim() !== '') {
              newUserMap.set(String(auditorId), name);
            } else {
              newUserMap.set(String(auditorId), `用户${auditorId}`);
            }
          } else {
            newUserMap.set(String(auditorId), `用户${auditorId}`);
          }
        } else {
          newUserMap.set(String(auditorId), `用户${auditorId}`);
        }
      }
    });

    const missingDeptIds = Array.from(uniqueDeptIds).filter(id => !newDeptMap.has(id));
    // 先用缓存映射出表，缺失的部门名在后台补齐
    userMap.value = newUserMap;
    deptMap.value = newDeptMap;
    if (missingDeptIds.length > 0) {
      await Promise.all(missingDeptIds.map(deptId =>
          getDeptById(Number(deptId))
              .then(dept => {
                if (dept?.deptName) {
                  newDeptMap.set(String(deptId), String(dept.deptName));
                } else {
                  newDeptMap.set(String(deptId), `部门${deptId}`);
                }
              })
              .catch(() => {
                newDeptMap.set(String(deptId), `部门${deptId}`);
              })
      ));
    }

    userMap.value = newUserMap;
    deptMap.value = newDeptMap;
    writeJsonCache(DEPTS_CACHE_KEY, Object.fromEntries(newDeptMap));
  } catch (e) { // eslint-disable-line no-unused-vars
    userMap.value = new Map();
    deptMap.value = new Map();
  }
};

// 获取部门名称
const getDeptName = (deptId) => {
  if (!deptId) return '';
  return deptMap.value.get(String(deptId)) || `部门${deptId}`;
};

// 获取用户名称
const getUserName = (userId) => {
  if (!userId) return '';
  const id = String(userId);
  if (userMap.value.has(id)) {
    return userMap.value.get(id);
  }
  const user = allUsers.value.find(u => {
    const uid = u?.userId ?? u?.user_id;
    return uid !== undefined && uid !== null && String(uid) === id;
  });
  if (user) {
    let name = user?.nickName ?? user?.nick_name ?? user?.userName ?? user?.user_name;
    if (name) {
      name = fixUserNameEncoding(name);
      if (name && name.trim() !== '') {
        userMap.value.set(id, name);
        return name;
      }
    }
  }
  return `用户${userId}`;
};

// 固定的绩效指标一级/二级分类
const performanceCategories = [
  {
    id: 'cat-1',
    code: '1',
    name: '产出指标',
    level: 1,
    children: [
      { id: 'cat-1-1', code: '1.1', name: '数量指标', level: 2, children: [] },
      { id: 'cat-1-2', code: '1.2', name: '质量指标', level: 2, children: [] },
      { id: 'cat-1-3', code: '1.3', name: '时效指标', level: 2, children: [] }
    ]
  },
  {
    id: 'cat-2',
    code: '2',
    name: '效益指标',
    level: 1,
    children: [
      { id: 'cat-2-1', code: '2.1', name: '社会效益指标', level: 2, children: [] },
      { id: 'cat-2-2', code: '2.2', name: '可持续影响指标', level: 2, children: [] }
    ]
  },
  {
    id: 'cat-3',
    code: '3',
    name: '满意度指标',
    level: 1,
    children: [
      { id: 'cat-3-1', code: '3.1', name: '服务对象满意度指标', level: 2, children: [] }
    ]
  }
];

// 原始绩效数据
const allPerformances = ref([]);
const performanceTreeData = ref([]);

// 当前筛选结果下的条目数量
const visibleNodeCount = computed(() => {
  const countNodes = (nodes) => {
    if (!Array.isArray(nodes) || nodes.length === 0) return 0;
    let n = 0;
    nodes.forEach(it => {
      if (it.level === 3) {
        n += 1;
      }
      if (Array.isArray(it.children) && it.children.length > 0) {
        n += countNodes(it.children);
      }
    });
    return n;
  };
  return countNodes(performanceTreeData.value);
});

// 年度选项


// 数据类型文本和标签
const getDataTypeText = (type) => {
  const map = { '1': '数值', '2': '百分比' };
  return map[type] || '未知';
};
const getDataTypeTag = (type) => {
  const map = { '1': 'primary', '2': 'warning' };
  return map[type] || 'info';
};

// 格式化数字
const formatNumber = (val) => {
  if (val === undefined || val === null) return '0.00';
  const num = Number(val);
  if (isNaN(num)) return '0.00';
  return num.toFixed(2);
};

// 任务状态映射
const mapTaskStatus = (status) => {
  const map = { '0': '未开始', '1': '进行中', '2': '审核中', '3': '已完成' };
  return map[status] || '未知';
};
const getTaskStatusTag = (status) => {
  const map = { '0': 'info', '1': 'primary', '2': 'warning', '3': 'success' };
  return map[status] || 'info';
};

// 构建树形数据结构
const buildPerformanceTree = (performances) => {
  const tree = JSON.parse(JSON.stringify(performanceCategories));

  const level3Items = performances.filter(p => {
    const ancestors = p.ancestors || '';
    const parts = ancestors.split(',').filter(Boolean);
    return parts.length >= 3;
  });

  level3Items.forEach(item => {
    const ancestors = item.ancestors || '';
    const parts = ancestors.split(',').filter(Boolean);
    const parentCode = parts.length >= 3 ? parts[2] : null;

    if (!parentCode) return;

    let found = false;
    for (const l1 of tree) {
      for (const l2 of l1.children) {
        if (l2.code === parentCode) {
          const node = {
            id: `perf-${item.perfId}`,
            perfId: item.perfId,
            code: item.perfCode || '',
            name: item.perfName || '',
            level: 3,
            targetValue: item.targetValue || 0,
            currentValue: item.currentValue || 0,
            dataType: item.dataType || '1',
            deptId: item.deptId,
            dept: getDeptName(item.deptId),
            principalId: item.principalId,
            principal: getUserName(item.principalId),
            auditorId: item.auditorId,
            auditorName: getUserName(item.auditorId),
            leaderId: item.leaderId,
            leaderName: getUserName(item.leaderId),
            progress: (() => {
              const target = Number(item.targetValue || 0);
              const current = Number(item.currentValue || 0);
              if (target > 0) {
                return Math.min(100, Math.round((current / target) * 100));
              }
              return 0;
            })(),
            _raw: item,
            children: []
          };
          l2.children.push(node);
          found = true;
          break;
        }
      }
      if (found) break;
    }
  });

  // 按数字序号排序
  tree.forEach(l1 => {
    l1.children.forEach(l2 => {
      if (l2.children && l2.children.length > 0) {
        l2.children.sort((a, b) => {
          const codeA = a.code || '';
          const codeB = b.code || '';
          const partsA = codeA.split('.').map(part => {
            const num = parseInt(part, 10);
            return isNaN(num) ? 0 : num;
          });
          const partsB = codeB.split('.').map(part => {
            const num = parseInt(part, 10);
            return isNaN(num) ? 0 : num;
          });
          for (let i = 0; i < Math.min(partsA.length, partsB.length); i++) {
            if (partsA[i] !== partsB[i]) {
              return partsA[i] - partsB[i];
            }
          }
          return partsA.length - partsB.length;
        });
      }
    });
  });

  const filteredTree = tree.map(l1 => {
    l1.children = l1.children.filter(l2 => l2.children.length > 0);
    return l1;
  }).filter(l1 => l1.children.length > 0);

  return filteredTree;
};

// 应用筛选条件
const applyFilters = () => {
  if (!allPerformances.value || allPerformances.value.length === 0) {
    performanceTreeData.value = buildPerformanceTree([]);
    return;
  }

  let filtered = allPerformances.value.filter(item => queryIds.value.has(String(item.perfId)));
  if (performanceMode.value) filtered = filtered.filter(item => isAutoCode(item.perfCode) === (performanceMode.value === 'auto'));
  if (performanceAuditor.value) filtered = filtered.filter(item => String(item.auditorId) === performanceAuditor.value);
  if (ownerFilter.value) filtered = filtered.filter(item => String(item.deptId) === ownerFilter.value);
  if (leaderFilter.value) filtered = filtered.filter(item => String(item.leaderId) === leaderFilter.value);
  if (quickFilter.value !== 'all') {
    const userId = getCurrentAuthInfo()?.userId;
    filtered = filtered.filter(item => quickFilter.value === 'mine' ? String(item.leaderId) === String(userId)
      : (quickFilter.value === 'todo' ? performanceShortcuts.value.todo : performanceShortcuts.value.returned)
        .some(key => String(key.perfId) === String(item.perfId) && (!filterYear.value || String(key.year) === String(filterYear.value))));
  }

  if (filterDataType.value) {
    filtered = filtered.filter(item => String(item.dataType) === String(filterDataType.value));
  }

  if (actualSearchText.value) {
    const keyword = String(actualSearchText.value).toLowerCase().trim();
    filtered = filtered.filter(item => {
      const code = (item.perfCode || '').toLowerCase();
      const name = (item.perfName || '').toLowerCase();
      return code.includes(keyword) || name.includes(keyword);
    });
  }

  const visible = filtered;
  const tree = buildPerformanceTree(visible);
  if (visible.length !== filtered.length) {
    // 关联任务被全部隐藏的分类节点不再展示
    const prune = nodes => (Array.isArray(nodes) ? nodes
      .map(node => {
        if (Array.isArray(node.children) && node.children.length > 0) node.children = prune(node.children);
        return node;
      })
      .filter(node => node.level === 3 || (Array.isArray(node.children) && node.children.length > 0)) : []);
    performanceTreeData.value = prune(tree);
    return;
  }
  performanceTreeData.value = tree;
};

watch(quickFilter, () => { applyFilters(); performanceMemory.resetPosition(); });
watch([ownerFilter, leaderFilter, performanceMode, performanceAuditor], () => { applyFilters(); performanceMemory.resetPosition(); });

// 搜索处理




// 年份切换处理


// 加载绩效数据
let openedPerformanceLink = '';
let performanceSavedDraft = '';
const performanceDetailItems = computed(() => detailLeaves(performanceTreeData.value, row => row.level === 3));
const openAdjacentPerformance = async row => {
  await handleViewDetail(row);
  await nextTick();
  performanceContentRef.value?.closest('.el-drawer__body')?.scrollTo({ top: 0 });
};
const performanceDraft = () => JSON.stringify([manualSubmitForm, performanceAuditForm, manualAuditForm]);
const performanceToolsBusy = computed(() => loading.value || refreshing.value || detailLoading.value || tasksLoading.value || performanceAuditLoading.value ||
  manualSubmitting.value || performanceAuditSubmitting.value || manualAuditSubmitting.value || taskDrawerVisible.value);
const performanceDetailLink = computed(() => `${window.location.origin}${router.resolve({ path: '/home/works/performance', query: {
  perfId: String(currentPerformance.value?.perfId || ''), year: String(currentYear.value || '')
} }).href}`);
const locateCurrentPerformance = async () => {
  const context = getPerformanceAuditContext();
  if (!await confirmDetailLeave(performanceDraft() !== performanceSavedDraft)) return;
  if (!isCurrentPerformanceAuditContext(context) || performanceToolsBusy.value) return;
  await loadPerformances();
  if (!allPerformances.value.some(row => String(row.perfId) === String(context.perfId))) {
    await queryBar.value?.replace({year:String(filterYear.value||'2026')});
    if(!allPerformances.value.some(row=>String(row.perfId)===String(context.perfId))){ElMessage.info('记录已删除或无权访问，无法定位');return;}
  }
  await locateDetailRow({ tree: () => performanceTreeData.value, matches: row => String(row.perfId) === String(context.perfId),
    reset: async () => { listFilters.value = { dataType: '', owner: '', leader: '', mode: '', auditor: '' }; filterDataType.value = ''; quickFilter.value = 'all'; ownerFilter.value = ''; leaderFilter.value = ''; searchText.value = ''; actualSearchText.value = ''; await queryBar.value?.replace({year:String(filterYear.value||'2026')}); applyFilters(); },
    expanded: expandedRowKeys, table: performanceTableRef, close: () => { detailDrawerVisible.value = false; },
    valid: () => isCurrentPerformanceAuditContext(context) });
};
const openPerformanceLink = async () => {
  const id = String(route.query.perfId || '');
  const key = `${id}:${route.query.year || ''}`;
  if (!id || key === openedPerformanceLink || performanceDetailDisposed) return;
  const linkedYear=getDefaultPerformanceYear();
  if(String(filterYear.value)!==linkedYear){queryBar.value?.replace({year:linkedYear});return;}
  openedPerformanceLink = key;
  const linkCurrent=()=>!performanceDetailDisposed&&openedPerformanceLink===key&&`${route.query.perfId||''}:${route.query.year||''}`===key;
  listFilters.value = { dataType: '', owner: '', leader: '', mode: '', auditor: '' };
  searchText.value = ''; actualSearchText.value = ''; filterDataType.value = ''; quickFilter.value = 'all'; ownerFilter.value = ''; leaderFilter.value = ''; applyFilters();
  const path = findDetailPath(performanceTreeData.value, row => String(row.perfId) === id);
  if(path)await handleViewDetail(path.at(-1));
  else{
    try{const result=await get('/list-filters/performance',{responseMode:'tree',focusId:id,'f.year':linkedYear});
      if(!linkCurrent())return;
      const focused=findDetailPath(buildPerformanceTree(result.items||[]),row=>String(row.perfId)===id);
      if(focused)await handleViewDetail(focused.at(-1),linkedYear);else ElMessage.info('绩效不存在、已删除或无权访问');
    }catch(error){if(linkCurrent())ElMessage.info(error.message||'绩效不存在、已删除或无权访问');}
  }
};
const loadPerformances = async (options={}) => {
  if(performanceDetailDisposed)return;
  return queryBar.value?.refresh(options);
};
// 展开/折叠处理
const handleExpandChange = (row, expanded) => {
  const id = row?.id !== undefined && row?.id !== null ? String(row.id) : '';
  if (!id) return;
  const cur = new Set(Array.isArray(expandedRowKeys.value) ? expandedRowKeys.value.map(String) : []);
  if (expanded) cur.add(id);
  else cur.delete(id);
  expandedRowKeys.value = Array.from(cur);

  if (expanded) markChildrenAnimating(row);
};

// 标记子节点动画
const markChildrenAnimating = (row) => {
  try {
    const children = Array.isArray(row?.children) ? row.children : [];
    if (!children.length) return;
    const ids = children
        .map(c => (c?.id ? String(c.id) : ''))
        .filter(Boolean)
        .slice(0, 60);
    ids.forEach(id => {
      animRowKeyMap[id] = true;
      setTimeout(() => {
        try { delete animRowKeyMap[id]; } catch (e) { // eslint-disable-line no-unused-vars
          // 忽略删除错误
        }
      }, 220);
    });
  } catch (e) { // eslint-disable-line no-unused-vars
    // 忽略错误
  }
};

// 行类名
const rowClassName = ({ row }) => {
  const id = row?.id ? String(row.id) : '';
  return id && animRowKeyMap[id] ? 'row-expand-anim' : '';
};

// 判断点击是否被阻止
const isRowToggleBlocked = (evt) => {
  const el = evt?.target;
  if (!el || typeof el.closest !== 'function') return false;
  return !!el.closest('button, a, .el-button, .el-dropdown, .el-input, .el-select');
};

// 切换行展开状态
const toggleRowExpand = (row) => {
  if (!row || !Array.isArray(row.children) || row.children.length === 0) return;
  const id = row?.id ? String(row.id) : '';
  if (!id) return;
  const cur = new Set(Array.isArray(expandedRowKeys.value) ? expandedRowKeys.value.map(String) : []);
  const expanded = cur.has(id);
  handleExpandChange(row, !expanded);
};

// 处理行点击
const handleRowClick = (row, _column, event) => {
  if (isRowToggleBlocked(event)) return;
  if (row.level === 1 || row.level === 2) {
    toggleRowExpand(row);
  } else if (row.level === 3) {
    handleViewDetail(row);
  }
};

// 获取层级样式类
const getLevelClass = (level) => {
  switch (level) {
    case 1: return 'font-bold text-gray-900 text-base';
    case 2: return 'font-medium text-gray-700';
    case 3: return 'text-gray-600 pl-2';
    default: return '';
  }
};

// 获取进度条状态
const getProgressStatus = (row) => {
  if (row.progress === 100) return 'success';
  return '';
};

// 查看绩效详情
const handleViewDetail = async (row, linkedYear) => {
  if (row.level !== 3 || performanceQueryState.value.loading && !performanceQueryState.value.background || loading.value) return;
  // A queued click may still contain a row from the previous DOM render.
  // Resolve its ID against the current result instead of copying stale annual values.
  if(!linkedYear){
    row=performanceDetailItems.value.find(item=>String(item.perfId)===String(row.perfId));
    if(!row)return;
  }

  invalidatePerformanceAudit();
  Object.assign(performanceAuditForm, { result: 1, comment: '同意' });
  Object.assign(manualAuditForm, { result: 1, comment: '同意' });
  currentPerformance.value = row;
  performanceDetailYear.value=String(linkedYear||filterYear.value||new Date().getFullYear());
  detailDrawerVisible.value = true;
  loadTransferLogs(row.perfId ?? row._raw?.perfId);
  const detailContext = getPerformanceAuditContext();

  // 获取实际编码
  const actualCode = row.code || row._raw?.perfCode || '';
  // 判断是否为自动任务指标（1.1.x 和 1.2.x）
  const isAuto = actualCode.startsWith('1.1.') || actualCode.startsWith('1.2.');

  // 重置手动提交表单
  resetManualForm();

  if (isAuto) {
    performanceAuditList.value = [];
    await loadRelatedTasks(row.perfId, performanceDetailYear.value || null);
    await loadRelationLogs(row.perfId);
  } else {
    relatedTasks.value = [];
    relationLogs.value = [];
    tasksLoading.value = false;
    await loadPerformanceAuditFlow(row.perfId);
  }
  if (isCurrentPerformanceAuditContext(detailContext)) performanceSavedDraft = performanceDraft();
};

// 加载当前绩效的审核流转，旧请求不再回写新详情。
const loadPerformanceAuditFlow = async (perfId, context = getPerformanceAuditContext()) => {
  if (!perfId || Number(perfId) !== context.perfId || !isCurrentPerformanceAuditContext(context)) return;
  const requestId = ++performanceAuditRequestId;
  const isCurrentRequest = () => requestId === performanceAuditRequestId && isCurrentPerformanceAuditContext(context);
  performanceAuditList.value = [];
  clearPerformanceAuditLogs();
  performanceAuditLoading.value = true;
  try {
    const data = await getPerformanceAuditsByPerfAndYear(context.perfId, context.year);
    if (!isCurrentRequest()) return;
    performanceAuditList.value = Array.isArray(data)
        ? data.filter(audit => matchesPerformanceAuditContext(audit, context)) : [];
  } catch (e) {
    if (!isCurrentRequest()) return;
    console.warn('加载绩效审核流转失败:', e);
    performanceAuditList.value = [];
  } finally {
    if (isCurrentRequest()) {
      performanceAuditLoading.value = false;
      performanceSavedDraft = performanceDraft();
    }
  }
};

const getPerformanceAuditSubKey = (it) => {
  const id = it?.subId ?? it?.sub_id ?? null;
  return id === undefined || id === null ? '' : String(id);
};

const togglePerformanceAuditItemLogs = async (item) => {
  const subId = item?.subId ?? item?.sub_id;
  if (!subId) return;
  const context = getPerformanceAuditContext();
  const requestId = performanceAuditRequestId;
  const isCurrentRequest = () => requestId === performanceAuditRequestId && isCurrentPerformanceAuditContext(context);
  if (!isCurrentRequest() || !matchesPerformanceAuditContext(item, context)) return;
  const key = String(subId);
  performanceAuditItemLogVisible[key] = !performanceAuditItemLogVisible[key];
  if (!performanceAuditItemLogVisible[key]) return;

  if (Array.isArray(performanceAuditItemLogList[key]) && performanceAuditItemLogList[key].length > 0) return;

  try {
    performanceAuditItemLogLoading[key] = true;
    const logs = await getPerformanceAuditLogs(Number(subId));
    if (!isCurrentRequest()) return;
    const arr = Array.isArray(logs) ? logs : [];
    arr.sort((a, b) => {
      const ta = a?.createTime ? new Date(a.createTime).getTime() : 0;
      const tb = b?.createTime ? new Date(b.createTime).getTime() : 0;
      return tb - ta;
    });
    performanceAuditItemLogList[key] = arr;
  } catch (e) { // eslint-disable-line no-unused-vars
    if (isCurrentRequest()) performanceAuditItemLogList[key] = [];
  } finally {
    if (isCurrentRequest()) performanceAuditItemLogLoading[key] = false;
  }
};

const loadRelatedTasks = async (perfId, year = null) => {
  const context = getPerformanceAuditContext();
  tasksLoading.value = true;
  relatedTasks.value = [];

  try {
    const tasks = await getTasksByPerfId(perfId, year);
    if (!isCurrentPerformanceAuditContext(context)) return;

    if (Array.isArray(tasks) && tasks.length > 0) {
      const formattedTasks = tasks.map(task => {
        const contributionValue = task.currentValue || task.current_value || 0;

        return {
          taskId: task.taskId || task.task_id,
          taskCode: task.taskCode || task.task_code || '',
          taskName: task.taskName || task.task_name || '',
          phase: task.phase || '',
          contributionValue: contributionValue,
          weight: task.weight || 1.00,
          status: task.status || '0',
          _raw: task
        };
      });

      relatedTasks.value = formattedTasks;

      if (formattedTasks.length === 0) {
        ElMessage.info('该绩效指标暂无关联任务');
      }
    } else {
      relatedTasks.value = [];
      ElMessage.info('该绩效指标暂无关联任务');
    }
  } catch (e) {
    if (!isCurrentPerformanceAuditContext(context)) return;
    console.error('加载关联任务失败:', e);
    const errorMsg = e?.message || '';
    if (errorMsg.includes('404')) {
      ElMessage.warning('获取关联任务的接口未找到，请检查后端接口是否正确');
    } else if (errorMsg.includes('500')) {
      ElMessage.error('服务器内部错误，请稍后重试');
    } else {
      ElMessage.error(errorMsg || '加载关联任务失败');
    }
    relatedTasks.value = [];
  } finally {
    if (isCurrentPerformanceAuditContext(context)) tasksLoading.value = false;
  }
};

// 刷新绩效指标
const reloadPerformanceData = async () => {
  await loadPerformances();
  if (detailDrawerVisible.value && currentPerformance.value) {
    const updated = allPerformances.value.find(p => p.perfId === currentPerformance.value.perfId);
    if (updated) currentPerformance.value = { ...currentPerformance.value, ...updated };
  }
};

const handleRefreshPerformance = async (row) => {
  if (!row || refreshing.value) return;
  refreshing.value = true;
  refreshingPerfId.value = row.perfId;
  try {
    const result = await refreshPerformance(row.perfId);
    if (result && typeof result === 'object') {
      ElMessage.info(`有效关联 ${Number(result.validRelationCount || 0)} 条，年度错配跳过 ${Number(result.skippedYearMismatchCount || 0)} 条，无绩效影响跳过 ${Number(result.skippedTaskDataType0Count || 0)} 条`);
    }
    ElMessage.success('指标刷新成功');
    await loadPerformances();

    if (detailDrawerVisible.value && currentPerformance.value?.perfId === row.perfId) {
      const updated = allPerformances.value.find(p => p.perfId === row.perfId);
      if (updated) {
        currentPerformance.value = {
          ...currentPerformance.value,
          targetValue: updated.targetValue,
          currentValue: updated.currentValue,
          progress: (() => {
            const target = Number(updated.targetValue || 0);
            const current = Number(updated.currentValue || 0);
            return target > 0 ? Math.min(100, Math.round((current / target) * 100)) : 0;
          })(),
          submitHistory: updated.submitHistory
        };
      }
    }
  } catch (e) {
    console.error('刷新指标失败:', e);
    ElMessage.error(e?.message || '刷新失败');
  } finally {
    refreshing.value = false;
    refreshingPerfId.value = null;
  }
};

// --- 任务详情抽屉相关状态 ---
const taskDrawerVisible = ref(false);
const currentTask = ref(null);
const taskDrawerLoading = ref(false);
const taskReturnToPerformance = ref(false);
const taskAuditSubmitting = ref(false);
const taskFeedbackSubmitting = ref(false);
let taskDetailVersion = 0;
let taskDetailId = null;
const taskDetailContext = () => ({ version: taskDetailVersion, taskId: taskDetailId });
const isCurrentTaskDetail = (context) => !performanceDetailDisposed && taskDrawerVisible.value &&
  context.version === taskDetailVersion && context.taskId === taskDetailId;
const clearTaskDetail = () => {
  currentTask.value = null;
  taskAuditList.value = [];
  taskAuditLogs.value = [];
  taskForthTasks.value = [];
  Object.keys(taskForthFeedbackData).forEach(key => delete taskForthFeedbackData[key]);
};
watch(taskDrawerVisible, visible => {
  if (!visible) {
    taskDetailVersion++;
    taskDetailId = null;
    clearTaskDetail();
  }
}, { flush: 'sync' });
const taskAuditSectionRef = ref(null);

const taskFeedbackForm = reactive({
  progress: 0,
  comment: ''
});
const taskFileList = ref([]);

const taskAuditForm = reactive({
  result: 1,
  comment: ''
});

const taskAuditList = ref([]);
const taskAuditLogs = ref([]);
const taskAuditLogsApiEnabled = ref(true);

const taskAuditItemLogVisible = reactive({});
const taskAuditItemLogLoading = reactive({});
const taskAuditItemLogList = reactive({});

const taskFileMap = ref(new Map());
const taskForthTasks = ref([]);
const taskForthFeedbackData = reactive({});

// 任务预览相关
const taskPreviewVisible = ref(false);
const taskPreviewType = ref('');
const taskPreviewUrl = ref('');
const taskPreviewFileName = ref('');
const taskPreviewFileBlob = ref(null);

// 计算属性
const taskLatestAudit = computed(() => currentTaskSubmission(taskAuditList.value));
const taskIsPercent = computed(() => {
  const dt = currentTask.value?._raw?.dataType;
  return String(dt) === '2';
});
const taskCanSubmitMaterial = computed(() => {
  if (!canUsePermission('tasks.fill')) return false;
  const auth = getCurrentAuthInfo();
  if (!auth) return false;
  const leaderId = currentTask.value?._raw?.leaderId;
  if (isAdminTestEnabled()) return true;
  if (leaderId === undefined || leaderId === null) return false;
  return Number(leaderId) === Number(auth.userId);
});
const taskIsAnyAuditor = computed(() => {
  const auth = getCurrentAuthInfo();
  if (!auth) return false;
  const userId = Number(auth.userId);
  const userRole = auth.role;
  if (userRole === '0') return true;

  const raw = currentTask.value?._raw;
  const auditorId = raw?.auditorId ?? raw?.auditor_id ?? raw?.auditId ?? raw?.audit_id ?? null;
  if (auditorId && Number(auditorId) === userId) return true;

  const principalId = raw?.principalId ?? raw?.principal_id ?? null;
  if (principalId && Number(principalId) === userId) return true;

  if (taskLatestAudit.value) {
    const currentHandlerId = taskLatestAudit.value.currentHandlerId ?? taskLatestAudit.value.current_handler_id;
    if (currentHandlerId && Number(currentHandlerId) === userId) return true;
  }

  if (taskAuditList.value && taskAuditList.value.length > 0) {
    for (const audit of taskAuditList.value) {
      const handlerId = audit.currentHandlerId ?? audit.current_handler_id;
      if (handlerId && Number(handlerId) === userId) return true;
    }
  }

  return false;
});
const taskCanAudit = computed(() => {
  if (!canProcessTaskSubmission(taskLatestAudit.value)) return false;
  const stage = Number(taskLatestAudit.value?.flowStatus ?? taskLatestAudit.value?.flow_status);
  if (!canUsePermission(stage === 30 ? 'tasks.archive' : 'tasks.audit')) return false;
  const auth = getCurrentAuthInfo();
  if (!auth || !taskLatestAudit.value) return false;
  const currentHandlerId = taskLatestAudit.value.currentHandlerId ?? taskLatestAudit.value.current_handler_id;
  if (!currentHandlerId) return false;
  return Number(currentHandlerId) === Number(auth.userId);
});

// 解析JWT
const parseJwtPayload = (token) => {
  try {
    if (!token || typeof token !== 'string') return null;
    const parts = token.split('.');
    if (parts.length < 2) return null;
    let payload = parts[1];
    payload = payload.replace(/-/g, '+').replace(/_/g, '/');
    const pad = payload.length % 4;
    if (pad) payload += '='.repeat(4 - pad);
    const json = atob(payload);
    return JSON.parse(json);
  } catch (e) { // eslint-disable-line no-unused-vars
    return null;
  }
};

const getCurrentAuthInfo = () => {
  const token = localStorage.getItem('token') || '';
  const payload = parseJwtPayload(token);
  if (!payload) return null;
  const userId = payload.id !== undefined ? Number(payload.id) : null;
  const role = accessState.token === token ? accessState.role : payload.role !== undefined ? String(payload.role) : '';
  if (!userId || Number.isNaN(userId)) return null;
  return { userId, role };
};

const findAdminName = () => {
  const users = Array.isArray(allUsers.value) ? allUsers.value : [];
  const hardcoded = users.find(u => Number(u?.userId) === 110228);
  if (hardcoded) return hardcoded.nickName || hardcoded.userName || getUserName(hardcoded.userId);
  const admin = users.find(u => String(u?.role) === '0');
  return admin ? (admin.nickName || admin.userName || getUserName(admin.userId)) : '';
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
  if (s === 20) return '待归口审批';
  if (s === 10) return '待专业群审核';
  if (s < 0) return '已退回';
  return '历史状态未记录';
};

// 步骤条相关
const taskStepApprover = computed(() => {
  const raw = currentTask.value?._raw;
  const leaderId = raw?.leaderId ?? raw?.leader_id ?? null;
  const auditorName = raw?.auditorName ?? raw?.auditName ?? null;
  const auditorId = raw?.auditorId ?? raw?.auditor_id ?? raw?.auditId ?? raw?.audit_id ?? null;
  const principalId = raw?.principalId ?? raw?.principal_id ?? null;

  let deptLeaderName = '';
  if (auditorName && auditorName !== '无') {
    deptLeaderName = auditorName;
  } else if (!auditorName && auditorId) {
    deptLeaderName = getUserName(auditorId);
  }

  return {
    submitterName: (raw?.leaderName && raw.leaderName.trim() !== '') ? raw.leaderName : (leaderId ? getUserName(leaderId) : ''),
    deptLeaderName: deptLeaderName,
    principalName: (raw?.principalName && raw.principalName.trim() !== '') ? raw.principalName : (principalId ? getUserName(principalId) : ''),
    adminName: findAdminName(),
  };
});

const taskStepActive = computed(() => {
  if (taskLatestAudit.value) {
    const s = Number(taskLatestAudit.value.flowStatus ?? taskLatestAudit.value.flow_status);
    if (s < 0) return taskWorkflowStep(taskLatestAudit.value, taskAuditLogs.value);
    if (!Number.isNaN(s)) {
      if (s >= 40) return 4;
      if (s >= 30) return 3;
      if (s >= 20) return 2;
      if (s >= 10) return 1;
      if (s <= -30) return 3;
      if (s <= -20) return 2;
      if (s <= -10) return 1;
      return 1;
    }
  }
  const rawStatus = currentTask.value?._raw?.status !== undefined && currentTask.value?._raw?.status !== null
      ? String(currentTask.value._raw.status)
      : '';
  if (rawStatus === '2') return 2;
  if (rawStatus === '3') return 4;
  return 0;
});

const taskStepDesc = computed(() => {
  const submission = taskLatestAudit.value;
  const configured = taskStepApprover.value;
  if (!submission) return {
    submit: `提交人：${configured.submitterName || '—'}`,
    dept: `审核人：${configured.deptLeaderName || '—'}`,
    principal: `审核人：${configured.principalName || '—'}（待流转）`,
    admin: `审核人：${configured.adminName || '—'}（待流转）`,
  };
  const describe = (stage, name) => taskStageDescription(submission, taskAuditLogs.value, stage, name, getUserName, formatDateTime);
  const submitter = submission.submitBy ?? submission.submit_by;
  return {
    submit: `提交人：${submitter ? getUserName(submitter) : configured.submitterName || '—'}（${formatDateTime(submission.submitTime ?? submission.submit_time) || '—'}）`,
    dept: describe('dept', configured.deptLeaderName),
    principal: describe('principal', configured.principalName),
    admin: describe('admin', configured.adminName),
  };
});

// 文件相关函数
const getTaskFileName = (fileId) => {
  if (fileId === undefined || fileId === null) return '';
  const id = String(fileId);
  let fileName = taskFileMap.value.get(id);
  if (!fileName) return `文件ID: ${fileId}`;
  if (fileName && fileName.includes('%') && !fileName.startsWith('文件ID:')) {
    try {
      fileName = decodeURIComponent(fileName);
    } catch (e) { // eslint-disable-line no-unused-vars
      // 忽略解码错误
    }
  }
  return fileName;
};

const loadTaskForthTasks = async (parentId, context) => {
  taskForthTasks.value = [];
  Object.keys(taskForthFeedbackData).forEach(key => {
    delete taskForthFeedbackData[key];
  });
  if (!parentId) return;
  try {
    const data = await getForthTasks(parentId);
    if (!isCurrentTaskDetail(context)) return;
    taskForthTasks.value = Array.isArray(data) ? data : (data?.data || []);
    taskForthTasks.value.forEach(task => {
      const taskId = task.taskId ?? task.task_id;
      if (!taskId) return;
      const currentValue = task.currentValue ?? task.current_value;
      const n = Number(currentValue);
      taskForthFeedbackData[taskId] = {
        progress: Number.isFinite(n) ? Math.round(n) : 0,
      };
    });
  } catch (e) { // eslint-disable-line no-unused-vars
    if (isCurrentTaskDetail(context)) taskForthTasks.value = [];
  }
};

const fetchTaskFileNameFromDownload = async (fileId) => {
  if (!fileId) return null;
  const id = String(fileId);
  if (taskFileMap.value.has(id)) {
    return taskFileMap.value.get(id);
  }

  try {
    const token = localStorage.getItem('token') || '';
    if (!token) return null;

    const resp = await adminTestFetch(`/system/download/${fileId}`, {
      method: 'HEAD',
      headers: { Authorization: token }
    });

    if (resp.ok) {
      const cd = resp.headers.get('content-disposition') || '';
      const filename = parseFilenameFromContentDisposition(cd);
      if (filename) {
        taskFileMap.value.set(id, filename);
        return filename;
      }
    }
  } catch (e) { // eslint-disable-line no-unused-vars
    // 忽略下载错误
  }
  return null;
};

const parseFilenameFromContentDisposition = (cd) => {
  if (!cd) return '';

  const mStar = cd.match(/filename\*\s*=\s*([^']*)''([^;]+)/i);
  if (mStar && mStar[2]) {
    try {
      return decodeURIComponent(mStar[2]);
    } catch (e) { // eslint-disable-line no-unused-vars
      return mStar[2];
    }
  }

  const m = cd.match(/filename\s*=\s*"([^";]+)"/i);
  if (m && m[1]) {
    let filename = m[1];
    if (filename.includes('%')) {
      try {
        filename = decodeURIComponent(filename);
      } catch (e) { // eslint-disable-line no-unused-vars
        // 忽略解码错误
      }
    }
    return filename;
  }
  return '';
};

// 转换任务数据
const transformTaskToFormat = (task) => {
  const formatDate = (date) => {
    if (!date) return '';
    const d = new Date(date);
    if (isNaN(d.getTime())) return '';
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  };

  const calcProgress = () => {
    const cur = Number(task?.currentValue ?? task?.current_value ?? 0);
    const target = Number(task?.targetValue ?? task?.target_value ?? 0);
    if (target <= 0) return 0;
    return Math.min(100, Math.round((cur / target) * 100));
  };

  const mapStatus = (status) => {
    const statusMap = {
      '0': '未开始',
      '1': '进行中',
      '2': '审核中',
      '3': '已完成'
    };
    return statusMap[status] || '未开始';
  };

  const getPhaseDateRange = () => {
    const y = task?.phase !== undefined && task?.phase !== null ? Number(task.phase) : NaN;
    if (Number.isFinite(y) && y > 1900) {
      return {
        startDate: `${y}-01-01`,
        endDate: `${y}-12-31`,
      };
    }
    return null;
  };

  const phaseRange = getPhaseDateRange();

  return {
    id: String(task.taskId),
    code: task.taskCode || '',
    name: task.taskName || '',
    level: Number(task.level) || 1,
    progress: calcProgress(),
    status: mapStatus(task.status),
    dept: task.deptName || getDeptName(task.deptId),
    principal: (() => {
      const leaderId = task?.leaderId ?? task?.leader_id;
      if (task.leaderName && task.leaderName.trim() !== '') {
        return task.leaderName;
      }
      if (leaderId) {
        return getUserName(leaderId);
      }
      return '';
    })(),
    startDate: phaseRange?.startDate || formatDate(task.createTime),
    endDate: phaseRange?.endDate || formatDate(task.updateTime) || formatDate(task.createTime),
    _raw: task,
    children: []
  };
};

const normalizeTaskAuditItem = (it) => {
  if (!it || typeof it !== 'object') return null;
  return {
    subId: it.subId ?? it.sub_id ?? null,
    taskId: it.taskId ?? it.task_id ?? null,
    fileId: it.fileId ?? it.file_id ?? null,
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
    comment: it.comment ?? null,
    _raw: it,
  };
};

const getTaskAuditSubKey = (it) => {
  const id = it?.subId ?? it?.sub_id ?? null;
  return id === undefined || id === null ? '' : String(id);
};

// 加载任务详情
const loadTaskDetail = async (taskId) => {
  if (!taskDrawerVisible.value || performanceDetailDisposed) return false;
  taskDetailVersion++;
  taskDetailId = Number(taskId);
  const context = taskDetailContext();
  clearTaskDetail();
  taskDrawerLoading.value = true;
  try {
    const taskDetail = await getTaskById(taskId);
    if (!isCurrentTaskDetail(context)) return false;
    if (!taskDetail) {
      ElMessage.error('获取任务详情失败');
      return false;
    }

    const transformedTask = transformTaskToFormat(taskDetail);
    currentTask.value = transformedTask;

    const cv = transformedTask?._raw?.currentValue ?? transformedTask?._raw?.current_value;
    const n = Number(cv);
    taskFeedbackForm.progress = Number.isFinite(n) ? Math.round(n) : 0;
    taskFileList.value = [];
    taskFeedbackForm.comment = '';
    taskAuditForm.result = 1;
    taskAuditForm.comment = '同意';

    await loadTaskForthTasks(taskId, context);
    if (!isCurrentTaskDetail(context)) return false;
    await loadTaskAuditForCurrentTask(taskId, context);

    return true;
  } catch (e) {
    if (!isCurrentTaskDetail(context)) return false;
    console.error('加载任务详情失败:', e);
    ElMessage.error(e?.message || '加载任务详情失败');
    return false;
  } finally {
    if (isCurrentTaskDetail(context)) taskDrawerLoading.value = false;
  }
};

// 加载任务的审批单信息
const loadTaskAuditForCurrentTask = async (taskId, context) => {
  try {
    let res;
    try {
      res = await getAuditByTaskId(Number(taskId));
    } catch (e) { // eslint-disable-line no-unused-vars
      if (!isCurrentTaskDetail(context)) return;
      res = await getAudit(Number(taskId));
    }

    if (!isCurrentTaskDetail(context)) return;
    if (Array.isArray(res)) {
      const normalized = res.map(normalizeTaskAuditItem).filter(item => item && Number(item.taskId) === Number(taskId));
      normalized.sort((a, b) => {
        const ta = a.submitTime ? new Date(a.submitTime).getTime() : 0;
        const tb = b.submitTime ? new Date(b.submitTime).getTime() : 0;
        return tb - ta;
      });
      taskAuditList.value = normalized;

      const fileIds = normalized
          .map(item => item.fileId)
          .filter(id => id !== undefined && id !== null);
      if (fileIds.length > 0) {
        await Promise.all(fileIds.map(fileId => fetchTaskFileNameFromDownload(fileId).catch(() => { })));
        if (!isCurrentTaskDetail(context)) return;
      }
    } else {
      taskAuditList.value = [];
    }

    const allLogs = [];
    if (taskAuditLogsApiEnabled.value && taskAuditList.value.length > 0) {
      for (const auditItem of taskAuditList.value) {
        const subId = auditItem.subId ?? auditItem.sub_id;
        if (subId) {
          try {
            const logs = await getAuditLogs(Number(subId));
            if (!isCurrentTaskDetail(context)) return;
            if (Array.isArray(logs) && logs.length > 0) {
              allLogs.push(...logs);
            }
          } catch (e) {
            if (!isCurrentTaskDetail(context)) return;
            const msg = String(e?.message || '');
            if (msg.includes('No endpoint GET /biz/audit/logs') || msg.includes('/biz/audit/logs') || msg.includes('404')) {
              taskAuditLogsApiEnabled.value = false;
              break;
            }
          }
        }
      }
    }

    if (allLogs.length > 0) {
      taskAuditLogs.value = allLogs;
      taskAuditLogs.value.sort((a, b) => {
        const ta = a?.createTime ? new Date(a.createTime).getTime() : 0;
        const tb = b?.createTime ? new Date(b.createTime).getTime() : 0;
        return tb - ta;
      });
    } else {
      taskAuditLogs.value = [];
    }

    if (taskAuditList.value && taskAuditList.value.length > 0) {
      const latest = taskAuditList.value[0];
      const comment = latest.comment ?? latest._raw?.comment ?? null;
      if (comment) {
        taskFeedbackForm.comment = String(comment);
      } else {
        const taskComment = currentTask.value?._raw?.comment;
        taskFeedbackForm.comment = taskComment ? String(taskComment) : '';
      }
    } else {
      const taskComment = currentTask.value?._raw?.comment;
      taskFeedbackForm.comment = taskComment ? String(taskComment) : '';
    }
  } catch (e) { // eslint-disable-line no-unused-vars
    if (!isCurrentTaskDetail(context)) return;
    taskAuditList.value = [];
    taskAuditLogs.value = [];
    taskFeedbackForm.comment = '';
  }
};

// 切换审批动作日志
const toggleTaskAuditItemLogs = async (item) => {
  const subId = item?.subId ?? item?.sub_id;
  if (!subId) return;
  const key = String(subId);
  taskAuditItemLogVisible[key] = !taskAuditItemLogVisible[key];
  if (!taskAuditItemLogVisible[key]) return;

  if (Array.isArray(taskAuditItemLogList[key]) && taskAuditItemLogList[key].length > 0) return;
  if (!taskAuditLogsApiEnabled.value) return;

  try {
    taskAuditItemLogLoading[key] = true;
    const logs = await getAuditLogs(Number(subId));
    const arr = Array.isArray(logs) ? logs : [];
    arr.sort((a, b) => {
      const ta = a?.createTime ? new Date(a.createTime).getTime() : 0;
      const tb = b?.createTime ? new Date(b.createTime).getTime() : 0;
      return tb - ta;
    });
    taskAuditItemLogList[key] = arr;
  } catch (e) { // eslint-disable-line no-unused-vars
    taskAuditItemLogList[key] = [];
  } finally {
    taskAuditItemLogLoading[key] = false;
  }
};

// 任务反馈相关函数
const handleTaskProgressWheel = (e) => {
  if (!taskIsPercent.value) return;
  e.preventDefault();
  const cur = Number(taskFeedbackForm.progress);
  const base = Number.isFinite(cur) ? cur : 0;
  const next = base + (e.deltaY < 0 ? 1 : -1);
  taskFeedbackForm.progress = Math.max(0, Math.min(100, Math.round(next)));
};

const handleTaskExceed = () => {
  ElMessage.warning('只能上传 1 个文件，请先删除旧文件。');
};

const handleTaskRemove = () => {
  taskFileList.value = [];
};

const handleTaskFileChange = (file, list) => {
  const fileName = file.name.toLowerCase();
  const isExtAllowed = fileName.endsWith('.doc') || fileName.endsWith('.docx') || fileName.endsWith('.pdf');

  if (!isExtAllowed) {
    ElMessage.error('不支持的文件类型！仅支持 doc, docx, pdf');
    taskFileList.value = [];
    return;
  }

  taskFileList.value = list.slice(-1);
};

const handleTaskPreview = (file) => {
  const raw = file?.raw;
  if (raw && (raw.type === 'application/pdf' || String(file.name || '').toLowerCase().endsWith('.pdf'))) {
    if (taskPreviewUrl.value) {
      try { URL.revokeObjectURL(taskPreviewUrl.value); } catch (e) { // eslint-disable-line no-unused-vars
        // 忽略撤销错误
      }
    }
    taskPreviewUrl.value = URL.createObjectURL(raw);
    taskPreviewVisible.value = true;
    taskPreviewType.value = 'pdf';
    taskPreviewFileName.value = file.name;
    return;
  }
  taskPreviewVisible.value = true;
  taskPreviewType.value = 'doc';
  taskPreviewFileName.value = file.name;
};

// 提交任务反馈
const submitTaskFeedback = async () => {
  if (taskFeedbackSubmitting.value || taskDrawerLoading.value) return;
  const context = taskDetailContext();
  if (!isCurrentTaskDetail(context)) return;
  if (!taskCanSubmitMaterial.value) {
    ElMessage.warning('仅该任务负责人可提交材料');
    return;
  }
  const taskId = currentTask.value?._raw?.taskId;
  if (!taskId) {
    ElMessage.error('未获取到任务信息，无法提交');
    return;
  }
  if (!taskFileList.value || taskFileList.value.length === 0) {
    ElMessage.warning('请先选择佐证材料文件（doc/docx/pdf）');
    return;
  }
  const rawFile = taskFileList.value[0]?.raw;
  if (!rawFile) {
    ElMessage.error('未获取到上传文件，请重新选择');
    return;
  }

  const taskRaw = { ...currentTask.value._raw };
  const comment = String(taskFeedbackForm.comment || '').trim();
  const dataType = taskRaw?.dataType !== undefined && taskRaw?.dataType !== null ? String(taskRaw.dataType) : '2';
  const rv = Number.isFinite(Number(taskFeedbackForm.progress)) ? Math.round(Number(taskFeedbackForm.progress)) : 0;
  const subList = taskForthTasks.value.map(task => {
    const id = task.taskId ?? task.task_id;
    const value = Number(taskForthFeedbackData[id]?.progress);
    return {
      task_id: Number(id),
      reported_value: Number.isFinite(value) ? Math.round(value) : 0,
      data_type: Number(task.dataType ?? task.data_type ?? '1'),
    };
  });
  const audit = taskLatestAudit.value;
  const flow = audit ? Number(audit.flowStatus ?? audit.flow_status) : NaN;
  const subId = audit?.subId ?? audit?.sub_id ?? null;
  const submitBy = audit?.submitBy ?? audit?.submit_by ?? null;
  const myId = getCurrentAuthInfo()?.userId ?? null;
  if (Number(taskId) !== context.taskId || (audit && Number(audit.taskId) !== context.taskId)) return;
  taskFeedbackSubmitting.value = true;
  taskDrawerLoading.value = true;
  try {
    let hasAuditor = false;
    if (taskRaw?.auditorId || taskRaw?.auditor_id || taskRaw?.auditId || taskRaw?.audit_id) {
      hasAuditor = true;
    } else if (taskRaw?.deptId) {
      try {
        const dept = await getDeptById(Number(taskRaw.deptId));
        if (dept?.leaderId) {
          hasAuditor = true;
        }
      } catch (e) { // eslint-disable-line no-unused-vars
        // 忽略部门查询错误
      }
    }

    if (!hasAuditor) {
      ElMessage.error({
        message: '该任务未设置审核人，无法提交材料。请联系管理员为任务设置审核人后再试。',
        duration: 5000,
        showClose: true
      });
      return;
    }


    if (comment.length > 50) {
      ElMessage.warning('完成情况最多50字');
      return;
    }

    const uploaded = await uploadFile(rawFile, Number(taskId));
    const fileId = uploaded?.fileId;
    if (!fileId) {
      ElMessage.error('上传失败：未拿到 fileId');
      return;
    }

    if (subList.length > 0) {
      await batchSubmitMaterial({
        third_task_id: Number(taskId),
        file_id: Number(fileId),
        comment,
        sub_list: subList,
      });
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
          comment,
        });
      } else {
        await submitMaterial({
          task_id: Number(taskId),
          file_id: Number(fileId),
          reported_value: rv,
          data_type: dataType,
          comment,
        });
      }
    }

    if (!isCurrentTaskDetail(context)) return;
    ElMessage.success('材料已提交（已进入审核流程）');
    taskFileList.value = [];
    taskFeedbackForm.comment = '';
    await loadTaskDetail(taskId);
  } catch (e) {
    if (isCurrentTaskDetail(context)) ElMessage.error(e?.message || '提交失败，请稍后重试');
  } finally {
    taskFeedbackSubmitting.value = false;
    if (isCurrentTaskDetail(context)) taskDrawerLoading.value = false;
  }
};

// 提交任务审核
const submitTaskAudit = async () => {
  if (taskAuditSubmitting.value || taskDrawerLoading.value) return;
  const context = taskDetailContext();
  if (!isCurrentTaskDetail(context) || !taskCanAudit.value ||
      Number(taskLatestAudit.value?.taskId) !== context.taskId ||
      Number(currentTask.value?._raw?.taskId) !== context.taskId) return;
  const result = taskAuditForm.result;
  const comment = taskAuditForm.comment;
  if (!taskAuditForm.comment) {
    ElMessage.warning('请输入审批意见');
    return;
  }

  if (!taskLatestAudit.value) {
    ElMessage.warning('未找到审批单信息');
    return;
  }

  const subId = taskLatestAudit.value.subId ?? taskLatestAudit.value.sub_id;
  if (!subId) {
    ElMessage.warning('未找到审批单ID');
    return;
  }

  taskAuditSubmitting.value = true;
  try {
    await auditTask({
      sub_id: Number(subId),
      is_pass: result === 1,
      title: comment,
      content: comment
    });

    if (!isCurrentTaskDetail(context)) return;
    ElMessage.success(result === 1 ? '审批通过' : '已驳回');

    taskAuditForm.result = 1;
    taskAuditForm.comment = '同意';

    const taskId = context.taskId;
    if (taskId) {
      await loadTaskDetail(taskId);
    }
  } catch (e) {  
    if (isCurrentTaskDetail(context)) ElMessage.error(e?.message || '审核提交失败，请稍后重试');
  } finally {
    taskAuditSubmitting.value = false;
  }
};

// 预览任务材料文件
const previewTaskMaterialFile = async (auditItem) => {
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

  if (taskPreviewUrl.value) {
    try { URL.revokeObjectURL(taskPreviewUrl.value); } catch (e) { // eslint-disable-line no-unused-vars
      // 忽略撤销错误
    }
  }

  try {
    const resp = await adminTestFetch(`/system/download/${fileId}`, {
      method: 'GET',
      headers: { Authorization: token }
    });

    if (resp.status === 401) {
      try { localStorage.removeItem('token'); } catch (e) { // eslint-disable-line no-unused-vars
        // 忽略清理错误
      }
      window.location.replace('/login');
      return;
    }

    if (!resp.ok) {
      throw new Error(`文件获取失败(${resp.status})`);
    }

    const cd = resp.headers.get('content-disposition') || '';
    const filename = parseFilenameFromContentDisposition(cd);
    taskPreviewFileName.value = filename || `file_${fileId}`;

    const blob = await resp.blob();
    taskPreviewFileBlob.value = blob;
    const ext = getExtFromName(taskPreviewFileName.value);
    const isPdf = ext === 'pdf' || (await isPdfBlob(blob));

    if (isPdf) {
      taskPreviewType.value = 'pdf';
      const pdfBlob = blob.type === 'application/pdf' ? blob : new Blob([blob], { type: 'application/pdf' });
      taskPreviewUrl.value = URL.createObjectURL(pdfBlob);
    } else {
      taskPreviewType.value = 'doc';
    }
    taskPreviewVisible.value = true;
  } catch (e) {
    ElMessage.error(e?.message || '预览失败，请稍后重试');
  }
};

const downloadTaskByFileId = async (fileId, fileSuffix) => { // eslint-disable-line no-unused-vars
  if (!fileId) return;
  const token = localStorage.getItem('token') || '';
  if (!token) {
    ElMessage.error('未登录或登录已过期，请先登录');
    return;
  }

  try {
    const resp = await adminTestFetch(`/system/download/${fileId}`, {
      method: 'GET',
      headers: { Authorization: token }
    });

    if (resp.status === 401) {
      try { localStorage.removeItem('token'); } catch (e) { // eslint-disable-line no-unused-vars
        // 忽略清理错误
      }
      window.location.replace('/login');
      return;
    }

    if (!resp.ok) {
      throw new Error(`下载失败(${resp.status})`);
    }

    const cd = resp.headers.get('content-disposition') || '';
    let filename = parseFilenameFromContentDisposition(cd);
    if (!filename) {
      filename = `file_${fileId}`;
    }

    const blob = await resp.blob();
    const url = URL.createObjectURL(blob);

    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    a.remove();
    setTimeout(() => {
      try { URL.revokeObjectURL(url); } catch (e) { // eslint-disable-line no-unused-vars
        // 忽略撤销错误
      }
    }, 800);
  } catch (e) {
    ElMessage.error(e?.message || '下载失败，请稍后重试');
  }
};

const downloadTaskPreviewFile = () => {
  if (!taskPreviewFileBlob.value || !taskPreviewFileName.value) {
    ElMessage.warning('暂无可下载的文件');
    return;
  }

  try {
    const url = URL.createObjectURL(taskPreviewFileBlob.value);
    const a = document.createElement('a');
    a.href = url;
    a.download = taskPreviewFileName.value;
    document.body.appendChild(a);
    a.click();
    a.remove();
    setTimeout(() => {
      try { URL.revokeObjectURL(url); } catch (e) { // eslint-disable-line no-unused-vars
        // 忽略撤销错误
      }
    }, 500);
  } catch (e) { // eslint-disable-line no-unused-vars
    ElMessage.error('下载失败，请稍后重试');
  }
};

const getExtFromName = (name) => {
  if (!name) return '';
  const m = name.match(/\.([a-z0-9]+)$/i);
  return m ? m[1].toLowerCase() : '';
};

const isPdfBlob = async (blob) => {
  try {
    const buf = await blob.slice(0, 5).arrayBuffer();
    const bytes = new Uint8Array(buf);
    return bytes[0] === 0x25 && bytes[1] === 0x50 && bytes[2] === 0x44 && bytes[3] === 0x46 && bytes[4] === 0x2D;
  } catch (e) { // eslint-disable-line no-unused-vars
    return false;
  }
};

const cleanupTaskPreviewUrl = () => {
  if (taskPreviewUrl.value) {
    try { URL.revokeObjectURL(taskPreviewUrl.value); } catch (e) { // eslint-disable-line no-unused-vars
      // 忽略撤销错误
    }
    taskPreviewUrl.value = '';
  }
  if (taskPreviewFileBlob.value) {
    taskPreviewFileBlob.value = null;
  }
};

// 是否允许手动提交（必须选择具体年份）
const canSubmitManual = computed(() => {
  if (!canUsePermission('performance.fill')) return false;
  return manualSubmitDisabledReason.value === '';
});

// 跳转到任务详情（打开任务详情抽屉）
const jumpToTask = async (task) => {
  taskReturnToPerformance.value = true;
  detailDrawerVisible.value = false;
  taskDrawerVisible.value = true;
  await loadTaskDetail(task.taskId);
};

const handleBackToPerformanceDetail = () => {
  taskDrawerVisible.value = false;
  if (currentPerformance.value) {
    detailDrawerVisible.value = true;
  }
};

const handleTaskDrawerClosed = () => {
  taskReturnToPerformance.value = false;
};

// 监听筛选条件变化
watch(detailDrawerVisible, (visible) => {
  if (!visible) {
    transferLogs.value = [];
    invalidatePerformanceAudit();
  }
}, { flush: 'sync' });

watch(filterYear, () => {
  invalidatePerformanceAudit();
}, { flush: 'sync' });

watch([filterDataType, actualSearchText], () => {
  applyFilters();
});

// 自动刷新定时器
let autoRefreshTimer = null;

const preparePerformanceLink = () => {
  openedPerformanceLink = '';
  invalidatePerformanceAudit();
  detailDrawerVisible.value = false;
  currentPerformance.value = null;
  performanceDetailYear.value = null;
  tasksLoading.value = false;
};
watch(() => `${route.query.perfId || ''}:${route.query.year || ''}`, () => {
  preparePerformanceLink();
  if(route.query.perfId)queryBar.value?.replace({year:getDefaultPerformanceYear()});
  else loadPerformances();
});
onMounted(() => {
  preparePerformanceLink();
  loadTransferPermission().then(loadRelationCounts);
  loadManualPerformanceAudits();
  void get('/performance/quick-filters').then(value=>{if(!performanceDetailDisposed){performanceShortcuts.value=value;shortcutError.value='';}}).catch(()=>{if(!performanceDetailDisposed)shortcutError.value='快捷筛选加载失败';});

  autoRefreshTimer = setInterval(() => {
    if (!document.hidden) {
      loadPerformances({background:true});
    }
  }, 30000);
});

onUnmounted(() => {
  taskDetailVersion++;
  taskDetailId = null;
  performanceDetailDisposed = true;
  invalidatePerformanceAudit();
  if (autoRefreshTimer) {
    clearInterval(autoRefreshTimer);
  }
});

</script>

<style scoped>
/* 所有样式保持不变 */
.works-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
}

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
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 16px;
}

.filter-left {
  display: flex;
  gap: 16px;
  align-items: center;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-size: 14px;
  color: #606266;
}

.filter-select {
  width: 120px;
}

.filter-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-input {
  width: 240px;
}

.search-btn {
  background-color: #1890ff;
  border-color: #1890ff;
}

.export-btn {
  background-color: #fff;
  border-color: #dcdfe6;
  color: #606266;
}

.table-wrapper {
  position: relative;
  flex: 1;
  overflow: auto;
}

.refresh-progress {
  position: fixed;
  inset: 0;
  z-index: 3000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.46);
  backdrop-filter: blur(2px);
}

.refresh-progress-card {
  width: min(320px, calc(100vw - 48px));
  padding: 20px 22px;
  border: 1px solid #d9ecff;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 14px 36px rgba(24, 144, 255, 0.18);
  text-align: center;
}

.refresh-spinner {
  width: 42px;
  height: 42px;
  margin: 0 auto 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #ecf5ff;
  color: #1890ff;
  font-size: 22px;
  animation: refresh-spin 0.9s linear infinite;
}

.refresh-text {
  font-size: 15px;
  font-weight: 700;
  color: #303133;
}

.refresh-subtext {
  margin-top: 6px;
  font-size: 13px;
  color: #606266;
}

.refresh-bar {
  height: 4px;
  margin-top: 16px;
  overflow: hidden;
  border-radius: 999px;
  background: #e4e7ed;
}

.refresh-bar span {
  display: block;
  width: 42%;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #1890ff, #67c23a);
  animation: refresh-bar-move 1.2s ease-in-out infinite;
}

.refresh-fade-enter-active,
.refresh-fade-leave-active {
  transition: opacity 0.18s ease;
}

.refresh-fade-enter-from,
.refresh-fade-leave-to {
  opacity: 0;
}

@keyframes refresh-spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes refresh-bar-move {
  0% {
    transform: translateX(-120%);
  }
  100% {
    transform: translateX(240%);
  }
}

.task-name-cell {
  cursor: pointer;
}

.task-name-text:hover {
  text-decoration: underline;
}

.task-code {
  color: #909399;
  font-family: monospace;
  margin-right: 6px;
  font-weight: 700;
}

.child-count {
  margin-left: 6px;
  color: #909399;
  font-size: 12px;
}

:deep(.el-table__expand-icon) {
  display: none;
}

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

.progress-text {
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: monospace;
  font-size: 13px;
}

.target-value {
  color: #909399;
}

.current-value {
  color: #1890ff;
  font-weight: bold;
}

.mx-1 {
  margin: 0 4px;
}

.drawer-content {
  padding: 0;
}

.drawer-header {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 24px;
}

.drawer-back-btn {
  flex-shrink: 0;
  color: #606266;
}

.drawer-back-btn:hover {
  color: #1890ff;
  background: #ecf5ff;
}

.drawer-title {
  color: #303133;
  font-size: 16px;
  font-weight: 600;
  line-height: 24px;
}

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

.info-row--professional-owner .label {
  width: 112px;
}

.highlight-value {
  color: #1890ff;
  font-weight: bold;
  font-family: monospace;
}

.font-mono {
  font-family: monospace;
}

.mt-4 {
  margin-top: 16px;
}

.mt-6 {
  margin-top: 24px;
}

.mb-4 {
  margin-bottom: 16px;
}

.mb-6 {
  margin-bottom: 24px;
}

.mr-1 {
  margin-right: 4px;
}

.ml-2 {
  margin-left: 8px;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
}

.text-gray-400 {
  color: #909399;
}

.text-gray-900 {
  color: #303133;
}

.text-gray-700 {
  color: #4a5568;
}

.text-gray-600 {
  color: #606266;
}

.text-base {
  font-size: 16px;
}

.font-bold {
  font-weight: bold;
}

.font-medium {
  font-weight: 500;
}

.pl-2 {
  padding-left: 8px;
}

.text-right {
  text-align: right;
}

.step-section {
  margin-top: 20px;
}

.progress-input-wrapper {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  user-select: none;
}

.percent-suffix {
  color: #606266;
  font-size: 14px;
}

.wheel-tip {
  color: #909399;
  font-size: 12px;
}

.hide-upload-trigger :deep(.el-upload--text) {
  display: none;
}

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
  cursor: pointer;
}

.preview-text:hover {
  color: #1890ff;
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

.doc-icon {
  margin-bottom: 20px;
}

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

.sub-title {
  font-size: 14px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 8px;
}

.upload-area {
  width: 100%;
}

/* 手动提交区域样式 */
.manual-submit-box {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
  margin-top: 16px;
  border: 1px solid #e4e7ed;
}

.target-value-display {
  font-size: 16px;
  font-weight: bold;
  color: #1890ff;
  font-family: monospace;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.submit-history-tip {
  margin-top: 20px;
}

:deep(.el-alert) {
  background-color: #ecf5ff;
}

:deep(.el-alert__title) {
  font-size: 14px;
  font-weight: bold;
}

/* 年份未选择提示样式 */
.year-warning-tip {
  margin-bottom: 20px;
}

.year-warning-tip :deep(.el-alert) {
  background-color: #fdf6ec;
  border: 1px solid #faecd8;
}

.year-warning-tip :deep(.el-alert__title) {
  font-size: 14px;
  color: #e6a23c;
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

/* 管理员编辑绩效关联任务 */
.relation-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.relation-count {
  color: #303133;
  font-weight: 600;
}

.relation-log {
  margin-bottom: 12px;
}
</style>
