package org.ar.audiolive.manager;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.ar.audiolive.util.SpUtil;
import org.ar.rtc.Constants;
import org.ar.rtc.IRtcEngineEventHandler;
import org.ar.rtc.RtcEngine;
import org.ar.rtc.live.LiveTranscoding;
import org.ar.rtc.rtmp.StreamingKit;
import org.ar.rtc.rtmp.internal.PushMode;
import org.json.JSONException;
import org.json.JSONObject;

public final class RtcManager {

    public interface RtcEventListener {
        void onJoinChannelSuccess(String uid);

        void onUserOnlineStateChanged(String uid, boolean isOnline);

        void onNetStateChanges(IRtcEngineEventHandler.RtcStats stats);

        void onAudioVolumeIndication(String uid, int volume);

        void onRequestToken();

        void onRemoteNetStateChanges(IRtcEngineEventHandler.RemoteAudioStats stats);
    }

    private final String TAG = RtcManager.class.getSimpleName();
    private RtcEventListener mListener;
    private static RtcManager instance;
    private final Context mContext;
    private RtcEngine mRtcEngine;
    private StreamingKit mStreamingKit;
    private String mUserId;

    public RtcManager(Context context){
        mContext =context.getApplicationContext();
    }

    public static RtcManager getInstance(Context context) {
        if (instance == null) {
            synchronized (RtcManager.class) {
                if (instance == null)
                    instance = new RtcManager(context);
            }
        }
        return instance;
    }

    public RtcEngine getRtcEngine() {
        return mRtcEngine;
    }

    public void setListener(RtcEventListener listener) {
        mListener = listener;
    }

    public void init(String appId){
        if (mRtcEngine ==null){
            mRtcEngine =RtcEngine.create(mContext, appId,mEventHandler);
//            priCloud();
        }
        if (mRtcEngine != null) {
            mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
            //mRtcEngine.setAudioProfile(Constants.AUDIO_PROFILE_MUSIC_STANDARD, Constants.AUDIO_SCENARIO_CHATROOM_ENTERTAINMENT);
            mRtcEngine.setAudioProfile(Constants.AUDIO_PROFILE_MUSIC_STANDARD_STEREO,Constants.AUDIO_SCENARIO_GAME_STREAMING);
            mRtcEngine.enableAudioVolumeIndication(500, 3, true);
            mRtcEngine.setEnableSpeakerphone(true);
        }
    }

    //rtc私有云
    private void priCloud(){
        JSONObject jsonObject =new JSONObject();
        try {
            jsonObject.put("Cmd","ConfPriCloudAddr");
            jsonObject.put("ServerAdd","pro.gateway.agrtc.cn");
            jsonObject.put("Port",6080);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mRtcEngine.setParameters(jsonObject.toString());
    }

    public void joinChannel(String rtcToken,String channelId, String userId,int roomType,boolean isAnchor) {
        Log.i(TAG, "joinChannel:  mRtcEngine ="+mRtcEngine);
        if (mRtcEngine != null){
            if (roomType== org.ar.audiolive.util.Constants.R_TYPE_RTC && !isAnchor){
                mRtcEngine.setClientRole(Constants.CLIENT_ROLE_AUDIENCE);
            }else {
                mRtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
            }
            mRtcEngine.joinChannel(rtcToken, channelId, null, userId);
            mRtcEngine.muteLocalAudioStream(false);
            //mRtcEngine.setEnableSpeakerphone(true);
            if (roomType== org.ar.audiolive.util.Constants.R_TYPE_CLIENT){
                createStreamingInstance();
            }else if(roomType== org.ar.audiolive.util.Constants.R_TYPE_SERVER){
                createStreamingInstance();
            }

        }
    }

    public void setClientRole(int role) {
        if (mRtcEngine != null)
            mRtcEngine.setClientRole(role);
    }

    public void enableInEarMonitoring(boolean enabled){
        if (mRtcEngine !=null){
            mRtcEngine.enableInEarMonitoring(enabled);
        }
    }

    public void setInEarMonitoringVolume(int i){
        if (mRtcEngine !=null){
            mRtcEngine.setInEarMonitoringVolume(i);
        }
    }

    public void muteLocalAudioStream(boolean muted) {
        if (mRtcEngine != null)
            mRtcEngine.muteLocalAudioStream(muted);
    }

    public void stopAllEffects(){
        if (mRtcEngine !=null){
            mRtcEngine.getAudioEffectManager().stopAllEffects();
        }
    }

    public void playEffect(int soundId,String path){
        if (mRtcEngine !=null){
            mRtcEngine.getAudioEffectManager().playEffect(soundId,path,0,1.0,1.0,100.0,true);
        }
    }

    public int startAudioMixing(String filePath) {
        if (mRtcEngine != null) {
            return mRtcEngine.startAudioMixing(filePath, false, false, -1);
        }
        return -1;
    }

    public void pauseAudioMixing() {
        if (mRtcEngine != null) {
            mRtcEngine.pauseAudioMixing();
        }
    }

    public void resumeAudioMixing() {
        if (mRtcEngine != null) {
            mRtcEngine.resumeAudioMixing();
        }
    }

    public void stopAudioMixing() {
        if (mRtcEngine != null)
            mRtcEngine.stopAudioMixing();
    }

    public void adjustAudioMixingVolume(int volume) {
        if (mRtcEngine != null){
            Log.i(TAG, "adjustAudioMixingVolume: v ="+volume);
            mRtcEngine.adjustAudioMixingVolume(volume);
            //mRtcEngine.adjustAudioMixingPlayoutVolume(volume);
        }
    }

    public void adjustRecordingSignalVolume(int i){
        if (mRtcEngine !=null){
            mRtcEngine.adjustRecordingSignalVolume(i);
        }
    }

    public void leaveChannel() {
        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel();
            setClientRole(Constants.CLIENT_ROLE_AUDIENCE);
        }
    }

