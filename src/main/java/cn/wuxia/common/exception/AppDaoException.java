/*
 * Copyright 2011-2020 武侠科技 All right reserved.
 */
package cn.wuxia.common.exception;

/**
 * DAO层统一定义的异常类。
 * 
 * @author songlin.li
 * @since 2011-2-24
 */
public class AppDaoException extends AppRollbackException {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public AppDaoException() {
        super();
    }

    public AppDaoException(String message) {
        super(message);
    }

    public AppDaoException(String message, Throwable cause) {
        super(message, cause);
    }

    public AppDaoException(Throwable cause) {
        super(cause);
    }
}
