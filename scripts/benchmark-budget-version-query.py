"""Compare version metadata aggregation on disposable synthetic data, never business data."""
import json
import os
from pathlib import Path
import re
import shutil
import statistics
import subprocess
import time
import uuid

root = Path(__file__).resolve().parents[1]
schema = 'biz_review_test_' + uuid.uuid4().hex
env = dict(os.environ, MYSQL_PWD=os.environ['SHUANGGAO_TEST_DB_PASSWORD'])
mysql = shutil.which('mysql') or 'C:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe'
command = [mysql, '--host=127.0.0.1', '--port=3306', '--user=root',
           '--default-character-set=utf8mb4', '--batch', '--raw', '--skip-column-names']

def sql(statement, database=True):
    return subprocess.run(command + ([schema] if database else []), input=statement.encode('utf-8'),
                          env=env, check=True, capture_output=True).stdout

columns = 'v.version_id,v.year,v.month,v.version_no,'
tail = ' WHERE v.is_delete=0 ORDER BY v.year DESC,v.month DESC,v.version_no DESC'
old = ('SELECT ' + columns + '(v.version_no=(SELECT MAX(x.version_no) FROM versions x '
       'WHERE x.year=v.year AND x.month=v.month AND x.is_delete=0)) isCurrent FROM versions v' + tail)
new = ('SELECT ' + columns + 'CAST((v.version_no=latest.version_no) AS SIGNED) isCurrent FROM versions v '
       'LEFT JOIN (SELECT year,month,MAX(version_no) version_no FROM versions '
       'WHERE is_delete=0 GROUP BY year,month) latest ON latest.year=v.year AND latest.month=v.month' + tail)

sql(f'CREATE DATABASE `{schema}`', False)
try:
    sql('CREATE TABLE versions(version_id BIGINT PRIMARY KEY,year INT NOT NULL,month INT NOT NULL,version_no INT NOT NULL,is_delete TINYINT NOT NULL,'
        'KEY idx_budget_version_period(year,month,is_delete,version_no))')
    results = []
    for count in (120, 600):
        sql('DELETE FROM versions')
        rows = ','.join(f'({i+1},{2025+i%2},{9+(i//2)%2},{i//4+1},{1 if i>=count-4 else 0})' for i in range(count))
        sql('INSERT INTO versions VALUES ' + rows)
        sql('ANALYZE TABLE versions')
        assert sql(old) == sql(new)
        measurements = {'rows': count, 'ordered_results_equal': True}
        for label, query in [('legacy', old), ('grouped', new)]:
            sql(query)
            durations = []
            server_durations = []
            for _ in range(5):
                start = time.perf_counter()
                sql(query)
                durations.append(round((time.perf_counter()-start)*1000, 2))
                timed = sql('SET @budget_benchmark_start=SYSDATE(6);' + query
                            + ';SELECT TIMESTAMPDIFF(MICROSECOND,@budget_benchmark_start,SYSDATE(6))').decode()
                server_durations.append(int(timed.strip().splitlines()[-1])/1000)
            measurements[label] = {'samples_ms': durations, 'median_ms': statistics.median(durations),
                                   'server_elapsed_samples_ms': server_durations,
                                   'server_elapsed_median_ms': statistics.median(server_durations),
                                   'explain': sql('EXPLAIN ' + query).decode().strip().splitlines()}
        results.append(measurements)
    report = {'scope': 'Synthetic aggregation query only, single client, warmed runs. samples_ms includes MySQL client startup; server_elapsed brackets the full SELECT with server timestamps in one client session, including result delivery and statement overhead but excluding connection startup. Not endpoint P95 or full-system acceptance.',
              'results': results, 'production_changes': False}
    (root/'target/performance-budget-version-query.json').write_text(json.dumps(report, indent=2)+'\n', encoding='utf-8')
    print(json.dumps(report, indent=2))
finally:
    if not re.fullmatch(r'biz_review_test_[0-9a-f]{32}', schema):
        raise RuntimeError('Unsafe cleanup target')
    sql(f'DROP DATABASE `{schema}`', False)
