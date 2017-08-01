/*
* Created on :9 Oct, 2015
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 武侠科技 All right reserved.
*/
package cn.wuxia.common.util.baidu;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.wuxia.common.util.JsonUtil;
import cn.wuxia.common.util.NumberUtil;
import cn.wuxia.common.web.httpclient.HttpClientException;
import cn.wuxia.common.web.httpclient.HttpClientUtil;

/** 
 * 获取经纬度,地址相互获取
 * 
 * @author songlin.li 
 * 密钥:f247cdb592eb43ebac6ccd27f796e2d2 
 */

public class MapApiUtil {
    protected static final Logger logger = LoggerFactory.getLogger("baidu");

    private static String key = "VAzT04n37uWd0NSuovWRn8B9";

    /** 
    * @param addr 
    * 查询的地址, 返回格式
    * <pre>{"lng":113.410443,"lat":23.114609}</pre>
    * @return 
    */
    public static Map<String, Object> getCoordinate(String addr) {
        String address = null;
        try {
            address = java.net.URLEncoder.encode(addr, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        String url = String.format("http://api.map.baidu.com/geocoder?address=%s&output=json&key=%s", address, key);
        String output;
        try {
            output = HttpClientUtil.get(url);
        } catch (HttpClientException e) {
            logger.error("", e);
            return null;
        }
        Map<String, Object> resp = JsonUtil.fromJson(output);
        Map<String, Object> result = (Map<String, Object>) resp.get("result");
        return (Map<String, Object>) result.get("location");
    }

    /**
     * 根据经纬度返回地址信息，返回格式：
     * <pre>{"city":"广州市","country":"中国","direction":"附近","distance":"13","district":"天河区","province":"广东省","street":"黄埔大道东","street_number":"600号","country_code":0}</pre>
     * @author songlin
     * @param lng
     * @param lat
     * @return
     */
    public static Map<String, Object> getAddress(float lng, float lat) {
        String url = String.format("http://api.map.baidu.com/geocoder/v2/?ak=%s&location=%s,%s&output=json&pois=1", key, lat, lng);
        String output;
        try {
            output = HttpClientUtil.get(url);
        } catch (HttpClientException e) {
            logger.error("", e);
            return null;
        }
        Map<String, Object> resp = JsonUtil.fromJson(output);
        Map<String, Object> result = (Map<String, Object>) resp.get("result");
        return (Map<String, Object>) result.get("addressComponent");
    }

    public static void main(String[] args) throws IOException {
        Map<String, Object> location = getCoordinate("广州市天河区黄埔大道东582号");
        System.out.println(location);
        System.out.println(getAddress(NumberUtil.toFloat(location.get("lng")), NumberUtil.toFloat(location.get("lat"))));//纬度
    }
}
