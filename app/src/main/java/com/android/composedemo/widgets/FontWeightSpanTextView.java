package com.android.composedemo.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.MetricAffectingSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.composedemo.R;

/**
 * 将{@link #mStartChar} 至 {@link #mEndChar}之间的字符串修改字体颜色为{@link #mTextColor}并且字重是{@link #mFontWeight}
 *
 * @author dingpeihua
 * @date 2024/12/23 10:51
 **/
public class FontWeightSpanTextView extends TextView {
    private String mStartChar;
    private String mEndChar;
    private int mTextColor;
    private int mFontWeight;
    private int mTextSize;
    private boolean mChangedChar;

    public FontWeightSpanTextView(@NonNull Context context) {
        this(context, null);
    }

    public FontWeightSpanTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FontWeightSpanTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FontWeightSpanTextView);
        mStartChar = ta.getString(R.styleable.FontWeightSpanTextView_fw_startChar);
        mEndChar = ta.getString(R.styleable.FontWeightSpanTextView_fw_endChar);
        mTextColor = ta.getColor(R.styleable.FontWeightSpanTextView_fw_textColor, Color.TRANSPARENT);
        mFontWeight = ta.getInt(R.styleable.FontWeightSpanTextView_fw_fontWeight, 0);
        mTextSize = ta.getDimensionPixelSize(R.styleable.FontWeightSpanTextView_fw_textSize, 0);
        mChangedChar = ta.getBoolean(R.styleable.FontWeightSpanTextView_fw_changedChar, false);
        ta.recycle();
        setText(getText());
    }

    public void setStartChar(String mStartChar) {
        this.mStartChar = mStartChar;
    }

    public void setEndChar(String mEndChar) {
        this.mEndChar = mEndChar;
    }

    public void setFtTextColor(int mTextColor) {
        this.mTextColor = mTextColor;
    }

    @Override
    public void setText(CharSequence text, TextView.BufferType type) {
        super.setText(parseText(text, type), type);
    }

    private CharSequence parseText(CharSequence text, TextView.BufferType type) {
        if (mStartChar == null || mEndChar == null) {
            return text;
        }
        SpannableStringBuilder spannable = new SpannableStringBuilder(text);
        int length = text.length();
        int startIndex = 0;
        while (startIndex < length) {
            char c = text.charAt(startIndex);
            if ((c + "").equals(mStartChar)) {
                for (int i = startIndex + 1; i < length; i++) {
                    char e = text.charAt(i);
                    if ((e + "").equals(mEndChar)) {
                        int start = mChangedChar ? startIndex : startIndex + 1;
                        int end = mChangedChar ? i + 1 : i;
                        if (mFontWeight > 0) {
                            spannable.setSpan(new FontWeightSpan(mFontWeight), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                        if (mTextColor != Color.TRANSPARENT) {
                            spannable.setSpan(new ForegroundColorSpan(mTextColor), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                        if (mTextSize > 0) {
                            spannable.setSpan(new AbsoluteSizeSpan(mTextSize), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                        break;
                    }
                }
            }
            ++startIndex;
        }
        return spannable;
    }

    /**
     * 修改字体的字重
     *
     * @author dingpeihua
     * @date 2024/12/27 14:20
     **/
    public static class FontWeightSpan extends MetricAffectingSpan {
        private int mTextFontWeight;
        private Typeface mTypeface;
        private String mFamily;

        public FontWeightSpan(@Nullable String family, int textFontWeight) {
            this.mFamily = family;
            this.mTextFontWeight = textFontWeight;
        }

        public FontWeightSpan(@NonNull Typeface typeface, int textFontWeight) {
            this.mTypeface = typeface;
            this.mTextFontWeight = textFontWeight;
        }

        public FontWeightSpan(int textFontWeight) {
            this.mTextFontWeight = textFontWeight;
        }

        public void setTextFontWeight(int textFontWeight) {
            this.mTextFontWeight = textFontWeight;
        }

        @Override
        public void updateDrawState(@NonNull TextPaint ds) {
            updateTypeface(ds);
        }

        @Override
        public void updateMeasureState(@NonNull TextPaint paint) {
            updateTypeface(paint);
        }

        private void updateTypeface(@NonNull Paint paint) {
            if (mTypeface != null) {
                paint.setTypeface(create(mTypeface, mTextFontWeight));
            } else if (mFamily != null) {
                applyFontFamily(paint, mFamily, mTextFontWeight);
            } else {
                paint.setTypeface(create(Typeface.DEFAULT, mTextFontWeight));
            }
        }

        private Typeface create(@NonNull Typeface typeface, int textFontWeight) {
            if (textFontWeight > 0) {
                return Typeface.create(typeface, textFontWeight, false);
            }
            return Typeface.create(typeface, Typeface.NORMAL);
        }

        private void applyFontFamily(@NonNull Paint paint, @NonNull String family, int textFontWeight) {
            int style;
            Typeface old = paint.getTypeface();
            if (old == null) {
                style = Typeface.NORMAL;
            } else {
                style = old.getStyle();
            }
            final Typeface styledTypeface = create(Typeface.create(family, style), textFontWeight);
            int fake = style & ~styledTypeface.getStyle();

            if ((fake & Typeface.BOLD) != 0) {
                paint.setFakeBoldText(true);
            }

            if ((fake & Typeface.ITALIC) != 0) {
                paint.setTextSkewX(-0.25f);
            }
            paint.setTypeface(styledTypeface);
        }
    }
}
