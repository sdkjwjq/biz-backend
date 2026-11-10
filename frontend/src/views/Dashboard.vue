<template>
  <div class="dashboard-fixed-container">
    <div v-if="loading" class="loading-mask">
      <div class="loadbox">
        <div class="loading-spinner"></div>
      </div>
    </div>

    <div class="head">
      <div class="showTime left-time">{{ timeStr }}</div>
      <h1>"双高建设计划"任务实时大屏</h1>
      <div class="head-btn-box">
        <div class="btn-group">
          <div class="dropdown-wrap" :class="{ active: activeType === ACTIVE_TYPES.YEAR }">
            <div class="nav-btn" @click="setType(ACTIVE_TYPES.YEAR)">
              {{ selectedYear }}年 <i class="arrow-down"></i>
            </div>
            <ul class="dropdown-list">
              <li v-for="year in YEAR_OPTIONS" :key="year"
                :class="{ on: selectedYear === year }" @click.stop="selectYear(year)">{{ year }}年</li>
            </ul>
          </div>
          <button class="nav-btn" :class="{ active: activeType === ACTIVE_TYPES.MID }" @click="setType(ACTIVE_TYPES.MID)">中期</button>
          <button class="nav-btn" :class="{ active: activeType === ACTIVE_TYPES.END }" @click="setType(ACTIVE_TYPES.END)">终期</button>
          <div class="divider"></div>
          <button class="nav-btn mission-btn" @click="goToWorks">任务中心</button>
        </div>
      </div>
    </div>

    <div class="mainbox">
      <ul class="clearfix h100">
        <li style="width: 30%">
          <div class="boxall" style="height: calc(62% - .15rem)">
            <div class="alltitle">归口部门建设任务情况</div>
            <div class="boxnav" ref="chartRef5"></div>
          </div>
          <div class="boxall" style="height: calc(30% - .15rem)">
            <div class="alltitle">建设增长趋势<span v-if="accessState.leadershipScope === 'DEPARTMENTS'">（分管部门，历史缺失不补算）</span></div>
            <div class="boxnav" ref="chartRef2"></div>
          </div>
        </li>

        <li style="width: 40%">
          <div class="boxall" style="height: calc(20% - .15rem)">
            <div class="row h100 display-flex">
              <div class="col-4 center-v">
                <div class="sqzs">
                  <p class="stat-label">建设任务总数</p>
                  <h1 class="performance-num">
                    <span class="num-text">{{ totalTaskCount }}</span>
                  </h1>
                </div>
              </div>
              <div class="col-4 center-v">
                <div class="sqzs">
                  <p class="stat-label">已完成任务数</p>
                  <h1 class="performance-num">
                    <span class="num-text">{{ completedTaskCount }}</span>
                  </h1>
                </div>
              </div>
              <div class="col-4 center-v">
                <div class="mini-pie-box">
                  <div class="tit01">任务完成度</div>
                  <div class="piebox" :ref="el => setPieRef(el, 0)"></div>
                </div>
              </div>
            </div>
          </div>
          <div class="boxall" style="height: calc(72% - .15rem)">
            <div class="alltitle">建设任务进度</div>
            <div class="boxnav" ref="chartRef4"></div>
          </div>
        </li>

        <li style="width: 30%">
          <div class="boxall" style="height: calc(62% - .15rem)">
            <div class="alltitle">建设成果情况</div>
            <div class="boxnav" ref="chartRef6"></div>
          </div>
          <div class="boxall" style="height: calc(30% - .15rem)">
            <div class="alltitle">建设成果展示</div>
            <div class="boxnav biaoge biaoge_list" style="width:100%; height:100%">
              <div class="biaoge_listIn">
                <ul class="ul_title">
                  <li style="width: 40%;">成果名称</li>
                  <li style="width: 20%;">级别</li>
                  <li style="width: 20%;">颁发时间</li>
                  <li style="width: 20%;">描述</li>
                </ul>
                <div class="ul_list" ref="representativeListScrollRef" @mouseenter="handleRepresentativeMouseEnter"
                  @mouseleave="handleRepresentativeMouseLeave">
                  <div class="ul_listIn" ref="representativeListInnerRef">
                    <ul v-for="(item, idx) in duplicatedRepresentativeList" :key="idx" class="ul_con"
                      :title="item.desc || item.title">
                      <li style="width: 40%;">{{ item.title }}</li>
                      <li style="width: 20%;">{{ item.level }}</li>
                      <li style="width: 20%;">{{ item.time }}</li>
                      <li style="width: 20%;">{{ item.desc }}</li>
                    </ul>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { accessState } from '@/utils/accessState';
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import * as echarts from 'echarts';
import { getAllAchievements } from '@/api/achievement';
import {
  getDeptTaskCompletionRates,
  getDeptYearTaskCompletionRates,
  getDeptMidTermTaskCompletionRates,
  getFirstLevelTaskDetails,
  getTrendData,
  getAllTaskDetails
} from '@/api/dashboard';
import { ElMessage } from 'element-plus';

const router = useRouter();
const loading = ref(true);
const timeStr = ref('');

const ACTIVE_TYPES = { YEAR: 'year', MID: 'mid', END: 'end' };
const YEAR_OPTIONS = ['2025', '2026', '2027', '2028', '2029'];
const MID_TERM_YEARS = [2025, 2026, 2027];
const END_TERM_YEARS = [2025, 2026, 2027, 2028, 2029];

