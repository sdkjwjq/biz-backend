// Run after batch-17-audit.py --fixed in a fresh disposable review fixture.
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
    const page = await browser.newPage({ viewport: { width: 1440, height: 1100 } });
    page.on('pageerror', e => result.errors.push(e.message));
    await page.route('**/*', route => new URL(route.request().url()).hostname === '127.0.0.1' ? route.continue() : route.abort());
    await page.goto('http://127.0.0.1:15173/login');
    await page.getByPlaceholder('账号', { exact: true }).fill('910002');
    await page.getByPlaceholder('密码', { exact: true }).fill('review-fixture-password');
    await page.getByRole('button', { name: /登\s*录/ }).click();
    await page.waitForURL('**/home/works');
    await page.getByRole('menuitem', { name: /消息中心/ }).click();
    await page.locator('.notice-item').filter({ hasText: '审计测试部门B' }).first().waitFor();
    const notices = await page.locator('.notice-item').allTextContents();
    result.departmentA = notices.filter(text => text.includes('审计测试部门A') && text.includes('3 个双高建设任务')).length;
    result.departmentB = notices.filter(text => text.includes('审计测试部门B') && text.includes('2 个双高建设任务')).length;
    assert.equal(result.departmentA, 4);
    assert.equal(result.departmentB, 2);
    await page.locator('.notice-item').filter({ hasText: '审计测试部门B' }).first().click();
    const dialog = page.getByRole('dialog', { name: '消息详情', exact: true });
    await dialog.waitFor();
    result.detail = await dialog.innerText();
    assert.ok(result.detail.includes('审计测试部门B') && result.detail.includes('2 个双高建设任务'));
    const output = path.join(root, 'evidence-batch-17');
    await page.screenshot({ path: path.join(output, 'managed-department-detail.png'), fullPage: true });
    assert.deepEqual(result.errors, []);
    await fs.writeFile(path.join(output, 'notices-fixed.json'), JSON.stringify(result, null, 2));
    console.log(JSON.stringify({ passed: true, departmentA: result.departmentA, departmentB: result.departmentB }));
  } finally { await browser.close(); }
}
main().catch(e => { console.error(e); process.exitCode = 1; });
