const { chromium } = require('../../target/ui-audit-tools/node_modules/playwright');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');
const output = path.resolve(__dirname, '../../target/ui-audit/evidence-batch-14');
const fixed = process.argv.includes('--fixed');
const stay = process.argv.includes('--stay');
const fail = process.argv.includes('--fail');

async function main() {
  const kind = process.argv[2];
  assert(['achievement-approval', 'achievement-upload', 'report-draft'].includes(kind));
  assert(!fail || (fixed && kind === 'report-draft'), '--fail requires report-draft --fixed');
  assert(!stay || (fixed && kind !== 'achievement-approval'), '--stay requires a fixed form scenario');
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const context = await browser.newContext({ viewport: { width: 1440, height: 1100 }, reducedMotion: 'reduce' });
  await context.route('**/*', route => new URL(route.request().url()).hostname === '127.0.0.1' ? route.continue() : route.abort());
  const page = await context.newPage();
  const result = { kind, errors: [] };
  page.on('pageerror', error => result.errors.push(error.message));
  let release = () => {};
  const hold = pattern => {
    let reached;
    const started = new Promise(resolve => { reached = resolve; });
    const gate = new Promise(resolve => { release = resolve; });
    const ready = page.route(pattern, async route => {
      if (fail && kind === 'report-draft' && route.request().method() !== 'POST') return route.continue();
      const response = fail ? null : await route.fetch();
      reached(); await gate;
      if (fail) await route.fulfill({ status: 503, contentType: 'application/json', body: JSON.stringify({ message: 'Synthetic failure' }) });
      else await route.fulfill({ response });
    });
    return { ready, started };
  };
  const shot = suffix => page.screenshot({ path: path.join(output, `${kind}-${suffix}.png`), fullPage: true });
  await fs.mkdir(output, { recursive: true });
  try {
    await page.goto('http://127.0.0.1:15173/login');
    await page.getByPlaceholder('账号', { exact: true }).fill(kind === 'report-draft' ? '110228' : '1910001');
    await page.getByPlaceholder('密码', { exact: true }).fill('review-fixture-password');
    await page.getByRole('button', { name: /登\s*录/ }).click();
    if (kind === 'report-draft') {
      await page.waitForURL('**/home/works');
      await page.getByText('审计专用A一级任务', { exact: true }).waitFor();
    } else await page.waitForURL('**/home/works/achievement');
    if (kind === 'report-draft') {
      await page.getByRole('menuitem', { name: /督办中心/ }).click();
      await page.locator('.report-form .el-select').click();
      await page.getByRole('option', { name: '仅用户A可见的三级任务', exact: true }).click();
      const title = page.getByPlaceholder('请输入简要说明 (如：关于xxx任务的阶段性材料)');
      const content = page.getByPlaceholder('请输入督办要求和整改建议...');
      await title.fill('第一条合成督办'); await content.fill('第一条合成内容');
      const held = hold('**/api/system/notice'); await held.ready;
      const posted = page.waitForRequest(req => req.method() === 'POST' && new URL(req.url()).pathname === '/api/system/notice');
      await page.getByRole('button', { name: '发送督办', exact: true }).click();
      await held.started; result.payload = (await posted).postDataJSON();
      if (!stay) {
      await page.getByRole('button', { name: '重置', exact: true }).click();
      await page.locator('.report-form .el-select').click();
      await page.getByRole('option', { name: '仅用户A可见的三级任务', exact: true }).click();
      await title.fill('第二条尚未发送的草稿'); await content.fill('不能丢失的新内容');
      }
      result.before = { title: await title.inputValue(), content: await content.inputValue() };
      await shot('before'); release();
      if (!fail) await page.getByText('督办信息已发送', { exact: true }).waitFor();
      await page.waitForFunction(() => !document.querySelector('.report-form .el-button.is-loading'));
      result.after = { title: await title.inputValue(), content: await content.inputValue() };
      assert.equal(result.payload.title, '第一条合成督办');
      assert.equal(result.after.title, fixed && (!stay || fail) ? result.before.title : '');
      assert.equal(result.after.content, fixed && (!stay || fail) ? result.before.content : '');
      await shot('after');
    } else {
      await page.evaluate(async () => { window.achievementApi = await import('/src/api/achievement.js'); });
      if (kind === 'achievement-approval') {
        result.seedResponses = await page.evaluate(async () => {
          const results = [];
          const existing = await window.achievementApi.getAllAchievements();
          for (const name of ['成果竞态A', '成果竞态B']) {
            if (existing.some(row => row.achName === name)) continue;
            results.push(await window.achievementApi.addAchievement({
            category: 1, level: '省级', achName: name, department: '合成颁发单位',
            gotTime: '2026-09-16 10:00:00', comment: '合成审批测试', isCompetition: 1, yiDengJiang: 2, createBy: 1910001
          }));
          }
          return results;
        });
        result.achievements = await page.evaluate(async () => (await window.achievementApi.getAllAchievements()).filter(row => ['成果竞态A','成果竞态B'].includes(row.achName)));
        assert.equal(result.achievements.length, 2);
        await page.evaluate(() => localStorage.removeItem('token'));
        await page.goto('http://127.0.0.1:15173/login');
        await page.getByPlaceholder('账号', { exact: true }).fill('110228');
        await page.getByPlaceholder('密码', { exact: true }).fill('review-fixture-password');
        await page.getByRole('button', { name: /登\s*录/ }).click();
        await page.waitForURL('**/home/works');
        await page.getByText('审计专用A一级任务', { exact: true }).waitFor();
      }
      if (kind === 'achievement-approval') {
        await page.getByRole('button', { name: '成果', exact: true }).click();
        await page.evaluate(async () => { window.achievementApi = await import('/src/api/achievement.js'); });
      }
      await page.locator('.table-wrapper .el-loading-mask').waitFor({ state: 'hidden' });
      if (kind === 'achievement-approval') {
        await page.locator('.el-table__body tr').filter({ hasText: '1.落实立德树人根本任务' }).locator('.el-table__expand-icon').click();
        const a = result.achievements.find(row => row.achName === '成果竞态A');
        const b = result.achievements.find(row => row.achName === '成果竞态B');
        const open = name => page.locator('.el-table__body tr').filter({ hasText: name }).getByRole('button', { name: '审核', exact: true }).click();
        const drawer = page.getByRole('dialog', { name: '成果审核', exact: true });
        const held = hold(`**/api/achievement/audit/achievement/${a.achId}`); await held.ready;
        await open(a.achName); await held.started;
        await drawer.getByRole('button', { name: /close/i }).click();
        await drawer.waitFor({ state: 'hidden' });
        await open(b.achName);
        await drawer.getByRole('button', { name: '提交审核', exact: true }).waitFor();
        const old = page.waitForResponse(res => res.url().endsWith(`/achievement/audit/achievement/${a.achId}`));
        release(); await old; await page.waitForTimeout(500);
        result.visible = await drawer.locator('.el-form-item').filter({ hasText: '建设成果名称' }).locator('input').inputValue();
        assert.equal(result.visible, '成果竞态B');
        await shot('before');
        const posted = page.waitForRequest(req => req.method() === 'POST' && new URL(req.url()).pathname === '/api/achievement/audit');
        await drawer.getByRole('button', { name: '提交审核', exact: true }).click();
        result.payload = (await posted).postDataJSON();
        await page.getByText('成果已归档', { exact: true }).waitFor();
        await page.unroute(`**/api/achievement/audit/achievement/${a.achId}`);
        result.saved = await page.evaluate(async ids => Promise.all(ids.map(id => window.achievementApi.getAchievementAuditsByAchId(id))), [a.achId,b.achId]);
        assert.equal(Number(result.payload.sub_id), Number(result.saved[fixed ? 1 : 0][0].subId));
        assert.equal(Number(result.saved[0][0].flowStatus), fixed ? 10 : 30);
        assert.equal(Number(result.saved[1][0].flowStatus), fixed ? 30 : 10);
      } else {
        const dialog = page.getByRole('dialog', { name: '新增成果', exact: true });
        const fill = async (name, count) => {
          await dialog.locator('.el-radio').filter({ has: page.getByText('是竞赛', { exact: true }) }).click();
          await dialog.locator('.el-form-item').filter({ hasText: '成果类别' }).locator('.el-select').click();
          await page.getByRole('option', { name: '1.落实立德树人根本任务', exact: true }).click();
          await dialog.locator('.el-form-item').filter({ hasText: '级别' }).locator('.el-select').click();
          await page.getByRole('option', { name: '省级', exact: true }).click();
          await dialog.getByPlaceholder('请输入建设成果名称', { exact: true }).fill(name);
          await dialog.getByPlaceholder('如：教育部办公厅等').fill('合成颁发单位');
          await dialog.getByPlaceholder('选择颁发时间').fill('2026年09月16日 10:00:00');
          await dialog.getByPlaceholder('选择颁发时间').press('Tab');
          await dialog.getByPlaceholder('请输入具体内容（项目名称、荣誉名称、奖项名称等）').fill(name + '内容');
          await dialog.getByRole('button', { name: '添加等次及数量' }).click();
          await dialog.locator('.grade-select').click();
          await page.getByRole('option', { name: '一等奖', exact: true }).click();
          await dialog.getByRole('spinbutton').fill(String(count));
        };
        await page.getByRole('button', { name: '新增', exact: true }).click();
        await fill('正在提交的成果C', 2);
        await dialog.locator('input[type=file]').setInputFiles({ name: 'achievement-c.pdf', mimeType: 'application/pdf', buffer: Buffer.from('%PDF-1.4\n% Synthetic achievement C\n%%EOF') });
        const held = hold('**/api/system/upload/0'); await held.ready;
        await dialog.getByRole('button', { name: '提交审核', exact: true }).click();
        await held.started;
        if (!stay) {
        await dialog.getByRole('button', { name: '取消', exact: true }).click();
        await dialog.waitFor({ state: 'hidden' });
        await page.getByRole('button', { name: '新增', exact: true }).click();
        await fill('尚未提交的成果D', 5);
        }
        await shot('before');
        const posted = page.waitForRequest(req => req.method() === 'POST' && new URL(req.url()).pathname === '/api/achievement/add');
        const completed = page.waitForResponse(res => res.request().method() === 'POST' && new URL(res.url()).pathname === '/api/achievement/add');
        release(); result.payload = (await posted).postDataJSON();
        await completed;
        if (!fixed) await page.getByText('成果已提交，待管理员归档审核', { exact: true }).waitFor();
        result.saved = await page.evaluate(async () => (await window.achievementApi.getAllAchievements()).filter(row => ['正在提交的成果C','尚未提交的成果D'].includes(row.achName)));
        result.saved = result.saved.filter(row => Number(row.fileId) === Number(result.payload.fileId));
        assert.equal(result.payload.achName, fixed ? '正在提交的成果C' : '尚未提交的成果D');
        assert.equal(result.saved.length, 1);
        assert.equal(result.saved[0].achName, fixed ? '正在提交的成果C' : '尚未提交的成果D');
        if (fixed) assert.equal(Number(result.saved[0].yiDengJiang), 2);
        else assert.notEqual(Number(result.saved[0].yiDengJiang), 5);
        assert.ok(result.saved[0].fileId);
        if (fixed && !stay) {
          assert.ok(await dialog.isVisible());
          assert.equal(await dialog.getByPlaceholder('请输入建设成果名称', { exact: true }).inputValue(), '尚未提交的成果D');
          assert.equal(await dialog.getByRole('spinbutton').inputValue(), '5');
        } else await dialog.waitFor({ state: 'hidden' });
        await shot('after');
      }
    }
    assert.deepEqual(result.errors, []);
    await fs.writeFile(path.join(output, kind + (fixed ? '-fixed' : '') + (stay ? '-stay' : '') + (fail ? '-fail' : '') + '.json'), JSON.stringify(result, null, 2));
    console.log(JSON.stringify({ kind, fixed, stay, fail, passed: true }));
  } catch (error) { console.error(JSON.stringify({ result, text: await page.locator('body').innerText() })); throw error;
  } finally { release(); await context.close(); await browser.close(); }
}
main().catch(error => { console.error(error); process.exitCode = 1; });
