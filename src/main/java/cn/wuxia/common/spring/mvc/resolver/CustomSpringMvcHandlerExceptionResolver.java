/**
 *
 */
package cn.wuxia.common.spring.mvc.resolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
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
public class CustomSpringMvcHandlerExceptionResolver implements HandlerExceptionResolver, Ordered {

    private MessageSourceHandler messageSourceHandler;

    public final Logger logger = LoggerFactory.getLogger(CustomSpringMvcHandlerExceptionResolver.class);

    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String messageVal = ex.getMessage();
        String forwardPage = null;
        logger.info("Exception Type:{} , And Simple Message:{}", ex.getClass().getName(), ex.getMessage());
        if (ex instanceof ServiceException) {
            ServiceException serviceException = (ServiceException) ex;
            if (messageSourceHandler != null) {
                messageVal = messageSourceHandler.getString(serviceException.getMessage(), request.getLocale(), serviceException.getValue());
            } else {
                messageVal = serviceException.getI18nMessage(request.getLocale());
            }
            logger.warn(messageVal);
        } else {
            logger.error("", ex);
            messageVal = ex.getLocalizedMessage();
        }

        request.setAttribute("message", messageVal);
        if (ex instanceof AppSecurityException) {
            response.setStatus(403);
        } else if (ex instanceof AppObjectNotFoundException) {
            response.setStatus(404);
        } else {
            response.setStatus(500);
        }
        return new ModelAndView(forwardPage);
    }

    @Override
    public int getOrder() {
        return -1;
    }

    public MessageSourceHandler getMessageSourceHandler() {
        return messageSourceHandler;
    }

    public void setMessageSourceHandler(MessageSourceHandler messageSourceHandler) {
        this.messageSourceHandler = messageSourceHandler;
    }

}
