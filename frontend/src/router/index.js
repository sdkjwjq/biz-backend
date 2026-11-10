import { createRouter, createWebHistory } from 'vue-router'
import { getPasswordStatus } from '@/api/user'
import { passwordState } from '@/utils/passwordPolicy'
import { clearWorksCache } from '@/utils/worksCache'
import { ElMessage } from 'element-plus'
import { safeLoginRedirect } from '@/utils/detailNavigation'
import { accessState, loadCurrentAccess, resetCurrentAccess, hasPermission } from '@/utils/accessState'
import { isLogoutNavigation } from '@/utils/logoutNavigation'
import { checkPlatformNotices } from '@/utils/platformNoticeState'

const BUDGET_ACCESS_ROLES = ['0', '2']
const ACHIEVEMENT_HOME = '/home/works/achievement'
const DEFAULT_HOME = '/home/works'
const ADMIN_ROLES = ['0']

const getTokenPayload = () => {
  try {
    const token = localStorage.getItem('token') || ''
    const parts = token.split('.')
    if (parts.length < 2) return null
    let payload = parts[1].replace(/-/g, '+').replace(/_/g, '/')
    const pad = payload.length % 4
    if (pad) payload += '='.repeat(4 - pad)
    return JSON.parse(atob(payload))
  } catch (e) { // eslint-disable-line no-unused-vars
    return null
  }
}

const getCurrentRole = () => {
  if (accessState.token === localStorage.getItem('token')) return accessState.role
  const payload = getTokenPayload()
  return payload?.role !== undefined && payload?.role !== null ? String(payload.role) : ''
}

const hasTokenFlag = (key) => {
  const payload = getTokenPayload()
  return payload?.[key] === true
}

const canAccessAchievement = () => {
  return accessState.canViewAchievement
}

const isDepartmentAchievementAccount = () => {
  return !accessState.isLeader && hasTokenFlag('isDepartmentAccount')
}

