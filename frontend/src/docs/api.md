# biz-backend 前端接口文档

本文档按当前源码整理，面向前端联调使用。后端全局配置了 `server.servlet.context-path=/api`，所以下方接口实际请求路径均需加 `/api` 前缀，例如登录为 `POST /api/system/login`。

## 通用约定

- 基础地址：`http://<host>:<port>/api`
- 默认端口：未在配置中显式设置，Spring Boot 默认 `8080`
- 鉴权 Header：除登录接口和 `/scheduled/**` 外，请求头必须携带 `Authorization: <token>`
- 注意：代码直接校验 `Authorization` 头的完整值，前端应传登录接口返回的 token 原文，不要加 `Bearer `
- Token 有效期：1 小时
- Content-Type：JSON 接口使用 `application/json`；文件上传使用 `multipart/form-data`
- 跨域：允许所有来源、允许携带凭据
- 时间字段：Java `Date` 由 Jackson 序列化，前端按实际响应解析

## 通用错误

多数控制器捕获异常后返回：

```json
{
  "message": "错误信息",
  "code": 500
}
```

JWT 拦截器会直接返回 HTTP 401：

```json
{
  "message": "No Token",
  "code": 401
}
```

用户管理接口额外有管理员角色校验，非管理员返回 HTTP 403：

```json
{
  "code": 403,
  "msg": "无权限访问该接口，仅管理员可访问"
}
```

## 枚举

用户角色 `role`：

| 值 | 含义 |
| --- | --- |
| `0` | 管理员 |
| `1` | 普通用户 |
| `2` | 负责人 |

任务状态 `status`：

| 值 | 含义 |
| --- | --- |
| `0` | 未开始 |
| `1` | 进行中 |
| `2` | 审核中 |
| `3` | 已完成 |

数据类型 `dataType` / `data_type`：

| 值 | 含义 |
| --- | --- |
| `0` | 对指标没有影响 |
| `1` | 数值累加 |
| `2` | 百分比取大 |

审批流状态 `flowStatus`：

| 值 | 含义 |
| --- | --- |
| `0` | 草稿 |
| `10` | 待所在部门审批 |
| `20` | 待归口部门审批 |
| `30` | 待管理员审批 |
| `40` | 已归档/审批完成 |
| `-10` | 被所在部门退回 |
| `-20` | 被归口部门退回 |
| `-30` | 被管理员退回 |

删除状态 `isDelete`：

| 值 | 含义 |
| --- | --- |
| `0` | 存在 |
| `1` | 删除 |

## 主要数据结构

### SysUser

```json
{
  "userId": 110228,
  "deptId": 1,
  "userName": "admin",
  "nickName": "管理员",
  "email": "admin@example.com",
  "password": "123456",
  "role": "0",
  "status": "0",
  "isDelete": 0,
  "createTime": "2026-01-01T00:00:00.000+00:00",
  "updateTime": "2026-01-01T00:00:00.000+00:00"
}
```

### BizTask / BizTaskVo

`GET /biz/tasks` 返回 `BizTaskVo[]`，比 `BizTask` 多部门和人员名称字段。

```json
{
  "taskId": 1,
  "projectId": 1,
  "parentId": 0,
  "ancestors": "0",
  "phase": 2026,
  "taskCode": "1.1.1",
  "taskName": "任务名称",
  "level": 3,
  "comment": "任务描述或提交备注",
  "deptId": 1,
  "deptName": "部门名称",
  "principalId": 1001,
  "principalName": "归口负责人账号",
  "auditorId": 1002,
  "auditorName": "审核人账号",
  "leaderId": 1003,
  "leaderName": "任务负责人账号",
  "expTarget": "预期达成情况",
  "expLevel": "国家级/省级/市级",
  "expEffect": "预期效果",
  "expMaterialDesc": "佐证材料清单",
  "dataType": "1",
  "targetValue": 100,
  "currentValue": 20,
  "weight": 1,
  "progress": 20,
  "status": "1",
  "isDelete": 0,
  "createTime": "2026-01-01T00:00:00.000+00:00",
  "updateTime": "2026-01-01T00:00:00.000+00:00"
}
```

### BizLevel4Task

