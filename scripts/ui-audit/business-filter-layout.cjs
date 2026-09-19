const { chromium, request } = require('../../target/ui-audit-tools/node_modules/playwright');
const { expect } = require('../../target/ui-audit-tools/node_modules/playwright/test');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');

(async () => {
  const state = JSON.parse(await fs.readFile(path.resolve(__dirname, '../../target/ui-audit/state.json')));
  assert.match(state.schema, /^biz_review_test_[0-9a-f]{32}$/);
  assert.equal(state.frontend, 'http://127.0.0.1:15273');
  const output = path.resolve(__dirname, '../../target/ui-audit/business-filter-layout');
  await fs.mkdir(output, { recursive: true });
  const api = await request.newContext({ baseURL: state.frontend });
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const checks = [], errors = [];
  try {
    await api.post('/api/system/password/reset', { data: { user_id: 110228, old_password: 'review-fixture-password', new_password: 'Convenience123' } });
    const auth = await (await api.post('/api/system/login', { data: { user_id: 110228, password: 'Convenience123' } })).json();
    assert.ok(auth.token);
    const context = await browser.newContext({ viewport: { width: 1920, height: 1000 } });
    await context.addInitScript(token => localStorage.setItem('token', token), auth.token);
    const page = await context.newPage(); page.setDefaultTimeout(15000);
    page.on('pageerror', error => errors.push(error.stack));
    const select = async (name, option) => {
      await page.getByRole('combobox', { name, exact: true }).locator('xpath=ancestor::*[contains(@class, "el-select__wrapper")]').click();
      await page.getByRole('listbox', { name, exact: true }).getByRole('option', { name: option, exact: true }).click();
    };
    const expand = async () => {
      for (let i = 0; i < 10; i++) {
        const closed = page.locator('.el-table__expand-icon:not(.el-table__expand-icon--expanded):visible');
        if (!await closed.count()) break;
        await closed.first().click();
      }
    };
    for (const [route, title, owner, pending, inactive] of [
      ['performance', '绩效', '归口部门', '审计手动绩效', '已归档历史绩效'],
      ['achievement', '成果', '报送部门', '连续审核成果A', '无审核证据的历史成果']
    ]) {
      await page.goto(state.frontend + '/home/works/' + route);
      const bar = page.locator('.business-filters');
      await expect(bar.getByRole('combobox', { name: title + '范围', exact: true })).toBeEnabled();
      await select(title + '年度', '2026年');
      await expect(bar.getByRole('combobox', { name: title + '范围', exact: true })).toBeEnabled();
      await select(title + owner, '审计测试部门A');
      await expand();
      await expect(page.getByRole('row').filter({ hasText: inactive })).toBeVisible();
      await select(title + '当前处理部门', '审计测试部门A');
      await expand();
      await expect(page.getByRole('row').filter({ hasText: pending })).toBeVisible();
      await expect(page.getByRole('row').filter({ hasText: inactive })).toHaveCount(0);
      await select(title + '范围', '我' + (title === '成果' ? '创建的' : '负责的'));
      await expect(page.getByRole('row').filter({ hasText: pending })).toHaveCount(0);
      await select(title + '范围', '全部');
      await page.reload(); await expand();
      await expect(page.getByRole('row').filter({ hasText: pending })).toBeVisible();
      await expect(bar.locator('.filter-item').filter({ has: page.getByText(owner, { exact: true }) })).toContainText('审计测试部门A');
      await expect(page.getByRole('row').filter({ hasText: inactive })).toHaveCount(0);
      await bar.getByRole('button', { name: '重置', exact: true }).click();
      await expect(bar.getByRole('combobox', { name: title + '范围', exact: true })).toBeEnabled();
      await select(title + '年度', '2026年'); await expand();
      await expect(page.getByRole('row').filter({ hasText: inactive })).toBeVisible();
      checks.push(title + ': department intersection, pending-only handlers, range, memory and reset');
      if (title === '绩效') {
        await select('绩效数据类型', '百分比');
        await expect(page.getByRole('row').filter({ hasText: pending })).toHaveCount(0);
        await select('绩效数据类型', '数值'); await expand();
        await expect(page.getByRole('row').filter({ hasText: pending })).toBeVisible();
      } else {
        await expect(bar.getByRole('button', { name: '新增', exact: true })).toHaveCount(0);
        await bar.getByRole('button', { name: '统计 / 导出', exact: true }).click();
        await expect(page.getByRole('dialog', { name: '成果统计与导出' })).toBeVisible();
        await page.keyboard.press('Escape');
      }
      await bar.getByRole('textbox', { name: title + '搜索' }).fill('不存在的记录');
      await bar.getByRole('textbox', { name: title + '搜索' }).press('Enter');
      await expect(page.getByRole('row').filter({ hasText: pending })).toHaveCount(0);
      await bar.getByRole('button', { name: '重置', exact: true }).click();
      await expect(bar.getByRole('combobox', { name: title + '范围', exact: true })).toBeEnabled();
      for (const width of [1920, 1440, 1280, 1024, 768]) {
        await page.setViewportSize({ width, height: 1000 });
        const size = await bar.evaluate(el => ({ client: el.clientWidth, scroll: el.scrollWidth }));
        assert.ok(size.scroll <= size.client + 1, title + ': overflow at ' + width);
        await page.screenshot({ path: path.join(output, route + '-' + width + '.png'), animations: 'disabled' });
      }
      checks.push(title + ': existing actions, keyword search, 5 viewport widths');
    }
    await api.post('/api/system/password/reset', { data: { user_id: 1910001, old_password: 'review-fixture-password', new_password: 'Convenience123' } });
    const uploader = await (await api.post('/api/system/login', { data: { user_id: 1910001, password: 'Convenience123' } })).json();
    assert.ok(uploader.token);
    const uploadContext = await browser.newContext({ viewport: { width: 1440, height: 1000 } });
    await uploadContext.addInitScript(token => localStorage.setItem('token', token), uploader.token);
    const uploadPage = await uploadContext.newPage();
    uploadPage.on('pageerror', error => errors.push(error.stack));
    await uploadPage.goto(state.frontend + '/home/works/achievement');
    await expect(uploadPage.locator('.business-filters').getByRole('button', { name: '新增', exact: true })).toBeVisible();
    await uploadPage.locator('.business-filters').getByRole('button', { name: '新增', exact: true }).click();
    await expect(uploadPage.getByRole('dialog').filter({ hasText: '报送部门' })).toBeVisible();
    checks.push('achievement uploader retains add permission and entry; administrator remains review-only');
    assert.deepEqual(errors, []);
    await fs.writeFile(path.join(output, 'results.json'), JSON.stringify({ checks, errors }, null, 2));
    console.log(JSON.stringify({ checks, errors }));
  } finally { await browser.close(); await api.dispose(); }
})().catch(error => { console.error(error); process.exitCode = 1; });
