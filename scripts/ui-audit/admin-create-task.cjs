const { chromium, request } = require('../../target/ui-audit-tools/node_modules/playwright');
const { expect } = require('../../target/ui-audit-tools/node_modules/playwright/test');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');

async function main() {
  const root = path.resolve(__dirname, '../../target/ui-audit');
  const state = JSON.parse(await fs.readFile(path.join(root, 'state.json')));
  assert.match(state.schema, /^biz_review_test_[0-9a-f]{32}$/);
  assert.equal(state.frontend, 'http://127.0.0.1:15273');
  const output = path.join(root, 'evidence-admin-create-task'); await fs.mkdir(output, { recursive: true });
  const api = await request.newContext({ baseURL: state.frontend });
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  const checks = [], errors = []; let page;
  const passed = name => { checks.push(name); console.log(name); };
  async function login(id) { const result = await (await api.post('/api/system/login', { data: { user_id: id, password: 'WorkRecords123' } })).json(); assert.ok(result.token); return result.token; }
  async function tab(id) {
    const context = await browser.newContext({ viewport: { width: 1440, height: 960 } });
    await context.addInitScript(token => localStorage.setItem('token', token), await login(id));
    const result = await context.newPage(); result.setDefaultTimeout(15000); result.on('pageerror', e => errors.push(e.message));
    await result.goto(state.frontend + '/home/works'); return result;
  }
  const button = (name, target = page) => target.getByRole('button', { name, exact: true });
  const dialog = () => page.getByRole('dialog', { name: '新增三级建设任务', exact: true });
  async function choose(label, option) {
    await dialog().locator('.el-select').filter({ has: page.getByRole('combobox', { name: label, exact: true }) }).click();
    await page.getByRole('listbox', { name: label, exact: true }).getByRole('option', { name: option, exact: false }).click();
  }
  async function fill(code) {
    await choose('二级目录', '审计专用A二级任务');
    await dialog().getByRole('textbox', { name: '任务编号', exact: true }).fill(code);
    await dialog().getByRole('textbox', { name: '任务名称', exact: true }).fill('管理员新增合成任务');
    await choose('归口部门', '审计测试部门B'); await choose('责任人', '审计用户A');
    await choose('专业群审核人', '审计审核人'); await choose('归口审核人', '审计部门负责人');
    await choose('数据类型', '数值（累加）');
    await dialog().getByRole('textbox', { name: '目标值', exact: true }).fill('12.5');
    await dialog().getByRole('textbox', { name: '材料要求', exact: true }).fill('合成材料要求');
  }
  try {
    const ordinary = await tab(910001);
    await expect(ordinary.locator('.project-card')).toBeVisible();
    await expect(button('新增任务', ordinary)).toHaveCount(0); await ordinary.context().close();
    page = await tab(110228);
    await page.locator('.el-select').filter({ has: page.getByRole('combobox', { name: '任务状态', exact: true }) }).click();
    await page.getByRole('listbox', { name: '任务状态', exact: true }).getByRole('option', { name: '进行中', exact: true }).click();
    await button('新增任务').click();
    await button('预览并确认', dialog()).click(); await expect(dialog().getByText('请填写或选择任务编号')).toBeVisible();
    passed('real-api-admin-only-entry-and-required-validation');
    const code = 'UI-CREATE-' + Date.now(); await fill(code);
    await dialog().getByRole('textbox', { name: '目标值', exact: true }).fill('-1');
    await button('预览并确认', dialog()).click(); await expect(dialog().getByText('请输入非负数，最多16位整数和4位小数')).toBeVisible();
    await dialog().getByRole('textbox', { name: '目标值', exact: true }).fill('12.5');
    await button('预览并确认', dialog()).click();
    await expect(dialog()).toContainText('审计测试部门B'); await expect(dialog()).toContainText('不会自动关联绩效');
    await page.screenshot({ path: path.join(output, 'create-preview.png'), fullPage: true });
    passed('validation-and-before-after-impact-preview');
    let posts = 0;
    await page.route('**/api/biz/tasks/manage/add?returnDetail=true', async route => { posts++; await new Promise(resolve => setTimeout(resolve, 700)); await route.continue(); });
    await button('确认保存', dialog()).click(); await expect(button('确认保存', dialog())).toBeDisabled();
    await expect(dialog()).toHaveCount(0); assert.equal(posts, 1); await page.unroute('**/api/biz/tasks/manage/add?returnDetail=true');
    const drawer = page.locator('.el-drawer:visible'); await expect(drawer).toContainText('管理员新增合成任务');
    const token = await login(110228);
    const tasks = await (await api.get('/api/biz/tasks', { headers: { Authorization: token } })).json();
    const task = tasks.find(item => item.taskCode === code); assert.ok(task); assert.equal(task.deptId,920002); assert.equal(task.leaderId,910001);
    assert.equal(String(task.status),'0'); assert.equal(Number(task.currentValue),0); assert.equal(Number(task.progress),0);
    passed('real-create-cross-department-zero-values-single-submit-and-open-detail');
    await button('返回列表并定位', drawer).click();
    const locate = page.getByRole('dialog', { name: '定位记录', exact: true });
    await expect(locate).toBeVisible(); await button('保留筛选', locate).click(); await expect(drawer).toBeVisible();
    await button('返回列表并定位', drawer).click(); await button('清除筛选并定位', locate).click();
    await expect(page.locator('.el-table__row').filter({ hasText: code })).toBeVisible();
    await page.screenshot({ path: path.join(output, 'created-and-located.png'), fullPage: true });
    passed('created-task-located-in-expanded-directory');
    await button('新增任务').click(); await fill(code); await button('预览并确认',dialog()).click(); await button('确认保存',dialog()).click();
    await expect(dialog().getByText('同项目、同年度已存在该任务编号')).toBeVisible(); await button('返回修改',dialog()).click();
    await expect(dialog().getByRole('textbox',{name:'任务编号',exact:true})).toHaveValue(code);
    await button('取消',dialog()).click(); await button('继续填写',page.getByRole('dialog',{name:'离开确认',exact:true})).click();
    await expect(page.getByRole('dialog',{name:'离开确认',exact:true})).toBeHidden();
    await expect(dialog()).toBeVisible(); passed('real-duplicate-error-retains-form-and-cancel-retains-edit');
    await page.setViewportSize({width:390,height:844});
    await dialog().locator('.el-dialog__body').evaluate(el=>{el.scrollTop=0;});
    const rect = await dialog().locator('.el-dialog').boundingBox(); assert.ok(rect.x >= 0 && rect.x+rect.width <= 391);
    await dialog().locator('.el-dialog__body').hover({position:{x:8,y:130}}); await page.mouse.wheel(0,2400);
    await expect.poll(()=>dialog().locator('.el-dialog__body').evaluate(el=>el.scrollTop)).toBeGreaterThan(0);
    await expect(dialog().getByRole('textbox',{name:'材料要求',exact:true})).toBeInViewport();
    await page.screenshot({path:path.join(output,'mobile-scroll.png'),fullPage:true}); passed('narrow-screen-real-wheel-scroll-and-footer-access');
    await button('取消',dialog()).click(); await button('放弃填写',page.getByRole('dialog',{name:'离开确认',exact:true})).click();
    await expect(dialog()).toHaveCount(0); await page.reload(); await expect(button('新增任务')).toBeVisible();
    assert.deepEqual(errors,[]); passed('reload-and-no-browser-exceptions');
  } catch(e) { if(page) await page.screenshot({path:path.join(output,'failure.png'),fullPage:true}); throw e; }
  finally { await fs.writeFile(path.join(output,'results.json'),JSON.stringify({checks,errors},null,2)); await browser.close(); await api.dispose(); }
}
main().catch(error=>{console.error(error);process.exitCode=1;});
