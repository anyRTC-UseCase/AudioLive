package org.ar.ar_audiomic.bean;

import java.util.List;

public class RoomListBean {

    /**
     * {
     * 	"code": 0,
     * 	"msg": "success.",
     * 	"data": {
     * 		"list": [{
     * 			"roomName": "ccgg",
     * 			"roomId": "123685",
     * 			"imageUrl": "https://oss.agrtc.cn/oss/fdfs/2bc76e6e6a6b815b11f1686ceee46db1.jpg",
     * 			"userNum": 0,
     * 			"ownerUid": "41632331",
     * 			"isPrivate": 2,
     * 			"roomPwd": "",
     * 			"id": 13
     *                }, {
     * 			"roomName": "dvdbd ",
     * 			"roomId": "147300",
     * 			"imageUrl": "https://oss.agrtc.cn/oss/fdfs/6cd4be46fcb9c1e348ecd8f58708fb45.jpg",
     * 			"userNum": 0,
     * 			"ownerUid": "94503140",
     * 			"isPrivate": 2,
     * 			"roomPwd": "",
     * 			"id": 12
     *        }, {
     * 			"roomName": "dsdd",
     * 			"roomId": "793720",
     * 			"imageUrl": "https://oss.agrtc.cn/oss/fdfs/b976137bbd724e36d600042680bdd99c.jpeg",
     * 			"userNum": 0,
     * 			"ownerUid": "94503140",
     * 			"isPrivate": 2,
     * 			"roomPwd": "",
     * 			"id": 11
     *        }, {
     * 			"roomName": "xxx",
     * 			"roomId": "182897",
     * 			"imageUrl": "https://oss.agrtc.cn/oss/fdfs/2bc76e6e6a6b815b11f1686ceee46db1.jpg",
     * 			"userNum": 0,
     * 			"ownerUid": "94503140",
     * 			"isPrivate": 2,
     * 			"roomPwd": "",
     * 			"id": 10
     *        }, {
     * 			"roomName": "ccc",
     * 			"roomId": "911224",
     * 			"imageUrl": "https://oss.agrtc.cn/oss/fdfs/7edcb6ede66c4beb581f5c2a1e833779.jpg",
     * 			"userNum": 0,
     * 			"ownerUid": "94503140",
     * 			"isPrivate": 2,
     * 			"roomPwd": "",
     * 			"id": 9
     *        }, {
     * 			"roomName": "djdjd",
     * 			"roomId": "sTKwDQ83uMgxxuA9",
     * 			"imageUrl": "https://oss.agrtc.cn/oss/fdfs/c12de8f1d4da60466bd6588acf1cff07.jpg",
     * 			"userNum": 0,
     * 			"ownerUid": "51312358",
     * 			"isPrivate": 2,
     * 			"roomPwd": "",
     * 			"id": 8
     *        }],
     * 		"haveNext": 0,
     * 		"nextId": 0* 	}
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
        private int nextId;
        private List<ListBean> list;
        public int getHaveNext() {
            return haveNext;
        }

        public void setHaveNext(int haveNext) {
            this.haveNext = haveNext;
        }

        public int getNextId() {
            return nextId;
        }

        public void setNextId(int nextId) {
            this.nextId = nextId;
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
            private String imageUrl;
            private int userNum;
            private String ownerUid;
            private String pullM3U8Url;
            private String pullRtmpUrl;
            private int isPrivate;
            private String roomPwd;
            private int id ;

            public String getPullM3U8Url() {
                return pullM3U8Url;
            }

            public void setPullM3U8Url(String pullM3U8Url) {
                this.pullM3U8Url = pullM3U8Url;
            }

            public String getPullRtmpUrl() {
                return pullRtmpUrl;
            }

            public void setPullRtmpUrl(String pullRtmpUrl) {
                this.pullRtmpUrl = pullRtmpUrl;
            }

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

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }
        }
    }
}