const getDefaultHome = () => {
  if(accessState.experience==='SELECT')return '/choose-role'
  if(accessState.experience==='EXPERT')return '/home/works'
  if (accessState.permissionEnforced && !accessState.isAdmin) {
    const homes = [['tasks.read', DEFAULT_HOME], ['performance.read', '/home/works/performance'], ['achievements.read', ACHIEVEMENT_HOME], ['core.read', '/home/core-indicators'], ['records.read', '/home/work-records'], ['budget.read', '/home/works/budget'], ['messages.read', '/home/notice'], ['overview.read', '/home/admin/overview'], ['dashboard.read', '/dashboard']]
    return homes.find(([code]) => hasPermission(code))?.[1] || '/home/access-unavailable'
  }
  return isDepartmentAchievementAccount() ? ACHIEVEMENT_HOME : DEFAULT_HOME
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/sso/logout', component: () => import('@/views/SsoLogout.vue'), meta: { title: '退出登录' } },
    {path:'/choose-role',component:()=>import('@/views/ReviewRoleChoice.vue'),meta:{requiresAuth:true,title:'选择使用角色'}},
    {path:'/review',redirect:to=>({path:'/home/works',query:to.query})},
    {path:'/review-history',redirect:to=>({path:'/home/works',query:{...to.query,reviewView:'history'}})},
    { path: '/required-notices', name: 'RequiredNotices', component: () => import('@/views/RequiredNotices.vue'), meta: { requiresAuth: true, title: '平台重要通知' } },
    {
      path: '/',
      redirect: to => ({ path: '/login', query: to.query }),
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: { title: '登录' }
    },
    {
      path: '/dashboard',
      name: 'Dashboard',
      component: () => import('@/views/Dashboard.vue'),
      meta: { title: '数据大屏监控', requiresAuth: true }
    },
    {
      path: '/home',
      name: 'Home',
      component: () => import('@/views/Home.vue'),
      meta: { title: '管理首页', requiresAuth: true },
      redirect: '/home/board',
      children: [
        {path:'expert-reviews',redirect:'/review-history'},
        {path:'admin/review-tasks',component:()=>import('@/components/admin/ReviewTaskManagement.vue'),meta:{title:'管理中心 / 评审任务管理',roles:ADMIN_ROLES}},
        { path: 'access-unavailable', component: () => import('@/components/AccessUnavailable.vue'), meta: { title: '暂无可用功能' } },
        { path: 'admin/permissions', component: () => import('@/components/admin/AdminPermissions.vue'), meta: { title: '管理中心 / 角色与权限', roles: ADMIN_ROLES } },
        {
          path: 'board',
          name: 'Board',
          component: () => import('@/components/AccessUnavailable.vue'),
          meta: { title: '管理首页 (驾驶舱)' }
        },
        {
          path: 'works',
          name: 'HomeWorks',
          component: () => import('@/components/TaskWorkspace.vue'),
          meta: { title: '工作中心 (我的任务)' }
        },
        {
          path: 'works/performance',
          name: 'Performance',
          component: () => import('@/components/Performance.vue'),
          meta: { title: '工作中心 (绩效)' }
        },
        {
          path: 'works/achievement',
          name: 'Achievement',
          component: () => import('@/components/Achievement.vue'),
          meta: { title: '工作中心 (成果)', requiresAchievementAccess: true }
        },
        {
          path: 'works/budget',
          name: 'Budget',
          component: () => import('@/components/Budget.vue'),
          meta: { title: '工作中心 (预算)', roles: BUDGET_ACCESS_ROLES }
        },
        {
          path: 'notice',
          name: 'Notice',
          component: () => import('@/components/Notice.vue'),
          meta: { title: '消息通知' }
        },
        {
          path: 'audit',
          name: 'Audit',
          component: () => import('@/components/Audit.vue'),
          meta: { title: '审核中心' }
        },
        {
          path: 'data',
          name: 'Data',
          redirect: () => getDefaultHome(),
          meta: { title: '数据中心' }
        },
        {
          path: 'query',
          name: 'Query',
          redirect: () => getDefaultHome(),
          meta: { title: '查询中心' }
        },
        {
          path: 'core-indicators',
          name: 'CoreIndicators',
          component: () => import('@/components/CoreIndicators.vue'),
          meta: { title: '核心指标' }
        },
        {
          path: 'work-records',
          name: 'WorkRecords',
          component: () => import('@/components/WorkRecords.vue'),
          meta: { title: '工作纪实' }
        },
        {
          path: 'report',
          name: 'Report',
          component: () => import('@/components/Report.vue'),
          meta: { title: '督办中心' }
        },
        {
          path: 'admin',
          redirect: '/home/admin/overview',
          meta: { title: '管理中心', roles: ADMIN_ROLES }
        },
        {
          path: 'admin/overview',
          name: 'AdminOverview',
          component: () => import('@/components/admin/AdminOverview.vue'),
          meta: { title: '管理中心 / 管理概览', requiresOverview: true }
        },
        {
          path: 'admin/accounts',
          name: 'AdminAccounts',
          component: () => import('@/components/admin/AdminAccounts.vue'),
          meta: { title: '管理中心 / 账号与组织', roles: ADMIN_ROLES }
        },
        { path: 'admin/notices', name: 'AdminNotices', component: () => import('@/components/admin/AdminNotices.vue'), meta: { title: '管理中心 / 通知管理', roles: ADMIN_ROLES } },
        { path: 'platform-notices/:id', name: 'PlatformNoticeRead', component: () => import('@/components/PlatformNoticeRead.vue'), meta: { title: '平台通知' } },
        {
          path: 'admin/business',
          name: 'AdminBusiness',
          component: () => import('@/components/admin/AdminBusiness.vue'),
          meta: { title: '管理中心 / 任务与绩效', roles: ADMIN_ROLES }
        },
        {
          path: 'admin/data',
          name: 'AdminData',
          component: () => import('@/components/admin/AdminData.vue'),
          meta: { title: '管理中心 / 数据维护', roles: ADMIN_ROLES }
        },
        {
          path: 'admin/operations',
          name: 'AdminOperations',
          component: () => import('@/components/admin/AdminOperations.vue'),
          meta: { title: '管理中心 / 操作记录', roles: ADMIN_ROLES }
        },
        // 兼容旧入口，统一跳转到真实管理中心。
        {
          path: 'system/user',
          name: 'SystemUser',
          redirect: '/home/admin/accounts',
          meta: { title: '用户管理' }
        },
        {
          path: 'system/permission',
          name: 'SystemPermission',
          redirect: '/home/admin/permissions',
          meta: { title: '角色与权限' }
        },
        {
          path: 'system/log',
          name: 'SystemLog',
          redirect: '/home/admin/operations',
          meta: { title: '系统日志' }
        }
      ]
    },
  ],
})

