package com.android.composedemo.widgets.nested;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.peihua8858.selector.picker.widget.nested.help.FlingHelper;


/**
 * @author jingyou
 */
public abstract class NestedRecyclerView extends RecyclerView {

    protected float mLastY = 0f;
    protected float mLastX = 0f;
    protected float mCurrentY = 0f;
    protected float mCurrentX = 0f;
    protected int mVelocityY = 0;
    protected int mTotalDy = 0;
    protected int mLastScrollState = SCROLL_STATE_IDLE;
    protected float CRITICAL = 0.0f;
    protected float mTouchSlop = 8;
    protected boolean isInterceptEvent = true;
    protected boolean isAcceptTouchEvent = true;
    public INestedScroller mNestedScroller;
    protected boolean hasHeader = false;
    private FlingHelper mFlingHelper;
    private boolean isFling = false;

    public void setHasHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
    }

    public NestedRecyclerView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public NestedRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public NestedRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
//        ViewConfiguration vc = ViewConfiguration.get(context);
//        this.mTouchSlop = vc.getScaledTouchSlop();
        initView(context, attrs);
        initListener();
        mFlingHelper = new FlingHelper(getContext());
    }

    protected void initListener() {
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                log("newState: " + newState);
                if (mLastScrollState == SCROLL_STATE_SETTLING && newState == SCROLL_STATE_IDLE && !canScrollVertically()) {
                    dispatchFling();
                }
                mLastScrollState = newState;
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (isFling) {
                    mTotalDy = 0;
                    isFling = false;
                }
                mTotalDy += dy;
            }
        });
        setListener();
    }

//    @Override
//    public void setScrollingTouchSlop(int slopConstant) {
//        ViewConfiguration vc = ViewConfiguration.get(this.getContext());
//        switch(slopConstant) {
//            case 1:
//                this.mTouchSlop = vc.getScaledPagingTouchSlop();
//                break;
//            default:
//            case 0:
//                this.mTouchSlop = vc.getScaledTouchSlop();
//        }
//
//    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isInterceptEvent = true;
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
                if (isThresholdMoving(dx, dy) && !canScrollVertically()) {
                    if (isNestedViewCanScroll(dy)) {
                        isInterceptEvent = false;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isInterceptEvent = true;
                break;
        }
        return isInterceptEvent ? super.onInterceptTouchEvent(e) : false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.mLastY = e.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                /**
                 * 华为补丁
                 * */
//                regulateScroll(this.mLastY - e.getRawY());
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return super.dispatchTouchEvent(e);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isAcceptTouchEvent = true;
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
                    if (isAcceptTouchEvent && isScrollEdge(dy)) {
                        isAcceptTouchEvent = false;
                        return super.onTouchEvent(e);
                    } else {
                        if (!isAcceptTouchEvent) {
                            if (isNestedViewCanScroll(dy)) {
                                dispatch(0, Math.round(dy));
                            } else {
                                isAcceptTouchEvent = true;
                            }
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isAcceptTouchEvent = true;
                break;
        }
        return isAcceptTouchEvent ? super.onTouchEvent(e) : true;
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
        mVelocityY = velocityY;
        if (velocityY != 0) {
            isFling = true;
        }
        return super.fling(velocityX, velocityY);
    }

    public boolean dispatch(int dx, int dy) {
        if (this.isAttachedToWindow()) {
            int scrollState = this.getScrollState();
            switch (scrollState) {
                case SCROLL_STATE_IDLE:
                case SCROLL_STATE_DRAGGING:
                    dispatchScroll(dx, dy);
                    break;
                case SCROLL_STATE_SETTLING:
                    dispatchFling();
                    break;
            }
        }
        return true;
    }

    public void setNestedScroller(INestedScroller nestedScroller) {
        this.mNestedScroller = nestedScroller;
    }

    public boolean isInterceptEvent() {
        return isInterceptEvent;
    }

    protected boolean isEdgeDirection(float dy) {
        return dy * endDirection() > 0;
    }

    protected boolean isThresholdMoving(float dx, float dy) {
        return Math.abs(dy) >= CRITICAL && Math.abs(dy) > Math.abs(dx);
    }

    protected void dispatchFling() {
        if (mVelocityY == 0) {
            return;
        }
        int direction = mVelocityY > 0 ? 1 : -1;
        Double splineFlingDistance = mFlingHelper.getSplineFlingDistance(mVelocityY * direction);
        int vy = mFlingHelper.getVelocityByDistance(splineFlingDistance - Double.valueOf(mTotalDy * direction)) * direction;
        int lastVelocityY = mFlingHelper.getFlingVelocity(vy);
        dispatchFling(0, (int) lastVelocityY);
        mTotalDy = 0;
        mVelocityY = 0;
    }

    protected boolean isNestedViewCanScroll(float dy) {
        boolean isCanScroll = false;
        NestedRecyclerView dispatchView = getDispatchView();
        if (dispatchView != null && !dispatchView.isScrollEdge(dy)) {
            isCanScroll = true;
        }
        return isCanScroll;
    }

    protected void log(String msg) {
        Log.d(getName(), "nested-> " + msg);
    }

    protected abstract void initView(@NonNull Context context, @Nullable AttributeSet attrs);

    protected abstract void setListener();

    protected abstract String getName();

    protected abstract boolean canScrollVertically();

    protected abstract boolean isScrollEdge(float dy);

    protected abstract int endDirection();

    protected abstract void dispatchScroll(int x, int y);

    protected abstract void dispatchFling(int x, int y);

    protected abstract boolean isParent();

    protected abstract void regulateScroll(float dy);

    protected abstract NestedRecyclerView getDispatchView();

}
