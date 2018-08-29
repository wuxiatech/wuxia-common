package cn.wuxia.common.hibernate.query;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;

import cn.wuxia.common.util.StringUtil;

/**
 * <h3>Class name</h3> <h4>Description</h4> <h4>Special Notes</h4>
 * 
 * @version 0.2
 * @author songlin.li 2012-4-26
 */

public class Conditions implements Serializable {

    private static final long serialVersionUID = -6338801823988130524L;

    public final static String EQUAL = " = ";

    public final static String LIKE = " LIKE ";

    public final static String AND = " AND ";

    public final static String OR = " OR ";

    private boolean isIgnoreCase = false;

    private String property;

    private Object value;

    private Object anotherValue;

    private String groupType;

    /**
     * default
     */
    private String conditionType;

    public Conditions() {
    }

    public Conditions(String name, Object value) {
        this.property = name;
        this.value = value;
        this.conditionType = MatchType.EQ.toString();
        this.groupType = AND;
    }

    public Conditions(String name, MatchType matchType, Object value) {
        this.property = name;
        this.conditionType = matchType.toString();
        this.value = value;
        this.groupType = AND;
    }

    public Conditions(String name, MatchType matchType) {
        this.property = name;
        this.conditionType = matchType.toString();
        this.groupType = AND;
    }

    public static Conditions where(String name, Object value) {
        return new Conditions(name, value);
    }

    /**
     * @return the property
     */
    public String getProperty() {
        return property;
    }

    /**
     * @param property the property to set
     */
    public void setProperty(String property) {
        this.property = property;
    }

    /**
     * Original value, not have the modifier
     * 
     * @author songlin
     * @return
     */
    public Object getValue() {
        return value;
    }

    /**
     * @return format the value
     */
    public Object getFormatValue() {
        if (value instanceof String) {
            if (StringUtil.isBlank((String) value)) {
                return null;
            } else if (value.toString().equals("%null%")) {
                return null;
            } else {
                return getMatchType().getFormatValue(value);
            }
        }
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(Object value) {
        this.value = value;
    }

    public void setBetweenValue(Object begin, Object end) {
        this.value = begin;
        this.anotherValue = end;
    }

    public Object getAnotherValue() {
        return anotherValue;
    }

    public void setAnotherValue(Object anotherValue) {
        this.anotherValue = anotherValue;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public MatchType getMatchType() {
        if (StringUtil.isBlank(conditionType)) {
            return MatchType.EQ;
        }
        return MatchType.valueOf(conditionType);
    }

    public void setMatchType(MatchType m) {
        this.conditionType = m.name();
    }

    // public String getConditionType() {
    // return this.conditionType;
    // }

    public void setConditionType(String conditionType) {
        this.conditionType = conditionType;
    }

    /**
     * @return
     */
    public String getGroupType() {
        if (StringUtil.isBlank(groupType))
            return AND;
        return groupType;
    }

    /**
     * @param String
     */
    public void setGroupType(String groupType) {
        groupType = StringUtil.startsWith(groupType, " ") ? groupType : " " + groupType;
        groupType = StringUtil.endsWith(groupType, " ") ? groupType : groupType + " ";
        this.groupType = groupType;
    }

    /**
     * @return
     */
    public boolean isIgnoreCase() {
        return isIgnoreCase;
    }

    /**
     * @param boolean
     */
    public void setIgnoreCase(boolean isIgnoreCase) {
        this.isIgnoreCase = isIgnoreCase;
    }

    public Conditions eq(String name, Object value) {
        return new Conditions(name, value);
    }

    public Conditions isnull(String name) {
        return new Conditions(name, MatchType.ISN);
    }

    public Conditions notnull(String name) {
        return new Conditions(name, MatchType.INN);
    }
}
