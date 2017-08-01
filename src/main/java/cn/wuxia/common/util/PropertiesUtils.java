/**
 * Copyright (c) 2005-2010 springside.org.cn Licensed under the Apache License,
 * Version 2.0 (the "License"); $Id: PropertiesUtils.java 1211 2010-09-10
 * 16:20:45Z calvinxiu $
 */
package cn.wuxia.common.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.Assert;
import org.springframework.util.DefaultPropertiesPersister;
import org.springframework.util.PropertiesPersister;

import com.google.common.collect.Maps;

/**
 * Get Properties Util Tools
 * 
 * @author songlin.li
 */
public class PropertiesUtils {

    private static final String DEFAULT_ENCODING = "UTF-8";

    private static Logger logger = LoggerFactory.getLogger(PropertiesUtils.class);

    private static final PropertiesPersister propertiesPersister = new DefaultPropertiesPersister();

    private static final ResourceLoader resourceLoader = new DefaultResourceLoader();

    private static final PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();

    // =========================开始静态获取properties的值，修改的properties重启后生效或者clean后生效=====================

    private static Properties initProperties;

    /**
     * 请使用
     * @author songlin
     * @return
     */

    @Deprecated
    public static Properties getProperties() {
        if (initProperties == null || initProperties.isEmpty())
            initProperties = loadProperties();
        return initProperties;
    }

    /**
    * 请使用@Value标签获取application.properties的值
    * @author songlin
    * @param key
    * @return
    */
    @Deprecated
    public static String getPropertiesValue(String key) {
        if (getProperties().containsKey(key)) {
            String value = getProperties().getProperty(key);
            String[] keys = StringUtil.getTemplateKey(value);
            if (ArrayUtil.isNotEmpty(keys)) {
                Map<String, Object> param = Maps.newHashMap();
                for (String k : keys) {
                    param.put(k, getPropertiesValue(k));
                }
                return StringUtil.replaceKeysSimple(param, value);
            }
            return value;
        } else
            return "";
    }

    /**
     * @description : clear properties
     */
    @Deprecated
    public static void clean() {
        initProperties.clear();
    }

    // =========================结束静态获取properties的值，修改的properties重启后生效或者clean后生效=====================

    /**
     * Loading multiple properties files , the same attribute value will
     * overwrite the last file loaded before the load. The file path to use the
     * Spring Resource format, files encoded using UTF-8.
     * <ul>
     * <li>Must support fully qualified URLs, e.g. "file:C:/test.dat".
     * <li>Must support classpath pseudo-URLs, e.g. "classpath:test.dat".
     * <li>Should support relative file paths, e.g. "WEB-INF/test.dat". (This
     * will be implementation-specific, typically provided by an
     * ApplicationContext implementation.)
     * </ul>
     * 
     * @see org.springframework.beans.factory.config.PropertyPlaceholderConfigurer
     */
    public static Properties loadProperties(String... resourcesPaths) {
        if (ArrayUtils.isEmpty(resourcesPaths)) {
            resourcesPaths = new String[] { "classpath:application.properties", "classpath:application.location.properties" };
        }
        Properties p = new Properties();
        for (String location : resourcesPaths) {

            logger.debug("Loading properties file from:" + location);

            InputStream is = null;
            try {
                Resource[] resources = patternResolver.getResources(location);
                for (Resource resource : resources) {
                    is = resource.getInputStream();
                    Properties properties = new Properties();
                    propertiesPersister.load(properties, new InputStreamReader(is, DEFAULT_ENCODING));
                    copy(p, properties);
                }
            } catch (IOException ex) {
                logger.warn("Could not load properties from classpath:" + location + ": " + ex.getMessage());
            } finally {
                if (is != null) {
                    IOUtils.closeQuietly(is);
                }
            }
        }
        return p;
    }

    /*---------------------------以下部分为实例部分，可以动态获取properties的值，每次调用都需要实例化-----------------------------------*/
    private Properties properties;

    private String filePath;

    /**
     * 支持spring classpath 模式，如:classpath:application.properties
     * 
     * @param resourcesPaths
     */
    public PropertiesUtils(String resourcesPaths) {
        properties = loadProperties(resourcesPaths);
        filePath = resourcesPaths;
    }

