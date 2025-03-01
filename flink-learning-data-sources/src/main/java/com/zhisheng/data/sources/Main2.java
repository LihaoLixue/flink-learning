package com.zhisheng.data.sources;

import com.zhisheng.data.sources.model.Student;
import com.zhisheng.data.sources.model.Test;
import com.zhisheng.data.sources.sources.SourceFromMySQL;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * Desc: 自定义 source，从 mysql 中读取数据
 * Created by zhisheng on 2019-02-17
 * Blog: http://www.54tianzhisheng.cn/tags/Flink/
 */
public class Main2 {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<Student> testDataStreamSource = env.addSource(new SourceFromMySQL());
        testDataStreamSource.print();

        env.execute("Flink add data sourc");
    }
}
