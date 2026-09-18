const { chromium } = require('../../target/ui-audit-tools/node_modules/playwright');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');

async function main() {
  const root = path.resolve(__dirname, '../../target/ui-audit');
  const state = JSON.parse(await fs.readFile(path.join(root, 'state.json')));
  assert.match(state.schema, /^biz_review_test_[0-9a-f]{32}$/);
  assert.equal(state.frontend, 'http://127.0.0.1:15273');
  const output = path.join(root, 'evidence-login-password-reset');
  await fs.mkdir(output, { recursive: true });
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  try {
    const context = await browser.newContext({ viewport: { width: 1440, height: 1000 } });
    const page = await context.newPage();
    const errors = [];
    page.on('pageerror', e => errors.push(e.message));
    await page.goto(state.frontend + '/login');
    await page.getByPlaceholder('账号', { exact: true }).fill('910001');
    await page.getByPlaceholder('密码', { exact: true }).fill('previous-login-entry');
    const open = () => page.getByRole('button', { name: '重置密码', exact: true }).click();
    const dialog = page.getByRole('dialog', { name: '重置密码', exact: true });
    const input = name => dialog.getByRole('textbox', { name, exact: true });
    // Password fields have no implicit textbox role.
    const field = name => dialog.locator(`input[aria-label="${name}"]`);
    const submit = () => dialog.getByRole('button', { name: '确认重置', exact: true }).click();
    await open();
    assert.equal(await input('账号').inputValue(), '910001');
    await submit(); await dialog.getByText('请输入原密码', { exact: true }).waitFor();
    await field('原密码').fill('incorrect');
    await field('新密码').fill('Short1A'); await field('确认新密码').fill('Short1A');
    await submit(); await dialog.locator('.el-alert').filter({ hasText: '新密码至少8位' }).waitFor();
    await field('新密码').fill('ResetUi123'); await field('确认新密码').fill('Different123');
    await submit(); await dialog.getByText('两次输入的新密码不一致', { exact: true }).waitFor();
    await field('确认新密码').fill('ResetUi123');
    await submit(); await dialog.getByText('账号或原密码错误', { exact: true }).waitFor();
    assert.equal(new URL(page.url()).pathname, '/login');
    assert.equal(await field('新密码').inputValue(), 'ResetUi123');
    await dialog.getByRole('button', { name: '取消', exact: true }).click();
    await dialog.waitFor({ state: 'hidden' }); await page.waitForTimeout(350);
    await open(); assert.equal(await field('原密码').inputValue(), '');
    assert.equal(await field('新密码').inputValue(), '');
    assert.equal(await dialog.locator('.el-alert').count(), 0);
    await field('原密码').fill('review-fixture-password');
    await field('新密码').fill('ResetUi123'); await field('确认新密码').fill('ResetUi123');
    let requests = 0;
    await page.route('**/api/system/password/reset', async route => {
      requests++;
      await new Promise(resolve => setTimeout(resolve, 500));
      await route.continue();
    });
    await dialog.getByRole('button', { name: '确认重置', exact: true }).evaluate(button => { button.click(); button.click(); });
    await dialog.waitFor({ state: 'hidden' });
    assert.equal(requests, 1);
    assert.equal(await page.getByPlaceholder('密码', { exact: true }).inputValue(), '');
    assert.equal(await page.getByPlaceholder('账号', { exact: true }).inputValue(), '910001');
    assert.equal(await page.evaluate(() => localStorage.getItem('token')), null);
    await page.screenshot({ path: path.join(output, 'reset-success.png'), fullPage: true });
    await page.getByPlaceholder('密码', { exact: true }).fill('ResetUi123');
    await page.getByRole('button', { name: '登 录', exact: true }).click();
    await page.waitForURL('**/home/**');
    await page.getByRole('menuitem', { name: /工作中心/ }).waitFor();
    assert.equal(await page.getByRole('dialog', { name: '请先修改密码', exact: true }).count(), 0);
    // A separate fresh visitor verifies the dialog fits a narrow viewport.
    const mobile = await browser.newContext({ viewport: { width: 390, height: 844 } });
    const narrow = await mobile.newPage(); await narrow.goto(state.frontend + '/login');
    await narrow.getByRole('button', { name: '重置密码', exact: true }).click();
    await narrow.waitForTimeout(400);
    const box = await narrow.locator('.el-dialog').boundingBox();
    assert.ok(box.x >= 0 && box.x + box.width <= 390);
    await narrow.screenshot({ path: path.join(output, 'reset-mobile.png'), fullPage: true });
    assert.deepEqual(errors, []);
    await fs.writeFile(path.join(output, 'results.json'), JSON.stringify({ passed: true,
      checks: ['prefill', 'required', 'weak-password', 'confirmation', 'wrong-original-stays-on-page',
        'close-clears-secrets', 'duplicate-submit', 'real-reset-and-login', 'no-forced-change', 'mobile-width'], errors }, null, 2));
    console.log('Password reset UI: 10 checks passed');
  } finally { await browser.close(); }
}
main().catch(e => { console.error(e); process.exitCode = 1; });
