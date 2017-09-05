package cn.wuxia.common.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.google.common.collect.Maps;

import cn.wuxia.common.util.DateUtil.DateFormatter;

/**
 * Http and Servlet utility class.
 * 
 * @author songlin.li
 */
public class ServletUtils {
    protected transient static final Logger logger = LoggerFactory.getLogger(ServletUtils.class);

    // -- Content Type Defined --//
    public static final String TEXT_TYPE = "text/plain";

    public static final String JSON_TYPE = "application/json";

    public static final String XML_TYPE = "text/xml";

    public static final String HTML_TYPE = "text/html";

    public static final String JS_TYPE = "text/javascript";

    public static final String EXCEL_TYPE = "application/vnd.ms-excel";

    // -- Header Defined --//
    public static final String AUTHENTICATION_HEADER = "Authorization";

    // -- common value defined --//
    public static final long ONE_YEAR_SECONDS = 60 * 60 * 24 * 365;

    /**
     * @description : Set the client cache expired time Header.
     * @param response
     * @param expiresSeconds
     */
    public static void setExpiresHeader(HttpServletResponse response, long expiresSeconds) {
        // Http 1.0 header
        response.setDateHeader("Expires", System.currentTimeMillis() + expiresSeconds * 1000);
        // Http 1.1 header
        response.setHeader("Cache-Control", "private, max-age=" + expiresSeconds);
    }

    /**
     * @description : Set banned the client cache of Header.
     * @param response
     */
    public static void setDisableCacheHeader(HttpServletResponse response) {
        // Http 1.0 header
        response.setDateHeader("Expires", 1L);
        response.addHeader("Pragma", "no-cache");
        // Http 1.1 header
        response.setHeader("Cache-Control", "no-cache, no-store, max-age=0");
    }

    /**
     * @description : Set LastModified Header.
     * @param response
     * @param lastModifiedDate
     */
    public static void setLastModifiedHeader(HttpServletResponse response, long lastModifiedDate) {
        response.setDateHeader("Last-Modified", lastModifiedDate);
    }

    /**
     * @description : Set Etag Header.
     * @param response
     * @param etag
     */
    public static void setEtag(HttpServletResponse response, String etag) {
        response.setHeader("ETag", etag);
    }

