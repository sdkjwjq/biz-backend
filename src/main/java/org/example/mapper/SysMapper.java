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