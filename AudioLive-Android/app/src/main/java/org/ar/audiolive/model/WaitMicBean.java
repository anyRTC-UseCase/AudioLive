package org.ar.audiolive.model;

public class WaitMicBean {

    public String waitAvatar;
    public String waitName;
    private String userId;

    public WaitMicBean(String userId, String waitAvatar, String waitName) {
        this.waitAvatar = waitAvatar;
        this.waitName = waitName;
        this.userId=userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getWaitAvatar() {
        return waitAvatar;
    }

    public void setWaitAvatar(String waitAvatar) {
        this.waitAvatar = waitAvatar;
    }

    public String getWaitName() {
        return waitName;
    }

    public void setWaitName(String waitName) {
        this.waitName = waitName;
    }
}
