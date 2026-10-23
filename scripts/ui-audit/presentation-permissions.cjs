// All business writes use serve.py --fixture presentation's isolated synthetic schema.
const { chromium, request } = require('../../target/ui-audit-tools/node_modules/playwright');
const { expect } = require('../../target/ui-audit-tools/node_modules/playwright/test');
const fs = require('node:fs/promises'), path = require('node:path'), assert = require('node:assert/strict');
(async () => {
  const state = JSON.parse(await fs.readFile(path.resolve(__dirname, '../../target/ui-audit/state.json')));
  assert.match(state.schema, /^biz_review_test_[0-9a-f]{32}$/);
  const api = await request.newContext({ baseURL: state.frontend }), browser = await chromium.launch({ channel: 'msedge', headless: true });
  const contexts = [], checks = [], errors = [], tokens = new Map(); let page;
  const output = path.resolve(__dirname, '../../target/ui-audit/presentation-permissions'); await fs.mkdir(output, { recursive: true });
  const done = name => { checks.push(name); console.log('PASS ' + name); };
  async function token(id) {
    if (!tokens.has(id)) { const r = await api.post('/api/system/login', { data: { user_id: id, password: 'review-fixture-password' } }); const data = await r.json(); assert.ok(data.token, JSON.stringify(data)); tokens.set(id, data.token); }
    return tokens.get(id);
  }
  async function tab(id) {
    const value = await token(id), context = await browser.newContext({ viewport: { width: 1440, height: 960 }, storageState: { cookies: [], origins: [{ origin: state.frontend, localStorage: [{ name: 'token', value }] }] } });
    contexts.push(context); const result = await context.newPage(); result.setDefaultTimeout(20000); result.on('pageerror', e => errors.push(e.message)); return result;
  }
  async function pick(name, value) {
    const combo = page.getByRole('combobox', { name, exact: true });
    await expect(combo).toBeEnabled();
    await combo.locator('xpath=ancestor::div[contains(concat(" ",normalize-space(@class)," ")," el-select ")][1]').click();
    await page.getByRole('option', { name: value, exact: true }).click();
    await page.keyboard.press('Escape');
  }
  try {
    page = await tab(111651);
    for (const [url, menu] of [['/home/works', '任务'], ['/home/works/performance', '绩效'], ['/home/works/achievement', '成果']]) {
      await page.goto(state.frontend + url); await expect(page.getByRole('menuitem', { name: menu, exact: true })).toBeVisible();
      await expect(page.locator('.el-table__body .el-table__row').first()).toBeVisible();
    }
    await page.goto(state.frontend + '/home/admin/overview'); await expect(page.getByRole('heading', { name: '管理概览', exact: true })).toBeVisible();
    assert.equal((await api.get('/api/admin/users', { headers: { Authorization: await token(111651) } })).status(), 403);
    done('confirmed-leader-schoolwide-lists-and-readonly-overview-without-admin-writes');

    page = await tab(110228); await page.goto(state.frontend + '/home/works');
    await expect(page.getByRole('combobox', { name: '任务责任人', exact: true })).toBeHidden();
    await page.getByRole('button', { name: /^高级筛选/ }).click();
    await pick('任务归口部门', '审计测试部门B'); await pick('任务类型', '重点');
    await expect(page.locator('.el-table__empty-text').first()).toBeVisible();
    await page.getByRole('button', { name: /^收起高级筛选/ }).click();
    await expect(page.locator('.advanced-toggle .active-count')).toHaveText('2');
    await page.getByRole('button', { name: '重置', exact: true }).click();
    await expect(page.locator('.el-table__body .el-table__row').first()).toBeVisible();
    done('task-advanced-intersection-collapse-keeps-conditions-and-reset');

    await page.goto(state.frontend + '/home/works/performance'); await pick('绩效填报方式', '手动填报');
    await expect(page.locator('.el-table__body')).toContainText('审计手动绩效');
    await expect(page.locator('.el-table__body')).not.toContainText('审计自动绩效');
    await page.getByRole('button', { name: /^高级筛选/ }).click(); await expect(page.getByRole('combobox', { name: '绩效专业群审核人', exact: true })).toBeVisible();
    done('performance-fill-mode-and-reviewer-advanced-controls');

    await page.goto(state.frontend + '/home/works/achievement'); await pick('成果审核状态', '已驳回');
    await page.getByRole('button', { name: /^高级筛选/ }).click(); await pick('成果成果类别', '1.落实立德树人根本任务'); await pick('成果成果级别', '省级');
    await page.locator('.el-table__expand-icon').first().click();
    await expect(page.locator('.rejection-reason')).toHaveText('驳回原因：请补充上级发文依据（合成驳回原因）');
    await expect(page.locator('.el-table__body')).toContainText('已驳回');
    const text = page.locator('.hover-scroll-text').first(); await expect(text).toBeVisible(); await expect(text).toHaveClass(/truncated/);
    assert.ok((await text.getAttribute('aria-label')).endsWith('全名结尾'));
    await text.hover(); await expect.poll(() => text.locator('.scroll-content').evaluate(el => getComputedStyle(el).transform)).not.toBe('none');
    await page.mouse.move(5, 5); await text.focus(); await expect(text).toBeFocused();
    await expect.poll(() => text.locator('.scroll-content').evaluate(el => getComputedStyle(el).animationName)).toMatch(/^reveal-name/);
    await page.screenshot({ path: path.join(output, 'achievement-rejection-and-advanced.png'), fullPage: true });
    await page.getByRole('button', { name: /^收起高级筛选/ }).click(); await page.reload();
    await expect(page.locator('.advanced-toggle .active-count')).toHaveText('2'); await expect(page.getByRole('combobox', { name: '成果审核状态', exact: true }).locator('xpath=ancestor::div[contains(concat(" ",normalize-space(@class)," ")," el-select ")][1]')).toContainText('已驳回');
    done('achievement-combined-filters-rejection-reason-name-scroll-keyboard-and-restored-conditions');

    await page.goto(state.frontend + '/home/core-indicators'); await page.getByRole('button', { name: /^高级筛选/ }).click();
    await page.getByRole('textbox', { name: '核心指标搜索', exact: true }).fill('产业发展报告'); await pick('核心指标最近填报状态', '尚未填报');
    await expect(page.locator('.indicator-table .el-table__body .indicator-row')).toHaveCount(1);
    await page.goto(state.frontend + '/home/work-records'); await expect(page.getByRole('combobox', { name: '纪实填报人', exact: true })).toBeHidden();
    await page.getByRole('button', { name: /^高级筛选/ }).click(); await expect(page.getByRole('combobox', { name: '纪实填报人', exact: true })).toBeVisible();
    done('core-indicator-search-status-intersection-and-work-record-advanced-author');

    page = await tab(910007); await page.goto(state.frontend + '/home/works/budget?year=2026&month=2');
    await expect(page.getByRole('button', { name: '导入', exact: true })).toBeVisible(); await expect(page.getByRole('button', { name: '解锁', exact: true })).toHaveCount(0);
    await page.getByRole('button', { name: /^高级筛选/ }).click(); await page.getByRole('combobox', { name: '预算改革任务', exact: true }).locator('xpath=ancestor::div[contains(concat(" ",normalize-space(@class)," ")," el-select ")][1]').click(); await page.getByRole('option').first().click(); await page.keyboard.press('Escape');
    await expect(page.locator('.lower-sheet .task-head')).toHaveCount(1);
    await page.getByRole('button', { name: '导入', exact: true }).click(); const dialog = page.getByRole('dialog', { name: /预算.*导入|导入.*预算/ });
    await dialog.locator('input[type=file]').setInputFiles(path.resolve(__dirname, '../../../biz/public/templates/budget-template.xlsx'));
    const uploaded = page.waitForResponse(r => r.url().includes('/api/budget/import') && r.request().method() === 'POST');
    await dialog.getByRole('button', { name: '确认导入并锁定', exact: true }).click(); const response = await uploaded; const data = await response.json(); assert.ok(data.sheet, JSON.stringify(data));
    await expect(dialog).toBeHidden(); await page.getByRole('button', { name: '版本记录', exact: true }).click();
    await expect(page.getByRole('dialog', { name: /预算版本记录/ })).toContainText('合成财务填报员');
    await expect(page.getByRole('button', { name: '恢复', exact: true })).toHaveCount(0);
    done('finance-normal-account-real-budget-import-and-version-view-without-admin-restoration');

    page = await tab(910006); await page.goto(state.frontend + '/home/works/budget?year=2026&month=2'); await expect(page.getByRole('button', { name: '导入', exact: true })).toBeVisible();
    for (const url of ['/home/works', '/home/works/performance', '/home/works/achievement', '/home/core-indicators', '/home/work-records', '/home/works/budget']) {
      page = await tab(110228); await page.setViewportSize({ width: 960, height: 640 }); await page.goto(state.frontend + url);
      await page.getByRole('button', { name: /^高级筛选/ }).click(); await expect(page.getByRole('button', { name: /^收起高级筛选/ })).toBeVisible();
      assert.equal(await page.evaluate(() => document.documentElement.scrollWidth > innerWidth + 2), false, url);
      if (['/home/works', '/home/works/performance', '/home/works/achievement'].includes(url)) {
        const row = page.locator('.el-table__body .el-table__row').first(); await row.scrollIntoViewIfNeeded(); await expect(row).toBeInViewport();
      }
    }
    done('office-budget-filling-and-six-pages-narrow-advanced-layout');
    page = await tab(110228); await page.setViewportSize({ width: 960, height: 640 }); await page.emulateMedia({ reducedMotion: 'reduce' });
    await page.goto(state.frontend + '/home/works/achievement'); await page.getByRole('button', { name: /^高级筛选/ }).click();
    await page.locator('.el-table__expand-icon').first().click(); const reducedName = page.locator('.hover-scroll-text').filter({ hasText: '合成长名称成果' });
    await reducedName.hover(); await expect.poll(() => reducedName.locator('.scroll-content').evaluate(el => getComputedStyle(el).animationName)).toBe('none');
    await expect.poll(() => reducedName.locator('.scroll-content').evaluate(el => el.clientHeight > 40)).toBe(true);
    await page.screenshot({ path: path.join(output, 'narrow-reduced-motion.png'), fullPage: true });
    await page.goto(state.frontend + '/home/works/budget?year=2026&month=2'); await page.getByRole('button', { name: /^高级筛选/ }).click();
    await page.screenshot({ path: path.join(output, 'narrow-budget.png'), fullPage: true });
    done('reduced-motion-wraps-complete-name-without-animation-and-narrow-screenshots');
    assert.deepEqual(errors, []); await fs.writeFile(path.join(output, 'result.json'), JSON.stringify({ checks, pageErrors: errors }, null, 2));
  } catch (e) { if (page) await page.screenshot({ path: path.join(output, 'failure.png'), fullPage: true }); throw e; }
  finally { for (const c of contexts) await c.close(); await browser.close(); await api.dispose(); }
})().catch(e => { console.error(e); process.exitCode = 1; });
