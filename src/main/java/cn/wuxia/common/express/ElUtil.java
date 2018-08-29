package cn.wuxia.common.express;

import cn.wuxia.common.util.ListUtil;
import org.nutz.el.El;
import org.nutz.el.opt.RunMethod;
import org.nutz.el.opt.custom.CustomMake;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ElUtil {



    /**
     * @param str
     * @return
     * @description : 返回?{} 内的key，key允许的字符包括.#:[]*\/+-()
     * @author songlin.li
     */
    public static String[] getTemplateKey(String str) {
        Pattern p = Pattern.compile("\\?+[{]+[\\w\\.\\#\\:\\[\\]\\*\\/\\+\\-\\(\\)\\s*]+[}]");
        Matcher m = p.matcher(str);

        List<String> value = new ArrayList<String>();
        while (m.find()) {
            String key = m.group();
            key = key.substring(2, key.length() - 1);
            value.add(key);
        }
        if (ListUtil.isEmpty(value)) {
            return new String[] {};
        }
        value = ListUtil.removeDuplicateBySet(value);
        return ListUtil.listToArray(value);
    }

    /**
     * @param str
     * @return
     * @description : 返回[]内的内容
     * @author songlin.li
     */
    public static String[] getBracketsKey(String str) {
        Pattern p = Pattern.compile("(\\[+[\\w\\.\\#\\:\\*\\/\\+\\-\\(\\)\\s*]+\\])");
        Matcher m = p.matcher(str);

        List<String> value = new ArrayList<String>();
        while (m.find()) {
            String key = m.group();
            key = key.substring(1, key.length() - 1);
            value.add(key);
        }
        if (ListUtil.isEmpty(value)) {
            return new String[] {};
        }
        value = ListUtil.removeDuplicateBySet(value);
        return ListUtil.listToArray(value);
    }



    /**
     * @param str
     * @return
     * @description : 返回括号的内容
     * @author songlin.li
     */
    public static String[] getBracketKey(String str) {
        Pattern p = Pattern.compile("[(]+[\\w\\.\\#\\:\\(\\)\\s*]+[)]");
        Matcher m = p.matcher(str);

        List<String> value = new ArrayList<String>();
        while (m.find()) {
            String key = m.group();
            key = key.substring(1, key.length() - 1);
            value.add(key);
        }
        if (ListUtil.isEmpty(value)) {
            return new String[] {};
        }
        value = ListUtil.removeDuplicateBySet(value);
        return ListUtil.listToArray(value);
    }


    /**
     * @param str
     * @return
     * @description : 返回单词
     * @author songlin.li
     */
    public static String[] getWordKey(String str) {
        Pattern p = Pattern.compile("\\w+");
        Matcher m = p.matcher(str);

        List<String> value = new ArrayList<String>();
        while (m.find()) {
            String key = m.group();
            value.add(key);
        }
        if (ListUtil.isEmpty(value)) {
            return new String[] {};
        }
        value = ListUtil.removeDuplicateBySet(value);
        return ListUtil.listToArray(value);
    }

    /**
     * @param str
     * @return
     * @description : 返回单词
     * @author songlin.li
     */
    public static String[] getNumberIndexKey(String str) {
        Pattern p = Pattern.compile("\\d+#");
        Matcher m = p.matcher(str);

        List<String> value = new ArrayList<String>();
        while (m.find()) {
            String key = m.group();
            value.add(key);
        }
        if (ListUtil.isEmpty(value)) {
            return new String[] {};
        }
        value = ListUtil.removeDuplicateBySet(value);
        return ListUtil.listToArray(value);
    }
    public static void main(String[] args) {

        String temp = "jflasdjfl ${fasdf} #{fasdf} fkaslf<a > fasl</b> ?{666} lolofasdf 中文救场?{[1#].score} f ?{1:20#}  fasldflll ?{[a]*[8#]} "
                + " f?{sum([1:20#].score)} fff++++fasd   ?{(1#+20#+30#-5#)/2}";
        String[] keys = getTemplateKey(temp);
        int i = 0;
        for (String key : keys) {
            System.out.println(i++ + " == " + key);

        }
        String[] keys2 = getBracketsKey(temp);
        int ii = 0;
        for (String key : keys2) {
            System.out.println(ii++ + " == " + key);
        }

        String[] keys3 = getBracketKey(temp);
        int iii = 0;
        for (String key : keys3) {
            System.out.println(iii++ + " == " + key);
        }
        //        普通运算
        System.out.println(El.eval("3+2*5"));
        // 输出为  13
        //        字符串操作
        System.out.println(El.eval("trim(\"  abc  \")"));

        // 输出为  abc
        //        Java 对象属性访问调用
        Context context = Lang.context();
        Testtext pet = new Testtext();
        pet.setName("GFW");
        context.set("pet", pet);
        System.out.println(El.eval(context, "pet.name"));
        // 输出为  GFW
        //        函数调用
        El.eval(context, "pet.setName('XiaoBai')");
        System.out.println(El.eval(context, "pet.getName()"));
        // 输出为  XiaoBai
        //        数组访问
//        context.set("x", Lang.array("A", "B", "C"));
//
//        System.out.println(El.eval(context, "x[0].toLowerCase()"));
        // 输出为  a
        //        列表访问
//        context.set("x", Lang.list("A", "B", "C"));
//
//        System.out.println(El.eval(context, "x.get(0).toLowerCase()"));
        // 输出为  a
        //        Map 访问
        context.set("map", Lang.map("{x:10, y:5}"));

        System.out.println(El.eval(context, "map['x'] * map['y']"));
        // 输出为  50
        //        判断
        context.set("a", 5);

        System.out.println(El.eval(context, "a>10"));
        // 输出为  false

        context.set("a", 20);
        System.out.println(El.eval(context, "a>10"));
        // 输出为  true
        CustomMake.me().register("sum", new TestFunction());
        System.out.println(El.eval(context, "sum(a+5)"));
        context.set("10x", 10);
        context.set("20x", 20);
        System.out.println(El.eval(context, "10x+20x"));
    }

    public static class Testtext {
        String name;

        Object value;

        public Testtext() {
        }

        public Testtext(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }

    public static class TestFunction implements RunMethod {

        @Override
        public Object run(List<Object> fetchParam) {
            Integer a = 0;
            for (Object object : fetchParam) {
                System.out.println("=====" + object);
                a += (Integer) object;
            }
            return a;
        }

        @Override
        public String fetchSelf() {
            return null;
        }
    }
}
