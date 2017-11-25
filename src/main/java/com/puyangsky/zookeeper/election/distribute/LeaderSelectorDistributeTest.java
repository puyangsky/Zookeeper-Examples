package com.puyangsky.zookeeper.election.distribute;

import com.puyangsky.zookeeper.ClientFactory;
import com.puyangsky.zookeeper.util.Config;
import com.puyangsky.zookeeper.util.RedisTool;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.utils.CloseableUtils;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Author:      puyangsky
 * Date:        2017/11/20 下午8:23
 */
public class LeaderSelectorDistributeTest {

    //消费者队列
    private static void election(String threadName, AtomicBoolean isMaster) {
        CuratorFramework client = null;
        LeaderSelector leaderSelector = null;
        try {
            client = ClientFactory.newClient(Config.HOST, Config.PORT);
            client.start();
            leaderSelector = new LeaderSelector(client, Config.PATH, new LeaderSelectorListener() {
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
                            isMaster.set(false);
                            RedisTool.set(Config.ELECTION_KEY, "false");
                            break;
                        }else {
                            //填充消费队列
                            System.out.println("push into " + Config.QUEUE_KEY + line);
                            RedisTool.lpush(Config.QUEUE_KEY, line);
                        }
                    }
                }
                @Override
                public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
                    if (connectionState == ConnectionState.LOST || connectionState == ConnectionState.SUSPENDED) {
                        System.out.println(threadName + ": lost");
                        // TODO 做出应对策略
                    }
                }
            });
            //释放leadership后还可以参与选主
            leaderSelector.autoRequeue();
            leaderSelector.start();
            Thread.sleep(Integer.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            RedisTool.set(Config.ELECTION_KEY, "false");
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
            String value = RedisTool.get(Config.ELECTION_KEY);
            boolean electionFinished = StringUtils.isNotEmpty(value) && Boolean.parseBoolean(value);
            if (electionFinished && !isMaster.get()) {
                String url = RedisTool.rpop(Config.QUEUE_KEY);
                if (StringUtils.isNotEmpty(url)) {
                    System.out.println(String.format("%s: consumes %s", threadName, url));
                }else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
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
