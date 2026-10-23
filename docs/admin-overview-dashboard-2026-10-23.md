# 综合数据看板

## 使用入口

管理员或已配置查阅范围的校级领导：管理中心 → 管理概览（`/home/admin/overview`）。

1. 选择年度、月份，页面显示任务、绩效、核心指标、成果、预算和工作纪实。
2. 点击任意统计数字，在分析抽屉中切换维度。
3. 点击图表或下方分组按钮，筛选明细；关闭筛选标签恢复完整结果。
4. 明细每页10条，可进入现有业务详情。核心指标进入单项历史；未提交人员没有纪实详情入口。
5. 下方展开“运行维护”，查看账号、业务配置异常、待办异常及预算版本。领导保持只读。

## 时间与计算口径

- 任务、绩效为所选年度的当前状态，不是历史月末快照。任务只计未删除三级任务，重点、困难及逾期可重叠。完成率按任务数量，平均推进进度按数据库保存进度。
- 任务逾期仍按所属年度年末判断，不使用新增计划日期替换原逾期规则。
- 绩效排除分类目录，按年度目标、年度完成值比较；缺少年度配置、无有效目标或完成值列入待核实，不汇总不同单位数值。
- 核心指标只使用同年度、不晚于所选月份、与当前映射一致的最近有效归档。保留归档时名称及目标快照；未归档留空，映射异常单列。
- 成果按取得日期所属年度统计，状态为当前审核状态。成果条数与已归档奖项数量分开；缺少取得日期另行提示。驳回明细保留原因。
- 预算只使用所选年月当前有效版本，无数据提示“尚未导入”。年度预算安排按资金来源汇总；改革任务对比展示分项预算，二者不相互替代。金额为万元。
- 趋势图复用原预算趋势服务，只展示截至所选月份的数据；缺月不补、不跨缺月计算新增值，负增长提示可能调账。原预算页继续隐藏趋势图。
- 纪实应填、已提交、未提交、提交率使用同一当前资格集合；失去资格前留下的已提交记录单列，可查阅原冻结快照。未提交名单不查询或暴露草稿。
- 分母为零、无有效完成值显示“—”；请求失败显示错误及重试按钮，不伪装为零。明细数值最多显示4位小数。

## 接口与权限

- `GET /admin/overview/summary?year=2026&month=4`：六区汇总、查阅范围及生成时间。可带 `module=tasks|performance|core|achievements|budget|records` 单区重试。
- `GET /admin/overview/analysis?year=2026&month=4&metric=tasks.total&dimension=department&page=1&pageSize=10`：预定义指标的图表和分页明细；可带图表返回的 `groupKey`。页大小1—100，超出最后一页自动纠正。
- 原 `GET /admin/overview` 保留为运行维护读取接口。
- 仅为上述两个新增 GET 路径增加领导权限例外；任务和绩效仍按数据库中的查阅范围过滤。普通用户无权访问，领导不能调用管理写接口。
- 所有统计只读，不触发任务/绩效重算、保存草稿或生成业务记录。本次无数据库迁移。

## 复验命令

在后端目录执行，数据库密码通过本地环境变量 `SHUANGGAO_TEST_DB_PASSWORD` 提供，不写入脚本。

```powershell
python scripts/run-review-regression.py overview
python scripts/run-review-regression.py
python scripts/run-work-record-regression.py
python scripts/ui-audit/serve.py --fixture overview --frontend-port 15173
# 另一个终端：
node scripts/ui-audit/overview-dashboard.cjs
# 验证后停止临时服务并自动删除随机测试库：
New-Item -ItemType File target/ui-audit/STOP -Force
```

临时服务为18080/15173，测试库限定为 `biz_review_test_<32位随机十六进制>`，不复制或修改本地业务数据。Playwright 结果及截图在 `target/ui-audit/overview-dashboard/`。

## 本次验证结果

| 检查 | 结果 |
| --- | --- |
| 后端业务隔离回归 | 116项通过，含新增看板9项 |
| 工作纪实隔离回归 | 35项通过 |
| 看板 Playwright | 12组通过，浏览器运行异常0个 |
| 既有页面 Playwright | 8组通过，覆盖领导查阅、六页筛选、成果长名称、财务真实导入及预算版本浏览 |
| 前端变更文件 ESLint | 通过 |
| 前端正式构建／后端打包 | 均通过；前端仍有大体积 chunk 提示，无构建失败 |

真实接口场景使用随机隔离库及合成人员、任务、成果、归档和预算数据，核对首页数字、图表、分页与分组，验证任务去重、绩效缺失配置、核心指标月份与旧映射排除、奖项数量、驳回原因、纪实资格交集、草稿隐私、领导范围变更与旧 Token、详情跳转、零数据及零分母、窄屏滚动。

异常模拟另行区分：Playwright 注入慢年度响应、单区统计失败、分析失败及关闭后的旧响应；后端隔离用例临时改名成果表验证模块独立失败，恢复后重试。缺少取得日期的历史兼容场景只在隔离库临时模拟可空字段，未修改正式库结构。预算恢复由既有版本回归覆盖。

最终日志：`target/overview-regression-all.log`、`target/overview-records-regression.log`、`target/overview-playwright-final-r3.log`、`target/overview-presentation-regression.log`。截图：`target/ui-audit/overview-dashboard/dashboard-desktop.png`、`budget-trend.png`、`dashboard-narrow-drawer.png`。

所有本次临时服务已停止，随机测试库已删除；原本的本地测试服务及未提交修改保留。

功能提交：后端 `ca9cd7d`、`dc48008`；前端 `de84393`、`084757e`。自动化夹具和交付说明单独提交。

## 发布与回退

本次不制作更新包、不部署线上。后续发布时先后端、再前端；无 SQL 迁移。

回退使用本功能提交的反向提交或上一版应用产物，不硬重置，不影响其他未提交工作。新增统计接口和图表均不改变业务数据，因此无业务数据恢复步骤。

仅回退应用代码时，在对应仓库按新到旧执行；若与后续改动发生冲突，逐项处理后重新构建：

```powershell
# 前端仓库
git revert 084757e
git revert de84393
# 后端仓库
git revert dc48008
git revert ca9cd7d
```

如同时撤销测试支持，先反向提交本次后端的 `test: verify overview dashboard and document delivery` 提交。以上仅为回退说明，本次未执行回退。
