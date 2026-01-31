# biz-backend 系统接口文档 v2.0

## 项目概述

`biz-backend` 是一个基于 Spring Boot 的业务管理后端系统，提供用户认证、任务管理、文件上传下载、审批流程、通知预警等核心功能，支持部门级任务协作与流程审批。

## 技术栈

- **核心框架**：Spring Boot 3.3.2
- **持久层**：MyBatis 3.0.3
- **数据库**：MySQL 9.1.0
- **认证授权**：JWT (java-jwt 4.4.0)
- **工具类**：Lombok 1.18.30、SLF4J 2.0.13

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 部署步骤

1. **克隆仓库**
   ```bash
   git clone https://github.com/sdkjwjq/biz-backend.git
   cd biz-backend
   ```

2. **配置数据库**
  - 新建 MySQL 数据库（推荐名称：`biz`）
  - 执行 `data/biz.sql` 初始化表结构
  - 执行 `data/insert.sql` 插入初始化数据
  - 修改 `application.properties` 配置数据库连接：
    ```properties
    spring.datasource.url=jdbc:mysql://localhost:3306/biz_db?useSSL=false&serverTimezone=UTC
    spring.datasource.username=root
    spring.datasource.password=your_password
    ```

3. **构建并启动**
   ```bash
   mvn clean package
   java -jar target/biz_backend-1.0-SNAPSHOT.jar
   ```

### 生产环境部署

#### 服务器信息
```
IP地址: 172.19.2.81
用户名: root
密码: Xzcit@xg.2025.8

数据库账号密码:
账号: root
密码: Xxxy@123
```

#### 后端部署
1. 修改数据库配置为生产环境密码
2. 使用 Maven 打包：先后执行 `clean` 和 `package`
3. 将生成的 `target/biz_backend-1.0-SNAPSHOT.jar` 上传到服务器
4. 停止旧进程：`ps aux|grep java` → `kill -9 [PID]`
5. 启动新进程：`nohup java -jar biz_backend-1.0-SNAPSHOT.jar &`

#### 前端部署
1. 修改 `vite.config.js`，将 IP 地址改为服务器 IP
2. 运行 `npm run builds` 生成 `dist` 文件夹
3. 将 `dist` 文件夹内容上传到 `/usr/share/nginx/html` 目录下

### 迭代记录
- 2025-12-08：完成登录功能
- 2025-12-11：完成登录、注销、修改密码
- 2025-12-21：补充实现根据部门ID获取任务、提交审批材料、获取审批单、文件上传/下载、通知发送/查看、审批任务、重新提交审批材料等接口
- 2025-12-26：修改登录方式为user_id登录，修改第一次审批人为AuditorId
- 2026-01-31：新增标志性成果管理、任务管理、定时任务触发接口

## 基础信息

- **基础路径**：无（接口路径已包含具体前缀）
- **认证方式**：JWT Token（登录后获取，请求时通过 `Authorization` 请求头携带）
- **服务地址**：`https://api.example.com`（实际调用时替换为部署的服务地址）
- **示例Token**：`eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoxLCJ1c2VyX25hbWUiOiJhZG1pbiIsImV4cCI6MTcxMjEwMzYwMH0.xxxxxx`

## Postman接口文档
https://www.postman.com/litianyi981119/biz/collection/21001135-3309c751-c3ca-4fdb-9d3e-f9aa8529b9c0/

## 接口列表

### 一、系统管理接口

#### 1.1 用户登录
- **接口**：`POST /system/login`
- **描述**：用户登录，返回Token
- **无需认证**：是
- **请求体**：
  ```json
  {
    "user_id": 110228,
    "password": "123456"
  }
  ```
- **响应**：
  ```json
  {
    "nick_name": "系统管理员",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
  ```

#### 1.2 用户注销
- **接口**：`POST /system/logout`
- **描述**：用户注销，Token加入黑名单
- **请求头**：`Authorization: Bearer {token}`
- **响应**：
  ```json
  {
    "message": "注销成功"
  }
  ```

