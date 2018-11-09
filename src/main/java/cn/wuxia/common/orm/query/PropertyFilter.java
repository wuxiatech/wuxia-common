/**
 * Copyright (c) 2005-20010 springside.org.cn Licensed under the Apache License,
 * Version 2.0 (the "License"); $Id: PropertyFilter.java 1205 2010-09-09
 * 15:12:17Z calvinxiu $
 */
package cn.wuxia.common.orm.query;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import cn.wuxia.common.util.ServletUtils;
import cn.wuxia.common.util.reflection.ConvertUtil;

/**
 * With specific ORM implementation-independent attribute filter conditions
 * wrapper class, major record page simple search filter conditions.
 * 
 * @author calvin
 */
public class PropertyFilter implements Serializable {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -8575301086035426871L;

    /** @description : OR relationship between the multiple attribute delimiters. */
    public static final String OR_SEPARATOR = "_OR_";

    private MatchType matchType = null;

    private Object matchValue = null;

    private Class<?> propertyClass = null;

    private String[] propertyNames = null;

    public PropertyFilter() {
    }

    /**
     * @param filterName Compare string attributes, including to be compared
     *            comparison type attribute value types and attribute list. eg.
     *            LLS_NAME_OR_LOGIN_NAME
     * @param value The values ​​to be compared.
     */
    public PropertyFilter(final String filterName, final String value) {

        String firstPart = StringUtils.substringBefore(filterName, "_");
        String matchTypeCode = StringUtils.substring(firstPart, 0, firstPart.length() - 1);
        String propertyTypeCode = StringUtils.substring(firstPart, firstPart.length() - 1, firstPart.length());

        try {
            matchType = MatchType.valueOf(matchTypeCode);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("filter name " + filterName
                    + "Written not by the rules, can not get the attribute type of comparison.", e);
        }

        try {
            propertyClass = Enum.valueOf(PropertyType.class, propertyTypeCode).getValue();
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("filter name " + filterName + "Written not by the rules, you can not get the property value type.", e);
        }

        String propertyNameStr = StringUtils.substringAfter(filterName, "_");
        Assert.isTrue(StringUtils.isNotBlank(propertyNameStr), "filter name" + filterName
                + "Written not by the rules,can not get the attribute name.");
        propertyNames = StringUtils.splitByWholeSeparator(propertyNameStr, PropertyFilter.OR_SEPARATOR);

        this.matchValue = ConvertUtil.convertToObject(value, propertyClass);
    }

    /**
     * @description : From HttpRequest create PropertyFilter list of default the
     *              Filter attribute name prefix for the filter.
     * @see #buildFromHttpRequest(HttpServletRequest, String)
     */
    public static List<PropertyFilter> buildFromHttpRequest(final HttpServletRequest request) {
        return buildFromHttpRequest(request, "filter");
    }

    /**
     * @description : Create PropertyFilter list from HttpRequest PropertyFilter
     *              naming rules _ attribute name for Filter property prefix _
     *              comparison type attribute type. eg. filter_EQS_name
     *              filter_FLS_name_OR_email
     */
    public static List<PropertyFilter> buildFromHttpRequest(final HttpServletRequest request, final String filterPrefix) {
        List<PropertyFilter> filterList = new ArrayList<PropertyFilter>();

        // Get attributes prefix name in the request parameters is constructed
        // to remove the the parameters Map of the prefix name.
        Map<String, Object> filterParamMap = ServletUtils.getParametersStartingWith1(request, filterPrefix + "_");

        // The analysis parameters Map, constructed PropertyFilter list
        for (Map.Entry<String, Object> entry : filterParamMap.entrySet()) {
            String filterName = entry.getKey();
            String value = (String) entry.getValue();
            // If the value is null value, then ignore this filter.
            if (StringUtils.isNotBlank(value)) {
                PropertyFilter filter = new PropertyFilter(filterName, value);
                filterList.add(filter);
            }
        }

        return filterList;
    }

    /**
     * @description : Get the type of the comparison value.
     */
    public Class<?> getPropertyClass() {
        return propertyClass;
    }

    /**
     * @description : Get comparative approach.
     */
    public MatchType getMatchType() {
        return matchType;
    }

    /**
     * @description : Get comparison value.
     */
    public Object getMatchValue() {
        return matchValue;
    }

    /**
     * @description : Get attribute name list.
     */
    public String[] getPropertyNames() {
        return propertyNames;
    }

    /**
     * @description : Get unique compare attribute name.
     */
    public String getPropertyName() {
        Assert.isTrue(propertyNames.length == 1, "There are not only one property in this filter.");
        return propertyNames[0];
    }

    /**
     * @description : Whether to compare multiple properties.
     */
    public boolean hasMultiProperties() {
        return (propertyNames.length > 1);
    }
}
