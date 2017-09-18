/*
 * Created on :Jun 28, 2013 Author :PL Change History Version Date Author Reason
 * <Ver.No> <date> <who modify> <reason>
 */
package cn.wuxia.common.spring.support;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import cn.wuxia.common.exception.ServiceException;
import cn.wuxia.common.spring.support.Msg.CustomMessageTypeEnum;

public class ValidationHandler {

    private Validator validator;

    public ValidationHandler() {
    }

    public ValidationHandler(Validator validator) {
        this.validator = validator;
    }

    public List<String> validate(Object target) {
        List<Object> validatorBeans = loadValidatorBeans(target);
        List<String> errorMsgs = new ArrayList<String>();
        validator = getValidator();
        for (Object object : validatorBeans) {
            Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object);
            for (ConstraintViolation<Object> constraintViolation : constraintViolations) {
                errorMsgs.add(constraintViolation.getMessage());
                Msg.addMessage(constraintViolation.getMessage(), CustomMessageTypeEnum.VALID, false);
            }
        }
        return errorMsgs;
    }

    public Validator getValidator() {
        if (validator == null) {
            validator = Validation.buildDefaultValidatorFactory().getValidator();
        }
        return validator;
    }

    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    /**
     * 执行验证
     * @author PL
     * @param target
     */
    public void doValidate(Object target) {
        List<Object> validatorBeans = loadValidatorBeans(target);
        validator = getValidator();
        for (Object object : validatorBeans) {
            Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object);
            Iterator<ConstraintViolation<Object>> iterable = constraintViolations.iterator();
            if (iterable.hasNext()) {
                throw new ServiceException(iterable.next().getMessage());
            }
        }
    }

    @SuppressWarnings({ "rawtypes" })
    protected List<Object> loadValidatorBeans(Object target) {
        List<Object> validatorBeans = new ArrayList<Object>();
        validatorBeans.add(target);
        try {
            Field[] fields = target.getClass().getDeclaredFields();
            Method[] methods = target.getClass().getDeclaredMethods();
            for (Field field : fields) {
                Class fieldType = field.getType();
                String fieldName = field.getName();
                Method getMethod = null;
                String getMethodName = null;
                String setMethodName = null;
                if (fieldName.length() > 1) {
                    String baseMethodName = String.valueOf(fieldName.charAt(0)).toUpperCase() + fieldName.substring(1);
                    getMethodName = "get" + baseMethodName;
                    setMethodName = "set" + baseMethodName;
                } else if (fieldName.length() == 1) {
                    String baseMethodName = fieldName.toUpperCase();
                    getMethodName = "get" + baseMethodName;
                    setMethodName = "set" + baseMethodName;
                }
                int methodFlag = 0;
                for (Method method : methods) {
                    if (getMethodName.equals(method.getName())) {
                        getMethod = method;
                        methodFlag = methodFlag | 1;
                    }
                    if (setMethodName.equals(method.getName())) {
                        methodFlag = methodFlag | 2;
                    }
                    if (methodFlag == 3) {
                        break;
                    }
                }
                if (methodFlag == 3) {
                    Annotation[] fieldAnnotations = field.getDeclaredAnnotations();
                    Annotation[] methodAnnotations = getMethod.getDeclaredAnnotations();
                    boolean flag = false;
                    if (fieldAnnotations != null && fieldAnnotations.length > 0) {
                        for (Annotation annotation : fieldAnnotations) {
                            if (ValidBean.class.equals(annotation.annotationType())) {
                                flag = true;
                            }
                        }
                    }
                    if (!flag && methodAnnotations != null && methodAnnotations.length > 0) {
                        for (Annotation annotation : methodAnnotations) {
                            if (ValidBean.class.equals(annotation.annotationType())) {
                                flag = true;
                            }
                        }
                    }
                    if (flag) {
                        Object object = fieldType.newInstance();
                        if (getMethod != null) {
                            object = getMethod.invoke(target);
                        }
                        if (object == null) {
                            object = fieldType.newInstance();
                            Method method = target.getClass().getMethod(setMethodName, fieldType);
                            method.invoke(target, object);
                        }
                        validatorBeans.add(object);
                    }
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return validatorBeans;
    }
}
