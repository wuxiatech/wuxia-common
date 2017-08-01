package cn.wuxia.common.exception;

/**
 * Validate Exception. business error message, rollback transaction
 * 
 * @author songlin.li
 */
public class ValidateException extends Exception {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 7563829349524153769L;

    public ValidateException() {
        super();
    }

    public ValidateException(String message) {
        super(message);
    }

    public ValidateException(Throwable cause) {
        super(cause);
    }

    public ValidateException(String message, Throwable cause) {
        super(message, cause);
    }
}
