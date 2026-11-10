export const exportModules = {
  tasks: ['建设任务', 'tasks.export'], performance: ['绩效', 'performance.export'],
  achievements: ['建设成果', 'achievements.export'], core: ['核心指标', 'core.export'],
  records: ['工作纪实', 'records.export'], budget: ['预算', 'budget.export'],
  'budget-versions': ['预算版本', 'budget.export'], notices: ['本人消息', 'messages.read'],
  audits: ['审核记录', 'audit.read'], supervision: ['督办任务', 'tasks.export'],
  'admin-users': ['账号', 'admin'], 'admin-departments': ['部门', 'admin'],
  'admin-roles': ['角色权限', 'admin'], 'admin-operations': ['管理操作', 'admin'],
  'platform-notices': ['平台通知', 'admin'], 'review-tasks': ['评审任务', 'admin']
}

export const exportDomain = domain => domain.startsWith('audits-') ? 'audits' : domain

export function dataExportUrl(domain, scope, filters = {}, context = {}) {
  domain = exportDomain(domain)
  if (!exportModules[domain] || !['all', 'filtered'].includes(scope)) throw new Error('不支持的导出范围')
  const params = new URLSearchParams({ scope })
  // A tab is a visibility context, not a text filter; keep todo/history separate.
  if (context.tab) params.set('tab', context.tab)
  if (scope === 'filtered') {
    if (context.section) params.set('section', context.section)
    for (const [key, value] of Object.entries(filters)) {
      for (const item of Array.isArray(value) ? value : [value]) {
        if (item != null && item !== '') params.append(`f.${key}`, String(item))
      }
    }
  }
  return `/data-exports/${domain}?${params}`
}
