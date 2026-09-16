# 第十四批修复交付

## 改动与 Git 记录

| 编号 | 修复 | 前端提交 |
|---|---|---|
| 14-1 | 成果审核记录及日志增加请求序号和成果归属校验；关闭、切换、卸载后旧响应失效；审批固定单据及意见 | `12006c7` |
| 14-2 | 文件上传前固定成果字段、奖项数量；表单版本阻止旧提交清空或关闭下一份草稿 | `34d2c7d` |
| 14-3 | 督办固定发送内容及收件人，成功回调只清空未再次编辑的原草稿；校验开始即锁定提交 | `b71128b` |

仅修改前端 Achievement.vue、Report.vue。接口和数据库结构未变。客户要求的材料覆盖、仅保留当前及上次材料规则未改动。原有 Achievement.vue、Audit.vue、vite.config.js 和后端未提交工作保留，没有混入修复提交。

## 本地闭环验证

Playwright + Edge，既有 review 合成账号/任务，真实本地前后端及随机隔离 MySQL。成功竞态场景暂停真实 HTTP 响应；督办失败场景在浏览器拦截并返回 503，不向后端发送该失败通知。

六个场景通过：

1. 延迟成果 A 审核数据，关闭再打开 B：真实审批后 A 保持 10、B 归档为 30。
2. 上传 C 期间取消并新建 D：真实保存 C 名称、一等奖 2、C 附件；D 名称和数量 5 保留且表单不关闭。
3. 成果正常上传提交：真实保存一等奖 2，成功关闭原表单。
4. 督办发送期间重置并填写新草稿：旧成功返回后新主题及正文保留。
5. 督办正常发送：成功后原主题及正文清空。
6. 督办发送失败期间填写新草稿：503 返回后新主题及正文保留、提交按钮恢复。

各场景浏览器 pageerror 均为空。前端生产构建、修改文件 ESLint、测试脚本语法检查通过；构建仍提示已有大分块警告。未修改后端业务代码，本批未重跑后端全量回归。

测试结束已停止临时前后端服务并删除随机隔离数据库，清理程序正常退出。

[测试结果和截图](fix-evidence/batch-14/)。原始缺陷证据继续保存在 audit-evidence/2026-09-16/batch-14，没有覆盖。

复测命令（先启动全新 `python scripts/ui-audit/serve.py --fixture review` 隔离环境，审核用例每个环境运行一次）：

```powershell
node scripts/ui-audit/batch-14-audit.cjs achievement-approval --fixed
node scripts/ui-audit/batch-14-audit.cjs achievement-upload --fixed
node scripts/ui-audit/batch-14-audit.cjs achievement-upload --fixed --stay
node scripts/ui-audit/batch-14-audit.cjs report-draft --fixed
node scripts/ui-audit/batch-14-audit.cjs report-draft --fixed --stay
node scripts/ui-audit/batch-14-audit.cjs report-draft --fixed --fail
```

## 部署与回滚

本地构建产物为 `biz/dist`，尚未部署。该产物包含本地原有未提交工作，正式发布前需要一并审核。仅需更新前端，无数据库迁移。

按需反向提交对应修复；整批回滚依次 `git revert b71128b`、`git revert 34d2c7d`、`git revert 12006c7`。在干净发布分支执行，保留当前工作区未提交内容；也可恢复上一版本前端产物。无需回滚 SQL。
