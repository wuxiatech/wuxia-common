/*
 * Copyright 2011-2020 武侠科技 All right reserved.
 */
package cn.wuxia.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 系统安全统一定义的异常类。
 * 
 * @author lhl
 * @since 2011-2-24
 */
@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "安全性检查")
public class AppSecurityException extends ServiceException {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public AppSecurityException() {
        super();
    }

    public AppSecurityException(String message, String... args) {
        super(message, args);
    }

    public AppSecurityException(String message, Throwable cause, String... args) {
        super(message, cause, args);
    }

}
