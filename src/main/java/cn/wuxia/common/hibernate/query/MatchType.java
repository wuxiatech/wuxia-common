/*
 * Created on :21 Dec, 2013 Author :songlin Change History Version Date Author
 * Reason <Ver.No> <date> <who modify> <reason>
 */
package cn.wuxia.common.hibernate.query;

import java.util.EnumSet;

import org.apache.commons.lang3.StringUtils;
import org.nutz.dao.Sqls;
import org.apache.commons.lang3.ArrayUtils;

/** @description : Type attribute comparison. */
public enum MatchType {
    /**
     * equals
     */
    EQ(" = %s "),
    /**
     * not equals
     */
    NE(" != %s "),
    /**
     * is null
     */
    ISN(" is null "),
    /**
     * is not null
     */
    INN(" is not null"),
    /**
     * left like
     */
    LL(" like %s "),
    /**
     * right like
     */
    RL(" like %s "),
    /**
     * full like
     */
    FL(" like %s "),
    /**
     * not like
     */
    NL(" not like %s "),
    /**
     * <
     */
    LT(" < %s "),
    /**
     * >
     */
    GT(" > %s "),
    /**
     * >=
     */
    GTE(" >= %s "),
    /**
     * <=
     */
    LTE(" <= %s "),
    /**
     * in
     */
    IN(" in (%s)"),
    
    /**
     * not in
     * 
     */
    NIN(" not in (%s)"),
    /**
     * between
     */
    BW(" between %s and %s ");

    String symbol;

    private MatchType(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        if (BW.equals(this)) {
            return getSymbol("?", "?");
        }
        return getSymbol("?");

    }

    public String getSymbol(Object... parameterType) {
        return String.format(symbol, parameterType);
    }

    public String getFormatValue(Object... object) {
        if (ArrayUtils.isEmpty(object)) {
            return getSymbol();
        }
        Object[] newValues = new Object[object.length];

        for (int i = 0; i < object.length; i++) {
            Object v = object[i];
            switch (this) {
                case NL:
                case FL:
                    v = "%" + Sqls.escapeSqlFieldValue(v.toString()) + "%";
                    break;
                case LL:
                    v = "%" + Sqls.escapeSqlFieldValue(v.toString());
                    break;
                case RL:
                    v = Sqls.escapeSqlFieldValue(v.toString()) + "%";
                    break;
                default:
                    break;
            }
            newValues[i] = Sqls.formatSqlFieldValue(v);
        }
        return String.format(symbol, newValues);
    }


    /**
     * object compare the name
     * 
     * @author string
     * @param object
     * @return
     */
    public final boolean compare(String string) {
        return StringUtils.equalsIgnoreCase(this.name(), string);
    }
}
