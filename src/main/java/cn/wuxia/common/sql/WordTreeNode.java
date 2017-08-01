/*
* Created on :15 Sep, 2014
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 武侠科技 All right reserved.
*/
package cn.wuxia.common.sql;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class WordTreeNode {
    private List wordlist = new ArrayList();

    WordTreeNode parent;

    WordTreeNode() {
    }

    public boolean cancelParameter(String paraName, String[] keyWords) {
        if (this.wordlist.size() <= 0) {
            return true;
        }

        Object pnt = this.wordlist.get(this.wordlist.size() - 1);

        for (int i = this.wordlist.size() - 1; (i >= 0) && (i < this.wordlist.size()); i--) {

            Object tmp = this.wordlist.get(i);
            if ((tmp instanceof WordTreeNode)) {
                if ((!((WordTreeNode) tmp).cancelParameter(paraName, keyWords)) || (((WordTreeNode) tmp).getWordlist().size() == 0)) {
                    if (!delParameter(paraName, tmp, keyWords)) {
                        return false;
                    }
                    i = this.wordlist.size();
                }
            }

            if (containParameter(tmp.toString().trim(), paraName.trim())) {

                if (!delParameter(paraName, tmp, keyWords)) {
                    return false;
                }
                i = this.wordlist.size();
            }
        }

        return true;
    }

    private boolean containParameter(String exp, String param) {
        if (param.length() > exp.length()) {
            return false;
        }
        int pos = exp.indexOf(param) + param.length();
        if (pos >= exp.length()) {
            return true;
        }
        if (("'" + param + "'").equals(exp)) {
            return true;
        }
        if ((param + "%'").equals(exp)) {
            return true;
        }
        if ((pos >= 0) && (SqlParser.EXPRESION_OPERATOR.indexOf(exp.charAt(pos)) >= 0) && (exp.charAt(pos) != '%')) {
            return true;
        }
        return false;
    }

    private boolean delParameter(String paramName, Object node, String[] keyWords) {
        int pos = this.wordlist.indexOf(node);

        boolean start_flag = false;
        boolean end_flag = false;
        boolean where_flag = false;
        int start_pos = -1;
        int end_pos = -1;
        int where_pos = -1;

        if (pos > 0) {
            for (int i = pos - 1; i >= 0; i--) {
                Object tmp = this.wordlist.get(i);
                if (tmp.toString().toLowerCase().trim().equals(SqlParser.KEY_WHERE)) {
                    where_flag = true;
                    where_pos = i;
                    break;
                }
                if (isKey(tmp, keyWords)) {
                    start_flag = true;
                    start_pos = i;
                    break;
                }
            }
        }

        for (int i = pos + 1; i < this.wordlist.size(); i++) {
            Object tmp = this.wordlist.get(i);
            if (isKey(tmp, keyWords)) {
                end_flag = true;
                end_pos = i;
                break;
            }
        }

        if (where_flag) {
            if (!end_flag) {
                start_pos = where_pos;
                end_pos = this.wordlist.size() - 1;
            } else {
                start_pos = where_pos + 1;
            }
        } else if (start_flag) {
            if (!end_flag) {
                end_pos = this.wordlist.size() - 1;

            } else {
                end_pos--;
            }
        } else {
            start_pos = 0;
            if (!end_flag) {
                end_pos = this.wordlist.size() - 1;
            }
        }

        removeNode(start_pos, end_pos);

        if ((start_pos > -1) && (end_pos > -1))
            return true;
        return false;
    }

    private void removeNode(int first, int last) {
        String word = this.wordlist.get(last).toString();
        if (word.equals("limit")) {
            last--;
        }

        for (int i = last; i >= first; i--) {
            this.wordlist.remove(i);
        }
    }

    private boolean isKey(Object word, String[] keyWords) {
        for (int i = 0; i < keyWords.length; i++) {
            if (keyWords[i].trim().toLowerCase().equals(word.toString().trim().toLowerCase()))
                return true;
        }
        return false;
    }

    public boolean createTree(List list) {
        if (list.size() < 0)
            return false;
        do {
            String word = popWord(list);
            if (word == null)
                return false;
            if (word.equals(")"))
                return false;
            if (word.equals("(")) {
                WordTreeNode node = new WordTreeNode();
                node.createTree(list);
                if (node.getWordlist().size() > 0) {
                    setParent(this);
                    this.wordlist.add(node);
                }
            } else {
                this.wordlist.add(word);
            }
        } while (list.size() > 0);
        return true;
    }

    String createSqlString() {
        StringBuffer sql = new StringBuffer();

        for (int i = 0; i < this.wordlist.size(); i++) {
            Object tmp = this.wordlist.get(i);
            if ((tmp instanceof WordTreeNode)) {
                sql.append(" (");
                sql.append(((WordTreeNode) tmp).createSqlString());
                sql.append(" )");
            } else {
                sql.append(" " + tmp);
            }
        }
        return sql.toString();
    }

    public String prnintTree(String tap, OutputStream out) throws Exception {
        tap = tap + "    ";
        for (int i = 0; i < this.wordlist.size(); i++) {
            Object tmp = this.wordlist.get(i);
            if ((tmp instanceof WordTreeNode)) {
                WordTreeNode tree = (WordTreeNode) tmp;
                String t = tap + tap;
                tree.prnintTree(t, out);
            }
        }

        return tap.substring(0, tap.length() - 4);
    }

    private String popWord(List list) {
        if (list.size() <= 0)
            return null;
        String tmp = (String) list.get(0);
        list.remove(tmp);
        return tmp;
    }

    public List getWordlist() {
        return this.wordlist;
    }

    public void setWordlist(List wordlist) {
        this.wordlist = wordlist;
    }

    public WordTreeNode getParent() {
        return this.parent;
    }

    public void setParent(WordTreeNode parent) {
        this.parent = parent;
    }
}