```json
{
  "taskId": 101,
  "parentId": 1,
  "phase": 2026,
  "taskName": "四级任务",
  "leaderId": 1003,
  "deptId": 1,
  "dataType": "1",
  "targetValue": 10,
  "currentValue": 5,
  "progress": 50,
  "status": "1",
  "isDelete": 0,
  "createTime": "2026-01-01T00:00:00.000+00:00",
  "updateTime": "2026-01-01T00:00:00.000+00:00"
}
```

### BizAuditVO

```json
{
  "subId": 1,
  "taskId": 1,
  "fileId": 1,
  "filename": "材料.pdf",
  "reportedValue": 20,
  "dataType": "1",
  "submitBy": 1003,
  "submitDeptId": 1,
  "manageDeptId": 2,
  "submitTime": "2026-01-01T00:00:00.000+00:00",
  "fileSuffix": "pdf",
  "flowStatus": 10,
  "currentHandlerId": 1002,
  "isDelete": 0,
  "sub_list": [
    {
      "task_id": 101,
      "reported_value": 5,
      "data_type": "1"
    }
  ]
}
```

### TaskCompletionVO

```json
{
  "totalTasks": 100,
  "completedTasks": 60,
  "completionRate": 60.0,
  "period": "all",
  "description": "所有任务完成率",
  "notStartedCount": 10,
  "inProgressCount": 20,
  "inReviewCount": 10,
  "finishedCount": 60,
  "notStartedRate": 10.0,
  "inProgressRate": 20.0,
  "inReviewRate": 10.0,
  "finishedRate": 60.0
}
```

## 系统接口 `/system`

| 方法 | 路径 | 鉴权 | 说明 |
| --- | --- | --- | --- |
| POST | `/system/login` | 否 | 登录 |
| GET | `/system/allUsers` | 是 | 获取所有用户 |
| POST | `/system/users/add` | 管理员 | 添加用户 |
| POST | `/system/users/update` | 管理员 | 更新用户 |
| POST | `/system/users/delete/{userId}` | 管理员 | 删除用户 |
| GET | `/system/dept/{deptId}` | 是 | 获取部门 |
| POST | `/system/password` | 是 | 修改当前用户密码 |
| POST | `/system/logout` | 是 | 注销 |
| POST | `/system/upload/{task_id}` | 是 | 上传文件 |
| GET | `/system/download/{file_id}` | 是 | 下载文件 |
| POST | `/system/notice` | 是 | 发送通知 |
| GET | `/system/notice` | 是 | 获取当前用户通知 |
| POST | `/system/notice/{notice_id}` | 是 | 通知设为已读 |
| POST | `/system/alert` | 是 | 按昵称发送预警 |

### POST `/system/login`

请求体：

```json
{
  "user_id": 110228,
  "password": "123456"
}
```

成功返回：

```json
{
  "nick_name": "管理员",
  "token": "jwt-token"
}
```

### POST `/system/users/add`

请求体 `SysUserDTO`：

```json
{
  "userId": 1001,
  "deptId": 1,
  "userName": "zhangsan",
  "nickName": "张三",
  "email": "zhangsan@example.com",
  "password": "123456",
  "role": "1",
  "status": "0",
  "isDelete": 0
}
```

返回字符串：`用户 zhangsan 添加成功`

### POST `/system/users/update`

请求体同添加用户，按 `userId` 更新。返回字符串：`用户 zhangsan 更新成功`

### POST `/system/users/delete/{userId}`

逻辑删除用户。返回字符串：`用户 1001 删除成功`

### GET `/system/dept/{deptId}`

返回 `SysDept`：

```json
{
  "deptId": 1,
  "deptName": "部门名称",
  "leaderId": 1001,
  "status": "0",
  "isDelete": 0,
  "createTime": "2026-01-01T00:00:00.000+00:00",
  "updateTime": "2026-01-01T00:00:00.000+00:00"
}
```

### POST `/system/password`

请求体：

```json
{
  "new_password": "new-password"
}
```

成功返回当前实现为：

```json
{
  "new_password": "密码修改成功"
}
```

### POST `/system/logout`

成功返回：

```json
{
  "message": "注销成功"
}
```

