package com.fishpan.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 刷新
 * Created by yupan on 17/6/24.
 */
public class XRecyclerViewHeader extends LinearLayout{
    public final static int STATE_NORMAL = 0;
    public final static int STATE_READY = 1;
    public final static int STATE_REFRESHING = 2;
    /** 箭头上线切换时间*/
    private final int ROTATE_ANIM_DURATION = 180;

    private View mContent;
    /** 箭头ImageView*/
    private ImageView mArrowImageView;
    /** 进度条*/
    private ProgressBar mProgressBar;
    /** 下拉刷新Label*/
    private TextView mHintTextView;
    /** 当前控件状态*/
    private int mStatus = STATE_NORMAL;
    /** 箭头动画-上*/
    private Animation mRotateUpAnim;
    /** 箭头动画-下*/
    private Animation mRotateDownAnim;

    private boolean mDismissing = false;
    private float mNormalHeight;

    public XRecyclerViewHeader(Context context) {
        this(context, null);
    }

    public XRecyclerViewHeader(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XRecyclerViewHeader(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);

        initView();
    }

    private void initView(){
        LayoutInflater.from(getContext()).inflate(R.layout.xrecycler_header, this, true);
        mContent = findViewById(R.id.xrecyclerview_header_content);
        mArrowImageView = (ImageView)findViewById(R.id.xrecyclerview_header_arrow);
        mHintTextView = (TextView)findViewById(R.id.xrecyclerview_header_hint_textview);
        mProgressBar = (ProgressBar)findViewById(R.id.xrecyclerview_header_progressbar);

        mRotateUpAnim = new RotateAnimation(0.0f, -180.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateUpAnim.setFillAfter(true);
        mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateDownAnim.setFillAfter(true);

        mNormalHeight = ViewUtils.dp2px(getContext(), 60);
        //  默认情况下不展示刷新内容
        setVisiableHeight(0);
    }

    public void setStatus(int status){
        if (status == STATE_REFRESHING) {	// 显示进度
            mArrowImageView.clearAnimation();
            mArrowImageView.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
        } else {	// 显示箭头图片
            mArrowImageView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.INVISIBLE);
        }

        switch (status) {
            case STATE_NORMAL:
                if (mStatus == STATE_READY) {
                    mArrowImageView.startAnimation(mRotateDownAnim);
                }
                if (mStatus == STATE_REFRESHING) {
                    mArrowImageView.clearAnimation();
                }
                mHintTextView.setText(R.string.xrecyclerview_header_hint_normal);
                break;
            case STATE_READY:
                if (mStatus != STATE_READY) {
                    mArrowImageView.clearAnimation();
                    mArrowImageView.startAnimation(mRotateUpAnim);
                    mHintTextView.setText(R.string.xrecyclerview_header_hint_ready);
                }
                break;
            case STATE_REFRESHING:
                smoothToHeight((int) mNormalHeight);
                mHintTextView.setText(R.string.xrecyclerview_header_hint_loading);
                break;
            default:
        }
        mStatus = status;
    }

    public void setVisiableHeight(int height) {
        if (height < 0)
            height = 0;

        LayoutParams lp = (LayoutParams) mContent.getLayoutParams();
        if(height > mNormalHeight){
            lp.topMargin = (int) (height - mNormalHeight);
            lp.height = (int) mNormalHeight;
        }else {
            lp.height = height;
            lp.topMargin = 0;
        }
        mContent.setLayoutParams(lp);
    }

    /**
     * 隐藏刷新View
     */
    public void dismiss(){
        smoothToHeight(0);
    }

    /**
     * 渐变高度
     * @param targetHeight 新高度
     */
    public void smoothToHeight(final int targetHeight){
        if(mDismissing){
            return;
        }

        mDismissing = true;
        ValueAnimator animator = ValueAnimator.ofInt(getVisiableHeight(), targetHeight);
        animator.setDuration(300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int height = (int) animation.getAnimatedValue();
                setVisiableHeight(height);
                mDismissing = height != targetHeight;
            }
        });
        animator.start();
    }

    public int getVisiableHeight(){
        LayoutParams params = (LayoutParams) mContent.getLayoutParams();
        return params.height + params.topMargin;
    }

    public float getNormalHeight() {
        return mNormalHeight;
    }
}