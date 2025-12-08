package org.example.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.entity.SysUser;

import java.util.List;

@Mapper
public interface SysMapper {
//    获取所有用户
    @Select("SELECT * FROM sys_user")
    public List<SysUser> getAllUsers();

//    根据id获取用户
    @Select("SELECT * FROM sys_user WHERE user_id = #{userId}")
    public SysUser getUserById(Long userId);

//    根据用户名获取用户
    @Select("SELECT * FROM sys_user WHERE user_name = #{userName}")
    public SysUser getUserByName(String userName);

//    添加用户
    @Insert("INSERT INTO sys_user (user_name, nick_name, email, password, role, status, is_delete, create_time, update_time) VALUES (#{userName}, #{nickName}, #{email}, #{password}, #{role}, #{status}, #{isDelete}, #{createTime}, #{updateTime})")
    public int addUser(SysUser user);

//    修改用户信息
    @Update("UPDATE sys_user SET user_name = #{userName}, nick_name = #{nickName}, email = #{email}, password = #{password}, role = #{role}, status = #{status}, is_delete = #{isDelete}, create_time = #{createTime}, update_time = #{updateTime} WHERE user_id = #{userId}")
    public int updateUser(SysUser user);

//    删除用户
    @Update("UPDATE sys_user SET is_delete = 1 WHERE user_id = #{userId}")
    public int deleteUser(Long userId);

}
