const { chromium, request } = require('../../target/ui-audit-tools/node_modules/playwright');
const { expect } = require('../../target/ui-audit-tools/node_modules/playwright/test');
const { execFileSync } = require('node:child_process');
const fs = require('node:fs/promises'), path = require('node:path'), assert = require('node:assert/strict');
async function main() {
  const root = path.resolve(__dirname, '../../target/ui-audit');
  const state = JSON.parse(await fs.readFile(path.join(root, 'state.json')));
  assert.match(state.schema, /^biz_review_test_[0-9a-f]{32}$/);
  assert.ok(process.env.SHUANGGAO_TEST_DB_PASSWORD);
  execFileSync(process.env.MYSQL_EXE || 'C:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe', ['--host=127.0.0.1', '--user=root', state.schema], {
    env: { ...process.env, MYSQL_PWD: process.env.SHUANGGAO_TEST_DB_PASSWORD },
    input: 'UPDATE sys_user SET force_password_change=1 WHERE user_id=910001;'
  });
  const out = path.join(root, 'evidence-password-return'); await fs.mkdir(out, { recursive: true });
  const api = await request.newContext({ baseURL: state.frontend });
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const page = await browser.newPage({ viewport: { width: 1440, height: 960 } }); page.setDefaultTimeout(15000);
  const checks = [], errors = []; page.on('pageerror', e => errors.push(e.message));
  const pass = name => { checks.push(name); console.log(name); };
  const gate = page.getByRole('dialog', { name: '请先修改密码', exact: true });
  async function login() {
    await page.goto(state.frontend + '/login');
    await page.getByPlaceholder('账号', { exact: true }).fill('910001'); await page.getByPlaceholder('密码', { exact: true }).fill('910001');
    await page.getByRole('button', { name: /登\s*录/ }).click(); await expect(page).toHaveURL(/\/home\//); await expect(gate).toBeVisible();
  }
  try {
    await login();
    await gate.getByPlaceholder('请输入新密码', { exact: true }).fill('Unsaved123');
    await page.evaluate(() => sessionStorage.setItem('works_return_test', 'synthetic'));
    let calls = 0;
    await page.route('**/system/logout', async route => { calls++; await new Promise(resolve => setTimeout(resolve, 500)); await route.continue(); });
    await gate.getByRole('button', { name: '返回登录' }).evaluate(b => { b.click(); b.click(); });
    await expect(gate.getByRole('button', { name: '保存', exact: true })).toBeDisabled();
    await expect(page).toHaveURL(/\/login$/); assert.equal(calls, 1);
    assert.equal(await page.evaluate(() => localStorage.getItem('token')), null);
    assert.equal(await page.evaluate(() => sessionStorage.getItem('works_return_test')), null);
    await page.unroute('**/system/logout');
    const data = await (await api.post('/api/system/login', { data: { user_id: 910001, password: '910001' } })).json();
    assert.equal(data.requiresPasswordChange, true); pass('real-return-clears-session-once-without-changing-password-or-flag');
    await page.goto(state.frontend + '/home/works'); await expect(page).toHaveURL(/\/login/);
    await login(); await expect(gate.getByPlaceholder('请输入新密码', { exact: true })).toHaveValue('');
    pass('protected-route-stays-blocked-and-relogin-still-requires-change');
    await page.setViewportSize({ width: 390, height: 844 });
    await expect(gate.getByRole('button', { name: '返回登录' })).toBeVisible();
    await page.screenshot({ path: path.join(out, 'return-mobile.png'), fullPage: true });
    await page.route('**/system/logout', route => route.fulfill({ status: 500, contentType: 'application/json', body: '{"code":500,"message":"模拟退出失败"}' }));
    await gate.getByRole('button', { name: '返回登录' }).click(); await expect(page).toHaveURL(/\/login$/);
    assert.equal(await page.evaluate(() => localStorage.getItem('token')), null);
    await page.unroute('**/system/logout'); pass('mobile-button-and-simulated-logout-failure-still-return');
    await login();
    await gate.getByPlaceholder('请输入新密码', { exact: true }).fill('ReturnSave123');
    await gate.getByPlaceholder('请再次输入新密码', { exact: true }).fill('ReturnSave123');
    await page.route('**/system/password', async route => { await new Promise(resolve => setTimeout(resolve, 800)); await route.continue(); });
    await gate.getByRole('button', { name: '保存', exact: true }).click();
    await expect(gate.getByRole('button', { name: '返回登录' })).toBeDisabled(); await expect(gate).toBeHidden();
    pass('real-password-save-blocks-return-until-complete'); assert.deepEqual(errors, []);
  } finally { await fs.writeFile(path.join(out, 'results.json'), JSON.stringify({ checks, errors }, null, 2)); await browser.close(); await api.dispose(); }
}
main().catch(error => { console.error(error); process.exitCode = 1; });
