const { chromium } = require('../../target/ui-audit-tools/node_modules/playwright');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');
const BASE = 'http://127.0.0.1:15173';
const OUTPUT = path.resolve(__dirname, '../../target/ui-audit/evidence-performance-context');
const names = { a: '审计手动绩效', b: '审计零值绩效B' };

async function main() {
  const name = process.argv[2];
  assert(['stale-failure', 'same-record', 'year-change', 'unmount'].includes(name));
  await fs.mkdir(OUTPUT, { recursive: true });
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const context = await browser.newContext({ viewport: { width: 1440, height: 1100 }, reducedMotion: 'reduce' });
  await context.route('**/*', route => new URL(route.request().url()).hostname === '127.0.0.1' ? route.continue() : route.abort());
  const page = await context.newPage();
  const result = { case: name, errors: [], checks: [] };
  page.on('pageerror', error => result.errors.push(error.message));
  const gates = [];
  const drawer = page.getByRole('dialog', { name: '绩效指标详情', exact: true });
  const frames = () => page.evaluate(() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve))));
  const close = async () => { await drawer.locator('.el-drawer__close-btn').click(); await drawer.waitFor({ state: 'hidden' }); };
  const open = key => page.getByText(names[key], { exact: true }).click();
  const settle = () => page.waitForFunction(() => [...document.querySelectorAll('.el-drawer.open .el-loading-mask')]
    .every(mask => getComputedStyle(mask).display === 'none' || mask.getBoundingClientRect().width === 0));
  const records = async () => (await drawer.locator('.log-user').allTextContents()).map(text => text.trim());
  const assertPending = async () => {
    await frames();
    assert.deepEqual(await records(), []);
    assert.equal(await drawer.getByRole('button', { name: '确认提交', exact: true }).count(), 0);
    assert.ok(await drawer.locator('.el-loading-mask:visible').count() > 0);
  };
  // 拦截一次请求并延迟真实响应；仅 stale-failure 主动模拟网络失败。
  const hold = async (perfId, year = 2026, fail = false) => {
    const url = `${BASE}/api/performance/audit/perf/${perfId}?year=${year}`;
    let release;
    let entered;
    const gate = new Promise(resolve => { release = resolve; });
    const reached = new Promise(resolve => { entered = resolve; });
    let finish;
    const done = new Promise(resolve => { finish = resolve; });
    let claimed = false;
    const handler = async route => {
      if (claimed) return route.fallback();
      claimed = true;
      entered();
      await gate;
      try {
        if (fail) await route.abort('failed');
        else await route.continue();
      } finally { finish(); }
    };
    await page.route(url, handler);
    const handle = { reached, release, flush: async () => {
      const completion = fail
        ? page.waitForEvent('requestfailed', { predicate: request => request.url() === url })
        : page.waitForResponse(response => response.url() === url);
      release();
      const response = await completion;
      if (!fail) await response.finished();
      await done;
      await page.unroute(url, handler);
      await frames();
    } };
    gates.push(handle);
    return handle;
  };
  try {
    await page.goto(BASE + '/login');
    await page.getByPlaceholder('账号', { exact: true }).fill('110228');
    await page.getByPlaceholder('密码', { exact: true }).fill('review-fixture-password');
    await page.getByRole('button', { name: /登\s*录/ }).click();
    await page.waitForURL('**/home/works');
    await page.getByText('审计专用A一级任务', { exact: true }).waitFor();
    await page.getByRole('menuitem', { name: '绩效', exact: true }).click();
    await page.getByText('社会效益指标', { exact: true }).click();
    await page.getByText(names.a, { exact: true }).waitFor();
    const a = await hold(950011, 2026, name === 'stale-failure');
    await open('a'); await a.reached; await assertPending(); await close();
    if (name === 'stale-failure') {
      const b = await hold(950021);
      await open('b'); await b.reached;
      await a.flush(); await assertPending();
      result.checks.push('旧 A 请求失败后，B 仍加载中且不可审批');
      await b.flush(); await settle();
      assert.deepEqual(await records(), ['审核单 971002']);
      assert.equal(await drawer.getByRole('button', { name: '确认提交', exact: true }).isEnabled(), true);
      result.checks.push('B 自身响应后才显示 B 的审核单及审批按钮');
    } else if (name === 'same-record') {
      await open('b'); await page.getByText('审核单 971002', { exact: true }).waitFor(); await settle(); await close();
      const a2 = await hold(950011);
      await open('a'); await a2.reached;
      await a.flush(); await assertPending();
      result.checks.push('A→B→A 后，旧 A 响应不能结束新 A 的加载');
      await a2.flush(); await settle();
      assert.deepEqual(await records(), ['审核单 971001']);
      result.checks.push('重新打开的 A 只接受本次请求');
    } else if (name === 'year-change') {
      const yearResponse = page.waitForResponse(response => new URL(response.url()).pathname === '/api/performance/year/2027');
      await page.locator('.filter-select').first().click();
      await page.getByRole('option', { name: '2027年', exact: true }).click();
      await (await yearResponse).finished();
      await page.locator('.table-wrapper .el-loading-mask').waitFor({ state: 'hidden' });
      await open('a'); await settle();
      await a.flush();
      assert.deepEqual(await records(), []);
      assert.equal(await drawer.locator('.el-form-item').filter({ hasText: '当前年份' }).locator('input').inputValue(), '2027');
      assert.equal(await drawer.getByRole('button', { name: '确认提交', exact: true }).count(), 0);
      result.checks.push('2026 年迟到响应未进入 2027 年详情，2027 年无审核单且不可审批');
    } else {
      await page.getByRole('menuitem', { name: '任务', exact: true }).click();
      await page.waitForURL('**/home/works');
      await a.flush();
      assert.equal(await page.getByRole('dialog', { name: '绩效指标详情', exact: true }).count(), 0);
      await page.getByRole('menuitem', { name: '绩效', exact: true }).click();
      await page.getByText('社会效益指标', { exact: true }).click();
      await open('b'); await page.getByText('审核单 971002', { exact: true }).waitFor(); await settle();
      assert.deepEqual(await records(), ['审核单 971002']);
      result.checks.push('组件卸载后迟到响应无异常，重新进入后可正常查看 B');
    }
    assert.deepEqual(result.errors, []);
    await page.screenshot({ path: path.join(OUTPUT, name + '.png'), fullPage: true });
    await fs.writeFile(path.join(OUTPUT, name + '.json'), JSON.stringify(result, null, 2));
    console.log(JSON.stringify(result, null, 2));
  } catch (error) {
    console.error(JSON.stringify({ url: page.url(), text: await page.locator('body').innerText(), result }));
    await page.screenshot({ path: path.join(OUTPUT, name + '-failure.png'), fullPage: true });
    throw error;
  } finally {
    gates.forEach(gate => gate.release());
    await context.close(); await browser.close();
  }
}
main().catch(error => { console.error(error); process.exitCode = 1; });
