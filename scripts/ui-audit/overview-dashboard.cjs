// Run only against serve.py --fixture overview (random synthetic schema).
const { chromium, request } = require('../../target/ui-audit-tools/node_modules/playwright');
const { expect } = require('../../target/ui-audit-tools/node_modules/playwright/test');
const fs = require('node:fs/promises'), path = require('node:path'), assert = require('node:assert/strict');
(async () => {
  const state = JSON.parse(await fs.readFile(path.resolve(__dirname, '../../target/ui-audit/state.json')));
  assert.match(state.schema, /^biz_review_test_[0-9a-f]{32}$/);
  const output = path.resolve(__dirname, '../../target/ui-audit/overview-dashboard'); await fs.mkdir(output, { recursive: true });
  const api = await request.newContext({ baseURL: state.frontend }), browser = await chromium.launch({ channel: 'msedge', headless: true });
  const checks = [], errors = [], contexts = [], tokens = new Map(); let page;
  const done = name => { checks.push(name); console.log('PASS ' + name); };
  const token = async id => {
    if (!tokens.has(id)) { const r = await api.post('/api/system/login', { data: { user_id: id, password: 'review-fixture-password' } }); const data = await r.json(); assert.ok(data.token, JSON.stringify(data)); tokens.set(id, data.token); }
    return tokens.get(id);
  };
  const tab = async id => {
    const value = await token(id), context = await browser.newContext({ viewport: { width: 1600, height: 1000 } }); contexts.push(context);
    await context.addInitScript(v => localStorage.setItem('token', v), value);
    const result = await context.newPage(); result.setDefaultTimeout(20000); result.on('pageerror', e => errors.push(e.message)); return result;
  };
  const get = async (url, id = 110228) => { const r = await api.get('/api' + url, { headers: { Authorization: await token(id) } }); assert.equal(r.status(), 200, await r.text()); return r.json(); };
  async function pick(name, label) {
    const combo = page.getByRole('combobox', { name, exact: true }); await expect(combo).toBeEnabled();
    await combo.locator('xpath=ancestor::div[contains(concat(" ",normalize-space(@class)," ")," el-select ")][1]').click();
    await page.getByRole('option', { name: label, exact: true }).click();
  }
  const home = async (month = 4) => { await page.goto(state.frontend + '/home/admin/overview'); await expect(page.locator('[data-metric="tasks.total"]')).toBeVisible(); await pick('看板月份', month + '月'); await expect(page.locator('[data-metric="tasks.total"]')).toBeVisible(); };
  const drawer = () => page.locator('.overview-drawer');
  const open = async metric => { await page.locator(`[data-metric="${metric}"]`).click(); await expect(drawer().locator('.analysis-table')).toBeVisible(); };
  const close = async () => { await drawer().getByRole('button', { name: '关闭分析', exact: true }).click(); await expect(drawer()).toBeHidden(); };
  try {
    const initialLeader = await get('/admin/users/111651/leadership');
    if (initialLeader.scope !== 'ALL') assert.equal((await api.put('/api/admin/users/111651/leadership', { headers: { Authorization: await token(110228) }, data: { scope: 'ALL', departmentIds: [], version: initialLeader.version, reason: '恢复隔离测试全校查阅前置条件' } })).status(), 200);
    page = await tab(110228); await home();
    const summary = await get('/admin/overview/summary?year=2026&month=4');
    assert.equal(Object.keys(summary.modules).length, 6);
    for (const [module, section] of Object.entries(summary.modules)) for (const metric of section.metrics) {
      const number = metric.value == null ? '—' : new Intl.NumberFormat('zh-CN', { maximumFractionDigits: 2 }).format(metric.value);
      await expect(page.locator(`[data-metric="${module}.${metric.key}"] strong`)).toContainText(number);
    }
    await page.screenshot({ path: path.join(output, 'dashboard-desktop.png'), fullPage: true });
    done('six-modules-and-all-summary-numbers-match-real-API');

    await open('tasks.total'); const a = await get('/admin/overview/analysis?year=2026&month=4&metric=tasks.total');
    assert.equal(a.total, 12); assert.equal(a.rows.length, 10); assert.equal(a.chart.reduce((sum, r) => sum + r.value, 0), a.total);
    await expect(drawer().locator('.analysis-table .el-table__body .el-table__row')).toHaveCount(10);
    await drawer().locator('.el-pagination').getByLabel('Go to next page').click();
    await expect(drawer().locator('.analysis-table .el-table__body .el-table__row')).toHaveCount(2);
    await expect(drawer().locator('canvas')).toBeVisible();
    // Click the middle of the first actual ECharts pie sector, not a simulated event.
    const canvas = drawer().locator('canvas').first(); await canvas.scrollIntoViewIfNeeded(); const box = await canvas.boundingBox();
    const angle = Math.PI * a.chart[0].value / a.total, radius = Math.min(box.width, box.height) * .26;
    const filteredRequest = page.waitForRequest(r => r.url().includes('/overview/analysis?') && new URL(r.url()).searchParams.has('groupKey'));
    // Locator click waits for the loading mask / drawer transition to stop intercepting input.
    await canvas.click({ position: { x: box.width / 2 + Math.sin(angle) * radius, y: box.height * .48 - Math.cos(angle) * radius } });
    await filteredRequest; await expect(drawer().locator('.analysis-controls .el-tag')).toBeVisible();
    await pick('分析维度', '部门对比'); await expect(drawer().locator('.analysis-controls .el-tag')).toHaveCount(0);
    await drawer().getByRole('button', { name: /审计测试部门B（1）/ }).click();
    await expect(drawer().locator('.analysis-table .el-table__body .el-table__row')).toHaveCount(1);
    await drawer().getByRole('button', { name: '详情', exact: true }).click(); await expect(page).toHaveURL(/taskId=930102/); await expect(page.getByRole('dialog').first()).toBeVisible();
    done('pagination-real-chart-click-group-intersection-and-existing-task-detail');

    await home(); await open('core.total'); await expect(drawer().locator('.analysis-table .el-table__body')).toContainText('归档时核心指标一');
    const core = await get('/admin/overview/analysis?year=2026&month=4&metric=core.total');
    assert.equal(core.rows.find(r => r.id === 1).value, 50); assert.equal(core.rows.find(r => r.id === 1).month, 2);
    assert.equal(core.rows.find(r => r.id === 10).value, null);
    await drawer().getByRole('button', { name: '历史', exact: true }).first().click();
    await expect(page).toHaveURL(/indicatorId=1/); await expect(page.getByRole('dialog', { name: '核心指标历史记录' })).toBeVisible();
    done('core-cutoff-excludes-future-and-obsolete-results-and-opens-history');

    await home(); await open('achievements.rejected');
    await expect(drawer()).toContainText('请补充上级发文依据（合成驳回原因）');
    await drawer().getByRole('button', { name: '详情', exact: true }).click(); await expect(page).toHaveURL(/achId=990101/);
    done('achievement-rejection-reason-and-existing-detail-link');

    await home(3); await expect(page.locator('[data-module="budget"]')).toContainText('尚未导入');
    await expect(page.locator('[data-metric="budget.expense"] strong')).toHaveText('—');
    await pick('看板月份', '5月'); await expect(page.locator('[data-metric="budget.expense"]')).toBeVisible(); await open('budget.expense');
    await pick('分析维度', '逐月趋势'); await expect(drawer()).toContainText('存在负增长');
    const budget = await get('/admin/overview/analysis?year=2026&month=5&metric=budget.expense&dimension=trend');
    assert.equal(budget.chart.length, 5); assert.equal(budget.chart[2].cumulative, null); assert.equal(budget.chart[3].incremental, null); assert.equal(budget.chart[4].adjustment, true);
    await pick('预算趋势视图', '当月新增支出'); await pick('预算趋势视图', '年度预算执行率');
    await drawer().locator('canvas').scrollIntoViewIfNeeded(); await page.screenshot({ path: path.join(output, 'budget-trend.png'), fullPage: true });
    await close(); await page.goto(state.frontend + '/home/works/budget'); await expect(page.locator('.budget-trend')).toHaveCount(0);
    done('budget-missing-null-gaps-adjustments-three-trend-views-original-chart-hidden');

    await home(); await open('records.missing'); await expect(drawer()).not.toContainText('SECRET-DRAFT');
    assert.equal((await get('/admin/overview/analysis?year=2026&month=4&metric=records.missing')).rows.some(r => r.recordId), false);
    await close(); await open('records.submitted'); await drawer().getByRole('button', { name: '详情', exact: true }).click();
    await expect(page).toHaveURL(/recordId=992001/); await expect(page.getByRole('heading', { name: /工作纪实/ }).first()).toBeVisible(); await expect(page.getByText('合成冻结快照')).toBeVisible();
    done('work-record-current-eligibility-draft-privacy-and-frozen-record-link');

    page = await tab(111651); await home(); await expect(page.locator('.dashboard-context')).toContainText('领导只读');
    await page.getByRole('button', { name: /运行维护/ }).click(); await expect(page.getByRole('button').filter({ hasText: '正常账号' })).toBeDisabled();
    assert.equal((await api.get('/api/admin/users', { headers: { Authorization: await token(111651) } })).status(), 403);
    const config = await get('/admin/users/111651/leadership');
    const save = await api.put('/api/admin/users/111651/leadership', { headers: { Authorization: await token(110228) }, data: { scope: 'DEPARTMENTS', departmentIds: [920001], version: config.version, reason: '隔离看板范围验证' } }); assert.equal(save.status(), 200, await save.text());
    await page.reload(); await expect(page.locator('.dashboard-context')).toContainText('分管部门');
    await expect(page.locator('[data-metric="tasks.total"] strong')).toContainText('11');
    assert.equal((await get('/admin/overview/analysis?year=2026&month=4&metric=tasks.total', 111651)).total, 11);
    page = await tab(910001); await page.goto(state.frontend + '/home/admin/overview'); await expect(page).not.toHaveURL(/admin\/overview/);
    assert.equal((await api.get('/api/admin/overview/summary?year=2026&month=4', { headers: { Authorization: await token(910001) } })).status(), 403);
    done('leader-readonly-maintenance-current-scope-old-token-and-ordinary-user-denial');

    page = await tab(110228); await home();
    await page.route('**/api/admin/overview/summary?**', async route => {
      if (new URL(route.request().url()).searchParams.get('year') === '2025') { const response = await route.fetch(); await new Promise(r => setTimeout(r, 800)); try { await route.fulfill({ response }); } catch {} } else await route.continue();
    });
    await pick('看板年度', '2025年'); await pick('看板年度', '2026年');
    await expect(page.locator('[data-metric="tasks.total"] strong')).toContainText('12'); await page.waitForTimeout(1000); await expect(page.locator('[data-metric="tasks.total"] strong')).toContainText('12');
    await page.unroute('**/api/admin/overview/summary?**');
    done('slow-old-year-response-cannot-replace-current-year');

    let failed = false;
    await page.route('**/api/admin/overview/summary?**', async route => { if (!failed) { failed = true; const response = await route.fetch(), body = await response.json(); body.modules.achievements = { status: 'error', message: '模拟成果统计故障' }; await route.fulfill({ json: body }); } else await route.continue(); });
    await page.getByRole('button', { name: '刷新', exact: true }).click();
    await expect(page.locator('[data-module="achievements"]')).toContainText('模拟成果统计故障');
    await expect(page.locator('[data-metric="achievements.total"]')).toHaveCount(0); await expect(page.locator('[data-metric="tasks.total"]')).toBeVisible();
    await page.locator('[data-module="achievements"]').getByRole('button', { name: '重试本区' }).click(); await expect(page.locator('[data-metric="achievements.total"]')).toBeVisible();
    await page.unroute('**/api/admin/overview/summary?**');
    done('injected-module-failure-is-not-zero-and-independent-retry-recovers');

    let reject = true;
    await page.route('**/api/admin/overview/analysis?**', async route => { if (reject) { reject = false; await route.fulfill({ status: 500, json: { code: 500, message: '模拟分析故障' } }); } else await route.continue(); });
    await page.locator('[data-metric="tasks.total"]').click(); await expect(drawer()).toContainText('模拟分析故障');
    await drawer().getByRole('button', { name: '重试分析' }).click(); await expect(drawer().locator('.analysis-table')).toBeVisible(); await close();
    await page.unroute('**/api/admin/overview/analysis?**');
    let release; const gate = new Promise(r => { release = r; });
    await page.route('**/api/admin/overview/analysis?**', async route => { const response = await route.fetch(); await gate; try { await route.fulfill({ response }); } catch {} });
    const pending = page.waitForRequest(r => r.url().includes('/overview/analysis?')); await page.locator('[data-metric="tasks.total"]').click(); await pending;
    await close(); release(); await page.unroute('**/api/admin/overview/analysis?**'); await open('performance.total'); await expect(drawer()).not.toContainText('任务推进'); await close();
    done('injected-analysis-failure-retry-and-close-cancels-old-response');

    await page.setViewportSize({ width: 960, height: 540 }); await page.reload();
    await expect(page.locator('[data-metric="tasks.total"]')).toBeVisible();
    await page.locator('[data-metric="records.expected"]').scrollIntoViewIfNeeded(); await expect(page.locator('[data-metric="records.expected"]')).toBeVisible();
    assert.equal(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth + 1), true);
    await open('tasks.total'); await drawer().getByRole('button', { name: '关闭分析', exact: true }).scrollIntoViewIfNeeded();
    await expect(drawer().getByRole('button', { name: '关闭分析', exact: true })).toBeInViewport();
    await page.screenshot({ path: path.join(output, 'dashboard-narrow-drawer.png'), fullPage: true }); await close();
    done('narrow-screen-page-table-scroll-and-accessible-drawer-footer');
    await pick('看板年度', '2029年');
    await expect(page.locator('[data-metric="tasks.total"] strong')).toHaveText('0项');
    await expect(page.locator('[data-metric="records.expected"] strong')).toHaveText('0人');
    await expect(page.locator('[data-metric="records.submissionRate"] strong')).toHaveText('—');
    await open('tasks.total'); await expect(drawer().getByText('此指标暂无可分析数据')).toBeVisible();
    await expect(drawer().locator('.analysis-table .el-table__empty-text')).toHaveText('暂无符合条件的记录'); await close();
    done('zero-records-and-zero-denominators-show-empty-data-without-false-percentage');
    assert.deepEqual(errors, []);
    await fs.writeFile(path.join(output, 'result.json'), JSON.stringify({ checks, errors, fixtures: 'random isolated synthetic database', simulated: ['delayed year response', 'module failure', 'analysis failure', 'closed analysis response'] }, null, 2));
    console.log(JSON.stringify({ checks: checks.length, errors }, null, 2));
  } finally {
    // 恢复合成领导的全校范围，便于继续运行既有界面回归。
    try { const current = await get('/admin/users/111651/leadership'); if (current.scope !== 'ALL') await api.put('/api/admin/users/111651/leadership', { headers: { Authorization: await token(110228) }, data: { scope: 'ALL', departmentIds: [], version: current.version, reason: '清理隔离范围测试' } }); }
    finally { for (const context of contexts) await context.close(); await browser.close(); await api.dispose(); }
  }
})().catch(error => { console.error(error); process.exitCode = 1; });
