package org.example.service;

import org.example.entity.SysDept;
import org.example.entity.SysUser;
import org.example.mapper.SysMapper;
import org.example.utils.JWTUtil;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AchievementEvidenceTest {
    private final SysMapper mapper = mock(SysMapper.class);
    private final SysService files = mock(SysService.class);
    private final PermissionService permissions = mock(PermissionService.class);
    private final AchievementService service = new AchievementService();

    AchievementEvidenceTest() {
        ReflectionTestUtils.setField(service, "sysMapper", mapper);
        ReflectionTestUtils.setField(service, "sysService", files);
        ReflectionTestUtils.setField(service, "permissions", permissions);
    }

    private MockHttpServletRequest request(long id) {
        SysUser user = new SysUser();
        user.setUserId(id);
        user.setUserName("test" + id);
        user.setRole("1");
        user.setDeptId(100L);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", JWTUtil.generateJwtToken(user));
        return request;
    }

    @Test
    void departmentReporterCanUploadWithoutTaskId() {
        SysUser user = new SysUser(); user.setUserId(990100L); user.setDeptId(100L);
        SysDept dept = new SysDept(); dept.setDeptId(100L);
        when(mapper.getUserById(990100L)).thenReturn(user);
        when(mapper.getDeptById(100L)).thenReturn(dept);
        var request = request(990100L);
        var file = new MockMultipartFile("file", "proof.pdf", "application/pdf", new byte[]{1, 2});
        when(files.uploadFile(file, 0L, request)).thenReturn("stored");
        assertEquals("stored", service.uploadEvidence(file, request));
        verify(files).uploadFile(file, 0L, request);
    }

    @Test
    void unrelatedUserAndInvalidFilesAreRejectedBeforeStorage() {
        var file = new MockMultipartFile("file", "proof.pdf", "application/pdf", new byte[]{1});
        assertThrows(AdminManagementException.class, () -> service.uploadEvidence(file, request(121362L)));
        SysUser user = new SysUser(); user.setUserId(990100L); user.setDeptId(100L);
        SysDept dept = new SysDept(); dept.setDeptId(100L);
        when(mapper.getUserById(990100L)).thenReturn(user);
        when(mapper.getDeptById(100L)).thenReturn(dept);
        assertThrows(AdminManagementException.class, () -> service.uploadEvidence(
                new MockMultipartFile("file", "proof.exe", "application/octet-stream", new byte[]{1}), request(990100L)));
        assertThrows(AdminManagementException.class, () -> service.uploadEvidence(
                new MockMultipartFile("file", "proof.pdf", "application/pdf", new byte[10 * 1024 * 1024 + 1]), request(990100L)));
        verifyNoInteractions(files);
    }
}