#### 1.3 修改密码
- **接口**：`POST /system/password`
- **描述**：修改当前用户密码
- **请求头**：`Authorization: Bearer {token}`
- **请求体**：
  ```json
  {
    "new_password": "654321"
  }
  ```
- **响应**：
  ```json
  {
    "new_password": "密码修改成功"
  }
  ```

#### 1.4 获取所有用户
- **接口**：`GET /system/allUsers`
- **描述**：获取系统所有用户信息
- **请求头**：`Authorization: Bearer {token}`
- **响应**：
  ```json
  [
    {
      "userId": 1,
      "deptId": 1,
      "userName": "admin",
      "nickName": "系统管理员",
      "email": "admin@example.com",
      "role": "0",
      "status": "0",
      "isDelete": 0,
      "createTime": "2024-01-01 00:00:00",
      "updateTime": "2024-01-01 00:00:00"
    }
  ]
  ```

#### 1.5 添加用户
- **接口**：`POST /system/users/add`
- **描述**：管理员添加新用户
- **请求头**：`Authorization: Bearer {token}`
- **权限**：仅管理员（role=0）
- **请求体**：
  ```json
  {
    "userId": 110229,
    "deptId": 1,
    "userName": "newuser",
    "nickName": "新用户",
    "email": "new@example.com",
    "password": "123456",
    "role": "1",
    "status": "0",
    "isDelete": 0
  }
  ```
- **响应**：`"用户 新用户 添加成功"`

#### 1.6 更新用户
- **接口**：`POST /system/users/update`
- **描述**：管理员更新用户信息
- **请求头**：`Authorization: Bearer {token}`
- **权限**：仅管理员（role=0）
- **请求体**：同添加用户
- **响应**：`"用户 新用户 更新成功"`

#### 1.7 删除用户
- **接口**：`POST /system/users/delete/{userId}`
- **描述**：管理员删除用户（逻辑删除）
- **请求头**：`Authorization: Bearer {token}`
- **权限**：仅管理员（role=0）
- **路径参数**：`userId`（用户ID）
- **响应**：`"用户 110229 删除成功"`

#### 1.8 获取部门信息
- **接口**：`GET /system/dept/{deptId}`
- **描述**：根据部门ID获取部门信息
- **请求头**：`Authorization: Bearer {token}`
- **路径参数**：`deptId`（部门ID）
- **响应**：
  ```json
  {
    "deptId": 1,
    "deptName": "技术部",
    "leaderId": 110228,
    "status": "0",
    "isDelete": 0,
    "createTime": "2024-01-01 00:00:00",
    "updateTime": "2024-01-01 00:00:00"
  }
  ```

### 二、任务管理接口

#### 2.1 获取全量任务数据
- **接口**：`GET /biz/tasks`
- **描述**：根据用户角色获取任务列表
- **请求头**：`Authorization: Bearer {token}`
- **权限规则**：
  - 管理员(role=0)：返回所有任务
  - 普通用户(role=1)：返回本人负责或参与的任务
  - 负责人(role=2)：返回本人归口负责的任务
- **响应**：
  ```json
  [
    {
      "taskId": 1,
      "projectId": 1,
      "parentId": 0,
      "ancestors": "0",
      "phase": 2024,
      "taskCode": "TSK001",
      "taskName": "项目初始化",
      "level": 1,
      "comment": "任务描述",
      "deptId": 1,
      "deptName": "技术部",
      "auditorId": 110228,
      "auditorName": "管理员",
      "principalId": 110228,
      "principalName": "管理员",
      "leaderId": 110228,
      "leaderName": "管理员",
      "expTarget": "完成框架搭建",
      "expLevel": "国家级",
      "expEffect": "提升效率",
      "expMaterialDesc": "文档材料",
      "dataType": "1",
      "targetValue": 100.00,
      "currentValue": 80.00,
      "weight": 0.5,
      "progress": 80,
      "status": "1",
      "isDelete": 0,
      "createTime": "2024-01-01 00:00:00",
      "updateTime": "2024-01-02 00:00:00"
    }
  ]
  ```

