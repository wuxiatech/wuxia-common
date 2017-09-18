/*
 * Copyright 2011-2020 武侠科技 All right reserved.
 */
package cn.wuxia.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * service层统一定义的异常类。
 * 
 * @author songlin.li
 * @since 2011-2-24
 */
@ResponseStatus(value= HttpStatus.INTERNAL_SERVER_ERROR, reason="业务层错误")
public class AppServiceException extends ServiceException {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public AppServiceException() {
        super();
    }

    public AppServiceException(String message, String... args) {
        super(message, args);
    }

    public AppServiceException(String message, Throwable cause, String... args) {
        super(message, cause, args);
    }

}
