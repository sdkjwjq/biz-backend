const { chromium } = require('../../target/ui-audit-tools/node_modules/playwright');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');
const BASE = 'http://127.0.0.1:15173';
const OUTPUT = path.resolve(__dirname, '../../target/ui-audit/evidence-business');

async function main() {
  const name = process.argv[2] || 'inspect-performance';
  assert(['inspect-performance', 'performance-race', 'zero-audit', 'performance-year', 'dashboard-unmount'].includes(name));
  await fs.mkdir(OUTPUT, { recursive: true });
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const context = await browser.newContext({ viewport: { width: 1440, height: 1000 }, reducedMotion: 'reduce' });
  await context.route('**/*', route => new URL(route.request().url()).hostname === '127.0.0.1' ? route.continue() : route.abort());
  const page = await context.newPage();
  const result = { case: name, errors: [], requests: [] };
  page.on('pageerror', error => result.errors.push({ message: error.message, stack: error.stack?.split('\n').slice(0, 6) }));
  page.on('request', request => {
    const pathname = new URL(request.url()).pathname;
    if (pathname.startsWith('/api/')) result.requests.push({ method: request.method(), path: pathname });
  });
  const screenshot = filename => page.screenshot({ path: path.join(OUTPUT, filename), fullPage: true });
  const settleDrawer = () => page.waitForFunction(() => [...document.querySelectorAll('.el-drawer.open .el-loading-mask')]
    .every(mask => getComputedStyle(mask).display === 'none' || mask.getBoundingClientRect().width === 0));
  try {
    await page.goto(BASE + '/login');
    await page.getByPlaceholder('账号', { exact: true }).fill(name === 'dashboard-unmount' ? '110228' : name === 'performance-year' ? '910001' : '910003');
    await page.getByPlaceholder('密码', { exact: true }).fill('review-fixture-password');
    await page.getByRole('button', { name: /登\s*录/ }).click();
    await page.waitForURL('**/home/works');
    await page.getByText('审计专用A一级任务', { exact: true }).waitFor();
    if (name === 'zero-audit') {
      const recordsResponse = page.waitForResponse(response => new URL(response.url()).pathname === '/api/performance/audit/todo');
      await page.getByRole('menuitem', { name: /审核中心/ }).click();
      await page.getByText('审计零值绩效B', { exact: true }).waitFor();
      await page.locator('.audit-container .el-loading-mask').waitFor({ state: 'hidden' });
      const apiRecord = (await (await recordsResponse).json()).find(row => Number(row.perfId) === 950021);
      result.apiValue = apiRecord.actualValue;
      result.text = await page.locator('.audit-container').innerText();
      const zeroRow = page.locator('.el-table__body tr').filter({ hasText: '审计零值绩效B' });
      result.zeroRow = await zeroRow.innerText();
      result.displayedValue = (await zeroRow.locator('td').nth(4).innerText()).trim();
      result.nonzeroDisplayedValue = (await page.locator('.el-table__body tr').filter({ hasText: '审计手动绩效' }).locator('td').nth(4).innerText()).trim();
      assert.equal(Number(result.apiValue), 0);
      assert.equal(result.displayedValue, '-');
      assert.equal(result.nonzeroDisplayedValue, '3');
      await screenshot('zero-audit.png');
    } else if (name !== 'dashboard-unmount') {
      await page.getByRole('menuitem', { name: '绩效', exact: true }).click();
      await page.getByText('社会效益指标', { exact: true }).click();
      await page.getByText('审计待填绩效C', { exact: true }).waitFor();
      result.listText = await page.locator('body').innerText();
      if (name === 'inspect-performance' || name === 'performance-year') {
        await page.getByText('审计待填绩效C', { exact: true }).click();
        await page.getByText('手动填报绩效', { exact: true }).waitFor();
        const drawer = page.getByRole('dialog', { name: '绩效指标详情', exact: true });
        await settleDrawer();
        result.drawerText = await drawer.innerText();
        result.inputs = await drawer.locator('input').evaluateAll(inputs => inputs.map(input => ({
          value: input.value, disabled: input.disabled, placeholder: input.placeholder, type: input.type
        })));
        result.selectedYearVisible = await page.getByText('2026年', { exact: true }).first().isVisible();
        if (name === 'performance-year') {
          const year = drawer.locator('.el-form-item').filter({ hasText: '当前年份' }).locator('input');
          result.yearInputValue = await year.inputValue();
          result.yearInputDisabled = await year.isDisabled();
          assert.equal(result.yearInputValue, '2026');
          assert.equal(result.yearInputDisabled, true);
          assert.equal(result.selectedYearVisible, true);
          assert.equal(await drawer.getByRole('button', { name: '提交填报', exact: true }).isEnabled(), true);
        }
        await screenshot('performance-year.png');
      }
      if (name === 'performance-race') {
        let release;
        let reached;
        const gate = new Promise(resolve => { release = resolve; });
        const entered = new Promise(resolve => { reached = resolve; });
        const slowPath = '**/api/performance/audit/perf/950011*';
        await page.route(slowPath, async route => { reached(); await gate; await route.continue(); });
        try {
          await page.getByText('审计手动绩效', { exact: true }).click();
          await entered;
          const drawer = page.getByRole('dialog', { name: '绩效指标详情', exact: true });
          await drawer.locator('.el-drawer__close-btn').click();
          await drawer.waitFor({ state: 'hidden' });
          await page.getByText('审计零值绩效B', { exact: true }).click();
          await page.getByText('审核单 971002', { exact: true }).waitFor();
          await settleDrawer();
          result.beforeRecord = await drawer.locator('.log-user').allTextContents();
          await screenshot('performance-race-before.png');
          const oldResponse = page.waitForResponse(response => new URL(response.url()).pathname === '/api/performance/audit/perf/950011');
          release();
          result.lateResponsePerfIds = (await (await oldResponse).json()).map(row => row.perfId);
          await (await oldResponse).finished();
          await page.evaluate(() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve))));
          await settleDrawer();
          result.currentInfo = await drawer.locator('.task-info-box').innerText();
          result.afterRecord = await drawer.locator('.log-user').allTextContents();
          result.approvalFormVisible = await drawer.getByText('审核处理', { exact: true }).isVisible();
          assert.ok(result.currentInfo.includes('审计零值绩效B'));
          assert.deepEqual(result.lateResponsePerfIds.map(Number), [950011]);
          assert.deepEqual(result.beforeRecord.map(text => text.trim()), ['审核单 971002']);
          assert.deepEqual(result.afterRecord.map(text => text.trim()), ['审核单 971002']);
          await screenshot('performance-race-after.png');
          // 实际审批 B，并在响应返回前切回 A；审批完成不能改写 A 的表单。
          let releaseApproval;
          const approvalGate = new Promise(resolve => { releaseApproval = resolve; });
          let approvalReached;
          const approvalEntered = new Promise(resolve => { approvalReached = resolve; });
          await page.route('**/api/performance/audit', async route => {
            const response = await route.fetch();
            approvalReached();
            await approvalGate;
            await route.fulfill({ response });
          });
          try {
            const auditRequest = page.waitForRequest(request => new URL(request.url()).pathname === '/api/performance/audit' && request.method() === 'POST');
            const auditResponse = page.waitForResponse(response => new URL(response.url()).pathname === '/api/performance/audit' && response.request().method() === 'POST');
            await drawer.getByRole('button', { name: '确认提交', exact: true }).click();
            result.submittedAuditId = (await auditRequest).postDataJSON().sub_id;
            await approvalEntered;
            await drawer.locator('.el-drawer__close-btn').click();
            await drawer.waitFor({ state: 'hidden' });
            const aRefresh = page.waitForResponse(response => new URL(response.url()).pathname === '/api/performance/audit/perf/950011');
            await page.getByText('审计手动绩效', { exact: true }).click();
            result.aFlowStatusAfter = (await (await aRefresh).json())[0].flowStatus;
            assert.equal(result.aFlowStatusAfter, 10);
            await page.getByText('审核单 971001', { exact: true }).waitFor();
            await settleDrawer();
            await drawer.getByPlaceholder('请输入审批意见（必填）...').fill('保留 A 的审批意见');
            releaseApproval();
            result.approvalResponse = await (await auditResponse).text();
            await (await auditResponse).finished();
            await page.evaluate(() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve))));
            result.aCommentAfterBResponse = await drawer.getByPlaceholder('请输入审批意见（必填）...').inputValue();
            assert.equal(result.aCommentAfterBResponse, '保留 A 的审批意见');
            assert.deepEqual((await drawer.locator('.log-user').allTextContents()).map(text => text.trim()), ['审核单 971001']);
            await drawer.locator('.el-drawer__close-btn').click();
            await drawer.waitFor({ state: 'hidden' });
            const bRefresh = page.waitForResponse(response => new URL(response.url()).pathname === '/api/performance/audit/perf/950021');
            await page.getByText('审计零值绩效B', { exact: true }).click();
            result.bFlowStatusAfter = (await (await bRefresh).json())[0].flowStatus;
            assert.equal(result.submittedAuditId, 971002);
            assert.ok(result.approvalResponse.includes('已通过专业群审核'));
            assert.equal(result.bFlowStatusAfter, 20);
            await settleDrawer();
            await screenshot('performance-race-correct-approval.png');
          } finally { releaseApproval(); await page.unroute('**/api/performance/audit'); }
        } finally { release(); await page.unroute(slowPath); }
      }
    } else {
      let release;
      let reached;
      const gate = new Promise(resolve => { release = resolve; });
      const entered = new Promise(resolve => { reached = resolve; });
      const slowPath = '**/api/dashboard/tasks/all_level';
      await page.route(slowPath, async route => { reached(); await gate; await route.continue(); });
      try {
        await page.getByRole('menuitem', { name: '数据大屏', exact: true }).click();
        await entered;
        await page.waitForURL('**/dashboard');
        const failure = page.waitForEvent('pageerror', { predicate: error => error.message.includes('Initialize failed: invalid dom'), timeout: 15000 });
        await page.goBack();
        await page.waitForURL('**/home/works');
        await page.locator('.dashboard-fixed-container').waitFor({ state: 'detached' });
        result.dashboardRemovedBeforeResponse = true;
        release();
        result.errorAfterLeaving = (await failure).message;
        result.returnedUrl = page.url();
        assert.ok(result.errorAfterLeaving.includes('Initialize failed: invalid dom'));
        await screenshot('dashboard-after-leaving.png');
      } finally { release(); await page.unroute(slowPath); }
    }
    await fs.writeFile(path.join(OUTPUT, name + '.json'), JSON.stringify(result, null, 2));
    console.log(JSON.stringify(result, null, 2));
  } catch (error) {
    console.error(JSON.stringify({ url: page.url(), text: await page.locator('body').innerText(), errors: result.errors }));
    await screenshot(name + '-failure.png');
    throw error;
  } finally {
    await context.close();
    await browser.close();
  }
}
main().catch(error => { console.error(error); process.exitCode = 1; });
