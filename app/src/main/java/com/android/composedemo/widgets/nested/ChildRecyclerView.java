package com.android.composedemo.widgets.nested;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.composedemo.widgets.nested.middle.INestedMiddleScroller;


/**
 * @author jingyou
 */
public class ChildRecyclerView extends NestedRecyclerView {

    private final int DIRECTION = -1;

    public INestedMiddleScroller mNestedMiddleScroller;

//    private int mMaxHeight = -1;

    public ChildRecyclerView(@NonNull Context context) {
        super(context);
    }

    public ChildRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ChildRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void initView(@NonNull Context context, @Nullable AttributeSet attrs) {

    }

    @Override
    protected void setListener() {

    }

    @Override
    protected String getName() {
        return "ChildRecyclerView";
    }

    @Override
    protected boolean canScrollVertically() {
        return canScrollVertically(-1);
    }

    @Override
    public boolean isScrollEdge(float dy) {
        return !canScrollVertically() && isEdgeDirection(dy);
    }

    @Override
    protected int endDirection() {
        return DIRECTION;
    }

    @Override
    protected void dispatchScroll(int x, int y) {
        if (mNestedScroller != null) {
            mNestedScroller.scroll(mNestedScroller.getParent(), x, y);
        } else if (mNestedMiddleScroller != null) {
            mNestedMiddleScroller.scroll(mNestedMiddleScroller.getParent(), x, y);
        }
    }

    @Override
    protected void dispatchFling(int x, int y) {
        if (mNestedScroller != null) {
            mNestedScroller.fling(mNestedScroller.getParent(), x, y);
        } else if (mNestedMiddleScroller != null) {
            mNestedMiddleScroller.fling(mNestedMiddleScroller.getParent(), x, y);
        }
    }

    @Override
    protected boolean isParent() {
        return false;
    }

    @Override
    protected void regulateScroll(float dy) {
//        if (!isEdgeDirection(dy) && isNestedViewCanScroll(dy)) {
//            isInterceptEvent = false;
//        }
    }

    @Override
    protected NestedRecyclerView getDispatchView() {
        if (mNestedScroller != null) {
            return mNestedScroller.getParent();
        } else if (mNestedMiddleScroller != null) {
            return mNestedMiddleScroller.getParent();
        }
        return null;
    }

    /**
     * 三层嵌套使用，常规双层嵌套勿设置
     * mNestedScroller 双层嵌套设置
     * mNestedMiddleScroller 三层嵌套设置
     * */
    public void setNestedMiddleScroller(INestedMiddleScroller nestedScroller) {
        this.mNestedMiddleScroller = nestedScroller;
    }

    public INestedMiddleScroller getNestedMiddleScroller() {
        return mNestedMiddleScroller;
    }

//    public void setMaxHeight(int maxHeight){
//        this.mMaxHeight = maxHeight;
//        requestLayout();
//    }
//
    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int heightSize = MeasureSpec.getSize(heightSpec);
        int heightMode = MeasureSpec.getMode(heightSpec);
        if (heightMode == MeasureSpec.EXACTLY){
            int newHeightMode = MeasureSpec.makeMeasureSpec(heightSize,MeasureSpec.AT_MOST);
            super.onMeasure(widthSpec, newHeightMode);
        }else {
            super.onMeasure(widthSpec, heightSpec);
        }
    }

}
