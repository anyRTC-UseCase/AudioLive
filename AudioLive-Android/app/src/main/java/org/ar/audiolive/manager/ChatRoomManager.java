package org.ar.audiolive.manager;


import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.ar.audiolive.bean.JoinUserBean;
import org.ar.audiolive.bean.MessageListBean;
import org.ar.audiolive.model.ChannelData;
import org.ar.audiolive.model.Member;
import org.ar.audiolive.model.Message;
import org.ar.audiolive.util.Constants;
import org.ar.audiolive.util.SpUtil;
import org.ar.rtc.IRtcEngineEventHandler;
import org.ar.rtm.ResultCallback;
import org.ar.rtm.RtmMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public final class ChatRoomManager implements MessageManager {

    private final String TAG = ChatRoomManager.class.getSimpleName();
    private static ChatRoomManager instance;

    private ChannelData mChannelData = new ChannelData();

    private ARServerManager serverManager =ARServerManager.getInstance();

    private RtcManager mRtcManager;
    private RtmManager mRtmManager;
    private ChatRoomEventListener mListener;
    public CountDownLatch  countDownLatch =new CountDownLatch(1);;

    private ChatRoomManager(Context context) {
        mRtcManager = RtcManager.getInstance(context);
        mRtcManager.setListener(mRtcListener);
        mRtmManager = RtmManager.getInstance(context);
        mRtmManager.setListener(mRtmListener);
        serverManager.setUpdateUserListener(joinUserBean -> {
            List<JoinUserBean.DataBean.UsersBean> userBeans =joinUserBean.getData().getList();
            for (int i = 0; i < userBeans.size(); i++) {
                String userId =userBeans.get(i).getUid();
                String name =userBeans.get(i).getUserName();
                String avatar=userBeans.get(i).getAvatar();
                mChannelData.addOrUpdateMember(new Member(userId,name,avatar));
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

    public void joinRtcChannel(String rtcToken,String channelId,int roomType,boolean isAnchor) {
        mRtcManager.joinChannel(rtcToken,channelId, SpUtil.getString(Constants.UID),roomType,isAnchor);
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
        String cmd ="",name ="";
        Log.i(TAG, "processChannelMessage: text ="+rtmMessage.getText());
        try {
            JSONObject jsonObject =new JSONObject(rtmMessage.getText());
            cmd =jsonObject.getString(Constants.CMD);
            name =jsonObject.getString(Constants.MUSIC_NAME);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "processChannelMessage: cmd ="+cmd);
        if (TextUtils.equals(cmd,Constants.MUSIC_STOP)){
            if (mListener !=null){
                mListener.onMusicUpdated(Constants.MUSIC_STATE_STOP,name);
            }
        }else if (TextUtils.equals(cmd,Constants.MUSIC_PLAYING)){
            if (mListener !=null){
                mListener.onMusicUpdated(Constants.MUSIC_STATE_PLAY,name);
            }
        }else if (TextUtils.equals(cmd,Constants.MUSIC_PAUSE)){
            if (mListener !=null){
                mListener.onMusicUpdated(Constants.MUSIC_STATE_PAUSE,name);
            }
        } else if (TextUtils.equals(cmd, Constants.EXIT)) {
            if (mListener != null) {
                mListener.onAnchorExit(userId);
            }
        } else if (TextUtils.equals(cmd, Constants.TOKEN_PAST_DUE)) {
            if (mListener != null) {
                mListener.onTokenPastDueExit();
            }
        } else if (TextUtils.equals(cmd,Constants.SEND_MSG)){
            if (mListener != null) {
                Log.i(TAG, "processChannelMessage: name=" + mChannelData.getName(userId));
                JSONObject object = null;
                String text ="";
                try {
                    object = new JSONObject(rtmMessage.getText());
                    text =object.getString(Constants.SEND_CONTENT);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mListener.onMessageAdd(new MessageListBean(MessageListBean.MSG_NORMAL, mChannelData.getName(userId) + "：" + text));
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
        public void onJoinRtmSuccess() {
            if (mListener!=null){
                mListener.onJoinRtmSuccess();
            }
        }

        @Override
        public void onRtmConnectStateChange(int state, int reason) {
            if (mListener !=null){
                mListener.onRtmConnectStateChange(state,reason);
            }
        }

        @Override
        public void onAnchorStateChanged(int state) {
            if (mListener!=null){
                mListener.onAnchorStateChanged(state);
            }
        }

        @Override
        public void onMemberJoined(String userId) {
            if (mListener != null){
                Log.i(TAG, "onMemberJoined: --->userId ="+userId);
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
                mListener.onMessageAdd(new MessageListBean(MessageListBean.MSG_JOIN_LEFT_ROOM,mChannelData.getName(userId)+" 离开了房间"));
                mListener.onMemberLeft(userId);
            }
        }

        @Override
        public void onMessageReceived(RtmMessage message,String userId) {
            try {
                JSONObject jsonObject =new JSONObject(message.getText());
                String cmd =jsonObject.getString(Constants.CMD);
               /* String name =jsonObject.getString("name");
                String avatar =jsonObject.getString("avatar");*/
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
