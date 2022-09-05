package org.ar.audiolive.bean;

import java.util.List;

public class JoinRoomBean {

    /**
     *{
     * 	"code": 0,
     * 	"msg": "success.",
     * 	"data": {
     * 		"room": {
     * 			"roomName": "打家劫舍",
     * 			"roomId": "503873",
     * 			"rType": 1,
     * 			"userNum": 1,
     * 			"state": 1,
     * 			"imageUrl": "https://oss.agrtc.cn/oss/fdfs/bb2d9380387aff6b1279d3860b96cdbc.jpg",
     * 			"isPrivate": 2,
     * 			"roomPwd": "",
     * 			"musicState": 0,
     * 			"ower": {
     * 				"uid": "503873",
     * 				"userName": "YAL-AL00"
     *                        },
     * 			"music": {
     * 				"musicId": 0,
     * 				"musicName": "",
     * 				"singer": "",
     * 				"musicUrl": ""
     *            },
     * 			"rtcToken": "0063ce71e86625992135b10638cb7ada976IADi2nFcW9hqfVcvsqo4JD3O8jbjf9tsWJxiD89GbeI2VX6tZaP/F6OKIgBJ5+MC8/BSYAQAAQDLoVFgAgDLoVFgAwDLoVFgBADLoVFg",
     * 			"rtmToken": "0063ce71e86625992135b10638cb7ada976IAA8fiyBX6Af+T0xh7NHKDr6uGY/D94N6dpsWbN2Pt8Cof8Xo4oAAAAAEAD524YE8/BSYAEA6APLoVFg"* 		},
     * 		"users": [],
     * 		"pullM3U8Url": "http://pull.ali.teameeting.cn/anyrtc/503873.m3u8?auth_key=1615962571-0-0-b8bea3859ae460048fd6edb6efab436b",
     * 		"pullRtmpUrl": "rtmp://pull.ali.teameeting.cn/anyrtc/503873?auth_key=1615962571-0-0-3b813b54a5fc94d0cbe627884dc58c46"
     *    }
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
        private RoomBeans room;
        private List<UserBean> users;
        private String pullM3U8Url;
        private String pullRtmpUrl;

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

        public RoomBeans getRoom() {
            return room;
        }

        public void setRoom(RoomBeans room) {
            this.room = room;
        }

        public List<UserBean> getUsers() {
            return users;
        }

        public void setUsers(List<UserBean> users) {
            this.users = users;
        }

        public static class RoomBeans {
            private String roomName;
            private String roomId;
            private int rType;
            private int userNum;
            private int state;
            private String imageUrl;
            private int isPrivate;
            private String roomPwd;
            private int musicState;
            private OwerBean ower;
            private String rtcToken;
            private String rtmToken;
            private MusicBean music;

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

            public int getrType() {
                return rType;
            }

            public void setrType(int rType) {
                this.rType = rType;
            }

            public int getUserNum() {
                return userNum;
            }

            public void setUserNum(int userNum) {
                this.userNum = userNum;
            }

            public int getState() {
                return state;
            }

            public void setState(int state) {
                this.state = state;
            }

            public String getImageUrl() {
                return imageUrl;
            }

            public void setImageUrl(String imageUrl) {
                this.imageUrl = imageUrl;
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

            public int getMusicState() {
                return musicState;
            }

            public void setMusicState(int musicState) {
                this.musicState = musicState;
            }

            public OwerBean getOwer() {
                return ower;
            }

            public void setOwer(OwerBean ower) {
                this.ower = ower;
            }

            public String getRtcToken() {
                return rtcToken;
            }

            public void setRtcToken(String rtcToken) {
                this.rtcToken = rtcToken;
            }

            public String getRtmToken() {
                return rtmToken;
            }

            public void setRtmToken(String rtmToken) {
                this.rtmToken = rtmToken;
            }

            public MusicBean getMusic() {
                return music;
            }

            public void setMusic(MusicBean music) {
                this.music = music;
            }

            public static class OwerBean {
                private String uid;
                private String userName;

                public String getUid() {
                    return uid;
                }

                public void setUid(String uid) {
                    this.uid = uid;
                }

                public String getUserName() {
                    return userName;
                }

                public void setUserName(String userName) {
                    this.userName = userName;
                }
            }

            public static class MusicBean {
                private int musicId;
                private String musicName;
                private String singer;
                private String musicUrl;

                public int getMusicId() {
                    return musicId;
                }

                public void setMusicId(int musicId) {
                    this.musicId = musicId;
                }

                public String getMusicName() {
                    return musicName;
                }

                public void setMusicName(String musicName) {
                    this.musicName = musicName;
                }

                public String getSinger() {
                    return singer;
                }

                public void setSinger(String singer) {
                    this.singer = singer;
                }

                public String getMusicUrl() {
                    return musicUrl;
                }

                public void setMusicUrl(String musicUrl) {
                    this.musicUrl = musicUrl;
                }
            }
        }
        public static class UserBean {
            private String avatar;
            private int enableAudio;
            private int enableChat;
            private int enableVideo;
            private int role;
            private String uid;
            private String userName;

            public String getAvatar() {
                return avatar;
            }

            public void setAvatar(String avatar) {
                this.avatar = avatar;
            }

            public int getEnableAudio() {
                return enableAudio;
            }

            public void setEnableAudio(int enableAudio) {
                this.enableAudio = enableAudio;
            }

            public int getEnableChat() {
                return enableChat;
            }

            public void setEnableChat(int enableChat) {
                this.enableChat = enableChat;
            }

            public int getEnableVideo() {
                return enableVideo;
            }

            public void setEnableVideo(int enableVideo) {
                this.enableVideo = enableVideo;
            }

            public int getRole() {
                return role;
            }

            public void setRole(int role) {
                this.role = role;
            }

            public String getUid() {
                return uid;
            }

            public void setUid(String uid) {
                this.uid = uid;
            }

            public String getUserName() {
                return userName;
            }

            public void setUserName(String userName) {
                this.userName = userName;
            }
        }
    }
}
