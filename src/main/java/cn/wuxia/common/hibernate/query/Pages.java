/**
 * 
 */
package cn.wuxia.common.hibernate.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

import cn.wuxia.common.hibernate.query.Sort.Direction;
import cn.wuxia.common.hibernate.query.Sort.Order;
import cn.wuxia.common.util.NumberUtil;

/**
 * initialize value, PageNo=1, PageSize=10, first=1.
 * 
 * @param <T> Page Type.
 * @author songlin.li 2012-5-30
 */
@JsonAutoDetect
public class Pages<T> implements Serializable {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -6560967807524300982L;

    // -- page parameter --//
    protected int pageNo = 1;

    protected int pageSize = -1;

    protected String sortPorperty;

    protected String sortOrder;

    protected boolean autoCount = true;

    protected boolean autoReturnFirstPage = false;

    // -- return result --//
    protected List<T> result = new ArrayList<T>();

    protected List<Conditions> conditions = new LinkedList<Conditions>();

    protected Sort sort;

    protected long totalCount = -1;

    protected int totalPages = 0;

    public Pages() {
    }

    public Pages(int pageSize) {
        this.pageSize = pageSize;
    }

    public Pages(int pageNo, int pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

    public Pages(int pageNo, int pageSize, Sort sort) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.sort = sort;
    }

    // --for page used --//
    /**
     * get current page number, start with 1 and the initialize value is 1.
     */
    public int getPageNo() {
        return pageNo;
    }

    /**
     * set current page number.
     */
    public void setPageNo(final int pageNo) {
        this.pageNo = pageNo;

        if (pageNo < 1) {
            this.pageNo = 1;
        }
    }

    /**
     * return Page object and reset PageNo
     */
    public Pages<T> pageNo(final int thePageNo) {
        setPageNo(thePageNo);
        return this;
    }

