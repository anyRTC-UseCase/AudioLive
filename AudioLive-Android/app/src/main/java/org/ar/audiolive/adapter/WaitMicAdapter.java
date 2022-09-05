package org.ar.audiolive.adapter;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import android.content.Context;
import org.ar.audiolive.R;
import org.ar.audiolive.model.WaitMicBean;

import de.hdodenhof.circleimageview.CircleImageView;

public class WaitMicAdapter extends BaseQuickAdapter<WaitMicBean, BaseViewHolder> {

    private final Context context;
    public WaitMicAdapter(Context context) {
        super(R.layout.wait_mic_item);
        this.context=context;
    }

    @Override
    protected void convert(BaseViewHolder helper, WaitMicBean item) {
        helper.setText(R.id.wait_mic_name,item.waitName);
        CircleImageView waitAvatar = helper.getView(R.id.wait_mic_avatar);
        Glide.with(context)
                .load(item.waitAvatar)
                .error(R.drawable.ic_unkown)
                .into(waitAvatar);
    }
}
