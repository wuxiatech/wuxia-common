package cn.wuxia.common.spring.support;

import java.util.ArrayList;
import java.util.List;

import cn.wuxia.common.bean.CustomMessage;

/**
 * 自定义message容器
 * 
 * @author PL
 * @since 2012-4-7
 */
public class Msg {
    public enum CustomMessageTypeEnum {
        SUCCESS, ERROR, WARN, INFO, VALID
    }

    public final static String ALLMESSAGESKEY = "alert_all";

    public final static String INFOMESSAGESKEY = "info";

    public final static String WARNMESSAGESKEY = "warn";

    public final static String ERRORMESSAGESKEY = "error";

    public final static String SUCCESSMESSAGESKEY = "success";

    public final static String VALIDMESSAGESKEY = "validMsgs";

    private static ThreadLocal<List<CustomMessage>> MESSAGELOCAL = new ThreadLocal<List<CustomMessage>>() {
        @Override
        protected List<CustomMessage> initialValue() {
            return new ArrayList<CustomMessage>();
        }

    };

    /**
     * 添加message类型为INFO
     * @author PL
     * @param message
     * @param args
     */
    public static void info(String message, String... args) {
        CustomMessage customMessage = new CustomMessage(message, CustomMessageTypeEnum.INFO, args);
        MESSAGELOCAL.get().add(customMessage);
    }

    /**
     * 添加message类型为WARN
     * @author PL
     * @param message
     * @param args
     */
    public static void warn(String message, String... args) {
        CustomMessage customMessage = new CustomMessage(message, CustomMessageTypeEnum.WARN, args);
        MESSAGELOCAL.get().add(customMessage);
    }

    /**
     * 添加message类型为ERROR
     * @author PL
     * @param message
     * @param args
     */
    public static void error(String message, String... args) {
        CustomMessage customMessage = new CustomMessage(message, CustomMessageTypeEnum.ERROR, args);
        MESSAGELOCAL.get().add(customMessage);
    }

    /**
     * 添加message类型为SUCCESS
     * @author PL
     * @param message
     * @param args
     */
    public static void success(String message, String... args) {
        CustomMessage customMessage = new CustomMessage(message, CustomMessageTypeEnum.SUCCESS, args);
        MESSAGELOCAL.get().add(customMessage);
    }

    /**
     * 添加message类型为SUCCESS
     * @author PL
     * @param message
     * @param args
     */
    public static void valid(String message, String... args) {
        CustomMessage customMessage = new CustomMessage(message, CustomMessageTypeEnum.VALID, args);
        MESSAGELOCAL.get().add(customMessage);
    }

    /**
     * 添加message类型为SUCCESS
     * @author PL
     * @param message
     * @param flag
     * @param args
     */
    public static void addMessage(String message, CustomMessageTypeEnum type, boolean flag, String... args) {
        CustomMessage customMessage = new CustomMessage(message, type, flag, args);
        MESSAGELOCAL.get().add(customMessage);
    }

    public static List<CustomMessage> getMessages() {
        List<CustomMessage> returnMessages = new ArrayList<CustomMessage>(0);
        List<CustomMessage> currentMessages = MESSAGELOCAL.get();
        returnMessages.addAll(currentMessages);
        return returnMessages;
    }

    public static void cleanMessages() {
        MESSAGELOCAL.get().clear();
    }
}
