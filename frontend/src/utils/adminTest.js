import { reactive } from 'vue'

const administrator = reactive({ token: '', active: false })
// The server's current access response is the only source for administrator controls.
export function setAdministratorAccess(active, token) {
  Object.assign(administrator, { active: active === true, token })
}

export const isAdminTestEnabled = () => administrator.active && administrator.token === localStorage.getItem('token')

export function clearAdminTest() {
  Object.assign(administrator, { token: '', active: false })
  try { sessionStorage.removeItem('shuanggao.adminTest') } catch { /* Storage is optional. */ }
}

/** Keep existing API callers compatible; administrator authorization is resolved by the server. */
export function adminTestFetch(input, options = {}) {
  return fetch(input, options)
}