#### 2.2 根据ID获取任务
- **接口**：`GET /biz/tasks/{taskId}`
- **描述**：根据任务ID获取任务详情
- **请求头**：`Authorization: Bearer {token}`
- **路径参数**：`taskId`（任务ID）
- **响应**：单个任务对象（同2.1格式）

#### 2.3 获取所有子任务
- **接口**：`GET /biz/tasks/children`
- **描述**：获取当前任务的所有子任务
- **请求头**：`Authorization: Bearer {token}`
- **查询参数**：`task_id`（父任务ID）
- **响应**：任务列表（同2.1格式）

#### 2.4 获取父任务
- **接口**：`GET /biz/tasks/parent`
- **描述**：获取当前任务的父任务
- **请求头**：`Authorization: Bearer {token}`
- **查询参数**：`task_id`（子任务ID）
- **响应**：单个任务对象（同2.1格式）

#### 2.5 根据部门ID获取任务
- **接口**：`GET /biz/tasks/dept`
- **描述**：根据部门ID获取该部门所有任务
- **请求头**：`Authorization: Bearer {token}`
- **查询参数**：`dept_id`（部门ID）
- **响应**：任务列表（同2.1格式）

#### 2.6 添加任务
- **接口**：`POST /biz/tasks/manage/add`
- **描述**：添加新任务（仅限三级任务）
- **请求头**：`Authorization: Bearer {token}`
- **请求体**：
  ```json
  {
    "taskId": null,
    "projectId": 1,
    "parentId": 2,
    "ancestors": "0,1,2",
    "phase": 2024,
    "taskCode": "TSK003",
    "taskName": "三级任务示例",
    "level": 3,
    "comment": "任务描述",
    "deptId": 1,
    "auditorId": 110228,
    "principalId": 110228,
    "leaderId": 110229,
    "expTarget": "完成具体实施",
    "expLevel": "省级",
    "expEffect": "实现目标",
    "expMaterialDesc": "实施材料",
    "dataType": "1",
    "targetValue": 50.00,
    "currentValue": 0.00,
    "weight": 0.3,
    "progress": 0,
    "status": "0"
  }
  ```
- **响应**：`"任务三级任务示例添加成功"`

#### 2.7 更新任务
- **接口**：`POST /biz/tasks/manage/update`
- **描述**：更新任务信息
- **请求头**：`Authorization: Bearer {token}`
- **请求体**：同添加任务（需包含taskId）
- **响应**：`"任务三级任务示例更新成功"`

#### 2.8 完成任务
- **接口**：`POST /biz/tasks/manage/finish/{taskId}`
- **描述**：管理员标记任务为完成状态
- **请求头**：`Authorization: Bearer {token}`
- **权限**：仅管理员（role=0）
- **路径参数**：`taskId`（任务ID）
- **响应**：`"任务三级任务示例已完成"`

### 三、审批流程接口

#### 3.1 提交审批材料
- **接口**：`POST /biz/submit`
- **描述**：提交任务审批材料，启动审批流程
- **请求头**：`Authorization: Bearer {token}`
- **请求体**：
  ```json
  {
    "task_id": 3,
    "reported_value": 100,
    "data_type": "1",
    "file_id": 1,
    "comment": "备注信息"
  }
  ```
- **响应**：`"提交成功，下一位审批人是张三"`

