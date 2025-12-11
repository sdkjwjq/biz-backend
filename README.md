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


## 通用错误码说明
| 状态码 | 说明 |
|--------|------|
| 401 | 未携带token、token无效或已过期 |
| 500 | 服务器内部错误（如业务逻辑异常） |


## 认证说明
- 除登录接口（`/system/login`）外，所有接口均需要在请求头携带`Authorization`令牌
- 令牌有效期为1小时（从生成时间开始计算）
- 令牌无效时需重新登录获取新令牌