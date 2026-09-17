# 第二十三批修复

提交 `fcace47`，三个问题共用部门详情计算路径。

- 23-1：直接读取部门配置的负责人，避免将部门 ID 当用户 ID；未配置、负责人不存在或已删除时不返回失效负责人。
- 23-2：总体、年度、中期均排除已删除任务；保留原有层级统计口径和历史空删除标记兼容。
- 23-3：中期比较年份前判空，按用户确认不计入中期；总体仍计入有效任务，不修改历史年份。

`scripts/ui-audit/batch-23-fixed.py` 三项验证通过，包括同号账号干扰、删除负责人、三种统计过滤、空年份以及无任务时比例为零。31 项隔离回归通过，后端打包成功；Python 语法和 Git 差异检查通过。

[定向证据](fix-evidence/batch-23/results.json) · [回归摘要](fix-evidence/batch-23/backend-regression.txt)

复跑：设置 `SHUANGGAO_TEST_DB_PASSWORD`，运行 `python scripts/run-review-regression.py`；启动 `python scripts/ui-audit/serve.py --fixture review` 后执行定向脚本。结束创建 `target/ui-audit/STOP`，等待临时服务和随机库清理。本批只用合成数据，旧审计证据保留。

未部署；产物 `target/biz_backend-1.0-SNAPSHOT.jar`。无数据库迁移、无需前端更新，按现有后端流程发布。回滚执行 `git revert fcace47` 后重新打包或恢复上一版产物，不硬重置，不影响既有未提交工作。

本批定向测试库与后续收尾巡检使用的随机库均已清理，临时服务已停止。收尾结果见 [覆盖与剩余清单](audit-closure-2026-09-17.md)。
