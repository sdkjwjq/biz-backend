package org.example;
import org.example.service.JdbcProjectionStream;
import org.apache.ibatis.type.DateTypeHandler;
import org.junit.jupiter.api.Test;
import java.sql.*;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class AuditProjectionDateTest {
 @Test void jdbcProjectionMatchesLegacyDateHandlerAtDayBoundaryAndForNull()throws Exception{
  ResultSet row=mock(ResultSet.class);
  for(String text:new String[]{"2026-09-01 00:00:00","2026-09-01 23:59:59.123"}){
   Timestamp timestamp=Timestamp.valueOf(text);when(row.getObject("submitTime")).thenReturn(LocalDateTime.parse(text.replace(' ','T')));when(row.getTimestamp("submitTime")).thenReturn(timestamp);
   var expected=new DateTypeHandler().getNullableResult(row,"submitTime");var actual=JdbcProjectionStream.legacyDate(row,"submitTime");assertEquals(expected,actual);assertEquals(java.util.Date.class,actual.getClass());
  }
  when(row.getTimestamp("submitTime")).thenReturn(null);assertNull(JdbcProjectionStream.legacyDate(row,"submitTime"));
 }
}
