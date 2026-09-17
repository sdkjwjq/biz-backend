const { chromium } = require('../../target/ui-audit-tools/node_modules/playwright');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');
async function main() {
  const root = path.resolve(__dirname, '../../target/ui-audit');
  assert.match(JSON.parse(await fs.readFile(path.join(root, 'state.json'))).schema, /^biz_review_test_[0-9a-f]{32}$/);
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const result = { errors: [] };
  try {
    const page = await browser.newPage({ viewport: { width: 1440, height: 1000 } });
    page.on('pageerror', e => result.errors.push(e.message));
    await page.route('**/*', route => new URL(route.request().url()).hostname === '127.0.0.1' ? route.continue() : route.abort());
    await page.goto('http://127.0.0.1:15173/login');
    await page.getByPlaceholder('账号', { exact: true }).fill('110228');
    await page.getByPlaceholder('密码', { exact: true }).fill('review-fixture-password');
    await page.getByRole('button', { name: /登\s*录/ }).click();
    await page.waitForURL('**/home/works');
    await page.getByText('审计专用A一级任务', { exact: true }).waitFor();
    const expand = async () => {
      for (let i = 0; i < 5; i++) {
        const arrows = page.locator('.el-table__expand-icon:not(.el-table__expand-icon--expanded):visible');
        if (!await arrows.count()) break;
        await arrows.first().click();
      }
    };
    await expand();
    await page.getByText('合成四级汇总任务', { exact: true }).waitFor();
    result.selfParentVisible = await page.getByText('合成层级校验任务', { exact: true }).count();
    assert.equal(result.selfParentVisible, 0);
    const output = path.join(root, 'evidence-batch-16');
    await page.screenshot({ path: path.join(output, 'tree-invalid.png'), fullPage: true });
    result.restored = await page.evaluate(async () => {
      const headers = { Authorization: localStorage.getItem('token'), 'Content-Type': 'application/json' };
      const task = await (await fetch('/api/biz/tasks/936003', { headers })).json();
      for (const key of ['isDelete', 'createTime', 'updateTime']) delete task[key];
      task.parentId = 930001;
      return (await fetch('/api/biz/tasks/manage/update', { method: 'POST', headers, body: JSON.stringify(task) })).text();
    });
    await page.evaluate(() => { const token = localStorage.getItem('token'); localStorage.clear(); localStorage.setItem('token', token); });
    await page.reload();
    await page.getByText('审计专用A一级任务', { exact: true }).waitFor();
    await expand();
    await page.getByText('合成层级校验任务', { exact: true }).waitFor();
    result.validParentVisible = true;
    await page.screenshot({ path: path.join(output, 'tree-restored.png'), fullPage: true });
    assert.deepEqual(result.errors, []);
    await fs.writeFile(path.join(output, 'tree-ui.json'), JSON.stringify(result, null, 2));
    console.log(JSON.stringify(result));
  } finally { await browser.close(); }
}
main().catch(e => { console.error(e); process.exitCode = 1; });
