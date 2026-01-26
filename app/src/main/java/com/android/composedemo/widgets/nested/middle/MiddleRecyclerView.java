package com.android.composedemo.widgets.nested.middle;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.android.composedemo.widgets.nested.ChildRecyclerView;


/**
 * @author jingyou
 */
public class MiddleRecyclerView extends ChildRecyclerView {

    private final int DIRECTION = -1;
    private boolean isDisallowInterceptTouchEvent = false;
    private boolean isDispatchChild = false;

    public MiddleRecyclerView(@NonNull Context context) {
        super(context);
    }

    public MiddleRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MiddleRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void initView(@NonNull Context context, @Nullable AttributeSet attrs) {
        setNestedMiddleScroller(new NestedMiddleScroller(this));
    }

    @Override
    protected void initListener() {
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (mLastScrollState == SCROLL_STATE_SETTLING
                        && newState == SCROLL_STATE_IDLE
                        && (!canScrollVerticallyUp() || !canScrollVerticallyDown())) {
                    dispatchFling();
                }
                mLastScrollState = newState;
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mTotalDy += dy;
            }
        });
        setListener();
    }

    @Override
    protected String getName() {
        return "MiddleRecyclerView";
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isInterceptEvent = !isChildAcceptEvent();
                this.mLastY = e.getRawY();
                this.mLastX = e.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                this.mCurrentY = e.getRawY();
                this.mCurrentX = e.getRawX();
                float dy = mLastY - mCurrentY;
                float dx = mLastX - mCurrentX;
                this.mLastY = mCurrentY;
                this.mLastX = mCurrentX;
                if (hasHeader) {
                    isInterceptEvent = false;
                    break;
                }

                if (isThresholdMoving(dx, dy)) {
                    if (isScrollEdgeDown(dy)) {
                        if (isNestedViewCanScroll(dy)) {
                            isInterceptEvent = false;
                        }
                    } else if (isScrollEdgeUp(dy)) {
                        isInterceptEvent = false;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
//                isInterceptEvent = true;
                break;
        }
        return isInterceptEvent ? super.onInterceptTouchEvent(e) : false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isAcceptTouchEvent = true;
                isDispatchChild = false;
                mVelocityY = 0;
                mTotalDy = 0;
                this.mLastY = e.getRawY();
                this.mLastX = e.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                this.mCurrentY = e.getRawY();
                this.mCurrentX = e.getRawX();
                float dy = mLastY - mCurrentY;
                float dx = mLastX - mCurrentX;
                this.mLastY = mCurrentY;
                this.mLastX = mCurrentX;
                if (isThresholdMoving(dx, dy)) {
                    if (isAcceptTouchEvent && isScrollEdgeDown(dy)) { // 上边界
                        isAcceptTouchEvent = false;
                        return super.onTouchEvent(e);
                    } else if (!isAcceptTouchEvent) {
                        if (isNestedViewCanScroll(dy)) {
                            dispatch(0, Math.round(dy));
                        } else {
                            isAcceptTouchEvent = true;
                        }
                    } else if (!isDispatchChild && isScrollEdgeUp(dy)) { //下边界
                        isDispatchChild = true;
                        return super.onTouchEvent(e);
                    } else if (isDispatchChild) {
                        if (!canScrollVerticallyUp() && (dy> 0 || !isNestedChildScrollEdgeDown(dy))) {
                            dispatchChild(0, Math.round(dy));
                        } else {
                            isDispatchChild = false;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isAcceptTouchEvent = true;
                isDispatchChild = false;
                break;
        }
        return (!isDispatchChild && isAcceptTouchEvent) ? super.onTouchEvent(e) : true;
    }

    @Override
    protected void regulateScroll(float dy) {
        if (isDisallowInterceptTouchEvent) {
            if (!isScrollEdgeUp(dy) || isNestedChildScrollEdgeDown(dy)) {
                requestDisallowInterceptTouchEvent(false);
                isDisallowInterceptTouchEvent = false;
            }
        }

        if ((dy > 0 && !canScrollVerticallyUp())
                || (dy<0 && !isNestedChildScrollEdgeDown(dy) && !canScrollVerticallyUp())) {
            requestDisallowInterceptTouchEvent(true);
            isDisallowInterceptTouchEvent = true;
        }
    }

    public boolean dispatchChild(int dx, int dy) {
        if (this.isAttachedToWindow()) {
            if (mNestedMiddleScroller != null && mNestedMiddleScroller.getChild() != null) {
                mNestedMiddleScroller.scroll(mNestedMiddleScroller.getChild(), dx, dy);
            }
        }
        return true;
    }

    @Override
    public void scrollBy(int x, int y) {
        if (mNestedMiddleScroller == null || mNestedMiddleScroller.getChild() == null) {
            super.scrollBy(x, y);
            return;
        }
        if (isScrollEdgeUp(y) || (!isNestedChildScrollEdgeDown(y) && !canScrollVerticallyUp())) {
            if (mNestedMiddleScroller!= null) {
                mNestedMiddleScroller.scroll(mNestedMiddleScroller.getChild(), x, y);
            }
        } else {
            super.scrollBy(x, y);
        }
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
        if (mNestedMiddleScroller == null || mNestedMiddleScroller.getChild() == null) {
            return super.fling(velocityX, velocityY);
        }
        if (isScrollEdgeUp(velocityY) || (!isNestedChildScrollEdgeDown(velocityY) && !canScrollVerticallyUp())) {
            if (mNestedMiddleScroller!= null) {
                mNestedMiddleScroller.fling(mNestedMiddleScroller.getChild(), velocityX, velocityY);
            }
        } else {
            return super.fling(velocityX, velocityY);
        }
        return true;
    }

    @Override
    protected void dispatchScroll(int x, int y) {
        if (y < 0) {
            if (mNestedScroller != null) {
                mNestedScroller.scroll(mNestedScroller.getParent(), x, y);
            }
        } else {
            if (mNestedMiddleScroller != null) {
                mNestedMiddleScroller.scroll(mNestedMiddleScroller.getChild(), x, y);
            }
        }
    }

    @Override
    protected void dispatchFling(int x, int y) {
        if (y < 0) {
            if (mNestedScroller != null) {
                mNestedScroller.fling(mNestedScroller.getParent(), x, y);
            }
        } else {
            if (mNestedMiddleScroller != null) {
                mNestedMiddleScroller.fling(mNestedMiddleScroller.getChild(), x, y);
            }
        }
    }

    /**
     * 子嵌套是否接受事件
     * */
    private boolean isChildAcceptEvent() {
        if (mNestedMiddleScroller!= null && mNestedMiddleScroller.getChild() != null) {
            // 是否滑动底部
            return !canScrollVerticallyUp();
        }
        return false;
    }

    /**
     * 子嵌套是否可下滑
     * */
    private boolean isNestedChildScrollEdgeDown(float dy) {
        if (mNestedMiddleScroller!= null && mNestedMiddleScroller.getChild() != null) {
            return mNestedMiddleScroller.getChild().isScrollEdge(dy);
        }
        return true;
    }

    /**
     * 子嵌套是否可上滑
     * */
    private boolean isNestedChildScrollEdgeUp() {
        if (mNestedMiddleScroller!= null && mNestedMiddleScroller.getChild() != null) {
            return !mNestedMiddleScroller.getChild().canScrollVertically(1);
        }
        return true;
    }

    public boolean isScrollEdgeUp(float dy) {
        return !canScrollVerticallyUp() && dy > 0;
    }

    public boolean isScrollEdgeDown(float dy) {
        return !canScrollVerticallyDown() && dy < 0;
    }

    @Override
    public boolean isScrollEdge(float dy) {
        if (dy > 0) {
            return !canScrollVerticallyUp() && isNestedChildScrollEdgeUp();
        } else {
            return !canScrollVerticallyDown();
        }
    }

    /**
     * 是否可向上滑动
     * */
    public boolean canScrollVerticallyUp() {
        return canScrollVertically(1);
    }

    /**
     * 是否可向下滑动
     * */
    public boolean canScrollVerticallyDown() {
        return canScrollVertically(-1);
    }
}
