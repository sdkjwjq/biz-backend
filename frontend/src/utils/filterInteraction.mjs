const clone = value => value === undefined ? undefined : JSON.parse(JSON.stringify(value))
export const filterKeys = key => [key, `${key}From`, `${key}To`]
export const asMany = value => value == null || value === '' ? [] : Array.isArray(value) ? value : [value]
export const asSingle = value => Array.isArray(value) ? value.length === 1 ? value[0] : '' : value ?? ''

export function basicViews(fields) {
  return fields.filter(f => f.basic).map(f => f.type === 'multi' ? { ...f, type: 'select', source: f } : f)
}

export function advancedViews(fields) {
  return fields.flatMap(f => !f.basic ? [f] : f.type === 'multi' ? [{ ...f, basic: false, source: f, label: `${f.label}（多选）`, group: '多项选择' }] : [])
}

// Only basic edits may auto-apply. Pending advanced edits must stay pending.
export function basicQuery(applied, draft, fields, advancedDraftKeys = new Set()) {
  const result = clone(applied)
  for (const field of fields.filter(f => f.basic && !advancedDraftKeys.has(f.key))) {
    for (const key of filterKeys(field.key)) {
      if (Object.hasOwn(draft, key)) result[key] = clone(draft[key])
      else delete result[key]
    }
  }
  return result
}

export function clearAdvancedQuery(draft, applied, fields) {
  const result = clone(draft)
  for (const field of fields) {
    if (!field.basic) for (const key of filterKeys(field.key)) delete result[key]
    else if (field.type === 'multi' && asMany(result[field.key]).length > 1) {
      if (asMany(applied[field.key]).length === 1) result[field.key] = clone(applied[field.key])
      else delete result[field.key]
    }
  }
  return result
}

export function createFilterTrigger(callback, delay = 350) {
  let timer
  const cancel = () => { clearTimeout(timer); timer = undefined }
  const flush = () => { cancel(); return callback() }
  return { cancel, flush, schedule: (text = false) => { cancel(); timer = setTimeout(flush, text ? delay : 0) } }
}
