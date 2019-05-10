package cn.wuxia.common.spring.mvc;

import cn.wuxia.common.bean.CustomMessage;
import cn.wuxia.common.spring.support.MessageSourceHandler;
import cn.wuxia.common.spring.support.Msg;
import cn.wuxia.common.spring.support.Msg.CustomMessageTypeEnum;
import cn.wuxia.common.util.ListUtil;
import cn.wuxia.common.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 自定义消息拦截器
 *
 * @author PL
 */
public class MessageInterceptor implements HandlerInterceptor {
    private static Logger logger = LoggerFactory.getLogger(MessageInterceptor.class);

    public final static String TYPEKEY = "type";

    public final static String MESSAGEKEY = "messages";

    MessageSourceHandler messageSourceHandler;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String url = request.getRequestURI() + (StringUtil.isBlank(request.getQueryString()) ? "" : "?" + request.getQueryString());
        logger.info("请求地址：{}", url);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        response.addHeader("x-frame-options","SAMEORIGIN");
        List<String> infos = new ArrayList<String>();
        List<String> warns = new ArrayList<String>();
        List<String> errors = new ArrayList<String>();
        List<String> succes = new ArrayList<String>();
        List<String> valids = new ArrayList<String>();
        List<CustomMessage> customMessages = Msg.getMessages();
        Msg.cleanMessages();
        for (CustomMessage customMessage : customMessages) {
            if (Boolean.TRUE.equals(customMessage.getTranslate())) {
                String message = translateMessage(customMessage.getKey(), request.getLocale(), customMessage.getArgs());

                if (StringUtil.isBlank(message)) {
                    continue;
                }
                if (CustomMessageTypeEnum.INFO.equals(customMessage.getType())) {
                    infos.add(message);
                } else if (CustomMessageTypeEnum.WARN.equals(customMessage.getType())) {
                    warns.add(message);
                } else if (CustomMessageTypeEnum.ERROR.equals(customMessage.getType())) {
                    errors.add(message);
                } else if (CustomMessageTypeEnum.SUCCESS.equals(customMessage.getType())) {
                    succes.add(message);
                } else if (CustomMessageTypeEnum.SUCCESS.equals(customMessage.getType())) {
                    valids.add(message);
                }
            }
        }
        /**
         * fixed bug:java.lang.IllegalStateException: Cannot create a session after the response has been committed
         */
        boolean isCommited = response.isCommitted();
        if (isCommited) {
            return;
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> all = (List<Map<String, Object>>) request.getSession().getAttribute(Msg.ALLMESSAGESKEY);
        if (ListUtil.isEmpty(all)) {
            all = new ArrayList<Map<String, Object>>();
        }else {
            logger.warn("message:-------------------------{}", all);
        }
        if (ListUtil.isNotEmpty(infos)) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(TYPEKEY, Msg.INFOMESSAGESKEY);
            map.put(MESSAGEKEY, infos);
            all.add(map);
        }
        if (ListUtil.isNotEmpty(warns)) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(TYPEKEY, Msg.WARNMESSAGESKEY);
            map.put(MESSAGEKEY, warns);
            all.add(map);
        }
        if (ListUtil.isNotEmpty(errors)) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(TYPEKEY, Msg.ERRORMESSAGESKEY);
            map.put(MESSAGEKEY, errors);
            all.add(map);
        }
        if (ListUtil.isNotEmpty(succes)) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(TYPEKEY, Msg.SUCCESSMESSAGESKEY);
            map.put(MESSAGEKEY, succes);
            all.add(map);
        }
        if (ListUtil.isNotEmpty(valids)) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(TYPEKEY, Msg.VALIDMESSAGESKEY);
            map.put(MESSAGEKEY, valids);
            all.add(map);
        }
        if (modelAndView != null) {
            modelAndView.addObject(Msg.ALLMESSAGESKEY, all);
            modelAndView.addObject(Msg.INFOMESSAGESKEY, infos);
            modelAndView.addObject(Msg.WARNMESSAGESKEY, warns);
            modelAndView.addObject(Msg.ERRORMESSAGESKEY, errors);
            modelAndView.addObject(Msg.SUCCESSMESSAGESKEY, succes);
            modelAndView.addObject(Msg.VALIDMESSAGESKEY, valids);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
//        request.getSession().removeAttribute(Msg.ALLMESSAGESKEY);
        Msg.cleanMessages();
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
