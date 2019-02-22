package cn.wuxia.common.web.sign;

import cn.wuxia.common.util.MD5Util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

public class SignUtil2 {

    /**
     * @param characterEncoding 编码格式
     * @param parameters        请求参数
     * @return
     * @Description：sign签名
     */
    public static String createSign(String appkey, SortedMap<String, Object> parameters) {
        StringBuffer sb = new StringBuffer();
        Set es = parameters.entrySet();
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            Object v = entry.getValue();
            if (null != v && !"".equals(v) && !"sign".equals(k) && !"key".equals(k)) {
                sb.append(k + "=" + v + "&");
            }
        }
        sb.append("key=" + appkey);
        String sign = MD5Util.MD5HexEncode(sb.toString(), "UTF-8").toUpperCase();
        return sign;
    }

    /**
     * 用于ASCII码从小到大排序
     *
     * @param packageParams
     * @return
     * @throws UnsupportedEncodingException
     * @author wuwenhao
     */
    public static String getSing(SortedMap<String, Object> signParams) throws UnsupportedEncodingException {
        StringBuffer sb = new StringBuffer();
        Set es = signParams.entrySet();
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            String v = (String) entry.getValue();
            sb.append(k + "=" + UrlEncode(v) + "&");
        }

        // 转换成String
        String packageValue = sb.toString();
        System.out.println("packageValue=" + packageValue);
        return packageValue;
    }

    public static String UrlEncode(String src) throws UnsupportedEncodingException {
        return URLEncoder.encode(src, "UTF-8").replace("+", "%20");
    }

    /**
     * 随机字符串
     *
     * @return
     * @author wuwenhao
     */
    public static String getNonceStr() {
        Random random = new Random();
        return MD5Util.MD5HexEncode(String.valueOf(random.nextInt(10000)), "UTF-8");
    }

    /**
     * 当前时候
     *
     * @return
     * @author wuwenhao
     */
    public static String getTimeStamp() {
        return String.valueOf(System.currentTimeMillis() / 1000);
    }

}
