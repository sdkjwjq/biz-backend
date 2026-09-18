const { chromium, request } = require('../../target/ui-audit-tools/node_modules/playwright');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');
async function main() {
  const root = path.resolve(__dirname, '../../target/ui-audit');
  assert.match(JSON.parse(await fs.readFile(path.join(root, 'state.json'))).schema, /^biz_review_test_[0-9a-f]{32}$/);
  const api = await request.newContext({ baseURL: 'http://127.0.0.1:15173' });
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const results = [];
  try {
    const login = async id => (await (await api.post('/api/system/login', { data: { user_id: id, password: 'review-fixture-password' } })).json()).token;
    const a = await login(910001), b = await login(910004);
    assert.ok(a && b);
    const context = await browser.newContext();
    await context.addInitScript(token => localStorage.setItem('token', token), a);
    await context.route('**/*', route => new URL(route.request().url()).hostname === '127.0.0.1' ? route.continue() : route.abort());
    const page = await context.newPage();
    await page.goto('http://127.0.0.1:15173/home/works');
    await page.locator('.works-container').waitFor();
    for (const httpStatus of [200, 401]) {
      let release, reached;
      const gate = new Promise(resolve => { release = resolve; });
      const arrived = new Promise(resolve => { reached = resolve; });
      await page.route('**/api/__closure_stale', async route => {
        reached(); await gate;
        await route.fulfill({ status: httpStatus, contentType: 'application/json', body: JSON.stringify({ code: 401, message: 'Invalid token' }) });
      });
      await page.evaluate(async token => {
        localStorage.setItem('token', token);
        const { createWorksCache } = await import('/src/utils/worksCache.js');
        window.oldCache = createWorksCache();
        window.oldCache.setItem('works_edge_test', 'A');
        const { get } = await import('/src/utils/request.js');
        window.staleRequest = get('/__closure_stale').catch(() => 'rejected');
      }, a);
      await arrived;
      await page.evaluate(token => localStorage.setItem('token', token), b);
      release();
      assert.equal(await page.evaluate(() => window.staleRequest), 'rejected');
      assert.equal(await page.evaluate(token => localStorage.getItem('token') === token, b), true);
      assert.equal(new URL(page.url()).pathname, '/home/works');
      const isolated = await page.evaluate(async () => {
        const { createWorksCache } = await import('/src/utils/worksCache.js');
        const cache = createWorksCache();
        window.oldCache.setItem('works_edge_test', 'late A');
        return cache.getItem('works_edge_test') === null && !window.oldCache.isCurrent();
      });
      assert.equal(isolated, true);
      await page.unroute('**/api/__closure_stale');
      results.push({ httpStatus, stale401DoesNotLogoutNewSession: true, staleCacheWriteIsolated: true });
    }
    // UI boundary fixture: all 15 real notices are presented as unread; read operations use the real API.
    await page.route('**/api/system/notice', async route => {
      const response = await route.fetch();
      const rows = await response.json();
      await route.fulfill({ response, json: rows.map(row => ({ ...row, isRead: 0 })) });
    });
    await page.goto('http://127.0.0.1:15173/home/notice');
    await page.locator('.notice-item').first().waitFor();
    await page.getByRole('tab', { name: '未读消息', exact: true }).click();
    await page.locator('.el-pagination .btn-next').click();
    assert.equal(await page.locator('.notice-item').count(), 5);
    for (let count = 5; count > 0; count--) {
      await page.locator('.notice-item').first().click();
      await page.getByRole('dialog', { name: '消息详情' }).waitFor();
      await page.waitForFunction(expected => document.querySelectorAll('.notice-item').length === expected, count === 1 ? 10 : count - 1);
      await page.getByRole('dialog', { name: '消息详情' }).getByRole('button', { name: 'Close this dialog' }).click();
    }
    assert.equal(await page.locator('.el-pager .is-active').innerText(), '1');
    await page.getByRole('button', { name: '全部已读', exact: true }).click();
    await page.getByText('暂无消息', { exact: true }).waitFor();
    assert.equal(await page.locator('.pagination-container').count(), 0);
    results.push({ noticeLastPageClamped: true, unreadEmptyState: true });
    // Cover each existing business status code, including historical rows that must not count.
    for (const [url, states] of [
      ['biz/audit/todo', [10, 20, 30, 40]],
      ['performance/audit/todo', [10, 20, -1]],
      ['achievement/audit/todo', [10, 20, -1]]
    ]) {
      await page.route(`**/api/${url}`, route => route.fulfill({
        status: 200, contentType: 'application/json', body: JSON.stringify(states.map(flowStatus => ({ flowStatus })))
      }));
    }
    await page.goto('http://127.0.0.1:15173/home/notice');
    const badge = page.getByRole('menuitem', { name: /审核中心/ }).locator('.el-badge__content');
    await badge.getByText('6', { exact: true }).waitFor();
    results.push({ mixedAuditTypes: 6, historyExcluded: true });
    await fs.writeFile(path.join(root, 'evidence-fixed/edges.json'), JSON.stringify(results, null, 2));
    console.log(JSON.stringify(results));
  } finally { await browser.close(); await api.dispose(); }
}
main().catch(error => { console.error(error); process.exitCode = 1; });
