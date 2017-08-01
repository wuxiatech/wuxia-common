/*
 * Created on :23 Apr, 2014 Author :songlin Change History Version Date Author
 * Reason <Ver.No> <date> <who modify> <reason>
 */
package cn.wuxia.common.exception;

/**
 * 图像处理异常
 * 
 * @author songlin
 */
public class ImgException extends Exception {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;

    public ImgException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImgException(String message) {
        super(message);
    }

}
