# 第二十三批：部门统计详情

基线后端 `b65f1da`。按用户要求，在第二十二批修复和闭环后继续检查；本批不修改业务代码，预算及已忽略事项不涉及。

后续状态：用户确认三项修复，空年份不计入中期；见 [修复记录](fix-batch-23.md)。

## 23-1【中 / BUG】部门已配置负责人，但详情没有返回

- 位置：`service/BizService.java` 的 `getDeptStatsDetail`，调用 `sysMapper.getDeptLeaderId(deptId)`；`mapper/SysMapper.java` 对应查询实际接收 userId。
- 合成部门 920001 明确配置 leader_id=910002，详情接口却没有 leaderId 和 leaderName。
- 原因：把部门 ID 当用户 ID 查询“该用户所在部门的负责人”。
- 影响：部门负责人信息缺失；若恰有同号账号，还可能取到其他部门负责人（后者为代码推导，本批未实测）。
- 建议：直接根据部门 ID 获取部门实体的 leaderId，再读取有效负责人信息；无负责人时正常返回。

## 23-2【中 / BUG】部门详情仍统计已删除任务

- 位置：`BizService.calculateDeptCompletionRate`；`BizMapper.getTasksByDeptId`、`getTasksByDeptIdAndPhase`。
- 合成部门初始总数 3；将其中一条任务置为删除、已完成后，详情仍返回总数 3、完成数 1，而未删除任务只有 2 且没有完成记录。
- 原因：详情的任务查询未过滤 is_delete，全部直接参与统计。
- 影响：总数、完成数及相应比例包含无效记录。
- 建议：部门详情统计统一过滤已删除任务，覆盖总体、年度和中期；统计是否只取三级属于另一个口径问题，本批不擅自改变或将其列为已确认错误。

## 23-3【中 / BUG】任务缺少年份导致整个部门详情失败

- 位置：`BizService.calculateDeptCompletionRate` 中 `task.getPhase() < endYear`。
- 在随机库为一条有效任务构造 phase=NULL，数据库允许保存；调用部门详情返回 code=500，提示对 getPhase() 返回值拆箱时空指针。
- 原因：中期筛选直接比较可空年份；部门详情同时计算三种统计，中期计算异常使总体、年度结果也无法返回。
- 影响：一条年份缺省的历史任务阻断整个部门详情。
- 建议：中期计算先处理空年份，其他统计正常返回；缺省年份是否计入中期的业务口径待确认，不自行补年份或修改历史数据。

## 验证与边界

`scripts/ui-audit/batch-23-audit.py` 三项断言通过；[真实接口及数据库证据](audit-evidence/2026-09-17/batch-23/results.json)。设置 `SHUANGGAO_TEST_DB_PASSWORD`，启动全新 `python scripts/ui-audit/serve.py --fixture review` 后执行该脚本。仅随机 `biz_review_test_*` 库，全部合成数据；删除状态及空年份直接在隔离库构造，代表数据库允许的历史状态，不声称由现有页面操作产生。

本批验证的是 `/dashboard/dept/{deptId}`，不扩大到其他统计接口或当前大屏页面表现。未运行浏览器检查和无关构建。Python 语法与 Git 差异检查通过，临时服务、随机库已清理；原始未提交工作保留。
