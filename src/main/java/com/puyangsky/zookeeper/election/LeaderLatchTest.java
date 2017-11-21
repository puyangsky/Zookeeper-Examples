package com.puyangsky.zookeeper.election;

import com.puyangsky.zookeeper.ClientFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.framework.recipes.leader.Participant;
import org.apache.curator.utils.CloseableUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Author:      puyangsky
 * Date:        2017/11/20 下午8:52
 */
public class LeaderLatchTest {
    private static final String PATH = "/test/latch";

    public static void main(String[] args) {
        List<LeaderLatch> latchList = new ArrayList<>();
        List<CuratorFramework> clients = new ArrayList<>();

        try {
            for (int i = 0; i < 10; i++) {
                CuratorFramework client = ClientFactory.newClient("127.0.0.1", 2181);
                client.start();
                clients.add(client);

                final LeaderLatch leaderLatch = new LeaderLatch(client, PATH, "client-" + i);
                leaderLatch.addListener(new LeaderLatchListener() {
                    @Override
                    public void isLeader() {
                        System.out.println(leaderLatch.getId() + ":I am leader now！");
                        try {
                            Thread.sleep(1000);
                            Collection<Participant> slaves = leaderLatch.getParticipants();
                            for (Participant slave : slaves) {
                                System.out.println("slaves:" + slave.getId());
                            }
                            System.out.println("leave master");

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void notLeader() {
                        System.out.println(leaderLatch.getId() + ":I am not leader.");
                    }
                });
                latchList.add(leaderLatch);
                leaderLatch.start();
            }

            Thread.sleep(Integer.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            for(CuratorFramework client : clients){
                CloseableUtils.closeQuietly(client);
            }

            for(LeaderLatch leaderLatch : latchList){
                CloseableUtils.closeQuietly(leaderLatch);
            }
        }
    }

}
