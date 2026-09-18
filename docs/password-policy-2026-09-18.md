# 登录强制改密交付

## 已确认的规则

仅当前密码不满足新规则的账号强制修改，包含管理员、普通用户、部门负责人和成果账号。新密码至少 6 位，包含 ASCII 大写字母、小写字母及数字；不额外限制特殊字符。已经合规的账号正常进入。

弹窗沿用截图文案，提供新密码、确认密码和保存按钮，不允许关闭、Esc 或点击遮罩跳过。保存成功后刷新当前页面恢复使用，后续登录不再重复要求修改。

## 改动范围

- 前端 `c2172b2`：新增全局强制改密弹窗和共享规则；路由进入前核对密码状态；待改密期间不挂载业务页面、暂停首页请求；直接访问大屏转到账号首页；普通主动改密也使用新规则。
- 后端 `fe04012`：新增 PasswordPolicy；登录结果增加 `requiresPasswordChange`；新增 `GET /system/password/status`；复用现有 `POST /system/password`；JWT 拦截器检查数据库当前密码，不依赖客户端标记或新 token。
- 待改密账号请求受保护业务接口返回 HTTP 428 和原 ErrorVO 结构；放行改密、状态查询和注销。密码不合规返回 HTTP 400，原密码不变。
- 账号管理的新增/重置密码保留原有兼容规则，可设置初始密码；若不符合新规则，使用该密码登录后同样进入强制改密流程。
- 不修改历史密码、数据库结构、原有角色规则或静态附件访问规则；无需迁移或回滚 SQL。

## 验证

- **35 项真实 HTTP/MySQL 隔离回归通过**。四类角色、已有 token 在密码变弱后的限制、匿名/非法 token、弱密码拒绝且原值不变、保存后放行、旧密码登录失败均覆盖。
- Playwright 四个账号通过：管理员、普通账号、成果账号强制流程，以及预先合规账号免弹窗。
- 覆盖刷新、直接访问大屏、关闭入口缺失、Esc、遮罩点击、5 类非法密码、确认不一致、失败重试、保存期间重复 Enter、成功后重载、390px 适配；未捕获页面异常为 0。
- 前端构建、后端打包、修改文件 ESLint 和暂存文件空白检查通过。前端保留原有包体大小警告。
- 测试只修改合成数据；真实本地管理员只核对登录和密码状态，没有替用户改密。

证据：[测试结果](audit-evidence/2026-09-18/password-policy/results.json)、[后端回归](audit-evidence/2026-09-18/password-policy/backend-regression.txt)、[初始弹窗](audit-evidence/2026-09-18/password-policy/required-initial.png)、[手机适配](audit-evidence/2026-09-18/password-policy/required-mobile.png)。

复跑：先运行 `python scripts/run-review-regression.py`，再启动 `python scripts/ui-audit/serve.py --fixture review --frontend-port 15273`，执行 `node scripts/ui-audit/password-policy-fixed.cjs`。全新隔离库中的历史弱密码作为测试起点，脚本会实际修改合成账号密码，重复测试需重新创建隔离库。结束创建 `target/ui-audit/STOP` 文件。原 15173 端口被另一项目使用，本次未停止该项目，测试启动器新增可选前端端口。

## 本地运行和产物

本地前端：http://127.0.0.1:5173/ ，后端：8080，连接既有本地 biz 数据库。后端已重新打包并重启；前端使用现有 Vite 服务。管理员当前返回 `requiresPasswordChange=true`，刷新即可测试。

产物：`target/password-policy-release-20260918/frontend.zip`、`biz_backend-1.0-SNAPSHOT.jar`；更新前后端 JAR 备份为同目录 `previous-backend.jar`。原有未提交工作保留，构建产物包含当前工作区内容。临时 UI 服务和测试库均已清理，手动测试服务继续运行。

正式部署应在维护窗口先更新后端、再更新前端，保留原环境配置、上传目录与日志。回滚可恢复此前前后端产物，或在干净分支逆向提交上述两个功能提交；用户已经保存的新密码继续有效，不回改密码数据。

## 本次发现的原有问题（未纳入修复）

本地 `token_blacklist.token` 为 varchar(255)，当前 JWT 超过该长度时，原注销接口插入黑名单失败。前端仍按原逻辑清除本地登录；不能声称服务端旧 token 一定失效。本次未修改黑名单表或扩大数据库变更。

首轮测试的“注销后 token 必须失效”断言发现此问题；最终测试只将本功能负责的“弱密码门禁允许进入注销处理器”纳入验收，没有将原注销缺陷计为修复。若后续修复，应单独扩展字段并提供迁移、回滚及注销回归。
