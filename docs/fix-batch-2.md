# 第二批后端修复记录

本批范围：跨年度绩效恢复、任务撤回边界和事务、绩效审核状态、年度任务启用。沿用现有接口和业务分层，每项验证通过后单独提交 Git。

## 1. 跨年度绩效恢复

- 改动：`PerformanceService.restorePerformanceSnapshot` 只恢复该年度实际值，再复用年度汇总方法更新总值；保留当前年度目标配置。
- 修复前复现：2026 年填 3、2027 年填 5，撤回 2026 年后总值错误变为 0。
- 验证通过：`performanceRollbackPreservesOtherYearsAndCurrentTargets`，覆盖撤回/退回 × 数值求和/百分比取最大值四种组合，另一年度、其审批单及当前目标均保留。

## 复测方式

在 `biz-backend` 目录设置进程环境变量 `SHUANGGAO_TEST_DB_PASSWORD` 后执行：

```powershell
python scripts/run-review-regression.py performanceRollbackPreservesOtherYearsAndCurrentTargets
```

不传方法名则运行整批隔离回归。脚本只读取本地 `biz` 表结构，在随机测试库内生成合成数据并启动随机端口 HTTP 服务，结束后自动删除测试库。测试源码和本文不记录本地数据库密码。

## 部署及回滚

本批无数据库结构变更或历史数据批量修改，无需迁移/回滚 SQL。部署沿用现有后端 JAR 和外置配置；回滚恢复原 JAR，代码可按独立提交逐项 revert。本次未部署或推送远端。
