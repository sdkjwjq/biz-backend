# 2026-11-09 线上包源码基线（后端）

本分支从后端 `251a3f416bcae9f3f656d9d9147801a275d320c2` 出发，补入构建 `shuanggao-update-20261109` 时尚未提交的后端源码。发布包 SHA-256：`3f7df66ae4562421bd178a4dea708e6a44407bd9864db51faa54dc3f599f6b0a`；包内 JAR SHA-256：`39a700a05bcadcb85edc3f6cee1a1bf7cf38a46deec439d7425fa9d1211912e1`。

补入内容包含管理查询及日志、督办确认、本人已发消息查询与撤回相关的源码。根据当时构建包的类清单，补入了 `RequestAccessLogFilter`、`SentMessageController`、`SentMessageService` 三个在原提交中不存在的类。

**构建核对：**以发布包 JAR 的 `BOOT-INF/classes` 为参照，基线候选构建包含同样的 301 个应用文件。297 个文件逐字节相同；`AdminTestFilter.class`、`PermissionService.class` 仅编译器生成的 `patt...$temp` 局部名称不同，归一后内容相同。`application.properties` 和 `sql/stale-review-notices.sql` 的文本行内容相同，仅行尾编码不同。此核对在下面的配置净化前完成。

**有意差异：**原源码配置文件包含明文数据库连接值和两条注释口令；本基线将 URL、用户名、密码改为 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` 环境变量占位，并清除注释口令及两处测试中的内嵌口令。运行时须由私有环境提供数据库配置。旧 Git 历史中的口令不会因本提交消失，若仍有效应更换。不要使用旧 JAR 中的配置作为新环境的凭据来源。

此标签是后续移植 SSO 的源码基线，不是新的生产更新包；未运行数据库迁移或部署。前端源码须配合同名标签 `release-20261109-source`（在独立前端仓库）。
