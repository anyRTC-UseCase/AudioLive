package org.ar.audiolive.manager;


import org.ar.audiolive.bean.MessageListBean;
import org.ar.rtc.IRtcEngineEventHandler;

public interface ChatRoomEventListener {

    void onJoinRtmSuccess();

    void onRtmConnectStateChange(int state, int reason);

    void onJoinChannelSuccess(String uid);

    void onNetStateChanges(IRtcEngineEventHandler.RtcStats stats);

    void onRemoteNetStateChanges(IRtcEngineEventHandler.RemoteAudioStats stats);

    void onApplyMicUpdated(String userId);

    void onRejectLineUpdated(String userId);

    void onAcceptLineUpdated(String userId);

    void onCancelApplyUpdated(String userId);

    void onAnchorExit(String userId);

    void onTokenPastDueExit();

    void onMusicUpdated(int state ,String name);

    void onAudioVolumeIndication(String userId, int volume);

    void onRequestToken();

    void onAnchorStateChanged(int state);

    void onMemberJoined(String userId);

    void onUserLineChanged(String userId, Boolean isLine);

    void onMessageAdd(MessageListBean messageListBean);

    void onMemberLeft(String userId);

}
