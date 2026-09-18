# 收尾修复交付（2026-09-18）

## 结果

上一轮剩余 **8 项 BUG 全部修复**，六个未上线原型页面关闭直达访问，统一跳转到当前账号首页。已完成本地测试、构建及 Git 记录，未部署。预算和此前明确忽略事项保持原约定；材料仍按客户要求保留当前及上一次提交。

## 改动与提交

| 问题 | 修复 | 仓库 / 提交 |
|---|---|---|
| 5-1 跨账号缓存 | 按登录会话隔离缓存；登录、退出、当前会话失效时清理；旧请求不得写入新会话，旧 401 不得清除新 token 或跳转登录页 | biz：`2c04d87`、`19cfd97` |
| 5-2 通知分页 | 筛选后按 10 条切片；标签切换回首页；已读操作减少列表时修正页码 | biz：`613d833` |
| 5-3 缓存深链接 | 缓存和远程加载均在渲染后定位任务，卸载后不再打开详情 | biz：`6788449` |
| 5-4 待办角标 | 汇总任务、绩效和成果，沿用各业务待办状态码 | biz：`14009e9` |
| 5-5 首页轮询 | 卸载清理定时器；请求进行中不叠加轮询；过期组件不更新数量 | biz：`950299c` |
| 24-1 部门统计 | 整体、年度、中期及汇总六条路径统一为有效三级任务范围 | biz-backend：`6b2e527` |
| 24-2 比较数据 | SQL 汇总实际年份和一至三级任务，删除任务不计入，比例由真实数量计算 | biz-backend：`5f982d9` |
| 24-3 扩展名 | 上传和任务提交均按不区分大小写校验 PDF/DOC/DOCX，存储规范化后缀 | biz-backend：`365375f` |
| 原型页面 | board、data、query、system/user、system/permission、system/log 重定向；保留正式顶部改密入口 | biz：`c442b62` |

附属清理：biz `0dbcf98` 删除已有未使用的计算属性，使本次涉及文件静态检查通过。测试：biz-backend `1449bde`。

## 本地验证

- **33 项后端 HTTP/MySQL 隔离回归全部通过**：原有 31 项加部门状态分布和真实对比两项。覆盖四种任务状态、删除记录、六个部门统计入口、空数据、不同年度和层级、33.33% 比例。
- Playwright：A 退出后 B 的请求延迟期间不显示 A 任务；HTTP 401 和业务 401 的旧请求均不影响 B 会话；旧缓存写入隔离。
- 通知：15 条分成 10＋5 条且不重复，5 条已读只有一页；末页逐条已读后回到第一页，全部已读后出现空状态。末页边界用真实通知构造全未读响应，已读操作调用真实接口。
- 深链接：冷加载、缓存刷新均打开任务详情。轮询：大屏往返前后每周期均一次通知请求。
- 待办：真实审核人 15 条绩效待办与角标一致；另外用合成响应验证三类共 6 条待办、历史状态不计数。
- 文件：PDF、DoC、DOCX、pdf 共四组真实上传→提交→查询审核单及任务值→撤回成功；无后缀、exe、pdf.exe 均拒绝，共 7 组。
- 综合巡检：**31 次页面访问、38 个读取接口检查、41 项鉴权检查通过**；正式页面未捕获异常为 0；六个原型路由及带旧改密查询参数的地址均回到首页。
- 前端生产构建、后端打包、7 个修改文件 ESLint、Git diff 空白检查通过。前端保留已有大包体警告。

首轮新增测试对空部门的预期不符合原 SQL 的 `HAVING COUNT > 0` 约定，修正测试后全量重跑为 33/33；没有改变空部门接口行为。综合脚本改为等待组件及路由完成，并按 pathname 判断带查询参数的重定向，最后整轮重跑通过。

证据：[本轮目录](audit-evidence/2026-09-18/closure-fixed/)、[后端结果](audit-evidence/2026-09-18/closure-fixed/backend-regression.txt)、[综合结果](audit-evidence/2026-09-18/closure-fixed/sweep.json)、[会话与分页边界](audit-evidence/2026-09-18/closure-fixed/edges.json)。历史复现证据未覆盖。

### 复跑

在本机配置测试密码环境变量后：

```powershell
python scripts/run-review-regression.py
python scripts/ui-audit/serve.py --fixture review
```

另一终端依次运行（通知边界放在普通通知用例之后，因为会改变合成通知已读状态）：

```powershell
node scripts/ui-audit/closure-frontend-fixed.cjs cache
node scripts/ui-audit/closure-frontend-fixed.cjs notices
node scripts/ui-audit/closure-frontend-fixed.cjs deep-link
node scripts/ui-audit/closure-frontend-fixed.cjs polling
node scripts/ui-audit/closure-badge-fixed.cjs
node scripts/ui-audit/closure-edges-fixed.cjs
node scripts/ui-audit/closure-upload-fixed.cjs
node scripts/ui-audit/closure-sweep-fixed.cjs
node scripts/ui-audit/closure-auth.cjs
New-Item -ItemType File -Path target/ui-audit/STOP -Force
```

只运行受隔离库守卫保护的测试；不要直接运行依赖业务库的两份旧 API 测试。浏览器脚本使用已有 `target/ui-audit-tools` Playwright 和本机 Edge。

## 构建产物与部署

产物目录：`target/closure-release-20260918/`。

| 文件 | SHA256 |
|---|---|
| frontend.zip | `D9288AB5BB0042B22AADA6FD92280275727B0E6116C06542FA6EDB129AE9B5D0` |
| biz_backend-1.0-SNAPSHOT.jar | `322EF6547AFA564C843B758C2F3F97A945599AF600E9193E07D39D98256DB527` |

本地构建包含用户原有未提交工作；这些工作未被覆盖或混入本次 Git 提交。正式发布前仍需按现有发布流程核对这些本地改动。

1. 备份当前前端目录、后端 JAR 及现有启动配置，记录版本。
2. 按现有服务方式更新后端 JAR，再更新前端静态文件；保留环境配置、凭据、uploads、日志及反向代理设置。
3. 启动后验证登录切换、通知分页、审核角标、统计对比及大写扩展名上传提交。
4. 本次接口路径和数据库结构不变，**无需迁移或回滚 SQL**。

回滚优先恢复上一版本构建产物并按原方式重启。源码回滚在干净分支中对上表相关提交按逆序 `git revert`，同时调整对应测试；不用硬重置，不在用户当前脏工作区执行回滚。

## 清理与收尾范围

两次 UI 临时服务和本次隔离测试库均已清理，保留测试证据及构建产物。真实业务库仅用于读取建表结构，测试业务数据全部为合成数据。

本轮已知待修清单清零，可结束常规巡检。真实规模压力、长时间运行/故障恢复、生产历史数据全量核对及发布回滚演练仍属于未验证的上线验收范围，不承诺项目不存在未知缺陷。
