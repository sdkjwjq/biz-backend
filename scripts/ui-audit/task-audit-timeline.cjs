// Production-preview regression for the current task's workflow display.
const { chromium, request } = require('playwright');
const { expect } = require('playwright/test');
const fs = require('node:fs/promises'), path = require('node:path'), assert = require('node:assert/strict');
(async () => {
  const state = JSON.parse(await fs.readFile(process.env.SHUANGGAO_UI_STATE_FILE, 'utf8'));
  const out = path.join(path.dirname(process.env.SHUANGGAO_UI_STATE_FILE), 'timeline-evidence');
  await fs.mkdir(out, { recursive: true });
  const api = await request.newContext({ baseURL: state.frontend });
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const contexts = [], checks = [], errors = [];
  async function login(id) {
    let result = await api.post('/api/system/login', { data: { user_id: id, password: 'review-fixture-password' } });
    assert.equal(result.status(), 200); let token = (await result.json()).token;
    const context = await api.get('/api/system/review-context', { headers: { Authorization: token } });
    if ((await context.json()).experience === 'SELECT') {
      result = await api.post('/api/system/review-context', { headers: { Authorization: token }, data: { experience: 'BUSINESS' } });
      assert.equal(result.status(), 200); token = (await result.json()).token;
    }
    return token;
  }
  async function pageFor(token) {
    const context = await browser.newContext({ viewport: { width: 1440, height: 1000 } }); contexts.push(context);
    await context.addInitScript(t => { localStorage.setItem('token', t); }, token);
    const page = await context.newPage(); page.setDefaultTimeout(25000);
    page.on('pageerror', e => errors.push(e.message));
    await page.route('**/*', r => new URL(r.request().url()).hostname === '127.0.0.1' ? r.continue() : r.abort());
    return page;
  }
  async function openTask(page, base = state.frontend) {
    await page.goto(base + '/home/works?taskId=930002');
    const drawer = page.getByRole('dialog', { name: '任务详情与反馈', exact: true });
    await expect(drawer).toBeVisible();
    await expect(drawer.locator('.log-section')).toContainText('本轮专业群通过');
    await expect(drawer.locator('.el-loading-mask:visible')).toHaveCount(0);
    return drawer;
  }
  const steps = drawer => drawer.locator('.custom-steps .el-step__description');
  async function pending(drawer) {
    await expect(steps(drawer).nth(1)).toContainText('2026-06-25');
    await expect(steps(drawer).nth(2)).not.toContainText('审核通过');
    await expect(steps(drawer).nth(3)).not.toContainText('已归档');
    await expect(drawer.locator('.custom-steps')).not.toContainText('2026-05-20');
  }
  async function record(token) {
    const result = await api.get('/api/biz/audit/task/930002', { headers: { Authorization: token } });
    assert.equal(result.status(), 200); return (await result.json()).find(r => Number(r.subId) === 970001);
  }
  try {
    const admin = await login(110228), principal = await login(910002);
    const before = await record(admin); assert.equal(before.flowStatus, 20);
    if (process.env.SHUANGGAO_BASELINE_FRONTEND) {
    const baseline = await pageFor(admin), old = await openTask(baseline, process.env.SHUANGGAO_BASELINE_FRONTEND);
    await expect(steps(old).nth(2)).toContainText('进行中');
    await expect(steps(old).nth(3)).toContainText('2026-05-20');
    await expect(steps(old).nth(3)).toContainText('已归档');
    await baseline.screenshot({ path: path.join(out, 'before-mixed-archive.png'), fullPage: true });
    checks.push('Baseline reproduces the premature archive description using a pre-submission log under the same submission ID');
    await baseline.context().close();
    }
    const current = await pageFor(admin); let drawer = await openTask(current);
    await pending(drawer); await expect(steps(drawer).nth(2)).toContainText('进行中');
    await expect(steps(drawer).nth(3)).toContainText('待流转');
    assert.equal((await record(admin)).flowStatus, 20);
    await current.screenshot({ path: path.join(out, 'after-current-cycle.png'), fullPage: true });
    await expect(drawer.locator('.log-section')).not.toContainText('已完成归档');
    checks.push('Current task shows only current-cycle evidence, without legacy archive text or maintenance notices');
    await current.goto(state.frontend + '/home/works/performance?perfId=950001&year=2026');
    const related = current.locator('.related-tasks-table, .el-table').filter({ hasText: '仅用户A可见的三级任务' });
    await related.getByRole('button', { name: '查看', exact: true }).click();
    drawer = current.getByRole('dialog').filter({ has: current.locator('.drawer-title').filter({ hasText: '任务详情与反馈' }) });
    await expect(drawer).toBeVisible(); await expect(drawer.locator('.log-section')).toContainText('本轮专业群通过');
    await pending(drawer); await current.screenshot({ path: path.join(out, 'performance-linked-task.png'), fullPage: true });
    checks.push('Performance-linked task drawer uses the same current-cycle workflow evidence');
    const reviewer = await pageFor(principal), reviewDrawer = await openTask(reviewer);
    await pending(reviewDrawer); await expect(reviewDrawer.getByRole('button', { name: '确认提交', exact: true })).toBeVisible();
    await reviewDrawer.getByPlaceholder('请输入审批意见（必填）...').fill('本轮归口审核通过');
    const approved = reviewer.waitForResponse(r => r.url().endsWith('/api/biz/audit') && r.request().method() === 'POST');
    await reviewDrawer.getByRole('button', { name: '确认提交', exact: true }).click(); assert.equal((await approved).status(), 200);
    await expect.poll(async () => (await record(admin)).flowStatus).toBe(30);
    await expect(steps(reviewDrawer).nth(2)).toContainText('审核通过');
    await expect(steps(reviewDrawer).nth(3)).not.toContainText('已归档');
    checks.push('Principal approval still advances only to flow 30, never directly to archive');
    drawer = await openTask(current); await expect(steps(drawer).nth(3)).toContainText('进行中');
    await expect(steps(drawer).nth(3)).not.toContainText('已归档');
    await drawer.getByPlaceholder('请输入审批意见（必填）...').fill('本轮实际归档');
    const archived = current.waitForResponse(r => r.url().endsWith('/api/biz/audit') && r.request().method() === 'POST');
    await drawer.getByRole('button', { name: '确认提交', exact: true }).click(); assert.equal((await archived).status(), 200);
    await expect.poll(async () => (await record(admin)).flowStatus).toBe(40);
    await expect(steps(drawer).nth(3)).toContainText('已归档'); await expect(steps(drawer).nth(3)).not.toContainText('2026-05-20');
    await current.screenshot({ path: path.join(out, 'after-real-archive.png'), fullPage: true });
    checks.push('Only a real final archive produces current-cycle archived text and its actual timestamp');
    assert.deepEqual(errors, []);
    await fs.writeFile(path.join(out, 'results.json'), JSON.stringify({ passed: true, checks, errors }, null, 2));
    console.log('Task timeline browser checks passed: ' + checks.length);
  } finally { for (const c of contexts) await c.close().catch(() => {}); await api.dispose(); await browser.close(); }
})().catch(e => { console.error(e); process.exitCode = 1; });
