import test from 'node:test'
import assert from 'node:assert/strict'
import { currentTaskStageAudit, taskStageDescription, currentTaskSubmission, canProcessTaskSubmission, taskWorkflowStep, taskSubmissionStatus, taskAuditPostStatus } from '../src/utils/taskAuditTimeline.mjs'

const audit = (flowStatus = 20, rest = {}) => ({ subId: 242, submitTime: '2026-06-24T12:53:13+08:00', flowStatus, ...rest })
const log = (preStatus, postStatus, createTime, rest = {}) => ({ subId: 242, preStatus, postStatus, createTime, ...rest })
const oldArchive = log(30, 40, '2026-05-20T17:13:05+08:00', { logId: 1296 })
const groupPass = log(10, 20, '2026-06-25T09:28:17+08:00', { logId: 2031 })

test('reported same-ID legacy archive cannot become a current-cycle archive', () => {
  const records = [groupPass, oldArchive]
  assert.deepEqual(currentTaskStageAudit(audit(), records, 'dept'), { time: groupPass.createTime, result: '审核通过' })
  assert.equal(currentTaskStageAudit(audit(), records, 'principal'), null)
  assert.equal(currentTaskStageAudit(audit(), records, 'admin'), null)
  assert.equal(records.length, 2, 'Historical records remain available')
})

test('another submission cannot supply step evidence even with a later timestamp', () => {
  const other = log(30, 40, '2026-07-01T08:00:00+08:00', { subId: 241 })
  assert.equal(currentTaskStageAudit(audit(40), [other], 'admin'), null)
})

test('current flow state blocks contradictory later-step logs inside the same cycle', () => {
  const records = [groupPass, log(20, 30, '2026-06-26T10:00:00+08:00'), log(30, 40, '2026-06-27T10:00:00+08:00')]
  assert.equal(currentTaskStageAudit(audit(10), records, 'dept'), null)
  assert.equal(currentTaskStageAudit(audit(20), records, 'principal'), null)
  assert.equal(currentTaskStageAudit(audit(20), records, 'admin'), null)
  assert.equal(currentTaskStageAudit(audit(30), records, 'admin'), null)
  assert.equal(currentTaskStageAudit(audit(30), records, 'principal').result, '审核通过')
})

test('a real current-cycle archive remains visible and supersedes legacy archive records', () => {
  const latest = log(30, 40, '2026-06-27T10:00:00+08:00', { logId: 3000 })
  assert.deepEqual(currentTaskStageAudit(audit(40), [oldArchive, latest], 'admin'), { time: latest.createTime, result: '已归档' })
})

test('same-time revisions use their original log IDs without losing long-ID precision', () => {
  const time = '2026-06-25T09:28:17+08:00'
  const a = log(10, 20, time, { logId: '9007199254740992' })
  const b = log(10, -10, time, { logId: '9007199254740993' })
  assert.equal(currentTaskStageAudit(audit(-10), [a, b], 'dept').result, '驳回')
})

test('snake-case responses, numeric timestamps, and same-instant submissions are supported', () => {
  const time = Date.parse('2026-06-24T12:53:13+08:00')
  const record = { sub_id: '242', pre_status: 20, post_status: 30, create_time: time }
  assert.deepEqual(currentTaskStageAudit({ sub_id: 242, flow_status: 30, submit_time: time }, [record], 'principal'), { time, result: '审核通过' })
})

test('missing identity or time evidence never borrows a historical timestamp', () => {
  for (const submission of [null, audit(40, { subId: null }), audit(40, { submitTime: null }), audit(40, { submitTime: 'invalid' })]) {
    assert.equal(currentTaskStageAudit(submission, [oldArchive], 'admin'), null)
  }
  assert.equal(currentTaskStageAudit(audit(40), [log(30, 40, null)], 'admin'), null)
  assert.equal(currentTaskStageAudit(audit(40), [{ ...oldArchive, subId: undefined }], 'admin'), null)
})

