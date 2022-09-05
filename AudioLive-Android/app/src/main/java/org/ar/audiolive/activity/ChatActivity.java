package org.ar.audiolive.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.kongzue.dialog.interfaces.OnDismissListener;
import com.kongzue.dialog.v3.MessageDialog;
import com.kongzue.dialog.v3.TipDialog;

import org.ar.audiolive.R;
import org.ar.audiolive.adapter.LogAdapter;
import org.ar.audiolive.adapter.InfoPagerAdapter;
import org.ar.audiolive.adapter.MessageAdapter;
import org.ar.audiolive.bean.JoinRoomBean;
import org.ar.audiolive.bean.LogBean;
import org.ar.audiolive.bean.InfoBean;
import org.ar.audiolive.bean.MessageListBean;
import org.ar.audiolive.bean.MusicBean;
import org.ar.audiolive.bean.RoomMusicInfoBean;
import org.ar.audiolive.dialog.AutoTipDialog;
import org.ar.audiolive.dialog.CommentDialogFragment;
import org.ar.audiolive.dialog.VolumeDialog;
import org.ar.audiolive.dialog.WaitMicDialog;
import org.ar.audiolive.manager.ARServerManager;
import org.ar.audiolive.manager.ChatRoomEventListener;
import org.ar.audiolive.manager.ChatRoomManager;
import org.ar.audiolive.model.ChannelData;
import org.ar.audiolive.model.Member;
import org.ar.audiolive.util.Constants;
import org.ar.audiolive.util.SpUtil;
import org.ar.rtc.IRtcEngineEventHandler;
import org.ar.rtc.mediaplayer.ARMediaPlayerKit;
import org.ar.rtc.mediaplayer.MediaPlayerObserver;
import org.ar.rtc.mediaplayer.PlayerConstans;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener, ChatRoomEventListener {

    private static final String TAG =ChatActivity.class.getSimpleName();

    private ImageView back;
    private TextView mTitle;

    private ViewPager mViewPager;
    private LinearLayout dotLayout;

    private ImageView imgMusic;
    private TextView mMusicName;

    private RecyclerView rvChatMsg,rvGuestLog;
    private LinearLayoutManager msgLayoutManager;
    private LinearLayoutManager guestLayoutManager;
    private MessageAdapter messageAdapter;
    private LogAdapter logAdapter;

    private TextView mChatInput,mApplyCount,tipAnchorLeave;
    private ImageView mJoinMic,mChatMic,mSpeaker,mVolume;

    private HorizontalScrollView mHsEffect;
    private Button mEffect_hhh,mEffect_qihong,mEffect_guzhang,mEffect_ganga,mEffect_wuya,mEffect_mymom;

    private int micState =0;
    private static final int MIC_STATE_UP=0;
    private static final int MIC_STATE_CANCEL=1;
    private static final int MIC_STATE_DOWN=2;

    //直播异常倒计时
    private int anchorCount =60;
    //主播异常，游客倒计时
    private int guestCount =35;
    //游客异常倒计时
    private int guestAbnormalCount =20;
    private static final int GUEST_TIME_COUNT=0;
    private static final int GUEST_LEAVE_TIP=1;
    private static final int ANCHOR_TIME_COUNT=2;
    private static final int MYSELF_LEAVE_TIP=3;
    private static final int GUEST_ABNORMAL_COUNT=4;

    private FrameLayout mFrameLayout;
    private String mAnchorId;
    private String mUserId;
    private String roomId;
    private String roomName;
    private String pushUrl;
    private String pullRtmpUrl;
    private boolean isAnchor;
    private boolean isMic,isSpeaker,isJoinMic,isExitRoom;

    private boolean isReconnect = false;
    private MessageDialog reconnectDialog;

    private ARServerManager serverManager;
    private Animation mMusicAnimation;

    private ARMediaPlayerKit mediaPlayerKit;

    private int prePos =0;
    private LayoutInflater inflater;
    private List<String> mUserIdList;
    private InfoPagerAdapter infoAdapter;
    private ChatRoomManager mChatRoomManager;
    private ChannelData mChannelData;
    private VolumeDialog volumeDialog;
    private WaitMicDialog waitMicDialog;
    private AutoTipDialog autoTipDialog;
    private HashMap<String,InfoBean> infoMap;
    private List<String> mWaitingList;
    private int mRoomType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initView();
        init();
        serverManager.getMusicList();
        serverManager.getRoomMusicInfo(roomId);
    }

    private void initView() {
        back =findViewById(R.id.back);
        mTitle =findViewById(R.id.title_content);
        mViewPager =findViewById(R.id.vp_info);
        dotLayout =findViewById(R.id.dotlayout);
        imgMusic =findViewById(R.id.img_music);
        tipAnchorLeave =findViewById(R.id.tip_anchor_leave);
        mMusicName =findViewById(R.id.music_name);
        rvChatMsg =findViewById(R.id.rv_chat);
        rvGuestLog =findViewById(R.id.rv_show_log);
        mChatInput =findViewById(R.id.chat_input);
        mJoinMic =findViewById(R.id.chat_join_mic);
        mChatMic =findViewById(R.id.chat_mic);
        mSpeaker =findViewById(R.id.chat_speaker);
        mVolume =findViewById(R.id.chat_volume);
        mApplyCount =findViewById(R.id.chat_apply_count);
        mFrameLayout =findViewById(R.id.fl_info);
        mHsEffect =findViewById(R.id.effect_hs);
        mEffect_hhh =findViewById(R.id.effect_hhh);
        mEffect_qihong =findViewById(R.id.effect_qihong);
        mEffect_guzhang =findViewById(R.id.effect_guzhang);
        mEffect_wuya =findViewById(R.id.effect_wuya);
        mEffect_ganga =findViewById(R.id.effect_ganga);
        mEffect_mymom =findViewById(R.id.effect_mymom);
    }

    private void init() {
        isAnchor =getIntent().getBooleanExtra(Constants.INTENT_IS_ANCHOR,false);
        roomId = getIntent().getStringExtra(Constants.INTENT_ROOM_ID);
        roomName =getIntent().getStringExtra(Constants.INTENT_ROOM_NAME);
        mRoomType =getIntent().getIntExtra(Constants.ROOM_TYPE,Constants.R_TYPE_RTC);
        mUserId =SpUtil.getString(Constants.UID);
        serverManager =ARServerManager.getInstance();
        mMusicAnimation = AnimationUtils.loadAnimation(this,R.anim.animation_rotate);
        mUserIdList =new ArrayList<>();
        mWaitingList =new ArrayList<>();
        initManager();
        msgLayoutManager=new LinearLayoutManager(this);
        guestLayoutManager=new LinearLayoutManager(this);
        messageAdapter =new MessageAdapter();
        logAdapter =new LogAdapter();
        rvChatMsg.setLayoutManager(msgLayoutManager);
        rvGuestLog.setLayoutManager(guestLayoutManager);
        rvChatMsg.setAdapter(messageAdapter);
        rvGuestLog.setAdapter(logAdapter);
        mTitle.setText(getString(R.string.brackets,roomName,roomId));
        if (isAnchor){
            showAnchor();
        }else {
            showGuest();
        }
        setListener();
        viewPagerConfig();
    }

    private void initManager() {
        mChatRoomManager =ChatRoomManager.instance(this);
        mChatRoomManager.setListener(this);
        mChatRoomManager.joinRtmChannel(roomId);
        mChannelData =mChatRoomManager.getChannelData();
        if (!TextUtils.isEmpty(SpUtil.getString(Constants.USER_NAME))){
            mChannelData.addOrUpdateMember(new Member(mUserId,SpUtil.getString(Constants.USER_NAME)));
        }else {
            mChannelData.addOrUpdateMember(new Member(mUserId,Build.MODEL));
        }
    }

    /**
     * 游客加入房间，开始拉流，并且播放
     * @param mute 是否静音，false：不静音，true：静音
     */
    private void initPlayer(boolean mute){
        mediaPlayerKit =new ARMediaPlayerKit();
        mediaPlayerKit.registerPlayerObserver(mediaPlayerObserver);
        Log.i(TAG, "initPlayer: pll ="+pullRtmpUrl);
        mediaPlayerKit.open(pullRtmpUrl,0);
        mediaPlayerKit.mute(mute);
        mediaPlayerKit.play();
    }

    private void showAnchor(){
        pushUrl =getIntent().getStringExtra(Constants.INTENT_RTMP_URL);
        mAnchorId =mUserId;
        rvGuestLog.setVisibility(View.GONE);
        mChannelData.setAnchorId(mAnchorId);
        mJoinMic.setVisibility(View.GONE);
        mFrameLayout.setVisibility(View.VISIBLE);
        volumeDialog =new VolumeDialog(this, mChatRoomManager, enabled -> {
            if (enabled){
                mHsEffect.setVisibility(View.VISIBLE);
            }else {
                mHsEffect.setVisibility(View.GONE);
            }
        });
        Log.i(TAG, "showAnchor: pushUrl ="+pushUrl+",mRoomType ="+mRoomType);
        switch (mRoomType){
            case Constants.R_TYPE_RTC:
                //mApplyCount.setVisibility(View.GONE);
                break;
            case Constants.R_TYPE_CLIENT:
                mApplyCount.setVisibility(View.VISIBLE);
                mChatRoomManager.getRtcManager().pushStream(pushUrl); //主播客户端推流
                break;
            case Constants.R_TYPE_SERVER:
                mApplyCount.setVisibility(View.VISIBLE);
                mChatRoomManager.getRtcManager().addPublishStreamUrl(pushUrl); //主播服务器推流
                break;
            default:
                break;
        }

    }

    private void showGuest(){
        mAnchorId =SpUtil.getString(Constants.ANCHOR_UID);
        mChannelData.setAnchorId(mAnchorId);
        mApplyCount.setVisibility(View.GONE);
        switch (mRoomType){
            case Constants.R_TYPE_RTC:
                rvGuestLog.setVisibility(View.GONE);
                mFrameLayout.setVisibility(View.VISIBLE);
                mChatMic.setVisibility(View.GONE);
                mVolume.setVisibility(View.GONE);
                mSpeaker.setVisibility(View.GONE);
                String rtcToken ="";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    rtcToken = Optional.ofNullable(serverManager)
                        .map(ARServerManager::getJoinRoomBean)
                        .map(JoinRoomBean::getData)
                        .map(JoinRoomBean.DataBean::getRoom)
                        .map(JoinRoomBean.DataBean.RoomBeans::getRtcToken)
                        .orElse("");
                }else {
                    rtcToken =serverManager.getJoinRoomBean().getData().getRoom().getRtcToken();
                }
                Log.i(TAG, "onAcceptLineUpdated: rtcToken ="+rtcToken);
                mChatRoomManager.joinRtcChannel(rtcToken,roomId,mRoomType,false);
                break;
            case Constants.R_TYPE_CLIENT:
            case Constants.R_TYPE_SERVER:
                rvGuestLog.setVisibility(View.VISIBLE);
                mFrameLayout.setVisibility(View.GONE);
                mChatMic.setVisibility(View.GONE);
                mSpeaker.setVisibility(View.GONE);
                mVolume.setVisibility(View.GONE);
                pullRtmpUrl =getIntent().getStringExtra(Constants.INTENT_RTMP_URL);
                initPlayer(false);
                break;
            default:
                break;
        }
        mChannelData.addOrUpdateMember(new Member(mAnchorId,SpUtil.getString(Constants.ANCHOR_NAME)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        serverManager.getRoomMusicInfo(roomId);
        serverManager.setMusicListListener(new ARServerManager.MusicListListener() {
            @Override
            public void getMusicList(MusicBean musicBean) {

            }

            @Override
            public void getRoomMusicInfo(RoomMusicInfoBean roomMusicInfoBean) {
                int musicId =roomMusicInfoBean.getData().getMusicId();
                int state =roomMusicInfoBean.getData().getMusicState();
                String musicName =roomMusicInfoBean.getData().getMusicName();
                if (musicId==0){
                    imgMusic.clearAnimation();
                    mMusicName.setVisibility(View.GONE);
                }else {
                    if (state==1){
                        imgMusic.startAnimation(mMusicAnimation);
                        mMusicName.setVisibility(View.VISIBLE);
                        mMusicName.setText(musicName);
                    }else if(state==2){
                        imgMusic.clearAnimation();
                        mMusicName.setVisibility(View.VISIBLE);
                        mMusicName.setText(musicName);
                    }else {
                        imgMusic.clearAnimation();
                        mMusicName.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    private void logAddData(String log){
        logAdapter.addData(new LogBean(R.drawable.circle,getDate(),log));
    }

    private String getDate(){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(new Date());
    }

    private void setListener(){
        imgMusic.setOnClickListener(this);
        mChatInput.setOnClickListener(this);
        mJoinMic.setOnClickListener(this);
        mChatMic.setOnClickListener(this);
        mSpeaker.setOnClickListener(this);
        mVolume.setOnClickListener(this);
        mApplyCount.setOnClickListener(this);
        back.setOnClickListener(this);
        mEffect_hhh.setOnClickListener(this);
        mEffect_qihong.setOnClickListener(this);
        mEffect_guzhang.setOnClickListener(this);
        mEffect_wuya.setOnClickListener(this);
        mEffect_ganga.setOnClickListener(this);
        mEffect_mymom.setOnClickListener(this);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void viewPagerConfig() {
        inflater =getLayoutInflater();
        infoMap =new HashMap<>();
        infoAdapter =new InfoPagerAdapter(this,infoMap,mChannelData);
        mViewPager.setPageMargin(30);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(infoAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (mViewPager !=null){
                    mViewPager.invalidate();
                }
            }

            @Override
            public void onPageSelected(int position) {
                if (dotLayout.getChildAt(prePos) !=null){
                    dotLayout.getChildAt(prePos).setEnabled(false);
                }
                if (dotLayout.getChildAt(position % infoMap.size()) !=null){
                    dotLayout.getChildAt(position % infoMap.size()).setEnabled(true);
                }
                prePos =position % infoMap.size();
                Log.i(TAG, "onPageSelected: pos ="+position+"prePos="+prePos);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mFrameLayout.setOnTouchListener((view, motionEvent) -> mViewPager.dispatchTouchEvent(motionEvent));
    }

    private void initDots(){
        if (null!=dotLayout){
            dotLayout.removeAllViews();
        }
        for (int i = 0; i <infoMap.size() ; i++) {
            ImageView dot =new ImageView(this);
            dot.setEnabled(false);
            dot.setImageResource(R.drawable.selector_dot_state);
            LinearLayout.LayoutParams params =new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.leftMargin =10;
            dot.setLayoutParams(params);
            dotLayout.addView(dot);
        }
        assert dotLayout != null;
        if (dotLayout.getChildCount()>0){
            dotLayout.getChildAt(0).setEnabled(true);
        }
    }

    public void showExitDialog() {
        MessageDialog.show(this, "退出房间", "当前正在直播，是否退出", "确定")
                .setCancelButton("取消", (baseDialog, v) ->{
                    baseDialog.doDismiss();
                    return true;
                } )
                .setOnOkButtonClickListener((baseDialog, v) -> {
                    anchorExitMsg();
                    baseDialog.doDismiss();
                    finish();
                    return true;
                });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode ==KeyEvent.KEYCODE_BACK){
            if(isAnchor){
                showExitDialog();
                return true;
            }else {
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.img_music:
                if (isAnchor){
                    Intent intent =new Intent(this,MusicActivity.class);
                    intent.putExtra(Constants.INTENT_ROOM_ID,roomId);
                    startActivity(intent);
                }else {
                    Toast.makeText(this, "只有主持人才可以", Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.chat_input:
                CommentDialogFragment dialogFragment = new CommentDialogFragment();
                dialogFragment.show(getSupportFragmentManager(), text -> {
                    JSONObject object =new JSONObject();
                    try {
                        object.put(Constants.CMD,Constants.SEND_MSG);
                        object.put(Constants.SEND_CONTENT,text);
                        object.put(Constants.USER_NAME,SpUtil.getString(Constants.USER_NAME));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mChatRoomManager.sendChannelMessage(object.toString());
                    addMessage(new MessageListBean(MessageListBean.MSG_NORMAL, SpUtil.getString(Constants.USER_NAME)+"："+text));
                });
                break;
            case R.id.chat_join_mic:
                joinMic();
                break;
            case R.id.chat_mic:
                if (isMic){
                    mChatMic.setImageResource(R.drawable.mic_open);
                }else {
                    mChatMic.setImageResource(R.drawable.mic_close);
                }
                isMic =!isMic;
                mChatRoomManager.getRtcManager().muteLocalAudioStream(isMic);
                break;
            case R.id.chat_speaker:
                if (isSpeaker){
                    mSpeaker.setImageResource(R.drawable.speaker_open);
                }else {
                    mSpeaker.setImageResource(R.drawable.speaker_close);
                }
                isSpeaker =!isSpeaker;
                mChatRoomManager.getRtcManager().getRtcEngine().setEnableSpeakerphone(!isSpeaker);
                break;
            case R.id.chat_apply_count:
                waitMicDialog =new WaitMicDialog(this, mWaitingList, userId -> {
                    mWaitingList.remove(userId);
                    mApplyCount.setText(String.valueOf(mWaitingList.size()));
                });
                waitMicDialog.show();
                break;
            case R.id.chat_volume:
                volumeDialog.show();
                break;
            case R.id.effect_hhh:
                playEffect(11,"chipmunk.wav");
                break;
            case R.id.effect_guzhang:
                playEffect(12,"guzhang.wav");
                break;
            case R.id.effect_ganga:
                playEffect(13,"awkward.wav");
                break;
            case R.id.effect_wuya:
                playEffect(14,"wuya.wav");
                break;
            case R.id.effect_qihong:
                playEffect(15,"qihong.wav");
                break;
            case R.id.effect_mymom:
                playEffect(16,"wodema.wav");
                break;
            case R.id.back:
                if(isAnchor){
                    showExitDialog();
                }else {
                    finish();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 避免运用第三方接口，返回JSON为空，从而避免空指针！
     * @return MusicBean.DataBean
     */
    private RoomMusicInfoBean.DataBean getMusicData(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 根对象为空就创建一个空Optional，否则就创建一个根对象的Optional
            return Optional.ofNullable(serverManager)
                    // 根对象为空就直接返回空Optional，否则返回这个值的 Optional
                    .map(ARServerManager::getRoomMusicInfoBean)
                    .map(RoomMusicInfoBean::getData)
                    .orElse(new RoomMusicInfoBean.DataBean()); //取不到值，new一个新对象
        }else {
            return serverManager.getRoomMusicInfoBean().getData();
        }
    }

    private void playEffect(int id,String path){
        path =getExternalFilesDir(path).getPath()+"/"+path;
        Log.i(TAG, "playEffect: path ="+path +",id ="+id);
        mChatRoomManager.getRtcManager().stopAllEffects();
        mChatRoomManager.getRtcManager().playEffect(id,path);
    }

    //游客上麦
    private void joinMic(){
        JSONObject jsonObject =new JSONObject();
        switch (micState){
            case MIC_STATE_UP: //上麦
                try {
                    jsonObject.put(Constants.CMD,Constants.MIC_APPLY);
                    jsonObject.put(Constants.USER_NAME,SpUtil.getString(Constants.USER_NAME));
                    jsonObject.put(Constants.USER_AVATAR,SpUtil.getString(Constants.USER_AVATAR));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mChatRoomManager.getRtmManager().sendMessageToPeer(mAnchorId,jsonObject.toString(),null);
                micState =MIC_STATE_CANCEL;
                mJoinMic.setImageResource(R.drawable.cancel_mic);
                break;
            case MIC_STATE_CANCEL: //取消上麦
                try {
                    jsonObject.put(Constants.CMD,Constants.MIC_CANCEL_APPLY);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mChatRoomManager.getRtmManager().sendMessageToPeer(mAnchorId,jsonObject.toString(),null);
                micState =MIC_STATE_UP;
                mJoinMic.setImageResource(R.drawable.up_mic);
                break;
            case MIC_STATE_DOWN: //下麦
                mJoinMic.setImageResource(R.drawable.up_mic);
                micState =MIC_STATE_UP;
                isJoinMic =false;
                mChatMic.setVisibility(View.GONE);
                if (mRoomType==Constants.R_TYPE_RTC){
                    mChatRoomManager.getRtcManager().setClientRole(org.ar.rtc.Constants.CLIENT_ROLE_AUDIENCE);
                    removeDataView(mUserId);
                }else {
                    mChatRoomManager.leaveRtcChannel();
                    mSpeaker.setVisibility(View.GONE);
                    mFrameLayout.setVisibility(View.GONE);
                    rvGuestLog.setVisibility(View.VISIBLE);
                    infoMap.clear();
                    mUserIdList.clear();
                    infoAdapter.setInfoMap(infoMap);
                    infoAdapter.notifyDataSetChanged();
                    initDots();
                    mediaPlayerKit.play();
                    mediaPlayerKit.mute(false);
                }

                break;
            default:
                break;
        }
    }

    public void addMessage(MessageListBean bean){
        messageAdapter.addData(bean);
        msgLayoutManager.scrollToPositionWithOffset(messageAdapter.getItemCount() - 1, Integer.MIN_VALUE);
    }

    public JoinRoomBean.DataBean.RoomBeans getRoom(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Optional.ofNullable(serverManager)
                    .map(ARServerManager::getJoinRoomBean)
                    .map(JoinRoomBean::getData)
                    .map(JoinRoomBean.DataBean::getRoom)
                    .orElse(new JoinRoomBean.DataBean.RoomBeans());
        }else {
            return serverManager.getJoinRoomBean().getData().getRoom();
        }
    }

    //更新游客音乐状态
    private void updateGuestMusic(){
        if (!isAnchor){
            if (getRoom()!=null){
                int musicState =getRoom().getMusicState();
                String name =getRoom().getMusic().getMusicName();
                if (musicState ==1){
                    imgMusic.startAnimation(mMusicAnimation);
                    mMusicName.setText(name);
                }else if (musicState==2){
                    mMusicName.setText(name);
                }
            }
        }
    }

    //展示数据视图
    private void showDataView(String uid,int pos){
        View view =inflater.inflate(R.layout.item_vp_info,null,false);
        infoMap.put(uid,new InfoBean(uid,view,pos));
        infoAdapter.setInfoMap(infoMap,uid);
        infoAdapter.setUserIdList(mUserIdList);
        infoAdapter.notifyDataSetChanged();
        initDots();
    }

    //移除数据视图
    private void removeDataView(String userId){
        mUserIdList.remove(userId);
        if (infoMap.containsKey(userId)){
            infoMap.remove(userId);
            infoAdapter.setInfoMap(infoMap);
            infoAdapter.setUserIdList(mUserIdList);
            infoAdapter.notifyDataSetChanged();
            initDots();
        }
    }

    /**
     * 加入或离开RTM频道，发消息通知
     */
    @Override
    public void onJoinRtmSuccess() {
        JSONObject object =new JSONObject();
        try {
            object.put(Constants.CMD,Constants.JOIN);
            object.put(Constants.USER_NAME,SpUtil.getString(Constants.USER_NAME));
            mChatRoomManager.sendChannelMessage(object.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mChatRoomManager.getRtmManager().subscribePeersOnlineStatus(mAnchorId);
    }

    /**
     * SDK 与 AR RTM 系统的连接状态发生改变回调。
     * @param state 新连接状态。
     * @param reason 连接状态改变原因。
     */
    @Override
    public void onRtmConnectStateChange(int state, int reason) {
        runOnUiThread(()->{
            Log.i(TAG, "onRtmConnectStateChange: state ="+state+",reason="+reason);
            if (state==4){
                isReconnect = true;
                reconnectDialog=MessageDialog.build(this)
                        .setTitle("提示")
                        .setMessage("正在重连....")
                        .setOnOkButtonClickListener((baseDialog, v) -> true)
                        .setCancelable(false);
                reconnectDialog.show();
                /*if (isAnchor){
                    handler.sendEmptyMessageDelayed(ANCHOR_TIME_COUNT,1000);
                }else {
                    handler.sendEmptyMessageDelayed(GUEST_ABNORMAL_COUNT,1000);
                }*/
            }else if (state ==3){
                if (isReconnect){
                    if (reconnectDialog!=null){
                        reconnectDialog.doDismiss();
                    }
                    isReconnect= false;
                    TipDialog.show(this, "重连成功！", TipDialog.TYPE.SUCCESS).setOnDismissListener(new OnDismissListener() {
                        @Override
                        public void onDismiss() {
                        }
                    });
                    if (isAnchor){
                        handler.removeMessages(ANCHOR_TIME_COUNT);
                        anchorCount =60;
                    }else {
                        handler.removeMessages(GUEST_ABNORMAL_COUNT);
                        guestAbnormalCount =20;
                    }
                    if (isAnchor){
                        mChatRoomManager.getRtcManager().stopAudioMixing();
                        serverManager.getRoomMusicInfo(roomId);
                        serverManager.setMusicListListener(new ARServerManager.MusicListListener() {

                            @Override
                            public void getMusicList(MusicBean musicBean) {
                            }

                            @Override
                            public void getRoomMusicInfo(RoomMusicInfoBean roomMusicInfoBean) {
                                int musicState =roomMusicInfoBean.getData().getMusicState();
                                String name =roomMusicInfoBean.getData().getMusicName();
                                Log.i(TAG, "onRtmConnectStateChange: musicState ="+musicState);
                                if (musicState !=0){
                                    JSONObject object =new JSONObject();
                                    try {
                                        object.put(Constants.CMD,Constants.MUSIC_STOP);
                                        object.put(Constants.MUSIC_NAME,name);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    mChatRoomManager.sendChannelMessage(object.toString());
                                }
                                serverManager.updateMusicState(Constants.MUSIC_STATE_STOP,roomId);
                                imgMusic.clearAnimation();
                                mMusicName.setVisibility(View.GONE);
                            }
                        });
                       /* int musicState =getMusicData().getMusicState();
                        String name =getMusicData().getMusicName();*/
                        if (mRoomType== Constants.R_TYPE_CLIENT){ //客户端推流
                            mChatRoomManager.getRtcManager().createStreamingInstance();
                            mChatRoomManager.getRtcManager().pushStream(pushUrl);
                        }else if(mRoomType== Constants.R_TYPE_SERVER){ //服务器推流
                            mChatRoomManager.getRtcManager().removePublishStreamUrl(pushUrl);
                            mChatRoomManager.getRtcManager().addPublishStreamUrl(pushUrl);
                        }

                    }else {
                        //游客是否退出房间，如果是，就不初始化拉流
                        if (!isExitRoom){
                            Log.i(TAG, "onRtmConnectStateChange: --->");
                            initPlayer(isJoinMic);
                        }
                    }
                }
            }
        });
    }

    /**
     * 加入成功的回调
     * @param uid 用户UID
     */
    @Override
    public void onJoinChannelSuccess(String uid) {
        runOnUiThread(()->{
            Log.i(TAG, "zhao onJoinChannelSuccess:  uid ="+uid);
            if (isAnchor || mRoomType!=Constants.R_TYPE_RTC){
                if (!mUserIdList.contains(uid)){
                    mUserIdList.add(uid);
                }
                showDataView(uid,0);
                updateGuestMusic();
            }
        });
    }

    /**
     * 加入/离开 RT C房间
     * @param userId ID
     * @param isLine true:加入房间，false：离开房间
     */
    @Override
    public void onUserLineChanged(String userId, Boolean isLine) {
        runOnUiThread(()->{
            Log.i(TAG, "zhao onUserLineChanged: 小伙伴加入成功！="+userId);
            if (isLine){
                /*if(!mChannelData.isAnchor(userId)){
                    addMessage(new MessageListBean(MessageListBean.MSG_SYSYTEM,mChannelData.getName(userId)+"加入了麦序"));
                }*/
                if (!mUserIdList.contains(userId)){
                    mUserIdList.add(userId);
                }
                int pos =-1;
                for (int i = 0; i <mUserIdList.size() ; i++) {
                    if (userId.equals(mUserIdList.get(i))){
                        pos =i;
                    }
                }
                showDataView(userId,pos);
            }else {
                //addMessage(new MessageListBean(MessageListBean.MSG_SYSYTEM,mChannelData.getName(userId)+"退出了麦序"));
                removeDataView(userId);
                Log.i(TAG, "onUserLineChanged: 小伙伴离开成功！");
            }
        });
    }

    /**
     * 本地统计的状态
     * @param stats RtcStats对象
     */
    @Override
    public void onNetStateChanges(IRtcEngineEventHandler.RtcStats stats) {
        runOnUiThread(()->{
            Log.i(TAG, "onNetStateChanges: rtt="+stats.lastmileDelay+",lossRate="+stats.txPacketLossRate);
            if (infoMap.containsKey(mUserId)){
                infoMap.get(mUserId).setDelay(stats.lastmileDelay);
                infoMap.get(mUserId).setPacket(stats.txPacketLossRate);
                infoAdapter.setInfoMap(infoMap,mUserId);
                infoAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * 远程统计状态
     * @param stats RemoteAudioStats对象
     */
    @Override
    public void onRemoteNetStateChanges(IRtcEngineEventHandler.RemoteAudioStats stats) {
        runOnUiThread(()->{
            Log.i(TAG, "onRemoteNetStateChanges: rtt="+stats.networkTransportDelay+",lossRate="+stats.audioLossRate);
            if (infoMap.containsKey(stats.uid)){
                infoMap.get(stats.uid).setDelay(stats.networkTransportDelay);
                infoMap.get(stats.uid).setPacket(stats.audioLossRate);
                infoAdapter.setInfoMap(infoMap,stats.uid);
                infoAdapter.notifyDataSetChanged();
            }

        });
    }


    /**
     * 说话者声音大小
     * @param userId ：说话者
     * @param volume：音量
     */
    @Override
    public void onAudioVolumeIndication(String userId, int volume) {
        runOnUiThread(()->{
            if (infoMap.containsKey(userId)){
                Log.i(TAG, "onAudioVolumeIndication: userId ="+userId+",volume ="+volume);
                infoMap.get(userId).setVolume(volume);
                infoAdapter.setInfoMap(infoMap,userId);
                infoAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * rtcToken 过期
     */
    @Override
    public void onRequestToken() {
      runOnUiThread(()->{
          if (isAnchor){
              JSONObject jsonObject =new JSONObject();
              try {
                  jsonObject.put(Constants.CMD,Constants.TOKEN_PAST_DUE);
              } catch (JSONException e) {
                  e.printStackTrace();
              }
              mChatRoomManager.getRtmManager().sendChannelMessage(jsonObject.toString(),null);
              showTokenPastDueExit();
          }
      });
    }

    /**
     * 提示游客体验到期
     */
    @Override
    public void onTokenPastDueExit() {
        runOnUiThread(this::showTokenPastDueExit);
    }

    /**
     * 申请上麦,返回主播
     * @param userId：申请者
     */
    @Override
    public void onApplyMicUpdated(String userId) {
        runOnUiThread(()->{
            Log.i(TAG, "onApplyMicUpdated: userId ="+userId);
            Log.i(TAG, "onApplyMicUpdated: mWaitingList size ="+mWaitingList.size());
            if (!mWaitingList.contains(userId)){
                mWaitingList.add(userId);
            }
            mApplyCount.setText(String.valueOf(mWaitingList.size()));
            if (waitMicDialog!=null){
                waitMicDialog.setDataList(mWaitingList);
            }
        });

    }

    /**
     * 取消上麦，返回主播
     * @param userId 取消者
     */
    @Override
    public void onCancelApplyUpdated(String userId) {
        runOnUiThread(()->{
            Log.i(TAG, "onCancelApplyUpdated: userId ="+userId);
            mWaitingList.remove(userId) ;
            mApplyCount.setText(String.valueOf(mWaitingList.size()));
            if (waitMicDialog!=null){
                waitMicDialog.setDataList(mWaitingList);
            }

            /*applyUsersMap.remove(userId);
            if (applyDialog.isShowing()){
                applyDialog.dismiss();
            }
            autoTipDialog =new AutoTipDialog(this,R.drawable.red_tip,userId+"取消上麦");
            autoTipDialog.show();*/
        });
    }

    /**
     * 拒绝上麦，返回游客
     * @param userId 主播Id
     */
    @Override
    public void onRejectLineUpdated(String userId) {
        runOnUiThread(()->{
            micState = MIC_STATE_UP;
            autoTipDialog =new AutoTipDialog(this,R.drawable.red_tip,"拒绝上麦");
            autoTipDialog.show();
            mJoinMic.setImageResource(R.drawable.up_mic);
        });
    }

    /**
     * 同意上麦，返回游客
     * @param userId 主播Id
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onAcceptLineUpdated(String userId) {
        runOnUiThread(()->{
            micState = MIC_STATE_DOWN;
            isJoinMic =true;
            autoTipDialog =new AutoTipDialog(this,R.drawable.success_tip,"同意上麦");
            autoTipDialog.show();
            mJoinMic.setImageResource(R.drawable.down_mic);
            if (mRoomType==Constants.R_TYPE_RTC){
                mChatRoomManager.getRtcManager().setClientRole(org.ar.rtc.Constants.CLIENT_ROLE_BROADCASTER);
                if (!mUserIdList.contains(mUserId)){
                    mUserIdList.add(mUserId);
                }
                int pos =-1;
                for (int i = 0; i <mUserIdList.size() ; i++) {
                    if (mUserId.equals(mUserIdList.get(i))){
                        pos =i;
                    }
                }
                Log.i(TAG, "onAcceptLineUpdated: pos ="+pos);
                showDataView(mUserId,pos);
            }else {
                String rtcToken ="";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    rtcToken = Optional.ofNullable(serverManager)
                            .map(ARServerManager::getJoinRoomBean)
                            .map(JoinRoomBean::getData)
                            .map(JoinRoomBean.DataBean::getRoom)
                            .map(JoinRoomBean.DataBean.RoomBeans::getRtcToken)
                            .orElse("");
                }else {
                    rtcToken=serverManager.getJoinRoomBean().getData().getRoom().getRtcToken();
                }
                Log.i(TAG, "onAcceptLineUpdated: rtcToken ="+rtcToken);
                mChatRoomManager.joinRtcChannel(rtcToken,roomId,mRoomType,false);
                mediaPlayerKit.mute(true);
                mSpeaker.setVisibility(View.GONE);
                mFrameLayout.setVisibility(View.VISIBLE);
                rvGuestLog.setVisibility(View.GONE);
            }
            mChatMic.setVisibility(View.VISIBLE);
            /*if (isMic){
                mChatMic.setImageResource(R.drawable.mic_close);
            }else {
                mChatMic.setImageResource(R.drawable.mic_open);
            }*/
            mChatMic.setImageResource(R.drawable.mic_open);
            mChatRoomManager.getRtcManager().muteLocalAudioStream(false);
            if (isSpeaker){
                mSpeaker.setImageResource(R.drawable.speaker_close);
            }else {
                mSpeaker.setImageResource(R.drawable.speaker_open);
            }
            mChatRoomManager.getRtcManager().getRtcEngine().setEnableSpeakerphone(!isSpeaker);
        });
    }

    /**
     * 主播离开
     * @param userId 主播ID
     */
    @Override
    public void onAnchorExit(String userId) {
        runOnUiThread(()->{
            if (!isAnchor && mChannelData.isAnchor(userId)){
                showGuestExit("主播离开，房间不存在");
            }
        });
    }

    /**
     * 返回游客：更新音乐状态
     * @param state 音乐播放状态
     * @param name 音乐名称
     */
    @Override
    public void onMusicUpdated(int state ,String name) {
        runOnUiThread(()->{
            if (!isAnchor){
                Log.i(TAG, "onMusicUpdated: state="+state);
                switch (state){
                    case Constants.MUSIC_STATE_STOP:
                        imgMusic.clearAnimation();
                        mMusicName.setVisibility(View.GONE);
                        break;
                    case Constants.MUSIC_STATE_PLAY:
                        imgMusic.startAnimation(mMusicAnimation);
                        mMusicName.setVisibility(View.VISIBLE);
                        mMusicName.setText(name);
                        break;
                    case Constants.MUSIC_STATE_PAUSE:
                        imgMusic.clearAnimation();
                        mMusicName.setVisibility(View.VISIBLE);
                        mMusicName.setText(name);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /**
     * 订阅主播状态
     * @param state 0: 用户在线，1: 连接状态不稳定，2: 用户不在线
     */
    @Override
    public void onAnchorStateChanged(int state) {
        runOnUiThread(()->{
            Log.i(TAG, "onAnchorStateChanged: state ="+state);
            if (state ==0){
                tipAnchorLeave.setVisibility(View.GONE);
            }else {
                tipAnchorLeave.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * 成员加入频道
     * @param userId ：成员ID
     */
    @Override
    public void onMemberJoined(String userId) {
        runOnUiThread(()->{
            if (mChannelData.isAnchor(userId)){
                Log.i(TAG, "onMemberJoined: ");
                initPlayer(isJoinMic);
                handler.postDelayed(() -> {
                    handler.removeMessages(GUEST_TIME_COUNT);
                    guestCount =35;
                },500);
            }
            serverManager.getJoinUserList(roomId);
        });
    }

    /**
     * 添加消息
     * @param messageListBean 消息对象
     */
    @Override
    public void onMessageAdd(MessageListBean messageListBean) {
        runOnUiThread(()->{
            addMessage(messageListBean);
        });
    }

    /**
     *  用户离开频道
     * @param userId 用户ID
     */
    @Override
    public void onMemberLeft(String userId) {
        runOnUiThread(()->{
            if (!isAnchor){
                if (mChannelData.isAnchor(userId)){
                    handler.sendEmptyMessage(GUEST_TIME_COUNT);
                }
            }else {
                serverManager.updateUserLeaveTs(userId,roomId);
            }
        });
    }

    private final Handler handler =new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case GUEST_TIME_COUNT:
                    guestCount--;
                    Log.i(TAG, "handleMessage: leaveTimeCount ="+guestCount);
                    handler.sendEmptyMessageDelayed(GUEST_TIME_COUNT,1000);
                    if (guestCount ==0){
                        handler.removeMessages(GUEST_TIME_COUNT);
                        handler.sendEmptyMessage(GUEST_LEAVE_TIP);
                    }
                    break;
                case GUEST_LEAVE_TIP:
                    //掉线异常退出
                    serverManager.deleteRoom(roomId);
                    showGuestExit("主播异常离开，房间不存在");
                    break;
                case ANCHOR_TIME_COUNT:
                    anchorCount--;
                    Log.i(TAG, "handleMessage:anchorCount leaveTimeCount ="+anchorCount);
                    handler.sendEmptyMessageDelayed(ANCHOR_TIME_COUNT,1000);
                    if (anchorCount ==0){
                        handler.removeMessages(ANCHOR_TIME_COUNT);
                        handler.sendEmptyMessage(MYSELF_LEAVE_TIP);
                    }
                    break;
                case MYSELF_LEAVE_TIP:
                    isExitRoom =true;
                    Toast.makeText(ChatActivity.this, "网络异常，退出房间", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case GUEST_ABNORMAL_COUNT:
                    guestAbnormalCount--;
                    handler.sendEmptyMessageDelayed(GUEST_ABNORMAL_COUNT,1000);
                    if (guestAbnormalCount ==0){
                        handler.removeMessages(GUEST_ABNORMAL_COUNT);
                        handler.sendEmptyMessage(MYSELF_LEAVE_TIP);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void showTokenPastDueExit(){
        MessageDialog.build(this)
                .setTitle("提示")
                .setMessage("体验时长已到，点击确定退出房间")
                .setOkButton("确定")
                .setOnOkButtonClickListener((baseDialog, v) -> {
                    baseDialog.doDismiss();
                    finish();
                    return true;
                })
                .setCancelable(false)
                .show();
    }

    private void showGuestExit(String msg){
        MessageDialog.build(this)
                .setTitle("提示")
                .setMessage(msg)
                .setOkButton("确定")
                .setOnOkButtonClickListener((baseDialog, v) -> {
                    baseDialog.doDismiss();
                    finish();
                    return true;
                })
                .setCancelable(false)
                .show();
    }

    private MediaPlayerObserver mediaPlayerObserver =new MediaPlayerObserver() {
        @Override
        public void onPlayerStateChanged(PlayerConstans.MediaPlayerState var1, PlayerConstans.MediaPlayerError var2) {
            runOnUiThread(()->{
                Log.i(TAG, "onPlayerStateChanged: State ="+var1.toString()+",error ="+var2.toString());
                switch (var1){
                    case PLAYER_STATE_IDLE:
                        logAddData("媒体文件初始化成功");
                        break;
                    case PLAYER_STATE_OPENING:
                        logAddData("正在打开媒体文件");
                        break;
                    case PLAYER_STATE_OPEN_COMPLETED:
                        logAddData("打开媒体文件已完成");
                        break;
                    case PLAYER_STATE_PLAYING:
                        logAddData("正在播放媒体文件");
                        break;
                    case PLAYER_STATE_PAUSED:
                        logAddData("暂停媒体文件");
                        break;
                    case PLAYER_STATE_PLAYBACK_COMPLETED:
                        logAddData("回放媒体文件");
                        break;
                    case PLAYER_STATE_STOPPED:
                        logAddData("停止媒体文件");
                        break;
                    case PLAYER_STATE_FAILED:
                        logAddData("媒体文件错误");
                        break;
                }
            });
        }

        @Override
        public void onPositionChanged(long var1) {

        }

        @Override
        public void onPlayerEvent(PlayerConstans.MediaPlayerEvent var1) {

        }

        @Override
        public void onMetaData(PlayerConstans.MediaPlayerMetadataType var1, byte[] var2) {

        }
    };

    private void initSpData(){
        SpUtil.putInt("musicVal",40);
        SpUtil.putInt("volumeVal",60);
        SpUtil.putInt("earVal",0);
        mChatRoomManager.getRtcManager().adjustAudioMixingVolume(40);
        mChatRoomManager.getRtcManager().adjustRecordingSignalVolume(60);
        mChatRoomManager.getRtcManager().enableInEarMonitoring(false);
        mChatRoomManager.getRtcManager().setInEarMonitoringVolume(0);
    }

    private void guestExit(){
        JSONObject jsonObject =new JSONObject();
        try {
            jsonObject.put(Constants.CMD,Constants.EXIT);
            jsonObject.put(Constants.USER_NAME,SpUtil.getString(Constants.USER_NAME));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mChatRoomManager.sendChannelMessage(jsonObject.toString());
        mChatRoomManager.getRtmManager().unsubscribePeersOnlineStatus(mAnchorId);
        serverManager.updateV2UserLeaveTs(roomId);
        if (mRoomType!=Constants.R_TYPE_RTC){
            mediaPlayerKit.stop();
            mediaPlayerKit.unRegisterPlayerObserver(mediaPlayerObserver);
            mediaPlayerKit.destroy();
        }
    }

    private void anchorExitMsg(){
        JSONObject jsonObject =new JSONObject();
        try {
            jsonObject.put(Constants.CMD,Constants.EXIT);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mChatRoomManager.sendChannelMessage(jsonObject.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //initMusicVolume();
        initSpData();
        if (isAnchor){
            serverManager.deleteRoom(roomId);
            if (mRoomType== Constants.R_TYPE_CLIENT){ //客户端停止推流
                mChatRoomManager.getRtcManager().unPushStream();
            }else if(mRoomType== Constants.R_TYPE_SERVER){ //服务器停止推流
                mChatRoomManager.getRtcManager().removePublishStreamUrl(pushUrl);
            }
            mChatRoomManager.getRtcManager().unPushStream();
            serverManager.updateMusicState(Constants.MUSIC_STATE_STOP,roomId);
        }else {
            guestExit();
        }
        Constants.isEffectOpen =false;
        mMusicAnimation.cancel();
        mChatRoomManager.getRtcManager().stopAudioMixing();
        mChatRoomManager.leaveRtcChannel();
        mChatRoomManager.leaveRtmChannel();
    }
}
