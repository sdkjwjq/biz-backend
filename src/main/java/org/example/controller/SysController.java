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
    public Object deleteUser(@PathVariable("userId") Long userId) {
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