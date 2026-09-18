// Only the disposable UI fixture, with uploads beneath target/ui-audit.
const { request } = require('../../target/ui-audit-tools/node_modules/playwright');
const fs = require('node:fs/promises');
const path = require('node:path');
const assert = require('node:assert/strict');
async function main() {
  const root = path.resolve(__dirname, '../../target/ui-audit');
  assert.match(JSON.parse(await fs.readFile(path.join(root, 'state.json'))).schema, /^biz_review_test_[0-9a-f]{32}$/);
  const api = await request.newContext({ baseURL: 'http://127.0.0.1:15173' });
  const results = [];
  try {
    const { token } = await (await api.post('/api/system/login', { data: { user_id: 910001, password: 'review-fixture-password' } })).json();
    assert.ok(token);
    const headers = { Authorization: token };
    for (const extension of ['PDF', 'DoC', 'DOCX', 'pdf']) {
      const uploaded = await (await api.post('/api/system/upload/930002', { headers,
        multipart: { file: { name: `closure-submit.${extension}`, mimeType: 'application/octet-stream', buffer: Buffer.from('synthetic regression material') } } })).json();
      assert.ok(uploaded.fileId, JSON.stringify(uploaded));
      const submitted = await api.post('/api/biz/sub', { headers, data: {
        task_id: 930002, file_id: uploaded.fileId, reported_value: 3, data_type: '1', comment: 'Synthetic uppercase upload regression'
      } });
      assert.match(await submitted.text(), /成功/);
      const audits = await (await api.get('/api/biz/audit/task/930002', { headers })).json();
      const audit = audits.find(item => Number(item.fileId) === Number(uploaded.fileId));
      assert.ok(audit, JSON.stringify(audits));
      assert.equal(audit.fileSuffix, extension.toLowerCase());
      const task = await (await api.get('/api/biz/tasks/930002', { headers })).json();
      assert.equal(Number(task.currentValue), 3);
      assert.equal(String(task.status), '2');
      const withdrawn = await api.post('/api/biz/drawback/930002', { headers });
      assert.match(await withdrawn.text(), /撤回/);
      results.push({ extension, uploaded: true, submitted: true, normalizedSuffix: audit.fileSuffix });
    }
    for (const name of ['closure.exe', 'closure', 'closure.pdf.exe']) {
      const error = await (await api.post('/api/system/upload/930002', { headers,
        multipart: { file: { name, mimeType: 'application/octet-stream', buffer: Buffer.from('invalid fixture') } } })).json();
      assert.equal(error.code, 500);
      results.push({ name, rejected: true });
    }
    await fs.mkdir(path.join(root, 'evidence-fixed'), { recursive: true });
    await fs.writeFile(path.join(root, 'evidence-fixed/upload.json'), JSON.stringify(results, null, 2));
    console.log(JSON.stringify({ passed: results.length }));
  } finally { await api.dispose(); }
}
main().catch(error => { console.error(error); process.exitCode = 1; });
