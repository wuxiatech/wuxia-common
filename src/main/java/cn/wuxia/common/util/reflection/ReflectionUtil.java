/**
 * Copyright (c) 2005-2010 springside.org.cn Licensed under the Apache License,
 * Version 2.0 (the "License"); $Id: ReflectionUtils.java 1211 2010-09-10
 * 16:20:45Z calvinxiu $
 */
package cn.wuxia.common.util.reflection;

import cn.wuxia.common.util.ListUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Reflective tools.
 *
 * @author calvin
 * @description : Provide access to private variables to get the generic type
 * Class to extract the properties of the elements in the
 * collection, convert a string to an object Util function.
 */
public class ReflectionUtil {
    public static final String CGLIB_CLASS_SEPARATOR = "$$";

    private static Logger logger = LoggerFactory.getLogger(ReflectionUtil.class);

    public static String reflectionToString(final Object object) {
        return (new ReflectionToStringBuilder(object, ToStringStyle.SHORT_PREFIX_STYLE) {
            @Override
            protected boolean accept(Field field) {
                if (Modifier.isStatic(field.getModifiers())) {
                    return false;
                }
                String name = field.getName();
                Object value = getFieldValue(object, name);
                return super.accept(field) && value != null;
            }
        }).toString();
    }

    /**
     * @param obj
     * @param propertyName
     * @return
     * @description : Invoke Getter method
     */
    public static Object invokeGetterMethod(Object obj, String propertyName) {
        String getterMethodName = "get" + StringUtils.capitalize(propertyName);
        return invokeMethod(obj, getterMethodName, new Class[]{}, new Object[]{});
    }

    /**
     * @param obj
     * @param propertyName
     * @param value
     * @description : Invoke Setter method.Using the value of the Class to Find
     * Setter method.
     */
    public static void invokeSetterMethod(Object obj, String propertyName, Object value) {
        invokeSetterMethod(obj, propertyName, value, null);
    }

    /**
     * @param obj
     * @param propertyName
     * @param value
     * @param propertyType The Setter method used to find, Class alternative use
     *                     value is empty.
     * @description :Invoke Setter method.
     */
    public static void invokeSetterMethod(Object obj, String propertyName, Object value, Class<?> propertyType) {
        Class<?> type = propertyType != null ? propertyType : value.getClass();
        String setterMethodName = "set" + StringUtils.capitalize(propertyName);
        invokeMethod(obj, setterMethodName, new Class[]{type}, new Object[]{value});
    }

    public static Field getFieldByName(Object obj, String name) {
        Field f = null;
        try {
            f = obj.getClass().getDeclaredField(name);
        } catch (Exception e) {
        }
        return f;
    }

    public static boolean isMethodExists(final Object object, final String methodName, final Class<?>[] parameterTypes) {
        Method method = getAccessbleMethod(object, methodName, parameterTypes);
        if (method == null) {
            return false;
        }
        return true;
    }

    public static Method getAccessbleMethod(Object object, String methodName, Class<?>... parameterTypes) {
        Assert.notNull(object, "object is null");
        for (Class<?> superClass = object.getClass(); superClass != Object.class; superClass = superClass
                .getSuperclass()) {
            try {
                Method method = superClass.getDeclaredMethod(methodName, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException e) {
            }
        }

        return null;
    }

    public static Method getGetterMethodByPropertyName(Object target, String propertyName) {
        String getterMethodName = "get" + StringUtils.capitalize(propertyName);
        if (!isMethodExists(target, getterMethodName, new Class[]{})) {
            getterMethodName = "is" + StringUtils.capitalize(propertyName);
        }
        return getAccessibleMethod(target, getterMethodName);
    }

    /**
     * @param obj
     * @param fieldName
     * @return
     * @description : Directly read object attribute values​​, ignoring the
     * private / protected modifiers, and not through the getter
     * function.
     */
    public static Object getFieldValue(final Object obj, final String fieldName) {
        Field field = getAccessibleField(obj, fieldName);

        if (field == null) {
            throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + obj + "]");
        }

        Object result = null;
        try {
            result = field.get(obj);
        } catch (IllegalAccessException e) {
            logger.error("Impossible to throw exception{}", e.getMessage());
        }
        return result;
    }

    /**
     * @param obj
     * @param fieldName
     * @param value
     * @description : Set the object attribute values ​​directly, ignoring the
     * private / protected modifiers without a setter function.
     */
    public static void setFieldValue(final Object obj, final String fieldName, final Object value) {
        Field field = getAccessibleField(obj, fieldName);

        if (field == null) {
            throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + obj + "]");
        }