### POST `/system/upload/{task_id}`

请求类型：`multipart/form-data`

| 参数 | 位置 | 类型 | 说明 |
| --- | --- | --- | --- |
| `task_id` | path | Long | 任务 ID |
| `file` | form-data | File | 仅允许 `pdf`、`doc`、`docx` |

成功返回：

```json
{
  "fileId": 1,
  "filename": "材料.pdf",
  "filepath": "/uploads/材料.pdf"
}
```

### GET `/system/download/{file_id}`

下载文件流，响应头为 `application/octet-stream`。异常时返回 `ErrorVO`。

### POST `/system/notice`

请求体：

```json
{
  "to_user_id": 1001,
  "type": "1",
  "trigger_event": "任务提醒",
  "title": "标题",
  "content": "内容",
  "source_type": "1",
  "source_id": 1,
  "is_read": "0"
}
```

注意：服务层当前固定写入 `sourceType="1"`、`isRead="0"`。成功返回字符串：`发送成功`

### GET `/system/notice`

返回当前用户收到的 `SysNotice[]`。

### POST `/system/notice/{notice_id}`

将通知设为已读。返回字符串：`已读`

### POST `/system/alert`

请求体：

```json
{
  "to_user_nick_name": "张三",
  "title": "预警标题",
  "content": "预警内容",
  "source_id": 1
}
```

成功返回字符串：`发送成功`

