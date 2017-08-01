package cn.wuxia.common.util;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jodd.jerry.Jerry;

public class HTMLUtil {
    private static Logger logger = LoggerFactory.getLogger(HTMLUtil.class);

    public static final String INPUT = "input";

    public static final String SELECT = "select";

    public static final String BUTTON = "button";

    public static final String TEXTAREA = "textarea";

    /**
     * Add attribute
     * 
     * @author songlin.li
     * @param html
     * @param property
     * @param value
     * @return
     */
    public static String addElementPorerty(String html, String property, String value) {
        Jerry doc = Jerry.jerry(html);
        if (StringUtil.isNotBlank(value) && StringUtil.isNotBlank(property)) {
            doc.$(":first").attr(property, value.toString());
        }
        return doc.html();
    }

    /**
     * Add attribute
     * 
     * @author songlin.li
     * @param html
     * @param map
     * @return
     */
    public static String addElementPorerty(String html, Map<String, Object> map) {
        Jerry doc = Jerry.jerry(html);
        for (String property : map.keySet()) {
            Object value = map.get(property);
            if (StringUtil.isNotBlank(value)) {
                doc.$(":first").attr(property, value.toString());
            }
        }

        return doc.html();
    }

    /**
     * Add attribute
     * 
     * @author songlin.li
     * @param html
     * @param map
     * @return
     */
    public static String addElementPorerty(String html, String element, String property, String value) {
        element = StringUtil.isBlank(element) ? "" : element;
        Jerry doc = Jerry.jerry(html);
        if (StringUtil.isNotBlank(value) && StringUtil.isNotBlank(value)) {
            doc.$(element + ":first").attr(property, value.toString());
        }
        return doc.html();
    }

    /**
     * Add attribute
     * 
     * @author songlin.li
     * @param html
     * @param map
     * @return
     */
    public static String addElementPorerty(String html, String element, Map<String, Object> map) {
        element = StringUtil.isBlank(element) ? "" : element;
        Jerry doc = Jerry.jerry(html);
        for (String key : map.keySet()) {
            Object value = map.get(key);
            if (StringUtil.isNotBlank(value)) {
                doc.$(element + ":first").attr(key, value.toString());
            }
        }
        return doc.html();
    }

    /**
     * Add Class
     * 
     * @author songlin.li
     * @return
     */
    public static String addElementClass(String html, String classNames) {
        Jerry doc = Jerry.jerry(html);
        doc.$(":first").addClass(classNames);
        return doc.html();
    }

    /**
     * Add Class
     * 
     * @author songlin.li
     * @return
     */
    public static String addElementClass(String html, String... classNames) {
        Jerry doc = Jerry.jerry(html);
        doc.$(":first").addClass(classNames);
        return doc.html();
    }

    /**
     * add element class by selector
     * 
     * <pre>
     * jquery selector
     * </pre>
     * 
     * @author songlin.li
     * @param classNames
     * @param html
     * @param element
     * @return
     */
    public static String addElementClass(String html, String filter, String[] classNames) {
        filter = StringUtil.isBlank(filter) ? ":first" : filter;
        Jerry doc = Jerry.jerry(html);
        doc.$(filter).addClass(classNames);
        return doc.html();
    }

    /**
     * Description of the method
     * 
     * @author songlin.li
     * @param orgi
     * @return
     */
    public static Jerry parse(String orgi) {
        return Jerry.jerry(orgi);
    }

    public static void main(String[] vars) {
        String th = "<th><dl><dt><label></label></dt><dd></dd></dl></th>";
        Jerry doc = Jerry.jerry(th);
        doc.$("dt").addClass("required-label");
        doc.$("label").text("abc");
        doc.$("dd").append("<input type='hidden'>");
        System.out.println(doc.html());
    }
}
