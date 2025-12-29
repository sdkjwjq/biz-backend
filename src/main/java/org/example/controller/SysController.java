package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.entity.SysNotice;
import org.example.entity.SysUser;
import org.example.entity.dto.SysAlertDTO;
import org.example.entity.dto.SysLoginDTO;
import org.example.entity.dto.SysNoticeDTO;
import org.example.entity.dto.SysPwdDTO;
import org.example.entity.vo.ErrorVO;
import org.example.entity.vo.SysLogoutVO;
import org.example.service.SysService;
import org.example.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/system")
public class SysController {

    @Autowired
    private SysService sysService;

    @GetMapping("/users")
    public List<SysUser> getAllUsers()
    {
        return sysService.getAllUsers();
    }

    // 根据部门ID获取部门信息（含 leaderId）
    @GetMapping("/dept/{deptId}")
    public Object getDeptById(@PathVariable("deptId") Long deptId) {
        try {
            return sysService.getDeptById(deptId);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    @PostMapping("/login")
    public Object login(@RequestBody SysLoginDTO sysLoginDTO){
        try{
            return sysService.login(sysLoginDTO.getUser_name(), sysLoginDTO.getPassword());
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

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
    //    上传文件
    @PostMapping("/upload")
    public Object uploadFile(@RequestParam("file") MultipartFile file, HttpServletRequest request){
        try{
            return sysService.uploadFile(file,request);
        }catch (Exception e){
            return new ErrorVO(e.getMessage(), 500);
        }
    }

//    根据文件名下载文件
//    @GetMapping("/download")
//    public void downloadFile(@RequestParam("filename") String fileName, HttpServletResponse response){
//        try{
//            sysService.downloadFile(fileName,response);
//        }catch (Exception e){
////            return new ErrorVO(e.getMessage(), 500);
//        }
//    }
//    根据file_id下载文件
    @GetMapping("/download/{file_id}")
    public Object downloadFile(@PathVariable("file_id") Long fileId, HttpServletResponse response){
        try{
            sysService.downloadFile(fileId,response);
            return "下载成功";
        }catch (Exception e){
            return new ErrorVO(e.getMessage(), 500);
        }
    }


//    发送通知
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
//    查看当前用户收到的通知
    @GetMapping("/notice")
    public List<SysNotice> getNotices(HttpServletRequest request) {
        try{
            Long userId=JWTUtil.getUserIdFromToken(request.getHeader("Authorization"));
            return sysService.getNotices(userId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
//    设为已读
    @PostMapping("/notice/{notice_id}")
    public Object setRead(@PathVariable("notice_id") Long noticeId){
        try{
            sysService.setRead(noticeId);
            return "已读";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }
//    站内信息 预警
//input token to_user_nick_name title/content source_id
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
