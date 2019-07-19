/*
 * Created on :2016年8月18日
 * Author     :songlin
 * Change History
 * Version       Date         Author           Reason
 * <Ver.No>     <date>        <who modify>       <reason>
 * Copyright 2014-2020 武侠科技 All right reserved.
 */
package cn.wuxia.common.util;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.collections.MapUtils;

import com.google.common.collect.Maps;

import cn.wuxia.common.util.reflection.BeanUtil;

public class MapUtil extends MapUtils {

    /**
     * 获取某前缀开头的结果集
     *
     * @param map
     * @param prefix
     * @return
     * @author songlin
     */
    public static Map<String, Object> getPrefixMap(final Map<String, Object> map, final String prefix) {
        Map<String, Object> m = Maps.newHashMap();
        for (Map.Entry<String, Object> s : map.entrySet()) {
            if (StringUtil.startsWith(s.getKey(), prefix)) {
                String key = StringUtil.substringAfter(s.getKey(), prefix);
                m.put(key, s.getValue());
            }
        }
        return m;
    }

    /**
     * 转换
     *
     * @param map
     * @return
     * @author songlin
     */
    public static Map<String, String> convert2string(Map<String, Object> map) {
        return Maps.transformEntries(map, new Maps.EntryTransformer<String, Object, String>() {
            @Override
            public String transformEntry(String key, Object value) {
                return StringUtil.isBlank(value) ? "" : value.toString();
            }
        });
    }

    public static <T> Map<String, T> convert2T(Map<String, Object> map) {
        return Maps.transformEntries(map, new Maps.EntryTransformer<String, Object, T>() {
            @Override
            public T transformEntry(String key, Object value) {
                return StringUtil.isBlank(value) ? null : (T) value;
            }
        });
    }
    // @SuppressWarnings("unchecked")
    // public static Map<String, String> convert2string(Map<String, Object> map)
    // {
    // return (Map)Collections.checkedMap(map, String.class, Object.class);
    //
    // }

    /**
     * 转换
     *
     * @param map
     * @return
     * @author songlin
     */
    public static Map<String, Object> convert2object(Map<String, String> map) {
        return Maps.transformEntries(map, new Maps.EntryTransformer<String, String, Object>() {
            @Override
            public Object transformEntry(String key, String value) {
                return value;
            }
        });
    }

    // @SuppressWarnings("unchecked")
    // public static Map<String, Object> convert2object(Map<String, String> map)
    // {
    // return (Map)Collections.checkedMap(map, String.class, String.class);
    // }

    /**
     * 转换为有序map
     *
     * @param map
     * @return
     */
    public static SortedMap<String, Object> covert2sorted(Map<String, Object> map) {
        return new TreeMap<>(map);
    }

    /**
     * 将Map转换为目标对象，支持深度转换及List拷贝 <br>
     * 不支持枚举，请自行在对象中设置如：
     *
     * <pre>
     * public ResultType getResultType() {
     * 	return resultType;
     * }
     *
     * public void setResultType(ResultType resultType) {
     * 	this.resultType = resultType;
     * }
     *
     * public void setResultType(String resultType) {
     * 	try {
     * 		this.resultType = ResultType.valueOf(resultType);
     *    } catch (Exception e) {
     * 		e.printStackTrace();
     *    }
     * }
     * </pre>
     *
     * @param map
     * @param type
     * @return
     */
    public static final <T> T mapToBean(Map<String, ? extends Object> map, Class<T> type) {
        return BeanUtil.mapToBean(map, type);
    }
}
