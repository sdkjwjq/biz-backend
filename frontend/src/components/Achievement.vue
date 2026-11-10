<template>
  <div class="works-container">
    <!-- 顶部滚动信息条（预留） -->
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

    <!-- 顶部项目信息卡片 -->
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
        <el-tag size="small" effect="plain" type="info">{{ achievementCountText }}</el-tag>
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
        <el-button type="primary" class="create-btn" v-if="false">
          <el-icon class="mr-1">
            <Plus />
          </el-icon>
          新建成果
        </el-button>
      </div>
    </div>

    <!-- 筛选与表格区域 -->
    <div class="table-card">
      <!-- 筛选区 -->
      <QueryFilterBar ref="queryBar" response-mode="tree" domain="achievements" title="成果" :defaults="{year: '2026'}" @result="receiveQuery" @state="achievementQueryState = $event" ><template #actions><el-button v-if="canUploadAchievement" type="primary" @click="handleAdd">新增</el-button><el-button :disabled="loading" @click="reportVisible=true">统计 / 导出</el-button></template></QueryFilterBar>
      <el-alert v-if="achievementListError" :title="achievementListError" type="error" :closable="false"><el-button link type="primary" @click="loadAchievements">重新加载成果</el-button></el-alert>
      <div class="table-wrapper" v-loading="loading || achievementQueryState.loading">
        <el-table ref="achievementTableRef" :data="tableData" :empty-text="achievementEmptyText" style="width: 100%" border row-key="id"
          :row-class-name="({ row }) => detailRowClass(row)"
          :tree-props="{ children: 'children', hasChildren: 'hasChildren' }" :expand-row-keys="expandedRowKeys"
          @expand-change="handleExpandChange" @row-click="handleRowClick" :default-expand-all="false"
          :header-cell-style="{ background: '#f8f9fa', color: '#606266', fontWeight: 'bold' }">
          <el-table-column prop="name" label="成果类别/建设成果名称" min-width="360" class-name="achievement-title-column">
            <template #default="scope">
              <span v-if="scope.row.isCategory" class="task-name-cell text-gray-900 text-base">
                <span class="task-name-text">{{ scope.row.name }}</span>
                <span v-if="(scope.row.children?.length || 0) > 0" class="child-count">（{{ scope.row.children.length
                  }}）</span>
              </span>
              <span v-else class="task-name-cell text-gray-600 pl-2">
                <div class="achievement-name-details"><HoverScrollText :text="scope.row.name" /><div v-if="Number(scope.row.auditStatus ?? scope.row.audit_status) < 0" class="rejection-reason"><b>驳回原因：</b>{{ scope.row.rejectionReason || scope.row.rejection_reason || '历史记录未保存驳回原因' }}</div></div>
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="level" label="级别" width="110" align="center">
            <template #default="scope">
              <span v-if="scope.row.isCategory"></span>
              <el-tag v-else :type="getLevelTag(scope.row.level)" effect="plain" size="small">
                {{ getLevelName(scope.row.level) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="gradeAndCount" label="成果等次及数量" min-width="220" align="center" show-overflow-tooltip>
            <template #header>
              <span>成果等次及数量</span>
              <el-tooltip content="特等奖、一等奖、二等奖、三等奖、金奖、银奖、铜奖、优胜奖、不定等次，并填报各等次获奖数量，同一建设成果信息可多选" placement="top">
                <el-icon class="ml-1" style="cursor: help; color: #909399;">
                  <QuestionFilled />
                </el-icon>
              </el-tooltip>
            </template>
            <template #default="scope">
              <span v-if="scope.row.isCategory"></span>
              <div v-else-if="getGradeAndCountText(scope.row)" class="grade-count-cell">
                {{ getGradeAndCountText(scope.row) }}
              </div>
              <span v-else></span>
            </template>
          </el-table-column>
          <el-table-column prop="gotTime" label="颁发时间" width="150" align="center">
            <template #header>
              <span>颁发时间</span>
              <el-tooltip content="建设成果上级发文时间" placement="top">
                <el-icon class="ml-1" style="cursor: help; color: #909399;">
                  <QuestionFilled />
                </el-icon>
              </el-tooltip>
            </template>
            <template #default="scope">
              <span v-if="scope.row.isCategory"></span>
              <span v-else>{{ formatDate(scope.row.gotTime ?? scope.row.got_time) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="fileId" label="佐证材料" width="130" align="center">
            <template #header>
              <span>佐证材料</span>
              <el-tooltip content="在对应成果条目中上传对应的文件原件和相应证书" placement="top">
                <el-icon class="ml-1" style="cursor: help; color: #909399;">
                  <QuestionFilled />
                </el-icon>
              </el-tooltip>
            </template>
            <template #default="scope">
              <span v-if="scope.row.isCategory"></span>
              <template v-else>
                <el-dropdown
                  v-if="scope.row.fileId || scope.row.file_id || scope.row._raw?.fileId || scope.row._raw?.file_id"
                  trigger="click" @command="(cmd) => handleMaterialAction(cmd, scope.row)">
                  <el-button link type="primary" size="small">
                    查看材料<el-icon class="el-icon--right">
                      <ArrowDown />
                    </el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="preview">
                        <el-icon>
                          <View />
                        </el-icon>
                        预览
                      </el-dropdown-item>
                      <el-dropdown-item command="download">
                        <el-icon>
                          <Download />
                        </el-icon>
                        下载
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
                <span v-else></span>
              </template>
            </template>
          </el-table-column>
          <el-table-column prop="department" label="组织部门" min-width="180" show-overflow-tooltip>
            <template #header>
              <span>组织部门</span>
              <el-tooltip content="如：教育部办公厅等，注意不同于校内部门" placement="top">
                <el-icon class="ml-1" style="cursor: help; color: #909399;">
                  <QuestionFilled />
                </el-icon>
              </el-tooltip>
            </template>
            <template #default="scope">
              <span v-if="scope.row.isCategory"></span>
              <span v-else>{{ scope.row.department ?? '' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="auditStatus" label="归档状态" width="120" align="center">
            <template #default="scope">
              <span v-if="scope.row.isCategory"></span>
              <el-tag v-else :type="getAchievementAuditStatusTag(scope.row.auditStatus ?? scope.row.audit_status)" size="small" effect="plain">
                {{ getAchievementAuditStatusText(scope.row.auditStatus ?? scope.row.audit_status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="210" fixed="right" align="center">
            <template #default="scope">
              <span v-if="scope.row.isCategory"></span>
              <div v-else class="table-actions">
                <el-button link type="primary" size="small" @click="handleViewDetail(scope.row)">
                  <el-icon class="mr-1">
                    <View />
                  </el-icon>
                  查看
                </el-button>
                <el-button v-if="canEditAchievement(scope.row)" link type="primary" size="small"
                  @click="handleEdit(scope.row)">
                  <el-icon class="mr-1">
                    <Edit />
                  </el-icon>修改
                </el-button>
                <el-button v-if="canResubmitAchievement(scope.row)" link type="primary" :loading="resubmittingId===scope.row.achId" :disabled="resubmittingId!=null" @click="handleResubmit(scope.row)">重新提交</el-button>
                <el-button v-if="canAuditAchievement(scope.row)" link type="primary" size="small"
                  @click="handleAudit(scope.row)">
                  <el-icon class="mr-1">
                    <Stamp />
                  </el-icon>
                  审核
                </el-button>
                <el-button v-if="canModifyAchievement(scope.row) && canUsePermission('achievements.delete')" link type="danger" size="small"
                  @click="handleDelete(scope.row)">
                  <el-icon class="mr-1">
                    <Delete />
                  </el-icon>删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 新增成果对话框 -->
    <el-config-provider :locale="zhCn">
      <el-dialog v-model="dialogVisible" :title="achievementDialogTitle" width="800px" :destroy-on-close="true">
        <el-form :model="form" label-width="150px" :rules="rules" ref="formRef" class="achievement-form">
          <el-form-item label="成果类别" prop="category">
            <el-select v-model="form.category" placeholder="请选择成果类别" style="width: 100%">
              <el-option v-for="(cat, index) in achievementCategories" :key="index" :label="cat"
                :value="index + 1"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="级别" prop="level">
            <el-select v-model="form.level" placeholder="请选择级别" style="width: 100%">
              <el-option label="国家级" value="国家级"></el-option>
              <el-option label="省级" value="省级"></el-option>
              <el-option label="市级" value="市级"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="建设成果名称" prop="achName">
            <el-input v-model="form.achName" placeholder="请输入建设成果名称"></el-input>
          </el-form-item>
          <el-form-item label="组织部门" prop="department">
            <el-input v-model="form.department" placeholder="如：教育部办公厅等"></el-input>
          </el-form-item>
          <el-form-item label="颁发时间" prop="gotTime">
            <el-date-picker v-model="form.gotTime" type="datetime" placeholder="选择颁发时间" style="width: 100%"
              format="YYYY年MM月DD日 HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss"
              :shortcuts="dateShortcuts"></el-date-picker>
          </el-form-item>
          <el-form-item label="具体内容" prop="comment">
            <el-input v-model="form.comment" type="textarea" :rows="3" placeholder="请输入具体内容（项目名称、荣誉名称、奖项名称等）"
              maxlength="500" show-word-limit></el-input>
          </el-form-item>
          <el-form-item label="是否竞赛">
            <el-radio-group v-model="form.isCompetition">
              <el-radio :label="0">不是竞赛</el-radio>
              <el-radio :label="1">是竞赛</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="成果等次及数量">
            <div class="grade-selection">
              <!-- 已选择的等次列表 -->
              <div v-if="gradeSelections.length > 0" class="grade-list">
                <div v-for="(item, index) in gradeSelections" :key="index" class="grade-item">
                  <el-select v-model="item.grade" placeholder="选择等次" style="width: 150px" class="grade-select">
                    <el-option v-for="grade in gradeOptions" :key="grade" :label="grade" :value="grade"></el-option>
                  </el-select>
                  <el-input-number v-model="item.count" :min="1" :max="999" placeholder="数量"
                    style="width: 120px; margin-left: 10px;"></el-input-number>
                  <el-button type="danger" link size="small" @click="removeGradeSelection(index)"
                    style="margin-left: 10px;">
                    <el-icon>
                      <Delete />
                    </el-icon>
                  </el-button>
                </div>
              </div>
              <!-- 添加新等次按钮 -->
              <el-button type="primary" plain @click="addGradeSelection" style="margin-top: 10px;">
                <el-icon class="mr-1">
                  <Plus />
                </el-icon>
                添加等次及数量
              </el-button>
            </div>
          </el-form-item>
          <el-form-item label="报送部门" prop="deptId">
            <el-select v-model="form.deptId" placeholder="请选择部门" style="width: 100%" filterable :disabled="currentAuth?.isDepartmentAccount">
              <el-option v-for="dept in deptList" :key="dept.deptId" :label="dept.deptName" :value="dept.deptId">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="佐证材料">
            <el-upload :auto-upload="false" :on-change="handleFileChange" :before-remove="handleFileRemove" :limit="1"
              :file-list="uploadFileList">
              <el-button type="primary">选择文件</el-button>
              <template #tip>
                <div class="el-upload__tip">支持上传文件原件和相应证书（仅支持 PDF、DOC、DOCX 格式，文件大小不超过 10MB）</div>
              </template>
            </el-upload>
            <div v-if="uploadFileList.length > 0" class="file-info"
              style="margin-top: 10px; color: #606266; font-size: 12px;">
              已选择文件：{{ uploadFileList[0].name }}（将在提交时自动上传）
            </div>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">{{ achievementSubmitText }}</el-button>
        </template>
      </el-dialog>
    </el-config-provider>

    <AchievementReport v-if="reportVisible" v-model="reportVisible" :rows="allAchievements.filter(item => queryIds.has(String(item.achId)))" :refresh-rows="refreshAchievementExport" :dept-name="getDeptNameById" :category-name="getCategoryName" />
    <!-- 成果详情抽屉 -->
    <el-drawer v-model="detailDrawerVisible" :title="isAuditMode ? '成果审核' : '成果详情'" size="70%" :destroy-on-close="true" @closed="handleDetailDrawerClosed">
      <div v-if="currentAchievement" ref="achievementContentRef" class="drawer-content">
        <AdjacentDetails :items="achievementDetailItems" :current-key="String(getAchievementId(currentAchievement))" :row-key="getAchievementId"
          :busy="loading || achievementAuditLoading || submitting" :dirty="() => achievementDraft() !== achievementSavedDraft" :open="openAdjacentAchievement" />
        <DetailActions :busy="loading || achievementAuditLoading || submitting" :link="achievementDetailLink" :locate="locateCurrentAchievement" />
        <h4 data-detail-section="基本信息">基本信息</h4>
        <el-form :model="form" label-width="150px" :rules="rules" ref="auditFormRef" class="achievement-form audit-form">
          <el-form-item label="成果类别" prop="category">
            <el-select v-model="form.category" placeholder="请选择成果类别" style="width: 100%" disabled>
              <el-option v-for="(cat, index) in achievementCategories" :key="index" :label="cat"
                :value="index + 1"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="级别" prop="level">
            <el-select v-model="form.level" placeholder="请选择级别" style="width: 100%" disabled>
              <el-option label="国家级" value="国家级"></el-option>
              <el-option label="省级" value="省级"></el-option>
              <el-option label="市级" value="市级"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="建设成果名称" prop="achName">
            <el-input v-model="form.achName" placeholder="请输入建设成果名称" disabled></el-input>
          </el-form-item>
          <el-form-item label="组织部门" prop="department">
            <el-input v-model="form.department" placeholder="如：教育部办公厅等" disabled></el-input>
          </el-form-item>
          <el-form-item label="颁发时间" prop="gotTime">
            <el-date-picker v-model="form.gotTime" type="datetime" placeholder="选择颁发时间" style="width: 100%"
              format="YYYY年MM月DD日 HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss"
              :shortcuts="dateShortcuts" disabled></el-date-picker>
          </el-form-item>
          <el-form-item label="具体内容" prop="comment">
            <el-input v-model="form.comment" type="textarea" :rows="3" placeholder="请输入具体内容（项目名称、荣誉名称、奖项名称等）"
              maxlength="500" show-word-limit disabled></el-input>
          </el-form-item>
          <el-form-item label="是否竞赛">
            <el-radio-group v-model="form.isCompetition" disabled>
              <el-radio :label="0">不是竞赛</el-radio>
              <el-radio :label="1">是竞赛</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="成果等次及数量">
            <div class="grade-selection">
              <div v-if="gradeSelections.length > 0" class="grade-list">
                <div v-for="(item, index) in gradeSelections" :key="index" class="grade-item">
                  <el-select v-model="item.grade" placeholder="选择等次" style="width: 150px" class="grade-select" disabled>
                    <el-option v-for="grade in gradeOptions" :key="grade" :label="grade" :value="grade"></el-option>
                  </el-select>
                  <el-input-number v-model="item.count" :min="1" :max="999" placeholder="数量" disabled
                    style="width: 120px; margin-left: 10px;"></el-input-number>
                  <el-button v-if="false" type="danger" link size="small" @click="removeGradeSelection(index)"
                    style="margin-left: 10px;">
                    <el-icon>
                      <Delete />
                    </el-icon>
                  </el-button>
                </div>
              </div>
              <el-button v-if="false" type="primary" plain @click="addGradeSelection" style="margin-top: 10px;">
                <el-icon class="mr-1">
                  <Plus />
                </el-icon>
                添加等次及数量
              </el-button>
            </div>
          </el-form-item>
          <el-form-item label="报送部门" prop="deptId">
            <el-select v-model="form.deptId" placeholder="请选择部门" style="width: 100%" filterable disabled>
              <el-option v-for="dept in deptList" :key="dept.deptId" :label="dept.deptName" :value="dept.deptId">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="佐证材料" data-detail-section="材料">
            <div class="audit-material-row">
              <el-button
                v-if="currentAchievement.fileId || currentAchievement.file_id || currentAchievement._raw?.fileId || currentAchievement._raw?.file_id"
                link type="primary" size="small" @click="handleMaterialAction('preview', currentAchievement)">
                查看原材料
              </el-button>
              <el-upload v-if="false" :auto-upload="false" :on-change="handleFileChange" :before-remove="handleFileRemove" :limit="1"
                :file-list="uploadFileList">
                <el-button type="primary">选择新文件</el-button>
                <template #tip>
                  <div class="el-upload__tip">支持 PDF、DOC、DOCX，文件大小不超过 10MB；不选择则保留原材料</div>
                </template>
              </el-upload>
            </div>
          </el-form-item>
        </el-form>

        <div class="achievement-audit-panel">
          <h4 class="section-title">归档审核流转</h4>
          <el-steps :active="achievementAuditStepActive" finish-status="success" process-status="process" align-center>
            <el-step title="提交归档" :description="achievementSubmitStepText" />
            <el-step title="管理员归档" :description="achievementAdminStepText" />
          </el-steps>

          <div v-if="isAuditMode && canAuditCurrentAchievement" class="achievement-audit-action">
            <el-form label-position="top">
              <el-form-item label="审核结果">
                <el-radio-group v-model="achievementAuditForm.result">
                  <el-radio :label="1">通过并归档</el-radio>
                  <el-radio :label="0">退回</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="审核意见">
                <el-input v-model="achievementAuditForm.comment" type="textarea" :rows="3" placeholder="请输入审核意见" />
              </el-form-item>
              <div class="drawer-footer">
                <el-button @click="cancelAudit">取消</el-button>
                <el-button type="primary" @click="handleAuditSubmit" :loading="submitting">提交审核</el-button>
              </div>
            </el-form>
          </div>

          <el-button v-if="!isAuditMode && canResubmitAchievement(currentAchievement)" type="primary" :loading="resubmittingId!=null" @click="handleResubmit(currentAchievement)">重新提交审核</el-button>
          <h4 class="section-title mt-4">审核日志</h4>
          <div v-loading="achievementAuditLoading" class="achievement-audit-log">
            <el-empty v-if="!achievementAuditLogs.length" description="暂无审核日志" :image-size="72" />
            <el-timeline v-else>
              <el-timeline-item
                v-for="log in achievementAuditLogs"
                :key="log.logId || log.log_id || `${log.operatorId || log.operator_id}_${log.createTime || log.create_time}`"
                :timestamp="formatDateTime(log.createTime || log.create_time)"
                placement="top">
                <div class="audit-log-card">
                  <div class="audit-log-title">{{ getAchievementActionText(log.actionType || log.action_type) }}</div>
                  <div class="audit-log-meta">
                    操作人：{{ getUserName(log.operatorId || log.operator_id) }}
                    <span class="audit-log-status">
                      {{ getAchievementAuditStatusText(log.preStatus ?? log.pre_status) }}
                      →
                      {{ getAchievementAuditStatusText(log.postStatus ?? log.post_status) }}
                    </span>
                  </div>
                  <div v-if="log.comment" class="audit-log-comment">
                    {{ getAchievementLogCommentLabel(log.actionType || log.action_type) }}：{{ log.comment }}
                  </div>
                </div>
              </el-timeline-item>
            </el-timeline>
          </div>
        </div>

        <!-- 成果基本信息 -->
        <div v-if="false" class="task-info-box">
          <div class="info-grid">
            <div class="info-row">
              <span class="label">成果类别：</span>
              <span class="value">{{ getCategoryName(currentAchievement.category ?? currentAchievement._raw?.category)
                }}</span>
            </div>
            <div class="info-row">
              <span class="label">级别：</span>
              <span class="value">
                <el-tag :type="getLevelTag(currentAchievement.level ?? currentAchievement._raw?.level)" effect="plain"
                  size="small">
                  {{ getLevelName(currentAchievement.level ?? currentAchievement._raw?.level) }}
                </el-tag>
              </span>
            </div>
            <div class="info-row info-row--full">
              <span class="label">建设成果名称：</span>
              <span class="value">{{ currentAchievement.name ?? currentAchievement.achName ??
                currentAchievement.ach_name ??
                '' }}</span>
            </div>
            <div class="info-row">
              <span class="label">组织部门：</span>
              <span class="value">{{ currentAchievement.department ?? currentAchievement._raw?.department ?? ''
                }}</span>
            </div>
            <div class="info-row">
              <span class="label">颁发时间：</span>
              <span class="value">{{ formatDate(currentAchievement.gotTime ?? currentAchievement.got_time ??
                currentAchievement._raw?.gotTime ?? currentAchievement._raw?.got_time) }}</span>
            </div>
            <div class="info-row">
              <span class="label">报送部门：</span>
              <span class="value">{{ getDeptNameById(currentAchievement.deptId ?? currentAchievement.dept_id ??
                currentAchievement._raw?.deptId ?? currentAchievement._raw?.dept_id) }}</span>
            </div>
            <div class="info-row">
              <span class="label">归档状态：</span>
              <span class="value">
                <el-tag :type="getAchievementAuditStatusTag(currentAchievement.auditStatus ?? currentAchievement.audit_status ?? currentAchievement._raw?.auditStatus ?? currentAchievement._raw?.audit_status)" size="small" effect="plain">
                  {{ getAchievementAuditStatusText(currentAchievement.auditStatus ?? currentAchievement.audit_status ?? currentAchievement._raw?.auditStatus ?? currentAchievement._raw?.audit_status) }}
                </el-tag>
              </span>
            </div>
            <div class="info-row info-row--full" v-if="currentAchievement.comment ?? currentAchievement._raw?.comment">
              <span class="label">具体内容：</span>
              <span class="value">{{ currentAchievement.comment ?? currentAchievement._raw?.comment }}</span>
            </div>
            <div class="info-row info-row--full" v-if="getGradeAndCountText(currentAchievement)">
              <span class="label">成果等次及数量：</span>
              <span class="value">{{ getGradeAndCountText(currentAchievement) }}</span>
            </div>
            <div class="info-row"
              v-if="currentAchievement.fileId || currentAchievement.file_id || currentAchievement._raw?.fileId || currentAchievement._raw?.file_id">
              <span class="label">佐证材料：</span>
              <span class="value">
                <el-button link type="primary" size="small"
                  @click="handleMaterialAction('preview', currentAchievement)">
                  <el-icon class="mr-1">
                    <View />
                  </el-icon>
                  预览
                </el-button>
                <el-button link type="primary" size="small"
                  @click="handleMaterialAction('download', currentAchievement)" style="margin-left: 10px;">
                  <el-icon class="mr-1">
                    <Download />
                  </el-icon>
                  下载
                </el-button>
              </span>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>

    <!-- 文件预览弹窗 -->
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
            在实际生产环境中，此处会集成 Office Web Apps 或 PDF 转换服务进行预览。
          </p>
          <el-button type="primary" class="mt-4" @click="downloadPreviewFile">下载文件</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>

const queryBar = ref(null), queryIds = ref(new Set());
const receiveQuery = async result => {
  allAchievements.value=result.items||[];achievementListReady.value=true;achievementListError.value='';loading.value=false;
  queryIds.value = new Set((result.matchedIds||[]).map(String));
  const nextYear = result.query.year === 'missing' ? '' : String(result.query.year || '');
  const changed = filterYear.value !== nextYear;
  filterYear.value = nextYear;
  applyFilters();
  void changed;
  listFilters.value = {owner:'',leader:'',status:'',category:'',level:'',competition:''}; quickFilter.value='all'; actualSearchText.value=''; searchText.value=''; applyFilters();
  await openAchievementLink();await achievementMemory.restoreTree(tableData.value);
};
import { isAdminTestEnabled } from '@/utils/adminTest';
import { adminTestFetch } from '@/utils/adminTest';

import { accessState, canUsePermission } from '@/utils/accessState';
import { ref, reactive, computed, onMounted, onUnmounted, watch, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import DetailActions from './DetailActions.vue';
import HoverScrollText from './HoverScrollText.vue';
import QueryFilterBar from './QueryFilterBar.vue';
import AdjacentDetails from './AdjacentDetails.vue';
import AchievementReport from './AchievementReport.vue';
import { useBrowseMemory } from '@/utils/browseMemory';
import { detailLeaves } from '@/utils/detailNavigation';
import { detailRowClass, locateDetailRow, confirmDetailLeave, findDetailPath } from '@/utils/detailNavigation';
import {
  Calendar, User, Plus, Download, View, QuestionFilled, Delete, Document, Stamp, Edit
} from '@element-plus/icons-vue';
import { ElMessageBox } from 'element-plus';
import {
  getTodoAchievementAudits,
  addAchievement,
  updateAchievement,
  resubmitAchievement,
  deleteAchievement,
  getAchievementAuditsByAchId,
  getAchievementAuditLogs,
  auditAchievement
} from '@/api/achievement';
import { get as queryGet } from '@/utils/request';
import { upload } from '@/utils/request';
import { getAllUsers } from '@/api/user';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';
import { getDeptById } from '@/api/dept';
import { ElMessage } from 'element-plus';
import zhCn from 'element-plus/dist/locale/zh-cn.mjs';

const route = useRoute();
const router = useRouter();

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

// 成果类别（固定十大类）
const achievementCategories = [
  '1.落实立德树人根本任务',
  '2.创新产教融合机制',
  '3.打造高水平专业群',
  '4.建设一流核心课程',
  '5.开放优质新形态教材',
  '6.建设高水平双师队伍',
  '7.建设产教融合实训基地',
  '8.构建数字化教学新生态',
  '9.拓展国际交流与合作',
  '10.打造一流区域型高端智库'
];

// 成果等次选项
const gradeOptions = [
  '特等奖',
  '一等奖',
  '二等奖',
  '三等奖',
  '金奖',
  '银奖',
  '铜奖',
  '优胜奖',
  '不定等次'
];

// 状态管理
const loading = ref(false);
const achievementListReady = ref(false), achievementListError = ref('');
const achievementQueryState = ref({ loading: true, initialized: false, error: '' });

watch(()=>achievementQueryState.value.responseCode,code=>{if([401,403,428].includes(Number(code))){detailDrawerVisible.value=false;allAchievements.value=[];queryIds.value=new Set();applyFilters();}})
watch(()=>achievementQueryState.value.error,error=>{if(error)loading.value=false})
const filterYear = ref('2026');
const searchText = ref('');
const actualSearchText = ref('');
const allAchievements = ref([]); // 原始数据
const tableData = ref([]); // 筛选后的树形数据
const achievementTableRef = ref(null);
const expandedRowKeys = ref([]); // 展开的行键
const quickFilter = ref('all');
const listFilters = ref({ owner: '', leader: '', status: '', category: '', level: '', competition: '' });
const achievementFilterMemory = Object.fromEntries(['status', 'category', 'level', 'competition'].map(key => [key, computed({ get: () => listFilters.value[key], set: value => { listFilters.value[key] = value; } })]));
const extraAchievementFilters = computed(() => [listFilters.value.status, listFilters.value.category, listFilters.value.level, listFilters.value.competition]);
const ownerFilter = computed({ get: () => listFilters.value.owner, set: value => { listFilters.value.owner = value; } });
const leaderFilter = computed({ get: () => listFilters.value.leader, set: value => { listFilters.value.leader = value; } });

const emit = defineEmits(['audit-completed']);
const reportVisible = ref(false);
const refreshAchievementExport = async () => {
  const query=queryBar.value?.applied||{};
  const params={purpose:'export',...Object.fromEntries(Object.entries(query).map(([k,v])=>['f.'+k,v]))};
  const result=await queryGet('/list-filters/achievements',params);
  return result.items;
};
const shortcutError = ref('');
const achievementTodoIds = ref([]);
const achievementContentRef = ref(null);
const achievementMemory = useBrowseMemory('achievements', { fields: { year: filterYear, search: actualSearchText, quick: quickFilter, owner: ownerFilter, leader: leaderFilter, ...achievementFilterMemory },
  expanded: expandedRowKeys, table: achievementTableRef, skip: () => !!route.query.achId });
if (!route.query.achId) searchText.value = actualSearchText.value;

const visibleAchievementCount = computed(() => {
  const countNodes = (nodes = []) => nodes.reduce((count, node) => {
    if (node.isCategory) return count + countNodes(node.children || []);
    return count + 1;
  }, 0);
  return countNodes(tableData.value);
});
const achievementCountText = computed(() => achievementListReady.value && achievementQueryState.value.initialized
  ? `共 ${visibleAchievementCount.value} 条`
  : achievementListError.value || achievementQueryState.value.error ? '成果加载失败' : '正在加载成果…');
const achievementEmptyText = computed(() => loading.value || achievementQueryState.value.loading ? '正在加载成果…'
  : achievementListError.value || achievementQueryState.value.error ? '成果加载失败，请重试'
    : achievementListReady.value && achievementQueryState.value.initialized ? '暂无匹配成果' : '正在加载成果…');



// 新增对话框
const dialogVisible = ref(false);
let achievementFormVersion = 0;
const isCurrentAchievementForm = (version) => !achievementDisposed && dialogVisible.value && version === achievementFormVersion;
watch(dialogVisible, () => { achievementFormVersion++; }, { flush: 'sync' });
const submitting = ref(false);
const isEditingAchievement = ref(false);
const editingAchievementId = ref(null);
const formRef = ref(null);
const auditFormRef = ref(null);
const isAuditMode = ref(false);
const auditingAchievementId = ref(null);
const auditingCreateBy = ref(null);
const achievementAuditList = ref([]);
const achievementAuditLogs = ref([]);
const achievementAuditLoading = ref(false);
let achievementDetailVersion = 0;
let achievementAuditRequestId = 0;
let achievementDisposed = false;
const achievementContext = () => ({ version: achievementDetailVersion, achId: Number(getAchievementId(currentAchievement.value)) });
const isCurrentAchievement = (context) => !achievementDisposed && detailDrawerVisible.value &&
  context.version === achievementDetailVersion && context.achId === Number(getAchievementId(currentAchievement.value));

onUnmounted(() => {
  achievementDisposed = true;
  achievementDetailVersion++;
  achievementAuditRequestId++;
});
const achievementAuditForm = reactive({
  result: 1,
  comment: '同意'
});

// 文件上传列表
const uploadFileList = ref([]);

// 文件预览相关
const previewVisible = ref(false);
const previewType = ref(''); // 'pdf' 或 'doc'
const previewUrl = ref('');
const previewFileName = ref('');
const previewFileBlob = ref(null);
const allUsers = ref([]); // 所有用户列表
const deptList = ref([]); // 部门列表
const detailDrawerVisible = ref(false);
watch(detailDrawerVisible, visible => {
  if (!visible) {
    achievementDetailVersion++;
    achievementAuditRequestId++;
    achievementAuditList.value = [];
    achievementAuditLogs.value = [];
    achievementAuditLoading.value = false;
  }
}, { flush: 'sync' }); // 详情抽屉显示状态
const currentAchievement = ref(null); // 当前查看的成果
const form = reactive({
  category: null,
  level: '',
  achName: '',
  department: '',
  gotTime: '',
  deptId: null,
  fileId: null,
  comment: '',
  isCompetition: 0,
  teDengJiang: 0,
  yiDengJiang: 0,
  erDengJiang: 0,
  sanDengJiang: 0,
  jinJiang: 0,
  yinJiang: 0,
  tongJiang: 0,
  youShengJiang: 0,
  budDengDengCi: 0
});

// 成果等次及数量选择列表
const gradeSelections = ref([]);

// 日期选择器快捷选项
const dateShortcuts = [
  {
    text: '今天',
    value: () => {
      const now = new Date();
      now.setHours(0, 0, 0, 0);
      return now;
    }
  },
  {
    text: '昨天',
    value: () => {
      const yesterday = new Date();
      yesterday.setDate(yesterday.getDate() - 1);
      yesterday.setHours(0, 0, 0, 0);
      return yesterday;
    }
  },
  {
    text: '一周前',
    value: () => {
      const weekAgo = new Date();
      weekAgo.setDate(weekAgo.getDate() - 7);
      weekAgo.setHours(0, 0, 0, 0);
      return weekAgo;
    }
  }
];

// 表单验证规则
const rules = {
  category: [{ required: true, message: '请选择成果类别', trigger: 'change' }],
  level: [{ required: true, message: '请选择级别', trigger: 'change' }],
  achName: [{ required: true, message: '请输入建设成果名称', trigger: 'blur' }],
  department: [{ required: true, message: '请输入组织部门', trigger: 'blur' }],
  gotTime: [{ required: true, message: '请选择颁发时间', trigger: 'change' }],
  deptId: [{ required: true, message: '请输入部门ID', trigger: 'blur' }],
  comment: [{ required: true, message: '请输入具体内容', trigger: 'blur' }]
};

// 上传相关 - 已改为使用自定义上传函数

const getAchievementId = (row) => {
  return row?.achId ?? row?.ach_id ?? row?._raw?.achId ?? row?._raw?.ach_id ?? null;
};

const getAchievementCreatorId = (row) => {
  return row?._raw?.createBy ?? row?._raw?.create_by ?? row?.createBy ?? row?.create_by ?? null;
};

const currentAuth = computed(() => getCurrentAuthInfo());

const canUploadAchievement = computed(() => {
  if (!canUsePermission('achievements.fill')) return false;
  const auth = currentAuth.value;
  if (!auth) return false;
  return isAdminTestEnabled() || auth.canUploadAchievement === true;
});

const canManageAllAchievements = computed(() => {
  if (!canUsePermission('achievements.audit')) return false;
  const auth = currentAuth.value;
  if (!auth) return false;
  return isAdminTestEnabled() || auth.canAuditAchievement === true;
});

const isPrimaryAchievementAdmin = computed(() => isAdminTestEnabled() || Number(currentAuth.value?.userId) === 110228);
const achievementDialogTitle = computed(() => isEditingAchievement.value ? '修改成果' : '新增成果');
const achievementSubmitText = computed(() => isEditingAchievement.value ? '保存修改' : '提交审核');

const canModifyAchievement = (row) => {
  if (!canUsePermission('achievements.fill')) return false;
  if (!row || row.isCategory) return false;
  const auth = currentAuth.value;
  if (!auth) return false;
  if (isAdminTestEnabled() || Number(auth.userId) === 110228) return true;
  if (!auth.canUploadAchievement) return false;

  const createBy = getAchievementCreatorId(row);
  if (createBy === null || createBy === undefined) return false;
  const deptId = row.deptId ?? row.dept_id ?? row._raw?.deptId ?? row._raw?.dept_id ?? null;
  return Number(createBy) === Number(auth.userId) && Number(deptId) === Number(auth.deptId);
};

const resubmittingId=ref(null);
const canResubmitAchievement=row=>canModifyAchievement(row)&&Number(row?.auditStatus??row?.audit_status??row?._raw?.auditStatus)<0;
const canEditAchievement=row=>canModifyAchievement(row)&&Number(row?.auditStatus??row?.audit_status??row?._raw?.auditStatus)!==10;
const handleResubmit=async row=>{
 if(resubmittingId.value!=null||submitting.value||!canResubmitAchievement(row))return;
 if(dialogVisible.value){ElMessage.warning('请先保存或取消当前修改，再重新提交成果');return;}
 const id=row.achId??row._raw?.achId??row.id,token=localStorage.getItem('token');
 resubmittingId.value=id;
 try{
  await ElMessageBox.confirm('将已保存的成果资料重新提交归档审核，原驳回记录仍会保留。若尚未保存修改，请先取消并保存。','重新提交成果',{confirmButtonText:'确认提交',cancelButtonText:'取消',type:'warning'});
  await resubmitAchievement(id);
  if(achievementDisposed||token!==localStorage.getItem('token'))return;
  detailDrawerVisible.value=false;await loadAchievements();emit('audit-completed');ElMessage.success('成果已重新提交审核');
 }catch(error){if(error!=='cancel'&&error!=='close')ElMessage.error(error.message||'重新提交失败，请刷新后重试')}
 finally{resubmittingId.value=null}
};

const getAchievementCurrentHandlerId = (row) => {
  return row?.currentHandlerId ?? row?.current_handler_id ?? row?._raw?.currentHandlerId ?? row?._raw?.current_handler_id ?? null;
};

const canAuditAchievement = (row) => {
  if (!row || row.isCategory || !canManageAllAchievements.value) return false;
  return Number(row.auditStatus ?? row.audit_status ?? row._raw?.auditStatus ?? row._raw?.audit_status ?? 30) === 10;
};

const latestAchievementAudit = computed(() => {
  return achievementAuditList.value.length ? achievementAuditList.value[0] : null;
});

const latestAchievementSubId = computed(() => {
  const item = latestAchievementAudit.value;
  return item?.subId ?? item?.sub_id ?? null;
});

const canAuditCurrentAchievement = computed(() => {
  if (achievementAuditLoading.value || !detailDrawerVisible.value || !isAuditMode.value || !canManageAllAchievements.value) return false;
  const item = latestAchievementAudit.value;
  if (!item || Number(item.achId ?? item.ach_id) !== Number(getAchievementId(currentAchievement.value))) return false;
  return Number(item.flowStatus ?? item.flow_status ?? 30) === 10;
});

const achievementAuditStepActive = computed(() => {
  const status = Number(latestAchievementAudit.value?.flowStatus ?? latestAchievementAudit.value?.flow_status ?? currentAchievement.value?.auditStatus ?? currentAchievement.value?.audit_status ?? 0);
  if (status === 30) return 2;
  if (status === 10) return 1;
  if (status < 0) return 1;
  return 0;
});

const formatDateTime = (dateStr) => {
  if (!dateStr) return '';
  try {
    const date = new Date(dateStr);
    if (Number.isNaN(date.getTime())) return String(dateStr);
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    const hh = String(date.getHours()).padStart(2, '0');
    const mm = String(date.getMinutes()).padStart(2, '0');
    return `${y}-${m}-${d} ${hh}:${mm}`;
  } catch {
    return String(dateStr);
  }
};

const getUserName = (userId) => {
  if (!userId) return '—';
  const user = allUsers.value.find(item => Number(item.userId ?? item.user_id) === Number(userId));
  return user?.nickName ?? user?.nick_name ?? user?.userName ?? user?.user_name ?? String(userId);
};

const achievementSubmitStepText = computed(() => {
  const item = latestAchievementAudit.value;
  const achievementStatus = Number(currentAchievement.value?.auditStatus ?? currentAchievement.value?.audit_status ?? currentAchievement.value?._raw?.auditStatus ?? currentAchievement.value?._raw?.audit_status);
  if (!item && achievementStatus === 30) return '已提交';
  if (!item) return '尚未提交';
  const submitBy = item.submitBy ?? item.submit_by;
  const submitTime = item.submitTime ?? item.submit_time;
  return `${getUserName(submitBy)} ${formatDateTime(submitTime)}`;
});

const achievementAdminStepText = computed(() => {
  const item = latestAchievementAudit.value;
  const achievementStatus = Number(currentAchievement.value?.auditStatus ?? currentAchievement.value?.audit_status ?? currentAchievement.value?._raw?.auditStatus ?? currentAchievement.value?._raw?.audit_status);
  if (!item && achievementStatus === 30) return '已归档';
  if (!item) return '待提交';
  const status = Number(item.flowStatus ?? item.flow_status ?? 0);
  const handlerId = item.currentHandlerId ?? item.current_handler_id ?? getAchievementCurrentHandlerId(currentAchievement.value) ?? 110228;
  if (status === 30) return `审核人：${getUserName(handlerId)}（已归档）`;
  if (status === 10) return `审核人：${getUserName(handlerId)}（待审核）`;
  if (status < 0) return `审核人：${getUserName(handlerId)}（已退回）`;
  return `审核人：${getUserName(handlerId)}`;
});

const getAchievementActionText = (actionType) => {
  const action = String(actionType || '').toLowerCase();
  if (action === 'submit') return '提交归档';
  if (action === 'pass') return '审核通过并归档';
  if (action === 'reject') return '退回';
  return actionType || '审核动作';
};

const getAchievementLogCommentLabel = (actionType) => {
  const action = String(actionType || '').toLowerCase();
  if (action === 'submit') return '具体内容';
  if (action === 'pass' || action === 'reject') return '审核意见';
  return '备注';
};

const getAchievementAuditStatusText = (status) => {
  const s = Number(status ?? 30);
  if (s === 10) return '待归档审核';
  if (s === 30) return '已归档';
  if (s < 0) return '已驳回';
  return '待处理';
};

const getAchievementAuditStatusTag = (status) => {
  const s = Number(status ?? 30);
  if (s === 10) return 'warning';
  if (s === 30) return 'success';
  if (s < 0) return 'danger';
  return 'info';
};

// 获取成果类别名称
const getCategoryName = (categoryValue) => {
  if (!categoryValue) return '';
  const index = Number(categoryValue) - 1;
  if (index >= 0 && index < achievementCategories.length) {
    return achievementCategories[index];
  }
  return categoryValue;
};

// 获取级别名称（兼容不同格式）
const getLevelName = (levelValue) => {
  if (!levelValue) return '';
  const level = String(levelValue).trim();
  // 如果已经是中文格式，直接返回
  if (level === '国家级' || level === '省级' || level === '市级') {
    return level;
  }
  // 如果是英文格式，转换为中文
  if (level === 'national' || level.toLowerCase() === 'national') return '国家级';
  if (level === 'provincial' || level.toLowerCase() === 'provincial') return '省级';
  if (level === 'municipal' || level.toLowerCase() === 'municipal') return '市级';
  // 如果包含关键词，返回对应级别
  if (level.includes('国家') || level.toLowerCase().includes('national')) return '国家级';
  if (level.includes('省') || level.toLowerCase().includes('provincial')) return '省级';
  if (level.includes('市') || level.toLowerCase().includes('municipal')) return '市级';
  return level;
};

// 获取级别标签类型
const getLevelTag = (levelValue) => {
  if (!levelValue) return 'info';
  const level = String(levelValue).trim();
  if (level === '国家级' || level.includes('国家') || level.toLowerCase() === 'national') return 'danger';
  if (level === '省级' || level.includes('省') || level.toLowerCase() === 'provincial') return 'warning';
  if (level === '市级' || level.includes('市') || level.toLowerCase() === 'municipal') return 'success';
  return 'info';
};

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '';
  try {
    const date = new Date(dateStr);
    if (isNaN(date.getTime())) return dateStr;
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  } catch {
    return dateStr;
  }
};

// 获取成果等次及数量文本
const getGradeAndCountText = (row) => {
  // 如果是类别节点，返回空
  if (row.isCategory) return '';

  // 优先从原始数据获取
  const raw = row._raw ?? row;
  const parts = [];
  const te = raw.teDengJiang ?? raw.te_deng_jiang ?? 0;
  const yi = raw.yiDengJiang ?? raw.yi_deng_jiang ?? 0;
  const er = raw.erDengJiang ?? raw.er_deng_jiang ?? 0;
  const san = raw.sanDengJiang ?? raw.san_deng_jiang ?? 0;
  const jin = raw.jinJiang ?? raw.jin_jiang ?? 0;
  const yin = raw.yinJiang ?? raw.yin_jiang ?? 0;
  const tong = raw.tongJiang ?? raw.tong_jiang ?? 0;
  const you = raw.youShengJiang ?? raw.you_sheng_jiang ?? 0;
  const bud = raw.budDengDengCi ?? raw.bud_deng_deng_ci ?? 0;

  if (te > 0) parts.push(`特等奖×${te}`);
  if (yi > 0) parts.push(`一等奖×${yi}`);
  if (er > 0) parts.push(`二等奖×${er}`);
  if (san > 0) parts.push(`三等奖×${san}`);
  if (jin > 0) parts.push(`金奖×${jin}`);
  if (yin > 0) parts.push(`银奖×${yin}`);
  if (tong > 0) parts.push(`铜奖×${tong}`);
  if (you > 0) parts.push(`优胜奖×${you}`);
  if (bud > 0) parts.push(`不定等次×${bud}`);

  return parts.length > 0 ? parts.join('，') : '';
};

// 处理佐证材料操作（预览或下载）
const handleMaterialAction = async (command, row) => {
  // 如果是类别节点，不处理
  if (row.isCategory) return;

  const fileId = row.fileId ?? row.file_id ?? row._raw?.fileId ?? row._raw?.file_id;
  if (!fileId) {
    ElMessage.warning('未找到佐证材料');
    return;
  }

  if (command === 'preview') {
    await previewMaterialFile(fileId);
  } else if (command === 'download') {
    await downloadMaterialFile(fileId);
  }
};

// 预览佐证材料
const previewMaterialFile = async (fileId) => {
  const token = localStorage.getItem('token') || '';
  if (!token) {
    ElMessage.error('未登录或登录已过期，请先登录');
    return;
  }

  // 清理之前的预览URL
  cleanupPreviewUrl();

  previewVisible.value = true;
  previewType.value = '';
  previewUrl.value = '';
  previewFileName.value = '';

  try {
    const resp = await adminTestFetch(`${API_BASE_URL}/system/download/${fileId}`, {
      method: 'GET',
      headers: { Authorization: token }
    });

    if (resp.status === 401) {
      try { localStorage.removeItem('token'); } catch { /* ignore */ }
      try {
        const full = `${window.location.pathname || ''}${window.location.search || ''}${window.location.hash || ''}`;
        window.location.replace(`/login?redirect=${encodeURIComponent(full)}`);
      } catch { /* ignore */ }
      return;
    }

    // 检查响应状态
    if (!resp.ok) {
      const contentType = resp.headers.get('content-type') || '';
      let err;
      if (contentType.includes('application/json')) {
        try { err = await resp.json(); } catch { /* ignore */ }
      } else {
        try { err = { message: await resp.text() }; } catch { /* ignore */ }
      }
      throw new Error(err?.message || `文件获取失败(${resp.status})`);
    }

    // 响应成功，获取文件流
    const cd = resp.headers.get('content-disposition') || '';
    const filename = parseFilenameFromContentDisposition(cd);
    previewFileName.value = filename || `file_${fileId}`;

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

// 下载佐证材料
const downloadMaterialFile = async (fileId) => {
  const token = localStorage.getItem('token') || '';
  if (!token) {
    ElMessage.error('未登录或登录已过期，请先登录');
    return;
  }

  try {
    const resp = await adminTestFetch(`${API_BASE_URL}/system/download/${fileId}`, {
      method: 'GET',
      headers: { Authorization: token }
    });

    if (resp.status === 401) {
      try { localStorage.removeItem('token'); } catch { /* ignore */ }
      try {
        const full = `${window.location.pathname || ''}${window.location.search || ''}${window.location.hash || ''}`;
        window.location.replace(`/login?redirect=${encodeURIComponent(full)}`);
      } catch { /* ignore */ }
      return;
    }

    if (!resp.ok) {
      const contentType = resp.headers.get('content-type') || '';
      let err;
      if (contentType.includes('application/json')) {
        try { err = await resp.json(); } catch { /* ignore */ }
      } else {
        try { err = { message: await resp.text() }; } catch { /* ignore */ }
      }
      throw new Error(err?.message || `文件获取失败(${resp.status})`);
    }

    // 获取文件名
    const cd = resp.headers.get('content-disposition') || '';
    let filename = parseFilenameFromContentDisposition(cd);
    if (!filename || filename.startsWith('file_')) {
      filename = `file_${fileId}`;
    }

    const blob = await resp.blob();
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;

    // 确保文件名正确解码
    let downloadName = filename;
    if (downloadName.includes('%')) {
      try {
        downloadName = decodeURIComponent(downloadName);
      } catch {
        // 解码失败，使用原始值
      }
    }
    a.download = downloadName;
    document.body.appendChild(a);
    a.click();
    a.remove();

    setTimeout(() => {
      try { URL.revokeObjectURL(url); } catch { /* ignore */ }
    }, 500);

    ElMessage.success('文件下载成功');
  } catch (e) {
    ElMessage.error(e?.message || '下载失败，请稍后重试');
  }
};

// 清理预览URL和Blob
const cleanupPreviewUrl = () => {
  if (previewUrl.value) {
    try { URL.revokeObjectURL(previewUrl.value); } catch { /* ignore */ }
    previewUrl.value = '';
  }
  if (previewFileBlob.value) {
    previewFileBlob.value = null;
  }
  previewFileName.value = '';
  previewType.value = '';
};

// 判断是否为PDF
const isPdfBlob = async (blob) => {
  try {
    const buf = await blob.slice(0, 5).arrayBuffer();
    const bytes = new Uint8Array(buf);
    // %PDF-
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

// 从 Content-Disposition 头中解析文件名
const parseFilenameFromContentDisposition = (contentDisposition) => {
  if (!contentDisposition) return '';
  // 尝试匹配 filename="..." 或 filename*=UTF-8''...
  const filenameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
  if (filenameMatch && filenameMatch[1]) {
    let filename = filenameMatch[1].replace(/['"]/g, '');
    // 处理 UTF-8 编码的文件名
    if (filename.startsWith("UTF-8''")) {
      filename = decodeURIComponent(filename.substring(7));
    } else if (filename.includes('%')) {
      try {
        filename = decodeURIComponent(filename);
      } catch {
        // 解码失败，使用原始值
      }
    }
    return filename;
  }
  return '';
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
      } catch {
        // 解码失败，使用原始值
      }
    }
    a.download = downloadName;
    document.body.appendChild(a);
    a.click();
    a.remove();
    setTimeout(() => {
      try { URL.revokeObjectURL(url); } catch { /* ignore */ }
    }, 500);
  } catch {
    ElMessage.error('下载失败，请稍后重试');
  }
};

// 获取当前用户的部门ID
const getCurrentUserDeptId = () => {
  const token = localStorage.getItem('token') || '';
  if (!token) return null;

  try {
    const parts = token.split('.');
    if (parts.length < 2) return null;
    let payload = parts[1];
    payload = payload.replace(/-/g, '+').replace(/_/g, '/');
    const pad = payload.length % 4;
    if (pad) payload += '='.repeat(4 - pad);
    const json = atob(payload);
    const decoded = JSON.parse(json);
    const deptId = decoded.deptId !== undefined && decoded.deptId !== null ? Number(decoded.deptId) : null;
    if (deptId && !Number.isNaN(deptId)) return deptId;
    const userId = decoded.id ? Number(decoded.id) : null;

    if (!userId) return null;

    // 从用户列表中查找当前用户的部门ID
    const currentUser = allUsers.value.find(u => Number(u.userId) === userId);
    return currentUser?.deptId ?? currentUser?.dept_id ?? null;
  } catch {
    return null;
  }
};

// 加载部门列表（从用户列表中提取部门ID，然后通过API获取部门名称）
const loadDeptList = async () => {
  try {
    const users = await getAllUsers();
    allUsers.value = Array.isArray(users) ? users : [];

    // 从用户列表中提取所有部门ID，去重
    const deptIdSet = new Set();
    allUsers.value.forEach(user => {
      const deptId = user.deptId ?? user.dept_id;
      if (deptId) {
        deptIdSet.add(Number(deptId));
      }
    });

    // 通过API获取每个部门的详细信息
    const deptPromises = Array.from(deptIdSet).map(async (deptId) => {
      try {
        const dept = await getDeptById(deptId);
        return {
          deptId: Number(deptId),
          deptName: dept?.deptName ?? dept?.dept_name ?? `部门${deptId}`
        };
      } catch {
        // 如果获取失败，使用默认名称
        return {
          deptId: Number(deptId),
          deptName: `部门${deptId}`
        };
      }
    });

    const depts = await Promise.all(deptPromises);

    // 转换为数组并排序
    deptList.value = depts.sort((a, b) => a.deptId - b.deptId);

    // 设置默认部门为当前用户的部门
    const currentDeptId = getCurrentUserDeptId();
    if (currentDeptId && deptIdSet.has(currentDeptId)) {
      form.deptId = currentDeptId;
    } else if (deptList.value.length > 0) {
      form.deptId = deptList.value[0].deptId;
    }
  } catch (e) {
    console.error('加载部门列表失败:', e);
    const currentDeptId = getCurrentUserDeptId();
    if (currentDeptId) {
      try {
        const dept = await getDeptById(currentDeptId);
        deptList.value = [{
          deptId: Number(currentDeptId),
          deptName: dept?.deptName ?? dept?.dept_name ?? `部门${currentDeptId}`
        }];
      } catch {
        deptList.value = [{ deptId: Number(currentDeptId), deptName: `部门${currentDeptId}` }];
      }
      form.deptId = Number(currentDeptId);
      return;
    }
    ElMessage.error('加载部门列表失败');
  }
};

// 添加等次选择
const addGradeSelection = () => {
  gradeSelections.value.push({
    grade: '',
    count: 1
  });
};

// 移除等次选择
const removeGradeSelection = (index) => {
  gradeSelections.value.splice(index, 1);
};

const resetAchievementForm = (defaults = {}) => {
  Object.assign(form, {
    category: null,
    level: '',
    achName: '',
    department: '',
    gotTime: '',
    deptId: getCurrentUserDeptId() || (deptList.value.length > 0 ? deptList.value[0].deptId : null),
    fileId: null,
    comment: '',
    isCompetition: 0,
    teDengJiang: 0,
    yiDengJiang: 0,
    erDengJiang: 0,
    sanDengJiang: 0,
    jinJiang: 0,
    yinJiang: 0,
    tongJiang: 0,
    youShengJiang: 0,
    budDengDengCi: 0,
    ...defaults
  });
  uploadFileList.value = [];
  if (formRef.value) {
    formRef.value.clearValidate();
  }
};

const buildGradeSelectionsFromAchievement = (row) => {
  const gradeFields = [
    ['特等奖', row.teDengJiang ?? row.te_deng_jiang ?? row._raw?.teDengJiang ?? row._raw?.te_deng_jiang],
    ['一等奖', row.yiDengJiang ?? row.yi_deng_jiang ?? row._raw?.yiDengJiang ?? row._raw?.yi_deng_jiang],
    ['二等奖', row.erDengJiang ?? row.er_deng_jiang ?? row._raw?.erDengJiang ?? row._raw?.er_deng_jiang],
    ['三等奖', row.sanDengJiang ?? row.san_deng_jiang ?? row._raw?.sanDengJiang ?? row._raw?.san_deng_jiang],
    ['金奖', row.jinJiang ?? row.jin_jiang ?? row._raw?.jinJiang ?? row._raw?.jin_jiang],
    ['银奖', row.yinJiang ?? row.yin_jiang ?? row._raw?.yinJiang ?? row._raw?.yin_jiang],
    ['铜奖', row.tongJiang ?? row.tong_jiang ?? row._raw?.tongJiang ?? row._raw?.tong_jiang],
    ['优胜奖', row.youShengJiang ?? row.you_sheng_jiang ?? row._raw?.youShengJiang ?? row._raw?.you_sheng_jiang],
    ['不定等次', row.budDengDengCi ?? row.bud_deng_deng_ci ?? row._raw?.budDengDengCi ?? row._raw?.bud_deng_deng_ci]
  ];
  return gradeFields
    .map(([grade, count]) => ({ grade, count: Number(count || 0) }))
    .filter(item => item.count > 0);
};

// 打开新增对话框
const handleAdd = () => {
  achievementFormVersion++;
  if (!canUploadAchievement.value) {
    ElMessage.warning('当前账号无权新增成果');
    return;
  }
  isEditingAchievement.value = false;
  editingAchievementId.value = null;
  isAuditMode.value = false;
  auditingAchievementId.value = null;
  auditingCreateBy.value = null;
  dialogVisible.value = true;
  gradeSelections.value = [];
  resetAchievementForm();
};

const handleEdit = (row) => {
  if (!canEditAchievement(row)) {
    ElMessage.warning('当前账号无权修改该成果');
    return;
  }
  const achId = getAchievementId(row);
  if (!achId) {
    ElMessage.error('无法获取成果ID');
    return;
  }
  isEditingAchievement.value = true;
  editingAchievementId.value = achId;
  isAuditMode.value = false;
  auditingAchievementId.value = null;
  auditingCreateBy.value = null;
  currentAchievement.value = row;
  gradeSelections.value = buildGradeSelectionsFromAchievement(row);
  fillFormFromAchievement(row);
  dialogVisible.value = true;
};

const fillFormFromAchievement = (row) => {
  resetAchievementForm({
    category: row.category ?? row._raw?.category ?? null,
    level: row.level ?? row._raw?.level ?? '',
    achName: row.achName ?? row.ach_name ?? row.name ?? row._raw?.achName ?? row._raw?.ach_name ?? '',
    department: row.department ?? row._raw?.department ?? '',
    gotTime: row.gotTime ?? row.got_time ?? row._raw?.gotTime ?? row._raw?.got_time ?? '',
    deptId: row.deptId ?? row.dept_id ?? row._raw?.deptId ?? row._raw?.dept_id ?? null,
    fileId: row.fileId ?? row.file_id ?? row._raw?.fileId ?? row._raw?.file_id ?? null,
    comment: row.comment ?? row._raw?.comment ?? '',
    isCompetition: Number(row.isCompetition ?? row.is_competition ?? row._raw?.isCompetition ?? row._raw?.is_competition ?? 0),
    teDengJiang: row.teDengJiang ?? row.te_deng_jiang ?? row._raw?.teDengJiang ?? row._raw?.te_deng_jiang ?? 0,
    yiDengJiang: row.yiDengJiang ?? row.yi_deng_jiang ?? row._raw?.yiDengJiang ?? row._raw?.yi_deng_jiang ?? 0,
    erDengJiang: row.erDengJiang ?? row.er_deng_jiang ?? row._raw?.erDengJiang ?? row._raw?.er_deng_jiang ?? 0,
    sanDengJiang: row.sanDengJiang ?? row.san_deng_jiang ?? row._raw?.sanDengJiang ?? row._raw?.san_deng_jiang ?? 0,
    jinJiang: row.jinJiang ?? row.jin_jiang ?? row._raw?.jinJiang ?? row._raw?.jin_jiang ?? 0,
    yinJiang: row.yinJiang ?? row.yin_jiang ?? row._raw?.yinJiang ?? row._raw?.yin_jiang ?? 0,
    tongJiang: row.tongJiang ?? row.tong_jiang ?? row._raw?.tongJiang ?? row._raw?.tong_jiang ?? 0,
    youShengJiang: row.youShengJiang ?? row.you_sheng_jiang ?? row._raw?.youShengJiang ?? row._raw?.you_sheng_jiang ?? 0,
    budDengDengCi: row.budDengDengCi ?? row.bud_deng_deng_ci ?? row._raw?.budDengDengCi ?? row._raw?.bud_deng_deng_ci ?? 0
  });
};

const handleAudit = async (row) => {
  if (!canAuditAchievement(row)) {
    ElMessage.warning('当前账号无权审核该成果');
    return;
  }

  const achId = getAchievementId(row);
  if (!achId) {
    ElMessage.error('无法获取成果ID');
    return;
  }

  achievementDetailVersion++;
  currentAchievement.value = row;
  isAuditMode.value = true;
  auditingAchievementId.value = achId;
  auditingCreateBy.value = getAchievementCreatorId(row);
  achievementAuditForm.result = 1;
  achievementAuditForm.comment = '同意';
  gradeSelections.value = buildGradeSelectionsFromAchievement(row);
  fillFormFromAchievement(row);
  detailDrawerVisible.value = true;
  await loadAchievementAuditInfo(achId);
};

// 文件选择变化
const handleFileChange = (file, fileList) => {
  // 验证文件类型和大小
  const fileName = file.name || '';
  const fileExt = fileName.toLowerCase().split('.').pop();
  const allowedTypes = ['pdf', 'doc', 'docx'];

  if (!allowedTypes.includes(fileExt)) {
    ElMessage.error('只能上传 PDF、DOC 或 DOCX 格式的文件！');
    uploadFileList.value = [];
    return;
  }

  const isLt10M = file.size / 1024 / 1024 < 10;
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB！');
    uploadFileList.value = [];
    return;
  }

  // 只保留最后一个文件
  uploadFileList.value = fileList.slice(-1);
};

// 文件移除
const handleFileRemove = () => {
  uploadFileList.value = [];
  form.fileId = null;
  return true;
};

// 将等次选择列表转换为表单字段
const convertGradeSelectionsToForm = () => {
  // 重置所有等次字段
  form.teDengJiang = 0;
  form.yiDengJiang = 0;
  form.erDengJiang = 0;
  form.sanDengJiang = 0;
  form.jinJiang = 0;
  form.yinJiang = 0;
  form.tongJiang = 0;
  form.youShengJiang = 0;
  form.budDengDengCi = 0;

  // 将等次选择转换为对应字段（累加相同等次的数量）
  gradeSelections.value.forEach(item => {
    if (!item.grade || !item.count) return;
    const count = Number(item.count) || 0;
    switch (item.grade) {
      case '特等奖':
        form.teDengJiang = (form.teDengJiang || 0) + count;
        break;
      case '一等奖':
        form.yiDengJiang = (form.yiDengJiang || 0) + count;
        break;
      case '二等奖':
        form.erDengJiang = (form.erDengJiang || 0) + count;
        break;
      case '三等奖':
        form.sanDengJiang = (form.sanDengJiang || 0) + count;
        break;
      case '金奖':
        form.jinJiang = (form.jinJiang || 0) + count;
        break;
      case '银奖':
        form.yinJiang = (form.yinJiang || 0) + count;
        break;
      case '铜奖':
        form.tongJiang = (form.tongJiang || 0) + count;
        break;
      case '优胜奖':
        form.youShengJiang = (form.youShengJiang || 0) + count;
        break;
      case '不定等次':
        form.budDengDengCi = (form.budDengDengCi || 0) + count;
        break;
    }
  });
};

const prepareAchievementPayload = async (createBy) => {
  const invalidQuantity = gradeSelections.value.some(item => {
    const count = Number(item.count);
    return !Number.isInteger(count) || count < 1 || count > 999;
  });
  if (invalidQuantity) {
    ElMessage.warning('成果数量必须为 1～999 的整数，请修改后再提交');
    return null;
  }
  const effectiveGradeSelections = gradeSelections.value.filter(item => item.grade || item.count);
  const incomplete = effectiveGradeSelections.find(item => !item.grade || !item.count);
  if (incomplete) {
    ElMessage.warning('请完成所有等次选择，确保等次和数量都已填写');
    return null;
  }
  gradeSelections.value = effectiveGradeSelections;
  convertGradeSelectionsToForm();

  const payload = {
    category: form.category,
    level: form.level,
    achName: form.achName,
    department: form.department,
    gotTime: form.gotTime,
    deptId: form.deptId,
    fileId: form.fileId,
    comment: form.comment,
    isCompetition: form.isCompetition,
    teDengJiang: form.teDengJiang || 0,
    yiDengJiang: form.yiDengJiang || 0,
    erDengJiang: form.erDengJiang || 0,
    sanDengJiang: form.sanDengJiang || 0,
    jinJiang: form.jinJiang || 0,
    yinJiang: form.yinJiang || 0,
    tongJiang: form.tongJiang || 0,
    youShengJiang: form.youShengJiang || 0,
    budDengDengCi: form.budDengDengCi || 0,
    createBy
  };
  if (uploadFileList.value.length > 0) {
    const file = uploadFileList.value[0].raw;
    if (file) {
      ElMessage.info('正在上传文件...');
      const uploadResponse = await upload('/achievement/upload', file);
      if (uploadResponse && uploadResponse.fileId) {
        payload.fileId = uploadResponse.fileId;
        ElMessage.success('文件上传成功');
      } else {
        ElMessage.error('文件上传失败：未获取到文件ID');
        return null;
      }
    }
  }

  return payload;
};

// 提交新增
const handleSubmit = async () => {
  if (!formRef.value || submitting.value || !dialogVisible.value) return;
  const version = achievementFormVersion;
  const auth = getCurrentAuthInfo();
  const currentUserId = auth?.userId;
  const canSubmit = canUploadAchievement.value || (isEditingAchievement.value && isPrimaryAchievementAdmin.value);

  if (!canSubmit) {
    ElMessage.warning(isEditingAchievement.value ? '当前账号无权修改成果' : '当前账号无权新增成果');
    return;
  }

  submitting.value = true;
  try {
    await formRef.value.validate();
    if (!isCurrentAchievementForm(version)) return;

    if (!currentUserId) {
      ElMessage.error('无法获取当前用户信息，请重新登录');
      submitting.value = false;
      return;
    }

    const revisingArchive = isEditingAchievement.value && Number(currentAchievement.value?.auditStatus ?? currentAchievement.value?.audit_status ?? currentAchievement.value?._raw?.auditStatus) === 30;
    if (revisingArchive) {
      await ElMessageBox.confirm('已归档成果修改后将重新提交审核，原归档记录保留。是否继续？', '修改已归档成果',
        { confirmButtonText: '修改并送审', cancelButtonText: '取消', type: 'warning' });
    }

    const achievementData = await prepareAchievementPayload(currentUserId);
    if (!achievementData) return;

    if (isEditingAchievement.value) {
      if (!editingAchievementId.value) {
        ElMessage.error('无法获取成果ID');
        return;
      }
      await updateAchievement(editingAchievementId.value, achievementData);
      if (!isCurrentAchievementForm(version)) return;
      ElMessage.success(revisingArchive ? '成果修改已送审，原归档记录保留' : canResubmitAchievement(currentAchievement.value) ? '成果修改成功，请点击“重新提交”送审' : '成果修改成功');
    } else {
      await addAchievement(achievementData);
      if (!isCurrentAchievementForm(version)) return;
      ElMessage.success('成果已提交，待管理员归档审核');
    }

    await loadAchievements();
    if (!isCurrentAchievementForm(version)) return;

    resetAchievementForm();
    gradeSelections.value = [];
    isEditingAchievement.value = false;
    editingAchievementId.value = null;
    dialogVisible.value = false;
  } catch (e) {
    if (isCurrentAchievementForm(version) && e?.message && !e.message.includes('validation')) {
      ElMessage.error(e.message || '保存失败，请稍后重试');
    }
  } finally {
    submitting.value = false;
  }
};

const handleAuditSubmit = async () => {
  if (submitting.value || achievementAuditLoading.value) return;
  const token = localStorage.getItem('token');
  const context = achievementContext();
  if (!isCurrentAchievement(context)) return;
  const subId = latestAchievementSubId.value;
  const result = achievementAuditForm.result;
  const comment = achievementAuditForm.comment;
  if (!canAuditCurrentAchievement.value) {
    ElMessage.warning('当前账号无权审核该成果');
    return;
  }
  if (!latestAchievementSubId.value) {
    ElMessage.error('无法获取成果审核单');
    return;
  }
  if (!achievementAuditForm.comment || !achievementAuditForm.comment.trim()) {
    ElMessage.warning('请输入审核意见');
    return;
  }

  try {
    submitting.value = true;
    await auditAchievement({
      sub_id: subId,
      is_pass: result === 1,
      title: comment,
      content: comment
    });
    if (!achievementDisposed && token === localStorage.getItem('token')) emit('audit-completed');
    if (!isCurrentAchievement(context)) return;
    ElMessage.success(result === 1 ? '成果已归档' : '成果已退回');
    await loadAchievements();
    if (!isCurrentAchievement(context)) return;
    await loadAchievementAuditInfo(context.achId, context);
    if (!isCurrentAchievement(context)) return;
    detailDrawerVisible.value = false;
  } catch (e) {
    if (isCurrentAchievement(context) && e?.message && !e.message.includes('validation')) {
      ElMessage.error(e.message || '审核失败，请稍后重试');
    }
  } finally {
    submitting.value = false;
  }
};

const cancelAudit = () => {
  detailDrawerVisible.value = false;
};

const handleDetailDrawerClosed = () => {
  if (detailDrawerVisible.value || dialogVisible.value) return;
  isAuditMode.value = false;
  auditingAchievementId.value = null;
  auditingCreateBy.value = null;
  achievementAuditList.value = [];
  achievementAuditLogs.value = [];
  gradeSelections.value = [];
  uploadFileList.value = [];
};

// 今天的日期（格式化）
const todayDate = computed(() => {
  const today = new Date();
  const year = today.getFullYear();
  const month = today.getMonth() + 1;
  const day = today.getDate();
  return `${year}年${month}月${day}日`;
});

// 构建树形数据结构（按类别分组）
const buildAchievementTree = (achievements) => {
  if (!achievements || achievements.length === 0) return [];

  // 按类别分组
  const categoryMap = new Map();

  achievements.forEach(achievement => {
    const category = achievement.category ?? achievement.category;
    const categoryNum = Number(category);

    if (!categoryMap.has(categoryNum)) {
      categoryMap.set(categoryNum, {
        id: `category-${categoryNum}`,
        name: getCategoryName(categoryNum),
        isCategory: true,
        category: categoryNum,
        children: [],
        hasChildren: true
      });
    }

    const categoryNode = categoryMap.get(categoryNum);
    // 构建成果节点，确保所有字段都被正确传递
    const achievementNode = {
      id: `achievement-${achievement.achId ?? achievement.ach_id}`,
      name: achievement.achName ?? achievement.ach_name ?? '',
      isCategory: false,
      // 保留原始数据的所有字段（包括 createBy）
      ...achievement,
      // 同时保存原始对象引用
      _raw: achievement
    };

    // 确保 createBy 字段被正确传递（处理可能的字段名差异）
    // 显式设置 createBy 字段，确保权限判断能正确获取
    if (achievement.createBy !== undefined && achievement.createBy !== null) {
      achievementNode.createBy = achievement.createBy;
    }
    if (achievement.create_by !== undefined && achievement.create_by !== null) {
      achievementNode.create_by = achievement.create_by;
    }

    // 调试：检查 createBy 字段
    if (!achievementNode.createBy && !achievementNode.create_by) {
      console.warn('[构建树形数据] 成果缺少 createBy 字段:', {
        achId: achievement.achId ?? achievement.ach_id,
        achName: achievement.achName ?? achievement.ach_name,
        achievementKeys: Object.keys(achievement),
        achievement: achievement
      });
    }

    categoryNode.children.push(achievementNode);
  });

  // 转换为数组并按类别编号排序
  const tree = Array.from(categoryMap.values()).sort((a, b) => {
    return (a.category || 0) - (b.category || 0);
  });

  // 对每个类别下的成果进行排序（按颁发时间倒序）
  tree.forEach(categoryNode => {
    if (categoryNode.children && categoryNode.children.length > 0) {
      categoryNode.children.sort((a, b) => {
        const timeA = a.gotTime ?? a.got_time ?? '';
        const timeB = b.gotTime ?? b.got_time ?? '';
        if (timeA && timeB) {
          return new Date(timeB) - new Date(timeA);
        }
        return 0;
      });
    }
  });

  return tree;
};

// 应用筛选条件
const applyFilters = () => {
  let filtered = allAchievements.value.filter(item => queryIds.value.has(String(item.achId)));
  const { status, category, level, competition } = listFilters.value;
  if (status) filtered = filtered.filter(item => status === 'returned' ? Number(item.auditStatus ?? item.audit_status) < 0 : String(item.auditStatus ?? item.audit_status ?? 30) === status);
  if (category) filtered = filtered.filter(item => String(item.category) === category);
  if (level) filtered = filtered.filter(item => getLevelName(item.level) === level);
  if (competition !== '' && competition != null) filtered = filtered.filter(item => String(item.isCompetition ?? item.is_competition ?? 0) === competition);
  if (ownerFilter.value) filtered = filtered.filter(item => String(item.deptId ?? item.dept_id) === ownerFilter.value);
  if (leaderFilter.value) filtered = filtered.filter(item => String(item.createBy ?? item.create_by) === leaderFilter.value);
  if (quickFilter.value !== 'all') {
    const userId = getCurrentAuthInfo()?.userId;
    filtered = filtered.filter(item => quickFilter.value === 'mine' ? String(item.createBy ?? item.create_by) === String(userId)
      : quickFilter.value === 'returned' ? Number(item.auditStatus ?? item.audit_status) < 0
        : achievementTodoIds.value.some(id => String(id) === String(getAchievementId(item))));
  }

  if (filterYear.value) {
    filtered = filtered.filter(item => getAchievementYear(item) === String(filterYear.value));
  }

  // 按搜索关键词筛选
  if (actualSearchText.value) {
    const keyword = String(actualSearchText.value).toLowerCase().trim();
    filtered = filtered.filter(item => {
      const name = (item.achName ?? item.ach_name ?? '').toLowerCase();
      const dept = (item.department ?? '').toLowerCase();
      const categoryName = getCategoryName(item.category).toLowerCase();
      return name.includes(keyword) || dept.includes(keyword) || categoryName.includes(keyword);
    });
  }

  // 构建树形结构
  tableData.value = buildAchievementTree(filtered);
};

watch(quickFilter, () => { applyFilters(); achievementMemory.resetPosition(); });
watch([ownerFilter, leaderFilter, extraAchievementFilters], () => { applyFilters(); achievementMemory.resetPosition(); });

const getAchievementYear = (item) => {
  const rawTime = item?.gotTime ?? item?.got_time ?? item?._raw?.gotTime ?? item?._raw?.got_time ?? '';
  if (!rawTime) return '';
  const date = new Date(rawTime);
  if (!Number.isNaN(date.getTime())) return String(date.getFullYear());
  const match = String(rawTime).match(/^(\d{4})/);
  return match ? match[1] : '';
};



// 搜索处理




// 加载成果数据
let openedAchievementLink = '';
let achievementSavedDraft = '';
const achievementDetailItems = computed(() => detailLeaves(tableData.value, row => !row.isCategory));
const openAdjacentAchievement = async row => {
  previewVisible.value = false;
  await handleViewDetail(row);
  await nextTick();
  achievementContentRef.value?.closest('.el-drawer__body')?.scrollTo({ top: 0 });
};
const achievementDraft = () => JSON.stringify([achievementAuditForm, form, gradeSelections.value,
  uploadFileList.value.map(file => [file.uid, file.name])]);
const achievementDetailLink = computed(() => `${window.location.origin}${router.resolve({ path: '/home/works/achievement', query: {
  achId: String(getAchievementId(currentAchievement.value) || ''), year: getAchievementYear(currentAchievement.value)
} }).href}`);
const locateCurrentAchievement = async () => {
  const context = achievementContext();
  if (!await confirmDetailLeave(achievementDraft() !== achievementSavedDraft)) return;
  const valid = () => !achievementDisposed && detailDrawerVisible.value && context.version === achievementContext().version && context.achId === achievementContext().achId;
  if (!valid() || submitting.value) return;
  const id = getAchievementId(currentAchievement.value);
  await loadAchievements();
  if (!allAchievements.value.some(row => String(getAchievementId(row)) === String(id))) {await queryBar.value?.replace({});if(!allAchievements.value.some(row=>String(getAchievementId(row))===String(id))){ElMessage.info('记录已删除或无权访问，无法定位');return;}}
  await locateDetailRow({ tree: () => tableData.value, matches: row => !row.isCategory && String(getAchievementId(row)) === String(id),
    reset: async () => { listFilters.value = { owner: '', leader: '', status: '', category: '', level: '', competition: '' }; filterYear.value = ''; quickFilter.value = 'all'; ownerFilter.value = ''; leaderFilter.value = ''; searchText.value = ''; actualSearchText.value = ''; await queryBar.value?.replace({}); applyFilters(); },
    expanded: expandedRowKeys, table: achievementTableRef, close: () => { detailDrawerVisible.value = false; }, valid });
};
const openAchievementLink = async () => {
  const id = String(route.query.achId || '');
  const key = `${id}:${route.query.year || ''}`;
  if (!id || key === openedAchievementLink || achievementDisposed) return;
  openedAchievementLink = key;
  const raw = allAchievements.value.find(row => String(getAchievementId(row)) === id);
  if(!raw){
    try{const result=await queryGet('/list-filters/achievements',{responseMode:'tree',focusId:id,'f.year':route.query.year||''});
      const path=findDetailPath(buildAchievementTree(result.items||[]),row=>!row.isCategory&&String(getAchievementId(row))===id);
      if(path&&!achievementDisposed)await handleViewDetail(path.at(-1));else ElMessage.info('成果不存在、已删除或无权访问');
    }catch(error){if(!achievementDisposed)ElMessage.info(error.message||'成果不存在、已删除或无权访问');}return;
  }
  listFilters.value = { owner: '', leader: '', status: '', category: '', level: '', competition: '' };
  filterYear.value = getAchievementYear(raw); quickFilter.value = 'all'; ownerFilter.value = ''; leaderFilter.value = ''; searchText.value = ''; actualSearchText.value = ''; applyFilters();
  const path = findDetailPath(tableData.value, row => !row.isCategory && String(getAchievementId(row)) === id);
  if (path) await handleViewDetail(path.at(-1));
};
const loadAchievements = async () => {
  if(achievementDisposed)return;
  return queryBar.value?.refresh();
};
// 展开/折叠处理
const handleExpandChange = (row, expanded) => {
  const id = row?.id !== undefined && row?.id !== null ? String(row.id) : '';
  if (!id) return;
  const cur = new Set(Array.isArray(expandedRowKeys.value) ? expandedRowKeys.value.map(String) : []);
  if (expanded) cur.add(id);
  else cur.delete(id);
  expandedRowKeys.value = Array.from(cur);
};

// 判断点击是否被阻止（点击按钮等交互控件时不触发展开）
const isRowToggleBlocked = (evt) => {
  const el = evt?.target;
  if (!el || typeof el.closest !== 'function') return false;
  return !!el.closest('button, a, .el-button, .el-dropdown, .el-input, .el-select, .el-switch, .el-checkbox, .el-radio');
};

// 切换行展开状态
const toggleRowExpand = (row) => {
  if (!row || !Array.isArray(row.children) || row.children.length === 0) return;
  const id = row?.id !== undefined && row?.id !== null ? String(row.id) : '';
  if (!id) return;
  const cur = new Set(Array.isArray(expandedRowKeys.value) ? expandedRowKeys.value.map(String) : []);
  const expanded = cur.has(id);
  handleExpandChange(row, !expanded);
};

// 处理行点击
const handleRowClick = (row, _column, event) => {
  if (isRowToggleBlocked(event)) return;
  // 如果是类别节点，点击展开/收起
  if (row?.isCategory) {
    toggleRowExpand(row);
    return;
  }
  // 如果是成果节点，可以在这里添加查看详情的逻辑
  // TODO: 实现成果详情查看
};

// 获取部门名称（根据部门ID）
const getDeptNameById = (deptId) => {
  if (!deptId) return '';
  const dept = deptList.value.find(d => Number(d.deptId) === Number(deptId));
  return dept?.deptName || `部门${deptId}`;
};

const loadAchievementAuditInfo = async (achId, context = achievementContext()) => {
  if (!isCurrentAchievement(context) || Number(achId) !== context.achId) return;
  const requestId = ++achievementAuditRequestId;
  const isCurrent = () => isCurrentAchievement(context) && requestId === achievementAuditRequestId;
  achievementAuditList.value = [];
  achievementAuditLogs.value = [];
  if (!achId) return;

  try {
    achievementAuditLoading.value = true;
    const records = await getAchievementAuditsByAchId(achId);
    if (!isCurrent()) return;
    achievementAuditList.value = Array.isArray(records) ? records.filter(item => Number(item.achId ?? item.ach_id) === context.achId) : [];
    const subIds = achievementAuditList.value
      .map(item => item.subId ?? item.sub_id)
      .filter(Boolean);
    const logGroups = await Promise.all(subIds.map(async (subId) => {
      try {
        const logs = await getAchievementAuditLogs(subId);
        return Array.isArray(logs) ? logs : [];
      } catch {
        return [];
      }
    }));
    if (!isCurrent()) return;
    achievementAuditLogs.value = logGroups.flat().sort((a, b) => {
      const at = new Date(a.createTime || a.create_time || 0).getTime();
      const bt = new Date(b.createTime || b.create_time || 0).getTime();
      return at - bt;
    });
  } catch (e) {
    if (!isCurrent()) return;
    console.error('加载成果审核信息失败:', e);
    achievementAuditList.value = [];
    achievementAuditLogs.value = [];
  } finally {
    if (isCurrent()) {
      achievementAuditLoading.value = false;
      achievementSavedDraft = achievementDraft();
    }
  }
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
  } catch {
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
  const deptId = payload.deptId !== undefined && payload.deptId !== null ? Number(payload.deptId) : null;
  return {
    userId,
    role,
    deptId: deptId && !Number.isNaN(deptId) ? deptId : null,
    isDepartmentAccount: payload.isDepartmentAccount === true,
    canViewAchievement: payload.canViewAchievement === true,
    canUploadAchievement: payload.canUploadAchievement === true,
    canAuditAchievement: payload.canAuditAchievement === true,
  };
};

// 查看成果详情
const handleViewDetail = async (row) => {
  if (row.isCategory) return;
  uploadFileList.value = [];
  achievementAuditForm.result = 1;
  achievementAuditForm.comment = '同意';
  achievementDetailVersion++;
  currentAchievement.value = row;
  isAuditMode.value = false;
  auditingAchievementId.value = null;
  auditingCreateBy.value = null;
  gradeSelections.value = buildGradeSelectionsFromAchievement(row);
  fillFormFromAchievement(row);
  detailDrawerVisible.value = true;
  await loadAchievementAuditInfo(getAchievementId(row));
};

// 删除成果
const handleDelete = async (row) => {
  if (row.isCategory) return;
  const token = localStorage.getItem('token');

  if (!canModifyAchievement(row)) {
    ElMessage.warning('当前账号无权删除该成果');
    return;
  }

  const achId = getAchievementId(row);
  if (!achId) {
    ElMessage.error('无法获取成果ID');
    return;
  }

  try {
    await ElMessageBox.confirm(
      '确定要删除该成果吗？删除后无法恢复。',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    await deleteAchievement(achId);
    if (!achievementDisposed && token === localStorage.getItem('token')) emit('audit-completed');
    ElMessage.success('删除成功');

    // 重新加载数据
    await loadAchievements();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除成果失败:', error);
      ElMessage.error(error?.message || '删除失败，请重试');
    }
  }
};

watch(() => `${route.query.achId || ''}:${route.query.year || ''}`, () => {
  openedAchievementLink = '';
  detailDrawerVisible.value = false;
  loadAchievements();
});
onMounted(() => {
  loadDeptList();
  void getTodoAchievementAudits().then(rows=>{if(!achievementDisposed){achievementTodoIds.value=(Array.isArray(rows)?rows:[]).map(row=>row.achId??row.ach_id);shortcutError.value='';}}).catch(()=>{if(!achievementDisposed)shortcutError.value='待审核筛选加载失败';});
});
</script>

<style scoped>
/* 容器样式 */
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
}

/* 筛选栏 */
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

/* 表格区域 */
.table-wrapper {
  flex: 1;
  overflow: auto;
}

.date-cell {
  font-size: 12px;
  line-height: 1.5;
  color: #606266;
}

.text-gray-400 {
  color: #9ca3af;
  font-size: 12px;
}

.mr-1 {
  margin-right: 4px;
}

.ml-1 {
  margin-left: 4px;
}

/* 已上传文件信息 */
.uploaded-file-info {
  margin-top: 8px;
}

/* 文件预览样式 */
.preview-container {
  width: 100%;
}

.pdf-viewer {
  width: 100%;
}

.pdf-viewer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.file-name-display {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.doc-viewer {
  text-align: center;
  padding: 40px 20px;
}

.doc-icon {
  margin-bottom: 16px;
}

.mt-4 {
  margin-top: 16px;
}

.mt-2 {
  margin-top: 8px;
}

.text-gray-500 {
  color: #909399;
  font-size: 14px;
  line-height: 1.6;
}

/* 成果等次选择样式 */
.grade-selection {
  width: 100%;
}

.grade-list {
  margin-bottom: 10px;
}

.grade-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
}

.grade-select {
  flex-shrink: 0;
}

.achievement-form :deep(.el-form-item__label) {
  white-space: nowrap;
}

.audit-form {
  padding: 4px 8px 20px;
}

.achievement-audit-panel {
  border-top: 1px solid #ebeef5;
  margin: 4px 8px 20px;
  padding-top: 18px;
}

.achievement-audit-action {
  background: #f8f9fa;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-top: 22px;
  padding: 16px;
}

.achievement-audit-log {
  min-height: 96px;
  padding: 4px 0 0;
}

.audit-log-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 10px 12px;
}

.audit-log-title {
  color: #303133;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 6px;
}

.audit-log-meta {
  color: #606266;
  font-size: 13px;
}

.audit-log-status {
  margin-left: 16px;
  color: #909399;
}

.audit-log-comment {
  color: #606266;
  font-size: 13px;
  line-height: 1.5;
  margin-top: 6px;
}

.audit-material-row {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  flex-wrap: wrap;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 8px;
}

.grade-count-cell {
  font-size: 12px;
  line-height: 1.5;
  color: #606266;
  text-align: left;
  padding: 4px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.achievement-name-details { flex: 1; min-width: 0; max-width: 100%; }
.rejection-reason { margin-top: 5px; color: #c45656; white-space: normal; overflow-wrap: anywhere; font-size: 12px; line-height: 1.5; }
/* The tree controls and name share the column width instead of wrapping a 100% name below them. */
.table-wrapper :deep(.achievement-title-column > .cell) {
  display: flex;
  align-items: flex-start;
}

.table-wrapper :deep(.achievement-title-column .el-table__indent),
.table-wrapper :deep(.achievement-title-column .el-table__placeholder),
.table-wrapper :deep(.achievement-title-column .el-table__expand-icon) {
  flex-shrink: 0;
}

.task-name-cell {
  cursor: pointer;
  display: flex;
  flex: 1;
  min-width: 0;
  align-items: baseline;
  max-width: 100%;
}

.task-name-text {
  min-width: 0;
  white-space: normal;
  overflow-wrap: anywhere;
}

.task-name-text:hover {
  text-decoration: underline;
}

.child-count {
  margin-left: 6px;
  color: #909399;
  font-size: 12px;
  flex-shrink: 0;
  white-space: nowrap;
}

.table-actions {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  white-space: nowrap;
}

.table-actions :deep(.el-button + .el-button) {
  margin-left: 0;
}

/* 字体样式类 - 与任务中心保持一致 */
.font-bold {
  font-weight: bold;
}

.font-medium {
  font-weight: 500;
}

.text-gray-900 {
  color: #303133 !important;
}

.text-gray-700 {
  color: #4a5568 !important;
}

.text-gray-600 {
  color: #606266 !important;
}

.text-base {
  font-size: 16px;
}

.pl-2 {
  padding-left: 8px;
}

/* 详情抽屉样式（参考任务详情） */
.drawer-content {
  padding: 0;
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
  width: 120px;
  color: #909399;
  font-size: 14px;
  white-space: nowrap;
  flex-shrink: 0;
}

.info-row .value {
  color: #303133;
  font-size: 14px;
  flex: 1;
  word-break: break-word;
}
</style>
