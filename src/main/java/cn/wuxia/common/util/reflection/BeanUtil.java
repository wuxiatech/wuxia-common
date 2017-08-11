/*
 * Created on :Jun 16, 2012 Author :songlin.li
 */
package cn.wuxia.common.util.reflection;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import cn.wuxia.common.util.ArrayUtil;
import cn.wuxia.common.util.ClassLoaderUtil;
import cn.wuxia.common.util.DateUtil;
import cn.wuxia.common.util.ListUtil;
import cn.wuxia.common.util.MapUtil;
import cn.wuxia.common.util.NumberUtil;
import cn.wuxia.common.util.StringUtil;

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
     * {@link  org.apache.commons.beanutils.BeanUtils.copyProperties}的复制方法，增加对java.util.Date的支持<br>
     * 这里要注意一点，
     * {@link org.apache.commons.beanutils.BeanUtils.copyProperties}
     * java.util.Date是不被支持的，而它的子类java.sql.Date是被支持的。
     * 因此如果对象包含时间类型的属性，且希望被转换的时候，一定要使用java.sql.Date类型。 否则在转换时会提示argument
     * mistype异常。
     * BeanUtils支持的转换类型如下：<br>
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
     * 如有可能尽量选择spring下同名方法
     * {@link org.springframework.beans.BeanUtils} 
     * 建议在使用spring框架应用下使用此方法
     * {@link org.apache.commons.beanutils.PropertyUtils}
     * PropertyUtils.copyProperties作用与 BeanUtils.copyProperties的同名方法十分相似，
     * 主要的区别在于后者提供类型转换功能，即发现两个JavaBean的同名属性为不同类型时，在支持的数据类型范围内进行转换，
     * 而前者不支持这个功能，而且速度会更快一些。
     * </pre>
     * 
     * @author songlin.li
     * @param target
     * @param source
     */
    public static void copyProperties(Object target, Object source) {
        try {
            BeanUtils.copyProperties(target, source);
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * @author linhl
     * @version 1.0
     * @since 2006-7-4
     * @lastest modify date 2006-7-4
     * @description 主要是：避免在编辑更新时以NULL值覆盖表单上没有出现的值;简化从request得到表单上值到目标对象的过程。
     */
    public static void copyProperties(Object dest, HttpServletRequest request)
            throws ServletException, IOException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, Exception {
        Assert.notNull(dest, "No destination bean specified");
        Enumeration<String> enum1 = request.getParameterNames();
        BeanUtilsBean bean = BeanUtilsBean.getInstance();
        while (enum1.hasMoreElements()) {
            String name = (String) enum1.nextElement();
            if (bean.getPropertyUtils().isWriteable(dest, name)) {
                Object value = request.getParameter(name);
                bean.copyProperty(dest, name, value);
            }
        }
    }

    /**
     * @description : copy source filed value to target, when source filed value
     *              is null then not convert target value
     * @param dest
     * @param orig
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

    /**
     * @author linhl
     * @version 1.0
     * @since 2006-12-8
     * @lastest modify date 2006-12-19
     * @description
     */
    public static void copyProperties(Object dest, Map parameters)
            throws ServletException, IOException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, Exception {
        if (dest == null)
            throw new IllegalArgumentException("No destination bean specified");
        Set keySet = parameters.keySet();
        BeanUtilsBean bean = BeanUtilsBean.getInstance();
        Iterator keys = keySet.iterator();
        while (keys.hasNext()) {
            String name = (String) keys.next();

            if (!name.equals("id")) {
                if (bean.getPropertyUtils().isWriteable(dest, name)) {
                    Object value = parameters.get(name);
                    if (value instanceof String) {
                        bean.copyProperty(dest, name, ((String) value).trim());
                    } else if (value instanceof String[]) {
                        bean.copyProperty(dest, name, (((String[]) value))[0].trim());// 有些parameters.get(name)取出来的竟然是个数组，
                        // 这个有可能跟不同的servlet
                        // container 实现有关
                    } else if (value instanceof Number) {
                        bean.copyProperty(dest, name, (Number) value);
                    }
                }
            }
        }
    }

    /**
     * override populate, convert with date
     * 
     * @author songlin.li
     * @param target
     * @param source
     */
    public static void populate(Object target, Map source) {
        try {
            BeanUtils.populate(target, source);
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            logger.error(e.getMessage(), e);
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
     * @author songlin.li
     * @param map
     * @param bean
     * @param prefix
     */
    public static void mapToBean(Map<String, Object> map, Object bean, String prefix) {
        if (bean == null || map == null) {
            return;
        }
        if (StringUtil.isBlank(prefix)) {
            String entityName = bean.getClass().getName().toLowerCase();
            String[] entityNames = entityName.split("\\.");
            entityName = entityNames[entityNames.length - 1];
            prefix = entityName + ".";
        } else if (!prefix.endsWith(".")) {
            prefix += ".";
        }

        for (Map.Entry<String, Object> m : map.entrySet()) {
            String fieldName = m.getKey();
            if (StringUtil.isNotBlank(prefix) && fieldName.startsWith(prefix)) {
                fieldName = StringUtil.substringAfter(fieldName, prefix);
            }
            try {
                ReflectionUtil.setFieldValue(bean, fieldName, m.getValue());
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    /**
     * if expr like *.b.c(* just a prefix) then will return bean.getB().getC()
     * 
     * @author songlin.li
     * @param bean
     * @param expr
     * @return
     */
    public static Object getPropertiesValueByPrefix(Object bean, String expr) {
        String keys[] = StringUtils.split(expr, "\\.");
        Object obj = null;
        for (int i = 1; i < keys.length; i++) {
            obj = ReflectionUtil.invokeGetterMethod(bean, keys[i]);
            bean = obj;
        }
        return obj;
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
                    logger.debug("泛型类型为<{}>", genericName);
                    Class<?> valueCls = ClassLoaderUtil.loadClass(genericName);
                    if (value instanceof List) {
                        value = ListUtil.copyProperties(valueCls, (List) value);
                    } else if (value instanceof Map) {
                        value = Lists.newArrayList(mapToBean((Map) value, valueCls));
                    } else {
                        Object v = valueCls.newInstance();
                        copyProperties(v, value);
                        value = Lists.newArrayList(v);
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
                } else if (StringUtil.equals(propertyType.getName(), "java.lang.String") && !(value instanceof String)) {
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
                } else if (value instanceof Map) {
                    value = mapToBean((Map) value, propertyType);
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
     * @param bean
     *            JavaBean to convert
     * @return map converted
     * @throws IntrospectionException
     *             failed to get class fields
     * @throws IllegalAccessException
     *             failed to instant JavaBean
     * @throws InvocationTargetException
     *             failed to call setters
     * @throws NoSuchMethodException 
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
     * @author songlin
     * @param dest
     * @param org
     * @param ignore
     * @return
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
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
        // p1.setTestDate(new Date());
        p2.setTestDate(new Date());
        // copyProperties(p2, p1);
        copyProperties(p1, p2);
        // org.apache.commons.beanutils.PropertyUtils.copyProperties(p2, p1);
        // org.springframework.beans.BeanUtils.copyProperties(p2, p1);
        System.out.println(p1.getTestDate());
        System.out.println(p2.getTestDate());
    }
}

class CopyBean1 {
    Date testDate;

    public Date getTestDate() {
        return testDate;
    }

    public void setTestDate(Date testDate) {
        this.testDate = testDate;
    }

}

class CopyBean2 {
    Date testDate;

    public Date getTestDate() {
        return testDate;
    }

    public void setTestDate(Date testDate) {
        this.testDate = testDate;
    }

}
