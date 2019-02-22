package cn.wuxia.common.express;

import cn.wuxia.common.util.ListUtil;
import cn.wuxia.common.util.StringUtil;
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

    public static String replace(String string, String key , String replacement){
        return StringUtil.replace(string, "<el>"+key+"</el>", replacement);
    }

    public static void main(String[] args) throws Exception {
        String express1 = " 1 in (2) ";
        String express2 = "if(false){ } else{2}";
        String express3 = " round(4.34,1) ";
        String express4 = " '哈哈'.equals('哈哈') ";
        String express5 = "case ";
        String express6 = " ((_1abx1 == 10 && bbx == 20) || abc == 1)";
        ExpressRunner runner = new ExpressRunner();

        System.out.println("表达式计算：" + express1 + " 处理结果： " + runner.execute(express1, null, null, false, false) );
        System.out.println("表达式计算：" + express2 + " 处理结果： " + runner.execute(express2, null, null, false, false) );
        System.out.println("表达式计算：" + express3 + " 处理结果： " + runner.execute(express3, null, null, false, false) );
        System.out.println("表达式计算：" + express4 + " 处理结果： " + runner.execute(express4, null, null, false, false) );
        IExpressContext<String,Object> context = new DefaultContext<String,Object>();
        context.put("_1abx1", 180);
        context.put("bbx", 201);
        context.put("abc", 1);
        //System.out.println(runner.execute("10x+20x", context, null, false, false));

        System.out.println(runner.execute(express6, context, null, false, false));

        String temp = "flskjflasjdfl <el>flsdakjfsdjflsjflaflkdsjb[</el>fsadfasdf<el>]fdsaf223{}fsdaf()fsaf}</el>f lfjksadlf234";
        for(String a :getTemplateKey(temp)){
            System.out.println(a);
        }

        String temp2 = "fasdf<el>int a = 10; if(score*a > 60){'你很好！'}else{'你很差'}</el>fsadfdsaf";
        String express7 = getTemplateKey(temp2)[0];
        ((DefaultContext<String, Object>) context).put("score", 30);
        Object replacement = runner.execute(express7, context, null, false, false);
        System.out.println(replace(temp2, express7, replacement+""));



        String express8 = "location.indexOf('佛山')<0";
        ((DefaultContext<String, Object>) context).put("location", "广东，佛山");

        Object express8result = runner.execute(express8, context, null, false, false);
        System.out.println(express8result);

    }
}
