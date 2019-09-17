package cn.wuxia.common.util;

import java.util.*;

import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;

import com.google.common.collect.Lists;

import cn.wuxia.common.util.reflection.BeanUtil;
import org.apache.commons.collections.ListUtils;

/**
 * <h3>Class name</h3> Array Tools
 * <h4>Description</h4>
 * <h4>Special Notes</h4> for more
 *
 * @author songlin.li 2012-5-29
 * @version 0.1
 * @see org.apache.commons.collections.ListUtils
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ListUtil extends CollectionUtils {

    /**
     * 去除null值
     *
     * @param source
     * @return
     * @author songlin
     */
    public static <T> List<T> removeAllNullValue(Collection<T> source) {
        List<T> c = Lists.newArrayList(source);
        c.removeAll(Collections.singleton(null));
        return c;
    }

    /**
     * @param source
     * @return
     * @description :remove Collection Duplicate values
     * @author songlin.li
     */
    public static <T> List<T> removeDuplicateBySet(Collection<T> source) {
        if (source == null) {
            return null;
        }
        Set set = new HashSet();
        List distinctResult = new ArrayList();
        for (Iterator iter = source.iterator(); iter.hasNext(); ) {
            Object element = iter.next();
            if (element instanceof String && StringUtil.isBlank(element)) {
                continue;
            }
            if (element != null && set.add(element)) {
                distinctResult.add(element);
            }
        }
        // source.clear();
        // source.addAll(distinctResult);
        return distinctResult;
    }

    /**
     * @param list
     * @return
     * @description : must be generic remove Collection Duplicate values
     * @author songlin.li
     */
    public static <T> List<T> removeDuplicateByContains(Collection<T> list) {
        if (null == list) {
            return null;
        }
        List<T> distinctResult = new ArrayList();
        for (T t : list) {
            if (t instanceof String && StringUtil.isBlank(t)) {
                continue;
            }
            if (t != null && !distinctResult.contains(t)) {
                distinctResult.add(t);
            }
        }
        return distinctResult;
    }

    /**
     * @param array
     * @return
     * @description :
     * <p>
     * <pre>
     * Arrays.asList() return java.util.Arrays$ArrayList are not return ArrayList,
     *  Arrays$ArrayList and ArrayList are extends AbstractList, remove,add Etc.
     *   method at AbstractList is  defaults throw UnsupportedOperationException and not do and works return Arrays
     *   .asList(array);
     *   this method is return ArrayList
     *              </pre>
     * @author songlin.li
     */
    public static <T> List<T> arrayToList(T[] array) {
        if (array == null) {
            return Lists.newArrayList();
        }
        return Lists.newArrayList(array);
    }

    /**
     * @param list
     * @return
     * @description : Description of the method
     * @author songlin.li
     */
    public static <T> T[] listToArray(List<T> list) {
        T[] a = null;
        if (isEmpty(list)) {
            return a;
        }
        for (T t : list) {
            a = ArrayUtil.add(a, t);
        }
        return list.toArray(a);
    }

    /**
     * 将原集合转换为目标Class型的集合，并复制其中相同属性的值
     *
     * @param t          目标类型的Class
     * @param sourceList 原list集合
     * @return 返回转换后的list集合
     * @author songlin.li
     */
    public static <T, E> List<T> copyProperties(Class<T> t, Collection<E> sourceList) {
        if (t != null && isNotEmpty(sourceList)) {
            try {
                List<T> list = Lists.newArrayList();
                for (E e : sourceList) {
                    if (e.getClass().equals(t)) {
                        return Lists.newArrayList((List<T>) sourceList);
                    }
                    T newT = t.newInstance();
                    /**
                     * 如果双方为Map
                     */
                    if (e instanceof Map && newT instanceof Map) {
                        newT = (T) Maps.newHashMap((Map) e);
                    } else if (e instanceof Map && !(newT instanceof Map)) {
                        newT = (T) BeanUtil.mapToBean((Map) e, t);
                    } else if (!(e instanceof Map) && (newT instanceof Map)) {
                        newT = (T) BeanUtil.beanToMap(e);
                    } else {
                        BeanUtil.copyProperties(newT, e);
                    }
                    list.add(newT);
                }
                return list;
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
