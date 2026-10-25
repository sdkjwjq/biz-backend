package org.example.service;

import org.example.entity.SysNotice;
import org.example.mapper.ReviewNoticeSql;
import org.example.mapper.SysMapper;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.util.MultiValueMap;
import java.util.*;

/** Stable UI order, exact existing predicates, and bodies hydrated only for the selected page. */
@Service
public class NoticeFilterPageService {
    private final PermissionService permissions;
    private final BusinessListFilterService filters;
    private final JdbcTemplate jdbc;
    private final SysMapper mapper;
    public NoticeFilterPageService(PermissionService permissions,BusinessListFilterService filters,JdbcTemplate jdbc,SysMapper mapper){this.permissions=permissions;this.filters=filters;this.jdbc=jdbc;this.mapper=mapper;}
    @Transactional(readOnly=true,isolation=Isolation.REPEATABLE_READ)
    public Map<String,Object> query(Long actor,MultiValueMap<String,String> params){
        permissions.require(actor,"messages.read");
        var fields=filters.definitions("notices",actor,params);
        var predicate=new ListFilterQuery(params,fields);
        int size=number(params.getFirst("pageSize"),10,100),requested=number(params.getFirst("page"),1,Integer.MAX_VALUE);
        String from=ReviewNoticeSql.jdbcVisibleFrom();
        List<Object> args=new ArrayList<>(List.of(actor));
        for(String key:List.of("isRead","type","fromUserId")){
            var selected=params.getOrDefault("f."+key,List.of()).stream().filter(v->v!=null&&!v.isBlank()).distinct().toList();
            if(selected.isEmpty())continue;
            String column=switch(key){case "isRead"->"n.is_read";case "fromUserId"->"n.from_user_id";default->"n.type";};
            from+=" AND BINARY COALESCE(CAST("+column+" AS CHAR),'missing') IN ("+String.join(",",Collections.nCopies(selected.size(),"?"))+")";
            args.addAll(selected);
        }
        boolean complex=params.keySet().stream().filter(k->k.startsWith("f.")).anyMatch(k->!Set.of("f.isRead","f.type","f.fromUserId").contains(k));
        // Equal/unknown timestamps must preserve the old SELECT encounter order, not invent an ID tie-break.
        boolean ambiguous=!jdbc.queryForList("SELECT n.create_time"+from+" GROUP BY n.create_time HAVING COUNT(*)>1 OR n.create_time IS NULL LIMIT 1",args.toArray()).isEmpty();
        long total;List<SysNotice> items;
        if(complex||ambiguous){
            String columns=complex&&params.containsKey("f.keyword")?",n.title,n.content":"";
            List<SysNotice> candidates;
            if(ambiguous)candidates=mapper.getNotices(actor);
            else candidates=jdbc.query("SELECT n.notice_id,n.from_user_id,n.to_user_id,n.type,n.source_type,n.source_id,n.is_read,n.create_time"+columns+from,
                    BeanPropertyRowMapper.newInstance(SysNotice.class),args.toArray());
            for(var candidate:candidates)if(candidate.getCreateTime()!=null)candidate.setCreateTime(new java.util.Date(candidate.getCreateTime().getTime()));
            Map<Long,Long> times=new HashMap<>();candidates.forEach(n->times.put(n.getNoticeId(),n.getCreateTime()==null?0:n.getCreateTime().getTime()));
            var matched=filters.noticeRows(candidates).stream().filter(r->predicate.matches(r,fields)).sorted((a,b)->Long.compare(times.get(Long.valueOf(b.get("id").toString())),times.get(Long.valueOf(a.get("id").toString())))).toList();
            total=matched.size();int page=(int)Math.min(requested,Math.max(1,(total+size-1)/size));
            var ids=matched.subList((page-1)*size,Math.min(page*size,matched.size())).stream().map(r->Long.valueOf(r.get("id").toString())).toList();
            items=hydrate(ids);
        }else{
            total=jdbc.queryForObject("SELECT COUNT(*)"+from,Long.class,args.toArray());
            int page=(int)Math.min(requested,Math.max(1,(total+size-1)/size));args.add(size);args.add((long)(page-1)*size);
            items=jdbc.query("SELECT n.*"+from+" ORDER BY n.create_time DESC LIMIT ? OFFSET ?",BeanPropertyRowMapper.newInstance(SysNotice.class),args.toArray());
        }
        int page=(int)Math.min(requested,Math.max(1,(total+size-1)/size));
        var names=new HashMap<Long,String>();var senders=items.stream().map(SysNotice::getFromUserId).filter(Objects::nonNull).distinct().toList();
        if(!senders.isEmpty())for(var row:jdbc.queryForList("SELECT user_id,COALESCE(NULLIF(nick_name,''),user_name) nick_name FROM sys_user WHERE user_id IN ("+String.join(",",Collections.nCopies(senders.size(),"?"))+")",senders.toArray())){if(row.get("nick_name")!=null)names.put(((Number)row.get("user_id")).longValue(),row.get("nick_name").toString());};
        var output=items.stream().map(item->{var row=filters.wireRow(item);row.put("senderName",names.get(item.getFromUserId()));return row;}).toList();
        var result=new LinkedHashMap<String,Object>();result.put("items",output);result.put("total",total);result.put("page",page);result.put("pageSize",size);result.put("summary",Map.of("unreadCount",mapper.countUnreadNotices(actor)));return result;
    }
    private List<SysNotice> hydrate(List<Long> ids){if(ids.isEmpty())return List.of();var rows=jdbc.query("SELECT * FROM sys_notice WHERE notice_id IN ("+String.join(",",Collections.nCopies(ids.size(),"?"))+")",BeanPropertyRowMapper.newInstance(SysNotice.class),ids.toArray());var map=new HashMap<Long,SysNotice>();rows.forEach(r->map.put(r.getNoticeId(),r));return ids.stream().map(map::get).filter(Objects::nonNull).toList();}
    private int number(String value,int fallback,int max){if(value==null||value.isBlank())return fallback;try{int n=Integer.parseInt(value);if(n>0)return Math.min(n,max);}catch(NumberFormatException ignored){}throw new AdminManagementException(400,"Invalid pagination");}
}
