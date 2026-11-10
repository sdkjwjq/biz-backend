<template>
  <div class="works-container">
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
            <el-icon><Calendar /></el-icon>
            <span>周期: 2025-01-01 至 2029-12-31</span>
          </div>
          <div class="meta-item">
            <el-icon><User /></el-icon>
            <span>责任部门：“双高”建设办公室</span>
          </div>
        </div>
      </div>
      <div class="project-actions">
        <el-button v-if="accessState.canViewCoreIndicators" @click="router.push('/home/core-indicators')">核心指标</el-button>
        <el-tag size="small" effect="plain" type="info">共 {{ hasCurrentBudget ? budgetTasks.length : '—' }} 条</el-tag>
        <div class="toggle-group">
          <button v-if="canUsePermission('tasks.read')" class="toggle-btn" :class="{ active: currentTab === 'task' }" @click="handleTabSwitch('task')">任务</button>
          <button v-if="canUsePermission('performance.read')" class="toggle-btn" :class="{ active: currentTab === 'performance' }" @click="handleTabSwitch('performance')">绩效</button>
          <button v-if="canUsePermission('achievements.read')" class="toggle-btn" :class="{ active: currentTab === 'achievement' }" @click="handleTabSwitch('achievement')">成果</button>
          <button v-if="canUsePermission('budget.read')" class="toggle-btn" :class="{ active: currentTab === 'budget' }" @click="handleTabSwitch('budget')">预算</button>
        </div>
      </div>
    </div>

    <div class="budget-view-tabs toggle-group">
      <button class="toggle-btn" :class="{ active: activeBudgetTab === 'total' }" @click="switchBudgetTab('total')">总预算</button>
      <button class="toggle-btn" :class="{ active: activeBudgetTab === 'annual' }" @click="switchBudgetTab('annual')">年度预算</button>
      <button class="toggle-btn" :class="{ active: activeBudgetTab === 'execution' }" @click="switchBudgetTab('execution')">年度预算执行</button>
    </div>

    <div v-if="activeBudgetTab === 'total'" class="budget-table-card">
      <p class="budget-plan-title"><strong>五年项目总预算</strong><span class="muted">（单位：万元 / %）· 默认锁定</span></p>
      <el-alert v-if="totalError" :title="totalError" type="error" :closable="false" /><el-button v-if="totalError" @click="loadTotal">重试</el-button><el-table :data="totalMatrix" v-loading="totalLoading" border>
        <el-table-column prop="item" label="建设内容" width="220" fixed />
        <el-table-column v-for="source in totalSources" :key="source" :label="source" align="center">
          <el-table-column label="金额" width="110" align="right"><template #default="{row}">{{ money(totalCell(row.item, source)?.amount) }}</template></el-table-column>
          <el-table-column label="比例" width="90" align="right"><template #default="{row}">{{ pct(totalCell(row.item, source)?.ratio) }}</template></el-table-column>
        </el-table-column>
      </el-table>
    </div>

    <div class="budget-table-card" v-show="activeBudgetTab === 'annual' || activeBudgetTab === 'execution'">
      <div class="budget-toolbar">
        <div class="budget-filter-panel">
          <QueryFilterBar class="budget-query" ref="budgetQuery" domain="budget" title="预算" :retain-previous="false" :remember="false" :context="{month:String(initialBudgetMonth),section:budgetSection}" :defaults="{year:initialYear}" :required-keys="['year']" :before-apply="beforeBudgetQuery" @state="budgetQueryState=$event" @result="receiveBudgetQuery" />
          <p class="budget-filter-note"><el-icon aria-hidden="true"><InfoFilled /></el-icon><span class="budget-note-copy"><span>筛选仅调整展示；合计、年度执行率分母及整表导出保持原口径。</span><span>导入、锁定和恢复均针对整张年度表。</span></span></p>
        </div>
        <el-alert v-if="budgetDirty" title="有未保存的修改，请保存后再离开或切换预算。" type="warning" :closable="false" />
        <el-alert v-if="budgetQueryState.initialized && !loading && !budgetQueryState.error && !budgetImported" title="所选年度尚未导入预算" type="info" :closable="false" />
        <div class="budget-data-actions">
          <el-button v-if="canFillBudget" type="primary" :loading="saveLoading" :disabled="!budgetDirty || isLocked || busy || !hasCurrentBudget" @click="saveChanges()">保存修改</el-button>
          <el-button v-if="isLocked ? isAdmin : canUsePermission('budget.lock', isAdmin)" class="toolbar-action" :type="isLocked ? undefined : 'warning'" plain
            :loading="lockLoading" :disabled="!hasCurrentBudget || busy" :title="isLocked && !isAdmin ? '仅管理员可以解锁预算' : ''" @click="toggleBudgetLock">
            <el-icon>
              <Lock v-if="isLocked" />
              <Unlock v-else />
            </el-icon>
            {{ isLocked ? '解锁' : '锁定' }}
          </el-button>
          <el-button v-if="canFillBudget" class="toolbar-action" type="primary" :disabled="busy || loading || isLocked" :title="isLocked ? '预算已锁定，请先由管理员解锁后再导入' : ''" @click="openImportDialog">
            <el-icon><Upload /></el-icon>
            导入
          </el-button>
          <el-button v-if="canUsePermission('budget.versions', canFillBudget)" class="toolbar-action" plain :loading="historyLoading" @click="openHistory">
            <el-icon><Clock /></el-icon>
            版本记录
          </el-button>
          <el-button v-if="canUsePermission('budget.export')" class="toolbar-action" type="success" plain :loading="exportLoading" :disabled="!hasCurrentBudget || busy" @click="exportCurrentBudget">
            <el-icon><Download /></el-icon>
            整表导出
          </el-button>
          <el-button v-if="budgetSection === 'execution'" class="toolbar-action" type="success" plain :loading="templateDownloading" @click="downloadTemplate">
            <el-icon><Download /></el-icon>
            模板下载
          </el-button>
        </div>
      </div>

      <div class="budget-sheet-card" v-loading="loading">
        <div v-if="hasCurrentBudget" class="budget-sheet-scroll">
          <table class="budget-sheet">
          <colgroup>
            <col class="side-col" />
            <col class="label-col" />
            <col class="label-col" />
            <col class="total-col" />
            <col v-for="source in visibleSources" :key="`source-${source.key}`" class="source-col" />
          </colgroup>
          <tbody>
            <tr>
              <th class="blank-head"></th>
              <th colspan="2" class="sub-head">{{ budgetSection === 'execution' ? '采集信息的维度' : '资金来源' }}</th>
              <th class="sub-head">合计数</th>
              <th v-for="source in visibleSources" :key="source.key" class="sub-head">{{ source.name }}</th>
            </tr>
            <tr>
              <th rowspan="4" class="vertical-head">预算<br />安排<br />（万元）</th>
              <th colspan="2" class="row-label">五年项目总预算</th>
              <td class="number-cell">{{ formatMoney(fiveYearTotal) }}</td>
              <td v-for="source in visibleSources" :key="`total-${source.key}`" class="number-cell">
                {{ formatMoney(source.total) }}
              </td>
            </tr>
            <tr>
              <th colspan="2" class="row-label">{{ filterYear }}年可使用资金</th>
              <td class="number-cell">{{ formatMoney(totalAvailable) }}</td>
              <td v-for="source in visibleSources" :key="`available-${source.key}`" :class="budgetSection === 'annual' ? 'number-cell' : 'input-cell'">
                <span v-if="budgetSection === 'annual'">{{ formatMoney(source.available) }}</span>
                <el-input-number :key="budgetEditDisabled ? 'disabled' : 'editable'" v-else v-model="source.available" :controls="false" :precision="2" :min="0" :disabled="isLocked || !canFillBudget || !budgetImported || busy || loading"
                  class="sheet-input" />
              </td>
            </tr>
            <tr>
              <th rowspan="2" class="row-label">其中</th>
              <th class="row-label">{{ filterYear }}年预算安排</th>
              <td class="number-cell">{{ formatMoney(totalAnnualPlan) }}</td>
              <td v-for="source in visibleSources" :key="`plan-${source.key}`" :class="budgetSection === 'annual' ? 'number-cell' : 'input-cell'">
                <span v-if="budgetSection === 'annual'">{{ formatMoney(source.plan) }}</span>
                <el-input-number :key="budgetEditDisabled ? 'disabled' : 'editable'" v-else v-model="source.plan" :controls="false" :precision="2" :min="0" :disabled="isLocked || !canFillBudget || !budgetImported || busy || loading"
                  class="sheet-input" />
              </td>
            </tr>
            <tr>
              <th class="row-label">上年结转结余资金</th>
              <td class="number-cell">{{ formatMoney(totalCarryover) }}</td>
              <td v-for="source in visibleSources" :key="`carry-${source.key}`" :class="budgetSection === 'annual' ? 'number-cell' : 'input-cell'">
                <span v-if="budgetSection === 'annual'">{{ formatMoney(source.carryover) }}</span>
                <el-input-number :key="budgetEditDisabled ? 'disabled' : 'editable'" v-else v-model="source.carryover" :controls="false" :precision="2" :min="0" :disabled="isLocked || !canFillBudget || !budgetImported || busy || loading"
                  class="sheet-input" />
              </td>
            </tr>
            <tr>
              <th class="blank-head"></th>
              <th colspan="2" class="row-label">{{ budgetSection === 'execution' ? '到位数（万元）' : '到位数' }}</th>
              <td class="number-cell">{{ formatMoney(totalArrived) }}</td>
              <td v-for="source in visibleSources" :key="`arrived-${source.key}`" :class="budgetSection === 'annual' ? 'number-cell' : 'input-cell'">
                <span v-if="budgetSection === 'annual'">{{ formatMoney(source.arrived) }}</span>
                <el-input-number :key="budgetEditDisabled ? 'disabled' : 'editable'" v-else v-model="source.arrived" :controls="false" :precision="2" :min="0" :disabled="isLocked || !canFillBudget || !budgetImported || busy || loading"
                  class="sheet-input" />
              </td>
            </tr>
          </tbody>
          </table>

          <table class="budget-sheet lower-sheet">
          <colgroup>
            <col class="side-col" />
            <col class="label-col" />
            <col class="label-col" />
            <col class="total-col" />
            <col v-for="task in visibleBudgetTasks" :key="`task-${task.key}`" class="task-col" />
          </colgroup>
          <tbody>
            <tr>
              <th :rowspan="activeBudgetTab === 'execution' ? 3 : 2" class="vertical-head">资金<br />支持<br />方向</th>
              <th colspan="2" class="row-label">{{ budgetSection === 'execution' ? '支出经济分类/用途（改革任务）' : '改革任务' }}</th>
              <th class="sub-head">合计数</th>
              <th v-for="task in visibleBudgetTasks" :key="`head-${task.key}`" class="task-head">
                <span class="task-index">{{ task.index }}</span>
                {{ task.name }}
              </th>
            </tr>
            <tr v-if="activeBudgetTab === 'execution'">
              <th colspan="2" class="row-label">预算数（万元）</th>
              <td class="number-cell">{{ formatPlanMoney(totalBudgetAmount) }}</td>
              <td v-for="task in visibleBudgetTasks" :key="`budgetAmount-${task.key}`" class="input-cell">
                <el-input-number :key="budgetEditDisabled ? 'disabled' : 'editable'" v-model="task.budgetAmount" :controls="false" :precision="2" :min="0" :disabled="isLocked || !canFillBudget || !budgetImported || busy || loading"
                  class="sheet-input" />
              </td>
            </tr>
            <tr>
              <th colspan="2" class="row-label">{{ budgetSection === 'execution' ? '总额（万元）' : '金额(万元)' }}</th>
              <td class="number-cell">{{ formatMoney(totalTaskBudget) }}</td>
              <td v-for="task in visibleBudgetTasks" :key="`amount-${task.key}`" :class="budgetSection === 'annual' ? 'number-cell' : 'input-cell'">
                <span v-if="budgetSection === 'annual'">{{ formatMoney(task.amount) }}</span>
                <el-input-number :key="budgetEditDisabled ? 'disabled' : 'editable'" v-else v-model="task.amount" :controls="false" :precision="2" :min="0" :disabled="isLocked || !canFillBudget || !budgetImported || busy || loading"
                  class="sheet-input" />
              </td>
            </tr>
            <tr v-for="(expense, index) in expenseRows" :key="expense.key">
              <th v-if="index === 0" :rowspan="expenseRows.length" class="vertical-head">支出<br />经济<br />分类</th>
              <th colspan="2" class="row-label">{{expense.label}}</th>
              <td class="number-cell">{{expense.budget ? formatPlanMoney(expense.total) : formatMoney(expense.total)}}</td>
              <td v-for="task in visibleBudgetTasks" :key="`${expense.key}-${task.key}`" :class="expense.budget || budgetSection === 'annual' ? 'number-cell' : 'input-cell'">
                <span v-if="expense.budget">{{ formatPlanMoney(task[expense.key]) }}</span>
                <span v-else-if="budgetSection === 'annual'">{{ formatMoney(task[expense.key]) }}</span>
                <el-input-number :key="budgetEditDisabled ? 'disabled' : 'editable'" v-else v-model="task[expense.key]" :controls="false" :precision="2" :min="0" :disabled="isLocked || !canFillBudget || !budgetImported || busy || loading" class="sheet-input" />
              </td>
            </tr>
            <tr v-if="activeBudgetTab === 'execution'">
              <th rowspan="5" class="effectiveness-head">资金使用有效性：选取一个最具代表性的核心产出指标</th>
              <th colspan="2" class="row-label">指标名称</th>
              <td class="dash-cell">——</td>
              <td v-for="task in visibleBudgetTasks" :key="`output-${task.key}`" class="text-cell">
                {{ task.coreOutput?.trim() ? task.coreOutput : archivedIndicators[task.index]?.indicatorName }}
              </td>
            </tr>
            <tr v-if="activeBudgetTab === 'execution'">
              <th colspan="2" class="row-label">项目期满的目标值</th>
              <td class="dash-cell">——</td>
              <td v-for="task in visibleBudgetTasks" :key="`target-${task.key}`" class="text-cell">
                {{ archivedIndicators[task.index]?.targetValue ?? task.targetValue }}
              </td>
            </tr>
            <tr v-if="activeBudgetTab === 'execution'">
              <th colspan="2" class="row-label">截至当前累计完成值</th>
              <td class="dash-cell">——</td>
              <td v-for="task in visibleBudgetTasks" :key="`current-${task.key}`" class="text-cell">
                {{ cumulativeValue(task) }}
              </td>
            </tr>
            <tr v-if="activeBudgetTab === 'execution'">
              <th colspan="2" class="row-label">{{ filterYear }}年完成值</th>
              <td class="dash-cell">——</td>
              <td v-for="task in visibleBudgetTasks" :key="`year-value-${task.key}`" class="text-cell">
                {{ yearValueOf(task) }}
              </td>
            </tr>
            <tr v-if="activeBudgetTab === 'execution'">
              <th colspan="2" class="row-label">工作说明</th>
              <td class="dash-cell">——</td>
              <td v-for="task in visibleBudgetTasks" :key="`comment-${task.key}`" class="text-cell comment-cell">
                {{ commentOf(task) }}
              </td>
            </tr>
          </tbody>
          </table>
        </div>
        <div v-else class="budget-placeholder" role="status">{{ requestedBudgetPeriod }}：{{ budgetQueryState.error ? '预算加载失败，请重新查询' : loading || !budgetQueryState.initialized ? '正在加载预算…' : '尚未导入预算' }}</div>
      </div>
    </div>

    <el-dialog v-model="importVisible" title="导入预算数据" width="620px" :destroy-on-close="true">
      <el-form label-width="110px">
        <el-form-item label="导入类型">
          <el-tag type="info" effect="plain">{{ budgetSection === 'execution' ? '年度预算执行表' : '年度预算表' }}</el-tag>
        </el-form-item>
        <el-form-item label="所属年度">
          <el-select v-model="importForm.year" style="width: 100%">
            <el-option v-for="year in yearOptions" :key="year" :label="`${year}年度`" :value="String(year)" />
          </el-select>
        </el-form-item>
        <el-form-item label="预算文件">
          <el-upload :auto-upload="false" :limit="1" :file-list="uploadFileList" :on-change="handleFileChange" :on-remove="handleFileRemove"
            accept=".xls,.xlsx,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet">
            <el-button type="primary" plain>
              <el-icon><Upload /></el-icon>
              选择 Excel 表
            </el-button>
            <template #tip>
              <div class="el-upload__tip">仅支持 .xls / .xlsx。确认导入后将作为预算执行依据并锁定。</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" :loading="importLoading" @click="confirmImport">确认导入并锁定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="historyVisible" title="预算版本记录"
      width="920px" :destroy-on-close="true">
      <el-alert title="导入、在线编辑和历史恢复都会生成新版本；恢复不会删除或改写原版本。在线编辑版本的原文件仅为导入来源附件。" type="info" :closable="false" show-icon />
      <QueryFilterBar v-if="historyVisible" ref="versionQuery" response-mode="page" :remember="false" domain="budget-versions" title="预算版本" :defaults="{year:String(historyContext.year),month:String(historyContext.month)}" :context="{pageSize:10,section:historyContext.section}" @state="versionQueryState=$event" @result="receiveVersionQuery" />
      <el-table v-loading="historyLoading" :data="historyList" class="history-table" empty-text="暂无版本记录">
        <el-table-column label="年月" width="110"><template #default="{row}">{{row.year}}年{{row.month}}月</template></el-table-column>
        <el-table-column label="版本" width="92">
          <template #default="{ row }">
            <el-tag :type="row.current ? 'success' : 'info'">V{{ row.versionNo }}</el-tag>
            <span v-if="row.current" class="current-version">当前</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90">
          <template #default="{ row }">{{ versionOperation(row.operationType) }}</template>
        </el-table-column>
        <el-table-column prop="fileName" label="文件" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ row.fileName || '—' }}</template>
        </el-table-column>
        <el-table-column prop="createdByName" label="操作人" width="120" />
        <el-table-column label="操作时间" width="168">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ row.remark || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="148" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewVersion(row)">查看</el-button>
            <el-button v-if="canUsePermission('budget.restore', isAdmin)" link type="warning" :disabled="row.current || busy || loading" :loading="restoringId === row.versionId"
              @click="restoreVersion(row)">恢复</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="versionPage" :page-size="10" :total="versionTotal" layout="total, prev, pager, next" @current-change="page=>versionQuery?.setPage(page)" />
    </el-dialog>

    <el-dialog v-model="versionDetailVisible" title="预算版本详情" width="1000px" append-to-body :destroy-on-close="true">
      <div v-loading="versionDetailLoading" class="version-detail">
        <el-descriptions v-if="versionDetail.version" :column="3" border>
          <el-descriptions-item label="版本">V{{ versionDetail.version.versionNo }}</el-descriptions-item>
          <el-descriptions-item label="操作">{{ versionOperation(versionDetail.version.operationType) }}</el-descriptions-item>
          <el-descriptions-item label="操作时间">{{ formatDateTime(versionDetail.version.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="操作人">{{ versionDetail.version.createdByName || '—' }}</el-descriptions-item>
          <el-descriptions-item :label="versionDetail.version.operationType === 'EDIT' ? '导入来源附件' : '文件'" :span="2">{{ versionDetail.version.fileName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="3">{{ versionDetail.version.remark || '—' }}</el-descriptions-item>
        </el-descriptions>
        <h4>资金来源</h4>
        <el-table :data="versionDetail.budget?.sources || []" max-height="260" border>
          <el-table-column prop="sourceName" label="资金来源" min-width="160" />
          <el-table-column label="五年总预算" width="120"><template #default="{ row }">{{ formatMoney(row.fiveYearTotal) }}</template></el-table-column>
          <el-table-column label="年度可用" width="120"><template #default="{ row }">{{ formatMoney(row.available) }}</template></el-table-column>
          <el-table-column label="年度安排" width="120"><template #default="{ row }">{{ formatMoney(row.annualPlan) }}</template></el-table-column>
          <el-table-column label="结转结余" width="120"><template #default="{ row }">{{ formatMoney(row.carryover) }}</template></el-table-column>
          <el-table-column label="到位数" width="120"><template #default="{ row }">{{ formatMoney(row.arrived) }}</template></el-table-column>
        </el-table>
        <h4>改革任务</h4>
        <el-table :data="versionDetail.budget?.tasks || []" max-height="320" border>
          <el-table-column prop="taskIndex" label="序号" width="70" />
          <el-table-column prop="taskName" label="改革任务" min-width="180" />
          <el-table-column label="金额" width="110"><template #default="{ row }">{{ formatMoney(row.amount) }}</template></el-table-column>
          <el-table-column label="商品和服务" width="120"><template #default="{ row }">{{ formatMoney(row.goods) }}</template></el-table-column>
          <el-table-column label="资本性支出" width="120"><template #default="{ row }">{{ formatMoney(row.capital) }}</template></el-table-column>
          <el-table-column label="其他支出" width="110"><template #default="{ row }">{{ formatMoney(row.other) }}</template></el-table-column>
          <el-table-column prop="achieved" label="累计实现情况" min-width="180" show-overflow-tooltip />
        </el-table>
      </div>
    </el-dialog>
    <el-dialog v-model="decisionVisible" :title="decisionLock ? '锁定预算' : '未保存修改'" width="460px" append-to-body
      :close-on-click-modal="false" :close-on-press-escape="!saveLoading" :show-close="!saveLoading" @closed="finishBudgetDecision('cancel')">
      <p>{{ decisionLock ? '当前预算有未保存修改，请选择保存并锁定，或放弃修改后锁定。' : '当前预算有未保存修改，请先保存或放弃修改，再继续操作。' }}</p>
      <template #footer>
        <el-button :disabled="saveLoading" @click="finishBudgetDecision('cancel')">取消</el-button>
        <el-button :disabled="saveLoading" @click="completeBudgetDecision('discard')">{{ decisionLock ? '放弃修改并锁定' : '放弃修改' }}</el-button>
        <el-button type="primary" :loading="saveLoading" @click="completeBudgetDecision('save')">{{ decisionLock ? '保存并锁定' : '保存后继续' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { accessState, canUsePermission } from '@/utils/accessState';
import { computed, nextTick, onUnmounted, reactive, ref, watch } from 'vue';
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router';
import QueryFilterBar from './QueryFilterBar.vue';
import { Calendar, User, Upload, Lock, Unlock, Download, Clock, InfoFilled } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { get } from '@/utils/request';
import { saveBudget, exportBudget as exportBudgetApi, exportBudgetTemplate, getBudgetVersion, importBudget,
  restoreBudgetVersion, updateBudgetLock } from '@/api/budget';

const route = useRoute();
const router = useRouter();
const archivedIndicators = ref({});
const cumulativeIndicators = ref({});
const budgetQuery=ref(null),budgetImported=ref(false),budgetTaskIds=ref([]),budgetApplied=ref({});
const visibleBudgetTasks=computed(()=>budgetTasks.filter(item=>budgetTaskIds.value.includes(String(item.key))));
const visibleSources=computed(()=>fundingSources.filter(item=>!budgetApplied.value.source?.length||budgetApplied.value.source.includes(String(item.key))));
const visibleExpenseTypes=computed(()=>[
  {key:'goods',label:'商品和服务支出',total:totalGoodsExpense.value},
  {key:'capital',label:'资本性支出',total:totalCapitalExpense.value},
  {key:'other',label:'其他支出',total:totalOtherExpense.value}
].filter(item=>!budgetApplied.value.expense?.length||budgetApplied.value.expense.includes(item.key)));
const receiveBudgetQuery=result=>{budgetApplied.value=result.query;filterYear.value=String(result.query.year);filterMonth.value=Number(result.query.month||initialBudgetMonth);budgetTaskIds.value=result.ids.map(String);budgetImported.value=result.imported;applyBudgetData(result.budget);};

const parseTokenPayload = () => {
  const token = localStorage.getItem('token') || '';
  const parts = token.split('.');
  if (parts.length < 2) return {};
  try {
    const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
    const json = decodeURIComponent(Array.from(atob(base64)).map((char) => {
      return `%${char.charCodeAt(0).toString(16).padStart(2, '0')}`;
    }).join(''));
    return JSON.parse(json);
  } catch {
    return {};
  }
};

const yearOptions = ['2025', '2026', '2027', '2028', '2029'];
const monthOptions = Array.from({ length: 12 }, (_, idx) => idx + 1);
const initialYear = yearOptions.includes(String(route.query.year || '')) ? String(route.query.year) : '2026';
const initialMonth = Number(route.query.month);
const filterYear = ref(initialYear);
const initialBudgetMonth=monthOptions.includes(initialMonth)?initialMonth:5;
const filterMonth = ref(initialBudgetMonth);
// 预算页三个独立视图：总预算 / 年度预算（现有可编辑月度表）/ 年度预算执行
const activeBudgetTab = ref('annual');
// —— 五年项目总预算：全系统一份，默认锁定，只读展示 ——
const totalRows = ref([]), totalLoading = ref(false), totalError=ref('');
const totalSources = ['项目预算总额', '中央财政投入资金', '地方财政投入资金', '举办方投入资金', '行业企业支持资金', '学校自筹资金'];
const totalMatrix = computed(() => [...new Set(totalRows.value.map(row => row.item))].map(item => ({ item })));
const totalCell = (item, source) => totalRows.value.find(row => row.item === item && row.source === source) || null;
const money = value => value == null ? '—' : Number(value).toFixed(2);
const pct = value => value == null ? '—' : Number(value).toFixed(2);
async function loadTotal() {
  totalLoading.value = true;totalError.value='';
  try { totalRows.value = (await get('/budget/plan/total')) || []; }
  catch (e) { totalError.value=e.message || '总预算加载失败'; }
  finally { totalLoading.value = false; }
}
// 年度预算与年度预算执行是两份独立数据（section 区分），切换页签要重新加载，避免互相覆盖。
// Element Plus initializes aria-disabled on mount; remount only when editability changes.
// The v-model remains in the sheet, so unlock/save refreshes cannot discard typed amounts.
const budgetEditDisabled = computed(() => isLocked.value || !canFillBudget.value || !budgetImported.value || busy.value || loading.value);
const budgetSection = computed(() => activeBudgetTab.value === 'execution' ? 'execution' : 'annual');
const switchBudgetTab = async (tab) => {
  if (tab === activeBudgetTab.value) return;
  if(busy.value || loading.value)return;
  if(!await confirmDiscard())return;
  activeBudgetTab.value = tab;
  if (tab === 'total') loadTotal();
  else { await nextTick(); await budgetQuery.value?.refresh(); }
};

const importVisible = ref(false);
const selectedFileName = ref('');
const selectedFile = ref(null);
const uploadFileList = ref([]);
const isLocked = ref(true);
const budgetQueryState=ref({loading:true});
const loading=computed(()=>budgetQueryState.value.loading);
const hasCurrentBudget=computed(()=>budgetQueryState.value.initialized&&!loading.value&&!budgetQueryState.value.error&&budgetImported.value);
const requestedBudgetPeriod=computed(()=>{const q=budgetQueryState.value.query||budgetApplied.value;return `${q.year||initialYear}年${q.month||initialBudgetMonth}月`;});
const saveLoading=ref(false),currentVersionNo=ref(0),savedCells=ref('');
const decisionVisible=ref(false),decisionLock=ref(false);
const busy=computed(()=>saveLoading.value||lockLoading.value||importLoading.value||restoringId.value!=null||decisionVisible.value);
const importLoading = ref(false);
const lockLoading = ref(false);
const exportLoading = ref(false);
const versionQueryState=ref({loading:true,initialized:false,error:''});
const versionQuery=ref(null),versionPage=ref(1),versionTotal=ref(0);
watch(()=>versionQueryState.value.loading,value=>{historyLoading.value=value});
const receiveVersionQuery=result=>{historyLoading.value=false;historyList.value=result.items;versionPage.value=result.page;versionTotal.value=result.total;};
const historyVisible = ref(false);
const historyLoading = ref(false);
const historyList = ref([]);
const historyContext = reactive({ year: '2026', month: 5, section:'annual' });
const versionDetailVisible = ref(false);
const versionDetailLoading = ref(false);
const versionDetail = ref({ version: null, budget: null });
const restoringId = ref(null);
const isAdmin = computed(() => accessState.token === localStorage.getItem('token') ? accessState.isAdmin : String(parseTokenPayload()?.role ?? '') === '0');

const canFillBudget = computed(() => accessState.token === localStorage.getItem('token') && accessState.canFillBudget);

const importForm = reactive({
  type: 'annual',
  year: '2026',
  month: 5
});

const todayDate = computed(() => {
  const today = new Date();
  return `${today.getFullYear()}年${today.getMonth() + 1}月${today.getDate()}日`;
});

const currentTab = computed(() => {
  const path = route.path;
  if (path.includes('/performance')) return 'performance';
  if (path.includes('/achievement')) return 'achievement';
  if (path.includes('/budget')) return 'budget';
  return 'task';
});

const handleTabSwitch = (tab) => {
  if (tab === 'task') router.push('/home/works');
  else if (tab === 'performance') router.push('/home/works/performance');
  else if (tab === 'achievement') router.push('/home/works/achievement');
  else if (tab === 'budget') router.push('/home/works/budget');
};

const fundingSources = reactive([]);
const budgetTasks = reactive([]);

// Keep the baseline separate from v-model; send only changed cells, never round-trip imported columns.
// 快照字段与页签无关：切换年度预算/年度预算执行时不会因字段集合变化误判为未保存
const editableCells=()=>({sources:fundingSources.map(s=>({sourceKey:s.key,annualPlan:s.plan,available:s.available,carryover:s.carryover,arrived:s.arrived})),tasks:budgetTasks.map(t=>({taskIndex:t.index,amount:t.amount,budgetAmount:t.budgetAmount,goods:t.goods,capital:t.capital,other:t.other}))});
const budgetDirty=computed(()=>!!savedCells.value&&JSON.stringify(editableCells())!==savedCells.value);
const changePatch=()=>{
 const original=JSON.parse(savedCells.value),current=editableCells();
 const diff=(kind,key)=>current[kind].flatMap(row=>{const before=original[kind].find(v=>String(v[key])===String(row[key]));const changed=Object.fromEntries(Object.entries(row).filter(([k,v])=>k!==key&&v!==before?.[k]));return Object.keys(changed).length?[{[key]:row[key],...changed}]:[]});
 const patch={year:Number(filterYear.value),month:Number(filterMonth.value),section:budgetSection.value,baseVersionNo:currentVersionNo.value,sources:diff('sources','sourceKey'),tasks:diff('tasks','taskIndex')};
 for(const row of [...patch.sources,...patch.tasks])for(const [key,value]of Object.entries(row))if(!['sourceKey','taskIndex'].includes(key)&&(!Number.isFinite(value)||value<0))throw new Error('金额不能为空，须填写非负数字');
 return patch;
};
let budgetDecisionPromise=null,budgetDecisionResolve;
function finishBudgetDecision(outcome){
 if(!budgetDecisionResolve)return;
 const resolve=budgetDecisionResolve;budgetDecisionResolve=null;budgetDecisionPromise=null;
 decisionVisible.value=false;resolve(outcome);
}
function decideBudgetChanges(lockAfterSave=false){
 if(budgetDecisionPromise)return budgetDecisionPromise;
 decisionLock.value=lockAfterSave;decisionVisible.value=true;
 budgetDecisionPromise=new Promise(resolve=>{budgetDecisionResolve=resolve});
 return budgetDecisionPromise;
}
async function completeBudgetDecision(action){
 if(saveLoading.value)return;
 if(action==='save'){if(await saveChanges(decisionLock.value))finishBudgetDecision('saved');return}
 const baseline=JSON.parse(savedCells.value);
 for(const source of fundingSources){const previous=baseline.sources.find(row=>row.sourceKey===source.key);if(previous){source.plan=previous.annualPlan;source.arrived=previous.arrived;if(budgetSection.value==='execution'){source.available=previous.available;source.carryover=previous.carryover}}}
 for(const task of budgetTasks){const previous=baseline.tasks.find(row=>row.taskIndex===task.index);if(previous){const keys=budgetSection.value==='execution'?['amount','budgetAmount','goods','capital','other']:['goods','capital','other'];for(const key of keys)task[key]=previous[key]}}
 finishBudgetDecision('discard');
}
async function confirmDiscard(){
 if(busy.value)return false;
 if(!budgetDirty.value)return true;
 if(loading.value)return false;
 return await decideBudgetChanges()!=='cancel';
}
// 月份不再是可筛选条件：清掉本地记忆/旧选中里的 month，只保留年度查询
const beforeBudgetQuery=(selected)=>{if(selected)delete selected.month;return busy.value?false:!budgetDirty.value||confirmDiscard();}
async function saveChanges(lockAfterSave=false){
 if(saveLoading.value||loading.value)return false;
 const token=localStorage.getItem('token'),year=filterYear.value,month=filterMonth.value;
 const isCurrent=()=>!budgetDisposed&&token===localStorage.getItem('token')&&year===filterYear.value&&month===filterMonth.value;
 saveLoading.value=true;
 try{const data=await saveBudget({...changePatch(),lockAfterSave});if(!isCurrent())return false;applyBudgetData(data);ElMessage.success(lockAfterSave?'修改已保存并锁定':'预算修改已保存');return true}
 catch(error){if(isCurrent())ElMessage.error(error.message||'保存失败，修改仍保留，请重试');return false}
 finally{saveLoading.value=false}
}
onBeforeRouteLeave(()=>confirmDiscard());
const beforeUnload=event=>{if(budgetDirty.value){event.preventDefault();event.returnValue=''}};
window.addEventListener('beforeunload',beforeUnload);
onUnmounted(()=>window.removeEventListener('beforeunload',beforeUnload));
onUnmounted(()=>finishBudgetDecision('cancel'));
defineExpose({confirmLeave:confirmDiscard});

const fiveYearTotal = computed(() => fundingSources.reduce((sum, item) => sum + Number(item.total || 0), 0));
const totalAvailable = computed(() => fundingSources.reduce((sum, item) => sum + Number(item.available || 0), 0));
const totalAnnualPlan = computed(() => fundingSources.reduce((sum, item) => sum + Number(item.plan || 0), 0));
const totalCarryover = computed(() => fundingSources.reduce((sum, item) => sum + Number(item.carryover || 0), 0));
const totalArrived = computed(() => fundingSources.reduce((sum, item) => sum + Number(item.arrived || 0), 0));
const totalTaskBudget = computed(() => budgetTasks.reduce((sum, item) => sum + Number(item.amount || 0), 0));
const totalGoodsExpense = computed(() => budgetTasks.reduce((sum, item) => sum + Number(item.goods || 0), 0));

const totalBudgetAmount = computed(() => budgetTasks.reduce((sum,item)=>sum+Number(item.budgetAmount||0),0));
// 支出经济分类：参照表只展示实际支出三行（商品和服务支出 / 资本性支出 / 其他支出）
const expenseRows = computed(() => activeBudgetTab.value === 'execution'
  ? visibleExpenseTypes.value.map(row => ({ ...row, label: row.key === 'goods' ? '其中：商品和服务支出' : row.label }))
  : visibleExpenseTypes.value);
const indicatorNumber = (value) => {
  if (value == null || value === '') return null;
  const num = Number(value);
  return Number.isFinite(num) ? num.toFixed(2) : String(value);
};
// 累计完成值取核心指标所有年度最近一次归档；当年完成值 = 本次累计值 − 上年末累计值
const cumulativeValue = (task) => indicatorNumber(cumulativeIndicators.value[task.index]?.actualValue) ?? '尚未归档';
const yearValueOf = (task) => indicatorNumber(archivedIndicators.value[task.index]?.yearValue) ?? '尚未归档';
const commentOf = (task) => archivedIndicators.value[task.index]?.comment || cumulativeIndicators.value[task.index]?.comment || '';
const totalCapitalExpense = computed(() => budgetTasks.reduce((sum, item) => sum + Number(item.capital || 0), 0));
const totalOtherExpense = computed(() => budgetTasks.reduce((sum, item) => sum + Number(item.other || 0), 0));

const formatPlanMoney=value=>value==null?'—':formatMoney(value);
const formatMoney = (value) => {
  const num = Number(value || 0);
  return Number.isFinite(num) ? num.toFixed(2) : '0.00';
};

const formatDateTime = (value) => {
  if (!value) return '—';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value);
  const parts = [date.getFullYear(), String(date.getMonth() + 1).padStart(2, '0'), String(date.getDate()).padStart(2, '0')];
  return `${parts.join('-')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
};

const versionOperation = (type) => ({ INITIAL: '初始版本', IMPORT: '导入', RESTORE: '恢复', EDIT: '在线编辑' }[type] || type || '—');

const pickValue = (item, camelKey, snakeKey, defaultValue = '') => {
  if (!item) return defaultValue;
  return item[camelKey] ?? item[snakeKey] ?? defaultValue;
};

const toNumber = (value) => {
  const num = Number(value ?? 0);
  return Number.isFinite(num) ? num : 0;
};

const replaceReactiveList = (target, source) => {
  target.splice(0, target.length, ...source);
};

const applyBudgetData = (data) => {
  archivedIndicators.value = data?.archivedIndicators || {};
  cumulativeIndicators.value = data?.cumulativeIndicators || {};
  const sheet = data?.sheet || {};
  const sources = Array.isArray(data?.sources) ? data.sources : [];
  const tasks = Array.isArray(data?.tasks) ? data.tasks : [];

  currentVersionNo.value=Number(data?.currentVersionNo||0);
  isLocked.value = Number(pickValue(sheet, 'locked', 'locked', 0)) === 1;

  if (sources.length > 0) {
    replaceReactiveList(fundingSources, sources.map((item, index) => ({
      key: pickValue(item, 'sourceKey', 'source_key', `source-${index}`),
      name: pickValue(item, 'sourceName', 'source_name', ''),
      total: toNumber(pickValue(item, 'fiveYearTotal', 'five_year_total', 0)),
      available: toNumber(pickValue(item, 'available', 'available', 0)),
      plan: toNumber(pickValue(item, 'annualPlan', 'annual_plan', 0)),
      carryover: toNumber(pickValue(item, 'carryover', 'carryover', 0)),
      arrived: toNumber(pickValue(item, 'arrived', 'arrived', 0))
    })));
  } else {
    replaceReactiveList(fundingSources, []);
  }

  if (tasks.length > 0) {
    replaceReactiveList(budgetTasks, tasks.map((item, index) => ({
      key: pickValue(item, 'taskIndex', 'task_index', index + 1),
      index: pickValue(item, 'taskIndex', 'task_index', index + 1),
      name: pickValue(item, 'taskName', 'task_name', ''),
      amount: toNumber(pickValue(item, 'amount', 'amount', 0)),
      goods: toNumber(pickValue(item, 'goods', 'goods', 0)),
      capital: toNumber(pickValue(item, 'capital', 'capital', 0)),
      other: toNumber(pickValue(item, 'other', 'other', 0)),
      goodsBudget: pickValue(item, 'goodsBudget', 'goods_budget', null) == null ? null : toNumber(pickValue(item, 'goodsBudget', 'goods_budget', null)),
      capitalBudget: pickValue(item, 'capitalBudget', 'capital_budget', null) == null ? null : toNumber(pickValue(item, 'capitalBudget', 'capital_budget', null)),
      otherBudget: pickValue(item, 'otherBudget', 'other_budget', null) == null ? null : toNumber(pickValue(item, 'otherBudget', 'other_budget', null)),
      budgetAmount: pickValue(item, 'budgetAmount', 'budget_amount', null) == null ? null : toNumber(pickValue(item, 'budgetAmount', 'budget_amount', null)),
      coreOutput: pickValue(item, 'coreOutput', 'core_output', ''),
      targetValue: pickValue(item, 'targetValue', 'target_value', ''),
      achieved: pickValue(item, 'achieved', 'achieved', '')
    })));
  } else {
    replaceReactiveList(budgetTasks, []);
  }
  savedCells.value=JSON.stringify(editableCells());
  budgetRequestVersion+=1;
};

let budgetRequestVersion = 0;

let detailRequestVersion = 0;
let budgetDisposed = false;
onUnmounted(() => {
  budgetDisposed = true;
  budgetRequestVersion += 1;

  detailRequestVersion += 1;
});

const templateDownloading = ref(false);
const downloadTemplate = async () => {
  if (templateDownloading.value) return;
  templateDownloading.value = true;
  try {
    const result = await exportBudgetTemplate(filterYear.value, budgetSection.value);
    downloadBlob(result.blob, result.filename);
    ElMessage.success('模板已下载');
  } catch (error) {
    ElMessage.error(error.message || '模板下载失败');
  } finally {
    templateDownloading.value = false;
  }
};

const downloadBlob = (blob, filename) => {
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
};

const exportCurrentBudget = async () => {
  if(!await confirmDiscard())return;
  exportLoading.value = true;
  try {
    const result = await exportBudgetApi(filterYear.value, filterMonth.value, budgetSection.value);
    downloadBlob(result.blob, result.filename);
    ElMessage.success('预算数据已导出');
  } catch (error) {
    ElMessage.error(error.message || '预算数据导出失败');
  } finally {
    exportLoading.value = false;
  }
};

const toggleBudgetLock = async () => {
  if (loading.value || busy.value) return;
  const version = budgetRequestVersion;
  const year = filterYear.value;
  const month = filterMonth.value;
  const isCurrent = () => !budgetDisposed && version === budgetRequestVersion
    && year === filterYear.value && month === filterMonth.value;
  if(!isLocked.value&&budgetDirty.value){
    const decision=await decideBudgetChanges(true);
    if(decision==='saved'||decision==='cancel')return;
  }
  lockLoading.value = true;
  try {
    const nextLocked = !isLocked.value;
    const data = await updateBudgetLock(year, month, nextLocked, budgetSection.value);
    if (!isCurrent()) return;
    if (data) applyBudgetData(data);
    else isLocked.value = nextLocked;
    ElMessage.success(nextLocked ? '预算表已锁定' : '预算表已解锁，可编辑');
  } catch (error) {
    if (!isCurrent()) return;
    ElMessage.error(error.message || '预算锁定状态更新失败');
  } finally {
    lockLoading.value = false;
  }
};

const loadHistory = async () => {await versionQuery.value?.refresh();};

const openHistory = async () => {
  historyContext.year = filterYear.value;
  historyContext.month = filterMonth.value;
  historyContext.section = budgetSection.value;
  historyList.value = [];
  versionPage.value=1;
  historyVisible.value = true;
};

const viewVersion = async (row) => {
  if (!row?.versionId) return;
  const requestId = ++detailRequestVersion;
  versionDetail.value = { version: null, budget: null };
  versionDetailVisible.value = true;
  versionDetailLoading.value = true;
  try {
    const data = await getBudgetVersion(row.versionId);
    if (budgetDisposed || requestId !== detailRequestVersion || !versionDetailVisible.value) return;
    versionDetail.value = data || { version: null, budget: null };
  } catch (error) {
    if (budgetDisposed || requestId !== detailRequestVersion || !versionDetailVisible.value) return;
    ElMessage.error(error.message || '预算版本详情加载失败');
  } finally {
    if (!budgetDisposed && requestId === detailRequestVersion) versionDetailLoading.value = false;
  }
};

const restoreVersion = async (row) => {
  if (!row?.versionId || row.current || busy.value || loading.value) return;
  if(!await confirmDiscard())return;
  try {
    const result = await ElMessageBox.prompt(
      `恢复 ${row.year}年${row.month}月 V${row.versionNo} 后将生成一个新的当前版本，原有版本不会删除。`,
      '恢复预算版本',
      {
        confirmButtonText: '确认恢复',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入恢复原因（最多200字）',
        inputValidator: value => {
          const text = String(value || '').trim();
          if (!text) return '请填写恢复原因';
          if (text.length > 200) return '恢复原因不能超过200字';
          return true;
        }
      }
    );
    restoringId.value = row.versionId;
    const data = await restoreBudgetVersion(row.versionId, String(result.value || '').trim());
    const samePeriod = String(filterYear.value) === String(row.year)
      && Number(filterMonth.value) === Number(row.month) && (data?.sheet?.section||'annual')===budgetSection.value;
    if (samePeriod && data) { applyBudgetData(data); restoringId.value=null; await budgetQuery.value?.refresh(); }
    versionDetailVisible.value = false;
    await loadHistory();
    ElMessage.success(`已恢复 V${row.versionNo}，并生成新的当前版本`);
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(error.message || '预算版本恢复失败');
  } finally {
    restoringId.value = null;
  }
};

const openImportDialog = async () => {
  if (isLocked.value) {
    ElMessage.warning('预算已锁定，请先由管理员解锁后再导入');
    return;
  }
  if(!await confirmDiscard())return;
  importForm.year = filterYear.value;
  // 年度预算/年度预算执行都是年度表，统一落在年度槽位（默认 5 月），导入时不再让用户选月份。
  importForm.month = initialBudgetMonth;
  selectedFileName.value = '';
  selectedFile.value = null;
  uploadFileList.value = [];
  importVisible.value = true;
};

const handleFileChange = (file, fileList = []) => {
  const name = file?.name || file?.raw?.name || '';
  const ext = name.split('.').pop()?.toLowerCase();
  if (!['xls', 'xlsx'].includes(ext)) {
    ElMessage.warning('请导入 .xls 或 .xlsx 格式的 Excel 表');
    selectedFileName.value = '';
    selectedFile.value = null;
    uploadFileList.value = [];
    return false;
  }
  selectedFileName.value = name;
  selectedFile.value = file.raw || file;
  uploadFileList.value = fileList.slice(-1);
  return true;
};

const handleFileRemove = () => {
  selectedFileName.value = '';
  selectedFile.value = null;
  uploadFileList.value = [];
};

const confirmImport = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择 Excel 表文件');
    return;
  }
  if(!await confirmDiscard())return;
  importLoading.value = true;
  try {
    const data = await importBudget(importForm.year, importForm.month, selectedFile.value, budgetSection.value);
    filterYear.value = importForm.year;
    filterMonth.value = importForm.month;
    if (data) applyBudgetData(data);
    importVisible.value = false;
    importLoading.value = false;
    await budgetQuery.value?.patch({year:String(importForm.year),month:String(importForm.month)});
    ElMessage.success('预算数据已导入并锁定');
  } catch (error) {
    ElMessage.error(error.message || '预算数据导入失败');
  } finally {
    importLoading.value = false;
  }
};

</script>

<style scoped>
.works-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
  min-height: 0;
  overflow: hidden;
}

.notice-bar,
.project-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  flex-shrink: 0;
}

.budget-table-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #ebeef5;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  overflow-x: hidden;
  overflow-y: auto;
}

.notice-bar {
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
  border: 1px solid #ebeef5;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  padding: 20px;
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

.meta-item,
.project-actions,
.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
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

.budget-toolbar {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 14px;
  margin-bottom: 18px;
  min-width: 0;
}

.budget-filter-panel {
  container: budget-filters / inline-size;
  min-width: 0;
  padding: 14px 16px;
  border: 1px solid #e6ebf2;
  border-radius: 8px;
  background: #f8fafc;
}

.budget-query {
  width: 100%;
  margin-bottom: 0;
}

.budget-query :deep(.basic-fields) {
  column-gap: 20px;
  row-gap: 12px;
}

.budget-query :deep(.basic-fields .query-field > .el-select) {
  width: 160px;
}

.budget-query :deep(.query-actions) {
  margin-left: auto;
  gap: 8px;
}

.budget-query :deep(.query-status) {
  position: static;
  margin: 0 0 8px;
  line-height: 20px;
}

.budget-query :deep(.applied-tags) {
  margin-top: 10px;
  gap: 8px;
}

.budget-query :deep(.advanced-groups) {
  background: #fff;
  border-color: #e0e7f0;
}

.budget-filter-note {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  margin: 10px 0 0;
  color: #65758b;
  font-size: 12px;
  line-height: 20px;
}

.budget-filter-note .el-icon {
  flex: none;
  margin-top: 3px;
  font-size: 14px;
  color: #7c8fa7;
}

.budget-note-copy {
  display: flex;
  flex-wrap: wrap;
  column-gap: 8px;
  min-width: 0;
}

.budget-note-copy > span {
  max-width: 100%;
}

.budget-data-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.budget-data-actions :deep(.el-button) {
  margin-left: 0;
}

@container budget-filters (max-width: 680px) {
  .budget-query :deep(.query-actions) {
    flex-basis: 100%;
    justify-content: flex-start;
    margin-left: 0;
  }
}

.toolbar-action {
  min-width: 96px;
}

.toolbar-action + .toolbar-action {
  margin-left: 0;
}

.history-table {
  margin-top: 16px;
}

.current-version {
  display: block;
  margin-top: 4px;
  color: #67c23a;
  font-size: 12px;
}

.version-detail h4 {
  margin: 18px 0 10px;
  color: #303133;
}

.budget-sheet-card {
  position: relative;
  flex: 0 0 auto;
  display: flex;
  overflow: hidden;
  min-width: 0;
  min-height: 0;
}

.budget-sheet-scroll {
  flex: 1;
  width: 100%;
  max-width: 100%;
  min-height: 0;
  max-height: 600px;
  overflow-x: auto;
  overflow-y: auto;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #fff;
  scrollbar-color: #9ca3af #eef2f7;
  scrollbar-width: auto;
}
.budget-placeholder{display:flex;align-items:center;justify-content:center;min-height:220px;width:100%;color:#909399;font-size:14px}

.budget-sheet-scroll::-webkit-scrollbar {
  width: 14px;
  height: 14px;
}

.budget-sheet-scroll::-webkit-scrollbar-track {
  background: #eef2f7;
  border-radius: 999px;
}

.budget-sheet-scroll::-webkit-scrollbar-thumb {
  background: #9ca3af;
  border: 3px solid #eef2f7;
  border-radius: 999px;
}

.budget-sheet-scroll::-webkit-scrollbar-thumb:hover {
  background: #6b7280;
}

.budget-sheet {
  width: 100%;
  min-width: 1660px;
  border-collapse: collapse;
  table-layout: fixed;
  font-size: 13px;
}

.lower-sheet {
  border-top: none;
}

.budget-sheet th,
.budget-sheet td {
  border: 1px solid #e5e7eb;
  height: 54px;
  padding: 6px 10px;
  color: #344054;
  background: #fff;
  vertical-align: middle;
}

.budget-sheet th {
  font-weight: 700;
  text-align: center;
}

.top-head {
  height: 44px;
  background: #f8f9fa;
}

.sub-head,
.blank-head {
  background: #f8f9fa;
}

.vertical-head {
  width: 74px;
  line-height: 1.45;
  background: #f8f9fa;
  white-space: nowrap;
  min-width: 74px;
}

.row-label {
  background: #fff;
  text-align: center;
  font-weight: 700;
  white-space: nowrap;
}

.task-head {
  line-height: 1.45;
  white-space: normal;
  word-break: break-word;
  background: #f8f9fa;
}

.task-index {
  margin-right: 4px;
  color: #5b6472;
}

.number-cell,
.input-cell,
.dash-cell {
  text-align: right;
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}

.dash-cell {
  text-align: center;
}

.text-cell {
  line-height: 1.45;
  text-align: left;
  white-space: normal;
  word-break: break-all;
}

.sheet-input {
  width: 100%;
}

.sheet-input :deep(.el-input__wrapper) {
  height: 32px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  background: #fff;
}

.sheet-input :deep(.el-input.is-disabled .el-input__wrapper),
.sheet-textarea :deep(.el-textarea.is-disabled .el-textarea__inner) {
  background: #f8f9fa;
  box-shadow: 0 0 0 1px #ebeef5 inset;
}

.sheet-input :deep(.el-input__inner) {
  text-align: right;
  font-variant-numeric: tabular-nums;
}

.sheet-textarea {
  width: 100%;
}

.sheet-textarea :deep(.el-textarea__inner) {
  min-height: 44px !important;
  font-size: 12px;
  line-height: 1.4;
  resize: none;
}

.effectiveness-head {
  width: 74px;
  line-height: 1.4;
  background: #f8f9fa;
  text-align: center;
  white-space: normal;
  word-break: break-word;
  min-width: 74px;
  padding: 8px 6px;
}

.side-col {
  width: 74px;
}

.label-col {
  width: 120px;
}

.total-col {
  width: 156px;
}

.source-col {
  width: 20%;
  min-width: 220px;
}

.task-col {
  width: 10%;
  min-width: 110px;
}

.side-col {
  min-width: 74px;
}

.label-col {
  min-width: 120px;
}

.total-col {
  min-width: 156px;
}

@media (max-width: 900px) {
  .project-card,
  .budget-toolbar {
    align-items: flex-start;
    flex-direction: column;
    gap: 12px;
  }

  .project-meta {
    flex-wrap: wrap;
  }
}
</style>
