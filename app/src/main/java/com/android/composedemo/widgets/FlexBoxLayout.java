package com.android.composedemo.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.android.composedemo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 流布局，支持自动换行
 * {@link #mRows}可限制行数，即使数据超过也不会显示,比如mRows=2，mColumns=3,数据为9个，则只会显示两行每行3个，总共6个
 * {@link #mColumns}可限制列数，item 宽度将根据列数{@link #mColumns}和间距{@link #spanH}均分
 * {@link #autoWidth}则根据子控件的大小自动布局,且自动换行直到{@link #mRows}行数为止
 *
 * @author dingpeihua
 * @date 2024/11/8 14:11
 **/
public class FlexBoxLayout extends ViewGroup implements View.OnClickListener {
    private final List<Rect> mChildrenPositionList = new ArrayList<>();
    private int mColumns; // 列数
    private int mRows; // 行数
    private int spanH; // 水平间隔
    private int spanV; // 垂直间隔
    private boolean autoWidth; // 是否根据子控件的大小自动布局
    private Adapter mAdapter;
    private boolean mLastBottomMargin;
    private boolean mFirstTopMargin;

    public FlexBoxLayout(Context context) {
        this(context, null);
    }

    public FlexBoxLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlexBoxLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FlexBoxLayout);
        mColumns = typedArray.getInt(R.styleable.FlexBoxLayout_columns, 0);
        mRows = typedArray.getInt(R.styleable.FlexBoxLayout_rows, Integer.MAX_VALUE);
        spanH = typedArray.getDimensionPixelSize(R.styleable.FlexBoxLayout_spanH, 0);
        spanV = typedArray.getDimensionPixelSize(R.styleable.FlexBoxLayout_spanV, 0);
        autoWidth = typedArray.getBoolean(R.styleable.FlexBoxLayout_autoWidth, false);
        typedArray.recycle();
        setBackgroundColor(Color.parseColor("#00000000"));
    }

    public void setAutoWidth(boolean autoWidth) {
        this.autoWidth = autoWidth;
    }

    public int getColumns() {
        return mColumns;
    }

    public void setColumns(int mColumns) {
        this.mColumns = mColumns;
    }

    public int getRows() {
        return mRows;
    }

    public void setRows(int mRows) {
        this.mRows = mRows;
    }

    public int getSpanH() {
        return spanH;
    }

    public void setSpanH(int spanH) {
        this.spanH = spanH;
    }

    public void setLastBottomMargin(boolean mLastBottomMargin) {
        this.mLastBottomMargin = mLastBottomMargin;
    }

    public void setFirstTopMargin(boolean mFirstTopMargin) {
        this.mFirstTopMargin = mFirstTopMargin;
    }

    public int getSpanV() {
        return spanV;
    }

    public void setSpanV(int spanV) {
        this.spanV = spanV;
    }

    public String tag = "FlexBoxLayout";

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mChildrenPositionList.clear(); // 清空记录位置的列表
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int currentWidth = 0;
        int currentHeight = 0;
        int maxHeightInRow = 0;

        int childCount = getChildCount();

        int rowCount = 0; // 用于统计行数
        if ((mColumns == 0 || mRows == 0) && !autoWidth) {
            throw new IllegalArgumentException("rows and columns must be > 0 or autoWidth must be true");
        }
        // 如果顶部间距需要添加, 在初始高度中增加顶部间距
        if (mFirstTopMargin) {
            currentHeight += spanV; // spanV 为行间距，假设用于顶部间距
        }
        // 计算每个子项的宽度
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue; // 如果子视图不可见，跳过
            }

            // 确保每个子控件被正确测量
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            int childWidth;
            int childHeight;

            // 根据 autoWidth 和列数来计算子视图宽度
            if (autoWidth) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
                childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            } else {
                // 计算每个子视图的宽度，均分父组件宽度
                childWidth = (widthSize - getPaddingLeft() - getPaddingRight() - spanH * (mColumns - 1)) / mColumns;
                measureChild(child, MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY), heightMeasureSpec);
                childWidth += (lp.leftMargin + lp.rightMargin);
            }
            childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;


            // 处理换行
            if (currentWidth + childWidth > widthSize - getPaddingLeft() - getPaddingRight()) { // 注意这里，不再考虑内边距
                // 换行
                currentWidth = childWidth; // 重置当前宽度
                maxHeightInRow = childHeight; // 更新最大高度
                rowCount++; // 增加行计数
                // 如果行数超过限制，终止后续的测量
                if (mRows != Integer.MAX_VALUE && rowCount >= mRows) {
                    break; // 超过限制，停止布局后续子视图
                }
                currentHeight += maxHeightInRow + spanV; // spanV 为行间距
            } else {
                // 不换行
                currentWidth += childWidth + (i > 0 ? spanH : 0); // 计算水平间距
                maxHeightInRow = Math.max(maxHeightInRow, childHeight); // 更新当前行的最大高度
            }
            // 记录子视图的位置
            mChildrenPositionList.add(new Rect(currentWidth - childWidth, currentHeight, currentWidth, currentHeight + childHeight));
        }


        currentHeight += maxHeightInRow + (mLastBottomMargin ? spanV : 0); // 加上最后一行的高度

        // 根据行数限制可设置的高度
        if (rowCount >= mRows) {
            currentHeight = Math.min(currentHeight, (maxHeightInRow + spanV) * mRows); // 最终高度只支持设定的行数
        }
        setMeasuredDimension(
                widthMode == MeasureSpec.EXACTLY ? widthSize : currentWidth + getPaddingLeft() + getPaddingRight(),
                heightMode == MeasureSpec.EXACTLY ? heightSize : currentHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue; // 不排布不可见的子视图
            }
            // 只在实际记录的列表内进行布局
            if (i < mChildrenPositionList.size()) {
                Rect rect = mChildrenPositionList.get(i);
                child.layout(rect.left, rect.top, rect.right, rect.bottom);
            }
        }
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    public <T extends Adapter> void setAdapter(T adapter) {
        // 添加item
        mAdapter = adapter;
        adapter.registerObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                updateItem();
            }
        });
        updateItem();
    }

    private final SparseArray<ViewHolder> vhPool = new SparseArray<>();

    private void updateItem() {
        removeAllViews();
        int n = mAdapter.getItemCount();
        if (!autoWidth) {
            if (mColumns == 0 || mRows == 0) {
                throw new IllegalArgumentException("rows and columns must be > 0 or autoWidth must be true");
            }
            //如果行数不限制，则不需要限制个数
            if (mRows != Integer.MAX_VALUE) {
                int totalCount = mRows * mColumns;
                n = Math.min(n, totalCount);
            }
        }
        for (int i = 0; i < n; i++) {
            ViewHolder holder = vhPool.get(i);
            if (holder == null) {
                holder = mAdapter.onCreateViewHolder(this);
                vhPool.put(i, holder);
            }
            try {
                mAdapter.onBindViewHolder(holder, i);
            } catch (Exception e) {
                e.printStackTrace();
            }
            View child = holder.itemView;
            addView(child);
        }
    }

    @Override
    public void onClick(View v) {
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i) == v && onItemClickListener != null) {
                onItemClickListener.onItemClick(v, i);
                break;
            }
        }
    }

    public abstract static class Adapter<VH extends ViewHolder> {
        public abstract VH onCreateViewHolder(ViewGroup parent);

        public abstract void onBindViewHolder(VH holder, int position);

        public abstract int getItemCount();

        private final DataSetObservable mDataSetObservable = new DataSetObservable();

        public void registerObserver(DataSetObserver observer) {
            mDataSetObservable.registerObserver(observer);
        }

        public void notifyDataSetChanged() {
            mDataSetObservable.notifyChanged();
        }
    }

    public abstract static class ViewHolder {

        public final View itemView;

        public ViewHolder(View itemView) {
            if (itemView == null) {
                throw new IllegalArgumentException("itemView may not be null");
            }
            this.itemView = itemView;
        }
    }

    private FlexBoxLayoutClickListener onItemClickListener;

    public FlexBoxLayoutClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(FlexBoxLayoutClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).setOnClickListener(this);
        }
    }

    public interface FlexBoxLayoutClickListener {
        void onItemClick(View view, int position);
    }
}

