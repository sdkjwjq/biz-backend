package org.example;

import org.example.entity.*;
import org.example.entity.vo.WorkRecordVO.Task;
import org.example.mapper.*;
import org.example.service.WorkRecordScopeService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorkRecordEligibilityTest {
    private SysUser user(long id,Long department){var u=new SysUser();u.setUserId(id);u.setDeptId(department);u.setStatus("1");u.setIsDelete(0);return u;}
    private BizTask node(long id,long parent,int level,Long department){var n=new BizTask();n.setTaskId(id);n.setParentId(parent);n.setLevel(level);n.setProjectId(1L);n.setDeptId(department);return n;}
    private Task task(long id,int year,Long department,Long leader){var t=new Task();t.setTaskId(id);t.setParentId(2L);t.setProjectId(1L);t.setPhase(year);t.setDeptId(department);t.setLeaderId(leader);return t;}
    private List<Long> ids(List<SysUser> users){return users.stream().map(SysUser::getUserId).toList();}
    private List<SysUser> original(WorkRecordScopeService service,List<SysUser> users,int year){
        Object catalog=ReflectionTestUtils.invokeMethod(service,"catalog");var departments=new HashMap<Long,SysDept>();List<SysUser> expected=new ArrayList<>();
        for(var u:users)if("1".equals(u.getStatus())&&!Integer.valueOf(1).equals(u.getIsDelete())){
            WorkRecordScopeService.Scope scope=ReflectionTestUtils.invokeMethod(service,"resolve",u,year,catalog,departments,false);
            if(!scope.tasks().isEmpty())expected.add(u);
        }
        return expected;
    }
    @Test void eligibilityEqualsOriginalResolutionForDepartmentsGoalsAndInvalidStructure(){
        var records=mock(WorkRecordMapper.class);var users=mock(SysMapper.class);var service=new WorkRecordScopeService(records,users);
        when(records.structure()).thenReturn(List.of(node(1,0,1,7L),node(2,1,2,6L)));
        var first=task(3,2026,5L,11L);var second=task(4,2025,8L,12L);var invalid=task(5,2026,9L,13L);invalid.setProjectId(2L);
        when(records.scopeTasks()).thenReturn(List.of(first,second,invalid));
        var goal=new BizLevel4Task();goal.setParentId(3L);goal.setDeptId(4L);goal.setLeaderId(14L);goal.setPhase(null);
        var wrongYear=new BizLevel4Task();wrongYear.setParentId(3L);wrongYear.setDeptId(3L);wrongYear.setLeaderId(15L);wrongYear.setPhase(2025);
        when(records.scopeGoals()).thenReturn(List.of(goal,wrongYear));
        List<SysUser> accounts=new ArrayList<>();for(long id=11;id<=30;id++)accounts.add(user(id,id%10));accounts.add(user(31,null));
        accounts.get(5).setStatus("0");accounts.get(6).setIsDelete(1);accounts.get(7).setIsDelete(2);
        when(users.getAllUsers()).thenReturn(accounts);
        Map<Long,SysDept> departments=new HashMap<>();for(long id=1;id<=9;id++){var d=new SysDept();d.setDeptId(id);d.setStatus("0");d.setIsDelete(0);departments.put(id,d);}
        when(users.getDeptById(anyLong())).thenAnswer(i->departments.get(i.getArgument(0)));
        for(int pass=0;pass<3;pass++){
            for(int year:List.of(2025,2026,2029))assertEquals(ids(original(service,accounts,year)),ids(service.eligibleAuthors(year)),"year="+year+" pass="+pass);
            departments.get(5L).setStatus("1");departments.get(6L).setIsDelete(1);departments.remove(4L);
            goal.setPhase(2025);
        }
        // A professional remains qualified even if their own department is unavailable.
        assertTrue(ids(service.eligibleAuthors(2026)).contains(11L));assertFalse(ids(service.eligibleAuthors(2026)).contains(13L));
        clearInvocations(users);assertTrue(service.eligibleAuthors(2029).isEmpty());verifyNoInteractions(users);
    }
    @Test void eligibilityCalculationAtCurrentAndFiveTimesSyntheticScale(){
        var records=mock(WorkRecordMapper.class);var users=mock(SysMapper.class);var service=new WorkRecordScopeService(records,users);
        when(records.structure()).thenReturn(List.of(node(1,0,1,7L),node(2,1,2,6L)));
        var dept=new SysDept();dept.setDeptId(5L);dept.setStatus("0");when(users.getDeptById(anyLong())).thenReturn(dept);
        for(int scale:List.of(1,5)){
            List<SysUser> accounts=new ArrayList<>();for(int i=0;i<200*scale;i++)accounts.add(user(1000+i,5L));
            List<Task> tasks=new ArrayList<>();for(int i=0;i<120*scale;i++)tasks.add(task(3000+i,2026,5L,i%3==0?1000L:null));
            when(users.getAllUsers()).thenReturn(accounts);when(records.scopeTasks()).thenReturn(tasks);
            List<Long> before=new ArrayList<>(),after=new ArrayList<>();
            for(int run=0;run<6;run++){
                long start=System.nanoTime();var old=original(service,accounts,2026);long middle=System.nanoTime();var value=service.eligibleAuthors(2026);long end=System.nanoTime();
                assertEquals(ids(old),ids(value));if(run>0){before.add((middle-start)/1000);after.add((end-middle)/1000);}
            }
            Collections.sort(before);Collections.sort(after);
            System.out.printf("WORK_RECORD_ELIGIBILITY_CPU_BENCH {\"users\":%d,\"tasks\":%d,\"legacy_median_us\":%d,\"optimized_median_us\":%d,\"equalIdsAndOrder\":true}%n",accounts.size(),tasks.size(),before.get(2),after.get(2));
        }
    }
}