// 静态字典配置提取
const AWARD_KEYS = {
  teDengJiang: '特等奖', yiDengJiang: '一等奖', erDengJiang: '二等奖',
  sanDengJiang: '三等奖', jinJiang: '金奖', yinJiang: '银奖',
  tongJiang: '铜奖', youShengJiang: '优胜奖', budDengDengCi: '不定等次'
};

const STANDARD_TASK_NAMES = [
  '打造一流区域型高端智库', '拓展国际交流与合作', '构建数字化教学新生态', '建设产教融合实训基地',
  '建设高水平双师队伍', '开发优质新形态教材', '建设一流核心课程', '打造高水平专业群', '创新产教融合机制', '落实立德树人根本任务'
];

const TASK_KEYWORDS = {
  '落实立德树人根本任务': ['三维联动', '立德树人新格局', '价值引领', '启智润心'],
  '创新产教融合机制': ['三机健全', '多方参与', '主动作为', '产教融合机制'],
  '打造高水平专业群': ['三需对接', '金专业', '匹配需求', '要素集聚'],
  '建设一流核心课程': ['三措并举', '金课程', '对接岗位', '数智融合'],
  '开发优质新形态教材': ['革故鼎新', '金教材', '双元开发', '形态多样'],
  '建设高水平双师队伍': ['卓越匠师', '金教师', '技艺精湛', '结构合理'],
  '建设产教融合实训基地': ['智造共享', '金基地', '场景真实', '开放融合'],
  '构建数字化教学新生态': ['数智赋能', '教学生态', '技术驱动', '数据变革'],
  '拓展国际交流与合作': ['文技出海', '国际化发展新模式', '教随产出', '校企同行'],
  '打造一流区域型高端智库': ['工程机械产业发展研究院', '区域型高端智库', '高端智库']
};

const STANDARD_DEPT_NAMES = [
  '党委办公室', '人事处', '党委宣传部', '学工处', '党委组织部', '工会', '国际教育学院',
  '国有资产管理处', '继续教育学院', '教务部', '科产部', '马克思主义学院', '团委', '现代教育技术中心', '网络思想政治工作中心'
];

const DEPT_ALIAS_MAP = [
  { keywords: ['党委办公室', '院长办公室'], target: '党委办公室' },
  { keywords: ['人事处', '党委教师工作部', '教师发展中心'], target: '人事处' },
  { keywords: ['党委宣传部', '网信办'], target: '党委宣传部' },
  { keywords: ['学工处', '党委学生工作部', '党委武装部', '大学生就业指导服务中心'], target: '学工处' },
  { keywords: ['党委组织部', '党委统战部', '党校'], target: '党委组织部' },
  { keywords: ['工会', '关工委办公室', '退休工作办公室'], target: '工会' },
  { keywords: ['国际教育学院', '外事办'], target: '国际教育学院' },
  { keywords: ['国有资产管理处', '国资', '国有资产'], target: '国有资产管理处' },
  { keywords: ['继续教育学院'], target: '继续教育学院' },
  { keywords: ['教务部', '创新创业学院', '招生办公室'], target: '教务部' },
  { keywords: ['科技与产业部', '科技与产业', '服务产业研究院', '科产部', '科产'], target: '科产部' },
  { keywords: ['马克思主义学院'], target: '马克思主义学院' },
  { keywords: ['团委'], target: '团委' },
  { keywords: ['现代教育技术中心', '现代教育技术'], target: '现代教育技术中心' },
  { keywords: ['网络思想政治工作中心', '网络思想政治工作'], target: '网络思想政治工作中心' }
];

const activeType = ref(ACTIVE_TYPES.YEAR);
const selectedYear = ref('2026');

// 通用工具方法
const normalizeApiList = (payload) => {
  if (Array.isArray(payload)) return payload;
  if (!payload || typeof payload !== 'object') return [];
  return payload.data || payload.list || [];
};

const parseYear = (value) => {
  const match = String(value || '').trim().match(/^\d{4}/);
  return match ? Number(match[0]) : null;
};

const getGotTime = (ach) => ach.gotTime || ach.got_time || '';
const getTaskName = (task) => task.taskName || task.task_name || '';

// 大屏数据状态
const totalTaskCount = ref(0);
const completedTaskCount = ref(0);
const taskCompletionRate = ref(0);
const deptStats = ref([]);
const firstLevelTasks = ref([]);
const trendData = ref([]);

let dataLoadVersion = 0;
const loadContext = () => ({ version: dataLoadVersion, type: activeType.value, year: Number(selectedYear.value) });
const isCurrentLoad = (context) => !disposed && context.version === dataLoadVersion
  && context.type === activeType.value && context.year === Number(selectedYear.value);

const isPhaseInSelectedRange = (phase, context = loadContext()) => {
  const year = parseYear(phase);
  if (!year) return false;
  if (context.type === ACTIVE_TYPES.YEAR) return year === context.year;
  if (context.type === ACTIVE_TYPES.MID) return MID_TERM_YEARS.includes(year);
  return END_TERM_YEARS.includes(year);
};

const setType = async (type) => {
  if (disposed) return;
  activeType.value = type;
  dataLoadVersion += 1;
  chartInitVersion += 1;
  const context = loadContext();
  await Promise.all([
    loadTaskStats(context),
    loadTrendData(type === ACTIVE_TYPES.YEAR ? context.year : null, context),
    loadAchievementSummary(context)
  ]);
  if (!isCurrentLoad(context)) return;
  await loadRepresentativeAchievements(selectedCategory.value, context);
  if (!isCurrentLoad(context)) return;
  loading.value = false;
  await nextTick();
  await initAllCharts(context);
};

