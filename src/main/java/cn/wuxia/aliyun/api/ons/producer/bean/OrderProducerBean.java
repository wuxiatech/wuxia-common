/*
* Created on :2017年1月20日
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 wuxia.gd.cn All right reserved.
*/
package cn.wuxia.aliyun.api.ons.producer.bean;

import java.util.Properties;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.SendResult;
import com.aliyun.openservices.ons.api.exception.ONSClientException;
import com.aliyun.openservices.ons.api.order.OrderProducer;

import cn.wuxia.aliyun.api.ons.bean.MessageBean;
import cn.wuxia.aliyun.api.ons.exception.MQException;
import cn.wuxia.common.exception.ValidateException;
import cn.wuxia.common.util.ValidatorUtil;


public class OrderProducerBean extends BasicProducerBean {

    private OrderProducer orderProducer;

    /**
     * Description of the method
     * @author songlin
     */
    public void start() {
        Properties properties = accountBean.toProperties();
        properties.setProperty("ProducerId", producerBean.ProducerId);
        orderProducer = ONSFactory.createOrderProducer(properties);
        orderProducer.start();
        logger.info("开始监听有序生产队列：{}", producerBean);
    }

    public void shutdown() {
        orderProducer.shutdown();
    }

    public SendResult send(MessageBean bean) throws MQException, ValidateException {
        ValidatorUtil.validate(bean);
        try {
            // 分区顺序消息中区分不同分区的关键字段，sharding key于普通消息的key是完全不同的概念。
            // 全局顺序消息，该字段可以设置为任意非空字符串。
            Message message = new Message(producerBean.getTopic(), producerBean.getExpression(), bean.getKey(), bean.getBody());
            if (null != bean.getDelayTime()) {
                message.setStartDeliverTime(System.currentTimeMillis() + bean.getDelayTime());
            }
            if (null != bean.getStartDate()) {
                long timeStamp = bean.getStartDate().getTime();
                message.setStartDeliverTime(timeStamp);
            }
            return orderProducer.send(message, bean.getKey());
        } catch (ONSClientException e) {
            throw new MQException("", e);
        } catch (Exception e) {
            throw new MQException("", e);
        }
    }
}
