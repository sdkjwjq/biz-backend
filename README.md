# biz-backend
### 2025-12-8
完成登录功能

### 2025-12-11
完成登录、注销、修改密码

# 系统接口文档

## 基础信息
- 基础路径：无（接口路径已包含具体前缀）
- 认证方式：JWT Token（登录后获取，请求时通过`Authorization`请求头携带）


## 接口列表

### 1. 用户登录
- **请求路径**：`/system/login`
- **请求方法**：POST
- **请求体参数**：
  ```json
  {
    "user_name": "string", // 用户名（必填）
    "password": "string"  // 密码（必填）
  }
  ```
- **成功响应**（200 OK）：
  ```json
  {
    "nick_name": "string", // 用户昵称
    "token": "string"      // JWT令牌（后续请求需携带）
  }
  ```
- **错误响应**（500 Internal Server Error）：
  ```json
  {
    "message": "string", // 错误信息（如"用户不存在"、"密码错误"）
    "code": 500
  }
  ```
- **说明**：无需认证，登录成功后返回令牌用于后续接口调用


### 2. 用户登出
- **请求路径**：`/system/logout`
- **请求方法**：POST
- **请求头**：`Authorization: string`（登录获取的token，必填）
- **成功响应**（200 OK）：
  ```json
  {
    "message": "注销成功"
  }
  ```
- **错误响应**：
    - 401 Unauthorized：`{"message": "token不存在", "code": 401}`
    - 500 Internal Server Error：`{"message": "错误信息", "code": 500}`
- **说明**：登出后令牌将被加入黑名单，无法再使用


### 3. 修改密码
- **请求路径**：`/system/password`
- **请求方法**：POST
- **请求头**：`Authorization: string`（登录获取的token，必填）
- **请求体参数**：
  ```json
  {
    "new_password": "string" // 新密码（必填）
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
    "message": "错误信息（如用户不存在）",
    "code": 500
  }
  ```
- **说明**：基于当前登录用户（通过token解析用户ID）修改密码


### 4. 获取所有用户
- **请求路径**：`/system/getAllUsers`
- **请求方法**：GET
- **请求头**：`Authorization: string`（登录获取的token，必填）
- **成功响应**（200 OK）：
  ```json
  [
    {
      "userId": 1,
      "deptId": 1,
      "userName": "string",
      "nickName": "string",
      "email": "string",
      "password": "string", // 注意：实际返回可能加密或脱敏
      "role": "string",
      "status": "string",
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
## 补充接口文档

### 5. 获取全量任务数据
- **请求路径**：`/biz/tasks`
- **请求方法**：GET
- **请求头**：`Authorization: string`（登录获取的token，必填）
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
      "taskName": "任务名称",
      "level": 1,
      "deptId": 1,
      "principalId": 1,
      "leaderId": 1,
      "expTarget": "预期目标",
      "expLevel": "预期级别",
      "expEffect": "预期效果",
      "expMaterialDesc": "预期材料描述",
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
  ]
  ```
- **错误响应**：
  - 401 Unauthorized：`{"message": "No Token/Invalid Token", "code": 401}`
  - 500 Internal Server Error：`{"message": "错误信息", "code": 500}`
- **说明**：需要认证，返回系统中所有任务信息


### 6. 根据ID获取任务
- **请求路径**：`/biz/tasks/{taskId}`
- **请求方法**：GET
- **请求头**：`Authorization: string`（登录获取的token，必填）
- **路径参数**：`taskId`（任务ID，必填）
- **成功响应**（200 OK）：
  ```json
  {
    "taskId": 1,
    "projectId": 1,
    "parentId": 0,
    "ancestors": "0,1",
    "phase": 1,
    "taskCode": "TSK001",
    "taskName": "任务名称",
    "level": 1,
    "deptId": 1,
    "principalId": 1,
    "leaderId": 1,
    "expTarget": "预期目标",
    "expLevel": "预期级别",
    "expEffect": "预期效果",
    "expMaterialDesc": "预期材料描述",
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
- **说明**：需要认证，根据任务ID返回对应任务详情


### 7. 获取当前任务的所有子任务
- **请求路径**：`/biz/tasks/children`
- **请求方法**：GET
- **请求头**：`Authorization: string`（登录获取的token，必填）
- **请求参数**：`task_id`（父任务ID，必填）
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
- **请求头**：`Authorization: string`（登录获取的token，必填）
- **请求参数**：`task_id`（子任务ID，必填）
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

## 通用错误码说明
| 状态码 | 说明 |
|--------|------|
| 401 | 未携带token、token无效或已过期 |
| 500 | 服务器内部错误（如业务逻辑异常） |


## 认证说明
- 除登录接口（`/system/login`）外，所有接口均需要在请求头携带`Authorization`令牌
- 令牌有效期为1小时（从生成时间开始计算）
- 令牌无效时需重新登录获取新令牌
