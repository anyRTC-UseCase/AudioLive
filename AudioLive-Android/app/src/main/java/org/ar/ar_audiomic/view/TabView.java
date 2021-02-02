package org.ar.ar_audiomic.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import org.ar.ar_audiomic.R;

public class TabView extends FrameLayout {

    private ImageView mNormalImageView;
    private ImageView mSelectedImageView;
    private TextView mTitleView;

    private String mTitle;
    private Drawable mNormalDrawable;
    private Drawable mSelectedDrawable;
    private int mTargetColor;

    // 标题和轮廓图默认的着色
    private static final int DEFAULT_TAB_COLOR = 0xFFAFAFAF;

    //标题和轮廓图最终的着色 0x11E533
    private static final int DEFAULT_TAB_TARGET_COLOR = 0x11E533;

    public TabView(Context context, AttributeSet attr) {
        super(context,attr);
        //加载布局
        inflate(context, R.layout.tab_layout,this);
        TypedArray typedArray =context.obtainStyledAttributes(attr,R.styleable.TabView);
        for (int i = 0 ;i<typedArray.getIndexCount(); i++){
            int attribute = typedArray.getIndex(i);
            switch (attribute){
                case R.styleable.TabView_tabColor:
                    // 获取标题和轮廓最终的着色
                    mTargetColor =typedArray.getColor(attribute,DEFAULT_TAB_TARGET_COLOR);
                    break;
                case R.styleable.TabView_tabImage:
                    //获取轮廓图
                    mNormalDrawable =typedArray.getDrawable(attribute);
                    break;
                case R.styleable.TabView_tabSelectedImage:
                    //获取选中图
                    mSelectedDrawable = typedArray.getDrawable(attribute);
                    break;
                case R.styleable.TabView_tabTitle:
                    //获取标题
                    mTitle =typedArray.getString(attribute);
                    break;
            }
        }
        typedArray.recycle();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        //设置标题,默认着色为黑色
        mTitleView =findViewById(R.id.tab_title);
        mTitleView.setTextColor(DEFAULT_TAB_COLOR);
        mTitleView.setText(mTitle);

        // 设置轮廓图片，不透明，默认着色为黑色
        mNormalImageView =findViewById(R.id.tab_image);
        mNormalDrawable.setTint(DEFAULT_TAB_COLOR);
        mNormalDrawable.setAlpha(255);
        mNormalImageView.setImageDrawable(mNormalDrawable);

        // 设置选中图片，透明， 默认着色为黑色
        mSelectedImageView =findViewById(R.id.tab_selected_image);
        mSelectedDrawable.setAlpha(0);
        mSelectedImageView.setImageDrawable(mSelectedDrawable);
    }


    /**
     * 根据进度值进行变色和透明度处理
     * @param percentage 进度值,取值[0,1]
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setXPercentage(float percentage){
        if (percentage <0 || percentage >1){
            return;
        }
        // 1. 颜色变换
        int finalColor = evaluate(percentage, DEFAULT_TAB_COLOR, mTargetColor);
        mTitleView.setTextColor(finalColor);
        mNormalDrawable.setTint(finalColor);

        // 2. 透明度变换
        if (percentage >= 0.5 && percentage <=1){
            //原理如下
            //进度值:0.5 - 1
            //透明度 0 - 1
            //公式: percentage -1 =(alpha -1) *0.5
            int alpha= (int) Math.ceil(255 *((percentage -1)* 2+ 1));
            mNormalDrawable.setAlpha(255 - alpha);
            mSelectedDrawable.setAlpha(alpha);
        }else {
            mNormalDrawable.setAlpha(255);
            mSelectedDrawable.setAlpha(0);
        }
        //3.更新UI
        invalidateUI();
    }


    /**
     * 根据不同线程更新UI。
     */
    private void invalidateUI() {
        if (Looper.myLooper() == Looper.getMainLooper()){
            //主线程
            invalidate();
        }else {
            //工作线程
            postInvalidate();
        }
    }

    /**
     * 计算不同进度值对应的颜色值,这个方法取自 ArgbEvaluator.java 类.
     * @param percentage  进度值,范围[0,1]
     * @param startValue 起始颜色值
     * @param endValue 最终颜色值
     * @return  返回与进度值相应的颜色值
     */
    private int evaluate(float percentage, int startValue, int endValue) {
        int startInt = startValue ;
        float startA = ((startInt >> 24) & 0xff) / 255.0f;
        float startR = ((startInt >> 16) & 0xff) / 255.0f;
        float startG = ((startInt >>  8) & 0xff) / 255.0f;
        float startB = ( startInt        & 0xff) / 255.0f;

        int endInt = endValue;
        float endA = ((endInt >> 24) & 0xff) / 255.0f;
        float endR = ((endInt >> 16) & 0xff) / 255.0f;
        float endG = ((endInt >>  8) & 0xff) / 255.0f;
        float endB = ( endInt        & 0xff) / 255.0f;

        // convert from sRGB to linear
        startR = (float) Math.pow(startR, 2.2);
        startG = (float) Math.pow(startG, 2.2);
        startB = (float) Math.pow(startB, 2.2);

        endR = (float) Math.pow(endR, 2.2);
        endG = (float) Math.pow(endG, 2.2);
        endB = (float) Math.pow(endB, 2.2);

        // compute the interpolated color in linear space
        float a = startA + percentage * (endA - startA);
        float r = startR + percentage * (endR - startR);
        float g = startG + percentage * (endG - startG);
        float b = startB + percentage * (endB - startB);

        a = a * 255.0f;
        r = (float) Math.pow(r, 1.0 / 2.2) * 255.0f;
        g = (float) Math.pow(g, 1.0 / 2.2) * 255.0f;
        b = (float) Math.pow(b, 1.0 / 2.2) * 255.0f;


        Log.i("TabView", "evaluate: return ="+(Math.round(a) << 24 | Math.round(r) << 16 | Math.round(g) << 8 | Math.round(b)));
        return Math.round(a) << 24 | Math.round(r) << 16 | Math.round(g) << 8 | Math.round(b);
    }
}
