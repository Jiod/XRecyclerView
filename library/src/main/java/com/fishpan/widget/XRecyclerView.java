package com.fishpan.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * 下拉刷新，加载更多；添加头部、底部控件
 * Created by yupan on 17/6/22.
 */
public class XRecyclerView extends RecyclerView {
    private final float MOVE_SCALE = 0.56F;
    private IRecycleViewListener mListener;
    private List<View> mHeaderView = new ArrayList<>();
    private List<View> mFooterView = new ArrayList<>();
    private WrapperAdapterDataObserver mDataObserver;
    private WrapperAdapter mAdapter;

    private XRecyclerViewHeader mRefreshHeader;
    private XRecyclerViewFooter mLoadmoreFooter;

    private float mLastX;
    private float mLastY;
    private boolean mPullRefreshing = false;
    private boolean mEnablePullRefresh = true;
    private boolean mEnableLoadMore = true;
    private boolean mLoading = false;

    public XRecyclerView(Context context) {
        this(context, null);
    }

    public XRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initView();
    }

    /**
     * View初始化
     */
    private void initView(){
        /** 默认布局方式*/
        initDefaultLayoutManager();

        /** 设置下拉刷新加载更多布局*/
        initPullableAndLoadMoreView();
    }

    /**
     * 设置刷新顶部和加载更多布局
     */
    private void initPullableAndLoadMoreView(){
        /** 下拉刷新 **/
        mRefreshHeader = new XRecyclerViewHeader(getContext());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mRefreshHeader.setLayoutParams(params);

        mHeaderView.clear();
        mHeaderView.add(mRefreshHeader);

        /** 加载更多 **/
        mLoadmoreFooter = new XRecyclerViewFooter(getContext());
        params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mLoadmoreFooter.setLayoutParams(params);
        mFooterView.clear();
        mFooterView.add(mLoadmoreFooter);
    }

    /**
     * 设置默认的LayoutManager
     */
    private void initDefaultLayoutManager(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        setLayoutManager(layoutManager);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        if(null != mAdapter){
            if(adapter != mAdapter.getInnerAdapter()){
                if(null != mDataObserver){  //进行反注册，防止内存泄漏
                    mAdapter.getInnerAdapter().unregisterAdapterDataObserver(mDataObserver);
                }
            }
        }

        mAdapter = new WrapperAdapter(adapter);
        if(null != adapter) {
            mDataObserver = new WrapperAdapterDataObserver(mAdapter);
            adapter.registerAdapterDataObserver(mDataObserver);
        }

        super.setAdapter(mAdapter);
    }

    /**
     * 添加头部View
     * @param view
     */
    public void addHeader(View view){
        mHeaderView.add(view);
    }

    /**
     * 添加脚部View
     * @param view
     */
    public void addFooter(View view){
        mFooterView.add(0, view);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastX = ev.getRawX();
                mLastY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:   //  在垂直方向
                onHandleMoveEvent(ev);
                break;
            default: //恢复
                onHandleDefaultEvent(ev);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 处理MOVE事件
     */
    private void onHandleMoveEvent(MotionEvent event){
        float durationX = event.getRawX() - mLastX;
        float durationY = event.getRawY() - mLastY;
        if(mEnablePullRefresh && isTop()) {   //允许下拉刷新、顶部
            if (Math.abs(durationX) < Math.abs(durationY)) {    //垂直方向上滑动
                updateHeaderHeight(durationY * MOVE_SCALE);
            }
        }
        if(mEnableLoadMore && isBottom()){
            if (Math.abs(durationX) < Math.abs(durationY)) {    //垂直方向上滑动
                updateFooterHeight(-durationY * MOVE_SCALE);
            }
        }
        mLastY = event.getRawY();
        mLastX = event.getRawX();
    }

    private boolean isBottom() {
        return getLastVisiablePosition() == mAdapter.getItemCount() - 1;
    }

    /**
     * 处理MOVE\DOWN以外的事件
     * @param event
     */
    private void onHandleDefaultEvent(MotionEvent event){
        if(isTop()) {
            if (mRefreshHeader.getVisiableHeight() >= mRefreshHeader.getNormalHeight()) {
                //  进行刷新
                mRefreshHeader.setStatus(XRecyclerViewHeader.STATE_REFRESHING);
                if (null != mListener) {
                    mListener.onRefresh();
                }
            } else {
                //  恢复高度，隐藏
                mRefreshHeader.dismiss();
            }
        }else if(isBottom()){
            if(mLoadmoreFooter.getVisiableHeight() > mLoadmoreFooter.getNormalHeight() + ViewUtils.dp2px(getContext(), 10)){    //开始刷新内容
                mLoadmoreFooter.setStatus(XRecyclerViewFooter.STATE_LOADING);
                mLoading = true;
                if(null != mListener){
                    mListener.onLoadMore();
                }
            } else {    //其它状态就停留好了
                mLoadmoreFooter.setStatus(XRecyclerViewFooter.STATE_NORMAL);
                mLoading = false;

            }
        }
    }

    /**
     * 停止刷新
     */
    public void stopRefresh(){
        mRefreshHeader.dismiss();
        mPullRefreshing = false;
    }

    /**
     * 停止加载更多
     */
    public void stopLoading(){
        mLoading = false;
        mLoadmoreFooter.setStatus(XRecyclerViewFooter.STATE_NORMAL);
    }

    /**
     * 更新刷新View的高度
     * @param delta
     */
    private void updateHeaderHeight(float delta) {
        mRefreshHeader.setVisiableHeight((int) delta + mRefreshHeader.getVisiableHeight());
        if (!mPullRefreshing && mEnablePullRefresh) { // 未处于刷新状态，更新箭头
            if (mRefreshHeader.getVisiableHeight() > mRefreshHeader.getNormalHeight()) {
                mRefreshHeader.setStatus(XRecyclerViewHeader.STATE_READY);
            } else {
                mRefreshHeader.setStatus(XRecyclerViewHeader.STATE_NORMAL);
            }
        }
    }

    /**
     * 更新底部Footer的高度
     * @param delta
     */
    private void updateFooterHeight(float delta) {
        delta += mLoadmoreFooter.getVisiableHeight();
        delta = delta < mLoadmoreFooter.getNormalHeight()? mLoadmoreFooter.getNormalHeight(): delta;
        mLoadmoreFooter.setVisiableHeight(delta);
        if(!mLoading && mEnableLoadMore){
            if(mLoadmoreFooter.getVisiableHeight() >= mLoadmoreFooter.getNormalHeight() + ViewUtils.dp2px(getContext(), 20)){
                mLoadmoreFooter.setStatus(XRecyclerViewFooter.STATE_READY);
            }else{
                mLoadmoreFooter.setStatus(XRecyclerViewFooter.STATE_NORMAL);
            }
        }
    }

    private boolean isTop(){
        return getFirstVisiblePosition() <= 1;
    }

    /**
     * 获取第一个完全可见的ITEM位置
     * @return
     */
    private int getFirstVisiblePosition(){
        LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
        return layoutManager.findFirstVisibleItemPosition();
    }

    /**
     * 获取最后一个展示的ITEM位置
     * @return
     */
    private int getLastVisiablePosition(){
        LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
        return layoutManager.findLastVisibleItemPosition();
    }

    public void setListener(XRecyclerView.IRecycleViewListener listener){
        mListener = listener;
    }

    public void setEnablePullRefresh(boolean enable){
        mEnablePullRefresh = enable;
        if(!enable && null != mRefreshHeader){
            mRefreshHeader.dismiss();
        }
    }

    public void setEnableLoadMore(boolean enable){
        mEnableLoadMore = enable;
        if(!enable && null != mLoadmoreFooter){
            mLoadmoreFooter.dismiss();
        }
    }

    /**
     * Adapter监听类
     */
    private class WrapperAdapterDataObserver extends AdapterDataObserver{
        private Adapter mObserverAdapter;

        public WrapperAdapterDataObserver(Adapter mObserverAdapter) {
            this.mObserverAdapter = mObserverAdapter;
        }

        @Override
        public void onChanged() {
            super.onChanged();
            mObserverAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 这个Adapter包裹着外部的Adapter
     */
    private class WrapperAdapter extends Adapter<ViewHolder>{
        /** 头部类型起点*/
        public final int ITEM_TYPE_HEADER = 10000;
        /** 脚部类型起点*/
        public final int ITEM_TYPE_FOOTER = 20000;
        /** 外部的Adapter，第三方设置RecyclerView的Adapter*/
        private Adapter mAdapter;

        WrapperAdapter(Adapter adapter) {
            this.mAdapter = adapter;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int itemType) {
            ViewHolder viewHolder;
            if(itemType >= ITEM_TYPE_HEADER){ // 属于头部或者底部View
                if(itemType >= ITEM_TYPE_FOOTER){   // 底部View
                    viewHolder = new SimpleViewHolder(mFooterView.get(itemType - ITEM_TYPE_FOOTER));
                }else{  //  头部View
                    viewHolder = new SimpleViewHolder(mHeaderView.get(itemType - ITEM_TYPE_HEADER));
                }
            }else{  //  第三发Adapter复制的View，这里有个规则，就是第三方的ItemType要小于10000
                viewHolder = mAdapter.onCreateViewHolder(parent, itemType);
            }
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            if(!(viewHolder instanceof SimpleViewHolder)){  //第三方的时候才会进行处理，头部和底部的View就不用再处理了
                position = position - mHeaderView.size();
                mAdapter.onBindViewHolder(viewHolder, position);
            }
        }

        @Override
        public int getItemViewType(int position) {
            int itemType;
            if (position < mHeaderView.size()) {    //头部
                itemType = ITEM_TYPE_HEADER + position;
            } else if(position >= mHeaderView.size() && position < (mHeaderView.size() + getAdapterItemCount())){   //  中间第三方
                itemType = mAdapter.getItemViewType(position - mHeaderView.size());
            } else {
                itemType = ITEM_TYPE_FOOTER + position - mHeaderView.size() - getAdapterItemCount();
            }
            return itemType;
        }

        @Override
        public int getItemCount() {
            return mHeaderView.size() + mFooterView.size() + getAdapterItemCount();
        }

        private int getAdapterItemCount(){
            return null == mAdapter? 0 : mAdapter.getItemCount();
        }

        public Adapter getInnerAdapter(){
            return mAdapter;
        }
    }

    private class SimpleViewHolder extends ViewHolder{
        SimpleViewHolder(View itemView) {
            super(itemView);
        }
    }

    public interface IRecycleViewListener {
        void onRefresh();

        void onLoadMore();
    }
}