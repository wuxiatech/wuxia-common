package cn.wuxia.common.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.nutz.dao.Sqls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

import cn.wuxia.common.spring.SpringContextHolder;
import cn.wuxia.common.sql.SqlParser;

/**
 * Dao Tools
 * 
 * @author songlin.li
 */
@Scope("prototype")
public class DaoUtil {
    protected static final Logger log = LoggerFactory.getLogger(DaoUtil.class);

    /**
     * dynamic parameter
     */
    private Hashtable<String, Object> paramValues;

    /**
     * Placeholder variables
     */
    private Map<String, String> vars;

    private SqlParser sqlParser;

    private String sqlKey;

    public DaoUtil() {
        paramValues = new Hashtable<>();
        sqlParser = new SqlParser();
        vars = new HashMap<String, String>();
    }

    /**
     * @description : The beginning of query package automatically to the
     *              request to Q_
     * @param request
     */
    public void initRequest(HttpServletRequest request) {
        Map<String, String[]> map = request.getParameterMap();
        String q = "Q_";
        // Start with like need to increase %
        String b = "b_";
        // End with like need to increase %
        String e = "e_";

        for (String key : map.keySet()) {
            int p = key.indexOf(q);

            if (p == 0) {
                String[] value = map.get(key);
                if (ArrayUtils.isEmpty(value)) {
                    continue;
                }

                if (key.contains(q + b + e)) {
                    key = key.substring(6);

                    if (paramValues.get(key) == null) {
                        put(key, value[0], "%", "%");
                    }
                } else if (key.contains(q + b)) {
                    key = key.substring(4);

                    if (paramValues.get(key) == null) {
                        put(key, value[0], "%", null);
                    }
                } else if (key.contains(q + e)) {
                    key = key.substring(4);

                    if (paramValues.get(key) == null) {
                        put(key, value[0], null, "%");
                    }
                } else {
                    key = key.substring(2);

                    // When manually put the key from the incoming parameters
                    // and code of web pages the same priority to take the key
                    // code
                    if (paramValues.get(key) == null) {
                        put(key, value[0]);
                    }
                }
            }
        }
    }

    /**
     * @description :put value in :key from parameter ,support {String,
     *              String[], Integer[]}
     * @param key
     * @param value
     */
    public void put(String key, Object value) {
        if (value != null) {
            // Dealing with a placeholder
            if (key.indexOf("$") == 0) {
                vars.put(key, value.toString());
            } else {
                // sql filter , to prevent sql injection
                if (value instanceof String) {
                    if (StringUtils.isNotBlank((String) value)) {
                        String values = Sqls.formatSqlFieldValue(value).toString();
                        values = values.replace("$$", "\\$").replace("@@", "\\@");
                        paramValues.put(key, values);
                    }
                } else if (value instanceof String[]) {
                    StringBuffer v1 = new StringBuffer();
                    for (String v : (String[]) value) {
                        v1.append(Sqls.formatSqlFieldValue(v) + ",");
                    }
                    if (StringUtils.isNotBlank(v1.toString())) {
                        paramValues.put(key, v1.toString().substring(0, v1.toString().length() - 1));
                    }
                } else if (value instanceof Number[]) {
                    StringBuffer v1 = new StringBuffer();
                    for (Number v : (Number[]) value) {
                        v1.append(Sqls.formatSqlFieldValue(v) + ",");
                    }
                    if (StringUtils.isNotBlank(v1.toString())) {
                        paramValues.put(key, v1.toString().substring(0, v1.toString().length() - 1));
                    }
                } else if (value instanceof Collection) {
                    Collection list = (Collection) value;
                    StringBuffer v = new StringBuffer();
                    for (Iterator iterator = list.iterator(); iterator.hasNext();) {
                        Object object = (Object) iterator.next();
                        v.append(Sqls.formatSqlFieldValue(object) + ",");
                    }
                    if (ListUtil.isNotEmpty(list)) {
                        paramValues.put(key, v.toString().substring(0, v.toString().length() - 1));
                    }
                } else {
                    paramValues.put(key, Sqls.formatSqlFieldValue(value).toString());
                }
            }
        }
    }

    /**
     * @description : parameter in sql use :key
     * @param key
     * @param value
     * @param b
     * @param e
     */
    public void put(String key, String value, String b, String e) {
        if (StringUtils.isNotBlank(value)) {
            if (b == null) {
                b = "";
            }

            if (e == null) {
                e = "";
            }

            paramValues.put(key, Sqls.formatSqlFieldValue(b + value + e).toString());
        }
    }

    /**
     * @description :get result sql
     * @param sql
     * @return
     */
    public String getParmeterSql(String sql) {
        if (StringUtil.isBlank(sql)) {
            return getParmeterSqlByKey(null);
        }
        try {
            /* if you want to use, must remove the // */
            sql = sqlParser.matching(sql, paramValues);
            // deal $key placeholder
            for (String var : vars.keySet()) {
                sql = sql.replaceAll("\\" + var, vars.get(var));
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        // 在mysql中格式化后会增加空格导致查询出错
        sql = StringUtil.replace(sql, " (", "(");
        sql = StringUtil.replace(sql, " (", "(");
        log.debug(sql);
        return sql;
    }

    /**
     * @description :sqlKey must not null
     * @return
     */
    public String getParmeterSql() {
        return getParmeterSqlByKey(null);
    }

    /**
     * @description :sqlKey must not null
     * @param sqlKey
     * @return
     */
    public String getParmeterSqlByKey(String sqlKey) {
        String sql = "";
        if (StringUtil.isNotBlank(sqlKey)) {
            sql = getQueryString(sqlKey);
        } else if (StringUtil.isNotBlank(this.sqlKey)) {
            sql = getQueryString(this.sqlKey);
        } else {
            return sql;
        }
        return getParmeterSql(sql);
    }

    /**
     * @description : empty condition
     */
    public void clear() {
        vars.clear();
        paramValues.clear();
    }

    /**
     * @description : replace special char in sql for SQL Server
     * @param queryValue
     * @return
     */
    public static String sqlEncodeForSQLServer(String queryValue) {
        return queryValue.replace("[", "[[]").replace("_", "[_]").replace("^", "[^]");
    }

    /**
     * @description : get Query String for query.xml
     * @param queryKey
     * @return
     */
    public String getQueryString(String queryKey) {
        return getQueryString("query", queryKey);
    }

    public String getQueryString(String beanId, String queryKey) {
        if (StringUtils.isBlank(queryKey))
            return "";
        Map<String, Object> queryMap = (Map<String, Object>) SpringContextHolder.getBean(beanId);
        if (queryMap != null) {
            Object obj = queryMap.get(queryKey);
            if (obj instanceof String) {
                return obj.toString();
            }
        }
        this.sqlKey = queryKey;
        return "";
    }

    /**
     * @description : get parameter，start with $ is placeholder parameter, other
     *              are dynamic parameter
     * @param key
     * @return
     */
    public Object getVars(String key) {
        if (key.indexOf("$") == 0) {
            return vars.get(key);
        }

        return paramValues.get(key);
    }

    public String getSqlKey() {
        return sqlKey;
    }

    public void setSqlKey(String sqlKey) {
        this.sqlKey = sqlKey;
    }

}
