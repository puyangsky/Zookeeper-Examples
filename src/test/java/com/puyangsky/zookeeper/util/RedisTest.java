package com.puyangsky.zookeeper.util;

import org.junit.Test;

/**
 * Author:      puyangsky
 * Date:        2017/11/25 下午11:24
 */
public class RedisTest {
    @Test
    public void test() {
        System.out.println(RedisTool.lpush("queue", "b"));
    }

    @Test
    public void testRpop() {
        System.out.println(RedisTool.rpop("queue") == null);
    }

    @Test
    public void testGet() {
        System.out.println(RedisTool.get(Config.ELECTION_KEY));
    }

    @Test
    public void testSet() {
        RedisTool.set(Config.ELECTION_KEY, "true");
    }
}
