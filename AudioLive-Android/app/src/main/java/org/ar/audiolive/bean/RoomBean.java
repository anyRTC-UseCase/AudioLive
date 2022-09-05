package org.ar.audiolive.bean;

public class RoomBean {

    private String imgUrl;
    private String roomName;
    private int roomNum;
    private String roomId;

    public RoomBean(String imgUrl, String roomName, int roomNum, String roomId) {
        this.imgUrl = imgUrl;
        this.roomName = roomName;
        this.roomNum = roomNum;
        this.roomId = roomId;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public int getRoomNum() {
        return roomNum;
    }

    public void setRoomNum(int roomNum) {
        this.roomNum = roomNum;
    }


    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