const selectYear = async (year) => {
  selectedYear.value = year;
  activeType.value = ACTIVE_TYPES.YEAR;
  await setType(ACTIVE_TYPES.YEAR);
};

const goToWorks = () => router.push('/home/works');

const tableData = ref([
  { category: 1, name: '落实立德树人根本任务', gradeText: '' },
  { category: 2, name: '创新产教融合机制', gradeText: '' },
  { category: 3, name: '打造高水平专业群', gradeText: '' },
  { category: 4, name: '建设一流核心课程', gradeText: '' },
  { category: 5, name: '开放优质新形态教材', gradeText: '' },
  { category: 6, name: '建设高水平双师队伍', gradeText: '' },
  { category: 7, name: '建设产教融合实训基地', gradeText: '' },
  { category: 8, name: '构建数字化教学新生态', gradeText: '' },
  { category: 9, name: '拓展国际交流与合作', gradeText: '' },
  { category: 10, name: '打造一流区域型高端智库', gradeText: '' }
]);

const representativeList = ref([]);
const allAchievementsData = ref([]);
const selectedCategory = ref(null);

const duplicatedRepresentativeList = computed(() => {
  return representativeList.value.length <= 4 
    ? representativeList.value 
    : [...representativeList.value, ...representativeList.value];
});

// 优化：通过遍历 AWARD_KEYS 简化拼装逻辑
const buildGradeTextFromCounts = (counts) => {
  return Object.entries(AWARD_KEYS)
    .filter(([key]) => counts[key] > 0)
    .map(([key, label]) => `${label}×${counts[key]}`)
    .join('，');
};

const loadTaskStats = async (context = loadContext()) => {
  try {
    const tasksList = normalizeApiList(await getAllTaskDetails(requestConfig));
    if (!isCurrentLoad(context)) return;
    const thirdLevelTasks = tasksList.filter(task => 
      Number(task.level) === 3 && Number(task.isDelete) === 0 && isPhaseInSelectedRange(task.phase, context)
    );

    totalTaskCount.value = thirdLevelTasks.length;
    completedTaskCount.value = thirdLevelTasks.filter(t => String(t.status) === '3').length;
    taskCompletionRate.value = totalTaskCount.value > 0 ? Math.round((completedTaskCount.value / totalTaskCount.value) * 100) : 0;
  } catch (e) {
    if (!isCurrentLoad(context) || e?.name === 'AbortError') return;
    console.warn('加载看板汇总数据失败:', e?.message || e);
    totalTaskCount.value = completedTaskCount.value = taskCompletionRate.value = 0;
  }

  try {
    const fetchDeptStats = () => {
      if (context.type === ACTIVE_TYPES.YEAR) return getDeptYearTaskCompletionRates(context.year, requestConfig);
      if (context.type === ACTIVE_TYPES.MID) return getDeptMidTermTaskCompletionRates(2028, requestConfig);
      return getDeptTaskCompletionRates(requestConfig);
    };
    const data = await fetchDeptStats();
    if (!isCurrentLoad(context)) return;
    deptStats.value = normalizeApiList(data);
  } catch (e) {
    if (!isCurrentLoad(context) || e?.name === 'AbortError') return;
    console.warn('加载部门统计数据失败:', e?.message || e);
    deptStats.value = [];
  }

  try {
    const data = await getFirstLevelTaskDetails(requestConfig);
    if (!isCurrentLoad(context)) return;
    firstLevelTasks.value = normalizeApiList(data);
  } catch (e) {
    if (!isCurrentLoad(context) || e?.name === 'AbortError') return;
    console.warn('加载一级任务详情失败:', e?.message || e);
    firstLevelTasks.value = [];
  }
};

const loadTrendData = async (year, context = loadContext()) => {
  try {
    const data = await getTrendData(year, requestConfig);
    if (!isCurrentLoad(context)) return;
    trendData.value = normalizeApiList(data);
  } catch (e) {
    if (!isCurrentLoad(context) || e?.name === 'AbortError') return;
    console.warn('加载趋势数据失败:', e?.message || e);
    trendData.value = [];
  }
};

// 优化：合并过滤和数据装配逻辑，减少冗余判断
const loadRepresentativeAchievements = async (category = null, context = loadContext()) => {
  try {
    if (allAchievementsData.value.length === 0) {
      const data = await getAllAchievements(requestConfig);
      if (!isCurrentLoad(context)) return;
      allAchievementsData.value = normalizeApiList(data);
    }

    let filtered = allAchievementsData.value.filter(ach => {
      if (Number(ach.auditStatus ?? ach.audit_status ?? 30) !== 30) return false;
      if (!isPhaseInSelectedRange(getGotTime(ach), context)) return false;
      return category === null || Number(ach.category) === category;
    });

    // 优先国家级/省级，如果没有则取全部
    let repList = filtered.filter(ach => ach.level?.includes('国家级') || ach.level?.includes('省级'));
    if (repList.length === 0) repList = filtered;

    if (repList.length > 0) {
      representativeList.value = repList
        .sort((a, b) => getGotTime(b).localeCompare(getGotTime(a)))
        .slice(0, 8)
        .map(ach => ({
          title: ach.achName || ach.ach_name || '',
          level: ach.level || '',
          time: getGotTime(ach).split(' ')[0],
          desc: ach.comment || ach.achName || ach.ach_name || ''
        }));
    } else {
      const msgMap = { [ACTIVE_TYPES.YEAR]: '该年度无建设成果', [ACTIVE_TYPES.MID]: '中期内无建设成果' };
      representativeList.value = [{ title: msgMap[context.type] || '当前条件下无建设成果', level: '', time: '', desc: '' }];
    }

    nextTick(() => {
      if (!isCurrentLoad(context)) return;
      stopAutoScroll();
      scrollStartTimer = schedule(startAutoScroll, 100);
    });
  } catch (e) {
    if (!isCurrentLoad(context) || e?.name === 'AbortError') return;
    console.error('加载代表性成果失败:', e);
  }
};

