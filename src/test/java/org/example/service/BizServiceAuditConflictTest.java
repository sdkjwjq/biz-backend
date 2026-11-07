package org.example.service;

import org.example.entity.BizMaterialSubmission;
import org.example.entity.dto.BizAuditDTO;
import org.example.mapper.BizMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BizServiceAuditConflictTest {
    @Test
    void rejectedSubmissionCannotBeApprovedAgainAndReportsConflict() {
        BizMapper mapper = mock(BizMapper.class);
        BizMaterialSubmission rejected = new BizMaterialSubmission();
        rejected.setSubId(180L);
        rejected.setIsDelete(1);
        rejected.setFlowStatus(-10);
        when(mapper.getAuditBySubIdIncludingDeletedForUpdate(180L)).thenReturn(rejected);

        BizService service = new BizService();
        ReflectionTestUtils.setField(service, "bizMapper", mapper);
        BizAuditDTO decision = new BizAuditDTO();
        decision.setSub_id(180L);
        decision.setIs_pass(true);

        AdminManagementException error = assertThrows(AdminManagementException.class,
                () -> service.audit(decision, 110228L));
        assertEquals(409, error.getCode());
        assertEquals("该审批单已被处理，请刷新列表后查看审核记录", error.getMessage());
    }

    @Test
    void archivedSubmissionCannotBeApprovedAgainAndReportsConflict() {
        BizMapper mapper = mock(BizMapper.class);
        BizMaterialSubmission archived = new BizMaterialSubmission();
        archived.setSubId(181L);
        archived.setIsDelete(0);
        archived.setFlowStatus(40);
        when(mapper.getAuditBySubIdIncludingDeletedForUpdate(181L)).thenReturn(archived);

        BizService service = new BizService();
        ReflectionTestUtils.setField(service, "bizMapper", mapper);
        BizAuditDTO decision = new BizAuditDTO();
        decision.setSub_id(181L);
        decision.setIs_pass(true);

        assertEquals(409, assertThrows(AdminManagementException.class,
                () -> service.audit(decision, 110228L)).getCode());
    }
}
