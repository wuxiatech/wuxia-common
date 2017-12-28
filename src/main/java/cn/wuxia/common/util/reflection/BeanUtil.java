/*
 * Created on :Jun 16, 2012 Author :songlin.li
 */
package cn.wuxia.common.util.reflection;

import cn.wuxia.common.util.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jodd.bean.BeanCopy;
import jodd.typeconverter.TypeConverterManager;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.beans.IntrospectionException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeanUtil extends BeanUtils {
    private static Logger logger = LoggerFactory.getLogger(BeanUtil.class);

    static {

        ConvertUtil.registerUtilDateConverter();
        // ConvertUtil.registerDateConverter();
        ConvertUtil.registerSqlBlobConverter();
    }

    /**
     * <pre>
     * override
     * 本方法使用jodd beanutil 扩展支持时间，不同类型值的拷贝。
     *
     * 不建议使用{@link  org.apache.commons.beanutils.BeanUtils#copyProperties(Object, Object)}的复制方法
     *
     * 因为它支持的转换类型如下：<br>
     * java.lang.BigDecimal<br>
     * java.lang.BigInteger<br>
     * boolean and java.lang.Boolean<br>
     * byte and java.lang.Byte<br>
     * char and java.lang.Character<br>
     * java.lang.Class<br>
     * double and java.lang.Double<br>
     * float and java.lang.Float<br>
     * int and java.lang.Integer<br>
     * long and java.lang.Long<br>
     * short and java.lang.Short<br>
     * java.lang.String<br>
     * java.sql.Date<br>
     * java.sql.Time<br>
     * java.sql.Timestamp<br>
     * 这个方法的优点是同属性名不同对象的拷贝，缺点是资源和时间花费较多
     * {@link org.apache.commons.beanutils.PropertyUtils#copyProperties(Object, Object)}
     * 作用与 {@link  org.apache.commons.beanutils.BeanUtils#copyProperties(Object, Object)}的同名方法十分相似，
     * 主要的区别在于后者提供类型转换功能，即发现两个JavaBean的同名属性为不同类型时，在支持的数据类型范围内进行转换，
     * 而前者不支持这个功能，而且速度会更快一些。
     *
     * 另外：如没有特殊要求，简单bean复制可以使用spring下同名方法
     * {@link org.springframework.beans.BeanUtils#copyProperties(Object, Object)}建议在使用spring框架应用下使用
     *
     *
     * </pre>
     *
     * @param target
     * @param source
     * @author songlin.li
     */
    public static void copyProperties(Object target, Object source) {
        BeanCopy.beans(source, target).copy();
    }

    /**
     * @param dest
     * @param orig
     * @description : copy source filed value to target, when source filed value
     * is null then not convert target value
     * @author songlin.li
     */
    public static void copyPropertiesWithoutNullValues(Object dest, Object orig) {
        List<Field> methods = ReflectionUtil.getAccessibleFields(orig);
        for (Field field : methods) {
            try {
                Object value = ReflectionUtil.invokeGetterMethod(orig, field.getName());
                if (value == null) {
                    continue;
                } else {
                    ReflectionUtil.invokeSetterMethod(dest, field.getName(), value);
                }
            } catch (Exception e) {
                // logger.warn(e.getMessage());
            }
        }
    }

    public static <T> T mapToBean(Map<String, Object> map, Class<T> beanClass, String prefix) {
        if (beanClass == null) {
            return null;
        }
        Object bean = null;
        try {
            bean = beanClass.newInstance();
        } catch (IllegalAccessException e) {
            logger.error("", e);
        } catch (InstantiationException e) {
            logger.error("", e);
        }
        mapToBean(map, bean, prefix);
        return (T) bean;
    }

    /**
     * Description of the method
     *
     * @param map
     * @param bean
     * @param prefix
     * @author songlin.li
     */
    public static void mapToBean(Map<String, Object> map, Object bean, String prefix) {
        if (bean == null || map == null) {
            return;
        }

        for (Map.Entry<String, Object> m : map.entrySet()) {
            String fieldName = m.getKey();
            if (StringUtil.isNotBlank(prefix)) {
                if (fieldName.startsWith(prefix)) {
                    fieldName = StringUtil.substringAfter(fieldName, prefix);
                } else {
                    continue;
                }
            }
            try {
                /**
                 * FIXED 当value为数组时，只能获取第一个值
                 */
                if (m.getValue() instanceof String[] && bean instanceof Map) {
                    ((Map) bean).put(fieldName, m.getValue());
                } else {
                    BeanUtils.setProperty(bean, fieldName, m.getValue());
                }
            } catch (Exception e) {
                logger.error("", e);
            }

        }
    }

    /**
     * 将Map转换为目标对象，支持深度转换及List拷贝
     *
     * @param map
     * @param type
     * @return
     */
    public static final <T> T mapToBean(Map<String, ? extends Object> map, Class<T> type) {
        if (MapUtil.isEmpty(map)) {
            return null;
        }
        Object obj = null;
        try {
            obj = type.newInstance();
        } catch (InstantiationException e1) {
            logger.warn(e1.getMessage());
        } catch (IllegalAccessException e1) {
            logger.warn(e1.getMessage());
        }
        List<Field> fields = ReflectionUtil.getAccessibleFields(type);
        for (Field field : fields) {
            String propertyName = field.getName();
            Object value = map.get(propertyName);
            if (value == null)
                continue;
            Class<?> propertyType = field.getType();
            logger.debug("invoke {} set{}({} {}) and value is {} and type is {}", type.getName(), StringUtil.capitalize(propertyName),
                    propertyType.getName(), propertyName, value, value.getClass().getName());
            try {
                if (propertyType.isAssignableFrom(List.class)) {
                    Type genericType = field.getGenericType();
                    /**
                     * 拿到泛型的类型
                     */
                    Type genericValueType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
                    String genericName = genericValueType.getTypeName();
                    Class<?> valueCls = null;
                    try {
                        valueCls = ClassLoaderUtil.loadClass(genericName);
                        logger.debug("泛型类型为<{}>", valueCls);
                        if (value instanceof List) {
                            value = ListUtil.copyProperties(valueCls, (List) value);
                        } else if (value instanceof Map) {
                            value = Lists.newArrayList(mapToBean((Map) value, valueCls));
                        } else {
                            Object v = valueCls.newInstance();
                            copyProperties(v, value);
                            value = Lists.newArrayList(v);
                        }
                    } catch (Exception e) {
                        logger.warn("泛型类型为<{}>", genericName);
                        value = TypeConverterManager.convertType(value, propertyType);
                    }
                } else if (propertyType.isAssignableFrom(Map.class)) {
                    // 如果还是一个Map，则尝试将map复制到obj的对象中
                    if (value instanceof Map) {

                    } else {
                        value = beanToMap(value);
                    }
                } else if (propertyType.isEnum()) {
                    logger.warn("暂不支持枚举转换，解决方法为：" + type.getName() + "请增加set" + StringUtil.capitalize(propertyName) + "(" + value.getClass().getName()
                            + " " + propertyName + ") 方法");
                    ReflectionUtil.invokeSetterMethod(obj, propertyName, value, value.getClass());
                    continue;
                } /* else if (StringUtil.equals(propertyType.getName(), "java.lang.String") && !(value instanceof String)) {
                     value = value.toString();
                  } else if (StringUtil.equals(propertyType.getName(), "java.util.Date") && !(value instanceof Date)) {
                     value = DateUtil.stringToDate(value.toString());
                  } else if (StringUtil.equals(propertyType.getName(), "java.lang.Float") && !(value instanceof Float)) {
                     value = value.toString();
                  } else if (StringUtil.equals(propertyType.getName(), "java.lang.Integer") && !(value instanceof Integer)) {
                     value = NumberUtil.toInteger(value);
                  } else if (StringUtil.equals(propertyType.getName(), "java.lang.Double") && !(value instanceof Double)) {
                     value = NumberUtil.toDouble(value);
                  } else if (StringUtil.equals(propertyType.getName(), "java.lang.Short") && !(value instanceof Short)) {
                     value = NumberUtil.toShort(value.toString());
                  } else if (StringUtil.equals(propertyType.getName(), "java.lang.Long") && !(value instanceof Long)) {
                     value = NumberUtil.toLong(value);
                  }*/ else if (value instanceof Map) {
                    value = mapToBean((Map) value, propertyType);
                }else {
                    //                    value = ConvertUtil.convert(value, propertyType);
                    value = TypeConverterManager.convertType(value, propertyType);
                }
                ReflectionUtil.invokeSetterMethod(obj, propertyName, value, propertyType);
            } catch (Exception e) {
                String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                logger.warn("invoke fail ： {} set{}({} {}) but value type is {}, error msg:", type.getName(), StringUtil.capitalize(propertyName),
                        propertyType.getName(), propertyName, value.getClass().getName(), msg);
                continue;
            }
        }
        return (T) obj;

    }

    /**
     * Converts a JavaBean to a map.
     *
     * @param bean JavaBean to convert
     * @return map converted
     */
    public static final Map<String, Object> beanToMap(Object bean) {
        Map<String, Object> returnMap = new HashMap<String, Object>();
        if (bean == null) {
            return returnMap;
        }

        List<Field> fields = ReflectionUtil.getAccessibleFields(bean);
        for (Field field : fields) {
            try {
                Object value = ReflectionUtil.invokeGetterMethod(bean, field.getName());
                if (value != null) {
                    returnMap.put(field.getName(), value);
                }
            } catch (Exception e) {
                logger.debug(e.getMessage());
            }
        }
        return returnMap;
    }

    /**
     * 比较两个对象的属性值
     *
     * @param dest
     * @param org
     * @param ignore
     * @return
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @author songlin
     */
    public static List<Map<String, Object>> compareProperties(Object dest, Object org, String... ignore)
            throws SecurityException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Assert.isNull(org, "对比原对象不能为空");
        Assert.isNull(dest, "对比目标对象不能为空");

        List<Map<String, Object>> list = Lists.newArrayList();
        Field[] fields = org.getClass().getDeclaredFields();
        BeanUtilsBean bean = BeanUtilsBean.getInstance();
        for (Field field : fields) {
            String propertyName = field.getName();
            /**
             * 忽略不需比较的属性
             */
            if (ArrayUtil.contains(ignore, propertyName)) {
                continue;
            }

            String type = field.getGenericType().toString(); // 获取属性的类型
            if (bean.getPropertyUtils().isWriteable(dest, propertyName)) {
                Object value = ReflectionUtil.invokeGetterMethod(org, propertyName);
                Object destValue = ReflectionUtil.invokeGetterMethod(dest, propertyName);
                if (value == null && destValue == null) {
                    continue;
                }

                Map<String, Object> rec = Maps.newHashMap();
                rec.put("fieldName", propertyName);
                if (type.equals("class java.lang.String")) { // 如果type是类类型，则前面包含"class
                    // "，后面跟类名

                    String _newV = (String) destValue;
                    String _orgV = (String) value;

                    String newValue = StringUtils.isBlank(_newV) ? "" : _newV.trim();
                    String originalValue = StringUtils.isBlank(_orgV) ? "" : _orgV.trim();
                    if ((StringUtils.isNotBlank(newValue) && !newValue.equals(originalValue))
                            || (StringUtils.isNotBlank(originalValue) && !originalValue.equals(newValue))) {
                        rec.put("newValue", newValue);
                        rec.put("originalValue", originalValue);
                        list.add(rec);
                    }
                }
                if (type.equals("class java.lang.Integer")) {
                    if (NumberUtil.compare((Integer) destValue, (Integer) value) != 0) {
                        rec.put("newValue", destValue);
                        rec.put("originalValue", value);
                        list.add(rec);
                    }
                }
                if (type.equals("class java.lang.Short")) {
                    if (NumberUtil.compare((Short) destValue, (Short) value) != 0) {
                        rec.put("newValue", destValue);
                        rec.put("originalValue", value);
                        list.add(rec);
                    }
                }
                if (type.equals("class java.lang.Double")) {
                    if (NumberUtil.compare((Double) destValue, (Double) value) != 0) {
                        rec.put("newValue", destValue);
                        rec.put("originalValue", value);
                        list.add(rec);
                    }
                }
                if (type.equals("class java.lang.Boolean")) {
                    if (!((Boolean) destValue).equals((Boolean) value)) {
                        rec.put("newValue", destValue);
                        rec.put("originalValue", value);
                        list.add(rec);
                    }
                }
                if (type.equals("class java.util.Date")) {
                    Date newValue = (Date) destValue;
                    Date originalValue = (Date) value;

                    if ((newValue != null && originalValue != null && !DateUtil.isSameDay(newValue, originalValue))) {
                        rec.put("newValue", newValue);
                        rec.put("originalValue", originalValue);
                        list.add(rec);
                    } else if ((newValue != null && originalValue == null)) {
                        rec.put("newValue", newValue);
                        rec.put("originalValue", null);
                        list.add(rec);
                    } else if ((newValue == null && originalValue != null)) {
                        rec.put("newValue", null);
                        rec.put("originalValue", originalValue);
                        list.add(rec);
                    }
                }
            }
        }
        return list;
    }

    public static void main(String[] args) throws Exception {
        CopyBean1 p1 = new CopyBean1();
        CopyBean2 p2 = new CopyBean2();
        //         p2.setTestDate(new Date());
        //         p2.setNumber(2);
        p1.setTestDate("2017-12-30");
        p1.setNumber("2");
        // copyProperties(p2, p1);
        //        copyProperties(p1, p2);
        //        org.apache.commons.beanutils.PropertyUtils.copyProperties(p2, p1);
        //         org.springframework.beans.BeanUtils.copyProperties(p1, p2);
        BeanCopy.beans(p1, p2).copy();
        System.out.println(p1.getTestDate());
        System.out.println(p2.getTestDate());
        System.out.println(p1.getNumber());
        System.out.println(p2.number);
    }
}

class CopyBean1 {
    String testDate;

    String number;

    public String getTestDate() {
        return testDate;
    }

    public void setTestDate(String testDate) {
        this.testDate = testDate;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}

class CopyBean2 {
    Date testDate;

    Integer number;

    public Date getTestDate() {
        return testDate;
    }

    public void setTestDate(Date testDate) {
        this.testDate = testDate;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }
}
