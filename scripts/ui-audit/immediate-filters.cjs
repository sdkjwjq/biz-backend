const { chromium, request } = require('playwright'), { expect } = require('playwright/test');
const fs = require('node:fs/promises'), path = require('node:path'), assert = require('node:assert/strict');
(async () => {
  const state = JSON.parse(await fs.readFile(process.env.SHUANGGAO_UI_STATE_FILE, 'utf8'));
  const out = path.join(path.dirname(process.env.SHUANGGAO_UI_STATE_FILE), 'filter-evidence');await fs.mkdir(out, { recursive: true });
  const browser = await chromium.launch({ channel: 'msedge', headless: true }), api = await request.newContext({ baseURL: state.frontend });
  const checks = [], errors = [], calls = [];let p;
  const call = async (method, url, token, data) => { const r = await api[method]('/api' + url, { headers: token ? { Authorization: token } : {}, ...(data === undefined ? {} : { data }) });assert.equal(r.status(), 200, await r.text());return r.json(); };
  const urlOf = r => new URL(r.url()), listPath = '/api/list-filters/tasks';
  const field = (bar, label, advanced = false) => bar.locator(advanced ? '.advanced-groups .query-field' : '.basic-fields > .query-field').filter({ has: p.locator('label').filter({ hasText: new RegExp('^' + label.replace(/[.*+?^${}()|[\]\\]/g, '\\$&') + '$') }) });
  const waitQuery = (pathname, key, value) => p.waitForResponse(r => urlOf(r).pathname === pathname && (key == null || urlOf(r).searchParams.getAll(key).includes(value)));
  async function choose(control, label) { await control.locator('.el-select').click();const options = p.locator('.el-select-dropdown__item:visible');const option = label ? options.filter({ hasText: new RegExp('^' + label + '$') }).first() : options.filter({ hasNot: p.locator('.is-disabled') }).first();await expect(option).toBeVisible();await option.click(); }
  try {
    let token = (await call('post', '/system/login', null, { user_id: 110228, password: 'review-fixture-password' })).token;
    token = (await call('post', '/system/review-context', token, { experience: 'BUSINESS' })).token;
    const context = await browser.newContext({ viewport: { width: 1440, height: 900 } });
    await context.addInitScript(t => { if (!sessionStorage.getItem('filter-seed')) { localStorage.setItem('token', t);sessionStorage.setItem('filter-seed', '1'); } }, token);
    p = await context.newPage();p.setDefaultTimeout(25000);p.on('pageerror', e => errors.push(e.message));
    p.on('request', r => { if (urlOf(r).pathname === listPath) calls.push(urlOf(r)); });
    let failOptions=true;
    await p.route('**/api/list-filters/tasks/filter-options?*',route=>{if(failOptions){failOptions=false;return route.fulfill({status:500,contentType:'application/json',body:JSON.stringify({code:500,message:'Synthetic option failure'})});}return route.continue();});
    await p.goto(state.frontend + '/home/works');
    await p.getByRole('button',{name:'重试选项',exact:true}).click();
    await expect(p.locator('.query-filter-bar .applied-tags')).toContainText('2026');
    await p.unroute('**/api/list-filters/tasks/filter-options?*');
    checks.push('Failed option definitions can retry without losing restored basic conditions');
    const bar = p.locator('.query-filter-bar[aria-label="任务筛选"]');
    await expect(field(bar, '任务状态')).toBeVisible();await expect(bar.getByRole('button', { name: '查询', exact: true })).toHaveCount(0);
    let waiting = waitQuery(listPath, 'f.deptId', '920001');await choose(field(bar, '归口部门'), '审计测试部门A');await waiting;
    await expect(field(bar, '归口部门').locator('.el-tag')).toHaveCount(0);assert.equal(calls.at(-1).searchParams.getAll('f.deptId').length, 1);
    checks.push('Basic department is single-select and applies without a query button');
    await bar.getByRole('button', { name: /高级筛选/ }).click();
    const many = field(bar, '归口部门（多选）', true);let before = calls.length;
    await choose(many, '审计测试部门B');await p.locator('.project-title').click();await p.waitForTimeout(450);assert.equal(calls.length, before);
    waiting = p.waitForResponse(r => urlOf(r).pathname === listPath && urlOf(r).searchParams.getAll('f.deptId').length === 2);
    await bar.getByRole('button', { name: '应用高级筛选', exact: true }).click();await waiting;await expect(bar.locator('.applied-tags')).toContainText('审计测试部门B');
    checks.push('Advanced multi-department remains pending until Apply and sends repeated values');
    const progress = field(bar, '完成进度（%）', true);await progress.locator('input').first().fill('80');
    waiting = waitQuery(listPath, 'f.status', '1');await choose(field(bar, '任务状态'), '进行中');const basicResponse = await waiting;
    assert.equal(urlOf(basicResponse).searchParams.has('f.progressFrom'), false);await expect(bar.getByText(/高级条件已修改/)).toBeVisible();
    waiting = waitQuery(listPath, 'f.progressFrom', '80');await bar.getByRole('button', { name: '应用高级筛选', exact: true }).click();await waiting;
    checks.push('Changing a basic field never applies pending advanced progress conditions');
    waiting = waitQuery(listPath, 'f.deptId', '920002');await choose(field(bar, '归口部门'), '审计测试部门B');await waiting;
    assert.equal(calls.at(-1).searchParams.getAll('f.deptId').length, 1);
    waiting = p.waitForResponse(r => urlOf(r).pathname === listPath && !urlOf(r).searchParams.has('f.progressFrom'));
    await bar.getByRole('button', { name: '清空高级条件', exact: true }).click();await waiting;assert.equal(calls.at(-1).searchParams.get('f.deptId'), '920002');
    checks.push('Basic selection replaces a multi scope; clearing advanced preserves single basic filters');
    const keyword = field(bar, '编号／名称').locator('input');before = calls.length;
    await keyword.fill('测');await keyword.fill('测试');await keyword.fill('测试任务');
    await waitQuery(listPath, 'f.keyword', '测试任务');await p.waitForTimeout(400);assert.equal(calls.length, before + 1);
    waiting = waitQuery(listPath, 'f.keyword', '回车');await keyword.fill('回车');await keyword.press('Enter');await waiting;
    await p.waitForTimeout(400);assert.equal(calls.at(-1).searchParams.get('f.keyword'), '回车');
    checks.push('Basic text coalesces typing, supports Enter and does not require Query');
    await p.route('**/api/list-filters/tasks?*', async route => { const u = new URL(route.request().url());if (u.searchParams.get('f.keyword') !== '旧响应') return route.continue();const response = await route.fetch();await new Promise(r => setTimeout(r, 600));try { await route.fulfill({ response }); } catch { /* request may have been canceled */ } });
    await keyword.fill('旧响应');await p.waitForTimeout(400);waiting = waitQuery(listPath, 'f.keyword', '新响应');await keyword.fill('新响应');await waiting;await p.waitForTimeout(700);
    await expect(bar.locator('.applied-tags')).toContainText('新响应');await expect(bar.locator('.applied-tags')).not.toContainText('旧响应');await p.unroute('**/api/list-filters/tasks?*');
    checks.push('Slow old responses cannot replace newer automatic filter results');
    let failQuery=true;
    await p.route('**/api/list-filters/tasks?*',route=>{if(failQuery&&new URL(route.request().url()).searchParams.get('f.keyword')==='失败重试'){failQuery=false;return route.fulfill({status:500,contentType:'application/json',body:JSON.stringify({code:500,message:'合成筛选失败'})});}return route.continue();});
    await keyword.fill('失败重试');await expect(bar.getByRole('alert')).toContainText('合成筛选失败');
    waiting=waitQuery(listPath,'f.keyword','失败重试');await bar.getByRole('button',{name:'重新查询',exact:true}).click();await waiting;
    await expect(bar.locator('.applied-tags')).toContainText('失败重试');await p.unroute('**/api/list-filters/tasks?*');
    checks.push('A failed automatic query retries its exact attempted conditions');

    await bar.getByRole('button', { name: '重置全部', exact: true }).click();await expect(bar.locator('.applied-tags')).not.toContainText('新响应');
    await p.screenshot({ path: path.join(out, 'basic-single-1440.png'), fullPage: true });

    for (const [route, label, fieldLabel, queryPath, queryKey, option] of [
      ['/home/works/performance', '绩效', '年度', 'performance', 'year', '2025年'],
      ['/home/works/achievement', '成果', '取得年度', 'achievements', 'year', '2025年'],
      ['/home/core-indicators', '核心指标', '月份', 'core', 'month', '1月'],
      ['/home/work-records', '纪实', '月份', 'records', 'month', '1月'],
      ['/home/notice', '消息', '阅读状态', 'notices', 'isRead', '未读'],
      ['/home/audit', '审核', '业务类型', 'audits', 'type', '任务佐证'],
      ['/home/report', '督办任务', '年度', 'supervision', 'year', '2025年'],
      ['/home/admin/accounts', '账号', '启停状态', 'admin-users', 'status', '停用'],
      ['/home/admin/permissions', '角色', '启停状态', 'admin-roles', 'enabled', '停用'],
      ['/home/admin/operations', '操作记录', '模块', 'admin-operations', 'module', null]
    ]) {
      await p.goto(state.frontend + route);const b = p.locator(`.query-filter-bar[aria-label="${label}筛选"]`);await expect(field(b, fieldLabel)).toBeVisible();
      waiting = p.waitForResponse(r => urlOf(r).pathname === '/api/list-filters/' + queryPath && urlOf(r).searchParams.has('f.' + queryKey));
      await choose(field(b, fieldLabel), option);await waiting;await expect(b.locator('.basic-fields').getByRole('button', { name: '查询', exact: true })).toHaveCount(0);
      checks.push(label + ': basic filter applies immediately');
    }
    await p.goto(state.frontend + '/home/works/budget?year=2026&month=5');const budgetBar = p.locator('.query-filter-bar[aria-label="预算筛选"]');
    const actual = p.locator('.lower-sheet tr').filter({ has: p.getByText('商品和服务支出', { exact: true }) }).getByRole('spinbutton').first();
    await expect(actual).toHaveValue('5.00');await actual.fill('9.00');await actual.press('Tab');
    await choose(field(budgetBar, '月份'), '6月');const decision = p.getByRole('dialog', { name: '未保存修改', exact: true });
    await expect(decision).toBeVisible();await decision.getByRole('button', { name: '取消', exact: true }).click();await expect(actual).toHaveValue('9.00');await expect(field(budgetBar, '月份')).toContainText('5月');
    await choose(field(budgetBar, '月份'), '6月');await decision.getByRole('button', { name: '放弃修改', exact: true }).click();await expect(actual).toHaveValue('4.00');
    checks.push('Automatic budget month changes retain save/discard/cancel protection and canceled filter selection');
    await p.goto(state.frontend + '/home/admin/accounts');await p.getByRole('tab', { name: '部门管理', exact: true }).click();
    const dept = p.locator('.query-filter-bar[aria-label="部门筛选"]');waiting = waitQuery('/api/list-filters/admin-departments', 'f.status', '1');await choose(field(dept, '启停状态'), '停用');await waiting;
    checks.push('Department management uses the same automatic basic interaction');
    await p.goto(state.frontend + '/home/admin/notices');waiting = p.waitForResponse(r => urlOf(r).pathname === '/api/admin/platform-notices' && urlOf(r).searchParams.get('status') === 'DRAFT');
    await p.locator('.admin-notices .panel .filters .el-select').click();await p.locator('.el-select-dropdown__item:visible').filter({ hasText: /^草稿$/ }).click();await waiting;
    checks.push('Independent platform notification filters apply immediately');
    const localTime = d => `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}T${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}:00`;
    let round = await call('post', '/admin/review-tasks', token, { name:'自动筛选评审轮次',year:2026,startsAt:localTime(new Date(Date.now()-3600000)),endsAt:localTime(new Date(Date.now()+3600000)) });
    const base = `/admin/review-tasks/${round.round_id}`;
    round = await call('post',base+'/members',token,{version:round.version,userIds:[910009],reason:'验证专家筛选'});
    round = await call('put',base+'/scope',token,{version:round.version,userIds:[910009],taskIds:[930002],reason:'仅授权一项任务'});
    round = await call('post',base+'/publish',token,{version:round.version,reason:'隔离发布'});
    let expert = (await call('post','/system/login',null,{user_id:910009,password:'review-fixture-password'})).token;
    expert = (await call('post','/system/review-context',expert,{experience:'EXPERT'})).token;
    const expertContext = await browser.newContext({viewport:{width:1440,height:900}});
    await expertContext.addInitScript(t=>localStorage.setItem('token',t),expert);
    p = await expertContext.newPage();p.on('pageerror',e=>errors.push(e.message));
    await p.goto(state.frontend+'/home/works');
    const expertBar=p.locator('.query-filter-bar[aria-label="任务筛选"]');
    waiting=waitQuery(`/api/review-work/${round.round_id}/tasks`,'f.status','0');
    await choose(field(expertBar,'任务状态'),'未开始');const scoped=await (await waiting).json();assert.equal(scoped.total,0);
    await expertBar.getByRole('button',{name:'重置全部',exact:true}).click();
    await expect(expertBar.locator('.applied-tags')).not.toContainText('未开始');
    checks.push('Expert role uses automatic basic filters on its own round-scoped endpoint');
    await p.goto(state.frontend + '/home/works');await p.setViewportSize({ width: 960, height: 540 });await expect(p.locator('.query-filter-bar')).toBeVisible();
    await p.screenshot({ path: path.join(out, 'basic-single-960.png'), fullPage: true });assert.deepEqual(errors, []);
    await call('put',base,token,{name:round.name,year:2026,startsAt:localTime(new Date(Date.now()-20*86400000)),endsAt:localTime(new Date(Date.now()-19*86400000)),description:round.description,version:round.version,reason:'结束自动筛选隔离验证'});
    await fs.writeFile(path.join(out, 'results.json'), JSON.stringify({ passed: true, checks, errors }, null, 2));console.log(JSON.stringify({ passed: true, checks: checks.length }));
  } catch (e) { if (p) { await p.screenshot({ path: path.join(out, 'failure.png'), fullPage: true });await fs.writeFile(path.join(out, 'failure.txt'), await p.locator('body').innerText()); }console.error(JSON.stringify({ checks, errors }));throw e; }
  finally { await api.dispose();await browser.close(); }
})().catch(e => { console.error(e.stack || e);process.exitCode = 1; });
