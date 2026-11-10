<template>
  <div class="common-layout">
    <!-- 左侧侧边栏 -->
    <aside class="sidebar" :class="{ 'collapsed': isCollapse }">
      <!-- Logo -->
      <div class="logo-container">
        <img src="@/assets/image/xzcit.jpg" alt="Logo" class="logo-image" />
        <span class="logo-text" v-show="!isCollapse">“双高建设计划”项目平台</span>
      </div>

      <!-- 菜单区域 -->
      <el-menu active-text-color="#fff" background-color="#001529" class="side-menu" :default-active="activeMenu"
        text-color="rgba(255,255,255,0.65)" :collapse="isCollapse" :collapse-transition="false" router
        @select="handleMenuSelect">
        <!-- 开启 router 模式，index 直接填写路由路径 -->
        <el-menu-item v-if="accessState.canViewDashboard" index="/dashboard">
          <el-icon>
            <Odometer />
          </el-icon>
          <template #title>数据大屏</template>
        </el-menu-item>

        <el-sub-menu index="works-submenu">
          <template #title>
          <el-icon>
            <Briefcase />
          </el-icon>
            <span>工作中心 (我的任务)</span>
          </template>
          <!-- “任务”二级菜单路由改为现有的 /home/works -->
          <el-menu-item v-if="canUsePermission('tasks.read', !isDepartmentAchievementAccount)" index="/home/works">任务</el-menu-item>
          <el-menu-item v-if="canUsePermission('performance.read', !isDepartmentAchievementAccount)" index="/home/works/performance">绩效</el-menu-item>
          <el-menu-item v-if="canAccessAchievementMenu" index="/home/works/achievement">成果</el-menu-item>
          <el-menu-item v-if="canAccessBudgetMenu" index="/home/works/budget">预算</el-menu-item>
          <el-menu-item v-if="canUsePermission('records.read', canAccessWorkRecords)" index="/home/work-records">工作纪实</el-menu-item>
          <el-menu-item v-if="!isExpertMode&&accessState.canViewCoreIndicators" index="/home/core-indicators">核心指标</el-menu-item>
        </el-sub-menu>

        <el-menu-item v-if="accessState.historyAvailable && (!accessState.isAdmin || isExpertMode)" index="/review-history">
          <el-icon><Stamp /></el-icon><template #title>历史评审</template>
        </el-menu-item>
        <el-menu-item v-if="canUsePermission('messages.read', !isDepartmentAchievementAccount)" index="/home/notice">
          <el-icon>
            <Bell />
          </el-icon>
          <template #title>
            <span class="menu-title-wrapper">
              <span class="menu-title">消息中心</span>
              <el-badge v-if="unreadCount > 0" :value="unreadCount" class="menu-badge" />
            </span>
          </template>
        </el-menu-item>

        <el-menu-item v-if="!isExpertMode && !isDepartmentAchievementAccount && accessState.canViewAuditCenter" index="/home/audit">
          <el-icon>
            <Stamp />
          </el-icon>
          <template #title>
            <span class="menu-title-wrapper">
              <span class="menu-title">审核中心</span>
              <el-badge v-if="todoAuditCount > 0" :value="todoAuditCount" class="menu-badge" />
            </span>
          </template>
        </el-menu-item>

        <el-menu-item index="/home/data" v-if="false">
          <el-icon>
            <DataLine />
          </el-icon>
          <template #title>数据中心</template>
        </el-menu-item>

        <el-menu-item index="/home/query" v-if="false">
          <el-icon>
            <Search />
          </el-icon>
          <template #title>查询中心</template>
        </el-menu-item>

        <el-menu-item v-if="!isExpertMode && hasPermission('tasks.supervise')" index="/home/report">
          <el-icon>
            <Upload />
          </el-icon>
          <template #title>督办中心</template>
        </el-menu-item>

        <el-sub-menu index="admin-submenu" v-if="!isExpertMode&&accessState.canViewOverview">
          <template #title>
            <el-icon>
              <Setting />
            </el-icon>
            <span>管理中心</span>
          </template>
          <el-menu-item index="/home/admin/overview">管理概览</el-menu-item>
          <el-menu-item v-if="accessState.isAdmin" index="/home/admin/accounts">账号与组织</el-menu-item>
          <el-menu-item v-if="accessState.isAdmin" index="/home/admin/notices">通知管理</el-menu-item>
          <el-menu-item v-if="accessState.isAdmin" index="/home/admin/permissions">角色与权限</el-menu-item>
          <el-menu-item v-if="accessState.isAdmin" index="/home/admin/review-tasks">评审任务管理</el-menu-item>
          <el-menu-item v-if="accessState.isAdmin" index="/home/admin/operations">操作记录</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </aside>

    <!-- 右侧主体内容 -->
    <div class="main-container">
      <!-- 顶部 Header -->
      <header class="top-header">
        <!-- 左侧：折叠按钮 + 面包屑 -->
        <div class="header-left">
          <el-icon class="fold-btn" @click="toggleCollapse">
            <Expand v-if="isCollapse" />
            <Fold v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">管理后台</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <!-- 右侧：功能区 -->
        <div class="header-right">
          <!-- 点击铃铛跳转到消息页 -->
          <div v-if="!isExpertMode && !isDepartmentAchievementAccount" class="header-action-item" @click="goToNotice" :class="{ 'loading-state': noticeLoading }">
            <!-- 登录后立即显示红点，加载中显示占位红点，加载完成后显示真实数量 -->
            <el-badge v-if="unreadCount > 0 || noticeLoading" :value="noticeLoading ? undefined : unreadCount" :is-dot="noticeLoading" class="badge-item">
              <el-icon class="bell-icon" :class="{ 'bell-loading': noticeLoading }">
                <Bell />
              </el-icon>
            </el-badge>
            <el-icon v-else class="bell-icon">
              <Bell />
            </el-icon>
          </div>

          <el-tag v-if="isExpertMode" size="small" effect="plain">专家评审</el-tag>
          <el-dropdown trigger="click" class="user-dropdown">
            <div class="user-trigger">
              <div class="user-avatar">{{ userAvatarText }}</div>
              <span class="user-name">{{ currentUserName }}</span>
              <el-icon class="el-icon--right">
                <ArrowDown />
              </el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-if="accessState.expertAvailable||isExpertMode&&accessState.businessAvailable" @click="$router.push('/choose-role')">切换使用角色</el-dropdown-item>
                <el-dropdown-item @click="openPasswordDialog">修改密码</el-dropdown-item>
                <el-dropdown-item divided :disabled="loggingOut" @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 内容滚动区域：使用 Router View -->
      <main class="content-wrapper">
        <router-view v-if="!passwordState.required" v-slot="{ Component }">
            <!-- 给子页面绑定自定义事件，用于同步未读消息数量 -->
            <component :is="Component" @update-unread="handleUpdateUnread" @audit-completed="loadTodoAuditCount(true)" />
        </router-view>
      </main>
    </div>

    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="420px" append-to-body>
      <el-form label-position="top" class="password-form" @submit.prevent>
        <el-form-item label="新密码" :error="passwordErrors.newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码"
            autocomplete="new-password" />
        </el-form-item>
        <el-form-item label="确认密码" :error="passwordErrors.confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码"
            autocomplete="new-password" @keyup.enter="handleChangePassword" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="passwordSubmitting" @click="handleChangePassword">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { clearAdminTest } from '@/utils/adminTest';
