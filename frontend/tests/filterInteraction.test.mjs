import test from 'node:test'
import assert from 'node:assert/strict'
import { basicViews, advancedViews, basicQuery, clearAdvancedQuery, createFilterTrigger, asSingle } from '../src/utils/filterInteraction.mjs'

const fields = [
  { key: 'year', type: 'select', basic: true, options: [] },
  { key: 'deptId', type: 'multi', label: '归口部门', basic: true, remote: true, options: [] },
  { key: 'keyword', type: 'text', basic: true, options: [] },
  { key: 'stage', type: 'multi', basic: false, group: '审核', options: [] },
  { key: 'progress', type: 'number', basic: false, group: '进度', options: [] }
]

test('single basic and multi advanced views share one backend field without changing definitions', () => {
  const basic = basicViews(fields), advanced = advancedViews(fields)
  assert.equal(basic.find(f => f.key === 'deptId').type, 'select')
  assert.equal(advanced.find(f => f.key === 'deptId').type, 'multi')
  assert.equal(advanced.find(f => f.key === 'deptId').source, fields[1])
  assert.equal(fields[1].type, 'multi')
  assert.equal(asSingle(['1', '2']), '')
  assert.equal(asSingle(['1']), '1')
})

test('automatic basic changes preserve applied advanced filters and exclude pending advanced edits', () => {
  const applied = { year: '2026', deptId: ['1'], stage: ['10'], progressFrom: 0 }
  const draft = { year: '2027', deptId: ['2', '3'], stage: ['20'], progressFrom: 80, keyword: '任务' }
  const query = basicQuery(applied, draft, fields, new Set(['deptId', 'stage', 'progress']))
  assert.deepEqual(query, { year: '2027', deptId: ['1'], stage: ['10'], progressFrom: 0, keyword: '任务' })
  assert.deepEqual(applied, { year: '2026', deptId: ['1'], stage: ['10'], progressFrom: 0 })
})

test('choosing a basic department replaces an applied multi selection; clearing can be undefined', () => {
  assert.deepEqual(basicQuery({ deptId: ['1', '2'] }, { deptId: ['3'] }, fields), { deptId: ['3'] })
  assert.deepEqual(basicQuery({ keyword: 'old' }, { keyword: undefined }, fields), { keyword: undefined })
})

test('clear advanced keeps basic conditions and removes multi department scopes', () => {
  assert.deepEqual(clearAdvancedQuery({ year: '2026', deptId: ['1', '2'], stage: ['10'], progressFrom: 80 }, { deptId: ['9'] }, fields), { year: '2026', deptId: ['9'] })
  assert.deepEqual(clearAdvancedQuery({ deptId: ['1', '2'] }, { deptId: ['1', '2'] }, fields), {})
})

test('typing coalesces, selection flushes, and disposal cancels pending automatic requests', async () => {
  const calls = [], trigger = createFilterTrigger(() => calls.push('query'), 20)
  trigger.schedule(true); trigger.schedule(true)
  await new Promise(resolve => setTimeout(resolve, 45))
  assert.equal(calls.length, 1)
  trigger.schedule(true); trigger.flush()
  await new Promise(resolve => setTimeout(resolve, 35))
  assert.equal(calls.length, 2)
  trigger.schedule(true); trigger.cancel()
  await new Promise(resolve => setTimeout(resolve, 35))
  assert.equal(calls.length, 2)
})
