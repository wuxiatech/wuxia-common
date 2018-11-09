package cn.wuxia.common.orm.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.wuxia.common.util.DaoUtil;
import cn.wuxia.common.util.StringUtil;

/**
 * <h3>Class name</h3> <h4>Description</h4> <h4>Special Notes</h4>
 * 
 * @version 1.3
 * @author songlin.li 2012-05-30
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class QueryUtil implements Serializable {
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 490860576994439459L;

    private static final Logger logger = LoggerFactory.getLogger(QueryUtil.class);

    private List<Conditions> conditions;

    private Pages pages;

    private List<Object> queryValues;

    private String queryKey;

    private String queryType;

    private String queryString;

    private String keyModel;

    private final static String BLANK = "";

    /**
     * ? parametric model
     */
    public final static String KEY_MODEL_QUESTION_MARK = "?";

    /**
     * : parametric model
     */
    public final static String KEY_MODEL_COLON = ":";

    /**
     * no parametric model
     */
    public final static String KEY_MODEL_NONE = "no_key";

    public QueryUtil() {
        conditions = new ArrayList<Conditions>();
        queryType = "hql";
        keyModel = KEY_MODEL_QUESTION_MARK;
        queryValues = new ArrayList<Object>();
    }

    public QueryUtil(Pages pages) {
        this();
        this.pages = pages;
    }

    public void clear() {
        conditions.clear();
    }

    private void addBean(Conditions bean) {
        conditions.add(bean);

    }

    /**
     * @description : Quick Add full bean
     * @param property
     * @param value
     * @param anotherValue
     * @param startStr
     * @param endStr
     * @return
     */
    public QueryUtil addBetween(String property, Object value, Object anotherValue) {
        Conditions bean = new Conditions(property, value);
        bean.setMatchType(MatchType.BW);
        bean.setAnotherValue(anotherValue);
        addBean(bean);
        return this;
    }

    /**
     * @description : Quick Add simple bean
     * @param property
     * @param value
     * @return
     */
    public QueryUtil add(String property, Object value) {
        Conditions bean = new Conditions(property, value);        
        addBean(bean);
        return this;
    }

   

    public void addQueryValue(Object value) {
        queryValues.add(value);
    }

    /**
     * @description : Separation dynamic query conditions
     * @param queryString
     * @param condictionsMap
     * @return
     */
    private String analyseQueryString(String queryString, Map condictionsMap) {
        queryString = queryString.replace("\n", " ");
        String regex = "\\$\\[(.+?)\\]";
        Matcher m = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(queryString);
        int keyId = 0;

        while (m.find()) {
            try {
                String key0 = m.group(0);
                String key1 = m.group(1);
                String mapKey = "$[" + key1 + "#" + keyId + "]";

                condictionsMap.put(mapKey, key1);
                queryString = StringUtil.replaceOnce(queryString, key0, mapKey);
                keyId++;
            }

            catch (Exception e) {
                continue;
            }
        }

        return queryString;
    }

    /**
     * @description : main method to combine sql and value
     * @author songlin.li
     * @return
     */
    public String combineQueryString() {
        DaoUtil daoUtil = new DaoUtil();
        if (StringUtil.isNotBlank(queryKey)) {
            daoUtil.setSqlKey(queryKey);
            if (queryKey.toLowerCase().lastIndexOf("_sql") > -1) {
                setQueryType("sql");
            }
            queryString = "";
        }
        for (Conditions condition : conditions) {
            daoUtil.put(condition.getProperty(), condition.getValue());
        }

        queryString = daoUtil.getParmeterSql(queryString);
        return queryString;
    }

    /**
     * @return the conditions
     */
    public List getConditions() {
        return conditions;
    }

    public String getQueryKey() {
        return queryKey;
    }

    public void setQueryKey(String queryKey) {
        this.queryKey = queryKey;
    }

    public String getQueryType() {
        return queryType;
    }

    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public List<Object> getQueryValues() {
        return queryValues;
    }

    public void setQueryValues(List<Object> queryValues) {
        this.queryValues = queryValues;
    }

    /**
     * @return
     */
    public String getKeyModel() {
        return keyModel;
    }

    /**
     * @param String
     */
    public void setKeyModel(String keyModel) {
        this.keyModel = keyModel;
    }

    /**
     * @return
     */
    public Pages getPages() {
        return pages;
    }

    /**
     * @param Pages
     */
    public void setPages(Pages pages) {
        this.pages = pages;
    }

}
