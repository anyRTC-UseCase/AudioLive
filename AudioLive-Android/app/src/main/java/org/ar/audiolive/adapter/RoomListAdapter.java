package org.ar.audiolive.adapter;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.ar.audiolive.R;
import org.ar.audiolive.bean.RoomBean;

public class RoomListAdapter extends BaseQuickAdapter<RoomBean,BaseViewHolder> {

    public RoomListAdapter() {
        super(R.layout.item_room_list);
    }

    @Override
    protected void convert(BaseViewHolder holder, RoomBean roomBean) {
        ImageView imageView =holder.findView(R.id.room_bg);
        assert imageView != null;
        Glide.with(getContext())
                .load(roomBean.getImgUrl())
                .error(R.drawable.image03)
                .into(imageView);
        holder.setText(R.id.room_name,roomBean.getRoomName());
        holder.setText(R.id.room_num,roomBean.getRoomNum()+"人在看");
        //holder.itemView.click
    }
}
