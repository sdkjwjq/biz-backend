export const reportDate = value => {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  const parts = new Intl.DateTimeFormat('en-CA', { timeZone: 'Asia/Shanghai', year: 'numeric', month: '2-digit', day: '2-digit' }).formatToParts(date)
  return ['year', 'month', 'day'].map(type => parts.find(part => part.type === type)?.value).join('-')
}

export const reportStatus = row => {
  const status = row.auditStatus ?? row.audit_status
  if (status === null || status === undefined) return 'unknown'
  if (Number(status) === 30) return 'archived'
  if (Number(status) === 10) return 'pending'
  if (Number(status) < 0) return 'returned'
  return 'unknown'
}
export const reportStatusNames = { archived: '已归档', pending: '待归档', returned: '已退回', unknown: '状态未知' }
export const awardQuantity = row => ['teDengJiang', 'yiDengJiang', 'erDengJiang', 'sanDengJiang', 'jinJiang', 'yinJiang', 'tongJiang', 'youShengJiang', 'budDengDengCi']
  .reduce((total, key) => total + (Number(row[key]) || 0), 0)

// Excel 打开时保留中文；文本加引号并防止被解释为公式。
export const reportCsv = rows => '\uFEFF' + rows.map(row => row.map(value => {
  let text = String(value ?? '')
  if (typeof value !== 'number' && /^[\s]*[=+\-@\t\r\n]/.test(text)) text = "'" + text
  return '"' + text.replaceAll('"', '""') + '"'
}).join(',')).join('\r\n')

export const downloadReport = (rows, name) => {
  const url = URL.createObjectURL(new Blob([reportCsv(rows)], { type: 'text/csv;charset=utf-8;' }))
  const link = document.createElement('a')
  link.href = url; link.download = name; document.body.appendChild(link); link.click(); link.remove()
  setTimeout(() => URL.revokeObjectURL(url), 1000)
}
