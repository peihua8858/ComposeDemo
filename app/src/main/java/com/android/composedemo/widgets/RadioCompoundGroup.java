package com.android.composedemo.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.autofill.AutofillManager;
import android.view.autofill.AutofillValue;
import android.widget.Checkable;
import android.widget.LinearLayout;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;

import com.android.composedemo.R;

/**
 * @author dingpeihua
 * @date 2024/12/30 14:03
 **/
public class RadioCompoundGroup extends LinearLayout {
    private int mCheckedId = -1;
    private boolean mProtectFromCheckedChange = false;
    private int mInitialCheckedId = View.NO_ID;
    private OnCheckedChangeListener mOnCheckedChangeListener;
    private RadioCompoundButton.OnCheckedChangeListener mChildOnCheckedChangeListener;
    private PassThroughHierarchyChangeListener mPassThroughListener;

    public RadioCompoundGroup(Context context) {
        this(context, null);
    }

    public RadioCompoundGroup(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RadioCompoundGroup(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RadioCompoundGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray attributes = context.obtainStyledAttributes(
                attrs, R.styleable.RadioCompoundGroup, 0, 0);
        int value = attributes.getResourceId(R.styleable.RadioCompoundGroup_android_checkedButton, View.NO_ID);
        if (value != View.NO_ID) {
            mCheckedId = value;
            mInitialCheckedId = value;
            setCheckedId(mCheckedId);
        }
        attributes.recycle();
        mChildOnCheckedChangeListener = new CheckedStateTracker();
        mPassThroughListener = new PassThroughHierarchyChangeListener();
        super.setOnHierarchyChangeListener(mPassThroughListener);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        if (mCheckedId != -1) {
            mProtectFromCheckedChange = true;
            setCheckedStateForView(mCheckedId, true);
            mProtectFromCheckedChange = false;
            setCheckedId(mCheckedId);
        }
    }

    @Override
    public void setOnHierarchyChangeListener(OnHierarchyChangeListener listener) {
        mPassThroughListener.mOnHierarchyChangeListener = listener;
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child instanceof Checkable) {
            final Checkable button = (Checkable) child;
            if (button.isChecked()) {
                mProtectFromCheckedChange = true;
                if (mCheckedId != -1) {
                    setCheckedStateForView(mCheckedId, false);
                }
                mProtectFromCheckedChange = false;
                setCheckedId(child.getId());
            }
        }

        super.addView(child, index, params);
    }

    private void setCheckedStateForView(int viewId, boolean checked) {
        View checkedView = findViewById(viewId);
        if (checkedView instanceof Checkable) {
            ((Checkable) checkedView).setChecked(checked);
        }
    }

    private void setCheckedId(@IdRes int id) {
        boolean changed = id != mCheckedId;
        mCheckedId = id;

        if (mOnCheckedChangeListener != null) {
            mOnCheckedChangeListener.onCheckedChanged(this, mCheckedId);
        }
        if (changed) {
            final AutofillManager afm = getContext().getSystemService(AutofillManager.class);
            if (afm != null) {
                afm.notifyValueChanged(this);
            }
        }
    }
    @Override
    public void autofill(AutofillValue value) {
        if (!isEnabled()) return;

        if (!value.isList()) {
            Log.w(VIEW_LOG_TAG, value + " could not be autofilled into " + this);
            return;
        }

        final int index = value.getListValue();
        final View child = getChildAt(index);
        if (child == null) {
            Log.w(VIEW_LOG_TAG, "RadioGroup.autoFill(): no child with index " + index);
            return;
        }

        check(child.getId());
    }

    @Override
    public int getAutofillType() {
        return isEnabled() ? AUTOFILL_TYPE_LIST : AUTOFILL_TYPE_NONE;
    }

    @Override
    public AutofillValue getAutofillValue() {
        if (!isEnabled()) return null;

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getId() == mCheckedId) {
                return AutofillValue.forList(i);
            }
        }
        return null;
    }
    @IdRes
    public int getCheckedId() {
        return mCheckedId;
    }

    public View getCheckedView() {
        return findViewById(mCheckedId);
    }

    /**
     * <p>Clears the selection. When the selection is cleared, no radio button
     * in this group is selected and {@link #getCheckedId()} returns
     * null.</p>
     *
     * @see #check(int)
     * @see #getCheckedId()
     */
    public void clearCheck() {
        check(-1);
    }

    public void check(@IdRes int id) {
        // don't even bother
        if (id != -1 && (id == mCheckedId)) {
            return;
        }

        if (mCheckedId != -1) {
            setCheckedStateForView(mCheckedId, false);
        }

        if (id != -1) {
            setCheckedStateForView(id, true);
        }

        setCheckedId(id);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener mOnCheckedChangeListener) {
        this.mOnCheckedChangeListener = mOnCheckedChangeListener;
    }

    private class PassThroughHierarchyChangeListener implements
            OnHierarchyChangeListener {
        private OnHierarchyChangeListener mOnHierarchyChangeListener;

        /**
         * {@inheritDoc}
         */
        @Override
        public void onChildViewAdded(View parent, View child) {
            if (parent == RadioCompoundGroup.this && child instanceof RadioCompoundButton) {
                int id = child.getId();
                // generates an id if it's missing
                if (id == View.NO_ID) {
                    id = View.generateViewId();
                    child.setId(id);
                }
                ((RadioCompoundButton) child).setOnCheckedChangeWidgetListener(
                        mChildOnCheckedChangeListener);
            }

            if (mOnHierarchyChangeListener != null) {
                mOnHierarchyChangeListener.onChildViewAdded(parent, child);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onChildViewRemoved(View parent, View child) {
            if (parent == RadioCompoundGroup.this && child instanceof RadioCompoundButton) {
                ((RadioCompoundButton) child).setOnCheckedChangeWidgetListener(null);
            }

            if (mOnHierarchyChangeListener != null) {
                mOnHierarchyChangeListener.onChildViewRemoved(parent, child);
            }
        }
    }

    private class CheckedStateTracker implements RadioCompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioCompoundButton buttonView, boolean isChecked) {
            // prevents from infinite recursion
            if (mProtectFromCheckedChange) {
                return;
            }

            mProtectFromCheckedChange = true;
            if (mCheckedId != -1) {
                setCheckedStateForView(mCheckedId, false);
            }
            mProtectFromCheckedChange = false;

            int id = buttonView.getId();
            check(id);
        }
    }

    public interface OnCheckedChangeListener {
        /**
         * <p>Called when the checked radio button has changed. When the
         * selection is cleared, checkedId is -1.</p>
         *
         * @param group     the group in which the checked radio button has changed
         * @param checkedId the unique identifier of the newly checked radio button
         */
        public void onCheckedChanged(RadioCompoundGroup group, @IdRes int checkedId);
    }
}
