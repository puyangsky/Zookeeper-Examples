package com.puyangsky.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * Author:      puyangsky
 * Date:        17/9/12 下午2:19
 */
public class ClientFactory {

    public static CuratorFramework newClient(String host, int port) {
        return CuratorFrameworkFactory.builder().connectString(String.format("%s:%d", host, port))
                .sessionTimeoutMs(30000)
                .connectionTimeoutMs(30000)
                .canBeReadOnly(false)
                .retryPolicy(new ExponentialBackoffRetry(1000, Integer.MAX_VALUE))
                .defaultData(null)
                .build();
    }
}
