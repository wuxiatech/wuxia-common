/*
 * Created on :2012-9-25 Author :songlin.li
 */
package cn.wuxia.common.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;

import org.nutz.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.google.common.collect.Maps;

import cn.wuxia.common.mapper.JacksonMapper;
import cn.wuxia.common.util.DateUtil.DateFormatter;

public class JsonUtil {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    /**
     * @description : fromJson
     * @author songlin.li
     * @param jsonString
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> fromJson(String jsonString) {
        if (StringUtil.isBlank(jsonString)) {
            return Maps.newHashMap();
        }
        //jsonString = decodeUrl(jsonString);
        return JacksonMapper.nonEmptyMapper().fromJson(jsonString, HashMap.class);
    }

    /**
     * @description : fromJson
     * @author songlin.li
     * @param json
     * @param clazz
     * @return
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (StringUtil.isBlank(json)) {
            return null;
        }
        //json = decodeUrl(json);
        return JacksonMapper.nonEmptyMapper().fromJson(json, clazz);
    }

    /**
     * @description : toJson
     * @author songlin.li
     * @param obj
     * @return
     */
    public static String toJson(Object obj) {
        String result = "";
        if (obj == null)
            return result;
        try {
            result = JacksonMapper.nonEmptyMapper().toJson(obj);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return result;

    }

    /**
     * @description : toFullJson
     * @author songlin.li
     * @param obj
     * @return
     */
    public static String toFullJson(Object obj) {
        String result = "";
        if (obj == null)
            return result;

        return JacksonMapper.alwaysMapper().toJson(obj);
    }

    public static String toJson(Object obj, DateFormatter formatter) throws JsonProcessingException {
        JacksonMapper jm = new JacksonMapper(Include.NON_EMPTY);
        SimpleDateFormat dateFormat = new SimpleDateFormat(formatter == null ? DateFormatter.FORMAT_YYYY_MM_DD.getFormat() : formatter.getFormat());
        return jm.getMapper().setDateFormat(dateFormat).writeValueAsString(obj);

    }

    public static String toFullJson(Object obj, DateFormatter formatter) throws JsonProcessingException {
        JacksonMapper jm = new JacksonMapper(Include.ALWAYS);
        SimpleDateFormat dateFormat = new SimpleDateFormat(formatter == null ? DateFormatter.FORMAT_YYYY_MM_DD.getFormat() : formatter.getFormat());
        return jm.getMapper().setDateFormat(dateFormat).writeValueAsString(obj);
    }

    public static <T> Collection<T> fromJsonToCollection(String json, Class<T> e) {
        JacksonMapper jm = new JacksonMapper();
        JavaType javaType = jm.createCollectionType(Collection.class, e);
        return jm.fromJson(json, javaType);
    }

    /**
     * @description : decode Url
     * @param url
     * @return
     */
    private static String decodeUrl(String url) {
        
        try {
            return URLDecoder.decode(url, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return url;
        } catch (IllegalArgumentException e) {
            logger.debug(e.getMessage());
            return url;
        }
    }

    public static String toJsonWithFormat(Object obj) {
        String result = "";
        if (obj == null)
            return result;
        try {
            result = Json.toJson(obj);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return result;

    }

    public static void main(String[] args) {
        String json = "rO0ABXQEnlt7ImlkIjoiWmgtcWcyQWRRa0txVDlBQlJxazVQQSIsInRpdGxlIjoi55yL55yL5o-Q6YaS5oiR5rKh5pyJIiwic3RhcnQiOiIyMDE4LTA0LTE5IDE0OjMwIiwiZW5kIjoiMjAxOC0wNC0xOSAxNTowMCIsImFsbERheSI6ZmFsc2UsImNsYXNzTmFtZSI6ImxhYmVsLXN1Y2Nlc3MiLCJ0ZXh0Q29sb3IiOiJ3aGl0ZSJ9LHsiaWQiOiJyYnNhVHJneVQxdXlfSFNySVRkdzhBIiwidGl0bGUiOiLmj5DphpI0Iiwic3RhcnQiOiIyMDE4LTA0LTI5IDExOjMwIiwiZW5kIjoiMjAxOC0wNC0yOSAxMjowMCIsImFsbERheSI6ZmFsc2UsImNsYXNzTmFtZSI6ImxhYmVsLXN1Y2Nlc3MiLCJ0ZXh0Q29sb3IiOiJ3aGl0ZSJ9LHsiaWQiOiJXaFpsVWZxelRsQ3oxTDg2TFhiVzNRIiwidGl0bGUiOiLmj5DphpIzIiwic3RhcnQiOiIyMDE4LTA0LTI5IDEwOjAwIiwiZW5kIjoiMjAxOC0wNC0yOSAxMDozMCIsImFsbERheSI6ZmFsc2UsImNsYXNzTmFtZSI6ImxhYmVsLWdyZXkiLCJ0ZXh0Q29sb3IiOiJ3aGl0ZSJ9LHsiaWQiOiJfRjBTcXNraVNuQzdsWm1WVWhaUEJ3IiwidGl0bGUiOiLmj5DphpIyIiwic3RhcnQiOiIyMDE4LTA0LTI5IDA5OjAwIiwiZW5kIjoiMjAxOC0wNC0yOSAwOTozMCIsImFsbERheSI6ZmFsc2UsImNsYXNzTmFtZSI6ImxhYmVsLXB1cnBsZSIsInRleHRDb2xvciI6IndoaXRlIn0seyJpZCI6Ii15X0h1ejhQUl9xaFYxSHNLLTBUMHciLCJ0aXRsZSI6IuaPkOmGkjEiLCJzdGFydCI6IjIwMTgtMDQtMjkgMDg6MzAiLCJlbmQiOiIyMDE4LTA0LTI5IDA5OjAwIiwiYWxsRGF5IjpmYWxzZSwiY2xhc3NOYW1lIjoibGFiZWwtcGluayIsInRleHRDb2xvciI6IndoaXRlIn0seyJpZCI6IlpQTjF4UzZEUmR1dUVTQWJ0a3JMTGciLCJ0aXRsZSI6IuiwgeeUn-aXpe-8nyIsInN0YXJ0IjoiMjAxOC0wNC0yOSAwMDowMCIsImVuZCI6IjIwMTgtMDQtMzAgMDA6MDAiLCJhbGxEYXkiOmZhbHNlLCJjbGFzc05hbWUiOiJsYWJlbC1pbmZvIiwidGV4dENvbG9yIjoid2hpdGUifSx7ImlkIjoiNWJXYkMxdEdRTEtuTGtJZ0J5djZ2QSIsInRpdGxlIjoi56ys5LqM5Liq5rWL6K-V5p2l5LqG5ZWmIiwic3RhcnQiOiIyMDE4LTA0LTE3IDE0OjAwIiwiZW5kIjoiMjAxOC0wNC0xNyAxNDozMCIsImFsbERheSI6ZmFsc2UsImNsYXNzTmFtZSI6ImxhYmVsLXN1Y2Nlc3MiLCJ0ZXh0Q29sb3IiOiJ3aGl0ZSJ9XQ";
        try {
          Object o=  BytesUtil.bytesToObject(EncodeUtils.base64Decode(json));
            System.out.println(o);
//           Collection<HashMap> l= JsonUtil.fromJsonToCollection(o.toString(), HashMap.class);
            Collection l=  JacksonMapper.nonEmptyMapper().fromJson(o.toString(), Collection.class);
            System.out.println(l);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


    }
}
