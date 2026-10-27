// Read-only UI checks against serve.py's disposable fixture and production build.
const fs=require('node:fs/promises'),sync=require('node:fs'),path=require('node:path'),assert=require('node:assert/strict');
const {chromium}=require('../../target/ui-audit-tools/node_modules/playwright');
const {expect:baseExpect}=require('../../target/ui-audit-tools/node_modules/playwright/test');
const expect=baseExpect.configure({timeout:20000});
const statePath=process.env.SHUANGGAO_UI_STATE_FILE;assert.ok(statePath);
const state=JSON.parse(sync.readFileSync(statePath,'utf8')),mode=process.argv[2];
assert.ok(['before','after'].includes(mode));assert.match(state.schema,/^biz_review_test_[0-9a-f]{32}$/);
const origin=new URL(state.frontend);assert.equal(origin.hostname,'127.0.0.1');assert.equal(origin.protocol,'http:');
const OUT=path.join(path.dirname(statePath),'title-evidence'),errors=[],checks=[],measurements=[];
let browser,context,page;
async function settle(){await expect(page.locator('.query-filter-bar').first()).toBeVisible();await expect(page.locator('.query-filter-bar p[role=status]').filter({visible:true})).toHaveCount(0);await expect(page.locator('.el-loading-mask').filter({visible:true})).toHaveCount(0);await page.evaluate(()=>new Promise(r=>requestAnimationFrame(()=>requestAnimationFrame(r))))}
async function cells(){return page.locator('.el-table__body-wrapper td:first-child .cell').evaluateAll(cells=>cells.filter(e=>e.querySelector('.task-name-cell')&&e.getBoundingClientRect().width>0&&e.getBoundingClientRect().height>0).map(e=>{const content=e.querySelector('.task-name-cell'),title=content.querySelector('.task-name-text,.hover-scroll-text'),arrow=e.querySelector('.el-table__expand-icon'),placeholder=e.querySelector('.el-table__placeholder'),r=e.getBoundingClientRect(),t=title.getBoundingClientRect(),a=arrow?.getBoundingClientRect(),p=placeholder?.getBoundingClientRect();return{text:title.textContent.trim(),category:!!content.querySelector('.task-name-text'),cell:r.toJSON(),title:t.toJSON(),arrow:a?.toJSON(),placeholder:p?.toJSON(),count:content.querySelector('.child-count')?.getBoundingClientRect().toJSON(),display:getComputedStyle(e).display}}))}
async function shot(name){await page.screenshot({path:path.join(OUT,`${mode}-${name}.png`),fullPage:true})}
(async()=>{await fs.mkdir(OUT,{recursive:true});browser=await chromium.launch({channel:'msedge',headless:true});context=await browser.newContext({viewport:{width:1280,height:720}});await context.route('**/*',r=>new URL(r.request().url()).hostname==='127.0.0.1'?r.continue():r.abort());page=await context.newPage();page.on('pageerror',e=>errors.push(e.message));page.setDefaultTimeout(30000);
try{
 await page.goto(origin.origin+'/login');await page.getByPlaceholder('账号',{exact:true}).fill('110228');await page.getByPlaceholder('密码',{exact:true}).fill('review-fixture-password');await page.getByRole('button',{name:/登\s*录/}).click();await page.waitForURL('**/home/**');await page.goto(origin.origin+'/home/works/achievement');await settle();
 if(mode==='before'){
  const data=await cells();measurements.push({size:'1280x720',cells:data});assert.ok(data.some(c=>c.arrow?.width&&c.title.y>=c.arrow.bottom-1),'Baseline must reproduce the arrow occupying a separate line');await shot('category-wrap');checks.push('Reproduced category title moving below the expansion arrow');
 }else{
  for(const [width,height]of [[1920,1080],[1536,864],[1280,720],[960,540]]){
   await page.setViewportSize({width,height});await settle();let data=await cells();assert.ok(data.filter(c=>c.category).length>=2);
   for(const c of data.filter(c=>c.category)){assert.ok(c.arrow.width>0);assert.ok(c.title.y<c.arrow.bottom&&c.arrow.y<c.title.bottom,'Arrow and category share the same text line: '+c.text);assert.ok(c.title.right<=c.cell.right+1);assert.ok(c.count.y<c.title.bottom&&c.count.bottom>c.title.y,'Count remains beside category')}
   const title=page.getByText('1.落实立德树人根本任务',{exact:true});const row=title.locator('xpath=ancestor::tr');const arrow=row.locator('.el-table__expand-icon');if(!await arrow.evaluate(e=>e.classList.contains('el-table__expand-icon--expanded')))await arrow.click();
   const long=page.locator('.hover-scroll-text').filter({hasText:'合成长名称成果'}).first();await expect(long).toBeVisible();await expect(page.getByText('请补充上级发文依据（合成驳回原因）',{exact:false})).toBeVisible();
   data=await cells();for(const c of data.filter(c=>!c.category)){assert.ok(c.title.right<=c.cell.right+1);if(c.placeholder?.width)assert.ok(c.title.x>=c.placeholder.right-1,'Name follows the tree indentation');assert.ok(c.title.width>200,'Long names retain meaningful available width')}
   measurements.push({size:`${width}x${height}`,cells:data});await shot(`expanded-${width}`);
   await title.click();await expect(long).toBeHidden();await arrow.click();await expect(long).toBeVisible();await arrow.click();await expect(long).toBeHidden();checks.push(`Category arrow/title/count, child title width, rejection reason and expansion at ${width}x${height}`);
  }
  await page.setViewportSize({width:1280,height:720});const category=page.getByText('1.落实立德树人根本任务',{exact:true});await category.click();const long=page.locator('.hover-scroll-text').filter({hasText:'合成长名称成果'}).first();await expect(long).toHaveClass(/truncated/);await long.hover();await expect.poll(()=>long.locator('.scroll-content').evaluate(e=>getComputedStyle(e).animationName)).toMatch(/^reveal-name(?:-|$)/);await long.focus();assert.equal(await long.evaluate(e=>document.activeElement===e),true);await page.emulateMedia({reducedMotion:'reduce'});await expect.poll(()=>long.locator('.scroll-content').evaluate(e=>getComputedStyle(e).animationName)).toBe('none');assert.ok(await long.evaluate(e=>e.scrollHeight<=e.clientHeight+1));checks.push('Long name hover, keyboard focus and reduced-motion full-name display retained');
  for(const [route,name]of [['/home/works','tasks'],['/home/works/performance','performance'],['/home/expert-reviews','expert'],['/home/core-indicators','core']]){
   await page.evaluate(url=>document.querySelector('#app').__vue_app__.config.globalProperties.$router.push(url),route);await settle();if(name==='tasks'||name==='performance'){const results=await page.locator('.virtual-tree-cell').evaluateAll(cells=>cells.filter(c=>c.querySelector('button')).map(c=>{const a=c.querySelector('button').getBoundingClientRect(),t=c.querySelector('.virtual-cell-content').getBoundingClientRect();return{a:a.toJSON(),t:t.toJSON()}}));assert.ok(results.length);for(const c of results)assert.ok(c.a.y<c.t.bottom&&c.t.y<c.a.bottom)}await shot(`inspection-${name}`);checks.push(`Inspected ${name} title layout without introducing global table styles`)
  }
 }
 assert.deepEqual(errors,[]);await fs.writeFile(path.join(OUT,`${mode}-results.json`),JSON.stringify({checks,errors,measurements},null,2));console.log(JSON.stringify({passed:true,mode,checks:checks.length}));
}catch(error){await fs.writeFile(path.join(OUT,`${mode}-failure.txt`),await page.locator('body').innerText());await shot('failure');throw error}finally{await context?.close();await browser?.close()}
})().catch(error=>{console.error(error.stack||error);process.exitCode=1});
