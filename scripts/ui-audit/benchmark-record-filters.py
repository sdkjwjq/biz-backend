"""Short HTTP comparison against serve.py's disposable database only.

Run after browser checks with SHUANGGAO_TEST_DB_PASSWORD set. Synthetic rows remain
in the disposable schema until serve.py stops; never points at the debug database.
"""
import concurrent.futures
import json
import math
import os
from pathlib import Path
import re
import shutil
import statistics
import subprocess
import time
import urllib.parse
import urllib.request

ROOT = Path(__file__).resolve().parents[2]
state = json.loads((ROOT / "target/ui-audit/state.json").read_text(encoding="utf-8"))
schema = state["schema"]
if not re.fullmatch(r"biz_review_test_[0-9a-f]{32}", schema) or state["frontend"] != "http://127.0.0.1:15173":
    raise RuntimeError("Only the disposable local UI fixture is allowed")
password = os.environ["SHUANGGAO_TEST_DB_PASSWORD"]
mysql = shutil.which("mysql") or "C:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe"
env = dict(os.environ, MYSQL_PWD=password)


def sql(statement):
    subprocess.run([mysql, "--host=127.0.0.1", "--port=3306", "--user=root", "--default-character-set=utf8mb4", schema],
                   input=statement.encode("utf-8"), env=env, check=True, capture_output=True)


def http(path, token=None, data=None):
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = token
    request = urllib.request.Request(state["frontend"] + "/api" + path,
                                     data=None if data is None else json.dumps(data).encode(), headers=headers)
    start = time.perf_counter()
    with urllib.request.urlopen(request, timeout=60) as response:
        body = response.read()
    return (time.perf_counter() - start) * 1000, len(body), json.loads(body)


token = http("/system/login", data={"user_id": 110228, "password": "review-fixture-password"})[2]["token"]
owners = [880000000 + i for i in range(50)]
params = [("f.year", "2025"), ("pageSize", "10")]+[("f.ownerId", str(owner)) for owner in owners]
legacy = "/list-filters/records?" + urllib.parse.urlencode(params)
paged = legacy + "&responseMode=page&includeFields=false"
results = []
for count in [10, 50]:
    # Unique synthetic owners, one year, 12 months; ten narrative entries per record.
    for owner in owners[:count]:
        values = ",".join(f"({owner},'Synthetic benchmark owner',2025,{month},1,NOW(),NOW(),NOW(),REPEAT('示例正文',100))" for month in range(1,13))
        sql("INSERT IGNORE INTO biz_work_record(owner_id,owner_name,record_year,record_month,status,create_time,update_time,submit_time,problems) VALUES " + values)
    for root in range(1,11):
        sql(f"INSERT IGNORE INTO biz_work_record_entry(record_id,reform_task_id,reform_task_name,key_progress,stage_results,typical_practices) "
            f"SELECT record_id,{root},'Synthetic reform',REPEAT('进展',100),REPEAT('成果',100),REPEAT('做法',100) FROM biz_work_record "
            f"WHERE owner_id BETWEEN {owners[0]} AND {owners[count-1]} AND record_year=2025")
    baseline = http(legacy, token)[2]
    optimized = http(paged, token)[2]
    expected = {key: baseline[key] for key in ["items", "total", "page", "pageSize"]}
    assert expected == {key: optimized[key] for key in expected}
    assert expected["total"] == count * 12
    for concurrency in [1, 10, 30]:
        for mode, path in [("legacy", legacy), ("paged", paged)]:
            http(path, token)  # Separate warmup from recorded requests.
            samples = max(20, concurrency)
            with concurrent.futures.ThreadPoolExecutor(max_workers=concurrency) as pool:
                responses = list(pool.map(lambda _: http(path, token), range(samples)))
            for _, _, body in responses:
                assert expected == {key: body[key] for key in expected}
            times = sorted(r[0] for r in responses)
            result = {"rows": count*12, "entries": count*120, "concurrency": concurrency,
                      "mode": mode, "samples": samples, "p50_ms": round(statistics.median(times),2),
                      "p95_ms": round(times[math.ceil(.95*len(times))-1],2),
                      "mean_response_bytes": round(statistics.mean(r[1] for r in responses))}
            results.append(result)
            print(json.dumps(result), flush=True)
report = {"scope": "Work-record common filters only; synthetic historical records with no current owner account",
          "environment": "Disposable local serve.py fixture, warmed short HTTP runs; includes Vite proxy",
          "limitations": "Not a whole-system acceptance test; no SQL count instrumentation, cold-start or long-duration load test",
          "all_responses_equivalent": True, "results": results}
(ROOT / "target/performance-record-filter-benchmark.json").write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding="utf-8")
