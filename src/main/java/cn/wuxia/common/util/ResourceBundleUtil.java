package cn.wuxia.common.util;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h3>Class name</h3> <h4>Description</h4> <h4>Special Notes</h4>
 * 
 * @ver 0.1
 * @author songlin.li 2008-9-10
 */
public class ResourceBundleUtil {
    protected static final Logger logger = LoggerFactory.getLogger(ResourceBundleUtil.class);

    public static String ApplicationResourcesPre = "locale/messages";

    protected static ResourceBundle bundle;

    protected static ResourceBundle rBundle;

    protected static Locale locale = Locale.getDefault();

    static {
        try {
            bundle = ResourceBundle.getBundle(ApplicationResourcesPre, locale);
        } catch (MissingResourceException e) {
            logger.warn("", e);
        }
    }

    /**
     * @description : get String
     * @param key
     * @return
     */
    public static String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
        }

        return key;
    }

    /**
     * @description :get Locale
     * @return
     */
    public static Locale getLocale() {
        return bundle.getLocale();
    }

    /**
     * @description : get String
     * @param key
     * @param local
     * @return
     */
    public static String getString(String key, Locale local) {
        if (local != null) {
            rBundle = ResourceBundle.getBundle(ApplicationResourcesPre, local);
        }
        try {
            return rBundle.getString(key);
        } catch (Exception e) {
        }

        return key;
    }

    /**
     * @description :set String
     * @param key
     * @param language
     * @param value
     */
    public void setString(String key, String language, String value) {
        PropertiesUtils propertiesUtil = new PropertiesUtils();
        propertiesUtil.setProperties(ApplicationResourcesPre + "_" + language + ".properties");
        propertiesUtil.setValue(key, value);
        propertiesUtil.saveFile();
        this.getClass().getClassLoader().clearAssertionStatus();
    }
}
