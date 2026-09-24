// 归属移交性能测量：只在 --fixture ownership-transfer-scale 的隔离环境运行，输出进入/退出批量移交与整树勾选的耗时。
const { chromium, request } = require('../../target/ui-audit-tools/node_modules/playwright');
const { expect } = require('../../target/ui-audit-tools/node_modules/playwright/test');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');

(async () => {
  const state = JSON.parse(await fs.readFile(path.resolve(__dirname, '../../target/ui-audit/state.json')));
  assert.match(state.schema, /^biz_review_test_[0-9a-f]{32}$/);
  const output = path.resolve(__dirname, '../../target/ui-audit/transfer-scale');
  await fs.mkdir(output, { recursive: true });
  const api = await request.newContext({ baseURL: state.frontend });
  const login = await (await api.post('/api/system/login',
    { data: { user_id: 110228, password: 'review-fixture-password' } })).json();
  assert.ok(login.token, 'admin login failed');
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const context = await browser.newContext({ viewport: { width: 1920, height: 1080 } });
  await context.addInitScript(token => localStorage.setItem('token', token), login.token);
  const page = await context.newPage();
  const errors = [];
  page.on('pageerror', e => errors.push(e.stack || e.message));
  // 进入表格后展开全部层级，还原真实使用中“整棵树都在页面上”的状态
  const expand = async () => {
    await page.locator('.el-table__body tr').first().waitFor({ state: 'attached' });
    // v-loading 遮罩淡出期间会挡住点击
    await expect(page.locator('.table-wrapper .el-loading-mask')).toBeHidden();
    for (let i = 0; i < 6; i++) {
      const closed = page.locator('.el-table__expand-icon:not(.el-table__expand-icon--expanded):visible');
      if (!await closed.count()) break;
      await closed.first().dispatchEvent('click');
      await settle();
    }
  };
  const settle = () => page.evaluate(() => new Promise(resolve =>
    requestAnimationFrame(() => requestAnimationFrame(() => resolve(0)))));
  const timed = async (action, selector, timeout = 30000, state = 'visible') => {
    const start = Date.now();
    await action();
    await page.waitForSelector(selector, { state, timeout });
    await settle();
    return Date.now() - start;
  };
  const toolbar = () => page.locator('.project-actions').innerText();
  const attempt = async (label, action, selector, timeout, state) => {
    try {
      return await timed(action, selector, timeout, state);
    } catch (error) {
      console.error(`${label} failed: ${error.message.split('\n')[0]}`);
      await page.screenshot({ path: path.join(output, `${label}-failed.png`) });
      return null;
    }
  };
  try {
    const started = Date.now();
    await page.goto(state.frontend + '/home/works');
    await expect(page.locator('.table-wrapper .el-table__header')).toBeVisible();
    await expand();
    const rows = await page.locator('.el-table__body tr').count();
    const load = Date.now() - started;
    await page.screenshot({ path: path.join(output, 'idle.png') });

    const enter = await attempt('enter', () => page.getByRole('button', { name: '批量移交', exact: true }).click(),
      '.table-wrapper .el-table__header .el-checkbox');
    const enterText = await toolbar();
    await page.screenshot({ path: path.join(output, 'selecting.png') });
    // 单行勾选：不改变列结构，只触发父组件重渲染，用于区分“加列开销”和“整表重渲染开销”
    const leafSelect = await attempt('leaf-select',
      () => page.locator('.el-table__body tr').nth(2).locator('.el-checkbox').click(),
      'button:has-text("确认移交（")', 20000);
    const leafText = await toolbar();
    await page.locator('.el-table__body tr').nth(2).locator('.el-checkbox').click();
    const parentSelect = await attempt('parent-select',
      () => page.locator('.el-table__body tr').first().locator('.el-checkbox').click(),
      'button:has-text("确认移交（")', 20000);
    const parentText = await toolbar();
    const checkedRows = await page.locator('.el-table__body .el-checkbox.is-checked').count();
    const exit = await attempt('exit', () => page.getByRole('button', { name: '退出批量移交' }).click(),
      '.table-wrapper .el-table__header .el-checkbox', 20000, 'hidden');

    const result = { rows, loadMs: load, enterMs: enter, enterText, leafSelectMs: leafSelect, leafText,
      parentSelectMs: parentSelect, parentText, checkedRows, exitMs: exit, errors };
    await fs.writeFile(path.join(output, 'results.json'), JSON.stringify(result, null, 2));
    console.log(JSON.stringify(result, null, 2));
  } finally {
    await browser.close();
    await api.dispose();
  }
})().catch(error => { console.error(error); process.exitCode = 1; });
