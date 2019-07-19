/*
 * Created on :2017年10月16日
 * Author     :songlin
 * Change History
 * Version       Date         Author           Reason
 * <Ver.No>     <date>        <who modify>       <reason>
 * Copyright 2014-2020 wuxia.gd.cn All right reserved.
 */
package cn.wuxia.common.test;

import cn.wuxia.common.util.reflection.ConvertUtil;
import cn.wuxia.common.web.httpclient.*;
import cn.wuxia.common.xml.Dom4jXmlUtil;
import jodd.typeconverter.TypeConverterManager;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.*;

public class XmlTest {
    public static void main(String[] args) throws Exception {
//        test();

        String xml = "<xml>" +
                "<return_code><![CDATA[SUCCESS]]></return_code>\n" +
                "<return_msg><![CDATA[OK]]></return_msg>\n" +
                "<result_code><![CDATA[SUCCESS]]></result_code>\n" +
                "<err_code><![CDATA[SUCCESS]]></err_code>\n" +
                "<err_code_des><![CDATA[OK]]></err_code_des>\n" +
                "<mch_billno><![CDATA[18102701225620334]]></mch_billno>\n" +
                "<mch_id><![CDATA[1511021581]]></mch_id>\n" +
                "<detail_id><![CDATA[1000041701201810273000008722414]]></detail_id>\n" +
                "<status><![CDATA[RECEIVED]]></status>\n" +
                "<send_type><![CDATA[API]]></send_type>\n" +
                "<hb_type><![CDATA[NORMAL]]></hb_type>\n" +
                "<total_num>1</total_num>\n" +
                "<total_amount>100</total_amount>\n" +
                "<send_time><![CDATA[2018-10-27 01:24:11]]></send_time>\n" +
                "<hblist>\n" +
                "<hbinfo>\n" +
                "<openid><![CDATA[o_94s1OTMFP0JwWMnbCWhuKa9wPI]]></openid>\n" +
                "<amount>100</amount>\n" +
                "<rcv_time><![CDATA[2018-10-27 01:24:52]]></rcv_time>\n" +
                "</hbinfo>\n" +
                "<hbinfo>\n" +
                "<openid><![CDATA[o_94s1OTMFP0JwWMnbCWhuKa9wPI]]></openid>\n" +
                "<amount>100</amount>\n" +
                "<rcv_time><![CDATA[2018-10-27 01:24:52]]></rcv_time>\n" +
                "</hbinfo>\n" +
                "</hblist>\n" +
                "</xml>";

        //System.out.println(XStreamXmlUtil.xmlToBean(xml, GetRedPackInfoResult.class));
        Map<String, Object> m = Dom4jXmlUtil.xml2map(xml, false);
        //System.out.println(ToStringBuilder.reflectionToString(XMLUtil.converyToJavaBean(xml, GetRedPackInfoResult.class)));
        //System.out.println(ToStringBuilder.reflectionToString(BeanUtil.mapToBean(m, GetRedPackInfoResult.class)));
    }


    public static void test() {
        Object convertValue = TypeConverterManager.get().convertType("123", BigDecimal.class);
        System.out.println(convertValue + "" + convertValue.getClass());

        convertValue = ConvertUtil.convert("2017/10/20 22:22:22", Date.class);
        System.out.println(convertValue + "" + convertValue.getClass());
        convertValue = TypeConverterManager.get().convertType("2017/10/20 22:22:22", Date.class);
        System.out.println(convertValue + "" + convertValue.getClass());


        convertValue = TypeConverterManager.get().convertType("2017-10-20", java.sql.Date.class);
        System.out.println(convertValue + "" + convertValue.getClass());
    }

    @Test
    public void testHttpclient() throws HttpClientException {
        HttpClientResponse response = null;
        /**
         * passed
         */
//        response = HttpClientRequest.post("http://127.0.0.1:8060/test/input").setFileContent(new File("/app/banban.log")).execute();
        /**
         * passed
         */
//        response =  HttpClientUtil.upload("http://127.0.0.1:8060/test/file", new File("/app/banban.log"), "imageFile");
        /**
         * passed
         */
//        response = HttpClientRequest.post("http://127.0.0.1:8060/test/form").addParam("name", "123").addParam("value", "789").execute();
        /**
         * passed
         */
//        response = HttpClientRequest.post("http://127.0.0.1:8060/test/form1").addParam("name", "123").addParam("value", "789").execute();
//        response = HttpClientRequest.post("http://127.0.0.1:8060/test/json").setJsonContent("{\"code\":200,\"msg\":\"\",\"data\":\"789\",\"page\":null,\"elapsed\":0}").execute();

//        response = HttpClientRequest.post("http://127.0.0.1:8060/test/json1").setJsonContent("{\"name\":200,\"value\":\"789\"}").execute();

//        response = HttpClientRequest.post("http://127.0.0.1:8060/test/input").setByteContent("123123123".getBytes()).execute();

//        response = HttpClientRequest.post("http://127.0.0.1:8060/test/text").setStringContent("{\"name\":200,\"value\":\"789\"}").execute();


//        System.out.println(response.getStringResult());

    }

//    @Test
    public void testAsync() throws Exception {
        HttpClientRequest request = new HttpClientRequest("http://127.0.0.1:8060/test/text");
        request.addParam("url", "http://fsdalfkjsadflkjsadlfkjasdlf");
        long start = System.currentTimeMillis();
        HttpClientRequest[] par = new HttpClientRequest[10];
        for (int i = 0; i < 10; i++) {
            //System.out.println("***********第" + i + "个***********");
            //call(request);
            //HttpClientUtil.get(request);
            par[i] = request;
        }
        HttpAsyncClientUtil.call(par);
        System.out.println("*************" + (System.currentTimeMillis() - start));
    }
}
