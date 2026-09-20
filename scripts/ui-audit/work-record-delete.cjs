const { chromium, request } = require('../../target/ui-audit-tools/node_modules/playwright');
const { expect } = require('../../target/ui-audit-tools/node_modules/playwright/test');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');
async function main() {
  const root = path.resolve(__dirname, '../../target/ui-audit');
  const state = JSON.parse(await fs.readFile(path.join(root, 'state.json')));
  assert.match(state.schema, /^biz_review_test_[0-9a-f]{32}$/);
  const out = path.join(root, 'evidence-work-record-delete'); await fs.mkdir(out, { recursive: true });
  const api = await request.newContext({ baseURL: state.frontend });
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const checks = [], errors = []; let page;
  const pass = name => { checks.push(name); console.log(name); };
  async function token(id) { return (await (await api.post('/api/system/login', { data: { user_id: id, password: 'WorkRecords123' } })).json()).token; }
  const owner = await token(910003), admin = await token(110228);
  async function create(auth, month) {
    const response = await api.post('/api/work-records', { headers: { Authorization: auth }, data: { year: 2026, month } });
    assert.equal(response.status(), 200, await response.text()); return (await response.json()).record;
  }
  async function submit(auth, record) {
    const response = await api.post(`/api/work-records/${record.recordId}/submit`, { headers: { Authorization: auth }, data: { version: record.version, entries: [{ reformTaskId: 930000, keyProgress: '删除功能合成测试' }] } });
    assert.equal(response.status(), 200, await response.text());
  }
  async function tab(auth) {
    const context = await browser.newContext({ viewport: { width: 1440, height: 960 } });
    await context.addInitScript(value => localStorage.setItem('token', value), auth);
    const p = await context.newPage(); p.setDefaultTimeout(15000); p.on('pageerror', e => errors.push(e.message));
    await p.goto(state.frontend + '/home/work-records'); return p;
  }
  try {
    const original = await create(owner, 1); await submit(owner, original);
    const reporter = await tab(owner); await expect(reporter.getByRole('button', { name: '查看', exact: true })).toBeVisible();
    await expect(reporter.getByRole('button', { name: '删除', exact: true })).toHaveCount(0);
    const viewer = await tab(await token(910004)); await expect(viewer.getByRole('button', { name: '导出', exact: true })).toBeVisible();
    await expect(viewer.getByRole('button', { name: '删除', exact: true })).toHaveCount(0); pass('non-admin-owner-and-viewer-have-no-delete-action');
    page = await tab(admin); await page.getByRole('button', { name: '删除', exact: true }).click();
    const dialog = page.getByRole('dialog', { name: '删除工作纪实' });
    await dialog.getByRole('button', { name: '确认删除', exact: true }).click();
    await expect(dialog.getByText('请填写1～300字的删除原因')).toBeVisible();
    await dialog.getByRole('button', { name: '取消', exact: true }).click();
    assert.equal((await api.get(`/api/work-records/${original.recordId}`, { headers: { Authorization: admin } })).status(), 200); pass('required-reason-and-cancel-preserve-record');
    await page.getByRole('button', { name: '删除', exact: true }).click();
    await dialog.getByRole('textbox', { name: '删除原因' }).fill('重复填报，需要重新整理');
    await page.route('**/work-records/*/delete', route => route.fulfill({ status: 500, contentType: 'application/json', body: JSON.stringify({ message: '模拟删除失败' }) }));
    await dialog.getByRole('button', { name: '确认删除', exact: true }).click();
    await expect(dialog.locator('.el-alert--error')).toBeVisible();
    await expect(dialog.getByRole('textbox', { name: '删除原因' })).toHaveValue('重复填报，需要重新整理');
    await page.unroute('**/work-records/*/delete'); pass('simulated-failure-keeps-reason-and-dialog');
    await page.setViewportSize({ width: 390, height: 844 });
    const box = await dialog.locator('.el-dialog').boundingBox(); assert.ok(box.x >= 0 && box.x + box.width <= 391);
    await page.screenshot({ path: path.join(out, 'mobile-delete.png'), fullPage: true });
    await page.setViewportSize({ width: 1440, height: 960 }); pass('mobile-confirmation-fits-screen');
    let calls = 0;
    await page.route('**/work-records/*/delete', async route => { calls++; await new Promise(resolve => setTimeout(resolve, 700)); await route.continue(); });
    await dialog.getByRole('button', { name: '确认删除', exact: true }).evaluate(button => { button.click(); button.click(); });
    await expect(dialog.getByRole('button', { name: '确认删除', exact: true })).toBeDisabled();
    await expect(dialog).toBeHidden(); await expect(page.getByText('暂无工作纪实', { exact: true })).toBeVisible();
    assert.equal(calls, 1); await page.unroute('**/work-records/*/delete');
    assert.equal((await api.get(`/api/work-records/${original.recordId}`, { headers: { Authorization: owner } })).status(), 404);
    assert.equal((await api.post('/api/work-records/export', { headers: { Authorization: admin }, data: { ids: [original.recordId] } })).status(), 404);
    pass('real-list-delete-blocks-double-click-old-detail-and-export');
    const replacement = await create(owner, 1); assert.notEqual(replacement.recordId, original.recordId); await submit(owner, replacement);
    await page.getByRole('button', { name: '刷新', exact: true }).click();
    await page.getByRole('button', { name: '查看', exact: true }).click();
    await page.getByRole('button', { name: '删除纪实', exact: true }).click();
    await dialog.getByRole('textbox', { name: '删除原因' }).fill('详情页删除测试');
    await dialog.getByRole('button', { name: '确认删除', exact: true }).click();
    await expect(page.getByText('暂无工作纪实', { exact: true })).toBeVisible(); pass('real-same-month-resubmit-and-detail-delete');
    const second = await token(910002);
    for (const [auth, months] of [[owner, 6], [second, 5]]) for (let month = 1; month <= months; month++) await submit(auth, await create(auth, month));
    await page.getByRole('button', { name: '刷新', exact: true }).click(); await expect(page.getByText('Total 11', { exact: true })).toBeVisible();
    await page.locator('.el-pagination').getByText('2', { exact: true }).click();
    await expect(page.locator('.el-table__body-wrapper tbody tr')).toHaveCount(1);
    await page.getByRole('button', { name: '删除', exact: true }).click();
    await dialog.getByRole('textbox', { name: '删除原因' }).fill('末页删除测试'); await dialog.getByRole('button', { name: '确认删除', exact: true }).click();
    await expect(page.getByText('Total 10', { exact: true })).toBeVisible(); await expect(page.locator('.el-table__body-wrapper tbody tr')).toHaveCount(10);
    pass('real-last-page-delete-corrects-pagination');
    await page.screenshot({ path: path.join(out, 'delete-list.png'), fullPage: true }); assert.deepEqual(errors, []);
  } catch (error) { if (page) await page.screenshot({ path: path.join(out, 'failure.png'), fullPage: true }); throw error; }
  finally { await fs.writeFile(path.join(out, 'results.json'), JSON.stringify({ checks, errors }, null, 2)); await browser.close(); await api.dispose(); }
}
main().catch(error => { console.error(error); process.exitCode = 1; });
