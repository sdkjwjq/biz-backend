# biz-backend 系统接口文档

### 迭代记录
- 2025-12-8：完成登录功能
- 2025-12-11：完成登录、注销、修改密码
- 2025-12-21：补充实现根据部门ID获取任务、提交审批材料、获取审批单、文件上传/下载、通知发送/查看、审批任务、重新提交审批材料等接口

## 基础信息
- 基础路径：无（接口路径已包含具体前缀）
- 认证方式：JWT Token（登录后获取，请求时通过`Authorization`请求头携带）
- 示例服务域名：`https://api.example.com`（实际调用时替换为部署的服务地址）
- 示例Token：`eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoxLCJ1c2VyX25hbWUiOiJhZG1pbiIsImV4cCI6MTcxMjEwMzYwMH0.xxxxxx`（所有需认证接口统一使用此示例，无需重复粘贴）
## Postman接口文档
https://www.postman.com/litianyi981119/biz/collection/21001135-3309c751-c3ca-4fdb-9d3e-f9aa8529b9c0/?action=share&creator=21001135

## 接口列表

### 1. 用户登录
- **请求路径**：`/system/login`
- **请求方法**：POST
- **示例调用URL**：`https://api.example.com/system/login`
- **请求体参数**：
  ```json
  {
    "user_name": "admin", // 示例值
    "password": "123456"  // 示例值
  }
  ```
- **成功响应**（200 OK）：
  ```json
  {
    "nick_name": "系统管理员",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoxLCJ1c2VyX25hbWUiOiJhZG1pbiIsImV4cCI6MTcxMjEwMzYwMH0.xxxxxx"
  }
  ```
- **错误响应**（500 Internal Server Error）：
  ```json
  {
    "message": "密码错误",
    "code": 500
  }
  ```
- **说明**：无需认证，登录成功后返回令牌用于后续接口调用

### 2. 用户登出
- **请求路径**：`/system/logout`
- **请求方法**：POST
- **示例调用URL**：`https://api.example.com/system/logout`
- **请求头**：`Authorization: 示例Token`（需替换为实际登录获取的Token）
- **成功响应**（200 OK）：
  ```json
  {
    "message": "注销成功"
  }
  ```
- **错误响应**：
  - 401 Unauthorized：`{"message": "token不存在", "code": 401}`
  - 500 Internal Server Error：`{"message": "注销失败", "code": 500}`
- **说明**：登出后令牌将被加入黑名单，无法再使用

### 3. 修改密码
- **请求路径**：`/system/password`
- **请求方法**：POST
- **示例调用URL**：`https://api.example.com/system/password`
- **请求头**：`Authorization: 示例Token`（需替换为实际登录获取的Token）
- **请求体参数**：
  ```json
  {
    "new_password": "654321" // 示例值
  }
  ```
- **成功响应**（200 OK）：
  ```json
  {
    "message": "密码修改成功"
  }
  ```
- **错误响应**（500 Internal Server Error）：
  ```json
  {
    "message": "用户不存在",
    "code": 500
  }
  ```
- **说明**：基于当前登录用户（通过token解析用户ID）修改密码

### 4. 获取所有用户
- **请求路径**：`/system/users`
- **请求方法**：GET
- **示例调用URL**：`https://api.example.com/system/users`
- **请求头**：`Authorization: 示例Token`（需替换为实际登录获取的Token）
- **成功响应**（200 OK）：
  ```json
  [
    {
      "userId": 1,
      "deptId": 1,
      "userName": "admin",
      "nickName": "系统管理员",
      "email": "admin@example.com",
      "password": "******", // 脱敏展示
      "role": "admin",
      "status": "正常",
      "isDelete": 0,
      "createTime": "2024-01-01T00:00:00",
      "updateTime": "2024-01-01T00:00:00"
    }
  ]
  ```
- **错误响应**（401 Unauthorized）：
  ```json
  {
    "message": "No Token/Invalid Token",
    "code": 401
  }
  ```
- **说明**：需要认证，返回系统中所有用户信息

