package org.example.service;

import org.example.entity.BizTask;
import org.example.entity.vo.BizTaskVo;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.*;

/** Pure presentation assembly. Callers must resolve their business/round scope first. */
@Service
public class TaskReadProjectionService {
    private final JdbcTemplate db;
    public TaskReadProjectionService(JdbcTemplate db){this.db=db;}
    public List<BizTaskVo> project(Collection<BizTask> tasks,boolean displayNames){
        Set<Long> people=new LinkedHashSet<>(),departments=new LinkedHashSet<>();
        for(var task:tasks){for(Long id:Arrays.asList(task.getLeaderId(),task.getAuditorId(),task.getPrincipalId()))if(id!=null)people.add(id);if(task.getDeptId()!=null)departments.add(task.getDeptId());}
        Map<Long,String> names=new HashMap<>(),depts=new HashMap<>();
        for(var chunk:chunks(people))for(var row:db.queryForList("SELECT user_id,user_name,nick_name FROM sys_user WHERE user_id IN("+marks(chunk.size())+")",chunk.toArray()))names.put(((Number)row.get("user_id")).longValue(),Objects.toString(displayNames?Optional.ofNullable(row.get("nick_name")).orElse(row.get("user_name")):row.get("user_name"),""));
        for(var chunk:chunks(departments))for(var row:db.queryForList("SELECT dept_id,dept_name FROM sys_dept WHERE dept_id IN("+marks(chunk.size())+")",chunk.toArray()))depts.put(((Number)row.get("dept_id")).longValue(),Objects.toString(row.get("dept_name"),""));
        List<BizTaskVo> result=new ArrayList<>();
        for(var task:tasks){var view=new BizTaskVo();BeanUtils.copyProperties(task,view);view.setLeaderName(names.getOrDefault(task.getLeaderId(),""));view.setPrincipalName(names.getOrDefault(task.getPrincipalId(),""));view.setAuditorName(task.getAuditorId()==null?"无":names.getOrDefault(task.getAuditorId(),""));view.setDeptName(depts.getOrDefault(task.getDeptId(),""));result.add(view);}
        return result;
    }
    static String marks(int size){return String.join(",",Collections.nCopies(size,"?"));}
    static List<List<Long>> chunks(Collection<Long> ids){var list=new ArrayList<>(ids);var result=new ArrayList<List<Long>>();for(int i=0;i<list.size();i+=500)result.add(list.subList(i,Math.min(i+500,list.size())));return result;}
}
