package org.ar.audiolive.adapter;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.viewpager.widget.PagerAdapter;

import org.ar.audiolive.R;
import org.ar.audiolive.bean.InfoBean;
import org.ar.audiolive.model.ChannelData;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class InfoPagerAdapter extends PagerAdapter {

    private static final String TAG =InfoPagerAdapter.class.getSimpleName();

    private Context mContext;
    private HashMap<String,InfoBean> infoMap;
    private String mUserId;
    private ChannelData mChannelData;
    private List<String> mUserIdList;

    public InfoPagerAdapter(Context context,HashMap<String,InfoBean> infoMap,ChannelData channelData) {
        mContext =context;
        this.infoMap =infoMap;
        this.mChannelData =channelData;
    }

    public void setInfoMap(HashMap<String,InfoBean> infoMap,String id){
        this.infoMap =infoMap;
        mUserId =id;
    }

    public void setInfoMap(HashMap<String,InfoBean> infoMap){
        this.infoMap =infoMap;
    }

    public void setUserIdList(List<String> mUserIdList) {
        this.mUserIdList = mUserIdList;
    }

    @Override
    public int getCount() {
        return infoMap.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view==object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        String user =mUserIdList.get(position);
        View view ;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            view= Optional.ofNullable(infoMap)
                    .map((info)->info.get(user))
                    .map(InfoBean::getView)
                    .orElse(new View(mContext));
        }else {
            view=infoMap.get(user).getView();
        }
        TextView name =view.findViewById(R.id.item_name);
        TextView type =view.findViewById(R.id.item_type);
        TextView delay =view.findViewById(R.id.item_delay);
        TextView packet =view.findViewById(R.id.item_packet);
        SeekBar volume = view.findViewById(R.id.item_seek);
        if (infoMap.size()>position){
            Set<String> keys =infoMap.keySet();
            for(String key:keys){
                InfoBean infoBean =infoMap.get(key);
                assert infoBean != null;
                if (key.equals(mUserId) && position==infoBean.getPosition()){
                    if (mChannelData.isAnchor(key)){
                        if (mChannelData.isMySelf(key)){
                            type.setText(mContext.getResources().getString(R.string.info_myself));
                        }else {
                            type.setText(mContext.getResources().getString(R.string.info_anchor));
                        }
                    }else if (mChannelData.isMySelf(key)){
                        type.setText(mContext.getResources().getString(R.string.info_myself));
                    }else {
                        type.setText(mContext.getResources().getString(R.string.info_guest));
                    }
                    name.setText(mContext.getResources().getString(R.string.id,infoBean.getUid()));
                    delay.setText(mContext.getResources().getString(R.string.delay,infoBean.getDelay()));
                    packet.setText(mContext.getResources().getString(R.string.loss_rate,infoBean.getPacket()));
                    volume.setProgress(infoBean.getVolume());
                }
            }
        }
        container.addView(view);
        return view;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }
}
