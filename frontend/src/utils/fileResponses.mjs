/** Keep error objects and proxy HTML out of download files and user-facing messages. */
export const isJsonResponse = contentType => /(?:^|[/+])json(?:\s*;|$)/i.test(contentType || '')
export const isHtmlResponse = contentType => /text\/html|application\/xhtml\+xml/i.test(contentType || '')

export function responseErrorMessage(value, fallback = '请求失败，请稍后重试') {
  const seen = new Set()
  function message(input, depth = 0) {
    if (typeof input === 'string') {
      const text = input.trim()
      if (!text || text === '[object Object]' || /<!doctype|<html|<body/i.test(text)) return ''
      if (/^(Internal Server Error|Bad Gateway|Service Unavailable|Gateway Timeout|Forbidden|Not Found)$/i.test(text)) return ''
      if (/^[{[]/.test(text)) {
        try { return message(JSON.parse(text), depth + 1) } catch { return '' }
      }
      return text
    }
    if (!input || typeof input !== 'object' || depth > 4 || seen.has(input)) return ''
    seen.add(input)
    for (const key of ['message', 'detail', 'error', 'title']) {
      const text = message(input[key], depth + 1)
      if (text) return text
    }
    return ''
  }
  return message(value) || fallback
}

export function downloadFilename(contentDisposition) {
  const star = (contentDisposition || '').match(/filename\*\s*=\s*[^']*''([^;]+)/i)
  const normal = (contentDisposition || '').match(/filename\s*=\s*"?([^";]+)"?/i)
  const raw = (star?.[1] || normal?.[1] || '').trim()
  try { return decodeURIComponent(raw) } catch { return raw }
}

export async function readDownloadResponse(response, defaultFilename, fallback = '导出失败，请稍后重试') {
  const type = response.headers.get('content-type') || ''
  if (!response.ok || isJsonResponse(type) || isHtmlResponse(type)) {
    const text = await response.text()
    let data = text
    if (isJsonResponse(type)) {
      try { data = JSON.parse(text) } catch { data = null }
    }
    const code = Number(data?.code ?? data?.status ?? response.status)
    const message = !response.ok || code >= 400 ? responseErrorMessage(data, fallback) : fallback
    throw Object.assign(new Error(message), { responseCode: code })
  }
  const blob = await response.blob()
  if (!blob.size) throw new Error('导出文件为空，请重试')
  return { blob, filename: downloadFilename(response.headers.get('content-disposition')) || defaultFilename }
}