#### 3.2 获取审批单
- **接口**：`GET /biz/audit/{taskId}`
- **描述**：根据任务ID获取当前用户的审批单
- **请求头**：`Authorization: Bearer {token}`
- **路径参数**：`taskId`（任务ID）
- **响应**：
  ```json
  [
    {
      "subId": 1,
      "taskId": 3,
      "fileId": 1,
      "filename": "report.pdf",
      "reportedValue": 100.00,
      "dataType": "1",
      "submitBy": 110229,
      "submitDeptId": 1,
      "manageDeptId": 1,
      "submitTime": "2024-01-05 00:00:00",
      "fileSuffix": "pdf",
      "flowStatus": 10,
      "currentHandlerId": 110228,
      "isDelete": 0
    }
  ]
  ```

#### 3.3 获取待我审批列表
- **接口**：`GET /biz/audit/todo`
- **描述**：获取当前用户待处理的审批单
- **请求头**：`Authorization: Bearer {token}`
- **响应**：审批单列表（同3.2格式）

#### 3.4 获取任务全部审批单
- **接口**：`GET /biz/audit/task/{taskId}`
- **描述**：获取指定任务的全部审批记录
- **请求头**：`Authorization: Bearer {token}`
- **权限**：管理员/任务负责人/归口负责人/提交人
- **路径参数**：`taskId`（任务ID）
- **响应**：审批单列表（同3.2格式）

#### 3.5 获取审批操作日志
- **接口**：`GET /biz/audit/logs/{subId}`
- **描述**：获取审批单的操作日志
- **请求头**：`Authorization: Bearer {token}`
- **路径参数**：`subId`（提交ID）
- **响应**：
  ```json
  [
    {
      "logId": 1,
      "subId": 1,
      "operatorId": 110229,
      "actionType": "提交",
      "preStatus": 0,
      "postStatus": 10,
      "comment": "提交任务",
      "createTime": "2024-01-05 00:00:00"
    }
  ]
  ```

#### 3.6 审批操作
- **接口**：`POST /biz/audit`
- **描述**：审批通过或退回
- **请求头**：`Authorization: Bearer {token}`
- **请求体**：
  ```json
  {
    "sub_id": 1,
    "is_pass": true,
    "title": "审批意见标题",
    "content": "详细审批意见"
  }
  ```
- **响应**：
  - 通过：`"已审批，下一位审批人为李四"`
  - 退回：`"已退回，退回到王五"`

#### 3.7 撤回提交
- **接口**：`POST /biz/drawback/{taskId}`
- **描述**：撤回已提交的审批申请
- **请求头**：`Authorization: Bearer {token}`
- **权限**：仅提交人
- **路径参数**：`taskId`（任务ID）
- **响应**：`"已撤回提交"`

#### 3.8 重新提交材料
- **接口**：`POST /biz/resub`
- **描述**：重新提交被退回的审批材料
- **请求头**：`Authorization: Bearer {token}`
- **请求体**：
  ```json
  {
    "sub_id": 1,
    "reported_value": 95,
    "data_type": "1",
    "file_id": 2
  }
  ```
- **响应**：`"已重新提交,审核人为张三"`

#### 3.9 获取上次审批文件
- **接口**：`GET /biz/audit/file/{taskId}`
- **描述**：获取任务上一次审批通过的文件
- **请求头**：`Authorization: Bearer {token}`
- **路径参数**：`taskId`（任务ID）
- **响应**：
  ```json
  {
    "fileId": 1,
    "filename": "last_report.pdf",
    "filepath": "/uploads/last_report.pdf"
  }
  ```

### 四、文件管理接口

#### 4.1 上传文件
- **接口**：`POST /system/upload/{task_id}`
- **描述**：上传文件并关联任务
- **请求头**：`Authorization: Bearer {token}`
- **Content-Type**：`multipart/form-data`
- **路径参数**：`task_id`（任务ID）
- **表单参数**：`file`（文件，支持pdf/doc/docx）
- **响应**：
  ```json
  {
    "fileId": 1,
    "filename": "report.pdf",
    "filepath": "/uploads/report.pdf"
  }
  ```

