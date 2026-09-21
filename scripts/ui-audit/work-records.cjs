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
  const output = path.join(root, 'evidence-work-records'); await fs.mkdir(output, { recursive: true });
  const api = await request.newContext({ baseURL: state.frontend });
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const checks = [], errors = [], tokens = new Map(); let page;
  const passed = name => { checks.push(name); console.log(name); };
  async function token(id) {
    if (!tokens.has(id)) {
      const response = await api.post('/api/system/login', { data: { user_id: id, password: 'WorkRecords123' } });
      const data = await response.json(); assert.ok(data.token); tokens.set(id, data.token);
    }
    return tokens.get(id);
  }
  async function call(id, method, route, data) {
    return api[method]('/api/work-records' + route, { headers: { Authorization: await token(id) }, data });
  }
  async function tab(id) {
    const context = await browser.newContext({ viewport: { width: 1440, height: 960 } });
    await context.addInitScript(value => localStorage.setItem('token', value), await token(id));
    const result = await context.newPage(); result.setDefaultTimeout(15000);
    result.on('pageerror', error => errors.push(error.message));
    await result.goto(state.frontend + '/home/work-records'); return result;
  }
  const button = (name, target = page) => target.getByRole('button', { name, exact: true });
  const select = (name, target = page) => target.locator('.el-select').filter({ has: page.getByRole('combobox', { name, exact: true }) });
  async function openDraft(target = page) {
    await target.getByRole('row').filter({ hasText: '2026年1月' }).getByRole('button', { name: '打开草稿', exact: true }).click();
    await expect(target.getByRole('heading', { name: '2026年1月工作纪实' })).toBeVisible();
  }
  try {
    page = await tab(910003);
    await expect(page.getByRole('menuitem', { name: '工作纪实', exact: true })).toBeVisible();
    await button('新建纪实').click();
    const create = page.getByRole('dialog', { name: '新建工作纪实' });
    await select('新建纪实月份', create).click();
    await expect(page.getByRole('option', { name: '12月', exact: true })).toHaveAttribute('aria-disabled', 'true');
    await page.getByRole('option', { name: '1月', exact: true }).click();
    await button('确定', create).click();
    await expect(button('保存草稿')).toBeVisible();
    await button('保存草稿').click(); await expect(page.getByText('草稿已保存', { exact: true })).toBeVisible();
    const records = await (await call(910003, 'get', '?year=2026&month=1')).json();
    const id = records.records[0].recordId;
    passed('real-api-create-empty-draft-and-future-month-disabled');
    await button('提交纪实').click(); await expect(page.getByText('请至少选择一项改革任务并填写有效内容')).toBeVisible();
    await select('选择改革任务').click();
    await page.getByRole('option', { name: '审计专用A一级任务' }).click(); await page.keyboard.press('Escape');
    await button('添加任务').click();
    const progress = page.getByRole('textbox', { name: '关键进展', exact: true });
    await progress.fill('😀'.repeat(301)); await button('保存草稿').click();
    await expect(page.getByText('每项填报内容不得超过300字')).toBeVisible();
    const before = await (await call(910003, 'get', '/' + id)).json(); assert.equal(before.record.version, 1);
    await progress.fill('😀'.repeat(300)); await button('保存草稿').click();
    await expect(page.locator('.detail-actions')).toContainText('内容已保存');
    await button('返回列表').first().click(); await openDraft(); await expect(progress).toHaveValue('😀'.repeat(300));
    passed('required-content-and-unicode-300-boundary-save-reopen');

    await progress.fill('未保存的进展'); await button('返回列表').first().click();
    let warning = page.getByRole('dialog', { name: '未保存提醒' });
    await button('继续填写', warning).click(); await expect(progress).toHaveValue('未保存的进展');
    await page.getByRole('menuitem', { name: '消息中心', exact: true }).click();
    await button('继续填写', warning).click(); await expect(page).toHaveURL(/work-records$/);
    await button('刷新统计').click(); await expect(page.getByText('统计已刷新，当前填写内容保留')).toBeVisible();
    await expect(progress).toHaveValue('未保存的进展'); await expect(page.locator('.detail-actions')).toContainText('有未保存');
    passed('unsaved-list-and-route-leave-cancel-statistics-preserves-text');

    let requests = 0;
    await page.route('**/work-records/*/save', async route => { requests++; await new Promise(resolve => setTimeout(resolve, 500)); await route.continue(); });
    await button('保存草稿').evaluate(element => { element.click(); element.click(); element.click(); });
    await expect(page.locator('.detail-actions')).toContainText('内容已保存'); assert.equal(requests, 1);
    await page.unroute('**/work-records/*/save'); passed('delayed-real-save-repeated-click-sends-once');

    const second = await tab(910003); await openDraft(second);
    await progress.fill('第一个窗口已保存'); await button('保存草稿').click(); await expect(page.locator('.detail-actions')).toContainText('内容已保存');
    await second.getByRole('textbox', { name: '关键进展', exact: true }).fill('第二窗口保留内容');
    await button('保存草稿', second).click();
    await expect(second.getByRole('alert').filter({ hasText: '其它窗口更新' })).toBeVisible();
    await expect(second.getByRole('textbox', { name: '关键进展', exact: true })).toHaveValue('第二窗口保留内容');
    await button('重新加载纪实', second).click(); await button('离开', second.getByRole('dialog', { name: '未保存提醒' })).click();
    await expect(second.getByRole('textbox', { name: '关键进展', exact: true })).toHaveValue('第一个窗口已保存');
    await second.context().close(); passed('real-two-window-conflict-retains-input-and-explicit-reload');

    await page.route('**/work-records/*/save', route => route.fulfill({ status: 500, contentType: 'application/json', body: JSON.stringify({ code: 500, message: '模拟保存故障' }) }));
    await progress.fill('故障后保留文本'); await button('保存草稿').click();
    await expect(page.getByRole('alert').filter({ hasText: '模拟保存故障' })).toBeVisible(); await expect(progress).toHaveValue('故障后保留文本');
    await page.unroute('**/work-records/*/save');
    passed('simulated-server-failure-retains-form');
    await button('保存草稿').click(); await expect(page.locator('.detail-actions')).toContainText('内容已保存');

    for (const width of [1920, 1440, 1024, 768]) {
      await page.setViewportSize({ width, height: 1000 });
      await page.locator('.work-records').evaluate(element => element.scrollIntoView({ block: 'start' }));
      assert.equal(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth), true, 'horizontal overflow at ' + width);
      await page.screenshot({ path: path.join(output, 'detail-' + width + '.png'), fullPage: true });
    }
    passed('responsive-layout-1920-1440-1024-768');
    await page.setViewportSize({ width: 1440, height: 960 });
    await button('提交纪实').click();
    await button('确认提交', page.getByRole('dialog', { name: '提交工作纪实' })).click();
    await expect(page.getByRole('alert').filter({ hasText: '已提交纪实不可修改' })).toBeVisible();
    await expect(button('保存草稿')).toHaveCount(0); await expect(page.locator('.readonly-text').first()).toHaveText('故障后保留文本');
    let submitted = await (await call(910003, 'get', '/' + id)).json(); assert.equal(submitted.record.status, 1);
    assert.equal((await call(910003, 'post', '/' + id + '/save', { version: submitted.record.version, entries: [] })).status(), 409);
    await page.screenshot({ path: path.join(output, 'submitted.png'), fullPage: true });
    passed('real-submit-readonly-and-direct-api-write-rejected');

    for (const user of [110228, 910004]) {
      const viewer = await tab(user); await expect(viewer.getByRole('menuitem', { name: '工作纪实', exact: true })).toBeVisible();
      await expect(button('新建纪实', viewer)).toHaveCount(user === 110228 ? 1 : 0);
      await viewer.getByRole('row').filter({ hasText: '2026年1月' }).getByRole('button', { name: '查看', exact: true }).click();
      await expect(viewer.locator('.readonly-text').first()).toHaveText('故障后保留文本'); await viewer.context().close();
    }
    const reporterPage = page;
    page = await tab(910005);
    await expect(page.getByRole('menuitem', { name: '工作纪实', exact: true })).toBeVisible();
    assert.equal((await (await call(910005, 'get', '/statistics?year=2026&month=3')).json()).totalTasks, 2);
    assert.equal((await (await call(910003, 'get', '/statistics?year=2026&month=3')).json()).totalTasks, 1);
    await button('新建纪实').click();
    const officeCreate = page.getByRole('dialog', { name: '新建工作纪实' });
    await select('新建纪实月份', officeCreate).click();
    await page.getByRole('option', { name: '3月', exact: true }).click();
    await button('确定', officeCreate).click();
    await expect(button('保存草稿')).toBeVisible();
    await select('选择改革任务').click();
    await page.getByRole('option', { name: '审计专用A一级任务' }).click(); await page.keyboard.press('Escape');
    await button('添加任务').click();
    await page.getByRole('textbox', { name: '关键进展', exact: true }).fill('双高办全校口径填报');
    await button('提交纪实').click();
    await button('确认提交', page.getByRole('dialog', { name: '提交工作纪实' })).click();
    await expect(page.locator('.readonly-text').first()).toHaveText('双高办全校口径填报');
    const officeRecords = await (await call(910005, 'get', '?year=2026&month=3')).json();
    const officeId = officeRecords.records[0].recordId;
    const officeDetail = await (await call(910005, 'get', '/' + officeId)).json();
    assert.equal(officeDetail.statistics.totalTasks, 2);
    assert.equal((await call(910005, 'post', '/' + officeId + '/delete', { version: officeDetail.record.version, reason: '越权删除' })).status(), 403);
    await page.screenshot({ path: path.join(output, 'office-school-wide.png'), fullPage: true });
    await page.context().close(); page = reporterPage;
    passed('real-office-fills-school-wide-scope-and-cannot-delete');

    const outsider = await tab(910001);
    await expect(outsider.getByText('您目前没有工作纪实填报或查阅权限')).toBeVisible();
    await expect(outsider.getByRole('menuitem', { name: '工作纪实', exact: true })).toHaveCount(0);
    assert.equal((await call(910001, 'get', '/' + id)).status(), 404); await outsider.context().close();
    passed('real-admin-configured-viewer-and-unauthorized-role-boundaries');

    await button('返回列表').first().click();
    // Artificially delayed obsolete response must never overwrite the new month filter.
    let started; const startedPromise = new Promise(resolve => { started = resolve; });
    await page.route('**/work-records?*', async route => {
      if (new URL(route.request().url()).searchParams.get('month') === '1') {
        const response = await route.fetch(); started(); await new Promise(resolve => setTimeout(resolve, 700));
        try { await route.fulfill({ response }); } catch { /* expected after cancellation */ }
      } else await route.continue();
    });
    await select('纪实月份').click(); await page.getByRole('option', { name: '1月', exact: true }).click();
    await startedPromise;
    await select('纪实月份').click(); await page.getByRole('option', { name: '2月', exact: true }).click();
    await expect(page.getByText('暂无工作纪实')).toBeVisible();
    await page.waitForTimeout(850); await expect(page.getByRole('row').filter({ hasText: '2026年1月' })).toHaveCount(0);
    await page.unroute('**/work-records?*'); passed('simulated-old-list-response-cannot-overwrite-new-filter');

    await button('新建纪实').click(); await select('新建纪实月份', create).click();
    await page.getByRole('option', { name: '1月', exact: true }).click(); await button('确定', create).click();
    await expect(page.getByRole('alert').filter({ hasText: '已提交纪实不可修改' })).toBeVisible();
    assert.equal((await (await call(910003, 'get', '?year=2026&month=1')).json()).total, 1);
    passed('existing-month-opens-frozen-record-without-duplicate');
    await button('返回列表').first().click();
    await button('新建纪实').click(); await select('新建纪实月份', create).click();
    await page.getByRole('option', { name: '2月', exact: true }).click(); await button('确定', create).click();
    await select('选择改革任务').click(); await page.getByRole('option', { name: '审计专用A一级任务' }).click(); await page.keyboard.press('Escape');
    await button('添加任务').click();
    await progress.fill('保留待移除文本');
    await button('移除任务').click();
    await button('取消', page.getByRole('dialog', { name: '移除填报项目' })).click();
    await expect(progress).toHaveValue('保留待移除文本');
    await button('返回列表').first().click(); await button('离开', warning).click();
    await page.getByRole('row').filter({ hasText: '2026年2月' }).getByRole('button', { name: '打开草稿' }).click();
    await expect(page.getByText('请选择需要填报的改革任务')).toBeVisible();
    passed('remove-entry-cancel-and-confirmed-leave-discards-unsaved-text');
    await button('返回列表').first().click();
    const secondId = (await (await call(910003, 'get', '?year=2026&month=2')).json()).records[0].recordId;
    let detailStarted; const detailStartedPromise = new Promise(resolve => { detailStarted = resolve; });
    await page.route('**/work-records/' + secondId, async route => {
      const response = await route.fetch(); detailStarted(); await new Promise(resolve => setTimeout(resolve, 600));
      try { await route.fulfill({ response }); } catch { /* component already left */ }
    });
    await page.getByRole('row').filter({ hasText: '2026年2月' }).getByRole('button', { name: '打开草稿' }).click();
    await detailStartedPromise; await page.getByRole('menuitem', { name: '消息中心', exact: true }).click();
    await expect(page).toHaveURL(/home\/notice$/); await page.waitForTimeout(750);
    await expect(page.locator('.work-records')).toHaveCount(0); await page.unroute('**/work-records/' + secondId);
    passed('simulated-delayed-detail-after-route-leave-is-discarded');
    assert.deepEqual(errors, []); passed('no-browser-runtime-errors');
  } catch (error) {
    if (page && !page.isClosed()) await page.screenshot({ path: path.join(output, 'failure.png'), fullPage: true });
    throw error;
  } finally {
    await fs.writeFile(path.join(output, 'results.json'), JSON.stringify({ checks, errors }, null, 2));
    await browser.close(); await api.dispose();
  }
}
main().catch(error => { console.error(error); process.exitCode = 1; });
