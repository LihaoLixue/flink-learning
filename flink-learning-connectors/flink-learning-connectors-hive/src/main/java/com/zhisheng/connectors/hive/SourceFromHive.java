package com.zhisheng.connectors.hive;

/**
 * Created on 2019-11-29
 *
 * @author :hao.li
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import java.sql.*;

public class SourceFromHive {
    public static void main(String[] args) throws Exception {

//        Class.forName("org.apache.hive.jdbc.HiveDriver");
//        Connection con = DriverManager.getConnection("jdbc:hive2://172.10.4.143:10000/aijiami", "hive", "hive");
//        Statement st = con.createStatement();
//        ResultSet rs = st.executeQuery("SELECT * from ods_scenes_detail_new limit 10");
//        while (rs.next()) {
//            System.out.println(rs.getString(1) + "," + rs.getString(2));
//        }
//        rs.close();
//        st.close();
//        con.close();

testConnectAndQuery();
    }

    public static void testConnectAndQuery() throws Exception {
//注册数据库驱动，用的hive的jdbc，驱动名固定写死
        Class.forName("org.apache.hive.jdbc.HiveDriver");
//如果用的是hive2服务，则写jdbc:hive2，后面跟上hive服务器的ip以及端口号，端口号默认是10000
        Connection conn = DriverManager.getConnection("jdbc:hive2://10.3.7234:10000/userdb", "root", "123456");
        Statement stat = conn.createStatement();
        ResultSet rs = stat.executeQuery("select * from test");
        while (rs.next()) {
            String name = rs.getString("name");
            System.out.println(name);
        }

        stat.close();
        conn.close();
    }

}


