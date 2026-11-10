import test from 'node:test'
import assert from 'node:assert/strict'
import { reactive, effect, stop } from 'vue'
import { permissionOverrideValue } from '../src/utils/permissionOverrides.mjs'

test('an inherited permission reacts immediately to allow, deny and removal without unrelated edits', () => {
  const form = reactive({ overrides: {} }), displayed = []
  const render = effect(() => displayed.push(permissionOverrideValue(form.overrides, 'budget.read')))
  form.overrides['budget.read'] = true
  form.overrides['budget.read'] = false
  delete form.overrides['budget.read']
  assert.deepEqual(displayed, ['inherit', 'allow', 'deny', 'inherit'])
  stop(render)
})

test('explicit false survives reload and modules remain independent when overrides are added or removed', () => {
  const form = reactive({ overrides: JSON.parse('{"records.read":false}') })
  let displayed
  const render = effect(() => { displayed = ['records.read', 'budget.read', 'tasks.read'].map(code => permissionOverrideValue(form.overrides, code)) })
  assert.deepEqual(displayed, ['deny', 'inherit', 'inherit'])
  form.overrides['budget.read'] = true
  assert.deepEqual(displayed, ['deny', 'allow', 'inherit'])
  delete form.overrides['records.read']
  assert.deepEqual(displayed, ['inherit', 'allow', 'inherit'])
  form.overrides = JSON.parse('{"tasks.read":false,"budget.read":true}')
  assert.deepEqual(displayed, ['inherit', 'allow', 'deny'])
  stop(render)
})
