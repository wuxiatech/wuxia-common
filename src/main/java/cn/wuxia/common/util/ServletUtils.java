package cn.wuxia.common.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

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
     * @param response
     * @param expiresSeconds
     * @description : Set the client cache expired time Header.
     */
    public static void setExpiresHeader(HttpServletResponse response, long expiresSeconds) {
        // Http 1.0 header
        response.setDateHeader("Expires", System.currentTimeMillis() + expiresSeconds * 1000);
        // Http 1.1 header
        response.setHeader("Cache-Control", "private, max-age=" + expiresSeconds);
    }

    /**
     * @param response
     * @description : Set banned the client cache of Header.
     */
    public static void setDisableCacheHeader(HttpServletResponse response) {
        // Http 1.0 header
        response.setDateHeader("Expires", 1L);
        response.addHeader("Pragma", "no-cache");
        // Http 1.1 header
        response.setHeader("Cache-Control", "no-cache, no-store, max-age=0");
    }

    /**
     * @param response
     * @param lastModifiedDate
     * @description : Set LastModified Header.
     */
    public static void setLastModifiedHeader(HttpServletResponse response, long lastModifiedDate) {
        response.setDateHeader("Last-Modified", lastModifiedDate);
    }

    /**
     * @param response
     * @param etag
     * @description : Set Etag Header.
     */
    public static void setEtag(HttpServletResponse response, String etag) {
        response.setHeader("ETag", etag);
    }

    /**
     * @param lastModified The content of the last modification time.
     * @description : According to the browser If-Modified-Since Header,
     * Calculation if the file has been modified. If no changes,
     * checkIfModify return false ,set 304 not modify status.
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
     * @param etag Content ETag.
     * @description : According to the browser If-None-Match Header, Calculation
     * Etag whether invalid. if Etag is valid, checkIfNoneMatch
     * return false, set 304 not modify status.
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
     * @param fileName After download the file name.
     * @description :Set the browser pop-up download dialog Header.
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
     * @param request
     * @param prefix
     * @return
     * @description : 多个值则使用逗号(,)分割
     * @author songlin.li
     */
    public static Map<String, String> getParametersStartingWith2(HttpServletRequest request, String prefix) {
        Map<String, Object> params = getParametersStartingWith1(request, prefix);
        Map<String, String> aMap = new HashMap<String, String>();
        for (String key : params.keySet()) {
            Object values = params.get(key);
            if (values instanceof String[]) {
                aMap.put(key, StringUtil.join((String[]) values, ","));
            } else if (values instanceof String) {
                aMap.put(key, (String) values);
            }
        }
        return Collections.unmodifiableMap(aMap);
    }

    /**
     * @description :With the same prefix made the Request Parameters. The
     * results of Parameter name has been to remove the prefix.
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
        return Collections.unmodifiableMap(params);
    }


    /**
     * Convenience method for deleting a cookie by name
     *
     * @param response the current web response
     * @param cookie   the cookie to delete
     * @param path     the path on which the cookie was set (i.e. /appfuse)
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
     * @param name    the name of the cookie to find
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
     *
     * @param param
     * @return
     * @author songlin
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
                        String[] v2 = {v.toString(), p[1]};
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
     * @param map
     * @return
     * @author songlin
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
     * @throws Exception
     * @author songlin.li
     */
    @Deprecated
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
        m2.put("abc", new String[]{"a", "b"});
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
