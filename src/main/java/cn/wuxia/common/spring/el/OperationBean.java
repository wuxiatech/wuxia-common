/*
* Created on :2015年10月30日
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 武侠科技 All right reserved.
*/
package cn.wuxia.common.spring.el;

import org.springframework.util.Assert;

/**
 * Base class for cache operations.
 *
 * @author songlin.li
 * @since 2015-10-30
 */
public class OperationBean {

    private String value = "";

    private String condition = "";

    private String unless = "";

    public void setValue(String value) {
        Assert.notNull(value);
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public void setCondition(String condition) {
        Assert.notNull(condition);
        this.condition = condition;
    }

    public String getCondition() {
        return this.condition;
    }

    public String getUnless() {
        return unless;
    }

    public void setUnless(String unless) {
        Assert.notNull(unless);
        this.unless = unless;
    }

    /**
     * This implementation compares the {@code toString()} results.
     * @see #toString()
     */
    @Override
    public boolean equals(Object other) {
        return (other instanceof OperationBean && toString().equals(other.toString()));
    }

    /**
     * This implementation returns {@code toString()}'s hash code.
     * @see #toString()
     */
    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * Return an identifying description for this cache operation.
     * <p>Has to be overridden in subclasses for correct {@code equals}
     * and {@code hashCode} behavior. Alternatively, {@link #equals}
     * and {@link #hashCode} can be overridden themselves.
     */
    @Override
    public String toString() {
        return getOperationDescription().toString();
    }

    /**
     * Return an identifying description for this caching operation.
     * <p>Available to subclasses, for inclusion in their {@code toString()} result.
     */
    protected StringBuilder getOperationDescription() {
        StringBuilder result = new StringBuilder(getClass().getSimpleName());
        result.append("value='").append(this.value);
        result.append("' | condition='").append(this.condition).append("'");
        result.append("' | unless='").append(this.unless).append("'");
        return result;
    }
}
