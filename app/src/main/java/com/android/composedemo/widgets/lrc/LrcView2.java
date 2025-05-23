package com.android.composedemo.widgets.lrc;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * LrcView can display LRC file and Seek it.
 *
 * @author douzifly
 */
public class LrcView2 extends View implements ILrcView {

    public final static String TAG = "LrcView";

    /**
     * normal display mode
     */
    public final static int DISPLAY_MODE_NORMAL = 0;
    /**
     * seek display mode
     */
    public final static int DISPLAY_MODE_SEEK = 1;
    /**
     * scale display mode ,scale font size
     */
    public final static int DISPLAY_MODE_SCALE = 2;

    private List<LrcRow> mLrcRows;    // all lrc rows of one lrc file
    private int mMinSeekFiredOffset = 10; // min offset for fire seek action, px;
    private int mHignlightRow = 0;   // current singing row , should be highlighted.
    private int mHighlightRowColor = Color.YELLOW;
    private int mNormalRowColor = Color.WHITE;
    private int mSeekLineColor = Color.CYAN;
    private int mSeekLineTextColor = Color.CYAN;
    private int mSeekLineTextSize = 15;
    private int mMinSeekLineTextSize = 13;
    private int mMaxSeekLineTextSize = 18;
    private int mLrcFontSize = 23;    // font size of lrc
    private int mMinLrcFontSize = 15;
    private int mMaxLrcFontSize = 35;
    private int mPaddingY = 10;        // padding of each row
    private int mSeekLinePaddingX = 0; // Seek line padding x
    private int mDisplayMode = DISPLAY_MODE_NORMAL;
    private LrcViewListener mLrcViewListener;
    private PorterDuffXfermode xformode;
    private String mLoadingLrcTip = "Downloading lrc...";

    private long currentPlayerMillis = -1;

    private Paint mPaint;
    private Paint mHighlightPaint;
    private float mTextHeight;
    public LrcView2(Context context) {
        this(context, null);
    }