// 优化：通过遍历 AWARD_KEYS 自动计算奖项数量，无需硬编码所有字段
const loadAchievementSummary = async (context = loadContext()) => {
  try {
    if (allAchievementsData.value.length === 0) {
      const data = await getAllAchievements(requestConfig);
      if (!isCurrentLoad(context)) return;
      allAchievementsData.value = normalizeApiList(data);
    }

    const achievements = allAchievementsData.value.filter(ach =>
      Number(ach.auditStatus ?? ach.audit_status ?? 30) === 30 && isPhaseInSelectedRange(getGotTime(ach), context)
    );
    const summaryMap = new Map();
    
    tableData.value.forEach(row => {
      const initCounts = { recordCount: 0 };
      Object.keys(AWARD_KEYS).forEach(k => initCounts[k] = 0);
      summaryMap.set(row.category, initCounts);
    });

    achievements.forEach(ach => {
      const cat = Number(ach.category);
      if (!summaryMap.has(cat)) return;
      const agg = summaryMap.get(cat);
      agg.recordCount += 1;

      Object.keys(AWARD_KEYS).forEach(key => {
        // 将驼峰转为下划线兼容取值
        const snakeKey = key.replace(/[A-Z]/g, letter => `_${letter.toLowerCase()}`);
        agg[key] += Number(ach[key] ?? ach[snakeKey] ?? 0);
      });
    });

    tableData.value = tableData.value.map(row => {
      const counts = summaryMap.get(row.category);
      return { ...row, gradeText: buildGradeTextFromCounts(counts), totalCount: counts.recordCount };
    });
  } catch (e) {
    if (!isCurrentLoad(context) || e?.name === 'AbortError') return;
    console.error('加载建设成效数据失败:', e);
    ElMessage.error(e?.message || '加载成效失败');
  }
};

const chartRef2 = ref(null);
const chartRef4 = ref(null);
const chartRef5 = ref(null);
const chartRef6 = ref(null);
const pieRefs = ref([]);
const setPieRef = (el, idx) => { if (el) pieRefs.value[idx] = el; };

const representativeListScrollRef = ref(null);
const representativeListInnerRef = ref(null);

let chartInstances = [];
let timers = [];
let scrollRafId = null;
let disposed = false;
let chartInitVersion = 0;
let scrollVersion = 0;
let scrollStartTimer = null;
let scrollMeasureTimer = null;
const requestController = new AbortController();
const requestConfig = { signal: requestController.signal };
const timeouts = new Set();
const schedule = (callback, delay) => {
  if (disposed) return null;
  const timer = setTimeout(() => {
    timeouts.delete(timer);
    if (!disposed) callback();
  }, delay);
  timeouts.add(timer);
  return timer;
};
const isHoveringRepresentative = ref(false);

const disposeChartInstances = () => {
  chartInstances.forEach(ins => ins?.dispose());
  chartInstances = [];
};

