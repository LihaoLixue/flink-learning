package com.lihao.hive;

/**
 * Created on 2019-12-06
 *
 * @author :hao.li
 */
import java.sql.*;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import java.sql.PreparedStatement;


public class HiveSink extends RichSinkFunction<Transaction> {
    private PreparedStatement state ;
    private Connection conn ;
    private String querySQL = "";
//    private String sql;

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        conn = getConnection();

        querySQL = "insert into transactions(transaction_id,card_number,terminal_id,transaction_time,transaction_type,amount) values(?,?,?,?,?,?)";
//        querySQL = "insert into transactions values (? ,?, ?, ?, ?, ?)";
        state = conn.prepareStatement(querySQL);

    }

    @Override
    public void close() throws Exception {
        super.close();
        if (state != null) {
            state.close();
        }
        if (conn != null) {
            conn.close();
        }

    }

    @Override
    public void invoke(Transaction value, Context context) throws Exception {
        state.setLong(1,value.getTransaction_id());
        state.setLong(2,value.getCard_number());
        state.setLong(3,value.getTerminal_id());
        state.setString(4,value.getTransaction_time().toString());
        state.setInt(5,value.getTransaction_type());
        state.setDouble(6,value.getAmount());
//        state.execute(querySQL);
        state.executeUpdate();
    }

    private static Connection getConnection() {
        Connection conn = null;
        try {
            String driverName = "org.apache.hive.jdbc.HiveDriver";
            String Url = "jdbc:hive2://10.3.7.234:10000/userdb";
            Class.forName(driverName);
            conn = DriverManager.getConnection(Url,"root","dtc2019234!");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }


    public static void main(String[] args) {
        getConnection();
    }

}
