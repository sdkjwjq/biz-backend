<template>
  <div class="dashboard-content" v-loading="loading">
    <!-- 顶部统计卡片 -->
    <div class="stats-grid">
      <div v-for="(card, index) in statsCards" :key="index" class="stat-card hover-effect">
        <div class="card-icon-bg">
          <component :is="card.icon" />
        </div>
        <div class="card-header">
          <span class="card-title">{{ card.title }}</span>
          <el-tag :type="card.tagType" size="small" effect="light" round>{{ card.tagText }}</el-tag>
        </div>
        <div class="card-body">
          <span class="card-value">{{ card.value }}</span>
          <span v-if="card.suffix" class="card-suffix">{{ card.suffix }}</span>
          <div class="trend-box" :class="card.trend === 'up' ? 'up' : 'down'" v-if="card.trend">
            <el-icon><component :is="card.trend === 'up' ? 'Top' : 'Bottom'" /></el-icon>
            <span>{{ card.trendRate }}%</span>
          </div>
        </div>
        <div class="card-progress">
          <el-progress 
            :percentage="card.percentage" 
            :status="card.status" 
            :stroke-width="6" 
            :show-text="false"
            :color="card.color"
          />
        </div>
        <div class="card-footer">
          <span>年度目标完成率</span>
          <span class="footer-val">{{ card.percentage }}%</span>
        </div>
      </div>
    </div>

    <!-- 中部：趋势图表 (新增) -->
    <div class="chart-section">
      <div class="panel-header no-border">
        <div class="panel-title">
          <el-icon class="title-icon"><TrendCharts /></el-icon>
          建设监测指标年度趋势
        </div>
        <el-radio-group v-model="chartPeriod" size="small">
          <el-radio-button label="month">月度</el-radio-button>
          <el-radio-button label="quarter">季度</el-radio-button>
        </el-radio-group>
      </div>
      <div ref="trendChartRef" class="trend-chart"></div>
    </div>

    <!-- 下部内容：左右布局 -->
    <div class="content-grid">
      
      <!-- 左侧：我的待办 -->
      <div class="grid-item left-panel">
        <div class="panel-header">
          <div class="panel-title">
            <el-icon class="title-icon"><List /></el-icon>
            我的待办
            <el-badge :value="todoList.length" class="ml-2" type="danger" />
          </div>
          <el-button type="primary" link @click="$router.push('/home/works')">进入工作中心</el-button>
        </div>
        
        <div class="panel-body">
          <el-table 
            :data="todoList" 
            style="width: 100%" 
            :header-cell-style="{background:'#f8f9fa', color:'#606266'}"
            stripe
          >
            <el-table-column prop="type" label="类型" width="80" align="center">
              <template #default="scope">
                <el-tag :type="getTodoTag(scope.row.type)" size="small" effect="plain">
                  {{ scope.row.type }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="任务/事项名称" min-width="200" show-overflow-tooltip>
              <template #default="scope">
                <span class="todo-title">{{ scope.row.name }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="project" label="所属项目" min-width="120" show-overflow-tooltip>
              <template #default="scope">
                <span class="project-text">{{ scope.row.project }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="deadline" label="截止时间" width="110" align="center">
              <template #default="scope">
                <span class="date-text" :class="{'text-danger': isUrgent(scope.row.deadline)}">
                  {{ scope.row.deadline }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="90" align="center">
              <template #default="scope">
                <el-button type="primary" text bg size="small" @click="handleProcess(scope.row)">
                  去处理
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- 右侧：工作滞后部门预警 -->
      <div class="grid-item right-panel">
        <div class="panel-header">
          <div class="panel-title">
            <el-icon class="title-icon warning"><WarningFilled /></el-icon>
            工作滞后预警 TOP5
          </div>
          <el-tooltip content="统计当前所有未完成任务的滞后比例" placement="top">
             <el-icon class="info-icon"><InfoFilled /></el-icon>
          </el-tooltip>
        </div>
        <div class="panel-body warning-list custom-scrollbar">
          <div v-for="(dept, idx) in warningDepts" :key="idx" class="warning-item">
            <div class="warning-index" :class="'idx-'+(idx+1)">{{ idx + 1 }}</div>
            <div class="warning-content">
              <div class="warning-header">
                <span class="dept-name">{{ dept.name }}</span>
                <div class="warning-actions">
                  <span :class="['dept-rate', getRateClass(dept.rate)]">滞后率 {{ dept.rate }}%</span>
                  <el-button link type="primary" size="small" @click="handleRemind(dept)">
                    <el-icon><Bell /></el-icon> 催办
                  </el-button>
                </div>
              </div>
              <el-progress 
                :percentage="dept.rate" 
                :color="getWarningColor(dept.rate)" 
                :stroke-width="6"
                :show-text="false"
              />
            </div>
          </div>
        </div>
        <!-- 底部提示框 -->
        <div class="panel-footer">
          <div class="warning-tip">
            <div class="tip-icon"><el-icon><Notification /></el-icon></div>
            <div class="tip-text">
              <span class="tip-title">系统提示：</span>
              请重点关注 <span class="highlight">安全保卫处</span> 和 <span class="highlight">党政办公室</span> 的材料上报进度，距中期检查仅剩 5 天。
            </div>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import { 
  List, WarningFilled, TrendCharts, 
  Bell, Notification, InfoFilled 
} from '@element-plus/icons-vue';
import { ElMessage, ElNotification } from 'element-plus';
import * as echarts from 'echarts';

// --- 状态管理 ---
const loading = ref(true);
const chartPeriod = ref('month');
const trendChartRef = ref(null);
let trendChart = null;

// --- 模拟数据 ---
const statsCards = [
  { 
    title: '任务总数', value: '4,321', trend: 'up', trendRate: 12.5,
    percentage: 98, status: 'success', 
    tagText: '正常推进', tagType: 'success', color: '#67c23a',
    icon: 'Files'
  },
  { 
    title: '资金执行', value: '1,234', suffix: '万元', trend: 'down', trendRate: 5.2,
    percentage: 27.6, status: '', 
    tagText: '进度滞后', tagType: 'warning', color: '#e6a23c',
    icon: 'Money'
  },
  { 
    title: '绩效指标', value: '120', suffix: '项', trend: 'up', trendRate: 8.4,
    percentage: 65.2, status: '', 
    tagText: '运行良好', tagType: 'primary', color: '#409eff',
    icon: 'DataAnalysis'
  },
  { 
    title: '预警事项', value: '15', trend: 'up', trendRate: 20.1,
    percentage: 12, status: 'exception', 
    tagText: '重点关注', tagType: 'danger', color: '#f56c6c',
    icon: 'WarningFilled'
  },
];

const todoList = [
  { type: '任务', name: '1.2.3 实施师德师风行为准则考核', project: '国双高项目', deadline: '2025-12-31' },
  { type: '任务', name: '2.1.2 建立校企定期需求对接机制', project: '国双高项目', deadline: '2025-12-31' },
  { type: '绩效', name: '3.1 核心课程建设数量达标审核', project: '省高水平学校建设', deadline: '2025-06-20' },
  { type: '资金', name: '4.1 实训基地设备采购资金审批', project: '数字化教学资源库', deadline: '2025-05-15' },
  { type: '任务', name: '5.2.1 国际交流合作项目备案', project: '国际化办学', deadline: '2025-08-10' },
];

const warningDepts = [
  { name: '安全保卫处', rate: 85 },
  { name: '党政办公室', rate: 72 },
  { name: '教务处', rate: 45 },
  { name: '信息技术中心', rate: 25 },
  { name: '财务处', rate: 15 },
];

// --- ECharts 初始化 ---
const initChart = () => {
  if (!trendChartRef.value) return;
  
  trendChart = echarts.init(trendChartRef.value);
  const option = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['任务完成数', '资金执行额(万)'], bottom: 0 },
    grid: { top: 30, right: 30, bottom: 40, left: 40, containLabel: true },
    xAxis: {
      type: 'category',
      data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
      axisLine: { lineStyle: { color: '#ddd' } },
      axisLabel: { color: '#666' }
    },
    yAxis: [
      { type: 'value', name: '数量', minInterval: 1, splitLine: { lineStyle: { type: 'dashed' } } },
      { type: 'value', name: '金额', splitLine: { show: false } }
    ],
    series: [
      {
        name: '任务完成数',
        type: 'line',
        smooth: true,
        itemStyle: { color: '#409eff' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64,158,255,0.3)' },
            { offset: 1, color: 'rgba(64,158,255,0)' }
          ])
        },
        data: [120, 132, 101, 134, 90, 230, 210, 180, 200, 250, 280, 300]
      },
      {
        name: '资金执行额(万)',
        type: 'bar',
        yAxisIndex: 1,
        barWidth: 12,
        itemStyle: { color: '#e6a23c', borderRadius: [4, 4, 0, 0] },
        data: [220, 182, 191, 234, 290, 330, 310, 350, 400, 420, 450, 480]
      }
    ]
  };
  trendChart.setOption(option);
};

