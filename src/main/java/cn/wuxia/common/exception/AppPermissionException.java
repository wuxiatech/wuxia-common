/*
 * Copyright 2011-2020 武侠科技 All right reserved.
 */
package cn.wuxia.common.exception;

/**
 * 权限统一定义的异常类。
 * 
 * @author lhl
 * @since 2011-2-24
 */
public class AppPermissionException extends AppSecurityException {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public AppPermissionException() {
        super();
    }

    public AppPermissionException(String message,String... args) {
        super(message,args);
    }
    
    public AppPermissionException(String message,Boolean flag,String... args) {
        super(message,flag,args);
    }
    
    public AppPermissionException(String message, Throwable cause,String... args) {
        super(message,cause,args);
    }
    
    public AppPermissionException(String message, Throwable cause,Boolean flag,String... args) {
        super(message,cause,flag,args);
    }
}
