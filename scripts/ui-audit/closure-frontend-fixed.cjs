// 只访问独立审计端口；依赖安装在 target/ui-audit-tools，不改动项目依赖。
const { chromium } = require('../../target/ui-audit-tools/node_modules/playwright');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');
const BASE = 'http://127.0.0.1:15173';
const OUTPUT = path.resolve(__dirname, '../../target/ui-audit/evidence-fixed');
const PASSWORD = 'review-fixture-password';

async function login(page, id = '910001', password = PASSWORD, redirect = '') {
  await page.goto(BASE + '/login' + (redirect ? '?redirect=' + encodeURIComponent(redirect) : ''));
  await page.getByPlaceholder('账号', { exact: true }).fill(id);
  await page.getByPlaceholder('密码', { exact: true }).fill(password);
  await page.getByRole('button', { name: /登\s*录/ }).click();
  await page.waitForURL('**/home/**');
}

async function logout(page) {
  await page.locator('.user-trigger').click();
  await page.getByRole('menuitem', { name: '退出登录' }).click();
  await page.getByRole('button', { name: '确定', exact: true }).click();
  await page.waitForURL('**/login');
}

async function main() {
  assert.match(JSON.parse(await fs.readFile(path.resolve(__dirname, '../../target/ui-audit/state.json'))).schema, /^biz_review_test_[0-9a-f]{32}$/);
  await fs.mkdir(OUTPUT, { recursive: true });
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const context = await browser.newContext({ viewport: { width: 1440, height: 1000 } });
  // 合成数据检查不加载外部头像或其他外部资源。
  await context.route('**/*', route => new URL(route.request().url()).hostname === '127.0.0.1'
    ? route.continue() : route.abort());
  const page = await context.newPage();
  const errors = [];
  page.on('pageerror', error => errors.push(error.message));
  const apiCalls = [];
  page.on('request', req => {
    const url = new URL(req.url());
    if (url.pathname.startsWith('/api/')) apiCalls.push({ method: req.method(), path: url.pathname });
  });
  const result = { case: process.argv[2] || 'cache', errors };
  try {
    assert(['cache', 'notices', 'deep-link', 'polling'].includes(result.case), 'Unknown audit case');
    if (result.case === 'polling') await page.clock.install();
    await login(page, '910001', PASSWORD,
      result.case === 'deep-link' ? '/home/works?taskId=930002' : '');
    await page.getByText('审计专用A一级任务', { exact: true }).first().waitFor();
    if (result.case === 'cache') {
      await logout(page);
      let release;
      const gate = new Promise(resolve => { release = resolve; });
      await page.route('**/api/biz/tasks', async route => { await gate; await route.continue(); });
      try {
        await login(page, '910004');
        await page.locator('.user-name').getByText('审计用户B', { exact: true }).waitFor();
        await page.waitForTimeout(500);
        assert.equal(await page.getByText('审计专用A一级任务', { exact: true }).count(), 0);
        result.visibleUser = await page.locator('.user-name').innerText();
        result.otherUsersTaskVisible = false;
        await page.screenshot({ path: path.join(OUTPUT, 'cache-cross-account.png'), fullPage: true });
        const refreshed = page.waitForResponse(response => new URL(response.url()).pathname === '/api/biz/tasks');
        release();
        const response = await refreshed;
        result.currentUserApiTaskCount = (await response.json()).length;
        assert.equal(result.currentUserApiTaskCount, 0);
        await page.getByText('审计专用A一级任务', { exact: true }).waitFor({ state: 'hidden' });
        result.otherUsersTaskRemovedAfterRefresh = true;
      } finally { release(); await page.unroute('**/api/biz/tasks'); }
    }
    if (result.case === 'notices') {
      await page.getByRole('menuitem', { name: /消息中心/ }).click();
      await page.locator('.notice-item').first().waitFor();
      const firstPage = await page.locator('.notice-title').allTextContents();
      result.firstPageCount = firstPage.length;
      assert.equal(firstPage.length, 10);
      await page.locator('.el-pagination .btn-next').click();
      const secondPage = await page.locator('.notice-title').allTextContents();
      result.secondPageCount = secondPage.length;
      result.pagesIdentical = JSON.stringify(firstPage) === JSON.stringify(secondPage);
      assert.equal(secondPage.length, 5);
      assert.equal(secondPage.some(title => firstPage.includes(title)), false);
      await page.screenshot({ path: path.join(OUTPUT, 'notices-page2.png'), fullPage: true });
      await page.getByRole('tab', { name: '已读消息', exact: true }).click();
      result.readCount = await page.locator('.notice-item').count();
      result.readPaginationPages = await page.locator('.el-pager .number').count();
      assert.equal(result.readCount, 5);
      assert.equal(result.readPaginationPages, 1);
      await page.screenshot({ path: path.join(OUTPUT, 'notices-filter-pagination.png'), fullPage: true });
    }
    if (result.case === 'deep-link') {
      const drawer = page.getByRole('dialog', { name: '任务详情与反馈', exact: true });
      await drawer.waitFor();
      result.coldVisitOpensTask = true;
      await page.screenshot({ path: path.join(OUTPUT, 'deep-link-cold.png'), fullPage: true });
      const refresh = page.waitForResponse(response => new URL(response.url()).pathname === '/api/biz/tasks');
      await page.reload();
      await refresh;
      await page.getByText('审计专用A一级任务', { exact: true }).waitFor();
      result.cachedVisitOpensTask = await drawer.waitFor({ timeout: 1800 }).then(() => true).catch(() => false);
      assert.equal(result.cachedVisitOpensTask, true);
      result.url = page.url();
      await page.screenshot({ path: path.join(OUTPUT, 'deep-link-cached.png'), fullPage: true });
    }
    if (result.case === 'polling') {
      await page.clock.pauseAt(new Date(Date.now() + 500));
      const countNotice = () => apiCalls.filter(item => item.path === '/api/system/notice').length;
      let before = countNotice();
      let refreshed = page.waitForResponse(response => new URL(response.url()).pathname === '/api/system/notice');
      await page.clock.runFor(30001);
      await (await refreshed).finished();
      result.firstMountRequestsPerInterval = countNotice() - before;
      assert.equal(result.firstMountRequestsPerInterval, 1);
      await page.getByRole('menuitem', { name: '数据大屏', exact: true }).click();
      await page.waitForURL('**/dashboard');
      await page.locator('.common-layout').waitFor({ state: 'detached' });
      await page.goBack();
      await page.waitForURL('**/home/works');
      await page.getByText('审计专用A一级任务', { exact: true }).waitFor();
      before = countNotice();
      refreshed = page.waitForResponse(response => new URL(response.url()).pathname === '/api/system/notice');
      await page.clock.runFor(30001);
      await (await refreshed).finished();
      result.secondMountRequestsPerInterval = countNotice() - before;
      assert.equal(result.secondMountRequestsPerInterval, 1);
      await page.screenshot({ path: path.join(OUTPUT, 'polling-after-return.png'), fullPage: true });
    }
    assert.deepEqual(errors, []);
    result.apiCalls = apiCalls;
    await fs.writeFile(path.join(OUTPUT, result.case + '.json'), JSON.stringify(result, null, 2));
    console.log(JSON.stringify(result, null, 2));
  } catch (error) {
    console.error(JSON.stringify({ url: page.url(), text: await page.locator('body').innerText(), apiCalls, errors }));
    await page.screenshot({ path: path.join(OUTPUT, result.case + '-failure.png'), fullPage: true });
    throw error;
  } finally {
    await context.close();
    await browser.close();
  }
}

main().catch(error => { console.error(error); process.exitCode = 1; });
