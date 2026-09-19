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
  const output = path.join(root, 'evidence-convenience-batch1'); await fs.mkdir(output, { recursive: true });
  const api = await request.newContext({ baseURL: state.frontend });
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const errors = [], checks = [];
  let page;
  const passed = name => { checks.push(name); console.log(name); };
  const loginToken = async id => {
    await api.post('/api/system/password/reset', { data: { user_id: id, old_password: 'review-fixture-password', new_password: 'Convenience123' } });
    const response = await (await api.post('/api/system/login', { data: { user_id: id, password: 'Convenience123' } })).json();
    assert.ok(response.token); return response.token;
  };
  const create = async token => {
    const context = await browser.newContext({ viewport: { width: 1440, height: 1000 } });
    if (token) await context.addInitScript(value => localStorage.setItem('token', value), token);
    const tab = await context.newPage(); tab.setDefaultTimeout(12000);
    tab.on('pageerror', e => errors.push(e.message));
    return tab;
  };
  const returnButton = drawer => drawer.getByRole('button', { name: '返回列表并定位', exact: true });
  const ready = async drawer => { await drawer.waitFor(); await expect(returnButton(drawer)).toBeEnabled(); };
  try {
    // Fresh fixture: real weak admin login must preserve the performance link through mandatory password change.
    page = await create();
    await page.goto(state.frontend + '/home/works/performance?perfId=950011&year=2027');
    await page.getByPlaceholder('账号', { exact: true }).fill('110228');
    await page.getByPlaceholder('密码', { exact: true }).fill('review-fixture-password');
    await page.getByRole('button', { name: '登 录', exact: true }).click();
    const required = page.getByRole('dialog', { name: '请先修改密码', exact: true });
    await required.waitFor();
    await required.getByPlaceholder('请输入新密码', { exact: true }).fill('Convenience123');
    await required.getByPlaceholder('请再次输入新密码', { exact: true }).fill('Convenience123');
    await required.getByRole('button', { name: '保存', exact: true }).click();
    let drawer = page.getByRole('dialog', { name: '绩效指标详情', exact: true }); await ready(drawer);
    assert.equal(new URL(page.url()).searchParams.get('year'), '2027');
    await drawer.getByText('审计手动绩效', { exact: true }).waitFor();
    passed('unauthenticated-link-login-and-forced-password-return');
    await page.evaluate(() => Object.defineProperty(navigator, 'clipboard', { configurable: true, value: { writeText: async () => { throw new Error('blocked'); } } }));
    await drawer.getByRole('button', { name: '复制详情链接', exact: true }).click();
    const copy = page.getByRole('dialog', { name: '手动复制详情链接', exact: true });
    await copy.waitFor();
    const link = await copy.getByRole('textbox', { name: '详情链接' }).inputValue();
    assert.equal(new URL(link).searchParams.get('perfId'), '950011');
    assert.equal(new URL(link).searchParams.get('year'), '2027');
    assert.ok(!link.includes('token'));
    await copy.getByRole('button', { name: /close/i }).click();
    await returnButton(drawer).click(); await drawer.waitFor({ state: 'hidden' });
    await page.locator('.detail-row-perf-950011:visible').waitFor();
    passed('performance-link-fallback-year-and-deep-tree-location');

    await page.goto(state.frontend + '/home/works/achievement?achId=979101&year=2026');
    drawer = page.getByRole('dialog', { name: '成果详情', exact: true }); await ready(drawer);
    await expect(drawer.getByPlaceholder('请输入建设成果名称')).toHaveValue('便利性合成成果');
    await returnButton(drawer).click(); await drawer.waitFor({ state: 'hidden' });
    await page.getByRole('row').filter({ hasText: '便利性合成成果' }).waitFor();
    passed('achievement-link-category-location');

    const userToken = await loginToken(910001); page = await create(userToken);
    await page.goto(state.frontend + '/home/works?taskId=930002');
    drawer = page.getByRole('dialog', { name: '任务详情与反馈', exact: true }); await ready(drawer);
    const scrollBefore = await page.evaluate(() => window.scrollY);
    await drawer.getByRole('navigation', { name: '详情快速跳转' }).getByRole('button', { name: '材料', exact: true }).click();
    await expect.poll(() => drawer.locator('.el-drawer__body').evaluate(el => el.scrollTop)).toBeGreaterThan(0);
    assert.equal(await page.evaluate(() => window.scrollY), scrollBefore);
    await drawer.getByPlaceholder('请输入完成情况（50字以内）').fill('保留我的草稿');
    await returnButton(drawer).click();
    let warning = page.getByRole('dialog', { name: '未提交内容', exact: true });
    await warning.getByRole('button', { name: '继续编辑', exact: true }).click();
    await expect(drawer.getByPlaceholder('请输入完成情况（50字以内）')).toHaveValue('保留我的草稿');
    await returnButton(drawer).click(); await warning.getByRole('button', { name: '放弃并返回', exact: true }).click();
    await drawer.waitFor({ state: 'hidden' }); await page.locator('.detail-row-930002:visible').waitFor();
    passed('task-section-scroll-dirty-cancel-discard-location');

    // Explicit injected state change: the item becomes completed while the current filter is in progress.
    await page.goto(state.frontend + '/home/works');
    await page.locator('.filter-item').filter({ hasText: '状态:' }).locator('.el-select').click();
    await page.getByRole('option', { name: '进行中', exact: true }).click();
    for (let i = 0; i < 4; i++) {
      const closed = page.locator('.el-table__expand-icon:not(.el-table__expand-icon--expanded):visible');
      if (!await closed.count()) break; await closed.first().click();
    }
    await page.getByRole('row').filter({ hasText: '仅用户A可见的三级任务' }).getByRole('button', { name: '查看', exact: true }).click();
    await ready(drawer);
    await page.route('**/api/biz/tasks', async route => {
      const response = await route.fetch(); const data = await response.json();
      data.forEach(item => { if (item.taskId === 930002) item.status = '3'; });
      await route.fulfill({ response, json: data });
    });
    await returnButton(drawer).click();
    warning = page.getByRole('dialog', { name: '定位记录', exact: true });
    await warning.getByRole('button', { name: '保留筛选', exact: true }).click();
    await expect(drawer).toBeVisible();
    await returnButton(drawer).click(); await warning.getByRole('button', { name: '清除筛选并定位', exact: true }).click();
    await drawer.waitFor({ state: 'hidden' }); await page.locator('.detail-row-930002:visible').waitFor();
    passed('filtered-out-item-keeps-filter-until-confirmation');
    await page.screenshot({ path: path.join(output, 'located-task.png'), fullPage: true });

    await page.unroute('**/api/biz/tasks');
    await page.goto(state.frontend + '/home/works/performance?perfId=950031&year=2026');
    const perfDrawer = page.getByRole('dialog', { name: '绩效指标详情', exact: true }); await ready(perfDrawer);
    await perfDrawer.getByRole('spinbutton').first().fill('7');
    await returnButton(perfDrawer).click();
    warning = page.getByRole('dialog', { name: '未提交内容', exact: true });
    await warning.getByRole('button', { name: '继续编辑', exact: true }).click();
    await expect(perfDrawer.getByRole('spinbutton').first()).toHaveValue('7');
    await returnButton(perfDrawer).click(); await warning.getByRole('button', { name: '放弃并返回', exact: true }).click();
    await perfDrawer.waitFor({ state: 'hidden' });
    passed('performance-unsaved-value-protection');

    const limited = await loginToken(910004); page = await create(limited);
    await page.goto(state.frontend + '/home/works?taskId=930002');
    await page.getByText('任务不存在、已删除或无权访问', { exact: true }).waitFor();
    assert.equal(await page.getByRole('dialog', { name: '任务详情与反馈', exact: true }).count(), 0);
    passed('unauthorized-task-link-does-not-open-details');

    const admin = await loginToken(110228); page = await create(admin);
    await page.goto(state.frontend + '/home/works?taskId=930002');
    drawer = page.getByRole('dialog', { name: '任务详情与反馈', exact: true }); await ready(drawer);
    assert.equal(await drawer.getByRole('navigation').getByRole('button', { name: '材料', exact: true }).count(), 0);
    await page.evaluate(() => Object.defineProperty(navigator, 'clipboard', { configurable: true, value: { writeText: async value => { window.copiedDetail = value; } } }));
    await drawer.getByRole('button', { name: '复制详情链接', exact: true }).click();
    assert.equal(new URL(await page.evaluate(() => window.copiedDetail)).searchParams.get('taskId'), '930002');
    await page.screenshot({ path: path.join(output, 'detail-tools.png'), fullPage: true });
    await page.route('**/api/biz/tasks', route => route.fulfill({ json: [] }));
    await returnButton(drawer).click();
    await page.getByText('记录已删除或无权访问，无法定位', { exact: true }).waitFor();
    await expect(drawer).toBeVisible();
    passed('deleted-record-stops-list-location');
    await page.goto(state.frontend + '/home/works/achievement?achId=999999999');
    await page.getByText('成果不存在、已删除或无权访问', { exact: true }).waitFor();
    passed('role-specific-sections-copy-success-and-missing-record');
    page = await create();
    await page.goto(state.frontend + '/login?redirect=' + encodeURIComponent('//example.invalid/external'));
    await page.getByPlaceholder('账号', { exact: true }).fill('110228');
    await page.getByPlaceholder('密码', { exact: true }).fill('Convenience123');
    await page.getByRole('button', { name: '登 录', exact: true }).click();
    await page.waitForURL('**/home/works');
    assert.equal(new URL(page.url()).origin, state.frontend);
    passed('external-login-redirect-rejected');
    let release, arrived;
    const gate = new Promise(resolve => { release = resolve; });
    const started = new Promise(resolve => { arrived = resolve; });
    await page.route('**/api/performance/task/950001?year=2026', async route => {
      const response = await route.fetch(); arrived(); await gate; await route.fulfill({ response });
    }, { times: 1 });
    await page.goto(state.frontend + '/home/works/performance?perfId=950001&year=2026');
    await Promise.race([started, new Promise((_, reject) => setTimeout(() => reject(new Error('Expected delayed task request')), 10000))]);
    await page.evaluate(() => {
      history.pushState({}, '', '/home/works/performance?perfId=950011&year=2027');
      dispatchEvent(new PopStateEvent('popstate'));
    });
    drawer = page.getByRole('dialog', { name: '绩效指标详情', exact: true }); await ready(drawer);
    await drawer.getByText('审计手动绩效', { exact: true }).waitFor();
    release(); await page.waitForTimeout(300);
    await drawer.getByText('审计手动绩效', { exact: true }).waitFor();
    assert.equal(await drawer.getByText('仅用户A可见的三级任务', { exact: true }).count(), 0);
    passed('same-page-detail-link-change-ignores-old-related-tasks');
    assert.deepEqual(errors, []);
    await fs.writeFile(path.join(output, 'results.json'), JSON.stringify({ checks, errors }, null, 2));
    console.log(JSON.stringify({ passed: checks.length, errors }));
  } catch (e) {
    if (page) await page.screenshot({ path: path.join(output, 'failure.png'), fullPage: true }).catch(() => {});
    throw e;
  } finally { await browser.close(); await api.dispose(); }
}
main().catch(e => { console.error(e); process.exitCode = 1; });
