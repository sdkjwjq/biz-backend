const { chromium, request } = require('../../target/ui-audit-tools/node_modules/playwright');
const { expect } = require('../../target/ui-audit-tools/node_modules/playwright/test');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');

(async () => {
  const state = JSON.parse(await fs.readFile(path.resolve(__dirname, '../../target/ui-audit/state.json')));
  assert.match(state.schema, /^biz_review_test_[0-9a-f]{32}$/);
  const output = path.resolve(__dirname, '../../target/ui-audit/ownership-transfer');
  await fs.mkdir(output, { recursive: true });
  const api = await request.newContext({ baseURL: state.frontend });
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const errors = [], checks = [];
  const login = async userId => {
    const result = await (await api.post('/api/system/login', { data: { user_id: userId, password: 'review-fixture-password' } })).json();
    assert.ok(result.token, `login failed for ${userId}`);
    return result.token;
  };
  const rows = (page, name) => page.locator('.el-table__body tr').filter({ hasText: name });
  // v-loading 遮罩淡出期间会挡住点击，先等它消失
  const waitTableReady = page => expect(page.locator('.table-wrapper .el-loading-mask')).toBeHidden();
  const settleTable = async page => { await waitTableReady(page); await page.waitForTimeout(300); };
  const expand = async page => {
    await page.locator('.el-table__body tr').first().waitFor({ state: 'attached' });
    await settleTable(page);
    for (let i = 0; i < 12; i++) {
      const closed = page.locator('.el-table__expand-icon:not(.el-table__expand-icon--expanded):visible');
      if (!await closed.count()) break;
      try {
        await closed.first().click({ timeout: 8000 });
      } catch (error) {
        await page.screenshot({ path: path.join(output, 'expand-failed.png') });
        console.error('expand failed at', page.url(), await page.locator('.table-wrapper .el-loading-mask').count());
        throw error;
      }
      await settleTable(page);
    }
  };
  const waitVisible = async (page, name) => {
    await rows(page, name).first().waitFor({ state: 'attached' });
    await expand(page);
    await expect(rows(page, name).first()).toBeVisible();
  };
  try {
    const adminToken = await login(110228);
    const userToken = await login(910001);

    // 非管理员：没有任何移交入口
    const userContext = await browser.newContext({ viewport: { width: 1600, height: 1000 } });
    await userContext.addInitScript(token => localStorage.setItem('token', token), userToken);
    const userPage = await userContext.newPage();
    userPage.on('pageerror', e => errors.push(e.stack || e.message));
    await userPage.goto(state.frontend + '/home/works');
    await expect(userPage.locator('.table-wrapper .el-table__header')).toBeVisible();
    await waitVisible(userPage, '仅用户A可见的三级任务');
    assert.equal(await userPage.locator('.transfer-btn').count(), 0, 'non-admin must not see the batch transfer button');
    assert.equal(await userPage.locator('.table-wrapper .el-table__header .el-checkbox').count(), 0,
      'non-admin must not see the selection column');
    await rows(userPage, '仅用户A可见的三级任务').getByText('查看', { exact: true }).click();
    await expect(userPage.locator('.el-drawer')).toBeVisible();
    assert.equal(await userPage.locator('.owner-actions').count(), 0, 'non-admin must not see the transfer action');
    await userPage.locator('.el-drawer__close-btn').click();
    await expect(userPage.locator('.el-drawer')).toBeHidden();
    await userContext.close();
    checks.push('non-admin sees no selection column, no batch button and no transfer action');

    const context = await browser.newContext({ viewport: { width: 1920, height: 1000 } });
    await context.addInitScript(token => localStorage.setItem('token', token), adminToken);
    const page = await context.newPage();
    page.on('pageerror', e => errors.push(e.stack || e.message));
    const before = await (await api.get('/api/biz/tasks/930002', { headers: { Authorization: adminToken } })).json();
    await page.goto(state.frontend + '/home/works');
    await waitVisible(page, '仅用户A可见的三级任务');

    const selectInDialog = async (dialog, name, option) => {
      const combobox = dialog.getByRole('combobox', { name, exact: true });
      await combobox.click();
      await combobox.fill(option);
      const dropdown = page.locator('.el-select-dropdown:visible').last();
      await dropdown.getByRole('option', { name: option, exact: true }).click();
      await expect(dropdown).toBeHidden();
    };

    // 管理员：批量移交一条无待审单据的三级任务
    await expect(page.locator('.transfer-btn')).toHaveText('批量移交');
    await page.getByRole('button', { name: '批量移交', exact: true }).click();
    await expect(page.locator('.table-wrapper .el-table__header .el-checkbox').first()).toBeVisible();
    // 点击一级任务应级联勾选其下全部三级任务，取消后回到未勾选
    await rows(page, '审计专用A一级任务').locator('.el-checkbox').click();
    await expect(page.getByRole('button', { name: '确认移交（2）' })).toBeVisible();
    await rows(page, '审计专用A一级任务').locator('.el-checkbox').click();
    await expect(page.locator('.transfer-hint')).toBeVisible();
    await rows(page, '仅用户A可见的三级任务').locator('.el-checkbox').click();
    const batch = page.getByRole('button', { name: '确认移交（1）' });
    await expect(batch).toBeVisible();
    await page.screenshot({ path: path.join(output, 'transfer-mode.png') });
    await batch.click();

    const dialog = page.locator('.transfer-dialog');
    await expect(dialog).toBeVisible();
    await selectInDialog(dialog, '目标归口部门', '审计测试部门B');
    await selectInDialog(dialog, '目标责任人', '审计用户B（910004）');
    await selectInDialog(dialog, '目标归口审核人', '审计审核人（910003）');
    await dialog.getByLabel('移交原因').fill('合成回归：人员调整');
    await dialog.getByRole('button', { name: '预览并确认' }).click();
    await expect(dialog).toContainText('审计测试部门B');
    await expect(dialog).toContainText('→');
    await page.screenshot({ path: path.join(output, 'transfer-preview.png') });
    await dialog.getByRole('button', { name: '确认提交' }).click();
    await expect(dialog).toBeHidden();
    await expand(page);
    checks.push('admin batch-preview-submit flow works');

    // 后端状态与日志
    const task = await (await api.get('/api/biz/tasks/930002', { headers: { Authorization: adminToken } })).json();
    assert.equal(Number(task.deptId), 920002, 'task dept must change');
    assert.equal(Number(task.leaderId), 910004, 'task leader must change');
    assert.equal(Number(task.principalId), 910003, 'task principal must change');
    assert.equal(Number(task.auditorId), Number(before.auditorId), 'auditor must stay unchanged');
    const logs = await (await api.get('/api/manage/transfer/logs?targetType=TASK&targetId=930002',
      { headers: { Authorization: adminToken } })).json();
    assert.equal(logs.length, 1, 'one ownership change log expected');
    assert.equal(logs[0].deptBeforeId, 920001);
    assert.equal(logs[0].reason, '合成回归：人员调整');
    assert.equal(Number(logs[0].operatorId), 110228);
    checks.push('task ownership persisted with a change log');

    // 详情抽屉显示归属变更记录
    await rows(page, '仅用户A可见的三级任务').getByText('查看', { exact: true }).click();
    await expect(page.locator('.el-drawer')).toBeVisible();
    await expect(page.locator('.owner-actions')).toBeVisible();
    await expect(page.locator('.drawer-content')).toContainText('归属变更记录');
    await expect(page.locator('.drawer-content')).toContainText('合成回归：人员调整');
    await page.screenshot({ path: path.join(output, 'transfer-log.png') });
    await page.locator('.el-drawer__close-btn').click();
    await expect(page.locator('.el-drawer')).toBeHidden();
    checks.push('detail drawer shows the ownership change log');

    // 有待审单据的任务被整批拒绝
    await page.getByRole('button', { name: '批量移交', exact: true }).click();
    await waitVisible(page, '移交夹具待审任务');
    await rows(page, '移交夹具待审任务').locator('.el-checkbox').click();
    await page.getByRole('button', { name: '确认移交（1）' }).click();
    await expect(dialog).toBeVisible();
    await selectInDialog(dialog, '目标归口部门', '审计测试部门B');
    await selectInDialog(dialog, '目标责任人', '审计用户B（910004）');
    await selectInDialog(dialog, '目标归口审核人', '审计审核人（910003）');
    await dialog.getByLabel('移交原因').fill('合成回归：应被拒绝');
    await dialog.getByRole('button', { name: '预览并确认' }).click();
    await dialog.getByRole('button', { name: '确认提交' }).click();
    await expect(dialog).toContainText('审核中的单据');
    await page.screenshot({ path: path.join(output, 'transfer-blocked.png') });
    const blocked = await (await api.get('/api/biz/tasks/930006', { headers: { Authorization: adminToken } })).json();
    assert.equal(Number(blocked.deptId), 920001, 'blocked task must stay unchanged');
    checks.push('task with an active audit is rejected as a whole');

    // 绩效页同样具备移交入口，共用同一个弹窗
    await page.goto(state.frontend + '/home/works/performance');
    await expect(page.locator('.transfer-btn')).toHaveText('批量移交');
    await page.getByRole('button', { name: '批量移交', exact: true }).click();
    await expect(page.locator('.table-wrapper .el-table__header .el-checkbox').first()).toBeVisible();
    await page.getByRole('button', { name: '退出批量移交' }).click();
    await expect(page.locator('.table-wrapper .el-table__header .el-checkbox')).toBeHidden();
    await page.locator('.el-table__body tr').first().waitFor({ state: 'attached' });
    await expand(page);
    await page.locator('.table-wrapper').getByText('查看', { exact: true }).first().click();
    const perfDrawer = page.locator('.el-drawer.open');
    await expect(perfDrawer).toBeVisible();
    await expect(perfDrawer.locator('.owner-actions')).toBeVisible();
    await perfDrawer.locator('.owner-actions').getByRole('button', { name: '移交' }).click();
    const perfDialog = page.locator('.transfer-dialog');
    await expect(perfDialog).toBeVisible();
    await expect(perfDialog).toContainText('绩效指标');
    await page.screenshot({ path: path.join(output, 'transfer-performance.png') });
    await perfDialog.getByRole('button', { name: '取消' }).click();
    await expect(perfDialog).toBeHidden();
    await perfDrawer.locator('.el-drawer__close-btn').click();
    await expect(perfDrawer).toBeHidden();
    checks.push('performance page exposes the same transfer entry for admins');

    assert.deepEqual(errors, []);
    await fs.writeFile(path.join(output, 'results.json'), JSON.stringify({ checks, errors }, null, 2));
    console.log(JSON.stringify({ checks }, null, 2));
  } finally {
    await browser.close();
    await api.dispose();
  }
})().catch(error => { console.error(error); process.exitCode = 1; });