    /**
     * every page size,default -1.
     */
    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(final int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * return Page object and reset pageSize
     */
    public Pages<T> pageSize(final int thePageSize) {
        setPageSize(thePageSize);
        return this;
    }

    /**
     * pageNo and pageSize calculate first data position, default is 1. when
     * first data bigger than total count then return first page.
     */
    public int getFirst() {
        this.pageNo = pageNo < 1 ? 1 : pageNo;
        int offset = ((pageNo - 1) * pageSize) + 1;
        if (isAutoReturnFirstPage()) {
            if (offset > this.getTotalCount()) {
                this.setPageNo(1);
                return 1;
            }
        }
        return offset;
    }

    public void setSortPorperty(final String sortPorperty) {
        this.sortPorperty = sortPorperty;
    }

    public Pages<T> orderBy(final String theOrderBy) {
        setSortPorperty(theOrderBy);
        return this;
    }

    /**
     * set order mode.
     * 
     * @param orderType 's value is desc or asc, when order is more than one
     *            value, used ',' split them.
     */
    public void setSortOrder(final String sortOrder) {
        String lowcaseOrder = StringUtils.lowerCase(sortOrder);

        // check order
        String[] orders = StringUtils.split(lowcaseOrder, ',');
        for (String orderStr : orders) {
            if (!StringUtils.equalsIgnoreCase(Direction.DESC.toString(), orderStr)
                    && !StringUtils.equalsIgnoreCase(Direction.ASC.toString(), orderStr)) {
                throw new IllegalArgumentException("order:" + orderStr + " unavailable ");
            }
        }

        this.sortOrder = lowcaseOrder;
    }

    public Pages<T> order(final String theOrder) {
        setSortOrder(theOrder);
        return this;
    }

    /**
     * generate order by
     * 
     * @author songlin.li
     * @return
     */
    @JsonIgnore
    public Sort getSort() {
        if (isOrderBySetted()) {
            String[] orderByArray = StringUtils.split(this.sortPorperty, ',');
            String[] orderArray = StringUtils.split(this.sortOrder, ',');

            Assert.isTrue(orderByArray.length == orderArray.length,
                    "Paging multiple sort parameters in the sort field and sort direction is not equal");
            List<Order> orders = Lists.newArrayList();
            for (int i = 0; i < orderByArray.length; i++) {
                if (StringUtils.equalsIgnoreCase(Direction.ASC.toString(), orderArray[i])) {
                    orders.add(new Order(Direction.ASC, orderByArray[i]));
                } else {
                    orders.add(new Order(Direction.DESC, orderByArray[i]));
                }
            }
            return new Sort(orders);
        }
        return this.sort;
    }

    /**
     * add a condition
     * 
     * @author songlin.li
     * @param conditions
     */
    public Pages<T> addCondition(final Conditions condition) {
        conditions.add(condition);
        return this;
    }

    /**
     * whether set order by value, no default value.
     */
    @JsonIgnore
    public boolean isOrderBySetted() {
        return (StringUtils.isNotBlank(sortPorperty) && StringUtils.isNotBlank(sortOrder));
    }

    /**
     * get query result whether count result records,default false.
     */
    public boolean isAutoCount() {
        return autoCount;
    }

    /**
     * Set the query object are automatically before executing count query to
     * obtain the total number of records .
     */
    public void setAutoCount(final boolean autoCount) {
        this.autoCount = autoCount;
    }

    public Pages<T> autoCount(final boolean theAutoCount) {
        setAutoCount(theAutoCount);
        return this;
    }

    /**
     * if autoReturnFirstPage, when first number bigger than total count
     * number,then return to first page.
     * 
     * @return
     */
    public boolean isAutoReturnFirstPage() {
        return autoReturnFirstPage;
    }

    /**
     * @param boolean
     */
    public void setAutoReturnFirstPage(boolean autoReturnFirstPage) {
        this.autoReturnFirstPage = autoReturnFirstPage;
    }

    public Pages<T> autoReturnFirstPage(final boolean autoReturnFirstPage) {
        setAutoReturnFirstPage(autoReturnFirstPage);
        return this;
    }

    // -- Access to query results function --//

    /**
     * Get a list of records within the page.
     */
    public List<T> getResult() {
        return result;
    }

    /**
     * Set the list of records within the page.
     */
    public void setResult(final List<T> result) {
        this.result = result;
    }

    /**
     * The total number of records, default -1.
     */
    public long getTotalCount() {
        return totalCount;
    }

    /**
     * Set the total number of records.
     */
    public void setTotalCount(final long totalCount) {
        this.totalCount = totalCount;
        this.totalPages = getTotalPages();
    }

    /**
     * Total number of pages according to pageSize and totalCount terms, default
     * -1.
     */
    public int getTotalPages() {
        if (totalCount <= 0) {
            return 0;
        }

        long count = totalCount / pageSize;
        if (totalCount % pageSize > 0) {
            count++;
        }

        return NumberUtil.toInteger(count);
    }

    /**
     * Whether there are Next Page.
     */
    public boolean isHasNext() {
        return (pageNo + 1 <= getTotalPages());
    }

    /**
     * Made on the next page Page number, numbered from 1. This page is Last
     * still returns the serial number of the Last.
     */
    public int getNextPage() {
        if (isHasNext()) {
            return pageNo + 1;
        } else {
            return pageNo;
        }
    }

    /**
     * Whether there are previous page.
     */
    public boolean isHasPre() {
        return (pageNo - 1 >= 1);
    }

    /**
     * Made on the page the page number , the serial number from 1. This page is
     * First still returns the serial number of the First.
     */
    public int getPrePage() {
        if (isHasPre()) {
            return pageNo - 1;
        } else {
            return pageNo;
        }
    }

    /**
     * @return
     */
    //@JsonIgnore
    public List<Conditions> getConditions() {
        /**
         * TODO 当某些情况下应该忽略查询条件
         */
        return conditions;
    }

    /**
     * @param List <ConditionsBean>
     */
    public void setConditions(List<Conditions> conditions) {
        this.conditions = conditions;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

}
