const { chromium, request } = require('../../target/ui-audit-tools/node_modules/playwright');
const { expect } = require('../../target/ui-audit-tools/node_modules/playwright/test');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');
async function main() {
  const root=path.resolve(__dirname,'../../target/ui-audit');
  const state=JSON.parse(await fs.readFile(path.join(root,'state.json')));
  assert.match(state.schema,/^biz_review_test_[0-9a-f]{32}$/);
  assert.equal(state.frontend,'http://127.0.0.1:15273');
  const out=path.join(root,'evidence-work-record-scroll');await fs.mkdir(out,{recursive:true});
  const api=await request.newContext({baseURL:state.frontend});
  const auth=await(await api.post('/api/system/login',{data:{user_id:910003,password:'WorkRecords123'}})).json();assert.ok(auth.token);
  const draft=await(await api.post('/api/work-records',{headers:{Authorization:auth.token},data:{year:2026,month:1}})).json();
  const browser=await chromium.launch({channel:'msedge',headless:true});const errors=[],checks=[];let page;
  try {
    for(const [width,height] of [[1440,600],[1440,900],[768,700]]) {
      const context=await browser.newContext({viewport:{width,height}});
      await context.addInitScript(token=>localStorage.setItem('token',token),auth.token);
      page=await context.newPage();page.setDefaultTimeout(10000);page.on('pageerror',e=>errors.push(e.message));
      await page.route('**/work-records/'+draft.record.recordId,async route=>{
        const response=await route.fetch();const data=await response.json();
        data.statistics.tasks=Array.from({length:22},(_,index)=>({...data.statistics.tasks[0],taskId:990000+index}));
        await route.fulfill({response,json:data});
      });
      await page.goto(state.frontend+'/home/work-records');
      await page.getByRole('button',{name:'打开草稿',exact:true}).click();
      const container=page.locator('.work-records');await expect(page.locator('.reform-picker')).toBeVisible();
      const bounds=await container.boundingBox();
      await page.mouse.move(bounds.x+10,Math.min(bounds.y+100,height-100));
      await page.mouse.wheel(0,300);await page.waitForTimeout(250);
      assert.ok(await container.evaluate(node=>node.scrollTop)>0,'Mouse wheel must scroll the work-record page');
      async function wheelTo(locator) {
        for(let step=0;step<40;step++) {
          const r=await locator.boundingBox(), panel=await container.boundingBox(),bar=await page.locator('.detail-actions').boundingBox();
          if(r.y>=panel.y+8 && r.y+r.height<=Math.min(bar.y,height)-8)return;
          await page.mouse.move(panel.x+10,panel.y+80);
          await page.mouse.wheel(0,r.y<panel.y+8 ? -80:80);await page.waitForTimeout(60);
        }
        throw new Error('Control cannot be reached without being covered');
      }
      const picker=page.locator('.reform-picker');await wheelTo(picker);await picker.click();
      await page.getByRole('option',{name:'审计专用A一级任务',exact:true}).click();await page.keyboard.press('Escape');
      const progress=page.getByRole('textbox',{name:'关键进展',exact:true});await wheelTo(progress);await progress.fill('滚轮可达的进展');
      const other=page.getByRole('textbox',{name:'其它事项',exact:true});await wheelTo(other);await other.fill('底部可点击');
      await page.screenshot({path:path.join(out,`${width}-${height}.png`)});
      checks.push(`wheel-selection-and-last-textarea-${width}x${height}`);
      await page.getByRole('button',{name:'返回列表',exact:true}).last().click();
      await page.getByRole('dialog',{name:'未保存提醒'}).getByRole('button',{name:'离开',exact:true}).click();
      await expect(page.getByRole('button',{name:'新建纪实',exact:true})).toBeVisible();
      assert.equal(await container.evaluate(node=>node.scrollTop),0);
      await context.close();
    }
    assert.deepEqual(errors,[]);console.log(JSON.stringify({checks,errors}));
    await fs.writeFile(path.join(out,'results.json'),JSON.stringify({checks,errors},null,2));
  } catch(e) {if(page)await page.screenshot({path:path.join(out,'failure.png')});throw e;}
  finally {await browser.close();await api.dispose();}
}
main().catch(e=>{console.error(e);process.exitCode=1;});
