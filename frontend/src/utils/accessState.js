import { reactive } from 'vue'
import { clearAdminTest, setAdministratorAccess } from '@/utils/adminTest'
import { get } from '@/utils/request'
import { clearWorksCache } from '@/utils/worksCache'

export const accessState = reactive({ canViewDashboard:false,currentUser:null,experience:'BUSINESS',reviewRoundsEnabled:false,businessAvailable:true,expertAvailable:false,historyAvailable:false, token: '', role: '', isAdmin: false, isLeader: false,
  permissions: [], permissionVersion: 0, permissionConfigured: false, permissionEnforced: false, permissionRoles: [], dataScopes: {}, canViewExpertReviews: false,
  canViewOverview: false, canViewBudget: false, canFillBudget: false, canViewAchievement: false, canViewAuditCenter: false, canViewCoreIndicators: false,
  leadershipScope: 'NONE', departmentIds: [] })
let requestVersion = 0
export function resetCurrentAccess() {
  clearAdminTest()
  requestVersion += 1
  Object.assign(accessState, { canViewDashboard:false,currentUser:null,experience:'BUSINESS',reviewRoundsEnabled:false,businessAvailable:true,expertAvailable:false,historyAvailable:false, token: '', role: '', isAdmin: false, isLeader: false,
    permissions: [], permissionVersion: 0, permissionConfigured: false, permissionEnforced: false, permissionRoles: [], dataScopes: {}, canViewExpertReviews: false,
    canViewOverview: false, canViewBudget: false, canFillBudget: false, canViewAchievement: false, canViewAuditCenter: false, canViewCoreIndicators: false,
    leadershipScope: 'NONE', departmentIds: [] })
}

export async function loadCurrentAccess() {
  const token = localStorage.getItem('token') || ''
  const version = ++requestVersion
  const access = await get('/system/access')
  if (version !== requestVersion || token !== localStorage.getItem('token')) return false
  if (accessState.token && (accessState.token !== token || accessState.role !== access.role
    || accessState.permissionVersion !== access.permissionVersion
    || JSON.stringify(accessState.permissions) !== JSON.stringify(access.permissions)
    || JSON.stringify(accessState.dataScopes) !== JSON.stringify(access.dataScopes)
    || accessState.leadershipScope !== access.leadershipScope
    || JSON.stringify(accessState.departmentIds) !== JSON.stringify(access.departmentIds))) clearWorksCache()
  if (!access.isAdmin) clearAdminTest()
  Object.assign(accessState, access, { token })
  setAdministratorAccess(access.isAdmin, token)
  return true
}

// 功能授权只控制入口，填报资格和当前审核处理人仍由业务接口核验。
export const hasPermission = code => accessState.isAdmin || accessState.permissions.includes(code)
export const canUsePermission = (code, legacy = true) => accessState.permissionEnforced ? hasPermission(code) : legacy
