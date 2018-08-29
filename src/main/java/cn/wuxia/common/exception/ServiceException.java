package cn.wuxia.common.exception;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.wuxia.common.spring.SpringContextHolder;
import cn.wuxia.common.spring.support.MessageSourceHandler;

/**
 * Service Exception. business error message, not rollback
 *
 * @author songlin.li
 */
public class ServiceException extends RuntimeException {
    protected Logger logger = LoggerFactory.getLogger(ServiceException.class);

    /**
     *
     */

    private String[] value;

    private static final long serialVersionUID = 5786746916662131826L;

    public ServiceException() {
        super();
    }

    public ServiceException(String message, String... args) {
        super(message);
        this.value = args;
        logger.error(getI18nMessage());
    }

    public ServiceException(String message, Throwable cause, String... args) {
        super(message, cause);
        this.value = args;
        logger.error(getI18nMessage(), cause);
    }

    public String[] getValue() {
        return value;
    }

    public void setValue(String[] value) {
        this.value = value;
    }

    @Override
    public String getMessage() {
        return getI18nMessage();
    }

    /**
     * 返回系统语言
     * <pre>
     * classpath:i18n/messages.properties 
     * example1: {error.message=system error!}
     * when 
     *      throw new ServiceException("${error.message} {}", "这个是参数")
     * then
     *      print result = system error! 这个是参数
     *      
     * example2: {error.message=用户{0}，你好，你的密码错了，账号：{1}!}
     * when 
     *      throw new ServiceException("{}${error.message}{}","参数1","参数2", "张三", "13800138000")
     * then
     *      print result = 参数1用户张三，你好，你的密码错了，账号：13800138000!参数2
     *      
     *      
     * </pre>
     * @author songlin
     * @return
     */
    public String getI18nMessage() {
        if (getMessageSourceHandler() != null) {
            return getMessageSourceHandler().getString(super.getMessage(), getValue());
        }
        return super.getMessage();
    }

    /**
     * 返回系统语言
     * <pre>
     * classpath:i18n/messages_{locale}.properties 
     * example1: {error.message=system error!}
     * when 
     *      throw new ServiceException("${error.message} {}", "这个是参数")
     * then
     *      print result = system error! 这个是参数
     *      
     * example2: {error.message=用户{0}，你好，你的密码错了，账号：{1}!}
     * when 
     *      throw new ServiceException("{}${error.message}{}","参数1","参数2", "张三", "13800138000")
     * then
     *      print result = 参数1用户张三，你好，你的密码错了，账号：13800138000!参数2
     *      
     *      
     * </pre>
     * @author songlin
     * @return
     */
    public String getI18nMessage(Locale locale) {
        if (getMessageSourceHandler() != null) {
            return getMessageSourceHandler().getString(super.getMessage(), locale, getValue());
        }
        return super.getMessage();
    }

    public MessageSourceHandler getMessageSourceHandler() {
        try {
            return SpringContextHolder.getBean(MessageSourceHandler.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new MessageSourceHandler();
    }
}
