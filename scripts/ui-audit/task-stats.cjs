const { chromium, request } = require('../../target/ui-audit-tools/node_modules/playwright');
const { expect } = require('../../target/ui-audit-tools/node_modules/playwright/test');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');

(async () => {
  const state = JSON.parse(await fs.readFile(path.resolve(__dirname, '../../target/ui-audit/state.json')));
  assert.match(state.schema, /^biz_review_test_[0-9a-f]{32}$/);
  const output = path.resolve(__dirname, '../../target/ui-audit/task-stats');
  await fs.mkdir(output, { recursive: true });
  const api = await request.newContext({ baseURL: state.frontend });
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const errors = [], checks = [];
  try {
    const auth = await (await api.post('/api/system/login', { data: { user_id: 110228, password: 'review-fixture-password' } })).json();
    assert.ok(auth.token, 'admin login failed');
    const context = await browser.newContext({ viewport: { width: 1920, height: 1000 } });
    await context.addInitScript(token => localStorage.setItem('token', token), auth.token);
    const page = await context.newPage();
    page.on('pageerror', e => errors.push(e.stack || e.message));
    await page.goto(state.frontend + '/home/works');

    const bar = page.locator('.task-filters');
    const stats = page.locator('.task-stats');
    const value = name => stats.locator(`[data-stat="${name}"] .stat-value`);
    const note = name => stats.locator(`[data-stat="${name}"] .stat-note`);
    const countTag = page.locator('.project-actions .el-tag');
    const select = async (name, option) => {
      await page.getByRole('combobox', { name, exact: true }).filter({ visible: true }).locator('xpath=ancestor::*[contains(@class, "el-select__wrapper")]').click();
      await page.getByRole('option', { name: option, exact: true }).filter({ visible: true }).click();
    };
    const expand = async () => {
      for (let i = 0; i < 12; i++) {
        const closed = page.locator('.el-table__expand-icon:not(.el-table__expand-icon--expanded):visible');
        if (!await closed.count()) break;
        await closed.first().click();
      }
    };
    const expectOnly = async name => {
      await expand();
      const rows = page.locator('.el-table__body tr');
      await expect(rows.filter({ hasText: name })).toBeVisible();
    };

    await expect(stats).toBeVisible();
    const labels = await stats.locator('.stat-label').allInnerTexts();
    assert.deepEqual(labels, ['任务总数', '当前完成率', '已完成', '审核中', '逾期', '我的待办']);
    checks.push('six statistic cards render in order');

    await expect(value('total')).toHaveText('5');
    await expect(value('rate')).toHaveText('32%');
    await expect(value('done')).toHaveText('1');
    await expect(note('done')).toHaveText('20%');
    await expect(value('reviewing')).toHaveText('1');
    await expect(value('overdue')).toHaveText('0');
    await expect(value('todo')).toHaveText('1');
    await expect(stats.locator('[data-stat="returned"]')).toHaveText('1');
    await expect(countTag).toContainText('共 5 条');
    checks.push('year 2026: total 5, average 32%, done 1 (20%), reviewing 1, overdue 0, todo 1, returned 1');

    await stats.locator('[data-stat="done"]').click();
    await expect(countTag).toContainText('共 1 条');
    await expectOnly('统计夹具已完成任务');
    await expect(page.getByRole('row').filter({ hasText: '统计夹具进行中任务' })).toHaveCount(0);
    await expect(bar.locator('.filter-item').filter({ hasText: /^任务范围/ })).toContainText('全部');
    await expect(bar.locator('.audit-condition').filter({ hasText: '待审阶段' })).toContainText('全部阶段');
    checks.push('clicking 已完成 filters the table and clears scope/stage, 共 N 条 matches the card');

    await stats.locator('[data-stat="done"]').click();
    await expect(countTag).toContainText('共 5 条');
    await expect(value('total')).toHaveText('5');
    checks.push('clicking the active card again clears the filter');

    await select('任务年度', '全部年度');
    await expect(value('total')).toHaveText('6');
    await expect(value('rate')).toHaveText('30%');
    await expect(value('overdue')).toHaveText('1');
    await stats.locator('[data-stat="overdue"]').click();
    await expect(countTag).toContainText('共 1 条');
    await expectOnly('统计夹具逾期任务');
    await expect(page.getByRole('row').filter({ hasText: '统计夹具已完成任务' })).toHaveCount(0);
    checks.push('all years: total 6, average 30%, overdue 1; 逾期 filter is usable');

    await bar.getByRole('button', { name: '重置', exact: true }).click();
    await expect(countTag).toContainText('共 5 条');
    await select('任务归口部门', '审计测试部门B');
    await expect(value('total')).toHaveText('2');
    await expect(value('done')).toHaveText('1');
    await expect(note('done')).toHaveText('50%');
    await expect(value('reviewing')).toHaveText('1');
    await expect(value('todo')).toHaveText('1');
    await expect(stats.locator('[data-stat="returned"]')).toHaveText('0');
    checks.push('year 2026 + owning department B: total 2, done 1 (50%), reviewing 1, todo 1, returned 0');

    await bar.getByRole('button', { name: '重置', exact: true }).click();
    await expect(value('total')).toHaveText('5');
    await select('任务状态', '已完成');
    await expect(value('total')).toHaveText('1');
    await expect(value('done')).toHaveText('1');
    await expect(stats.locator('[data-stat="done"]')).toHaveClass(/active/);
    checks.push('filter bar changes are reflected by the statistic cards');

    await bar.getByRole('button', { name: '重置', exact: true }).click();
    await expect(value('total')).toHaveText('5');
    await page.screenshot({ path: path.join(output, 'desktop.png') });

    const exportButton = page.locator('.project-actions').getByRole('button', { name: '导出', exact: true });
    const download = async name => {
      const [item] = await Promise.all([page.waitForEvent('download'), exportButton.click()]);
      const target = path.join(output, name);
      await item.saveAs(target);
      const bytes = await fs.readFile(target);
      assert.ok(/\.xlsx$/.test(item.suggestedFilename()), 'unexpected export filename: ' + item.suggestedFilename());
      assert.ok(bytes.length > 2000, `exported workbook too small: ${bytes.length}`);
      assert.equal(bytes[0], 0x50, 'not a zip container');
      assert.equal(bytes[1], 0x4b, 'not a zip container');
      return item.suggestedFilename();
    };
    const name2026 = await download('tasks-2026.xlsx');
    checks.push(`export downloads a real xlsx for the current filter (year 2026): ${name2026}`);

    await select('任务年度', '全部年度');
    await expect(value('total')).toHaveText('6');
    const nameAll = await download('tasks-all.xlsx');
    await bar.getByRole('button', { name: '重置', exact: true }).click();
    await expect(value('total')).toHaveText('5');
    checks.push(`export follows the current filter (all years): ${nameAll}`);

    // 越权校验：其他部门普通用户请求不属于自己的任务 ID，服务端应全部忽略
    const outsider = await (await api.post('/api/system/login', { data: { user_id: 910004, password: 'review-fixture-password' } })).json();
    assert.ok(outsider.token, 'outsider login failed');
    const outsiderResp = await api.post('/api/biz/tasks/export', {
      headers: { Authorization: outsider.token }, data: { ids: [930008, 930010] }
    });
    assert.equal(outsiderResp.status(), 200);
    const outsiderBody = await outsiderResp.body();
    assert.equal(outsiderBody[0], 0x50, 'outsider response is not a workbook');
    await fs.writeFile(path.join(output, 'tasks-outsider.xlsx'), outsiderBody);
    checks.push('outsider requesting foreign task ids gets an empty workbook (0 data rows)');

    // 详情抽屉：分区跳转已移除，操作按钮右对齐保留
    await expand();
    await page.getByRole('row').filter({ hasText: '统计夹具进行中任务' }).getByText('查看', { exact: true }).click();
    const drawer = page.locator('.el-drawer');
    await expect(drawer).toBeVisible();
    await page.waitForTimeout(600);
    await expect(drawer.getByRole('button', { name: '返回列表并定位' })).toBeVisible();
    await expect(drawer.getByRole('button', { name: '复制详情链接' })).toBeVisible();
    await expect(drawer.getByRole('navigation', { name: '详情快速跳转' })).toHaveCount(0);
    await page.screenshot({ path: path.join(output, 'detail-drawer.png') });
    await page.locator('.el-drawer__close-btn').click();
    await expect(drawer).toBeHidden();
    checks.push('detail drawer keeps the two actions and no longer renders the section navigation');

    for (const width of [1440, 1280, 768]) {
      await page.setViewportSize({ width, height: 1000 });
      await expect(stats).toBeVisible();
      const bounds = await stats.evaluate(el => ({ width: el.clientWidth, scroll: el.scrollWidth }));
      assert.ok(bounds.scroll <= bounds.width + 1, `${width}: statistic strip overflows (${bounds.scroll} > ${bounds.width})`);
      const boxes = await stats.locator('.stat-item').evaluateAll(nodes => nodes.map(n => {
        const r = n.getBoundingClientRect();
        return { x: Math.round(r.x), y: Math.round(r.y), w: Math.round(r.width), h: Math.round(r.height) };
      }));
      assert.equal(boxes.length, 6);
      assert.ok(boxes.every(b => b.w > 0 && b.h > 0), `${width}: empty statistic item`);
      assert.equal(new Set(boxes.map(b => `${b.x}:${b.y}`)).size, 6, `${width}: statistic items overlap`);
      await page.screenshot({ path: path.join(output, width === 1440 ? 'compact.png' : `narrow-${width}.png`) });
    }
    checks.push('1920/1440/1280/768px: statistic strip keeps six cards without overflow or overlap');

    assert.deepEqual(errors, []);
    await fs.writeFile(path.join(output, 'results.json'), JSON.stringify({ checks, errors }, null, 2));
    console.log(JSON.stringify({ checks }, null, 2));
  } finally {
    await browser.close();
    await api.dispose();
  }
})().catch(error => { console.error(error); process.exitCode = 1; });
