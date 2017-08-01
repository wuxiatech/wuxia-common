/*
 * Created on :Sep 4, 2012 Author :songlin.li
 */
package cn.wuxia.common.sensitive;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import cn.wuxia.common.exception.ValidateException;
import cn.wuxia.common.exception.ValidateSensitiveException;
import cn.wuxia.common.util.ListUtil;
import cn.wuxia.common.util.StringUtil;
import cn.wuxia.common.util.reflection.ReflectionUtil;

public class ValidtionSensitiveUtil {

    public static <T> void validate(T entity) throws ValidateException {
        List<String> validateErrors = null;
        try {
            validateErrors = getViolations(entity);
        } catch (ValidateSensitiveException e) {
            throw new ValidateException(e.getMessage());
        }
        if (ListUtil.isNotEmpty(validateErrors)) {
            throw new ValidateException(StringUtil.join(validateErrors, ";"));
        }
    }

    public static <T> List<String> getViolations(T entity) throws ValidateSensitiveException {
        List<String> validateError = new ArrayList<String>();
        List<Method> result = ReflectionUtil.getAccessibleMethods(ReflectionUtil.getTargetClass(entity), false);

        if (!ListUtil.isEmpty(result)) {
            for (Method method : result) {
                try {
                    method.setAccessible(true);
                    Annotation ano = method.getAnnotation(CheckSensitiveWord.class);
                    if (ano != null) {
                        Object value = method.invoke(entity, new Object[0]);
                        SensitiveWordFilter filter = new SensitiveWordFilter();
                        if (filter.isContaintSensitiveWord(value.toString(), SensitiveWordFilter.maxMatchType)) {
                            validateError.add("存在非法内容");
                            throw new ValidateSensitiveException(method.getName() + "存在非法内容");
                        }
                    }
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    validateError.add("内容错误");
                    continue;
                }
            }
        }

        return validateError;
    }

}
