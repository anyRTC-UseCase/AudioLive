package org.ar.ar_audiomic.bean;

import java.util.List;

public class JoinUserBean {
    /**
     * {
     * 	"code": 0,
     * 	"msg": "success.",
     * 	"data": {
     * 		"total": 2147483647,
     * 		"count": 1,
     * 		"haveNext": 0,
     * 		"nextId": 0,
     * 		"list": [{
     * 			"id": 262,
     * 			"uid": "45499492",
     * 			"role": 0,
     * 			"enableAudio": 0,
     * 			"enableVideo": 0,
     * 			"enableChat": 0,
     * 			"userName": "YAL-AL00",
     * 			"avatar": "https://oss.agrtc.cn/oss/fdfs/36fc7b27ec0cc00abdbae8825e4f03fb.jpg"
     *                }]* 	}
     * }
     */

    private String code;
    private String msg;
    private DataBean data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
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
        private int total;
        private int count;
        private int haveNext;
        private int nextId;
        private List<UsersBean> list;

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

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

        public List<UsersBean> getList() {
            return list;
        }

        public void setList(List<UsersBean> list) {
            this.list = list;
        }

        public static class UsersBean {
            private int id;
            private String uid;
            private int role;
            private int enableAudio;
            private int enableVideo;
            private int enableChat;
            private String userName;
            private String avatar;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getUid() {
                return uid;
            }

            public void setUid(String uid) {
                this.uid = uid;
            }

            public int getRole() {
                return role;
            }

            public void setRole(int role) {
                this.role = role;
            }

            public int getEnableAudio() {
                return enableAudio;
            }

            public void setEnableAudio(int enableAudio) {
                this.enableAudio = enableAudio;
            }

            public int getEnableVideo() {
                return enableVideo;
            }

            public void setEnableVideo(int enableVideo) {
                this.enableVideo = enableVideo;
            }

            public int getEnableChat() {
                return enableChat;
            }

            public void setEnableChat(int enableChat) {
                this.enableChat = enableChat;
            }

            public String getUserName() {
                return userName;
            }

            public void setUserName(String userName) {
                this.userName = userName;
            }

            public String getAvatar() {
                return avatar;
            }

            public void setAvatar(String avatar) {
                this.avatar = avatar;
            }
        }
    }
}
