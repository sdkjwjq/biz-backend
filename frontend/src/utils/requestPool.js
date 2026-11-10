/** Component-owned bounded reads; cancelled queued work never starts. No response cache. */
export function createRequestPool(limit = 2) {
  if (!Number.isInteger(limit) || limit < 1) throw new RangeError('Invalid request limit')
  const queued = [], jobs = new Map()
  let active = 0, disposed = false
  function pump() {
    while (!disposed && active < limit && queued.length) {
      const job = queued.shift()
      active++
      Promise.resolve().then(job.execute).then(job.resolve, job.reject).finally(() => {
        active--
        if (jobs.get(job.key) === job) jobs.delete(job.key)
        pump()
      })
    }
  }
  function clearQueued() {
    for (const job of queued.splice(0)) {
      if (jobs.get(job.key) === job) jobs.delete(job.key)
      job.resolve(undefined)
    }
  }
  return {
    run(key, execute) {
      if (disposed) return Promise.resolve(undefined)
      if (jobs.has(key)) return jobs.get(key).promise
      const job = { key, execute }
      job.promise = new Promise((resolve, reject) => { job.resolve = resolve; job.reject = reject })
      jobs.set(key, job); queued.push(job); pump()
      return job.promise
    },
    clearQueued,
    dispose() { disposed = true; clearQueued() }
  }
}
