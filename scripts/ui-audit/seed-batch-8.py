"""Load audit batch synthetic records into the currently running isolated UI schema."""
import argparse
import importlib.util
import json
import os
from pathlib import Path
import re
import sys
import subprocess

ROOT = Path(__file__).resolve().parents[2]
parser = argparse.ArgumentParser()
parser.add_argument('--batch', choices=['8', '9', '10', '11', '12', '13'], default='8')
batch = parser.parse_args().batch
schema = json.loads((ROOT / 'target/ui-audit/state.json').read_text())['schema']
if not re.fullmatch(r'biz_review_test_[0-9a-f]{32}', schema):
    raise RuntimeError('Refusing non-isolated schema')
spec = importlib.util.spec_from_file_location('runner', ROOT / 'scripts/run-review-regression.py')
runner = importlib.util.module_from_spec(spec)
sys.dont_write_bytecode = True
spec.loader.exec_module(runner)
env = os.environ.copy()
env['MYSQL_PWD'] = env['SHUANGGAO_TEST_DB_PASSWORD']
subprocess.run([runner.executable('mysql'), '--host=127.0.0.1', '--port=3306',
                '--user=' + env.get('SHUANGGAO_TEST_DB_USER', 'root'), '--default-character-set=utf8mb4', schema],
               input=(ROOT / f'scripts/ui-audit/fixture-batch-{batch}.sql').read_bytes(), env=env, check=True)
print(f'Batch {batch} synthetic fixture loaded into isolated schema')
