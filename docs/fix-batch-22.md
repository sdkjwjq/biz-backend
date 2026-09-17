# 第二十二批修复

- **22-1 / `8fed24e`**：新增任务复用已有任务层级校验，检查父节点和祖先存在、未删除且无循环，沿用原项目、层级与部门限制。
- **22-2 / `875957a`**：普通通知在写入前校验接收账号存在、未删除；失败明确返回错误，不增加未知停用规则。
- **22-3 / `fe0b2b4`**：月度审核提醒跳过空处理人、不存在及已删除处理人的审核单，记录业务日志；有效处理人按审核单计数，各发一条提醒，结果说明发送人数与跳过记录数。全为异常记录也正常返回。

## 测试

`scripts/ui-audit/batch-22-fixed.py` 三项闭环通过：删除直接父节点或祖先时新增拒绝且无写入，正常父链可创建；空、不存在、已删除接收人均被拒绝，正常账号收到通知；两条有效审核单及两条异常记录混合时，正常审核人收到“2 个待审核任务”的一条通知，跳过 2 条，全部异常时不发通知且不报错。

31 项隔离回归通过，后端打包成功，Python 语法与 Git 差异检查通过。无前端变更，未运行浏览器检查。

[定向结果](fix-evidence/batch-22/results.json) · [回归摘要](fix-evidence/batch-22/backend-regression.txt)

设置 `SHUANGGAO_TEST_DB_PASSWORD` 后，可运行 `python scripts/run-review-regression.py`，启动 `python scripts/ui-audit/serve.py --fixture review` 后执行 `python scripts/ui-audit/batch-22-fixed.py`。结束创建 `target/ui-audit/STOP`，等待服务与随机库清理。全部使用合成数据，原始审计证据及既有未提交工作保留。

## 发布与回滚

未部署。产物 `target/biz_backend-1.0-SNAPSHOT.jar`；沿用现有后端部署流程，无数据库迁移或前端更新。按需 `git revert` 对应修复提交并重新打包，或恢复上一版本产物，不使用硬重置。

按用户要求，闭环后继续审计下一批，新问题先报告再决定是否修复。

本批临时测试服务及随机库已清理；后续审计使用全新隔离库。
