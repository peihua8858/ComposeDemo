package com.android.composedemo.widgets.nested;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author jingyou
 */
public class ParentRecyclerView extends NestedRecyclerView {

    private final int DIRECTION = 1;
    private boolean isDisallowInterceptTouchEvent = false;

    public ParentRecyclerView(@NonNull Context context) {
        super(context);
    }

    public ParentRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ParentRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void initView(@NonNull Context context, @Nullable AttributeSet attrs) {
        setNestedScroller(new NestedScroller(this));
    }

    @Override
    protected void setListener() {

    }

    @Override
    protected String getName() {
        return "ParentRecyclerView";
    }

    @Override
    protected boolean canScrollVertically() {
        return canScrollVertically(1);
    }

    @Override
    public boolean canScrollVertically(int direction) {
        //列表不在顶部时下拉，如果ChildRecyclerView可以滚动，则表示ParentRecyclerView可以滚动，防止被下拉刷新组件拦截
        if (getChildCount() == 1 && getDispatchView() != null) {
            return super.canScrollVertically(direction) || (direction < 0 && isNestedViewCanScroll(direction));
        }
        return super.canScrollVertically(direction);
    }

    @Override
    protected boolean isScrollEdge(float dy) {
        return !canScrollVertically() && isEdgeDirection(dy);
    }

    @Override
    protected int endDirection() {
        return DIRECTION;
    }

    @Override
    protected void dispatchScroll(int x, int y) {
        if (mNestedScroller != null) {
            mNestedScroller.scroll(mNestedScroller.getChild(), x, y);
        }
    }

    @Override
    protected void dispatchFling(int x, int y) {
        if (mNestedScroller != null) {
            mNestedScroller.fling(mNestedScroller.getChild(), x, y);
        }
    }

    @Override
    protected boolean isParent() {
        return true;
    }

    @Override
    protected NestedRecyclerView getDispatchView() {
        if (mNestedScroller != null) {
            return mNestedScroller.getChild();
        }
        return null;
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return super.onStartNestedScroll(child, target, nestedScrollAxes);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        super.onNestedPreScroll(target, dx, dy, consumed);
    }

    public boolean isRegulateScroll(float dy) {
        return !isEdgeDirection(dy) && isNestedViewCanScroll(dy);
    }

    @Override
    protected void regulateScroll(float dy) {
        if (isDisallowInterceptTouchEvent) {
            if (isEdgeDirection(dy) || !isNestedViewCanScroll(dy)) {
                requestDisallowInterceptTouchEvent(false);
                isDisallowInterceptTouchEvent = false;
            }
        }

        if ((dy > 0 && !canScrollVertically(1)) || (!isEdgeDirection(dy) && isNestedViewCanScroll(dy))) {
            requestDisallowInterceptTouchEvent(true);
            isDisallowInterceptTouchEvent = true;
        }
    }

    public INestedScroller getNestedScroller() {
        return mNestedScroller;
    }

//    @Override
//    protected void onMeasure(int widthSpec, int heightSpec) {
//        super.onMeasure(widthSpec, heightSpec);
//        ChildRecyclerView child = mNestedScroller.getChild();
//        if (child != null){
//            child.setMaxHeight(getMeasuredHeight());
//        }
//    }
}
