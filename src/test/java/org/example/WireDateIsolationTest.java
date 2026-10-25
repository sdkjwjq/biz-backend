package org.example;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.service.BusinessListFilterService;
import org.junit.jupiter.api.Test;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;
class WireDateIsolationTest {
 public static class Bean {
  private final Date value;Bean(Date value){this.value=value;}
  public Date getCreatedAt(){return value;}
  public java.io.Serializable getGenericDate(){return value;}
  @JsonIgnore public Date getIgnoredDate(){throw new AssertionError("Ignored getter must not be invoked");}
 }
 @Test void reflectionMetadataNeverCachesOneObjectsValues(){
  var service=new BusinessListFilterService(null,null,null,null,null,null,null,new ObjectMapper());Date first=new Date(0),second=new Date(86400123);
  var a=service.wireRow(new Bean(first));var b=service.wireRow(new Bean(second));assertSame(first,a.get("createdAt"));assertSame(second,b.get("createdAt"));assertSame(second,b.get("genericDate"));assertFalse(b.containsKey("ignoredDate"));
 }
}