// --- 业务方法 ---
const isUrgent = (deadline) => {
  const target = new Date(deadline).getTime();
  const now = new Date().getTime();
  const diffDays = (target - now) / (1000 * 3600 * 24);
  return diffDays < 30; // 小于30天显示红色
};

const getTodoTag = (type) => {
  const map = { '任务': '', '绩效': 'warning', '资金': 'success' };
  return map[type] || 'info';
};

const getWarningColor = (rate) => {
  if (rate > 80) return '#f56c6c'; // 严重滞后
  if (rate > 50) return '#e6a23c'; // 中度
  return '#409eff'; // 轻度
};

const getRateClass = (rate) => {
  if (rate > 80) return 'text-danger';
  if (rate > 50) return 'text-warning';
  return 'text-primary';
};

const handleProcess = (row) => {
  ElMessage.success(`正在跳转处理：${row.name}`);
  // 实际逻辑：router.push(...)
};

const handleRemind = (dept) => {
  ElNotification({
    title: '催办发送成功',
    message: `已向 ${dept.name} 负责人发送短信和系统提醒。`,
    type: 'success',
  });
};

const handleResize = () => trendChart && trendChart.resize();

// --- 生命周期 ---
onMounted(() => {
  // 模拟加载延迟
  setTimeout(() => {
    loading.value = false;
    nextTick(() => {
      initChart();
      window.addEventListener('resize', handleResize);
    });
  }, 600);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  if (trendChart) trendChart.dispose();
});
</script>

