package com.fishpan.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 加载更多View
 * Created by yupan on 17/6/24.
 */
public class XRecyclerViewFooter extends LinearLayout {
    public final static int STATE_NORMAL = 0;
    public final static int STATE_READY = 1;
    public final static int STATE_LOADING = 2;

    private float mNormalHeight;
    private View mContent;
    private View mProgressBar;
    private TextView mHintView;

    public XRecyclerViewFooter(Context context) {
        this(context, null);
    }

    public XRecyclerViewFooter(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XRecyclerViewFooter(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView(){
        LayoutInflater.from(getContext()).inflate(R.layout.xrecycler_footer, this, true);
        mContent = findViewById(R.id.xrecyclerview_footer_content);
        mProgressBar = mContent.findViewById(R.id.xrecyclerview_footer_progressbar);
        mHintView = (TextView) mContent.findViewById(R.id.xrecyclerview_footer_hint_textview);

        mNormalHeight = ViewUtils.dp2px(getContext(), 60);
    }

    public void setStatus(int status){
        switch (status){
            case STATE_READY:
                mProgressBar.setVisibility(View.GONE);
                mHintView.setText(R.string.xrecyclerview_footer_hint_ready);
                mHintView.setVisibility(VISIBLE);
                break;
            case STATE_LOADING:
                mHintView.setVisibility(GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                setVisiableHeight((int) mNormalHeight);
                break;
            case STATE_NORMAL:
                mProgressBar.setVisibility(View.GONE);
                mHintView.setText(R.string.xrecyclerview_footer_hint_normal);
                mHintView.setVisibility(VISIBLE);
                break;
        }
    }

    public void dismiss(){
        setVisiableHeight(0);
    }

    public void setVisiableHeight(float height) {
        LayoutParams lp = (LayoutParams) mContent.getLayoutParams();
        if(height > mNormalHeight){
            lp.bottomMargin = (int) (height - mNormalHeight);
            lp.height = (int) mNormalHeight;
        }else{
            lp.height = (int) height;
            lp.bottomMargin = 0;
        }
        mContent.setLayoutParams(lp);
    }

    public int getVisiableHeight() {
        LayoutParams params = (LayoutParams) mContent.getLayoutParams();
        return params.height + params.bottomMargin;
    }

    public float getNormalHeight() {
        return mNormalHeight;
    }
}
