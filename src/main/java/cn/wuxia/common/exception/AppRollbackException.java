package cn.wuxia.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  error message, rollback transaction
 * 
 * @author songlin.li
 */
public class AppRollbackException extends Exception {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 7563829349524153769L;

    public AppRollbackException() {
        super();
    }

    public AppRollbackException(String message) {
        super(message);
        logger.error(message);
    }

    public AppRollbackException(String message, Throwable cause) {
        super(message, cause);
        logger.error(message, cause);
    }

    public AppRollbackException(Throwable cause) {
        super(cause);
        logger.error("", cause);
    }
}
