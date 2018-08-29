package cn.wuxia.common.express;

import cn.wuxia.common.util.ListUtil;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import com.ql.util.express.IExpressContext;
import org.nutz.el.El;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QlExpressUtil {


    /**
     * @param str
     * @return
     * @description : 返回?{} 内的key，key允许的字符包括.#:[]*\/+-()
     * @author songlin.li
     */
    public static String[] getTemplateKey(String str) {
        Pattern p = Pattern.compile("<el>.*?</el>");
        Matcher m = p.matcher(str);

        List<String> value = new ArrayList<String>();
        while (m.find()) {
            String key = m.group();
            key = key.substring(4, key.length() - 5);
            value.add(key);
        }
        if (ListUtil.isEmpty(value)) {
            return new String[] {};
        }
        value = ListUtil.removeDuplicateBySet(value);
        return ListUtil.listToArray(value);
    }
    public static void main(String[] args) throws Exception {
        String express1 = " 1 in (2) ";
        String express2 = "if(false){ } else{2}";
        String express3 = " round(4.34,1) ";
        String express4 = " '哈哈'.equals('哈哈') ";
        String express5 = "case ";
        ExpressRunner runner = new ExpressRunner();

        System.out.println("表达式计算：" + express1 + " 处理结果： " + runner.execute(express1, null, null, false, false) );
        System.out.println("表达式计算：" + express2 + " 处理结果： " + runner.execute(express2, null, null, false, false) );
        System.out.println("表达式计算：" + express3 + " 处理结果： " + runner.execute(express3, null, null, false, false) );
        System.out.println("表达式计算：" + express4 + " 处理结果： " + runner.execute(express4, null, null, false, false) );
        IExpressContext<String,Object> context = new DefaultContext<String,Object>();
        context.put("10x", 10);
        context.put("20x", 20);
        //System.out.println(runner.execute("10x+20x", context, null, false, false));



        String temp = "flskjflasjdfl <el>flsdakjfsdjflsjflaflkdsjb[</el>fsadfasdf<el>]fdsaf223{}fsdaf()fsaf}</el>f lfjksadlf234";
        for(String a :getTemplateKey(temp)){
            System.out.println(a);
        }
    }
}
