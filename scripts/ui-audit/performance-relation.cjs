// 绩效关联任务编辑回归：只在 --fixture performance-relation 的隔离环境运行，每个环境只跑一次。
const { chromium, request } = require('../../target/ui-audit-tools/node_modules/playwright');
const { expect } = require('../../target/ui-audit-tools/node_modules/playwright/test');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');

(async () => {
  const state = JSON.parse(await fs.readFile(path.resolve(__dirname, '../../target/ui-audit/state.json')));
  assert.match(state.schema, /^biz_review_test_[0-9a-f]{32}$/);
  const output = path.resolve(__dirname, '../../target/ui-audit/performance-relation');
  await fs.mkdir(output, { recursive: true });
  const api = await request.newContext({ baseURL: state.frontend });
  const login = async userId => {
    const result = await (await api.post('/api/system/login',
      { data: { user_id: userId, password: 'review-fixture-password' } })).json();
    assert.ok(result.token, `login failed for ${userId}`);
    return result.token;
  };
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const errors = [], checks = [];
  const settleTable = async page => {
    await expect(page.locator('.table-wrapper .el-loading-mask')).toBeHidden();
    await page.waitForTimeout(300);
  };
  const expand = async page => {
    await page.locator('.el-table__body tr').first().waitFor({ state: 'attached' });
    await settleTable(page);
    for (let i = 0; i < 8; i++) {
      const closed = page.locator('.el-table__expand-icon:not(.el-table__expand-icon--expanded):visible');
      if (!await closed.count()) break;
      await closed.first().click();
      await settleTable(page);
    }
  };
  const perfRow = (page, name) => page.locator('.el-table__body tr').filter({ hasText: name }).first();
  const openPerfDrawer = async (page, name) => {
    await perfRow(page, name).getByText('查看', { exact: true }).click();
    const drawer = page.locator('.el-drawer.open');
    await expect(drawer).toBeVisible();
    return drawer;
  };
  try {
    const adminToken = await login(110228);
    const userToken = await login(910001);

    // 非管理员：看不到关联任务数列与编辑入口，接口一律 403
    const userContext = await browser.newContext({ viewport: { width: 1600, height: 1000 } });
    await userContext.addInitScript(token => localStorage.setItem('token', token), userToken);
    const userPage = await userContext.newPage();
    userPage.on('pageerror', e => errors.push(e.stack || e.message));
    await userPage.goto(state.frontend + '/home/works/performance');
    await expect(userPage.locator('.table-wrapper .el-table__header')).toBeVisible();
    await expand(userPage);
    assert.equal(await userPage.locator('.table-wrapper .el-table__header .relation-count-cell').count(), 0,
      'non-admin must not see the relation count column');
    const userDrawer = await openPerfDrawer(userPage, '审计自动绩效');
    assert.equal(await userDrawer.getByRole('button', { name: '编辑关联' }).count(), 0,
      'non-admin must not see the relation edit entry');
    await userPage.locator('.el-drawer.open .el-drawer__close-btn').click();
    await expect(userPage.locator('.el-drawer.open')).toBeHidden();
    await userContext.close();

    assert.equal((await api.post('/api/manage/performance-relation', {
      headers: { Authorization: userToken },
      data: { perfId: 950001, year: 2026, addTaskIds: [930006], removeTaskIds: [], reason: 'synthetic' }
    })).status(), 403);
    assert.equal((await api.get('/api/manage/performance-relation/candidates?perfId=950001&year=2026',
      { headers: { Authorization: userToken } })).status(), 403);
    checks.push('non-admin sees no relation count column, no edit entry and gets 403');

    // 候选：已关联的 930002 与可新增的 930006；年度不一致与数据类型为 0 的任务不出现
    const candidates = await (await api.get('/api/manage/performance-relation/candidates?perfId=950001&year=2026',
      { headers: { Authorization: adminToken } })).json();
    assert.equal(candidates.year, 2026);
    const ids = candidates.candidates.map(item => Number(item.taskId));
    assert.deepEqual(ids.sort((a, b) => a - b), [930002, 930006], 'candidates should exclude year mismatch and dataType 0');
    assert.equal(Number(candidates.candidates.find(item => Number(item.taskId) === 930002).linked), 1);
    assert.equal(Number(candidates.candidates.find(item => Number(item.taskId) === 930006).linked), 0);

    const context = await browser.newContext({ viewport: { width: 1920, height: 1000 } });
    await context.addInitScript(token => localStorage.setItem('token', token), adminToken);
    const page = await context.newPage();
    page.on('pageerror', e => errors.push(e.stack || e.message));
    await page.goto(state.frontend + '/home/works/performance');
    await expect(page.locator('.table-wrapper .el-table__header')).toBeVisible();
    await expand(page);
    await expect(page.locator('.table-wrapper .el-table__header .relation-count-cell').first()).toBeVisible();
    await expect(perfRow(page, '审计自动绩效').locator('.relation-count-cell')).toHaveText('1');

    const drawer = await openPerfDrawer(page, '审计自动绩效');
    await expect(drawer.getByRole('button', { name: '编辑关联' })).toBeVisible();
    await drawer.getByRole('button', { name: '编辑关联' }).click();
    const dialog = page.locator('.relation-dialog');
    await expect(dialog).toBeVisible();
    await expect(dialog).toContainText('审计自动绩效');
    await expect(dialog).toContainText('2026');
    await expect(dialog.locator('.pick-item').filter({ hasText: '关联验收任务A' })).toBeVisible();
    assert.equal(await dialog.locator('.pick-item').filter({ hasText: '关联验收任务B' }).count(), 0,
      'year mismatch task must not be selectable');
    assert.equal(await dialog.locator('.pick-item').filter({ hasText: '关联验收任务C' }).count(), 0,
      'dataType 0 task must not be selectable');
    await page.waitForTimeout(500);
    await page.screenshot({ path: path.join(output, 'relation-pick.png') });

    await dialog.locator('.pick-item').filter({ hasText: '关联验收任务A' }).click();
    await expect(dialog.getByRole('button', { name: /下一步/ })).toContainText('新增 1、解除 0');
    await dialog.getByRole('button', { name: /下一步/ }).click();
    await expect(dialog).toContainText('新增关联 1 条');
    await dialog.getByLabel('变更原因').fill('合成回归：新增关联');
    await page.waitForTimeout(300);
    await page.screenshot({ path: path.join(output, 'relation-preview.png') });
    await dialog.getByRole('button', { name: '确认提交' }).click();
    await expect(dialog).toBeHidden();

    await expect(drawer.locator('.related-task-table tbody tr').filter({ hasText: '关联验收任务A' })).toBeVisible();
    await expect(drawer.locator('.related-task-table tbody tr').filter({ hasText: '仅用户A可见的三级任务' })).toBeVisible();
    await expect(drawer.locator('.relation-log')).toContainText('关联变更记录');
    await expect(drawer.locator('.relation-log')).toContainText('合成回归：新增关联');
    await expect(perfRow(page, '审计自动绩效').locator('.relation-count-cell')).toHaveText('2');
    const afterAdd = await (await api.get('/api/performance/950001', { headers: { Authorization: adminToken } })).json();
    assert.equal(Number(afterAdd.currentValue), 5, 'perf value should be recomputed from the new relation');
    await page.waitForTimeout(300);
    await page.screenshot({ path: path.join(output, 'relation-log.png') });
    checks.push('admin adds a relation in the dialog and the value is recomputed');

    // 解除关联：关联行保留（软删除），完成值回退
    await drawer.getByRole('button', { name: '编辑关联' }).click();
    await expect(dialog).toBeVisible();
    await dialog.locator('.pick-item').filter({ hasText: '关联验收任务A' }).click();
    await expect(dialog.getByRole('button', { name: /下一步/ })).toContainText('新增 0、解除 1');
    await dialog.getByRole('button', { name: /下一步/ }).click();
    await expect(dialog).toContainText('解除关联 1 条');
    await dialog.getByLabel('变更原因').fill('合成回归：解除关联');
    await dialog.getByRole('button', { name: '确认提交' }).click();
    await expect(dialog).toBeHidden();
    await expect(drawer.locator('.related-task-table tbody tr').filter({ hasText: '关联验收任务A' })).toHaveCount(0);
    await expect(drawer.locator('.relation-log')).toContainText('合成回归：解除关联');
    await expect(perfRow(page, '审计自动绩效').locator('.relation-count-cell')).toHaveText('1');
    const afterRemove = await (await api.get('/api/performance/950001', { headers: { Authorization: adminToken } })).json();
    assert.equal(Number(afterRemove.currentValue), 0, 'perf value should roll back after removing the relation');
    const logs = await (await api.get('/api/manage/performance-relation/logs?perfId=950001&year=2026',
      { headers: { Authorization: adminToken } })).json();
    assert.equal(logs.length, 2, 'both changes must be logged');
    assert.equal(logs[0].action, 'REMOVE');
    assert.equal(logs[1].action, 'ADD');
    assert.equal(Number(logs[0].operatorId), 110228);
    checks.push('admin removes a relation with soft delete and history kept');

    // 筛选之后隐藏关联任务数为 0 的自动指标
    await page.locator('.el-drawer.open .el-drawer__close-btn').click();
    await expect(page.locator('.el-drawer.open')).toBeHidden();
    const selectFilter = async (name, option) => {
      await page.getByRole('combobox', { name, exact: true }).filter({ visible: true })
        .locator('xpath=ancestor::*[contains(@class,"el-select__wrapper")]').click();
      await page.locator('.el-select-dropdown:visible').last().getByRole('option', { name: option, exact: true }).click();
      await page.waitForTimeout(400);
    };
    await expect(perfRow(page, '审计无关联绩效').locator('.relation-count-cell')).toHaveText('0');
    await selectFilter('绩效归口部门', '审计测试部门B');
    await expect(page.locator('.el-table__body tr').filter({ hasText: '审计无关联绩效' })).toHaveCount(0);
    await selectFilter('绩效归口部门', '审计测试部门A');
    await expect(perfRow(page, '审计自动绩效')).toBeVisible();
    assert.equal(await page.locator('.el-table__body tr').filter({ hasText: '审计无关联绩效' }).count(), 0,
      'zero relation indicator should stay hidden while filtering');
    checks.push('indicators without relations are hidden while filtering');

    assert.deepEqual(errors, []);
    await fs.writeFile(path.join(output, 'results.json'), JSON.stringify({ checks, errors }, null, 2));
    console.log(JSON.stringify({ checks }, null, 2));
  } finally {
    await browser.close();
    await api.dispose();
  }
})().catch(error => { console.error(error); process.exitCode = 1; });
