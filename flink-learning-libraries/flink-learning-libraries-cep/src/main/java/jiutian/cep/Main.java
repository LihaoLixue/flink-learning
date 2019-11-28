package jiutian.cep;

<<<<<<< HEAD:flink-learning-libraries/flink-learning-libraries-gelly/src/main/java/com/jiutian/cep/Main.java
package com.jiutian.cep;
=======
package com.zhisheng.libraries.cep;
>>>>>>> 3909df68d381d3c6d84945719e998a30c9af1411:flink-learning-libraries/flink-learning-libraries-cep/src/main/java/com/zhisheng/libraries/cep/Main.java


import com.zhisheng.common.model.MetricEvent;
import com.zhisheng.common.utils.ExecutionEnvUtil;
import com.zhisheng.common.utils.KafkaConfigUtil;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * blog：http://www.54tianzhisheng.cn/
 * 微信公众号：zhisheng
 */
public class Main {
    public static void main(String[] args) throws Exception{
        final ParameterTool parameterTool = ExecutionEnvUtil.createParameterTool(args);
        StreamExecutionEnvironment env = ExecutionEnvUtil.prepare(parameterTool);
        DataStreamSource<MetricEvent> data = KafkaConfigUtil.buildSource(env);
        data.print();

        env.execute("flink learning cep");
    }
}
