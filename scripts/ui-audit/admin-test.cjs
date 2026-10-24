// All writes are confined to serve.py --fixture admintest's disposable synthetic schema.
const { chromium, request } = require('../../target/ui-audit-tools/node_modules/playwright');
const { expect: baseExpect } = require('../../target/ui-audit-tools/node_modules/playwright/test');
const expect = baseExpect.configure({timeout: 20000});
const fs = require('node:fs/promises'), path = require('node:path'), assert = require('node:assert/strict');
(async () => {
  const state = JSON.parse(await fs.readFile(path.resolve(__dirname, '../../target/ui-audit/state.json')));
  assert.match(state.schema, /^biz_review_test_[0-9a-f]{32}$/);
  const api = await request.newContext({ baseURL: state.frontend }), browser = await chromium.launch({ channel: 'msedge', headless: true });
  const contexts = [], errors = [], checks = [], output = path.resolve(__dirname, '../../target/ui-audit/admin-test');
  await fs.mkdir(output, { recursive: true });
  const token = async id => (await (await api.post('/api/system/login', { data: { user_id: id, password: 'review-fixture-password' } })).json()).token;
  const pageFor = async id => {
    const value = await token(id);
    const context = await browser.newContext({ viewport: { width: 1600, height: 1000 }, storageState: { cookies: [], origins: [{ origin: state.frontend, localStorage: [{ name: 'token', value }] }] } }); contexts.push(context);
    const page = await context.newPage(); page.on('pageerror', e => errors.push(e.message));
    await page.goto(state.frontend + '/home/works'); await expect(page.getByRole('menuitem', {name:'任务',exact:true})).toBeVisible(); return { page, token: value };
  };
  const pick = async (page, select, text) => {
    await select.locator('xpath=ancestor-or-self::div[contains(concat(" ",normalize-space(@class)," ")," el-select ")][1]').click();
    await page.getByRole('option', { name: text, exact: true }).click();
  };
  const done = name => { checks.push(name); console.log('PASS ' + name); };
  let page;
  try {
    const ordinary = await pageFor(910001);
    await expect(ordinary.page.getByRole('menuitem', { name: '任务', exact: true })).toBeVisible();
    await expect(ordinary.page.getByRole('button', { name: '管理员测试', exact: true })).toHaveCount(0);
    done('ordinary-account-has-no-test-control');
    const admin = await pageFor(990009); page = admin.page;
    await expect(page.getByRole('button', { name: '管理员测试', exact: true })).toHaveCount(0);
    await page.goto(state.frontend + '/home/works?taskId=930002');
    let drawer = page.getByRole('dialog', { name: '任务详情与反馈' });
    await expect(drawer.getByRole('spinbutton')).toBeEnabled();
    await drawer.getByRole('button', { name: /close/i }).click();
    let dialog;
    done('administrator-has-direct-fill-permission-without-test-control');

    for (const url of ['/home/works/performance', '/home/works/achievement', '/home/works/budget', '/home/work-records', '/home/core-indicators', '/home/notice', '/home/audit', '/home/report', '/home/admin/overview', '/home/admin/accounts', '/home/admin/operations', '/dashboard']) {
      await page.goto(state.frontend + url);
      await expect(page.locator(url === '/dashboard' ? '.dashboard-fixed-container' : '.user-trigger')).toBeVisible();
      await expect(page.getByRole('button', { name: '管理员测试', exact: true })).toHaveCount(0);
      assert.equal(new URL(page.url()).pathname, url);
    }
    done('all-business-pages-work-without-a-test-switch');

    await page.goto(state.frontend + '/home/works?taskId=930002'); drawer = page.getByRole('dialog', { name: '任务详情与反馈' });
    await expect(drawer.getByRole('spinbutton')).toBeEnabled(); await drawer.getByRole('spinbutton').fill('5');
    await drawer.getByPlaceholder('请输入完成情况（50字以内）').fill('管理员代填合成任务');
    await drawer.locator('input[type=file]').setInputFiles({ name: 'admin-test.pdf', mimeType: 'application/pdf', buffer: Buffer.from('%PDF-1.4\n% synthetic admin test\n%%EOF') });
    await drawer.getByRole('button', { name: '提交反馈', exact: true }).click();
    await expect(page.getByText('材料已提交（已进入审核流程）', { exact: true })).toBeVisible();
    done('real-task-fill-and-material-upload-by-nonowner-admin');

    const approve = async name => {
      await page.goto(state.frontend + '/home/audit');
      const row = page.locator('.el-table__body .el-table__row').filter({ hasText: name }).first();
      await row.getByRole('button', { name: '审批', exact: true }).click();
      const audit = page.getByRole('dialog', { name: '业务审批', exact: true });
      await audit.getByPlaceholder('请输入审批意见（必填）...').fill('管理员代审合成数据');
      await audit.getByRole('button', { name: '确认提交', exact: true }).click(); await expect(audit).toBeHidden();
    };
    await approve('仅用户A可见的三级任务'); await approve('仅用户A可见的三级任务'); await approve('仅用户A可见的三级任务');
    await approve('审计手动绩效'); await approve('审计手动绩效');
    done('real-task-three-stage-and-performance-two-stage-delegated-approval');

    await page.goto(state.frontend + '/home/works/achievement'); await page.getByRole('button', { name: '新增', exact: true }).click();
    dialog = page.getByRole('dialog', { name: '新增成果', exact: true });
    await dialog.locator('.el-form-item').filter({ hasText: '成果类别' }).locator('.el-select').click();
    await page.getByRole('option').first().click();
    await pick(page, dialog.locator('.el-form-item').filter({ hasText: '级别' }).locator('.el-select'), '省级');
    await dialog.getByPlaceholder('请输入建设成果名称').fill('管理员跨部门合成成果');
    await dialog.getByPlaceholder('如：教育部办公厅等').fill('合成测试单位');
    await dialog.getByPlaceholder('请输入具体内容（项目名称、荣誉名称、奖项名称等）').fill('管理员跨部门代填合成成果');
    await dialog.getByPlaceholder('选择颁发时间').fill('2026-01-01 00:00:00'); await dialog.getByPlaceholder('选择颁发时间').press('Tab');
    await pick(page, dialog.locator('.el-form-item').filter({ hasText: '报送部门' }).locator('.el-select'), '审计测试部门B');
    await dialog.getByRole('button', { name: '提交审核', exact: true }).click(); await expect(dialog).toBeHidden();
    await approve('管理员跨部门合成成果');
    done('unlisted-admin-creates-cross-department-achievement-and-archives');

    await page.goto(state.frontend + '/home/work-records'); await page.getByRole('button', { name: '新建纪实', exact: true }).click();
    dialog = page.getByRole('dialog', { name: '新建工作纪实', exact: true });
    await pick(page, dialog.getByRole('combobox', { name: '新建纪实月份' }), '1月');
    await dialog.getByRole('button', { name: '确定', exact: true }).click();
    await pick(page, page.getByRole('combobox', { name: '选择改革任务' }), '审计专用A一级任务');
    await page.getByRole('button', { name: '添加任务', exact: true }).click(); await page.getByRole('textbox', { name: '关键进展', exact: true }).fill('管理员测试工作纪实');
    await page.getByRole('button', { name: '保存草稿', exact: true }).click(); await expect(page.getByText('草稿已保存', { exact: true })).toBeVisible();
    await page.getByRole('button', { name: '提交纪实', exact: true }).click();
    await page.getByRole('dialog', { name: '提交工作纪实', exact: true }).getByRole('button', { name: '确认提交', exact: true }).click();
    await expect(page.getByText('工作纪实已提交', { exact: true })).toBeVisible(); await expect(page.getByRole('button', { name: '保存草稿', exact: true })).toHaveCount(0);
    done('admin-without-task-duty-can-create-save-submit-work-record');

    await page.goto(state.frontend + '/home/core-indicators'); await page.getByRole('button', { name: '填报', exact: true }).first().click();
    dialog = page.getByRole('dialog', { name: '核心指标填报', exact: true });
    await dialog.locator('.el-input input').nth(0).fill('0'); await dialog.locator('.el-input input').nth(1).fill('2');
    await dialog.getByRole('button', { name: '提交审核', exact: true }).click();
    await page.getByRole('dialog', { name: '提交确认', exact: true }).getByRole('button', { name: '确认提交', exact: true }).click();
    await expect(page.getByRole('dialog', { name: '核心指标详情', exact: true })).toContainText('待归口审核');
    await approve('构建“云端”思政阵地数量'); await approve('构建“云端”思政阵地数量');
    done('core-indicator-real-fill-and-two-stage-delegated-review');

    await page.goto(state.frontend + '/home/admin/operations');
    await pick(page, page.locator('.filters .el-select').first(), '管理员操作'); await page.getByRole('button', { name: '查询', exact: true }).click();
    await expect(page.locator('.el-table__body')).toContainText('测试管理员');
    await expect(page.locator('.el-table__body')).toContainText('管理员直接操作');
    await page.screenshot({ path: path.join(output, 'admin-test-operations.png'), fullPage: true });
    const records = await (await api.get('/api/admin/operations?module=ADMIN_OPERATION&pageSize=100', { headers: { Authorization: admin.token } })).json();
    assert.ok(records.total >= 12); assert.ok(records.items.every(row => row.operatorId === 990009));
    done('visible-trace-records-real-admin-and-reason');

    await page.goto(state.frontend + '/home/works');
    await page.locator('.user-trigger').click(); await page.getByRole('menuitem', { name: '退出登录', exact: true }).click();
    const logoutResponse = page.waitForResponse(response => response.url().endsWith('/api/system/logout'));
    await page.getByRole('dialog', { name: '提示', exact: true }).getByRole('button', { name: '确定', exact: true }).click();
    assert.equal((await logoutResponse).status(), 200); await expect(page).toHaveURL(/\/login$/);
    assert.equal(await page.evaluate(() => sessionStorage.getItem('shuanggao.adminTest')), null);
    assert.equal((await api.get('/api/biz/tasks', { headers: { Authorization: admin.token } })).status(), 401);
    done('logout-clears-test-session-and-revokes-original-long-token');
    assert.deepEqual(errors, []);
    await fs.writeFile(path.join(output, 'result.json'), JSON.stringify({ checks, pageErrors: errors }, null, 2));
  } catch (failure) { if (page) await page.screenshot({ path: path.join(output, 'failure.png'), fullPage: true }); throw failure; }
  finally { for (const context of contexts) await context.close(); await api.dispose(); await browser.close(); }
})().catch(error => { console.error(error); process.exitCode = 1; });
