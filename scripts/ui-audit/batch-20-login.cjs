// Run after batch-20-audit.py in the disposable review fixture.
const { chromium } = require('../../target/ui-audit-tools/node_modules/playwright');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');

async function main() {
  const state = JSON.parse(await fs.readFile(path.resolve(__dirname, '../../target/ui-audit/state.json')));
  assert.match(state.schema, /^biz_review_test_[0-9a-f]{32}$/);
  const output = path.resolve(__dirname, '../../target/ui-audit/evidence-batch-20');
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  try {
    const page = await browser.newPage();
    const errors = [];
    const results = [];
    let requests = 0;
    page.on('pageerror', error => errors.push(error.message));
    page.on('request', request => {
      if (request.method() === 'POST' && request.url().endsWith('/system/login')) requests++;
    });
    await page.route('**/*', route => new URL(route.request().url()).hostname === '127.0.0.1' ? route.continue() : route.abort());
    for (const [userId, password] of [[920201, ''], [920202, '      ']]) {
      await page.goto('http://127.0.0.1:15173/login');
      await page.getByPlaceholder('账号', { exact: true }).fill(String(userId));
      await page.getByPlaceholder('密码', { exact: true }).fill(password);
      await page.getByRole('button', { name: /登\s*录/ }).click();
      if (password === '') {
        await page.getByText('请输入密码', { exact: true }).waitFor();
        assert.equal(requests, 0);
      } else {
        await page.waitForURL('**/home/**');
        assert.equal(requests, 1);
      }
      await page.screenshot({ path: path.join(output, `login-${userId}.png`), fullPage: true });
      results.push({ userId, browserMessage: password === '' ? '请输入密码' : '登录成功', loginRequests: requests });
    }
    assert.deepEqual(errors, []);
    await fs.writeFile(path.join(output, 'browser.json'), JSON.stringify({ results, errors }, null, 2) + '\n');
    console.log(JSON.stringify({ verified: results.length, errors }));
  } finally {
    await browser.close();
  }
}
main().catch(error => { console.error(error); process.exitCode = 1; });
