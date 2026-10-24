package org.example.service;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.util.MultiValueMap;

/** Whitelisted, typed predicates shared by read lists and exports. Never accepts SQL/expressions. */
public final class ListFilterQuery {
    public record Field(String key,String label,String type,boolean basic,String group,List<Map<String,Object>> options) {}
    private final Map<String,List<String>> values=new LinkedHashMap<>();
    public ListFilterQuery(MultiValueMap<String,String> input,List<Field> fields){
        Map<String,Field> allowed=new HashMap<>();for(var f:fields)allowed.put(f.key(),f);
        for(var e:input.entrySet()){
            if(!e.getKey().startsWith("f."))continue;
            String key=e.getKey().substring(2),base=key.replaceFirst("(From|To)$","");
            var f=allowed.get(base);if(f==null)throw bad("不支持的筛选条件："+key);
            boolean range=!key.equals(base);
            if(range&&!Set.of("date","number").contains(f.type()))throw bad("该条件不支持区间："+f.label());
            var selected=e.getValue().stream().filter(v->v!=null&&!v.isBlank()).distinct().toList();
            if(selected.isEmpty())continue;if(selected.size()>100)throw bad("单个条件最多选择100项");
            if(!f.type().equals("multi")&&selected.size()>1)throw bad("该条件仅允许一个值："+f.label());
            for(String value:selected){if(value.length()>300)throw bad("筛选文字过长");
                if(f.type().equals("number")){try{new BigDecimal(value);}catch(Exception x){throw bad("数值条件不正确");}}
                if(f.type().equals("date")){if(range&&value.equals("missing"))throw bad("日期区间不支持未设置");if(!value.equals("missing"))try{LocalDate.parse(value);}catch(Exception x){throw bad("日期条件不正确");}}
                if(f.type().equals("select")&&!f.options().isEmpty()&&f.options().stream().noneMatch(o->String.valueOf(o.get("value")).equals(value)))throw bad("筛选选项不正确："+f.label());
            }values.put(key,selected);
        }
        for(var f:fields){if(values.containsKey(f.key())&&(values.containsKey(f.key()+"From")||values.containsKey(f.key()+"To")))throw bad("不能同时设置缺失值和区间："+f.label());}
        for(var f:fields)if(values.containsKey(f.key()+"From")&&values.containsKey(f.key()+"To")){
            String a=first(f.key()+"From"),b=first(f.key()+"To");
            if((f.type().equals("number")?new BigDecimal(a).compareTo(new BigDecimal(b)):a.compareTo(b))>0)throw bad("区间起点不能大于终点："+f.label());
        }
    }
    public boolean active(){return !values.isEmpty();}
    public String first(String key){return values.getOrDefault(key,List.of("")).get(0);}
    public boolean matches(Map<String,Object> row,List<Field> fields){
        for(var f:fields){String key=f.key();Object value=row.get(key);var selected=values.get(key);
            if(selected!=null){
                if(f.type().equals("text")){if(!str(value).toLowerCase(Locale.ROOT).contains(selected.get(0).trim().toLowerCase(Locale.ROOT)))return false;}
                else if(value instanceof Collection<?> list){if(list.stream().noneMatch(v->selected.contains(str(v))))return false;}
                else if(!selected.contains(value==null||str(value).isBlank()?"missing":str(value)))return false;
            }
            if(values.containsKey(key+"From")||values.containsKey(key+"To")){
                if(value==null||str(value).isBlank())return false;
                if(f.type().equals("number")){BigDecimal n;try{n=new BigDecimal(str(value));}catch(Exception e){return false;}
                    if(values.containsKey(key+"From")&&n.compareTo(new BigDecimal(first(key+"From")))<0)return false;
                    if(values.containsKey(key+"To")&&n.compareTo(new BigDecimal(first(key+"To")))>0)return false;
                }else{String date=date(value);if(date.isEmpty())return false;if(values.containsKey(key+"From")&&date.compareTo(first(key+"From"))<0)return false;if(values.containsKey(key+"To")&&date.compareTo(first(key+"To"))>0)return false;}
            }
        }return true;
    }
    public static String date(Object value){if(value==null)return "";if(value instanceof java.sql.Date d)return d.toLocalDate().toString();if(value instanceof java.util.Date d)return d.toInstant().atZone(ZoneId.of("Asia/Shanghai")).toLocalDate().toString();if(value instanceof Number n)return Instant.ofEpochMilli(n.longValue()).atZone(ZoneId.of("Asia/Shanghai")).toLocalDate().toString();String s=str(value);return s.length()>=10?s.substring(0,10):"";}
    public static String str(Object value){return value==null?"":String.valueOf(value);}
    private static AdminManagementException bad(String message){return new AdminManagementException(400,message);}
}
