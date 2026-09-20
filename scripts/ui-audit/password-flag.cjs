const { chromium, request } = require('../../target/ui-audit-tools/node_modules/playwright');
const { expect } = require('../../target/ui-audit-tools/node_modules/playwright/test');
const { execFileSync } = require('node:child_process');
const fs = require('node:fs/promises'), path = require('node:path'), assert = require('node:assert/strict');
async function main() {
  const root = path.resolve(__dirname, '../../target/ui-audit');
  const state = JSON.parse(await fs.readFile(path.join(root, 'state.json')));
  assert.match(state.schema, /^biz_review_test_[0-9a-f]{32}$/);
  assert.ok(process.env.SHUANGGAO_TEST_DB_PASSWORD);
  const mysql = process.env.MYSQL_EXE || 'C:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe';
  const sql = input => execFileSync(mysql, ['--host=127.0.0.1', '--user=root', '--batch', '--skip-column-names', state.schema], {
    env: { ...process.env, MYSQL_PWD: process.env.SHUANGGAO_TEST_DB_PASSWORD }, input, encoding: 'utf8'
  }).trim();
  sql("UPDATE sys_user SET force_password_change=1 WHERE user_id IN (110228,910001)");
  const migration = await fs.readFile(path.resolve(__dirname, '../password/001_force_password_change.sql'), 'utf8');
  const before = sql('SELECT user_id,password,force_password_change FROM sys_user ORDER BY user_id');
  sql(migration); sql(migration);
  assert.equal(sql('SELECT user_id,password,force_password_change FROM sys_user ORDER BY user_id'), before);
  const out = path.join(root, 'evidence-password-flag'); await fs.mkdir(out, { recursive: true });
  const api = await request.newContext({ baseURL: state.frontend });
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const checks = ['migration-preserves-existing-passwords-and-flags'], errors = [];
  const pass = name => { checks.push(name); console.log(name); };
  const apiLogin = async (id, password) => (await (await api.post('/api/system/login', { data: { user_id: id, password } })).json());
  async function login(page, id, password) {
    await page.goto(state.frontend + '/login');
    await page.getByPlaceholder('账号', { exact: true }).fill(String(id));
    await page.getByPlaceholder('密码', { exact: true }).fill(password);
    await page.getByRole('button', { name: /登\s*录/ }).click(); await page.waitForURL(/\/home\//);
  }
  try {
    for (const id of [110228, 910001]) {
      const context = await browser.newContext({ viewport: { width: 1440, height: 960 } });
      const page = await context.newPage(); page.setDefaultTimeout(15000); page.on('pageerror', e => errors.push(e.message));
      await login(page, id, String(id));
      const gate = page.getByRole('dialog', { name: '请先修改密码', exact: true }); await expect(gate).toBeVisible();
      await page.reload(); await expect(gate).toBeVisible();
      await page.keyboard.press('Escape'); await expect(gate).toBeVisible();
      await gate.getByPlaceholder('请输入新密码', { exact: true }).fill('ChangedPass123');
      await gate.getByPlaceholder('请再次输入新密码', { exact: true }).fill('ChangedPass123');
      await gate.getByRole('button', { name: '保存', exact: true }).click(); await expect(gate).toBeHidden();
      assert.equal(sql(`SELECT force_password_change FROM sys_user WHERE user_id=${id}`), '0');
      assert.ok(!(await apiLogin(id, String(id))).token); assert.ok((await apiLogin(id, 'ChangedPass123')).token);
      pass(`real-first-login-id-gate-refresh-change-clears-flag-${id}`);
      await context.close();
      const resetContext = await browser.newContext(); const resetPage = await resetContext.newPage();
      resetPage.on('pageerror', e => errors.push(e.message));
      await resetPage.goto(state.frontend + '/login'); await resetPage.getByPlaceholder('账号', { exact: true }).fill(String(id));
      await resetPage.getByRole('button', { name: '重置密码', exact: true }).click();
      const dialog = resetPage.getByRole('dialog', { name: '重置密码', exact: true });
      await dialog.getByRole('textbox', { name: '原密码', exact: true }).fill(String(id));
      await dialog.getByRole('textbox', { name: '新密码', exact: true }).fill('ResetPass456');
      await dialog.getByRole('textbox', { name: '确认新密码', exact: true }).fill('ResetPass456');
      let calls = 0;
      await resetPage.route('**/system/password/reset', async route => { calls++; await new Promise(resolve => setTimeout(resolve, 400)); await route.continue(); });
      await dialog.getByRole('button', { name: '确认重置', exact: true }).evaluate(b => { b.click(); b.click(); });
      await expect(dialog).toBeHidden(); assert.equal(calls, 1);
      assert.ok(!(await apiLogin(id, 'ChangedPass123')).token);
      assert.equal((await apiLogin(id, 'ResetPass456')).requiresPasswordChange, false);
      await login(resetPage, id, 'ResetPass456'); await expect(resetPage.getByRole('dialog', { name: '请先修改密码', exact: true })).toHaveCount(0);
      await resetPage.screenshot({ path: path.join(out, `reset-${id}.png`), fullPage: true });
      pass(`real-already-changed-id-reset-no-duplicate-and-new-password-login-${id}`); await resetContext.close();
    }
    assert.deepEqual(errors, []);
  } finally { await fs.writeFile(path.join(out, 'results.json'), JSON.stringify({ checks, errors }, null, 2)); await browser.close(); await api.dispose(); }
}
main().catch(error => { console.error(error); process.exitCode = 1; });
