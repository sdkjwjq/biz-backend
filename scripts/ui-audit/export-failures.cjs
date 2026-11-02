// Export failure/retry UI checks. Only the disposable serve.py environment is used.
const { chromium, request } = require('playwright');
const { expect } = require('playwright/test');
const fs = require('node:fs/promises'), path = require('node:path'), assert = require('node:assert/strict');
(async () => {
  const state = JSON.parse(await fs.readFile(process.env.SHUANGGAO_UI_STATE_FILE, 'utf8'));
  const out = path.join(path.dirname(process.env.SHUANGGAO_UI_STATE_FILE), 'export-failure-evidence');
  await fs.mkdir(out, { recursive: true });
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const api = await request.newContext({ baseURL: state.frontend });
  const checks = [], errors = [], downloads = [];
  let page;
  try {
    const login = await api.post('/api/system/login', { data: { user_id: 110228, password: 'review-fixture-password' } });
    assert.equal(login.status(), 200);let token = (await login.json()).token;
    const selected = await api.post('/api/system/review-context', { headers: { Authorization: token }, data: { experience: 'BUSINESS' } });
    assert.equal(selected.status(), 200);token = (await selected.json()).token;
    const context = await browser.newContext({ viewport: { width: 1440, height: 900 } });
    await context.addInitScript(t => { if (!sessionStorage.getItem('failure-seed')) { localStorage.setItem('token', t);sessionStorage.setItem('failure-seed', '1'); } }, token);
    page = await context.newPage();page.setDefaultTimeout(20000);
    page.on('pageerror', e => errors.push(e.message));page.on('download', d => downloads.push(d));
    const ready = page.waitForResponse(r => r.url().includes('/api/list-filters/tasks?') && r.status() === 200);
    await page.goto(state.frontend + '/home/works');await ready;
    const cases = [
      { name: 'structured-500', status: 500, type: 'application/json', body: JSON.stringify({ status: 500, error: 'Internal Server Error', path: '/api/data-exports/tasks' }), text: /服务暂时|导出失败/ },
      { name: 'business-200-error', status: 200, type: 'application/problem+json', body: JSON.stringify({ code: 403, message: { detail: '合成测试：没有导出权限' } }), text: /没有导出权限/ },
      { name: 'html-proxy-error', status: 502, type: 'text/html', body: '<html><body>502 Bad Gateway</body></html>', text: /服务暂时|导出失败/ },
      { name: 'json-success-is-not-file', status: 200, type: 'application/json', body: JSON.stringify({ code: 200, message: '成功' }), text: /未返回文件|导出失败/ },
      { name: 'empty-file', status: 200, type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', body: '', text: /文件为空/ }
    ];
    for (const [name, endpoint] of [['导出全量明细', '/api/data-exports/tasks'], ['导出筛选结果', '/api/list-filters/tasks/export']]) {
      const button = page.getByRole('button', { name, exact: true });
      await expect(button).toBeVisible();
      for (const item of cases) {
        const matcher = url => url.pathname === endpoint;
        const handler = route => route.fulfill({ status: item.status, contentType: item.type, body: item.body });
        await page.route(matcher, handler);
        const before = downloads.length;
        await button.click();
        const toast = page.locator('.el-message--error').last();await expect(toast).toBeVisible();await expect(toast).toContainText(item.text);
        await expect(page.getByText('[object Object]', { exact: true })).toHaveCount(0);
        await expect(button).toBeEnabled();assert.equal(downloads.length, before);
        // Clear a previous toast so the next assertion cannot accidentally match it.
        await expect(toast).not.toBeVisible({ timeout: 7000 });
        await page.unroute(matcher, handler);
        checks.push(name + ': ' + item.name + ' gives one readable error, no fake file and recovers the button');
      }
      const event = page.waitForEvent('download');await button.click();const download = await event;
      const file = path.join(out, endpoint.includes('list-filters') ? 'filtered-retry.xlsx' : 'all-retry.xlsx');
      await download.saveAs(file);assert.equal((await fs.readFile(file)).subarray(0, 2).toString(), 'PK');
      checks.push(name + ': retry downloads a real workbook');
    }
    await page.screenshot({ path: path.join(out, 'task-export-fixed-1440.png'), fullPage: true });
    assert.deepEqual(errors, []);
    await fs.writeFile(path.join(out, 'results.json'), JSON.stringify({ passed: true, checks, errors }, null, 2));
    console.log(JSON.stringify({ passed: true, checks: checks.length }));
  } catch (e) {
    if (page) await page.screenshot({ path: path.join(out, 'failure.png'), fullPage: true });
    console.error(JSON.stringify({ checks, errors }));throw e;
  } finally { await api.dispose();await browser.close(); }
})().catch(e => { console.error(e.stack || e);process.exitCode = 1; });
