const { chromium, request } = require('../../target/ui-audit-tools/node_modules/playwright');
const { expect } = require('../../target/ui-audit-tools/node_modules/playwright/test');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');

(async () => {
  const state = JSON.parse(await fs.readFile(path.resolve(__dirname, '../../target/ui-audit/state.json')));
  assert.match(state.schema, /^biz_review_test_[0-9a-f]{32}$/);
  const output = path.resolve(__dirname, '../../target/ui-audit/leadership-access');
  await fs.mkdir(output, { recursive: true });
  const api = await request.newContext({ baseURL: state.frontend });
  const login = async (user_id, password) => {
    const data = await (await api.post('/api/system/login', { data: { user_id, password } })).json();
    assert.ok(data.token, JSON.stringify(data)); return data.token;
  };
  const admin = await login(110228, 'review-fixture-password');
  const school = await login(910010, 'LeaderTest123');
  const department = await login(910011, 'LeaderTest123');
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const errors = [];
  const pageFor = async token => {
    const context = await browser.newContext({ viewport: { width: 1440, height: 950 } });
    await context.addInitScript(value => localStorage.setItem('token', value), token);
    const page = await context.newPage(); page.on('pageerror', error => errors.push(error.message)); return page;
  };
  try {
    const page = await pageFor(admin);
    await page.goto(state.frontend + '/home/admin/accounts');
    await page.getByPlaceholder('姓名、工号或账号').fill('910010');
    await page.getByRole('button', { name: '查询', exact: true }).click();
    await page.getByRole('row').filter({ hasText: '910010' }).getByRole('button', { name: '查阅范围' }).click();
    const dialog = page.getByRole('dialog', { name: '校级领导查阅范围' });
    await expect(dialog.getByText('合成全校领导（910010）')).toBeVisible();
    await dialog.locator('.el-select__wrapper').first().click();
    await page.getByRole('option', { name: '全校', exact: true }).click();
    await dialog.locator('textarea').fill('Playwright 全校领导只读验证');
    await dialog.getByRole('button', { name: '保存', exact: true }).dblclick();
    await page.getByRole('dialog', { name: '确认变更' }).getByRole('button', { name: '确认保存', exact: true }).click();
    await expect(dialog).toBeHidden();
    const config = await (await api.get('/api/admin/users/910010/leadership', { headers: { Authorization: admin } })).json();
    assert.equal(config.version, 1, '重复点击只能保存一次');
    assert.equal(config.scope, 'ALL');

    const configured = await api.put('/api/admin/users/910011/leadership', { headers: { Authorization: admin }, data: { scope: 'DEPARTMENTS', departmentIds: [920001], version: 0, reason: 'Playwright 部门领导' } });
    assert.equal(configured.status(), 200);
    const leader = await pageFor(school);
    await leader.goto(state.frontend + '/home/admin/overview');
    await expect(leader.getByRole('heading', { name: '管理概览', exact: true })).toBeVisible();
    await expect(leader.getByText('正常账号', { exact: true })).toBeVisible();
    await expect(leader.getByText('账号与组织', { exact: true })).toHaveCount(0);
    await expect(leader.getByText('审核中心', { exact: true })).toHaveCount(0);
    await expect(leader.getByRole('button').filter({ hasText: '正常账号' })).toBeDisabled();
    await leader.screenshot({ path: path.join(output, 'school-overview.png'), fullPage: true });
    await leader.goto(state.frontend + '/home/works');
    const tasks = await (await api.get('/api/biz/tasks', { headers: { Authorization: school } })).json();
    assert.ok(tasks.some(task => task.taskId === 930010));
    await expect(leader.getByRole('button', { name: '新增任务', exact: true })).toHaveCount(0);
    await leader.goto(state.frontend + '/home/notice');
    await leader.getByText('领导浏览通知', { exact: true }).click();
    await expect(leader.getByRole('dialog', { name: '消息详情' })).toBeVisible();
    await expect(leader.getByRole('button', { name: /前往处理业务/ })).toHaveCount(0);
    await leader.screenshot({ path: path.join(output, 'notice-readonly.png'), fullPage: true });

    const scoped = await pageFor(department);
    await scoped.goto(state.frontend + '/home/admin/overview');
    await expect(scoped.getByRole('heading', { name: '管理概览', exact: true })).toBeVisible();
    const scopeTasks = await (await api.get('/api/biz/tasks', { headers: { Authorization: department } })).json();
    assert.ok(scopeTasks.some(task => task.taskId === 930002));
    assert.ok(!scopeTasks.some(task => task.taskId === 930010));
    assert.equal((await api.get('/api/biz/tasks/930010', { headers: { Authorization: department } })).status(), 403);
    await scoped.goto(state.frontend + '/home/works/achievement');
    await expect(scoped).toHaveURL(/\/home\/works\/achievement/);
    await scoped.goto(state.frontend + '/home/works/budget');
    await expect(scoped).toHaveURL(/\/home\/works\/budget/);
    await scoped.goto(state.frontend + '/home/admin/accounts');
    await expect(scoped).toHaveURL(/\/home\/works(?:\?|$)/);
    await scoped.screenshot({ path: path.join(output, 'department-tasks.png'), fullPage: true });
    assert.deepEqual(errors, []);
    const result = { checks: ['管理员配置', '重复提交', '全校及部门范围', '只读管理概览', '菜单', '个人消息只读', '直接链接拒绝', '成果预算查阅', '管理路由拒绝'], errors };
    await fs.writeFile(path.join(output, 'result.json'), JSON.stringify(result, null, 2));
    console.log(JSON.stringify(result, null, 2));
  } finally { await browser.close(); await api.dispose(); }
})().catch(error => { console.error(error); process.exitCode = 1; });
