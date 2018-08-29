/*
* Created on :2017年2月24日
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 wuxia.gd.cn All right reserved.
*/
package cn.wuxia.common.spring.orm.mongo;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bson.Document;

public class MongoSort {

    /** key为排序的名称, value为顺序 */
    private Map<String, MongoOrder> field = new LinkedHashMap<String, MongoOrder>();

    public MongoSort() {
    }

    public MongoSort(String key, MongoOrder order) {
        field.put(key, order);
    }

    public MongoSort asc(String key) {
        field.put(key, MongoOrder.ASC);
        return this;
    }

    public MongoSort desc(String key) {
        field.put(key, MongoOrder.DESC);
        return this;
    }

    public Document getSortObject() {
        Document dbo = new Document();
        for (String k : field.keySet()) {
            dbo.put(k, (field.get(k).equals(MongoOrder.ASC) ? 1 : -1));
        }
        return dbo;
    }
}
