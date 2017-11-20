package com.puyangsky.zookeeper.watcher;

import com.puyangsky.zookeeper.ClientFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.zookeeper.data.Stat;

/**
 * Author:      puyangsky
 * Date:        17/9/12 下午2:13
 */
public class Listener {

    private static CuratorFramework client = ClientFactory.newClient("127.0.0.1", 2181);
    private static final String PATH = "/crud";

    public static void curd() {
        try {
            String s = client.create().forPath(PATH, "fuck".getBytes());
            System.out.println(s);

            new Thread(()->{
                try {
                    Stat stat = client.setData().forPath(PATH, "fuck you".getBytes());
                    System.out.println(stat.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
            byte[] res = client.getData().watched().forPath(PATH);
            System.out.println("Get from zk: " + new String(res));


            client.delete().forPath(PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void listen() throws Exception {
        TreeCache treeCache = new TreeCache(client, PATH);
        treeCache.getListenable().addListener((curatorFramework, treeCacheEvent) -> {
            switch (treeCacheEvent.getType()) {
                case NODE_ADDED:
                    System.out.println("[zk] receive zk event: 创建事件" + treeCacheEvent.getType());
                    break;
                case NODE_REMOVED:
                    System.out.println("[zk] receive zk event: 删除事件" + treeCacheEvent.getType());
                    break;
                case NODE_UPDATED:
                    System.out.println("[zk] receive zk event: 更新数据事件" + treeCacheEvent.getType());
                    break;
                case INITIALIZED:
                    System.out.println("[zk] receive zk event: 初始化" + treeCacheEvent.getType());
                    break;
                case CONNECTION_LOST:
                    System.out.println("[zk] receive zk event: 失去连接" + treeCacheEvent.getType());
                    break;
                default:
                    System.out.println("[zk] receive zk event: 其他事件" + treeCacheEvent.getType());
                    break;
            }
        });
        treeCache.start();
        System.out.println("[zk] client starting...");
    }


    public static void main(String[] args) {
        try {
            client.start();
            listen();
            String res = client.create().forPath(PATH, "fuck".getBytes());
            System.out.println("add path: " + res);
            byte[] data = client.getData().forPath(PATH);
            System.out.println("get from: " + res + ", data: " + new String(data));
            client.delete().forPath(PATH);

            Thread.sleep(Integer.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
