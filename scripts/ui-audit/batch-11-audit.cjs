const { chromium } = require('../../target/ui-audit-tools/node_modules/playwright');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');
const output = path.resolve(__dirname, '../../target/ui-audit/evidence-batch-11');
const fixed = process.argv.includes('--fixed');
const stay = process.argv.includes('--stay');

async function main() {
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const context = await browser.newContext({ viewport: { width: 1440, height: 1100 } });
  await context.route('**/*', route => new URL(route.request().url()).hostname === '127.0.0.1' ? route.continue() : route.abort());
  const page = await context.newPage();
  const result = { case: 'feedback-upload-race', errors: [] };
  page.on('pageerror', e => result.errors.push(e.message));
  let release;
  const gate = new Promise(resolve => { release = resolve; });
  await fs.mkdir(output, { recursive: true });
  try {
    await page.goto('http://127.0.0.1:15173/login');
    await page.getByPlaceholder('账号', { exact: true }).fill('910001');
    await page.getByPlaceholder('密码', { exact: true }).fill('review-fixture-password');
    await page.getByRole('button', { name: /登\s*录/ }).click();
    await page.waitForURL('**/home/works');
    await page.getByText('审计专用A一级任务', { exact: true }).waitFor();
    for (const name of ['审计专用A一级任务', '审计专用A二级任务']) {
      const icon = page.locator('.el-table__row').filter({ hasText: name }).locator('.el-table__expand-icon');
      if (!(await icon.getAttribute('class')).includes('expanded')) await icon.click();
    }
    let reached;
    const started = new Promise(resolve => { reached = resolve; });
    await page.route('**/api/system/upload**', async route => {
      const response = await route.fetch();
      reached();
      await gate;
      await route.fulfill({ response });
    });
    const open = name => page.locator('.el-table__row').filter({ hasText: name }).getByRole('button', { name: '查看', exact: true }).click();
    const drawer = page.getByRole('dialog', { name: '任务详情与反馈' });
    await open(stay ? '填报竞态B' : '填报竞态A');
    await drawer.locator('.el-loading-mask').waitFor({ state: 'hidden' });
    await drawer.getByRole('spinbutton').fill('3');
    await drawer.getByPlaceholder('请输入完成情况（50字以内）').fill('A本次填报3');
    await drawer.locator('input[type=file]').setInputFiles({ name: 'synthetic.pdf', mimeType: 'application/pdf', buffer: Buffer.from('%PDF-1.4\n% Synthetic audit fixture\n%%EOF') });
    await drawer.getByRole('button', { name: '提交反馈', exact: true }).click();
    await started;
    if (!stay) {
    await drawer.getByRole('button', { name: /close/i }).click();
    await drawer.waitFor({ state: 'hidden' });
    await open('填报竞态B');
    await drawer.locator('.el-loading-mask').waitFor({ state: 'hidden' });
    result.bValue = await drawer.getByRole('spinbutton').inputValue();
    await drawer.getByPlaceholder('请输入完成情况（50字以内）').fill('B尚未提交的草稿');
    }
    await page.screenshot({ path: path.join(output, 'before-upload-return.png'), fullPage: true });
    const posted = page.waitForRequest(req => req.method() === 'POST' && new URL(req.url()).pathname === '/api/biz/sub');
    const submitted = page.waitForResponse(res => res.request().method() === 'POST' && new URL(res.url()).pathname === '/api/biz/sub');
    release();
    result.payload = (await posted).postDataJSON();
    await submitted;
    await page.waitForTimeout(700);
    result.bDraftAfter = await drawer.getByPlaceholder('请输入完成情况（50字以内）').inputValue();
    result.saved = await page.evaluate(async taskId => {
      const api = await import('/src/api/audit.js');
      return api.getAuditByTaskId(taskId);
    }, stay ? 932002 : 932001);
    assert.equal(Number(result.payload.task_id), stay ? 932002 : 932001);
    assert.equal(Number(result.payload.reported_value), fixed ? 3 : 7);
    assert.equal(Number(result.saved[0].reportedValue), fixed ? 3 : 7);
    if (!stay) assert.equal(result.bDraftAfter, fixed ? 'B尚未提交的草稿' : '');
    assert.deepEqual(result.errors, []);
    await fs.writeFile(path.join(output, stay ? 'feedback-stay-fixed.json' : fixed ? 'feedback-upload-race-fixed.json' : 'feedback-upload-race.json'), JSON.stringify(result, null, 2));
    console.log(JSON.stringify({ fixed, stay, passed: true, saved: Number(result.saved[0].reportedValue) }));
  } catch (error) { console.error(await page.locator('body').innerText()); throw error;
  } finally { release(); await context.close(); await browser.close(); }
}
main().catch(error => { console.error(error); process.exitCode = 1; });
