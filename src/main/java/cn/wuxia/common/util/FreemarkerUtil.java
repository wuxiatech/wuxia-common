package cn.wuxia.common.util;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class FreemarkerUtil {
    // 第一步：创建一个Configuration对象，直接new一个对象。构造方法的参数就是freemarker对于的版本号。
    private static
    Configuration configuration = new Configuration(Configuration.getVersion());

    /**
     * @param templatePath
     * @param templateName
     * @param outPath
     * @param values
     * @return
     * @throws IOException
     * @throws TemplateException
     */
    public static void templateResolver(String templatePath, String templateName, String outPath, HashMap<String, Object> values) throws IOException, TemplateException {
        // 第二步：设置模板文件所在的路径。
        configuration.setDirectoryForTemplateLoading(new File(templatePath));
        // 第三步：设置模板文件使用的字符集。一般就是utf-8.
        configuration.setDefaultEncoding("utf-8");
        // 第四步：加载一个模板，创建一个模板对象。
        Template template = configuration.getTemplate(templateName, "utf-8");
        // 第五步：创建一个模板使用的数据集，可以是pojo也可以是map。一般是Map。
        // 第六步：创建一个Writer对象，一般创建一FileWriter对象，指定生成的文件名。
        Writer out = new FileWriter(new File(outPath));
        // 第七步：调用模板对象的process方法输出文件。
        template.process(values, out);
        // 第八步：关闭流。
        out.close();
    }

    /**
     * @param templatePath
     * @param templateName
     * @param values
     * @return
     * @throws IOException
     * @throws TemplateException
     */
    public static String templateResolver(String templatePath, String templateName, HashMap<String, Object> values) throws IOException, TemplateException {
        // 第二步：设置模板文件所在的路径。
        configuration.setDirectoryForTemplateLoading(new File(templatePath));
        // 第三步：设置模板文件使用的字符集。一般就是utf-8.
        configuration.setDefaultEncoding("utf-8");
        // 第四步：加载一个模板，创建一个模板对象。
        Template template = configuration.getTemplate(templateName, "utf-8");
        // 第五步：创建一个模板使用的数据集，可以是pojo也可以是map。一般是Map。
        // 第六步：创建一个Writer对象。
        Writer out = new StringWriter();
        // 第七步：调用模板对象的process方法输出文件。
        template.process(values, out);
        // 第八步：输出格式化后端内容。
        return out.toString();
    }

    /**
     * @param templateStr
     * @param values
     * @return
     * @throws IOException
     * @throws TemplateException
     */
    public static String templateResolver(String templateStr, HashMap<String, Object> values) throws IOException, TemplateException {
        StringTemplateLoader stringLoader = new StringTemplateLoader();
        stringLoader.putTemplate("template", templateStr);

        configuration.setTemplateLoader(stringLoader);
        configuration.setDefaultEncoding("UTF-8");

        Writer out = new StringWriter();
        configuration.getTemplate("template", "UTF-8").process(values, out);

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
