// Only for a fresh disposable review fixture.
const { chromium } = require('../../target/ui-audit-tools/node_modules/playwright');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');
async function main() {
  const root = path.resolve(__dirname, '../../target/ui-audit');
  assert.match(JSON.parse(await fs.readFile(path.join(root, 'state.json'))).schema, /^biz_review_test_[0-9a-f]{32}$/);
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const result = { rejected: [], errors: [] };
  try {
    const page = await browser.newPage({ viewport: { width: 1440, height: 1000 } });
    await page.route('**/*', route => new URL(route.request().url()).hostname === '127.0.0.1' ? route.continue() : route.abort());
    page.on('pageerror', e => result.errors.push(e.message));
    let requests = 0;
    page.on('request', req => { if (req.method() === 'POST' && req.url().endsWith('/system/password')) requests++; });
    const login = async password => {
      await page.getByPlaceholder('账号', { exact: true }).fill('910004');
      await page.getByPlaceholder('密码', { exact: true }).fill(password);
      await page.getByRole('button', { name: /登\s*录/ }).click();
      await page.waitForURL('**/home/works');
    };
    await page.goto('http://127.0.0.1:15173/login');
    await login('review-fixture-password');
    await page.locator('.user-trigger').click();
    await page.getByRole('menuitem', { name: '修改密码', exact: true }).click();
    const dialog = page.getByRole('dialog', { name: '修改密码', exact: true });
    const password = dialog.getByPlaceholder('请输入新密码', { exact: true });
    const confirm = dialog.getByPlaceholder('请再次输入新密码', { exact: true });
    for (const [name, value, message] of [['empty', '', '请输入新密码'], ['blank', '      ', '请输入新密码'], ['short', '12345', '密码长度至少6位']]) {
      await password.fill(value); await confirm.fill(value);
      await dialog.getByRole('button', { name: '保存', exact: true }).click();
      await dialog.getByText(message, { exact: true }).waitFor();
      assert.equal(requests, 0);
      result.rejected.push(name);
    }
    const output = path.join(root, 'evidence-fix-15');
    await fs.mkdir(output, { recursive: true });
    await page.screenshot({ path: path.join(output, 'password-validation.png'), fullPage: true });
    await password.fill('abc123'); await confirm.fill('abc123');
    await dialog.getByRole('button', { name: '保存', exact: true }).click();
    await page.waitForURL('**/login');
    assert.equal(requests, 1);
    await login('abc123');
    result.validPasswordLogin = true;
    assert.deepEqual(result.errors, []);
    await fs.writeFile(path.join(output, 'password-fixed.json'), JSON.stringify(result, null, 2));
    console.log(JSON.stringify(result));
  } finally { await browser.close(); }
}
main().catch(e => { console.error(e); process.exitCode = 1; });
