package com.zhisheng.connectors.redis;

/**
 * Created on 2019-12-06
 *
 * @author :hao.li
 */

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.sql.Timestamp;
import java.util.Properties;
import java.util.Random;

/**
 * 向kafka中写数据
 * 可使用此main函数进行测试一下
 */

public class KafkaProduce {
    public static final String broker_list = "10.3.7.232:9092,10.3.7.2323:9092,10.3.7.234:9092";
    public static final String topic = "transaction";  //kafka topic 需要和 flink 程序用同一个 topic

    public static void writeToKafka() throws InterruptedException {
        Properties props = new Properties();
        props.put("bootstrap.servers", broker_list);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        KafkaProducer producer = new KafkaProducer<String, String>(props);

        while (true) {
            Integer transaction_id = (int) (1 + Math.random() * (1000000000 - 1 + 1));
            Integer card_number = (int) (1 + Math.random() * (100000000 - 1 + 1));
            Integer terminal_id = (int) (1 + Math.random() * (10000000 - 1 + 1));
            Timestamp transaction_time = new Timestamp(System.nanoTime());
            Integer transaction_type = (int) (1 + Math.random() * (8 - 1 + 1));
            Float amount = new Random().nextFloat() * 10000;

//            Transaction transaction = new Transaction(transaction_id,card_number,terminal_id,transaction_time,transaction_type,amount);
//            String a = JSON.toJSONString(transaction);
            String a = transaction_id + ":" + card_number;
            ProducerRecord record = new ProducerRecord<String, String>(topic, null, null, a);
            producer.send(record);
            System.out.println("发送数据: " + a);
            Thread.sleep(5000);
            producer.flush();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        writeToKafka();
    }
}