### 5. 获取全量任务数据
- **请求路径**：`/biz/tasks`
- **请求方法**：GET
- **示例调用URL**：`https://api.example.com/biz/tasks`
- **请求头**：`Authorization: 示例Token`（需替换为实际登录获取的Token）
- **成功响应**（200 OK）：
  ```json
  [
    {
      "taskId": 1,
      "projectId": 1,
      "parentId": 0,
      "ancestors": "0,1",
      "phase": 1,
      "taskCode": "TSK001",
      "taskName": "项目初始化",
      "level": 1,
      "deptId": 1,
      "principalId": 1,
      "leaderId": 1,
      "expTarget": "完成项目框架搭建",
      "expLevel": "一级",
      "expEffect": "满足业务基础运行需求",
      "expMaterialDesc": "项目文档、代码仓库初始化",
      "dataType": "数值型",
      "targetValue": 100.00,
      "currentValue": 80.00,
      "weight": 0.5,
      "progress": 80,
      "status": "进行中",
      "isDelete": 0,
      "createTime": "2024-01-01T00:00:00",
      "updateTime": "2024-01-02T00:00:00"
    }
  ]
  ```
- **错误响应**：
  - 401 Unauthorized：`{"message": "No Token/Invalid Token", "code": 401}`
  - 500 Internal Server Error：`{"message": "查询任务数据失败", "code": 500}`
- **说明**：需要认证，返回系统中所有任务信息

### 6. 根据ID获取任务
- **请求路径**：`/biz/tasks/{taskId}`
- **请求方法**：GET
- **示例调用URL**：`https://api.example.com/biz/tasks/1`（示例taskId=1）
- **请求头**：`Authorization: 示例Token`（需替换为实际登录获取的Token）
- **路径参数**：`taskId`（任务ID，必填，示例值：1）
- **成功响应**（200 OK）：
  ```json
  {
    "taskId": 1,
    "projectId": 1,
    "parentId": 0,
    "ancestors": "0,1",
    "phase": 1,
    "taskCode": "TSK001",
    "taskName": "项目初始化",
    "level": 1,
    "deptId": 1,
    "principalId": 1,
    "leaderId": 1,
    "expTarget": "完成项目框架搭建",
    "expLevel": "一级",
    "expEffect": "满足业务基础运行需求",
    "expMaterialDesc": "项目文档、代码仓库初始化",
    "dataType": "数值型",
    "targetValue": 100.00,
    "currentValue": 80.00,
    "weight": 0.5,
    "progress": 80,
    "status": "进行中",
    "isDelete": 0,
    "createTime": "2024-01-01T00:00:00",
    "updateTime": "2024-01-02T00:00:00"
  }
  ```
- **错误响应**：
  - 401 Unauthorized：`{"message": "No Token/Invalid Token", "code": 401}`
  - 500 Internal Server Error：`{"message": "错误信息（如任务ID为空、任务不存在）", "code": 500}`
- **说明**：需要认证，根据任务ID返回对应任务详情

### 7. 获取当前任务的所有子任务
- **请求路径**：`/biz/tasks/children`
- **请求方法**：GET
- **示例调用URL**：`https://api.example.com/biz/tasks/children?task_id=1`（示例task_id=1）
- **请求头**：`Authorization: 示例Token`（需替换为实际登录获取的Token）
- **请求参数**：`task_id`（父任务ID，必填，示例值：1）
- **成功响应**（200 OK）：
  ```json
  [
    {
      "taskId": 2,
      "projectId": 1,
      "parentId": 1,
      "ancestors": "0,1,2",
      "phase": 1,
      "taskCode": "TSK002",
      "taskName": "子任务名称",
      "level": 2,
      "deptId": 1,
      "principalId": 1,
      "leaderId": 1,
      "expTarget": "子任务预期目标",
      "expLevel": "子任务预期级别",
      "expEffect": "子任务预期效果",
      "expMaterialDesc": "子任务预期材料描述",
      "dataType": "数据类型",
      "targetValue": 50.00,
      "currentValue": 30.00,
      "weight": 0.3,
      "progress": 60,
      "status": "进行中",
      "isDelete": 0,
      "createTime": "2024-01-03T00:00:00",
      "updateTime": "2024-01-04T00:00:00"
    }
  ]
  ```
