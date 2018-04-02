package cn.wuxia.common.spring.orm;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import cn.wuxia.common.hibernate.query.Conditions;
import cn.wuxia.common.hibernate.query.MatchType;
import cn.wuxia.common.hibernate.query.Pages;
import cn.wuxia.common.hibernate.query.Sort;
import cn.wuxia.common.util.ListUtil;
import cn.wuxia.common.util.StringUtil;

public abstract class JdbcTemplateDao {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected JdbcTemplate jdbcTemplate;

    public abstract void setJdbcTemplate(final DataSource dataSource);

    /**
     * update by sql, jdbcTemplate mode
     * 
     * @param sql
     * @param objs
     */
    public void saveOrUpdate(String sql, Object... objs) {
        jdbcTemplate.update(sql, objs);
    }

    /**
     * 支持简单的Conditions 赋值查询，复杂sql请自行处理再调用本方法
     * @author songlin
     * @param page
     * @param clas
     * @param sql
     * @param values
     * @return
     */
    public <X> Pages<X> findPageBySql(final Pages<X> page, final Class<X> clas, final String sql, final Object... values) {
        int classNameIndex = sql.toLowerCase().indexOf("from");
        if (classNameIndex == -1)
            return null;
        Assert.notNull(page, "page can not be null");
        /**
         * 动态拼接参数
         */
        List<Object> paramValue = ListUtil.arrayToList(values);
        String querySql = dualDynamicCondition(sql, page.getConditions(), paramValue);

        if (page.isAutoCount()) {
            long totalCount = countSQLResult(querySql, paramValue.toArray());
            page.setTotalCount(totalCount);
            if (totalCount == 0) {
                return page;
            }
        }
        querySql += appendOrderBy(querySql, page.getSort());
        querySql += " limit ?,?";
        paramValue.add(page.getFirst() - 1);
        paramValue.add(page.getPageSize());
        logger.debug(querySql);
        logger.debug("{}", paramValue);
        List<X> list = jdbcTemplate.query(querySql, BeanPropertyRowMapper.newInstance(clas), paramValue.toArray());
        page.setResult(list);
        return page;
    }

    /**
     * 处理动态参数
     */
    protected String dualDynamicCondition(String sql, List<Conditions> conditions, Object values) {
        Assert.notNull(values, "不能为空，即时没有值也必须构造一个List或Map");
        if (ListUtil.isEmpty(conditions)) {
            return sql;
        }
        String conditionSql = "";
        if (values instanceof List) {
            conditionSql = appendConditionParameterAndValue(conditions, (List) values);
        } else if (values instanceof Map) {
            conditionSql = appendConditionParameterAndValue(conditions, (Map) values);
        }
        if (StringUtil.isNotBlank(conditionSql)) {
            /**
             * 如果sql在xml中定义，则需要转换换行为空字符
             */
            sql = StringUtil.replaceChars(StringUtil.replaceChars(sql, "\t", " "), "\n", "");
            int whereIndexof = StringUtil.lastIndexOfIgnoreCase(sql, " where ");
            if (whereIndexof > 0) {
                conditionSql = " " + Conditions.AND + conditionSql;
            } else {
                conditionSql = " where " + conditionSql;
            }
            int groupByIndexof = StringUtil.lastIndexOfIgnoreCase(sql, "group by");
            int orderByIndexof = StringUtil.lastIndexOfIgnoreCase(sql, "order by");
            if (groupByIndexof > 0) {
                sql = StringUtil.insert(sql, conditionSql, groupByIndexof);
            } else if (orderByIndexof > 0) {
                sql = StringUtil.insert(sql, conditionSql, orderByIndexof);
            } else {
                sql += conditionSql;
            }
        }
        return sql;
    }

    /**
     * count record
     *
     * @author songlin.li
     * @param sql
     * @param values
     * @return
     */
    protected long countSQLResult(String sql, Object... values) {
        long recordTotal;
        int classNameIndex = sql.toLowerCase().indexOf("from");
        if (classNameIndex == -1)
            return 0;
        else {
            sql = "select count(1) as count from (" + sql + ") orgi";
        }
        recordTotal = jdbcTemplate.queryForObject(sql, Long.class, values);
        logger.debug("Total: " + recordTotal);
        return recordTotal;
    }

