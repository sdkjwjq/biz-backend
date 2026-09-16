const { chromium } = require('../../target/ui-audit-tools/node_modules/playwright');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');
const output = path.resolve(__dirname, '../../target/ui-audit/evidence-batch-13');

async function main() {
  const kind = process.argv[2];
  assert(['related-upload', 'withdraw-context', 'manual-submit'].includes(kind));
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const context = await browser.newContext({ viewport: { width: 1440, height: 1100 }, reducedMotion: 'reduce' });
  await context.route('**/*', route => new URL(route.request().url()).hostname === '127.0.0.1' ? route.continue() : route.abort());
  const page = await context.newPage();
  const result = { kind, errors: [] };
  page.on('pageerror', error => result.errors.push(error.message));
  let release = () => {};
  const hold = async pattern => {
    let reached;
    const started = new Promise(resolve => { reached = resolve; });
    const gate = new Promise(resolve => { release = resolve; });
    await page.route(pattern, async route => {
      const response = await route.fetch(); reached(); await gate; await route.fulfill({ response });
    });
    return started;
  };
  const shot = suffix => page.screenshot({ path: path.join(output, `${kind}-${suffix}.png`), fullPage: true });
  await fs.mkdir(output, { recursive: true });
  try {
    await page.goto('http://127.0.0.1:15173/login');
    await page.getByPlaceholder('账号', { exact: true }).fill('910001');
    await page.getByPlaceholder('密码', { exact: true }).fill('review-fixture-password');
    await page.getByRole('button', { name: /登\s*录/ }).click();
    await page.waitForURL('**/home/works');
    await page.getByText('审计专用A一级任务', { exact: true }).waitFor();
    if (kind === 'manual-submit' && process.argv.includes('--reset-manual')) {
      await page.evaluate(async () => {
        const api = await import('/src/api/performance.js');
        const records = await api.getPerformanceAuditsByPerfAndYear(950031, 2026);
        const pending = records.find(row => Number(row.flowStatus) === 10 && Number(row.submitBy) === 910001);
        if (pending) await api.withdrawPerformanceAudit(pending.subId);
      });
    }
    if (kind === 'withdraw-context') {
      for (const name of ['审计专用A一级任务', '审计专用A二级任务']) {
        const icon = page.locator('.el-table__row').filter({ hasText: name }).locator('.el-table__expand-icon');
        if (!(await icon.getAttribute('class')).includes('expanded')) await icon.click();
      }
      const open = name => page.locator('.el-table__row').filter({ hasText: name }).getByRole('button', { name: '查看', exact: true }).click();
      const drawer = page.getByRole('dialog', { name: '任务详情与反馈' });
      await open('填报竞态A');
      await drawer.locator('.el-loading-mask').waitFor({ state: 'hidden' });
      const started = hold('**/api/biz/drawback/**');
      await drawer.getByRole('button', { name: '撤回提交', exact: true }).click();
      await page.getByRole('button', { name: '确认撤回', exact: true }).click();
      await started;
      await drawer.getByRole('button', { name: /close/i }).click();
      await drawer.waitFor({ state: 'hidden' });
      await open('填报竞态B');
      await drawer.locator('.el-loading-mask').waitFor({ state: 'hidden' });
      await drawer.getByPlaceholder('请输入完成情况（50字以内）').fill('B撤回期间的新草稿');
      result.before = await drawer.locator('.task-info-box').innerText();
      await shot('before');
      release();
      await page.getByText('已撤回提交', { exact: true }).waitFor();
      await page.waitForFunction(() => [...document.querySelectorAll('.el-drawer.open .task-info-box')].some(el => el.textContent.includes('填报竞态A')));
      result.after = await drawer.locator('.task-info-box').innerText();
      result.draftAfter = await drawer.getByPlaceholder('请输入完成情况（50字以内）').inputValue();
      assert.ok(result.before.includes('填报竞态B'));
      assert.ok(result.after.includes('填报竞态A'));
      assert.equal(result.draftAfter, '');
      await shot('after');
    } else {
      await page.getByRole('menuitem', { name: '绩效', exact: true }).click();
      await page.getByText(kind === 'manual-submit' ? '社会效益指标' : '数量指标', { exact: true }).click();
      const detail = page.getByRole('dialog', { name: '绩效指标详情', exact: true });
      if (kind === 'manual-submit') {
        await page.getByText('审计待填绩效C', { exact: true }).click();
        await detail.getByRole('spinbutton').fill('7');
        const started = hold('**/api/performance/submit');
        await detail.getByRole('button', { name: '提交填报', exact: true }).click();
        await page.getByRole('button', { name: '确认提交', exact: true }).click();
        await started;
        await detail.getByRole('button', { name: /close/i }).click();
        await detail.waitFor({ state: 'hidden' });
        await page.getByText('审计待填绩效D', { exact: true }).click();
        await detail.getByRole('spinbutton').fill('9');
        result.before = await detail.locator('.task-info-box').innerText();
        await shot('before');
        release();
        await page.getByText('提交成功，已进入绩效审核流程', { exact: true }).waitFor();
        await page.waitForFunction(() => [...document.querySelectorAll('.el-drawer.open .info-row')].some(el => el.textContent.includes('当前完成值') && /7/.test(el.textContent)));
        await page.waitForTimeout(700);
        result.after = await detail.locator('.task-info-box').innerText();
        result.inputAfter = await detail.getByRole('spinbutton').inputValue();
        result.yearData = await page.evaluate(async () => {
          const api = await import('/src/api/performance.js');
          return (await api.getPerformanceByYear(2026)).filter(row => [950031,950041].includes(Number(row.perfId)));
        });
        assert.ok(result.after.includes('审计待填绩效D'));
        assert.ok(result.after.includes('7'));
        assert.equal(Number(result.yearData.find(row => Number(row.perfId) === 950041).currentValue), 0);
        await shot('after');
      } else {
        await page.getByText('审计自动绩效', { exact: true }).click();
        await detail.getByText('填报竞态A', { exact: true }).waitFor();
        const open = name => detail.locator('.el-table__row').filter({ hasText: name }).getByRole('button', { name: '查看', exact: true }).click();
        const task = page.locator('.el-drawer.open').filter({ hasText: '任务详情与反馈' });
        await open('填报竞态A');
        await task.getByRole('spinbutton').waitFor();
        await task.locator('.el-loading-mask').waitFor({ state: 'hidden' });
        await task.getByRole('spinbutton').fill('3');
        await task.locator('input[type=file]').setInputFiles({ name: 'batch13.pdf', mimeType: 'application/pdf', buffer: Buffer.from('%PDF-1.4\n% Synthetic audit fixture\n%%EOF') });
        const started = hold('**/api/system/upload/**');
        await task.getByRole('button', { name: '提交反馈', exact: true }).click();
        await started;
        await task.getByRole('button', { name: '返回绩效指标详情', exact: true }).click();
        await detail.waitFor();
        await open('填报竞态B');
        await task.getByRole('spinbutton').waitFor();
        await task.locator('.el-loading-mask').waitFor({ state: 'hidden' });
        result.bValue = await task.getByRole('spinbutton').inputValue();
        await shot('before');
        const posted = page.waitForRequest(req => req.method() === 'POST' && new URL(req.url()).pathname === '/api/biz/sub');
        release();
        result.payload = (await posted).postDataJSON();
        await page.getByText('材料已提交（已进入审核流程）', { exact: true }).waitFor();
        result.saved = await page.evaluate(async () => {
          const api = await import('/src/api/audit.js'); return api.getAuditByTaskId(932001);
        });
        assert.equal(Number(result.payload.task_id), 932001);
        assert.equal(Number(result.payload.reported_value), 7);
        assert.equal(Number(result.saved[0].reportedValue), 7);
      }
    }
    assert.deepEqual(result.errors, []);
    await fs.writeFile(path.join(output, kind + '.json'), JSON.stringify(result, null, 2));
    console.log(JSON.stringify({ kind, reproduced: true }));
  } catch (error) { console.error(await page.locator('body').innerText()); throw error;
  } finally { release(); await context.close(); await browser.close(); }
}
main().catch(error => { console.error(error); process.exitCode = 1; });
