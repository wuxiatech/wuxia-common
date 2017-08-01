/*
* Created on :15 Sep, 2014
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 武侠科技 All right reserved.
*/
package cn.wuxia.common.sql;

import java.io.Serializable;

public class Parameter implements Serializable {
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;

    private String name;

    private String lable;

    private Object value;

    private Class<?> type;

    private String validate;

    public Parameter() {
    }

    public Parameter(String name, String lable, Object value, Class<?> type, String validate) {
        this.name = name;
        this.lable = lable;
        this.value = value;
        this.type = type;
        this.validate = validate;
    }

    public String getLabel() {
        return this.lable;
    }

    public String getName() {
        return this.name;
    }

    public String getValidate() {
        return this.validate;
    }

    public Object getValue() {
        return this.value;
    }

    public Class<?> getType() {
        return this.type;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setValidate(String validate) {
        this.validate = validate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLabel(String label) {
        this.lable = label;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

}
