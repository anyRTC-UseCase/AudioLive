package org.ar.audiolive.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.ar.audiolive.R;
import org.ar.audiolive.adapter.MusicAdapter;
import org.ar.audiolive.bean.MusicBean;
import org.ar.audiolive.bean.RoomMusicInfoBean;
import org.ar.audiolive.manager.ARServerManager;
import org.ar.audiolive.manager.ChatRoomManager;
import org.ar.audiolive.model.MusicItemBean;
import org.ar.audiolive.util.Constants;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MusicActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG =MusicActivity.class.getSimpleName();
    private RecyclerView mMusicRecycler;
    private LinearLayoutManager linearLayoutManager;
    private ImageView mBack;
    private MusicAdapter musicAdapter;
    private ARServerManager serverManager;
    private MusicBean mMusicBean;
    private ChatRoomManager mChatRoomManager;
    private String roomId;
    private int mPrePos =-1; //记录上一个播放的位置

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);
        mMusicRecycler =findViewById(R.id.music_recycle);
        mBack =findViewById(R.id.music_back);
        serverManager =ARServerManager.getInstance();
        mChatRoomManager =ChatRoomManager.instance(this);
        roomId =getIntent().getStringExtra(Constants.INTENT_ROOM_ID);
        linearLayoutManager =new LinearLayoutManager(this);
        musicAdapter =new MusicAdapter();
        mMusicRecycler.setLayoutManager(linearLayoutManager);
        mMusicRecycler.setAdapter(musicAdapter);
        serverManager.getMusicList();
        updateMusicList();
        setListener();
    }

    private void updateMusicList(){
        serverManager.setMusicListListener(new ARServerManager.MusicListListener() {
            @Override
            public void getMusicList(MusicBean musicBean) {
                List<MusicItemBean>  musicItemBeans =new ArrayList<>();
                for (int i = 0; i <musicBean.getData().size() ; i++) {
                    musicItemBeans.add(new MusicItemBean(musicBean.getData().get(i).getMusicName()));
                }
                musicAdapter.setNewInstance(musicItemBeans);
                mMusicBean =musicBean;
                serverManager.getRoomMusicInfo(roomId);
            }

            @Override
            public void getRoomMusicInfo(RoomMusicInfoBean roomMusicInfoBean) {
                int musicId =roomMusicInfoBean.getData().getMusicId();
                int state =roomMusicInfoBean.getData().getMusicState();
                Log.i(TAG, "getRoomMusicInfo: musicId ="+musicId+",state ="+state);
                if (musicId!=0){
                    if (state!=0){
                        mPrePos =musicId-1;
                    }
                    musicAdapter.getItem((musicId-1)).setState(state);
                    musicAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    private void setListener() {
        mBack.setOnClickListener(this);
        musicAdapter.addChildClickViewIds(R.id.music_play,R.id.music_stop);
        musicAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            //0：停止；1：播放；2：暂停；3：切歌
            int musicState = -1;
            String musicName="";
            switch (view.getId()){
                case R.id.music_play:
                    int musicId =0;
                    String path ="";
                    if (mMusicBean!=null){
                        path =mMusicBean.getData().get(position).getMusicUrl();
                        musicId =mMusicBean.getData().get(position).getMusicId();
                        musicName =mMusicBean.getData().get(position).getMusicName();
                    }
                    Log.i(TAG, "setListener: getState ="+musicAdapter.getItem(position).getState()+",mPrePos="+mPrePos+",pso="+position);
                    if (musicAdapter.getItem(position).getState()==Constants.MUSIC_STATE_PLAY){  //是否为播放状态
                        musicAdapter.getItem(position).setState(Constants.MUSIC_STATE_PAUSE);
                        mChatRoomManager.getRtcManager().pauseAudioMixing();
                        serverManager.updateMusicState(Constants.MUSIC_STATE_PAUSE,roomId);
                        musicState =2;
                    }else {
                        if (mPrePos ==-1){ //第一次播放音乐
                            musicAdapter.getItem(position).setState(Constants.MUSIC_STATE_PLAY);
                            mChatRoomManager.getRtcManager().startAudioMixing(path);
                            serverManager.addMusic(musicId,Constants.MUSIC_STATE_PLAY,roomId);
                            musicState =1;
                        }else {
                            musicAdapter.getItem(position).setState(Constants.MUSIC_STATE_PLAY);
                            if (mPrePos==position){  //操作同一个音乐
                                mChatRoomManager.getRtcManager().resumeAudioMixing();
                                serverManager.updateMusicState(Constants.MUSIC_STATE_PLAY,roomId);
                                musicState =1;
                            }else {
                                musicAdapter.getItem(mPrePos).setState(Constants.MUSIC_STATE_STOP);
                                mChatRoomManager.getRtcManager().stopAudioMixing();
                                mChatRoomManager.getRtcManager().startAudioMixing(path);
                                serverManager.addMusic(musicId,Constants.MUSIC_STATE_PLAY,roomId);
                                musicState =1;
                            }
                        }
                    }
                    mPrePos =position;
                    musicAdapter.notifyDataSetChanged();
                    break;
                case R.id.music_stop:
                    mPrePos=-1;
                    musicAdapter.getItem(position).setState(Constants.MUSIC_STATE_STOP);
                    musicAdapter.notifyDataSetChanged();
                    mChatRoomManager.getRtcManager().stopAudioMixing();
                    serverManager.updateMusicState(Constants.MUSIC_STATE_STOP,roomId);
                    musicState =0;
                    break;
                default:
                    break;
            }
            Log.i(TAG, "setListener: musicState ="+musicState);
            switch (musicState){
                case Constants.MUSIC_STATE_STOP:
                    sendChannelMessage(Constants.MUSIC_STOP,musicName);
                    break;
                case Constants.MUSIC_STATE_PLAY:
                    sendChannelMessage(Constants.MUSIC_PLAYING,musicName);
                    break;
                case Constants.MUSIC_STATE_PAUSE:
                    sendChannelMessage(Constants.MUSIC_PAUSE,musicName);
                    break;
                default:
                    break;
            }

        });
    }

    //播放音乐
    private void sendChannelMessage(String cmd,String musicName){
        JSONObject object =new JSONObject();
        try {
            object.put(Constants.CMD,cmd);
            object.put(Constants.MUSIC_NAME,musicName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mChatRoomManager.sendChannelMessage(object.toString());
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.music_back) {
            finish();
        }
    }
}
