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
  const output = path.join(root, 'evidence-convenience-batch3'); await fs.mkdir(output, { recursive: true });
  const api = await request.newContext({ baseURL: state.frontend });
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const checks = [], errors = [];
  let page;
  const passed = name => { checks.push(name); console.log(name); };
  const tokens = new Map();
  const login = async id => {
    if (tokens.has(id)) return tokens.get(id);
    await api.post('/api/system/password/reset', { data: { user_id: id, old_password: 'review-fixture-password', new_password: 'Convenience123' } });
    const data = await (await api.post('/api/system/login', { data: { user_id: id, password: 'Convenience123' } })).json();
    assert.ok(data.token); tokens.set(id, data.token); return data.token;
  };
  const create = async id => {
    const context = await browser.newContext({ viewport: { width: 1440, height: 1000 } });
    await context.addInitScript(token => localStorage.setItem('token', token), await login(id));
    const tab = await context.newPage(); tab.setDefaultTimeout(15000);
    tab.on('pageerror', e => errors.push(e.message)); return tab;
  };
  const dialog = () => page.getByRole('dialog', { name: '业务审批', exact: true });
  const next = () => dialog().getByRole('button', { name: '提交并审核下一条', exact: true });
  const id = async () => Number(await dialog().locator('.el-descriptions__content').first().innerText());
  const openFirst = async () => page.locator('.audit-container .el-table__body').getByRole('button', { name: '审批', exact: true }).first().click();
  const badge = async count => {
    const b = page.getByRole('menuitem', { name: /审核中心/ }).locator('.el-badge');
    if (count) await expect(b).toHaveText(String(count)); else await expect(b).toBeHidden();
  };
  const get = async (url, who = 910003) => (await api.get('/api' + url, { headers: { Authorization: await login(who) } })).json();
  try {
    page = await create(110228); await page.goto(state.frontend + '/home/audit'); await badge(4); await openFirst();
    const seen = [];
    for (let i = 0; i < 4; i++) {
      await expect(next()).toBeEnabled(); seen.push(await dialog().locator('.el-descriptions__content').allTextContents());
      if (i % 2) await dialog().getByText('驳回', { exact: true }).click();
      await next().click(); await badge(3 - i);
    }
    await expect(dialog()).toBeHidden();
    assert.equal(new Set(seen.map(cells => cells[0] + ':' + cells[1])).size, 4);
    assert.equal((await get('/biz/audit/todo', 110228)).length, 0);
    assert.equal((await get('/achievement/audit/todo', 110228)).length, 0);
    for (let i = 0; i < seen.length; i++) {
      const prefix = seen[i][1].includes('成果') ? 'achievement' : 'biz';
      // 任务退回会软删除审核单，现有日志接口不再提供该审核单；退回通过真实写接口及待办移除核验。
      if (prefix === 'biz' && i % 2) continue;
      const logs = await get(`/${prefix}/audit/logs/${seen[i][0].trim()}`, 110228);
      assert.ok(logs.some(log => Number(log.postStatus ?? log.post_status) === (i % 2 ? -10 : prefix === 'biz' ? 40 : 30)));
    }
    passed('real-task-and-achievement-pass-reject-with-overlapping-submission-ids');
    await page.context().close();

    page = await create(910003); await page.goto(state.frontend + '/home/audit'); await badge(15);
    const initial = (await get('/performance/audit/todo')).map(row => row.subId);
    const buttons = page.locator('.audit-container .el-table__body').getByRole('button', { name: '审批', exact: true });
    await expect(buttons).toHaveCount(10); await buttons.nth(9).click();
    for (let i = 9; i < 15; i++) {
      await expect.poll(id).toBe(initial[i]); await expect(next()).toBeEnabled();
      if (i % 2) await dialog().getByText('驳回', { exact: true }).click();
      await dialog().getByPlaceholder('请输入审批意见（必填）...').fill(`真实连续审批 ${i}`);
      await next().click(); await badge(15 - (i - 8));
    }
    await expect(dialog()).toBeHidden(); await expect(page.getByText('后续待办已处理完', { exact: true })).toBeVisible();
    assert.equal((await get('/performance/audit/todo')).length, 9);
    passed('real-performance-cross-original-page-boundary-and-no-wrap');
    await openFirst();
    for (let i = 0; i < 9; i++) {
      await expect.poll(id).toBe(initial[i]); await expect(next()).toBeEnabled(); await next().click(); await badge(8 - i);
    }
    await expect(dialog()).toBeHidden(); await expect(buttons).toHaveCount(0);
    for (let i = 0; i < initial.length; i++) {
      const logs = await get(`/performance/audit/logs/${initial[i]}`);
      assert.ok(logs.some(log => Number(log.postStatus ?? log.post_status) === (i >= 9 && i % 2 ? -10 : 20)));
    }
    await page.screenshot({ path: path.join(output, 'all-performance-completed.png'), animations: 'disabled' });
    passed('real-fifteen-performance-approvals-and-immediate-badge-zero');
    await page.context().close();

    const setup = async () => {
      page = await create(910003);
      const model = { rows: Array.from({ length: 4 }, (_, i) => ({ subId: 980001 + i, perfId: 950011, perfName: '连续样本' + i,
        flowStatus: 10, submitBy: 910001, year: 2026 })), posts: [], failPost: false, failRefresh: false, gate: null };
      await page.route('**/api/biz/audit/todo', route => route.fulfill({ json: [] }));
      await page.route('**/api/achievement/audit/todo', route => route.fulfill({ json: [] }));
      await page.route('**/api/performance/audit/todo', async route => {
        if (model.gate && model.posts.length) await model.gate;
        await route.fulfill(model.failRefresh ? { status: 500, json: { code: 500, message: '模拟刷新失败' } } : { json: model.rows });
      });
      await page.route('**/api/performance/audit', async route => {
        const data = route.request().postDataJSON(); model.posts.push(data);
        if (model.failPost) return route.fulfill({ status: 500, json: { code: 500, message: '模拟审批失败' } });
        model.rows = model.rows.filter(row => row.subId !== data.sub_id);
        await route.fulfill({ json: '审批成功' });
      });
      await page.goto(state.frontend + '/home/audit'); await badge(4); await openFirst(); return model;
    };
    let model = await setup(); model.failPost = true;
    await dialog().getByPlaceholder('请输入审批意见（必填）...').fill('失败保留意见'); await next().click();
    await expect(page.getByText('模拟审批失败', { exact: true })).toBeVisible();
    assert.equal(await id(), 980001); await expect(dialog().getByPlaceholder('请输入审批意见（必填）...')).toHaveValue('失败保留意见');
    await badge(4); model.failPost = false;
    model.rows = model.rows.filter(row => row.subId !== 980002); // 模拟下一条被其他审核人处理。
    await next().click(); await expect.poll(id).toBe(980003); await expect(next()).toBeEnabled();
    assert.equal(model.posts.length, 2); await expect(dialog().getByPlaceholder('请输入审批意见（必填）...')).toHaveValue('同意');
    passed('injected-submit-failure-preserves-draft-and-concurrent-completion-is-skipped'); await page.context().close();

    model = await setup(); model.failRefresh = true; await next().click();
    await expect(dialog()).toBeHidden(); await expect(page.getByText('审核已成功，列表刷新失败，请刷新列表后继续', { exact: true })).toBeVisible();
    assert.equal(model.posts.length, 1); model.failRefresh = false;
    await page.getByRole('button', { name: '刷新列表', exact: true }).click();
    await expect(page.getByText('审核已成功，列表刷新失败，请刷新列表后继续', { exact: true })).toHaveCount(0);
    await expect(page.locator('.audit-container .el-table__body').getByRole('button', { name: '审批', exact: true })).toHaveCount(3);
    assert.equal(model.posts.length, 1); passed('injected-post-success-refresh-failure-retry-without-resubmission'); await page.context().close();

    model = await setup(); let release; model.gate = new Promise(resolve => { release = resolve; });
    await next().evaluate(button => { button.click(); button.click(); });
    await expect.poll(() => model.posts.length).toBe(1); await expect(next()).toBeDisabled();
    await dialog().getByRole('button', { name: '关闭', exact: true }).click(); release();
    await expect(page.locator('.audit-container .el-loading-mask')).toBeHidden(); await expect(dialog()).toBeHidden();
    assert.equal(model.posts.length, 1); passed('injected-double-click-and-close-during-refresh-cancels-next'); await page.context().close();

    model = await setup();
    await dialog().getByRole('button', { name: '关闭', exact: true }).click();
    await page.getByPlaceholder('搜索任务名称、提交人...').fill('连续样本2');
    await page.locator('.filter-bar .el-select').click(); await page.getByRole('option', { name: '绩效填报', exact: true }).click();
    await openFirst(); assert.equal(await id(), 980003); await next().click();
    await expect(dialog()).toBeHidden(); await expect(page.getByText('后续待办已处理完', { exact: true })).toBeVisible();
    assert.equal(model.rows.length, 3); passed('search-and-type-intersection-does-not-open-unfiltered-records'); await page.context().close();

    model = await setup(); model.gate = new Promise(resolve => { release = resolve; });
    await next().click(); await expect.poll(() => model.posts.length).toBe(1);
    // 在异步刷新期间注入筛选变化，验证取消自动继续的版本检查。
    await page.getByPlaceholder('搜索任务名称、提交人...').evaluate(input => {
      input.value = '连续样本3'; input.dispatchEvent(new Event('input', { bubbles: true }));
    });
    release(); await expect(dialog()).toBeHidden();
    await expect(page.locator('.audit-container .el-table__body').getByRole('button', { name: '审批', exact: true })).toHaveCount(1);
    passed('injected-filter-change-during-refresh-cancels-next'); await page.context().close();

    model = await setup(); model.gate = new Promise(resolve => { release = resolve; });
    await next().click(); await expect.poll(() => model.posts.length).toBe(1);
    await page.getByRole('menuitem', { name: /消息中心/ }).evaluate(item => item.click());
    await expect(page).toHaveURL(/\/home\/notice$/); release();
    await expect(dialog()).toHaveCount(0);
    passed('route-change-during-refresh-does-not-reopen-audit'); await page.context().close();

    model = await setup();
    await next().hover();
    await page.screenshot({ path: path.join(output, 'continuous-approval-buttons.png'), animations: 'disabled' });
    await dialog().getByRole('button', { name: '确认提交', exact: true }).click();
    await expect(dialog()).toBeHidden(); await badge(3);
    await expect(page.locator('.audit-container .el-loading-mask')).toBeHidden();
    assert.equal(model.posts.length, 1); passed('original-confirm-submit-closes-without-opening-next'); await page.context().close();

    assert.deepEqual(errors, []);
    await fs.writeFile(path.join(output, 'results.json'), JSON.stringify({ checks, errors }, null, 2));
  } catch (error) {
    if (page && !page.isClosed()) { await page.screenshot({ path: path.join(output, 'failure.png') }); console.error((await page.locator('body').innerText()).slice(-4500)); }
    throw error;
  } finally { await browser.close(); await api.dispose(); }
}
main().catch(error => { console.error(error); process.exitCode = 1; });
