package org.example.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import org.example.entity.SysUser;
import org.example.entity.dto.SysLoginDTO;
import org.example.entity.dto.SysPwdDTO;
import org.example.entity.vo.ErrorVO;
import org.example.entity.vo.SysLogoutVO;
import org.example.service.SysService;
import org.example.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/system")
public class SysController {

    @Autowired
    private SysService sysService;

    @GetMapping("/getAllUsers")
    public List<SysUser> getAllUsers()
    {
        return sysService.getAllUsers();
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



}
