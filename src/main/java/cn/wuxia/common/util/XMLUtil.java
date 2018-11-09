package cn.wuxia.common.util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.*;

/**
 * @see {@link cn.wuxia.common.xml.Dom4jXmlUtil} {@link cn.wuxia.common.xml.XStreamXmlUtil}
 */
public class XMLUtil {
    private static Logger logger = LoggerFactory.getLogger(XMLUtil.class);

    /**
     * get xml tag's text by tagName
     *
     * @param inputStr
     * @param tagName
     * @return
     * @author songlin.li
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
            root.elements();
            outputString = root.getText();
        } catch (DocumentException e) {
            logger.error(e.getMessage(), e);
        }
        return outputString;
    }

    /**
     * get children tag's text
     *
     * @param inputStr
     * @param tagName
     * @return
     * @author songlin.li
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
     * @param inputStr
     * @param tagName
     * @return
     * @author songlin.li
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
     * @param fileName fileName
     * @param tagName  second level tagName
     * @return
     * @author songlin.li
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
     * @param inputStr
     * @param tagName
     * @return
     * @author songlin.li
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


    public static InputStream String2Inputstream(String str) {
        return new ByteArrayInputStream(str.getBytes());
    }

    /**
     * JavaBean转换成xml
     * 默认编码UTF-8
     *
     * @param obj
     * @param writer
     * @return
     */
    public static String convertToXml(Object obj) {
        return convertToXml(obj, "UTF-8");
    }

    /**
     * JavaBean转换成xml
     *
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
     *
     * @param xml
     * @param c
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T converyToJavaBean(String xml, Class<T> c) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(c);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (T) unmarshaller.unmarshal(new StringReader(xml));
    }

}