import { createRefreshQueue } from '@/utils/refreshQueue';
import { ref, reactive, computed, watch, onMounted, onUnmounted } from 'vue';
import { accessState, canUsePermission as businessPermission, hasPermission, resetCurrentAccess } from '@/utils/accessState';
import { navigateToLogout } from '@/utils/logoutNavigation';
import { getSsoStatus } from '@/api/user';
import { useRoute, useRouter } from 'vue-router'; // 引入 useRouter
// 引入图标
import {
  Odometer, Briefcase, Stamp, DataLine, Search, Upload, Setting,
  Fold, Expand, Bell, ArrowDown
} from '@element-plus/icons-vue';
import { logout, getAllUsers, changePassword } from '@/api/user';
import { getUnreadNoticeCount } from '@/api/notice';
import { createWorksCache, clearWorksCache } from '@/utils/worksCache';
import { passwordState, PASSWORD_RULE, isPasswordValid } from '@/utils/passwordPolicy';
import { getTodoAuditCount } from '@/api/audit';
import { getTodoPerformanceAuditCount } from '@/api/performance';
import { getTodoAchievementAuditCount } from '@/api/achievement';
import { getTodoCoreAuditCount } from '@/api/coreIndicators';
import { getWorkRecordCapabilities } from '@/api/workRecords';
import { ElMessage, ElMessageBox } from 'element-plus';

