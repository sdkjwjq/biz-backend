const { chromium, request } = require('../../target/ui-audit-tools/node_modules/playwright');
const { expect } = require('../../target/ui-audit-tools/node_modules/playwright/test');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');

async function main() {
  const root = path.resolve(__dirname, '../../target/ui-audit');
  const state = JSON.parse(await fs.readFile(path.join(root, 'state.json')));
  assert.match(state.schema, /^biz_review_test_[0-9a-f]{32}$/);
  assert.equal(state.frontend, 'http://127.0.0.1:15273');
  const output = path.join(root, 'evidence-convenience-batch2'); await fs.mkdir(output, { recursive: true });
  const api = await request.newContext({ baseURL: state.frontend });
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const checks = [], errors = [];
  let page;
  const passed = name => { checks.push(name); console.log(name); };
  const token = async id => {
    await api.post('/api/system/password/reset', { data: { user_id: id, old_password: 'review-fixture-password', new_password: 'Convenience123' } });
    const data = await (await api.post('/api/system/login', { data: { user_id: id, password: 'Convenience123' } })).json();
    assert.ok(data.token); return data.token;
  };
  const create = async id => {
    const context = await browser.newContext({ viewport: { width: 1440, height: 900 } });
    await context.addInitScript(value => { if (!localStorage.getItem('token')) localStorage.setItem('token', value); }, await token(id));
    const tab = await context.newPage(); tab.setDefaultTimeout(15000);
    tab.on('pageerror', e => errors.push(e.message)); return tab;
  };
  const quick = name => page.locator('.quick-filters').getByRole('radio', { name, exact: true });
  const select = async name => { await expect(quick(name)).toBeEnabled(); await page.locator('.quick-filters .el-radio-button').filter({ hasText: name }).click(); };
  const expand = async () => {
    for (let i = 0; i < 20; i++) {
      const closed = page.locator('.el-table__expand-icon:not(.el-table__expand-icon--expanded):visible');
      if (!await closed.count()) break; await closed.first().click();
    }
  };
  try {
    page = await create(910003);
    await page.goto(state.frontend + '/home/works/performance');
    await select('待我审核'); await expand();
    await expect(page.getByRole('row').filter({ hasText: '审计手动绩效' })).toBeVisible();
    await expect(page.getByRole('row').filter({ hasText: '审计零值绩效B' })).toBeVisible();
    await expect(page.getByRole('row').filter({ hasText: '审计待填绩效C' })).toHaveCount(0);
    await page.reload(); await expect(quick('待我审核')).toBeChecked();
    await expect(page.getByRole('row').filter({ hasText: '审计手动绩效' })).toBeVisible();
    passed('performance-real-todo-filter-and-refresh-expansion-memory');
    await page.getByRole('row').filter({ hasText: '审计手动绩效' }).getByRole('button', { name: '查看', exact: true }).click();
    let drawer = page.getByRole('dialog', { name: '绩效指标详情', exact: true });
    await expect(drawer.locator('.adjacent-details')).toContainText('1 / 2');
    await expect(drawer.getByRole('button', { name: '上一个', exact: true })).toBeDisabled();
    await drawer.getByRole('button', { name: '下一个', exact: true }).click();
    await expect(drawer.locator('.adjacent-details')).toContainText('2 / 2');
    await expect(drawer.getByRole('button', { name: '下一个', exact: true })).toBeDisabled();
    await expect(drawer.getByText('审计零值绩效B', { exact: true })).toBeVisible();
    const opinion = drawer.getByPlaceholder('请输入审批意见（必填）...');
    await opinion.fill('保留审批草稿');
    await drawer.getByRole('button', { name: '上一个', exact: true }).click();
    let warning = page.getByRole('dialog', { name: '切换详情', exact: true });
    await warning.getByRole('button', { name: '继续编辑', exact: true }).click();
    await expect(opinion).toHaveValue('保留审批草稿');
    await drawer.getByRole('button', { name: '上一个', exact: true }).click();
    await warning.getByRole('button', { name: '放弃并切换', exact: true }).click();
    await expect(drawer.locator('.adjacent-details')).toContainText('1 / 2');
    await expect(opinion).toHaveValue('同意');
    await warning.waitFor({ state: 'hidden' });
    await page.screenshot({ path: path.join(output, 'performance-navigation.png') });
    passed('performance-filtered-navigation-count-and-bounds');
    await drawer.getByRole('button', { name: /close/i }).click();
    await select('我负责的'); await expect(page.getByRole('row').filter({ hasText: '审计手动绩效' })).toHaveCount(0);
    await page.getByRole('button', { name: '重置筛选', exact: true }).click();
    await expect(quick('全部')).toBeChecked();
    passed('performance-mine-empty-and-reset');
    await select('已退回'); await expand();
    await expect(page.getByRole('row').filter({ hasText: '审计待填绩效C' })).toBeVisible();
    await expect(page.getByRole('row').filter({ hasText: '审计手动绩效' })).toHaveCount(0);
    await page.locator('.filter-item').filter({ hasText: '年度' }).locator('.el-select').click();
    await page.getByRole('option', { name: '2027年', exact: true }).click(); await expand();
    await expect(page.getByRole('row').filter({ hasText: '审计手动绩效' })).toBeVisible();
    await page.reload(); await expect(quick('已退回')).toBeChecked();
    await expect(page.getByRole('row').filter({ hasText: '审计手动绩效' })).toBeVisible();
    passed('performance-returned-year-intersection-and-year-memory');

    page = await create(910001);
    await page.goto(state.frontend + '/home/works'); await select('我负责的'); await expand();
    await expect(page.getByRole('row').filter({ hasText: '仅用户A可见的三级任务' })).toBeVisible();
    await page.reload(); await expect(quick('我负责的')).toBeChecked();
    await expect(page.getByRole('row').filter({ hasText: '仅用户A可见的三级任务' })).toBeVisible();
    await select('已退回'); await expect(page.getByRole('row').filter({ hasText: '仅用户A可见的三级任务' })).toHaveCount(0);
    await expand();
    await expect(page.getByRole('row').filter({ hasText: '导航测试B' })).toBeVisible();
    await expect(page.getByRole('row').filter({ hasText: '导航测试C' })).toHaveCount(0);
    await page.getByRole('button', { name: '重置筛选', exact: true }).click(); await expect(quick('全部')).toBeChecked();
    passed('task-mine-memory-returned-empty-and-reset');

    page = await create(1910001);
    await page.goto(state.frontend + '/home/works/achievement'); await select('我创建的'); await expand();
    await expect(page.getByRole('row').filter({ hasText: '便利性合成成果' })).toBeVisible();
    await page.reload(); await expect(quick('我创建的')).toBeChecked();
    await expect(page.getByRole('row').filter({ hasText: '便利性合成成果' })).toBeVisible();
    await page.getByRole('row').filter({ hasText: '便利性合成成果' }).getByRole('button', { name: '查看', exact: true }).click();
    drawer = page.getByRole('dialog', { name: '成果详情', exact: true });
    await expect(drawer.locator('.adjacent-details')).toContainText('1 / 2');
    await expect(drawer.getByRole('button', { name: '上一个', exact: true })).toBeDisabled();
    await drawer.getByRole('button', { name: '下一个', exact: true }).click();
    await expect(drawer.locator('.adjacent-details')).toContainText('2 / 2');
    await expect(drawer.getByPlaceholder('请输入建设成果名称')).toHaveValue('便利性退回成果B');
    await expect(drawer.getByRole('button', { name: '下一个', exact: true })).toBeDisabled();
    await page.screenshot({ path: path.join(output, 'achievement-memory.png') });
    passed('achievement-real-owner-memory-category-order-and-bounds');
    await drawer.getByRole('button', { name: /close/i }).click();
    await select('待我审核'); await expect(page.getByRole('row').filter({ hasText: '便利性合成成果' })).toHaveCount(0);
    passed('achievement-real-todo-excludes-archived');
    await select('已退回'); await expand();
    await page.getByRole('row').filter({ hasText: '便利性退回成果B' }).getByRole('button', { name: '查看', exact: true }).click();
    await expect(drawer.locator('.adjacent-details')).toContainText('1 / 1');
    await expect(drawer.getByRole('button', { name: '下一个', exact: true })).toBeDisabled();
    await drawer.getByRole('button', { name: /close/i }).click();
    passed('achievement-returned-single-result-bounds');
    await page.locator('.user-trigger').click(); await page.getByText('退出登录', { exact: true }).click();
    await page.getByRole('dialog').getByRole('button', { name: '确定', exact: true }).click();
    await expect(page).toHaveURL(/\/login/);
    assert.equal(await page.evaluate(() => Object.keys(sessionStorage).filter(key => key.startsWith('works_')).length), 0);
    await page.getByPlaceholder('账号', { exact: true }).fill('910001');
    await page.getByPlaceholder('密码', { exact: true }).fill('Convenience123');
    await page.getByRole('button', { name: '登 录', exact: true }).click();
    await page.goto(state.frontend + '/home/works/achievement'); await expect(quick('全部')).toBeChecked();
    passed('logout-clears-memory-and-switch-account-defaults');
    page = await create(910001);
    await page.goto(state.frontend + '/home/works/performance'); await select('全部');
    await page.getByPlaceholder('搜索指标编码或名称...').fill('审计待填绩效C');
    await page.getByPlaceholder('搜索指标编码或名称...').press('Enter'); await expand();
    await expect(page.getByRole('row').filter({ hasText: '审计待填绩效C' })).toBeVisible();
    await page.getByPlaceholder('搜索指标编码或名称...').fill('尚未执行的搜索');
    await page.reload();
    await expect(page.getByPlaceholder('搜索指标编码或名称...')).toHaveValue('审计待填绩效C');
    await expect(page.getByRole('row').filter({ hasText: '审计待填绩效C' })).toBeVisible();
    passed('memory-saves-executed-search-only');
    await page.goto(state.frontend + '/home/works/performance?perfId=950011&year=2027');
    drawer = page.getByRole('dialog', { name: '绩效指标详情', exact: true });
    await expect(drawer.getByText('审计手动绩效', { exact: true })).toBeVisible();
    passed('explicit-link-overrides-remembered-filter');

    page = await create(910001);
    await page.route('**/api/biz/tasks/quick-filters', route => route.fulfill({ status: 500, json: { code: 500, message: '模拟摘要失败' } }));
    await page.goto(state.frontend + '/home/works');
    await expect(page.locator('.quick-filters [role=alert]')).toContainText('快捷筛选加载失败');
    await page.unroute('**/api/biz/tasks/quick-filters');
    await page.locator('.quick-filters').getByRole('button', { name: '重试', exact: true }).click();
    await expect(page.locator('.quick-filters [role=alert]')).toHaveCount(0);
    await select('已退回'); await expand();
    await expect(page.getByRole('row').filter({ hasText: '导航测试B' })).toBeVisible();
    passed('injected-shortcut-failure-and-real-retry');
    await page.context().addInitScript(() => {
      const get = Storage.prototype.getItem, set = Storage.prototype.setItem;
      Storage.prototype.getItem = function(key) { if (this === sessionStorage) throw new Error('blocked storage'); return get.call(this, key); };
      Storage.prototype.setItem = function(key, value) { if (this === sessionStorage) throw new Error('blocked storage'); return set.call(this, key, value); };
    });
    await page.reload(); await select('我负责的'); await expand();
    await expect(page.getByRole('row').filter({ hasText: '仅用户A可见的三级任务' })).toBeVisible();
    passed('injected-session-storage-denied-keeps-browsing-working');

    page = await create(910001);
    await page.setViewportSize({ width: 1280, height: 450 });
    await page.goto(state.frontend + '/home/works'); await select('全部'); await expand();
    await page.getByRole('row').filter({ hasText: '导航测试C' }).scrollIntoViewIfNeeded();
    await expect.poll(() => page.evaluate(() => Object.entries(sessionStorage).some(([key, value]) => key.startsWith('works_browse_v1_tasks') && JSON.parse(value).scrollTop > 0))).toBe(true);
    const remembered = await page.evaluate(() => JSON.parse(Object.entries(sessionStorage).find(([key]) => key.startsWith('works_browse_v1_tasks'))[1]));
    await page.reload(); await expect(quick('全部')).toBeEnabled();
    const restored = await page.evaluate(() => JSON.parse(Object.entries(sessionStorage).find(([key]) => key.startsWith('works_browse_v1_tasks'))[1]));
    assert.ok(Math.abs(remembered.scrollTop - restored.scrollTop) < 5, `${remembered.scrollTop} != ${restored.scrollTop}`);
    assert.equal(remembered.anchor, restored.anchor);
    passed('real-scroll-anchor-restored-after-refresh');
    await page.route('**/api/biz/tasks', async route => {
      const response = await route.fetch(); const data = await response.json();
      await route.fulfill({ response, json: data.filter(row => row.taskId !== 930012) });
    });
    await page.evaluate(() => {
      const key = Object.keys(sessionStorage).find(key => key.startsWith('works_browse_v1_tasks'));
      const value = JSON.parse(sessionStorage.getItem(key)); value.expanded.push('deleted-directory');
      value.anchor = 'detail-row-930012'; sessionStorage.setItem(key, JSON.stringify(value));
    });
    await page.reload(); await expect(quick('全部')).toBeEnabled();
    await expect(page.getByRole('row').filter({ hasText: '导航测试C' })).toHaveCount(0);
    assert.equal(await page.evaluate(() => JSON.parse(Object.entries(sessionStorage).find(([key]) => key.startsWith('works_browse_v1_tasks'))[1]).expanded.includes('deleted-directory')), false);
    passed('injected-deleted-anchor-and-directory-repaired');

    page = await create(910003);
    let release, seen;
    const blocked = new Promise(resolve => { release = resolve; });
    const started = new Promise(resolve => { seen = resolve; });
    await page.route('**/api/performance/audit/perf/950011?*', async route => {
      const response = await route.fetch(); seen(); await blocked; await route.fulfill({ response });
    });
    await page.goto(state.frontend + '/home/works/performance'); await select('待我审核'); await expand();
    await page.getByRole('row').filter({ hasText: '审计手动绩效' }).getByRole('button', { name: '查看', exact: true }).click();
    await started;
    drawer = page.getByRole('dialog', { name: '绩效指标详情', exact: true });
    await expect(drawer.getByRole('button', { name: '下一个', exact: true })).toBeDisabled();
    await drawer.getByRole('button', { name: /close/i }).click();
    await page.getByRole('row').filter({ hasText: '审计零值绩效B' }).getByRole('button', { name: '查看', exact: true }).click();
    await expect(drawer.getByText('审计零值绩效B', { exact: true })).toBeVisible();
    release();
    await expect(drawer.getByRole('button', { name: '上一个', exact: true })).toBeEnabled();
    await expect(drawer.getByText('审计零值绩效B', { exact: true })).toBeVisible();
    passed('injected-slow-response-busy-guard-and-close-reopen-isolation');
    assert.deepEqual(errors, []);
    await fs.writeFile(path.join(output, 'results.json'), JSON.stringify({ checks, errors }, null, 2));
  } catch (error) {
    if (page) { await page.screenshot({ path: path.join(output, 'failure.png') }); console.error((await page.locator('body').innerText()).slice(0, 3500)); }
    throw error;
  } finally { await browser.close(); await api.dispose(); }
}
main().catch(error => { console.error(error); process.exitCode = 1; });
