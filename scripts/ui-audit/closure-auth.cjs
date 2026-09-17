const { request } = require('../../target/ui-audit-tools/node_modules/playwright');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');
async function main() {
  const root = path.resolve(__dirname, '../../target/ui-audit');
  assert.match(JSON.parse(await fs.readFile(path.join(root, 'state.json'))).schema, /^biz_review_test_[0-9a-f]{32}$/);
  const sweep = JSON.parse(await fs.readFile(path.join(root, 'evidence-closure/results.json')));
  const api = await request.newContext({ baseURL: 'http://127.0.0.1:15173' });
  const checks = [];
  try {
    for (const row of sweep.api) {
      const response = await api.get('/api' + row.route);
      assert.equal(response.status(), 401, row.route);
      checks.push({ route: row.route, noTokenStatus: response.status() });
    }
    const login = await api.post('/api/system/login', { data: { user_id: 910001, password: 'review-fixture-password' } });
    const { token } = await login.json();
    const users = await api.post('/api/system/users/add', { headers: { Authorization: token }, data: {} });
    assert.equal(users.status(), 403);
    checks.push({ route: '/system/users/add', ordinaryUserStatus: 403 });
    for (const route of ['/biz/tasks/manage/add','/scheduled/month_leader_trigger']) {
      const response = await api.post('/api' + route, { headers: { Authorization: token }, data: {} });
      const body = await response.json();
      assert.equal(body.code, 500);
      assert.match(body.message, /管理员/);
      checks.push({ route, ordinaryUserError: body });
    }
    await fs.writeFile(path.join(root, 'evidence-closure/auth.json'), JSON.stringify(checks, null, 2) + '\n');
    console.log(JSON.stringify({ passed: checks.length }));
  } finally { await api.dispose(); }
}
main().catch(error => { console.error(error); process.exitCode = 1; });
