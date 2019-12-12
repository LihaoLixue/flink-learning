package com.lihao.redis.demo;

/**
 * Created on 2019-12-11
 *
 * @author :hao.li
 */
import redis.clients.jedis.Jedis;

import java.util.List;

/**
 * @Author: qlq
 * @Description
 * @Date: 22:34 2018/10/9
 */
public class MessageConsumer implements Runnable {
    public static final String MESSAGE_KEY = "message:queue";
    private volatile int count;

    public void consumerMessage() {
        Jedis jedis = JedisPoolUtils.getJedis();
        List<String> brpop = jedis.brpop(0, MESSAGE_KEY);

        System.out.println(Thread.currentThread().getName() + " consumer message,message=" + brpop + ",count=" + count);
        count++;
    }

    @Override
    public void run() {
        while (true) {
            consumerMessage();
        }
    }

    public static void main(String[] args) {
        MessageConsumer messageConsumer = new MessageConsumer();
        Thread t1 = new Thread(messageConsumer, "thread6");
        Thread t2 = new Thread(messageConsumer, "thread7");
        t1.start();
        t2.start();
    }
}
