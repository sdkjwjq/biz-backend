const { chromium, request } = require('../../target/ui-audit-tools/node_modules/playwright');
const { expect } = require('../../target/ui-audit-tools/node_modules/playwright/test');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');
async function main() {
  const root=path.resolve(__dirname,'../../target/ui-audit');
  const state=JSON.parse(await fs.readFile(path.join(root,'state.json')));
  assert.match(state.schema,/^biz_review_test_[0-9a-f]{32}$/); assert.equal(state.frontend,'http://127.0.0.1:15273');
  const out=path.join(root,'evidence-work-record-export'); await fs.mkdir(out,{recursive:true});
  const api=await request.newContext({baseURL:state.frontend});
  const browser=await chromium.launch({channel:'msedge',headless:true});
  const checks=[],errors=[]; const passed=name=>{checks.push(name);console.log(name);};
  async function token(id) { const data=await(await api.post('/api/system/login',{data:{user_id:id,password:'WorkRecords123'}})).json(); assert.ok(data.token);return data.token; }
  async function tab(id) {
    const context=await browser.newContext({viewport:{width:1440,height:960},acceptDownloads:true});
    await context.addInitScript(value=>localStorage.setItem('token',value),await token(id));
    const page=await context.newPage();page.setDefaultTimeout(15000);page.on('pageerror',e=>errors.push(e.message));
    await page.goto(state.frontend+'/home/work-records');return page;
  }
  async function download(page,trigger,name) {
    const event=page.waitForEvent('download');await trigger();const file=await event;
    assert.match(file.suggestedFilename(),/\.docx$/);await file.saveAs(path.join(out,name));
    const bytes=await fs.readFile(path.join(out,name));assert.equal(bytes.subarray(0,2).toString(),'PK');assert.ok(bytes.length>5000);
    await expect(page.getByRole('button',{name:/导出所选/})).not.toHaveClass(/is-loading/);
  }
  let page;
  try {
    for(const id of [910003,910002]) {
      const auth=await token(id);
      for(let month=1;month<=(id===910003?9:3);month++) {
        const record=await(await api.post('/api/work-records',{headers:{Authorization:auth},data:{year:2026,month}})).json();
        if(record.record.status===1)continue;
        const response=await api.post('/api/work-records/'+record.record.recordId+'/submit',{headers:{Authorization:auth},data:{version:record.record.version,entries:[{reformTaskId:930000,keyProgress:'Export '+id+' month '+month,stageResults:'成果',typicalPractices:'做法'}]}});
        assert.equal(response.status(),200,await response.text());
      }
    }
    page=await tab(110228);await expect(page.getByText('Total 12')).toBeVisible();
    await download(page,()=>page.getByRole('button',{name:'导出',exact:true}).first().click(),'single.docx');
    passed('real-single-row-download-is-word-file');
    await page.locator('.el-table__body-wrapper .el-checkbox').first().click();
    await page.locator('.el-pagination').getByText('2',{exact:true}).click();
    await expect(page.locator('.el-table__body-wrapper tbody tr')).toHaveCount(2);
    await page.locator('.el-table__body-wrapper .el-checkbox').first().click();
    await expect(page.getByRole('button',{name:'导出所选（2）',exact:true})).toBeEnabled();
    let calls=0;await page.route('**/work-records/export',async route=>{calls++;await new Promise(resolve=>setTimeout(resolve,400));await route.continue();});
    await download(page,()=>page.getByRole('button',{name:'导出所选（2）',exact:true}).evaluate(button=>{button.click();button.click();}),'merged.docx');
    assert.equal(calls,1);await page.unroute('**/work-records/export');
    passed('real-cross-page-selection-batch-download-and-duplicate-click');
    await page.locator('.el-select').filter({has:page.getByRole('combobox',{name:'纪实月份',exact:true})}).click();
    await page.getByRole('option',{name:'1月',exact:true}).click();
    await expect(page.getByRole('button',{name:'导出所选（0）',exact:true})).toBeDisabled();
    await expect(page.locator('.el-table__body-wrapper tbody tr')).toHaveCount(2);passed('changing-filter-clears-export-selection');
    await page.getByRole('button',{name:'查看',exact:true}).first().click();
    const event=page.waitForEvent('download');await page.getByRole('button',{name:'导出 Word',exact:true}).click();
    await(await event).saveAs(path.join(out,'detail.docx'));passed('real-readonly-detail-export');
    await page.getByRole('button',{name:'返回列表',exact:true}).first().click();
    await page.route('**/work-records/export',route=>route.fulfill({status:403,contentType:'application/json',body:JSON.stringify({code:403,message:'没有导出权限'})}));
    let downloads=0;page.on('download',()=>downloads++);
    await page.getByRole('button',{name:'导出',exact:true}).first().click();await expect(page.getByText('该用户无权限执行此操作',{exact:true})).toBeVisible();
    assert.equal(downloads,0);await page.unroute('**/work-records/export');passed('simulated-export-error-is-message-not-corrupt-download');
    await page.screenshot({path:path.join(out,'export-list.png'),fullPage:true});
    const filler=await tab(910003);await expect(filler.getByRole('button',{name:'新建纪实',exact:true})).toBeVisible();
    await expect(filler.getByRole('button',{name:/导出/})).toHaveCount(0);
    const denied=await api.post('/api/work-records/export',{headers:{Authorization:await token(910003)},data:{ids:[1]}});assert.equal(denied.status(),403);
    passed('real-filler-cannot-export-through-ui-or-api');assert.deepEqual(errors,[]);
  } catch(e) { if(page)await page.screenshot({path:path.join(out,'failure.png'),fullPage:true});throw e; }
  finally {await fs.writeFile(path.join(out,'results.json'),JSON.stringify({checks,errors},null,2));await browser.close();await api.dispose();}
}
main().catch(e=>{console.error(e);process.exitCode=1;});