const isExpertMode=computed(()=>accessState.experience==='EXPERT');
const canUsePermission=(code,legacy=true)=>isExpertMode.value?code==='tasks.read':businessPermission(code,legacy);
const route = useRoute();
const router = useRouter(); // 获取 router 实例
const isCollapse = ref(false);
const unreadCount = ref(0); // 未读消息数量
const noticeLoading = ref(false); // 消息加载状态
const todoAuditCount = ref(0); // 待审核任务数量
const auditLoading = ref(false); // 审核任务加载状态
const passwordDialogVisible = ref(false);
const passwordSubmitting = ref(false);
const passwordForm = reactive({
  newPassword: '',
  confirmPassword: '',
});
const passwordErrors = reactive({
  newPassword: '',
  confirmPassword: '',
});

// --- 当前用户信息（右上角显示） ---
const currentUserName = ref(''); // 姓名（昵称/用户名）

const parseJwtPayload = (token) => {
  try {
    if (!token || typeof token !== 'string') return null;
    const parts = token.split('.');
    if (parts.length < 2) return null;
    let payload = parts[1].replace(/-/g, '+').replace(/_/g, '/');
    const pad = payload.length % 4;
    if (pad) payload += '='.repeat(4 - pad);
    // JWT 的 JSON 使用 UTF-8；atob 返回的是字节串，不能直接当作中文文本。
    const bytes = Uint8Array.from(atob(payload), char => char.charCodeAt(0));
    const json = new TextDecoder('utf-8', { fatal: true }).decode(bytes);
    return JSON.parse(json);
  } catch (e) { // eslint-disable-line no-unused-vars
    return null;
  }
};

const userAvatarText = computed(() => {
  const name = String(currentUserName.value || '').trim();
  return name ? name.charAt(0) : '用';
});

const currentRole = computed(() => {
  if (accessState.token === localStorage.getItem('token')) return accessState.role;
  const token = localStorage.getItem('token') || '';
  const payload = parseJwtPayload(token);
  return payload?.role !== undefined && payload?.role !== null ? String(payload.role) : '';
});

const isDepartmentAchievementAccount = computed(() => {
  const token = localStorage.getItem('token') || '';
  const payload = parseJwtPayload(token);
  return !accessState.isLeader && payload?.isDepartmentAccount === true;
});
const canAccessAchievementMenu = computed(() => {
  const token = localStorage.getItem('token') || '';
  const payload = parseJwtPayload(token);
  return canUsePermission('achievements.read', accessState.canViewAchievement || payload?.canViewAchievement === true);
});
const canAccessBudgetMenu = computed(() => canUsePermission('budget.read', !isDepartmentAchievementAccount.value && (accessState.canViewBudget || ['0', '2'].includes(currentRole.value))));

// 读取缓存的辅助函数
const worksCache = createWorksCache();
let homeDisposed = false;
const canAccessWorkRecords = ref(false);
let workRecordAccessVersion = 0;
let workRecordAccessController;
async function loadWorkRecordAccess() {
  const version = ++workRecordAccessVersion;
  workRecordAccessController?.abort();
  canAccessWorkRecords.value = false;
  if (isExpertMode.value || passwordState.required || !isCurrentHome()) return;
  workRecordAccessController = new AbortController();
  try {
    const access = await getWorkRecordCapabilities({ signal: workRecordAccessController.signal });
    if (isCurrentHome() && version === workRecordAccessVersion) {
      canAccessWorkRecords.value = access.canCreate || access.canViewAll || access.canViewOwnRecords;
    }
  } catch { /* 权限读取失败时保留原有菜单，不展示纪实入口。 */ }
}
watch(() => passwordState.required, loadWorkRecordAccess);
let countRefreshTimer = null;
const isCurrentHome = () => !homeDisposed && worksCache.isCurrent();
const readJsonCache = (key) => {
  try {
    const raw = worksCache.getItem(key);
    if (!raw) return null;
    return JSON.parse(raw);
  } catch (e) { // eslint-disable-line no-unused-vars
    return null;
  }
};

