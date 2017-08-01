/*
 * Copyright 2011-2020 武侠科技 All right reserved.
 */
package cn.wuxia.common.exception;

/**
 * service层统一定义的异常类。
 * 
 * @author lhl
 * @since 2011-2-24
 */
public class AppServiceException extends ServiceException {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public AppServiceException() {
        super();
    }

    public AppServiceException(String message,String... args) {
        super(message,args);
    }
    
    public AppServiceException(String message,Boolean flag,String... args) {
        super(message,flag,args);
    }
    
    public AppServiceException(String message, Throwable cause,String... args) {
        super(message,cause,args);
    }
    
    public AppServiceException(String message, Throwable cause,Boolean flag,String... args) {
        super(message,cause,flag,args);
    }
}
