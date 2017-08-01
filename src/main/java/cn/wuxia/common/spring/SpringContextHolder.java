/**
 * Copyright (c) 2005-2010 springside.org.cn Licensed under the Apache License,
 * Version 2.0 (the "License"); $Id: SpringContextHolder.java 1211 2010-09-10
 * 16:20:45Z calvinxiu $
 */
package cn.wuxia.common.spring;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ServletContextAware;

/**
 * static save Spring ApplicationContext, get ApplicaitonContext in everywhere.
 * 
 * @author songlin.li
 */
public class SpringContextHolder implements ApplicationContextAware, ServletContextAware, DisposableBean {

    private static ApplicationContext applicationContext = null;

    private static ServletContext servletContext = null;

    private static Logger logger = LoggerFactory.getLogger(SpringContextHolder.class);

    /**
     * implements ApplicationContextAware interface, injection Context in static
     * variable.
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        logger.debug("injection ApplicationContext in SpringContextHolder:" + applicationContext);

        if (SpringContextHolder.applicationContext != null) {
            logger.warn(
                    "SpringContextHolder ApplicationContext has been replace,inhere ApplicationContext:" + SpringContextHolder.applicationContext);
        }

        SpringContextHolder.applicationContext = applicationContext; // NOSONAR
    }

    /**
     * implements DisposableBean interface, close Context and clean static
     * variable.
     */
    public void destroy() throws Exception {
        SpringContextHolder.clear();
    }

    /**
     * get static variable from ApplicationContext.
     */
    public static ApplicationContext getApplicationContext() {
        assertContextInjected();
        return applicationContext;
    }

    /**
     * get bean from applicationContext.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        assertContextInjected();
        return (T) applicationContext.getBean(name);
    }

    /**
     * get bean from applicationContext.
     */
    public static <T> T getBean(String name, boolean required) {
        if (required) {
            return getBean(name);
        } else {
            try {
                return getBean(name);
            } catch (BeansException e) {
                logger.warn(name, e.getCause());
                return null;
            }
        }

    }

    /**
     * get bean from applicationContext.
     */
    public static <T> T getBean(Class<T> requiredType) {
        assertContextInjected();
        return applicationContext.getBean(requiredType);
    }

    /**
     * get bean from applicationContext.
     */
    public static <T> T getBean(Class<T> requiredType, boolean required) {
        if (required) {
            return getBean(requiredType);
        } else {
            try {
                return getBean(requiredType);
            } catch (BeansException e) {
                logger.warn(requiredType.getName(), e.getCause());
                return null;
            }
        }

    }

    /**
     * clean ApplicationContext to null at SpringContextHolder.
     */
    public static void clear() {
        logger.debug("clean ApplicationContext to null at SpringContextHolder:" + applicationContext);
        applicationContext = null;
    }

    /**
     * check not null ApplicationContext.
     */
    private static void assertContextInjected() {
        if (applicationContext == null) {
            throw new IllegalStateException("applicaitonContext has not injection,please defined SpringContextHolder in applicationContext.xml");
        }
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public static ServletContext getServletContext() {
        return servletContext;
    }

}