- **错误响应**：
  - 401 Unauthorized：`{"message": "No Token/Invalid Token", "code": 401}`
  - 500 Internal Server Error：`{"message": "错误信息（如任务ID为空、任务不存在）", "code": 500}`
- **说明**：需要认证，根据父任务ID返回其所有子任务信息

### 8. 获取当前任务的父任务
- **请求路径**：`/biz/tasks/parent`
- **请求方法**：GET
- **示例调用URL**：`https://api.example.com/biz/tasks/parent?task_id=2`（示例task_id=2）
- **请求头**：`Authorization: 示例Token`（需替换为实际登录获取的Token）
- **请求参数**：`task_id`（子任务ID，必填，示例值：2）
- **成功响应**（200 OK）：
  ```json
  {
    "taskId": 1,
    "projectId": 1,
    "parentId": 0,
    "ancestors": "0,1",
    "phase": 1,
    "taskCode": "TSK001",
    "taskName": "父任务名称",
    "level": 1,
    "deptId": 1,
    "principalId": 1,
    "leaderId": 1,
    "expTarget": "父任务预期目标",
    "expLevel": "父任务预期级别",
    "expEffect": "父任务预期效果",
    "expMaterialDesc": "父任务预期材料描述",
    "dataType": "数据类型",
    "targetValue": 100.00,
    "currentValue": 50.00,
    "weight": 0.5,
    "progress": 50,
    "status": "进行中",
    "isDelete": 0,
    "createTime": "2024-01-01T00:00:00",
    "updateTime": "2024-01-02T00:00:00"
  }
  ```
- **错误响应**：
  - 401 Unauthorized：`{"message": "No Token/Invalid Token", "code": 401}`
  - 500 Internal Server Error：`{"message": "错误信息（如任务ID为空、任务不存在）", "code": 500}`
- **说明**：需要认证，根据子任务ID返回其父任务信息

### 9. 根据部门ID获取任务
- **请求路径**：`/biz/tasks/dept`
- **请求方法**：GET
- **示例调用URL**：`https://api.example.com/biz/tasks/dept?dept_id=1`（示例dept_id=1）
- **请求头**：`Authorization: 示例Token`（需替换为实际登录获取的Token）
- **请求参数**：`dept_id`（部门ID，必填，示例值：1）
- **成功响应**（200 OK）：
  ```json
  [
    {
      "taskId": 1,
      "projectId": 1,
      "parentId": 0,
      "ancestors": "0,1",
      "phase": 1,
      "taskCode": "TSK001",
      "taskName": "部门任务1",
      "level": 1,
      "deptId": 1,
      "principalId": 1,
      "leaderId": 1,
      "expTarget": "部门任务目标",
      "expLevel": "一级",
      "expEffect": "满足部门业务需求",
      "expMaterialDesc": "部门任务材料描述",
      "dataType": "数值型",
      "targetValue": 100.00,
      "currentValue": 80.00,
      "weight": 0.5,
      "progress": 80,
      "status": "进行中",
      "isDelete": 0,
      "createTime": "2024-01-01T00:00:00",
      "updateTime": "2024-01-02T00:00:00"
    }
  ]
  ```
- **错误响应**：
  - 401 Unauthorized：`{"message": "No Token/Invalid Token", "code": 401}`
  - 500 Internal Server Error：`{"message": "获取任务失败,请检查部门id是否正确", "code": 500}`
- **说明**：需要认证，根据部门ID返回该部门下的所有任务信息

### 10. 提交审批材料
- **请求路径**：`/biz/submit`
- **请求方法**：POST
- **示例调用URL**：`https://api.example.com/biz/submit`
- **请求头**：`Authorization: 示例Token`（需替换为实际登录获取的Token）
- **请求体参数**：
  ```json
  {
    "task_id": 3, // 三级任务ID，示例值
    "filename": "report.pdf", // 已上传的文件名，示例值
    "reported_value": "100", // 申报值，示例值
    "data_type": "数值型" // 数据类型，示例值
  }
  ```
- **成功响应**（200 OK）：
  ```json
  "提交成功"
  ```
