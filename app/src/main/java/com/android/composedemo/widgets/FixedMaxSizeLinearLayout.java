package com.android.composedemo.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.android.composedemo.R;

public class FixedMaxSizeLinearLayout extends LinearLayout {
    private int mMaxHeight;
    private int mMaxWidth;

    public FixedMaxSizeLinearLayout(Context context) {
        this(context, null);
    }

    public FixedMaxSizeLinearLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FixedMaxSizeLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FixedMaxSizeLinearLayout);
        mMaxWidth = ta.getDimensionPixelSize(R.styleable.FixedMaxSizeLinearLayout_android_maxWidth, 0);
        mMaxHeight = ta.getDimensionPixelSize(R.styleable.FixedMaxSizeLinearLayout_android_maxHeight, 0);
        ta.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(getSpecMode(mMaxWidth, widthMeasureSpec), getSpecMode(mMaxHeight, heightMeasureSpec));
    }

    private int getSpecMode(int value, int defaultValue) {
        return value > 0 ? MeasureSpec.makeMeasureSpec(value, MeasureSpec.AT_MOST) : defaultValue;
    }
}
