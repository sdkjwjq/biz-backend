package org.example.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.SelectProvider;
import org.example.entity.*;

import java.util.List;

/**
 * 系统数据访问接口
 */
@Mapper
public interface SysMapper {
    /** 二进制比较保留密码大小写及尾部空格；仅修改密码和更新时间。 */
    @Update("UPDATE sys_user SET password = #{newPassword}, force_password_change = 0, update_time = NOW() "
            + "WHERE user_id = #{userId} AND (BINARY password <=> BINARY #{oldPassword}) "
            + "AND (is_delete = 0 OR is_delete IS NULL)")
    int resetPassword(@Param("userId") Long userId, @Param("oldPassword") String oldPassword,
                      @Param("newPassword") String newPassword);

    /**
     * 获取所有用户
     * @return 用户列表
     */
    @Select("SELECT user_id, dept_id, user_name, nick_name, email, role, status, is_delete, create_time, update_time FROM sys_user")
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
     * 获取某用户作为部门负责人的部门ID列表
     * @param leaderId 部门负责人ID
     * @return 部门ID列表
     */
    @Select("SELECT dept_id FROM sys_dept WHERE leader_id = #{leaderId} AND is_delete = 0")
    public List<Long> getDeptIdsByLeaderId(Long leaderId);

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

    @Select("SELECT * FROM sys_user WHERE nick_name = #{nickName} AND (is_delete = 0 OR is_delete IS NULL)")
    List<SysUser> getActiveUsersByNickName(String nickName);

    /**
     * 添加用户
     * userId手动添加而非自增
     * @param user 用户实体
     * @return 用户ID
     */
    @Insert("INSERT INTO sys_user (user_id, dept_id, user_name, nick_name, email, password, role, status, is_delete, create_time, update_time, force_password_change) VALUES (#{userId}, #{deptId}, #{userName}, #{nickName}, #{email}, #{password}, #{role}, #{status}, #{isDelete}, #{createTime}, #{updateTime}, 1)")
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
    @SelectProvider(type = ReviewNoticeSql.class, method = "visible")
    public List<SysNotice> getNotices(@Param("userId") Long userId);

    @SelectProvider(type = ReviewNoticeSql.class, method = "retireTask")
    List<Long> staleTaskNotices(@Param("subId") Long subId);

    @SelectProvider(type = ReviewNoticeSql.class, method = "retirePerformance")
    List<Long> stalePerformanceNotices(@Param("subId") Long subId);

    @SelectProvider(type = ReviewNoticeSql.class, method = "retireAchievement")
    List<Long> staleAchievementNotices(@Param("subId") Long subId);

    @Update("<script>UPDATE sys_notice SET is_delete=1 WHERE COALESCE(is_delete,0)=0 AND notice_id IN "
            + "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    int retireNoticeIds(@Param("ids") List<Long> ids);

    default void retireTaskNotices(Long subId) { retireIfPresent(staleTaskNotices(subId)); }
    default void retirePerformanceNotices(Long subId) { retireIfPresent(stalePerformanceNotices(subId)); }
    default void retireAchievementNotices(Long subId) { retireIfPresent(staleAchievementNotices(subId)); }
    default void retireIfPresent(List<Long> ids) {
        if (!ids.isEmpty()) retireNoticeIds(ids);
    }

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
    @Update("UPDATE sys_notice SET is_read = 1 WHERE notice_id = #{noticeId} AND to_user_id = #{userId} AND is_delete = 0")
    public int setRead(@Param("noticeId") Long noticeId, @Param("userId") Long userId);
}
