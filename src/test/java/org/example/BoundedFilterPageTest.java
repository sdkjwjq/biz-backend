package org.example;
import org.example.service.BoundedFilterPage;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.*;
class BoundedFilterPageTest {
 @Test void exactStreamingClampsLastPageAndBoundsRetainedMemory()throws Exception{
  var page=new BoundedFilterPage<Integer>(90000,37);for(int i=0;i<100003;i++)page.accept(i);
  var result=page.result();assertEquals(100003,result.total());assertEquals(2703,result.page());assertEquals(IntStream.range(99974,100003).boxed().toList(),result.items());
  for(String name:List.of("requestedItems","tail")){var field=page.getClass().getDeclaredField(name);field.setAccessible(true);assertTrue(((Collection<?>)field.get(page)).size()<=37);}
 }
 @Test void wholeCandidateSetIsMatchedBeforePaging(){var result=BoundedFilterPage.scan(2,7,offset->IntStream.range(offset,Math.min(1005,offset+500)).boxed().toList(),value->value%100==0);assertEquals(11,result.total());assertEquals(List.of(700,800,900,1000),result.items());}
 @Test void rejectsUnboundedProducer(){assertThrows(IllegalStateException.class,()->BoundedFilterPage.scan(1,10,offset->IntStream.range(0,501).boxed().toList(),value->true));}
}
