# 右上角姓名乱码修复

## 原因和修复

Home.vue 直接将 `atob` 返回的字节串作为 JSON 文本，JWT 内 UTF-8 中文姓名因此变成乱码。强制改密期间跳过用户目录请求，直接取 JWT 的 username，使该问题更容易出现；普通登录的姓名备用路径也受影响。

前端提交 `a0e229a`：将字节串转换为 Uint8Array，经 UTF-8 TextDecoder 解码后再解析 JSON。格式错误继续沿用原有空值回退。不改变密码、改密触发条件、接口或数据库。

## 验证

Playwright 用合成 JWT 和拦截的 API 响应验证，不访问业务数据：徐忠杰、张老师、AuditAdmin 三种姓名，分别覆盖强制改密及正常登录的备用路径，共 6 个用例。修复前中文乱码可复现，修复后姓名均正确，页面异常为 0。

Home.vue ESLint、暂存差异检查及前端构建通过。原有包体警告保留。本地 Vite 服务已自动更新，用户刷新即可测试。

脚本：`scripts/ui-audit/username-encoding-fixed.cjs`。证据：[修复前后](audit-evidence/2026-09-18/username-encoding/)、[修复后截图](audit-evidence/2026-09-18/username-encoding/after.png)。

回滚可逆向提交 `a0e229a`，无需 SQL。截图中另标出的弹窗尚未获具体异常说明，已询问是重复出现还是布局问题；本次不声称修复未确认的弹窗问题。