test('current-cycle rejections are preserved while pre-submission ones are ignored', () => {
  const rejected = log(20, -20, '2026-06-26T10:00:00+08:00')
  assert.equal(currentTaskStageAudit(audit(-20), [rejected], 'principal').result, '驳回')
  assert.equal(currentTaskStageAudit(audit(-20), [log(20, -20, '2026-05-20T10:00:00+08:00')], 'principal'), null)
})

test('a rejected stage cannot show a later stage as already archived', () => {
  const archive = log(30, 40, '2026-06-27T10:00:00+08:00')
  const principal = log(20, 30, '2026-06-26T10:00:00+08:00')
  assert.equal(currentTaskStageAudit(audit(-10), [principal, archive], 'principal'), null)
  assert.equal(currentTaskStageAudit(audit(-20), [archive], 'admin'), null)
})

test('resubmission starts a fresh display cycle without removing earlier evidence', () => {
  const previous = [groupPass, log(20, -20, '2026-06-26T10:00:00+08:00')]
  const resubmitted = audit(10, { submitTime: '2026-07-01T09:00:00+08:00' })
  for (const stage of ['dept', 'principal', 'admin']) assert.equal(currentTaskStageAudit(resubmitted, previous, stage), null)
  assert.equal(previous.length, 2)
  const passed = log(10, 20, '2026-07-02T09:00:00+08:00')
  assert.deepEqual(currentTaskStageAudit({ ...resubmitted, flowStatus: 20 }, [...previous, passed], 'dept'), { time: passed.createTime, result: '审核通过' })
})

test('canonical -10 return at principal stage retains professional approval and real rejection', () => {
  const reject = log(20, -10, '2026-06-26T10:00:00+08:00', { operatorId: 42 })
  assert.equal(currentTaskStageAudit(audit(-10), [groupPass, reject], 'dept').result, '审核通过')
  assert.equal(currentTaskStageAudit(audit(-10), [groupPass, reject], 'principal').result, '驳回')
  assert.equal(currentTaskStageAudit(audit(-10), [groupPass, reject], 'admin'), null)
})

test('completed evidence identifies the actual operator rather than current configuration', () => {
  const recorded = { ...groupPass, operatorId: 42 }
  const description = taskStageDescription(audit(20), [recorded], 'dept', 'New reviewer', id => id === 42 ? 'Actual reviewer' : 'Other', value => value)
  assert.match(description, /Actual reviewer/)
  assert.doesNotMatch(description, /New reviewer/)
})

test('archived history without evidence invents neither operator nor approval date', () => {
  const resolve = () => 'Configured reviewer'
  for (const stage of ['dept', 'principal', 'admin']) {
    const description = taskStageDescription(audit(40), [oldArchive], stage, 'New reviewer', resolve, value => value)
    assert.doesNotMatch(description, /New reviewer|Configured reviewer|2026-05-20|审核通过|修复|异常/)
  }
})

test('pending principal shows real handler while archive remains waiting', () => {
  const submission = audit(20, { currentHandlerId: 42 })
  const resolve = id => id === 42 ? 'Current handler' : ''
  assert.match(taskStageDescription(submission, [oldArchive], 'principal', 'Configured', resolve, String), /Current handler.*进行中/)
  assert.match(taskStageDescription(submission, [oldArchive], 'admin', 'Archive reviewer', resolve, String), /待流转/)
})

test('returned history stays visible but cannot become an actionable approval', () => {
  const returned = audit(-10, { isDelete: 1 })
  const logs = [groupPass, log(20, -10, '2026-06-26T10:00:00+08:00')]
  assert.equal(currentTaskSubmission([returned]), returned)
  assert.equal(canProcessTaskSubmission(returned), false)
  assert.equal(taskWorkflowStep(returned, logs), 2)
  assert.equal(taskSubmissionStatus(returned, logs, ''), '归口退回')
})

