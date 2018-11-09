/*
 * Created on :2017年10月16日
 * Author     :songlin
 * Change History
 * Version       Date         Author           Reason
 * <Ver.No>     <date>        <who modify>       <reason>
 * Copyright 2014-2020 wuxia.gd.cn All right reserved.
 */
package cn.wuxia.common.test;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wuxia.common.excel.ExcelUtil;
import cn.wuxia.common.excel.bean.ExcelBean;
import cn.wuxia.common.util.XMLUtil;
import cn.wuxia.common.util.reflection.BeanUtil;
import cn.wuxia.common.util.reflection.ConvertUtil;
import cn.wuxia.common.xml.Dom4jXmlUtil;
import cn.wuxia.common.xml.XStreamXmlUtil;
import com.google.common.collect.Maps;
import jodd.typeconverter.TypeConverterManager;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class ExcelTest {
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

    public static void testExport() throws Exception {
        // System.out.println((MAX_ROWS + 1) / MAX_ROWS + 1);
        // Write the output to a file
        long start = System.currentTimeMillis();
        FileOutputStream fileOut1 = new FileOutputStream("/app/workbook1.XLSx");
        // FileOutputStream fileOut2 = new
        // FileOutputStream("c:/workbook2.xls");
        String[] selfields = new String[]{"apply_organ", "business_line", "name", "form_title", "undertake_date", "remark", "accept", "remark_date",
                "user_name", "office_phone"};
        String[] selfieldsName = new String[]{"提出机构", "业务条线", "承办部门名称", "创意名称", "转承办部门日期", "处理意见", "是否认可", "反馈意见日期", "联系人", "电话"};
        List<Map<String, String>> dataList1 = new ArrayList<Map<String, String>>();
        for (int i = 0; i < (16); i++) {
            Map m = new HashMap();
            for (String selfield : selfields) {
                m.put(selfield, i + " 我是仲文中午呢访问访问了福建省辽");
            }
            dataList1.add(m);
        }
        List<Map<String, Object>> dataList2 = new ArrayList<Map<String, Object>>();
        ExcelBean excelBean = new ExcelBean();
        excelBean.setFileName("workbook1.XLSx");
        excelBean.setDataList(dataList1);
        excelBean.setSelfields(selfields);
        excelBean.setSelfieldsName(selfieldsName);
        excelBean.setSheetName("sheet");
        ExcelUtil.createExcel(excelBean, fileOut1);

        // excelBean.setDataList(dataList2);
        // ExcelUtil.createExcel(excelBean, fileOut2);

        fileOut1.close();
        // fileOut2.close();
        long end = System.currentTimeMillis();
        System.out.println(("create Excel end, Used " + ((end - start) / 1000) + " s"));
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
}
