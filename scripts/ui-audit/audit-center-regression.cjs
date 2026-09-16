const { chromium } = require('../../target/ui-audit-tools/node_modules/playwright');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');
const BASE = 'http://127.0.0.1:15173';
const OUTPUT = path.resolve(__dirname, '../../target/ui-audit/evidence-audit-center');

async function main() {
  const name = process.argv[2];
  assert(['tab-race', 'filters', 'last-page', 'deep-link', 'value-contract'].includes(name));
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const context = await browser.newContext({ viewport: { width: 1440, height: 1100 }, reducedMotion: 'reduce' });
  await context.route('**/*', route => new URL(route.request().url()).hostname === '127.0.0.1' ? route.continue() : route.abort());
  const page = await context.newPage();
  const result = { case: name, errors: [], checks: [] };
  page.on('pageerror', error => result.errors.push(error.message));
  await fs.mkdir(OUTPUT, { recursive: true });
  let release = () => {};
  const frames = () => page.evaluate(() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve))));
  const responseFor = pathname => page.waitForResponse(response => new URL(response.url()).pathname === pathname);
  const settle = async () => { await frames(); await page.locator('.audit-container .el-loading-mask').waitFor({ state: 'hidden' }); };
  const rows = page.locator('.audit-container .el-table__body tr');
  const search = page.getByPlaceholder('搜索任务名称、提交人...');
  const refresh = async () => {
    const response = responseFor('/api/performance/audit/todo');
    await page.locator('.filter-right button').click(); await (await response).finished(); await settle();
  };
  try {
    await page.goto(BASE + '/login');
    await page.getByPlaceholder('账号', { exact: true }).fill('910003');
    await page.getByPlaceholder('密码', { exact: true }).fill('review-fixture-password');
    await page.getByRole('button', { name: /登\s*录/ }).click();
    await page.waitForURL('**/home/works');
    await page.getByText('审计专用A一级任务', { exact: true }).waitFor();
    const initial = responseFor('/api/performance/audit/todo');
    await page.getByRole('menuitem', { name: /审核中心/ }).click();
    await (await initial).finished(); await settle();
    if (name === 'tab-race') {
      const gate = new Promise(resolve => { release = resolve; });
      const entered = page.waitForRequest(request => new URL(request.url()).pathname === '/api/performance/audit/records');
      await page.route('**/api/performance/audit/records', async route => { await gate; await route.continue(); });
      await page.getByText('审批记录', { exact: true }).click(); await entered;
      const todo = responseFor('/api/performance/audit/todo');
      await page.getByText('待我审批', { exact: true }).click(); await (await todo).finished(); await settle();
      const before = await rows.allTextContents();
      const old = responseFor('/api/performance/audit/records'); release(); await (await old).finished(); await frames();
      assert.deepEqual(await rows.allTextContents(), before);
      assert.ok((await page.locator('.audit-container .header-left').innerText()).includes('15 待处理'));
      result.checks.push('历史慢响应不覆盖当前待办，待办数量保持 15');
    } else if (name === 'filters') {
      await page.locator('.el-pager li').filter({ hasText: /^2$/ }).click();
      await search.fill('审计零值绩效B'); await frames();
      assert.equal(await rows.count(), 1);
      assert.equal((await page.locator('.el-pager li.is-active').innerText()).trim(), '1');
      assert.ok((await page.locator('.el-pagination__total').innerText()).includes('1'));
      await search.fill('没有匹配的合成名称'); await frames(); assert.equal(await rows.count(), 0);
      await search.fill(''); await frames(); assert.equal(await rows.count(), 10);
      await page.locator('.filter-left .el-select').click(); await page.getByRole('option', { name: '成果归档', exact: true }).click(); await frames();
      assert.equal(await rows.count(), 0);
      await page.locator('.filter-left .el-select').click(); await page.getByRole('option', { name: '绩效填报', exact: true }).click(); await frames();
      assert.equal(await rows.count(), 10);
      assert.ok((await page.locator('.audit-container .header-left').innerText()).includes('15 待处理'));
      result.checks.push('搜索与类型筛选按结果分页并回到第一页，空结果和待办统计正确');
    } else if (name === 'deep-link') {
      await page.goto(BASE + '/home/audit?type=performance&subId=972013');
      const dialog = page.getByRole('dialog', { name: '业务审批', exact: true });
      await dialog.waitFor();
      assert.ok((await dialog.innerText()).includes('972013'));
      result.checks.push('通知链接能够打开第一页之外的审核单 972013');
    } else if (name === 'last-page') {
      result.approvedIds = [];
      // 每次审批真实的最后一页记录；剩 10 条时应自动回到第一页。
      for (let i = 0; i < 5; i += 1) {
        await page.locator('.el-pager li').filter({ hasText: /^2$/ }).click();
        await rows.last().getByRole('button', { name: '审批', exact: true }).click();
        const request = page.waitForRequest(req => new URL(req.url()).pathname === '/api/performance/audit' && req.method() === 'POST');
        const response = responseFor('/api/performance/audit');
        const reload = responseFor('/api/performance/audit/todo');
        await page.getByRole('dialog', { name: '业务审批' }).getByRole('button', { name: '确认提交', exact: true }).click();
        result.approvedIds.push((await request).postDataJSON().sub_id);
        assert.ok((await (await response).text()).includes('已通过专业群审核'));
        await (await reload).finished(); await settle();
      }
      assert.equal(await rows.count(), 10);
      assert.equal((await page.locator('.el-pager li.is-active').innerText()).trim(), '1');
      assert.ok((await page.locator('.audit-container .header-left').innerText()).includes('10 待处理'));
      result.checks.push('真实审批末页 5 条后自动回首页，待办和总数均为 10');
    } else {
      // 补充前端契约测试：明确标记响应注入，覆盖旧接口的字符串零及缺失字段。
      result.responseInjected = true;
      const values = [0, '0', null, '', undefined, 3];
      const expected = ['0', '0', '-', '-', '-', '3'];
      for (let i = 0; i < values.length; i += 1) {
        await page.route('**/api/biz/audit/todo', route => route.fulfill({ json: [{
          subId: 979000 + i, taskId: 930002, submitBy: 910001, flowStatus: 10, reportedValue: values[i]
        }] }));
        await refresh();
        const row = rows.filter({ hasText: '仅用户A可见的三级任务' });
        assert.equal((await row.locator('td').nth(4).innerText()).trim(), expected[i]);
        await row.getByRole('button', { name: '审批', exact: true }).click();
        const dialog = page.getByRole('dialog', { name: '业务审批' });
        assert.equal((await dialog.locator('.highlight-value').innerText()).trim(), expected[i]);
        await dialog.getByRole('button', { name: '关闭', exact: true }).click(); await dialog.waitFor({ state: 'hidden' });
        await page.unroute('**/api/biz/audit/todo');
      }
      result.checks.push('任务列表与弹窗：数值零、字符串零、null、空字符串、缺省和非零值均正确');
    }
    assert.deepEqual(result.errors, []);
    await page.screenshot({ path: path.join(OUTPUT, name + '.png'), fullPage: true });
    await fs.writeFile(path.join(OUTPUT, name + '.json'), JSON.stringify(result, null, 2));
    console.log(JSON.stringify(result, null, 2));
  } catch (error) {
    console.error(JSON.stringify({ url: page.url(), text: await page.locator('body').innerText(), result }));
    await page.screenshot({ path: path.join(OUTPUT, name + '-failure.png'), fullPage: true });
    throw error;
  } finally { release(); await context.close(); await browser.close(); }
}
main().catch(error => { console.error(error); process.exitCode = 1; });
