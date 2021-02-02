package org.ar.ar_audiomic.manager;


import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import org.ar.ar_audiomic.bean.JoinRoomBean;
import org.ar.ar_audiomic.bean.JoinUserBean;
import org.ar.ar_audiomic.bean.MessageListBean;
import org.ar.ar_audiomic.model.ChannelData;
import org.ar.ar_audiomic.model.Member;
import org.ar.ar_audiomic.model.Message;
import org.ar.ar_audiomic.util.Constants;
import org.ar.ar_audiomic.util.SpUtil;
import org.ar.rtc.IRtcEngineEventHandler;
import org.ar.rtm.ErrorInfo;
import org.ar.rtm.ResultCallback;
import org.ar.rtm.RtmAttribute;
import org.ar.rtm.RtmChannelMember;
import org.ar.rtm.RtmMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public final class ChatRoomManager implements MessageManager {

    private final String TAG = ChatRoomManager.class.getSimpleName();
    private static ChatRoomManager instance;

    private ChannelData mChannelData = new ChannelData();

    private ARServerManager serverManager =ARServerManager.getInstance();

    private RtcManager mRtcManager;
    private RtmManager mRtmManager;
    private ChatRoomEventListener mListener;

    public CountDownLatch countDownLatch;


    private ChatRoomManager(Context context) {
        mRtcManager = RtcManager.getInstance(context);
        mRtcManager.setListener(mRtcListener);
        mRtmManager = RtmManager.getInstance(context);
        mRtmManager.setListener(mRtmListener);
        //new Thread(() -> {}).start();
        serverManager.setUpdateUserListener(joinUserBean -> {
            List<JoinUserBean.DataBean.UsersBean> userBeans =joinUserBean.getData().getList();
            for (int i = 0; i < userBeans.size(); i++) {
                mChannelData.addOrUpdateMember(new Member(userBeans.get(i).getUid(),userBeans.get(i).getUserName()));
            }
            if (countDownLatch !=null){
                countDownLatch.countDown();
            }
        });
    }

    public RtcManager getRtcManager() {
        return mRtcManager;
    }

    public RtmManager getRtmManager() {
        return mRtmManager;
    }

    public ChannelData getChannelData(){
        return mChannelData;
    }

    public void setListener(ChatRoomEventListener listener) {
        mListener = listener;
    }

    public static ChatRoomManager instance(Context context) {
        if (instance == null) {
            synchronized (ChatRoomManager.class) {
                if (instance == null)
                    instance = new ChatRoomManager(context);
            }
        }
        return instance;
    }

    public void login(String rtmToken,String userId){
        mRtmManager.login(rtmToken,userId,null);
    }

    public void joinRtcChannel(String rtcToken,String channelId) {
        mRtcManager.joinChannel(rtcToken,channelId, SpUtil.getString(Constants.UID));
    }

    public void joinRtmChannel(String channelId) {
        mRtmManager.joinChannel(channelId, null);
    }

    public void leaveRtcChannel() {
        mRtcManager.leaveChannel();
    }

    public void leaveRtmChannel() {
        mRtmManager.leaveChannel();
    }


    @Override
    public void sendOrder(String userId, String orderType, String content, ResultCallback<Void> callback) {
        Message message = new Message(orderType, content, SpUtil.getString(Constants.UID));
        mRtmManager.sendMessageToPeer(userId, message.toJsonString(), callback);
    }

    @Override
    public void sendMessage(String text) {

    }

    @Override
    public void sendChannelMessage(String text) {
        mRtmManager.sendChannelMessage(text,null);
    }

    @Override
    public void processMessage(RtmMessage rtmMessage, String userId) {

    }

    @Override
    public void processChannelMessage(RtmMessage rtmMessage, String userId) {
        String cmd ="",name ="",singer ="";
        try {
            JSONObject jsonObject =new JSONObject(rtmMessage.getText());
            cmd =jsonObject.getString("cmd");
            name =jsonObject.getString("musicName");
            singer =jsonObject.getString("singer");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (TextUtils.equals(cmd,Constants.MUSIC_PLAYING)){
            if (mListener !=null){
                mListener.onMusicUpdated(true,name,singer);
            }
        }else if (TextUtils.equals(cmd,Constants.MUSIC_PAUSE)){
            if (mListener !=null){
                mListener.onMusicUpdated(false,name,singer);
            }
        }else if (TextUtils.equals(cmd,Constants.ANCHOR_EXIT)){
            if (mListener !=null){
                mListener.onAnchorExit(userId);
            }
        }else if(TextUtils.equals(cmd,Constants.TOKEN_PAST_DUE)){
            if (mListener !=null){
                mListener.onTokenPastDueExit();
            }
        }else {
            if (mListener!=null){
                Log.i(TAG, "processChannelMessage: name="+mChannelData.getName(userId));
                mListener.onMessageAdd(new MessageListBean(MessageListBean.MSG_NORMAL,mChannelData.getName(userId)+"："+rtmMessage.getText()));
            }
        }
    }

    @Override
    public void addMessage(Message message) {

    }

    private RtcManager.RtcEventListener mRtcListener = new RtcManager.RtcEventListener() {
        @Override
        public void onJoinChannelSuccess(String uid) {
            if (mListener !=null){
                mListener.onJoinChannelSuccess(uid);
            }
        }

        @Override
        public void onUserOnlineStateChanged(String uid, boolean isOnline) {
            if (mListener != null)
                mListener.onUserLineChanged(String.valueOf(uid), isOnline);
        }

        @Override
        public void onNetStateChanges(IRtcEngineEventHandler.RtcStats stats) {
            if (mListener != null)
                mListener.onNetStateChanges(stats);
        }

        @Override
        public void onAudioVolumeIndication(String uid, int volume) {
            if (mListener !=null){
                mListener.onAudioVolumeIndication(String.valueOf(uid), volume);
            }
        }

        @Override
        public void onRequestToken() {
            if (mListener !=null){
                mListener.onRequestToken();
            }
        }

        @Override
        public void onRemoteNetStateChanges(IRtcEngineEventHandler.RemoteAudioStats stats) {
            if (mListener != null)
                mListener.onRemoteNetStateChanges(stats);
        }
    };

    private RtmManager.RtmEventListener mRtmListener = new RtmManager.RtmEventListener() {

        @Override
        public void onRtmConnectStateChange(int state, int reason) {
            if (mListener !=null){
                mListener.onRtmConnectStateChange(state,reason);
            }
        }

        @Override
        public void onInitMembers(List<RtmChannelMember> members) {
            if (mListener != null) {
                for (int i = 0; i <members.size() ; i++) {
                    mListener.onMemberListUpdated(members.get(i).getUserId());
                }
            }
        }

        @Override
        public void onMemberCount(int count) {
            if (mListener!=null){
                mListener.onMemberCountUpdate(count);
            }
        }

        @Override
        public void onMemberJoined(String userId) {
            if (mListener != null){
                Log.i(TAG, "onMemberJoined: --->userId ="+userId);
                countDownLatch =new CountDownLatch(1);
                mListener.onMemberJoined(userId);
                new Thread(() -> {
                    try {
                        countDownLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mListener.onMessageAdd(new MessageListBean(MessageListBean.MSG_JOIN_LEFT_ROOM,mChannelData.getName(userId)+" 进入了房间"));
                }).start();
            }
        }


        @Override
        public void onMemberLeft(String userId) {
            //退出处理
            if (mListener != null){
                //if (mChannelData.isAnchor(userId)){
                    //mListener.onMessageAdd(new MessageListBean(MessageListBean.MSG_JOIN_LEFT_ROOM,userId,"主播离开了"));
               // }else {
                    mListener.onMessageAdd(new MessageListBean(MessageListBean.MSG_JOIN_LEFT_ROOM,mChannelData.getName(userId)+" 离开了房间"));
               // }
                mListener.onMemberLeft(userId);
            }
        }

        @Override
        public void onMessageReceived(RtmMessage message,String userId) {
            try {
                JSONObject jsonObject =new JSONObject(message.getText());
                String cmd =jsonObject.getString("cmd");
                Log.i(TAG, "onMessageReceived: cmd ="+cmd);
                if (TextUtils.equals(Constants.MIC_APPLY,cmd)){
                    if (mListener !=null){
                        mListener.onApplyMicUpdated(userId);
                    }
                }else if (TextUtils.equals(Constants.MIC_ACCEPT_LINE,cmd)){  //同意上麦
                    if (mListener !=null){
                        mListener.onAcceptLineUpdated(userId);
                    }
                }else if (TextUtils.equals(Constants.MIC_REJECT_LINE,cmd)){ //拒绝上麦
                    if (mListener !=null){
                        mListener.onRejectLineUpdated(userId);
                    }
                }else if (TextUtils.equals(Constants.MIC_CANCEL_APPLY,cmd)){ //取消申请
                    if (mListener !=null){
                        mListener.onCancelApplyUpdated(userId);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onChannelMessageReceived(RtmMessage message,String userId) {
            processChannelMessage(message,userId);
        }
    };
}
