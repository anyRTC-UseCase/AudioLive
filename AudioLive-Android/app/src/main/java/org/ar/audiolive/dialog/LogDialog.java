package org.ar.audiolive.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.ar.audiolive.R;
import org.ar.audiolive.adapter.LogAdapter;
import org.ar.audiolive.bean.LogBean;

import java.util.List;
import java.util.Objects;

public class LogDialog extends Dialog {

    private RecyclerView rvLog;
    private LinearLayoutManager linearLayoutManager;
    private LogAdapter logAdapter;
    private ImageView mBack;
    private TextView mTitle;
    private List<LogBean> mLogList;
    private Context mContext;

    public LogDialog(@NonNull Context context, List<LogBean> mLogList) {
        super(context);
        mContext =context;
        this.mLogList =mLogList;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        rvLog=findViewById(R.id.rv_log);
        mBack =findViewById(R.id.back);
        mTitle =findViewById(R.id.title_content);

        mTitle.setText(mContext.getResources().getString(R.string.log));
        linearLayoutManager =new LinearLayoutManager(mContext);
        logAdapter =new LogAdapter();
        rvLog.setLayoutManager(linearLayoutManager);
        rvLog.setAdapter(logAdapter);
        for (LogBean logBean:mLogList) {
            logAdapter.addData(logBean);
        }
        mBack.setOnClickListener(view -> dismiss());
    }
}
