package cn.wuxia.common.util;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import jodd.bean.BeanTemplateParser;

import java.util.Map;

public class StringParserUtil {

    /**
     * @param template
     * @param bean
     * @return
     * @description : string support el tags <code>${contents}</code> and inner Object like<code>${bean.contents}</code>
     * 使用{@link #parse(String, Object)}有更多的解释支持
     * @author songlin.li
     * @see {@link jodd.bean.BeanTemplateParser#parse(String, Object) }
     */
    public static String simpleParse(String template, Object bean) {
        if (StringUtils.isBlank(template) || bean == null)
            return template;
        BeanTemplateParser btp = new BeanTemplateParser();
        return btp.parse(template, bean);
    }

    public static String simpleParse(String template, Object[] beans) {
        if (StringUtils.isBlank(template) || ArrayUtils.isEmpty(beans))
            return template;
        for (Object bean : beans) {
            template = simpleParse(template, bean);
        }
        return template;
    }

    /**
     * @param template
     * @param bean
     * @return
     * @description : 使用spel <code>#{contents}</code> and inner Object like<code>#{bean.contents}</code>
     * 更多语法请自行百度spring的spel
     * @author songlin.li
     *
     */
    public static String spelParse(String template, Object bean) {
        if (StringUtils.isBlank(template) || bean == null)
            return template;
        ExpressionParser parser = new SpelExpressionParser();
        //设置上下文
//        StandardEvaluationContext context = new StandardEvaluationContext(bean);
        //设置变量
        //context.setVariable("变量名", "变量值");
        return parser.parseExpression(template, new TemplateParserContext()).getValue(bean, String.class);
    }

    public static String spelParse(String template, Object[] beans) {
        if (StringUtils.isBlank(template) || ArrayUtils.isEmpty(beans))
            return template;
        for (Object bean : beans) {
            template = spelParse(template, bean);
        }
        return template;
    }

    /**
     * @param template
     * @param bean
     * @return
     * @description : 使用spel <code>#{contents}</code>
     * 更多语法请自行百度spring的spel
     * @author songlin.li
     *
     */
    public static String spelParse(String template, String key, String value) {
        if (StringUtils.isBlank(template) || StringUtil.isBlank(key))
            return template;
        ExpressionParser parser = new SpelExpressionParser();
        //设置上下文
        StandardEvaluationContext context = new StandardEvaluationContext();
        //设置变量
        context.setVariable(key, value);
        return parser.parseExpression(template, new TemplateParserContext()).getValue(context, String.class);
    }

    public static void main(String[] args) {
        Map m = Maps.newHashMap();

        m.put("a", 10);
        m.put("b", "hlya");
        m.put("c", "仲文");

        m.put("d", new Testtext("我是名字", "我是值"));
        String temp = "aafsaf 中文 #{[a]} , #{[d].name}  new Date()  ${b}   ${d.value}";
        System.out.println(spelParse(temp, m));
        System.out.println(simpleParse(temp, m));
        System.out.println(spelParse("fsdaljflsakfjl#{name}, flasjfl#{value.name}  fasdfas#{value.value[c]}", new Testtext("我是名字", new Testtext2("我是名字2", m))));
        ExpressionParser parser = new SpelExpressionParser();
        String t2 = "T(cn.wuxia.common.util.DateUtil).format(new java.util.Date(),'yyyy-MM-dd')";
        System.out.println(parser.parseExpression(t2).getValue(String.class));


        System.out.println(simpleParse("sdlfjaslkfjlsadkfjadls;f", m));
    }

    static class Testtext{
        String name;
        Object value;

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

    static class Testtext2{
        String name;
        Object value;

        public Testtext2(String name, Object value) {
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
}
