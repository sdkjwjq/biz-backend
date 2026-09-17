# 第二十一批修复

- `09c6d4f`：任务管理更新增加事务并调用既有绩效汇总入口；该入口全量计算自动绩效，可覆盖旧年度关联失效情况，手动绩效按原规则跳过。新增任务仅允许 level=3，非法值明确拒绝，不自动纠正。
- `011db9f`：预警 DTO 新增可选 `to_user_id`，提供时按 ID 定位；旧姓名参数保留，查询未删除人员，同名明确提示使用 ID，唯一姓名正常发送；不存在、已删除及空接收人拒绝，不落通知。旧错误结构保留，无数据库变更。

## 验证

`scripts/ui-audit/batch-21-fixed.py` 三项定向验证全部通过：任务实际值 7 即时同步绩效及年度值 7；隔离库临时触发器使绩效刷新失败时，任务、绩效及年度表全部回滚。触发器在 finally 中移除。六种非法新增层级均拒绝且不写库，三级正常创建。预警覆盖同名不写入、指定 ID 精确发送、唯一有效姓名发送，以及四种非法/失效接收人拒绝。

31 项后端隔离回归全部通过；后端打包、Python 语法和 Git 差异检查通过。无前端变更，本批未运行浏览器检查。

[定向证据](fix-evidence/batch-21/results.json) · [回归摘要](fix-evidence/batch-21/backend-regression.txt)

复跑：设置 `SHUANGGAO_TEST_DB_PASSWORD`，先运行 `python scripts/run-review-regression.py`，启动 `python scripts/ui-audit/serve.py --fixture review` 后执行 `python scripts/ui-audit/batch-21-fixed.py`。结束创建 `target/ui-audit/STOP` 并等待随机库删除。只使用合成数据，业务库不写入；旧审计证据保留。

## 交付

未部署。产物 `target/biz_backend-1.0-SNAPSHOT.jar`，按现有后端流程发布，无 SQL 或前端更新。回滚使用对应提交的 `git revert` 后重新打包，或恢复上版产物；不使用硬重置，保留既有未提交工作。按用户要求，修复完成后继续下一批审计。

本批测试服务及随机数据库已清理，后续审计使用全新隔离库。
