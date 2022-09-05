package org.ar.audiolive.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.ar.audiolive.R;
import org.ar.audiolive.bean.LogBean;


public class LogAdapter extends BaseQuickAdapter<LogBean, BaseViewHolder> {

    public LogAdapter() {
        super(R.layout.item_guest_show);
    }

    @Override
    protected void convert(BaseViewHolder holder, LogBean logBean) {
        holder.setImageResource(R.id.img_state, logBean.getImgRes());
        holder.setText(R.id.time_date, logBean.getTimes());
        holder.setText(R.id.text_log, logBean.getTextLog());
    }
}
