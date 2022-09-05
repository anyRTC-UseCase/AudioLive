package org.ar.audiolive.bean;

import java.io.Serializable;

public class LogBean implements Serializable {

    private int imgRes;
    private String times;
    private String textLog;

    public LogBean(int imgRes, String times, String textLog) {
        this.imgRes = imgRes;
        this.times = times;
        this.textLog = textLog;
    }

    public int getImgRes() {
        return imgRes;
    }

    public void setImgRes(int imgRes) {
        this.imgRes = imgRes;
    }

    public String getTimes() {
        return times;
    }

    public void setTimes(String times) {
        this.times = times;
    }

    public String getTextLog() {
        return textLog;
    }

    public void setTextLog(String textLog) {
        this.textLog = textLog;
    }
}
