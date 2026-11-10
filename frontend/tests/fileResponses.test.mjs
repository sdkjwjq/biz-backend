import test from 'node:test'
import assert from 'node:assert/strict'
import { responseErrorMessage, readDownloadResponse, downloadFilename, isJsonResponse } from '../src/utils/fileResponses.mjs'

test('structured server errors never become object strings or proxy HTML', () => {
  assert.equal(responseErrorMessage({ status: 500, error: 'Internal Server Error', path: '/api/private' }, '导出失败'), '导出失败')
  assert.equal(responseErrorMessage({ message: { detail: '没有导出权限' } }), '没有导出权限')
  assert.equal(responseErrorMessage('<html><body>502 Bad Gateway</body></html>', '导出失败'), '导出失败')
  assert.equal(responseErrorMessage('[object Object]', '导出失败'), '导出失败')
  const cycle = {}; cycle.message = cycle
  assert.equal(responseErrorMessage(cycle, '导出失败'), '导出失败')
  assert.equal(isJsonResponse('application/problem+json; charset=UTF-8'), true)
})

test('download reader rejects HTTP and business JSON errors, including success-status error objects', async () => {
  for (const status of [200, 403, 500]) {
    const response = new Response(JSON.stringify({ code: 403, message: '没有导出权限' }), { status, headers: { 'content-type': 'application/json' } })
    await assert.rejects(readDownloadResponse(response, 'task.xlsx'), e => e.message === '没有导出权限' && e.responseCode === 403)
  }
  await assert.rejects(readDownloadResponse(new Response(JSON.stringify({ status: 500, error: 'Internal Server Error' }), { status: 500, headers: { 'content-type': 'application/json' } }), 'task.xlsx'), /导出失败/)
  await assert.rejects(readDownloadResponse(new Response('{broken', { status: 500, headers: { 'content-type': 'application/problem+json' } }), 'task.xlsx'), /导出失败/)
  await assert.rejects(readDownloadResponse(new Response(JSON.stringify({ code: 200, message: '成功' }), { headers: { 'content-type': 'application/json' } }), 'task.xlsx'), /导出失败/)
})

test('download reader preserves bytes and encoded filenames; empty files and proxy login pages are rejected', async () => {
  const bytes = new Uint8Array([80, 75, 3, 4, 0, 255])
  const result = await readDownloadResponse(new Response(bytes, { headers: { 'content-type': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 'content-disposition': "attachment; filename*=UTF-8''%E4%BB%BB%E5%8A%A1.xlsx" } }), 'fallback.xlsx')
  assert.equal(result.filename, '任务.xlsx')
  assert.deepEqual(new Uint8Array(await result.blob.arrayBuffer()), bytes)
  assert.equal(downloadFilename('attachment; filename="budget.xlsx"'), 'budget.xlsx')
  await assert.rejects(readDownloadResponse(new Response(''), 'task.xlsx'), /文件为空/)
  await assert.rejects(readDownloadResponse(new Response('<html>login</html>', { headers: { 'content-type': 'text/html' } }), 'task.xlsx'), /导出失败/)
})
