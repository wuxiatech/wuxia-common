/*
 * Created on :2 Apr, 2014 Author :songlin Change History Version Date Author
 * Reason <Ver.No> <date> <who modify> <reason>
 */
package cn.wuxia.common.util;

import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.Sets;

public class ArrayUtil extends ArrayUtils {
    public static Object[] removeDuplicateBySet(Object[] source) {
        if (source == null) {
            return null;
        }
        Set set = Sets.newHashSet();
        Object[] result = new Object[] {};
        int i = 0;
        for (Object element : source) {
            if (set.add(element)) {
                result[i++] = element;
            }
        }
        return result;
    }
}
