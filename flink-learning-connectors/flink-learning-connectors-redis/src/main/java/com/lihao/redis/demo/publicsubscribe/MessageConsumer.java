package com.lihao.redis.demo.publicsubscribe;

/**
 * Created on 2019-12-12
 *
 * @author :hao.li
 */
import com.lihao.redis.demo.JedisPoolUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

/**
 * @Author: qlq
 * @Description
 * @Date: 22:34 2018/10/9
 */
public class MessageConsumer implements Runnable {
    public static final String CHANNEL_KEY = "channel:1";//频道

    public static final String EXIT_COMMAND = "exit";//结束程序的消息

    private MyJedisPubSub myJedisPubSub = new MyJedisPubSub();//处理接收消息

    public void consumerMessage() {
        Jedis jedis = JedisPoolUtils.getJedis();
        jedis.subscribe(myJedisPubSub, CHANNEL_KEY);//第一个参数是处理接收消息，第二个参数是订阅的消息频道
    }

    @Override
    public void run() {
        while (true) {
            consumerMessage();
        }
    }

    public static void main(String[] args) {
        MessageConsumer messageConsumer = new MessageConsumer();
        Thread t1 = new Thread(messageConsumer, "thread5");
        Thread t2 = new Thread(messageConsumer, "thread6");
        t1.start();
        t2.start();
    }
}

/**
 * 继承JedisPubSub，重写接收消息的方法
 */
class MyJedisPubSub extends JedisPubSub {
    @Override
    /** JedisPubSub类是一个没有抽象方法的抽象类,里面方法都是一些空实现
     * 所以可以选择需要的方法覆盖,这儿使用的是SUBSCRIBE指令，所以覆盖了onMessage
     * 如果使用PSUBSCRIBE指令，则覆盖onPMessage方法
     * 当然也可以选择BinaryJedisPubSub,同样是抽象类，但方法参数为byte[]
     **/
    public void onMessage(String channel, String message) {
        System.out.println(Thread.currentThread().getName()+"-接收到消息:channel=" + channel + ",message=" + message);
        //接收到exit消息后退出
        if (MessageConsumer.EXIT_COMMAND.equals(message)) {
            System.exit(0);
        }
    }
}
