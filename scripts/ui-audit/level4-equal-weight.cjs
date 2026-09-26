const { chromium, request } = require('../../target/ui-audit-tools/node_modules/playwright');
const { expect } = require('../../target/ui-audit-tools/node_modules/playwright/test');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');

async function login(api, userId) {
  const response = await api.post('/api/system/login', {
    data: { user_id: userId, password: 'review-fixture-password' }
  });
  const body = await response.json();
  assert.ok(body.token, `login failed for ${userId}: ${JSON.stringify(body)}`);
  return body.token;
}

async function main() {
  const root = path.resolve(__dirname, '../../target/ui-audit');
  const state = JSON.parse(await fs.readFile(path.join(root, 'state.json')));
  assert.match(state.schema, /^biz_review_test_[0-9a-f]{32}$/);
  const output = path.join(root, 'evidence-level4-equal-weight');
  await fs.mkdir(output, { recursive: true });
  const api = await request.newContext({ baseURL: state.frontend });
  const ownerToken = await login(api, 910001);
  const auditorToken = await login(api, 910003);
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const result = { checks: [], pageErrors: [] };

  try {
    const owner = await browser.newContext({ viewport: { width: 1440, height: 1000 } });
    await owner.addInitScript(token => localStorage.setItem('token', token), ownerToken);
    const taskPage = await owner.newPage();
    taskPage.on('pageerror', error => result.pageErrors.push(error.message));
    await taskPage.goto(state.frontend + '/home/works?taskId=930002');
    const drawer = taskPage.getByRole('dialog', { name: '任务详情与反馈', exact: true });
    await drawer.waitFor();
    await drawer.locator('.drawer-content > .el-loading-mask').waitFor({ state: 'hidden' });

    await drawer.getByText('实际完成率 150%', { exact: true }).waitFor();
    await drawer.getByText('计入汇总 100%', { exact: true }).first().waitFor();
    await drawer.getByText('实际完成率 50%', { exact: true }).waitFor();
    await drawer.getByText('实际完成率 100%', { exact: true }).waitFor();
    await drawer.getByText(/预计三级任务完成率：83\.33%/).waitFor();
    result.checks.push('页面展示150%实际完成率、100%计入上限及83.33%等权预览');

    const firstInput = drawer.getByRole('spinbutton').first();
    await firstInput.fill('5');
    await drawer.getByText(/预计三级任务完成率：66\.67%/).waitFor();
    await firstInput.fill('15');
    await drawer.getByText(/预计三级任务完成率：83\.33%/).waitFor();
    result.checks.push('输入变化实时刷新等权预览');
    await taskPage.screenshot({ path: path.join(output, 'task-preview.png'), fullPage: true });
    await owner.close();

    const submit = await api.post('/api/biz/sub', {
      headers: { Authorization: ownerToken },
      data: {
        third_task_id: 930002,
        file_id: 961100,
        comment: '四级等权验收',
        sub_list: [
          { task_id: 961001, reported_value: 15, data_type: '1' },
          { task_id: 961002, reported_value: 50, data_type: '2' },
          { task_id: 961003, reported_value: 1, data_type: '1' }
        ]
      }
    });
    assert.equal(submit.status(), 200, await submit.text());
    const todoResponse = await api.get('/api/biz/audit/todo', { headers: { Authorization: auditorToken } });
    const todo = await todoResponse.json();
    const audit = todo.find(item => Number(item.taskId ?? item.task_id) === 930002);
    assert.ok(audit, `task audit not found: ${JSON.stringify(todo)}`);
    const subId = Number(audit.subId ?? audit.sub_id);

    const auditor = await browser.newContext({ viewport: { width: 1440, height: 1000 } });
    await auditor.addInitScript(token => localStorage.setItem('token', token), auditorToken);
    const auditPage = await auditor.newPage();
    auditPage.on('pageerror', error => result.pageErrors.push(error.message));
    await auditPage.goto(state.frontend + '/home/audit?taskId=930002');
    const dialog = auditPage.getByRole('dialog', { name: /业务审批|详情查看/ });
    await dialog.waitFor();
    const rows = dialog.locator('.level4-audit-table .el-table__body tr');
    await expect(rows).toHaveCount(3);
    assert.match(await rows.nth(0).innerText(), /15\s+10\s+150%\s+100%/);
    assert.match(await rows.nth(1).innerText(), /50\s+100\s+50%\s+50%/);
    assert.match(await rows.nth(2).innerText(), /1\s+1\s+100%\s+100%/);
    result.checks.push('审核详情逐项展示填报值、目标值、实际完成率和计入完成率');
    await auditPage.screenshot({ path: path.join(output, 'audit-detail.png'), fullPage: true });
    await auditor.close();

    const task = await (await api.get('/api/biz/tasks/930002', {
      headers: { Authorization: ownerToken }
    })).json();
    assert.equal(Number(task.progress), 83);
    assert.equal(Number(task.currentValue).toFixed(4), '8.3333');
    const children = await (await api.get('/api/biz/tasks/forth?parent_id=930002', {
      headers: { Authorization: ownerToken }
    })).json();
    assert.equal(Number(children[0].actualProgress), 150);
    assert.equal(Number(children[0].countedProgress), 100);
    result.checks.push('真实提交后后端保存父任务83%与8.3333，并返回150%/100%双进度');
    assert.deepEqual(result.pageErrors, []);
    await fs.writeFile(path.join(output, 'results.json'), JSON.stringify(result, null, 2));
    console.log(JSON.stringify(result, null, 2));
  } finally {
    await browser.close();
    await api.dispose();
  }
}

main().catch(error => { console.error(error); process.exitCode = 1; });