    /**
     * 请使用PropertiesUtils(String resourcesPaths)构造方法
     * 默认读取:classpath:application.properties，classpath:application.location.
     * properties
     */
    @Deprecated
    public PropertiesUtils() {
        properties = loadProperties();
    }

    /**
     * @description : Load the configuration file
     * @param filePath To read the configuration file path + name
     */
    public void setProperties(String filePath) {
        try {
            InputStream inputFile;
            try {
                inputFile = new FileInputStream(filePath);
            } catch (Exception e) {
                inputFile = this.getClass().getClassLoader().getResourceAsStream(filePath);
            }
            this.filePath = filePath;
            properties.load(inputFile);
            inputFile.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * @description : get property value by key
     * @param key
     * @return key's value
     */
    public String getPropertyValue(String key) {
        if (properties.containsKey(key)) {
            String value = properties.getProperty(key);
            String[] keys = StringUtil.getTemplateKey(value);
            if (ArrayUtil.isNotEmpty(keys)) {
                Map<String, Object> param = Maps.newHashMap();
                for (String k : keys) {
                    param.put(k, getPropertyValue(k));
                }
                return StringUtil.replaceKeysSimple(param, value);
            }
            return value;
        } else
            return "";
    }

    /**
     * @description : clear properties
     */
    public void clear() {
        properties.clear();
    }

    /**
     * @description : Change or add a key value , when the key exists in the
     *              properties file of the key values ​​instead of by value ,
     *              when the key does not exist , the key value is the value
     * @param key To deposit the key
     * @param value The value to be deposited
     */
    public void setValue(String key, String value) {
        properties.setProperty(key, value);
    }

    /**
     * @description : Will change the file data into specified file , the file
     *              does not exist in advance.
     * @param fileName File path + file name
     * @param description A description of the file
     */
    public void saveFile(String fileName, String description) {
        try {
            FileOutputStream outputFile = new FileOutputStream(fileName);
            properties.store(outputFile, description);
            outputFile.flush();
            outputFile.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * @description :saveFile
     */
    public void saveFile() {
        String fileName = ClassLoaderUtil.getAbsolutePathOfClassLoaderClassPath() + filePath;
        fileName = fileName.replaceAll("file:/", "");
        logger.info("-------------saveFile(): " + fileName);
        try {
            FileOutputStream outputFile = new FileOutputStream(fileName);
            properties.store(outputFile, "");
            outputFile.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 获取所有属性
     * @author songlin
     * @return
     */
    public Map<String, Object> getAll() {
        Map<String, Object> m = Maps.newHashMap();
        Iterator<Entry<Object, Object>> it = properties.entrySet().iterator();
        while (it.hasNext()) {
            Entry<Object, Object> entry = it.next();
            String key = entry.getKey() + "";
            Object value = entry.getValue();
            m.put(key, value);
        }
        return m;
    }

    public static void main(String[] args) {
        Properties propertie = loadProperties("file:/Users/songlin/Documents/ibmallworkspace/WeChatAPI/src/main/resources/wechat.config.properties",
                "file:/Users/songlin/Documents/ibmallworkspace/MicroApp/src/main/resources/wechat.config.properties");
        //        logger.error(propertie.getProperty("cas.logoutUrl"));
        for (Map.Entry<Object, Object> s : propertie.entrySet()) {
            System.out.println(s.getKey() + "   " + s.getValue());
        }
        //        System.out.println(ClassLoaderUtil.getAbsolutePathOfClassLoaderClassPath());
    }

    /**
     * 从orgi拷贝内容到desc, 为空则不拷贝
     * @author songlin
     * @param desc
     * @param orgi
     */
    public static void copy(Properties desc, Properties orgi) {
        Assert.notNull(desc, "desc is null");
        Assert.notNull(orgi, "orgi is null");
        for (Map.Entry<Object, Object> set : orgi.entrySet()) {
            if (StringUtil.isNotBlank(set.getValue())) {
                desc.put(set.getKey(), set.getValue());
            }
        }
    }

}
