# biz-backend 系统接口文档
## 修订记录
| 日期 | 完成功能 |
|------|----------|
| 2025-12-8 | 完成登录功能 |
| 2025-12-11 | 完成登录、注销、修改密码 |
| 2025-12-20 | 补充部门任务、文件操作、审批、通知等接口 |

## 基础信息
- 基础路径：无（接口路径已包含具体前缀）
- 认证方式：JWT Token（登录后获取，请求时通过`Authorization`请求头携带）
- 示例服务域名：`https://api.example.com`（实际调用时替换为部署的服务地址）
- 示例Token：`eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoxLCJ1c2VyX25hbWUiOiJhZG1pbiIsImV4cCI6MTcxMjEwMzYwMH0.xxxxxx`

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
      "currentValue": 60.00,
      "weight": 0.6,
      "progress": 60,
      "status": "进行中",
      "isDelete": 0,
      "createTime": "2024-01-01T00:00:00",
      "updateTime": "2024-01-05T00:00:00"
    }
  ]
  ```
- **错误响应**：
  - 401 Unauthorized：`{"message": "No Token/Invalid Token", "code": 401}`
  - 500 Internal Server Error：`{"message": "获取任务失败,请检查部门id是否正确", "code": 500}`
- **说明**：需要认证，根据部门ID返回该部门的所有任务信息

### 10. 上传文件
- **请求路径**：`/system/upload`
- **请求方法**：POST
- **示例调用URL**：`https://api.example.com/system/upload`
- **请求头**：
  - `Authorization: 示例Token`（需替换为实际登录获取的Token）
  - `Content-Type: multipart/form-data`
- **请求参数**：`file`（文件，必填，支持pdf、doc、docx格式）
- **成功响应**（200 OK）：
  ```json
  {
    "fileId": 1,
    "filename": "example.pdf",
    "filePath": "/uploads/xxxx-xxxx-xxxx.pdf"
  }
  ```
- **错误响应**：
  - 401 Unauthorized：`{"message": "No Token/Invalid Token", "code": 401}`
  - 500 Internal Server Error：`{"message": "上传文件失败（具体错误信息：如文件格式不支持、文件大小超限）", "code": 500}`
- **说明**：需要认证，上传文件后返回文件相关信息，用于后续提交审批等操作；文件大小限制为10MB

### 11. 根据文件ID下载文件
- **请求路径**：`/system/download/{file_id}`
- **请求方法**：GET
- **示例调用URL**：`https://api.example.com/system/download/1`（示例file_id=1）
- **请求头**：`Authorization: 示例Token`（需替换为实际登录获取的Token）
- **路径参数**：`file_id`（文件ID，必填，示例值：1）
- **成功响应**（200 OK）：文件流（浏览器会自动下载，响应头包含`Content-Disposition: attachment; filename="example.pdf"`）
- **错误响应**：
  - 401 Unauthorized：`{"message": "No Token/Invalid Token", "code": 401}`
  - 404 Not Found：`{"message": "文件不存在", "code": 404}`
  - 500 Internal Server Error：`{"message": "文件已被删除或移动/下载文件异常", "code": 500}`
- **说明**：需要认证，根据文件ID下载对应的文件

### 12. 提交审批材料
- **请求路径**：`/biz/submit`
- **请求方法**：POST
- **示例调用URL**：`https://api.example.com/biz/submit`
- **请求头**：`Authorization: 示例Token`（需替换为实际登录获取的Token）
- **请求体参数**：
  ```json
  {
    "task_id": 3, // 三级任务ID，示例值
    "filename": "example.pdf", // 已上传的文件名，示例值
    "reported_value": "100", // 上报值，示例值
    "data_type": "数值型" // 数据类型，示例值（数值型/文本型）
  }
  ```
- **成功响应**（200 OK）：
  ```json
  {
    "message": "提交成功",
    "submit_id": 1 // 审批单ID
  }
  ```
- **错误响应**：
  - 401 Unauthorized：`{"message": "No Token/Invalid Token", "code": 401}`
  - 500 Internal Server Error：`{"message": "错误信息（如任务不存在、非三级任务、文件格式错误、上报值与数据类型不匹配）", "code": 500}`
- **说明**：需要认证，仅允许提交三级任务的审批材料，提交后任务状态更新为“审核中”，并自动向部门负责人发送审批通知

### 13. 获取审批单
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
      "fileId": 1,
      "reportedValue": "100",
      "dataType": "数值型",
      "submitBy": 1,
      "submitDeptId": 1,
      "manageDeptId": 1,
      "submitTime": "2024-01-10T00:00:00",
      "fileSuffix": "pdf",
      "flowStatus": 10, // 10-待审核 20-审核通过 30-审核驳回
      "currentHandlerId": 2,
      "isDelete": 0,
      "auditOpinion": "" // 审核意见（审核后有值）
    }
  ]
  ```
- **错误响应**：
  - 401 Unauthorized：`{"message": "No Token/Invalid Token", "code": 401}`
  - 404 Not Found：`{"message": "未查询到该任务的审批单", "code": 404}`
  - 500 Internal Server Error：`{"message": "获取审批单失败,请检查任务id是否正确", "code": 500}`
- **说明**：需要认证，根据任务ID和当前登录用户权限（提交人/审核人）返回对应的审批单信息

### 14. 审批操作（通过/驳回）
- **请求路径**：`/biz/audit/operate`
- **请求方法**：POST
- **示例调用URL**：`https://api.example.com/biz/audit/operate`
- **请求头**：`Authorization: 示例Token`（需替换为实际登录获取的Token）
- **请求体参数**：
  ```json
  {
    "sub_id": 1, // 审批单ID，示例值
    "operate_type": "pass", // pass-通过 reject-驳回，示例值
    "opinion": "审批通过，符合要求" // 审核意见，示例值（驳回时必填）
  }
  ```
