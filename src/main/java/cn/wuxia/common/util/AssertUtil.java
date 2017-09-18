package cn.wuxia.common.util;

import java.util.Collection;
import java.util.Locale;

import org.nutz.lang.Lang;
import org.springframework.util.Assert;

/**
 * 暂时不要使用，待优化工具
 * TODO
 * @ver 0.1
 * @author songlin.li 2012-05-17
 */
@Deprecated
public class AssertUtil extends Assert {

    /**
     * @description : Throws custom exception, exception information from the
     *              configuration file.
     * @param message -Corresponding error message key
     * @param exception - Need to construct an exception class
     */
    public static void errorWithBundle(String message, Class<?> exception) {
        try {
            message = ResourceBundleUtil.getString(message);
        } catch (Exception e) {
        }

        throw (RuntimeException) ClassLoaderUtil.newInstanceByConstructor(exception, new Class[] { String.class }, new Object[] { message });
    }

    /**
     * @param object
     * @param message
     * @throws Exception
     */
    public static void notNullWithBundle(Object object, String message, Class<?> exception) {
        try {
            Assert.notNull(object, message);
        } catch (Exception e) {
            errorWithBundle(message, exception);
        }
    }

    /**
     * @param object
     * @param message
     * @throws Exception
     */
    public static void notEmptyWithBundle(Collection<?> collection, String message, Class<?> exception) {
        if (ListUtil.isEmpty(collection)) {
            errorWithBundle(message, exception);
        }

    }

    /**
     * if not unique, throws exception
     * 
     * @param object
     * @param message
     * @throws Exception
     */
    public static void uniqueWithBundle(Collection<?> collection, String message, Class<?> exception) {
        if (ListUtil.isEmpty(collection)) {
            errorWithBundle(message + "_null", exception);
        }

        if (collection.size() != 1) {
            errorWithBundle(message + "_more", exception);
        }
    }

    /**
     * if not true, throws exception
     * 
     * @param b
     * @param message
     * @throws Exception
     */
    public static void isTrueWithBundle(boolean b, String message, Class<?> exception) throws Exception {
        try {
            Assert.isTrue(b);
        } catch (Exception e) {
            errorWithBundle(message, exception);
        }
    }

    public final static String ERROR = "error.";

    public final static String SPECIALCHAR = "#";

    /**
     * throws exception
     * 
     * @param <T>
     * @param message
     * @param message
     * @throws T
     */
    public static <T extends Throwable> void error(String message, Class<T> exception) throws T {
        throw Lang.makeThrow(exception, message);
    }

    /**
     * @param <T>
     * @param object
     * @param message
     * @param exception
     * @throws T
     */
    public static <T extends Throwable> void notNull(Object object, String message, Class<T> exception) throws T {
        if (object == null) {
            error(message, exception);
        }
    }

    /**
     * not null
     * 
     * @param <T>
     * @param collection
     * @param message
     * @param exception
     * @throws T
     */
    public static <T extends Throwable> void notEmpty(Collection<?> collection, String message, Class<T> exception) throws T {
        if (ListUtil.isEmpty(collection)) {
            error(message, exception);
        }
    }

    /**
     * unique error
     * 
     * @param collection
     * @param message
     * @param exception
     * @throws T
     */
    public static <T extends Throwable> void enique(Collection<?> collection, String message, Class<T> exception) throws T {
        if (ListUtil.isEmpty(collection) || collection.size() != 1) {
            error(message, exception);
        }
    }

    /**
     * show exception
     * 
     * @param e Throwable
     * @param locale Locale
     * @return error message
     */
    public static String showError(Throwable e, Locale locale) {
        // error.sql#aaa#ddd：#error.sql is the key，aaa and ddd are String.format
        // parameter
        String mes = e.getMessage();
        if (mes != null && mes.contains(ERROR)) {
            String[] messages = mes.split(SPECIALCHAR);

            if (messages.length > 1) {
                Object[] entity = new Object[messages.length - 1];

                for (int i = 0; i < entity.length; i++) {
                    entity[i] = messages[i + 1];
                }

                mes = String.format(ResourceBundleUtil.getString(messages[0], locale), entity);
            } else {
                mes = ResourceBundleUtil.getString(messages[0], locale);
            }
        }

        return mes;
    }

    /**
     * show message
     * 
     * @param mes
     * @param locale
     * @return
     */
    public static String showMessage(String mes, Locale locale) {
        // error.sql#aaa#ddd：#error.sql is the key，aaa and ddd are String.format
        // parameter
        if (mes != null) {
            String[] messages = mes.split(SPECIALCHAR);
            if (messages.length > 1) {
                Object[] entity = new Object[messages.length - 1];

                for (int i = 0; i < entity.length; i++) {
                    entity[i] = messages[i + 1];
                }

                mes = String.format(ResourceBundleUtil.getString(messages[0], locale), entity);
            } else {
                mes = ResourceBundleUtil.getString(messages[0], locale);
            }
        }
        return mes;
    }
}
