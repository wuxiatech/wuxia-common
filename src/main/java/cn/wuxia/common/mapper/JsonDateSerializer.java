/*
 * Created on :Jul 26, 2012 Author :songlin.li
 */
package cn.wuxia.common.mapper;

import java.io.IOException;
import java.util.Date;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import cn.wuxia.common.util.DateUtil;
import cn.wuxia.common.util.DateUtil.DateFormatter;

/**
 * 
 * [ticket id]
 * @see JsonDateDeserializer
 * @author songlin
 * @ Version : V<Ver.No> <15 May, 2015>
 */
public class JsonDateSerializer extends JsonSerializer<Date> {

    @Override
    public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        String formattedDate = DateUtil.dateToString(value, DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS);
        jgen.writeString(formattedDate);
    }
}