    private IRtcEngineEventHandler mEventHandler = new IRtcEngineEventHandler() {

        @Override
        public void onJoinChannelSuccess(String channel, String uid, int elapsed) {
            super.onJoinChannelSuccess(channel, uid, elapsed);
            Log.i(TAG, "onJoinChannelSuccess: mListener ="+mListener);
            mUserId = uid;
            if (mListener != null)
                mListener.onJoinChannelSuccess(uid);
        }

        @Override
        public void onRejoinChannelSuccess(String channel, String uid, int elapsed) {
            super.onRejoinChannelSuccess(channel, uid, elapsed);
            Log.i(TAG, "onRejoinChannelSuccess: ======>");
        }

        @Override
        public void onRtcStats(RtcStats stats) {
            super.onRtcStats(stats);
            if (mListener !=null){
                mListener.onNetStateChanges(stats);
            }
        }

        @Override
        public void onRequestToken() {
            super.onRequestToken();
            Log.i(TAG, "onRequestToken: --->!!!");
            if (mListener!=null){
                mListener.onRequestToken();
            }
        }

        @Override
        public void onRemoteAudioStats(RemoteAudioStats stats) {
            super.onRemoteAudioStats(stats);
            if (mListener !=null){
                mListener.onRemoteNetStateChanges(stats);
            }
        }

        @Override
        public void onAudioVolumeIndication(AudioVolumeInfo[] speakers, int totalVolume) {
            super.onAudioVolumeIndication(speakers, totalVolume);
            for (AudioVolumeInfo info : speakers) {
                Log.i(TAG, "onAudioVolumeIndication: uid ="+info.uid);
                if (info.volume > 0) {
                    String uid =null;
                    if (TextUtils.isEmpty(info.uid)){
                        uid =mUserId;
                    }else {
                        if (TextUtils.equals(info.uid,"0")){
                            uid = SpUtil.getString(org.ar.audiolive.util.Constants.UID);
                        }else {
                            uid = info.uid;
                        }
                    }
                    if (mListener != null)
                        mListener.onAudioVolumeIndication(uid, info.volume);
                }
            }
        }

        @Override
        public void onUserJoined(String uid, int elapsed) {
            super.onUserJoined(uid, elapsed);
            Log.i(TAG, "onUserJoined: uid ="+uid);
                if (mListener != null)
                mListener.onUserOnlineStateChanged(uid, true);
        }

        @Override
        public void onUserOffline(String uid, int reason) {
            super.onUserOffline(uid, reason);
            if (mListener != null)
                mListener.onUserOnlineStateChanged(uid, false);
        }

        @Override
        public void onClientRoleChanged(int oldRole, int newRole) {
            super.onClientRoleChanged(oldRole, newRole);

        }

        @Override
        public void onStreamInjectedStatus(String url, String uid, int status) {
            super.onStreamInjectedStatus(url, uid, status);
            Log.i(TAG, "zhao onStreamInjectedStatus: url ="+url+",uid="+uid+",state ="+status);
        }

        @Override
        public void onRtmpStreamingStateChanged(String url, int state, int errCode) {
            super.onRtmpStreamingStateChanged(url, state, errCode);
            Log.i(TAG, " onRtmpStreamingStateChanged: url ="+url+",state="+state+",errCode ="+errCode);
        }

        @Override
        public void onTranscodingUpdated() {
            super.onTranscodingUpdated();
            Log.i(TAG, " onTranscodingUpdated: --->");
        }
    };

    public void addPublishStreamUrl(String pushUrl){
        LiveTranscoding liveTranscoding =new LiveTranscoding();
        LiveTranscoding.TranscodingUser  transcodingUser=new LiveTranscoding.TranscodingUser();
        transcodingUser.uid =SpUtil.getString(org.ar.audiolive.util.Constants.UID);
        liveTranscoding.addUser(transcodingUser);
        mRtcEngine.setLiveTranscoding(liveTranscoding);
        int ret=mRtcEngine.addPublishStreamUrl(pushUrl,true);
        Log.i(TAG, "addPublishStreamUrl: ret ="+ret);
    }

    public void removePublishStreamUrl(String pushUrl){
        mRtcEngine.removePublishStreamUrl(pushUrl);
    }

    public void createStreamingInstance(){
        mStreamingKit =StreamingKit.createInstance();
        Log.i(TAG, "pushStream: mStreamingKit ="+mStreamingKit +",mRtcEngine ="+mRtcEngine);
        if (mRtcEngine !=null){
            int ret =mStreamingKit.setRtcEngine(mRtcEngine);
            Log.i(TAG, "createStreamingInstance: ret ="+ret);
        }
    }

    public void pushStream(String pushUrl){
        if (mStreamingKit != null) {
            mStreamingKit.setMode(PushMode.AudMix);
            mStreamingKit.pushStream(pushUrl);
        }
    }

    public void unPushStream(){
        if (mStreamingKit != null) {
            mStreamingKit.unPushStream();
            mStreamingKit.release();
            mStreamingKit=null;
        }
    }
}
