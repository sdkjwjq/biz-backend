package org.example.service;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.entity.SysFile;
import org.example.entity.SysNotice;
import org.example.entity.SysUser;
import org.example.entity.TokenBlacklist;
import org.example.entity.dto.FileUploadDTO;
import org.example.entity.dto.SysAlertDTO;
import org.example.entity.dto.SysNoticeDTO;
import org.example.entity.vo.FileUploadVO;
import org.example.entity.vo.SysLoginVO;
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

    //    上传文件
    public Object uploadFile(MultipartFile file, HttpServletRequest request) {
        try{
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
            sysMapper.uploadFile(sysFile);
            return new FileUploadVO(sysMapper.getFileByName(sysFile.getFileName()).getFileId(), fileUploadDTO.getFilename(), fileUploadDTO.getFilepath());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


//    根据file_id下载文件
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

//    发送消息
    public void sendNotice(SysNoticeDTO sysNoticeDTO, Long userId) {
        try{
            SysNotice sysNotice = new SysNotice();
            sysNotice.setFromUserId(userId);
            sysNotice.setToUserId(sysNoticeDTO.getTo_user_id());
            sysNotice.setType(sysNoticeDTO.getType());
            sysNotice.setTriggerEvent(sysNoticeDTO.getTrigger_event());
            sysNotice.setTitle(sysNoticeDTO.getTitle());
            sysNotice.setContent(sysNoticeDTO.getContent());
//            sysNotice.setSourceType(sysNoticeDTO.getSource_type());
            sysNotice.setSourceType("1");
            sysNotice.setSourceId(sysNoticeDTO.getSource_id());
            sysNotice.setIsRead("0");
            sysMapper.sendNotice(sysNotice);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    查看当前用户收到的通知
    public List<SysNotice> getNotices(Long userId) {
        try{
            return sysMapper.getNotices(userId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    设为已读setRead
    public void setRead(Long noticeId) {
        try{
            sysMapper.setRead(noticeId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

//    站内预警
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public SysUser getUserById(Long userId) {
        return sysMapper.getUserById(userId);
    }

    public SysUser getUserByName(String userName) {
        return sysMapper.getUserByName(userName);
    }

    public void addUser(SysUser user) {
        sysMapper.addUser(user);
    }

    public void updateUser(SysUser user) {
        sysMapper.updateUser(user);
    }

    public void deleteUser(Long userId) {
        sysMapper.deleteUser(userId);
    }
}
