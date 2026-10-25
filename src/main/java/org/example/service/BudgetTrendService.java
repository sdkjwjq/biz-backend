package org.example.service;

import org.example.entity.*;
import org.example.mapper.BudgetMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/** 仅读取每月当前有效预算；不从历史版本重复累计。 */
@Service
public class BudgetTrendService {
    private final BudgetMapper budgets;
    public BudgetTrendService(BudgetMapper budgets) { this.budgets = budgets; }
    record MonthValues(BigDecimal cumulative,BigDecimal denominator) {
        BigDecimal executionRate(){return cumulative==null||denominator==null||denominator.signum()<=0?null:cumulative.multiply(BigDecimal.valueOf(100)).divide(denominator,4,RoundingMode.HALF_UP);}
    }

    @Transactional(readOnly=true, isolation=org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public Map<String,Object> trends(Integer year, Integer taskIndex) {
        if(year==null || year<2025 || year>2029 || taskIndex==null || taskIndex<0 || taskIndex>10)
            throw new TaskManagementException(400,"请选择2025—2029年度及全校或十大改革任务");
        List<Map<String,Object>> points=new ArrayList<>();
        Map<Integer,MonthValues> months=yearValues(year,taskIndex);
        BigDecimal previous=null;
        for(int month=1;month<=12;month++) {
            MonthValues values=months.getOrDefault(month,new MonthValues(null,null));
            BigDecimal cumulative=values.cumulative();
            BigDecimal incremental=cumulative==null?null:month==1?cumulative:previous==null?null:cumulative.subtract(previous);
            Map<String,Object> point=new LinkedHashMap<>();
            point.put("month",month);point.put("cumulative",cumulative);point.put("incremental",incremental);
            point.put("executionRate",values.executionRate());
            point.put("adjustment",incremental!=null&&incremental.signum()<0);
            points.add(point);previous=cumulative;
        }
        return Map.of("year",year,"taskIndex",taskIndex,"points",points);
    }
    private Map<Integer,MonthValues> yearValues(Integer year,int taskIndex){
        var sheets=budgets.trendSheets(year);Map<Integer,BizBudgetSheet> byMonth=new HashMap<>();
        for(var sheet:sheets)if(byMonth.put(sheet.getMonth(),sheet)!=null)return legacyYearValues(year,taskIndex);
        if(sheets.isEmpty())return Map.of();
        var ids=sheets.stream().map(BizBudgetSheet::getSheetId).toList();
        Map<Long,List<BizBudgetTaskItem>> tasks=new HashMap<>();
        for(var item:budgets.trendTasks(ids))tasks.computeIfAbsent(item.getSheetId(),id->new ArrayList<>()).add(item);
        Map<Long,List<BizBudgetSourceItem>> sources=new HashMap<>();
        if(taskIndex==0)for(var item:budgets.trendSources(ids))sources.computeIfAbsent(item.getSheetId(),id->new ArrayList<>()).add(item);
        Map<Integer,MonthValues> result=new HashMap<>();
        for(var sheet:sheets){
            var items=tasks.getOrDefault(sheet.getSheetId(),List.of());Set<Integer> seen=new HashSet<>();boolean duplicate=false;
            for(var item:items)if(item.getTaskIndex()!=null&&item.getTaskIndex()>=1&&item.getTaskIndex()<=10&&!seen.add(item.getTaskIndex())){duplicate=true;break;}
            // The original ORDER BY leaves ties unspecified. Preserve its exact encounter order when it matters.
            if(duplicate)items=budgets.getTasks(sheet.getSheetId());
            result.put(sheet.getMonth(),monthValues(items,sources.getOrDefault(sheet.getSheetId(),List.of()),taskIndex));
        }
        return result;
    }
    private Map<Integer,MonthValues> legacyYearValues(Integer year,int taskIndex){
        Map<Integer,MonthValues> result=new HashMap<>();
        for(int month=1;month<=12;month++){
            var sheet=budgets.getSheet(year,month);
            if(sheet!=null)result.put(month,monthValues(budgets.getTasks(sheet.getSheetId()),taskIndex==0?budgets.getSources(sheet.getSheetId()):List.of(),taskIndex));
        }
        return result;
    }
    MonthValues monthValues(List<BizBudgetTaskItem> tasks,List<BizBudgetSourceItem> sources,int taskIndex){
        BigDecimal cumulative=null,denominator=null;
        if(taskIndex==0){
            Map<Integer,BizBudgetTaskItem> distinct=new HashMap<>();
            for(var task:tasks)if(task.getTaskIndex()!=null&&task.getTaskIndex()>=1&&task.getTaskIndex()<=10)distinct.put(task.getTaskIndex(),task);
            if(distinct.size()==10)cumulative=expense(new ArrayList<>(distinct.values()));
            if(!sources.isEmpty()&&sources.stream().allMatch(s->s.getAnnualPlan()!=null))denominator=sources.stream().map(BizBudgetSourceItem::getAnnualPlan).reduce(BigDecimal.ZERO,BigDecimal::add);
        }else{
            var selected=tasks.stream().filter(t->Integer.valueOf(taskIndex).equals(t.getTaskIndex())).toList();
            if(selected.size()==1){cumulative=expense(selected);denominator=selected.get(0).getAmount();}
        }
        return new MonthValues(cumulative,denominator);
    }
    private BigDecimal expense(List<BizBudgetTaskItem> tasks) {
        BigDecimal sum=BigDecimal.ZERO;
        for(var task:tasks) {
            if(task.getGoods()==null||task.getCapital()==null||task.getOther()==null)return null;
            sum=sum.add(task.getGoods()).add(task.getCapital()).add(task.getOther());
        }
        return sum;
    }
}
