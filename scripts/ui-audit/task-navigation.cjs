const { chromium, request } = require('../../target/ui-audit-tools/node_modules/playwright');
const { expect } = require('../../target/ui-audit-tools/node_modules/playwright/test');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');

async function main() {
  const root = path.resolve(__dirname, '../../target/ui-audit');
  const state = JSON.parse(await fs.readFile(path.join(root, 'state.json')));
  assert.match(state.schema, /^biz_review_test_[0-9a-f]{32}$/);
  assert.equal(state.frontend, 'http://127.0.0.1:15273');
  const output = path.join(root, 'evidence-task-navigation'); await fs.mkdir(output, { recursive: true });
  const api = await request.newContext({ baseURL: state.frontend });
  const reset = await api.post('/api/system/password/reset', { data: {
    user_id: 910001, old_password: 'review-fixture-password', new_password: 'Navigation123'
  } }); assert.ok([200, 401].includes(reset.status()));
  const login = await (await api.post('/api/system/login', { data: { user_id: 910001, password: 'Navigation123' } })).json();
  assert.ok(login.token);
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const errors = [], checks = [];
  checks.push = function (...items) { console.log(items.join(', ')); return Array.prototype.push.apply(this, items); };
  try {
    const context = await browser.newContext({ viewport: { width: 1440, height: 1000 } });
    await context.addInitScript(token => localStorage.setItem('token', token), login.token);
    const page = await context.newPage(); page.setDefaultTimeout(10000); page.on('pageerror', e => errors.push(e.message));
    await page.goto(state.frontend + '/home/works?taskId=930002');
    const drawer = page.getByRole('dialog', { name: '任务详情与反馈', exact: true });
    const previous = drawer.getByRole('button', { name: '上一个', exact: true });
    const next = drawer.getByRole('button', { name: '下一个', exact: true });
    const ready = async () => {
      await drawer.locator('.drawer-content > .el-loading-mask').waitFor({ state: 'hidden' });
      await page.waitForTimeout(100);
    };
    await drawer.waitFor(); await ready();
    await expect(previous).toBeDisabled(); await expect(next).toBeEnabled();
    await drawer.getByText('1 / 3', { exact: true }).waitFor();
    checks.push('initial-first-boundary-and-year-filter');
    await next.click(); await ready();
    await drawer.getByText('导航测试B', { exact: true }).waitFor();
    await drawer.getByText('导航B四级子目标', { exact: false }).first().waitFor();
    await drawer.getByText('2 / 3', { exact: true }).waitFor();
    checks.push('next-loads-correct-task-and-fourth-level');
    await drawer.getByRole('spinbutton').first().fill('7');
    await next.click();
    const confirm = page.getByRole('dialog', { name: '切换任务', exact: true });
    await confirm.getByRole('button', { name: '继续编辑', exact: true }).click();
    assert.equal(await drawer.getByRole('spinbutton').first().inputValue(), '7');
    await next.click(); await confirm.getByRole('button', { name: '放弃并切换', exact: true }).click();
    await ready(); await drawer.getByText('导航测试C', { exact: true }).waitFor();
    await expect(next).toBeDisabled(); await expect(previous).toBeEnabled();
    assert.equal(await drawer.getByText('导航B四级子目标', { exact: false }).count(), 0);
    checks.push('dirty-fourth-level-cancel-discard-and-last-boundary');
    await previous.click(); await ready();
    assert.equal(await drawer.getByRole('spinbutton').first().inputValue(), '2');
    await previous.click(); await ready();
    await drawer.getByPlaceholder('请输入完成情况（50字以内）').fill('未提交的反馈');
    await next.click(); await confirm.getByRole('button', { name: '继续编辑', exact: true }).click();
    assert.equal(await drawer.getByPlaceholder('请输入完成情况（50字以内）').inputValue(), '未提交的反馈');
    await next.click(); await confirm.getByRole('button', { name: '放弃并切换', exact: true }).click(); await ready();
    await previous.click(); await ready();
    checks.push('feedback-unsaved-protection-and-back-navigation');
    await drawer.locator('input[type=file]').setInputFiles({ name: 'synthetic.pdf', mimeType: 'application/pdf', buffer: Buffer.from('%PDF-1.4 synthetic') });
    await next.click(); await confirm.getByRole('button', { name: '继续编辑', exact: true }).click();
    await drawer.getByText('synthetic.pdf', { exact: false }).first().waitFor();
    await next.click(); await confirm.getByRole('button', { name: '放弃并切换', exact: true }).click(); await ready();
    checks.push('selected-file-protection');
    await previous.click(); await ready();
    let release, arrive;
    const held = new Promise(resolve => { release = resolve; });
    const reached = new Promise(resolve => { arrive = resolve; });
    await page.route('**/api/biz/tasks/forth?parent_id=930003', async route => {
      const response = await route.fetch(); arrive(); await held; await route.fulfill({ response });
    }, { times: 1 });
    await next.click();
    await Promise.race([reached, new Promise((_, reject) => setTimeout(() => reject(new Error('Delayed request did not start')), 10000))]);
    await expect(next).toBeDisabled(); await expect(previous).toBeDisabled();
    await drawer.getByRole('button', { name: /close/i }).click();
    await page.goto(state.frontend + '/home/works?taskId=930012');
    await drawer.waitFor(); await ready();
    release(); await page.waitForTimeout(300);
    await drawer.getByText('导航测试C', { exact: true }).waitFor();
    assert.equal(await drawer.getByText('导航B四级子目标', { exact: false }).count(), 0);
    checks.push('loading-lock-close-reopen-stale-response');
    await drawer.getByRole('button', { name: /close/i }).click();
    const search = page.getByPlaceholder('搜索任务编号或名称...');
    await search.fill('导航测试B'); await search.press('Enter');
    await page.waitForTimeout(200);
    for (let i = 0; i < 3; i++) {
      const collapsed = page.locator('.el-table__expand-icon:not(.el-table__expand-icon--expanded):visible');
      if (!await collapsed.count()) break;
      await collapsed.first().click();
    }
    await page.getByRole('row').filter({ hasText: '导航测试B' }).getByRole('button', { name: '查看', exact: true }).click();
    await ready(); await drawer.getByText('1 / 1', { exact: true }).waitFor();
    await expect(previous).toBeDisabled(); await expect(next).toBeDisabled();
    checks.push('search-single-result');
    await page.screenshot({ path: path.join(output, 'task-navigation.png'), fullPage: true });
    assert.deepEqual(errors, []);
    await fs.writeFile(path.join(output, 'results.json'), JSON.stringify({ checks, errors }, null, 2));
    console.log(JSON.stringify({ passed: checks.length, errors }));
  } finally { await browser.close(); await api.dispose(); }
}
main().catch(e => { console.error(e); process.exitCode = 1; });
