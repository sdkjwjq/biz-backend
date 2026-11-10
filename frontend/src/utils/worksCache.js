// 缓存按登录会话隔离；旧请求只能访问它启动时的会话。
export const createWorksCache = () => {
  const token = localStorage.getItem('token') || ''
  const suffix = token.split('.')[2] || 'anonymous'
  const isCurrent = () => !!token && localStorage.getItem('token') === token
  return {
    isCurrent,
    getItem(key) {
      try { return isCurrent() ? sessionStorage.getItem(`${key}:${suffix}`) : null } catch { return null }
    },
    setItem(key, value) {
      try { if (isCurrent()) sessionStorage.setItem(`${key}:${suffix}`, value) } catch { /* 缓存不可用不影响业务 */ }
    }
  }
}

export const clearWorksCache = () => {
  try {
    Object.keys(sessionStorage).filter(key => key.startsWith('works_')).forEach(key => sessionStorage.removeItem(key))
  } catch { /* 缓存不可用不影响退出 */ }
}
