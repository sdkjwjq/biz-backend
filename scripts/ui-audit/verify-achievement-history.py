"""Exercise archived achievement revisions against a disposable ui-audit server.

Run with scripts/ui-audit/serve.py --fixture base. Never point this at production.
"""
import argparse
import json
import re
import urllib.request
import uuid


def request(base, path, token=None, payload=None):
    data = None if payload is None else json.dumps(payload, ensure_ascii=False).encode("utf-8")
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = token
    req = urllib.request.Request(base + path, data=data, headers=headers)
    with urllib.request.urlopen(req, timeout=20) as response:
        body = response.read().decode("utf-8")
    try:
        result = json.loads(body)
    except json.JSONDecodeError:
        result = body
    if isinstance(result, dict) and result.get("code", 200) >= 400:
        raise AssertionError(f"{path}: {result}")
    return result


def payload(name, comment):
    return {"category": 1, "level": "省级", "achName": name,
            "department": "审计测试部门", "gotTime": "2026-05-31 12:00:00",
            "deptId": 920001, "comment": comment, "isCompetition": 0,
            "teDengJiang": 0, "yiDengJiang": 0, "erDengJiang": 0,
            "sanDengJiang": 0, "jinJiang": 0, "yinJiang": 0,
            "tongJiang": 0, "youShengJiang": 0, "budDengDengCi": 0}


def one(records, status):
    matches = [row for row in records if row["flowStatus"] == status]
    assert len(matches) == 1, (status, records)
    return matches[0]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--base", default="http://127.0.0.1:15176/api")
    args = parser.parse_args()
    if not re.fullmatch(r"http://127\.0\.0\.1:\d+/api", args.base):
        raise SystemExit("Only a local isolated server is allowed")
    uploader = request(args.base, "/system/login", payload={"user_id": 1910001,
                         "password": "review-fixture-password"})["token"]
    admin = request(args.base, "/system/login", payload={"user_id": 110228,
                      "password": "review-fixture-password"})["token"]
    name = "隔离成果历史核对-" + uuid.uuid4().hex[:8]
    original = "首次提交的成果正文"
    added = request(args.base, "/achievement/add", uploader, payload(name, original))
    ach_id = int(re.search(r"成果ID[：:]\s*(\d+)", str(added)).group(1))
    records = request(args.base, f"/achievement/audit/achievement/{ach_id}", admin)
    old = one(records, 10)
    assert old["achievementComment"] == original
    assert old["historicalDetailsAvailable"] is True
    request(args.base, "/achievement/audit", admin,
            {"sub_id": old["subId"], "is_pass": True, "title": "同意"})

    revised = "修改后重新送审的成果正文"
    request(args.base, f"/achievement/update/{ach_id}", uploader, payload(name + "-修订", revised))
    records = request(args.base, f"/achievement/audit/achievement/{ach_id}", admin)
    previous = next(row for row in records if row["subId"] == old["subId"])
    current = one(records, 10)
    assert previous["achievementComment"] == original, previous
    assert previous["achName"] == name, previous
    assert previous["flowStatus"] == 30
    assert current["achievementComment"] == revised, current
    assert current["achName"] == name + "-修订", current
    assert current["subId"] != old["subId"]

    request(args.base, "/achievement/audit", admin,
            {"sub_id": current["subId"], "is_pass": False, "title": "退回补正"})
    request(args.base, f"/achievement/update/{ach_id}", uploader,
            payload(name + "-补正", "退回补正后的正文"))
    request(args.base, f"/achievement/{ach_id}/resubmit", uploader, {})
    records = request(args.base, f"/achievement/audit/achievement/{ach_id}", admin)
    assert next(row for row in records if row["subId"] == old["subId"])["achievementComment"] == original
    assert next(row for row in records if row["subId"] == current["subId"])["achievementComment"] == revised
    assert one(records, 10)["achievementComment"] == "退回补正后的正文"
    print(json.dumps({"result": "PASS", "achievementId": ach_id,
                      "archivedSubmissionId": old["subId"],
                      "rejectedSubmissionId": current["subId"],
                      "submissions": len(records)}, ensure_ascii=False))


if __name__ == "__main__":
    main()
