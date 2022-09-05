package org.ar.audiolive.adapter;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.ar.audiolive.R;
import org.ar.audiolive.bean.MessageListBean;

public class MessageAdapter extends BaseQuickAdapter<MessageListBean, BaseViewHolder> {

    public MessageAdapter() {
        super(R.layout.item_message_list);
    }

    @Override
    protected void convert(BaseViewHolder helper, MessageListBean item) {
        LinearLayout layout =helper.getView(R.id.ll_msg);
        TextView tvMsg = helper.getView(R.id.tv_message);
        switch (item.type){
            case MessageListBean.MSG_SYSYTEM:
                tvMsg.setTextColor(getContext().getResources().getColor(R.color.msg_text_system));
                layout.setBackgroundResource(R.drawable.shape_msg_text_bg);
                tvMsg.setText(item.content);
                break;
            case MessageListBean.MSG_NORMAL:
                tvMsg.setTextColor(getContext().getResources().getColor(R.color.msg_text_normal_msg));
                layout.setBackgroundResource(R.drawable.shape_msg_text_bg);
                SpannableString content =new SpannableString(item.content);
                content.setSpan(new ForegroundColorSpan(Color.parseColor("#7CE3FF")),
                        0,item.content.indexOf("："), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tvMsg.setText(content);
                break;
            case MessageListBean.MSG_JOIN_LEFT_ROOM:
                tvMsg.setTextColor(getContext().getResources().getColor(R.color.white));
                layout.setBackgroundResource(R.drawable.shape_msg_left_bg);
                tvMsg.setText(item.content);
                break;
            default:
                break;
        }
    }
}
