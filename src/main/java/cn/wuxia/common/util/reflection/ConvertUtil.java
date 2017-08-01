/**
 * Copyright (c) 2005-2010 springside.org.cn Licensed under the Apache License,
 * Version 2.0 (the "License"); $Id: ConvertUtils.java 1211 2010-09-10 16:20:45Z
 * calvinxiu $
 */
package cn.wuxia.common.util.reflection;

import java.sql.Clob;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.sql.rowset.serial.SerialClob;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.wuxia.common.util.DateUtil;
import cn.wuxia.common.util.DateUtil.DateFormatter;

/**
 * [ticket id] Description of the class
 * 
 * @author songlin.li @ Version : V<Ver.No> <Jun 16, 2012>
 */
@SuppressWarnings("rawtypes")
public class ConvertUtil extends ConvertUtils {
    private static Logger logger = LoggerFactory.getLogger(ConvertUtil.class);
    static {
        registerDateConverter();
        registerUtilDateConverter();
        registerSqlBlobConverter();
    }

    /**
     * Extraction of the objects in the collection attributes ( getter functions
     * ) , into the List.
     * 
     * @param collection Source collection.
     * @param propertyName Want to extract the attribute name.
     */
    public static List convertElementPropertyToList(final Collection<?> collection, final String propertyName) {
        List<Object> list = new ArrayList<Object>();
        try {
            for (Object obj : collection) {
                list.add(PropertyUtils.getProperty(obj, propertyName));
            }
        } catch (Exception e) {
            throw ReflectionUtil.convertReflectionExceptionToUnchecked(e);
        }
        return list;
    }

    /**
     * 获取指定List Map集合的某个key的所有值
     * @author songlin
     * @param collection
     * @param key
     * @return
     */
    public static List<?> convertEntryKeyToList(final Collection<?> collection, final String key) {
        List<Object> list = new ArrayList<Object>();
        try {
            for (Object obj : collection) {
                if (obj instanceof Map) {
                    list.add(MapUtils.getObject((Map) obj, key));
                }
            }
        } catch (Exception e) {
            throw ReflectionUtil.convertReflectionExceptionToUnchecked(e);
        }
        return list;
    }

    /**
     * Extract the objects in the collection attributes ( through a getter
     * function ) , combined into a string separated by the separator.
     * 
     * @param collection Source collection.
     * @param propertyName Want to extract the attribute name.
     * @param separator separating character.
     */
    public static String convertElementPropertyToString(final Collection<?> collection, final String propertyName, final String separator) {
        List list = convertElementPropertyToList(collection, propertyName);
        return StringUtils.join(list, separator);
    }

    /**
     * 将集合的某个key的值以指定分隔符拼接字符串
     * @author songlin
     * @param collection
     * @param key
     * @param separator
     * @return
     */
    public static String convertEntryKeyToString(final Collection<?> collection, final String key, final String separator) {
        List<?> list = convertEntryKeyToList(collection, key);
        return StringUtils.join(list, separator);
    }

    /**
     * Convert the string to the appropriate type.
     * 
     * @param value To be converted string.
     * @param toType Convert the target type.
     */
    public static Object convertToObject(String value, Class<?> toType) {
        try {
            return ConvertUtils.convert(value, toType);
        } catch (Exception e) {
            throw ReflectionUtil.convertReflectionExceptionToUnchecked(e);
        }
    }

    /**
     * 转换字符串数组到相应类型.
     * 
     * @param value 待转换的字符串.
     * @param toType 转换目标类型.
     */
    public static Object convertToObject(String[] values, Class<?> toType) {
        try {
            return convert(values, toType);
        } catch (Exception e) {
            throw ReflectionUtil.convertReflectionExceptionToUnchecked(e);
        }
    }

    /**
     * defined Converter format yyyy-MM-dd , yyyy-MM-dd HH:mm:ss or dd/MM/yyyy
     * dd/MM/yyyy HH:mm:ss
     */
    public static void registerDateConverter() {
        DateConverter dc = new DateConverter();
        dc.setUseLocaleFormat(true);
        dc.setPatterns(new String[] { DateFormatter.FORMAT_YYYY_MM_DD.getFormat(), DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS.getFormat(),
                DateFormatter.FORMAT_YYYY_MM_DD_EEE.getFormat(), DateFormatter.FORMAT_DD_MM_YYYY.getFormat(),
                DateFormatter.FORMAT_DD_MM_YYYY_HH_MM_SS.getFormat(), DateFormatter.FORMAT_DD_MMM_YYYY_HH_MM_SS.getFormat(),
                DateFormatter.FORMAT_DD_MMM_YYYY.getFormat() });
        register(dc, Date.class);
    }

    public static void registerUtilDateConverter() {
        register(new UtilDateConvert(), java.util.Date.class);
    }

    public static void registerSqlBlobConverter() {
        register(new SqlClobConverter(), java.sql.Clob.class);
    }

}

final class UtilDateConvert implements Converter {

    public <T> T convert(Class<T> arg0, Object date) {

        if (date == null) {
            return null;
        } else if (date instanceof Date) {
            return (T)date;
        }

        Date d = null;
        try {
            d = DateUtil.stringToDate((String) date);
        } catch (Exception e) {
            d = DateUtil.stringToDate((String) date, DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS);
            if (d == null) {
                d = DateUtil.stringToDate((String) date, DateFormatter.FORMAT_YYYY_MM_DD);
            }
        }
        return (T)d;
    }
}

/**
 * @author linhl
 * @version 1.0
 * @since 2006-10-27
 * @lastest modify date 2006-10-27
 * @description java.util.Date类转换器，主要用来把从request里得到的value如果原来字段对应的是java.util.
 *              Date类型的则把value转化成java.util.Date类型。
 *              主要是参考apache.commons.beanutils.converters里的SqlDateConverter类实现的。
 */
final class SqlClobConverter implements Converter {

    // ----------------------------------------------------------- Constructors

    /**
     * Create a {@link Converter} that will throw a {@link ConversionException}
     * if a conversion error occurs.
     */
    public SqlClobConverter() {
        this.defaultValue = null;
        this.useDefault = false;

    }

    /**
     * Create a {@link Converter} that will return the specified default value
     * if a conversion error occurs.
     * 
     * @param defaultValue
     *            The default value to be returned
     */
    public SqlClobConverter(Object defaultValue) {
        this.defaultValue = defaultValue;
        this.useDefault = true;

    }

    // ----------------------------------------------------- Instance Variables

    /**
     * The default value specified to our Constructor, if any.
     */
    private Object defaultValue = null;

    /**
     * Should we return the default value on conversion errors?
     */
    private boolean useDefault = true;

    // --------------------------------------------------------- Public Methods

    /**
     * Convert the specified input object into an output object of the specified
     * type.
     * 
     * @param type
     *            Data type to which this value should be converted
     * @param value
     *            The input value to be converted
     * 
     * @exception ConversionException
     *                if conversion cannot be performed successfully
     */
    @SuppressWarnings("unchecked")
    public Object convert(Class type, Object value) {
        if (value == null) {
            if (useDefault) {
                return (defaultValue);
            } else {
                throw new ConversionException("No value specified");
            }
        }

        if (value instanceof Clob) {
            return (value);
        }

        try {
            // 核心句：把value转成Clob类型
            char[] ch = new char[255];
            SerialClob clob = new SerialClob(ch);
            // System.out.println("1208:" + value.toString());
            clob.setString(1, value.toString());
            return (Clob) (clob);
        } catch (Exception e) {
            if (useDefault) {
                return (defaultValue);
            } else {
                throw new ConversionException(e);
            }
        }
    }

}
