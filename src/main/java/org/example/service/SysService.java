package org.example.service;


import org.example.entity.SysUser;
import org.example.entity.TokenBlacklist;
import org.example.entity.vo.SysLoginVO;
import org.example.mapper.SysMapper;
import org.example.mapper.TokenBlacklistMapper;
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
    public SysLoginVO login(String userName, String password) {
        SysUser user = sysMapper.getUserByName(userName);
//        System.out.println(user);
        if (user != null && user.getPassword().equals(password)) {
//            System.out.println(user);
            SysLoginVO sysLoginVo = new SysLoginVO(user.getNickName(), JWTUtil.generateJwtToken(user));
            return sysLoginVo;
        } else if (user == null){
            throw new RuntimeException("用户不存在");
        } else if (!user.getPassword().equals(password)){
            throw new RuntimeException("密码错误");
        }
        return null;
    }

//    修改密码
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
//    退出登录
    public void logout(String token) {
        try{
            tokenBlacklistMapper.addToBlacklist(new TokenBlacklist(token, JWTUtil.verifyJwtToken(token).getExpiresAt()));
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }


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
