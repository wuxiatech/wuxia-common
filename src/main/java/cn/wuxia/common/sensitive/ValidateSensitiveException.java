package cn.wuxia.common.sensitive;

import cn.wuxia.common.exception.ValidateException;

/**
 * Validate Exception. business error message, rollback transaction
 * 
 * @author songlin.li
 */
public class ValidateSensitiveException extends ValidateException {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 7563829349524153769L;

    public ValidateSensitiveException() {
        super();
    }

    public ValidateSensitiveException(String message) {
        super(message);
    }

    public ValidateSensitiveException(Throwable cause) {
        super(cause);
    }

    public ValidateSensitiveException(String message, Throwable cause) {
        super(message, cause);
    }
}