    public LrcView2(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public LrcView2(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextSize(mLrcFontSize);
        xformode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
        mHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHighlightPaint.setTextSize(mLrcFontSize);
        mHighlightPaint.setXfermode(xformode);

        //文字精确高度
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        mTextHeight = fontMetrics.bottom-fontMetrics.descent-fontMetrics.ascent;
    }


    public void setListener(LrcViewListener l) {
        mLrcViewListener = l;
    }

    public void setLoadingTipText(String text) {
        mLoadingLrcTip = text;
    }

    public void setHighlightRowColor(int highlightRowColor) {
        this.mHighlightRowColor = highlightRowColor;
    }

    public void setNormalRowColor(int normalRowColor) {
        this.mNormalRowColor = normalRowColor;
    }

    public void setSeekLineColor(int seekLineColor) {
        this.mSeekLineColor = seekLineColor;
    }

    public void setSeekLineTextColor(int seekLineTextColor) {
        this.mSeekLineTextColor = seekLineTextColor;
    }

    public void setSeekLineTextSize(int seekLineTextSize) {
        this.mSeekLineTextSize = seekLineTextSize;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int height = getHeight(); // height of this view
        final int width = getWidth(); // width of this view
        if (mLrcRows == null || mLrcRows.size() == 0) {
            if (mLoadingLrcTip != null) {
                // draw tip when no lrc.
                mPaint.setColor(mHighlightRowColor);
                mPaint.setTextSize(mLrcFontSize);
                mPaint.setTextAlign(Align.CENTER);
                canvas.drawText(mLoadingLrcTip, width / 2, height / 2 - mLrcFontSize, mPaint);
            }
            return;
        }

        float rowY = 0; // vertical point of each row.
        final float rowX = width / 2f;
        int rowNum = 0;

        // 1, draw highlight row at center.
        // 2, draw rows above highlight row.
        // 3, draw rows below highlight row.

        // 1 highlight row
        float highlightRowY = height / 2f - mLrcFontSize;
        drawHighlightRow(canvas,width, height,rowX,highlightRowY);
//        String highlightText = mLrcRows.get(mHignlightRow).content;
//        mPaint.setColor(mHighlightRowColor);
//        mPaint.setTextSize(mLrcFontSize);
//        mPaint.setTextAlign(Align.CENTER);
//        canvas.drawText(highlightText, rowX, highlightRowY, mPaint);

        if (mDisplayMode == DISPLAY_MODE_SEEK) {
            // draw Seek line and current time when moving.
            mPaint.setColor(mSeekLineColor);
            canvas.drawLine(mSeekLinePaddingX, highlightRowY, width - mSeekLinePaddingX, highlightRowY, mPaint);
            mPaint.setColor(mSeekLineTextColor);
            mPaint.setTextSize(mSeekLineTextSize);
            mPaint.setTextAlign(Align.LEFT);
            canvas.drawText(mLrcRows.get(mHignlightRow).strTime, 0, highlightRowY, mPaint);
        }

        // 2 above rows
        mPaint.setColor(mNormalRowColor);
        mPaint.setTextSize(mLrcFontSize);
        mPaint.setTextAlign(Align.CENTER);
        rowNum = mHignlightRow - 1;
        rowY = highlightRowY - mPaddingY - mLrcFontSize;
        while (rowY > -mLrcFontSize && rowNum >= 0) {
            String text = mLrcRows.get(rowNum).content;
            canvas.drawText(text, rowX, rowY, mPaint);
            rowY -= (mPaddingY + mLrcFontSize);
            rowNum--;
        }

        // 3 below rows
        rowNum = mHignlightRow + 1;
        rowY = highlightRowY + mPaddingY + mLrcFontSize;
        while (rowY < height && rowNum < mLrcRows.size()) {
            String text = mLrcRows.get(rowNum).content;
            canvas.drawText(text, rowX, rowY, mPaint);
            rowY += (mPaddingY + mLrcFontSize);
            rowNum++;
        }
    }

    private void drawHighlightRow(Canvas canvas,int width, int height,float rowX, float highlightRowY) {
        LrcRow current = mLrcRows.get(mHignlightRow);
        LrcRow next = mHignlightRow + 1 == mLrcRows.size() ? null : mLrcRows.get(mHignlightRow + 1);



        long start = current.time;
        long end = next != null ? next.time : Long.MAX_VALUE;
        String mText = current.content;
        Bitmap srcBitmap = Bitmap.createBitmap(getMeasuredWidth(), Math.round(mTextHeight), Bitmap.Config.ARGB_8888);
        Canvas srcCanvas = new Canvas(srcBitmap);
        mHighlightPaint.setColor(mNormalRowColor);
        mHighlightPaint.setTextSize(mLrcFontSize);
        mHighlightPaint.setTextAlign(Align.CENTER);
        mHighlightPaint.setXfermode(null);
        float highLineWidth = mHighlightPaint.measureText(mText);
        srcCanvas.drawText(mText, highLineWidth / 2f, mTextHeight, mHighlightPaint);
        mHighlightPaint.setXfermode(xformode);
        mHighlightPaint.setColor(mHighlightRowColor);


        float index = ((currentPlayerMillis - start) * 1.0f / (end - start) * highLineWidth);
        RectF rectF = new RectF(0, 0, index,mTextHeight);
        srcCanvas.drawRect(rectF, mHighlightPaint);
        float leftOffset = (width - highLineWidth) / 2f;
        canvas.drawBitmap(srcBitmap, leftOffset,  height/2f-mTextHeight-20, null);
    }

    public void seekLrc(int position, boolean cb) {
        if (mLrcRows == null || position < 0 || position > mLrcRows.size()) {
            return;
        }
        LrcRow lrcRow = mLrcRows.get(position);
        mHignlightRow = position;
        invalidate();
        if (mLrcViewListener != null && cb) {
            mLrcViewListener.onLrcSeeked(position, lrcRow);
        }
    }

    private float mLastMotionY;
    private PointF mPointerOneLastMotion = new PointF();
    private PointF mPointerTwoLastMotion = new PointF();
    private boolean mIsFirstMove = false; // whether is first move , some events can't not detected in touch down,
    // such as two pointer touch, so it's good place to detect it in first move

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mLrcRows == null || mLrcRows.size() == 0) {
            return super.onTouchEvent(event);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "down,mLastMotionY:" + mLastMotionY);
                mLastMotionY = event.getY();
                mIsFirstMove = true;
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:

                if (event.getPointerCount() == 2) {
                    Log.d(TAG, "two move");
                    doScale(event);
                    return true;
                }
                Log.d(TAG, "one move");
                // single pointer mode ,seek
                if (mDisplayMode == DISPLAY_MODE_SCALE) {
                    //if scaling but pointer become not two ,do nothing.
                    return true;
                }

