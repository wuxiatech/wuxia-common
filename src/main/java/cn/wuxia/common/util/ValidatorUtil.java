/*
 * Created on :Sep 4, 2012 Author :songlin.li
 */
package cn.wuxia.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import cn.wuxia.common.exception.ValidateException;

public class ValidatorUtil<T> {
    private static Validator validator;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    public static <T> void validate(T entity) throws ValidateException {
        List<String> validateErrors = getViolations(entity);
        if (ListUtil.isNotEmpty(validateErrors)) {
            throw new ValidateException(StringUtil.join(validateErrors, ";"));
        }
    }

    public static <T> List<String> getViolations(T entity) {
        List<String> validateError = new ArrayList<String>();

        Set<ConstraintViolation<T>> constraintViolations = validator.validate(entity);

        if (constraintViolations != null && constraintViolations.size() > 0) {
            for (ConstraintViolation<T> constraintViolation : constraintViolations) {
                String msg = constraintViolation.getMessage();
                if (StringUtil.startsWith(msg, "{") && StringUtil.endsWith(msg, "}")) {
                    String key = StringUtil.substringBetween(msg, "{", "}");
                    msg = ResourceBundleUtil.getString(key);
                    validateError.add(constraintViolation.getPropertyPath().toString() + " " + msg);
                } else {
                    validateError.add(constraintViolation.getPropertyPath().toString() + " " + msg);
                }
            }
        }

        return validateError;
    }

    /**
     * 调用JSR303的validate方法, 验证失败时抛出ConstraintViolationException, 而不是返回constraintViolations.
     */
    public static void validateWithException(Validator validator, Object object, Class<?>... groups) throws ConstraintViolationException {
        Set constraintViolations = validator.validate(object, groups);
        if (!constraintViolations.isEmpty()) {
            throw new ConstraintViolationException(constraintViolations);
        }
    }

    /**
     * 辅助方法, 转换ConstraintViolationException中的Set<ConstraintViolations>中为List<message>.
     */
    public static List<String> extractMessage(ConstraintViolationException e) {
        return extractMessage(e.getConstraintViolations());
    }

    /**
     * 辅助方法, 转换Set<ConstraintViolation>为List<message>
     */
    public static List<String> extractMessage(Set<? extends ConstraintViolation> constraintViolations) {
        List<String> errorMessages = Lists.newArrayList();
        for (ConstraintViolation violation : constraintViolations) {
            errorMessages.add(violation.getMessage());
        }
        return errorMessages;
    }

    /**
     * 辅助方法, 转换ConstraintViolationException中的Set<ConstraintViolations>为Map<property, message>.
     */
    public static Map<String, String> extractPropertyAndMessage(ConstraintViolationException e) {
        return extractPropertyAndMessage(e.getConstraintViolations());
    }

    /**
     * 辅助方法, 转换Set<ConstraintViolation>为Map<property, message>.
     */
    public static Map<String, String> extractPropertyAndMessage(Set<? extends ConstraintViolation> constraintViolations) {
        Map<String, String> errorMessages = Maps.newHashMap();
        for (ConstraintViolation violation : constraintViolations) {
            errorMessages.put(violation.getPropertyPath().toString(), violation.getMessage());
        }
        return errorMessages;
    }

    /**
     * 辅助方法, 转换ConstraintViolationException中的Set<ConstraintViolations>为List<propertyPath message>.
     */
    public static List<String> extractPropertyAndMessageAsList(ConstraintViolationException e) {
        return extractPropertyAndMessageAsList(e.getConstraintViolations(), " ");
    }

    /**
     * 辅助方法, 转换Set<ConstraintViolations>为List<propertyPath message>.
     */
    public static List<String> extractPropertyAndMessageAsList(Set<? extends ConstraintViolation> constraintViolations) {
        return extractPropertyAndMessageAsList(constraintViolations, " ");
    }

    /**
     * 辅助方法, 转换ConstraintViolationException中的Set<ConstraintViolations>为List<propertyPath + separator + message>.
     */
    public static List<String> extractPropertyAndMessageAsList(ConstraintViolationException e, String separator) {
        return extractPropertyAndMessageAsList(e.getConstraintViolations(), separator);
    }

    /**
     * 辅助方法, 转换Set<ConstraintViolation>为List<propertyPath + separator + message>.
     */
    public static List<String> extractPropertyAndMessageAsList(Set<? extends ConstraintViolation> constraintViolations, String separator) {
        List<String> errorMessages = Lists.newArrayList();
        for (ConstraintViolation violation : constraintViolations) {
            errorMessages.add(violation.getPropertyPath() + separator + violation.getMessage());
        }
        return errorMessages;
    }

}
