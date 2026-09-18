const { chromium, request } = require('../../target/ui-audit-tools/node_modules/playwright');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');
const BASE = 'http://127.0.0.1:15273';
async function main() {
  const root = path.resolve(__dirname, '../../target/ui-audit');
  const state = JSON.parse(await fs.readFile(path.join(root, 'state.json')));
  assert.match(state.schema, /^biz_review_test_[0-9a-f]{32}$/); assert.equal(state.frontend, BASE);
  const output = path.join(root, 'evidence-audit-badge-immediate'); await fs.mkdir(output, { recursive: true });
  const api = await request.newContext({ baseURL: BASE });
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const results = [], errors = [];
  let login = await (await api.post('/api/system/login', { data: { user_id: 910003, password: 'review-fixture-password' } })).json();
  if (!login.token) login = await (await api.post('/api/system/login', { data: { user_id: 910003, password: 'Review123' } })).json();
  assert.ok(login.token); const headers = { Authorization: login.token };
  await api.post('/api/system/password', { headers, data: { new_password: 'Review123' } });
  const create = async () => {
    const context = await browser.newContext({ viewport: { width: 1440, height: 1000 } });
    await context.addInitScript(token => localStorage.setItem('token', token), login.token);
    await context.route('**/*', route => new URL(route.request().url()).hostname === '127.0.0.1' ? route.continue() : route.abort());
    const page = await context.newPage(); page.on('pageerror', error => errors.push(error.message));
    return { context, page };
  };
  const badge = page => page.getByRole('menuitem', { name: /审核中心/ }).locator('.el-badge');
  const count = async (page, expected) => {
    if (expected === 0) await badge(page).waitFor({ state: 'hidden', timeout: 5000 });
    else await badge(page).getByText(String(expected), { exact: true }).waitFor({ timeout: 5000 });
  };
  const approve = async (page, reject = false) => {
    await page.locator('.audit-container .el-table__body').getByRole('button', { name: '审批', exact: true }).first().click();
    const dialog = page.getByRole('dialog', { name: '业务审批', exact: true });
    await dialog.waitFor();
    if (reject) await dialog.getByText('驳回', { exact: true }).click();
    await dialog.getByPlaceholder('请输入审批意见（必填）...').fill('合成即时角标测试');
    await dialog.getByRole('button', { name: '确认提交', exact: true }).click();
    await dialog.waitFor({ state: 'hidden' });
  };
  try {
    const { context, page } = await create();
    await page.clock.install();
    await page.goto(BASE + '/home/audit'); await count(page, 15);
    await approve(page); await count(page, 14);
    console.log('Real approval: 15 -> 14');
    const pending = await (await api.get('/api/performance/audit/todo', { headers })).json();
    assert.equal(pending.length, 14);
    for (const row of pending.slice(0, 13)) {
      const result = await api.post('/api/performance/audit', { headers, data: { sub_id: row.subId, is_pass: true, title: '合成样本准备', content: '合成样本准备' } });
      assert.match(await result.text(), /成功|通过/);
    }
    await page.reload(); await count(page, 1);
    await page.locator('.audit-container .el-loading-mask').waitFor({ state: 'hidden' });
    await page.clock.pauseAt(new Date(Date.now() + 100));
    let release, arrived;
    const gate = new Promise(resolve => { release = resolve; });
    const reached = new Promise(resolve => { arrived = resolve; });
    await page.route('**/api/performance/audit/todo', async route => {
      const response = await route.fetch(); const json = await response.json(); arrived(); await gate;
      await route.fulfill({ response, json });
    }, { times: 1 });
    await page.clock.runFor(30001);
    await page.clock.resume();
    await Promise.race([reached, new Promise((_, reject) => setTimeout(() => reject(new Error('Poll did not start')), 10000))]);
    console.log('Stale poll held');
    await approve(page); await count(page, 0);
    release(); await page.waitForTimeout(300); await count(page, 0);
    assert.equal((await (await api.get('/api/performance/audit/todo', { headers })).json()).length, 0);
    await page.screenshot({ path: path.join(output, 'last-item-completed.png'), fullPage: true });
    results.push({ realPerformanceApproval: true, multiCount: '15 -> 14', finalCount: '1 -> 0', stalePollIgnored: true });
    console.log('Real approval: 1 -> 0; stale poll ignored');
    await context.close();
    // Contract tests for each frontend success branch and failure path, using explicitly injected responses.
    for (const type of ['task', 'performance', 'achievement']) {
      const { context, page } = await create(); let done = false, fail = true;
      const prefix = type === 'task' ? 'biz' : type;
      const row = { subId: 979001, taskId: 930002, perfId: 950011, achId: 979001,
        perfName: '合成待审绩效', achName: '合成待审成果', flowStatus: 10, submitBy: 910001, year: 2026 };
      for (const kind of ['biz', 'performance', 'achievement']) await page.route(`**/api/${kind}/audit/todo`, route => route.fulfill({ json: kind === prefix && !done ? [row] : [] }));
      await page.route(`**/api/${prefix}/audit`, route => {
        if (fail) return route.fulfill({ status: 500, json: { code: 500, message: '合成审核失败' } });
        done = true; return route.fulfill({ json: '审核成功' });
      });
      await page.goto(BASE + '/home/audit'); await count(page, 1);
      await page.locator('.audit-container .el-table__body').getByRole('button', { name: '审批', exact: true }).first().click();
      const dialog = page.getByRole('dialog', { name: '业务审批', exact: true }); await dialog.waitFor();
      await dialog.getByPlaceholder('请输入审批意见（必填）...').fill('合成失败后重试');
      await dialog.getByRole('button', { name: '确认提交', exact: true }).click();
      await page.getByText('合成审核失败', { exact: true }).waitFor(); await count(page, 1);
      fail = false; await dialog.getByText('驳回', { exact: true }).click();
      await dialog.getByRole('button', { name: '确认提交', exact: true }).click();
      await dialog.waitFor({ state: 'hidden' }); await count(page, 0);
      results.push({ type, injectedResponses: true, failedKeepsBadge: true, successfulRejectClearsBadge: true });
      await context.close();
    }
    assert.deepEqual(errors, []);
    await fs.writeFile(path.join(output, 'results.json'), JSON.stringify({ results, errors }, null, 2));
    console.log(JSON.stringify({ passed: results.length, errors }));
  } finally { await browser.close(); await api.dispose(); }
}
main().catch(error => { console.error(error); process.exitCode = 1; });
