/*
* Created on :2017年2月12日
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 wuxia.gd.cn All right reserved.
*/
package cn.wuxia.aliyun.api.ons.handler;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import cn.wuxia.aliyun.api.ons.bean.BasicONSBean;
import cn.wuxia.aliyun.api.ons.bean.BusinessMQ;
import cn.wuxia.aliyun.api.ons.bean.ONSAccountBean;
import cn.wuxia.aliyun.api.ons.exception.MQException;
import cn.wuxia.aliyun.api.ons.producer.bean.BasicProducerBean;
import cn.wuxia.aliyun.api.ons.producer.bean.OrderProducerBean;
import cn.wuxia.aliyun.api.ons.producer.bean.ProducerONSBean;
import cn.wuxia.aliyun.api.ons.producer.bean.UnorderProducerBean;
import cn.wuxia.common.util.ListUtil;
import cn.wuxia.common.util.MapUtil;

public class ProducerHandler {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, BasicProducerBean> producersMap;

    private List<BasicONSBean> producers;

    private ONSAccountBean accountBean;

    public void start() throws MQException {
        if (ListUtil.isEmpty(getProducers())) {
            logger.info("生产者队列为空");
            return;
        }
        if (null == getAccountBean()) {
            throw new MQException("账号信息不能为空");
        }
        if (MapUtil.isEmpty(producersMap)) {
            producersMap = Maps.newHashMap();
        }
        for (BasicONSBean bean : getProducers()) {
            /**
             * 循环拿到生产者部分
             */
            if (bean != null && bean instanceof ProducerONSBean) {
                ProducerONSBean onsBean = (ProducerONSBean) bean;
                BusinessMQ business = onsBean.getBusiness();
                if (business.isOrder_()) {
                    OrderProducerBean producerBean = new OrderProducerBean();
                    producerBean.setProducerBean(onsBean);
                    producerBean.setAccountBean(getAccountBean());
                    producerBean.start();
                    producersMap.put(business.getName(), producerBean);
                } else {
                    UnorderProducerBean producerBean = new UnorderProducerBean();
                    producerBean.setProducerBean(onsBean);
                    producerBean.setAccountBean(getAccountBean());
                    producerBean.start();
                    producersMap.put(business.getName(), producerBean);
                }
            }
        }
    }

    public ONSAccountBean getAccountBean() {
        return accountBean;
    }

    public void setAccountBean(ONSAccountBean accountBean) {
        this.accountBean = accountBean;
    }

    public void shutdown() {
        if (MapUtil.isNotEmpty(producersMap)) {
            for (Map.Entry<String, BasicProducerBean> producer : producersMap.entrySet()) {
                producer.getValue().shutdown();
            }
        }

    }

    public BasicProducerBean getProducerBean(String business) {
        if (MapUtil.isNotEmpty(producersMap)) {
            return producersMap.get(business);
        }
        return null;
    }

    public List<BasicONSBean> getProducers() {
        return producers;
    }

    public void setProducers(List<BasicONSBean> producers) {
        this.producers = producers;
    }

}
