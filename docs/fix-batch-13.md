# 第十三批修复与闭环

## 修改及 Git

| 编号 | 前端提交 | 改动 |
| --- | --- | --- |
| 13-1 | `3e3f32b` | Performance.vue 关联任务填报在第一次异步操作前固定任务、文件、数值、子任务、完成情况和重提单号。提交中防重复调用，详情加载时禁止提交；回调只修改原详情。 |
| 13-2 | `900ef73` | Works.vue 撤回按任务 ID 防重复调用；刷新列表后只在详情序号和任务仍匹配时修改表单、当前任务和 loading，兼容列表与详情入口。 |
| 13-3 | `211cae6` | Performance.vue 手动填报固定指标、年份、填报值和名称；验证和确认后检查上下文，发出请求后各异步回调再次检查，避免更新其他指标或年份。 |

保持原有接口、整数处理、权限和退回重提规则，不改数据库。已发出的操作仍处理原对象；切换页面不会使其结果覆盖新详情。客户材料覆盖及仅保留当前/上一次材料的规则未修改。

## 测试

使用 Playwright + Edge、真实本地 HTTP、随机隔离 MySQL。所有业务数据、文件和通知均为合成数据。失败场景明确注入 503；其余场景只延迟真实响应。

1. 关联任务 A 填报 3，上传期间打开 B（7）：A 审核单实际保存 3，B 保持 7。
2. 保持原关联任务详情正常提交：保存 3。
3. 四级子任务填报期间切换 B：请求保持原父/子任务 ID，子任务实际保存 3。
4. 上传失败后不提交业务审核单，当前 B 详情不变。
5. 撤回 A 期间切到 B：A 实际恢复 3，B 详情和草稿不变。
6. 保持 B 详情正常撤回：实际恢复提交前值 7。
7. 取消撤回：未发出撤回请求，按钮恢复可用。
8. 撤回失败：B 仍审核中且数值为 3，详情不变。
9. 绩效 C 提交 7 期间打开 D：D 名称、目标 20、完成值 0、草稿 9 均保留；C 实际保存 7。
10. 保持 D 详情正常提交：D 实际保存 7。
11. 取消绩效确认：未发出提交请求，输入 7 保留。
12. C 提交 2026 年数据期间切到 D 的 2027 年详情：旧响应不覆盖新年份详情和草稿，C 的 2026 年值正确保存。
13. 上述跨年份场景改为提交失败：C 数据未写入，D 的新年份详情和草稿保持不变。

[结构化证据](fix-evidence/batch-13/)。原始缺陷证据没有覆盖。以上最终场景通过，无浏览器脚本异常。跨年份首次运行因缺少 D 的 2027 年合成记录无法完成，补充专用年度种子后通过。

两处修改文件 ESLint、前端生产构建、测试脚本语法检查通过。保留混合行尾，以 `core.whitespace=cr-at-eol` 检查差异；Git 记录只含本次改动，原有未提交文件保留。构建仅有原有大分包提示。

本次未改后端，不重复执行后端打包及 23 项后端回归；退回重提分支保留原规则，未额外做退回重提端到端测试。

## 全新环境复测顺序

启动 `serve.py --fixture review`，按既有方式设置数据库密码环境变量：

```powershell
python scripts/ui-audit/seed-batch-8.py --batch 11
python scripts/ui-audit/seed-batch-8.py --batch 13
python scripts/ui-audit/seed-batch-8.py --batch 13-regression
python scripts/ui-audit/seed-batch-8.py --batch 13-year
node scripts/ui-audit/batch-13-audit.cjs related-upload --fixed
node scripts/ui-audit/batch-13-audit.cjs withdraw-context --fixed
node scripts/ui-audit/batch-13-audit.cjs manual-submit --fixed
node scripts/ui-audit/batch-13-audit.cjs related-upload --fixed --stay
node scripts/ui-audit/batch-13-audit.cjs withdraw-context --fixed --stay
node scripts/ui-audit/batch-13-audit.cjs manual-submit --fixed --stay
node scripts/ui-audit/batch-13-audit.cjs related-upload --fixed --fourth
node scripts/ui-audit/batch-13-audit.cjs related-upload --fixed --fail-upload
node scripts/ui-audit/batch-13-audit.cjs manual-submit --fixed --reset-manual --cancel
node scripts/ui-audit/batch-13-audit.cjs related-upload --fixed --stay
node scripts/ui-audit/batch-13-audit.cjs withdraw-context --fixed --stay --cancel
node scripts/ui-audit/batch-13-audit.cjs withdraw-context --fixed --stay --fail-withdrawal
node scripts/ui-audit/batch-13-audit.cjs manual-submit --fixed --reset-manual --year
node scripts/ui-audit/batch-13-audit.cjs manual-submit --fixed --reset-manual --year --fail-manual
New-Item -ItemType File -Path target/ui-audit/STOP -Force
```

其中重复执行的正常任务提交用于准备撤回取消/失败的前置状态，不重复计算场景。测试完成后临时服务及随机数据库已清理。

## 部署与回滚

前端产物为 `biz/dist`，未部署；无需接口或数据库迁移，也无需回滚 SQL。

在前端仓库依次执行 `git revert 211cae6`、`git revert 900ef73`、`git revert 3e3f32b` 后重新构建发布，或恢复前一版前端产物。不使用硬重置，不影响原有未提交工作。
