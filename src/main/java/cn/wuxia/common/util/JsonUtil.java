/*
 * Created on :2012-9-25 Author :songlin.li
 */
package cn.wuxia.common.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
        jsonString = decodeUrl(jsonString);
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
        json = decodeUrl(json);
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

    public static String toJsonWithDateFormat(Object obj, DateFormatter formatter) throws JsonProcessingException {
        JacksonMapper jm = new JacksonMapper(Include.NON_EMPTY);
        SimpleDateFormat dateFormat = new SimpleDateFormat(formatter == null ? DateFormatter.FORMAT_YYYY_MM_DD.getFormat() : formatter.getFormat());
        return jm.getMapper().setDateFormat(dateFormat).writeValueAsString(obj);

    }

    public static String toFullJsonWithDateFormat(Object obj, DateFormatter formatter) throws JsonProcessingException {
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

}