- **错误响应**（500 Internal Server Error）：
  ```json
  {
    "message": "该任务不是三级任务,无法提交", // 可能的错误信息，根据实际情况变化
    "code": 500
  }
  ```
- **说明**：需要认证，仅允许提交三级任务的审批材料，文件格式限pdf、doc、docx

### 11. 获取审批单
- **请求路径**：`/biz/audit/{taskId}`
- **请求方法**：GET
- **示例调用URL**：`https://api.example.com/biz/audit/3`（示例taskId=3）
- **请求头**：`Authorization: 示例Token`（需替换为实际登录获取的Token）
- **路径参数**：`taskId`（任务ID，必填，示例值：3）
- **成功响应**（200 OK）：
  ```json
  [
    {
      "subId": 1,
      "taskId": 3,
      "fileId": 2,
      "reportedValue": "100",
      "dataType": "数值型",
      "submitBy": 1,
      "submitDeptId": 1,
      "manageDeptId": 1,
      "submitTime": "2024-01-05T00:00:00",
      "fileSuffix": "pdf",
      "flowStatus": 10,
      "currentHandlerId": 2,
      "isDelete": 0
    }
  ]
  ```
- **错误响应**：
  - 401 Unauthorized：`{"message": "No Token/Invalid Token", "code": 401}`
  - 500 Internal Server Error：`{"message": "获取审批单失败,请检查任务id是否正确", "code": 500}`
- **说明**：需要认证，根据任务ID返回当前用户作为处理人的审批单信息

### 12. 文件上传
- **请求路径**：`/system/upload`
- **请求方法**：POST
- **示例调用URL**：`https://api.example.com/system/upload`
- **请求头**：`Authorization: 示例Token`（需替换为实际登录获取的Token）
- **请求体参数**：`file`（文件，form-data格式，必填）
- **成功响应**（200 OK）：
  ```json
  {
    "fileId": 1,
    "filename": "report.pdf",
    "filePath": "/uploads/report.pdf"
  }
  ```
- **错误响应**（500 Internal Server Error）：
  ```json
  {
    "message": "文件上传失败",
    "code": 500
  }
  ```
- **说明**：需要认证，用于上传审批材料等文件，支持pdf、doc、docx格式，单个文件大小限制为10MB

### 13. 根据文件ID下载文件
- **请求路径**：`/system/download/{file_id}`
- **请求方法**：GET
- **示例调用URL**：`https://api.example.com/system/download/1`（示例file_id=1）
- **请求头**：`Authorization: 示例Token`（需替换为实际登录获取的Token）
- **路径参数**：`file_id`（文件ID，必填，示例值：1）
- **成功响应**（200 OK）：文件流（浏览器自动下载）
- **错误响应**：
  - 401 Unauthorized：`{"message": "No Token/Invalid Token", "code": 401}`
  - 500 Internal Server Error：`{"message": "文件不存在", "code": 500}`
- **说明**：需要认证，根据文件ID下载对应的文件

### 14. 发送通知
- **请求路径**：`/system/notice`
- **请求方法**：POST
- **示例调用URL**：`https://api.example.com/system/notice`
- **请求头**：`Authorization: 示例Token`（需替换为实际登录获取的Token）
- **请求体参数**：
  ```json
  {
    "to_user_id": 2, // 接收人ID，示例值
    "type": "审批通知", // 通知类型，示例值
    "trigger_event": "审批单提交", // 触发事件，示例值
    "title": "新的审批单待处理", // 通知标题，示例值
    "content": "您有一条新的审批单（任务ID：3）需要处理，请及时操作", // 通知内容，示例值
    "source_type": "1", // 关联来源类型，1=审批单，示例值
    "source_id": 1 // 关联业务ID，示例值
  }
  ```
- **成功响应**（200 OK）：
  ```json
  "发送成功"
  ```
- **错误响应**（500 Internal Server Error）：
  ```json
  {
    "message": "发送失败",
    "code": 500
  }
  ```
- **说明**：需要认证，用于向指定用户发送通知，支持审批、任务、系统类通知类型

