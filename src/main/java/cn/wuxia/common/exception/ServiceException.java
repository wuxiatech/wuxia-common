package cn.wuxia.common.exception;

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

    // 是否需要翻译
    private Boolean flag;

    private static final long serialVersionUID = 5786746916662131826L;

    public ServiceException() {
        super();
    }

    public ServiceException(String message, String... args) {
        super(message);
        logger.error(getMessage());
        this.value = args;
    }

    public ServiceException(String message, Boolean flag, String... args) {
        super(message);
        logger.error(getMessage());
        this.flag = flag;
        this.value = args;
    }

    public ServiceException(String message, Throwable cause, String... args) {
        super(message, cause);
        logger.error(getMessage(), cause);
        this.value = args;
    }

    public ServiceException(String message, Throwable cause, Boolean flag, String... args) {
        super(message, cause);
        logger.error(getMessage(), cause);
        this.flag = flag;
        this.value = args;
    }

    public String[] getValue() {
        return value;
    }

    public void setValue(String[] value) {
        this.value = value;
    }

    public Boolean getFlag() {
        if (flag == null) {
            flag = Boolean.TRUE;
        }
        return flag;
    }

    public void setFlag(Boolean flag) {
        this.flag = flag;
    }

    @Override
    public String getMessage() {
        if (Boolean.TRUE.equals(getFlag()) && getMessageSourceHandler() != null) {
            return getMessageSourceHandler().getString(super.getMessage(), getValue());
        }
        return super.getMessage();
    }

    public MessageSourceHandler getMessageSourceHandler() {
        try {
            return SpringContextHolder.getBean("messageSourceHandler");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
