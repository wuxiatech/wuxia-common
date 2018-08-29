/*
 * Created on :14 Jan, 2014 Author :songlin Change History Version Date Author
 * Reason <Ver.No> <date> <who modify> <reason>
 */
package cn.wuxia.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrowserUtils {
    protected transient static final Logger logger = LoggerFactory.getLogger(ServletUtils.class);

    public enum BrowserType {
        WEIXIN("MicroMessenger"), IE10("MSIE 10.0"), IE9("MSIE 9.0"), IE8("MSIE 8.0"), IE7("MSIE 7.0"), IE6("MSIE 6.0"), Firefox("Firefox"), Safari(
                "Safari"), Chrome("Chrome"), Opera("Opera"), Camino("Camino"), Gecko("Gecko"), Other("其它");
        String bname;

        private BrowserType(String bname) {
            this.bname = bname;
        }

        public String getBname() {
            return bname;
        }

    }

    private final static String MAXTHON = "Maxthon";

    private final static String QQ = "QQBrowser";

    private final static String GREEN = "GreenBrowser";

    private final static String SE360 = "360SE";

    /**
     * 判断是否是微信浏览器
     */
    public static boolean isWeiXin(HttpServletRequest request) {
        return isWeiXin(getUserAgent(request));
    }

    /**
     * 判断是否是微信浏览器
     */
    public static boolean isWeiXin(String userAgent) {
        logger.info(userAgent);
        return userAgent.toLowerCase().indexOf("micromessenger") > 0 ? true : false;
    }

    /**
     * 获取微信版本
     *
     * @param request
     * @return
     */
    public static String getWeiXinVersion(HttpServletRequest request) {
        return getWeiXinVersion(getUserAgent(request));
    }

    /**
     * 获取微信版本
     *
     * @param request
     * @return
     */
    public static String getWeiXinVersion(String userAgent) {
        return StringUtil.substringBetween(userAgent + " ", "MicroMessenger/", " ");
    }

    /**
     * 判断是否是IE
     */
    public static boolean isIE(HttpServletRequest request) {
        return isIE(getUserAgent(request));
    }

    /**
     * 判断是否是IE
     */
    public static boolean isIE(String userAgent) {
        return userAgent.toLowerCase().indexOf("msie") > 0 ? true : false;
    }

    /**
     * 获取IE版本
     *
     * @param request
     * @return
     */
    public static Double getIEversion(HttpServletRequest request) {
        Double version = 0.0;
        if (isIE(request)) {
            // TODO
        }
        return version;
    }

    /**
     * 获取浏览器类型
     *
     * @param request
     * @return
     */
    public static BrowserType getBrowserType(HttpServletRequest request) {
        for (BrowserType b : BrowserType.values()) {
            if (getBrowserType(request, b.getBname())) {
                return b;
            }
        }
        return BrowserType.Other;
    }

    private static boolean getBrowserType(HttpServletRequest request, String brosertype) {
        return getUserAgent(request).toLowerCase().indexOf(brosertype.toLowerCase()) > 0 ? true : false;
    }

    public static String checkBrowse(final HttpServletRequest request) {
        String userAgent = getUserAgent(request);
        if (StringUtil.isBlank(userAgent))
            return BrowserType.Other.bname;

        for (BrowserType b : BrowserType.values()) {
            if (regex(b.getBname(), userAgent)) {
                return b.bname;
            }
        }
        if (regex(SE360, userAgent))
            return SE360;
        if (regex(GREEN, userAgent))
            return GREEN;
        if (regex(QQ, userAgent))
            return QQ;
        if (regex(MAXTHON, userAgent))
            return MAXTHON;
        return BrowserType.Other.bname;
    }

    public static boolean regex(String regex, String str) {
        Pattern p = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher m = p.matcher(str);
        return m.find();
    }

    public static String getUserAgent(final HttpServletRequest request) {
        //获取浏览器信息
        return request.getHeader("User-Agent") == null ? "" : request.getHeader("User-Agent");
    }

    /**
     * 获取网络类型
     *
     * @param request
     * @return
     */
    public static String getNetType(final HttpServletRequest request) {
        return getNetType(getUserAgent(request));
    }

    /**
     * 获取网络类型
     *
     * @param userAgent
     * @return
     */
    public static String getNetType(String userAgent) {
        return StringUtil.substringBetween(userAgent + " ", "NetType/", " ");
    }

    /**
     * 获取浏览器语言
     *
     * @param request
     * @return
     */
    public static String getLanguage(final HttpServletRequest request) {
        return getLanguage(getUserAgent(request));
    }

    /**
     * 获取浏览器语言
     *
     * @param userAgent
     * @return
     */
    public static String getLanguage(String userAgent) {
        return StringUtil.substringBetween(userAgent + " ", "Language/", " ");
    }
}