test('withdrawn positive history cannot masquerade as a current pending submission', () => {
  const withdrawn = audit(20, { isDelete: 1 })
  assert.equal(currentTaskSubmission([withdrawn]), null)
  assert.equal(canProcessTaskSubmission(withdrawn), false)
  assert.equal(taskSubmissionStatus(withdrawn, [log(20, -20, '2026-06-26T10:00:00+08:00', { actionType: '撤回提交' })], ''), '已撤回')
  assert.equal(canProcessTaskSubmission(audit(40, { isDelete: 0 })), false)
  assert.equal(canProcessTaskSubmission(audit(20, { isDelete: 0 })), true)
})

test('history uses actual return stage and does not label withdrawal as rejection', () => {
  assert.equal(taskAuditPostStatus(log(20, -10, ''), ''), '归口退回')
  assert.equal(taskAuditPostStatus(log(30, -10, ''), ''), '双高办退回')
  assert.equal(taskAuditPostStatus(log(20, -20, '', { actionType: '撤回提交' }), ''), '已撤回')
})


test('step-back returns invalidate earlier passes while retaining the real return actor', () => {
  const returned = log(20, 10, '2026-06-26T10:00:00+08:00', { operatorId: 99, logId: 3000 })
  assert.equal(currentTaskStageAudit(audit(10), [groupPass, returned], 'dept'), null)
  assert.equal(currentTaskStageAudit(audit(10), [groupPass, returned], 'principal').result, '退回至专业群审核')
  const again = log(10, 20, '2026-06-27T10:00:00+08:00', { operatorId: 42, logId: 3001 })
  assert.equal(currentTaskStageAudit(audit(20), [groupPass, returned, again], 'dept').operatorId, 42)
  assert.equal(currentTaskStageAudit(audit(20), [groupPass, returned, again], 'principal'), null)
  assert.match(taskStageDescription(audit(20, { currentHandlerId: 99 }), [groupPass, returned, again], 'principal', 'Configured', () => 'Current reviewer', String), /Current reviewer.*进行中/)
})

test('administrator return cannot leave a previous principal approval displayed as current', () => {
  const passed = log(20, 30, '2026-06-26T10:00:00+08:00', { operatorId: 51 })
  const returned = log(30, 20, '2026-06-27T10:00:00+08:00', { operatorId: 52 })
  const records = [groupPass, passed, returned, oldArchive]
  assert.equal(currentTaskStageAudit(audit(20), records, 'principal'), null)
  assert.equal(currentTaskStageAudit(audit(20), records, 'admin').result, '退回至归口审核')
  assert.equal(currentTaskStageAudit(audit(20), records, 'dept').result, '审核通过')
  assert.equal(taskAuditPostStatus(returned, ''), '退回至归口审核')
})

test('a repaired legacy administrator return invalidates the former principal pass without rewriting logs', () => {
  const passed = log(20, 30, '2026-11-03T17:12:09+08:00', { operatorId: 120796 })
  const returned = log(30, -10, '2026-11-03T17:14:35+08:00', { operatorId: 110228 })
  const records = [groupPass, passed, returned, oldArchive]
  const pending = audit(20, { currentHandlerId: 120796 })
  assert.equal(taskWorkflowStep(pending, records), 2)
  assert.equal(currentTaskStageAudit(pending, records, 'principal'), null)
  assert.match(taskStageDescription(pending, records, 'principal', 'Configured', id => id === 120796 ? 'Current principal' : '', String), /Current principal.*进行中/)
  assert.equal(currentTaskStageAudit(pending, records, 'admin').result, '退回至归口审核')
  assert.equal(returned.postStatus, -10)
  // Until the database is repaired, a closed old return must not pretend to be an active review.
  assert.equal(currentTaskStageAudit(audit(-10, { isDelete: 1 }), records, 'admin').result, '驳回')
})
