package org.example.controller;

import org.example.entity.SysUser;
import org.example.entity.dto.SysLoginDTO;
import org.example.entity.vo.ErrorVo;
import org.example.entity.vo.SysLoginVo;
import org.example.service.SysService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
            return sysService.login(sysLoginDTO.getUserName(), sysLoginDTO.getPassword());
        } catch (Exception e) {
            return new ErrorVo(e.getMessage(), 500);
        }
    }


}
