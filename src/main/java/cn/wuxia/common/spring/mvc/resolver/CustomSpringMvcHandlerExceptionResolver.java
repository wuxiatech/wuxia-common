/**
 * 
 */
package cn.wuxia.common.spring.mvc.resolver;

import java.util.Locale;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import cn.wuxia.common.exception.AppObjectNotFoundException;
import cn.wuxia.common.exception.AppSecurityException;
import cn.wuxia.common.exception.ServiceException;
import cn.wuxia.common.spring.support.MessageSourceHandler;

/**
 * 统一异常处理器
 * 
 * @author songlin.li
 * @since 2012-4-7
 */
@Component
public class CustomSpringMvcHandlerExceptionResolver implements HandlerExceptionResolver, Ordered {

    private MessageSourceHandler messageSourceHandler;

    public final Logger logger = LoggerFactory.getLogger(CustomSpringMvcHandlerExceptionResolver.class);

    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String messageVal = ex.getMessage();
        String forwardPage = null;
        logger.info("Exception Type:{} , And Simple Message:{}", ex.getClass().getName(), ex.getMessage());
        if (ex instanceof ServiceException) {
            logger.warn(ex.getMessage());
            ServiceException serviceException = (ServiceException) ex;
            messageVal = serviceException.getMessage();
        } else {
            logger.error("", ex);
            messageVal = translateMessage("{error.systemError}", request.getLocale());
        }

        request.setAttribute("message", messageVal);
        if (ex instanceof AppSecurityException) {
            forwardPage = "error/403";
        } else if (ex instanceof AppObjectNotFoundException) {
            forwardPage = "error/404";
        } else {
            forwardPage = "error/500";
        }
        return new ModelAndView(forwardPage);
    }

    @Override
    public int getOrder() {
        return -1;
    }

    public String translateMessage(String message, Locale locale, String... args) {
        String returnValue = message;
        if (message != null && messageSourceHandler != null) {
            returnValue = messageSourceHandler.getString(message, locale, args);
        }
        return returnValue;
    }

    public MessageSourceHandler getMessageSourceHandler() {
        return messageSourceHandler;
    }

    @Autowired
    public void setMessageSourceHandler(MessageSourceHandler messageSourceHandler) {
        this.messageSourceHandler = messageSourceHandler;
    }

}
