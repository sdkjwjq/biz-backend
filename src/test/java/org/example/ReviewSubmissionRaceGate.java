package org.example;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** 仅测试注册：控制真实查询后的请求时序，不替换 SQL、返回值或业务服务。 */
@Intercepts(@Signature(type = Executor.class, method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}))
class ReviewSubmissionRaceGate implements Interceptor {
    volatile Race current;

    static class Race {
        final String queryName;
        final CountDownLatch firstChecked = new CountDownLatch(1);
        final CountDownLatch secondChecked = new CountDownLatch(1);
        final CountDownLatch firstFinished = new CountDownLatch(1);
        final AtomicInteger emptyChecks = new AtomicInteger();

        Race(String queryName) {
            this.queryName = queryName;
        }
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Race race = current;
        Object result = invocation.proceed();
        String statementId = ((MappedStatement) invocation.getArgs()[0]).getId();
        if (race != null && statementId.endsWith("." + race.queryName)
                && result instanceof List<?> rows && rows.isEmpty()) {
            int order = race.emptyChecks.incrementAndGet();
            if (order == 1) {
                race.firstChecked.countDown();
                // 原实现允许第二条请求也读到空审核单；加锁后第二条会等待，首条到时继续。
                race.secondChecked.await(2, TimeUnit.SECONDS);
            } else if (order == 2) {
                race.secondChecked.countDown();
                // 首条提交完成后才恢复第二条，确定性覆盖“先检查、后插入”的竞争窗口。
                if (!race.firstFinished.await(20, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("First review request did not finish");
                }
            }
        }
        return result;
    }
}