// 处理用户名的编码问题
const fixUserNameEncoding = (name) => {
  if (!name || typeof name !== 'string') return name;
  try {
    // 移除可能的BOM标记
    let fixed = name.replace(/^\uFEFF/, '').trim();
    if (!fixed) return name;
    
    // 检查是否包含乱码字符（替换字符）
    if (fixed.includes('\uFFFD') || /[\uFFFD]/.test(fixed)) {
      // 尝试修复编码
      try {
        // 先尝试 UTF-8 解码
        fixed = decodeURIComponent(encodeURIComponent(fixed));
      } catch (e) { // eslint-disable-line no-unused-vars
        // 如果修复失败，返回原始值
      }
    }
    
    // 如果修复后的字符串仍然看起来像乱码（包含非中文字符且长度异常），尝试其他方法
    // 但不要过度处理，避免破坏正常的中文字符
    
    return fixed;
  } catch (e) { // eslint-disable-line no-unused-vars
    return name;
  }
};

const loadCurrentUserName = async () => {
  if(accessState.currentUser){currentUserName.value=accessState.currentUser.nickName||accessState.currentUser.userName;return;}
  if(isExpertMode.value){currentUserName.value=parseJwtPayload(localStorage.getItem('token'))?.username||'用户';return;}
  const token = localStorage.getItem('token') || '';
  const payload = parseJwtPayload(token);
  const userId = payload?.id !== undefined && payload?.id !== null ? Number(payload.id) : null;
  if (!userId || Number.isNaN(userId)) {
    currentUserName.value = '用户';
    return;
  }
  
  // 策略1：优先尝试从 /system/allUsers 接口获取当前用户信息（如果权限允许）
  try {
    const users = await getAllUsers();
    if (Array.isArray(users)) {
      const me = users.find(u => {
        const uid = u?.userId ?? u?.user_id;
        return uid !== undefined && uid !== null && Number(uid) === Number(userId);
      });
      if (me) {
        // 优先使用 nickName（姓名），其次 userName（账号）
        let name = me?.nickName ?? me?.nick_name ?? me?.userName ?? me?.user_name;
        if (name) {
          name = fixUserNameEncoding(name);
          if (name && name.trim() !== '') {
            currentUserName.value = name;
            return; // 成功获取，直接返回
          }
        }
      }
    }
  } catch (e) { // eslint-disable-line no-unused-vars
    // 如果接口调用失败（可能是权限不足），继续尝试其他方法
    // 不打印错误日志，因为 403 是预期行为
  }
  
  // 策略2：从缓存中获取用户信息（如果之前有管理员权限访问过）
  try {
    const cachedUsers = readJsonCache('works_users_cache_v1');
    if (Array.isArray(cachedUsers)) {
      const me = cachedUsers.find(u => {
        const uid = u?.userId ?? u?.user_id;
        return uid !== undefined && uid !== null && Number(uid) === Number(userId);
      });
      if (me) {
        // 优先使用 nickName（姓名），其次 userName（账号）
        let name = me?.nickName ?? me?.nick_name ?? me?.userName ?? me?.user_name;
        if (name) {
          name = fixUserNameEncoding(name);
          if (name && name.trim() !== '') {
            currentUserName.value = name;
            return; // 成功获取，直接返回
          }
        }
      }
    }
  } catch (e) { // eslint-disable-line no-unused-vars
    // 忽略缓存读取错误
  }
  
  // 策略3：使用 JWT token 中的 username（账号）作为最后备选
  const tokenUsername = payload?.username;
  if (tokenUsername) {
    const name = fixUserNameEncoding(String(tokenUsername));
    if (name && name.trim() !== '') {
      currentUserName.value = name;
    } else {
      currentUserName.value = `用户${userId}`;
    }
  } else {
    currentUserName.value = `用户${userId}`;
  }
};

// 当前激活的菜单项 (对应路由路径)
const activeMenu = computed(() => route.path==='/home/works'&&route.query.reviewView==='history'?'/review-history':route.path);

// 动态面包屑标题
const currentTitle = computed(() => {
  // 优先使用路由元数据中的 title
  return isExpertMode.value?'专家评审 / 任务':route.meta.title || '管理首页';
});

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value;
};

