// Keep the session valid until all page leave guards have accepted navigation.
// This state is private to the active router, never supplied by a URL parameter.
const pendingLogouts = new WeakMap()

export const isLogoutNavigation = (router, to, token) => {
  const pending = pendingLogouts.get(router)
  return !!pending && !pending.committed && pending.token === token && to.fullPath === '/login'
}

export async function navigateToLogout(router, token, commitLogout) {
  if (pendingLogouts.has(router)) return false
  const pending = { token, committed: false }
  pendingLogouts.set(router, pending)
  const removeGuard = router.beforeResolve(to => {
    if (!isLogoutNavigation(router, to, localStorage.getItem('token'))) return
    // Start the request and clear local state synchronously, after draft guards
    // and route resolution. There is no second navigation or repeated prompt.
    commitLogout()
    pending.committed = true
  })
  try {
    const failure = await router.push('/login')
    return !failure && pending.committed
  } finally {
    removeGuard()
    if (pendingLogouts.get(router) === pending) pendingLogouts.delete(router)
  }
}
