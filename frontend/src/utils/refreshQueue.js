/** Coalesce refresh triggers, preserving a follow-up for changes during an active read. */
export function createRefreshQueue(task, { delay = 80 } = {}) {
  let pending = null
  let timer = null
  let running = false
  let repeat = false
  let disposed = false
  let resolvePending
  let rejectPending

  async function flush() {
    timer = null
    running = true
    try {
      do {
        repeat = false
        await task()
      } while (repeat && !disposed)
      resolvePending()
    } catch (error) {
      rejectPending(error)
    } finally {
      running = false
      pending = null
    }
  }

  function refresh({ force = false } = {}) {
    if (disposed) return Promise.resolve()
    if (pending) {
      if (running && force) repeat = true
      return pending
    }
    pending = new Promise((resolve, reject) => {
      resolvePending = resolve
      rejectPending = reject
    })
    const result = pending
    if (force) timer = setTimeout(flush, delay)
    else void flush()
    return result
  }

  function dispose() {
    disposed = true
    repeat = false
    if (timer !== null) {
      clearTimeout(timer)
      timer = null
      resolvePending()
      pending = null
    }
  }

  return { refresh, dispose }
}
