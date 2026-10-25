package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.SysNotice;
import org.example.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NoticeFilterProjectionTest {
    @Test void unrelatedNoticesKeepUnknownStateWithoutLookingUpNullOrQueryingBusinessRows(){
        var system=mock(SysService.class);var jdbc=mock(JdbcTemplate.class);
        var filters=new BusinessListFilterService(null,null,null,null,null,null,jdbc,new ObjectMapper());
        ReflectionTestUtils.setField(filters,"system",system);
        List<SysNotice> notices=new ArrayList<>();
        for(String source:Arrays.asList("0","9",null)){
            var notice=new SysNotice();notice.setNoticeId((long)notices.size()+1);notice.setSourceType(source);
            notice.setTitle("ordinary");notice.setContent("unrelated");notices.add(notice);
        }
        when(system.getNotices(1L)).thenReturn(notices);
        List<Map<String,Object>> rows=ReflectionTestUtils.invokeMethod(filters,"noticeRows",1L);
        assertNotNull(rows);assertEquals(List.of(1L,2L,3L),rows.stream().map(r->r.get("id")).toList());
        assertTrue(rows.stream().allMatch(r->r.get("pending").equals("unknown")));verifyNoInteractions(jdbc);
    }
}
