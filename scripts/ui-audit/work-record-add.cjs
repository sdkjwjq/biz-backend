const { chromium, request } = require('../../target/ui-audit-tools/node_modules/playwright');
const { expect } = require('../../target/ui-audit-tools/node_modules/playwright/test');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');
const { execFileSync } = require('node:child_process');
async function main() {
  const root = path.resolve(__dirname, '../../target/ui-audit');
  const state = JSON.parse(await fs.readFile(path.join(root, 'state.json')));
  assert.match(state.schema, /^biz_review_test_[0-9a-f]{32}$/);
  assert.ok(process.env.SHUANGGAO_TEST_DB_PASSWORD);
  // Only the disposable schema receives this second reform/task hierarchy.
  const mysql = process.env.MYSQL_EXE || 'C:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe';
  execFileSync(mysql, ['--host=127.0.0.1', '--user=root', '--default-character-set=utf8mb4', state.schema], {
    env: { ...process.env, MYSQL_PWD: process.env.SHUANGGAO_TEST_DB_PASSWORD },
    input: "INSERT INTO biz_task(task_id,project_id,parent_id,phase,task_code,task_name,level,leader_id,auditor_id,principal_id,dept_id,data_type,target_value,current_value,status,is_delete) VALUES(930100,1,0,2026,'2','Second reform',1,910001,910003,910002,920001,'1',10,0,'1',0),(930101,1,930100,2026,'2.1','Second directory',2,910001,910003,910002,920001,'1',10,0,'1',0),(930102,1,930101,2026,'2.1.1','Second task',3,910001,910003,910002,920001,'1',10,0,'1',0);"
  });
  const out = path.join(root, 'evidence-work-record-add'); await fs.mkdir(out, { recursive: true });
  const api = await request.newContext({ baseURL: state.frontend });
  const login = async id => (await (await api.post('/api/system/login', { data: { user_id: id, password: 'WorkRecords123' } })).json()).token;
  const token = await login(910003), headers = { Authorization: token };
  const old = await (await api.post('/api/work-records', { headers, data: { year: 2026, month: 3 } })).json();
  const id = old.record.recordId;
  const saved = await api.post(`/api/work-records/${id}/save`, { headers, data: { version: old.record.version, entries: [{ reformTaskId: 930000, keyProgress: 'Original saved entry' }] } });
  assert.equal(saved.status(), 200);
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const context = await browser.newContext({ viewport: { width: 1440, height: 960 } });
  await context.addInitScript(t => localStorage.setItem('token', t), token);
  const page = await context.newPage(); page.setDefaultTimeout(15000);
  const checks = [], errors = []; page.on('pageerror', e => errors.push(e.message));
  const pass = name => { checks.push(name); console.log(name); };
  const button = name => page.getByRole('button', { name, exact: true });
  const picker = page.locator('.reform-picker .el-select');
  try {
    await page.goto(state.frontend + '/home/work-records');
    await page.getByRole('row').filter({ hasText: '2026年3月' }).getByRole('button', { name: '打开草稿' }).click();
    await expect(page.getByRole('textbox', { name: '关键进展', exact: true })).toHaveValue('Original saved entry');
    await expect(button('添加任务')).toBeDisabled();
    await picker.click(); await expect(page.getByRole('option', { name: '审计专用A一级任务', exact: true })).toHaveAttribute('aria-disabled', 'true');
    await page.getByRole('option', { name: 'Second reform', exact: true }).click();
    await expect(page.locator('.reform-entry')).toHaveCount(1);
    await button('添加任务').evaluate(b => { b.click(); b.click(); });
    await expect(page.locator('.reform-entry')).toHaveCount(2); await expect(button('添加任务')).toBeDisabled();
    pass('existing-draft-preserved-select-then-add-and-prevent-duplicates');
    const second = page.locator('.reform-entry').nth(1);
    await second.getByRole('textbox', { name: '关键进展', exact: true }).fill('Second progress');
    await second.getByRole('textbox', { name: '阶段性成果', exact: true }).fill('Second result');
    await second.getByRole('textbox', { name: '典型做法', exact: true }).fill('Second practice');
    await second.getByRole('button', { name: '移除任务' }).click();
    const warning = page.getByRole('dialog', { name: '移除填报项目' });
    await warning.getByRole('button', { name: '取消', exact: true }).click();
    await expect(second.getByRole('textbox', { name: '关键进展', exact: true })).toHaveValue('Second progress');
    await button('保存草稿').click(); await expect(page.locator('.detail-actions')).toContainText('内容已保存');
    await button('返回列表').first().click();
    await page.getByRole('row').filter({ hasText: '2026年3月' }).getByRole('button', { name: '打开草稿' }).click();
    await expect(page.locator('.reform-entry')).toHaveCount(2);
    await expect(second.getByRole('textbox', { name: '阶段性成果', exact: true })).toHaveValue('Second result');
    pass('multiple-entries-save-reopen-and-remove-cancel');
    await page.setViewportSize({ width: 390, height: 844 }); await button('添加任务').scrollIntoViewIfNeeded();
    const box = await page.locator('.reform-picker').boundingBox(); assert.ok(box.x >= 0 && box.x + box.width <= 390);
    await page.screenshot({ path: path.join(out, 'mobile-add.png'), fullPage: true });
    await page.setViewportSize({ width: 1440, height: 960 });
    await button('提交纪实').click(); await page.getByRole('dialog').getByRole('button', { name: '确认提交', exact: true }).click();
    await expect(button('添加任务')).toHaveCount(0); await expect(button('移除任务')).toHaveCount(0);
    const detail = await (await api.get(`/api/work-records/${id}`, { headers })).json();
    assert.deepEqual(detail.entries.map(e => e.reformTaskId), [930000, 930100]);
    const exported = await api.post('/api/work-records/export', { headers: { Authorization: await login(110228) }, data: { ids: [id] } });
    assert.equal(exported.status(), 200); const word = path.join(out, 'multiple-entries.docx'); await fs.writeFile(word, await exported.body());
    execFileSync('python', ['-c', 'import zipfile,sys; s=zipfile.ZipFile(sys.argv[1]).read("word/document.xml").decode(); assert all(x in s for x in ["Original saved entry","Second reform","Second progress","Second result","Second practice"]); assert s.index("Original saved entry") < s.index("Second progress")', word]);
    pass('mobile-layout-submitted-readonly-and-real-word-contains-both-entries-in-order');
    assert.deepEqual(errors, []);
  } finally {
    await fs.writeFile(path.join(out, 'results.json'), JSON.stringify({ checks, errors }, null, 2));
    await browser.close(); await api.dispose();
  }
}
main().catch(error => { console.error(error); process.exitCode = 1; });
