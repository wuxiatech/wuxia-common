/*
* Created on :15 Sep, 2014
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 武侠科技 All right reserved.
*/
package cn.wuxia.common.sql;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class SqlParser {
    private static final Logger log = LoggerFactory.getLogger(SqlParser.class);

    public static String LOGIC_SEPARATOR = ",()! ";

    public static String EXPRESION_OPERATOR = "()+-*/%!><";

    public static String WORD_OPERATOR = "=()+-*/%!>< ";

    public static String KEY_WHERE = "where";

    public static String[] KEY_WORK = { "union", ",", "and", "or", "||", "!", "limit", "order", "group", "having", "jion" };

    private List<String> useParams;

    public SqlParser() {
    }

    public List getUseParams() {
        return this.useParams;
    }

    public void setUseParams(List useParams) {
        this.useParams = useParams;
    }

    private List<String> splitWord(String sql, String sparator) {
        List<String> list = new ArrayList<>();
        sql = sql.trim();
        String word = "";
        char tmp = ' ';
        int size = sql.length();
        for (int i = 0; i < size; i++) {
            tmp = sql.charAt(i);

            if (tmp == ' ') {
                if (word.trim().equals("")) {
                    word = word + tmp;
                } else {
                    list.add(new String(word.trim()));
                    word = "";
                }
            } else if (sparator.indexOf(tmp) >= 0) {
                if (!word.trim().equals("")) {
                    list.add(word.trim());
                }
                list.add(String.valueOf(tmp));
                word = "";
            } else {
                word = word + tmp;
            }
        }

        if (!word.trim().equals(""))
            list.add(word);
        return list;
    }

    public List<String> getParameters(String sql) {
        String sp = "() *+-%/><=";

        List<String> list = splitWord(sql, sp);
        String tmp = "";
        for (int i = list.size() - 1; i >= 0; i--) {
            tmp = list.get(i);
            if ((tmp.trim().charAt(0) != ':') && (StringUtils.indexOf(tmp, "':") < 0)) {
                list.remove(tmp);
            }
        }

        return list;
    }

    private boolean parmIsExist(String name, List parmList) {
        for (int i = 0; i < parmList.size(); i++) {
            String tmp = (String) parmList.get(i);
            if (tmp.equals(name))
                return true;
        }
        return false;
    }

    public String matching(String sql, Hashtable<String, Object> parmTable) throws Exception {
        sql = adjustQL(sql);

        ParameterList parmList = new ParameterList();

        Iterator<String> it = parmTable.keySet().iterator();

        while (it.hasNext()) {
            String name = it.next();
            Object value = parmTable.get(name);
            Parameter p = new Parameter(name, "", value, null, "");
            parmList.add(p);
        }

        return matching(sql, parmList);
    }

    public String matching(String sql, ParameterList list) throws Exception {
        sql = adjustQL(sql);

        if (list == null) {
            list = new ParameterList();
        }
        String[] pNames = list.getParNamse();
        String[] names = new String[pNames.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = pNames[i].trim();
        }

        String tmp = recombineParam(sql, names);

        List<String> tmpList = splitWord(tmp, WORD_OPERATOR);

        for (int i = 0; i < names.length; i++) {
            String value = list.get(pNames[i]).getValue().toString();

            tmp = tmp.replaceAll(":" + names[i], value);
        }

        try {
            if (log.isDebugEnabled()) {
                for (int i = 0; i < names.length; i++) {
                    log.debug("参数: " + names[i] + "=" + list.get(names[i]).getValue() + "             ");
                }
                log.debug("结果HQL : " + tmp);
            }
        } catch (Exception localException) {
        }
        return tmp;
    }

    private String adjustQL(String ql) {
        return StringUtils.replace(ql, "\n", " \n");
    }

    private String recombineParam(String sql, String[] params) {
        List paramList = getNoParameterVar(sql, params);
        log.debug("no use param " + paramList);

        List list = splitWord(sql, LOGIC_SEPARATOR);

        WordTreeNode tree = new WordTreeNode();

        tree.createTree(list);
        try {
            tree.prnintTree("    ", System.out);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        for (int i = 0; i < paramList.size(); i++) {

            tree.cancelParameter((String) paramList.get(i), KEY_WORK);
        }

        if (tree.getWordlist().size() > 0) {
            Object obj = tree.getWordlist().get(tree.getWordlist().size() - 1);
            if ((obj instanceof String)) {
                String word = (String) obj;
                if (word.trim().toLowerCase().equals("where")) {
                    tree.getWordlist().remove(tree.getWordlist().size() - 1);
                }
            }
        }

        if (tree.getWordlist().size() > 4) {
            for (int i = 0; i < tree.getWordlist().size() - 1; i++) {
                Object obj = tree.getWordlist().get(i);
                if ((obj instanceof String)) {
                    String wh = (String) obj;
                    if (wh.equals("where")) {
                        Object tmp = tree.getWordlist().get(i + 1);
                        if ((tmp instanceof String)) {
                            String lm = ((String) tree.getWordlist().get(i + 1)).trim().toLowerCase();
                            if (lm.equals("limit")) {
                                tree.getWordlist().remove(i);
                            }
                        }
                    }
                }
            }
        }

        return tree.createSqlString();
    }

    private List<String> getNoParameterVar(String sql, String[] params) {
        List<String> paramList = getParameters(sql);
        List<String> noParameterVar = getParameters(sql);
        this.useParams = getParameters(sql);
        log.debug("all parameter " + paramList);
        String sqlParm = "";
        String srcParm = "";
        String srcSubParm = "";

        for (int i = 0; i < params.length; i++) {
            for (int j = 0; j < paramList.size(); j++) {
                String tmp = (String) paramList.get(j);
                sqlParm = tmp.trim();

                srcParm = ":" + params[i].trim();
                srcSubParm = "'" + srcParm + "'";

                if ((sqlParm.equals(srcParm)) || (sqlParm.equals(srcSubParm))) {

                    noParameterVar.remove(tmp);
                }
            }
        }

        this.useParams.removeAll(noParameterVar);
        return noParameterVar;
    }

    private String replayParamValue(String sql, ParameterList pList) {
        List list = splitWord(sql, WORD_OPERATOR);
        WordTreeNode tree = new WordTreeNode();
        for (int i = 0; i < pList.size(); i++) {
            String name = pList.getParNamse()[i];
            String value = pList.get(name).getValue().toString();
            for (int j = 0; j < list.size(); j++) {
                Object obj = list.get(j);
                if ((obj instanceof String)) {
                    String tmp = obj.toString().trim();
                    if ((tmp.equals(name)) || (tmp.equals("'" + name + "'"))) {
                        tmp = tmp.replaceAll(name, value);
                        list.set(j, tmp);
                    }
                }
            }
        }

        tree.createTree(list);

        return "";
    }

    private void testCancelParameter(String sql, String paramName) throws Exception {
        String[] keyWords = { "where", "and", "or", "||", "!", "limit" };

        WordTreeNode tree = new WordTreeNode();
        List list = splitWord(sql, LOGIC_SEPARATOR);
        tree.createTree(list);

        tree.cancelParameter(paramName, keyWords);

        log.debug(tree.createSqlString());
    }

    public void testCreateTree(String sql) throws Exception {
        WordTreeNode treeNode = new WordTreeNode();
        List list = splitWord(sql, LOGIC_SEPARATOR);
        treeNode.createTree(list);

        treeNode.prnintTree("  ", System.out);
    }

    private void testSplitWord(String sql) {
        List list = splitWord(sql, LOGIC_SEPARATOR);
        int count = list.size();
        for (int i = 0; i < count; i++) {
            System.out.print(list.get(i) + " ");
        }
    }

    private void testRecombinParameter(String sql, String[] params) {
        String rbSql = recombineParam(sql, params);

        System.out.println("重新组合后的SQL是：\n" + rbSql);
    }

    private void testGetParameters(String sql) {
        List list = getParameters(sql);
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
        }
    }

    public String[] getLimitParameter(String sql) throws Exception {
        String[] rs = new String[3];

        String parms = StringUtils.substringAfter(sql, "limit");
        String tmpSql = StringUtils.substringBefore(sql, "limit");
        if ((parms == null) || (parms.equals(""))) {
            return null;
        }
        String[] limitParms = StringUtils.split(parms, ",");
        if (limitParms.length != 2) {
            String msg = "QL语法错误！ \t 错误: limit参数错误 \t 提示：QL:" + sql + " \t";
            throw new Exception(msg);
        }
        rs[0] = tmpSql;
        rs[1] = limitParms[0].trim();
        rs[2] = limitParms[1].trim();

        return rs;
    }

    public void test_getParameter() {
        String sql = "  dfsf  where a=a: and b= :b ";
        List list = splitWord(sql, LOGIC_SEPARATOR);
    }

    public static void main(String[] args) throws Exception {
        String tsql = ":kk select * from tbuser where tt=:kk  and :kk>0";
        int i = StringUtils.indexOf(tsql, ":kk", 100);
        System.out.println("index=" + i);
        String tt = StringUtils.replace(tsql, ":kk", "888", 1);
        System.out.println(tt);

        SqlParser p = new SqlParser();
        String sql = " select district 区,house_name 楼盘 , price 价格,addr_amount 地址数  from tb_house_info where  deptcode like '4412%' and (1=1 and  house_name like ':house_name'+'%' and price>=:price) union all select '合计' ,'',null,sum(addr_amount)  from tb_house_info where deptcode like '4412%' and  (1=1 and house_name like ':house_name'+'%' and price>=:price) ";
        Hashtable table = new Hashtable();
        table.put("price", "12");

        table.put("pk", "88");
        String psql = p.matching(sql, table);
        System.out.println("==================");
        System.out.println(psql);
    }
}