- **成功响应**（200 OK）：
  ```json
  {
    "message": "审批操作成功",
    "flow_status": 20 // 20-审核通过 30-审核驳回
  }
  ```
- **错误响应**：
  - 401 Unauthorized：`{"message": "No Token/Invalid Token", "code": 401}`
  - 403 Forbidden：`{"message": "无审批权限", "code": 403}`
  - 500 Internal Server Error：`{"message": "审批操作失败（如审批单不存在、状态异常、驳回时未填写意见）", "code": 500}`
- **说明**：需要认证，仅部门负责人/审批人可操作；审批通过后任务状态更新为“已完成”，驳回后任务状态更新为“驳回修改”，并向提交人发送通知

### 15. 发送通知
- **请求路径**：`/system/notice`
- **请求方法**：POST
- **示例调用URL**：`https://api.example.com/system/notice`
- **请求头**：`Authorization: 示例Token`（需替换为实际登录获取的Token）
- **请求体参数**：
  ```json
  {
    "to_user_id": 2, // 接收人ID，示例值
    "type": "audit_notice", // 通知类型：audit_notice-审批通知、system_notice-系统通知，示例值
    "trigger_event": "task_submit", // 触发事件：task_submit-任务提交、audit_operate-审批操作，示例值
    "title": "新的审批待办", // 通知标题，示例值
    "content": "您有一条任务审批待处理，任务名称：项目初始化", // 通知内容，示例值
    "source_type": "1", // 关联来源类型：1-任务 2-审批单，示例值
    "source_id": 1 // 关联业务ID（任务ID/审批单ID），示例值
  }
  ```
- **成功响应**（200 OK）：
  ```json
  {
    "message": "发送成功",
    "notice_id": 1
  }
  ```
- **错误响应**：
  - 401 Unauthorized：`{"message": "No Token/Invalid Token", "code": 401}`
  - 500 Internal Server Error：`{"message": "发送通知失败（如接收人不存在、参数缺失）", "code": 500}`
- **说明**：需要认证，系统内部调用或管理员操作，用于向指定用户发送业务通知

### 16. 获取当前用户通知列表
- **请求路径**：`/system/notice/list`
- **请求方法**：GET
- **示例调用URL**：`https://api.example.com/system/notice/list?page=1&size=10`
- **请求头**：`Authorization: 示例Token`（需替换为实际登录获取的Token）
- **请求参数**：
  - `page`（页码，必填，示例值：1）
  - `size`（每页条数，必填，示例值：10）
  - `read_status`（阅读状态，可选：0-未读 1-已读，示例值：0）
- **成功响应**（200 OK）：
  ```json
  {
    "total": 1,
    "list": [
      {
        "noticeId": 1,
        "toUserId": 2,
        "type": "audit_notice",
        "triggerEvent": "task_submit",
        "title": "新的审批待办",
        "content": "您有一条任务审批待处理，任务名称：项目初始化",
        "sourceType": "1",
        "sourceId": 1,
        "readStatus": 0, // 0-未读 1-已读
        "createTime": "2024-01-10T00:00:00",
        "readTime": null
      }
    ],
    "page": 1,
    "size": 10
  }
  ```
- **错误响应**：
  - 401 Unauthorized：`{"message": "No Token/Invalid Token", "code": 401}`
  - 500 Internal Server Error：`{"message": "获取通知列表失败", "code": 500}`
- **说明**：需要认证，返回当前登录用户的通知列表，支持分页和按阅读状态筛选

### 17. 标记通知为已读
- **请求路径**：`/system/notice/read/{noticeId}`
- **请求方法**：PUT
- **示例调用URL**：`https://api.example.com/system/notice/read/1`（示例noticeId=1）
- **请求头**：`Authorization: 示例Token`（需替换为实际登录获取的Token）
- **路径参数**：`noticeId`（通知ID，必填，示例值：1）
- **成功响应**（200 OK）：
  ```json
  {
    "message": "标记已读成功"
  }
  ```
- **错误响应**：
  - 401 Unauthorized：`{"message": "No Token/Invalid Token", "code": 401}`
  - 403 Forbidden：`{"message": "无权限操作该通知", "code": 403}`
  - 500 Internal Server Error：`{"message": "标记已读失败（如通知不存在）", "code": 500}`
- **说明**：需要认证，仅通知接收人可操作，标记后`readStatus`更新为1，`readTime`记录当前时间

## 通用错误码说明
| 状态码 | 说明 |
|--------|------|
| 401 | 未携带token、token无效或已过期 |
| 403 | 无接口操作权限（如非审批人执行审批操作） |
| 404 | 资源不存在（如任务ID/文件ID/审批单ID不存在） |
| 500 | 服务器内部错误（如业务逻辑异常、数据库操作失败） |

## 认证说明
- 除登录接口（`/system/login`）外，所有接口均需要在请求头携带`Authorization`令牌，格式为`Bearer {Token}`（注：部分场景可省略Bearer，仅传Token）
- 令牌有效期为1小时（从生成时间开始计算）
- 令牌无效时需重新登录获取新令牌
- 登出接口调用后，令牌立即失效，加入黑名单

## 数据格式说明
- 所有接口请求/响应数据编码为UTF-8
- 日期时间格式统一为ISO 8601标准（如：2024-01-01T00:00:00）
- 文件上传仅支持pdf、doc、docx格式，单个文件大小不超过10MB
- 任务级别（level）：1-一级任务 2-二级任务 3-三级任务（仅三级任务可提交审批）
- 审批单流程状态（flowStatus）：10-待审核 20-审核通过 30-审核驳回
