package com.puyangsky.zookeeper.util;

import redis.clients.jedis.Jedis;

/**
 * Author:      puyangsky
 * Date:        2017/11/25 下午4:49
 */
public class RedisTool {
    // TODO read from config
    private static Jedis jedis = new Jedis("localhost", 6379);

    public static void set(String key, String value) {
        jedis.set(key, value);
    }

    public static String get(String key) {
        return jedis.get(key);
    }
}