## 业务任务接口 `/biz`

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/biz/tasks` | 按当前用户角色获取任务 |
| GET | `/biz/tasks/{taskId}` | 获取任务详情 |
| GET | `/biz/tasks/children?task_id={id}` | 获取直接子任务 |
| GET | `/biz/tasks/forth?parent_id={id}` | 获取三级任务下四级任务 |
| GET | `/biz/tasks/parent?task_id={id}` | 获取父任务 |
| GET | `/biz/tasks/dept?dept_id={id}` | 按部门获取任务 |
| POST | `/biz/tasks/manage/add` | 添加三级任务 |
| POST | `/biz/tasks/manage/update` | 更新任务 |
| POST | `/biz/tasks/manage/finish/{taskId}` | 管理员完成任务 |
| POST | `/biz/sub` | 提交单个三级/四级任务材料 |
| POST | `/biz/sub` | 汇总提交三级任务下的四级任务 |
| GET | `/biz/audit/{taskId}` | 获取当前用户处理的某任务审批单 |
| GET | `/biz/audit/todo` | 待我审批 |
| GET | `/biz/audit/task/{taskId}` | 获取某任务全部审批单 |
| GET | `/biz/audit/file/{taskId}` | 获取上次审批通过文件 |
| GET | `/biz/audit/logs/{subId}` | 获取审批日志 |
| POST | `/biz/audit` | 审批通过/退回 |
| POST | `/biz/drawback/{taskId}` | 撤回提交 |
| POST | `/biz/resub` | 退回后重新提交 |

### GET `/biz/tasks`

返回 `BizTaskVo[]`。角色逻辑：

- 管理员 `0`：返回所有 1-3 级任务
- 普通用户 `1`：返回负责人、归口负责人或审核人为自己的任务
- 负责人 `2`：返回归口负责人为自己的任务

### GET `/biz/tasks/{taskId}`

返回 `BizTask`。

### GET `/biz/tasks/children?task_id={id}`

返回 `BizTask[]`，为当前任务的直接子任务。

### GET `/biz/tasks/forth?parent_id={id}`

要求 `parent_id` 是三级任务 ID。返回 `BizLevel4Task[]`。

### GET `/biz/tasks/parent?task_id={id}`

返回父级 `BizTask`。

### GET `/biz/tasks/dept?dept_id={id}`

返回该部门 `BizTask[]`。

### POST `/biz/tasks/manage/add`

只允许添加三级任务。校验规则包括：父任务必须存在且为二级任务、部门必须与父任务一致、`projectId` 必须为 `1`。

请求体 `BizTaskDTO`：

```json
{
  "projectId": 1,
  "parentId": 2,
  "ancestors": "1,2",
  "phase": 2026,
  "taskCode": "1.1.1",
  "taskName": "新增任务",
  "level": 3,
  "comment": "说明",
  "deptId": 1,
  "auditorId": 1002,
  "principalId": 1001,
  "leaderId": 1003,
  "expTarget": "目标",
  "expLevel": "省级",
  "expEffect": "效果",
  "expMaterialDesc": "材料清单",
  "dataType": "1",
  "targetValue": 100,
  "currentValue": 0,
  "weight": 1,
  "progress": 0,
  "status": "0"
}
```

### POST `/biz/tasks/manage/update`

请求体同 `BizTaskDTO`，必须包含 `taskId`。返回字符串：`任务xxx更新成功`

### POST `/biz/tasks/manage/finish/{taskId}`

仅管理员可操作。会将任务置为已完成、`currentValue=targetValue`，并重算绩效。返回字符串。

### POST `/biz/sub`

提交单个三级或四级任务材料。请求体：

```json
{
  "task_id": 1,
  "reported_value": 0.5,
  "data_type": "1",
  "file_id": 1,
  "comment": "提交备注"
}
```

注意：当前服务层会将 `reported_value * task.targetValue` 写入提交值和任务当前值。

### POST `/biz/sub`

提交三级任务下多个四级任务，适合前端在三级任务页面填写四级明细后统一提交。

```json
{
  "third_task_id": 1,
  "file_id": 1,
  "comment": "提交备注",
  "sub_list": [
    {
      "task_id": 101,
      "reported_value": 10,
      "data_type": "1"
    }
  ]
}
```

### GET `/biz/audit/{taskId}`

返回当前用户作为 `currentHandlerId` 的该任务审批单：`BizAuditVO[]`。

### GET `/biz/audit/todo`

返回待当前用户审批的审批单：`BizAuditVO[]`。

### GET `/biz/audit/task/{taskId}`

返回某任务全部审批单：`BizAuditVO[]`。权限：管理员、任务负责人、审核人、归口负责人，或本人曾提交过该任务。

### GET `/biz/audit/file/{taskId}`

返回该任务最近一次审批通过的文件：

```json
{
  "fileId": 1,
  "filename": "材料.pdf",
  "filepath": "/uploads/材料.pdf"
}
```

没有历史通过文件时返回 `null`。

### GET `/biz/audit/logs/{subId}`

返回 `BizAuditLog[]`：

```json
{
  "logId": 1,
  "subId": 1,
  "operatorId": 1002,
  "actionType": "通过",
  "preStatus": 10,
  "postStatus": 20,
  "comment": "同意",
  "createTime": "2026-01-01T00:00:00.000+00:00"
}
```

### POST `/biz/audit`

当前处理人审批。请求体：

```json
{
  "sub_id": 1,
  "is_pass": true,
  "title": "同意",
  "content": "审批意见"
}
```

说明：`content` 当前未被服务层使用，日志意见使用 `title`。

返回：`已审批，下一位审批人为xxx`、`已退回，退回到xxx` 或 `审批完成`。

### POST `/biz/drawback/{taskId}`

提交人撤回最新审批单，任务状态回到未开始，返回字符串：`已撤回提交`

### POST `/biz/resub`

退回后重新提交。请求体：

```json
{
  "sub_id": 1,
  "file_id": 2,
  "comment": "重新提交备注",
  "sub_list": [
    {
      "task_id": 101,
      "reported_value": 10,
      "data_type": "1"
    }
  ]
}
```

## 看板接口 `/dashboard`

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/dashboard/summary` | 看板汇总 |
| GET | `/dashboard/completion/overall` | 全部任务完成率 |
| GET | `/dashboard/completion/year?year=2026` | 年度任务完成率 |
| GET | `/dashboard/completion/midterm?year=2028` | 中期任务完成率 |
| GET | `/dashboard/completion/first-level` | 一级任务完成率 |
| GET | `/dashboard/dept/overall` | 各部门整体完成率 |
| GET | `/dashboard/dept/year?year=2026` | 各部门年度完成率 |
| GET | `/dashboard/dept/midterm?endYear=2028` | 各部门中期完成率 |
| GET | `/dashboard/tasks/first-level` | 一级任务详情 |
| GET | `/dashboard/tasks/all_level` | 全量 1-3 级任务详情 |
| GET | `/dashboard/dept/{deptId}` | 单部门统计 |
| GET | `/dashboard/dept/batch?deptIds=1,2,3` | 批量部门统计 |
| GET | `/dashboard/comparison/{dimension}` | 完成率对比 |