// 接收子路由组件（如消息中心）上报的未读消息数量
const handleUpdateUnread = (count) => {
  unreadCount.value = count;
};

// 加载未读消息数量
const fetchUnreadCount = async () => {
  if (isExpertMode.value || !isCurrentHome() || passwordState.required || document.hidden || noticeLoading.value) return;
  if (!canUsePermission('messages.read')) { unreadCount.value = 0; return; }
  noticeLoading.value = true;
  try {
    const count = await getUnreadNoticeCount();
    if (!isCurrentHome()) return;
    unreadCount.value = Number(count) || 0;
  } catch (error) {
    if (!isCurrentHome()) return;
    console.error('加载未读消息失败:', error);
    unreadCount.value = 0;
  } finally {
    if (isCurrentHome()) noticeLoading.value = false;
  }
};

// 加载待审核任务数量
let auditCountRequestId = 0;
const fetchTodoAuditCount = async () => {
  if (isExpertMode.value || !isCurrentHome() || passwordState.required || document.hidden) return;
  if (!canUsePermission('audit.read')) { todoAuditCount.value = 0; return; }
  const requestId = ++auditCountRequestId;
  const isCurrentRequest = () => isCurrentHome() && requestId === auditCountRequestId;
  auditLoading.value = true;
  try {
    const counts = await Promise.all([
      ...[['tasks', getTodoAuditCount], ['performance', getTodoPerformanceAuditCount], ['achievements', getTodoAchievementAuditCount], ['core', getTodoCoreAuditCount]].map(([domain, fetch]) => !accessState.permissionEnforced || hasPermission(`${domain}.audit`) || hasPermission(`${domain}.archive`) ? fetch().catch(() => 0) : Promise.resolve(0))
    ]);
    if (!isCurrentRequest()) return;
    todoAuditCount.value = counts.reduce((sum, count) => sum + (Number(count) || 0), 0);
  } catch (error) {
    if (!isCurrentRequest()) return;
    console.error('加载待审核任务数量失败:', error);
    todoAuditCount.value = 0;
  } finally {
    if (isCurrentRequest()) auditLoading.value = false;
  }
};

const unreadRefresh = createRefreshQueue(fetchUnreadCount);
const auditRefresh = createRefreshQueue(fetchTodoAuditCount);
const loadUnreadCount = (force = false) => {
  if (isExpertMode.value || !isCurrentHome() || passwordState.required || document.hidden) return Promise.resolve();
  return unreadRefresh.refresh({ force });
};
const loadTodoAuditCount = (force = false) => {
  if (isExpertMode.value || !isCurrentHome() || passwordState.required || document.hidden) return Promise.resolve();
  return auditRefresh.refresh({ force });
};
const handleCountVisibility = () => {
  if (!document.hidden && !isDepartmentAchievementAccount.value) {
    loadUnreadCount(true);
    loadTodoAuditCount(true);
  }
};

// 跳转到消息通知页面的方法
const handleCoreCount = () => loadTodoAuditCount(true);
const goToNotice = () => {
  router.push('/home/notice');
};

const resetPasswordForm = () => {
  passwordForm.newPassword = '';
  passwordForm.confirmPassword = '';
  passwordErrors.newPassword = '';
  passwordErrors.confirmPassword = '';
};

const openPasswordDialog = () => {
  resetPasswordForm();
  passwordDialogVisible.value = true;
};

const validatePasswordForm = () => {
  passwordErrors.newPassword = '';
  passwordErrors.confirmPassword = '';

  const nextPassword = String(passwordForm.newPassword || '');
  const confirmPassword = String(passwordForm.confirmPassword || '');

  if (!nextPassword.trim()) {
    passwordErrors.newPassword = '请输入新密码';
  } else if (!isPasswordValid(nextPassword)) {
    passwordErrors.newPassword = PASSWORD_RULE;
  }

  if (!confirmPassword) {
    passwordErrors.confirmPassword = '请再次输入新密码';
  } else if (nextPassword && confirmPassword !== nextPassword) {
    passwordErrors.confirmPassword = '两次输入的密码不一致';
  }

  return !passwordErrors.newPassword && !passwordErrors.confirmPassword;
};