#### 4.2 下载文件
- **接口**：`GET /system/download/{file_id}`
- **描述**：根据文件ID下载文件
- **请求头**：`Authorization: Bearer {token}`
- **路径参数**：`file_id`（文件ID）
- **响应**：文件流

### 五、通知管理接口

#### 5.1 发送通知
- **接口**：`POST /system/notice`
- **描述**：发送系统通知
- **请求头**：`Authorization: Bearer {token}`
- **请求体**：
  ```json
  {
    "to_user_id": 110229,
    "type": "1",
    "trigger_event": "任务审核",
    "title": "新的审批单",
    "content": "您有新的任务需要审核",
    "source_type": "0",
    "source_id": 3
  }
  ```
- **响应**：`"发送成功"`

#### 5.2 查看通知
- **接口**：`GET /system/notice`
- **描述**：查看当前用户收到的通知
- **请求头**：`Authorization: Bearer {token}`
- **响应**：
  ```json
  [
    {
      "noticeId": 1,
      "fromUserId": 110228,
      "toUserId": 110229,
      "type": "1",
      "triggerEvent": "任务审核",
      "title": "新的审批单",
      "content": "您有新的任务需要审核",
      "sourceType": "0",
      "sourceId": 3,
      "isRead": "0",
      "isDelete": 0,
      "createTime": "2024-01-06 00:00:00"
    }
  ]
  ```

#### 5.3 标记已读
- **接口**：`POST /system/notice/{notice_id}`
- **描述**：将通知标记为已读
- **请求头**：`Authorization: Bearer {token}`
- **路径参数**：`notice_id`（通知ID）
- **响应**：`"已读"`

#### 5.4 发送预警
- **接口**：`POST /system/alert`
- **描述**：发送站内预警信息
- **请求头**：`Authorization: Bearer {token}`
- **请求体**：
  ```json
  {
    "to_user_nick_name": "张三",
    "title": "任务预警",
    "content": "任务进度滞后",
    "source_id": 3
  }
  ```
- **响应**：`"发送成功"`

### 六、标志性成果接口

#### 6.1 查询单个成果
- **接口**：`GET /achievement/{achId}`
- **描述**：根据ID查询标志性成果
- **请求头**：`Authorization: Bearer {token}`
- **路径参数**：`achId`（成果ID）
- **响应**：
  ```json
  {
    "achId": 1,
    "category": 1,
    "level": "国家级",
    "achName": "国家级教学成果奖",
    "department": "教育部",
    "gotTime": "2024-01-01 00:00:00",
    "deptId": 1,
    "fileId": 1,
    "comment": "备注信息",
    "isCompetition": 0,
    "teDengJiang": 0,
    "yiDengJiang": 1,
    "erDengJiang": 0,
    "sanDengJiang": 0,
    "jinJiang": 0,
    "yinJiang": 0,
    "tongJiang": 0,
    "youShengJiang": 0,
    "budDengDengCi": 0,
    "createBy": 110228,
    "isDelete": 0,
    "createTime": "2024-01-01 00:00:00"
  }
  ```

#### 6.2 查询所有成果
- **接口**：`GET /achievement/`
- **描述**：查询所有未删除的标志性成果
- **请求头**：`Authorization: Bearer {token}`
- **响应**：成果列表（同6.1格式）

#### 6.3 新增成果
- **接口**：`POST /achievement/add`
- **描述**：新增标志性成果
- **请求头**：`Authorization: Bearer {token}`
- **请求体**：同6.1响应格式（不含achId）
- **响应**：`"标志性成果「国家级教学成果奖」添加成功，成果ID：1"`

#### 6.4 更新成果
- **接口**：`POST /achievement/update/{id}`
- **描述**：更新标志性成果
- **请求头**：`Authorization: Bearer {token}`
- **路径参数**：`id`（成果ID）
- **请求体**：同6.1响应格式
- **响应**：`"标志性成果「国家级教学成果奖」修改成功"`

