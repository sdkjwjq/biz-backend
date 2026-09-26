const { chromium, request } = require('../../target/ui-audit-tools/node_modules/playwright');
const { expect } = require('../../target/ui-audit-tools/node_modules/playwright/test');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');

(async () => {
  const state = JSON.parse(await fs.readFile(path.resolve(__dirname, '../../target/ui-audit/state.json')));
  assert.match(state.schema, /^biz_review_test_[0-9a-f]{32}$/);
  const output = path.resolve(__dirname, '../../target/ui-audit/responsibility-filters');
  await fs.mkdir(output, { recursive: true });
  const api = await request.newContext({ baseURL: state.frontend });
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const errors = [], checks = [];
  try {
    await api.post('/api/system/password/reset', { data: { user_id: 110228, old_password: 'review-fixture-password', new_password: 'Convenience123' } });
    const auth = await (await api.post('/api/system/login', { data: { user_id: 110228, password: 'Convenience123' } })).json();
    assert.ok(auth.token);
    const context = await browser.newContext({ viewport: { width: 1440, height: 1000 } });
    await context.addInitScript(token => localStorage.setItem('token', token), auth.token);
    const page = await context.newPage();
    page.setDefaultTimeout(20000);
    page.on('pageerror', error => errors.push(error.stack || error.message));
    const select = async (name, option) => {
      const input = page.getByRole('combobox', { name, exact: true }).filter({ visible: true });
      const wrapper = input.locator('xpath=ancestor::*[contains(@class, "el-select__wrapper")]');
      for (let attempt = 0; attempt < 20; attempt++) {
        await wrapper.click();
        await page.waitForTimeout(250);
        const target = page.getByRole('option', { name: option, exact: true }).filter({ visible: true });
        if (await target.count()) { await target.click(); return; }
        await page.keyboard.press('Escape');
        await page.waitForTimeout(250);
      }
      throw new Error(`筛选选项未加载：${name} / ${option}`);
    };
    const expand = async () => {
      for (let i = 0; i < 10; i++) {
        const closed = page.locator('.el-table__expand-icon:not(.el-table__expand-icon--expanded):visible');
        if (!await closed.count()) break;
        await closed.first().click();
      }
    };

    await page.goto(state.frontend + '/home/works');
    await expect(page.getByRole('combobox', { name: '任务责任人', exact: true })).toBeVisible();
    await select('任务责任人', 'audit_user_b');
    await expand();
    await expect(page.getByRole('row').filter({ hasText: '责任人筛选任务B' })).toBeVisible();
    await expect(page.getByRole('row').filter({ hasText: '仅用户A可见的三级任务' })).toHaveCount(0);
    await page.reload(); await expand();
    await expect(page.getByRole('row').filter({ hasText: '责任人筛选任务B' })).toBeVisible();
    await page.locator('.task-filters').getByRole('button', { name: '重置', exact: true }).click(); await expand();
    await expect(page.getByRole('row').filter({ hasText: '仅用户A可见的三级任务' })).toBeVisible();
    checks.push('任务责任人筛选支持交集、刷新记忆和重置');

    await page.goto(state.frontend + '/home/works/performance');
    await expect(page.getByRole('combobox', { name: '绩效责任人', exact: true })).toBeVisible();
    await select('绩效责任人', '审计用户B'); await expand();
    await expect(page.getByRole('row').filter({ hasText: '责任人筛选绩效B' })).toBeVisible();
    await expect(page.getByRole('row').filter({ hasText: '责任人筛选绩效A' })).toHaveCount(0);
    await page.locator('.business-filters').getByRole('button', { name: '重置', exact: true }).click(); await expand();
    await expect(page.getByRole('row').filter({ hasText: '责任人筛选绩效A' })).toBeVisible();
    checks.push('绩效责任人筛选与重置正确');

    await page.goto(state.frontend + '/home/works/achievement');
    await expect(page.getByRole('combobox', { name: '成果填报人', exact: true })).toBeVisible();
    await select('成果填报人', '审计用户B'); await expand();
    await expect(page.getByRole('row').filter({ hasText: '填报人筛选成果B' })).toBeVisible();
    await expect(page.getByRole('row').filter({ hasText: '填报人筛选成果A' })).toHaveCount(0);
    await page.locator('.business-filters').getByRole('button', { name: '重置', exact: true }).click(); await expand();
    await expect(page.getByRole('row').filter({ hasText: '填报人筛选成果A' })).toBeVisible();
    checks.push('成果填报人筛选与重置正确');

    const budgetRequests = [];
    page.on('request', req => {
      if (req.url().includes('/api/budget?')) budgetRequests.push(req.url());
    });
    await page.goto(state.frontend + '/home/works/budget');
    const toolbar = page.locator('.budget-toolbar');
    const selects = toolbar.locator('.el-select');
    await expect(selects).toHaveCount(2);
    await selects.nth(0).locator('.el-select__wrapper').click();
    await page.getByRole('option', { name: '2025年', exact: true }).filter({ visible: true }).click();
    await selects.nth(1).locator('.el-select__wrapper').click();
    await page.getByRole('option', { name: '4月', exact: true }).filter({ visible: true }).click();
    await expect.poll(() => budgetRequests.some(url => url.includes('year=2025') && url.includes('month=4'))).toBeTruthy();
    await toolbar.getByRole('button', { name: /导入/ }).click();
    const dialog = page.getByRole('dialog', { name: '导入预算数据' });
    await expect(dialog).toBeVisible();
    await expect(dialog.getByText('所属年度', { exact: true })).toBeVisible();
    await expect(dialog.getByText('所属月份', { exact: true })).toBeVisible();
    checks.push('预算按年度和月份浏览，导入表单同时绑定年度和月份');

    await page.screenshot({ path: path.join(output, 'budget-year-month.png'), animations: 'disabled' });
    assert.deepEqual(errors, []);
    await fs.writeFile(path.join(output, 'results.json'), JSON.stringify({ checks, errors }, null, 2));
    console.log(JSON.stringify({ checks, errors }, null, 2));
  } finally {
    await browser.close();
    await api.dispose();
  }
})().catch(error => { console.error(error); process.exitCode = 1; });