### GET `/dashboard/summary`

返回 `DashboardSummaryVO`：

```json
{
  "overallCompletion": {},
  "yearCompletion": {},
  "midTermCompletion": {},
  "firstLevelCompletion": {},
  "deptOverallStats": [],
  "deptYearStats": [],
  "deptMidTermStats": [],
  "firstLevelTasks": [],
  "currentYear": "2026",
  "midTermEndYear": "2028",
  "updateTime": "2026-05-24 12:00:00"
}
```

### 完成率接口

- `/dashboard/completion/overall` 返回 `TaskCompletionVO`
- `/dashboard/completion/year` 返回 `TaskCompletionVO`，`year` 为空时使用当前年份
- `/dashboard/completion/midterm` 返回 `TaskCompletionVO`，参数名是 `year`，为空时默认 `2028`
- `/dashboard/completion/first-level` 返回 `TaskCompletionVO`

### 部门统计接口

`/dashboard/dept/overall`、`/dashboard/dept/year`、`/dashboard/dept/midterm` 返回 `DeptTaskStatsVO[]`：

```json
{
  "deptId": 1,
  "deptName": "部门名称",
  "totalTasks": 10,
  "completedTasks": 6,
  "completionRate": 60.0,
  "notStartedCount": 1,
  "inProgressCount": 2,
  "inReviewCount": 1,
  "finishedCount": 6,
  "notStartedRate": 10.0,
  "inProgressRate": 20.0,
  "inReviewRate": 10.0,
  "finishedRate": 60.0
}
```

### GET `/dashboard/tasks/first-level`

返回 `FirstLevelTaskDetailVO[]`：

```json
{
  "taskId": 1,
  "taskName": "一级任务",
  "deptId": 1,
  "deptName": "部门名称",
  "status": "1",
  "targetValue": 100,
  "currentValue": 60,
  "progress": 60,
  "createTime": "2026-01-01 00:00:00",
  "updateTime": "2026-01-01 00:00:00",
  "completed": false,
  "statusText": "进行中",
  "completionRate": 60.0
}
```

### GET `/dashboard/dept/{deptId}`

返回 Map：

```json
{
  "deptId": 1,
  "deptName": "部门名称",
  "leaderId": 1001,
  "leaderName": "负责人姓名",
  "overall": {},
  "year": {},
  "midterm": {}
}
```

### GET `/dashboard/comparison/{dimension}`

`dimension` 可选：

| 值 | 说明 |
| --- | --- |
| `dept` | 各部门完成率对比，`data` 为 `DeptTaskStatsVO[]` |
| `year` | 年度对比，当前是代码内示例数据 2023-2025 |
| `level` | 任务级别对比，其中二级、三级当前是代码内示例数据 |

返回：

```json
{
  "dimension": "部门",
  "data": []
}
```

## 趋势接口 `/dashboard/trend`

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/dashboard/trend/{year}` | 获取指定年份趋势数据 |
| GET | `/dashboard/trend/` | 获取当前年份趋势数据 |
| POST | `/dashboard/trend/record` | 手动记录当天趋势数据 |

趋势数据 `BizTrendData`：

```json
{
  "dataId": 1,
  "year": 2026,
  "month": 5,
  "day": 24,
  "createTime": "2026-05-24T00:00:00.000+00:00",
  "totalTasks": 100,
  "completionCount": 60,
  "completionRate": 60.0,
  "isDelete": 0
}
```

## 绩效接口 `/performance`

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/performance` | 获取所有绩效指标 |
| POST | `/performance/refresh` | 重新计算所有绩效 |
| GET | `/performance/{perfId}` | 获取单个绩效指标 |
| GET | `/performance/task/{prefId}` | 根据绩效 ID 获取关联任务 |
| GET | `/performance/year/{year}` | 获取指定年份绩效 |
| POST | `/performance/submit?pref_id=1&actual_value=80&year=2026` | 手动提交绩效实际完成度 |

