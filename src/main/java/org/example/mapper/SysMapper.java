package org.example.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Options;
import org.example.entity.SysFile;
import org.example.entity.SysNotice;
import org.example.entity.SysUser;

import java.util.List;

@Mapper
public interface SysMapper {
//    获取所有用户
    @Select("SELECT * FROM sys_user")
    public List<SysUser> getAllUsers();
//  根据id获取部门
    @Select("SELECT * FROM sys_dept WHERE dept_id = #{deptId}")
    public List<SysUser> getDeptById(Long deptId);

//    根据userId获取 部门
    @Select("SELECT * FROM sys_dept WHERE dept_id = (SELECT dept_id FROM sys_user WHERE user_id = #{userId})")
    public List<SysUser> getDeptByUserId(Long userId);

//    根据userId获取部门负责人id
    @Select("SELECT leader_id FROM sys_dept WHERE dept_id = (SELECT dept_id FROM sys_user WHERE user_id = #{userId})")
    public Long getDeptLeaderId(Long userId);

//    根据id获取用户
    @Select("SELECT * FROM sys_user WHERE user_id = #{userId}")
    public SysUser getUserById(Long userId);

//    根据用户名获取用户
    @Select("SELECT * FROM sys_user WHERE user_name = #{userName}")
    public SysUser getUserByName(String userName);
//  根据nick_name获取用户
    @Select("SELECT * FROM sys_user WHERE nick_name = #{nickName}")
    public SysUser getUserByNickName(String nickName);
//    添加用户
    @Insert("INSERT INTO sys_user (user_name, nick_name, email, password, role, status, is_delete, create_time, update_time) VALUES (#{userName}, #{nickName}, #{email}, #{password}, #{role}, #{status}, #{isDelete}, #{createTime}, #{updateTime})")
    public void addUser(SysUser user);

//    修改用户信息
    @Update("UPDATE sys_user SET user_name = #{userName}, nick_name = #{nickName}, email = #{email}, password = #{password}, role = #{role}, status = #{status}, is_delete = #{isDelete}, create_time = #{createTime}, update_time = #{updateTime} WHERE user_id = #{userId}")
    public void updateUser(SysUser user);

//    删除用户
    @Update("UPDATE sys_user SET is_delete = 1 WHERE user_id = #{userId}")
    public void deleteUser(Long userId);

    //    上传文件
    @Insert("INSERT INTO sys_file (file_name, file_path, file_url, file_suffix, file_size, upload_by, is_delete, upload_time) VALUES (#{fileName}, #{filePath}, #{fileUrl}, #{fileSuffix}, #{fileSize}, #{uploadBy}, #{isDelete}, #{uploadTime})")
    @Options(useGeneratedKeys = true, keyProperty = "fileId", keyColumn = "file_id")
    public Long uploadFile(SysFile file);

//    根据名称查询文件
    @Select("SELECT * FROM sys_file WHERE file_name = #{fileName}")
    public SysFile getFileByName(String fileName);

    @Select("SELECT * FROM sys_file WHERE file_id = #{fileId}")
    public SysFile getFileById(Long fileId);

//    发送通知
    @Insert("INSERT INTO sys_notice (from_user_id, to_user_id, type, trigger_event, title, content, source_type, source_id, is_read, is_delete, create_time) VALUES (#{fromUserId}, #{toUserId}, #{type}, #{triggerEvent}, #{title}, #{content}, #{sourceType}, #{sourceId}, #{isRead}, #{isDelete}, #{createTime})")
    public void sendNotice(SysNotice notice);

//    查看当前用户收到的信息
    @Select("SELECT * FROM sys_notice WHERE to_user_id = #{userId}")
    public List<SysNotice> getNotices(Long userId);

//    getNoticeById
    @Select("SELECT * FROM sys_notice WHERE notice_id = #{noticeId}")
    public SysNotice getNoticeById(Long noticeId);

//    setRead
    @Update("UPDATE sys_notice SET is_read = 1 WHERE notice_id = #{noticeId}")
    public void setRead(Long noticeId);

}
