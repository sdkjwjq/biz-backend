import { after, test } from 'node:test'
import assert from 'node:assert/strict'
import { createMemoryHistory, createRouter } from 'vue-router'
import { isLogoutNavigation, navigateToLogout } from '../src/utils/logoutNavigation.js'

const originalStorage = Object.getOwnPropertyDescriptor(globalThis, 'localStorage')
after(() => {
  if (originalStorage) Object.defineProperty(globalThis, 'localStorage', originalStorage)
  else delete globalThis.localStorage
})

async function fixture(leave = () => true) {
  const data = new Map([['token', 'synthetic-session']])
  const storage = { getItem: key => data.get(key) ?? null, removeItem: key => data.delete(key) }
  Object.defineProperty(globalThis, 'localStorage', { configurable: true, value: storage })
  const component = { render: () => null }
  const router = createRouter({ history: createMemoryHistory(), routes: [
    { path: '/home', component }, { path: '/login', component }, { path: '/other', component }
  ] })
  router.beforeEach(async to => {
    if (to.path !== '/login') return true
    if (!await leave()) return false
    if (storage.getItem('token') && !isLogoutNavigation(router, to, storage.getItem('token'))) return '/home'
  })
  await router.push('/home')
  let count = 0
  const commit = () => { count++; storage.removeItem('token') }
  return { router, data, storage, commit, count: () => count }
}

test('ordinary authenticated login visits retain the existing redirect', async () => {
  const f = await fixture()
  await f.router.push('/login')
  assert.equal(f.router.currentRoute.value.path, '/home')
  assert.equal(f.storage.getItem('token'), 'synthetic-session')
  assert.equal(f.count(), 0)
})

test('canceled leave preserves session and removes the temporary logout intent', async () => {
  const f = await fixture(() => false)
  assert.equal(await navigateToLogout(f.router, f.storage.getItem('token'), f.commit), false)
  assert.equal(f.router.currentRoute.value.path, '/home')
  assert.equal(f.storage.getItem('token'), 'synthetic-session')
  assert.equal(f.count(), 0)
  assert.equal(isLogoutNavigation(f.router, { fullPath: '/login' }, 'synthetic-session'), false)
})

test('async save finishes with a valid session before one logout commit', async () => {
  let release, entered
  const gate = new Promise(resolve => { release = resolve })
  const reached = new Promise(resolve => { entered = resolve })
  const f = await fixture(async () => { entered(); return await gate })
  const result = navigateToLogout(f.router, f.storage.getItem('token'), f.commit)
  await reached
  assert.equal(f.storage.getItem('token'), 'synthetic-session')
  assert.equal(f.count(), 0)
  release(true)
  assert.equal(await result, true)
  assert.equal(f.router.currentRoute.value.path, '/login')
  assert.equal(f.storage.getItem('token'), null)
  assert.equal(f.count(), 1)
})

test('duplicate attempts do not commit twice and a canceled attempt can be retried', async () => {
  let release, entered, allow = false
  const gate = new Promise(resolve => { release = resolve })
  const reached = new Promise(resolve => { entered = resolve })
  const f = await fixture(async () => { if (allow) return true; entered(); return await gate })
  const first = navigateToLogout(f.router, 'synthetic-session', f.commit)
  await reached
  assert.equal(await navigateToLogout(f.router, 'synthetic-session', f.commit), false)
  release(false)
  assert.equal(await first, false)
  allow = true
  assert.equal(await navigateToLogout(f.router, 'synthetic-session', f.commit), true)
  assert.equal(f.count(), 1)
})

test('a superseding navigation cancels logout without losing the session', async () => {
  let release, entered
  const gate = new Promise(resolve => { release = resolve })
  const reached = new Promise(resolve => { entered = resolve })
  const f = await fixture(async () => { entered(); return await gate })
  const result = navigateToLogout(f.router, 'synthetic-session', f.commit)
  await reached
  await f.router.push('/other')
  release(true)
  assert.equal(await result, false)
  assert.equal(f.router.currentRoute.value.path, '/other')
  assert.equal(f.storage.getItem('token'), 'synthetic-session')
  assert.equal(f.count(), 0)
})

test('navigation errors retain the session and clean up the logout guard', async () => {
  const error = new Error('Synthetic failed draft save')
  const f = await fixture(() => { throw error })
  f.router.onError(() => {})
  await assert.rejects(navigateToLogout(f.router, 'synthetic-session', f.commit), error)
  assert.equal(f.storage.getItem('token'), 'synthetic-session')
  assert.equal(f.count(), 0)
  assert.equal(isLogoutNavigation(f.router, { fullPath: '/login' }, 'synthetic-session'), false)
})
