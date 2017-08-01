/*
 * Created on :Jun 27, 2013 Author :PL Change History Version Date Author Reason
 * <Ver.No> <date> <who modify> <reason>
 */
package cn.wuxia.common.spring.support;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceResourceBundle;

import cn.wuxia.common.util.StringUtil;

public class MessageSourceHandler {
    private final String PATTERN1 = "[{]\\s*(\\w|[.])*\\s*[}]";

    private final String PATTERN2 = "([{]|[}])";

    private MessageSource messageSource;

    private Locale defaultLocale = LocaleContextHolder.getLocale();

    public MessageSourceHandler() {
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

    public String getString(String message, String... args) {
        message = getString(message, defaultLocale);
        return translateParam(message, args);
    }

    public String getString(String message, Locale locale, String... args) {
        if (StringUtil.isBlank(message))
            return "";
        String returnVal = message;
        if (messageSource != null && locale != null) {
            MessageSourceResourceBundle sourceResourceBundle = new MessageSourceResourceBundle(messageSource, locale);
            Pattern pattern = Pattern.compile(PATTERN1);
            Matcher matcher = pattern.matcher(message);
            Map<String, String> map = new HashMap<String, String>();
            int num = 0;
            String translate = message;
            while (matcher.find(num)) {
                String val = matcher.group();
                num = matcher.start() + val.length();
                map.put(val, translate(sourceResourceBundle, val));
            }
            for (String key : map.keySet()) {
                translate = translate.replace(key, map.get(key));
            }
            returnVal = translate;
        }
        returnVal = translateParam(returnVal, args);
        return returnVal;
    }

    public String translate(MessageSourceResourceBundle sourceResourceBundle, String message) {
        String returnVal = message;
        if (message != null) {
            String key = message.replaceAll(PATTERN2, "").trim();
            try {
                returnVal = sourceResourceBundle.getString(key);
            } catch (Exception e) {
                returnVal = message;
            }
        }
        return returnVal;
    }

    public String translateParam(String message, String[] args) {
        if (StringUtil.isBlank(message))
            return "";
        if (ArrayUtils.isNotEmpty(args)) {
            Map<String, String> map = new HashMap<String, String>();
            int index = 0;
            for (String value : args) {
                map.put("[{]\\s*" + index++ + "\\s*[}]", value);
            }
            for (String key : map.keySet()) {
                message = message.replaceAll(key, map.get(key));
            }
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