#### 6.5 删除成果
- **接口**：`POST /achievement/delete/{achId}`
- **描述**：逻辑删除标志性成果
- **请求头**：`Authorization: Bearer {token}`
- **路径参数**：`achId`（成果ID）
- **响应**：`"标志性成果「国家级教学成果奖」删除成功"`

### 七、定时任务接口

#### 7.1 触发月度部门负责人提醒
- **接口**：`POST /scheduled/month_leader_trigger`
- **描述**：手动触发月度部门负责人提醒（原自动每月1号9:00执行）
- **请求头**：`Authorization: Bearer {token}`
- **响应**：`"向X名用户发送月度提醒成功"`

#### 7.2 触发月度审核人提醒
- **接口**：`POST /scheduled/month_auditor_trigger`
- **描述**：手动触发月度审核人提醒（原自动每月1号9:00执行）
- **请求头**：`Authorization: Bearer {token}`
- **响应**：`"向X名用户发送月度提醒成功"`

#### 7.3 触发年度提醒
- **接口**：`POST /scheduled/year_trigger`
- **描述**：手动触发年度提醒（原自动每年1月1号10:00执行）
- **请求头**：`Authorization: Bearer {token}`
- **响应**：`"年度提醒完成，成功发送 X 条提醒"`

#### 7.4 更新任务状态
- **接口**：`POST /scheduled/update_task_status`
- **描述**：手动触发任务状态更新（原自动每年1月1号10:00执行）
- **请求头**：`Authorization: Bearer {token}`
- **响应**：无

## 通用说明

### 状态码说明
| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 401 | 未认证或Token无效 |
| 403 | 权限不足 |
| 500 | 服务器内部错误 |

### 认证说明
- 除登录接口外，所有接口需要在请求头携带Token
- Token格式：`Authorization: Bearer {token}`
- Token有效期为1小时
- 注销后Token会被加入黑名单

### 权限说明
| 角色值 | 角色 | 权限说明 |
|--------|------|----------|
| 0 | 管理员 | 所有权限 |
| 1 | 普通用户 | 本人任务相关权限 |
| 2 | 负责人 | 归口负责任务权限 |

### 审批流程状态
| 状态值 | 状态说明 |
|--------|----------|
| 0 | 草稿 |
| 10 | 待部门负责人审批 |
| 20 | 待归口负责人审批 |
| 30 | 待管理员审批 |
| 40 | 已归档 |
| -10 | 被部门负责人退回 |
| -20 | 被归口负责人退回 |
| -30 | 被管理员退回 |

### 任务状态
| 状态值 | 状态说明 |
|--------|----------|
| 0 | 未开始 |
| 1 | 进行中 |
| 2 | 审核中 |
| 3 | 已完成 |

### 数据类型
| 类型值 | 类型说明 |
|--------|----------|
| 0 | 对指标没有影响 |
| 1 | 数值累加 |
| 2 | 百分比取大 |

## 注意事项

1. **文件格式限制**：仅支持pdf、doc、docx格式
2. **任务层级**：系统采用三级任务结构，只能提交三级任务的审批
3. **审批流程**：部门负责人→归口负责人→管理员三级审批
4. **定时任务**：系统内置定时提醒功能，也可手动触发
5. **数据安全**：敏感操作需管理员权限，所有操作记录日志

## 更新日志

### v2.0 (2026-01-31)
- 新增标志性成果管理接口
- 新增定时任务触发接口
- 完善任务管理接口
- 补充完整审批流程接口
- 规范接口文档格式

### v1.2 (2025-12-26)
- 修改登录方式为user_id登录
- 调整首次审批人为任务AuditorId

### v1.1 (2025-12-21)
- 新增审批流程相关接口
- 新增文件管理接口
- 新增通知系统接口

### v1.0 (2025-12-11)
- 基础用户认证功能
- 基础任务管理功能
