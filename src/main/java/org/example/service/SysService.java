package org.example.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.entity.*;
import org.example.entity.dto.FileUploadDTO;
import org.example.entity.dto.SysAlertDTO;
import org.example.entity.dto.SysNoticeDTO;
import org.example.entity.dto.SysUserDTO;
import org.example.entity.vo.FileUploadVO;
import org.example.entity.vo.SysLoginVO;
import org.example.mapper.BizMapper;
import org.example.mapper.SysMapper;
import org.example.mapper.TokenBlacklistMapper;
import org.example.utils.FileUploadUtil;
import org.example.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * 系统服务类：处理用户管理、登录认证、文件上传等系统功能
 */
@Service
public class SysService {

    @Autowired
    private SysMapper sysMapper;
    @Autowired
    private BizMapper bizMapper;

    /**
     * 获取所有用户
     * @return 用户列表
     */
    public List<SysUser> getAllUsers() {
        return sysMapper.getAllUsers();
    }

    /**
     * 获取部门信息（用于前端根据 deptId 查询 leaderId 等信息）
     * @param deptId 部门ID
     * @return 部门对象
     */
    public SysDept getDeptById(Long deptId) {
        if (deptId == null) {
            throw new RuntimeException("deptId为空");
        }
        SysDept dept = sysMapper.getDeptById(deptId);
        if (dept == null) {
            throw new RuntimeException("部门不存在");
        }
        return dept;
    }

    /**
     * 用户登录
     * 改成了user_id
     * @param userId 用户ID
     * @param password 密码
     * @return 登录视图对象
     */
    public SysLoginVO login(Long userId, String password) {
        SysUser user = sysMapper.getUserById(userId);
        if (user != null && user.getPassword().equals(password)) {
            SysLoginVO sysLoginVo = new SysLoginVO(user.getNickName(), JWTUtil.generateJwtToken(user));
            return sysLoginVo;
        } else if (user == null){
            throw new RuntimeException("用户不存在");
        } else if (!user.getPassword().equals(password)){
            throw new RuntimeException("密码错误");
        }
        return null;
    }

    /**
     * 修改密码
     * @param userId 用户ID
     * @param newPassword 新密码
     */
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

