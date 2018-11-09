/*
* Created on :15 Sep, 2014
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 武侠科技 All right reserved.
*/
package cn.wuxia.common.orm.sql;

import java.util.Hashtable;
import java.util.Iterator;

public class ParameterList {
    private Hashtable<String, Parameter> table = new Hashtable<>();

    public ParameterList() {
    }

    public void add(Parameter parm) {
        this.table.put(parm.getName(), parm);
    }

    public Parameter get(String name) {
        return (Parameter) this.table.get(name);
    }

    public void remove(String name) {
        this.table.remove(name);
    }

    public void clear() {
        this.table.clear();
    }

    public String[] getParNamse() {
        String[] names = new String[this.table.size()];
        Iterator<String> it = this.table.keySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            names[(i++)] = it.next();
        }
        return names;
    }

    public int size() {
        return this.table.size();
    }
}
