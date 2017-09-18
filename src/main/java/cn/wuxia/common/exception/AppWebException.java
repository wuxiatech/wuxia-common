/*
 * Copyright 2011-2020 武侠科技 All right reserved.
 */
package cn.wuxia.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Web层统一定义的异常类。
 * 
 * @author lhl
 * @since 2011-2-24
 */
@ResponseStatus(value= HttpStatus.INTERNAL_SERVER_ERROR, reason="处理层错误")
public class AppWebException extends ServiceException {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public AppWebException() {
        super();
    }

    public AppWebException(String message, String... args) {
        super(message, args);
    }

    public AppWebException(String message, Throwable cause, String... args) {
        super(message, cause, args);
    }

   
}
