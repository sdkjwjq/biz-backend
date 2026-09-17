const { chromium, request } = require('../../target/ui-audit-tools/node_modules/playwright');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');
async function main() {
  const root = path.resolve(__dirname, '../../target/ui-audit');
  assert.match(JSON.parse(await fs.readFile(path.join(root, 'state.json'))).schema, /^biz_review_test_[0-9a-f]{32}$/);
  const api = await request.newContext({ baseURL: 'http://127.0.0.1:15173' });
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  try {
    const login = await api.post('/api/system/login', { data: { user_id: 910003, password: 'review-fixture-password' } });
    const { token } = await login.json();
    const pending = await (await api.get('/api/performance/audit/todo', { headers: { Authorization: token } })).json();
    assert.ok(pending.length > 0);
    const context = await browser.newContext();
    await context.addInitScript(value => localStorage.setItem('token', value), token);
    await context.route('**/*', route => new URL(route.request().url()).hostname === '127.0.0.1' ? route.continue() : route.abort());
    const page = await context.newPage();
    await page.goto('http://127.0.0.1:15173/home/audit');
    await page.getByText(`${pending.length} 待处理`, { exact: true }).waitFor();
    const badgeCount = await page.getByRole('menuitem', { name: /审核中心/ }).locator('.el-badge').count();
    assert.equal(badgeCount, 0);
    await page.screenshot({ path: path.join(root, 'evidence-closure/badge.png'), fullPage: true });
    await fs.writeFile(path.join(root, 'evidence-closure/badge.json'), JSON.stringify({ pendingCount: pending.length, sidebarBadgeCount: badgeCount }, null, 2) + '\n');
    console.log(JSON.stringify({ pendingCount: pending.length, sidebarBadgeCount: badgeCount }));
  } finally { await browser.close(); await api.dispose(); }
}
main().catch(error => { console.error(error); process.exitCode = 1; });