    /**
     * build the queryString to append condition
     *
     * @author songlin
     * @param queryString
     * @param conditions
     * @return
     */
    private String appendConditionParameterAndValue(List<Conditions> conditions, List<Object> values) {
        String appendCondition = " ";
        List<Object> appendValues = Lists.newLinkedList();
        if (ListUtil.isNotEmpty(conditions)) {
            List<String> queryParameter = Lists.newArrayList();
            for (Conditions condition : conditions) {
                switch (condition.getMatchType()) {
                    case LL:
                        queryParameter.add(condition.getProperty() + condition.getMatchType().getSymbol());
                        appendValues.add("%" + condition.getValue());
                        break;
                    case RL:
                        queryParameter.add(condition.getProperty() + condition.getMatchType().getSymbol());
                        appendValues.add(condition.getValue() + "%");
                        break;
                    case FL:
                        queryParameter.add(condition.getProperty() + condition.getMatchType().getSymbol());
                        appendValues.add("%" + condition.getValue() + "%");
                        break;
                    case BW:
                        queryParameter.add(condition.getProperty() + condition.getMatchType().getSymbol());
                        appendValues.add(condition.getValue());
                        appendValues.add(condition.getAnotherValue());
                        break;
                    case IN:
                    case NIN:
                        queryParameter.add(condition.getProperty() + condition.getMatchType().getSymbol(":" + condition.getProperty()));
                        break;
                    default:
                        queryParameter.add(condition.getProperty() + condition.getMatchType().getSymbol());
                        appendValues.add(condition.getValue());
                        break;
                }

            }
            /**
             * 需要判断是否需要添加and开头，此处先默认需要添加and开头即前面需要已有查询条件
             */
            appendCondition = " " + StringUtil.join(queryParameter, Conditions.AND) + " ";
            logger.debug("append conditions sql:" + appendCondition);

        }
        values.addAll(appendValues);
        return appendCondition;
    }

    private String appendConditionParameterAndValue(List<Conditions> conditions, Map<String, Object> values) {
        String appendCondition = " ";
        if (ListUtil.isNotEmpty(conditions)) {
            if (values == null) {
                values = Maps.newHashMap();
            }
            List<String> queryParameter = Lists.newArrayList();
            for (Conditions condition : conditions) {
                if (MatchType.LL.equals(condition.getMatchType())) {
                    values.put(condition.getProperty(), "%" + condition.getValue());
                } else if (MatchType.RL.equals(condition.getMatchType())) {
                    values.put(condition.getProperty(), condition.getValue() + "%");
                } else if (MatchType.FL.equals(condition.getMatchType())) {
                    values.put(condition.getProperty(), "%" + condition.getValue() + "%");
                } else if (MatchType.BW.equals(condition.getMatchType())) {
                    queryParameter.add(condition.getProperty()
                            + condition.getMatchType().getSymbol(":" + condition.getProperty(), ":" + condition.getProperty() + "2"));
                    values.put(condition.getProperty(), condition.getValue());
                    values.put(condition.getProperty() + "2", condition.getAnotherValue());
                    continue;
                } else {
                    values.put(condition.getProperty(), condition.getValue());
                }
                queryParameter.add(condition.getProperty() + condition.getMatchType().getSymbol(":" + condition.getProperty()));
            }
            /**
             * 需要判断是否需要添加and开头，此处先默认需要添加and开头即前面需要已有查询条件
             */
            appendCondition = " " + StringUtil.join(queryParameter, Conditions.AND) + " ";
            logger.debug("append conditions sql:" + appendCondition);
        }
        return appendCondition;
    }

    /**
     * build the queryString to append the sort order by
     *
     * @author songlin
     * @param queryString
     * @param sort
     * @return
     */
    protected String appendOrderBy(String queryString, Sort sort) {
        String orderBy = "";
        if (sort != null) {
            Assert.doesNotContain(queryString, "order by",
                    "duplicate order by,hql already has the sort: " + StringUtil.substringAfter(queryString, "order by"));
            orderBy = " order by " + sort.toString();
        }
        return orderBy;
    }

}
