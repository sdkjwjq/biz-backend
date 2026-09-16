const { chromium } = require('../../target/ui-audit-tools/node_modules/playwright');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');
const OUTPUT = path.resolve(__dirname, '../../target/ui-audit/evidence-batch-8');

async function main() {
  const name = process.argv[2];
  const fixed = process.argv.includes('--fixed');
  assert(['empty-trend', 'year-race', 'budget-race'].includes(name));
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const context = await browser.newContext({ viewport: { width: 1440, height: 1100 }, reducedMotion: 'reduce' });
  await context.route('**/*', route => new URL(route.request().url()).hostname === '127.0.0.1' ? route.continue() : route.abort());
  const page = await context.newPage();
  const result = { case: name, fixed, errors: [] };
  let echartsUrl;
  page.on('request', req => { if (/\/echarts\.js\?/.test(req.url())) echartsUrl = req.url(); });
  page.on('pageerror', error => result.errors.push(error.message));
  const responseFor = predicate => page.waitForResponse(res => predicate(new URL(res.url())));
  const readTrend = async ({ moduleUrl }) => {
    const ec = await import(moduleUrl);
    const box = [...document.querySelectorAll('.boxall')].find(el => el.querySelector('.alltitle')?.textContent === '建设增长趋势');
    const chart = box && ec.getInstanceByDom(box.querySelector('.boxnav'));
    return chart?.getOption()?.series?.[0]?.data;
  };
  const selectYear = async year => {
    await page.locator('.dropdown-wrap').hover();
    await page.locator('.dropdown-list li').filter({ hasText: year + '年' }).click();
  };
  let release = () => {};
  await fs.mkdir(OUTPUT, { recursive: true });
  try {
    await page.goto('http://127.0.0.1:15173/login');
    await page.getByPlaceholder('账号', { exact: true }).fill('110228');
    await page.getByPlaceholder('密码', { exact: true }).fill('review-fixture-password');
    await page.getByRole('button', { name: /登\s*录/ }).click();
    await page.waitForURL('**/home/works');
    await page.getByText('审计专用A一级任务', { exact: true }).waitFor();
    if (name === 'budget-race') {
      await page.getByRole('button', { name: '预算', exact: true }).click();
      await page.locator('.budget-toolbar').waitFor();
      const month = page.locator('.toolbar-left .el-select').nth(1);
      let reached;
      const entered = new Promise(resolve => { reached = resolve; });
      const gate = new Promise(resolve => { release = resolve; });
      await page.route('**/api/budget?*', async route => {
        if (new URL(route.request().url()).searchParams.get('month') === '1') { reached(); await gate; }
        await route.continue();
      });
      await month.click(); await page.getByRole('option', { name: '1月', exact: true }).click(); await entered;
      const second = responseFor(url => url.pathname === '/api/budget' && url.searchParams.get('month') === '2');
      await month.click(); await page.getByRole('option', { name: '2月', exact: true }).click();
      result.currentApi = await (await second).json();
      await page.waitForFunction(() => document.querySelector('.budget-table-card').textContent.includes('222'));
      result.before = await page.locator('.budget-table-card').innerText();
      const old = responseFor(url => url.pathname === '/api/budget' && url.searchParams.get('month') === '1');
      release(); result.lateApi = await (await old).json();
      await (await old).finished();
      await page.evaluate(() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve))));
      result.selectedMonth = await month.innerText();
      result.after = await page.locator('.budget-table-card').innerText();
      assert.ok(result.selectedMonth.includes('2月'));
      assert.equal(result.currentApi.sheet.month, 2); assert.equal(result.lateApi.sheet.month, 1);
      assert.ok(result.after.includes(fixed ? '222' : '111'));
      assert.equal(result.after.includes('解锁'), !fixed);
    } else {
      const initial = responseFor(url => url.pathname === '/api/dashboard/trend/2026');
      await page.getByRole('menuitem', { name: '数据大屏', exact: true }).click();
      result.initialApi = await (await initial).json();
      await page.waitForFunction(() => document.querySelectorAll('[_echarts_instance_]').length === 5);
      assert.ok(echartsUrl);
      await page.evaluate(async url => { window.__auditEcharts = await import(url); }, echartsUrl);
      result.initialChartData = await page.evaluate(readTrend, { moduleUrl: echartsUrl });
      if (name === 'empty-trend') {
        assert.deepEqual(result.initialApi, []);
        assert.deepEqual(result.initialChartData, [10, 15, 12, 25, 20, 45]);
      } else {
        let reached;
        const entered = new Promise(resolve => { reached = resolve; });
        const gate = new Promise(resolve => { release = resolve; });
        await page.route('**/api/dashboard/trend/2025', async route => { reached(); await gate; await route.continue(); });
        await selectYear('2025'); await entered;
        const current = responseFor(url => url.pathname === '/api/dashboard/trend/2027');
        await selectYear('2027'); result.currentApi = await (await current).json();
        result.before = await (await page.waitForFunction(() => {
          const ec = window.__auditEcharts;
          return [...document.querySelectorAll('[_echarts_instance_]')].map(el => ec.getInstanceByDom(el)?.getOption()?.series?.[0]).find(series => series?.type === 'line' && series?.data?.[0] === 33)?.data;
        })).jsonValue();
        const old = responseFor(url => url.pathname === '/api/dashboard/trend/2025');
        release(); result.lateApi = await (await old).json();
        if (fixed) {
          await (await old).finished();
          await page.evaluate(() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve))));
          result.after = await page.evaluate(readTrend, { moduleUrl: echartsUrl });
        } else {
        result.after = await (await page.waitForFunction(() => {
          const ec = window.__auditEcharts;
          return [...document.querySelectorAll('[_echarts_instance_]')].map(el => ec.getInstanceByDom(el)?.getOption()?.series?.[0]).find(series => series?.type === 'line' && series?.data?.[0] === 77)?.data;
        })).jsonValue();
        }
        result.selectedYear = await page.locator('.dropdown-wrap .nav-btn').innerText();
        assert.ok(result.selectedYear.includes('2027')); assert.deepEqual(result.before, [33]); assert.deepEqual(result.after, fixed ? [33] : [77]);
      }
    }
    assert.deepEqual(result.errors, []);
    if (name !== 'budget-race') {
      await page.mouse.move(700, 50);
      await page.waitForTimeout(1200); // 等待图表入场动画结束，复核稳定值并保存可读截图。
      result.stableChartData = await page.evaluate(readTrend, { moduleUrl: echartsUrl });
      assert.deepEqual(result.stableChartData, name === 'year-race' ? (fixed ? [33] : [77]) : [10, 15, 12, 25, 20, 45]);
      await page.evaluate(() => {
        for (const el of document.querySelectorAll('[_echarts_instance_]')) {
          const chart = window.__auditEcharts.getInstanceByDom(el);
          if (chart?.getOption()?.series?.[0]?.type === 'line') chart.dispatchAction({ type: 'showTip', seriesIndex: 0, dataIndex: 0 });
        }
      });
    }
    await page.screenshot({ path: path.join(OUTPUT, name + (fixed ? '-fixed' : '') + '.png'), fullPage: true });
    await fs.writeFile(path.join(OUTPUT, name + (fixed ? '-fixed' : '') + '.json'), JSON.stringify(result, null, 2));
    console.log(JSON.stringify({ case: name, fixed, passed: true, errors: result.errors }));
  } catch (error) {
    console.error(JSON.stringify({ result, url: page.url() }));
    await page.screenshot({ path: path.join(OUTPUT, name + '-failure.png'), fullPage: true });
    throw error;
  } finally { release(); await context.close(); await browser.close(); }
}
main().catch(error => { console.error(error); process.exitCode = 1; });
