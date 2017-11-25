package com.puyangsky.zookeeper.election.distribute;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Author:      puyangsky
 * Date:        2017/11/25 下午3:12
 */
public class Test1 {
    private static ExecutorService executorService = Executors.newFixedThreadPool(1);
    private static ExecutorService consumeExecutorService = Executors.newFixedThreadPool(1);
    private static AtomicBoolean isMaster = new AtomicBoolean(false);
    public static void main(String[] args) {
        executorService.submit(new LeaderSelectorDistributeTest.ElectionTask(Test1.class.getName(), isMaster));
        consumeExecutorService.submit(new LeaderSelectorDistributeTest.SlaveTask(Test1.class.getName(), isMaster));
    }
}
