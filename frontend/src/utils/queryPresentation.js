// Query parameters have string wire values; object and multi-select order do
// not change the meaning of a filter.
export const queryFingerprint = query => JSON.stringify(Object.entries(query || {})
  .filter(([, value]) => Array.isArray(value) ? value.length : value !== '' && value != null)
  .sort(([a], [b]) => a.localeCompare(b))
  .map(([key, value]) => [key, Array.isArray(value) ? value.map(String).sort() : String(value)]))
