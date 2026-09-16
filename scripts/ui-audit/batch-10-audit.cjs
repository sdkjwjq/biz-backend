const { chromium } = require('../../target/ui-audit-tools/node_modules/playwright');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');
const output = path.resolve(__dirname, '../../target/ui-audit/evidence-batch-10');
const fixed = process.argv.includes('--fixed');

async function main() {
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const context = await browser.newContext({ viewport: { width: 1440, height: 1100 }, reducedMotion: 'reduce' });
  await context.route('**/*', route => new URL(route.request().url()).hostname === '127.0.0.1' ? route.continue() : route.abort());
  const page = await context.newPage();
  const result = { case: 'task-approval-race', errors: [] };
  page.on('pageerror', e => result.errors.push(e.message));
  let release;
  const gate = new Promise(resolve => { release = resolve; });
  await fs.mkdir(output, { recursive: true });
  try {
    await page.goto('http://127.0.0.1:15173/login');
    await page.getByPlaceholder('账号', { exact: true }).fill('910003');
    await page.getByPlaceholder('密码', { exact: true }).fill('review-fixture-password');
    await page.getByRole('button', { name: /登\s*录/ }).click();
    await page.waitForURL('**/home/works');
    await page.getByText('审计专用A一级任务', { exact: true }).waitFor();
    await page.evaluate(async () => { window.auditApi = await import('/src/api/audit.js'); });
    result.before = await page.evaluate(async () => Promise.all([931001, 931002].map(id => window.auditApi.getAuditByTaskId(id))));
    let reached;
    const started = new Promise(resolve => { reached = resolve; });
    await page.route('**/api/biz/audit/task/931001', async route => {
      const response = await route.fetch();
      reached();
      await gate;
      await route.fulfill({ response });
    });
    // Expand real tree rows; no component state or server response is rewritten.
    for (const name of ['审计专用A一级任务', '审计专用A二级任务']) {
      const row = page.locator('.el-table__row').filter({ hasText: name });
      const expand = row.locator('.el-table__expand-icon');
      if (!(await expand.getAttribute('class')).includes('expanded')) await expand.click();
    }
    const open = name => page.locator('.el-table__row').filter({ hasText: name }).getByRole('button', { name: '查看', exact: true }).click();
    await open('竞态任务A');
    await started;
    const drawer = page.getByRole('dialog', { name: '任务详情与反馈' });
    await drawer.getByRole('button', { name: /close/i }).click();
    await drawer.waitFor({ state: 'hidden' });
    const bLoaded = page.waitForResponse(res => res.url().includes('/biz/audit/task/931002'));
    await open('竞态任务B');
    await bLoaded;
    await drawer.getByRole('button', { name: '确认提交', exact: true }).waitFor();
    release();
    await page.waitForResponse(res => res.url().includes('/biz/audit/task/931001'));
    await page.waitForTimeout(700);
    result.visibleTask = await drawer.locator('.task-info-box').innerText();
    assert.ok(result.visibleTask.includes('竞态任务B'));
    await page.screenshot({ path: path.join(output, 'before-approval.png'), fullPage: true });
    const posted = page.waitForRequest(req => req.method() === 'POST' && new URL(req.url()).pathname === '/api/biz/audit');
    await drawer.getByRole('button', { name: '确认提交', exact: true }).click();
    result.posted = (await posted).postDataJSON();
    await page.getByText('审批通过', { exact: true }).waitFor();
    await page.unroute('**/api/biz/audit/task/931001');
    result.after = await page.evaluate(async () => Promise.all([931001, 931002].map(id => window.auditApi.getAuditByTaskId(id))));
    assert.equal(Number(result.posted.sub_id), fixed ? 981002 : 981001);
    assert.equal(Number(result.after[0][0].flowStatus), fixed ? 10 : 20);
    assert.equal(Number(result.after[1][0].flowStatus), fixed ? 20 : 10);
    assert.deepEqual(result.errors, []);
    await fs.writeFile(path.join(output, fixed ? 'task-approval-race-fixed.json' : 'task-approval-race.json'), JSON.stringify(result, null, 2));
    console.log(JSON.stringify({ fixed, passed: true, shownTask: 931002, approvedTask: fixed ? 931002 : 931001 }));
  } catch (error) {
    console.error(await page.locator('body').innerText());
    throw error;
  } finally { release(); await context.close(); await browser.close(); }
}
main().catch(error => { console.error(error); process.exitCode = 1; });
