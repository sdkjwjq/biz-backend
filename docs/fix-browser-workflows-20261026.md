# 2026-10-26 浏览器巡检问题修复记录

## 范围

承接 `.run/browser-full-20261025/TEST_REPORT_AND_FIX_PLAN.txt` 中的七项问题。本次恢复此前暂停的修复，不追加容量优化，不制作服务器更新包、不部署线上。

开始前保存了两个仓库的工作区差异及已有文件副本，位置为 `.run/repair-resume-20261026/before` 和同目录的 `*-before.patch`。已有专家评语、Vite 配置、其他测试及操作手册脚本修改不纳入本次提交。

## 修复结果

| 问题 | 处理 |
| --- | --- |
| BUG-01 预算历史沿用旧月份 | 每次打开历史，以主表已生效年月初始化；不恢复上次弹窗的筛选记忆。 |
| BUG-02 网页预算修改在锁定时丢失 | 增加“保存修改”，仅提交原有可编辑单元格；保存形成 EDIT 版本。锁定时明确选择保存、放弃或取消，保存并锁定使用同一事务。 |
| BUG-03 管理员待办角标有数、列表为空 | 保留已单独完成的 `7055eb9` 修复，验证角标与未筛选列表范围一致；不重复改变权限逻辑。 |
| BUG-04 无关联消息导致列表失败 | 保留已有空值保护，补充 `sourceId=null` 通知的真实接口回归。 |
| BUG-05 绩效切年后详情串值 | 加载时拒绝打开旧行；详情绑定指标与年度，响应校验版本；直接链接按目标年度加载，后台刷新保留未提交输入。 |
| BUG-06 绩效驳回后审核人找不到记录 | 通过审核日志纳入实际执行过通过、驳回的审核人，列表、筛选候选及深链定位使用同一判定，继续遵守现有状态与数据权限。 |
| BUG-07 驳回成果修改后不能重新提交 | 保存与重新提交分开；重新提交生成新审核单，保留成果 ID、原填报人、原部门和旧驳回记录。重复或并发提交只成功一次。 |

补测修复了两处离开确认问题：侧栏原本同时使用菜单路由与手动跳转，会打断异步确认，现仅保留菜单路由；数据维护页取消切换后复原页签原生选中状态，能够再次切换。

## 关键保护与兼容

- 新增 `POST /budget/save`，携带年月、`baseVersionNo`、变更单元格及可选 `lockAfterSave`。已有接口继续兼容。
- 预算保存、导入、恢复、锁定共用预算表行锁；旧版本、已锁定返回 409。保存失败保留草稿，不显示成功；无金额变化不创建版本。
- 金额使用精确十进制读取，非空、非负，最多两位小数、16 位整数；未修改的导入数值保留原精度。不增加自动汇总公式。
- 查询、重置、导入、恢复、导出、路由离开及管理页签切换均保护预算草稿；浏览器刷新使用原生未保存提醒。
- EDIT 保存完整快照，保留原 Excel 来源附件；来源附件不冒充编辑后的文件，整表导出读取当前保存结果。
- 新增 `POST /achievement/{id}/resubmit`，事务内锁定成果并检查状态、重复审核单、真实账号及权限；管理员代填日志保留真实操作者。
- 不新增数据库字段或表，不执行业务数据回填，不改写既有审核日志。测试中的业务写入仅发生在随机隔离库。

## 验证

后端隔离回归入口：

```text
python scripts/run-review-regression.py browserrepairs
```

该组共 16 项测试，包括预算保存/版本恢复/精度/无效参数/权限收紧/并发、绩效年度退回恢复、审核人历史及定位、成果退回重提归档/管理员并发代填、无关联通知、管理员角标范围。使用隔离库和合成数据，结束后删除隔离库。

浏览器脚本：`scripts/ui-audit/browser-repair-regression.cjs`。使用正式构建和 Edge/Playwright，读取隔离服务 `state.json`，拒绝非随机测试库名称和非本机地址。

```text
python scripts/ui-audit/serve.py --fixture browser-repairs --run-id browser-repairs --production --frontend-port 15526 --backend-port 18526
```

在另一终端将 `SHUANGGAO_UI_STATE_FILE` 指向 `target/ui-audit-browser-repairs/state.json`，依次运行以下模式（budget 初始化后续模式所需的六月预算）：

```text
node scripts/ui-audit/browser-repair-regression.cjs budget
node scripts/ui-audit/browser-repair-regression.cjs performance
node scripts/ui-audit/browser-repair-regression.cjs achievement
node scripts/ui-audit/browser-repair-regression.cjs audit
node scripts/ui-audit/browser-repair-regression.cjs finance
node scripts/ui-audit/browser-repair-regression.cjs admin-tabs
```

需要现有回归环境、数据库凭据环境变量、Playwright 和 Edge。通过创建该服务目录下的 `STOP` 文件结束服务并清理隔离库；不能指向用户调试库或线上服务。

浏览器覆盖预算“五月历史→六月导入→保存→锁定→刷新→导出”、保存失败重试、保存/放弃/取消离开、管理页签；绩效延迟切年、后台刷新保留草稿、直接链接和同页快速切换；成果修改重提；实际审核人驳回历史；财务编辑及领导只读。

最终结果：后端 16 项测试通过，浏览器六种模式合计 18 项检查通过，均无失败；成功浏览器场景未捕获页面运行异常。实际执行记录和截图位于 `.run/repair-resume-20261026` 及 `biz-backend/target/ui-audit-repair-resume-20261026/browser-repair-evidence`。已用 Excel 读取验证导出文件包含保存后的 222.22。前端对本次涉及文件运行 ESLint，并完成正式构建；后端完成 Maven 打包。

本次为针对七项问题的回归，不声称完成全系统容量测试或全部业务组合穷举。构建仍提示部分既有 JS 包超过 500 KB，不在本轮修复范围。

## 后续发布及回退

业务修复提交：后端 `9199715`；前端预算及离开保护 `8014c87`、成果重提 `b14d749`、绩效年度一致性 `6705d5a`。管理员审核范围已有修复为 `7055eb9`。

本次没有结构迁移，后续获准发布时先后端、后前端。须重新制作完整更新包和校验文件，不能将本地 target JAR 单独替换到旧包后沿用旧校验值。

代码按本次提交使用 `git revert` 回退，保留更早的管理员审核范围修复。回退应用不删除 EDIT 版本、成果新审核单或日志；如后续发生正式业务填报，先备份再评估回退，不使用删表或清历史方式回退。
