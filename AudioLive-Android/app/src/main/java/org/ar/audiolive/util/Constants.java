package org.ar.audiolive.util;

public class Constants {

    public static final String SERVER = "http://arlive.agrtc.cn:12680/arapi/arlive/v1/%1$s";
//    public static final String SERVER = "http://pro.a/rlive.agrtc.cn:12680/arapi/arlive/v1/%1$s";
    //public static final String SERVER = "http://192.168.199.140:12680/arapi/arlive/v1/%1$s";

    public static final String CONTENT_TYPE = "Content-Type";

    public static final String AUTHORIZATION  = "Authorization";

    public static final String UID = "uid";

    public static final String CMD ="cmd";

    public static final String USER_NAME = "userName";
    public static final String USER_AVATAR = "avatar";

    public static final String SEND_MSG = "msg";
    public static final String SEND_CONTENT = "content";

    public static final String ANCHOR_UID = "anchorUid";
    public static final String ANCHOR_NAME = "anchorName";

    public static final String INTENT_IS_ANCHOR = "isAnchor";
    public static final String INTENT_ROOM_ID = "roomId";
    public static final String INTENT_ROOM_NAME = "roomName";
    public static final String INTENT_RTMP_URL = "rtmpUrl";
    public static final String ROOM_TYPE = "roomType";

    public static final int R_TYPE_RTC = 1;
    public static final int R_TYPE_CLIENT = 2;
    public static final int R_TYPE_SERVER = 3;

    public static final String MIC_APPLY ="apply";
    public static final String MIC_REJECT_LINE ="rejectLine";
    public static final String MIC_ACCEPT_LINE ="acceptLine";
    public static final String MIC_CANCEL_APPLY ="cancelApply";

    public static final String EXIT ="exit";
    public static final String JOIN ="join";

    public static final String TOKEN_PAST_DUE ="tokenPastDue";

    public static final String MUSIC_PLAYING ="playing";
    public static final String MUSIC_NAME ="musicName";
    public static final String MUSIC_PAUSE ="pause";
    public static final String MUSIC_STOP ="normal";

    public static boolean isEffectOpen;

    public static final int MUSIC_STATE_STOP = 0;
    public static final int MUSIC_STATE_PLAY = 1;
    public static final int MUSIC_STATE_PAUSE = 2;

}
