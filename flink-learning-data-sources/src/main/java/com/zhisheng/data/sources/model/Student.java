package com.zhisheng.data.sources.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Desc:
 * Created by zhisheng on 2019-02-17
 * Blog: http://www.54tianzhisheng.cn/tags/Flink/
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    public int id;
    public String name;
    public String password;
    public int age;

//    public Student(int id, String name, String password, int age) {
//        this.id = id;
//        this.name = name;
//        this.password = password;
//        this.age = age;
//    }
}
