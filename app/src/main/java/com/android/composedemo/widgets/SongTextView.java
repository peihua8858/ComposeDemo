package com.android.composedemo.widgets;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created  on 2016/3/13.
 */
public class SongTextView extends View {
    private int postIndex;
    private Paint mPaint;
    private int delta = 15;
    private float mTextHeight;
    private float mTextWidth;
    private String mText="过完整个夏天,忧伤并没有好一些,过完整个夏天";
    private PorterDuffXfermode xformode;
    public SongTextView(Context ctx)
    {
        this(ctx,null);
    }
    public SongTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SongTextView(Context context,  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    public void init()
    {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        xformode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
        mPaint.setColor(Color.CYAN);
        mPaint.setTextSize(60.0f);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setXfermode(null);
        mPaint.setTextAlign(Paint.Align.LEFT);
        //文字精确高度
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        mTextHeight = fontMetrics.bottom-fontMetrics.descent-fontMetrics.ascent;
        mTextWidth  = mPaint.measureText(mText);
    }
    /**
     *计算 控件的宽高
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final  int mWidth;
        final  int mHeight;
        /**
         * 设置宽度
         */
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY)// match_parent , accurate
            mWidth = widthSize;
        else
        {
            // 由图片决定的宽
            int desireByImg = getPaddingLeft() + getPaddingRight()
                    + getMeasuredWidth();
            if (widthMode == MeasureSpec.AT_MOST)// wrap_content
                mWidth = Math.min(desireByImg, widthSize);
            else
                mWidth = desireByImg;
        }
        /***
         * 设置高度
         */
        int   heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int   heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (heightMode == MeasureSpec.EXACTLY)// match_parent , accurate
            mHeight = heightSize;
        else
        {
            int desire = getPaddingTop() + getPaddingBottom()
                    + getMeasuredHeight();
            if (heightMode == MeasureSpec.AT_MOST)// wrap_content
                mHeight = Math.min(desire, heightSize);
            else
                mHeight = desire;
        }
        setMeasuredDimension( mWidth,  mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Bitmap srcBitmap = Bitmap.createBitmap(getMeasuredWidth(),getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas srcCanvas = new Canvas(srcBitmap);
        srcCanvas.drawText(mText, 0, mTextHeight, mPaint);
        mPaint.setXfermode(xformode);
        mPaint.setColor(Color.RED);
        RectF rectF = new RectF(0,0,postIndex,getMeasuredHeight());
        srcCanvas.drawRect(rectF, mPaint);
        canvas.drawBitmap(srcBitmap, 0, 0, null);
        init();
        if(postIndex<mTextWidth)
        {
            postIndex+=10;
        }else{
            postIndex=0;
        }
        postInvalidateDelayed(100);
    }
}