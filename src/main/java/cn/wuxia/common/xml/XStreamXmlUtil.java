package cn.wuxia.common.xml;

import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Element;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;

public class XStreamXmlUtil {

    /** XML转Bean对象 */
    @SuppressWarnings("unchecked")
    public static <T> T xmlToBean(String xml, Class<T> clazz) {
        XStream xstream = new XStream();
        //xstream.registerConverter(new DateConverter());
        xstream.autodetectAnnotations(true);
        xstream.processAnnotations(clazz);
        xstream.setClassLoader(clazz.getClassLoader());
        return (T) xstream.fromXML(xml);
    }

    /** Bean对象转XML */
    public static String beanToXml(Object bean) {
//      return "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + new XStream().toXML(bean);
        XStream xstream = new XStream();
        //xstream.registerConverter(new DateConverter());
        xstream.autodetectAnnotations(true);
        return xstream.toXML(bean);
    }
}
