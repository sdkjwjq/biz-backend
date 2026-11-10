# 双高平台统一身份认证校内联调分支

分支：`campus-sso-test-20261110`。这是供学校内网测试的**源码分支**，不是线上更新包。

本仓库同时包含 Spring Boot 后端（仓库根目录）和 Vue 前端（`frontend/`）。只保留现有程序源码、SSO 改动、针对性测试和联调说明；没有线上数据库备份、模拟认证服务、截图、编译产物或真实学校应用密钥。

## 从空目录获取

```powershell
git clone --branch campus-sso-test-20261110 --single-branch git@github.com:sdkjwjq/biz-backend.git shuanggao-sso-test
cd shuanggao-sso-test
git branch --show-current
```

如学校电脑使用 HTTPS Git 凭据，可将上面的 SSH 仓库地址替换为 `https://github.com/sdkjwjq/biz-backend.git`。

## 构建检查

需要 Java 17、Maven、Node.js、npm 和可访问的**隔离 MySQL 数据库**。

```powershell
mvn -q -Dtest=SchoolSsoServiceTest test
mvn -q -DskipTests package
cd frontend
npm ci
npx eslint src/api/user.js src/router/index.js src/views/Home.vue src/views/Login.vue src/views/ReviewRoleChoice.vue src/views/SsoLogout.vue
npm run build
```

`application.properties` 中数据库地址、用户名、密码均需由 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` 环境变量显式提供；默认不连接任何数据库。定时任务默认关闭。**不要指向线上 `biz` 库。**

## 启动隔离环境

先确保学校信息中心允许登记一个供测试使用的**精确回调地址**，且该地址的浏览器页面能到达本机前端。如果学校只登记了生产地址 `http://172.19.2.81/`，本地环境无法收到授权回调；此时先完成只读预检，不要替换线上服务。

在一个私有 PowerShell 窗口设置以下变量后，从仓库根目录启动后端。方括号内容由本人在学校电脑输入，**不要写进文件或聊天**：

```powershell
$env:DB_URL='jdbc:mysql://127.0.0.1:3306/[隔离库名]?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai'
$env:DB_USERNAME='[隔离库账号]'
$dbPassword=Read-Host '隔离数据库密码' -AsSecureString
$env:DB_PASSWORD=[System.Net.NetworkCredential]::new('', $dbPassword).Password
Remove-Variable dbPassword
$env:SSO_ENABLED='true'
$env:SSO_CLIENT_ID='[学校提供的 app_key]'
$ssoSecret=Read-Host '学校 app_secret' -AsSecureString
$env:SSO_CLIENT_SECRET=[System.Net.NetworkCredential]::new('', $ssoSecret).Password
Remove-Variable ssoSecret
$env:SSO_CAS_BASE_URL='http://ca.xzcit.cn/cas'
$env:SSO_REDIRECT_URI='[信息中心登记的完整测试回调地址]'
mvn spring-boot:run
```

上述命令用不回显的交互输入接收密码和密钥；不要由 Agent 读取、生成、记录或回传。测试结束后关闭该 PowerShell 窗口。若学校确认有 HTTPS 认证基址，优先核对后使用学校指定的地址。后端默认端口 8080、接口前缀 `/api`。在另一个窗口从 `frontend/` 启动前端：

```powershell
npm run dev -- --host 127.0.0.1 --port 5175
```

若登记的回调是这台电脑的内网 IP，则把前端 `--host` 改为对应 IP 或 `0.0.0.0`，并检查防火墙。`SSO_REDIRECT_URI` 与登记值须完全一致，包括协议、端口及末尾 `/`。

## 联调步骤和回传

直接把 [docs/统一认证校内联调提示词20261110.md](docs/统一认证校内联调提示词20261110.md) 全文交给学校电脑上的 Agent。它会先核对学校网络与回调登记，再用本人学校账号完成浏览器登录、角色选择、原业务页和双端退出，并生成脱敏结果。`docs/统一身份认证本地接入说明20261110.md` 是接口与配置说明，`docs/本地模拟浏览器测试结果.json` 仅为本地模拟结果。
