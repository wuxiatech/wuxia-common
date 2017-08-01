/*
* Created on :2015年6月29日
* Author     :yanzj
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 武侠科技 All right reserved.
*/
package cn.wuxia.common.util.baidu;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.wuxia.common.util.JsonUtil;
import cn.wuxia.common.util.StringUtil;
import cn.wuxia.common.web.httpclient.HttpClientException;
import cn.wuxia.common.web.httpclient.HttpClientRequest;
import cn.wuxia.common.web.httpclient.HttpClientUtil;

public class UrlApiUtil {

    protected static final Logger logger = LoggerFactory.getLogger("baidu");

    /**
     * 获取短网址 <br/>
     * 该方法调用百度短网址Api把长网址转换成短网址 <br/>
     * @author CaRson.Yan
     * @param longUrl 长网址
     * @return map
     *      包含以下信息：<br/>
     *      1) status   : 转换状态，为0时代表转换成功，否则转换失败 <br/>
     *      2) tinyurl  : 转换后的短网址，若status为0时有返回 <br/>
     *      3) err_msg  : 错误信息，当status不为0时有返回
     */
    public static String getShortUrl(String longUrl) {
        //百度短网址api的请求地址
        HttpClientRequest request = new HttpClientRequest("http://dwz.cn/create.php");
        request.addParam("url", longUrl);
        //获取从百度短网址api返回的json格式数据
        String result;
        try {
            result = HttpClientUtil.post(request).getStringResult();
        } catch (HttpClientException e) {
            logger.error("", e);
            return null;
        }
        //转为map类型并返回
        Map<String, Object> map = null;
        if (StringUtil.isNotBlank(result)) {
            map = JsonUtil.fromJson(result);
        }
        if (StringUtil.isNotBlank(map.get("err_msg"))) {
            throw new IllegalArgumentException("");
        }
        return (String) map.get("tinyurl");
    }

    /**
     * 
     * @author songlin
     * @param longUrl
     * @return
     */
    public static String getFddShortUrl(String longUrl) {
        //fdd短网址api的请求地址
        HttpClientRequest request = new HttpClientRequest("http://fdd.link/url/create");
        request.addParam("url", longUrl);
        //获取从百度短网址api返回的json格式数据
        String result;
        try {
            result = HttpClientUtil.get(request).getStringResult();
        } catch (HttpClientException e) {
            logger.error("", e);
            return null;
        }
        //转为map类型并返回
        Map<String, Object> map = null;
        if (StringUtil.isNotBlank(result)) {
            map = JsonUtil.fromJson(result);
        }
        if (StringUtil.isBlank(map.get("surl"))) {
            throw new IllegalArgumentException(map.get("msg").toString());
        }
        return (String) map.get("surl");
    }

    /**
     * 根据短连接修改长连接
     * @author songlin
     * @param shortUrl
     * @param longUrl
     * @throws HttpClientException 
     */
    public static void updateShortUrl(String shortUrl, String longUrl) throws HttpClientException {
        //fdd短网址api的请求地址
        HttpClientRequest request = new HttpClientRequest("http://fdd.link/url/update");
        request.addParam("surl", shortUrl);
        request.addParam("longurl", longUrl);
        //获取从百度短网址api返回的json格式数据
        String result = HttpClientUtil.post(request).getStringResult();
        //转为map类型并返回
        Map<String, Object> map = null;
        if (StringUtil.isNotBlank(result)) {
            map = JsonUtil.fromJson(result);
        }
        if (!StringUtil.equals("0", map.get("status").toString())) {
            throw new IllegalArgumentException(map.get("msg").toString());
        }
    }

    public static void main(String[] args) {
        //        System.out.println(getShortUrl("http://fendoudou.com/auth/register"));
        System.out.println(getFddShortUrl("http://fendoudou.com/auth/register"));
    }

}
