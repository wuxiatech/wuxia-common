/*
* Created on :2015年7月15日
* Author     :Administrator
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 www.ibmall.cn All right reserved.
*/
package cn.wuxia.qiyukf.openapi.session.util;

import java.util.ArrayList;
import java.util.List;

import cn.wuxia.qiyukf.openapi.session.model.DuplicateRemovalMessage;
import cn.wuxia.qiyukf.openapi.session.model.QiyuMessage;

/**
 * 排重工具
 * 
 * @author guwen @ Version : V<Ver.No> <2016年1月8日>
 */
public class RepeatedUtil {

    private static final int MESSAGE_CACHE_SIZE = 1000;

    private static List<DuplicateRemovalMessage> MESSAGE_CACHE = new ArrayList<DuplicateRemovalMessage>(MESSAGE_CACHE_SIZE);

    /** 
     * @Description: 判断微信请求是否重复 
     * @return boolean 如果重复返回true 
     */
    public static synchronized boolean isDuplicate(QiyuMessage receive) {
        String createTime = receive.getTimeStamp() + "";
        String msgId = receive.getMsgId();

        DuplicateRemovalMessage duplicateRemovalMessage = new DuplicateRemovalMessage();

        if (msgId != null) {
            duplicateRemovalMessage.setMsgId(msgId);
        } else {
            duplicateRemovalMessage.setTimeStamp(createTime);
            duplicateRemovalMessage.setUid(receive.getUid());
            ;
        }

        if (MESSAGE_CACHE.contains(duplicateRemovalMessage)) {
            // 缓存中存在，直接pass  
            return true;
        } else {
            setMessageToCache(duplicateRemovalMessage);
            return false;
        }
    }

    private static void setMessageToCache(DuplicateRemovalMessage duplicateRemovalMessage) {
        if (MESSAGE_CACHE.size() >= MESSAGE_CACHE_SIZE) {
            MESSAGE_CACHE.remove(0);
        }
        MESSAGE_CACHE.add(duplicateRemovalMessage);
    }

}
