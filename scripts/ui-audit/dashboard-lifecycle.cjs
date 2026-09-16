const { chromium } = require('../../target/ui-audit-tools/node_modules/playwright');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');
const OUTPUT = path.resolve(__dirname, '../../target/ui-audit/evidence-dashboard');

async function main() {
  const name = process.argv[2];
  assert(['delayed-init', 'chart-request', 'repeat', 'scroll'].includes(name));
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const context = await browser.newContext({ viewport: { width: 1440, height: 1000 }, reducedMotion: 'reduce' });
  await context.route('**/*', route => new URL(route.request().url()).hostname === '127.0.0.1' ? route.continue() : route.abort());
  const page = await context.newPage();
  const result = { case: name, errors: [], cancellationErrors: [] };
  page.on('pageerror', error => result.errors.push(error.message));
  page.on('console', message => {
    if (message.type() === 'error' && /AbortError|invalid dom|加载大屏|加载成效/.test(message.text())) result.cancellationErrors.push(message.text());
  });
  let release = () => {};
  const enter = async () => {
    await page.getByRole('menuitem', { name: '数据大屏', exact: true }).click();
    await page.waitForURL('**/dashboard');
  };
  const ready = () => page.waitForFunction(() => document.querySelectorAll('.dashboard-fixed-container [_echarts_instance_]').length === 5);
  const leave = async () => {
    await page.goBack(); await page.waitForURL('**/home/works');
    await page.locator('.dashboard-fixed-container').waitFor({ state: 'detached' });
  };
  await fs.mkdir(OUTPUT, { recursive: true });
  try {
    await page.goto('http://127.0.0.1:15173/login');
    await page.getByPlaceholder('账号', { exact: true }).fill('110228');
    await page.getByPlaceholder('密码', { exact: true }).fill('review-fixture-password');
    await page.getByRole('button', { name: /登\s*录/ }).click();
    await page.waitForURL('**/home/works');
    await page.getByText('审计专用A一级任务', { exact: true }).waitFor();
    if (name === 'delayed-init') {
      // 保持真实定时器，只延长大屏的初始化等待，确定性地在回调前离开。
      await page.evaluate(() => {
        const originalSet = window.setTimeout.bind(window);
        const originalClear = window.clearTimeout.bind(window);
        window.__initTimer = { hold: true, pending: [], cleared: 0, fired: 0 };
        window.setTimeout = (callback, delay, ...args) => {
          if (window.__initTimer.hold && delay === 800 && new Error().stack.includes('Dashboard.vue')) {
            const id = originalSet(() => { window.__initTimer.fired++; callback(...args); }, 60000);
            window.__initTimer.pending.push(id); return id;
          }
          return originalSet(callback, delay, ...args);
        };
        window.clearTimeout = id => {
          if (window.__initTimer.pending.includes(id)) {
            window.__initTimer.pending = window.__initTimer.pending.filter(value => value !== id);
            window.__initTimer.cleared++;
          }
          return originalClear(id);
        };
      });
      await enter(); await page.waitForFunction(() => window.__initTimer.pending.length > 0);
      await leave();
      result.timer = await page.evaluate(() => { window.__initTimer.hold = false; return window.__initTimer; });
      assert.equal(result.timer.pending.length, 0); assert.equal(result.timer.cleared, 1); assert.equal(result.timer.fired, 0);
    } else if (name === 'chart-request') {
      let reached;
      const entered = new Promise(resolve => { reached = resolve; });
      const gate = new Promise(resolve => { release = resolve; });
      let count = 0;
      await page.route('**/api/dashboard/tasks/all_level', async route => {
        if (++count === 2) { reached(); await gate; }
        await route.continue();
      });
      await enter(); await entered;
      const canceled = page.waitForEvent('requestfailed', { predicate: request => new URL(request.url()).pathname === '/api/dashboard/tasks/all_level' });
      await leave(); release();
      result.canceledRequest = (await canceled).failure()?.errorText;
      await page.unroute('**/api/dashboard/tasks/all_level');
    } else if (name === 'repeat') {
      result.completedCycles = 0;
      for (let i = 0; i < 3; i++) {
        await enter(); await ready(); await leave();
        assert.equal(await page.locator('[_echarts_instance_]').count(), 0);
        result.completedCycles++;
      }
    } else {
      // 仅此场景注入六条合成归档成果，激活列表滚动；其余请求仍为真实接口。
      result.responseInjected = true;
      await page.route('**/api/achievement/', route => route.fulfill({ json: Array.from({ length: 6 }, (_, i) => ({
        achId: 989000 + i, category: 1, achName: `滚动回归成果${i}`, level: '省级',
        gotTime: '2026-09-01 10:00:00', auditStatus: 30, comment: '合成滚动测试'
      })) }));
      await enter(); await ready();
      await page.waitForFunction(() => {
        const el = document.querySelector('.ul_listIn');
        return el && el.style.transform && el.style.transform !== 'translateY(0px)';
      });
      await page.evaluate(() => { window.__oldScroll = document.querySelector('.ul_listIn'); });
      await leave();
      const before = await page.evaluate(() => window.__oldScroll.style.transform);
      await page.waitForTimeout(600); // 超过两层滚动延迟，确保已脱离 DOM 的列表不再变化。
      assert.equal(await page.evaluate(() => window.__oldScroll.style.transform), before);
      result.detachedScrollStopped = true;
    }
    await enter(); await ready();
    result.chartsAfterReentry = await page.locator('.dashboard-fixed-container [_echarts_instance_]').count();
    assert.deepEqual(result.errors, []); assert.deepEqual(result.cancellationErrors, []);
    assert.equal(await page.locator('.el-message--error').count(), 0);
    await page.screenshot({ path: path.join(OUTPUT, name + '.png'), fullPage: true });
    await fs.writeFile(path.join(OUTPUT, name + '.json'), JSON.stringify(result, null, 2));
    console.log(JSON.stringify(result, null, 2));
  } catch (error) {
    await page.screenshot({ path: path.join(OUTPUT, name + '-failure.png'), fullPage: true });
    console.error(JSON.stringify(result)); throw error;
  } finally { release(); await context.close(); await browser.close(); }
}
main().catch(error => { console.error(error); process.exitCode = 1; });