    /**
     * 用户注销
     * @param token Token字符串
     */
    public void logout(String token) {
        try{
            tokenBlacklistMapper.addToBlacklist(new TokenBlacklist(token, JWTUtil.verifyJwtToken(token).getExpiresAt()));
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 上传文件
     * @param file 文件对象
     * @param taskId 任务ID
     * @param request HTTP请求
     * @return 文件上传视图对象
     */
    public Object uploadFile(MultipartFile file, Long taskId,HttpServletRequest request) {
        try{
            if (file.isEmpty()) {
                throw new RuntimeException("文件不能为空");
            }
            // 如果文件后缀不是doc,docx,pdf中的一个，报错
            if (!file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1).matches("doc|docx|pdf")) {
                throw new RuntimeException("文件格式错误");
            }
            FileUploadDTO fileUploadDTO = FileUploadUtil.upload(file);
            SysFile sysFile = new SysFile();
            sysFile.setFilePath(fileUploadDTO.getFilepath());
            sysFile.setFileName(fileUploadDTO.getFilename());
            sysFile.setFileUrl(fileUploadDTO.getFilepath());
            sysFile.setFileSuffix(file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1));
            sysFile.setFileSize(file.getSize());
            Long userId = JWTUtil.getUserIdFromToken(request.getHeader("Authorization"));
            sysFile.setUploadBy(userId);
            sysFile.setUploadTime(new Date());
            System.out.println(sysFile);
            sysMapper.uploadFile(sysFile);
            return new FileUploadVO(sysMapper.getFileByName(sysFile.getFileName()).getFileId(), fileUploadDTO.getFilename(), fileUploadDTO.getFilepath());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据file_id下载文件
     * @param fileId 文件ID
     * @param response HTTP响应
     * @throws IOException IO异常
     */
    public void downloadFile(Long fileId, HttpServletResponse response) throws IOException {
        // 1. 查询文件信息
        SysFile sysFile = sysMapper.getFileById(fileId);
        if (sysFile == null) {
            throw new RuntimeException("文件不存在");
        }

        // 2. 构建文件路径
        String fullPath = System.getProperty("user.dir") + sysFile.getFilePath();
        File file = new File(fullPath);
        if (!file.exists()) {
            throw new RuntimeException("文件已被删除或移动");
        }

        // 3. 设置响应头
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + URLEncoder.encode(sysFile.getFileName(), StandardCharsets.UTF_8.name()) + "\"");
        response.setContentLengthLong(file.length());

        // 4. 写入文件流
        try (InputStream in = new FileInputStream(file);
             OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
        }
    }

    /**
     * 发送消息
     * @param sysNoticeDTO 通知数据
     * @param userId 用户ID
     */
    public void sendNotice(SysNoticeDTO sysNoticeDTO, Long userId) {
        try{
            SysNotice sysNotice = new SysNotice();
            sysNotice.setFromUserId(userId);
            sysNotice.setToUserId(sysNoticeDTO.getTo_user_id());
            sysNotice.setType(sysNoticeDTO.getType());
            sysNotice.setTriggerEvent(sysNoticeDTO.getTrigger_event());
            sysNotice.setTitle(sysNoticeDTO.getTitle());
            sysNotice.setContent(sysNoticeDTO.getContent());
            sysNotice.setSourceType("1");
            sysNotice.setSourceId(sysNoticeDTO.getSource_id());
            sysNotice.setIsRead("0");
            sysMapper.sendNotice(sysNotice);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 查看当前用户收到的通知
     * @param userId 用户ID
     * @return 通知列表
     */
    public List<SysNotice> getNotices(Long userId) {
        try{
            return sysMapper.getNotices(userId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 设为已读setRead
     * @param noticeId 通知ID
     */
    public void setRead(Long noticeId) {
        try{
            sysMapper.setRead(noticeId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 站内预警
     * @param sysAlertDTO 预警数据
     * @param userId 用户ID
     */
    public void sendAlert(SysAlertDTO sysAlertDTO, Long userId) {
        try{
            SysNotice sysNotice = new SysNotice();
            sysNotice.setFromUserId(userId);
            sysNotice.setToUserId(sysMapper.getUserByNickName(sysAlertDTO.getTo_user_nick_name()).getUserId());
            sysNotice.setType("1");
            sysNotice.setTriggerEvent("1");
            sysNotice.setTitle(sysAlertDTO.getTitle());
            sysNotice.setContent(sysAlertDTO.getContent());
            sysNotice.setSourceType("1");
            sysNotice.setSourceId(sysAlertDTO.getSource_id());
            sysNotice.setIsRead("0");
            sysMapper.sendNotice(sysNotice);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据ID获取用户
     * @param userId 用户ID
     * @return 用户对象
     */
    public SysUser getUserById(Long userId) {
        return sysMapper.getUserById(userId);
    }

    /**
     * 根据用户名获取用户
     * @param userName 用户名
     * @return 用户对象
     */
    public SysUser getUserByName(String userName) {
        return sysMapper.getUserByName(userName);
    }

    /**
     * 添加用户
     * @param user 用户数据传输对象
     */
    public void addUser(SysUserDTO user) {
        // id已存在，报错
        if(sysMapper.getUserById(user.getUserId()) != null){
            throw new RuntimeException("用户id已存在");
        }
        if(sysMapper.getUserByName(user.getUserName()) != null){
            throw new RuntimeException("用户名已存在，请添加文字进行区分");
        }
        // 验证deptId是否存在
        if(sysMapper.getDeptById(user.getDeptId()) == null){
            throw new RuntimeException("部门不存在");
        }

        sysMapper.addUser(userDTO2User(user));
    }

    /**
     * 更新用户
     * @param user 用户数据传输对象
     */
    public void updateUser(SysUserDTO  user) {
        if(sysMapper.getUserByName(user.getUserName()) != null){
            throw new RuntimeException("用户名已存在，请添加文字进行区分");
        }
        if(sysMapper.getDeptById(user.getDeptId()) == null){
            throw new RuntimeException("部门不存在");
        }

        sysMapper.updateUser(userDTO2User(user));
    }

    /**
     * 删除用户
     * @param userId 用户ID
     */
    public void deleteUser(Long userId) {
        if(sysMapper.getUserById(userId) == null){
            throw new RuntimeException("用户不存在");
        }

        if(!bizMapper.getTasks(userId).isEmpty()){
            throw new RuntimeException("该用户名下有任务，请先修改任务负责人");
        }
        sysMapper.deleteUser(userId);
    }

    /**
     * UserDTO转User转换方法
     * @param user 用户数据传输对象
     * @return 用户实体
     */
    public SysUser userDTO2User(SysUserDTO user) {
        SysUser newUser = new SysUser();
        newUser.setUserId(user.getUserId());
        newUser.setDeptId(user.getDeptId());
        newUser.setUserName(user.getUserName());
        newUser.setNickName(user.getNickName());
        newUser.setEmail(user.getEmail());
        newUser.setPassword(user.getPassword());
        newUser.setRole(user.getRole());
        newUser.setCreateTime(new Date());
        newUser.setUpdateTime(new Date());
        newUser.setStatus("1");
        newUser.setIsDelete(0);
        return newUser;
    }
}