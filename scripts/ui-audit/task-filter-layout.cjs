const { chromium, request } = require('../../target/ui-audit-tools/node_modules/playwright');
const { expect } = require('../../target/ui-audit-tools/node_modules/playwright/test');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');

(async () => {
  const state = JSON.parse(await fs.readFile(path.resolve(__dirname, '../../target/ui-audit/state.json')));
  assert.match(state.schema, /^biz_review_test_[0-9a-f]{32}$/);
  assert.equal(state.frontend, 'http://127.0.0.1:15273');
  const output = path.resolve(__dirname, '../../target/ui-audit/task-filter-layout');
  await fs.mkdir(output, { recursive: true });
  const api = await request.newContext({ baseURL: state.frontend });
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const errors = [], checks = [];
  try {
    await api.post('/api/system/password/reset', { data: { user_id: 110228, old_password: 'review-fixture-password', new_password: 'Convenience123' } });
    const auth = await (await api.post('/api/system/login', { data: { user_id: 110228, password: 'Convenience123' } })).json();
    assert.ok(auth.token);
    const context = await browser.newContext({ viewport: { width: 1920, height: 1000 } });
    await context.addInitScript(token => localStorage.setItem('token', token), auth.token);
    const page = await context.newPage();
    page.on('pageerror', e => errors.push(e.stack || e.message));
    await page.goto(state.frontend + '/home/works');
    const bar = page.locator('.task-filters');
    await expect(bar.getByRole('combobox', { name: '任务范围', exact: true })).toBeEnabled();
    const select = async (name, option) => {
      await page.getByRole('combobox', { name, exact: true }).filter({ visible: true }).locator('xpath=ancestor::*[contains(@class, "el-select__wrapper")]').click();
      await page.getByRole('option', { name: option, exact: true }).filter({ visible: true }).click();
    };
    const expand = async () => {
      for (let i = 0; i < 10; i++) {
        const closed = page.locator('.el-table__expand-icon:not(.el-table__expand-icon--expanded):visible');
        if (!await closed.count()) break;
        await closed.first().click();
      }
    };
    await expect(bar.getByRole('combobox', { name: '任务归口部门', exact: true })).toBeVisible();
    const boxes = await bar.locator('.el-select__wrapper:visible, .el-input__wrapper:visible, button:visible').evaluateAll(nodes => nodes.map(n => ({ y: n.getBoundingClientRect().y, h: n.getBoundingClientRect().height })));
    assert.ok(new Set(boxes.map(b => Math.round(b.y))).size <= 2);
    assert.ok(boxes.every(b => b.h === 32));
    await page.screenshot({ path: path.join(output, 'desktop.png') });
    checks.push('1920px: controls use at most two rows and consistent 32px height');
    await select('任务归口部门', '审计测试部门A');
    await expand();
    await expect(page.getByRole('row').filter({ hasText: '客户待专业群审核任务' })).toBeVisible();
    await select('任务状态', '审核中');
    await select('任务待审阶段', '待归口部门审核');
    await select('任务当前处理部门', '审计测试部门B');
    await expand();
    await expect(page.getByRole('row').filter({ hasText: '客户待归口审核任务' })).toBeVisible();
    await expect(page.getByRole('row').filter({ hasText: '客户待专业群审核任务' })).toHaveCount(0);
    await select('任务范围', '我负责的');
    await select('任务范围', '全部');
    checks.push('range, status, stage and actual handling department filters remain usable');
    await page.setViewportSize({ width: 1440, height: 1000 });
    await expect(bar.getByRole('combobox', { name: '任务归口部门', exact: true })).toBeVisible();
    const ownerBox = await bar.getByRole('combobox', { name: '任务归口部门', exact: true }).boundingBox();
    const handlerBox = await bar.getByRole('combobox', { name: '任务当前处理部门', exact: true }).boundingBox();
    assert.ok(ownerBox.x < handlerBox.x && Math.abs(ownerBox.y - handlerBox.y) < 1);
    await select('任务当前处理部门', '审计测试部门A');
    await expect(page.getByRole('row').filter({ hasText: '客户待归口审核任务' })).toHaveCount(0);
    await page.screenshot({ path: path.join(output, 'compact.png') });
    await bar.getByRole('button', { name: '查询', exact: true }).click();
    await page.reload();
    await expect(bar.locator('.audit-condition').filter({ hasText: /^归口部门/ })).toContainText('审计测试部门A');
    await expect(bar.locator('.audit-condition').filter({ hasText: '当前处理部门' })).toContainText('审计测试部门A');
    await bar.getByRole('button', { name: '重置', exact: true }).click();
    await expect(bar.locator('.audit-condition').filter({ hasText: '当前处理部门' })).toContainText('全部部门');
    await expect(bar.locator('.audit-condition').filter({ hasText: /^归口部门/ })).toContainText('全部部门');
    await expand();
    await expect(page.getByRole('row').filter({ hasText: '客户待归口审核任务' })).toBeVisible();
    checks.push('1440px: owning department before current handler department; independent intersection, memory and reset');
    await bar.getByRole('textbox', { name: '任务搜索' }).fill('不存在的任务名称');
    await bar.getByRole('textbox', { name: '任务搜索' }).press('Enter');
    await expect(page.getByRole('row').filter({ hasText: '客户待归口审核任务' })).toHaveCount(0);
    await bar.getByRole('button', { name: '重置', exact: true }).click();
    await expand();
    await expect(page.getByRole('row').filter({ hasText: '客户待归口审核任务' })).toBeVisible();
    checks.push('keyword Enter search and reset preserve existing behavior');
    for (const width of [1280, 1024, 768]) {
      await page.setViewportSize({ width, height: 1000 });
      await expect(bar.getByRole('button', { name: '重置', exact: true })).toBeVisible();
      const bounds = await bar.evaluate(el => ({ width: el.clientWidth, scroll: el.scrollWidth }));
      assert.ok(bounds.scroll <= bounds.width + 1, `${width}: filter overflow`);
    }
    await page.screenshot({ path: path.join(output, 'narrow.png') });
    checks.push('1280/1024/768px: filter controls do not overflow');
    assert.deepEqual(errors, []);
    await fs.writeFile(path.join(output, 'results.json'), JSON.stringify({ checks, errors }, null, 2));
    console.log(JSON.stringify({ checks, errors }, null, 2));
  } finally { await browser.close(); await api.dispose(); }
})().catch(e => { console.error(e); process.exitCode = 1; });
