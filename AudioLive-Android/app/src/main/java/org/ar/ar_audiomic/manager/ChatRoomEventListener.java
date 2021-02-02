package org.ar.ar_audiomic.manager;


import org.ar.ar_audiomic.bean.MessageListBean;
import org.ar.rtc.IRtcEngineEventHandler;

public interface ChatRoomEventListener {

    void onRtmConnectStateChange(int state, int reason);

    void onJoinChannelSuccess(String uid);

    void onNetStateChanges(IRtcEngineEventHandler.RtcStats stats);

    void onRemoteNetStateChanges(IRtcEngineEventHandler.RemoteAudioStats stats);

    void onApplyMicUpdated( String userId);

    void onRejectLineUpdated(String userId);

    void onAcceptLineUpdated(String userId);

    void onCancelApplyUpdated(String userId);

    void onAnchorExit(String userId);

    void onTokenPastDueExit();

    void onMusicUpdated(boolean isPlay,String name,String singer);

    void onAudioVolumeIndication(String userId, int volume);

    void onRequestToken();

    void onMemberJoined(String userId);

    void onMemberCountUpdate(int count);

    void onMemberListUpdated(String userId);

    void onUserLineChanged(String userId, Boolean isLine);

    void onMessageAdd(MessageListBean messageListBean);

    void onMemberLeft(String userId);

}