### 15. 查看当前用户收到的通知
- **请求路径**：`/system/notice`
- **请求方法**：GET
- **示例调用URL**：`https://api.example.com/system/notice`
- **请求头**：`Authorization: 示例Token`（需替换为实际登录获取的Token）
- **成功响应**（200 OK）：
  ```json
  [
    {
      "noticeId": 1,
      "fromUserId": 1,
      "toUserId": 2,
      "type": "审批通知",
      "triggerEvent": "审批单提交",
      "title": "新的审批单待处理",
      "content": "您有一条新的审批单（任务ID：3）需要处理，请及时操作",
      "sourceType": "1",
      "sourceId": 1,
      "isRead": "0", // 0=未读，1=已读
      "isDelete": 0,
      "createTime": "2024-01-06T00:00:00"
    }
  ]
  ```
- **错误响应**（401 Unauthorized）：
  ```json
  {
    "message": "No Token/Invalid Token",
    "code": 401
  }
  ```
- **说明**：需要认证，返回当前登录用户收到的所有通知信息，按创建时间倒序排列

### 16. 审批任务
- **请求路径**：`/biz/audit`
- **请求方法**：POST
- **示例调用URL**：`https://api.example.com/biz/audit`
- **请求头**：`Authorization: 示例Token`（需替换为实际登录获取的Token）
- **请求体参数**：
  ```json
  {
    "sub_id": 1, // 审批单ID，示例值
    "is_pass": true, // 是否通过，true为通过，false为退回
    "opinion": "同意该审批单内容，建议加快推进任务进度" // 审批意见，示例值
  }
  ```
- **成功响应**（200 OK）：
  ```json
  {
    "message": "已审批，下一位审批人为管理员（ID：3）",
    "next_handler_id": 3 // 下一位处理人ID（退回时无此字段）
  }
  ```
  退回场景响应：
  ```json
  {
    "message": "已退回，退回到提交人（ID：1）",
    "back_to_id": 1
  }
  ```
- **错误响应**：
  - 401 Unauthorized：`{"message": "No Token/Invalid Token", "code": 401}`
  - 500 Internal Server Error：
    ```json
    {
      "message": "该审批单不存在或已处理",
      "code": 500
    }
    ```
- **说明**：
  1. 需要认证，当前用户需为审批单的当前处理人才能进行审批操作
  2. 审批通过会流转到下一处理人，审批退回会返回到提交人，并同步发送通知
  3. 审批单流转至最后一位处理人并通过后，审批单状态标记为"已完成"；退回后状态标记为"已退回"

### 17. 重新提交审批材料
- **请求路径**：`/biz/resubmit`
- **请求方法**：POST
- **示例调用URL**：`https://api.example.com/biz/resubmit`
- **请求头**：`Authorization: 示例Token`（需替换为实际登录获取的Token）
- **请求体参数**：
  ```json
  {
    "sub_id": 1, // 原审批单ID，示例值
    "filename": "report_v2.pdf", // 重新上传的文件名，示例值
    "reported_value": "95", // 重新申报值，示例值
    "data_type": "数值型" // 数据类型，示例值
  }
  ```
- **成功响应**（200 OK）：
  ```json
  "重新提交成功，审批单已流转至第一位处理人"
  ```
- **错误响应**：
  - 401 Unauthorized：`{"message": "No Token/Invalid Token", "code": 401}`
  - 500 Internal Server Error：
    ```json
    {
      "message": "该审批单未被退回，无法重新提交",
      "code": 500
    }
    ```
- **说明**：
  1. 需要认证，仅允许审批单提交人对"已退回"状态的审批单进行重新提交
  2. 重新提交后审批单状态重置为"待处理"，流转至第一位处理人，并同步发送通知
  3. 重新提交时可更新申报值和审批材料文件

## 通用错误码说明
| 状态码 | 说明                             |
| ------ | -------------------------------- |
| 401    | 未携带token、token无效或已过期   |
| 500    | 服务器内部错误（如业务逻辑异常） |

## 认证说明
- 除登录接口（`/system/login`）外，所有接口均需要在请求头携带`Authorization`令牌
- 令牌有效期为1小时（从生成时间开始计算）
- 令牌无效时需重新登录获取新令牌
- Token格式要求：请求头中需携带 `Authorization: Bearer {token}`（示例：`Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.xxxxxx`）
