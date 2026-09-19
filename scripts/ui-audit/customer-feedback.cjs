const { chromium, request } = require('../../target/ui-audit-tools/node_modules/playwright');
const { expect } = require('../../target/ui-audit-tools/node_modules/playwright/test');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');

async function main() {
  const root = path.resolve(__dirname, '../../target/ui-audit');
  const state = JSON.parse(await fs.readFile(path.join(root, 'state.json')));
  assert.match(state.schema, /^biz_review_test_[0-9a-f]{32}$/); assert.equal(state.frontend, 'http://127.0.0.1:15273');
  const before = process.argv.includes('--before');
  const output = path.join(root, 'evidence-customer-' + (before ? 'before' : 'after')); await fs.mkdir(output, { recursive: true });
  const api = await request.newContext({ baseURL: state.frontend });
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const checks = [], errors = [];
  let page;
  try {
    await api.post('/api/system/password/reset', { data: { user_id: 110228, old_password: 'review-fixture-password', new_password: 'Convenience123' } });
    const auth = await (await api.post('/api/system/login', { data: { user_id: 110228, password: 'Convenience123' } })).json();
    assert.ok(auth.token);
    const context = await browser.newContext({ viewport: { width: before ? 1440 : 1920, height: 1000 } });
    await context.addInitScript(token => localStorage.setItem('token', token), auth.token);
    page = await context.newPage(); page.setDefaultTimeout(15000); page.on('pageerror', e => errors.push(e.message));
    await page.clock.install();
    await page.goto(state.frontend + '/home/works/achievement');
    await expect(page.locator('.quick-filters').getByRole('radio', { name: '全部', exact: true })).toBeEnabled();
    const badge = page.getByRole('menuitem', { name: /审核中心/ }).locator('.el-badge');
    await expect(badge).toContainText(/\d/); const originalCount = Number(await badge.innerText());
    await expect(page.locator('.table-wrapper .el-loading-mask')).toBeHidden();
    for (let i = 0; i < 10; i++) {
      const closed = page.locator('.el-table__expand-icon:not(.el-table__expand-icon--expanded):visible');
      if (!await closed.count()) break; await closed.first().click();
    }
    await page.locator('.el-table__body').getByRole('button', { name: '审核', exact: true }).first().click();
    const drawer = page.getByRole('dialog', { name: '成果审核', exact: true });
    await expect(drawer.getByRole('button', { name: '提交审核', exact: true })).toBeVisible();
    await page.clock.pauseAt(new Date(Date.now() + 100));
    await drawer.getByRole('button', { name: '提交审核', exact: true }).click();
    await expect(drawer).not.toHaveClass(/\bopen\b/);
    await page.clock.runFor(1000); await expect(drawer).toBeHidden();
    const todos = await (await api.get('/api/achievement/audit/todo', { headers: { Authorization: auth.token } })).json();
    assert.equal(todos.length + 2, originalCount - 1);
    await expect(badge).toHaveText(String(before ? originalCount : originalCount - 1));
    checks.push(before ? 'REPRODUCED: achievement approval leaves sidebar count unchanged despite backend pending decrease' : 'FIXED: achievement approval immediately updates sidebar count');
    await page.screenshot({ path: path.join(output, 'achievement-badge.png'), animations: 'disabled' });
    await page.clock.resume();
    await page.goto(state.frontend + '/home/works');
    await page.locator('.filter-item').filter({ hasText: before ? '状态:' : '任务状态' }).locator('.el-select').click();
    await expect(page.getByRole('option', { name: '审核中', exact: true })).toHaveCount(before ? 0 : 1);
    checks.push(before ? 'REPRODUCED: no independent in-review status filter' : 'FIXED: independent in-review status filter exists');
    await page.keyboard.press('Escape');
    if (!before) {
      const expand = async () => {
        for (let i = 0; i < 10; i++) {
          const closed = page.locator('.el-table__expand-icon:not(.el-table__expand-icon--expanded):visible');
          if (!await closed.count()) break; await closed.first().click();
        }
      };
      await expect(page.getByRole('combobox', { name: '任务范围', exact: true })).toBeEnabled();
      await page.locator('.filter-item').filter({ hasText: '任务状态' }).locator('.el-select').click();
      await page.getByRole('option', { name: '审核中', exact: true }).click(); await expand();
      await expect(page.getByRole('row').filter({ hasText: '客户待专业群审核任务' })).toBeVisible();
      await page.getByRole('combobox', { name: '任务待审阶段', exact: true }).locator('xpath=ancestor::*[contains(@class, "el-select__wrapper")]').click();
      await page.getByRole('option', { name: '待归口部门审核', exact: true }).click(); await expand();
      await expect(page.getByRole('row').filter({ hasText: '客户待归口审核任务' })).toBeVisible();
      await expect(page.getByRole('row').filter({ hasText: '客户待专业群审核任务' })).toHaveCount(0);
      await page.getByRole('combobox', { name: '任务当前处理部门', exact: true }).locator('xpath=ancestor::*[contains(@class, "el-select__wrapper")]').click();
      await page.getByRole('option', { name: '审计测试部门B', exact: true }).click();
      await expect(page.getByRole('row').filter({ hasText: '客户待归口审核任务' })).toBeVisible();
      await page.reload(); await expand();
      await expect(page.getByRole('row').filter({ hasText: '客户待归口审核任务' })).toBeVisible();
      await page.getByRole('combobox', { name: '任务当前处理部门', exact: true }).locator('xpath=ancestor::*[contains(@class, "el-select__wrapper")]').click();
      await page.getByRole('option', { name: '审计测试部门A', exact: true }).click();
      await expect(page.getByRole('row').filter({ hasText: '客户待归口审核任务' })).toHaveCount(0);
      await page.getByRole('button', { name: '重置', exact: true }).click(); await expand();
      await expect(page.getByRole('row').filter({ hasText: '客户待归口审核任务' })).toBeVisible();
      checks.push('task status, audit stage and actual handler department intersect correctly; memory and reset work');
      await page.screenshot({ path: path.join(output, 'task-stage-filters.png'), animations: 'disabled' });

      await page.goto(state.frontend + '/home/works/achievement');
      await page.getByRole('button', { name: '统计 / 导出', exact: true }).click();
      const report = page.getByRole('dialog', { name: '成果统计与导出', exact: true });
      const summary = report.locator('.report-summary');
      await expect(summary).toContainText('成果 3 条'); await expect(summary).toContainText('已归档 2 条'); await expect(summary).toContainText('奖项数量 2');
      const setDates = async (start, end) => {
        await report.getByPlaceholder('开始日期', { exact: true }).fill(start);
        await report.getByPlaceholder('开始日期', { exact: true }).press('Tab');
        await report.getByPlaceholder('结束日期', { exact: true }).fill(end);
        await report.getByPlaceholder('结束日期', { exact: true }).press('Enter');
        await report.getByText('成果统计与导出', { exact: true }).click();
      };
      await setDates('2026-06-01', '2026-06-30'); await expect(summary).toContainText('成果 3 条');
      await report.getByRole('combobox', { name: '统计时间口径', exact: true }).locator('xpath=ancestor::*[contains(@class, "el-select__wrapper")]').click();
      await page.getByRole('option', { name: '最近提交时间', exact: true }).click();
      await expect(summary).toContainText('成果 0 条'); await expect(report.getByRole('button', { name: '导出明细 CSV', exact: true })).toBeDisabled();
      await setDates('2026-07-01', '2026-07-01'); await expect(summary).toContainText('成果 2 条');
      const downloaded = page.waitForEvent('download'); await report.getByRole('button', { name: '导出明细 CSV', exact: true }).click();
      const csv = await fs.readFile(await (await downloaded).path(), 'utf8');
      assert.ok(csv.includes('审核情况未知')); assert.ok(csv.includes('连续审核成果A')); assert.ok(!csv.includes('无审核证据的历史成果'));
      await report.getByRole('button', { name: '重置统计条件', exact: true }).click();
      await report.getByRole('combobox', { name: '统计时间口径', exact: true }).locator('xpath=ancestor::*[contains(@class, "el-select__wrapper")]').click();
      await page.getByRole('option', { name: '当前归档时间', exact: true }).click();
      await setDates('2026-01-01', '2026-12-31'); await expect(summary).toContainText('成果 1 条');
      await report.getByRole('button', { name: '重置统计条件', exact: true }).click();
      await report.getByRole('combobox', { name: '统计状态', exact: true }).locator('xpath=ancestor::*[contains(@class, "el-select__wrapper")]').click();
      await page.getByRole('option', { name: '已归档', exact: true }).click(); await expect(summary).toContainText('成果 2 条');
      const summaryDownload = page.waitForEvent('download'); await report.getByRole('button', { name: '导出统计 CSV', exact: true }).click();
      const summaryCsv = await fs.readFile(await (await summaryDownload).path(), 'utf8'); assert.ok(summaryCsv.includes('成果条数')); assert.ok(summaryCsv.includes('审计测试部门A'));
      await page.screenshot({ path: path.join(output, 'achievement-report.png'), animations: 'disabled' });
      checks.push('real report totals, three time bases, inclusive date boundaries, status intersection and CSV downloads');
      checks.push('legacy records without audit evidence excluded from date-based report; professional review remains unknown');
    }
    assert.deepEqual(errors, []);
    await fs.writeFile(path.join(output, 'results.json'), JSON.stringify({ checks, errors }, null, 2));
    console.log(JSON.stringify({ checks, errors }));
  } catch (e) { if (page) await page.screenshot({ path: path.join(output, 'failure.png') }); throw e; }
  finally { await browser.close(); await api.dispose(); }
}
main().catch(e => { console.error(e); process.exitCode = 1; });
