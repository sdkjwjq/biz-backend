const { chromium } = require('../../target/ui-audit-tools/node_modules/playwright');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');
const output = path.resolve(__dirname, '../../target/ui-audit/evidence-batch-12');

async function main() {
  const kind = process.argv[2];
  assert(['approval-dialog', 'file-preview', 'related-task'].includes(kind));
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const context = await browser.newContext({ viewport: { width: 1440, height: 1100 }, reducedMotion: 'reduce' });
  await context.route('**/*', route => new URL(route.request().url()).hostname === '127.0.0.1' ? route.continue() : route.abort());
  const page = await context.newPage();
  const result = { kind, errors: [] };
  page.on('pageerror', error => result.errors.push(error.message));
  let release = () => {};
  const hold = async pattern => {
    let reached;
    const started = new Promise(resolve => { reached = resolve; });
    const gate = new Promise(resolve => { release = resolve; });
    await page.route(pattern, async route => {
      const response = await route.fetch(); reached(); await gate; await route.fulfill({ response });
    });
    return { started };
  };
  const screenshot = suffix => page.screenshot({ path: path.join(output, `${kind}-${suffix}.png`), fullPage: true });
  await fs.mkdir(output, { recursive: true });
  try {
    await page.goto('http://127.0.0.1:15173/login');
    await page.getByPlaceholder('账号', { exact: true }).fill('910003');
    await page.getByPlaceholder('密码', { exact: true }).fill('review-fixture-password');
    await page.getByRole('button', { name: /登\s*录/ }).click();
    await page.waitForURL('**/home/works');
    await page.getByText('审计专用A一级任务', { exact: true }).waitFor();
    const rows = page.locator('.audit-container .el-table__body tr');
    const dialog = page.getByRole('dialog', { name: '业务审批', exact: true });
    if (kind !== 'related-task') {
      await page.getByRole('menuitem', { name: /审核中心/ }).click();
      await page.locator('.audit-container .el-loading-mask').waitFor({ state: 'hidden' });
      await page.getByPlaceholder('搜索任务名称、提交人...').fill(kind === 'file-preview' ? '填报竞态' : '审计手动绩效');
    }
    if (kind === 'approval-dialog') {
      await rows.filter({ hasText: '审计手动绩效' }).getByRole('button', { name: '审批', exact: true }).click();
      const held = await hold('**/api/performance/audit');
      const submitted = page.waitForRequest(req => req.method() === 'POST' && new URL(req.url()).pathname === '/api/performance/audit');
      await dialog.getByRole('button', { name: '确认提交', exact: true }).click();
      await held.started;
      result.payload = (await submitted).postDataJSON();
      await dialog.getByRole('button', { name: '关闭', exact: true }).click();
      await dialog.waitFor({ state: 'hidden' });
      await page.getByPlaceholder('搜索任务名称、提交人...').fill('审计零值绩效B');
      await rows.filter({ hasText: '审计零值绩效B' }).getByRole('button', { name: '审批', exact: true }).click();
      await dialog.getByText('驳回', { exact: true }).click();
      await dialog.getByPlaceholder('请输入审批意见（必填）...').fill('B审批草稿，尚未提交');
      result.before = await dialog.innerText();
      await screenshot('before');
      release();
      await page.getByText('已驳回', { exact: true }).waitFor();
      await dialog.waitFor({ state: 'hidden' });
      result.displayedOutcome = '已驳回';
      result.bClosedWithoutSubmission = true;
      result.saved = await page.evaluate(async () => {
        const api = await import('/src/api/performance.js');
        return Promise.all([950011, 950021].map(id => api.getPerformanceAuditsByPerfAndYear(id, 2026)));
      });
      assert.equal(result.payload.is_pass, true);
      assert.equal(Number(result.saved[0][0].flowStatus), 20);
      assert.equal(Number(result.saved[1][0].flowStatus), 10);
      await screenshot('after');
    } else if (kind === 'file-preview') {
      await page.evaluate(async () => { window.auditApi = await import('/src/api/audit.js'); });
      const records = await page.evaluate(async () => Promise.all([932001,932002].map(id => window.auditApi.getAuditByTaskId(id))));
      result.files = records.map(records => ({ id: records[0].fileId, name: records[0].filename }));
      const preview = page.getByRole('dialog', { name: '文件预览', exact: true });
      await rows.filter({ hasText: '填报竞态A' }).getByRole('button', { name: '审批', exact: true }).click();
      const held = await hold(`**/api/system/download/${result.files[0].id}`);
      await dialog.locator('.file-link').click();
      await held.started;
      await preview.getByRole('button', { name: /close/i }).click();
      await preview.waitFor({ state: 'hidden' });
      await dialog.getByRole('button', { name: '关闭', exact: true }).click();
      await dialog.waitFor({ state: 'hidden' });
      await rows.filter({ hasText: '填报竞态B' }).getByRole('button', { name: '审批', exact: true }).click();
      await dialog.locator('.file-link').click();
      await preview.locator('.file-name-display').waitFor();
      result.beforeName = await preview.locator('.file-name-display').innerText();
      await screenshot('before');
      release();
      await page.waitForFunction(name => document.querySelector('.file-name-display')?.textContent === name, result.files[0].name);
      result.afterName = await preview.locator('.file-name-display').innerText();
      assert.equal(result.beforeName, result.files[1].name);
      assert.equal(result.afterName, result.files[0].name);
      await screenshot('after');
    } else {
      await page.getByRole('menuitem', { name: '绩效', exact: true }).click();
      await page.getByText('数量指标', { exact: true }).click();
      await page.getByText('审计自动绩效', { exact: true }).click();
      const detail = page.getByRole('dialog', { name: '绩效指标详情', exact: true });
      await detail.getByText('竞态任务A', { exact: true }).waitFor();
      const held = await hold('**/api/biz/audit/task/931001');
      const open = name => detail.locator('.el-table__row').filter({ hasText: name }).getByRole('button', { name: '查看', exact: true }).click();
      await open('竞态任务A');
      await held.started;
      const task = page.locator('.el-drawer.open').filter({ hasText: '任务详情与反馈' });
      await task.getByRole('button', { name: '返回绩效指标详情', exact: true }).click();
      await detail.waitFor();
      await open('竞态任务B');
      await task.getByRole('button', { name: '确认提交', exact: true }).waitFor();
      await task.locator('.el-loading-mask').waitFor({ state: 'hidden' });
      const old = page.waitForResponse(res => res.url().includes('/biz/audit/task/931001'));
      release(); await old;
      await page.waitForTimeout(500);
      result.visible = await task.locator('.task-info-box').innerText();
      assert.ok(result.visible.includes('竞态任务B'));
      await screenshot('before-approval');
      const posted = page.waitForRequest(req => req.method() === 'POST' && new URL(req.url()).pathname === '/api/biz/audit');
      await task.getByRole('button', { name: '确认提交', exact: true }).click();
      result.payload = (await posted).postDataJSON();
      await page.getByText('审批通过', { exact: true }).waitFor();
      await page.unroute('**/api/biz/audit/task/931001');
      result.saved = await page.evaluate(async () => {
        const api = await import('/src/api/audit.js');
        return Promise.all([931001,931002].map(id => api.getAuditByTaskId(id)));
      });
      assert.equal(Number(result.payload.sub_id), 981001);
      assert.equal(Number(result.saved[0][0].flowStatus), 20);
      assert.equal(Number(result.saved[1][0].flowStatus), 10);
    }
    assert.deepEqual(result.errors, []);
    await fs.writeFile(path.join(output, kind + '.json'), JSON.stringify(result, null, 2));
    console.log(JSON.stringify({ kind, reproduced: true }));
  } catch (error) { console.error(await page.locator('body').innerText()); throw error;
  } finally { release(); await context.close(); await browser.close(); }
}
main().catch(error => { console.error(error); process.exitCode = 1; });
