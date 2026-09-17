// Broad local smoke sweep; no business source modifications or real user data.
const { chromium, request } = require('../../target/ui-audit-tools/node_modules/playwright');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');
const baseURL = 'http://127.0.0.1:15173';
const output = path.resolve(__dirname, '../../target/ui-audit/evidence-closure');

async function main() {
  const state = JSON.parse(await fs.readFile(path.resolve(__dirname, '../../target/ui-audit/state.json')));
  assert.match(state.schema, /^biz_review_test_[0-9a-f]{32}$/);
  await fs.mkdir(output, { recursive: true });
  const api = await request.newContext({ baseURL });
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const result = { pages: [], api: [], findings: [] };
  const call = async (url, token, data) => {
    const response = await api.fetch('/api' + url, { method: data === undefined ? 'GET' : 'POST',
      headers: token ? { Authorization: token } : {}, data });
    const raw = await response.text();
    let body; try { body = JSON.parse(raw); } catch { body = raw; }
    return { status: response.status(), body };
  };
  try {
    let admin;
    for (const id of [110228, 910001, 910003, 1910001]) {
      const login = await call('/system/login', null, { user_id: id, password: 'review-fixture-password' });
      assert.ok(login.body.token);
      const token = login.body.token;
      if (id === 110228) admin = token;
      const context = await browser.newContext({ viewport: { width: 1440, height: 1000 } });
      await context.addInitScript(value => localStorage.setItem('token', value), token);
      await context.route('**/*', route => new URL(route.request().url()).hostname === '127.0.0.1' ? route.continue() : route.abort());
      const page = await context.newPage();
      let errors = [];
      page.on('pageerror', error => errors.push(error.message));
      const routes = id === 1910001 ? ['/home/works/achievement', '/home/works', '/home/audit']
        : ['/home/works', '/home/works/performance', '/home/notice', '/home/audit', '/home/report', '/dashboard'];
      if (id === 110228) routes.push('/home/works/achievement');
      for (const route of routes) {
        errors = [];
        await page.goto(baseURL + route);
        await page.waitForTimeout(700);
        await page.locator('.el-loading-mask:visible').first().waitFor({ state: 'hidden', timeout: 10000 }).catch(() => {});
        const text = await page.locator('body').innerText();
        result.pages.push({ id, route, actualPath: new URL(page.url()).pathname, textLength: text.length, errors: [...errors] });
        assert.ok(text.length > 20, route);
      }
      if (id === 110228) {
        await page.setViewportSize({ width: 390, height: 844 });
        for (const route of ['/home/works', '/home/works/performance', '/home/report']) {
          await page.goto(baseURL + route);
          await page.waitForTimeout(600);
          const widths = await page.evaluate(() => ({ viewport: innerWidth, document: document.documentElement.scrollWidth }));
          result.pages.push({ id, route, mobile: true, widths });
          await page.screenshot({ path: path.join(output, 'mobile-' + route.split('/').pop() + '.png'), fullPage: true });
        }
        await page.setViewportSize({ width: 1440, height: 1000 });
        for (const route of ['/home/board', '/home/data', '/home/query', '/home/system/user', '/home/system/permission', '/home/system/log']) {
          await page.goto(baseURL + route);
          await page.waitForTimeout(800);
          result.pages.push({ id, route, hiddenPrototype: true, actualPath: new URL(page.url()).pathname,
            textSample: (await page.locator('body').innerText()).slice(-900) });
        }
        await page.goto(baseURL + '/home/system/user?tab=security&action=password');
        const dialog = page.getByRole('dialog', { name: '修改密码', exact: true });
        await dialog.waitFor();
        const fields = dialog.locator('input');
        await fields.nth(0).fill('deliberately-wrong-password');
        await fields.nth(1).fill('synthetic-new-password');
        await fields.nth(2).fill('synthetic-new-password');
        let passwordRequests = 0;
        page.on('request', req => { if (req.url().includes('/system/password')) passwordRequests++; });
        await dialog.getByRole('button', { name: '确认修改' }).click();
        await page.getByText('密码修改成功，请重新登录', { exact: true }).waitFor();
        const oldLogin = await call('/system/login', null, { user_id: id, password: 'review-fixture-password' });
        const newLogin = await call('/system/login', null, { user_id: id, password: 'synthetic-new-password' });
        assert.equal(passwordRequests, 0);
        assert.ok(oldLogin.body.token && !newLogin.body.token);
        result.findings.push({ id: 'prototype-password', passwordRequests, oldPasswordStillValid: true, newPasswordInvalid: true });
        await page.screenshot({ path: path.join(output, 'prototype-password.png'), fullPage: true });
      }
      await context.close();
    }
    const routes = ['/system/allUsers','/system/dept/920001','/system/notice','/biz/tasks','/biz/tasks/930002',
      '/biz/tasks/children?task_id=930000','/biz/tasks/forth?parent_id=930002','/biz/tasks/parent?task_id=930002',
      '/biz/tasks/dept?dept_id=920001','/biz/audit/todo','/biz/audit/task/930002',
      '/achievement/','/achievement/audit/todo','/achievement/audit/records',
      '/performance','/performance/950011','/performance/year/2026','/performance/task/950011?year=2026',
      '/performance/audit/todo','/performance/audit/records','/performance/audit/perf/950011?year=2026',
      '/performance/audit/logs/971001','/dashboard/summary','/dashboard/completion/overall',
      '/dashboard/completion/year?year=2026','/dashboard/completion/midterm?endYear=2028',
      '/dashboard/completion/first-level','/dashboard/dept/overall','/dashboard/dept/year?year=2026',
      '/dashboard/dept/midterm?endYear=2028','/dashboard/tasks/first-level','/dashboard/tasks/all_level',
      '/dashboard/dept/920001','/dashboard/dept/batch?deptIds=920001,920002',
      '/dashboard/comparison/dept','/dashboard/comparison/year','/dashboard/comparison/level','/dashboard/trend/2026'];
    for (const route of routes) {
      const response = await call(route, admin);
      result.api.push({ route, status: response.status, businessError: response.body?.code >= 400 ? response.body : null });
    }
    const stats = (await call('/dashboard/dept/overall', admin)).body.find(row => row.deptId === 920001);
    assert.ok(stats.inProgressCount > stats.totalTasks, stats);
    result.findings.push({ id: 'department-counts', stats });
    const year = (await call('/dashboard/comparison/year', admin)).body;
    const level = (await call('/dashboard/comparison/level', admin)).body;
    result.findings.push({ id: 'comparison-placeholder', year, level });
    const uploaded = [];
    for (const name of ['closure.pdf', 'closure.PDF']) {
      const response = await api.post('/api/system/upload/930002', { headers: { Authorization: admin },
        multipart: { file: { name, mimeType: 'application/pdf', buffer: Buffer.from('%PDF-1.4\n% synthetic audit file\n%%EOF') } } });
      uploaded.push({ name, status: response.status(), body: await response.json() });
    }
    assert.ok(uploaded[0].body.fileId || uploaded[0].body.file_id, uploaded);
    assert.equal(uploaded[1].body.code, 500);
    result.findings.push({ id: 'uppercase-upload', uploaded });
    await fs.writeFile(path.join(output, 'results.json'), JSON.stringify(result, null, 2) + '\n');
    console.log(JSON.stringify({ pages: result.pages.length, api: result.api.length,
      apiErrors: result.api.filter(row => row.status !== 200 || row.businessError), findings: result.findings.map(row => row.id) }));
  } finally { await browser.close(); await api.dispose(); }
}
main().catch(error => { console.error(error); process.exitCode = 1; });
