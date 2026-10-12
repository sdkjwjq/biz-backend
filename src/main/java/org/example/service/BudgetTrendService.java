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

    @Transactional(readOnly=true, isolation=org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public Map<String,Object> trends(Integer year, Integer taskIndex) {
        if(year==null || year<2025 || year>2029 || taskIndex==null || taskIndex<0 || taskIndex>10)
            throw new TaskManagementException(400,"请选择2025—2029年度及全校或十大改革任务");
        List<Map<String,Object>> points=new ArrayList<>();
        BigDecimal previous=null;
        for(int month=1;month<=12;month++) {
            BizBudgetSheet sheet=budgets.getSheet(year,month);
            BigDecimal cumulative=null,denominator=null;
            if(sheet!=null) {
                List<BizBudgetTaskItem> tasks=budgets.getTasks(sheet.getSheetId());
                if(taskIndex==0) {
                    Map<Integer,BizBudgetTaskItem> distinct=new HashMap<>();
                    for(var task:tasks) if(task.getTaskIndex()!=null && task.getTaskIndex()>=1 && task.getTaskIndex()<=10)
                        distinct.put(task.getTaskIndex(),task);
                    if(distinct.size()==10) cumulative=expense(new ArrayList<>(distinct.values()));
                    List<BizBudgetSourceItem> sources=budgets.getSources(sheet.getSheetId());
                    if(!sources.isEmpty() && sources.stream().allMatch(s->s.getAnnualPlan()!=null))
                        denominator=sources.stream().map(BizBudgetSourceItem::getAnnualPlan).reduce(BigDecimal.ZERO,BigDecimal::add);
                } else {
                    List<BizBudgetTaskItem> selected=tasks.stream().filter(t->taskIndex.equals(t.getTaskIndex())).toList();
                    if(selected.size()==1) { cumulative=expense(selected);denominator=selected.get(0).getAmount(); }
                }
            }
            BigDecimal incremental=cumulative==null?null:month==1?cumulative:previous==null?null:cumulative.subtract(previous);
            Map<String,Object> point=new LinkedHashMap<>();
            point.put("month",month);point.put("cumulative",cumulative);point.put("incremental",incremental);
            point.put("executionRate",cumulative==null||denominator==null||denominator.signum()<=0?null:cumulative.multiply(BigDecimal.valueOf(100)).divide(denominator,4,RoundingMode.HALF_UP));
            point.put("adjustment",incremental!=null&&incremental.signum()<0);
            points.add(point);previous=cumulative;
        }
        return Map.of("year",year,"taskIndex",taskIndex,"points",points);
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
