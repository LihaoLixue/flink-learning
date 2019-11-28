package jiutian.cep.cep;

/**
 * Created on 2019-09-12
 *
 * @author :hao.li
 */

import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FlinkLoginFail {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<LoginEvent> loginEventStream = env.fromCollection(Arrays.asList(
                new LoginEvent("1","192.168.0.1","fail"),
                new LoginEvent("1","192.168.0.2","fail"),
                new LoginEvent("1","192.168.0.3","fail"),
                new LoginEvent("1","192.168.10,10","success"),
                new LoginEvent("1","192.168.0.4","fail"),
                new LoginEvent("1","192.168.0.5","fail"),
                new LoginEvent("1","192.168.0.6","fail"),
                new LoginEvent("1","192.168.0.7","fail"),
                new LoginEvent("2","192.168.10,10","success")

        ));

        Pattern<LoginEvent, LoginEvent> loginFailPattern = Pattern.<LoginEvent>
                begin("begin")
                .where(new IterativeCondition<LoginEvent>() {
                    @Override
                    public boolean filter(LoginEvent loginEvent, Context context) throws Exception {
                        return loginEvent.getType().equals("fail");
                    }
                })
                .next("next")
                .where(new IterativeCondition<LoginEvent>() {
                    @Override
                    public boolean filter(LoginEvent loginEvent, Context context) throws Exception {
                        return loginEvent.getType().equals("fail");
                    }
                })
                .within(Time.seconds(3))
                .times(5);

        PatternStream<LoginEvent> patternStream = CEP.pattern(
                loginEventStream.keyBy(LoginEvent::getUserId),
                loginFailPattern);

        DataStream<LoginWarning> loginFailDataStream = patternStream.select((Map<String, List<LoginEvent>> pattern) -> {
            List<LoginEvent> first = pattern.get("begin");
            List<LoginEvent> second = pattern.get("next");

//            return new LoginWarning(second.get(0).getUserId(),second.get(0).getIp(), second.get(0).getType());
            return new LoginWarning(first.get(0).getUserId(),first.get(0).getIp(), first.get(0).getType());
        });

        loginFailDataStream.print();

        env.execute();
    }
}
