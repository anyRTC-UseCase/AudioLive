package org.ar.audiolive.model;

public class MusicItemBean {

    private String musicName;
    private int state;

    public MusicItemBean(String musicName) {
        this.musicName = musicName;
    }

    public MusicItemBean(String musicName, int state) {
        this.musicName = musicName;
        this.state = state;
    }

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
