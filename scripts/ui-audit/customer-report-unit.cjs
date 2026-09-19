const assert = require('node:assert/strict');
const { pathToFileURL } = require('node:url');
const path = require('node:path');
async function main() {
  const { reportCsv, reportDate, reportStatus, awardQuantity } = await import(pathToFileURL(path.resolve(__dirname, '../../../biz/src/utils/achievementReport.js')));
  assert.equal(reportDate('2026-06-30T16:00:00Z'), '2026-07-01');
  assert.equal(reportDate(null), ''); assert.equal(reportDate('invalid'), '');
  assert.equal(reportStatus({ auditStatus: null }), 'unknown');
  assert.equal(reportStatus({ auditStatus: -30 }), 'returned');
  assert.equal(awardQuantity({ yiDengJiang: 2, jinJiang: 3, erDengJiang: null }), 5);
  const csv = reportCsv([['=SUM(1,2)', '+cmd', '-cmd', '@cmd', '\tcmd', '正常中文', 'a"b\nc', 0]]);
  assert.ok(csv.startsWith('\uFEFF'));
  for (const formula of ['=SUM(1,2)', '+cmd', '-cmd', '@cmd', '\tcmd']) assert.ok(csv.includes('"\'' + formula + '"'));
  assert.ok(csv.includes('"a""b\nc"')); assert.ok(csv.endsWith('"0"'));
  console.log('PASS: Shanghai date boundaries, missing evidence, status classification, award totals, CSV escaping and formula protection');
}
main().catch(e => { console.error(e); process.exitCode = 1; });
