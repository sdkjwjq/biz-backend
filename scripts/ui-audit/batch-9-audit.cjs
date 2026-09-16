const { chromium } = require('../../target/ui-audit-tools/node_modules/playwright');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');
const OUTPUT = path.resolve(__dirname, '../../target/ui-audit/evidence-batch-9');

async function main() {
  const name = process.argv[2];
  assert(['archived-link', 'task-link-and-sender'].includes(name));
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const context = await browser.newContext({ viewport: { width: 1440, height: 1100 }, reducedMotion: 'reduce' });
  await context.route('**/*', route => new URL(route.request().url()).hostname === '127.0.0.1' ? route.continue() : route.abort());
  const page = await context.newPage();
  const result = { case: name, errors: [], routes: [] };
  page.on('pageerror', error => result.errors.push(error.message));
  page.on('framenavigated', frame => { if (frame === page.mainFrame()) result.routes.push(new URL(frame.url()).pathname + new URL(frame.url()).search); });
  const screenshot = suffix => page.screenshot({ path: path.join(OUTPUT, name + '-' + suffix + '.png'), fullPage: true });
  await fs.mkdir(OUTPUT, { recursive: true });
  try {
    await page.goto('http://127.0.0.1:15173/login');
    await page.getByPlaceholder('账号', { exact: true }).fill('910001');
    await page.getByPlaceholder('密码', { exact: true }).fill('review-fixture-password');
    await page.getByRole('button', { name: /登\s*录/ }).click();
    await page.waitForURL('**/home/works');
    await page.getByText('审计专用A一级任务', { exact: true }).waitFor();
    const notices = page.waitForResponse(res => new URL(res.url()).pathname === '/api/system/notice');
    await page.getByRole('menuitem', { name: /消息中心/ }).click();
    const rows = await (await notices).json();
    const id = name === 'archived-link' ? 989101 : 989102;
    result.notice = rows.find(row => Number(row.noticeId ?? row.notice_id) === id);
    assert.ok(result.notice);
    const title = result.notice.title;
    await page.locator('.notice-item').filter({ hasText: title }).click();
    const dialog = page.getByRole('dialog', { name: '消息详情', exact: true });
    await dialog.waitFor();
    await page.waitForTimeout(350); // 等待弹窗过渡完成，使截图中的发送人和正文清晰可读。
    if (name === 'task-link-and-sender') {
      result.senderLabel = await dialog.locator('.sender').innerText();
      assert.equal(Number(result.notice.fromUserId), 910003);
      assert.ok(result.senderLabel.includes('系统管理员'));
      assert.equal(String(result.notice.sourceType), '1');
      assert.equal(Number(result.notice.sourceId), 930002);
      await screenshot('sender');
    }
    await dialog.getByRole('button', { name: /前往处理业务/ }).click();
    const message = name === 'archived-link'
      ? '该绩效暂无与您相关的审批单（可能已流转给下一位或不在您的待办中）'
      : '该成果暂无与您相关的审批单（可能已处理或不在您的待办中）';
    await page.getByText(message, { exact: true }).waitFor();
    result.jumpMessage = message;
    result.openApprovalDialogs = await page.getByRole('dialog', { name: '业务审批', exact: true }).count();
    assert.equal(result.openApprovalDialogs, 0);
    await screenshot('failed-jump');
    if (name === 'archived-link') {
      const records = page.waitForResponse(res => new URL(res.url()).pathname === '/api/performance/audit/records');
      await page.getByText('审批记录', { exact: true }).click();
      result.availableHistory = (await (await records).json()).map(row => ({ subId: row.subId, perfName: row.perfName, flowStatus: row.flowStatus }));
      assert.ok(result.availableHistory.some(row => Number(row.subId) === 972099));
      await page.getByText('已归档历史绩效', { exact: true }).waitFor();
      await screenshot('history-exists');
    } else {
      assert.ok(result.routes.some(route => route.includes('type=achievement') && route.includes('930002')));
    }
    assert.deepEqual(result.errors, []);
    await fs.writeFile(path.join(OUTPUT, name + '.json'), JSON.stringify(result, null, 2));
    console.log(JSON.stringify({ case: name, reproduced: true, errors: result.errors }));
  } catch (error) {
    console.error(JSON.stringify({ result, text: await page.locator('body').innerText() }));
    await screenshot('failure'); throw error;
  } finally { await context.close(); await browser.close(); }
}
main().catch(error => { console.error(error); process.exitCode = 1; });
