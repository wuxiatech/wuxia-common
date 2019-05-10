package cn.wuxia.common.orm;

import cn.wuxia.common.orm.query.Conditions;
import cn.wuxia.common.orm.query.MatchType;
import cn.wuxia.common.util.ListUtil;
import cn.wuxia.common.util.StringUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Sqls;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

@Slf4j
public class PageSQLHandler {

    /**
     * 处理动态参数
     */
    public static String dualDynamicCondition(String sql, List<Conditions> conditions, Object values) {
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
            if (groupByIndexof > 0 && whereIndexof < groupByIndexof) {
                sql = StringUtil.insert(sql, conditionSql, groupByIndexof);
            } else if (orderByIndexof > 0 && orderByIndexof > whereIndexof) {
                sql = StringUtil.insert(sql, conditionSql, orderByIndexof);
            } else {
                sql += conditionSql;
            }
        }
        return sql;
    }
    /**
     * build the queryString to append condition
     *
     * @param conditions
     * @param values
     * @return
     * @author songlin
     */
    private static String appendConditionParameterAndValue(List<Conditions> conditions, List<Object> values) {
        String appendCondition = " ";
        List<Object> appendValues = Lists.newLinkedList();
        if (ListUtil.isNotEmpty(conditions)) {
            List<String> queryParameter = Lists.newArrayList();
            for (Conditions condition : conditions) {
                switch (condition.getMatchType()) {
                    case LL:
                        if (StringUtil.isNotBlank(condition.getValue())) {
                            queryParameter.add(condition.getProperty() + condition.getMatchType().getSymbol());
                            appendValues.add(condition.getValue() + "%");
                        }
                        break;
                    case RL:
                        if (StringUtil.isNotBlank(condition.getValue())) {
                            queryParameter.add(condition.getProperty() + condition.getMatchType().getSymbol());
                            appendValues.add("%" + condition.getValue());
                        }
                        break;
                    case FL:
                        if (StringUtil.isNotBlank(condition.getValue())) {
                            queryParameter.add(condition.getProperty() + condition.getMatchType().getSymbol());
                            appendValues.add("%" + condition.getValue() + "%");
                        }
                        break;
                    case BW:
                        queryParameter.add(condition.getProperty() + condition.getMatchType().getSymbol());
                        appendValues.add(condition.getValue());
                        appendValues.add(condition.getAnotherValue());
                        break;
                    case IN:
                    case NIN:
                        List v1 = Lists.newArrayList();
                        Object v = condition.getValue();
                        if (v instanceof List) {
                            for (Object val : (List) v) {
                                v1.add(Sqls.formatSqlFieldValue(val));
                            }

                        } else if (v instanceof Object[]) {
                            for (Object val : (Object[]) v) {
                                v1.add(Sqls.formatSqlFieldValue(val));
                            }
                        }
                        if (ListUtil.isNotEmpty(v1)) {
                            queryParameter.add(condition.getProperty() + condition.getMatchType().getSymbol(StringUtil.join(v1, ",")));
                        }
                        break;
                    default:
                        if (StringUtil.isNotBlank(condition.getValue())) {
                            queryParameter.add(condition.getProperty() + condition.getMatchType().getSymbol());
                            appendValues.add(condition.getValue());
                        }
                        break;
                }

            }
            if (ListUtil.isEmpty(queryParameter)) {
                return "";
            }
            /**
             * 需要判断是否需要添加and开头，此处先默认需要添加and开头即前面需要已有查询条件
             */

            appendCondition = " " + StringUtil.join(queryParameter, Conditions.AND) + " ";
            log.debug("append conditions sql:" + appendCondition);

        }
        values.addAll(appendValues);
        return appendCondition;
    }

    private static String appendConditionParameterAndValue(List<Conditions> conditions, Map<String, Object> values) {
        String appendCondition = " ";
        if (ListUtil.isNotEmpty(conditions)) {
            if (values == null) {
                values = Maps.newHashMap();
            }
            List<String> queryParameter = Lists.newArrayList();
            for (Conditions condition : conditions) {
                if (MatchType.LL.equals(condition.getMatchType())) {
                    if (StringUtil.isNotBlank(condition.getValue())) {
                        values.put(condition.getProperty(), "%" + condition.getValue());
                        queryParameter.add(condition.getProperty() + condition.getMatchType().getSymbol(":" + condition.getProperty()));
                    }
                } else if (MatchType.RL.equals(condition.getMatchType())) {
                    if (StringUtil.isNotBlank(condition.getValue())) {
                        values.put(condition.getProperty(), condition.getValue() + "%");
                        queryParameter.add(condition.getProperty() + condition.getMatchType().getSymbol(":" + condition.getProperty()));
                    }
                } else if (MatchType.FL.equals(condition.getMatchType())) {
                    if (StringUtil.isNotBlank(condition.getValue())) {
                        values.put(condition.getProperty(), "%" + condition.getValue() + "%");
                        queryParameter.add(condition.getProperty() + condition.getMatchType().getSymbol(":" + condition.getProperty()));
                    }
                } else if (MatchType.BW.equals(condition.getMatchType())) {
                    queryParameter.add(condition.getProperty()
                            + condition.getMatchType().getSymbol(":" + condition.getProperty(), ":" + condition.getProperty() + "2"));
                    values.put(condition.getProperty(), condition.getValue());
                    values.put(condition.getProperty() + "2", condition.getAnotherValue());
                    queryParameter.add(condition.getProperty() + condition.getMatchType().getSymbol(":" + condition.getProperty()));
                    continue;
                } else if (MatchType.ISN.equals(condition.getMatchType())) {
                    if (StringUtil.isNotBlank(condition.getValue())) {
                        queryParameter.add(condition.getProperty() + condition.getMatchType().getSymbol());
                    }
                } else {
                    if (StringUtil.isNotBlank(condition.getValue())) {
                        values.put(condition.getProperty(), condition.getValue());
                        queryParameter.add(condition.getProperty() + condition.getMatchType().getSymbol(":" + condition.getProperty()));
                    }
                }

            }
            if (ListUtil.isEmpty(queryParameter)) {
                return "";
            }
            /**
             * 需要判断是否需要添加and开头，此处先默认需要添加and开头即前面需要已有查询条件
             */
            appendCondition = " " + StringUtil.join(queryParameter, Conditions.AND) + " ";
            log.debug("append conditions sql:" + appendCondition);
        }
        return appendCondition;
    }
}
