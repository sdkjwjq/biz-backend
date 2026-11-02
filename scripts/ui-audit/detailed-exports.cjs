// Real clicks and downloads against serve.py's disposable production preview.
const { chromium, request } = require('playwright');
const { expect } = require('playwright/test');
const fs = require('node:fs/promises'), path = require('node:path'), assert = require('node:assert/strict');
(async () => {
  const state = JSON.parse(await fs.readFile(process.env.SHUANGGAO_UI_STATE_FILE, 'utf8'));
  const out = path.join(path.dirname(process.env.SHUANGGAO_UI_STATE_FILE), 'export-evidence');
  await fs.mkdir(out, { recursive: true });
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const api = await request.newContext({ baseURL: state.frontend });
  const checks = [], errors = [], legacy = [];
  let page;
  try {
    const login = await api.post('/api/system/login', { data: { user_id: 110228, password: 'review-fixture-password' } });
    assert.equal(login.status(), 200);let token = (await login.json()).token;
    const selected = await api.post('/api/system/review-context', { headers: { Authorization: token }, data: { experience: 'BUSINESS' } });
    assert.equal(selected.status(), 200);token = (await selected.json()).token;
    const context = await browser.newContext({ viewport: { width: 1440, height: 900 } });
    await context.addInitScript(t => { if (!sessionStorage.getItem('export-seed')) { localStorage.setItem('token', t);sessionStorage.setItem('export-seed', '1'); } }, token);
    page = await context.newPage();page.setDefaultTimeout(25000);
    page.on('pageerror', e => errors.push(e.message));
    page.on('request', r => { if (r.url().includes('/expert-reviews')) legacy.push(r.url()); });
    async function save(label, locator) {
      const event = page.waitForEvent('download');await locator.click();const download = await event;
      assert.match(download.suggestedFilename(), /\.xlsx$/);
      const file = path.join(out, label + '.xlsx');await download.saveAs(file);
      const bytes = await fs.readFile(file);assert.equal(bytes.subarray(0, 2).toString(), 'PK');
    }
    await page.goto(state.frontend + '/home/works');
    await expect(page.getByRole('button', { name: '导出全量明细', exact: true })).toBeVisible();
    await save('tasks-all', page.getByRole('button', { name: '导出全量明细', exact: true }));
    await save('tasks-filtered', page.getByRole('button', { name: '导出筛选结果', exact: true }));
    assert.deepEqual(legacy, []);await expect(page.getByText(/旧评审入口已停用/)).toHaveCount(0);
    await page.screenshot({ path: path.join(out, 'tasks-1440.png'), fullPage: true });
    checks.push('Both task buttons download business-detail Excel without any legacy expert request');
    for (const [route, name] of [
      ['/home/works/performance', 'performance'], ['/home/works/achievement', 'achievements'],
      ['/home/core-indicators', 'core'], ['/home/work-records', 'records'],
      ['/home/works/budget', 'budget'], ['/home/notice', 'notices'], ['/home/audit', 'audits'], ['/home/report', 'supervision'],
      ['/home/admin/accounts', 'users'], ['/home/admin/permissions', 'roles'],
      ['/home/admin/operations', 'operations'], ['/home/admin/notices', 'platform-notices'],
      ['/home/admin/review-tasks', 'review-tasks']
    ]) {
      await page.goto(state.frontend + route);
      const button = page.getByRole('button', { name: '导出全量明细', exact: true }).filter({ visible: true });
      await expect(button).toBeVisible();await save(name + '-all', button);
      checks.push(name + ': visible all-data button downloads a real workbook');
    }
    await page.goto(state.frontend + '/home/admin/accounts');
    await page.getByRole('tab', { name: '部门管理', exact: true }).click();
    await save('departments-all', page.getByRole('button', { name: '导出全量明细', exact: true }).filter({ visible: true }));
    checks.push('Department tab downloads all authorized department details');
    await page.goto(state.frontend + '/home/works/budget');
    await page.getByRole('button', { name: '版本记录', exact: true }).click();
    const history = page.getByRole('dialog', { name: '预算版本记录', exact: true });
    await save('budget-versions-all', history.getByRole('button', { name: '导出全量明细', exact: true }));
    checks.push('Budget version dialog provides an independent all-history export');
    await page.goto(state.frontend + '/home/works');await page.setViewportSize({ width: 960, height: 540 });
    const button = page.getByRole('button', { name: '导出全量明细', exact: true });
    await button.scrollIntoViewIfNeeded();await expect(button).toBeVisible();
    await page.screenshot({ path: path.join(out, 'tasks-960.png'), fullPage: true });
    checks.push('All-data action remains reachable in a 960×540 viewport');
    assert.deepEqual(errors, []);assert.deepEqual(legacy, []);
    await fs.writeFile(path.join(out, 'results.json'), JSON.stringify({ passed: true, checks, errors, legacy }, null, 2));
    console.log(JSON.stringify({ passed: true, checks: checks.length }));
  } catch (e) {
    if (page) { await page.screenshot({ path: path.join(out, 'failure.png'), fullPage: true });await fs.writeFile(path.join(out, 'failure.txt'), await page.locator('body').innerText()); }
    console.error(JSON.stringify({ checks, errors, legacy }));throw e;
  } finally { await api.dispose();await browser.close(); }
})().catch(e => { console.error(e.stack || e);process.exitCode = 1; });
