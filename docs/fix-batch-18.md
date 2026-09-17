# 第十八批：成果并发审核修复

## 范围与实现

仅处理 18-2。按用户要求，预算模块全部忽略，18-3 不修改；保留其他未提交工作。

成果审核原先普通读取待审状态，两个并发请求可同时通过检查，分别写入相互矛盾的结果、日志与通知。

新增 `AchievementMapper.getAchievementSubmissionByIdForUpdate`，仅供 `AchievementService.auditAchievement` 在已有事务中使用。获取行锁后沿用原状态校验；同一审核单的后续请求等待前一个事务完成，再读取最新状态，返回现有“当前状态不可审核”错误，不再产生第二次业务写入。普通详情及日志查询保持现状。

接口、数据库结构与成果退回重提规则未变，无数据库迁移。锁仅覆盖当前审核单，释放依赖已有事务提交或回滚。

## 闭环验证

- 现有后端隔离回归：31 项通过，失败 0、错误 0。
- 新增真实 HTTP/MySQL 并发检查：通过＋退回、通过＋通过、退回＋退回，三组均通过。测试先由独立事务持有目标行锁，确认两个 HTTP 请求都进入锁等待后释放，确保请求实际重叠。
- 每组仅一个成功结果，另一个返回状态不可审核；成果及审核单状态、处理人一致，审核日志和提交人通知各新增一条。
- 每组再反向重试一次，三次均被拒绝，审核单、成果状态、审核日志与通知保持不变。
- 后端 `mvn.cmd -B -DskipTests package` 成功。前端无改动；本次验证为 API/数据库闭环，未执行浏览器检查。

脚本：`scripts/ui-audit/batch-18-approval-fixed.py`。复跑前设置 `SHUANGGAO_TEST_DB_PASSWORD`，运行 `python scripts/run-review-regression.py` 编译并验证，再通过 `python scripts/ui-audit/serve.py --fixture review` 启动隔离服务，执行上述脚本。结束时创建 `target/ui-audit/STOP`，等待服务清理随机数据库。

只使用合成数据及随机 `biz_review_test_*` 数据库；原始审计脚本及证据保留。详见 [并发结果](fix-evidence/batch-18/results.json) 与 [回归摘要](fix-evidence/batch-18/backend-regression.txt)。

## 部署与回滚

本次未部署。后端产物：`target/biz_backend-1.0-SNAPSHOT.jar`；按现有后端更新流程替换并重启，无前端更新或 SQL。

业务修复提交：`c9d5da6`。回滚可执行 `git revert c9d5da6` 后重新打包，或恢复上一版本后端产物；不使用硬重置，不影响既有未提交工作。回滚会恢复原并发缺陷。

交付前已停止临时服务并删除随机测试数据库；Python 语法与提交差异检查通过。