### GET `/performance`

返回 `BizPerformance[]`。

### POST `/performance/refresh`

返回多行字符串，包含更新成功/失败数量。

### GET `/performance/{perfId}`

返回 `BizPerformance`。

### GET `/performance/task/{prefId}`

注意路径参数名拼写为 `prefId`，含义是 `perfId`。返回该绩效关联的 `BizTask[]`。

### GET `/performance/year/{year}`

返回 `PerformanceYearVO[]`。

### POST `/performance/submit`

请求参数为 query/form 参数，不是 JSON：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `pref_id` | Long | 绩效 ID |
| `actual_value` | BigDecimal | 实际完成度，0-100 |
| `year` | Integer | 年份 |

仅允许 `perfCode` 以 `1.3`、`2`、`3` 开头的绩效手动提交。成功返回字符串：`提交成功`

## 标志性成果接口 `/achievement`

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/achievement/` | 查询所有未删除成果 |
| GET | `/achievement/{achId}` | 查询单个成果 |
| POST | `/achievement/add` | 新增成果 |
| POST | `/achievement/update/{id}` | 修改成果 |
| POST | `/achievement/delete/{achId}` | 逻辑删除成果 |

`BizAchievement`：

```json
{
  "achId": 1,
  "category": 1,
  "level": "省级",
  "achName": "成果名称",
  "department": "组织部门",
  "gotTime": "2026-01-01T00:00:00.000+00:00",
  "deptId": 1,
  "fileId": 1,
  "comment": "备注",
  "isCompetition": 0,
  "teDengJiang": 0,
  "yiDengJiang": 0,
  "erDengJiang": 0,
  "sanDengJiang": 0,
  "jinJiang": 0,
  "yinJiang": 0,
  "tongJiang": 0,
  "youShengJiang": 0,
  "budDengDengCi": 0,
  "createBy": 1001,
  "isDelete": 0,
  "createTime": "2026-01-01T00:00:00.000+00:00"
}
```

新增必填校验：`category`、`level`、`achName`、`gotTime`。

成果类别 `category`：

| 值 | 含义 |
| --- | --- |
| `1` | 落实立德树人根本任务 |
| `2` | 创新产教融合机制 |
| `3` | 打造高水平专业群 |
| `4` | 建设一流核心课程 |
| `5` | 开放优质新形态教材 |
| `6` | 建设高水平双师队伍 |
| `7` | 建设产教融合实训基地 |
| `8` | 构建数字化教学新生态 |
| `9` | 拓展国际交流与合作 |
| `10` | 打造一流区域型高端智库 |

## 定时任务手动触发接口 `/scheduled`

`/scheduled/**` 未配置 JWT 拦截器，当前源码下不需要鉴权。

| 方法 | 路径 | 说明 | 返回 |
| --- | --- | --- | --- |
| POST | `/scheduled/month_leader_trigger` | 手动触发每月部门负责人提醒 | 字符串 |
| POST | `/scheduled/month_auditor_trigger` | 手动触发每月审核人提醒 | 字符串 |
| POST | `/scheduled/year_trigger` | 手动触发年度提醒 | 字符串 |
| POST | `/scheduled/update_task_status` | 手动更新任务状态 | 无响应体 |

## 前端联调注意事项

- 所有接口实际都有 `/api` 前缀。
- `Authorization` 传 token 原文，不要加 `Bearer `。
- 大多数写操作成功时返回普通字符串，不是统一响应对象。
- 大多数错误被包装成 HTTP 200 + `ErrorVO`，但鉴权失败是 HTTP 401，角色失败是 HTTP 403。
- `/dashboard/completion/midterm` 的 query 参数名在代码里是 `year`；`/dashboard/dept/midterm` 的 query 参数名是 `endYear`。
- `/performance/task/{prefId}` 的路径变量拼写是 `prefId`，语义是绩效 ID。
- `/dashboard/comparison/year` 和 `/dashboard/comparison/level` 当前包含硬编码示例数据，不完全来自数据库。
- 用户增删改接口除 JWT 外还要求 `role=0`。
