/**
 * Copyright (c) 2005-2010 springside.org.cn Licensed under the Apache License,
 * Version 2.0 (the "License"); $Id: EncodeUtils.java 1211 2010-09-10 16:20:45Z
 * calvinxiu $
 */
package cn.wuxia.common.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.Formatter;
import java.util.Random;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * The variety of encoding formats overweight Tools. ntegrated Commons-Codec,
 * Commons-Lang and JDK codec.
 * 
 * @author calvin
 */
public class EncodeUtils {

    private static final String DEFAULT_URL_ENCODING = "UTF-8";

    /**
     * @description : Hex encoding
     * @param input
     * @return
     */
    public static String hexEncode(byte[] input) {
        return Hex.encodeHexString(input);
    }

    /**
     * @description :Hex decoding.
     * @param input
     * @return
     */
    public static byte[] hexDecode(String input) {
        try {
            return Hex.decodeHex(input.toCharArray());
        } catch (DecoderException e) {
            throw new IllegalStateException("Hex Decoder exception", e);
        }
    }

    /**
     * @description : Base64 encoding
     * @param input
     * @return
     */
    public static String base64Encode(byte[] input) {
        return new String(Base64.encodeBase64(input));
    }

    /**
     * @description : Base64 encoding, URL security (Base64 URL of illegal
     *              characters such as +, / = converted to other characters, see
     *              RFC3548).
     * @param input
     * @return
     */
    public static String base64UrlSafeEncode(byte[] input) {
        return Base64.encodeBase64URLSafeString(input);
    }

    /**
     * @description :Base64 decoding
     * @param input
     * @return
     */
    public static byte[] base64Decode(String input) {
        return Base64.decodeBase64(input);
    }

    /**
     * @description :URL encoding, Encode default UTF-8.
     * @param input
     * @return
     */
    public static String urlEncode(String input) {
        try {
            return URLEncoder.encode(URLEncoder.encode(input, DEFAULT_URL_ENCODING), DEFAULT_URL_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Unsupported Encoding Exception", e);
        }
    }

    /**
     * @description :URL decoding, Encode default UTF-8.
     * @param input
     * @return
     */
    public static String urlDecode(String input) {
        try {
            return URLDecoder.decode(URLDecoder.decode(input, DEFAULT_URL_ENCODING), DEFAULT_URL_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Unsupported Encoding Exception", e);
        }
    }

    /**
     * @description :Html escape
     * @param html
     * @return
     */
    public static String htmlEscape(String html) {
        return StringEscapeUtils.escapeHtml4(html);
    }

    /**
     * @description :html Unescape
     * @param htmlEscaped
     * @return
     */
    public static String htmlUnescape(String htmlEscaped) {
        String value = StringEscapeUtils.unescapeHtml4(htmlEscaped);
        value = value.replaceAll("eval\\((.*)\\)", "");
        value = value.replaceAll("[\\\"\\\'][\\s]*javascript:(.*)[\\\"\\\']", "\"j*\"s*");
        value = value.replaceAll("<script>", "&lt;script&gt;").replaceAll("</script>", "&lt;/script&gt;");
        value = value.replaceAll("<javascript>", "&lt;javascript&gt;").replaceAll("</javascript>", "&lt;/javascript&gt;");
        return value;
    }

    /**
     * @description :xml Escape
     * @param xml
     * @return
     */
    public static String xmlEscape(String xml) {
        return StringEscapeUtils.escapeXml10(xml);
    }

    /**
     * @description : xml Unescape
     * @param xmlEscaped
     * @return
     */
    public static String xmlUnescape(String xmlEscaped) {
        return StringEscapeUtils.unescapeXml(xmlEscaped);
    }

    /**
     * encode id
     * 
     * @author songlin
     * @param id
     * @return
     */

    private static String PRE_ = "fc";

    private static String POS_ = "bt";

    /**
     * encode id
     * @see cn.wuxia.common.security.RSAUtils.encryptByPublicKey
     * @see cn.wuxia.common.security.RSAUtils.encryptByPrivateKey
     * @author songlin
     * @param id
     * @return
     */
    @Deprecated
    public static String idEncodeUrl(Number id) {
        if (null == id) {
            return "";
        }
        DecimalFormat df = new DecimalFormat("#");//转换成整型
        String str = df.format(id);
        // 太短id故增加前后缀
        if (str.length() < 5) {
            if (id.intValue() % 2 == 0) {
                str = PRE_ + str;
            } else {
                str = str + POS_;
            }

        }
        return hexEncode(Base64.encodeBase64URLSafeString(str.getBytes()).getBytes());
    }

    /**
     * decode id
     * @see cn.wuxia.common.security.RSAUtils.decryptByPublicKey
     * @see cn.wuxia.common.security.RSAUtils.decryptByPrivateKey
     * @author songlin
     * @param encodeId
     * @return
     */
    @Deprecated
    public static Number idDecodeUrl(String encodeId) {
        if (StringUtil.isBlank(encodeId)) {
            return null;
        }
        try {
            String str = new String(Base64.decodeBase64(hexDecode(encodeId)));
            // 如果有前缀
            if (StringUtil.startsWith(str, PRE_)) {
                str = StringUtil.substringAfter(str, PRE_);
            }
            // 如果有后缀
            if (StringUtil.endsWith(str, POS_)) {
                str = StringUtil.substringBefore(str, POS_);
            }
            return NumberUtils.createNumber(str);
        } catch (Exception e) {
            return NumberUtils.createNumber(encodeId);
        }
    }

    public static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    /**
     * 62^6 = 56 800 235 584
     * @author songlin
     * @param url
     * @return
     */
    private final static String[] chars = new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r",
            "s", "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };

    /**
     * 62^6 = 56 800 235 584
     * @author songlin
     * @param url
     * @return
     */
    public static String shortUrl(String url) {
        // 可以自定义生成 MD5 加密字符传前的混合 KEY
        String key = "test";

        // 对传入网址进行 MD5 加密
        String hex = md5ByHex(key + url);

        int i = new Random().nextInt(4);//产成4以内随机数

        // 把加密字符按照 8 位一组 16 进制与 0x3FFFFFFF 进行位与运算
        String sTempSubString = hex.substring(i * 8, i * 8 + 8);

        // 这里需要使用 long 型来转换，因为 Inteper .parseInt() 只能处理 31 位 , 首位为符号位 , 如果不用long ，则会越界
        long lHexLong = 0x3FFFFFFF & Long.parseLong(sTempSubString, 16);
        String outChars = "";
        for (int j = 0; j < 6; j++) {
            // 把得到的值与 0x0000003D 进行位与运算，取得字符数组 chars 索引
            long index = 0x0000003D & lHexLong;
            // 把取得的字符相加
            outChars += chars[(int) index];
            // 每次循环按位右移 5 位
            lHexLong = lHexLong >> 5;
        }
        // 把字符串存入对应索引的输出数组
        return outChars;
    }

    /**
     * MD5加密(32位大写)    
     * @param src
     * @return
     */
    public static String md5ByHex(String src) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] b = src.getBytes();
            md.reset();
            md.update(b);
            byte[] hash = md.digest();
            String hs = "";
            String stmp = "";
            for (int i = 0; i < hash.length; i++) {
                stmp = Integer.toHexString(hash[i] & 0xFF);
                if (stmp.length() == 1)
                    hs = hs + "0" + stmp;
                else {
                    hs = hs + stmp;
                }
            }
            return hs.toUpperCase();
        } catch (Exception e) {
            return "";
        }
    }

}
