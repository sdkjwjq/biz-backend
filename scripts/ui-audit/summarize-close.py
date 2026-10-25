"""Validate paired capacity reports without treating denied requests as successes."""
import json,sys,math
from pathlib import Path
root=Path(__file__).resolve().parents[2]
folders=[Path(value) for value in sys.argv[1:]]
assert len(folders)==2,'Pass the 1x and 5x evidence folders'
profiles=['admin','ordinary','leaderAll','leaderDepartment','expert','finance']
complex_names={'accountRoles','recordText','messageText','operationText','versionDates'}
report={'scales':[],'checks':{},'unmet':[]}
for scale,folder in zip([1,5],folders):
 entry={'scale':scale,'folder':str(folder.relative_to(root)),'profiles':{}}
 for profile in profiles:
  before=json.loads((folder/f'performance-close-baseline-{profile}.json').read_text())
  after=json.loads((folder/f'performance-close-optimized-{profile}.json').read_text())
  assert before['schema']==after['schema']
  old={(r['workload'],r['concurrency']):r for r in before['results']}
  assert len(old)==len(after['results'])
  comparisons=[]
  for row in after['results']:
   key=(row['workload'],row['concurrency']);assert key in old and row['samples']>=60
   limit=2000 if row['workload'] in complex_names or row['workload'].startswith('overview') else 1000
   value={**row,'baselineP95Ms':old[key]['p95Ms'],'limitMs':limit,'passed':row['p95Ms']<=limit}
   comparisons.append(value)
   if not value['passed']:report['unmet'].append({'scale':scale,'profile':profile,**value})
  entry['profiles'][profile]=comparisons
 entry['pages']={label:json.loads((folder/f'close-page-performance-{label}.json').read_text()) for label in ['baseline','optimized']}
 if scale==5:
  for row in entry['pages']['optimized']:
   if row['coldP95']>1500 or row['warmP95']>1000:report['unmet'].append({'scale':scale,'page':row})
 report['scales'].append(entry)
report['checks']['optimizedCapacityScenarios']=sum(len(rows) for scale in report['scales'] for rows in scale['profiles'].values())
report['checks']['minimumSamplesPerScenario']=min(row['samples'] for scale in report['scales'] for rows in scale['profiles'].values() for row in rows)
report['passed']=not report['unmet']
(root/'target/performance-close-capacity-summary.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf8')
print(json.dumps({'scenarios':report['checks']['optimizedCapacityScenarios'],'unmet':report['unmet']},ensure_ascii=False))
if report['unmet']:sys.exit(1)
