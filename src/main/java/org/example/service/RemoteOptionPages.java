package org.example.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import java.util.*;

/** Small option projections only; Unicode search keeps Java's original exact matcher. */
@Service
public class RemoteOptionPages {
    private final JdbcTemplate jdbc;
    public RemoteOptionPages(JdbcTemplate jdbc){this.jdbc=jdbc;}
    @org.springframework.transaction.annotation.Transactional(readOnly=true,isolation=org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public Map<String,Object> query(String source,List<Object> arguments,MultiValueMap<String,String> params){return query(source,arguments,params,"CAST(CONVERT(value USING utf16) AS BINARY)",null);}
    @org.springframework.transaction.annotation.Transactional(readOnly=true,isolation=org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public Map<String,Object> query(String source,List<Object> arguments,MultiValueMap<String,String> params,String ordering,String extraSearch){
        String keyword=Objects.toString(params.getFirst("keyword"),"").trim().toLowerCase(Locale.ROOT);
        if(keyword.length()>300)throw new AdminManagementException(400,"筛选文字过长");
        var selected=params.getOrDefault("ids",List.of());if(selected.size()>100)throw new AdminManagementException(400,"已选项最多100项");
        int requested=positive(params.getFirst("page"),1),size=Math.min(100,positive(params.getFirst("pageSize"),50));
        source="SELECT DISTINCT CAST(raw_options.value AS CHAR CHARACTER SET utf8mb4) COLLATE utf8mb4_bin value,CAST(raw_options.label AS CHAR CHARACTER SET utf8mb4) COLLATE utf8mb4_bin label FROM ("+source.replace("SELECT DISTINCT","SELECT")+") raw_options";
        String from=" FROM ("+source+") option_source WHERE value IS NOT NULL AND NOT REGEXP_LIKE(CAST(value AS CHAR),'^[\\\\x{0009}-\\\\x{000D}\\\\x{001C}-\\\\x{0020}\\\\x{1680}\\\\x{2000}-\\\\x{2006}\\\\x{2008}-\\\\x{200A}\\\\x{2028}\\\\x{2029}\\\\x{205F}\\\\x{3000}]*$','c')";
        String order=" ORDER BY "+ordering;
        long total;int page;List<Map<String,Object>> items;
        if(keyword.isEmpty()){
            total=jdbc.queryForObject("SELECT COUNT(*)"+from,Long.class,arguments.toArray());page=(int)Math.min(requested,Math.max(1,(total+size-1)/size));
            var args=new ArrayList<>(arguments);args.add(size);args.add((long)(page-1)*size);items=jdbc.queryForList("SELECT value,label"+from+order+" LIMIT ? OFFSET ?",args.toArray());
        }else{
            var collected=BoundedFilterPage.scan(requested,size,offset->{var args=new ArrayList<>(arguments);args.add(BoundedFilterPage.BATCH_SIZE);args.add(offset);return jdbc.queryForList("SELECT value,label"+(extraSearch==null?"":","+extraSearch+" _search")+from+order+" LIMIT ? OFFSET ?",args.toArray());},r->text(r.get("value")).contains(keyword)||text(r.get("label")).contains(keyword)||text(r.get("_search")).contains(keyword));
            total=collected.total();page=collected.page();items=collected.items();
        }
        List<Map<String,Object>> echoed=List.of();
        if(!selected.isEmpty()){var args=new ArrayList<>(arguments);args.addAll(selected);echoed=jdbc.queryForList("SELECT value,label"+from+" AND BINARY value IN ("+String.join(",",Collections.nCopies(selected.size(),"?"))+")"+order,args.toArray());}
        items.forEach(this::strings);echoed.forEach(this::strings);
        return Map.of("items",items,"total",total,"page",page,"pageSize",size,"selected",echoed);
    }
    private String text(Object v){return Objects.toString(v,"").toLowerCase(Locale.ROOT);}
    private void strings(Map<String,Object> row){row.remove("_search");row.put("value",Objects.toString(row.get("value"),""));row.put("label",Objects.toString(row.get("label"),""));}
    private int positive(String value,int fallback){if(value==null||value.isBlank())return fallback;try{int n=Integer.parseInt(value);if(n>0)return n;}catch(NumberFormatException ignored){}throw new AdminManagementException(400,"分页参数不正确");}
}
