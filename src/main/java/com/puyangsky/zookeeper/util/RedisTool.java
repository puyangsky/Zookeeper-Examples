package com.puyangsky.zookeeper.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Author:      puyangsky
 * Date:        2017/11/25 下午4:49
 */
public class RedisTool {
    // TODO read from config
    private static JedisPool jedisPool = new JedisPool();

    public static void set(String key, String value) {
        Jedis jedis = jedisPool.getResource();
        jedis.set(key, value);
        jedis.close();
    }

    public static String get(String key) {
        Jedis jedis = jedisPool.getResource();
        String rs = jedis.get(key);
        jedis.close();
        return rs;
    }

    public static long lpush(String key, String value) {
        Jedis jedis = jedisPool.getResource();
        long rs = jedis.lpush(key, value);
        jedis.close();
        return rs;
    }

    public static String rpop(String key) {
        Jedis jedis = jedisPool.getResource();
        String rs = jedis.rpop(key);
        jedis.close();
        return rs;
    }

    public static String lpop(String key) {
        Jedis jedis = jedisPool.getResource();
        String rs = jedis.lpop(key);
        jedis.close();
        return rs;
    }

    public static void del(String key) {
        Jedis jedis = jedisPool.getResource();
        jedis.del(key);
        jedis.close();
    }
}
