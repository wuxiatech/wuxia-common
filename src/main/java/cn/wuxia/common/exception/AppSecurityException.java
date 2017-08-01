/*
 * Copyright 2011-2020 武侠科技 All right reserved.
 */
package cn.wuxia.common.exception;

/**
 * 系统安全统一定义的异常类。
 * 
 * @author lhl
 * @since 2011-2-24
 */
public class AppSecurityException extends ServiceException {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public AppSecurityException() {
        super();
    }

    public AppSecurityException(String message,String... args) {
        super(message,args);
    }
    
    public AppSecurityException(String message,Boolean flag,String... args) {
        super(message,flag,args);
    }
    
    public AppSecurityException(String message, Throwable cause,String... args) {
        super(message,cause,args);
    }
    
    public AppSecurityException(String message, Throwable cause,Boolean flag,String... args) {
        super(message,cause,flag,args);
    }
}
