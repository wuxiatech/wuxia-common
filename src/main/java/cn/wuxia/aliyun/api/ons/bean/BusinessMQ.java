/*
* Created on :2017年3月9日
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 wuxia.gd.cn All right reserved.
*/
package cn.wuxia.aliyun.api.ons.bean;

import java.io.Serializable;

public class BusinessMQ implements Serializable {
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;

    String name;

    boolean order_;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOrder_() {
        return order_;
    }

    public void setOrder_(boolean order_) {
        this.order_ = order_;
    }

    public BusinessMQ() {
    }

    public BusinessMQ(String name, boolean order_) {
        this.name = name;
        this.order_ = order_;
    }
}
