const { chromium } = require('../../target/ui-audit-tools/node_modules/playwright');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');

async function main() {
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const results = [];
  try {
    // Run after batch-10-audit.cjs --fixed: B has advanced, A is still actionable.
    for (const kind of ['audit-success', 'audit-failure', 'children', 'unmount']) {
      const context = await browser.newContext({ viewport: { width: 1440, height: 1100 } });
      await context.route('**/*', route => new URL(route.request().url()).hostname === '127.0.0.1' ? route.continue() : route.abort());
      const page = await context.newPage();
      const errors = [];
      page.on('pageerror', error => errors.push(error.message));
      let release;
      const gate = new Promise(resolve => { release = resolve; });
      try {
        await page.goto('http://127.0.0.1:15173/login');
        await page.getByPlaceholder('账号', { exact: true }).fill('910003');
        await page.getByPlaceholder('密码', { exact: true }).fill('review-fixture-password');
        await page.getByRole('button', { name: /登\s*录/ }).click();
        await page.waitForURL('**/home/works');
        await page.getByText('审计专用A一级任务', { exact: true }).waitFor();
        for (const name of ['审计专用A一级任务', '审计专用A二级任务']) {
          const icon = page.locator('.el-table__row').filter({ hasText: name }).locator('.el-table__expand-icon');
          if (!(await icon.getAttribute('class')).includes('expanded')) await icon.click();
        }
        let reached;
        const started = new Promise(resolve => { reached = resolve; });
        const match = kind === 'children' ? '**/api/biz/tasks/forth?parent_id=931001' : '**/api/biz/audit/task/931001';
        await page.route(match, async route => {
          const response = await route.fetch();
          reached();
          await gate;
          if (kind === 'audit-failure') await route.fulfill({ status: 503, json: { message: '合成延迟失败' } });
          else await route.fulfill({ response });
        });
        const open = name => page.locator('.el-table__row').filter({ hasText: name }).getByRole('button', { name: '查看', exact: true }).click();
        const drawer = page.getByRole('dialog', { name: '任务详情与反馈' });
        await open('竞态任务A');
        await started;
        assert.equal(await drawer.getByRole('button', { name: '确认提交', exact: true }).count(), 0);
        await drawer.getByRole('button', { name: /close/i }).click();
        await drawer.waitFor({ state: 'hidden' });
        if (kind === 'unmount') {
          await page.getByRole('menuitem', { name: /消息中心/ }).click();
          await page.waitForURL('**/home/notice');
        } else {
          await open('竞态任务B');
          await drawer.locator('.el-loading-mask').waitFor({ state: 'hidden' });
        }
        const completed = page.waitForResponse(res => kind === 'children'
          ? res.url().includes('/biz/tasks/forth?parent_id=931001') : res.url().includes('/biz/audit/task/931001'));
        release();
        await completed;
        await page.waitForTimeout(500);
        await page.unroute(match);
        if (kind !== 'unmount') {
          assert.ok((await drawer.locator('.task-info-box').innerText()).includes('竞态任务B'));
          assert.equal(await drawer.getByRole('button', { name: '确认提交', exact: true }).count(), 0);
          await drawer.getByRole('button', { name: /close/i }).click();
          await drawer.waitFor({ state: 'hidden' });
          await open('竞态任务A');
          await drawer.locator('.el-loading-mask').waitFor({ state: 'hidden' });
          assert.ok(await drawer.getByRole('button', { name: '确认提交', exact: true }).isEnabled());
        } else assert.equal(await drawer.count(), 0);
        assert.deepEqual(errors, []);
        results.push({ kind, passed: true, errors });
      } finally { release(); await context.close(); }
    }
    const output = path.resolve(__dirname, '../../target/ui-audit/evidence-batch-10/lifecycle.json');
    await fs.writeFile(output, JSON.stringify(results, null, 2));
    console.log(JSON.stringify(results));
  } finally { await browser.close(); }
}
main().catch(error => { console.error(error); process.exitCode = 1; });
