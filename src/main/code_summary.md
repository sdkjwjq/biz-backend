# 代码文件汇总

生成时间: 2026-01-31 19:10:57
源目录: `D:\college\0workspace\biz-backend\biz-backend\src\main`

---

## 目录

- [code_summary.md](#code_summarymd)
- [get_code.py](#get_codepy)
- [java\org\example\BizApplication.java](#bizapplicationjava)
- [java\org\example\config\JacksonConfig.java](#jacksonconfigjava)
- [java\org\example\config\JwtInterceptor.java](#jwtinterceptorjava)
- [java\org\example\config\UserRoleInterceptor.java](#userroleinterceptorjava)
- [java\org\example\config\WebConfig.java](#webconfigjava)
- [java\org\example\controller\AchievementController.java](#achievementcontrollerjava)
- [java\org\example\controller\BizController.java](#bizcontrollerjava)
- [java\org\example\controller\ScheduledTaskController.java](#scheduledtaskcontrollerjava)
- [java\org\example\controller\SysController.java](#syscontrollerjava)
- [java\org\example\entity\BizAchievement.java](#bizachievementjava)
- [java\org\example\entity\BizAuditLog.java](#bizauditlogjava)
- [java\org\example\entity\BizMaterialSubmission.java](#bizmaterialsubmissionjava)
- [java\org\example\entity\BizPerformance.java](#bizperformancejava)
- [java\org\example\entity\BizPerformanceYear.java](#bizperformanceyearjava)
- [java\org\example\entity\BizProject.java](#bizprojectjava)
- [java\org\example\entity\BizProjectPhase.java](#bizprojectphasejava)
- [java\org\example\entity\BizTask.java](#biztaskjava)
- [java\org\example\entity\dto\BizAuditDTO.java](#bizauditdtojava)
- [java\org\example\entity\dto\BizReSubDTO.java](#bizresubdtojava)
- [java\org\example\entity\dto\BizSubDTO.java](#bizsubdtojava)
- [java\org\example\entity\dto\BizTaskDTO.java](#biztaskdtojava)
- [java\org\example\entity\dto\FileUploadDTO.java](#fileuploaddtojava)
- [java\org\example\entity\dto\SysAlertDTO.java](#sysalertdtojava)
- [java\org\example\entity\dto\SysLoginDTO.java](#syslogindtojava)
- [java\org\example\entity\dto\SysNoticeDTO.java](#sysnoticedtojava)
- [java\org\example\entity\dto\SysPwdDTO.java](#syspwddtojava)
- [java\org\example\entity\dto\SysUserDTO.java](#sysuserdtojava)
- [java\org\example\entity\RelTaskPerformance.java](#reltaskperformancejava)
- [java\org\example\entity\SysDept.java](#sysdeptjava)
- [java\org\example\entity\SysFile.java](#sysfilejava)
- [java\org\example\entity\SysNotice.java](#sysnoticejava)
- [java\org\example\entity\SysUser.java](#sysuserjava)
- [java\org\example\entity\TokenBlacklist.java](#tokenblacklistjava)
- [java\org\example\entity\vo\BizAuditVO.java](#bizauditvojava)
- [java\org\example\entity\vo\BizTaskVo.java](#biztaskvojava)
- [java\org\example\entity\vo\ErrorVO.java](#errorvojava)
- [java\org\example\entity\vo\FileUploadVO.java](#fileuploadvojava)
- [java\org\example\entity\vo\SysLoginVO.java](#sysloginvojava)
- [java\org\example\entity\vo\SysLogoutVO.java](#syslogoutvojava)
- [java\org\example\entity\vo\SysPasswordVO.java](#syspasswordvojava)
- [java\org\example\mapper\AchievementMapper.java](#achievementmapperjava)
- [java\org\example\mapper\BizMapper.java](#bizmapperjava)
- [java\org\example\mapper\SysMapper.java](#sysmapperjava)
- [java\org\example\mapper\TokenBlacklistMapper.java](#tokenblacklistmapperjava)
- [java\org\example\service\AchievementService.java](#achievementservicejava)
- [java\org\example\service\BizService.java](#bizservicejava)
- [java\org\example\service\ScheduledTaskService.java](#scheduledtaskservicejava)
- [java\org\example\service\SysService.java](#sysservicejava)
- [java\org\example\utils\FileUploadUtil.java](#fileuploadutiljava)
- [java\org\example\utils\JWTUtil.java](#jwtutiljava)
- [resources\application.properties](#applicationproperties)

---

#### code_summary.md

```markdown
```

#### get_code.py

```python
#!/usr/bin/env python3
"""
代码文件转Markdown脚本
将当前目录及其子目录中的所有代码文件转换为Markdown格式
"""

import os
import argparse
from pathlib import Path
from datetime import datetime

# 常见代码文件的扩展名映射
FILE_EXTENSIONS = {
    # 编程语言
    '.py': 'python',
    '.java': 'java',
    '.js': 'javascript',
    '.ts': 'typescript',
    '.jsx': 'jsx',
    '.tsx': 'tsx',
    '.c': 'c',
    '.cpp': 'cpp',
    '.cc': 'cpp',
    '.h': 'c',
    '.hpp': 'cpp',
    '.cs': 'csharp',
    '.go': 'go',
    '.rs': 'rust',
    '.rb': 'ruby',
    '.php': 'php',
    '.swift': 'swift',
    '.kt': 'kotlin',
    '.kts': 'kotlin',
    '.scala': 'scala',

    # 脚本和配置
    '.sh': 'bash',
    '.bash': 'bash',
    '.zsh': 'bash',
    '.ps1': 'powershell',
    '.yml': 'yaml',
    '.yaml': 'yaml',
    '.json': 'json',
    '.xml': 'xml',
    '.html': 'html',
    '.css': 'css',
    '.scss': 'scss',
    '.less': 'less',
    '.sql': 'sql',
    '.md': 'markdown',

    # 其他
    '.txt': 'text',
    '.cfg': 'ini',
    '.ini': 'ini',
    '.toml': 'toml',
}

# 要排除的目录
EXCLUDE_DIRS = {
    '.git', '__pycache__', 'node_modules', 'venv', '.env',
    'dist', 'build', 'target', 'out', 'bin', 'obj'
}

# 要排除的文件
EXCLUDE_FILES = {
    '.DS_Store', 'Thumbs.db', '.gitignore', '.gitattributes'
}

def get_language_from_extension(file_path):
    """根据文件扩展名获取编程语言"""
    ext = Path(file_path).suffix.lower()
    return FILE_EXTENSIONS.get(ext, 'text')

def should_exclude_file(file_path):
    """检查文件是否应该被排除"""
    path = Path(file_path)

    # 检查是否在排除的目录中
    for part in path.parts:
        if part in EXCLUDE_DIRS:
            return True

    # 检查是否是排除的文件
    if path.name in EXCLUDE_FILES:
        return True

    # 检查是否是隐藏文件（以点开头）
    if path.name.startswith('.'):
        return True

    return False

def read_file_content(file_path):
    """读取文件内容，处理编码问题"""
    try:
        # 尝试多种编码
        encodings = ['utf-8', 'gbk', 'latin-1', 'iso-8859-1']

        for encoding in encodings:
            try:
                with open(file_path, 'r', encoding=encoding) as f:
                    return f.read()
            except UnicodeDecodeError:
                continue

        # 如果所有编码都失败，尝试二进制读取
        with open(file_path, 'rb') as f:
            content = f.read()
            # 尝试解码为utf-8，忽略错误
            return content.decode('utf-8', errors='ignore')

    except Exception as e:
        return f"无法读取文件: {e}"

def collect_code_files(root_dir):
    """收集所有代码文件"""
    code_files = []

    for root, dirs, files in os.walk(root_dir):
        # 排除不需要的目录
        dirs[:] = [d for d in dirs if d not in EXCLUDE_DIRS]

        for file in files:
            file_path = os.path.join(root, file)

            if should_exclude_file(file_path):
                continue

            # 获取相对路径
            rel_path = os.path.relpath(file_path, root_dir)
            code_files.append((rel_path, file_path))

    # 按文件路径排序
    code_files.sort(key=lambda x: x[0].lower())

    return code_files

def generate_markdown(root_dir, output_file, max_file_size=1024*1024):  # 默认1MB
    """生成Markdown文档"""

    print(f"正在扫描目录: {root_dir}")
    code_files = collect_code_files(root_dir)

    if not code_files:
        print("未找到任何代码文件")
        return

    print(f"找到 {len(code_files)} 个文件")

    with open(output_file, 'w', encoding='utf-8') as md_file:
        # 写入标题
        md_file.write(f"# 代码文件汇总\n\n")
        md_file.write(f"生成时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
        md_file.write(f"源目录: `{root_dir}`\n\n")
        md_file.write("---\n\n")

        # 写入目录
        md_file.write("## 目录\n\n")
        for rel_path, _ in code_files:
            filename = os.path.basename(rel_path)
            md_file.write(f"- [{rel_path}](#{filename.replace('.', '').lower()})\n")
        md_file.write("\n---\n\n")

        # 写入每个文件的内容
        for rel_path, full_path in code_files:
            filename = os.path.basename(rel_path)
            language = get_language_from_extension(filename)

            print(f"处理文件: {rel_path}")

            # 检查文件大小
            file_size = os.path.getsize(full_path)
            if file_size > max_file_size:
                print(f"  警告: 文件过大 ({file_size} bytes)，跳过")
                md_file.write(f"#### {rel_path}\n")
                md_file.write(f"\n文件过大 ({file_size} bytes)，已跳过\n\n")
                continue

            # 读取文件内容
            content = read_file_content(full_path)

            # 写入Markdown
            md_file.write(f"#### {rel_path}\n")
            md_file.write(f"\n```{language}\n")
            md_file.write(content)

            # 确保代码块以换行结束
            if content and not content.endswith('\n'):
                md_file.write('\n')

            md_file.write("```\n\n")

    print(f"\nMarkdown文件已生成: {output_file}")

def main():
    parser = argparse.ArgumentParser(description='将代码文件转换为Markdown格式')
    parser.add_argument('-o', '--output', default='code_summary.md',
                       help='输出Markdown文件名 (默认: code_summary.md)')
    parser.add_argument('-d', '--dir', default='.',
                       help='要扫描的目录 (默认: 当前目录)')
    parser.add_argument('-s', '--max-size', type=int, default=1024*1024,
                       help='最大文件大小(bytes)，超过此大小的文件将被跳过 (默认: 1MB)')

    args = parser.parse_args()

    root_dir = os.path.abspath(args.dir)
    output_file = args.output

    if not os.path.exists(root_dir):
        print(f"错误: 目录不存在: {root_dir}")
        return

    generate_markdown(root_dir, output_file, args.max_size)

if __name__ == "__main__":
    main()
```

#### java\org\example\BizApplication.java

```java
package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 业务应用主启动类
 */
@SpringBootApplication
@EnableScheduling  // 启用定时任务支持
public class BizApplication {
    /**
     * 应用入口方法
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(BizApplication.class, args);
    }
}
```

#### java\org\example\config\JacksonConfig.java

```java
package org.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Jackson全局配置：统一Date类型序列化/反序列化格式
 */
@Configuration
public class JacksonConfig {

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = new ObjectMapper();

        // 1. 指定Date类型的解析/生成格式（匹配yyyy-MM-dd HH:mm:ss）
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        // 2. 设置时区（避免时间偏移，建议使用东八区）
        objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        // 3. 解决序列化时Date类型为时间戳的问题
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 4. 支持Java 8时间类型（可选，兼容LocalDateTime等）
        objectMapper.registerModule(new JavaTimeModule());

        converter.setObjectMapper(objectMapper);
        return converter;
    }
}
```

#### java\org\example\config\JwtInterceptor.java

```java
package org.example.config;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.entity.vo.ErrorVO;
import org.example.mapper.TokenBlacklistMapper;
import org.example.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * JWT拦截器：验证请求中的Token有效性
 * - 检查Token是否存在
 * - 验证Token是否在黑名单中
 * - 解析Token并设置用户角色信息
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private TokenBlacklistMapper tokenBlacklistMapper;

    /**
     * 前置处理：验证Token
     * @param request HTTP请求
     * @param response HTTP响应
     * @param handler 处理器
     * @return 验证通过返回true，否则返回false
     * @throws Exception 异常信息
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            sendError(response, 401, "No Token");
            return false;
        }

        // 检查黑名单
        if (tokenBlacklistMapper.findByToken(token) != null) {
            sendError(response, 401, "Invalid Token");
            return false;
        }

        try {
            DecodedJWT decodedJWT = JWTUtil.verifyJwtToken(token);
            request.setAttribute("userRole", decodedJWT.getClaim("role").asString());
            return true;
        } catch (Exception e) {
            sendError(response, 401, "No Token: " + e.getMessage());
            return false;
        }
    }

    /**
     * 发送错误响应
     * @param response HTTP响应
     * @param code 错误码
     * @param message 错误信息
     * @throws IOException IO异常
     */
    private void sendError(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json");
        response.getWriter().write(new ObjectMapper().writeValueAsString(new ErrorVO(message, code)));
    }
}
```

#### java\org\example\config\UserRoleInterceptor.java

```java
package org.example.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.utils.JWTUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;

/**
 * 用户角色拦截器：验证用户角色权限
 * 仅允许管理员角色（role=0）访问受保护接口
 */
@Component
public class UserRoleInterceptor implements HandlerInterceptor {

    /** 允许访问的角色：0-管理员 */
    private static final String ALLOWED_ROLE = "0";

    /**
     * 角色验证，待调试，待完善
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 从请求头解析Token获取角色（你的真实逻辑）
        String token = request.getHeader("Authorization");
        String userRole = JWTUtil.getRoleFromToken(token);

        // 2. 角色校验：无角色/角色非0则拒绝访问
        if (userRole == null || !ALLOWED_ROLE.equals(userRole)) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpStatus.FORBIDDEN.value());
            PrintWriter writer = response.getWriter();
            writer.write("{\"code\":403,\"msg\":\"无权限访问该接口，仅管理员可访问\"}");
            writer.flush();
            writer.close();
            return false;
        }

        // 角色校验通过，放行请求
        return true;
    }
}
```

#### java\org\example\config\WebConfig.java

```java
package org.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类：配置CORS、拦截器、路径匹配等
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置路径匹配
     * @param configurer 路径匹配配置器
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 不区分尾部斜杠
        configurer.setUseTrailingSlashMatch(true);
    }

    /**
     * 配置CORS跨域
     * @param registry CORS注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("*")
                .allowedOriginPatterns("*") // 允许的域名
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Autowired
    private UserRoleInterceptor userRoleInterceptor;

    /**
     * 配置拦截器
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/system/**")
                .addPathPatterns("/biz/**")
                .addPathPatterns("/achievement/**")
                .excludePathPatterns("/system/login");

        registry.addInterceptor(userRoleInterceptor)
                .addPathPatterns("/system/users/**");
    }
}
```

#### java\org\example\controller\AchievementController.java

```java
package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.entity.BizAchievement;
import org.example.entity.vo.ErrorVO;
import org.example.service.AchievementService;
import org.example.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标志性成果控制层
 * 处理标志性成果基础增删查改HTTP请求
 */
@RestController
@RequestMapping("/achievement")
public class AchievementController {

    @Autowired
    private AchievementService achievementService;

    /**
     * 根据成果ID查询单条标志性成果信息
     * @param achId 成果ID
     * @return 成果实体/错误信息
     */
    @GetMapping("/{achId}")
    public Object getAchievementById(@PathVariable Long achId) {
        try {
            return achievementService.getAchievementById(achId);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 查询所有未删除的标志性成果列表
     * @return 成果实体列表/错误信息
     */
    @GetMapping("/")
    public Object listAllAchievements() {
        try {
            List<BizAchievement> achievementList = achievementService.listAllAchievements();
            return achievementList;
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 新增标志性成果
     * @param achievement 成果实体（JSON请求体）
     * @return 操作结果/错误信息
     */
    @PostMapping("/add")
    public Object addAchievement(@RequestBody BizAchievement achievement, HttpServletRequest  request) {
        try {
            Long achId = achievementService.addAchievement(achievement, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
            return "标志性成果「" + achievement.getAchName() + "」添加成功，成果ID：" + achId;
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 修改标志性成果信息
     * @param achievement 成果实体（含成果ID，JSON请求体）
     * @return 操作结果/错误信息
     */
    @PostMapping("/update/{id}")
    public Object updateAchievement(@PathVariable("id") Long id,@RequestBody BizAchievement achievement) {
        try {
            achievementService.updateAchievement(id,achievement);
            return "标志性成果「" + achievement.getAchName() + "」修改成功";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 逻辑删除标志性成果
     * @param achId 成果ID（路径参数）
     * @return 操作结果/错误信息
     */
    @PostMapping("/delete/{achId}")
    public Object deleteAchievement(@PathVariable Long achId) {
        try {
            // 查询成果名称，用于返回友好提示
            BizAchievement achievement = achievementService.getAchievementById(achId);
            achievementService.deleteAchievement(achId);
            return "标志性成果「" + achievement.getAchName() + "」删除成功";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }
}
```

#### java\org\example\controller\BizController.java

```java
package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.entity.dto.BizAuditDTO;
import org.example.entity.dto.BizSubDTO;
import org.example.entity.dto.BizReSubDTO;
import org.example.entity.dto.BizTaskDTO;
import org.example.entity.vo.ErrorVO;
import org.example.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.example.service.BizService;

/**
 * 业务控制器：处理所有业务相关请求
 * 包含任务管理、材料提交、审批流程等功能
 */
@RestController
@RequestMapping("/biz")
public class BizController {
    @Autowired
    private BizService bizService;

    /**
     * 获取全量任务数据
     * @param request HTTP请求
     * @return 任务列表或错误信息
     */
    @GetMapping("/tasks")
    public Object getTasks(HttpServletRequest request){
        try{
            return bizService.getTasksByUserRole(JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 根据id获取任务
     * @param taskId 任务ID
     * @return 任务详情或错误信息
     */
    @GetMapping("/tasks/{taskId}")
    public Object getTaskById(@PathVariable("taskId") Long taskId){
        try{
            return bizService.getTaskById(taskId);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取当前任务的所有子任务
     * @param taskId 任务ID
     * @return 子任务列表或错误信息
     */
    @GetMapping("/tasks/children")
    public Object getAllChildrenTasks(@RequestParam("task_id") Long taskId){
        try{
            return bizService.getAllChildrenTasks(taskId);
        }catch (Exception e){
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取当前任务的父任务
     * @param taskId 任务ID
     * @return 父任务信息或错误信息
     */
    @GetMapping("/tasks/parent")
    public Object getParentTask(@RequestParam("task_id") Long taskId){
        try{
            return bizService.getParentTask(taskId);
        }catch (Exception e){
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 根据部门id获取任务
     * @param deptId 部门ID
     * @return 任务列表或错误信息
     */
    @GetMapping("/tasks/dept")
    public Object getTasksByDeptId(@RequestParam("dept_id") Long deptId){
        try{
            return bizService.getTasksByDeptId(deptId);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 添加任务
     * @param task 任务数据
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/tasks/manage/add")
    public Object addTask(@RequestBody BizTaskDTO task, HttpServletRequest request){
        try{
            bizService.addTask(task);
            return "任务"+task.getTaskName()+"添加成功";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 更新任务
     * @param task 任务数据
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/tasks/manage/update")
    public Object updateTask(@RequestBody BizTaskDTO task, HttpServletRequest request){
        try{
            bizService.updateTask(task);
            return "任务"+task.getTaskName()+"更新成功";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 完成任务
     * @param taskId 任务ID
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/tasks/manage/finish/{taskId}")
    public Object finishTask(@PathVariable("taskId") Long taskId, HttpServletRequest request){
        try{
            return bizService.finishTask(taskId, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 提交审批材料
     * @param bizSubDTO 提交数据
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/submit")
    public Object submitMaterial(@RequestBody BizSubDTO bizSubDTO, HttpServletRequest request){
        try{
            return bizService.submitMaterial(bizSubDTO, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")) );
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取审批单
     * @param taskId 任务ID
     * @param request HTTP请求
     * @return 审批单信息或错误信息
     */
    @GetMapping("/audit/{taskId}")
    public Object getAudit(@PathVariable("taskId") Long taskId,HttpServletRequest request){
        try{
            return bizService.getAudit(taskId, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取"待我审批"的审批单列表（按 current_handler_id 查询）
     * @param request HTTP请求
     * @return 待审批列表或错误信息
     */
    @GetMapping("/audit/todo")
    public Object getTodoAudits(HttpServletRequest request) {
        try {
            return bizService.getTodoAudits(JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取某任务的全部审批单（用于任务详情抽屉展示完整流程）
     * @param taskId 任务ID
     * @param request HTTP请求
     * @return 审批单列表或错误信息
     */
    @GetMapping("/audit/task/{taskId}")
    public Object getAuditByTaskId(@PathVariable("taskId") Long taskId, HttpServletRequest request) {
        try {
            return bizService.getAuditByTaskId(taskId, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 根据任务id获取上次周期上传的文件
     * @param taskId 任务ID
     * @param response HTTP响应
     * @return 文件信息或错误信息
     */
    @GetMapping("/audit/file/{taskId}")
    public Object getLastCycleFiles(@PathVariable("taskId") Long taskId, HttpServletResponse  response) {
        try {
            return bizService.getLastCycleFiles(taskId);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取审批操作日志（biz_audit_log）
     * @param subId 提交ID
     * @param request HTTP请求
     * @return 操作日志或错误信息
     */
    @GetMapping("/audit/logs/{subId}")
    public Object getAuditLogs(@PathVariable("subId") Long subId, HttpServletRequest request) {
        try {
            return bizService.getAuditLogs(subId, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 审批操作
     * @param bizAuditDTO 审批数据
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/audit")
    public Object audit(@RequestBody BizAuditDTO bizAuditDTO, HttpServletRequest request){
        try{
            return bizService.audit(bizAuditDTO, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 撤回提交申请
     * @param taskId 任务ID
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/drawback/{taskId}")
    public Object drawbackSubmit(@PathVariable("taskId") Long taskId, HttpServletRequest request){
        try{
            return bizService.drawbackSubmit(taskId, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 重新提交材料
     * @param bizReSubDTO 重新提交数据
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/resub")
    public Object reSubmitMaterial(@RequestBody BizReSubDTO bizReSubDTO, HttpServletRequest request){
        try{
            return bizService.reSubmitMaterial(bizReSubDTO, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }
}
```

#### java\org\example\controller\ScheduledTaskController.java

```java
package org.example.controller;

import org.example.service.ScheduledTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scheduled")
public class ScheduledTaskController {

    @Autowired
    private ScheduledTaskService scheduledTaskService;

    @PostMapping("/month_leader_trigger")
    public String triggerManualReminder() {
        return scheduledTaskService.triggerMonthDeptLeaderReminder();
    }

    @PostMapping("/month_auditor_trigger")
    public String triggerAuditorMonthReminder() {
        return scheduledTaskService.triggerMonthAuditorReminder();

    }

    @PostMapping("/year_trigger")
    public String triggerYearReminder() {
        return scheduledTaskService.triggerYearlyReminder();
    }

    @PostMapping("/update_task_status")
    public void updateTaskStatus() {
        scheduledTaskService.updateTaskStatus();
    }
}
```

#### java\org\example\controller\SysController.java

```java
package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.entity.SysNotice;
import org.example.entity.SysUser;
import org.example.entity.dto.*;
import org.example.entity.vo.ErrorVO;
import org.example.entity.vo.SysLogoutVO;
import org.example.service.SysService;
import org.example.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 系统控制器：处理用户管理、登录认证、文件上传等系统功能
 */
@RestController
@RequestMapping("/system")
public class SysController {

    @Autowired
    private SysService sysService;

    /**
     * 获取所有用户
     * @return 用户列表或错误信息
     */
    @GetMapping("/allUsers")
    public Object getAllUsers() {
        try{
            return sysService.getAllUsers();
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 添加用户
     * @param user 用户数据
     * @return 操作结果或错误信息
     */
    @PostMapping("/users/add")
    public Object addUser(@RequestBody SysUserDTO  user) {
        try{
            sysService.addUser(user);
            return "用户 "+user.getUserName()+" 添加成功";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 更新用户
     * @param user 用户数据
     * @return 操作结果或错误信息
     */
    @PostMapping("/users/update")
    public Object updateUser(@RequestBody SysUserDTO user) {
        try{
            sysService.updateUser(user);
            return "用户 "+user.getUserName()+" 更新成功";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 删除用户
     * @param userId 用户ID
     * @return 操作结果或错误信息
     */
    @PostMapping("/users/delete/{userId}")
    public Object deleteUser(@PathVariable Long userId) {
        try{
            sysService.deleteUser(userId);
            return "用户 "+userId+" 删除成功";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 根据部门ID获取部门信息（含 leaderId）
     * @param deptId 部门ID
     * @return 部门信息或错误信息
     */
    @GetMapping("/dept/{deptId}")
    public Object getDeptById(@PathVariable("deptId") Long deptId) {
        try {
            return sysService.getDeptById(deptId);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 用户登录
     * @param sysLoginDTO 登录数据
     * @return 登录结果或错误信息
     */
    @PostMapping("/login")
    public Object login(@RequestBody SysLoginDTO sysLoginDTO){
        try{
            return sysService.login(sysLoginDTO.getUser_id(), sysLoginDTO.getPassword());
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 修改密码
     * @param sysPasswordDTO 密码数据
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/password")
    public Object changePassword(@RequestBody SysPwdDTO sysPasswordDTO, HttpServletRequest request){
        try{
            //根据token获取用户ID
            Long userId=JWTUtil.getUserIdFromToken(request.getHeader("Authorization"));
            sysService.changePassword(userId,sysPasswordDTO.getNew_password());
            return new SysPwdDTO("密码修改成功");
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 用户注销
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/logout")
    public Object logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null) {
            try {
                sysService.logout(token);
                return new SysLogoutVO("注销成功");
            } catch (Exception e) {
                return new ErrorVO(e.getMessage(), 500);
            }
        }
        return new ErrorVO("token不存在", 401);
    }

    /**
     * 上传文件
     * @param file 文件对象
     * @param taskId 任务ID
     * @param request HTTP请求
     * @return 文件信息或错误信息
     */
    @PostMapping("/upload/{task_id}")
    public Object uploadFile(@RequestParam("file") MultipartFile file, @PathVariable("task_id") Long taskId, HttpServletRequest request){
        try{
            return sysService.uploadFile(file,taskId,request);
        }catch (Exception e){
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 根据file_id下载文件
     * @param fileId 文件ID
     * @param response HTTP响应
     * @return 文件流或错误信息
     */
    @GetMapping("/download/{file_id}")
    public Object downloadFile(@PathVariable("file_id") Long fileId, HttpServletResponse response){
        try{
            sysService.downloadFile(fileId,response);
            return "下载成功";
        }catch (Exception e){
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 发送通知
     * @param sysNoticeDTO 通知数据
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/notice")
    public Object sendNotice(@RequestBody SysNoticeDTO sysNoticeDTO, HttpServletRequest request){
        try{
            Long userId=JWTUtil.getUserIdFromToken(request.getHeader("Authorization"));
            sysService.sendNotice(sysNoticeDTO,userId);
            return "发送成功";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 查看当前用户收到的通知
     * @param request HTTP请求
     * @return 通知列表或异常
     */
    @GetMapping("/notice")
    public List<SysNotice> getNotices(HttpServletRequest request) {
        try{
            Long userId=JWTUtil.getUserIdFromToken(request.getHeader("Authorization"));
            return sysService.getNotices(userId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 设为已读
     * @param noticeId 通知ID
     * @return 操作结果或错误信息
     */
    @PostMapping("/notice/{notice_id}")
    public Object setRead(@PathVariable("notice_id") Long noticeId){
        try{
            sysService.setRead(noticeId);
            return "已读";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 站内信息预警
     * input token to_user_nick_name title/content source_id
     * @param sysAlertDTO 预警数据
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/alert")
    public Object sendAlert(@RequestBody SysAlertDTO sysAlertDTO, HttpServletRequest request){
        try{
            Long userId=JWTUtil.getUserIdFromToken(request.getHeader("Authorization"));
            sysService.sendAlert(sysAlertDTO,userId);
            return "发送成功";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }


}
```

#### java\org\example\entity\BizAchievement.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

/**
 * 标志性成果表实体类
 * 对应表：biz_achievement
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizAchievement {
    private Long achId; // 成果ID(主键)

    /**
     * 成果类别枚举：
     * 1:落实立德树人根本任务 2:创新产教融合机制 3:打造高水平专业群
     * 4:建设一流核心课程 5:开放优质新形态教材 6:建设高水平双师队伍
     * 7:建设产教融合实训基地 8:构建数字化教学新生态 9:拓展国际交流与合作
     * 10:打造一流区域型高端智库
     */
    private Integer category; // 类别

    /**
     * 成果级别：国家级/省级/市级
     */
    private String level; // 级别
    private String achName; // 成果名称
    private String department; // 组织部门(如：教育部办公厅等，区别于校内部门)
    private Date gotTime; // 颁发时间
    private Long deptId; // 部门ID
    private Long fileId; // 佐证材料文件ID
    private String comment; // 备注

    /**
     * 是否竞赛：0:不是竞赛 1:是竞赛
     */
    private Integer isCompetition; // 是否竞赛

    private Integer teDengJiang; // 特等奖数量
    private Integer yiDengJiang; // 一等奖数量
    private Integer erDengJiang; // 二等奖数量
    private Integer sanDengJiang; // 三等奖数量
    private Integer jinJiang; // 金奖数量
    private Integer yinJiang; // 银奖数量
    private Integer tongJiang; // 铜奖数量
    private Integer youShengJiang; // 优胜奖数量
    private Integer budDengDengCi; // 不定等次数量

    private Long createBy; // 创建人ID(关联sys_user表userId)
    private Integer isDelete; // 0:存在 1:删除
    private Date createTime; // 创建时间(默认当前时间)
}
```

#### java\org\example\entity\BizAuditLog.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizAuditLog {
    private Long logId; // 日志ID
    private Long subId; // 提交ID
    private Long operatorId; // 操作人ID
    private String actionType; // 动作
    /**
     * 流程状态参考 BizMaterialSubmission 的 flowStatus 枚举
     */
    private Integer preStatus; // 前状态
    private Integer postStatus; // 后状态
    private String comment; // 意见
    private Date createTime; // 创建时间
}
```

#### java\org\example\entity\BizMaterialSubmission.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizMaterialSubmission {
    private Long subId; // 提交ID
    private Long taskId; // 任务ID
    private Long fileId; // 文件ID

    // 填报数据相关
    private BigDecimal reportedValue; // 本次填报完成值
    private String dataType; // 数据类型 0:无影响 1:数值累加 2:百分比取大
    private Long submitBy; // 提交人ID
    private Long submitDeptId; // 提交人部门ID
    private Long manageDeptId; // 归口部门ID
    private Date submitTime; // 提交时间
    private String fileSuffix; // 后缀（仅允许pdf/doc/docx）

    /**
     * 流程状态枚举：
     * 0:草稿
     * 10:待[所在部门]审批 20:待[归口部门]审批 30:待[管理员]审批
     * 40:已归档
     * -10:被所在部门退回 -20:被归口部门退回 -30:被管理员退回
     */
    private Integer flowStatus;
    private Long currentHandlerId; // 当前处理人ID
    private Integer isDelete; // 0:存在 1:删除
}
```

#### java\org\example\entity\BizPerformance.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizPerformance {
    private Long perfId; // 指标ID
    private Long projectId; // 项目ID
    private Long parentId; // 父指标ID
    private String ancestors; // 祖先指标ID集合
    private String perfCode; // 编码
    private String perfName; // 名称

    private BigDecimal targetValue; // 总目标值
    private BigDecimal currentValue; // 当前完成值(计算得出)
    private String dataType; // 数据类型 0:无影响 1:数值累加 2:百分比取大

    private Long deptId; // 归口部门ID
    private Long principalId; // 归口负责人ID
    private Long leaderId; // 任务负责人ID

    private Integer isDelete; // 0:存在 1:删除
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间
}
```

#### java\org\example\entity\BizPerformanceYear.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizPerformanceYear {
    private Long yearId; // 年度ID
    private Long perfId; // 指标ID
    private Integer year; // 年份
    private BigDecimal targetValue; // 年度目标值
    private BigDecimal actualValue; // 年度实际完成
    private String dataType; // 数据类型 0:无影响 1:数值累加 2:百分比取大
    private Integer isDelete; // 0:存在 1:删除
}
```

#### java\org\example\entity\BizProject.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizProject {
    private Long projectId; // 项目ID
    private String projectName; // 项目名称
    private String projectCode; // 项目编码
    private Long leaderId; // 项目负责人ID
    private Date startDate; // 开始日期
    private Date endDate; // 结束日期
    /**
     * 项目状态枚举：
     * 0:未开始 1:进行中 2:已完成
     * 3:暂停 4:逾期
     */
    private String status;
    private Integer isDelete; // 0:存在 1:删除
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间
}
```

#### java\org\example\entity\BizProjectPhase.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizProjectPhase {
    private Long phaseId; // 阶段ID
    private Long projectId; // 项目ID
    private String phaseName; // 阶段名称
    private Date startDate; // 开始日期
    private Date endDate; // 结束日期
    private String isCurrent; // 是否当前阶段 0:否 1:是
    private Integer isDelete; // 0:存在 1:删除
}
```

#### java\org\example\entity\BizTask.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizTask {
    private Long taskId; // 任务ID
    private Long projectId; // 项目ID
    private Long parentId; // 父任务节点ID
    private String ancestors; // 祖先节点ID集合
    private Integer phase; // 所属年份
    private String taskCode; // 任务编号
    private String taskName; // 任务名称
    private Integer level; // 任务层级
    private String comment;// 任务描述

    // 组织归属相关
    private Long deptId; // 归口部门ID
    private Long auditorId;//审核人ID
    private Long principalId; // 归口负责人ID
    private Long leaderId; // 任务负责人ID

    // 数据需求配置相关
    private String expTarget; // 预期达成情况
    private String expLevel; // 预期成果级别（国/省/市）
    private String expEffect; // 预期效果
    private String expMaterialDesc; // 预期过程（佐证）材料清单(文本描述)
    /**
     * 数据类型枚举：
     * 0:对指标没有影响
     * 1:数值(累加)
     * 2:百分比(取大)
     */
    private String dataType;
    private BigDecimal targetValue; // 目标值
    private BigDecimal currentValue; // 当前完成值(缓存统计)
    private BigDecimal weight; // 权重(冗余)
    private Integer progress; // 任务进度(冗余)
    /**
     * 任务状态枚举：
     * 0:未开始 1:进行中
     * 2:审核中 3:已完成
     */
    private String status;
    private Integer isDelete; // 0:存在 1:删除
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间
}
```

#### java\org\example\entity\dto\BizAuditDTO.java

```java
package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizAuditDTO {
    private Long sub_id;
    private Boolean is_pass;
    private String title;
    private String content;
//    private String file_id;
}
```

#### java\org\example\entity\dto\BizReSubDTO.java

```java
package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizReSubDTO {
    private Long sub_id;
    private BigDecimal reported_value;
    private String data_type;
    private Long file_id;
}
```

#### java\org\example\entity\dto\BizSubDTO.java

```java
package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
//reported_value(可空 value_type) task_id
public class BizSubDTO {
    private Long task_id;
    private BigDecimal reported_value;
    private String data_type;
    private Long file_id;
    private String comment;
}
```

#### java\org\example\entity\dto\BizTaskDTO.java

```java
package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizTaskDTO {
    private Long taskId; // 任务ID
    private Long projectId; // 项目ID
    private Long parentId; // 父任务节点ID
    private String ancestors; // 祖先节点ID集合
    private Integer phase; // 所属年份
    private String taskCode; // 任务编号
    private String taskName; // 任务名称
    private Integer level; // 任务层级
    private String comment;// 任务描述

    // 组织归属相关
    private Long deptId; // 归口部门ID
    private Long auditorId;//审核人ID
    private Long principalId; // 归口负责人ID
    private Long leaderId; // 任务负责人ID

    // 数据需求配置相关
    private String expTarget; // 预期达成情况
    private String expLevel; // 预期成果级别（国/省/市）
    private String expEffect; // 预期效果
    private String expMaterialDesc; // 预期过程（佐证）材料清单(文本描述)
    /**
     * 数据类型枚举：
     * 0:对指标没有影响
     * 1:数值(累加)
     * 2:百分比(取大)
     */
    private String dataType;
    private BigDecimal targetValue; // 目标值
    private BigDecimal currentValue; // 当前完成值(缓存统计)
    private BigDecimal weight; // 权重(冗余)
    private Integer progress; // 任务进度(冗余)
    /**
     * 任务状态枚举：
     * 0:未开始 1:进行中
     * 2:审核中 3:已完成
     */
    private String status;

}
```

#### java\org\example\entity\dto\FileUploadDTO.java

```java
package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//上传文件 post
//input token reported_value(可空 value_type) task_id文件
//新生成file数据
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadDTO {
    private String filename;
    private String filepath;
}
```

#### java\org\example\entity\dto\SysAlertDTO.java

```java
package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
//站内信息 预警
//input token to_user_nick_name title/content source_id
public class SysAlertDTO {
    private String to_user_nick_name;
    private String title;
    private String content;
    private Long source_id;
}
```

#### java\org\example\entity\dto\SysLoginDTO.java

```java
package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysLoginDTO {
    private Long user_id;
    private String password;
}
```

#### java\org\example\entity\dto\SysNoticeDTO.java

```java
package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysNoticeDTO {
    private Long to_user_id;
    private String type;
    private String trigger_event;
    private String title;
    private String content;
    private String source_type;
    private Long source_id;
    private String is_read;
}
```

#### java\org\example\entity\dto\SysPwdDTO.java

```java
package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysPwdDTO {
    private String new_password;
}
```

#### java\org\example\entity\dto\SysUserDTO.java

```java
package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysUserDTO {
    private Long userId; // 用户ID
    private Long deptId; // 所属部门ID
    private String userName; // 账号
    private String nickName; // 姓名
    private String email; // 邮箱
    private String password; // 密码
    private String role; // 角色 0:admin 1:user 2:leader
    private String status; // 状态 0:正常 1:停用
    private Integer isDelete; // 0:存在 1:删除
}
```

#### java\org\example\entity\RelTaskPerformance.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RelTaskPerformance {
    private Long id; // 关联ID
    private Long taskId; // 任务ID
    private Long perfId; // 指标ID
    private Long yearId; // 绩效年度ID
    private BigDecimal weight; // 权重
    private BigDecimal contributionValue; // 该任务为KPI贡献的数值
    private String dataType; // 数据类型 0:无影响 1:数值累加 2:百分比取大
}
```

#### java\org\example\entity\SysDept.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysDept {
    private Long deptId; // 部门ID
    private String deptName; // 部门名称
    private Long leaderId; // 部门负责人ID
    private String status; // 状态 0:正常 1:停用
    private Integer isDelete; // 0:存在 1:删除
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间
}
```

#### java\org\example\entity\SysFile.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysFile {
    private Long fileId; // 文件ID
    private String fileName; // 文件名
    private String filePath; // 路径
    private String fileUrl; // URL
    private String fileSuffix; // 后缀
    private Long fileSize; // 大小
    private Long uploadBy; // 上传人ID
    private Integer isDelete; // 0:存在 1:删除
    private Date uploadTime; // 上传时间
}
```

#### java\org\example\entity\SysNotice.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysNotice {
    private Long noticeId; // 通知ID
    private Long fromUserId; // 发起人ID
    private Long toUserId; // 接收人ID
    private String type; // 类型
    private String triggerEvent; // 触发事件
    private String title; // 标题
    private String content; // 内容
    private String sourceType; // 关联来源类型
    private Long sourceId; // 关联业务ID
    private String isRead; // 阅读状态 0:未读 1:已读
    private Integer isDelete; // 0:存在 1:删除
    private Date createTime; // 创建时间
}
```

#### java\org\example\entity\SysUser.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysUser {
    private Long userId; // 用户ID
    private Long deptId; // 所属部门ID
    private String userName; // 账号
    private String nickName; // 姓名
    private String email; // 邮箱
    private String password; // 密码
    private String role; // 角色 0:admin 1:user 2:leader
    private String status; // 状态 0:正常 1:停用
    private Integer isDelete; // 0:存在 1:删除
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间
}
```

#### java\org\example\entity\TokenBlacklist.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenBlacklist {
    private String token; // 失效token
    private Date expiryTime; // 过期时间
}
```

#### java\org\example\entity\vo\BizAuditVO.java

```java
package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizAuditVO {
    private Long subId; // 提交ID
    private Long taskId; // 任务ID
    private Long fileId; // 文件ID
    private String filename; // 文件名

    // 填报数据相关
    private BigDecimal reportedValue; // 本次填报完成值
    private String dataType; // 数据类型 0:无影响 1:数值累加 2:百分比取大
    private Long submitBy; // 提交人ID
    private Long submitDeptId; // 提交人部门ID
    private Long manageDeptId; // 归口部门ID
    private Date submitTime; // 提交时间
    private String fileSuffix; // 后缀（仅允许pdf/doc/docx）

    /**
     * 流程状态枚举：
     * 0:草稿
     * 10:待[所在部门]审批 20:待[归口部门]审批 30:待[管理员]审批
     * 40:已归档
     * -10:被所在部门退回 -20:被归口部门退回 -30:被管理员退回
     */
    private Integer flowStatus;
    private Long currentHandlerId; // 当前处理人ID
    private Integer isDelete; // 0:存在 1:删除


}
```

#### java\org\example\entity\vo\BizTaskVo.java

```java
package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizTaskVo {
    private Long taskId; // 任务ID
    private Long projectId; // 项目ID
    private Long parentId; // 父任务节点ID
    private String ancestors; // 祖先节点ID集合
    private Integer phase; // 所属年份
    private String taskCode; // 任务编号
    private String taskName; // 任务名称
    private Integer level; // 任务层级
    private String comment;// 任务描述

    // 组织归属相关
    private Long deptId; // 归口部门ID
    private String deptName;
    private Long principalId; // 归口负责人ID
    private String principalName;
    private Long auditorId;//审核人ID
    private String auditorName;
    private Long leaderId; // 任务负责人ID
    private String leaderName;

    // 数据需求配置相关
    private String expTarget; // 预期达成情况
    private String expLevel; // 预期成果级别（国/省/市）
    private String expEffect; // 预期效果
    private String expMaterialDesc; // 预期过程（佐证）材料清单(文本描述)
    /**
     * 数据类型枚举：
     * 0:对指标没有影响
     * 1:数值(累加)
     * 2:百分比(取大)
     */
    private String dataType;
    private BigDecimal targetValue; // 目标值
    private BigDecimal currentValue; // 当前完成值(缓存统计)
    private BigDecimal weight; // 权重(冗余)
    private Integer progress; // 任务进度(冗余)
    /**
     * 任务状态枚举：
     * 0:未开始 1:进行中
     * 2:审核中 3:已完成
     */
    private String status;
    private Integer isDelete; // 0:存在 1:删除
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间
}
```

#### java\org\example\entity\vo\ErrorVO.java

```java
package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorVO {
    private String message;
    private Integer code;
}
```

#### java\org\example\entity\vo\FileUploadVO.java

```java
package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadVO {
    private Long fileId;
    private String filename;
    private String filepath;
}
```

#### java\org\example\entity\vo\SysLoginVO.java

```java
package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysLoginVO {

    private String nick_name;
    private String token;
}
```

#### java\org\example\entity\vo\SysLogoutVO.java

```java
package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysLogoutVO {
    private String message;
}
```

#### java\org\example\entity\vo\SysPasswordVO.java

```java
package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysPasswordVO {
    private String message;
}
```

#### java\org\example\mapper\AchievementMapper.java

```java
package org.example.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Options;
import org.example.entity.BizAchievement;

import java.util.List;

/**
 * 标志性成果数据访问接口
 * 对应表：biz_achievement
 */
@Mapper
public interface AchievementMapper {

    /**
     * 根据成果ID查询单条成果信息
     * @param achId 成果ID
     * @return 标志性成果对象
     */
    @Select("SELECT * FROM biz_achievement WHERE ach_id = #{achId} AND is_delete = 0")
    BizAchievement getAchievementById(Long achId);

    /**
     * 查询所有未删除的标志性成果列表
     * @return 标志性成果列表
     */
    @Select("SELECT * FROM biz_achievement WHERE is_delete = 0")
    List<BizAchievement> listAllAchievements();


    /**
     * 查询最新的一个标志性成果
     * @return 最新的标志性成果对象
     */
    @Select("SELECT * FROM biz_achievement WHERE is_delete = 0 ORDER BY create_time DESC LIMIT 1")
    BizAchievement getLatestAchievement();

    /**
     * 新增标志性成果（自增主键返回）
     * @param achievement 标志性成果实体
     * @return 自增的成果ID
     */
    @Insert("INSERT INTO biz_achievement (category, level, ach_name, department, got_time, dept_id, file_id, " +
            "comment, is_competition, te_deng_jiang, yi_deng_jiang, er_deng_jiang, san_deng_jiang, jin_jiang, " +
            "yin_jiang, tong_jiang, you_sheng_jiang, bud_deng_deng_ci, create_by, is_delete, create_time) " +
            "VALUES (#{category}, #{level}, #{achName}, #{department}, #{gotTime}, #{deptId}, #{fileId}, " +
            "#{comment}, #{isCompetition}, #{teDengJiang}, #{yiDengJiang}, #{erDengJiang}, #{sanDengJiang}, #{jinJiang}, " +
            "#{yinJiang}, #{tongJiang}, #{youShengJiang}, #{budDengDengCi}, #{createBy}, #{isDelete}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "achId", keyColumn = "ach_id")
    void addAchievement(BizAchievement achievement);

    /**
     * 根据成果ID修改标志性成果信息
     * @param achievement 标志性成果实体（含需修改的成果ID）
     */
    @Update("UPDATE biz_achievement SET category = #{category}, level = #{level}, ach_name = #{achName}, " +
            "department = #{department}, got_time = #{gotTime}, dept_id = #{deptId}, file_id = #{fileId}, " +
            "comment = #{comment}, is_competition = #{isCompetition}, te_deng_jiang = #{teDengJiang}, " +
            "yi_deng_jiang = #{yiDengJiang}, er_deng_jiang = #{erDengJiang}, san_deng_jiang = #{sanDengJiang}, " +
            "jin_jiang = #{jinJiang}, yin_jiang = #{yinJiang}, tong_jiang = #{tongJiang}, you_sheng_jiang = #{youShengJiang}, " +
            "bud_deng_deng_ci = #{budDengDengCi}, create_by = #{createBy} " +
            "WHERE ach_id = #{achId} AND is_delete = 0")
    void updateAchievement(BizAchievement achievement);

    /**
     * 逻辑删除标志性成果（置is_delete=1）
     * @param achId 成果ID
     */
    @Update("UPDATE biz_achievement SET is_delete = 1 WHERE ach_id = #{achId}")
    void deleteAchievement(Long achId);
}
```

#### java\org\example\mapper\BizMapper.java

```java
package org.example.mapper;

import org.apache.ibatis.annotations.*;
import org.example.entity.BizAuditLog;
import org.example.entity.BizMaterialSubmission;
import org.example.entity.BizTask;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 业务数据访问接口
 */
@Mapper
public interface BizMapper {

    /**
     * 任务相关操作
     */

    /**
     * 获取全部任务
     * @return 任务列表
     */
    @Select("SELECT * FROM biz_task")
    List<BizTask> getAllTasks();

    /**
     * 根据id获取任务
     * @param taskId 任务ID
     * @return 任务对象
     */
    @Select("SELECT * FROM biz_task WHERE task_id = #{taskId}")
    BizTask getTaskById(Long taskId);

    /**
     * 根据部门id获取任务
     * @param deptId 部门ID
     * @return 任务列表
     */
    @Select("SELECT * FROM biz_task WHERE dept_id = #{deptId}")
    List<BizTask> getTasksByDeptId(Long deptId);

//    getTasksByDeptIdAndPhase
    @Select("SELECT * FROM biz_task WHERE dept_id = #{deptId} AND phase = #{phase}")
    List<BizTask> getTasksByDeptIdAndPhase(Long deptId, Integer phase);



    /**
     * 根据归口负责人获取任务
     * @param principalId 负责人ID
     * @return 任务列表
     */
    @Select("SELECT * FROM biz_task WHERE principal_id = #{principalId}")
    List<BizTask> getTasksByPrincipalId(Long principalId);

    /**
     * 根据负责人ID获取任务
     * @param leaderId 负责人ID
     * @return 任务列表
     */
    @Select("SELECT * FROM biz_task WHERE leader_id = #{leaderId}")
    List<BizTask> getTasksByLeaderId(Long leaderId);

    /**
     * 根据负责人ID或归口负责人ID或审核人ID获取任务
     * @param userId 用户ID
     * @return 任务列表
     */
    @Select("SELECT * FROM biz_task WHERE leader_id = #{userId} OR principal_id = #{userId} OR auditor_id=#{userId}")
    List<BizTask> getTasks(Long userId);

    /**
     * 获取所有一级任务
     * @return 一级任务列表
     */
    @Select("SELECT * FROM biz_task WHERE level=1")
    List<BizTask> getAllFirstLevelTasks();

    /**
     * 获取当前任务的所有子任务
     * @param taskId 任务ID
     * @return 子任务列表
     */
    @Select("SELECT * FROM biz_task WHERE parent_id = #{taskId}")
    List<BizTask> getAllChildrenTasks(Long taskId);

    /**
     * 根据一级任务id获取其二级子任务
     * @param parentId 父任务ID
     * @return 二级任务列表
     */
    @Select("SELECT * FROM biz_task WHERE parent_id = #{parentId} AND level=2")
    List<BizTask> getSecondLevelTasksByParentId(Long parentId);

    /**
     * 根据二级任务id获取其三级子任务
     * @param parentId 父任务ID
     * @return 三级任务列表
     */
    @Select("SELECT * FROM biz_task WHERE parent_id = #{parentId} AND level=3")
    List<BizTask> getThirdLevelTasksByParentId(Long parentId);


    /**
     * 根据任务阶段获取任务
     * @param phase 任务阶段
     * @return 任务列表
     */
    @Select("SELECT * FROM biz_task WHERE phase = #{phase}")
    List<BizTask> getTasksByPhase(Integer phase);

    /**
     * 新增任务
     * @param task 任务实体（taskId会自增，无需传入）
     * @return 影响的行数（1表示新增成功）
     */
    @Insert("INSERT INTO biz_task (" +
            "project_id, parent_id, ancestors, phase, task_code, task_name, level, comment, " +
            "leader_id, auditor_id, principal_id, dept_id, exp_target, exp_level, exp_effect, " +
            "exp_material_desc, data_type, target_value, current_value, weight, progress, " +
            "status, is_delete, create_time, update_time" +
            ") VALUES (" +
            "#{projectId}, #{parentId}, #{ancestors}, #{phase}, #{taskCode}, #{taskName}, #{level}, #{comment}, " +
            "#{leaderId}, #{auditorId}, #{principalId}, #{deptId}, #{expTarget}, #{expLevel}, #{expEffect}, " +
            "#{expMaterialDesc}, #{dataType}, #{targetValue}, #{currentValue}, #{weight}, #{progress}, " +
            "#{status}, #{isDelete}, #{createTime}, #{updateTime}" +
            ")")
    // 新增注解：返回自增的taskId到实体对象中
    @Options(useGeneratedKeys = true, keyProperty = "taskId", keyColumn = "task_id")
    void addTask(BizTask task);

    /**
     * 更新任务
     * 全字段更新任务（根据taskId更新所有字段）
     * @param task 任务实体
     * @return 影响的行数
     */
    @Update("UPDATE biz_task SET project_id=#{projectId},parent_id=#{parentId},ancestors=#{ancestors},phase=#{phase},task_code=#{taskCode},task_name=#{taskName},level=#{level},comment=#{comment},leader_id=#{leaderId},auditor_id=#{auditorId},principal_id=#{principalId},dept_id=#{deptId},exp_target=#{expTarget},exp_level=#{expLevel},exp_effect=#{expEffect},exp_material_desc=#{expMaterialDesc},data_type=#{dataType},target_value=#{targetValue},current_value=#{currentValue},weight=#{weight},progress=#{progress},status=#{status},is_delete=#{isDelete},create_time=#{createTime},update_time=NOW() WHERE task_id=#{taskId}")
    int updateTask(BizTask task);

    /**
     * 提交后更新任务
     * @param task 任务实体
     */
    @Update("UPDATE biz_task SET current_value = #{currentValue}, status = #{status}, update_time = NOW() WHERE task_id = #{taskId}")
    void updateCurrentTask(BizTask task);

    /**
     * 根据任务id获取部门leaderid
     * @param taskId 任务ID
     * @return 负责人ID
     */
    @Select("SELECT leader_id FROM sys_dept WHERE dept_id = (SELECT dept_id FROM biz_task WHERE task_id = #{taskId})")
    Long getTaskLeaderId(Long taskId);

    /**
     * 根据任务id获取归口负责人Id
     * @param taskId 任务ID
     * @return 归口负责人ID
     */
    @Select("SELECT principal_id FROM biz_task WHERE task_id = #{taskId}")
    Long getTaskPrincipalId(Long taskId);

    /**
     * 日志相关操作
     */

    /**
     * 创建审批单日志
     * @param auditLog 审批日志实体
     */
    @Insert("insert into biz_audit_log(sub_id,operator_id,action_type,pre_status,post_status,comment,create_time) values(#{subId},#{operatorId},#{actionType},#{preStatus},#{postStatus},#{comment},#{createTime})")
    void createAuditLog(BizAuditLog auditLog);

    /**
     * 更新日志
     * @param auditLog 审批日志实体
     */
    @Update("update biz_audit_log set sub_id=#{subId},operator_id=#{operatorId},action_type=#{actionType},pre_status=#{preStatus},post_status=#{postStatus},comment=#{comment},create_time=#{createTime} where log_id=#{logId}")
    void updateAuditLog(BizAuditLog auditLog);

    /**
     * 根据 subId 获取审批操作日志（biz_audit_log）
     * @param subId 提交ID
     * @return 审批日志列表
     */
    @Select("SELECT " +
            "log_id AS logId, " +
            "sub_id AS subId, " +
            "operator_id AS operatorId, " +
            "action_type AS actionType, " +
            "pre_status AS preStatus, " +
            "post_status AS postStatus, " +
            "comment, " +
            "create_time AS createTime " +
            "FROM biz_audit_log WHERE sub_id = #{subId} " +
            "ORDER BY create_time DESC, log_id DESC")
    List<BizAuditLog> getAuditLogsBySubId(@Param("subId") Long subId);

    /**
     * 材料提交相关操作
     */

    /**
     * 创建审批流程
     * @param bizMaterialSubmission 材料提交实体
     * @return 提交ID
     */
    @Insert("insert into biz_material_submission(" +
            "task_id, file_id, reported_value, data_type, submit_by, submit_dept_id, " +
            "manage_dept_id, submit_time, file_suffix, flow_status, current_handler_id, is_delete" +
            ") values(" +
            "#{taskId}, #{fileId}, #{reportedValue}, #{dataType}, #{submitBy}, #{submitDeptId}, " +
            "#{manageDeptId}, #{submitTime}, #{fileSuffix}, #{flowStatus}, #{currentHandlerId}, #{isDelete}" +
            ")")
    void createAudit(BizMaterialSubmission bizMaterialSubmission);

    /**
     * 获取最新的审批单id
     * @return 最新的提交ID
     */
    @Select("SELECT sub_id FROM biz_material_submission ORDER BY sub_id DESC LIMIT 1")
    Long getNewestSubId();

    /**
     * 更新审批单 【核心修复点1：注解改为@Update；核心修复点2：where前加空格】
     * @param bizMaterialSubmission 材料提交实体
     */
    @Update("update biz_material_submission set task_id=#{taskId},file_id=#{fileId},reported_value=#{reportedValue},data_type=#{dataType},"
            + "submit_by=#{submitBy},submit_dept_id=#{submitDeptId},manage_dept_id=#{manageDeptId},submit_time=#{submitTime},file_suffix=#{fileSuffix},"
            + "flow_status=#{flowStatus},current_handler_id=#{currentHandlerId},is_delete=#{isDelete} " + // 此处添加空格
            "where sub_id=#{subId}")
    void updateAudit(BizMaterialSubmission bizMaterialSubmission);

    /**
     * 更新任务状态 【优化：参数名统一为驼峰风格，与Java规范一致】
     * @param taskId 任务ID
     * @param status 状态
     */
    @Update("UPDATE biz_task SET status = #{status} WHERE task_id = #{taskId}")
    void updateTaskStatus(@Param("taskId") Long taskId, @Param("status") String status);

    /**
     * 更新审批单flow_status 【优化：参数名统一为驼峰风格】
     * @param subId 提交ID
     * @param flowStatus 流程状态
     */
    @Update("UPDATE biz_material_submission SET flow_status = #{flowStatus} WHERE sub_id = #{subId}")
    void updateAuditFlowStatus(@Param("subId") Long subId, @Param("flowStatus") Integer flowStatus);

    /**
     * 根据taskId及current_handler_id获取审批单
     * @param taskId 任务ID
     * @param currentHandlerId 当前处理人ID
     * @return 材料提交列表
     */
    @Select("SELECT * FROM biz_material_submission WHERE task_id = #{taskId} AND current_handler_id = #{currentHandlerId} AND is_delete = 0")
    List<BizMaterialSubmission> getAudit(@Param("taskId") Long taskId,
                                         @Param("currentHandlerId") Long currentHandlerId);

    /**
     * 根据任务id获取最新的审批单
     * @param taskId 任务ID
     * @return 材料提交对象
     */
    @Select("SELECT * FROM biz_material_submission WHERE task_id = #{taskId} AND is_delete = 0 ORDER BY sub_id DESC LIMIT 1")
    BizMaterialSubmission getNewestAudit(@Param("taskId") Long taskId);

    /**
     * 获取"待我审批"的审批单（按当前处理人查询）
     * @param userId 用户ID
     * @return 待审批列表
     */
    @Select("SELECT * FROM biz_material_submission " +
            "WHERE current_handler_id = #{userId} AND is_delete = 0 AND flow_status >= 10 AND flow_status < 40 AND is_delete = 0" +
            "ORDER BY submit_time DESC, sub_id DESC")
    List<BizMaterialSubmission> getTodoAudits(@Param("userId") Long userId);

    /**
     * 按 task_id 获取该任务全部审批单（用于任务详情抽屉展示完整流程）
     * @param taskId 任务ID
     * @return 审批单列表
     */
    @Select("SELECT * FROM biz_material_submission WHERE task_id = #{taskId} AND is_delete = 0 ORDER BY sub_id DESC")
    List<BizMaterialSubmission> getAuditsByTaskId(@Param("taskId") Long taskId);

    /**
     * 根据subId获取审批单
     * @param subId 提交ID
     * @return 材料提交对象
     */
    @Select("SELECT * FROM biz_material_submission WHERE sub_id = #{subId} AND is_delete = 0")
    BizMaterialSubmission getAuditBySubId(Long subId);

    /**
     * 根据subId获取提交人
     * @param subId 提交ID
     * @return 提交人ID
     */
    @Select("SELECT submit_by FROM biz_material_submission WHERE sub_id = #{subId} AND is_delete = 0")
    Long getAuditSubmitBy(Long subId);

    /**
     * 根据任务id获取最近的审批单
     * @param taskId 任务ID
     * @return 材料提交对象
     */
    @Select("SELECT * FROM biz_material_submission WHERE task_id = #{taskId} AND is_delete = 0 ORDER BY sub_id DESC LIMIT 1")
    BizMaterialSubmission getLatestAuditByTaskId(Long taskId);

    /**
     * 根据任务id获取最近的、且状态为40的审批单
     * @param taskId 任务ID
     * @return 材料提交对象
     */
    @Select("SELECT * FROM biz_material_submission WHERE task_id = #{taskId} AND flow_status = 40 AND is_delete = 0 ORDER BY sub_id DESC LIMIT 1")
    BizMaterialSubmission getLatestApprovedAuditByTaskId(Long taskId);

    /**
     * 获取所有待审核的审批单（flow_status在10-30之间）
     * @return 待审核审批单列表
     */
    @Select("SELECT * FROM biz_material_submission " +
            "WHERE flow_status >= 10 AND flow_status < 40 AND is_delete = 0 " +
            "ORDER BY submit_time DESC")
    List<BizMaterialSubmission> getAllPendingAudits();

    /**
     * 根据处理人ID统计待审核任务数量
     * @param handlerId 处理人ID
     * @return 待审核任务数量
     */
    @Select("SELECT COUNT(*) FROM biz_material_submission " +
            "WHERE current_handler_id = #{handlerId} " +
            "AND flow_status >= 10 AND flow_status < 40 AND is_delete = 0")
    Integer countPendingAuditsByHandler(@Param("handlerId") Long handlerId);
}
```

#### java\org\example\mapper\SysMapper.java

```java
package org.example.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Options;
import org.example.entity.*;

import java.util.List;

/**
 * 系统数据访问接口
 */
@Mapper
public interface SysMapper {
    /**
     * 获取所有用户
     * @return 用户列表
     */
    @Select("SELECT * FROM sys_user")
    public List<SysUser> getAllUsers();


    /**
     * 获取所有用户ID
     * @return 用户ID列表
     */
    @Select("SELECT user_id FROM sys_user")
    public List<Long> getAllUserIds();

    /**
     * 根据id获取部门
     * @param deptId 部门ID
     * @return 部门对象
     */
    @Select("SELECT * FROM sys_dept WHERE dept_id = #{deptId}")
    public SysDept getDeptById(Long deptId);

    /**
     * 根据userId获取部门
     * @param userId 用户ID
     * @return 部门对象
     */
    @Select("SELECT * FROM sys_dept WHERE dept_id = (SELECT dept_id FROM sys_user WHERE user_id = #{userId})")
    public SysDept getDeptByUserId(Long userId);

    /**
     * 根据userId获取部门负责人id
     * @param userId 用户ID
     * @return 负责人ID
     */
    @Select("SELECT leader_id FROM sys_dept WHERE dept_id = (SELECT dept_id FROM sys_user WHERE user_id = #{userId})")
    public Long getDeptLeaderId(Long userId);

    /**
     * 根据部门ID获取部门名称
     * @param deptId 部门ID
     * @return 部门名称
     */
    @Select("SELECT dept_name FROM sys_dept WHERE dept_id = #{deptId}")
    public String getDeptNameByDeptId(Long deptId);


//    获取所有的部门负责人ID
    @Select("SELECT leader_id FROM sys_dept")
    public List<Long> getAllDeptLeaders();

    /**
     * 根据id获取用户
     * @param userId 用户ID
     * @return 用户对象
     */
    @Select("SELECT * FROM sys_user WHERE user_id = #{userId}")
    public SysUser getUserById(Long userId);

    /**
     * 根据用户名获取用户
     * @param userName 用户名
     * @return 用户对象
     */
    @Select("SELECT * FROM sys_user WHERE user_name = #{userName}")
    public SysUser getUserByName(String userName);

    /**
     * 根据昵称获取用户
     * @param nickName 昵称
     * @return 用户对象
     */
    @Select("SELECT * FROM sys_user WHERE nick_name = #{nickName}")
    public SysUser getUserByNickName(String nickName);

    /**
     * 添加用户
     * userId手动添加而非自增
     * @param user 用户实体
     * @return 用户ID
     */
    @Insert("INSERT INTO sys_user (user_id, dept_id, user_name, nick_name, email, password, role, status, is_delete, create_time, update_time) VALUES (#{userId}, #{deptId}, #{userName}, #{nickName}, #{email}, #{password}, #{role}, #{status}, #{isDelete}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "userId", keyColumn = "user_id")
    public void addUser(SysUser user);

    /**
     * 修改用户信息
     * @param user 用户实体
     */
    @Update("UPDATE sys_user SET dept_id = #{deptId}, user_name = #{userName}, nick_name = #{nickName}, email = #{email}, password = #{password}, role = #{role}, status = #{status}, update_time = #{updateTime} WHERE user_id = #{userId}")
    public void updateUser(SysUser user);

    /**
     * 删除用户
     * @param userId 用户ID
     */
    @Update("UPDATE sys_user SET is_delete = 1 WHERE user_id = #{userId}")
    public void deleteUser(Long userId);

    /**
     * 上传文件
     * @param file 文件实体
     * @return 文件ID
     */
    @Insert("INSERT INTO sys_file (file_name, file_path, file_url, file_suffix, file_size, upload_by, is_delete, upload_time) VALUES (#{fileName}, #{filePath}, #{fileUrl}, #{fileSuffix}, #{fileSize}, #{uploadBy}, #{isDelete}, #{uploadTime})")
    @Options(useGeneratedKeys = true, keyProperty = "fileId", keyColumn = "file_id")
    public void uploadFile(SysFile file);

    /**
     * 根据名称查询文件
     * @param fileName 文件名
     * @return 文件对象
     */
    @Select("SELECT * FROM sys_file WHERE file_name = #{fileName}")
    public SysFile getFileByName(String fileName);

    /**
     * 根据ID查询文件
     * @param fileId 文件ID
     * @return 文件对象
     */
    @Select("SELECT * FROM sys_file WHERE file_id = #{fileId}")
    public SysFile getFileById(Long fileId);

    /**
     * 发送通知（带返回通知ID）
     * @param notice 通知实体
     * @return 通知ID
     */
    @Insert("INSERT INTO sys_notice (from_user_id, to_user_id, type, trigger_event, title, content, source_type, source_id, is_read, is_delete, create_time) " +
            "VALUES (#{fromUserId}, #{toUserId}, #{type}, #{triggerEvent}, #{title}, #{content}, #{sourceType}, #{sourceId}, #{isRead}, #{isDelete}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "noticeId", keyColumn = "notice_id")
    void sendNotice(SysNotice notice);
    /**
     * 查看当前用户收到的信息
     * @param userId 用户ID
     * @return 通知列表
     */
    @Select("SELECT * FROM sys_notice WHERE to_user_id = #{userId}")
    public List<SysNotice> getNotices(Long userId);

    /**
     * 根据ID获取通知
     * @param noticeId 通知ID
     * @return 通知对象
     */
    @Select("SELECT * FROM sys_notice WHERE notice_id = #{noticeId}")
    public SysNotice getNoticeById(Long noticeId);

    /**
     * 设为已读
     * @param noticeId 通知ID
     */
    @Update("UPDATE sys_notice SET is_read = 1 WHERE notice_id = #{noticeId}")
    public void setRead(Long noticeId);
}
```

#### java\org\example\mapper\TokenBlacklistMapper.java

```java
package org.example.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.entity.TokenBlacklist;

/**
 * Token黑名单数据访问接口
 */
@Mapper
public interface TokenBlacklistMapper {
    /**
     * 添加Token到黑名单
     * @param tokenBlacklist Token黑名单实体
     */
    @Insert("INSERT INTO token_blacklist(token, expiry_time) VALUES(#{token}, #{expiryTime})")
    void addToBlacklist(TokenBlacklist tokenBlacklist);

    /**
     * 根据Token查询黑名单
     * @param token Token字符串
     * @return Token黑名单实体
     */
    @Select("SELECT * FROM token_blacklist WHERE token = #{token}")
    TokenBlacklist findByToken(String token);

    /**
     * 清理过期的Token
     */
    @Select("DELETE FROM token_blacklist WHERE expiry_time < NOW()")
    void cleanupExpiredTokens();
}
```

#### java\org\example\service\AchievementService.java

```java
package org.example.service;

import org.example.entity.BizAchievement;
import org.example.mapper.AchievementMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 标志性成果业务服务类
 * 处理标志性成果的基础增删查改业务逻辑
 */
@Service
public class AchievementService {

    /**
     * 注入成果Mapper接口，操作数据库
     */
    @Autowired
    private AchievementMapper achievementMapper;

    /**
     * 根据成果ID查询未删除的标志性成果
     * @param achId 成果ID
     * @return 标志性成果实体
     */
    public BizAchievement getAchievementById(Long achId) {
        // 参数非空校验
        if (achId == null) {
            throw new RuntimeException("成果ID不能为空");
        }
        // 查询成果
        BizAchievement achievement = achievementMapper.getAchievementById(achId);
        // 成果不存在校验
        if (achievement == null) {
            throw new RuntimeException("标志性成果不存在或已被删除");
        }
        return achievement;
    }

    /**
     * 查询所有未删除的标志性成果列表
     * @return 标志性成果实体列表
     */
    public List<BizAchievement> listAllAchievements() {
        try {
            return achievementMapper.listAllAchievements();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 新增标志性成果
     * 自动填充创建时间、默认删除状态，返回自增成果ID
     * @param achievement 标志性成果实体（含核心业务字段）
     * @return 自增的成果ID
     */
    public Long addAchievement(BizAchievement achievement,Long createBy) {
        // 核心参数非空校验
        if (achievement == null) {
            throw new RuntimeException("新增成果信息不能为空");
        }
        if (achievement.getCategory() == null) {
            throw new RuntimeException("成果类别不能为空");
        }
        if (achievement.getLevel() == null || achievement.getLevel().trim().isEmpty()) {
            throw new RuntimeException("成果级别不能为空");
        }
        if (achievement.getAchName() == null || achievement.getAchName().trim().isEmpty()) {
            throw new RuntimeException("成果名称不能为空");
        }
        if (achievement.getGotTime() == null) {
            throw new RuntimeException("成果颁发时间不能为空");
        }
        // 自动填充公共字段
        achievement.setIsDelete(0); // 默认未删除
        achievement.setCreateTime(new Date()); // 填充当前创建时间
        try {
            // 调用Mapper新增，返回自增ID
            achievementMapper.addAchievement(achievement);
            return achievementMapper.getLatestAchievement().getAchId();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 修改标志性成果信息
     * 仅修改未删除的成果，根据成果ID更新
     * @param achievement 标志性成果实体（含成果ID和需修改字段）
     */
    public void updateAchievement(Long id,BizAchievement achievement) {
        // 核心参数非空校验
        if (achievement == null) {
            throw new RuntimeException("修改成果信息不能为空");
        }
        if (achievementMapper.getAchievementById(id) == null) {
            throw new RuntimeException("成果ID不存在，无法修改");
        }
        try {
            achievementMapper.updateAchievement(achievement);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 逻辑删除标志性成果
     * 置is_delete=1，并非物理删除
     * @param achId 成果ID
     */
    public void deleteAchievement(Long achId) {
        // 参数非空校验
        if (achId == null) {
            throw new RuntimeException("成果ID不能为空");
        }
        // 校验成果是否存在
        if (achievementMapper.getAchievementById(achId) == null) {
            throw new RuntimeException("标志性成果不存在或已被删除，无法删除");
        }
        try {
            achievementMapper.deleteAchievement(achId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
```

#### java\org\example\service\BizService.java

```java
package org.example.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.entity.*;
import org.example.entity.dto.BizAuditDTO;
import org.example.entity.dto.BizSubDTO;
import org.example.entity.dto.BizReSubDTO;
import org.example.entity.dto.BizTaskDTO;
import org.example.entity.vo.BizAuditVO;
import org.example.entity.vo.BizTaskVo;
import org.example.entity.vo.FileUploadVO;
import org.example.mapper.BizMapper;
import org.example.mapper.SysMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 业务服务类：处理所有业务逻辑
 */
@Service
public class BizService {

    /** 管理员ID */
    private Long ADMIN_ID = 110228L;

    @Autowired
    private BizMapper bizMapper;

    @Autowired
    private SysMapper sysMapper;

    /**
     * 获取全部任务
     * @return 任务列表
     */
    public List<BizTask> getAllTasks() {
        return bizMapper.getAllTasks();
    }

    /**
     * 根据id获取任务
     * @param taskId 任务ID
     * @return 任务对象
     */
    public BizTask getTaskById(Long taskId) {
        try {
            if (taskId == null) {
                throw new RuntimeException("taskID为空");
            }
            if (bizMapper.getTaskById(taskId) == null) {
                throw new RuntimeException("该任务不存在");
            }
            return bizMapper.getTaskById(taskId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取当前任务的所有子任务
     * @param taskId 任务ID
     * @return 子任务列表
     */
    public List<BizTask> getAllChildrenTasks(Long taskId) {
        try {
            if (taskId == null) {
                throw new RuntimeException("taskID为空");
            }
            if (bizMapper.getTaskById(taskId) == null) {
                throw new RuntimeException("该任务不存在");
            }
            return bizMapper.getAllChildrenTasks(taskId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取当前任务的父任务
     * @param taskId 任务ID
     * @return 父任务对象
     */
    public BizTask getParentTask(Long taskId) {
        try {
            if (taskId == null) {
                throw new RuntimeException("taskID为空");
            }
            if (bizMapper.getTaskById(taskId) == null) {
                throw new RuntimeException("该任务不存在");
            }
            Long parentId = bizMapper.getTaskById(taskId).getParentId();
            return bizMapper.getTaskById(parentId);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据部门ID获取任务
     * @param deptId 部门ID
     * @return 任务列表
     */
    public List<BizTask> getTasksByDeptId(Long deptId) {
        try {
            return bizMapper.getTasksByDeptId(deptId);
        } catch (Exception e) {
            throw new RuntimeException("获取任务失败,请检查部门id是否正确");
        }
    }

    /**
     * 根据用户角色获取任务
     * 角色为0和2返回所有任务，角色为1返回leaderid以及为自己的任务
     * @param userId 用户ID
     * @return 任务视图对象列表
     */
    public List<BizTaskVo> getTasksByUserRole(Long userId) {
        try {
            SysUser sysUser = sysMapper.getUserById(userId);
            if (sysUser == null) {
                throw new RuntimeException("用户不存在");
            }

            if (sysUser.getRole().equals("0")) {
                return taskListToTaskVoList(bizMapper.getAllTasks());
            } else if (sysUser.getRole().equals("1")) {
                return taskListToTaskVoList(bizMapper.getTasks(sysUser.getUserId()));
            } else if (sysUser.getRole().equals("2")) {
                return taskListToTaskVoList(bizMapper.getTasksByPrincipalId(sysUser.getUserId()));
            } else {
                throw new RuntimeException("用户角色错误");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 添加任务
     * 只能添加三级任务,根据parent字段判断二级任务是否正确
     * @param taskDTO 任务数据
     */
    public void addTask(BizTaskDTO taskDTO) {
        try {
            // 只能添加三级任务,根据parent字段判断二级任务是否正确
            if (bizMapper.getTaskById(taskDTO.getParentId()) == null) {
                throw new RuntimeException("该二级任务不存在");
            }
            if (bizMapper.getTaskById(taskDTO.getParentId()).getLevel() != 2) {
                throw new RuntimeException("该任务不是二级任务,无法添加");
            }
            if (bizMapper.getTaskById(taskDTO.getParentId()).getDeptId() != taskDTO.getDeptId()) {
                throw new RuntimeException("该任务所属部门与二级任务部门不一致");
            }
            if(taskDTO.getProjectId()!=1){
                throw new RuntimeException("该任务所属项目id不为1");
            }

            BizTask task = taskDTO2Task(taskDTO);
            task.setIsDelete(0);
            task.setCreateTime(new Date());
            task.setUpdateTime(new Date());
            bizMapper.addTask(task);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 更新任务
     * @param taskDTO 任务数据
     */
    public void updateTask(BizTaskDTO taskDTO) {
        try {
            if (bizMapper.getTaskById(taskDTO.getTaskId()) == null) {
                throw new RuntimeException("该任务不存在");
            }
            BizTask task = taskDTO2Task(taskDTO);
            task.setUpdateTime(new Date());
            bizMapper.updateTask(task);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 完成任务
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @Transactional
    public String finishTask(Long taskId, Long userId){
        try {
            if (userId == null) {
                throw new RuntimeException("用户ID为空");
            }
            SysUser sysUser = sysMapper.getUserById(userId);
            if (sysUser == null) {
                throw new RuntimeException("用户不存在");
            }
            if (!sysUser.getRole().equals("0")) {
                throw new RuntimeException("仅限管理员访问");
            }
            if (bizMapper.getTaskById(taskId) == null) {
                throw new RuntimeException("该任务不存在");
            }
            if (bizMapper.getTaskById(taskId).getStatus().equals("2")) {
                throw new RuntimeException("该任务正在审核中，请先完成审核");
            }
            if (bizMapper.getTaskById(taskId).getStatus().equals("3")) {
                throw new RuntimeException("该任务已完成，请勿重复提交");
            }
            BizTask taskById = bizMapper.getTaskById(taskId);
            taskById.setStatus("3");
            taskById.setCurrentValue(taskById.getTargetValue());
            bizMapper.updateTask(taskById);
            sendNotice(userId, taskById.getLeaderId(), "任务完成", "任务已完成", "任务"+taskById.getTaskName()+"已完成", "0", taskId);
            createAuditLog(taskId, userId, "任务完成", 1, 3, "任务完成");
            return "任务"+taskById.getTaskName()+"已完成";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 提交材料
     * @param bizSubDTO 提交数据
     * @param userId 用户ID
     * @return 操作结果
     */
    @Transactional
    public String submitMaterial(BizSubDTO bizSubDTO, Long userId) {
        try {
            // 检查taskid是否存在
            if (bizMapper.getTaskById(bizSubDTO.getTask_id()) == null) {
                throw new RuntimeException("该任务不存在");
            }
            // 验证任务必须为三级任务，否则无法提交
            if (bizMapper.getTaskById(bizSubDTO.getTask_id()).getLevel() != 3) {
                throw new RuntimeException("该任务不是三级任务,无法提交");
            }
            // 验证任务状态，如果当前status为2，则禁止提交
            if (bizMapper.getTaskById(bizSubDTO.getTask_id()).getStatus().equals("2")) {
                throw new RuntimeException("该任务状态未开始或正在审核中,无法提交");
            }
            // 验证任务状态，如果当前status为3，则禁止提交
            if (bizMapper.getTaskById(bizSubDTO.getTask_id()).getStatus().equals("3")) {
                throw new RuntimeException("该任务状态已完成,无法提交");
            }
            // 检查文件是否存在
            SysFile sysFile = sysMapper.getFileById(bizSubDTO.getFile_id());
            if (sysFile == null) {
                throw new RuntimeException("该文件不存在");
            }
            // 验证文件后缀，只能为pdf,doc,docx
            if (!sysFile.getFileName().endsWith(".pdf") && !sysFile.getFileName().endsWith(".doc")
                    && !sysFile.getFileName().endsWith(".docx")) {
                throw new RuntimeException("文件格式错误,请上传pdf,doc,docx格式的文件");
            }

            BizTask task = bizMapper.getTaskById(bizSubDTO.getTask_id());
            BizMaterialSubmission bizMaterialSubmission = new BizMaterialSubmission();
            bizMaterialSubmission.setTaskId(bizSubDTO.getTask_id());
            bizMaterialSubmission.setFileId(sysMapper.getFileByName(sysFile.getFileName()).getFileId());

            // 本次填报值只保留整数，并写入任务 current_value（过程即显示进度）
            BigDecimal rv = bizSubDTO.getReported_value() != null ? bizSubDTO.getReported_value() : BigDecimal.ZERO;
            rv = rv.setScale(0, RoundingMode.HALF_UP);
            bizMaterialSubmission.setReportedValue(rv);
            bizMaterialSubmission.setDataType(bizSubDTO.getData_type());
            bizMaterialSubmission.setSubmitBy(userId);
            bizMaterialSubmission.setSubmitDeptId(sysMapper.getUserById(userId).getDeptId());
            bizMaterialSubmission.setManageDeptId(task.getDeptId());
            bizMaterialSubmission.setSubmitTime(new Date());
            bizMaterialSubmission.setFileSuffix(sysMapper.getFileByName(sysFile.getFileName()).getFileSuffix());
            bizMaterialSubmission.setFlowStatus(10);
            // 已修改，修改内容及原因：将部门审核人从任务的AuditorId改为提交人所在部门的负责人，确保flowStatus=10时审核人能正确收到通知
            // flowStatus = 10 表示"待[所在部门]审批"
            Long handlerId = task.getAuditorId();
            bizMaterialSubmission.setCurrentHandlerId(handlerId);
            bizMaterialSubmission.setIsDelete(0);

            bizMapper.createAudit(bizMaterialSubmission);
            Long subId = bizMapper.getNewestSubId();

            // 提交后任务进入"审核中"，并把 current_value 覆盖写为本次填报值
            BizTask bizTask = bizMapper.getTaskById(bizSubDTO.getTask_id());
            if (bizTask != null) {
                bizTask.setCurrentValue(rv);
                bizTask.setStatus("2");
                bizTask.setComment(bizSubDTO.getComment());
                bizTask.setUpdateTime(new Date());
                bizMapper.updateTask(bizTask);
            }

            // 发送审批信息（使用封装方法）
            // 已修改，修改内容及原因：添加null检查，避免handlerId为null时发送通知导致数据库约束错误
            // 只有当 handlerId 不为 null 时才发送通知
            if (handlerId != null) {
                sendNotice(userId,
                        handlerId,
                        "任务审核",
                        "任务审核",
                        "您有新的任务需要审核",
                        "0",
                        bizSubDTO.getTask_id());
            }

            // 创建审批日志（使用封装方法）
            createAuditLog(subId, userId, "提交", 0, 10, "提交任务");
            // 已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
            String resultMsg = "提交成功";
            if (handlerId != null) {
                try {
                    SysUser handler = sysMapper.getUserById(handlerId);
                    if (handler != null) {
                        resultMsg += "，下一位审批人是" + handler.getUserName();
                    } else {
                        resultMsg += "，下一位审批人ID为" + handlerId;
                    }
                } catch (Exception e) {
                    resultMsg += "，下一位审批人ID为" + handlerId;
                }
            }
            return resultMsg;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据taskId查询审批单
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 审批视图对象列表
     */
    public List<BizAuditVO> getAudit(Long taskId, Long userId) {
        try {
            return auditListToAuditVoList(bizMapper.getAudit(taskId, userId));
        } catch (RuntimeException e) {
            throw new RuntimeException("获取审批单失败,请检查任务id是否正确");
        }
    }

    /**
     * 获取"待我审批"的审批单（按 current_handler_id 查询）
     * @param userId 用户ID
     * @return 审批视图对象列表
     */
    public List<BizAuditVO> getTodoAudits(Long userId) {
        try {
            if (userId == null)
                throw new RuntimeException("userId为空");
            return auditListToAuditVoList(bizMapper.getTodoAudits(userId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据 taskId 查询该任务全部审批单（用于任务详情抽屉展示完整流程）
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 审批视图对象列表
     */
    public List<BizAuditVO> getAuditByTaskId(Long taskId, Long userId) {
        try {
            if (taskId == null)
                throw new RuntimeException("taskId为空");
            BizTask task = bizMapper.getTaskById(taskId);
            if (task == null)
                throw new RuntimeException("该任务不存在");

            SysUser me = sysMapper.getUserById(userId);
            if (me == null)
                throw new RuntimeException("用户不存在");

            // 最小权限校验：管理员/任务负责人/归口负责人可查看；或本人提交过该任务审批单也可查看
            if ("0".equals(me.getRole())
                    || (task.getLeaderId() != null && task.getLeaderId().equals(userId))
                    || (task.getAuditorId() != null && task.getAuditorId().equals(userId))
                    || (task.getPrincipalId() != null && task.getPrincipalId().equals(userId))) {
                return auditListToAuditVoList(bizMapper.getAuditsByTaskId(taskId));
            }

            List<BizAuditVO> list = auditListToAuditVoList(bizMapper.getAuditsByTaskId(taskId));
            boolean submittedByMe = false;
            for (BizAuditVO s : list) {
                if (s != null && s.getSubmitBy() != null && s.getSubmitBy().equals(userId)) {
                    submittedByMe = true;
                    break;
                }
            }
            if (!submittedByMe) {
                throw new RuntimeException("无权限查看该任务的审批记录");
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据 subId 查询审批操作日志（biz_audit_log）
     * @param subId 提交ID
     * @param userId 用户ID
     * @return 审批日志列表
     */
    public List<BizAuditLog> getAuditLogs(Long subId, Long userId) {
        try {
            if (subId == null)
                throw new RuntimeException("subId为空");
            BizMaterialSubmission submission = bizMapper.getAuditBySubId(subId);
            if (submission == null)
                throw new RuntimeException("审批单不存在");

            BizTask task = bizMapper.getTaskById(submission.getTaskId());
            if (task == null)
                throw new RuntimeException("任务不存在");

            SysUser me = sysMapper.getUserById(userId);
            if (me == null)
                throw new RuntimeException("用户不存在");

            // 最小权限校验：管理员/任务部门负责人/归口负责人/提交人可查看
            boolean allowed = "0".equals(me.getRole())
                    || (task.getLeaderId() != null && task.getLeaderId().equals(userId))
                    || (task.getAuditorId() != null && task.getAuditorId().equals(userId))
                    || (task.getPrincipalId() != null && task.getPrincipalId().equals(userId))
                    || (submission.getSubmitBy() != null && submission.getSubmitBy().equals(userId));
            if (!allowed) {
                throw new RuntimeException("无权限查看该审批单的操作日志");
            }

            return bizMapper.getAuditLogsBySubId(subId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 审批任务
     * @param bizAuditDTO 审批数据
     * @param userId 用户ID
     * @return 操作结果
     */
    @Transactional
    public Object audit(BizAuditDTO bizAuditDTO, Long userId) {
        Long subId = bizAuditDTO.getSub_id();
        try {
            BizMaterialSubmission bizMaterialSubmission = bizMapper.getAuditBySubId(subId);
            if (bizMaterialSubmission == null) {
                throw new RuntimeException("该任务不存在");
            }

            // 已修改，修改内容及原因：添加currentHandlerId的null检查，避免在equals比较时出现空指针异常
            // 检查 currentHandlerId 是否为 null
            Long currentHandlerId = bizMaterialSubmission.getCurrentHandlerId();
            if (currentHandlerId == null) {
                throw new RuntimeException("审批单的当前处理人未设置，无法进行审核");
            }

            Map<Integer, Long> nextHandlerMap = Map.of(
                    10, bizMapper.getTaskPrincipalId(bizMaterialSubmission.getTaskId()),
                    20, ADMIN_ID);

            // 已修改，修改内容及原因：安全获取任务的AuditorId，避免getTaskById返回null时出现空指针异常
            // 安全获取任务的 AuditorId
            BizTask task = bizMapper.getTaskById(bizMaterialSubmission.getTaskId());
            Long taskAuditorId = (task != null) ? task.getAuditorId() : null;

            Map<Integer, Long> backHandlerMap = Map.of(
                    10, bizMaterialSubmission.getSubmitBy(),
                    20, taskAuditorId != null ? taskAuditorId : bizMaterialSubmission.getSubmitBy(),
                    30, bizMapper.getTaskPrincipalId(bizMaterialSubmission.getTaskId()),
                    -20, bizMaterialSubmission.getSubmitBy(),
                    -30, taskAuditorId != null ? taskAuditorId : bizMaterialSubmission.getSubmitBy());

            // 分支1：当前用户是处理人
            if (currentHandlerId.equals(userId)) {
                if (bizMaterialSubmission.getFlowStatus() == 10) {
                    // 部门负责人审核逻辑
                    if (bizAuditDTO.getIs_pass()) {
                        Long nextHandlerId = nextHandlerMap.get(bizMaterialSubmission.getFlowStatus());

                        // 更新审批单状态（使用封装方法）
                        updateAuditStatus(subId, nextHandlerId, 20);

                        // 发送通知（使用封装方法）
                        sendNotice(userId,
                                nextHandlerId,
                                "任务审核",
                                "任务审核",
                                "您有新的任务需要审核",
                                "0",
                                bizMaterialSubmission.getTaskId());

                        // 创建审批日志（使用封装方法）
                        createAuditLog(subId, userId, "通过", 10, 20, bizAuditDTO.getTitle());

                        System.out.println("已审批，下一位审批人id为" + nextHandlerId);
                        // 已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
                        String resultMsg = "已审批";
                        if (nextHandlerId != null) {
                            try {
                                SysUser nextUser = sysMapper.getUserById(nextHandlerId);
                                if (nextUser != null) {
                                    resultMsg += "，下一位审批人为" + nextUser.getUserName();
                                } else {
                                    resultMsg += "，下一位审批人ID为" + nextHandlerId;
                                }
                            } catch (Exception e) {
                                resultMsg += "，下一位审批人ID为" + nextHandlerId;
                            }
                        }
                        return resultMsg;
                    } else {
                        Long backHandlerId = backHandlerMap.get(bizMaterialSubmission.getFlowStatus());

                        // 更新审批单状态（使用封装方法）
                        updateAuditStatus(subId, backHandlerId, -10);
                        // 退回后任务不应保持"审核中"，恢复为"进行中"，允许重新提交
                        bizMapper.updateTaskStatus(bizMaterialSubmission.getTaskId(), "1");

                        // 发送通知（使用封装方法）
                        sendNotice(userId,
                                backHandlerId,
                                "任务退回",
                                "任务退回",
                                "您提交的材料被退回",
                                "0",
                                bizMaterialSubmission.getTaskId());

                        // 创建审批日志（使用封装方法）
                        createAuditLog(subId, userId, "退回", 10, -10, bizAuditDTO.getTitle());

                        System.out.println("已退回，退回到id为" + backHandlerId);
                        // 已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
                        String resultMsg = "已退回";
                        if (backHandlerId != null) {
                            try {
                                SysUser backUser = sysMapper.getUserById(backHandlerId);
                                if (backUser != null) {
                                    resultMsg += "，退回到" + backUser.getUserName();
                                } else {
                                    resultMsg += "，退回到ID为" + backHandlerId;
                                }
                            } catch (Exception e) {
                                resultMsg += "，退回到ID为" + backHandlerId;
                            }
                        }
                        return resultMsg;
                    }
                } else if (bizMaterialSubmission.getFlowStatus() == 20) {
                    if (bizAuditDTO.getIs_pass()) {
                        Long nextHandlerId = nextHandlerMap.get(bizMaterialSubmission.getFlowStatus());
                        ;// 管理员

                        // 更新审批单状态（使用封装方法）
                        updateAuditStatus(subId, nextHandlerId, 30);

                        // 发送通知（使用封装方法）
                        sendNotice(userId,
                                nextHandlerId,
                                "任务审核",
                                "任务审核",
                                "您有新的任务需要审核",
                                "0",
                                bizMaterialSubmission.getTaskId());

                        // 创建审批日志（使用封装方法）
                        createAuditLog(subId, userId, "通过", 20, 30, bizAuditDTO.getTitle());

                        System.out.println("已审批，下一位审批人id为" + nextHandlerId);
                        // 已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
                        String resultMsg = "已审批";
                        if (nextHandlerId != null) {
                            try {
                                SysUser nextUser = sysMapper.getUserById(nextHandlerId);
                                if (nextUser != null) {
                                    resultMsg += "，下一位审批人为" + nextUser.getUserName();
                                } else {
                                    resultMsg += "，下一位审批人ID为" + nextHandlerId;
                                }
                            } catch (Exception e) {
                                resultMsg += "，下一位审批人ID为" + nextHandlerId;
                            }
                        }
                        return resultMsg;
                    } else {
                        // 退回到提交人的部门负责人
                        // 根据提交人id获取部门负责人id
                        Long submitBy = bizMaterialSubmission.getSubmitBy();
                        Long backHandlerId = backHandlerMap.get(bizMaterialSubmission.getFlowStatus());

                        // 更新审批单状态（使用封装方法）
                        updateAuditStatus(subId, backHandlerId, -20);
                        // 退回后任务不应保持"审核中"，恢复为"进行中"，允许重新提交
                        bizMapper.updateTaskStatus(bizMaterialSubmission.getTaskId(), "1");

                        // 发送通知（使用封装方法）
                        sendNotice(userId,
                                backHandlerId,
                                "任务退回",
                                "任务退回",
                                "您提交的材料被退回",
                                "0",
                                bizMaterialSubmission.getTaskId());

                        // 创建审批日志（使用封装方法）
                        createAuditLog(subId, userId, "退回", 20, -20, bizAuditDTO.getTitle());

                        System.out.println("已退回，退回到id为" + backHandlerId);
                        // 已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
                        String resultMsg = "已退回";
                        if (backHandlerId != null) {
                            try {
                                SysUser backUser = sysMapper.getUserById(backHandlerId);
                                if (backUser != null) {
                                    resultMsg += "，退回到" + backUser.getUserName();
                                } else {
                                    resultMsg += "，退回到ID为" + backHandlerId;
                                }
                            } catch (Exception e) {
                                resultMsg += "，退回到ID为" + backHandlerId;
                            }
                        }
                        return resultMsg;
                    }
                } else if (bizMaterialSubmission.getFlowStatus() == 30) {
                    if (bizAuditDTO.getIs_pass()) {

                        // 更新审批单状态（使用封装方法）
                        bizMaterialSubmission.setFlowStatus(40);
                        bizMapper.updateAudit(bizMaterialSubmission);
                        // 更新任务状态
                        BizTask bizTask = bizMapper.getTaskById(bizMaterialSubmission.getTaskId());
                        // 口径调整：任务 current_value 以"本次填报值"为准（覆盖写），不再在最终通过时重复累加
                        BigDecimal rv = bizMaterialSubmission.getReportedValue() != null ? bizMaterialSubmission.getReportedValue() : BigDecimal.ZERO;
                        rv = rv.setScale(0, RoundingMode.HALF_UP);
                        bizTask.setCurrentValue(rv);
                        if (bizTask.getCurrentValue().compareTo(bizTask.getTargetValue()) >= 0) {
                            bizTask.setStatus("3");
                        } else {
                            bizTask.setStatus("1");
                        }
                        bizMapper.updateCurrentTask(bizTask);

                        // 发送通知，告知提交人审批过程已完成
                        sendNotice(userId,
                                bizMaterialSubmission.getSubmitBy(),
                                "任务审核完成",
                                "审核完成",
                                "您提交的审核任务已完成",
                                "0",
                                bizMaterialSubmission.getTaskId());

                        // 创建审批日志（使用封装方法）
                        createAuditLog(subId, userId, "通过", 30, 40, bizAuditDTO.getTitle());

                        System.out.println("审批完成");
                        return "审批完成";
                    } else {
                        // 退回到归口负责人
                        Long backHandlerId = backHandlerMap.get(bizMaterialSubmission.getFlowStatus());

                        // 更新审批单状态（使用封装方法）
                        updateAuditStatus(subId, backHandlerId, -30);
                        // 退回后任务不应保持"审核中"，恢复为"进行中"，允许重新提交
                        bizMapper.updateTaskStatus(bizMaterialSubmission.getTaskId(), "1");

                        // 发送通知（使用封装方法）
                        sendNotice(userId,
                                backHandlerId,
                                "任务退回",
                                "任务退回",
                                "您提交的材料被退回",
                                "0",
                                bizMaterialSubmission.getTaskId());

                        // 创建审批日志（使用封装方法）
                        createAuditLog(subId, userId, "退回", 30, -30, bizAuditDTO.getTitle());

                        System.out.println("已退回，退回到id为" + backHandlerId);
                        // 已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
                        String resultMsg = "已退回";
                        if (backHandlerId != null) {
                            try {
                                SysUser backUser = sysMapper.getUserById(backHandlerId);
                                if (backUser != null) {
                                    resultMsg += "，退回到" + backUser.getUserName();
                                } else {
                                    resultMsg += "，退回到ID为" + backHandlerId;
                                }
                            } catch (Exception e) {
                                resultMsg += "，退回到ID为" + backHandlerId;
                            }
                        }
                        return resultMsg;
                    }
                } else if (bizMaterialSubmission.getFlowStatus() == -20) {
                    if (bizAuditDTO.getIs_pass()) {
                        throw new RuntimeException("请重新提交材料");
                    } else {
                        Long backHandlerId = backHandlerMap.get(bizMaterialSubmission.getFlowStatus());
                        updateAuditStatus(subId, backHandlerId, -10);
                        // 退回后任务不应保持"审核中"，恢复为"进行中"，允许重新提交
                        bizMapper.updateTaskStatus(bizMaterialSubmission.getTaskId(), "1");
                        sendNotice(userId,
                                backHandlerId,
                                "任务退回",
                                "任务退回",
                                "您提交的材料被退回",
                                "0",
                                bizMaterialSubmission.getTaskId());
                        createAuditLog(subId, userId, "退回", -20, -10, bizAuditDTO.getTitle());
                        System.out.println("已退回，退回到id为" + backHandlerId);
                        // 已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
                        String resultMsg = "已退回";
                        if (backHandlerId != null) {
                            try {
                                SysUser backUser = sysMapper.getUserById(backHandlerId);
                                if (backUser != null) {
                                    resultMsg += "，退回到" + backUser.getUserName();
                                } else {
                                    resultMsg += "，退回到ID为" + backHandlerId;
                                }
                            } catch (Exception e) {
                                resultMsg += "，退回到ID为" + backHandlerId;
                            }
                        }
                        return resultMsg;
                    }
                } else if (bizMaterialSubmission.getFlowStatus() == -30) {
                    if (bizAuditDTO.getIs_pass()) {
                        throw new RuntimeException("请重新提交材料");
                    }
                    Long backHandlerId = backHandlerMap.get(bizMaterialSubmission.getFlowStatus());
                    updateAuditStatus(subId, backHandlerId, -20);
                    // 退回后任务不应保持"审核中"，恢复为"进行中"，允许重新提交
                    bizMapper.updateTaskStatus(bizMaterialSubmission.getTaskId(), "1");
                    sendNotice(userId,
                            backHandlerId,
                            "任务退回",
                            "任务退回",
                            "您提交的材料被退回",
                            "0",
                            bizMaterialSubmission.getTaskId());
                    createAuditLog(subId, userId, "退回", -30, -20, bizAuditDTO.getTitle());
                    System.out.println("已退回，退回到id为" + backHandlerId);
                    return "已退回，退回到" + sysMapper.getUserById(backHandlerId).getUserName();
                } else {// 补充：flowStatus不在枚举值范围内的返回值
                    throw new RuntimeException("不支持的审批流程状态：" + bizMaterialSubmission.getFlowStatus());
                }
            } else {
                // 分支2：当前用户不是处理人
                throw new RuntimeException("您不是该任务的当前审批人，无法操作");
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 退回重新提交材料
     * @param resubDTOBiz 重新提交数据
     * @param userId 用户ID
     * @return 操作结果
     */
    @Transactional
    public String reSubmitMaterial(BizReSubDTO resubDTOBiz, Long userId) {
        try {
            BizMaterialSubmission bizMaterialSubmission = bizMapper.getAuditBySubId(resubDTOBiz.getSub_id());
            if (bizMaterialSubmission == null) {
                throw new RuntimeException("该任务不存在");
            }
            Map<Integer, Long> nextHandlerMap = Map.of(
                    -10, sysMapper.getDeptLeaderId(userId),
                    -20, bizMapper.getTaskPrincipalId(bizMaterialSubmission.getTaskId()),
                    -30, 1L);

            Long nextHandlerId = nextHandlerMap.get(bizMaterialSubmission.getFlowStatus());
            if (bizMaterialSubmission.getFlowStatus() >= 0) {
                throw new RuntimeException("该任务状态不是退回状态,无法重新提交");
            }
            // 重新提交同样只保留整数，并覆盖写任务 current_value
            BigDecimal rv = resubDTOBiz.getReported_value() != null ? resubDTOBiz.getReported_value() : BigDecimal.ZERO;
            rv = rv.setScale(0, RoundingMode.HALF_UP);
            bizMaterialSubmission.setReportedValue(rv);
            bizMaterialSubmission.setDataType(resubDTOBiz.getData_type());
            bizMaterialSubmission.setFileId(resubDTOBiz.getFile_id());
            bizMaterialSubmission.setSubmitTime(new Date());
            bizMaterialSubmission.setFlowStatus(-bizMaterialSubmission.getFlowStatus());
            bizMaterialSubmission.setCurrentHandlerId(nextHandlerId);
            bizMapper.updateAudit(bizMaterialSubmission);

            // 重新提交后恢复任务状态为"审核中"，并覆盖写 current_value
            BizTask bizTask = bizMapper.getTaskById(bizMaterialSubmission.getTaskId());
            if (bizTask != null) {
                bizTask.setCurrentValue(rv);
                bizTask.setStatus("2");
                bizMapper.updateCurrentTask(bizTask);
            }

            // 已修改，修改内容及原因：添加null检查，避免nextHandlerId为null时发送通知导致数据库约束错误
            // 只有当 nextHandlerId 不为 null 时才发送通知
            if (nextHandlerId != null) {
                sendNotice(userId,
                        nextHandlerId,
                        "任务审核",
                        "任务审核",
                        "您有新的任务需要审核",
                        "0",
                        bizMaterialSubmission.getTaskId());
            }

            createAuditLog(resubDTOBiz.getSub_id(), userId, "重新提交", -bizMaterialSubmission.getFlowStatus(),
                    bizMaterialSubmission.getFlowStatus(), "重新提交");
            // 已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
            String resultMsg = "已重新提交";
            if (nextHandlerId != null) {
                try {
                    SysUser handler = sysMapper.getUserById(nextHandlerId);
                    if (handler != null) {
                        resultMsg += ",审核人为" + handler.getUserName();
                    }
                } catch (Exception e) {
                    // 忽略获取用户信息失败的情况
                }
            }
            return resultMsg;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 撤回提交申请
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 操作结果
     */
    public Object drawbackSubmit(Long taskId, Long userId){
        try{
            BizMaterialSubmission bizMaterialSubmission = bizMapper.getNewestAudit(taskId);
            if (bizMaterialSubmission == null) {
                throw new RuntimeException("该任务不存在");
            }
            if (!bizMaterialSubmission.getSubmitBy().equals(userId)){
                throw new RuntimeException("您不是该任务的提交人，无法撤回");
            }
            bizMaterialSubmission.setIsDelete(1);
            bizMapper.updateAudit(bizMaterialSubmission);
            createAuditLog(bizMaterialSubmission.getSubId(), userId, "撤回提交", bizMaterialSubmission.getFlowStatus(), -bizMaterialSubmission.getFlowStatus(), "撤回提交");
            return "已撤回提交";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取该任务上一次审批通过的文件
     * @param taskId 任务ID
     * @return 文件上传视图对象
     */
    public FileUploadVO getLastCycleFiles(Long taskId) {
        try{
            BizTask task = bizMapper.getTaskById(taskId);
            if (task == null) {
                throw new RuntimeException("任务不存在");
            }
            BizMaterialSubmission submission = bizMapper.getLatestApprovedAuditByTaskId(taskId);
            if (submission == null) {
                return null;
            }
            FileUploadVO fileUploadVo = new FileUploadVO();
            fileUploadVo.setFileId(submission.getFileId());
            fileUploadVo.setFilename(sysMapper.getFileById(submission.getFileId()).getFileName());
            fileUploadVo.setFilepath(sysMapper.getFileById(submission.getFileId()).getFileUrl());
            return fileUploadVo;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据父任务ID获取三级任务
     * @param parentId 父任务ID
     * @return 任务列表
     */
    public List<BizTask> getThirdLevelTasksByParentId(Long parentId) {
        try {
            return bizMapper.getThirdLevelTasksByParentId(parentId);
        } catch (RuntimeException e) {
            throw new RuntimeException("获取任务失败,请检查任务id是否正确");
        }
    }

    /**
     * 根据归口负责人ID获取任务
     * @param principalId 归口负责人ID
     * @return 任务列表
     */
    public List<BizTask> getTasksByPrincipalId(Long principalId) {
        try {
            return bizMapper.getTasksByPrincipalId(principalId);
        } catch (Exception e) {
            throw new RuntimeException("获取任务失败,请检查负责人id是否正确");
        }
    }

    /**
     * 封装方法：发送系统通知
     * @param fromUserId 发送人ID
     * @param toUserId 接收人ID
     * @param triggerEvent 触发事件
     * @param title 标题
     * @param content 内容
     * @param sourceType 来源类型
     * @param sourceId 来源ID
     */
    private void sendNotice(Long fromUserId, Long toUserId, String triggerEvent,
                            String title, String content, String sourceType, Long sourceId) {
        // 已修改，修改内容及原因：添加toUserId的null检查，避免toUserId为null时插入数据库导致约束错误
        // 如果 toUserId 为 null，不发送通知，避免数据库约束错误
        if (toUserId == null) {
            System.out.println("警告：无法发送通知，接收人ID为空。标题：" + title);
            return;
        }
        SysNotice sysNotice = new SysNotice();
        sysNotice.setFromUserId(fromUserId);
        sysNotice.setToUserId(toUserId);
        sysNotice.setTriggerEvent(triggerEvent);
        sysNotice.setTitle(title);
        sysNotice.setContent(content);
        sysNotice.setSourceType(sourceType);
        sysNotice.setSourceId(sourceId);
        sysNotice.setIsRead("0");

        sysMapper.sendNotice(sysNotice);
        System.out.println("id=" + toUserId + "的用户收到一条通知：" + title);
    }

    /**
     * 封装方法：创建审批日志
     * @param subId 提交ID
     * @param operatorId 操作人ID
     * @param actionType 动作类型
     * @param preStatus 前状态
     * @param postStatus 后状态
     * @param comment 意见
     */
    private void createAuditLog(Long subId, Long operatorId, String actionType,
                                Integer preStatus, Integer postStatus, String comment) {
        BizAuditLog bizAuditLog = new BizAuditLog();
        bizAuditLog.setLogId(null);
        bizAuditLog.setSubId(subId);
        bizAuditLog.setOperatorId(operatorId);
        bizAuditLog.setActionType(actionType);
        bizAuditLog.setPreStatus(preStatus);
        bizAuditLog.setPostStatus(postStatus);
        bizAuditLog.setComment(comment);
        bizAuditLog.setCreateTime(new Date());

        bizMapper.createAuditLog(bizAuditLog);
    }

    /**
     * 封装方法：更新审批单状态和处理人
     * @param subId 提交ID
     * @param currentHandlerId 当前处理人ID
     * @param flowStatus 流程状态
     */
    private void updateAuditStatus(Long subId, Long currentHandlerId, Integer flowStatus) {
        BizMaterialSubmission bizMaterialSubmission = bizMapper.getAuditBySubId(subId);
        if (bizMaterialSubmission != null) {
            bizMaterialSubmission.setCurrentHandlerId(currentHandlerId);
            bizMaterialSubmission.setFlowStatus(flowStatus);
            bizMapper.updateAudit(bizMaterialSubmission);
        }
    }

    /**
     * TaskToTaskVo转换方法
     * @param task 任务实体
     * @return 任务视图对象
     */
    public BizTaskVo taskToTaskVo(BizTask task) {
        BizTaskVo taskVo = new BizTaskVo();
        taskVo.setTaskId(task.getTaskId());
        taskVo.setProjectId(task.getProjectId());
        taskVo.setParentId(task.getParentId());
        taskVo.setAncestors(task.getAncestors());
        taskVo.setPhase(task.getPhase());
        taskVo.setTaskCode(task.getTaskCode());
        taskVo.setTaskName(task.getTaskName());
        taskVo.setLevel(task.getLevel());
        taskVo.setComment(task.getComment());
        taskVo.setDeptId(task.getDeptId());
        taskVo.setDeptName(sysMapper.getDeptById(task.getDeptId()).getDeptName());
        taskVo.setPrincipalId(task.getPrincipalId());
        taskVo.setPrincipalName(sysMapper.getUserById(task.getPrincipalId()).getUserName());
        // 避免空指针错误
        if(task.getAuditorId()!=null){
            taskVo.setAuditorId(task.getAuditorId());
            taskVo.setAuditorName(sysMapper.getUserById(task.getAuditorId()).getUserName());
        }else {
            taskVo.setAuditorId(null);
            taskVo.setAuditorName("无");
        }
        taskVo.setLeaderId(task.getLeaderId());
        taskVo.setLeaderName(sysMapper.getUserById(task.getLeaderId()).getUserName());
        taskVo.setExpTarget(task.getExpTarget());
        taskVo.setExpLevel(task.getExpLevel());
        taskVo.setExpEffect(task.getExpEffect());
        taskVo.setExpMaterialDesc(task.getExpMaterialDesc());
        taskVo.setDataType(task.getDataType());
        taskVo.setTargetValue(task.getTargetValue());
        taskVo.setCurrentValue(task.getCurrentValue());
        taskVo.setWeight(task.getWeight());
        taskVo.setProgress(task.getProgress());
        taskVo.setStatus(task.getStatus());
        taskVo.setIsDelete(task.getIsDelete());
        taskVo.setCreateTime(task.getCreateTime());
        taskVo.setUpdateTime(task.getUpdateTime());
        return taskVo;
    }

    /**
     * TaskListToTaskVoList转换方法
     * @param tasks 任务实体列表
     * @return 任务视图对象列表
     */
    public List<BizTaskVo> taskListToTaskVoList(List<BizTask> tasks) {
        List<BizTaskVo> taskVos = new ArrayList<>();
        for (BizTask task : tasks) {
            taskVos.add(taskToTaskVo(task));
        }
        return taskVos;
    }

    /**
     * TaskDTO转Task转换方法
     * @param taskDTO 任务数据传输对象
     * @return 任务实体
     */
    public BizTask taskDTO2Task(BizTaskDTO taskDTO) {
        BizTask task = new BizTask();
        task.setTaskId(taskDTO.getTaskId());
        task.setProjectId(taskDTO.getProjectId());
        task.setParentId(taskDTO.getParentId());
        task.setAncestors(taskDTO.getAncestors());
        task.setPhase(taskDTO.getPhase());
        task.setTaskCode(taskDTO.getTaskCode());
        task.setTaskName(taskDTO.getTaskName());
        task.setLevel(taskDTO.getLevel());
        task.setComment(taskDTO.getComment());
        task.setDeptId(taskDTO.getDeptId());
        task.setAuditorId(taskDTO.getAuditorId());
        task.setPrincipalId(taskDTO.getPrincipalId());
        task.setLeaderId(taskDTO.getLeaderId());
        task.setExpTarget(taskDTO.getExpTarget());
        task.setExpLevel(taskDTO.getExpLevel());
        task.setExpEffect(taskDTO.getExpEffect());
        task.setExpMaterialDesc(taskDTO.getExpMaterialDesc());
        task.setDataType(taskDTO.getDataType());
        task.setTargetValue(taskDTO.getTargetValue());
        task.setCurrentValue(taskDTO.getCurrentValue());
        task.setWeight(taskDTO.getWeight());
        task.setProgress(taskDTO.getProgress());
        task.setStatus(taskDTO.getStatus());
        return task;
    }

    /**
     * Audit转AuditVO转换方法
     * @param audit 材料提交实体
     * @return 审批视图对象
     */
    public BizAuditVO auditToAuditVo(BizMaterialSubmission audit){
        BizAuditVO bizAuditVO = new BizAuditVO();
        bizAuditVO.setSubId(audit.getSubId());
        bizAuditVO.setTaskId(audit.getTaskId());
        bizAuditVO.setFileId(audit.getFileId());
        bizAuditVO.setFilename(sysMapper.getFileById(audit.getFileId()).getFileName());
        bizAuditVO.setReportedValue(audit.getReportedValue());
        bizAuditVO.setDataType(audit.getDataType());
        bizAuditVO.setSubmitBy(audit.getSubmitBy());
        bizAuditVO.setSubmitDeptId(audit.getSubmitDeptId());
        bizAuditVO.setManageDeptId(audit.getManageDeptId());
        bizAuditVO.setSubmitTime(audit.getSubmitTime());
        bizAuditVO.setFileSuffix(audit.getFileSuffix());
        bizAuditVO.setFlowStatus(audit.getFlowStatus());
        bizAuditVO.setCurrentHandlerId(audit.getCurrentHandlerId());
        bizAuditVO.setIsDelete(audit.getIsDelete());
        return bizAuditVO;
    }

    /**
     * AuditList转AuditVoList转换方法
     * @param audits 材料提交实体列表
     * @return 审批视图对象列表
     */
    public List<BizAuditVO> auditListToAuditVoList(List<BizMaterialSubmission> audits){
        List<BizAuditVO> auditVos = new ArrayList<>();
        for (BizMaterialSubmission audit : audits) {
            auditVos.add(auditToAuditVo(audit));
        }
        return auditVos;
    }
}
```

#### java\org\example\service\ScheduledTaskService.java

```java
package org.example.service;

import org.example.entity.*;
import org.example.mapper.BizMapper;
import org.example.mapper.SysMapper;
import org.example.mapper.TokenBlacklistMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 定时任务服务类
 * 负责执行定期提醒、清理等后台任务
 */
@Service
public class ScheduledTaskService {

    private Long ADMIN_ID = 110228L;

    @Autowired
    private BizMapper bizMapper;

    @Autowired
    private SysMapper sysMapper;

    @Autowired
    private TokenBlacklistMapper tokenBlacklistMapper;

    /**
     * 每月初提醒审核任务
     * 每月1号上午9:00执行
     * 提醒所有部门负责人本部门任务完成情况
     */
    @Scheduled(cron = "0 0 9 1 * ?")  // 每月1号上午9:00
    @Transactional
    public String monthlyDeptLeaderReminder() {
        try {
            System.out.println("[" + new Date() + "] 开始执行月度部门负责人审核任务提醒...");

            Set<Long> deptLeaderIds = getDeptLeadersId();

            if (deptLeaderIds.isEmpty()) {
                System.out.println("没有需要发送年度提醒的用户");
                return "没有需要发送年度提醒的用户";
            }

            int successCount = 0;
            for (Long deptLeaderId: deptLeaderIds){
                SysDept dept = sysMapper.getDeptByUserId(deptLeaderId);
//                获取当前时间，将年份转为Integer
                int currentYear = java.time.LocalDate.now().getYear();
//                获取本年度本部门的所有任务
                List<BizTask> currentYearTasks = bizMapper.getTasksByDeptIdAndPhase(dept.getDeptId(),currentYear);
                if (currentYearTasks.isEmpty()) {
                    continue;
                }
                // 统计截止本月有进展的任务数量
                Integer monthCount = 0;
                for (BizTask task: currentYearTasks){
                    if (task.getCurrentValue().signum() != 0) {
                        monthCount++;
                    }
                }

//                统计已完成任务数量
                Integer completeCount = 0;
                for (BizTask task: currentYearTasks){
                    if (task.getStatus().equals("3")) {
                        completeCount++;
                    }
                }

//                统计待审核任务数量
                Integer auditCount = 0;
                for (BizTask task: currentYearTasks){
                    if (task.getStatus().equals("2")) {
                        auditCount++;
                    }
                }

                String title = "部门双高建设任务完成情况月度提醒";
                String content = "尊敬的 "+ sysMapper.getUserById(deptLeaderId).getNickName() +
                        " 您好,本年度贵部门有 " + currentYearTasks.size() + " 个双高建设任务，\n" +
                        "截至本月，共" +  monthCount + "个任务已有进展。\n"+
                        "已完成任务" + completeCount + " 个，" + auditCount + " 个任务尚未审核\n"+
                        "请及时关注本部门双高建设任务进展情况。";
                sendSystemNotice(deptLeaderId, title, content, "月度提醒");
                System.out.println("向用户ID " + deptLeaderId + " 发送月度提醒成功");
                successCount++;
            }
            return "向"+successCount+"名用户发送月度提醒成功";

        } catch (Exception e) {
            System.err.println("执行月度审核提醒任务时发生错误: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }


    /**
     * 每月初提醒审核任务
     * 每月1号上午9:00执行
     * 提醒所有有待审核任务的负责人
     */
    @Scheduled(cron = "0 0 9 1 * ?")  // 每月1号上午9:00
    @Transactional
    public String monthlyAuditorReminder() {
        try {
            System.out.println("[" + new Date() + "] 开始执行月度审核人审核任务提醒...");
            List<BizMaterialSubmission> allPendingAudits = bizMapper.getAllPendingAudits();


            Set<Long> auditorIds = new HashSet<>();
            for (BizMaterialSubmission pendingAudit: allPendingAudits){
                auditorIds.add(pendingAudit.getCurrentHandlerId());
            }

            if (auditorIds.isEmpty()) {
                System.out.println("没有需要发送年度提醒的用户");
                return "没有需要发送年度提醒的用户";
            }
            int successCount = 0;
            for (Long auditorId: auditorIds){
//              获取待审核任务数量
                Integer auditCount = 0;
                for (BizMaterialSubmission pendingAudit: allPendingAudits){
                    if (pendingAudit.getCurrentHandlerId().equals(auditorId)) {
                        auditCount++;
                    }
                }

                String title = "月度审核任务提醒";
                String content = "尊敬的 " + sysMapper.getUserById(auditorId).getNickName() +
                        " 您好,截至目前，您有 " + auditCount + " 个待审核任务。\n" +
                        "请及时处理。";

                sendSystemNotice(auditorId, title, content, "月度提醒");
                System.out.println("向用户ID " + auditorId + " 发送月度提醒成功");
                successCount++;
            }
            return "向"+successCount+"名用户发送月度提醒成功";
        } catch (Exception e) {
            System.err.println("执行月度审核提醒任务时发生错误: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
//
    /**
     * 年度提醒任务
     * 每年1月1日上午10:00执行
     * 提醒所有部门负责人年度任务情况
     */
    @Scheduled(cron = "0 0 10 1 1 ?")  // 每年1月1日上午10:00
    @Transactional
    public String yearlyReminder() {
        try {
            System.out.println("[" + new Date() + "] 开始执行年度提醒任务...");

            // 获取所有需要发送年度提醒的用户ID
            Set<Long> leadersId = getDeptLeadersId();

            if (leadersId.isEmpty()) {
                System.out.println("没有需要发送年度提醒的用户");
                return "没有需要发送年度提醒的用户";
            }
            int currentYear = java.time.LocalDate.now().getYear();
            int successCount = 0;
            for(Long leaderId: leadersId){
                SysDept dept = sysMapper.getDeptByUserId(leaderId);
//                获取本年度本部门的所有任务
                List<BizTask> currentYearTasks = bizMapper.getTasksByDeptIdAndPhase(dept.getDeptId(),currentYear);
                if (currentYearTasks.isEmpty()) {
                    continue;
                }

                String title = "部门双高建设任务年度提醒";
                String content = "尊敬的 "+ sysMapper.getUserById(leaderId).getNickName() +
                        " 您好,本年度贵部门有 " + currentYearTasks.size() + " 个双高建设任务，\n" +
                        "请及时关注本部门双高建设任务进展情况。";
                sendSystemNotice(leaderId, title, content, "月度提醒");
                successCount++;
                System.out.println("向用户ID " + leaderId + " 发送月度提醒成功");

            }

            System.out.println("年度提醒完成，成功发送 " + successCount + " 条提醒");
            return "年度提醒完成，成功发送 " + successCount + " 条提醒";

        } catch (Exception e) {
            System.err.println("执行年度提醒任务时发生错误: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
//
    /**
     * 封装方法：发送系统通知
     *
     * @param toUserId 接收用户ID
     * @param title 通知标题
     * @param content 通知内容
     * @param triggerEvent 触发事件类型
     */
    private void sendSystemNotice(Long toUserId, String title, String content, String triggerEvent) {
        SysNotice notice = new SysNotice();
        notice.setFromUserId(ADMIN_ID);
        notice.setToUserId(toUserId);
        notice.setType("1");  // 提醒类型
        notice.setTriggerEvent(triggerEvent);
        notice.setTitle(title);
        notice.setContent(content);
        notice.setSourceType("0");
        notice.setSourceId(0L);
        notice.setIsRead("0");
        notice.setIsDelete(0);
        notice.setCreateTime(new Date());

        sysMapper.sendNotice(notice);

        System.out.println("系统通知已发送 - 接收人ID: " + toUserId + ", 标题: " + title);
    }

    /**
     * 获取需要发送年度提醒的所有用户ID
     * 您可以自定义这里的逻辑
     *
     * @return 用户ID集合
     */
    private Set<Long> getDeptLeadersId() {
        Set<Long> userIds = new HashSet<>();

        try {
            // 获取所有部门负责人
            List<Long> deptLeaders = sysMapper.getAllDeptLeaders();
            if (deptLeaders != null) {
                userIds.addAll(deptLeaders);
            }

            // 可以根据需要添加其他用户组
            // 例如：所有管理员、所有用户等

        } catch (Exception e) {
            System.err.println("获取用户列表时发生错误: " + e.getMessage());
        }

        return userIds;
    }
    /**
     * 测试方法：手动触发月度提醒
     */
    public String triggerMonthDeptLeaderReminder() {
        System.out.println("手动触发月度审核提醒...");
        return monthlyDeptLeaderReminder();
    }

    /**
     * 测试方法：手动触发月度提醒
     */
    public String triggerMonthAuditorReminder() {
        System.out.println("手动触发月度审核提醒...");
        return monthlyAuditorReminder();
    }


    /**
     * 测试方法：手动触发年度提醒
     */
    public String triggerYearlyReminder() {
        System.out.println("手动触发年度提醒...");
        return yearlyReminder();
    }
    /**
     * 每日清理过期的Token黑名单
     * 每天凌晨2:00执行
     */
    @Scheduled(cron = "0 0 2 * * ?")  // 每天凌晨2:00
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            System.out.println("[" + new Date() + "] 开始清理过期Token...");
            tokenBlacklistMapper.cleanupExpiredTokens();
            System.out.println("Token清理完成");
        } catch (Exception e) {
            System.err.println("清理过期Token时发生错误: " + e.getMessage());
        }
    }


    /**
     * 每年更新任务状态
     * 每天凌晨10:00执行
     */
    @Scheduled(cron = "0 0 10 1 1 ?")
    @Transactional
    public void updateTaskStatus() {
        try {
            System.out.println("[" + new Date() + "] 开始更新任务状态...");
            int currentYear = java.time.LocalDate.now().getYear();
            List<BizTask> tasks = bizMapper.getTasksByPhase(currentYear);
            for (BizTask task : tasks) {
                task.setStatus("1");
                bizMapper.updateTask(task);
            }
            System.out.println("已将"+currentYear+"所有任务的状态修改为进行中");
        } catch (Exception e) {
            System.err.println("更新任务状态时发生错误: " + e.getMessage());
        }
    }


}
```

#### java\org\example\service\SysService.java

```java
package org.example.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.entity.*;
import org.example.entity.dto.FileUploadDTO;
import org.example.entity.dto.SysAlertDTO;
import org.example.entity.dto.SysNoticeDTO;
import org.example.entity.dto.SysUserDTO;
import org.example.entity.vo.FileUploadVO;
import org.example.entity.vo.SysLoginVO;
import org.example.mapper.BizMapper;
import org.example.mapper.SysMapper;
import org.example.mapper.TokenBlacklistMapper;
import org.example.utils.FileUploadUtil;
import org.example.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * 系统服务类：处理用户管理、登录认证、文件上传等系统功能
 */
@Service
public class SysService {

    @Autowired
    private SysMapper sysMapper;
    @Autowired
    private BizMapper bizMapper;

    /**
     * 获取所有用户
     * @return 用户列表
     */
    public List<SysUser> getAllUsers() {
        return sysMapper.getAllUsers();
    }

    /**
     * 获取部门信息（用于前端根据 deptId 查询 leaderId 等信息）
     * @param deptId 部门ID
     * @return 部门对象
     */
    public SysDept getDeptById(Long deptId) {
        if (deptId == null) {
            throw new RuntimeException("deptId为空");
        }
        SysDept dept = sysMapper.getDeptById(deptId);
        if (dept == null) {
            throw new RuntimeException("部门不存在");
        }
        return dept;
    }

    /**
     * 用户登录
     * 改成了user_id
     * @param userId 用户ID
     * @param password 密码
     * @return 登录视图对象
     */
    public SysLoginVO login(Long userId, String password) {
        SysUser user = sysMapper.getUserById(userId);
        if (user != null && user.getPassword().equals(password)) {
            SysLoginVO sysLoginVo = new SysLoginVO(user.getNickName(), JWTUtil.generateJwtToken(user));
            return sysLoginVo;
        } else if (user == null){
            throw new RuntimeException("用户不存在");
        } else if (!user.getPassword().equals(password)){
            throw new RuntimeException("密码错误");
        }
        return null;
    }

    /**
     * 修改密码
     * @param userId 用户ID
     * @param newPassword 新密码
     */
    public void changePassword(Long userId, String newPassword) {
        SysUser user = sysMapper.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPassword(newPassword);
        sysMapper.updateUser(user);
    }

    @Autowired
    private TokenBlacklistMapper tokenBlacklistMapper;

    /**
     * 用户注销
     * @param token Token字符串
     */
    public void logout(String token) {
        try{
            tokenBlacklistMapper.addToBlacklist(new TokenBlacklist(token, JWTUtil.verifyJwtToken(token).getExpiresAt()));
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 上传文件
     * @param file 文件对象
     * @param taskId 任务ID
     * @param request HTTP请求
     * @return 文件上传视图对象
     */
    public Object uploadFile(MultipartFile file, Long taskId,HttpServletRequest request) {
        try{
            if (file.isEmpty()) {
                throw new RuntimeException("文件不能为空");
            }
            // 如果文件后缀不是doc,docx,pdf中的一个，报错
            if (!file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1).matches("doc|docx|pdf")) {
                throw new RuntimeException("文件格式错误");
            }
            FileUploadDTO fileUploadDTO = FileUploadUtil.upload(file);
            SysFile sysFile = new SysFile();
            sysFile.setFilePath(fileUploadDTO.getFilepath());
            sysFile.setFileName(fileUploadDTO.getFilename());
            sysFile.setFileUrl(fileUploadDTO.getFilepath());
            sysFile.setFileSuffix(file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1));
            sysFile.setFileSize(file.getSize());
            Long userId = JWTUtil.getUserIdFromToken(request.getHeader("Authorization"));
            sysFile.setUploadBy(userId);
            sysFile.setUploadTime(new Date());
            System.out.println(sysFile);
            sysMapper.uploadFile(sysFile);
            return new FileUploadVO(sysMapper.getFileByName(sysFile.getFileName()).getFileId(), fileUploadDTO.getFilename(), fileUploadDTO.getFilepath());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据file_id下载文件
     * @param fileId 文件ID
     * @param response HTTP响应
     * @throws IOException IO异常
     */
    public void downloadFile(Long fileId, HttpServletResponse response) throws IOException {
        // 1. 查询文件信息
        SysFile sysFile = sysMapper.getFileById(fileId);
        if (sysFile == null) {
            throw new RuntimeException("文件不存在");
        }

        // 2. 构建文件路径
        String fullPath = System.getProperty("user.dir") + sysFile.getFilePath();
        File file = new File(fullPath);
        if (!file.exists()) {
            throw new RuntimeException("文件已被删除或移动");
        }

        // 3. 设置响应头
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + URLEncoder.encode(sysFile.getFileName(), StandardCharsets.UTF_8.name()) + "\"");
        response.setContentLengthLong(file.length());

        // 4. 写入文件流
        try (InputStream in = new FileInputStream(file);
             OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
        }
    }

    /**
     * 发送消息
     * @param sysNoticeDTO 通知数据
     * @param userId 用户ID
     */
    public void sendNotice(SysNoticeDTO sysNoticeDTO, Long userId) {
        try{
            SysNotice sysNotice = new SysNotice();
            sysNotice.setFromUserId(userId);
            sysNotice.setToUserId(sysNoticeDTO.getTo_user_id());
            sysNotice.setType(sysNoticeDTO.getType());
            sysNotice.setTriggerEvent(sysNoticeDTO.getTrigger_event());
            sysNotice.setTitle(sysNoticeDTO.getTitle());
            sysNotice.setContent(sysNoticeDTO.getContent());
            sysNotice.setSourceType("1");
            sysNotice.setSourceId(sysNoticeDTO.getSource_id());
            sysNotice.setIsRead("0");
            sysMapper.sendNotice(sysNotice);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 查看当前用户收到的通知
     * @param userId 用户ID
     * @return 通知列表
     */
    public List<SysNotice> getNotices(Long userId) {
        try{
            return sysMapper.getNotices(userId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 设为已读setRead
     * @param noticeId 通知ID
     */
    public void setRead(Long noticeId) {
        try{
            sysMapper.setRead(noticeId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 站内预警
     * @param sysAlertDTO 预警数据
     * @param userId 用户ID
     */
    public void sendAlert(SysAlertDTO sysAlertDTO, Long userId) {
        try{
            SysNotice sysNotice = new SysNotice();
            sysNotice.setFromUserId(userId);
            sysNotice.setToUserId(sysMapper.getUserByNickName(sysAlertDTO.getTo_user_nick_name()).getUserId());
            sysNotice.setType("1");
            sysNotice.setTriggerEvent("1");
            sysNotice.setTitle(sysAlertDTO.getTitle());
            sysNotice.setContent(sysAlertDTO.getContent());
            sysNotice.setSourceType("1");
            sysNotice.setSourceId(sysAlertDTO.getSource_id());
            sysNotice.setIsRead("0");
            sysMapper.sendNotice(sysNotice);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据ID获取用户
     * @param userId 用户ID
     * @return 用户对象
     */
    public SysUser getUserById(Long userId) {
        return sysMapper.getUserById(userId);
    }

    /**
     * 根据用户名获取用户
     * @param userName 用户名
     * @return 用户对象
     */
    public SysUser getUserByName(String userName) {
        return sysMapper.getUserByName(userName);
    }

    /**
     * 添加用户
     * @param user 用户数据传输对象
     */
    public void addUser(SysUserDTO user) {
        // id已存在，报错
        if(sysMapper.getUserById(user.getUserId()) != null){
            throw new RuntimeException("用户id已存在");
        }
        if(sysMapper.getUserByName(user.getUserName()) != null){
            throw new RuntimeException("用户名已存在，请添加文字进行区分");
        }
        // 验证deptId是否存在
        if(sysMapper.getDeptById(user.getDeptId()) == null){
            throw new RuntimeException("部门不存在");
        }

        sysMapper.addUser(userDTO2User(user));
    }

    /**
     * 更新用户
     * @param user 用户数据传输对象
     */
    public void updateUser(SysUserDTO  user) {
        if(sysMapper.getUserByName(user.getUserName()) != null){
            throw new RuntimeException("用户名已存在，请添加文字进行区分");
        }
        if(sysMapper.getDeptById(user.getDeptId()) == null){
            throw new RuntimeException("部门不存在");
        }

        sysMapper.updateUser(userDTO2User(user));
    }

    /**
     * 删除用户
     * @param userId 用户ID
     */
    public void deleteUser(Long userId) {
        if(sysMapper.getUserById(userId) == null){
            throw new RuntimeException("用户不存在");
        }

        if(!bizMapper.getTasks(userId).isEmpty()){
            throw new RuntimeException("该用户名下有任务，请先修改任务负责人");
        }
        sysMapper.deleteUser(userId);
    }

    /**
     * UserDTO转User转换方法
     * @param user 用户数据传输对象
     * @return 用户实体
     */
    public SysUser userDTO2User(SysUserDTO user) {
        SysUser newUser = new SysUser();
        newUser.setUserId(user.getUserId());
        newUser.setDeptId(user.getDeptId());
        newUser.setUserName(user.getUserName());
        newUser.setNickName(user.getNickName());
        newUser.setEmail(user.getEmail());
        newUser.setPassword(user.getPassword());
        newUser.setRole(user.getRole());
        newUser.setCreateTime(new Date());
        newUser.setUpdateTime(new Date());
        newUser.setStatus("1");
        newUser.setIsDelete(0);
        return newUser;
    }
}
```

#### java\org\example\utils\FileUploadUtil.java

```java
package org.example.utils;

import org.example.entity.dto.FileUploadDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件上传工具类
 */
public class FileUploadUtil {
    /** 上传目录 */
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    /** 非法字符正则 */
    private static final Pattern INVALID_CHARS = Pattern.compile("[\\\\/:*?\"<>|]");

    /** 重复文件模式正则 */
    private static final Pattern DUPLICATE_PATTERN = Pattern.compile("(.+?)(?:\\((\\d+)\\))?(\\.[^.]*)?$");

    /**
     * 上传文件
     * @param file 文件对象
     * @return 文件上传数据传输对象
     * @throws IOException IO异常
     */
    public static FileUploadDTO upload(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }

        // 创建上传目录
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 获取并处理原始文件名
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.trim().isEmpty()) {
            throw new RuntimeException("文件名不能为空");
        }

        // 安全处理
        String safeFileName = Paths.get(originalName).getFileName().toString();
        safeFileName = INVALID_CHARS.matcher(safeFileName).replaceAll("");

        if (safeFileName.isEmpty()) {
            safeFileName = "uploaded_file";
        }

        // 生成唯一文件名
        String finalFileName = getUniqueFileName(uploadPath, safeFileName);

        // 保存文件
        Path filePath = uploadPath.resolve(finalFileName);
        Files.copy(file.getInputStream(), filePath);
        filePath.toFile().setReadable(true, false);

        return new FileUploadDTO(finalFileName, "/uploads/" + finalFileName);
    }

    /**
     * 智能生成唯一文件名
     * 示例：
     * file.txt -> file.txt (不存在时)
     * file.txt -> file(1).txt (file.txt已存在)
     * file(1).txt -> file(2).txt (file(1).txt已存在)
     * file(2).txt -> file(3).txt (file(2).txt已存在)
     * @param uploadPath 上传路径
     * @param fileName 文件名
     * @return 唯一文件名
     */
    private static String getUniqueFileName(Path uploadPath, String fileName) {
        // 解析文件名，提取基础名称、数字后缀和扩展名
        Matcher matcher = DUPLICATE_PATTERN.matcher(fileName);
        if (!matcher.matches()) {
            throw new RuntimeException("文件名格式不正确");
        }

        String baseName = matcher.group(1);      // 基础名称
        String numberStr = matcher.group(2);     // 数字后缀，可能为null
        String extension = matcher.group(3) != null ? matcher.group(3) : ""; // 扩展名

        int startNumber = 1;
        if (numberStr != null) {
            // 如果文件名已经有数字后缀，则从该数字+1开始
            try {
                startNumber = Integer.parseInt(numberStr) + 1;
                // 如果有数字后缀，baseName就是原始基础名
                // 例如：file(1).txt -> baseName = "file", numberStr = "1"
            } catch (NumberFormatException e) {
                // 如果数字格式错误，从1开始
                startNumber = 1;
            }
        }

        // 先检查原始文件名（没有后缀的情况）
        if (numberStr == null && !Files.exists(uploadPath.resolve(fileName))) {
            return fileName;
        }

        // 寻找下一个可用的数字后缀
        for (int i = startNumber; i <= 1000; i++) {
            String newFileName;
            if (i == 1) {
                // 第一次添加后缀
                newFileName = String.format("%s(%d)%s", baseName, i, extension);
            } else {
                // 更新数字后缀
                newFileName = String.format("%s(%d)%s", baseName, i, extension);
            }

            if (!Files.exists(uploadPath.resolve(newFileName))) {
                return newFileName;
            }
        }

        throw new RuntimeException("文件名冲突过多，请重命名文件后上传");
    }
}
```

#### java\org\example\utils\JWTUtil.java

```java
package org.example.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.example.entity.SysUser;
import org.example.entity.TokenBlacklist;
import org.example.mapper.TokenBlacklistMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * JWT工具类：生成、验证和解析Token
 */
public class JWTUtil {
    /** 密钥，与生成 Token 时使用的密钥一致 */
    private static final String SECRET_KEY = "secretKey";

    /**
     * 生成jwt token
     * @param user 用户对象
     * @return Token字符串
     */
    public static String generateJwtToken(SysUser user) {
        long nowMillis = System.currentTimeMillis();
        long expMillis = nowMillis + 3600000; // 令牌有效期为 1 小时
        Date exp = new Date(expMillis);

        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        return JWT.create()
                .withClaim("id",user.getUserId())
                .withClaim("username", user.getUserName())
                .withClaim("role", user.getRole())
                .withIssuer("auth0")
                .withExpiresAt(exp)
                .sign(algorithm);
    }

    /**
     * 验证jwt token
     * @param token Token字符串
     * @return 解码后的JWT对象
     */
    public static DecodedJWT verifyJwtToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("auth0")
                    .build();
            return verifier.verify(token);
        } catch (JWTVerificationException exception) {
            // Invalid signature/claims
            throw new RuntimeException("Invalid token: " + exception.getMessage());
        }
    }

    /**
     * 从Token获取用户ID
     * @param token Token字符串
     * @return 用户ID
     */
    public static Long getUserIdFromToken(String token) {
        DecodedJWT decodedJWT = verifyJwtToken(token);
        return decodedJWT.getClaim("id").asLong();
    }

    /**
     * 从Token获取角色
     * @param token Token字符串
     * @return 角色字符串
     */
    public static String getRoleFromToken(String token) {
        DecodedJWT decodedJWT = verifyJwtToken(token);
        return decodedJWT.getClaim("role").asString();
    }

    /**
     * 检查Token是否过期
     * @param token Token字符串
     * @return 是否过期
     */
    public static boolean isTokenExpired(String token) {
        DecodedJWT decodedJWT = verifyJwtToken(token);
        Date expiresAt = decodedJWT.getExpiresAt();
        return expiresAt.before(new Date());
    }

    /**
     * 检查Token是否在黑名单中
     * @param token Token字符串
     * @return 是否在黑名单中
     */
    public static boolean isTokenInBlacklist(String token) {
        // TODO: 添加黑名单检查逻辑
        return false;
    }
}
```

#### resources\application.properties

```text
# ?????
spring.datasource.url=jdbc:mysql://localhost:3306/biz
spring.datasource.username=root
spring.datasource.password=981119

# ??????
spring.web.resources.static-locations=file:${user.dir}/uploads/
spring.mvc.static-path-pattern=/uploads/**

# MyBatis??
mybatis.type-aliases-package=org.example.entity
mybatis.configuration.map-underscore-to-camel-case=true

# ??????
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB

# ??????
spring.task.scheduling.pool.size=5
spring.task.scheduling.thread-name-prefix=scheduled-task-

# ???????????true?
schedule.enabled=true

# ????????
schedule.monthly-reminder.enabled=true
schedule.monthly-reminder.cron=0 0 9 1 * ?
schedule.token-cleanup.enabled=true
schedule.token-cleanup.cron=0 0 2 * * ?
```