// 路由守卫：未登录不可进入管理平台；已登录访问登录页自动跳转
router.beforeEach(async (to, from, next) => {
  const token = localStorage.getItem('token') || ''
  const authed = !!token
  if (accessState.token !== token) resetCurrentAccess()
  if (!authed) passwordState.required = false

  // 设置页面标题
  if (to.meta?.title) document.title = String(to.meta.title)

  if (to.path === '/sso/logout') return next()

  if (to.path === '/login') {
    if (authed && !to.query.code && !to.query.error && !isLogoutNavigation(router, to, token)) return next(safeLoginRedirect(router, to.query.redirect, '/home/board'))
    return next()
  }

  if (to.matched.some(r => r.meta && r.meta.requiresAuth)) {
    if (!authed) return next({ path: '/login', query: { redirect: to.fullPath } })
    try {
      const status = await getPasswordStatus()
      if (localStorage.getItem('token') !== token) return next(false)
      passwordState.required = status.requiresPasswordChange === true
      if (!passwordState.required && !(await loadCurrentAccess())) return next(false)
      if (passwordState.required && to.path !== getDefaultHome()) {
        return next({ path: getDefaultHome(), query: { passwordReturn: to.fullPath } })
      }
      if (!passwordState.required && to.query.passwordReturn) {
        const target = safeLoginRedirect(router, to.query.passwordReturn, getDefaultHome())
        return next(target)
      }
    } catch (error) {
      if (localStorage.getItem('token') !== token) return next(false)
      clearWorksCache()
      localStorage.removeItem('token')
      passwordState.required = false
      ElMessage.error(error?.message || '无法核对密码状态，请重新登录')
      return next('/login')
    }
  }

  if (authed && !passwordState.required && to.matched.some(r => r.meta?.requiresAuth)) {
    let pending
    try { pending = await checkPlatformNotices() } catch {
      if (to.path !== '/required-notices') return next({ path: '/required-notices', query: { return: to.fullPath } })
    }
    if (localStorage.getItem('token') !== token) return next(false)
    if (to.path === '/required-notices') {
      if (pending && !pending.length) { const target = safeLoginRedirect(router, to.query.return, getDefaultHome()); return next(target.startsWith('/required-notices') ? getDefaultHome() : target) }
      return next()
    }
    if (pending?.length) return next({ path: '/required-notices', query: { return: to.fullPath } })
  }

  if(authed&&!passwordState.required&&accessState.reviewRoundsEnabled){
    if(to.path==='/choose-role')return next()
    const history=to.path==='/home/works'&&to.query.reviewView==='history'
    if(history&&accessState.isAdmin&&accessState.experience==='BUSINESS')return next({path:'/home/admin/review-tasks',query:{archive:to.query.archive==='legacy'?'legacy':undefined}})
    if(accessState.experience==='SELECT'&&!history)return next('/choose-role')
    if(accessState.experience==='EXPERT'&&to.path!=='/home/works')return next('/home/works')
    if(history||accessState.experience==='EXPERT'&&to.path==='/home/works')return next()
  }
  const featureByPath = { '/dashboard': 'dashboard.read', '/home/works': 'tasks.read', '/home/works/performance': 'performance.read', '/home/works/achievement': 'achievements.read', '/home/works/budget': 'budget.read', '/home/core-indicators': 'core.read', '/home/work-records': 'records.read', '/home/notice': 'messages.read', '/home/audit': 'audit.read', '/home/report': 'tasks.supervise' }
  if(authed&&!passwordState.required&&to.path==='/dashboard'&&!accessState.canViewDashboard)return next(getDefaultHome())
  if (to.path === '/home/board') return next(getDefaultHome())
  if (authed && isDepartmentAchievementAccount() && !to.path.startsWith('/home/platform-notices/') && to.path !== ACHIEVEMENT_HOME) {
    ElMessage.warning('当前账号仅可进入成果页面')
    return next(ACHIEVEMENT_HOME)
  }
  featureByPath['/home/expert-reviews'] = 'expert.read'
  if (!passwordState.required && accessState.permissionEnforced && featureByPath[to.path] && !hasPermission(featureByPath[to.path])) return next(getDefaultHome())
  if (authed && to.path === '/home/core-indicators' && !accessState.canViewCoreIndicators) return next(getDefaultHome())

  if (authed && to.matched.some(r => r.meta?.requiresAchievementAccess) && !canAccessAchievement()) {
    return next(getDefaultHome())
  }

  const allowedRoles = to.matched
    .map(r => r.meta?.roles)
    .filter(Array.isArray)
    .at(-1)

  if (!passwordState.required && to.meta.requiresOverview && !accessState.canViewOverview) return next(getDefaultHome())
  if (!passwordState.required && to.path === '/home/audit' && !accessState.canViewAuditCenter) return next(getDefaultHome())
  if (allowedRoles && !(to.path === '/home/works/budget' && accessState.canViewBudget) && !allowedRoles.includes(getCurrentRole())) {
    return next(getDefaultHome())
  }

  return next()
})

window.addEventListener('platform-notice-required', () => {
  if (router.currentRoute.value.path !== '/required-notices') router.replace({ path: '/required-notices', query: { return: router.currentRoute.value.fullPath } })
})
export default router
