package org.example.service;

import java.util.*;
import java.util.function.*;

/** Exact filtering over bounded candidate batches. Retains at most two result pages. */
public final class BoundedFilterPage<T> {
    public static final int BATCH_SIZE=500;
    public record Result<T>(List<T> items,long total,int page,int pageSize) {}
    private final int requested,size;
    private final List<T> requestedItems=new ArrayList<>();
    private final ArrayDeque<T> tail=new ArrayDeque<>();
    private long total;
    public BoundedFilterPage(int requested,int size){this.requested=requested;this.size=size;}
    public void accept(T value){
        long position=total++;
        if(position/size+1==requested)requestedItems.add(value);
        tail.addLast(value);if(tail.size()>size)tail.removeFirst();
    }
    public Result<T> result(){
        int page=(int)Math.min(requested,Math.max(1,(total+size-1)/size));
        if(page==requested)return new Result<>(List.copyOf(requestedItems),total,page,size);
        var last=new ArrayList<>(tail);int count=total==0?0:(int)((total-1)%size+1);
        return new Result<>(List.copyOf(last.subList(last.size()-count,last.size())),total,page,size);
    }
    public static <T> Result<T> scan(int requested,int size,IntFunction<List<T>> fetch,Predicate<T> matches){
        var page=new BoundedFilterPage<T>(requested,size);
        for(int offset=0;;offset+=BATCH_SIZE){var batch=fetch.apply(offset);if(batch.size()>BATCH_SIZE)throw new IllegalStateException("Candidate batch exceeds limit");for(var row:batch)if(matches.test(row))page.accept(row);if(batch.size()<BATCH_SIZE)break;}
        return page.result();
    }
    private BoundedFilterPage(){throw new UnsupportedOperationException();}
}
