package com.puyangsky.zookeeper.election.distribute;

import com.puyangsky.zookeeper.ClientFactory;
import com.puyangsky.zookeeper.util.RedisTool;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.utils.CloseableUtils;

import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Author:      puyangsky
 * Date:        2017/11/20 下午8:23
 */
public class LeaderSelectorDistributeTest {
    private static final String PATH = "/test/leader";
    // ThreadLocal变量记录当前线程是否为master
//    private static ThreadLocal<Boolean> isMaster = ThreadLocal.withInitial(() -> false);
    private static boolean electionFinished;

    //消费者队列
    private static LinkedBlockingQueue<String> consumeQueue = new LinkedBlockingQueue<>();

    private static void election(String threadName, AtomicBoolean isMaster) {
        CuratorFramework client = null;
        LeaderSelector leaderSelector = null;
        try {
            client = ClientFactory.newClient(Config.HOST, Config.PORT);
            client.start();
            leaderSelector = new LeaderSelector(client, PATH, new LeaderSelectorListener() {
                @Override
                public void takeLeadership(CuratorFramework curatorFramework) throws Exception {
                    // TODO 执行master操作，生成metadata，填充消费队列
                    System.out.println(String.format("[%s]: I am a leader now", threadName));
                    isMaster.set(true);
                    RedisTool.set(Config.ELECTION_KEY, "true");
                    while (true) {
                        Scanner scanner = new Scanner(System.in);
                        String line = scanner.nextLine();
                        if (line.equals("bye")) {
                            System.out.println(String.format("[%s]: Release leadership", threadName));
                            break;
                        }else {
                            consumeQueue.offer(line);
                        }
                    }
                    isMaster.set(false);
                    RedisTool.set(Config.ELECTION_KEY, "false");
                }
                @Override
                public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {}
            });
            //释放leadership后还可以参与选主
            leaderSelector.autoRequeue();
            leaderSelector.start();
            Thread.sleep(Integer.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseableUtils.closeQuietly(client);
            CloseableUtils.closeQuietly(leaderSelector);
        }
    }

    /**
     * slave节点执行的操作
     * fetch from consumerQueue
     */
    private static void slaverWork(String threadName, AtomicBoolean isMaster) {
        while (true) {
            electionFinished = Boolean.parseBoolean(RedisTool.get(Config.ELECTION_KEY));
            if (electionFinished && !isMaster.get()) {
                System.out.println(threadName + ": test");
                System.out.println(String.format("%s: begin slave work", threadName));
                String url = null;
                try {
                    url = consumeQueue.poll(1, TimeUnit.MINUTES);
                    System.out.println(String.format("%s: consumes %s", threadName, url));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class ElectionTask implements Runnable {
        private String threadName;
        private AtomicBoolean isMaster;
        public ElectionTask(String threadName, AtomicBoolean isMaster) {
            this.threadName = threadName;
            this.isMaster = isMaster;
        }
        @Override
        public void run() {
            election(threadName, isMaster);
        }
    }

    static class SlaveTask implements Runnable {
        private String threadName;
        private AtomicBoolean isMaster;
        public SlaveTask(String threadName, AtomicBoolean isMaster) {
            this.threadName = threadName;
            this.isMaster = isMaster;
        }
        @Override
        public void run() {
            slaverWork(this.threadName, isMaster);
        }
    }
}
