/*
* Created on :2016年12月14日
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 www.ibmall.cn All right reserved.
*/
package cn.wuxia.qiyukf.openapi.session.model;

public class DuplicateRemovalMessage {

    private String msgId;

    private String uid;

    private String timeStamp;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((timeStamp == null) ? 0 : timeStamp.hashCode());
        result = prime * result + ((uid == null) ? 0 : uid.hashCode());
        result = prime * result + ((msgId == null) ? 0 : msgId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DuplicateRemovalMessage other = (DuplicateRemovalMessage) obj;
        if (timeStamp == null) {
            if (other.timeStamp != null)
                return false;
        } else if (!timeStamp.equals(other.timeStamp))
            return false;
        if (uid == null) {
            if (other.uid != null)
                return false;
        } else if (!uid.equals(other.uid))
            return false;
        if (msgId == null) {
            if (other.msgId != null)
                return false;
        } else if (!msgId.equals(other.msgId))
            return false;
        return true;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DuplicateRemovalMessage [msgId=");
        builder.append(msgId);
        builder.append(", uid=");
        builder.append(uid);
        builder.append(", timeStamp=");
        builder.append(timeStamp);
        builder.append("]");
        return builder.toString();
    }

}
