package org.ar.ar_audiomic.bean;

import android.view.View;

public class InfoBean {

    private String uid;
    private int delay;
    private int packet;
    private int position;
    private int volume;
    private View view;

    public InfoBean(String uid) {
        this.uid = uid;
    }

    public InfoBean(String uid, int position) {
        this.uid = uid;
        this.position = position;
    }

    public InfoBean(String uid,View view,int position) {
        this.uid = uid;
        this.view = view;
        this.position = position;
    }

    public InfoBean(String uid, int delay, int packet, int position) {
        this.uid = uid;
        this.delay = delay;
        this.packet = packet;
        this.position =position;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getPacket() {
        return packet;
    }

    public void setPacket(int packet) {
        this.packet = packet;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }
}