const handleChangePassword = async () => {
  if (passwordSubmitting.value) return;
  if (!validatePasswordForm()) return;

  try {
    passwordSubmitting.value = true;
    await changePassword(passwordForm.newPassword);
    ElMessage.success('密码修改成功，请重新登录');
    passwordDialogVisible.value = false;
    resetPasswordForm();
    clearWorksCache();
    localStorage.removeItem('token');
    clearAdminTest();
    router.push('/login');
  } catch (error) {
    ElMessage.error(error?.message || '密码修改失败');
  } finally {
    passwordSubmitting.value = false;
  }
};

// 处理菜单项点击事件，防止事件冒泡和错误路由
const handleMenuSelect = (index) => {
  // 如果是工作中心子菜单的父菜单，不执行路由跳转
  if (index === 'works-submenu' || index === 'admin-submenu') {
    return;
  }
  // 其他菜单项由 router 模式自动处理
};

// el-menu's router mode owns navigation. A second router.push here would
// cancel asynchronous leave guards, including budget save/discard decisions.

onMounted(() => {
  window.addEventListener('platform-notice-read', fetchUnreadCount);
  window.addEventListener('audit-count-updated', handleCoreCount);
  document.addEventListener('visibilitychange', handleCountVisibility);
  loadWorkRecordAccess();
  if (passwordState.required) {
    currentUserName.value = parseJwtPayload(localStorage.getItem('token'))?.username || '用户';
    return;
  }
  loadCurrentUserName();
  if (isExpertMode.value || isDepartmentAchievementAccount.value) {
    return;
  }
  // 登录后立即加载未读消息数量和待审核任务数量
  loadUnreadCount();
  loadTodoAuditCount();
  
  // 定时刷新数量（每30秒刷新一次）
  countRefreshTimer = setInterval(() => {
    if (!document.hidden && !isDepartmentAchievementAccount.value) {
      loadUnreadCount();
      loadTodoAuditCount();
    }
  }, 30000);
});

onUnmounted(() => {
  window.removeEventListener('platform-notice-read', fetchUnreadCount);
  window.removeEventListener('audit-count-updated', handleCoreCount);
  document.removeEventListener('visibilitychange', handleCountVisibility);
  homeDisposed = true;
  unreadRefresh.dispose();
  auditRefresh.dispose();
  workRecordAccessController?.abort();
  clearInterval(countRefreshTimer);
});

// 退出登录
const loggingOut = ref(false);
const handleLogout = async () => {
  if (loggingOut.value) return;
  loggingOut.value = true;
  try {
    await ElMessageBox.confirm(
      '确定要退出登录吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    const ssoSession = parseJwtPayload(localStorage.getItem('token'))?.loginSource === 'SSO';
    let logoutRequest;
    const left = await navigateToLogout(router, localStorage.getItem('token'), () => {
      // Capture the current token in the request before clearing it. Logout
      // failures retain the previous local-signout behavior without redirects.
      logoutRequest = logout().catch(error => console.error('登出接口调用失败:', error));
      clearWorksCache();
      localStorage.removeItem('token');
      clearAdminTest();
      resetCurrentAccess();
      passwordState.required = false;
      unreadCount.value = 0;
    });
    if (left) {
      await logoutRequest;
      ElMessage.success('已退出登录');
      if (ssoSession) {
        try {
          const config = await getSsoStatus();
          if (config?.logoutUrl) window.location.assign(config.logoutUrl);
        } catch { /* Local session is already closed; the login page remains available. */ }
      }
    }
  } catch (error) {
    // 用户取消操作
    if (error !== 'cancel' && error !== 'close') {
      console.error('退出登录失败:', error);
    }
  } finally { loggingOut.value = false; }
};
</script>

<style scoped>
/* 全局布局容器 */
.common-layout {
  display: flex;
  height: 100vh;
  width: 100%;
  background-color: #f0f2f5;
  overflow: hidden;
}

/* --- 左侧侧边栏样式 --- */
.sidebar {
  width: 280px;
  background-color: #001529;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  transition: width 0.3s ease-in-out;
}

.sidebar.collapsed {
  width: 64px;
}

.logo-container {
  min-height: 64px;
  height: auto;
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background-color: #002140;
  overflow: visible;
  white-space: normal;
  transition: padding 0.3s;
  min-width: 0;
  flex-shrink: 0;
}

.sidebar.collapsed .logo-container {
  padding: 12px 16px;
  justify-content: center;
  min-height: 64px;
  height: auto;
}

.logo-image {
  width: 32px;
  height: 32px;
  border-radius: 4px;
  margin-right: 12px;
  flex-shrink: 0;
}

.sidebar.collapsed .logo-image {
  margin-right: 0;
}

.logo-text {
  color: white;
  font-size: 16px;
  font-weight: bold;
  letter-spacing: 0.5px;
  transition: opacity 0.2s;
  white-space: normal;
  overflow: visible;
  line-height: 1.4;
  word-break: break-all;
  flex: 1;
  min-width: 0;
}

.side-menu {
  flex: 1;
  border-right: none;
  overflow-y: auto;
  overflow-x: hidden;
}

/* 覆盖 Element Plus 菜单样式 */
:deep(.el-menu) {
  border-right: none;
}

:deep(.el-menu-item.is-active) {
  background-color: #1890ff !important;
  color: #fff !important;
}

:deep(.el-menu-item:hover) {
  background-color: #001f3d !important;
}

:deep(.el-menu--collapse .el-sub-menu__title) {
  padding-left: 20px !important;
}

/* --- 右侧主体内容样式 --- */
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  height: 100vh;
  overflow: hidden;
}

