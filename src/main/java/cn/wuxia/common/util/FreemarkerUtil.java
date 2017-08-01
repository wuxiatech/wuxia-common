package cn.wuxia.common.util;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

public class FreemarkerUtil {
    private static Configuration cfg = new Configuration();

    /**
     *
     * @param templateStr
     * @param values
     * @return
     * @throws IOException
     * @throws TemplateException
     */
    public static String templateResolver(String templateStr, HashMap<String, Object> values) throws IOException, TemplateException {
        StringTemplateLoader stringLoader = new StringTemplateLoader();
        stringLoader.putTemplate("template", templateStr);

        Configuration config = new Configuration();
        config.setTemplateLoader(stringLoader);
        config.setDefaultEncoding("UTF-8");

        Writer out = new StringWriter();
        config.getTemplate("template", "UTF-8").process(values, out);

        return out.toString();
    }

    public static void main(String[] args) {
        HashMap<String, Object> map = Maps.newHashMap();
        map.put("verifyEmailUrl", "${ctx}/auth/account/verfiymail");
        try {
            System.out.println(
                    templateResolver("/Users/songlin/Documents/ucmworkspace/ueq-ucm/src/main/webapp/WEB-INF/freemarker/mail", map));
        } catch (IOException | TemplateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
