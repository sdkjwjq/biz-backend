// End-to-end password gate tests against a disposable schema only.
const { chromium, request } = require('../../target/ui-audit-tools/node_modules/playwright');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');
const BASE = 'http://127.0.0.1:15273';
const OLD = 'review-fixture-password';
const STRONG = 'Aa123456';
async function main() {
  const root = path.resolve(__dirname, '../../target/ui-audit');
  const state = JSON.parse(await fs.readFile(path.join(root, 'state.json')));
  assert.match(state.schema, /^biz_review_test_[0-9a-f]{32}$/);
  assert.equal(state.frontend, BASE);
  const output = path.join(root, 'evidence-password-policy');
  await fs.mkdir(output, { recursive: true });
  const api = await request.newContext({ baseURL: BASE });
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const results = [], errors = [];
  const apiLogin = async (id, password) => (await (await api.post('/api/system/login', { data: { user_id: id, password } })).json());
  const login = async (page, id, password) => {
    await page.goto(BASE + '/login');
    await page.getByPlaceholder('账号', { exact: true }).fill(String(id));
    await page.getByPlaceholder('密码', { exact: true }).fill(password);
    await page.getByRole('button', { name: /登\s*录/ }).click();
    await page.waitForURL(url => url.pathname.startsWith('/home/'));
  };
  const newContext = async () => {
    const context = await browser.newContext({ viewport: { width: 1440, height: 1000 } });
    await context.route('**/*', route => new URL(route.request().url()).hostname === '127.0.0.1' ? route.continue() : route.abort());
    const page = await context.newPage();
    page.on('pageerror', error => errors.push(error.message));
    return { context, page };
  };
  try {
    for (const id of [110228, 910001, 1910001]) {
      const { context, page } = await newContext();
      let writes = 0, businessRequests = 0;
      page.on('request', req => {
        const pathname = new URL(req.url()).pathname;
        if (req.method() === 'POST' && pathname === '/api/system/password') writes++;
        if (/^\/api\/(biz|dashboard|performance|achievement)\//.test(pathname)) businessRequests++;
      });
      await login(page, id, OLD);
      const dialog = page.getByRole('dialog', { name: '请先修改密码', exact: true });
      await dialog.waitFor();
      assert.equal(businessRequests, 0, 'Business pages must not mount before password change');
      assert.equal(await dialog.locator('.el-dialog__headerbtn').count(), 0);
      assert.equal(await dialog.getByRole('button', { name: '取消', exact: true }).count(), 0);
      await page.keyboard.press('Escape');
      await page.mouse.click(10, 10);
      assert.equal(await dialog.isVisible(), true);
      await page.reload();
      await dialog.waitFor();
      await page.goto(BASE + '/dashboard');
      await page.waitForURL(url => url.pathname === (id === 1910001 ? '/home/works/achievement' : '/home/works'));
      await dialog.waitFor();
      assert.equal(new URL(page.url()).pathname, id === 1910001 ? '/home/works/achievement' : '/home/works');
      const password = dialog.getByPlaceholder('请输入新密码', { exact: true });
      const confirm = dialog.getByPlaceholder('请再次输入新密码', { exact: true });
      const save = dialog.getByRole('button', { name: '保存', exact: true });
      await dialog.getByText('新密码至少8位，并同时包含大写字母、小写字母和数字。', { exact: true }).waitFor();
      for (const invalid of ['12345678', 'abcdefgh1', 'ABCDEFGH1', 'Abcdefgh', 'Ab1', 'Aa1234', 'Aa12345']) {
        await password.fill(invalid); await confirm.fill(invalid); await save.click();
        assert.equal(writes, 0);
        await dialog.locator('.el-form-item__error').first().waitFor();
      }
      await password.fill(STRONG); await confirm.fill(STRONG + 'x'); await save.click();
      await dialog.getByText('两次输入的密码不一致', { exact: true }).waitFor();
      assert.equal(writes, 0);
      await confirm.fill(STRONG);
      await page.screenshot({ path: path.join(output, `required-${id}.png`), fullPage: true });
      if (id === 910001) {
        await page.route('**/api/system/password', route => route.fulfill({ status: 500, contentType: 'application/json', body: JSON.stringify({ code: 500, message: '合成保存失败，请重试' }) }), { times: 1 });
        await save.click();
        await page.getByText('合成保存失败，请重试', { exact: true }).waitFor();
        assert.equal(await dialog.isVisible(), true);
        assert.ok((await apiLogin(id, OLD)).token);
        await page.setViewportSize({ width: 390, height: 844 });
        const box = await dialog.boundingBox();
        assert.ok(box.x >= 0 && box.x + box.width <= 390);
        await page.screenshot({ path: path.join(output, 'required-mobile.png'), fullPage: true });
        await page.setViewportSize({ width: 1440, height: 1000 });
      }
      let release, reached;
      const gate = new Promise(resolve => { release = resolve; });
      const arrived = new Promise(resolve => { reached = resolve; });
      await page.route('**/api/system/password', async route => { reached(); await gate; await route.continue(); }, { times: 1 });
      const before = writes;
      await save.click(); await arrived;
      assert.equal(await save.isDisabled(), true);
      await page.keyboard.press('Enter'); await page.keyboard.press('Enter');
      assert.equal(writes, before + 1);
      const refreshed = page.waitForResponse(response => new URL(response.url()).pathname === '/api/system/password/status');
      release(); await refreshed;
      await dialog.waitFor({ state: 'hidden' });
      const loggedIn = await apiLogin(id, STRONG);
      assert.ok(loggedIn.token);
      assert.equal(loggedIn.requiresPasswordChange, false);
      assert.equal((await apiLogin(id, OLD)).token, undefined);
      await page.reload();
      await page.locator('.common-layout').waitFor();
      assert.equal(await dialog.isVisible(), false);
      results.push({ id, forced: true, reloadAndDashboardGuarded: true, validation: true, singleSubmission: true, oldPasswordRejected: true, strongPasswordUngated: true });
      await context.close();
    }
    // Already compliant accounts enter normally; no forced prompt on a fresh login.
    const weakLogin = await apiLogin(910004, OLD);
    assert.ok(weakLogin.token);
    assert.equal((await api.post('/api/system/password', { headers: { Authorization: weakLogin.token }, data: { new_password: STRONG } })).status(), 200);
    const { context, page } = await newContext();
    await login(page, 910004, STRONG);
    await page.locator('.works-container').waitFor();
    assert.equal(await page.getByRole('dialog', { name: '请先修改密码', exact: true }).isVisible(), false);
    results.push({ id: 910004, alreadyCompliantSkipsDialog: true });
    await context.close();
    assert.deepEqual(errors, []);
    await fs.writeFile(path.join(output, 'results.json'), JSON.stringify({ results, errors }, null, 2));
    console.log(JSON.stringify({ passedAccounts: results.length, errors }));
  } finally { await browser.close(); await api.dispose(); }
}
main().catch(error => { console.error(error); process.exitCode = 1; });
