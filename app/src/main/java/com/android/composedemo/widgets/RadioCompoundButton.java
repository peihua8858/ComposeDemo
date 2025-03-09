package com.android.composedemo.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.SoundEffectConstants;
import android.view.autofill.AutofillManager;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.android.composedemo.R;

public class RadioCompoundButton extends LinearLayout implements Checkable {
    private final ImageView mButton;
    private final CheckedTextView mTextView;
    private boolean mChecked;
    private boolean mBroadcasting;
    private OnCheckedChangeListener mOnCheckedChangeListener;
    private OnCheckedChangeListener mOnCheckedChangeWidgetListener;
    private static final int[] CHECKED_STATE_SET = {
            android.R.attr.state_checked
    };

    public RadioCompoundButton(Context context) {
        this(context, null);
    }

    public RadioCompoundButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RadioCompoundButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RadioCompoundButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RadioCompoundButton);
        Drawable drawable = ta.getDrawable(R.styleable.RadioCompoundButton_android_button);
        boolean isChecked = ta.getBoolean(R.styleable.RadioCompoundButton_android_checked, false);
        boolean showText = ta.getBoolean(R.styleable.RadioCompoundButton_android_showText, true);
        int drawablePadding = ta.getDimensionPixelSize(R.styleable.RadioCompoundButton_android_drawablePadding, 0);
        int buttonWidth = ta.getDimensionPixelSize(R.styleable.RadioCompoundButton_buttonWidth, 0);
        int buttonHeight = ta.getDimensionPixelSize(R.styleable.RadioCompoundButton_buttonHeight, 0);
        int direction = ta.getInt(R.styleable.RadioCompoundButton_direction, 1);
        ta.recycle();
        mButton = new ImageView(context);
        mTextView = new CheckedTextView(context, attrs);
        LayoutParams textParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        LayoutParams buttonParams = new LayoutParams(buttonWidth > 0 ? buttonWidth : LayoutParams.WRAP_CONTENT, buttonHeight > 0 ? buttonHeight : LayoutParams.WRAP_CONTENT);
        if (direction == 1) {
            addView(mButton, buttonParams);
            setDrawablePadding(textParams, drawablePadding);
            addView(mTextView, textParams);
        } else {
            addView(mTextView, textParams);
            setDrawablePadding(buttonParams, drawablePadding);
            addView(mButton, buttonParams);
        }
        mTextView.setVisibility(showText ? VISIBLE : GONE);
        mButton.setImageDrawable(drawable);
        mTextView.setBackground(null);
        setChecked(isChecked);
        setClickable(true);
    }

    private void setDrawablePadding(MarginLayoutParams params, int drawablePadding) {
        if (drawablePadding > 0) {
            if (getOrientation() == HORIZONTAL) {
                params.setMarginStart(drawablePadding);
            } else {
                params.topMargin = drawablePadding;
            }
        }
    }

    @Override
    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            refreshDrawableState();
            mButton.refreshDrawableState();
            mTextView.setChecked(checked);
            if (mBroadcasting) {
                return;
            }
            mBroadcasting = true;
            if (mOnCheckedChangeListener != null) {
                mOnCheckedChangeListener.onCheckedChanged(this, mChecked);
            }
            if (mOnCheckedChangeWidgetListener != null) {
                mOnCheckedChangeWidgetListener.onCheckedChanged(this, mChecked);
            }
            final AutofillManager afm = getContext().getSystemService(AutofillManager.class);
            if (afm != null) {
                afm.notifyValueChanged(this);
            }
            mBroadcasting = false;
        }
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        if (!isChecked()) {
            setChecked(!mChecked);
        }
    }

    public Drawable getButtonDrawable() {
        return mButton.getDrawable().mutate();
    }

    public Drawable newDrawable() {
        Drawable drawable = getButtonDrawable();
        Drawable.ConstantState constantState = drawable.getConstantState();
        return constantState.newDrawable();
    }

    public CharSequence getText() {
        return mTextView.getText();
    }

    @Override
    public boolean performClick() {
        toggle();

        final boolean handled = super.performClick();
        if (!handled) {
            // View only makes a sound effect if the onClickListener was
            // called, so we'll need to make one here instead.
            playSoundEffect(SoundEffectConstants.CLICK);
        }

        return handled;
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener mOnCheckedChangeListener) {
        this.mOnCheckedChangeListener = mOnCheckedChangeListener;
    }

    void setOnCheckedChangeWidgetListener(OnCheckedChangeListener mOnCheckedChangeWidgetListener) {
        this.mOnCheckedChangeWidgetListener = mOnCheckedChangeWidgetListener;
    }

    public interface OnCheckedChangeListener {
        /**
         * Called when the checked state of a compound button has changed.
         *
         * @param buttonView The compound button view whose state has changed.
         * @param isChecked  The new checked state of buttonView.
         */
        void onCheckedChanged(RadioCompoundButton buttonView, boolean isChecked);
    }
}
