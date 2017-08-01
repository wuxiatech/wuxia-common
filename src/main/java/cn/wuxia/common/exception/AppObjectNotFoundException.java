/*
 * Copyright 2011-2020 武侠科技 All right reserved.
 */
package cn.wuxia.common.exception;

/**
 * 找不到数据
 * 
 * @author songlin
 * @since
 */
public class AppObjectNotFoundException extends ServiceException {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public AppObjectNotFoundException() {
        super();
    }

    public AppObjectNotFoundException(String message, String... args) {
        super(message, args);
    }

    public AppObjectNotFoundException(String message, Boolean flag, String... args) {
        super(message, flag, args);
    }

    public AppObjectNotFoundException(String message, Throwable cause, String... args) {
        super(message, cause, args);
    }

    public AppObjectNotFoundException(String message, Throwable cause, Boolean flag, String... args) {
        super(message, cause, flag, args);
    }
}
