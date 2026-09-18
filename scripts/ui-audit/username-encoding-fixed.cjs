// Frontend regression with intercepted synthetic API responses; never accesses business data.
const { chromium } = require('../../target/ui-audit-tools/node_modules/playwright');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');
async function main() {
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const output = path.resolve(__dirname, '../../target/username-encoding');
  await fs.mkdir(output, { recursive: true });
  const results = [], errors = [];
  const reproduce = process.argv.includes('--reproduce');
  try {
    for (const required of [true, false]) {
      for (const username of ['徐忠杰', '张老师', 'AuditAdmin']) {
        const context = await browser.newContext({ viewport: { width: 1440, height: 1000 } });
        const token = 'fixture.' + Buffer.from(JSON.stringify({ id: 999001, username, role: '0' })).toString('base64url') + '.synthetic';
        await context.addInitScript(value => localStorage.setItem('token', value), token);
        await context.route('**/*', route => {
          const url = new URL(route.request().url());
          if (url.hostname !== '127.0.0.1') return route.abort();
          if (url.pathname.startsWith('/api/')) {
            const json = url.pathname === '/api/system/password/status' ? { requiresPasswordChange: required } : [];
            return route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(json) });
          }
          return route.continue();
        });
        const page = await context.newPage();
        page.on('pageerror', error => errors.push(error.message));
        await page.goto('http://127.0.0.1:5173/home/works');
        await page.waitForFunction(() => document.querySelector('.user-name')?.textContent?.trim());
        const name = page.locator('.user-name');
        if (reproduce && username !== 'AuditAdmin') {
          await page.waitForFunction(() => document.querySelector('.user-name')?.textContent?.trim() !== '用户');
          assert.notEqual((await name.innerText()).trim(), username);
        } else {
          await name.getByText(username, { exact: true }).waitFor();
          assert.equal((await name.innerText()).trim(), username);
        }
        const dialog = page.getByRole('dialog', { name: '请先修改密码', exact: true });
        if (required) await dialog.waitFor();
        else assert.equal(await dialog.isVisible(), false);
        results.push({ username, required, displayed: (await name.innerText()).trim() });
        if (required && username === '徐忠杰') await page.screenshot({ path: path.join(output, reproduce ? 'before.png' : 'after.png'), fullPage: true });
        await context.close();
      }
    }
    assert.deepEqual(errors, []);
    await fs.writeFile(path.join(output, reproduce ? 'before.json' : 'after.json'), JSON.stringify({ results, errors }, null, 2));
    console.log(JSON.stringify({ cases: results.length, reproduce, errors }));
  } finally { await browser.close(); }
}
main().catch(error => { console.error(error); process.exitCode = 1; });
