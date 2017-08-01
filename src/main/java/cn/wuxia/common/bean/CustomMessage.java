package cn.wuxia.common.bean;

import cn.wuxia.common.spring.support.Msg.CustomMessageTypeEnum;

/**
 * 自定义消息
 * @author PL
 */
public class CustomMessage {
    private String key;

    private String[] args;

    private CustomMessageTypeEnum type;

    private Boolean translate;

    public CustomMessage(String key, CustomMessageTypeEnum type, String... args) {
        this.key = key;
        this.type = type;
        this.args = args;
        this.translate = true;
    }

    public CustomMessage(String key, CustomMessageTypeEnum type, Boolean translate, String... args) {
        this.key = key;
        this.type = type;
        this.args = args;
        this.translate = translate;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public CustomMessageTypeEnum getType() {
        return type;
    }

    public void setType(CustomMessageTypeEnum type) {
        this.type = type;
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public Boolean getTranslate() {
        translate = translate == null ? true : translate;
        return translate;
    }

    public void setTranslate(Boolean translate) {
        this.translate = translate;
    }

}
