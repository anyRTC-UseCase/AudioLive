package org.ar.audiolive.bean;

import java.util.List;

public class RoomListBean {

    /**
     * {
     * 	"code": 0,
     * 	"msg": "success.",
     * 	"data": {
     * 		"list": [{
     * 			"roomName": "5464",
     * 			"roomId": "978079",
     * 			"roomType": "1",
     * 			"imageUrl": "https://oss.agrtc.cn/oss/fdfs/bb2d9380387aff6b1279d3860b96cdbc.jpg",
     * 			"userNum": 0,
     * 			"ownerUid": "88449957",
     * 			"isPrivate": 2,
     * 			"roomPwd": ""
     *                }, {
     * 			"roomName": "3464",
     * 			"roomId": "470198",
     * 			"roomType": "1",
     * 			"imageUrl": "https://oss.agrtc.cn/oss/fdfs/d490b7896857482244d90913bc3509ab.jpeg",
     * 			"userNum": 0,
     * 			"ownerUid": "88449957",
     * 			"isPrivate": 2,
     * 			"roomPwd": ""
     *        }, {
     * 			"roomName": "6464",
     * 			"roomId": "808262",
     * 			"roomType": "1",
     * 			"imageUrl": "https://oss.agrtc.cn/oss/fdfs/72eaa974eee039cb79d4f9eebf22c38b.jpg",
     * 			"userNum": 0,
     * 			"ownerUid": "88449957",
     * 			"isPrivate": 2,
     * 			"roomPwd": ""
     *        }],
     * 		"haveNext": 0,
     * 		"totalPageNum": 1* 	}
     * }
     */

    private int code;
    private String msg;
    private DataBean data;


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        private int haveNext;
        private int totalPageNum;
        private List<ListBean> list;
        public int getHaveNext() {
            return haveNext;
        }

        public void setHaveNext(int haveNext) {
            this.haveNext = haveNext;
        }

        public int getTotalPageNum() {
            return totalPageNum;
        }

        public void setTotalPageNum(int nextId) {
            this.totalPageNum = nextId;
        }

        public List<ListBean> getList() {
            return list;
        }

        public void setList(List<ListBean> list) {
            this.list = list;
        }

        public static class ListBean {

            private String roomName;
            private String roomId;
            private String roomType;
            private String imageUrl;
            private int userNum;
            private String ownerUid;
            private int isPrivate;
            private String roomPwd;

            public String getRoomName() {
                return roomName;
            }

            public void setRoomName(String roomName) {
                this.roomName = roomName;
            }

            public String getRoomId() {
                return roomId;
            }

            public void setRoomId(String roomId) {
                this.roomId = roomId;
            }

            public String getRoomType() {
                return roomType;
            }

            public void setRoomType(String roomType) {
                this.roomType = roomType;
            }

            public String getImageUrl() {
                return imageUrl;
            }

            public void setImageUrl(String imageUrl) {
                this.imageUrl = imageUrl;
            }

            public int getUserNum() {
                return userNum;
            }

            public void setUserNum(int userNum) {
                this.userNum = userNum;
            }

            public String getOwnerUid() {
                return ownerUid;
            }

            public void setOwnerUid(String ownerUid) {
                this.ownerUid = ownerUid;
            }

            public int getIsPrivate() {
                return isPrivate;
            }

            public void setIsPrivate(int isPrivate) {
                this.isPrivate = isPrivate;
            }

            public String getRoomPwd() {
                return roomPwd;
            }

            public void setRoomPwd(String roomPwd) {
                this.roomPwd = roomPwd;
            }
        }
    }
}