<style scoped>
.dashboard-content {
  padding-bottom: 20px;
}

/* 顶部统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}
@media (max-width: 1200px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 768px) { .stats-grid { grid-template-columns: 1fr; } }

.stat-card {
  background: #fff;
  padding: 24px;
  border-radius: 12px;
  border: 1px solid #f0f2f5;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.02);
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}
.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}
/* 卡片背景大图标装饰 */
.card-icon-bg {
  position: absolute;
  right: -10px;
  top: -10px;
  font-size: 100px;
  color: rgba(0, 0, 0, 0.03);
  transform: rotate(15deg);
  pointer-events: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.card-title {
  color: #606266;
  font-size: 14px;
  font-weight: 500;
}

.card-body {
  display: flex;
  align-items: baseline;
  margin-bottom: 16px;
  gap: 8px;
}
.card-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  font-family: 'DIN Alternate', sans-serif;
  line-height: 1;
}
.card-suffix {
  font-size: 13px;
  color: #909399;
}
.trend-box {
  display: flex;
  align-items: center;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
}
.trend-box.up { color: #f56c6c; background: rgba(245, 108, 108, 0.1); }
.trend-box.down { color: #67c23a; background: rgba(103, 194, 58, 0.1); }

.card-footer {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
  margin-top: 12px;
}
.footer-val {
  color: #303133;
  font-weight: bold;
}

/* 中部图表区 */
.chart-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid #f0f2f5;
}
.trend-chart {
  width: 100%;
  height: 300px;
}
.no-border { border-bottom: none !important; padding-left: 0 !important; padding-right: 0 !important; }

/* 底部左右布局 */
.content-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}
@media (max-width: 992px) { .content-grid { grid-template-columns: 1fr; } }

.grid-item {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #f0f2f5;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f2f5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.panel-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  display: flex;
  align-items: center;
}
.title-icon {
  color: #409eff;
  margin-right: 8px;
  font-size: 18px;
  background: rgba(64, 158, 255, 0.1);
  padding: 4px;
  border-radius: 6px;
  box-sizing: content-box;
}
.title-icon.warning { color: #e6a23c; background: rgba(230, 162, 60, 0.1); }
.info-icon { color: #909399; cursor: pointer; }

.panel-body { padding: 0; flex: 1; }

/* 待办列表优化 */
.todo-title { font-weight: 500; color: #303133; }
.project-text { color: #909399; font-size: 12px; }
.date-text { color: #909399; font-size: 13px; font-family: monospace; }
.text-danger { color: #f56c6c; font-weight: bold; }
.ml-2 { margin-left: 8px; }

/* 预警列表优化 */
.warning-list {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-height: 400px;
  overflow-y: auto;
}
.warning-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}
.warning-index {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  background: #f0f2f5;
  color: #909399;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  flex-shrink: 0;
  margin-top: 2px;
}
.idx-1 { background: #fee2e2; color: #f56c6c; }
.idx-2 { background: #fef0db; color: #e6a23c; }
.idx-3 { background: #ecf5ff; color: #409eff; }

.warning-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.warning-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}
.dept-name { font-weight: 500; color: #303133; }
.warning-actions { display: flex; align-items: center; gap: 10px; }
.dept-rate { font-size: 12px; }
.text-danger { color: #f56c6c; }
.text-warning { color: #e6a23c; }
.text-primary { color: #409eff; }

.panel-footer { padding: 0 20px 20px; }
.warning-tip {
  background-color: #fdf6ec;
  border-radius: 8px;
  padding: 12px;
  color: #e6a23c;
  font-size: 13px;
  line-height: 1.6;
  display: flex;
  gap: 10px;
}
.tip-icon { font-size: 18px; margin-top: 2px; }
.highlight { color: #f56c6c; font-weight: bold; padding: 0 4px; }
.tip-title { font-weight: bold; color: #d48806; }

/* 滚动条美化 */
.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #e0e3e9; border-radius: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
</style>