// Run only with the disposable review fixture; assertions reproduce current bugs.
const { request, chromium } = require('../../target/ui-audit-tools/node_modules/playwright');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');
const output = path.resolve(__dirname, '../../target/ui-audit/evidence-batch-15');
const baseURL = 'http://127.0.0.1:15173';
const password = 'review-fixture-password';

async function main() {
  const state = JSON.parse(await fs.readFile(path.resolve(__dirname, '../../target/ui-audit/state.json')));
  assert.match(state.schema, /^biz_review_test_[0-9a-f]{32}$/);
  const api = await request.newContext({ baseURL });
  let browser;
  const results = { schema: state.schema, cases: [], errors: [] };
  const call = async (endpoint, token, data) => {
    const res = await api.fetch('/api/system/' + endpoint, {
      method: data === undefined ? 'GET' : 'POST',
      headers: token ? { Authorization: token } : {}, data
    });
    const raw = await res.text();
    let body; try { body = JSON.parse(raw); } catch { body = raw; }
    return { status: res.status(), body };
  };
  const login = async (id, pass = password) => call('login', null, { user_id: id, password: pass });
  const tokenOf = response => { assert.ok(response.body.token); return response.body.token; };
  const user = (id, role = '1', status = '0') => ({
    userId: id, deptId: 920001, userName: 'batch15_' + id, nickName: '合成账号' + id,
    email: `batch15_${id}@example.invalid`, password, role, status, isDelete: 0
  });
  try {
    const admin = tokenOf(await login(110228));
    for (const row of [user(915001, '0'), user(915002), user(915003), user(915004)]) {
      const added = await call('users/add', admin, row);
      assert.equal(typeof added.body, 'string', JSON.stringify(added));
    }
    const oldToken = tokenOf(await login(915001));
    await call('users/update', admin, user(915001, '1'));
    const current = (await call('allUsers', admin)).body.find(row => row.userId === 915001);
    assert.equal(current.role, '1');
    const newToken = tokenOf(await login(915001));
    const denied = await call('users/delete/915002', newToken, {});
    assert.equal(denied.status, 403);
    const stale = await call('users/delete/915002', oldToken, {});
    const victim = (await call('allUsers', admin)).body.find(row => row.userId === 915002);
    assert.equal(stale.status, 200); assert.equal(victim.isDelete, 1);
    results.cases.push({ id: '15-1', roleAfterChange: current.role, newSession: denied,
      oldSession: stale, victimDeleted: victim.isDelete });

    const before = (await call('allUsers', admin)).body.find(row => row.userId === 915003);
    const update = await call('users/update', admin, user(915003, '1', '0'));
    const after = (await call('allUsers', admin)).body.find(row => row.userId === 915003);
    assert.equal(before.status, '1'); assert.equal(after.status, '1');
    const statusLogin = await login(915003);
    results.cases.push({ id: '15-2', requestedStatus: '0', createdStatus: before.status,
      updateResponse: update, updatedStatus: after.status, statusOneCanLogin: !!statusLogin.body.token });

    const accountToken = tokenOf(await login(915004));
    const changed = await call('password', accountToken, { new_password: '' });
    const emptyLogin = await login(915004, '');
    const oldLogin = await login(915004);
    assert.ok(emptyLogin.body.token); assert.ok(!oldLogin.body.token);
    browser = await chromium.launch({ channel: 'msedge', headless: true });
    const page = await browser.newPage({ viewport: { width: 1440, height: 1000 } });
    page.on('pageerror', error => results.errors.push(error.message));
    await page.route('**/*', route => new URL(route.request().url()).hostname === '127.0.0.1' ? route.continue() : route.abort());
    let loginRequests = 0;
    page.on('request', req => { if (req.method() === 'POST' && req.url().endsWith('/system/login')) loginRequests++; });
    await page.goto(baseURL + '/login');
    await page.getByPlaceholder('账号', { exact: true }).fill('915004');
    await page.getByRole('button', { name: /登\s*录/ }).click();
    await page.getByText('请输入密码', { exact: true }).waitFor();
    assert.equal(loginRequests, 0);
    await fs.mkdir(output, { recursive: true });
    await page.screenshot({ path: path.join(output, 'empty-password-login.png'), fullPage: true });
    results.cases.push({ id: '15-3', changed, emptyPasswordApiLogin: !!emptyLogin.body.token,
      originalPasswordRejected: !oldLogin.body.token, browserLoginRequests: loginRequests,
      browserMessage: '请输入密码' });
    assert.deepEqual(results.errors, []);
    await fs.writeFile(path.join(output, 'account-audit.json'), JSON.stringify(results, null, 2));
    console.log(JSON.stringify({ reproduced: results.cases.map(row => row.id), errors: results.errors }));
  } finally { if (browser) await browser.close(); await api.dispose(); }
}
main().catch(error => { console.error(error); process.exitCode = 1; });
