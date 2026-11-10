const stages = { dept: [10, 20], principal: [20, 30], admin: [30, 40] }
const field = (row, camel, snake) => row?.[camel] ?? row?.[snake]
const timestamp = value => {
  if (value === null || value === undefined || value === '') return null
  const result = value instanceof Date ? value.getTime() : typeof value === 'number' ? value : Date.parse(value)
  return Number.isFinite(result) ? result : null
}

/** Historical logs remain available, but may not establish a current-cycle step. */
export function currentTaskStageAudit(submission, logs, stage) {
  const transition = stages[stage]
  const subId = field(submission, 'subId', 'sub_id')
  const rawStatus = field(submission, 'flowStatus', 'flow_status')
  const submittedAt = timestamp(field(submission, 'submitTime', 'submit_time'))
  if (!transition || subId === null || subId === undefined || subId === '' || rawStatus === null || rawStatus === undefined || submittedAt === null || !Array.isArray(logs)) return null
  const status = Number(rawStatus)
  const [before, passed] = transition
  const cycle = logs.filter(log => {
    const time = timestamp(field(log, 'createTime', 'create_time'))
    return String(field(log, 'subId', 'sub_id')) === String(subId) && time !== null && time >= submittedAt
  }).sort((a, b) => {
    const difference = timestamp(field(b, 'createTime', 'create_time')) - timestamp(field(a, 'createTime', 'create_time'))
    if (difference) return difference
    const left = String(field(a, 'logId', 'log_id') ?? '')
    const right = String(field(b, 'logId', 'log_id') ?? '')
    if (!/^\d+$/.test(left) || !/^\d+$/.test(right)) return 0
    return BigInt(right) > BigInt(left) ? 1 : BigInt(right) < BigInt(left) ? -1 : 0
  })
  // Returned submissions use -10 at every stage; the real rejection identifies that stage.
  const rejected = cycle.find(log => Number(field(log, 'postStatus', 'post_status')) < 0 && [10, 20, 30].includes(Number(field(log, 'preStatus', 'pre_status'))))
  const failedAt = rejected ? Number(field(rejected, 'preStatus', 'pre_status')) : Math.abs(status)
  if (!Number.isFinite(status)) return null
  // A backward transition ends the prior approval round at this stage and all later stages.
  const returnTarget = log => {
    const from = Number(field(log, 'preStatus', 'pre_status'))
    const to = Number(field(log, 'postStatus', 'post_status'))
    // A guarded maintenance repair can reopen a legacy terminal return. Keep the
    // original negative log intact while invalidating the previous approval cycle.
    if (from === 30 && (to === 20 || (status >= 0 && to < 0))) return 20
    if (from === 20 && (to === 10 || (status >= 0 && to < 0))) return 10
    return null
  }
  const backwards = log => returnTarget(log) !== null
  const ownReturn = cycle.find(log => backwards(log) && Number(field(log, 'preStatus', 'pre_status')) === before)
  if (ownReturn && status >= 0 && status < before) {
    return {
      time: field(ownReturn, 'createTime', 'create_time'),
      result: before === 30 ? '退回至归口审核' : '退回至专业群审核',
      ...(field(ownReturn, 'operatorId', 'operator_id') != null ? { operatorId: field(ownReturn, 'operatorId', 'operator_id') } : {}),
    }
  }
  if (status >= 0 ? status < passed : failedAt < before) return null
  const boundary = cycle.findIndex(log => backwards(log) && returnTarget(log) <= before)
  const relevant = boundary < 0 ? cycle : cycle.slice(0, boundary)
  const matches = relevant.filter(log => {
    const after = Number(field(log, 'postStatus', 'post_status'))
    return Number(field(log, 'preStatus', 'pre_status')) === before && (after === passed || (status < 0 && after < 0))
  })
  const log = matches[0]
  if (!log) return null
  return {
    time: field(log, 'createTime', 'create_time'),
    result: Number(field(log, 'postStatus', 'post_status')) === passed ? stage === 'admin' ? '已归档' : '审核通过' : '驳回',
    ...(field(log, 'operatorId', 'operator_id') != null ? { operatorId: field(log, 'operatorId', 'operator_id') } : {}),
  }
}

export function taskStageDescription(submission, logs, stage, configuredName, nameOf, formatTime) {
  const audit = currentTaskStageAudit(submission, logs, stage)
  if (audit) {
    const name = audit.operatorId == null ? '—' : nameOf(audit.operatorId)
    return `审核人：${name || '—'}（${formatTime(audit.time)} ${audit.result}）`
  }
  const status = Number(field(submission, 'flowStatus', 'flow_status'))
  const [before, passed] = stages[stage]
  // No attribution or date is invented when earlier stages have no surviving evidence.
  if (status >= passed) return stage === 'admin' ? '已归档' : '已流转'
  const name = status === before ? nameOf(field(submission, 'currentHandlerId', 'current_handler_id')) || configuredName : configuredName
  return `审核人：${name || '—'}（${status === before ? '进行中' : '待流转'}）`
}

export function currentTaskSubmission(records) {
  const newest = records?.[0]
  if (!newest) return null
  return Number(field(newest, 'isDelete', 'is_delete')) === 1 && Number(field(newest, 'flowStatus', 'flow_status')) >= 0 ? null : newest
}

export function canProcessTaskSubmission(submission) {
  return !!submission && Number(field(submission, 'isDelete', 'is_delete')) !== 1 && [10, 20, 30].includes(Number(field(submission, 'flowStatus', 'flow_status')))
}

export function taskWorkflowStep(submission, logs) {
  const status = Number(field(submission, 'flowStatus', 'flow_status'))
  if (status < 0) {
    for (const [stage, step] of [['admin', 3], ['principal', 2], ['dept', 1]]) {
      if (currentTaskStageAudit(submission, logs, stage)?.result === '驳回') return step
    }
    return status <= -30 ? 3 : status <= -20 ? 2 : 1
  }
  return status >= 40 ? 4 : status >= 30 ? 3 : status >= 20 ? 2 : status >= 10 ? 1 : 0
}

export function taskSubmissionStatus(submission, logs, fallback) {
  const status = Number(field(submission, 'flowStatus', 'flow_status'))
  if (status < 0) return ['已退回', '专业群退回', '归口退回', '双高办退回'][taskWorkflowStep(submission, logs)]
  if (Number(field(submission, 'isDelete', 'is_delete')) === 1) {
    const id = field(submission, 'subId', 'sub_id')
    return logs.some(l => String(field(l, 'subId', 'sub_id')) === String(id) && field(l, 'actionType', 'action_type') === '撤回提交') ? '已撤回' : '历史记录'
  }
  return fallback
}

export function taskAuditPostStatus(log, fallback) {
  if (field(log, 'actionType', 'action_type') === '撤回提交') return '已撤回'
  const from = Number(field(log, 'preStatus', 'pre_status')), to = Number(field(log, 'postStatus', 'post_status'))
  if (from === 20 && to === 10) return '退回至专业群审核'
  if (from === 30 && to === 20) return '退回至归口审核'
  if (Number(field(log, 'postStatus', 'post_status')) < 0) return ({ 10: '专业群退回', 20: '归口退回', 30: '双高办退回' })[Number(field(log, 'preStatus', 'pre_status'))] || '已退回'
  return fallback
}
