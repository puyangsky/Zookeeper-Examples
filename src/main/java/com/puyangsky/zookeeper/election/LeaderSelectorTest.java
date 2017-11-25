package com.puyangsky.zookeeper.election;

import com.puyangsky.zookeeper.ClientFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.utils.CloseableUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Author:      puyangsky
 * Date:        2017/11/20 下午8:23
 */
public class LeaderSelectorTest {
    private static final String PATH = "/test/leader";

    public static void main(String[] args) {
        List<LeaderSelector> selectors = new ArrayList<>();
        List<CuratorFramework> clients = new ArrayList<>();
        try {
            for (int i = 0; i < 10; i++) {
                CuratorFramework client = ClientFactory.newClient("127.0.0.1", 2181);
                client.start();
                clients.add(client);
                final String name = "client-" + i;
                LeaderSelector leaderSelector = new LeaderSelector(client, PATH, new LeaderSelectorListener() {
                    @Override
                    public void takeLeadership(CuratorFramework curatorFramework) throws Exception {
                        System.out.println(name + ":I am a leader now");
                        while (true) {
                            Scanner scanner = new Scanner(System.in);
                            String line = scanner.nextLine();
                            if (line.equals("bye")) {
                                System.out.println("release leadership");
                                break;
                            }
                        }
                    }
                    @Override
                    public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
//                        System.out.println("state changed");
                    }
                });
                //释放leadership后还可以参与选主
                leaderSelector.autoRequeue();
                leaderSelector.start();
                selectors.add(leaderSelector);
            }
            Thread.sleep(Integer.MAX_VALUE);

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            for (CuratorFramework client : clients) {
                CloseableUtils.closeQuietly(client);
            }
            for(org.apache.curator.framework.recipes.leader.LeaderSelector selector : selectors){
                CloseableUtils.closeQuietly(selector);
            }
        }
    }
}
