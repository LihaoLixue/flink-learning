package com.lihao.hive;

/**
 * Created on 2019-12-06
 *
 * @author :hao.li
 */

import com.alibaba.fastjson.JSON;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

import java.util.Properties;

public class Main {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties props = new Properties();
        props.put("bootstrap.servers", "10.3.7.234:9092,10.3.7.233:9092,10.3.7.232:9092");
        props.put("zookeeper.connect", "10.3.7.234:2181,10.3.7.233:2181,10.3.7.232:2181");
        props.put("group.id", "bigdata-transaction");       //需要修改groupid
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "latest");

        SingleOutputStreamOperator<Transaction> transaction = env.addSource(
                new FlinkKafkaConsumer<>(
                        "transaction"
                        , new SimpleStringSchema()
                        , props
                )
                        .setStartFromLatest()
        )
                .setParallelism(1)
                .map(string -> JSON.parseObject(string, Transaction.class));
        transaction.print();
                transaction.addSink(new HiveSink()); //数据 sink 到 mysql

        env.execute("Flink add sink");
    }

}
