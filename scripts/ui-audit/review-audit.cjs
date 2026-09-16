const { chromium } = require('../../target/ui-audit-tools/node_modules/playwright');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');
const BASE = 'http://127.0.0.1:15173';
const OUTPUT = path.resolve(__dirname, '../../target/ui-audit/evidence-review');

async function main() {
  const name = process.argv[2];
  assert(['pagination', 'history-tab', 'achievement-count', 'achievement-validation'].includes(name));
  const isAchievement = name.startsWith('achievement-');
  const isValidation = name === 'achievement-validation';
  await fs.mkdir(OUTPUT, { recursive: true });
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const context = await browser.newContext({ viewport: { width: 1440, height: 1100 }, reducedMotion: 'reduce' });
  await context.route('**/*', route => new URL(route.request().url()).hostname === '127.0.0.1' ? route.continue() : route.abort());
  const page = await context.newPage();
  const result = { case: name, errors: [], requests: [] };
  page.on('pageerror', error => result.errors.push(error.message));
  page.on('request', request => {
    const pathname = new URL(request.url()).pathname;
    if (pathname.startsWith('/api/')) result.requests.push({ method: request.method(), path: pathname });
  });
  const screenshot = filename => page.screenshot({ path: path.join(OUTPUT, filename), fullPage: true });
  const responseFor = pathname => page.waitForResponse(response => new URL(response.url()).pathname === pathname);
  const select = async (dialog, label, option) => {
    await dialog.locator('.el-form-item').filter({ has: page.locator('.el-form-item__label', { hasText: label }) }).locator('.el-select').click();
    await page.getByRole('option', { name: option, exact: true }).click();
  };
  try {
    await page.goto(BASE + '/login');
    const user = isAchievement ? '1910001' : name === 'history-tab' ? '910001' : '910003';
    await page.getByPlaceholder('账号', { exact: true }).fill(user);
    await page.getByPlaceholder('密码', { exact: true }).fill('review-fixture-password');
    await page.getByRole('button', { name: /登\s*录/ }).click();
    if (isAchievement) {
      await page.waitForURL('**/home/works/achievement');
      await page.locator('.table-wrapper .el-loading-mask').waitFor({ state: 'hidden' });
      await page.getByRole('button', { name: '新增', exact: true }).click();
      const dialog = page.getByRole('dialog', { name: '新增成果', exact: true });
      if (isValidation) {
        await dialog.getByRole('button', { name: '提交审核', exact: true }).click();
        await dialog.getByText('请输入建设成果名称', { exact: true }).waitFor();
        result.emptyFormErrors = await dialog.locator('.el-form-item__error').allTextContents();
        result.addRequestsBeforeValidForm = result.requests.filter(item => item.path === '/api/achievement/add').length;
        assert.ok(result.emptyFormErrors.length >= 5);
        assert.equal(result.addRequestsBeforeValidForm, 0);
        await dialog.locator('input[type=file]').setInputFiles({ name: 'audit-invalid.txt', mimeType: 'text/plain', buffer: Buffer.from('synthetic audit text') });
        await page.getByText('只能上传 PDF、DOC 或 DOCX 格式的文件！', { exact: true }).waitFor();
        result.invalidFileRejected = true;
        await dialog.locator('.el-upload-list__item').waitFor({ state: 'hidden' });
      }
      await select(dialog, '成果类别', '1.落实立德树人根本任务');
      await select(dialog, '级别', '省级');
      const achievementName = isValidation ? '审计整数数量对照' : '审计小数奖项数量';
      await dialog.getByPlaceholder('请输入建设成果名称', { exact: true }).fill(achievementName);
      await dialog.getByPlaceholder('如：教育部办公厅等').fill('审计测试单位');
      await dialog.getByPlaceholder('选择颁发时间').fill('2026年09月16日 10:00:00');
      await dialog.getByPlaceholder('选择颁发时间').press('Tab');
      await dialog.getByPlaceholder('请输入具体内容（项目名称、荣誉名称、奖项名称等）').fill('用于验证奖项数量是否被静默截断');
      await dialog.getByRole('button', { name: '添加等次及数量' }).click();
      await dialog.locator('.grade-select').click();
      await page.getByRole('option', { name: '一等奖', exact: true }).click();
      const count = dialog.getByRole('spinbutton');
      await count.fill(isValidation ? '2' : '1.5');
      await count.press('Tab');
      result.visibleCountBeforeSubmit = await count.inputValue();
      await screenshot(name + '-before.png');
      if (!isValidation) {
        await dialog.locator('input[type=file]').setInputFiles({ name: 'quantity-regression.pdf', mimeType: 'application/pdf', buffer: Buffer.from('%PDF-1.4\n% synthetic quantity test\n%%EOF') });
        const before = result.requests.filter(item => item.method === 'POST').length;
        await dialog.getByRole('button', { name: '提交审核', exact: true }).click();
        await page.getByText('成果数量必须为 1～999 的整数，请修改后再提交', { exact: true }).waitFor();
        assert.equal(await count.inputValue(), '1.5');
        assert.equal(result.requests.filter(item => item.method === 'POST').length, before);
        result.fractionRejectedBeforeUpload = true;
        await screenshot(name + '-rejected.png');
        await count.fill('2');
        await count.press('Tab');
      }
      const addRequest = page.waitForRequest(request => new URL(request.url()).pathname === '/api/achievement/add');
      const addResponse = responseFor('/api/achievement/add');
      const listResponse = responseFor('/api/achievement/');
      await dialog.getByRole('button', { name: '提交审核', exact: true }).click();
      const payload = (await addRequest).postDataJSON();
      result.sentCount = payload.yiDengJiang;
      result.addResponse = await (await addResponse).text();
      const savedId = Number(result.addResponse.match(/成果ID：(\d+)/)?.[1]);
      assert.ok(savedId > 0);
      const saved = (await (await listResponse).json()).find(row => Number(row.achId) === savedId);
      result.savedCount = saved?.yiDengJiang;
      result.savedAchievementId = saved?.achId;
      await dialog.waitFor({ state: 'hidden' });
      assert.equal(result.visibleCountBeforeSubmit, isValidation ? '2' : '1.5');
      assert.equal(result.sentCount, 2);
      assert.equal(result.savedCount, 2);
      await page.locator('.el-table__body tr').filter({ hasText: '1.落实立德树人根本任务' }).locator('.el-table__expand-icon').click();
      const savedRow = page.locator('.el-table__body tr').filter({ hasText: achievementName }).last();
      await savedRow.waitFor();
      result.visibleSavedRow = await savedRow.innerText();
      assert.ok(result.visibleSavedRow.includes('一等奖×2'));
      await screenshot(name + '-after.png');
    } else {
      await page.waitForURL('**/home/works');
      await page.getByText('审计专用A一级任务', { exact: true }).waitFor();
      const todoResponse = responseFor('/api/performance/audit/todo');
      await page.getByRole('menuitem', { name: /审核中心/ }).click();
      const todo = await (await todoResponse).json();
      await page.locator('.audit-container .el-loading-mask').waitFor({ state: 'hidden' });
      if (name === 'pagination') {
        result.apiTodoCount = todo.length;
        const rows = page.locator('.audit-container .el-table__body tr');
        result.page1Rows = await rows.allTextContents();
        await page.locator('.el-pager li').filter({ hasText: /^2$/ }).click();
        result.activePage = await page.locator('.el-pager li.is-active').innerText();
        result.page2Rows = await rows.allTextContents();
        assert.equal(result.apiTodoCount, 15);
        assert.equal(result.page1Rows.length, 10);
        assert.equal(result.activePage, '2');
        assert.equal(result.page2Rows.length, 5);
        const names = text => text.replace(/^\d+/, '').trim();
        assert.equal(new Set([...result.page1Rows, ...result.page2Rows].map(names)).size, 15);
        await screenshot('audit-page2.png');
      } else {
        result.initialTodoCount = todo.length;
        const start = result.requests.length;
        const recordsResponse = responseFor('/api/performance/audit/records');
        await page.getByText('审批记录', { exact: true }).click();
        result.apiHistory = (await (await recordsResponse).json()).map(row => ({ subId: row.subId, perfName: row.perfName, flowStatus: row.flowStatus }));
        await page.getByText('已归档历史绩效', { exact: true }).waitFor();
        await page.locator('.audit-container .el-loading-mask').waitFor({ state: 'hidden' });
        result.rowsOnSwitch = await page.locator('.audit-container .el-table__body tr').count();
        result.recordRequestsOnSwitch = result.requests.slice(start).filter(item => item.path.endsWith('/audit/records'));
        assert.equal(result.recordRequestsOnSwitch.length, 2);
        assert.ok(result.apiHistory.some(row => Number(row.subId) === 972099));
        assert.equal(result.rowsOnSwitch, 1);
        assert.equal(await page.locator('.header-left .el-tag').count(), 0);
        await screenshot('history-auto-loaded.png');
        const todoAgain = responseFor('/api/performance/audit/todo');
        await page.getByText('待我审批', { exact: true }).click();
        await (await todoAgain).finished();
        await page.locator('.audit-container .el-loading-mask').waitFor({ state: 'hidden' });
        assert.equal(await page.locator('.audit-container .el-table__body tr').count(), 0);
        result.archivedRowsInTodo = 0;
      }
    }
    await fs.writeFile(path.join(OUTPUT, name + '.json'), JSON.stringify(result, null, 2));
    console.log(JSON.stringify(result, null, 2));
  } catch (error) {
    console.error(JSON.stringify({ url: page.url(), text: await page.locator('body').innerText(), errors: result.errors }));
    await screenshot(name + '-failure.png');
    throw error;
  } finally {
    await context.close();
    await browser.close();
  }
}
main().catch(error => { console.error(error); process.exitCode = 1; });