        try {
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            logger.error("Impossible to throw exception:{}", e.getMessage());
        }
    }

    /**
     * @param obj
     * @param fieldName
     * @return
     * @description : Cycle upcast get the object DeclaredField, and force set
     * up to access. As upcast to the Object still can not be
     * found, return null.
     */
    public static Field getAccessibleField(final Object obj, final String fieldName) {
        Assert.notNull(obj, "object can not be empty");
        Assert.hasText(fieldName, "fieldName");
        for (Class<?> superClass = obj.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()) {
            try {
                Field field = superClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException e) {// NOSONAR
                // Field not in the current class definition, continue upcast
            }
        }
        return null;
    }

    /**
     * @param obj
     * @param methodName
     * @param parameterTypes
     * @param args
     * @return
     * @description : Directly invoke the object methods, ignoring private /
     * protected modifiers.For a one-time invoke.
     */
    public static Object invokeMethod(final Object obj, final String methodName, final Class<?>[] parameterTypes,
                                      final Object[] args) {
        Method method = getAccessibleMethod(obj, methodName, parameterTypes);
        if (method == null) {
            throw new IllegalArgumentException("Could not find method [" + methodName + "] on target [" + obj + "]");
        }

        try {
            return method.invoke(obj, args);
        } catch (Exception e) {
            throw convertReflectionExceptionToUnchecked(e);
        }
    }

    /**
     * @param clazz The class to introspect
     * @return the first generic declaration, or Object.class if cannot be
     * determined
     * @description :Through reflection the generic parameter type of the parent
     * class's Class definition statement. If you are not able to
     * find, return Object.class. eg. public UserDao extends
     * HibernateDao<User>
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getSuperClassGenricType(final Class clazz) {
        return getSuperClassGenricType(clazz, 0);
    }

    /**
     * @param clazz clazz The class to introspect
     * @param index the Index of the generic ddeclaration,start from 0.
     * @return the index generic declaration, or Object.class if cannot be
     * determined
     * @description : Through reflection the generic parameter type of the
     * parent class's Class definition statement. If you are not
     * able to find, return Object.class. Such as public UserDao
     * extends HibernateDao <User,Long>
     */
    @SuppressWarnings("unchecked")
    public static Class getSuperClassGenricType(final Class clazz, final int index) {

        Type genType = clazz.getGenericSuperclass();

        if (!(genType instanceof ParameterizedType)) {
            logger.warn(clazz.getSimpleName() + "'s superclass not ParameterizedType");
            return Object.class;
        }

        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

        if (index >= params.length || index < 0) {
            logger.warn("Index: " + index + ", Size of " + clazz.getSimpleName() + "'s Parameterized Type: "
                    + params.length);
            return Object.class;
        }
        if (!(params[index] instanceof Class)) {
            logger.warn(clazz.getSimpleName() + " not set the actual class on superclass generic parameter");
            return Object.class;
        }

        return (Class) params[index];
    }

    /**
     * @param e
     * @return
     * @description : Reflection when checked exception into unchecked
     * exception.
     */
    public static RuntimeException convertReflectionExceptionToUnchecked(Exception e) {
        if (e instanceof IllegalAccessException || e instanceof IllegalArgumentException
                || e instanceof NoSuchMethodException) {
            return new IllegalArgumentException("Reflection Exception.", e);
        } else if (e instanceof InvocationTargetException) {
            return new RuntimeException("Reflection Exception.", ((InvocationTargetException) e).getTargetException());
        } else if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new RuntimeException("Unexpected Checked Exception.", e);
    }

    /**
     * 判断obj参数是否存在fiedlName字段
     *
     * @param target    要判断的目标对象
     * @param fieldName 字段名称
     * @return boolean
     */
    public static boolean hasField(Object target, String fieldName) {
        return getAccessibleField(target, fieldName) != null;
    }

    /**
     * 循环向上转型, 获取对象的DeclaredField, 并强制设置为可访问. 如向上转型到Object仍无法找到, 返回null.
     *
     * @param targetClass 目标对象Class
     * @param fieldName   class中的字段名
     * @return {@link Field}
     */
    public static Field getAccessibleField(final Class targetClass, final String fieldName) {
        Assert.notNull(targetClass, "targetClass不能为空");
        Assert.hasText(fieldName, "fieldName不能为空");
        for (Class<?> superClass = targetClass; superClass != Object.class; superClass = superClass.getSuperclass()) {
            try {
                Field field = superClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException e) {
            }
        }
        return null;
    }

    /**
     * 循环向上转型, 获取对象的所有DeclaredField, 并强制设置为可访问.
     *
     * @param target 目标对象Class
     * @return List
     */
    public static List<Field> getAccessibleFields(final Object target) {
        Assert.notNull(target, "target不能为空");
        return getAccessibleFields(getTargetClass(target));
    }

    /**
     * 循环向上转型, 获取对象的所有DeclaredField, 并强制设置为可访问.
     *
     * @param targetClass 目标对象Class
     * @return List
     */
    public static List<Field> getAccessibleFields(final Class targetClass) {
        return getAccessibleFields(targetClass, false);
    }

    /**
     * 获取对象的所有DeclaredField,并强制设置为可访问.
     *
     * @param targetClass  目标对象Class
     * @param ignoreParent 是否循环向上转型,获取所有父类的Field
     * @return List
     */
    public static List<Field> getAccessibleFields(final Class targetClass, final boolean ignoreParent) {
        Assert.notNull(targetClass, "targetClass不能为空");
        List<Field> fields = new ArrayList<Field>();

        Class<?> sc = targetClass;

        do {
            Field[] result = sc.getDeclaredFields();

            if (!ArrayUtils.isEmpty(result)) {

                for (Field field : result) {
                    field.setAccessible(true);
                }

                CollectionUtils.addAll(fields, result);
            }

            sc = sc.getSuperclass();

        } while (sc != Object.class && !ignoreParent);

        return fields;
    }

    /**
     * @param target
     * @param methodName
     * @param parameterTypes
     * @return
     * @description :Cycle upcast get the object DeclaredMethod, and force set
     * up to access. As upcast to the Object still can not be
     * found, return null. Used in the method requires multiple
     * calls first to use this function first obtain the Method,
     * and then call Method.invoke (Object obj, Object ... args)
     */
    public static Method getAccessibleMethod(final Object target, final String methodName, Class<?>... parameterTypes) {
        Assert.notNull(target, "object can not be empty");
        return getAccessibleMethod(getTargetClass(target), methodName, parameterTypes);
    }

    /**
     * 循环向上转型, 获取对象的DeclaredMethod,并强制设置为可访问. 如向上转型到Object仍无法找到, 返回null.
     * 用于方法需要被多次调用的情况. 先使用本函数先取得Method,然后调用Method.invoke(Object obj, Object...
     * args)
     *
     * @param targetClass    目标对象Class
     * @param parameterTypes 方法参数类型
     * @return {@link Method}
     */
    public static Method getAccessibleMethod(final Class targetClass, final String methodName,
                                             Class<?>... parameterTypes) {
        Assert.notNull(targetClass, "targetClass不能为空");
        Assert.notNull(methodName, "methodName不能为空");

        for (Class<?> superClass = targetClass; superClass != Object.class; superClass = superClass.getSuperclass()) {
            try {
                Method method = superClass.getDeclaredMethod(methodName, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException e) {
            }
        }
        return null;
    }

    /**
     * 循环向上转型, 获取对象的所有DeclaredMethod 并强制设置为可访问.
     *
     * @param target 目标对象Object
     * @return List
     */
    public static List<Method> getAccessibleMethods(final Object target) {
        Assert.notNull(target, "target不能为空");
        return getAccessibleMethods(getTargetClass(target));
    }

    /**
     * 循环向上转型, 获取对象的所有DeclaredMethod 并强制设置为可访问.
     *
     * @param targetClass 目标对象Class
     * @return List
     */
    public static List<Method> getAccessibleMethods(final Class targetClass) {
        return getAccessibleMethods(targetClass, false);
    }

    /**
     * 获取对象的所有DeclaredMethod 并强制设置为可访问.
     *
     * @param targetClass  目标对象Class
     * @param ignoreParent 是否循环向上转型,获取所有父类的Method
     * @return List
     */
    public static List<Method> getAccessibleMethods(final Class targetClass, boolean ignoreParent) {
        Assert.notNull(targetClass, "targetClass不能为空");
        List<Method> methods = new ArrayList<Method>();

        Class<?> superClass = targetClass;
        do {
            Method[] result = superClass.getDeclaredMethods();

            if (!ArrayUtils.isEmpty(result)) {

                for (Method method : result) {
                    method.setAccessible(true);
                }

                CollectionUtils.addAll(methods, result);
            }

            superClass = superClass.getSuperclass();
        } while (superClass != Object.class && !ignoreParent);

        return methods;
    }

    /**
     * 获取对象中的注解
     *
     * @param target          目标对象Object
     * @param annotationClass 注解
     * @return Object
     */
    public static <T> T getAnnotation(Object target, Class annotationClass) {
        Assert.notNull(target, "target不能为空");
        return (T) getAnnotation(target.getClass(), annotationClass);
    }

    /**
     * 获取对象中的注解
     *
     * @param targetClass     目标对象Class
     * @param annotationClass 注解类型Class
     * @return Object
     */
    public static <T extends Annotation> T getAnnotation(Class targetClass, Class annotationClass) {
        Assert.notNull(targetClass, "targetClass不能为空");
        Assert.notNull(annotationClass, "annotationClass不能为空");

        if (targetClass.isAnnotationPresent(annotationClass)) {
            return (T) targetClass.getAnnotation(annotationClass);
        }

        return null;
    }

    /**
     * 获取Object对象中所有annotationClass类型的注解
     *
     * @param target          目标对象Object
     * @param annotationClass Annotation类型
     * @return {@link Annotation}
     */
    public static <T extends Annotation> List<T> getAnnotations(Object target, Class annotationClass) {
        Assert.notNull(target, "target不能为空");
        return getAnnotations(getTargetClass(target), annotationClass);
    }

    /**
     * 获取对象中的所有annotationClass注解
     *
     * @param targetClass     目标对象Class
     * @param annotationClass 注解类型Class
     * @return List
     */
    public static <T extends Annotation> List<T> getAnnotations(Class targetClass, Class annotationClass) {
        Assert.notNull(targetClass, "targetClass不能为空");
        Assert.notNull(annotationClass, "annotationClass不能为空");

        List<T> result = new ArrayList<T>();
        Annotation annotation = targetClass.getAnnotation(annotationClass);
        if (annotation != null) {
            result.add((T) annotation);
        }
        Constructor[] constructors = targetClass.getDeclaredConstructors();
        // 获取构造方法里的注解
        CollectionUtils.addAll(result, getAnnotations(constructors, annotationClass).iterator());

        Field[] fields = targetClass.getDeclaredFields();
        // 获取字段中的注解
        CollectionUtils.addAll(result, getAnnotations(fields, annotationClass).iterator());

        Method[] methods = targetClass.getDeclaredMethods();
        // 获取方法中的注解
        CollectionUtils.addAll(result, getAnnotations(methods, annotationClass).iterator());
        if (targetClass.getSuperclass() != null) {
            List<T> temp = getAnnotations(targetClass.getSuperclass(), annotationClass);
            if (CollectionUtils.isNotEmpty(temp)) {
                CollectionUtils.addAll(result, temp.iterator());
            }
        }

        return result;
    }

    /**
     * 获取field的annotationClass注解
     *
     * @param field           field对象
     * @param annotationClass annotationClass注解
     * @return {@link Annotation}
     */
    public static <T extends Annotation> T getAnnotation(Field field, Class annotationClass) {

        Assert.notNull(field, "field不能为空");
        Assert.notNull(annotationClass, "annotationClass不能为空");

        field.setAccessible(true);
        if (field.isAnnotationPresent(annotationClass)) {
            return (T) field.getAnnotation(annotationClass);
        }
        return null;
    }

    /**
     * 获取field数组中匹配的annotationClass注解
     *
     * @param fields          field对象数组
     * @param annotationClass annotationClass注解
     * @return List
     */
    public static <T extends Annotation> List<T> getAnnotations(Field[] fields, Class annotationClass) {

        if (ArrayUtils.isEmpty(fields)) {
            return Lists.newArrayList();
        }

        List<T> result = new ArrayList<T>();

        for (Field field : fields) {
            field.setAccessible(true);
            Annotation annotation = getAnnotation(field, annotationClass);
            if (annotation != null) {
                result.add((T) annotation);
            }
        }

        return result;
    }

    /**
     * 获取method的annotationClass注解
     *
     * @param method          method对象
     * @param annotationClass annotationClass注解
     * @return {@link Annotation}
     */
    public static <T extends Annotation> T getAnnotation(Method method, Class annotationClass) {

        Assert.notNull(method, "method不能为空");
        Assert.notNull(annotationClass, "annotationClass不能为空");

        method.setAccessible(true);
        if (method.isAnnotationPresent(annotationClass)) {
            return (T) method.getAnnotation(annotationClass);
        }
        return null;
    }

    /**
     * 获取method数组中匹配的annotationClass注解
     *
     * @param methods         method对象数组
     * @param annotationClass annotationClass注解
     * @return List
     */
    public static <T extends Annotation> List<T> getAnnotations(Method[] methods, Class annotationClass) {

        if (ArrayUtils.isEmpty(methods)) {
            return Lists.newArrayList();
        }

        List<T> result = new ArrayList<T>();

        for (Method method : methods) {

            Annotation annotation = getAnnotation(method, annotationClass);
            if (annotation != null) {
                result.add((T) annotation);
            }
        }

        return result;
    }

    /**
     * 获取constructor的annotationClass注解
     *
     * @param constructor     constructor对象
     * @param annotationClass annotationClass注解
     * @return {@link Annotation}
     */
    public static <T extends Annotation> T getAnnotation(Constructor constructor, Class annotationClass) {

        Assert.notNull(constructor, "constructor不能为空");
        Assert.notNull(annotationClass, "annotationClass不能为空");

        constructor.setAccessible(true);

        if (constructor.isAnnotationPresent(annotationClass)) {
            return (T) constructor.getAnnotation(annotationClass);
        }

        return null;
    }

    /**
     * 获取constructors数组中匹配的annotationClass注解
     *
     * @param constructors    constructor对象数组
     * @param annotationClass annotationClass注解
     * @return List
     */
    public static <T extends Annotation> List<T> getAnnotations(Constructor[] constructors, Class annotationClass) {

        if (ArrayUtils.isEmpty(constructors)) {
            return Lists.newArrayList();
        }

        List<T> result = new ArrayList<T>();

        for (Constructor constructor : constructors) {
            Annotation annotation = getAnnotation(constructor, annotationClass);
            if (annotation != null) {
                result.add((T) annotation);
            }
        }

        return result;
    }

    /**
     * 更具类型获取o中的所有字段名称
     *
     * @param targetClass 目标对象Class
     * @param type        要获取名称的类型
     * @return List
     */
    public static List<String> getAccessibleFieldNames(final Class targetClass, Class type) {

        Assert.notNull(targetClass, "targetClass不能为空");
        Assert.notNull(type, "type不能为空");

        List<String> list = new ArrayList<String>();

        for (Field field : targetClass.getDeclaredFields()) {
            if (field.getType().equals(type)) {
                list.add(field.getName());
            }
        }

        return list;
    }

    /**
     * 更具类型获取o中的所有字段名称
     *
     * @param targetClass 目标对象Class
     * @return List
     */
    public static List<String> getAccessibleFieldNames(final Class targetClass, final boolean ignoreParent) {
        Assert.notNull(targetClass, "targetClass不能为空");
        List<String> fields = new ArrayList<String>();

        Class<?> sc = targetClass;

        do {
            Field[] result = sc.getDeclaredFields();

            if (!ArrayUtils.isEmpty(result)) {

                for (Field field : result) {
                    field.setAccessible(true);
                }
                List<String> fieldNames = ConvertUtil.convertElementPropertyToList(ListUtil.arrayToList(result), "name");
                CollectionUtils.addAll(fields, ListUtil.listToArray(fieldNames));
            }

            sc = sc.getSuperclass();

        } while (sc != Object.class && !ignoreParent);

        return fields;
    }

    /**
     * 通过Class创建对象
     *
     * @param targetClass 目标对象Class
     * @return Object
     */
    public static <T> T newInstance(Class targetClass) {
        Assert.notNull(targetClass, "targetClass不能为空");
        try {
            return (T) targetClass.newInstance();
        } catch (Exception e) {
            throw convertReflectionExceptionToUnchecked(e);
        }

    }

    /**
     * 获取对象Class如果被cglib AOP过的对象或对象为CGLIB的Class，将获取真正的Class类型
     *
     * @param target 对象
     * @return Class
     */
    public static Class<?> getTargetClass(Object target) {
        Assert.notNull(target, "target不能为空");
        return getTargetClass(target.getClass());

    }

    /**
     * 获取Class如果被cglib AOP过的对象或对象为CGLIB的Class，将获取真正的Class类型
     *
     * @param targetClass 对象
     * @return Class
     */
    public static Class<?> getTargetClass(Class<?> targetClass) {

        Assert.notNull(targetClass, "targetClass不能为空");

        Class clazz = targetClass;
        if (clazz != null && clazz.getName().contains(CGLIB_CLASS_SEPARATOR)) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null && !Object.class.equals(superClass)) {
                return superClass;
            }
        }
        return clazz;
    }

}