                doSeek(event);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mDisplayMode == DISPLAY_MODE_SEEK) {
                    seekLrc(mHignlightRow, true);
                }
                mDisplayMode = DISPLAY_MODE_NORMAL;
                invalidate();
                break;
            default:                break;
        }
        return true;
    }

    private void doScale(MotionEvent event) {
        if (mDisplayMode == DISPLAY_MODE_SEEK) {
            // if Seeking but pointer become two, become to scale mode
            mDisplayMode = DISPLAY_MODE_SCALE;
            Log.d(TAG, "two move but teaking ...change mode");
            return;
        }
        // two pointer mode , scale font
        if (mIsFirstMove) {
            mDisplayMode = DISPLAY_MODE_SCALE;
            invalidate();
            mIsFirstMove = false;
            setTwoPointerLocation(event);
        }
        int scaleSize = getScale(event);
        Log.d(TAG, "scaleSize:" + scaleSize);
        if (scaleSize != 0) {
            setNewFontSize(scaleSize);
            invalidate();
        }
        setTwoPointerLocation(event);
    }

    private void doSeek(MotionEvent event) {
        float y = event.getY();
        float offsetY = y - mLastMotionY; // touch offset.
        if (Math.abs(offsetY) < mMinSeekFiredOffset) {
            // move to short ,do not fire seek action
            return;
        }
        mDisplayMode = DISPLAY_MODE_SEEK;
        int rowOffset = Math.abs((int) offsetY / mLrcFontSize); // highlight row offset.
        Log.d(TAG, "move new hightlightrow : " + mHignlightRow + " offsetY: " + offsetY + " rowOffset:" + rowOffset);
        if (offsetY < 0) {
            // finger move up
            mHignlightRow += rowOffset;
        } else if (offsetY > 0) {
            // finger move down
            mHignlightRow -= rowOffset;
        }
        mHignlightRow = Math.max(0, mHignlightRow);
        mHignlightRow = Math.min(mHignlightRow, mLrcRows.size() - 1);

        if (rowOffset > 0) {
            mLastMotionY = y;
            invalidate();
        }
    }

    private void setTwoPointerLocation(MotionEvent event) {
        mPointerOneLastMotion.x = event.getX(0);
        mPointerOneLastMotion.y = event.getY(0);
        mPointerTwoLastMotion.x = event.getX(1);
        mPointerTwoLastMotion.y = event.getY(1);
    }

    private void setNewFontSize(int scaleSize) {
        mLrcFontSize += scaleSize;
        mSeekLineTextSize += scaleSize;
        mLrcFontSize = Math.max(mLrcFontSize, mMinLrcFontSize);
        mLrcFontSize = Math.min(mLrcFontSize, mMaxLrcFontSize);
        mSeekLineTextSize = Math.max(mSeekLineTextSize, mMinSeekLineTextSize);
        mSeekLineTextSize = Math.min(mSeekLineTextSize, mMaxSeekLineTextSize);
    }

    // get font scale offset
    private int getScale(MotionEvent event) {
        Log.d(TAG, "scaleSize getScale");
        float x0 = event.getX(0);
        float y0 = event.getY(0);
        float x1 = event.getX(1);
        float y1 = event.getY(1);
        float maxOffset = 0; // max offset between x or y axis,used to decide scale size

        boolean zoomin = false;

        float oldXOffset = Math.abs(mPointerOneLastMotion.x - mPointerTwoLastMotion.x);
        float newXoffset = Math.abs(x1 - x0);

        float oldYOffset = Math.abs(mPointerOneLastMotion.y - mPointerTwoLastMotion.y);
        float newYoffset = Math.abs(y1 - y0);

        maxOffset = Math.max(Math.abs(newXoffset - oldXOffset), Math.abs(newYoffset - oldYOffset));
        if (maxOffset == Math.abs(newXoffset - oldXOffset)) {
            zoomin = newXoffset > oldXOffset ? true : false;
        } else {
            zoomin = newYoffset > oldYOffset ? true : false;
        }

        Log.d(TAG, "scaleSize maxOffset:" + maxOffset);

        if (zoomin)
            return (int) (maxOffset / 10);
        else
            return -(int) (maxOffset / 10);
    }

    public void setLrc(List<LrcRow> lrcRows) {
        mLrcRows = lrcRows;
        invalidate();
    }

    public void seekLrcToTime(long time) {
        currentPlayerMillis = time;
        if (mLrcRows == null || mLrcRows.size() == 0) {
            return;
        }

        if (mDisplayMode != DISPLAY_MODE_NORMAL) {
            // touching
            return;
        }

        Log.d(TAG, "seekLrcToTime:" + time);
        // find row
        for (int i = 0; i < mLrcRows.size(); i++) {
            LrcRow current = mLrcRows.get(i);
            LrcRow next = i + 1 == mLrcRows.size() ? null : mLrcRows.get(i + 1);

            if ((time >= current.time && next != null && time < next.time)
                    || (time > current.time && next == null)) {
                seekLrc(i, false);
                return;
            }
        }
    }
}
