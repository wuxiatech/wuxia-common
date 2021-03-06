/*
 * Created on :Jun 27, 2013 Author :PL Change History Version Date Author Reason
 * <Ver.No> <date> <who modify> <reason>
 */
package cn.wuxia.common.spring.support;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceResourceBundle;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import cn.wuxia.common.util.StringUtil;

public class MessageSourceHandler {
    public final Logger logger = LoggerFactory.getLogger(MessageSourceHandler.class);

    private final String MSG_PATTERN = "\\$+[{]+[\\w\\.]+[}]";

    private MessageSource messageSource;

    private Locale defaultLocale;

    public MessageSourceHandler() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setBasename("classpath:i18n/messages");
        messageSource.setUseCodeAsDefaultMessage(true);
        this.messageSource = messageSource;
        this.defaultLocale = LocaleContextHolder.getLocale();

        logger.info("初始化国际化文件classpath:i18n/messages_{}.properties, 如不存在则使用classpath:i18n/messages.properties", this.defaultLocale.getLanguage());

    }

    public MessageSourceHandler(MessageSource messageSource, Locale defaultLocale) {
        this.messageSource = messageSource;
        this.defaultLocale = defaultLocale;
    }

    public MessageSourceHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getString(String message) {
        return getString(message, defaultLocale);
    }

    /**
     * 返回系统语言
     * <pre>
     * classpath:i18n/messages.properties 
     * example1: {error.message=system error!}
     * when 
     *      {@link #getString(String,  String...)}("${error.message} {}","这个是参数")
     * then
     *      return system error! 这个是参数
     *      
     * example2: {error.message=用户{0}，你好，你的密码错了，账号：{1}!}
     * when 
     *      {@link #getString(String,  String...)}("{}${error.message}{}","参数1","参数2", "张三", "13800138000")
     * then
     *      return 参数1用户张三，你好，你的密码错了，账号：13800138000!参数2
     *      
     *      
     * </pre>
     * @author songlin
     * @return
     */
    public String getString(String message, String... args) {
        return getString(message, defaultLocale, args);
    }

    /**
     * 返回系统语言
     * <pre>
     * classpath:i18n/messages_{locale}.properties 
     * example1: {error.message=system error!}
     * when 
     *      {@link #getString(String, Locale, String...)}("${error.message} {}","en_US", "这个是参数")
     * then
     *      return system error! 这个是参数
     *      
     * example2: {error.message=用户{0}，你好，你的密码错了，账号：{1}!}
     * when 
     *      {@link #getString(String, Locale, String...)}("{}${error.message}{}","en_US","参数1","参数2", "张三", "13800138000")
     * then
     *      return 参数1用户张三，你好，你的密码错了，账号：13800138000!参数2
     *      
     *      
     * </pre>
     * @author songlin
     * @return
     */
    public String getString(String message, Locale locale, String... args) {
        if (StringUtil.isBlank(message))
            return "";
        if (messageSource != null && locale != null) {
            MessageSourceResourceBundle sourceResourceBundle = new MessageSourceResourceBundle(messageSource, locale);
            Pattern pattern = Pattern.compile(MSG_PATTERN, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(message);
            while (matcher.find()) {
                String key = matcher.group();
                String propertiesKey = key.substring(2, key.length() - 1);
                String value = sourceResourceBundle.getString(propertiesKey);
                message = StringUtil.replace(message, key, value);
            }
        }
        if (ArrayUtils.isNotEmpty(args)) {

            for (String value : args) {
                message = message.replaceFirst("\\{\\}", value);
            }

            /**
             * TODO {} 与 {0}不能混用
             */
            message = MessageFormat.format(message, args);
        }
        return message;
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

}
