package cn.wuxia.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import com.google.common.collect.Lists;

import cn.wuxia.common.util.reflection.BeanUtil;

/**
 * <h3>Class name</h3> Array Tools
 * <h4>Description</h4>
 * <h4>Special Notes</h4> for more
 * 
 * @see org.apache.commons.collections.ListUtils
 * @version 0.1
 * @author songlin.li 2012-5-29
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ListUtil extends CollectionUtils {

    /**
     * 去除null值
     * 
     * @author songlin
     * @param source
     * @return
     */
    public static <T> List<T> removeAllNullValue(Collection<T> source) {
        List<T> c = Lists.newArrayList(source);
        c.removeAll(Collections.singleton(null));
        return c;
    }

    /**
     * @description :remove Collection Duplicate values
     * @author songlin.li
     * @param list
     * @return
     */
    public static <T> List<T> removeDuplicateBySet(Collection<T> source) {
        if (source == null) {
            return null;
        }
        Set set = new HashSet();
        List distinctResult = new ArrayList();
        for (Iterator iter = source.iterator(); iter.hasNext();) {
            Object element = iter.next();
            if (set.add(element))
                distinctResult.add(element);
        }
        // source.clear();
        // source.addAll(distinctResult);
        return distinctResult;
    }

    /**
     * @description : must be generic remove Collection Duplicate values
     * @author songlin.li
     * @param list
     * @return
     */
    public static <T> List<T> removeDuplicateByContains(Collection<T> list) {
        if (null == list) {
            return null;
        }
        List<T> distinctResult = new ArrayList();
        for (T t : list) {
            if (!distinctResult.contains(t)) {
                distinctResult.add(t);
            }
        }
        return distinctResult;
    }

    /**
     * @description :
     * 
     *              <pre>
     * Arrays.asList() return java.util.Arrays$ArrayList are not return ArrayList, 
     *  Arrays$ArrayList and ArrayList are extends AbstractList, remove,add Etc. 
     *   method at AbstractList is  defaults throw UnsupportedOperationException and not do and works return Arrays.asList(array); 
     *   this method is return ArrayList
     *              </pre>
     * 
     * @author songlin.li
     * @param array
     * @return
     */
    public static <T> List<T> arrayToList(T[] array) {
        return Lists.newArrayList(array);
    }

    /**
     * @description : Description of the method
     * @author songlin.li
     * @param list
     * @return
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
     * @author songlin.l
     * @param t
     *            目标类型的Class
     * @param sourceList
     *            原list集合
     * @return 返回转换后的list集合
     */
    public static <T, E> List<T> copyProperties(Class<T> t, Collection<E> sourceList) {
        if (t != null && isNotEmpty(sourceList)) {
            try {
                List<T> list = Lists.newArrayList();
                for (E e : sourceList) {
                    T newT = t.newInstance();
                    if (e instanceof Map) {
                        newT = (T) BeanUtil.mapToBean((Map) e, t);
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