const initAllCharts = async (context = loadContext()) => {
  if (!isCurrentLoad(context)) return;
  const version = ++chartInitVersion;
  const allTasksList = normalizeApiList(await getAllTaskDetails(requestConfig).catch(() => []));
  if (!isCurrentLoad(context) || version !== chartInitVersion
      || ![chartRef2.value, chartRef4.value, chartRef5.value, chartRef6.value].every(el => el?.isConnected)) return;
  disposeChartInstances();
  const commonTextStyle = { color: 'rgba(255,255,255,0.7)', fontSize: 12 };
  const statusConfig = [
    { name: '逾期', color: '#F09EA0' }, { name: '未开始', color: '#909399' },
    { name: '进行中', color: '#6AACF9' }, { name: '审核中', color: '#FCEF44' }, { name: '已完成', color: '#A4F7B4' }
  ];

  // 1. 建设增长趋势
  const growthTrendChart = echarts.init(chartRef2.value);
  const trendSource = Array.isArray(trendData.value) ? trendData.value : [];
  // 时间轴按时间正序绘制，避免接口顺序异常时把趋势画反
  const tData = [...trendSource].sort((a, b) =>
    (Number(a.month) * 100 + Number(a.day)) - (Number(b.month) * 100 + Number(b.day)));
  
  growthTrendChart.setOption({
    tooltip: { trigger: 'axis', formatter: p => p.length ? `${p[0].axisValue}<br/>完成率：${p[0].data}%` : '' },
    legend: { data: ['任务完成率'], right: '4%', top: '2%', textStyle: { color: '#fff' } },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '15%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, axisLabel: commonTextStyle, data: tData.map(d => `${String(d.month).padStart(2, '0')}-${String(d.day).padStart(2, '0')}`) },
    yAxis: { type: 'value', min: 0, max: 100, splitLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } }, axisLabel: { ...commonTextStyle, formatter: '{value}%' } },
    series: [{ name: '任务完成率', type: 'line', smooth: true, itemStyle: { color: '#4facfe' }, areaStyle: { opacity: 0.1 }, data: tData.map(d => Number((d.completionRate ?? d.completion_rate ?? 0).toFixed?.(2) ?? 0)) }]
  });

  // 2. 任务进度
  const taskProgressChart = echarts.init(chartRef4.value);
  const allTasksById = new Map(allTasksList.map(t => [t.taskId || t.task_id, t]));

  const taskMapping = new Map(STANDARD_TASK_NAMES.map(sName => {
    let matched = (firstLevelTasks.value || []).find(t => getTaskName(t).includes(sName) || sName.includes(getTaskName(t)));
    if (!matched) matched = (firstLevelTasks.value || []).find(t => (TASK_KEYWORDS[sName] || []).some(kw => getTaskName(t).includes(kw)));
    return [sName, matched || null];
  }));

  const countThirdLevelTasks = (firstLevelTaskId) => {
    if (!firstLevelTaskId) return { total: 0, notStarted: 0, inProgress: 0, auditing: 0, completed: 0 };
    const validTasks = allTasksList.filter(t => Number(t.level) === 3 && isPhaseInSelectedRange(t.phase, context) && (
      (t.ancestors || '').split(',').includes(String(firstLevelTaskId)) || 
      (() => { let ct = t, d = 0; while (ct && d++ < 10) { let pid = ct.parentId || ct.parent_id; if (pid === firstLevelTaskId) return true; ct = allTasksById.get(pid); } return false; })()
    ));
    const counts = { total: validTasks.length, notStarted: 0, inProgress: 0, auditing: 0, completed: 0 };
    validTasks.forEach(t => { const s = String(t.status || '0'); if (s==='0') counts.notStarted++; else if (s==='1') counts.inProgress++; else if (s==='2') counts.auditing++; else if (s==='3') counts.completed++; });
    return counts;
  };

  const taskStatusCounts = STANDARD_TASK_NAMES.map(sName => countThirdLevelTasks(taskMapping.get(sName)?.taskId || taskMapping.get(sName)?.task_id));
  const taskProgressTotals = taskStatusCounts.map(c => c.total);

  taskProgressChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, formatter: (params) => {
      return params[0].name + '<br/>' + params.map(p => {
        const total = taskProgressTotals[p.dataIndex];
        return `${p.seriesName}: ${p.value} (${total > 0 ? ((p.value / total) * 100).toFixed(1) : 0}%)`;
      }).join('<br/>');
    }},
    grid: { left: '3%', right: '10%', bottom: '3%', top: '15%', containLabel: true },
    legend: { data: statusConfig.map(s => s.name), right: '5%', top: '2%', textStyle: { color: '#fff' } },
    xAxis: { type: 'value', show: false },
    yAxis: { type: 'category', data: STANDARD_TASK_NAMES, axisLabel: { color: '#fff', fontSize: 13 }, axisLine: { show: false } },
    series: [
      { name: '逾期', data: taskStatusCounts.map(() => 0) },
      { name: '未开始', data: taskStatusCounts.map(c => c.notStarted) },
      { name: '进行中', data: taskStatusCounts.map(c => c.inProgress) },
      { name: '审核中', data: taskStatusCounts.map(c => c.auditing) },
      { name: '已完成', data: taskStatusCounts.map(c => c.completed), itemStyle: { borderRadius: [0, 5, 5, 0] } }
    ].map((s, i) => ({ ...s, type: 'bar', stack: 'total', itemStyle: { ...s.itemStyle, color: statusConfig[i].color }, label: { show: true, position: 'inside', color: '#fff', fontSize: 11, formatter: p => (p.value > 0 && (p.value/taskProgressTotals[p.dataIndex]) > 0.05) ? `${((p.value/taskProgressTotals[p.dataIndex])*100).toFixed(1)}%` : '' } }))
    .concat([{ name: '总数', type: 'bar', stack: 'total', data: Array(taskProgressTotals.length).fill(0), itemStyle: { color: 'transparent' }, label: { show: true, position: 'right', color: '#fff', fontSize: 13, fontWeight: 'bold', formatter: p => taskProgressTotals[p.dataIndex] } }])
  });

  // 3. 部门任务情况
  const deptTaskChart = echarts.init(chartRef5.value);
  const mapDeptNameToStandard = (name) => {
    if (!name) return null;
    if (STANDARD_DEPT_NAMES.includes(name)) return name;
    const match = DEPT_ALIAS_MAP.find(a => a.keywords.some(kw => name.includes(kw)));
    return match ? match.target : null;
  };

  const deptAggMap = new Map(STANDARD_DEPT_NAMES.map(n => [n, { name: n, total: 0, notStarted: 0, inProgress: 0, auditing: 0, completed: 0 }]));
  (deptStats.value || []).forEach(dept => {
    const stdName = mapDeptNameToStandard(dept.deptName || '');
    if (stdName && deptAggMap.has(stdName)) {
      const agg = deptAggMap.get(stdName);
      agg.total += dept.totalTasks || 0; agg.notStarted += dept.notStartedCount || 0;
      agg.inProgress += dept.inProgressCount || 0; agg.auditing += dept.inReviewCount || 0; agg.completed += dept.finishedCount || 0;
    }
  });

  const validDepts = Array.from(deptAggMap.values()).filter(a => a.total > 0);
  const deptNames = validDepts.map(d => d.name);
  
  deptTaskChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: statusConfig.map(s => s.name), right: '4%', top: '0', textStyle: { color: '#fff' } },
    grid: { left: '3%', right: '4%', bottom: '10%', top: '15%', containLabel: true },
    xAxis: { type: 'category', data: deptNames, axisLabel: { color: '#fff', interval: 0, formatter: v => String(v||'').split('').join('\n'), fontSize: 10, lineHeight: 12 } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } }, axisLabel: { color: '#fff' } },
    series: [
      { name: '逾期', data: validDepts.map(() => 0) },
      { name: '未开始', data: validDepts.map(d => d.notStarted) },
      { name: '进行中', data: validDepts.map(d => d.inProgress) },
      { name: '审核中', data: validDepts.map(d => d.auditing) },
      { name: '已完成', data: validDepts.map(d => d.completed) }
    ].map((s, i) => ({ ...s, type: 'bar', stack: 'dept', itemStyle: { color: statusConfig[i].color } }))
  });

  // 4. 任务完成度圆环
  const completionRate = Math.round(taskCompletionRate.value || 0);
  if (pieRefs.value[0]?.isConnected) {
    const pieChart = echarts.init(pieRefs.value[0]);
    pieChart.setOption({
      title: { text: completionRate + '%', left: 'center', top: 'center', textStyle: { color: '#fff', fontSize: 16, fontWeight: 'bold' } },
      series: [{
        type: 'pie', radius: ['60%', '85%'], center: ['50%', '50%'], label: { show: false }, emphasis: { scale: false },
        data: [
          { value: completionRate, itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#4facfe' }, { offset: 1, color: '#00f2fe' }]) } },
          { value: 100 - completionRate, itemStyle: { color: 'rgba(255,255,255,0.05)' } }
        ]
      }]
    });
    chartInstances.push(pieChart);
  }

  // 5. 建设成果情况柱状图
  const achievementChart = echarts.init(chartRef6.value);
  const achievementCounts = tableData.value.map(row => row.totalCount || 0);
  
  achievementChart.setOption({
    title: achievementCounts.some(v => v > 0) ? { show: false } : { text: activeType.value === ACTIVE_TYPES.YEAR ? '该年度无建设成果' : activeType.value === ACTIVE_TYPES.MID ? '中期内无建设成果' : '当前条件下无建设成果', left: 'center', top: 'middle', textStyle: { color: 'rgba(255,255,255,0.7)', fontSize: 14 } },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, formatter: p => `${p[0].name}<br/>成果数量: ${p[0].value}` },
    grid: { left: '15%', right: '5%', bottom: '15%', top: '10%', containLabel: false },
    xAxis: { type: 'category', data: tableData.value.map(row => row.name), axisLabel: { color: '#fff', fontSize: 11, rotate: 45, interval: 0, formatter: v => v?.length > 8 ? v.slice(0, 8) + '...' : v }, axisLine: { lineStyle: { color: 'rgba(255,255,255,0.3)' } } },
    yAxis: { type: 'value', axisLabel: { color: '#fff', fontSize: 11 }, axisLine: { lineStyle: { color: 'rgba(255,255,255,0.3)' } }, splitLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } } },
    series: [{
      name: '成果数量', type: 'bar', data: achievementCounts, barWidth: '50%',
      itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#4facfe' }, { offset: 1, color: '#00f2fe' }]) },
      label: { show: true, position: 'top', color: '#fff', fontSize: 11, formatter: p => p.value > 0 ? p.value : '' },
      emphasis: { itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#6bb5ff' }, { offset: 1, color: '#1ae5ff' }]) } }
    }]
  });

  achievementChart.off('click');
  achievementChart.on('click', async (params) => {
    if (!params.name) return;
    const clickedRow = tableData.value.find(row => row.name === params.name);
    if (clickedRow) {
      selectedCategory.value = selectedCategory.value === clickedRow.category ? null : clickedRow.category;
      await loadRepresentativeAchievements(selectedCategory.value);
    }
  });

  chartInstances.push(growthTrendChart, taskProgressChart, deptTaskChart, achievementChart);
};

