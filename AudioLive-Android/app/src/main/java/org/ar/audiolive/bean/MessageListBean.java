package org.ar.audiolive.bean;

public class MessageListBean {

    public static final int MSG_SYSYTEM = 0;
    public static final int MSG_NORMAL = 1;
    public static final int MSG_JOIN_LEFT_ROOM =2;

    public int type;
    public String content;

    public MessageListBean(int type, String content) {
        this.type = type;
        this.content = content;
    }


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    @Override
    public String toString() {
        return "MessageListBean{" +
                "type=" + type +
                ", content='" + content + '\'' +
                '}';
    }
}
