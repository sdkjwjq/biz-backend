import test from 'node:test'
import assert from 'node:assert/strict'
import { queryFingerprint } from '../src/utils/queryPresentation.js'

test('parameter key order does not make an unchanged query dirty', () => {
  assert.equal(queryFingerprint({ year: '2026', status: '1' }), queryFingerprint({ status: '1', year: '2026' }))
})
test('empty fields equal their omitted wire representation', () => {
  assert.equal(queryFingerprint({ year: '2026', keyword: '', owner: [], status: null }), queryFingerprint({ year: '2026' }))
})
test('numeric defaults and string control values identify the same query', () => {
  assert.equal(queryFingerprint({ year: 2026, month: 6 }), queryFingerprint({ year: '2026', month: '6' }))
})
test('multi-select order is not a business filter change', () => {
  assert.equal(queryFingerprint({ owner: [2, 10] }), queryFingerprint({ owner: ['10', '2'] }))
})
test('zero and false remain meaningful values', () => {
  assert.notEqual(queryFingerprint({ progressFrom: 0 }), queryFingerprint({}))
  assert.notEqual(queryFingerprint({ active: false }), queryFingerprint({}))
})
test('real changes to years, text and range boundaries remain distinguishable', () => {
  for (const [a, b] of [[{ year: 2026 }, { year: 2027 }], [{ keyword: '甲' }, { keyword: '乙' }], [{ progressTo: 50 }, { progressTo: 51 }]]) {
    assert.notEqual(queryFingerprint(a), queryFingerprint(b))
  }
})