const handleResize = () => {
  if (disposed) return;
  document.documentElement.style.fontSize = (document.documentElement.clientWidth / 1920) * 100 + 'px';
  chartInstances.forEach(ins => ins?.resize());
};

const initClock = () => {
  const pad = (n) => (n < 10 ? '0' + n : n);
  const update = () => {
    const dt = new Date();
    timeStr.value = `${dt.getFullYear()}/${pad(dt.getMonth() + 1)}/${pad(dt.getDate())} ${pad(dt.getHours())}:${pad(dt.getMinutes())}:${pad(dt.getSeconds())}`;
  };
  update();
  timers.push(setInterval(update, 1000));
};

const startAutoScroll = () => {
  if (disposed) return;
  const version = scrollVersion;
  const wrapper = representativeListScrollRef.value;
  const inner = representativeListInnerRef.value;
  if (!wrapper?.isConnected || !inner?.isConnected || representativeList.value.length <= 4) return;

  inner.style.transition = 'none';
  inner.style.transform = 'translateY(0px)';

  nextTick(() => {
    if (disposed || version !== scrollVersion) return;
    scrollMeasureTimer = schedule(() => {
      if (!wrapper.isConnected || !inner.isConnected || version !== scrollVersion) return;
      const itemHeight = inner.querySelector('.ul_con')?.offsetHeight; 
      if (!itemHeight) return;
      
      const totalHeight = itemHeight * representativeList.value.length; 
      let translateY = 0;

      const step = () => {
        if (disposed || version !== scrollVersion || !wrapper.isConnected || !inner.isConnected) return stopAutoScroll();
        if (!isHoveringRepresentative.value) {
          translateY -= 0.2; 
          if (Math.abs(translateY) >= totalHeight) translateY = 0;
          inner.style.transition = 'none';
          inner.style.transform = `translateY(${translateY}px)`;
        }
        scrollRafId = requestAnimationFrame(step);
      };
      if (!scrollRafId) scrollRafId = requestAnimationFrame(step);
    }, 200);
  });
};

