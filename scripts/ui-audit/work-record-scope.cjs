// Requires serve.py --fixture work-record-scope; all users and writes are synthetic.
const { chromium, request } = require('../../target/ui-audit-tools/node_modules/playwright');
const { expect } = require('../../target/ui-audit-tools/node_modules/playwright/test');
const fs = require('node:fs/promises'), path = require('node:path'), assert = require('node:assert/strict');
const { spawnSync } = require('node:child_process');
(async () => {
  const state = JSON.parse(await fs.readFile(path.resolve(__dirname, '../../target/ui-audit/state.json')));
  assert.match(state.schema, /^biz_review_test_[0-9a-f]{32}$/);
  const output = path.resolve(__dirname, '../../target/ui-audit/work-record-scope'); await fs.mkdir(output, { recursive: true });
  const api = await request.newContext({ baseURL: state.frontend }), browser = await chromium.launch({ channel: 'msedge', headless: true });
  const contexts = [], errors = [], checks = [], tokens = new Map(); let page;
  const done = name => { checks.push(name); console.log('PASS ' + name); };
  async function token(user) {
    if (!tokens.has(user)) {
      const response = await api.post('/api/system/login', { data: { user_id: user, password: 'review-fixture-password' } });
      const result = await response.json(); assert.ok(result.token, JSON.stringify(result)); tokens.set(user, result.token);
    }
    return tokens.get(user);
  }
  async function call(user, method, url, data) {
    return api[method]('/api' + url, { headers: { Authorization: await token(user) }, data });
  }
  async function body(response) {
    const result = await response.json(); assert.equal(response.status(), 200, JSON.stringify(result));
    assert.ok(!(result.code >= 400), JSON.stringify(result)); return result;
  }
  async function tab(user) {
    const value = await token(user), context = await browser.newContext({ viewport: { width: 1440, height: 960 }, storageState: {
      cookies: [], origins: [{ origin: state.frontend, localStorage: [{ name: 'token', value }] }]
    } }); contexts.push(context);
    const result = await context.newPage(); result.setDefaultTimeout(15000); result.on('pageerror', error => errors.push(error.message));
    await result.goto(state.frontend + '/home/work-records?year=2026&month=2'); return result;
  }
  const pick = async (target, name, text) => {
    await target.getByRole('combobox', { name, exact: true }).locator('xpath=ancestor::div[contains(concat(" ",normalize-space(@class)," ")," el-select ")][1]').click();
    await page.getByRole('option', { name: text, exact: true }).click();
  };
  const taskIds = stats => stats.tasks.map(row => row.taskId).sort((a, b) => a - b);
  try {
    const all = [930002, 930003, 930102, 930202, 930302, 930303];
    for (const user of [910001, 910004, 910021]) {
      const stats = await body(await call(user, 'get', '/work-records/statistics?year=2026&month=2'));
      assert.deepEqual(taskIds(stats), all); assert.equal(stats.reformTasks.length, 4); assert.match(stats.notice, /专业群/);
    }
    const expected = new Map([[910011, [930002, 930003, 930302, 930303]], [910012, [930002, 930003, 930102]], [910014, [930202, 930302, 930303]], [910018, [930202]], [910003, [930002, 930003, 930302, 930303]]]);
    for (const [user, ids] of expected) {
      const stats = await body(await call(user, 'get', '/work-records/statistics?year=2026&month=2'));
      assert.deepEqual(taskIds(stats), ids); assert.equal(stats.totalTasks, ids.length); assert.match(stats.notice, /归口部门/);
    }
    done('real-scope-l3-l4-professionals-schoolwide-and-departments-expand-complete-roots');

    for (const user of [910013, 910017]) {
      const access = await body(await call(user, 'get', '/work-records/capabilities')); assert.equal(access.canCreate, false);
      assert.equal((await call(user, 'post', '/work-records', { year: 2026, month: 2 })).status(), 403);
    }
    const core = await body(await call(910011, 'get', '/core-indicators/capabilities')); assert.equal(core.canFill, false);
    page = await tab(910017); await expect(page.getByRole('button', { name: '新建纪实', exact: true })).toHaveCount(0);
    done('view-only-leadership-and-unrelated-department-do-not-grant-filling-or-core-permission');

    const old = await body(await call(910011, 'get', '/work-records/990101')); assert.equal(old.statistics.totalTasks, 1);
    page = await tab(910011); await page.getByRole('button', { name: '新建纪实', exact: true }).click();
    let dialog = page.getByRole('dialog', { name: '新建工作纪实', exact: true });
    await pick(dialog, '新建纪实月份', '2月'); await dialog.getByRole('button', { name: '确定', exact: true }).click();
    await expect(page.locator('.task-summary-table .el-table__body .el-table__row')).toHaveCount(4);
    await expect(page.locator('.task-summary-table')).toContainText('一级A的部门B任务');
    await expect(page.locator('.task-summary-table')).toContainText('一级D的部门C兄弟任务');
    await expect(page.locator('.task-summary-table')).not.toContainText('一级B的部门B任务');
    const select = page.getByRole('combobox', { name: '选择改革任务', exact: true });
    await select.locator('xpath=ancestor::div[contains(concat(" ",normalize-space(@class)," ")," el-select ")][1]').click();
    assert.deepEqual((await page.getByRole('option').allTextContents()).sort(), ['审计专用A一级任务', '纪实一级任务D'].sort());
    await page.getByRole('option', { name: '纪实一级任务D', exact: true }).click();
    await page.getByRole('button', { name: '添加任务', exact: true }).click();
    await page.getByRole('textbox', { name: '关键进展', exact: true }).fill('部门A填写四级归口命中的整个一级任务D');
    let saves = 0;
    await page.route('**/api/work-records/*/save', async route => { saves++; await new Promise(resolve => setTimeout(resolve, 300)); await route.continue(); });
    await page.getByRole('button', { name: '保存草稿', exact: true }).evaluate(element => { element.click(); element.click(); });
    await expect(page.locator('.detail-actions')).toContainText('内容已保存'); assert.equal(saves, 1); await page.unroute('**/api/work-records/*/save');
    const list = await body(await call(910011, 'get', '/work-records?year=2026&month=2'));
    const id = list.records[0].recordId, saved = await body(await call(910011, 'get', '/work-records/' + id));
    assert.equal(saved.statistics.totalTasks, 4); assert.equal(saved.entries[0].reformTaskId, 930300);
    done('department-browser-full-root-details-select-add-real-save-and-repeat-click-guard');

    const denied = await call(910011, 'post', '/work-records/' + id + '/save', { version: saved.record.version, entries: [{ reformTaskId: 930100, keyProgress: '越权一级任务' }] });
    assert.equal(denied.status(), 400);
    const unchanged = await body(await call(910011, 'get', '/work-records/' + id)); assert.equal(unchanged.record.version, saved.record.version);
    assert.deepEqual(unchanged.entries, saved.entries);
    const other = await body(await call(910012, 'post', '/work-records', { year: 2026, month: 2 }));
    assert.equal((await call(910011, 'get', '/work-records/' + other.record.recordId)).status(), 404);
    done('forged-root-and-other-department-draft-are-rejected-without-writes');

    await page.getByRole('button', { name: '提交纪实', exact: true }).click();
    await page.getByRole('dialog', { name: '提交工作纪实', exact: true }).getByRole('button', { name: '确认提交', exact: true }).click();
    await expect(page.getByText('工作纪实已提交', { exact: true })).toBeVisible();
    await expect(page.getByRole('button', { name: '保存草稿', exact: true })).toHaveCount(0);
    await expect(page.getByText('展示提交时保存的任务范围和统计结果，后续任务或权限调整不改变本份纪实。', { exact: true })).toBeVisible();
    await page.screenshot({ path: path.join(output, 'department-submitted-roots.png'), fullPage: true });
    const submitted = await body(await call(910011, 'get', '/work-records/' + id)); assert.equal(submitted.record.status, 1); assert.equal(submitted.statistics.totalTasks, 4);
    done('real-submit-freezes-four-task-scope-and-becomes-readonly');

    const adminRows = await body(await call(110228, 'get', '/admin/users?keyword=910011'));
    const user = adminRows.items.find(row => row.userId === 910011); assert.ok(user);
    await body(await call(110228, 'put', '/admin/users/910011', { userName: user.userName, nickName: user.nickName, email: user.email || '', role: user.role, deptId: 920004, expectedUpdateTime: user.updateTime, reason: '合成数据：核验部门调整后的资格与冻结快照' }));
    assert.equal((await body(await call(910011, 'get', '/work-records/capabilities'))).canCreate, false);
    const afterTransfer = await body(await call(910011, 'get', '/work-records/' + id)); assert.deepEqual(afterTransfer.statistics, submitted.statistics);
    const legacy = await body(await call(910011, 'get', '/work-records/990101')); assert.deepEqual(legacy.statistics, old.statistics); assert.deepEqual(legacy.entries, old.entries);
    await page.reload(); await expect(page.getByRole('button', { name: '新建纪实', exact: true })).toHaveCount(0);
    done('department-change-revokes-new-filling-with-old-token-and-retains-own-submitted-history');

    const download = await call(110228, 'post', '/work-records/export', { ids: [990101] }); assert.equal(download.status(), 200);
    const filename = path.join(output, 'legacy-frozen.docx'); await fs.writeFile(filename, await download.body());
    const verify = spawnSync('python', ['-c', 'import sys,zipfile; d=zipfile.ZipFile(sys.argv[1]).read("word/document.xml").decode("utf-8"); assert "旧版冻结统计：本人负责的任务，共1条。" in d; assert "历史冻结进展" in d; assert "一级D的部门C兄弟任务" not in d', filename], { encoding: 'utf8' });
    assert.equal(verify.status, 0, verify.stderr);
    done('legacy-word-export-retains-original-one-task-snapshot');

    const professional = await tab(910004); page = professional;
    await professional.getByRole('button', { name: '新建纪实', exact: true }).click(); dialog = professional.getByRole('dialog', { name: '新建工作纪实', exact: true });
    await pick(dialog, '新建纪实月份', '2月'); await dialog.getByRole('button', { name: '确定', exact: true }).click();
    await expect(professional.locator('.task-summary-table .el-table__body .el-table__row')).toHaveCount(6);
    await expect(professional.locator('.task-summary-table')).toContainText('一级C的部门C任务');
    await professional.screenshot({ path: path.join(output, 'professional-schoolwide.png'), fullPage: true });
    done('fourth-level-only-responsible-user-can-fill-all-school-roots-in-browser');
    assert.deepEqual(errors, []);
    await fs.writeFile(path.join(output, 'result.json'), JSON.stringify({ checks, pageErrors: errors }, null, 2));
  } catch (error) { if (page) await page.screenshot({ path: path.join(output, 'failure.png'), fullPage: true }); throw error; }
  finally { for (const context of contexts) await context.close(); await api.dispose(); await browser.close(); }
})().catch(error => { console.error(error); process.exitCode = 1; });
