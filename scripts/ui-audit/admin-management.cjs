const { chromium, request } = require('../../target/ui-audit-tools/node_modules/playwright');
const { expect } = require('../../target/ui-audit-tools/node_modules/playwright/test');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');

(async () => {
  const state = JSON.parse(await fs.readFile(path.resolve(__dirname, '../../target/ui-audit/state.json')));
  assert.match(state.schema, /^biz_review_test_[0-9a-f]{32}$/);
  const output = path.resolve(__dirname, '../../target/ui-audit/admin-management');
  await fs.mkdir(output, { recursive: true });
  const api = await request.newContext({ baseURL: state.frontend });
  const adminLogin = await (await api.post('/api/system/login', { data: { user_id: 110228, password: 'review-fixture-password' } })).json();
  const userLogin = await (await api.post('/api/system/login', { data: { user_id: 910001, password: 'review-fixture-password' } })).json();
  assert.ok(adminLogin.token && userLogin.token);
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const errors = [];
  try {
    const context = await browser.newContext({ viewport: { width: 1440, height: 950 } });
    await context.addInitScript(token => localStorage.setItem('token', token), adminLogin.token);
    const page = await context.newPage();
    page.on('pageerror', e => errors.push(e.stack || e.message));
    await page.goto(state.frontend + '/home/admin/overview');
    await expect(page.getByRole('heading', { name: '管理概览' })).toBeVisible();
    await expect(page.getByText('正常账号', { exact: true })).toBeVisible();
    await expect(page.getByText('管理中心', { exact: true })).toBeVisible();
    await page.screenshot({ path: path.join(output, 'overview.png'), fullPage: true });

    await page.goto(state.frontend + '/home/admin/accounts');
    await expect(page.getByRole('heading', { name: '账号与组织' })).toBeVisible();
    await page.getByRole('button', { name: '新增账号' }).click();
    const dialog = page.getByRole('dialog', { name: '新增账号' });
    const inputFor = label => dialog.locator('.el-form-item').filter({ hasText: label }).locator('input').first();
    await inputFor('工号').fill('990099');
    await inputFor('姓名').fill('界面测试账号');
    await inputFor('账号名称').fill('ui_admin_test');
    await inputFor('邮箱').fill('ui@example.invalid');
    await dialog.locator('.el-form-item').filter({ hasText: '部门' }).locator('.el-select__wrapper').click();
    await page.getByRole('option', { name: '审计测试部门A', exact: true }).click();
    await dialog.getByRole('button', { name: '保存' }).click();
    await expect(page.getByText('界面测试账号', { exact: true })).toBeVisible();

    const row = page.getByRole('row').filter({ hasText: '990099' });
    await row.getByRole('button', { name: '重置密码' }).click();
    await page.getByRole('dialog', { name: '重置密码' }).locator('textarea').fill('Playwright 重置验证');
    await page.getByRole('dialog', { name: '重置密码' }).getByRole('button', { name: '确认' }).click();
    await row.getByRole('button', { name: '停用' }).click();
    const disable = page.getByRole('dialog', { name: '停用账号' });
    await disable.locator('textarea').fill('Playwright 停用验证');
    await disable.getByRole('button', { name: '确认' }).click();
    await expect(row.getByText('停用', { exact: true })).toBeVisible();
    await page.screenshot({ path: path.join(output, 'accounts.png'), fullPage: true });

    await page.goto(state.frontend + '/home/admin/operations');
    await expect(page.getByRole('heading', { name: '操作记录' })).toBeVisible();
    await expect(page.getByText('界面测试账号', { exact: true }).first()).toBeVisible();
    await page.screenshot({ path: path.join(output, 'operations.png'), fullPage: true });

    await page.goto(state.frontend + '/home/admin/data?tab=deleted');
    await expect(page.getByRole('heading', { name: '数据维护' })).toBeVisible();
    await expect(page.getByText('已删除纪实', { exact: true }).first()).toBeVisible();

    await page.setViewportSize({ width: 900, height: 900 });
    await page.goto(state.frontend + '/home/admin/overview');
    await expect(page.getByRole('heading', { name: '管理概览' })).toBeVisible();
    await page.screenshot({ path: path.join(output, 'overview-900.png'), fullPage: true });

    const ordinary = await browser.newContext({ viewport: { width: 1280, height: 800 } });
    await ordinary.addInitScript(token => localStorage.setItem('token', token), userLogin.token);
    const denied = await ordinary.newPage();
    await denied.goto(state.frontend + '/home/admin/overview');
    await expect(denied).toHaveURL(/\/home\/works(?:\?|$)/);
    await expect(denied.getByText('管理中心', { exact: true })).toHaveCount(0);
    await ordinary.close();
    assert.deepEqual(errors, []);
    console.log(JSON.stringify({ checks: ['overview', 'create-user', 'reset-password', 'disable-user', 'operations', 'deleted-records', 'responsive', 'non-admin-route'], errors }, null, 2));
  } finally {
    await browser.close(); await api.dispose();
  }
})().catch(error => { console.error(error); process.exitCode = 1; });
