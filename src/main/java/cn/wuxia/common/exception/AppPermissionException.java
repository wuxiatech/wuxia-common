/*
 * Copyright 2011-2020 武侠科技 All right reserved.
 */
package cn.wuxia.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 权限统一定义的异常类。
 * 
 * @author lhl
 * @since 2011-2-24
 */
@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "无权访问")
public class AppPermissionException extends AppSecurityException {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public AppPermissionException() {
        super();
    }

    public AppPermissionException(String message, String... args) {
        super(message, args);
    }

    public AppPermissionException(String message, Throwable cause, String... args) {
        super(message, cause, args);
    }

}
