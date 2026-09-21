# 工作纪实：双高办与管理员填报能力开放（2026-09-21）

## 变更内容

此前工作纪实仅"三级任务专业群审核人"（biz_task.auditor_id）可填报，统计范围为本人审核的三级任务。本次按确认口径开放：

- role=0 的管理员和 dept_id=100（"双高"建设办公室）的有效用户可填报工作纪实。
- 这两类账号统计口径为全校该年度全部三级任务，可填年度 2025—2029（仍受"不超过当前月份"限制）。
- 普通审核人口径与体验不变；删除仍仅管理员；导出仍仅管理员及 viewer-user-ids 配置账号。
- 无数据库结构变更，无前端代码变更（能力完全由 /work-records/capabilities 驱动）。

实现要点：

- `WorkRecordService` 新增特权填报人判定（常量 OFFICE_DEPT_ID=100），capabilities、requireFiller、草稿可编辑判定统一走新逻辑。
- `WorkRecordMapper.allLevel3Tasks(year)` 查询全校三级任务；`WorkRecordStatisticsService.calculate` 增加口径参数，所有调用点按填报人身份传入。
- 附带行为（已确认）：双高办成为填报人后按 capabilities 现有逻辑可查看全部已提交纪实；双高办用户若本身也是审核人，统一按全校口径。

## 验证结果

| 验证 | 结果 |
| --- | --- |
| 工作纪实 HTTP/MySQL 专项（WorkRecordApiTest） | 27 项通过：新增管理员/双高办全校口径闭环、权限边界；修复 69a743b 遗留的 428 过期断言（改为 force_password_change 标记驱动） |
| 既有后端隔离回归（run-review-regression.py） | 51 项中 50 项通过；`deletedAchievementsNeverRemainPendingAndCompletedHistorySurvives` 在未修改基线上同样失败，属既有问题，与本次无关 |
| Playwright work-records.cjs | 15 组通过（原 14 组 + 新增双高办全校填报 1 组），浏览器运行时异常 0 |
| Playwright work-record-add.cjs / work-record-delete.cjs | 各在全新隔离环境通过 3 组 / 7 组 |
| 前端生产构建 | 通过；ESLint 3 个错误均为既有未改动文件（Achievement.vue、Budget.vue） |

fixture 变更：`serve.py` work-records 环境新增 dept_id=100 部门与双高办合成账号 910005（仅该隔离环境）；`work-records.cjs` 管理员边界断言同步更新（管理员现在可见"新建纪实"）。证据：`target/ui-audit/evidence-work-records/`（含 office-school-wide.png）。

注意：三个纪实 Playwright 套件各自需要全新隔离环境，同一环境连跑会因合成数据相互污染而失败，属既有约束。

## 回滚

仅代码变更，反向提交即可；无 SQL 迁移、无数据修正。发版打包走 release 一键更新包流程，是否发版另行确认。
