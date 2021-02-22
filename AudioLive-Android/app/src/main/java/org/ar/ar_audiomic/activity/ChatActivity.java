package org.ar.ar_audiomic.activity;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
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

import org.ar.ar_audiomic.R;
import org.ar.ar_audiomic.adapter.LogAdapter;
import org.ar.ar_audiomic.adapter.InfoPagerAdapter;
import org.ar.ar_audiomic.adapter.MessageAdapter;
import org.ar.ar_audiomic.bean.AddBean;
import org.ar.ar_audiomic.bean.JoinRoomBean;
import org.ar.ar_audiomic.bean.LogBean;
import org.ar.ar_audiomic.bean.InfoBean;
import org.ar.ar_audiomic.bean.MessageListBean;
import org.ar.ar_audiomic.bean.MusicBean;
import org.ar.ar_audiomic.dialog.ApplyDialog;
import org.ar.ar_audiomic.dialog.AutoTipDialog;
import org.ar.ar_audiomic.dialog.CommentDialogFragment;
import org.ar.ar_audiomic.dialog.LogDialog;
import org.ar.ar_audiomic.dialog.VolumeDialog;
import org.ar.ar_audiomic.manager.ARServerManager;
import org.ar.ar_audiomic.manager.ChatRoomEventListener;
import org.ar.ar_audiomic.manager.ChatRoomManager;
import org.ar.ar_audiomic.model.ChannelData;
import org.ar.ar_audiomic.model.Member;
import org.ar.ar_audiomic.util.Constants;
import org.ar.ar_audiomic.util.SpUtil;
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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener, ChatRoomEventListener {

    private static final String TAG =ChatActivity.class.getSimpleName();

    private ImageView back;
    private TextView mTitle,mLog;

    private ViewPager mViewPager;
    private LinearLayout dotLayout;

    private ImageView imgMusic,mMusicState;
    private TextView mMusicName;

    private RecyclerView rvChatMsg,rvGuestLog;
    private LinearLayoutManager msgLayoutManager;
    private LinearLayoutManager guestLayoutManager;
    private MessageAdapter messageAdapter;
    private LogAdapter logAdapter;

    private TextView mChatInput;
    private ImageView mJoinMic,mChatMic,mSpeaker,mVolume;

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

    private static final int MUSIC_STATE_PLAY=1;
    private static final int MUSIC_STATE_PAUSE=2;

    private FrameLayout mFrameLayout;
    private RelativeLayout rlMusic;
    private String mAnchorId;
    private String mUserId;
    private String roomId;
    private String roomName;
    private String pushUrl;
    private String pullRtmpUrl;
    private boolean isAnchor;
    private boolean isMic,isSpeaker,isPlay,isPlaying,isJoinMic,isExitRoom;

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
    private AutoTipDialog autoTipDialog;
    private ApplyDialog applyDialog;
    private HashMap<String,InfoBean> infoMap;
    private List<LogBean> mLogList;
    //申请上麦人
    private HashMap<String,ApplyDialog> applyUsersMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initView();
        init();
        initMusicVolume();
        serverManager.getMusicList();
    }

    private void initView() {
        back =findViewById(R.id.back);
        mTitle =findViewById(R.id.title_content);
        mLog =findViewById(R.id.save);
        mViewPager =findViewById(R.id.vp_info);
        dotLayout =findViewById(R.id.dotlayout);
        imgMusic =findViewById(R.id.img_music);
        mMusicName =findViewById(R.id.music_name);
        mMusicState =findViewById(R.id.music_state);
        rvChatMsg =findViewById(R.id.rv_chat);
        rvGuestLog =findViewById(R.id.rv_show_log);
        mChatInput =findViewById(R.id.chat_input);
        mJoinMic =findViewById(R.id.chat_join_mic);
        mChatMic =findViewById(R.id.chat_mic);
        mSpeaker =findViewById(R.id.chat_speaker);
        mVolume =findViewById(R.id.chat_volume);
        mFrameLayout =findViewById(R.id.fl_info);
        rlMusic =findViewById(R.id.rl_music);
    }

    private void init() {
        isAnchor =getIntent().getBooleanExtra(Constants.INTENT_IS_ANCHOR,false);
        roomId = getIntent().getStringExtra(Constants.INTENT_ROOM_ID);
        roomName =getIntent().getStringExtra(Constants.INTENT_ROOM_NAME);
        mUserId =SpUtil.getString(Constants.UID);
        serverManager =ARServerManager.getInstance();
        mMusicAnimation = AnimationUtils.loadAnimation(this,R.anim.animation_rotate);
        mLogList =new ArrayList<>();
        mUserIdList =new ArrayList<>();
        applyUsersMap =new HashMap<>();
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
        mLog.setVisibility(View.VISIBLE);
        mLog.setText(getString(R.string.log));
        mLog.setTextColor(getResources().getColor(R.color.white));
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
        mLogList.add(new LogBean(R.drawable.blue_circle,getDate(),"初始化完成"));
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
        pushUrl =getIntent().getStringExtra(Constants.INTENT_PUSH_URL);
        mAnchorId =mUserId;
        rlMusic.setVisibility(View.VISIBLE);
        rvGuestLog.setVisibility(View.GONE);
        mChannelData.setAnchorId(mAnchorId);
        mJoinMic.setVisibility(View.GONE);
        mFrameLayout.setVisibility(View.VISIBLE);
        volumeDialog =new VolumeDialog(this,mChatRoomManager);
        Log.i(TAG, "showAnchor: pushUrl ="+pushUrl);
        mChatRoomManager.getRtcManager().pushStream(pushUrl); //主播推流
    }

    private void showGuest(){
        mAnchorId =SpUtil.getString(Constants.ANCHOR_UID);
        pullRtmpUrl =getIntent().getStringExtra(Constants.INTENT_PULL_RTMP_URL);
        mChannelData.setAnchorId(mAnchorId);
        rlMusic.setVisibility(View.INVISIBLE);
        rvGuestLog.setVisibility(View.VISIBLE);
        mFrameLayout.setVisibility(View.GONE);
        mChatMic.setVisibility(View.GONE);
        mSpeaker.setVisibility(View.GONE);
        mVolume.setVisibility(View.GONE);
        mChannelData.addOrUpdateMember(new Member(mAnchorId,"主播"));
        initPlayer(false);
    }

    private void logAddData(String log){
        logAdapter.addData(new LogBean(R.drawable.circle,getDate(),log));
    }

    private String getDate(){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(new Date());
    }

    private void setListener(){
        mMusicState.setOnClickListener(this);
        mChatInput.setOnClickListener(this);
        mJoinMic.setOnClickListener(this);
        mChatMic.setOnClickListener(this);
        mSpeaker.setOnClickListener(this);
        mVolume.setOnClickListener(this);
        mLog.setOnClickListener(this);
        back.setOnClickListener(this);
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

    private void initMusicVolume(){
        mChatRoomManager.getRtcManager().adjustAudioMixingVolume(40);
        mChatRoomManager.getRtcManager().adjustRecordingSignalVolume(60);
        mChatRoomManager.getRtcManager().enableInEarMonitoring(false);
        mChatRoomManager.getRtcManager().setInEarMonitoringVolume(0);
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
            case R.id.music_state:
                if (isAnchor){
                    playMusic();
                }else {
                    Toast.makeText(this, "只能主播才能播放音乐", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.chat_input:
                CommentDialogFragment dialogFragment = new CommentDialogFragment();
                dialogFragment.show(getSupportFragmentManager(), text -> {
                    mChatRoomManager.sendChannelMessage(text);
                    addMessage(new MessageListBean(MessageListBean.MSG_NORMAL, "我："+text));
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
            case R.id.chat_volume:
                volumeDialog.show();
                break;
            case R.id.save:
                LogDialog logDialog =new LogDialog(this,mLogList);
                logDialog.show();
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
    private MusicBean.DataBean getMusicData(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 根对象为空就创建一个空Optional，否则就创建一个根对象的Optional
            return Optional.ofNullable(serverManager)
                    // 根对象为空就直接返回空Optional，否则返回这个值的 Optional
                    .map(ARServerManager::getMusicBean)
                    .map(MusicBean::getData)
                    .map((music) ->music.get(0))
                    .orElse(new MusicBean.DataBean()); //取不到值，new一个新对象
        }else {
            return null;
        }
    }


    //播放音乐
    private void playMusic(){
        int state ;
        int musicId = Objects.requireNonNull(getMusicData()).getMusicId();
        String name =getMusicData().getMusicName();
        String singer =getMusicData().getSinger();
        String path =getMusicData().getMusicUrl();
        JSONObject object =new JSONObject();
        Log.i(TAG, "onClick: path ="+path);
        if (isPlay){
            mChatRoomManager.getRtcManager().pauseAudioMixing();
            isPlay =false;
            state =2;
            imgMusic.clearAnimation();
        }else {
            if (isPlaying){
                mChatRoomManager.getRtcManager().resumeAudioMixing();
                isPlay =true;
            }else {
                mChatRoomManager.getRtcManager().startAudioMixing(path);
                isPlay =true;
                isPlaying =true;
            }
            state =1;
            imgMusic.startAnimation(mMusicAnimation);
        }
        switch (state){
            case MUSIC_STATE_PLAY: //播放音乐
                try {
                    object.put("cmd",Constants.MUSIC_PLAYING);
                    object.put("musicName",name);
                    object.put("singer",singer);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mChatRoomManager.sendChannelMessage(object.toString());
                addMessage(new MessageListBean(MessageListBean.MSG_SYSYTEM, "主播播放了音乐"));
                mLogList.add(new LogBean(R.drawable.blue_circle,getDate(),"播放音乐完成"));
                break;
            case MUSIC_STATE_PAUSE: //暂停音乐
                try {
                    object.put("cmd",Constants.MUSIC_PAUSE);
                    object.put("musicName",name);
                    object.put("singer",singer);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mChatRoomManager.sendChannelMessage(object.toString());
                addMessage(new MessageListBean(MessageListBean.MSG_SYSYTEM, "主播暂停了音乐"));
                mLogList.add(new LogBean(R.drawable.blue_circle,getDate(),"暂停音乐完成"));
                break;
            default:
                break;
        }
        serverManager.addMusic(musicId,state,roomId);
        mMusicName.setText(getString(R.string.music_name,singer,name));
        mMusicState.setSelected(isPlay);
    }

    //游客上麦
    private void joinMic(){
        JSONObject jsonObject =new JSONObject();
        switch (micState){
            case MIC_STATE_UP: //上麦
                try {
                    jsonObject.put("cmd","apply");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mChatRoomManager.getRtmManager().sendMessageToPeer(mAnchorId,jsonObject.toString(),null);
                micState =MIC_STATE_CANCEL;
                mJoinMic.setImageResource(R.drawable.cancel_mic);
                mLogList.add(new LogBean(R.drawable.blue_circle,getDate(),"申请上麦成功"));
                break;
            case MIC_STATE_CANCEL: //取消上麦
                try {
                    jsonObject.put("cmd","cancelApply");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mChatRoomManager.getRtmManager().sendMessageToPeer(mAnchorId,jsonObject.toString(),null);
                micState =MIC_STATE_UP;
                mJoinMic.setImageResource(R.drawable.up_mic);
                mLogList.add(new LogBean(R.drawable.blue_circle,getDate(),"取消上麦成功"));
                break;
            case MIC_STATE_DOWN: //下麦
                mJoinMic.setImageResource(R.drawable.up_mic);
                micState =MIC_STATE_UP;
                isJoinMic =false;
                mChatRoomManager.leaveRtcChannel();
                mChatMic.setVisibility(View.GONE);
                mSpeaker.setVisibility(View.GONE);
                mFrameLayout.setVisibility(View.GONE);
                rvGuestLog.setVisibility(View.VISIBLE);
                rlMusic.setVisibility(View.INVISIBLE);
                infoMap.clear();
                mUserIdList.clear();
                infoAdapter.setInfoMap(infoMap);
                infoAdapter.notifyDataSetChanged();
                initDots();
                mediaPlayerKit.play();
                mediaPlayerKit.mute(false);
                mLogList.add(new LogBean(R.drawable.blue_circle,getDate(),"下麦成功"));
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
            return null;
        }
    }

    //更新游客音乐状态
    private void updateGuestMusic(){
        if (!isAnchor){
            if (getRoom()!=null){
                int musicState =getRoom().getMusicState();
                String name =getRoom().getMusic().getMusicName();
                String singer =getRoom().getMusic().getSinger();
                if (musicState ==1){
                    Log.i(TAG, "updateGuestMusic: -===>");
                    imgMusic.startAnimation(mMusicAnimation);
                    mMusicName.setText(getString(R.string.music_name,singer,name));
                    mMusicState.setSelected(true);
                }else if (musicState==2){
                    mMusicName.setText(getString(R.string.music_name,singer,name));
                    mMusicState.setSelected(false);
                }
            }
        }
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
                mLogList.add(new LogBean(R.drawable.blue_circle,getDate(),"网络异常"));
                if (isAnchor){
                    handler.sendEmptyMessageDelayed(ANCHOR_TIME_COUNT,1000);
                }else {
                    handler.sendEmptyMessageDelayed(GUEST_ABNORMAL_COUNT,1000);
                }
                Log.i(TAG, "onRtmConnectStateChange:--4-> state ="+state+",reason="+reason);
            }else if (state ==3){
                Log.i(TAG, "onRtmConnectStateChange:--3-> state ="+state+",reason="+reason);
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
                    mLogList.add(new LogBean(R.drawable.blue_circle,getDate(),"网络重连成功"));
                    if (isAnchor){
                        mChatRoomManager.getRtcManager().stopAudioMixing();
                        int musicId = Objects.requireNonNull(getMusicData()).getMusicId();
                        if (isPlay){
                            addMessage(new MessageListBean(MessageListBean.MSG_SYSYTEM, "主播暂停了音乐"));
                            JSONObject object =new JSONObject();
                            String name =getMusicData().getMusicName();
                            String singer =getMusicData().getSinger();
                            try {
                                object.put("cmd",Constants.MUSIC_PAUSE);
                                object.put("musicName",name);
                                object.put("singer",singer);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            mChatRoomManager.sendChannelMessage(object.toString());
                        }
                        isPlay =false;
                        isPlaying =false;
                        serverManager.addMusic(musicId,2,roomId);
                        mMusicState.setSelected(isPlay);
                        imgMusic.clearAnimation();
                        mChatRoomManager.getRtcManager().createStreamingInstance();
                        mChatRoomManager.getRtcManager().pushStream(pushUrl);
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
            mLogList.add(new LogBean(R.drawable.blue_circle,getDate(),"加入频道成功"));
            Log.i(TAG, "onJoinChannelSuccess:  uid ="+uid);
            View view =inflater.inflate(R.layout.item_vp_info,null,false);
            if (!mUserIdList.contains(uid)){
                mUserIdList.add(uid);
            }
            infoMap.put(uid,new InfoBean(uid,view,0));
            infoAdapter.setInfoMap(infoMap,uid);
            infoAdapter.setUserIdList(mUserIdList);
            infoAdapter.notifyDataSetChanged();
            initDots();
            updateGuestMusic();
        });
    }

    /**
     * 加入/离开房间
     * @param userId ID
     * @param isLine true:加入房间，false：离开房间
     */
    @Override
    public void onUserLineChanged(String userId, Boolean isLine) {
        runOnUiThread(()->{
            Log.i(TAG, "onUserLineChanged: 小伙伴加入成功！="+userId);
            if (isLine){
                applyUsersMap.remove(userId);
                if(!mChannelData.isAnchor(userId)){
                    addMessage(new MessageListBean(MessageListBean.MSG_SYSYTEM,mChannelData.getName(userId)+"加入了麦序"));
                }
                if (!mUserIdList.contains(userId)){
                    mUserIdList.add(userId);
                }
                int pos =-1;
                for (int i = 0; i <mUserIdList.size() ; i++) {
                    if (userId.equals(mUserIdList.get(i))){
                        pos =i;
                    }
                }
                View view =inflater.inflate(R.layout.item_vp_info,null,false);
                infoMap.put(userId,new InfoBean(userId,view,pos));
                infoAdapter.setInfoMap(infoMap,userId);
                infoAdapter.setUserIdList(mUserIdList);
                infoAdapter.notifyDataSetChanged();
                initDots();
            }else {
                addMessage(new MessageListBean(MessageListBean.MSG_SYSYTEM,mChannelData.getName(userId)+"退出了麦序"));
                mUserIdList.remove(userId);
                if (infoMap.containsKey(userId)){
                    infoMap.remove(userId);
                    infoAdapter.setInfoMap(infoMap);
                    infoAdapter.setUserIdList(mUserIdList);
                    infoAdapter.notifyDataSetChanged();
                    initDots();
                }
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
              Log.i(TAG, "onRequestToken: ===>");
              JSONObject jsonObject =new JSONObject();
              try {
                  jsonObject.put("cmd",Constants.TOKEN_PAST_DUE);
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
            JSONObject object =new JSONObject();
            applyDialog =new ApplyDialog(this, "连麦申请", mChannelData.getName(userId) + "申请连麦",
                    "拒绝", "确定", () -> {
                try {
                    object.put("cmd",Constants.MIC_REJECT_LINE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mChatRoomManager.getRtmManager().sendMessageToPeer(userId,object.toString(),null);
                applyUsersMap.remove(userId);
            }, () -> {
                try {
                    object.put("cmd",Constants.MIC_ACCEPT_LINE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mChatRoomManager.getRtmManager().sendMessageToPeer(userId,object.toString(),null);
                applyUsersMap.remove(userId);
            });
            applyDialog.setCancelable(false);
            applyUsersMap.put(userId,applyDialog);
            for(Map.Entry<String,ApplyDialog> map:applyUsersMap.entrySet()){
                String id = map.getKey();
                ApplyDialog dialog = map.getValue();
                if (TextUtils.equals(id,userId)){
                    dialog.show();
                }else {
                    try {
                        object.put("cmd",Constants.MIC_ACCEPT_LINE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mChatRoomManager.getRtmManager().sendMessageToPeer(id,object.toString(),null);
                    dialog.dismiss();
                }
            }
            mLogList.add(new LogBean(R.drawable.blue_circle,getDate(),mChannelData.getName(userId)+"申请上麦完成"));
        });

    }

    /**
     * 取消上麦，返回主播
     * @param userId 取消者
     */
    @Override
    public void onCancelApplyUpdated(String userId) {
        runOnUiThread(()->{
            applyUsersMap.remove(userId);
            if (applyDialog.isShowing()){
                applyDialog.dismiss();
            }
            autoTipDialog =new AutoTipDialog(this,R.drawable.red_tip,userId+"取消上麦");
            autoTipDialog.show();
            mLogList.add(new LogBean(R.drawable.blue_circle,getDate(),userId+"取消上麦完成"));
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
            mLogList.add(new LogBean(R.drawable.blue_circle,getDate(),"拒绝上麦"));
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
            String rtcToken = Optional.ofNullable(serverManager)
                    .map(ARServerManager::getJoinRoomBean)
                    .map(JoinRoomBean::getData)
                    .map(JoinRoomBean.DataBean::getRoom)
                    .map(JoinRoomBean.DataBean.RoomBeans::getRtcToken)
                    .orElse("");
            Log.i(TAG, "onAcceptLineUpdated: rtcToken ="+rtcToken);
            mChatRoomManager.joinRtcChannel(rtcToken,roomId);
            mediaPlayerKit.mute(true);
            if (isMic){
                mChatMic.setImageResource(R.drawable.mic_close);
            }else {
                mChatMic.setImageResource(R.drawable.mic_open);
            }
            mChatRoomManager.getRtcManager().muteLocalAudioStream(isMic);
            if (isSpeaker){
                mSpeaker.setImageResource(R.drawable.speaker_close);
            }else {
                mSpeaker.setImageResource(R.drawable.speaker_open);
            }
            mChatRoomManager.getRtcManager().getRtcEngine().setEnableSpeakerphone(!isSpeaker);
            rlMusic.setVisibility(View.VISIBLE);
            mChatMic.setVisibility(View.VISIBLE);
            mSpeaker.setVisibility(View.GONE);
            mFrameLayout.setVisibility(View.VISIBLE);
            rvGuestLog.setVisibility(View.GONE);
            mLogList.add(new LogBean(R.drawable.blue_circle,getDate(),"上麦成功"));
        });
    }

    /**
     * 主播离开
     * @param userId 主播ID
     */
    @Override
    public void onAnchorExit(String userId) {
        runOnUiThread(()->{
            if (!isAnchor){
                showGuestExit("主播离开，房间不存在");
            }
        });
    }

    /**
     * 返回游客：更新音乐状态
     * @param isPlay 是否在播放
     * @param name 音乐名称
     * @param singer 音乐歌手
     */
    @Override
    public void onMusicUpdated(boolean isPlay,String name,String singer) {
        runOnUiThread(()->{
            if (!isAnchor){
                if (isPlay){
                    imgMusic.startAnimation(mMusicAnimation);
                    mMusicState.setSelected(true);
                    addMessage(new MessageListBean(MessageListBean.MSG_SYSYTEM, "主播播放了音乐"));
                    mLogList.add(new LogBean(R.drawable.blue_circle,getDate(),"主播播放成功"));
                }else {
                    imgMusic.clearAnimation();
                    mMusicState.setSelected(false);
                    addMessage(new MessageListBean(MessageListBean.MSG_SYSYTEM, "主播暂停了音乐"));
                    mLogList.add(new LogBean(R.drawable.blue_circle,getDate(),"暂停音乐成功"));
                }
                mMusicName.setText(getString(R.string.music_name,singer,name));
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

    private final Handler handler =new Handler(){
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
    }

    private void guestExit(){
        serverManager.updateV2UserLeaveTs(roomId);
        mediaPlayerKit.stop();
        mediaPlayerKit.unRegisterPlayerObserver(mediaPlayerObserver);
        mediaPlayerKit.destroy();
    }

    private void anchorExitMsg(){
        JSONObject jsonObject =new JSONObject();
        try {
            jsonObject.put("cmd",Constants.ANCHOR_EXIT);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mChatRoomManager.getRtmManager().sendChannelMessage(jsonObject.toString(),null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        initSpData();
        if (isAnchor){
            serverManager.deleteRoom(roomId);
            mChatRoomManager.getRtcManager().unPushStream();
            int musicId = Objects.requireNonNull(getMusicData()).getMusicId();
            serverManager.addMusic(musicId,MUSIC_STATE_PAUSE,roomId);
        }else {
            guestExit();
        }
        mMusicAnimation.cancel();
        mChatRoomManager.getRtcManager().stopAudioMixing();
        mChatRoomManager.leaveRtcChannel();
        mChatRoomManager.leaveRtmChannel();
    }
}
