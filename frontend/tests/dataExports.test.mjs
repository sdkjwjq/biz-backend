import test from 'node:test'
import assert from 'node:assert/strict'
import { dataExportUrl } from '../src/utils/dataExports.mjs'

test('full export ignores page, filters and budget subsection but preserves audit visibility tab', () => {
  const url = dataExportUrl('audits-done', 'all', { year: '2025', keyword: 'private' }, { page: 3, pageSize: 10, tab: 'done' })
  assert.equal(url, '/data-exports/audits?scope=all&tab=done')
  assert.equal(dataExportUrl('budget', 'all', { month: '5' }, { section: 'annual' }), '/data-exports/budget?scope=all')
})

test('filtered export uses applied multi values, literal Unicode and section context', () => {
  const url = new URL(dataExportUrl('tasks', 'filtered', { deptId: ['1', '2'], keyword: '任务&100%_😀', progressFrom: 0 }, {}), 'http://local')
  assert.deepEqual(url.searchParams.getAll('f.deptId'), ['1', '2'])
  assert.equal(url.searchParams.get('f.keyword'), '任务&100%_😀')
  assert.equal(url.searchParams.get('f.progressFrom'), '0')
  assert.throws(() => dataExportUrl('unknown', 'all'))
})
