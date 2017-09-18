/*
 * Copyright 2011-2020 武侠科技 All right reserved.
 */
package cn.wuxia.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 找不到数据
 * 
 * @author songlin
 * @since
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "资源不存在或访问对象不存在")
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

    public AppObjectNotFoundException(String message, Throwable cause, String... args) {
        super(message, cause, args);
    }

}
