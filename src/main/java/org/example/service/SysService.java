package org.example.service;


import org.example.entity.SysUser;
import org.example.entity.vo.SysLoginVo;
import org.example.mapper.SysMapper;
import org.example.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysService {

    @Autowired
    private SysMapper sysMapper;
//    获取所有用户
    public List<SysUser> getAllUsers() {
        return sysMapper.getAllUsers();
    }
//      登录
    public SysLoginVo login(String userName, String password) {
        SysUser user = sysMapper.getUserByName(userName);
        if (user != null && user.getPassword().equals(password)) {
            SysLoginVo sysLoginVo = new SysLoginVo(user.getNickName(), JWTUtil.generateJwtToken(user));
            return sysLoginVo;
        } else if (user == null){
            throw new RuntimeException("用户不存在");
        } else if (!user.getPassword().equals(password)){
            throw new RuntimeException("密码错误");
        }
        return null;
    }



    public SysUser getUserById(Long userId) {
        return sysMapper.getUserById(userId);
    }

    public SysUser getUserByName(String userName) {
        return sysMapper.getUserByName(userName);
    }

    public int addUser(SysUser user) {
        return sysMapper.addUser(user);
    }

    public int updateUser(SysUser user) {
        return sysMapper.updateUser(user);
    }

    public int deleteUser(Long userId) {
        return sysMapper.deleteUser(userId);
    }
}
