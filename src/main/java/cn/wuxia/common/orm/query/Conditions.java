package cn.wuxia.common.orm.query;

import cn.wuxia.common.util.ClassLoaderUtil;
import cn.wuxia.common.util.DateUtil;
import cn.wuxia.common.util.NumberUtil;
import cn.wuxia.common.util.StringUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jodd.typeconverter.TypeConverterManager;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * <h3>Class name</h3> <h4>Description</h4> <h4>Special Notes</h4>
 *
 * @author songlin.li 2012-4-26
 * @version 0.2
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

    private String propertyType;

    /**
     * default
     */
    private String conditionType;

    public Conditions() {
    }

    public Conditions(String name, Object value) {
        this.property = name;
        this.value = value;
        this.conditionType = cn.wuxia.common.orm.query.MatchType.EQ.toString();
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
     * @return
     * @author songlin
     */
    public Object getValue() {
        return getPropertyValue();
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
        return getPropertyValue(anotherValue);
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
     * @param groupType
     */
    public void setGroupType(String groupType) {
        groupType = StringUtil.startsWith(groupType, " ") ? groupType : " " + groupType;
        groupType = StringUtil.endsWith(groupType, " ") ? groupType : groupType + " ";
        this.groupType = groupType;
    }

    @JsonIgnore
    public Class getPropertyTypeClass() {
        if (StringUtil.isBlank(propertyType)) return null;
        try {
            return ClassLoaderUtil.loadClass(this.propertyType);
        } catch (Exception e) {
            return null;
        }
    }

    public Object getPropertyValue(Object value) {
        if (StringUtil.equals(propertyType, "java.lang.String") || StringUtil.equalsIgnoreCase(propertyType, "String")) {
            return value.toString();
        } else if (StringUtil.equals(propertyType, "java.util.Date") || StringUtil.equalsIgnoreCase(propertyType, "Date")) {
            return DateUtil.stringToDate(value.toString());
        } else if (StringUtil.equals(propertyType, "java.lang.Float") || StringUtil.equalsIgnoreCase(propertyType, "Float")) {
            return value.toString();
        } else if (StringUtil.equals(propertyType, "java.lang.Integer") || StringUtil.equalsIgnoreCase(propertyType, "Integer")) {
            return NumberUtil.toInteger(value);
        } else if (StringUtil.equals(propertyType, "java.lang.Double") || StringUtil.equalsIgnoreCase(propertyType, "Double")) {
            return NumberUtil.toDouble(value);
        } else if (StringUtil.equals(propertyType, "java.lang.Short") || StringUtil.equalsIgnoreCase(propertyType, "Short")) {
            return NumberUtil.toShort(value.toString());
        } else if (StringUtil.equals(propertyType, "java.lang.Long") || StringUtil.equalsIgnoreCase(propertyType, "Long")) {
            return NumberUtil.toLong(value);
        } else if (getPropertyTypeClass() != null && getPropertyTypeClass().isEnum()) {
            try {
                return EnumUtils.getEnum(getPropertyTypeClass(), "" + value);
            } catch (Exception e) {
            }
        } else if (getPropertyTypeClass() != null) {
            try {
                return TypeConverterManager.get().convertType(value, getPropertyTypeClass());
            } catch (Exception e) {
            }
        }
        return value;
    }

    public Object getPropertyValue() {
        return getPropertyValue(value);
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    /**
     * @return
     */
    public boolean isIgnoreCase() {
        return isIgnoreCase;
    }

    /**
     * @param isIgnoreCase
     */
    public void setIgnoreCase(boolean isIgnoreCase) {
        this.isIgnoreCase = isIgnoreCase;
    }

    public static Conditions eq(String name, Object value) {
        return new Conditions(name, value);
    }

    public static Conditions isNull(String name) {
        return new Conditions(name, MatchType.ISN);
    }

    public static Conditions notNull(String name) {
        return new Conditions(name, MatchType.INN);
    }

    public static Conditions bw(String name, Object begin, Object end) {
        Conditions condition = new Conditions(name, MatchType.BW, begin);
        condition.setAnotherValue(end);
        return condition;
    }

    public static Conditions llike(String name, String value) {
        return new Conditions(name, MatchType.LL, value);
    }

    public static Conditions rlike(String name, String value) {
        return new Conditions(name, MatchType.RL, value);
    }

    public static Conditions flike(String name, String value) {
        return new Conditions(name, MatchType.FL, value);
    }

    public static Conditions ne(String name, Object value) {
        return new Conditions(name, MatchType.NE, value);
    }

    public static Conditions lte(String name, Object value) {
        return new Conditions(name, MatchType.LTE, value);
    }

    public static Conditions gte(String name, Object value) {
        return new Conditions(name, MatchType.GTE, value);
    }

    public static Conditions lt(String name, Object value) {
        return new Conditions(name, MatchType.LT, value);
    }

    public static Conditions gt(String name, Object value) {
        return new Conditions(name, MatchType.GT, value);
    }

    public static Conditions isTrue(String name) {
        return new Conditions(name, MatchType.EQ, true);
    }

    public static Conditions isFalse(String name) {
        return new Conditions(name, MatchType.EQ, false);
    }
}
