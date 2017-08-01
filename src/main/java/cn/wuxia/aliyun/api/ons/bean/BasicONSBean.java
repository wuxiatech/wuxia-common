/*
* Created on :2017年2月13日
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 wuxia.gd.cn All right reserved.
*/
package cn.wuxia.aliyun.api.ons.bean;

import com.aliyun.openservices.ons.api.bean.Subscription;

public class BasicONSBean extends Subscription {
    private BusinessMQ business;

    private Boolean istartup = Boolean.TRUE;

    public BusinessMQ getBusiness() {
        return business;
    }

    public void setBusiness(BusinessMQ business) {
        this.business = business;
    }

    public Boolean isIstartup() {
        return istartup;
    }

    public void setIstartup(Boolean istartup) {
        this.istartup = istartup;
    }

}
