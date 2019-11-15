package com.zhisheng.data.sources.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created on 2019-10-21
 *
 * @author :hao.li
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Test {
    public int id;
    public int ns;
    public int value;
    public int clock;
    public int itemid;

    public int getId() {
        return id;
    }

    public int getNs() {
        return ns;
    }

    public int getValue() {
        return value;
    }

    public int getClock() {
        return clock;
    }

    public int getItemid() {
        return itemid;
    }
}