const stopAutoScroll = () => {
  scrollVersion += 1;
  [scrollStartTimer, scrollMeasureTimer].forEach(timer => {
    clearTimeout(timer);
    timeouts.delete(timer);
  });
  scrollStartTimer = scrollMeasureTimer = null;
  if (scrollRafId) { cancelAnimationFrame(scrollRafId); scrollRafId = null; }
};
const handleRepresentativeMouseEnter = () => isHoveringRepresentative.value = true;
const handleRepresentativeMouseLeave = () => isHoveringRepresentative.value = false;

onMounted(async () => {
  handleResize();
  window.addEventListener('resize', handleResize);
  initClock();

  const context = loadContext();
  try {
    await Promise.all([loadTaskStats(context), loadTrendData(context.year, context), loadAchievementSummary(context)]);
    if (!isCurrentLoad(context)) return;
    await loadRepresentativeAchievements(null, context);
  } catch (e) {
    if (!isCurrentLoad(context) || e?.name === 'AbortError') return;
    console.error('加载大屏数据失败:', e);
  }

  schedule(async () => {
    if (!isCurrentLoad(context)) return;
    loading.value = false;
    await nextTick();
    await initAllCharts(context);
    if (!isCurrentLoad(context)) return;
    stopAutoScroll();
    scrollStartTimer = schedule(startAutoScroll, 100);
  }, 800);
});

onUnmounted(() => {
  disposed = true;
  chartInitVersion += 1;
  requestController.abort();
  timeouts.forEach(clearTimeout);
  timeouts.clear();
  document.documentElement.style.fontSize = '';
  window.removeEventListener('resize', handleResize);
  timers.forEach(clearInterval);
  disposeChartInstances();
  stopAutoScroll();
});
</script>

<style scoped>
@font-face {
  font-family: 'electronicFont';
  src: url("@/assets/font/DS-DIGIT.TTF");
}

.dashboard-fixed-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 2500;
  background: #000d4a url("@/assets/image/bg.jpg") no-repeat center center;
  background-size: cover;
  color: #fff;
  font-family: "微软雅黑", sans-serif;
  overflow: hidden;
  box-sizing: border-box;
}

.head {
  height: 1.05rem;
  background: url("@/assets/image/head_bg.png") no-repeat center center;
  background-size: 100% 100%;
  text-align: center;
  position: relative;
  z-index: 3000;
}

.head h1 {
  font-size: 0.38rem;
  line-height: 0.8rem;
  margin: 0;
  font-weight: bold;
  background: linear-gradient(to bottom, #fff 50%, #4facfe 100%);
  -webkit-background-clip: text;
  color: transparent;
}

.showTime.left-time {
  position: absolute;
  left: 0.3rem;
  top: 0;
  line-height: 0.8rem;
  font-family: 'electronicFont';
  font-size: 0.28rem;
  color: rgba(255, 255, 255, 0.7);
}

.head-btn-box {
  position: absolute;
  right: 0.3rem;
  top: 0;
  height: 0.8rem;
  display: flex;
  align-items: center;
}

.btn-group {
  display: flex;
  align-items: center;
  gap: 0.12rem;
}

.nav-btn {
  background: rgba(30, 45, 80, 0.8);
  border: 1px solid rgba(79, 172, 254, 0.3);
  color: #c9d6f0;
  padding: 0.06rem 0.18rem;
  border-radius: 2px;
  cursor: pointer;
  font-size: 0.14rem;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0.85rem;
  justify-content: center;
}

.nav-btn.active,
.dropdown-wrap.active .nav-btn {
  background: #60a5fa !important;
  border-color: #60a5fa;
  color: #fff;
  font-weight: bold;
}

.divider {
  width: 1px;
  height: 0.18rem;
  background: rgba(255, 255, 255, 0.2);
  margin: 0 0.05rem;
}

.dropdown-wrap {
  position: relative;
}

.dropdown-list {
  position: absolute;
  top: 100%;
  left: 0;
  width: 100%;
  background: #06215d;
  border: 1px solid #4facfe;
  border-radius: 2px;
  display: none;
  z-index: 9999;
  list-style: none;
  padding: 4px 0;
  margin: 0;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.5);
}

.dropdown-wrap:hover .dropdown-list {
  display: block;
}

.dropdown-list li {
  padding: 8px 0;
  font-size: 0.13rem;
  cursor: pointer;
  text-align: center;
  color: #fff;
  transition: background 0.2s;
}

.dropdown-list li:hover,
.dropdown-list li.on {
  background: #4facfe;
}

.mainbox {
  padding: 0.1rem 0.1rem;
  height: calc(100vh - 1.05rem);
  box-sizing: border-box;
}

.mainbox>ul {
  list-style: none;
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
}

