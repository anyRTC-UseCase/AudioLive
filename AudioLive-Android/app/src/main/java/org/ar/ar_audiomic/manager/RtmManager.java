package org.ar.ar_audiomic.manager;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.ar.rtm.ChannelAttributeOptions;
import org.ar.rtm.ErrorInfo;
import org.ar.rtm.ResultCallback;
import org.ar.rtm.RtmChannel;
import org.ar.rtm.RtmChannelAttribute;
import org.ar.rtm.RtmChannelListener;
import org.ar.rtm.RtmChannelMember;
import org.ar.rtm.RtmClient;
import org.ar.rtm.RtmClientListener;
import org.ar.rtm.RtmMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public final class RtmManager {

    private static final String TAG =RtmManager.class.getSimpleName();

    private Context mContext;
    private RtmClient mRtmClient;
    private static RtmManager rtmInstance;
    private RtmChannel mRtmChannel;
    private boolean mIsLogin;
    private RtmEventListener mListener;

    public interface RtmEventListener {

        void onRtmConnectStateChange(int state,int reason);

        void onInitMembers(List<RtmChannelMember> members);

        void onMemberCount(int count);

        void onMemberJoined(String userId);

        void onMemberLeft(String userId);

        void onMessageReceived(RtmMessage message,String userId);

        void onChannelMessageReceived(RtmMessage message,String userId);
    }

    private RtmManager(Context context){
        mContext =context.getApplicationContext();
    }

    public static RtmManager getInstance(Context context){
        if (rtmInstance ==null){
            synchronized (RtmManager.class){
                if (rtmInstance ==null){
                    rtmInstance =new RtmManager(context);
                }
            }
        }
        return rtmInstance;
    }

    public void setListener(RtmEventListener listener){
        mListener =listener;
    }

    public void init(String appId){
        if (mRtmClient ==null){
            try {
                mRtmClient =RtmClient.createInstance(mContext, appId,mClientListener);
                //priCloud();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //rtm私有云
    private void priCloud(){
        JSONObject jsonObject =new JSONObject();
        try {
            jsonObject.put("Cmd","ConfPriCloudAddr");
            jsonObject.put("ServerAdd","pro.rtmgw.agrtc.cn");
            jsonObject.put("Port",7080);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mRtmClient.setParameters(jsonObject.toString());
    }

    public void login(String rtmToken,String userId, final ResultCallback<Void> callback){
        if (mRtmClient !=null){
            if (mIsLogin) {
                if (callback != null)
                    callback.onSuccess(null);
                return;
            }
            mRtmClient.login(rtmToken, String.valueOf(userId), new ResultCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "rtm login success");
                    mIsLogin = true;
                    if (callback != null)
                        callback.onSuccess(aVoid);
                }

                @Override
                public void onFailure(ErrorInfo errorInfo) {
                    Log.e(TAG, String.format("rtm join %s", errorInfo.getErrorDescription()));
                    mIsLogin = false;
                    if (callback != null)
                        callback.onFailure(errorInfo);
                }
            });
        }
    }

    public void joinChannel(final String channelId, ResultCallback<Void> callback) {
        if (mRtmClient != null) {
            leaveChannel();
            Log.w(TAG, String.format("joinChannel %s", channelId));
            try {
                final RtmChannel rtmChannel = mRtmClient.createChannel(channelId, mChannelListener);
                if (rtmChannel == null) return;
                rtmChannel.join(new ResultCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "rtm join success");
                        mRtmChannel = rtmChannel;
                        getMembers();
                    }

                    @Override
                    public void onFailure(ErrorInfo errorInfo) {
                        Log.e(TAG, String.format("rtm join %s", errorInfo.getErrorDescription()));
                        //AlertUtil.showToast("RTM login failed, see the log to get more info");
                        mRtmChannel = rtmChannel;
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void getMembers() {
        if (mRtmChannel != null) {
            mRtmChannel.getMembers(new ResultCallback<List<RtmChannelMember>>() {
                @Override
                public void onSuccess(List<RtmChannelMember> rtmChannelMembers) {
                    if (mListener != null)
                        mListener.onInitMembers(rtmChannelMembers);
                }

                @Override
                public void onFailure(ErrorInfo errorInfo) {

                }
            });
        }
    }


    public RtmClient getRtmClient() {
        return mRtmClient;
    }


    private ChannelAttributeOptions options() {
        return new ChannelAttributeOptions(true);
    }

    public RtmChannel getRtmChannel() {
        return mRtmChannel;
    }

    public void sendChannelMessage(final String content, final ResultCallback<Void> callback) {
        if (mRtmClient != null) {
            RtmMessage message = mRtmClient.createMessage(content);
            if (mRtmChannel != null) {
                mRtmChannel.sendMessage(message, new ResultCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, String.format("sendChannelMessage %s", content));
                        if (callback != null)
                            callback.onSuccess(aVoid);
                    }

                    @Override
                    public void onFailure(ErrorInfo errorInfo) {
                        Log.e(TAG, String.format("sendChannelMessage %s", errorInfo.getErrorDescription()));

                        if (callback != null)
                            callback.onFailure(errorInfo);
                    }
                });
            }
        }
    }

    public void sendMessageToPeer(final String userId, final String content, final ResultCallback<Void> callback) {
        if (TextUtils.isEmpty(userId)) return;

        if (mRtmClient != null) {
            RtmMessage message = mRtmClient.createMessage(content);
            mRtmClient.sendMessageToPeer(userId, message, null, new ResultCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, String.format("sendMessageToPeer %s %s", userId, content));

                    if (callback != null)
                        callback.onSuccess(aVoid);
                }

                @Override
                public void onFailure(ErrorInfo errorInfo) {
                    Log.e(TAG, String.format("sendMessageToPeer %s", errorInfo.getErrorDescription()));

                    if (callback != null)
                        callback.onFailure(errorInfo);
                }
            });
        }
    }

    public void leaveChannel() {
        if (mRtmChannel != null) {
            Log.w(TAG, String.format("leaveChannel %s", mRtmChannel.getId()));
            mRtmChannel.leave(null);
            mRtmChannel.release();
            mRtmChannel = null;
        }
    }

    private RtmClientListener mClientListener =new RtmClientListener() {

        @Override
        public void onConnectionStateChanged(int var1, int var2) {
            if (mListener!=null){
                mListener.onRtmConnectStateChange(var1,var2);
            }
        }

        @Override
        public void onMessageReceived(RtmMessage rtmMessage, String uid) {
            if (mListener != null)
                mListener.onMessageReceived(rtmMessage,uid);
        }

        @Override
        public void onTokenExpired() {

        }


        @Override
        public void onPeersOnlineStatusChanged(Map<String, Integer> var1) {

        }
    };

    private RtmChannelListener mChannelListener =new RtmChannelListener() {
        @Override
        public void onMemberCountUpdated(int var1) {
            //getMembers();
            if (mListener !=null){
                mListener.onMemberCount(var1);
            }
       }

        @Override
        public void onAttributesUpdated(List<RtmChannelAttribute> var1) {

        }

        @Override
        public void onMessageReceived(RtmMessage rtmMessage, RtmChannelMember rtmChannelMember) {
            if (mListener != null){
                mListener.onChannelMessageReceived(rtmMessage,rtmChannelMember.getUserId());
            }
        }

        @Override
        public void onMemberJoined(RtmChannelMember var1) {
            if (mListener !=null){
                mListener.onMemberJoined(var1.getUserId());
            }
        }

        @Override
        public void onMemberLeft(RtmChannelMember var1) {
            if (mListener !=null){
                mListener.onMemberLeft(var1.getUserId());
            }
        }
    };

}
