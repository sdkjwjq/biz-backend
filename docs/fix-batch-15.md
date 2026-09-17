# 第十五批修复：账号状态保存与密码校验

用户选择：15-1 忽略；15-2、15-3 修复。状态兼容方案经用户明确确认。

## 范围与提交

| 问题 | 改动 | Git |
|---|---|---|
| 15-2 | 新增/修改显式传入 0、1 时保存原值；修改缺省/null 保留数据库原状态；新增缺省/null 保留默认 1；非法值返回 HTTP 400，不写入 | 后端 `df3c9f7` |
| 15-3 | 改密拒绝缺失/null、空字符串、全空白及不足 6 位；返回 HTTP 400 和 ErrorVO，保留原密码；合法密码不自动去掉空格 | 后端 `08ba7b2` |
| 15-3 页面配套 | 全空格密码在提交前显示“请输入新密码” | 前端 `a3dd058` |

不改状态与登录的关系，不批量改历史数据，不处理降权旧凭证问题，不引入旧密码验证。现有成功响应、接口地址和数据库结构不变。其他原有未提交改动保留。

## 验证

- 后端专项 `passwordValidationPreservesExistingCredentials`：6 类非法请求返回 400，数据库密码不变且原密码仍可登录；合法 6 位及含首尾空格密码保存、登录成功，旧密码失效。
- 后端专项 `accountStatusPreservesExplicitAndOmittedValues`：新增默认/显式状态，0→1→0 切换，缺省/null 更新保留原值；4 类非法状态在新增和修改均拒绝且没有业务写入；两种状态的登录兼容行为保留。
- Playwright + Edge：真实页面拦截空字符串、全空格、5 位密码，均未调用改密接口；6 位密码真实保存后跳回登录页并重新登录成功，pageerror 为空。
- 前端生产构建通过，仍有既有大分块警告。
- 完整后端隔离回归 25 项通过，失败/错误/跳过均为 0；后端打包通过。
- Home.vue ESLint 有一条历史 `currentUserId` 未使用错误（236 行）；对修改前 HEAD 执行相同检查也出现同一错误。本次未新增 lint 错误，未修改无关代码。

[测试结果与截图](fix-evidence/batch-15/)。原始审计证据继续保留。

收尾：专项、完整回归及浏览器使用的临时数据库均已删除，临时前后端服务已停止。

复测使用 `scripts/run-review-regression.py` 和 `scripts/ui-audit/serve.py --fixture review` 建立随机隔离库；页面脚本为 `scripts/ui-audit/batch-15-password-fixed.cjs`。原始 batch-15-audit.cjs 用于复现旧缺陷，修复后不能作为通过标准。

## 部署与回滚

仅生成本地产物，未部署。更新后端后再更新前端。前端 `biz/dist` 含原有本地未提交工作，发布前需要一并审核；后端产物为 `biz-backend/target/biz_backend-1.0-SNAPSHOT.jar`。无需数据库迁移。

在干净发布分支按需反向提交：前端 `git revert a3dd058`；后端先 `git revert df3c9f7`，再 `git revert 08ba7b2`。也可恢复上一版本产物。无需回滚 SQL，不硬重置当前工作区。
