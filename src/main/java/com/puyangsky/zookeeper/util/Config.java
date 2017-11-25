package com.puyangsky.zookeeper.util;

/**
 * Author:      puyangsky
 * Date:        2017/11/25 上午11:45
 */
public interface Config {
    String HOST = "localhost";
    int PORT = 2181;
    String ELECTION_KEY = "ELECTION_KEY";
    String PATH = "/test/leader";
    String QUEUE_KEY = "QUEUE_KEY";
}