.mainbox>ul>li {
  float: left;
  padding: 0 0.1rem;
  height: 100%;
  box-sizing: border-box;
}

.boxall {
  background: rgba(6, 48, 109, 0.3);
  margin-bottom: 0.15rem;
  padding: 0.15rem;
  position: relative;
  border: 1px solid rgba(73, 188, 247, 0.2);
  box-shadow: inset 0 0 15px rgba(79, 172, 254, 0.1);
}

.boxall::before {
  content: "";
  position: absolute;
  width: 10px;
  height: 10px;
  border-top: 2px solid #02a6b5;
  border-left: 2px solid #02a6b5;
  top: -1px;
  left: -1px;
}

.boxall::after {
  content: "";
  position: absolute;
  width: 10px;
  height: 10px;
  border-bottom: 2px solid #02a6b5;
  border-right: 2px solid #02a6b5;
  bottom: -1px;
  right: -1px;
}

.alltitle {
  font-size: 0.18rem;
  color: #fff;
  line-height: 0.3rem;
  padding-left: 0.15rem;
  border-left: 4px solid #4facfe;
  margin-bottom: 0.12rem;
  font-weight: bold;
}

.boxnav {
  height: calc(100% - 0.45rem);
  width: 100%;
}

.num-active {
  color: #fef000 !important;
  font-weight: bold;
  font-family: 'electronicFont';
  font-size: 0.16rem;
}

.mini-pie-box {
  text-align: center;
  padding: 0.1rem;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: center;
  height: 100%;
}

.tit01 {
  font-size: 0.24rem;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 0.1rem;
  margin-top: 0;
  padding: 0;
  text-align: center;
  font-weight: bold;
  letter-spacing: 2px;
  display: block;
}

.piebox {
  width: 100%;
  height: 0.72rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.scroll-table {
  overflow-y: auto;
}

table {
  width: 100%;
  text-align: center;
  border-collapse: collapse;
  table-layout: fixed;
}

table th {
  background: rgba(79, 172, 254, 0.2);
  padding: 0.1rem;
  font-size: 0.14rem;
  color: #4facfe;
}

table td,
table th {
  border: 1px solid rgba(255, 255, 255, 0.1);
}

table td {
  padding: 0.08rem;
  font-size: 0.13rem;
}

.up {
  color: #ff4b4b;
  font-weight: bold;
}

.down {
  color: #00ff00;
  font-weight: bold;
}

.scroll-table {
  scrollbar-width: thin;
  scrollbar-color: #4facfe transparent;
}

.scroll-table::-webkit-scrollbar {
  width: 6px;
}

.scroll-table::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.2);
}

.scroll-table::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, #4facfe, #00f2fe);
  border-radius: 4px;
}

.scroll-table::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(180deg, #60a5fa, #38bdf8);
}

.biaoge_list {
  overflow-y: auto;
  height: 100%;
}

.biaoge_listIn {
  width: 100%;
  height: 100%;
}

.ul_title {
  display: flex;
  list-style: none;
  margin: 0;
  padding: 0;
  background: rgba(79, 172, 254, 0.2);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.ul_title li {
  padding: 0.1rem;
  font-size: 0.14rem;
  color: #4facfe;
  text-align: center;
  font-weight: bold;
}

.ul_list {
  height: 1.6rem;
  overflow: hidden;
  position: relative;
}

.ul_listIn {
  width: 100%;
}

.ul_con {
  display: flex;
  list-style: none;
  margin: 0;
  padding: 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  transition: background 0.2s;
  height: 0.4rem;
  min-height: 0.4rem;
  align-items: center;
}

.ul_con:hover {
  background: rgba(79, 172, 254, 0.1);
}

.ul_con li {
  padding: 0.08rem;
  font-size: 0.13rem;
  color: rgba(255, 255, 255, 0.9);
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.biaoge_list::-webkit-scrollbar {
  width: 6px;
}

.biaoge_list::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.2);
}

.biaoge_list::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, #4facfe, #00f2fe);
  border-radius: 4px;
}

.biaoge_list::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(180deg, #60a5fa, #38bdf8);
}

.loading-mask {
  position: absolute;
  inset: 0;
  background: #000d4a;
  z-index: 9999;
  display: flex;
  justify-content: center;
  align-items: center;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 3px solid rgba(79, 172, 254, 0.3);
  border-top-color: #4facfe;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.row {
  display: flex;
  width: 100%;
}

.col-4 {
  width: 33.33%;
}

.col-5 {
  width: 45%;
}

.col-7 {
  width: 55%;
}

.display-flex {
  display: flex;
  align-items: center;
  justify-content: center;
}

.clearfix:after {
  content: "";
  display: table;
  clear: both;
}

.h100 {
  height: 100%;
}

.sqzs {
  text-align: center;
  padding: 0.1rem;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: center;
  height: 100%;
}

.stat-label {
  font-size: 0.24rem;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 0.1rem;
  margin-top: 0;
  padding: 0;
  letter-spacing: 2px;
  font-weight: bold;
  display: block;
}

.performance-num {
  margin: 0;
  display: flex;
  align-items: baseline;
  justify-content: center;
  color: #fef000;
  text-shadow: 0 0 20px rgba(254, 240, 0, 0.6);
}

.num-text {
  font-size: 0.72rem;
  font-family: 'electronicFont';
  font-weight: normal;
  line-height: 1;
}

.center-v {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: center;
  height: 100%;
}
</style>