    /**
     * @description : According to the browser If-Modified-Since Header,
     *              Calculation if the file has been modified. If no changes,
     *              checkIfModify return false ,set 304 not modify status.
     * @param lastModified The content of the last modification time.
     */
    public static boolean checkIfModifiedSince(HttpServletRequest request, HttpServletResponse response, long lastModified) {
        long ifModifiedSince = request.getDateHeader("If-Modified-Since");
        if ((ifModifiedSince != -1) && (lastModified < ifModifiedSince + 1000)) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return false;
        }
        return true;
    }

    /**
     * @description : According to the browser If-None-Match Header, Calculation
     *              Etag whether invalid. if Etag is valid, checkIfNoneMatch
     *              return false, set 304 not modify status.
     * @param etag Content ETag.
     */
    public static boolean checkIfNoneMatchEtag(HttpServletRequest request, HttpServletResponse response, String etag) {
        String headerValue = request.getHeader("If-None-Match");
        if (headerValue != null) {
            boolean conditionSatisfied = false;
            if (!"*".equals(headerValue)) {
                StringTokenizer commaTokenizer = new StringTokenizer(headerValue, ",");

                while (!conditionSatisfied && commaTokenizer.hasMoreTokens()) {
                    String currentToken = commaTokenizer.nextToken();
                    if (currentToken.trim().equals(etag)) {
                        conditionSatisfied = true;
                    }
                }
            } else {
                conditionSatisfied = true;
            }

            if (conditionSatisfied) {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                response.setHeader("ETag", etag);
                return false;
            }
        }
        return true;
    }

    /**
     * @description :Set the browser pop-up download dialog Header.
     * @param fileName After download the file name.
     */
    public static void setFileDownloadHeader(HttpServletRequest request, HttpServletResponse response, String fileName) {
        try {
            // Chinese filenames support
            if (request.getHeader("User-Agent").toUpperCase().indexOf("MSIE") > 0) {
                // IE browser
                fileName = URLEncoder.encode(fileName, "UTF-8");
            } else {
                // firefox browser
                fileName = new String(fileName.getBytes("UTF-8"), "ISO8859-1");
            }

            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        } catch (UnsupportedEncodingException e) {
        }
    }

    /**
     * <strong>Request To Entity</strong>
     * <p>
     * Request to Entity
     * </p>
     * 
     * @param entity bean
     * @param request
     * @return object
     * @author songlin.li
     */
    public static <T> T parameterToEntity(HttpServletRequest request, Class<T> entity, String prefix) {
        Assert.notNull(entity, "Entity must be not null");
        Object bean = null;
        try {
            bean = entity.newInstance();
        } catch (InstantiationException e1) {
            logger.error(e1.getMessage(), e1);
        } catch (IllegalAccessException e1) {
            logger.error(e1.getMessage(), e1);
        }
        if (StringUtil.isBlank(prefix)) {
            String entityName = entity.getName().toLowerCase();
            String[] entityNames = entityName.split("\\.");
            entityName = entityNames[entityNames.length - 1];
            prefix = entityName + ".";
        } else if (!prefix.endsWith(".")) {
            prefix += ".";
        }
        Map<String, String> m = getParametersStartingWith2(request, prefix);
        bean = getFiledNames(bean, entity, m);

        return (T) bean;
    }

    private static <T> Object getFiledNames(Object bean, Class<T> entity, Map<String, String> m) {
        Field[] fields = entity.getDeclaredFields();
        int len = fields.length;
        for (int i = 0; i < len; i++) {
            Field field = entity.getDeclaredFields()[i];
            String fieldName = field.getName();
            if (m.get(fieldName) != null) {
                try {
                    setFieldValue(field, bean, m.get(fieldName));
                } catch (IllegalAccessException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        entity = (Class<T>) entity.getSuperclass();
        if (entity != null) {
            bean = getFiledNames(bean, entity, m);
        }
        return bean;
    }

    /**
     * @description : The data object assignment to designated the corresponding
     *              attributes
     * @param field
     * @param bean
     * @param value
     * @throws IllegalAccessException
     */
    private static void setFieldValue(Field field, Object bean, String value) throws IllegalAccessException {
        // Take the field of data types
        String fieldType = field.getType().getName();
        field.setAccessible(true);
        if (StringUtil.isNotBlank(value)) {
            try {
                if (fieldType.equals("java.lang.String")) {
                    field.set(bean, value);
                } else if (fieldType.equals("java.lang.Integer") || fieldType.equals("int")) {
                    field.set(bean, Integer.valueOf(value));
                } else if (fieldType.equals("java.lang.Long") || fieldType.equals("long")) {
                    field.set(bean, Long.valueOf(value));
                } else if (fieldType.equals("java.lang.Float") || fieldType.equals("float")) {
                    field.set(bean, Float.valueOf(value));
                } else if (fieldType.equals("java.lang.Double") || fieldType.equals("double")) {
                    field.set(bean, Double.valueOf(value));
                } else if (fieldType.equals("java.math.BigDecimal")) {
                    field.set(bean, new BigDecimal(value));
                } else if (fieldType.equals("java.util.Date")) {
                    Date d = DateUtil.parse(value, DateFormatter.FORMAT_DD_MMM_YYYY_HH_MM_SS);
                    if (d == null) {
                        d = DateUtil.parse(value, DateFormatter.FORMAT_DD_MMM_YYYY);
                    }
                    field.set(bean, d);
                } else if (fieldType.equals("java.sql.Date")) {
                    Date d = DateUtil.parse(value, DateFormatter.FORMAT_DD_MMM_YYYY_HH_MM_SS);
                    if (d == null) {
                        d = DateUtil.parse(value, DateFormatter.FORMAT_DD_MMM_YYYY);
                    }
                    field.set(bean, DateUtil.utilDateToSQLDate(d));
                } else if (fieldType.equals("java.lang.Boolean") || fieldType.equals("boolean")) {
                    field.set(bean, Boolean.valueOf(value));
                } else if (fieldType.equals("java.lang.Byte") || fieldType.equals("byte")) {
                    field.set(bean, Byte.valueOf(value));
                } else if (fieldType.equals("java.lang.Short") || fieldType.equals("short")) {
                    field.set(bean, Short.valueOf(value));
                }
            } catch (NumberFormatException ex) {
                // When using a simple data types will throw an exception
                logger.info(ex.getMessage(), ex);
            } catch (Exception e) {
                field.set(bean, null);
                logger.info(e.getMessage(), e);
            }
        }
    }

    public static Map<String, Object> getParametersMap(final HttpServletRequest req) {
        Map<String, Object> params = new HashMap<>();
        Enumeration<String> emu = req.getParameterNames();
        while (emu.hasMoreElements()) {
            String key = emu.nextElement();
            String[] values = req.getParameterValues(key);
            if (values != null) {
                if (values.length == 1) {
                    params.put(key, values[0]);
                } else {
                    params.put(key, values);
                }
            }
        }

        return Collections.unmodifiableMap(params);
    }

    /**
     * @description : 多个值则使用逗号(,)分割
     * @author songlin.li
     * @param request
     * @param prefix
     * @return
     */
    public static Map<String, String> getParametersStartingWith2(HttpServletRequest request, String prefix) {
        Map<String, Object> params = getParametersStartingWith1(request, prefix);
        Map<String, String> aMap = new HashMap<String, String>();
        for (String key : params.keySet()) {
            Object values = params.get(key);
            if (values instanceof String[]) {
                aMap.put(key, StringUtil.join((String[])values, ","));
            } else if (values instanceof String) {
                aMap.put(key, (String) values);
            }
        }
        return aMap;
    }

    /**
     * @description :With the same prefix made the Request Parameters. The
     *              results of Parameter name has been to remove the prefix.
     */
    public static Map<String, Object> getParametersStartingWith1(HttpServletRequest request, String prefix) {
        Assert.notNull(request, "Request must not be null");
        if (StringUtil.isBlank(prefix)) {
            return getParametersMap(request);
        }
        Enumeration<String> paramNames = request.getParameterNames();
        Map<String, Object> params = new TreeMap<String, Object>();
        while (paramNames != null && paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            if (paramName.startsWith(prefix)) {
                String unprefixed = paramName.substring(prefix.length());
                String[] values = request.getParameterValues(paramName);
                if (values == null || values.length == 0) {
                    // Do nothing, no values found at all.
                } else if (values.length > 1) {
                    params.put(unprefixed, values);
                } else {
                    params.put(unprefixed, values[0]);
                }
            }
        }
        return params;
    }

    /**
     * @description : start entity is key, like user.userName
     * @author songlin.li
     * @param request
     * @return
     */
    public Map<String, Map<String, Object[]>> parameterToEntityMap(ServletRequest request) {
        Map<String, String[]> m = request.getParameterMap();
        Map<String, String> e = Maps.newLinkedHashMap();
        for (String key : m.keySet()) {
            String entity = key.split("\\.")[0];
            e.put(entity, "");
        }
        Map<String, Map<String, Object[]>> mm = Maps.newLinkedHashMap();
        for (String entity : e.keySet()) {
            Map<String, Object[]> map = Maps.newLinkedHashMap();
            for (String key : m.keySet()) {
                String[] values = m.get(key);
                String entityKey = key.split("\\.")[1];
                // entityKey = key.substring(key.indexOf(".")+1);
                if (key.startsWith(entity)) {
                    map.put(entityKey, values);
                }
            }
            mm.put(entity, map);
        }
        return mm;
    }

    /**
     * Convenience method for deleting a cookie by name
     * 
     * @param response the current web response
     * @param cookie the cookie to delete
     * @param path the path on which the cookie was set (i.e. /appfuse)
     */
    public static void deleteCookie(HttpServletResponse response, Cookie cookie, String path) {
        if (cookie != null) {
            // Delete the cookie by setting its maximum age to zero
            cookie.setMaxAge(0);
            cookie.setPath(path);
            response.addCookie(cookie);
        }
    }

    /**
     * Convenience method to get a cookie by name
     * 
     * @param request the current request
     * @param name the name of the cookie to find
     * @return the cookie (if found), null if not found
     */
    public static Cookie getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        Cookie returnCookie = null;

        if (cookies == null) {
            return returnCookie;
        }

        for (int i = 0; i < cookies.length; i++) {
            Cookie thisCookie = cookies[i];
            if (thisCookie.getName().equals(name)) {
                // cookies with no value do me no good!
                if (!thisCookie.getValue().equals("")) {
                    returnCookie = thisCookie;

                    break;
                }
            }
        }

        return returnCookie;
    }

    public final static String getErrorUrl(HttpServletRequest request) {
        String errorUrl = (String) request.getAttribute("javax.servlet.error.request_uri");
        if (errorUrl == null) {
            errorUrl = (String) request.getAttribute("javax.servlet.forward.request_uri");
        }
        if (errorUrl == null) {
            errorUrl = (String) request.getAttribute("javax.servlet.include.request_uri");
        }
        if (errorUrl == null) {
            errorUrl = request.getRequestURL().toString();
        }
        return errorUrl;
    }

    public static final String getFullRequestUrl(HttpServletRequest req) {
        return (req.getQueryString() == null ? req.getRequestURL() : req.getRequestURL().append("?").append(req.getQueryString())).toString();
    }

    public static void setCookie(HttpServletResponse response, String name, String value, String path, int maxAge) {
        if (logger.isDebugEnabled()) {
            logger.debug("Setting cookie '" + name + "' on path '" + path + "'");
        }

        Cookie cookie = new Cookie(name, value);
        cookie.setSecure(false);
        cookie.setPath((path == null || "".equals(path) ? "/" : path));
        cookie.setMaxAge(maxAge); // 30 days

        response.addCookie(cookie);
    }

    /**
     * 获得远程IP,经过代理后也可获取真实访问者ip
     */
    public static String getRemoteIP(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 重写request getQueryString 函数 ，解决乱码问题
     * 
     * @param request
     * @return
     */
    public static String getQueryString(final HttpServletRequest request) {
        Map<String, String[]> map = request.getParameterMap();
        Map<String, Object> m = new HashMap<>();
        m.putAll(map);
        return getUrlParamsByMap(m);
    }

    /**
     * 将url参数转换成map ex. aa=11&bb=22&cc=33 -> {aa=11,bb=22,cc=33}
     * 暂不支持数组如：aa=11&aa=22
     * @author songlin
     * @param param
     * @return
     */
    public static Map<String, Object> getUrlParams(String param) {
        Map<String, Object> map = new LinkedHashMap<String, Object>(0);
        if (StringUtils.isBlank(param)) {
            return map;
        }
        if (param.indexOf("?") > 0) {
            param = param.substring(param.indexOf("?") + 1);
        }
        String[] params = param.split("&");
        for (int i = 0; i < params.length; i++) {
            String[] p = params[i].split("=");
            if (p.length == 2) {
                if (map.containsKey(p[0])) {
                    Object v = map.get(p[0]);
                    if (v instanceof String) {
                        String[] v2 = { v.toString(), p[1] };
                        map.put(p[0], v2);
                    } else if (v instanceof String[]) {
                        String[] v2 = (String[]) v;
                        v2[v2.length] = p[1];
                        map.put(p[0], v2);
                    }
                } else {
                    map.put(p[0], p[1]);
                }
            }
        }
        return map;
    }

    /**
     * 将map转换成url ex. {abc=123, 123=abc} -> abc=123&123=abc
     * 
     * @author songlin
     * @param map
     * @return
     */
    public static String getUrlParamsByMap(final Map<String, Object> map) {
        if (map == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object obVal = entry.getValue();
            if (obVal == null)
                continue;
            if (obVal instanceof Object[]) {
                for (Object o : (Object[]) obVal) {
                    if (null == o)
                        continue;
                    sb.append(entry.getKey() + "=" + StringUtil.trim("" + o));
                    sb.append("&");
                }
            } else if (obVal instanceof Collection) {
                for (Object o : (Collection<?>) obVal) {
                    if (null == o)
                        continue;
                    sb.append(entry.getKey() + "=" + StringUtil.trim("" + o));
                    sb.append("&");
                }
            } else {
                sb.append(entry.getKey() + "=" + StringUtil.trim("" + obVal));
                sb.append("&");
            }

        }
        String s = sb.toString();
        if (s.endsWith("&")) {
            s = StringUtils.substringBeforeLast(s, "&");
        }
        return s;
    }

    /**
     * 将HTTP资源另存为文件
     * 
     * @param destUrl
     * @throws IOException
     * @author songlin.li
     * @throws Exception
     */
    public static void requestToFile(String fileName, String savePath, String requestUrl) throws Exception {
        FileUtil fileUtil = new FileUtil(savePath, fileName);

        // 建立链接
        URL url = new URL(requestUrl);
        HttpURLConnection httpUrl = (HttpURLConnection) url.openConnection();
        // 连接指定的资源
        httpUrl.connect();

        // 建立文件
        fileUtil.createFile();
        if (StringUtil.endsWithIgnoreCase(fileName, ".jsp")) {
            String contentType = "<%@ page contentType=\"text/html; charset=" + fileUtil.getCharset() + "\" pageEncoding=\"" + fileUtil.getCharset()
                    + "\"%>";
            fileUtil.write(contentType);
        }
        // 获取网络输入流
        String content = fileUtil.read(httpUrl.getInputStream());
        fileUtil.write(content);
        httpUrl.disconnect();
        fileUtil.close();
        logger.debug("destUrl：[" + requestUrl + "]， save to ：[" + savePath + fileName + "]");
    }

    public static void main(String[] args) {
        Map<String, Object> m = new HashMap<>();
        Map<String, String[]> m2 = new HashMap<>();
        m2.put("abc", new String[] { "a", "b" });
        m.putAll(m2);
        System.out.println(getUrlParamsByMap(m));

        String a = "abc=a&abc=b&c=3";
        Map m3 = getUrlParams(a);
        System.out.println(m3.get("abc"));
        a = "http://afsfsdflas.com/fasfa?abc=a&abc=b?fafsadf";
        Map m4 = getUrlParams(a);
        System.out.println(m4.get("abc"));
    }
}