/* 顶部 Header */
.top-header {
  height: 64px;
  background-color: #fff;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  flex-shrink: 0;
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
}

.fold-btn {
  font-size: 20px;
  margin-right: 16px;
  cursor: pointer;
  color: #606266;
  transition: color 0.3s;
}

.fold-btn:hover {
  color: #1890ff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.header-action-item {
  display: flex;
  align-items: center;
  cursor: pointer;
  position: relative;
}

.header-action-item.loading-state {
  opacity: 0.7;
}

.bell-icon {
  font-size: 20px;
  color: #606266;
  transition: color 0.3s;
}

.bell-loading {
  color: #909399;
  opacity: 0.7;
}

.user-dropdown {
  cursor: pointer;
}

.user-trigger {
  display: flex;
  align-items: center;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-trigger:hover {
  background-color: #f5f5f5;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: #1890ff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
  margin-right: 8px;
}

.user-name {
  font-size: 14px;
  color: #606266;
  margin-right: 4px;
}

/* 内容区域容器 */
.content-wrapper {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  padding: 24px;
  background-color: #f0f2f5;
  box-sizing: border-box;
}

/* 菜单项标题和徽章样式 */
.menu-title-wrapper {
  position: relative;
  display: inline-block;
}

.menu-title {
  display: inline-block;
}

.menu-badge {
  position: absolute;
  top: -6px;
  right: -8px;
  transform: translate(50%, -50%);
  z-index: 10;
}

/* 菜单项中的徽章样式调整 - 去掉白边 */
:deep(.el-menu-item .menu-badge .el-badge__content) {
  border: none !important;
  box-shadow: none !important;
  background-color: #f56c6c !important;
  padding: 0 4px !important;
  min-width: 16px !important;
  height: 16px !important;
  line-height: 16px !important;
  font-size: 11px !important;
}

/* 确保菜单项标题区域正确布局，不影响其他菜单项 */
:deep(.el-menu-item .el-menu-item__title) {
  display: flex !important;
  align-items: center !important;
  width: 100% !important;
  flex: 1;
  min-height: auto !important;
  height: auto !important;
}

/* 确保所有菜单项高度一致 */
:deep(.el-menu-item) {
  height: 56px !important;
  line-height: 56px !important;
}

/* 防止子菜单项点击区域重叠 */
:deep(.el-sub-menu .el-menu-item) {
  position: relative;
  z-index: 1;
  pointer-events: auto;
}

/* 确保子菜单项可以正常点击 */
:deep(.el-sub-menu__title) {
  pointer-events: auto;
}

/* 防止菜单项点击事件冲突 */
:deep(.el-menu-item.is-active) {
  z-index: 2;
}

/* 优化子菜单展开时的点击区域 */
:deep(.el-sub-menu.is-opened .el-menu-item) {
  background-color: transparent;
}

:deep(.el-sub-menu.is-opened .el-menu-item:hover) {
  background-color: #001f3d !important;
}

/* 确保子菜单项点击时不会触发父菜单的路由 */
:deep(.el-sub-menu .el-menu-item) {
  cursor: pointer;
}
</style>

<style scoped>
</style>
