package org.ar.audiolive.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.ar.audiolive.R;
import org.ar.audiolive.model.MusicItemBean;
import org.ar.audiolive.util.Constants;

public class MusicAdapter extends BaseQuickAdapter<MusicItemBean, BaseViewHolder> {

    public MusicAdapter() {
        super(R.layout.item_music_list);
    }

    @Override
    protected void convert(BaseViewHolder helper, MusicItemBean musicItemBean) {
        helper.setText(R.id.music_name,musicItemBean.getMusicName());
        switch (musicItemBean.getState()){
            case Constants.MUSIC_STATE_STOP:
                helper.setVisible(R.id.music_stop,false);
                helper.setImageResource(R.id.music_play,R.drawable.playing);
                break;
            case Constants.MUSIC_STATE_PLAY:
                helper.setVisible(R.id.music_stop,true);
                helper.setImageResource(R.id.music_play,R.drawable.pause);
                break;
            case Constants.MUSIC_STATE_PAUSE:
                helper.setVisible(R.id.music_stop,true);
                helper.setImageResource(R.id.music_play,R.drawable.playing);
                break;
            default:
                break;
        }
    }
}
