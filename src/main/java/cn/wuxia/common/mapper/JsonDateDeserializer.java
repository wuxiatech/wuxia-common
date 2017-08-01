/*
 * Created on :Jul 26, 2012 Author :songlin.li
 */
package cn.wuxia.common.mapper;

import java.io.IOException;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import cn.wuxia.common.util.DateUtil;
import cn.wuxia.common.util.DateUtil.DateFormatter;

/**
 * 
 * [ticket id]
 * @see JsonDateSerializer
 * @author songlin
 * @ Version : V<Ver.No> <15 May, 2015>
 */
public class JsonDateDeserializer extends JsonDeserializer<Date> {

    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return DateUtil.stringToDate(p.getText(), DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS);
    }
}
