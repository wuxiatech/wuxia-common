package cn.wuxia.common.exception;

/**
 * 
 * [ticket id]
 * Description of the class 
 * @author songlin.li
 * @ Version : V<Ver.No> <2013年6月30日>
 */
public class EmailException extends Exception {

    private static final long serialVersionUID = -2623309261327598087L;

    public EmailException(String msg) {
        super(msg);
    }

    public EmailException(String message, Throwable e) {
        super(message, e);
    }

    public EmailException(Throwable e) {
        super(e);
    }

}
