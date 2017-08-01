package cn.wuxia.common.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class XMLUtil {
    private static Logger logger = LoggerFactory.getLogger(XMLUtil.class);

    /**
     * get xml tag's text by tagName
     * 
     * @author songlin.li
     * @param inputStr
     * @param tagName
     * @return
     */
    public static String getTagText(String inputStr) {
        if (StringUtil.isBlank(inputStr)) {
            return "";
        }
        StringReader read = new StringReader(inputStr);
        SAXReader reader = new SAXReader();
        Document doc;
        String outputString = "";
        try {
            doc = reader.read(read);
            Element root = doc.getRootElement();
            outputString = root.getText();
        } catch (DocumentException e) {
            logger.error(e.getMessage(), e);
        }
        return outputString;
    }

    /**
     * get children tag's text
     * 
     * @author songlin.li
     * @param inputStr
     * @param tagName
     * @return
     */
    public static String getChildTagText(String inputStr, String tagName) {
        if (StringUtil.isBlank(inputStr)) {
            return "";
        }
        StringReader read = new StringReader(inputStr);
        SAXReader reader = new SAXReader();
        Document doc;
        String outputString = "";
        try {
            doc = reader.read(read);
            Element field = doc.getRootElement();
            Element output = field.element(tagName);
            outputString = output.getText();
        } catch (DocumentException e) {
            logger.error(e.getMessage(), e);
        }
        return outputString;
    }

    /**
     * Description of the method
     * 
     * @author songlin.li
     * @param inputStr
     * @param tagName
     * @return
     */
    public static String getChildTagXML(String inputStr, String tagName) {
        if (StringUtil.isBlank(inputStr)) {
            return "";
        }
        StringReader read = new StringReader(inputStr);
        SAXReader reader = new SAXReader();
        Document doc;
        String outputString = "";
        try {
            doc = reader.read(read);
            Element field = doc.getRootElement();
            Element output = field.element(tagName);
            outputString = output.asXML();
        } catch (DocumentException e) {
            logger.error(e.getMessage(), e);
        }
        return outputString;
    }

    /**
     * path is /WEB-INF/classes + fileName
     * 
     * @author songlin.li
     * @param fileName fileName
     * @param tagName second level tagName
     * @return
     */
    public static Map<String, String> getXML(String fileName, String tagName) {
        Map<String, String> map = new HashMap<String, String>();
        try {
            String path = ClassLoaderUtil.getClassesPath(fileName);
            File file = FileUtil.getFile(path);
            if (file.exists()) {
                SAXReader reader = new SAXReader();
                Document doc = reader.read(file);
                Element root = doc.getRootElement();
                Iterator it = root.elementIterator(tagName);
                while (it.hasNext()) {
                    Element txtElement = (Element) it.next();
                    map.put(txtElement.attributeValue("name"), txtElement.asXML());
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return map;
    }

    /**
     * Description of the method
     * 
     * @author songlin.li
     * @param inputStr
     * @param tagName
     * @return
     */
    public static List<Map<String, String>> getTagNameMap(String inputStr, String tagName) {
        StringReader read = new StringReader(inputStr);
        SAXReader reader = new SAXReader();
        Document doc;
        List<Map<String, String>> list = new ArrayList();
        try {
            doc = reader.read(read);
            Element root = doc.getRootElement();
            Iterator childs = root.elementIterator();
            while (childs.hasNext()) {
                Element nodes = (Element) childs.next();
                if (StringUtil.equals(nodes.getName(), tagName)) {
                    Iterator it = nodes.elementIterator();
                    Map<String, String> map = new HashMap<String, String>();
                    while (it.hasNext()) {
                        Element txtElement = (Element) it.next();
                        map.put(txtElement.getName(), txtElement.getText());
                    }
                    list.add(map);
                }
            }
        } catch (DocumentException e) {
            logger.error(e.getMessage(), e);
        }
        return list;
    }

    public static String XMLFileToXMLString(String fileName) {
        String path = ClassLoaderUtil.getClassesPath(fileName);
        logger.debug("file Name:" + path);
        File file = FileUtil.getFile(path);
        if (file.exists()) {
            try {
                SAXReader reader = new SAXReader();
                Document doc = reader.read(file);
                return doc.asXML();
            } catch (DocumentException e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            logger.debug("file not found!");
            return "";
        }
        return "";
    }

    /**
     * 解析xml,返回第一级元素键值对。如果第一级元素有子节点，则此节点的值是子节点的xml数据。
     * @param strxml
     * @return
     * @throws org.jdom2.JDOMException
     * @throws IOException
     */
    public static Map<String, String> doXMLParse(String strxml) throws org.jdom2.JDOMException, IOException {
        strxml = strxml.replaceFirst("encoding=\".*\"", "encoding=\"UTF-8\"");

        if (null == strxml || "".equals(strxml)) {
            return null;
        }

        Map<String, String> m = Maps.newHashMap();

        InputStream in = new ByteArrayInputStream(strxml.getBytes("UTF-8"));
        org.jdom2.input.SAXBuilder builder = new org.jdom2.input.SAXBuilder();
        org.jdom2.Document doc = builder.build(in);
        org.jdom2.Element root = doc.getRootElement();
        List<org.jdom2.Element> list = root.getChildren();
        Iterator<org.jdom2.Element> it = list.iterator();
        while (it.hasNext()) {
            org.jdom2.Element e = (org.jdom2.Element) it.next();
            String k = e.getName();
            String v = "";
            List<org.jdom2.Element> children = e.getChildren();
            if (children.isEmpty()) {
                v = e.getTextNormalize();
            } else {
                v = XMLUtil.getChildrenText(children);
            }

            m.put(k, v);
        }

        //关闭流
        in.close();

        return m;
    }

    /**
     * 获取子结点的xml
     * @param children
     * @return String
     */
    public static String getChildrenText(List<org.jdom2.Element> children) {
        StringBuffer sb = new StringBuffer();
        if (!children.isEmpty()) {
            Iterator<org.jdom2.Element> it = children.iterator();
            while (it.hasNext()) {
                org.jdom2.Element e = (org.jdom2.Element) it.next();
                String name = e.getName();
                String value = e.getTextNormalize();
                List<org.jdom2.Element> list = e.getChildren();
                sb.append("<" + name + ">");
                if (!list.isEmpty()) {
                    sb.append(XMLUtil.getChildrenText(list));
                }
                sb.append(value);
                sb.append("</" + name + ">");
            }
        }

        return sb.toString();
    }

    /**
     * 获取xml编码字符集
     * @param strxml
     * @return
     * @throws IOException 
     * @throws org.jdom2.JDOMException 
     */
    public static String getXMLEncoding(String strxml) throws org.jdom2.JDOMException, IOException {
        InputStream in = String2Inputstream(strxml);
        org.jdom2.input.SAXBuilder builder = new org.jdom2.input.SAXBuilder();
        org.jdom2.Document doc = builder.build(in);
        in.close();
        return (String) doc.getProperty("encoding");
    }

    public static void main(String[] args) {
        String template = XMLFileToXMLString("/template/contact.xml");

        template = template.replaceFirst("<\\?.*?>", "");
        // logger.debug(template);

        // System.out.println(HTMLUtil.parseHTML(template));

        // getXML("E:\\Crown\\workspace\\ServicePartner\\src\\main\\webapp\\common\\template.xml");
        Map m = getXML("/template/contact.xml", "DataSet");
        for (Object l : m.values()) {
            System.out.println(getTagNameMap(l.toString(), "Contracts"));
        }
    }

    public static InputStream String2Inputstream(String str) {
        return new ByteArrayInputStream(str.getBytes());
    }

    /** 
     * JavaBean转换成xml 
     * 默认编码UTF-8 
     * @param obj 
     * @param writer 
     * @return  
     */
    public static String convertToXml(Object obj) {
        return convertToXml(obj, "UTF-8");
    }

    /** 
     * JavaBean转换成xml 
     * @param obj 
     * @param encoding  
     * @return  
     */
    public static String convertToXml(Object obj, String encoding) {
        String result = null;
        try {
            JAXBContext context = JAXBContext.newInstance(obj.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, encoding);

            StringWriter writer = new StringWriter();
            marshaller.marshal(obj, writer);
            result = writer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /** 
     * xml转换成JavaBean 
     * @param xml 
     * @param c 
     * @return 
     */
    @SuppressWarnings("unchecked")
    public static <T> T converyToJavaBean(String xml, Class<T> c) {
        T t = null;
        try {
            JAXBContext context = JAXBContext.newInstance(c);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            t = (T) unmarshaller.unmarshal(new StringReader(xml));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return t;
    }
}
