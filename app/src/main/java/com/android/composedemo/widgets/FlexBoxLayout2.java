package com.android.composedemo.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.android.composedemo.R;

import java.util.ArrayList;
import java.util.List;

import kotlin.Pair;

/**
 * 流布局/网格布局（支持跨行跨列）
 *
 * 1) autoWidth=true：保持原有流式换行（不支持span）
 * 2) autoWidth=false 且 mColumns>0：固定列宽网格布局，支持 item 跨行跨列(rowSpan/colSpan)
 *
 * mRows：最多显示行数（超出不显示）
 * mColumns：列数（固定列宽均分）
 */
public class FlexBoxLayout2 extends ViewGroup implements View.OnClickListener {
    private final List<Rect> mChildrenPositionList = new ArrayList<>();

    private int mColumns; // 列数
    private int mRows;    // 行数
    private int spanH;    // 水平间隔
    private int spanV;    // 垂直间隔
    private boolean autoWidth;
    private SpanSizeLookup spanSizeLookup;
    private RecyclerView.Adapter mAdapter;
    private boolean mLastBottomMargin;
    private boolean mFirstTopMargin;

    public FlexBoxLayout2(Context context) {
        this(context, null);
    }

    public FlexBoxLayout2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlexBoxLayout2(Context context, AttributeSet attrs, int defStyleAttr) {
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

    public int getSpanV() {
        return spanV;
    }

    public void setSpanV(int spanV) {
        this.spanV = spanV;
    }

    public void setLastBottomMargin(boolean mLastBottomMargin) {
        this.mLastBottomMargin = mLastBottomMargin;
    }

    public void setFirstTopMargin(boolean mFirstTopMargin) {
        this.mFirstTopMargin = mFirstTopMargin;
    }

    public RecyclerView.Adapter getAdapter() {
        return mAdapter;
    }

    public void setSpanSizeLookup(SpanSizeLookup spanSizeLookup) {
        this.spanSizeLookup = spanSizeLookup;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mChildrenPositionList.clear();

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if ((mColumns == 0 || mRows == 0) && !autoWidth) {
            throw new IllegalArgumentException("rows and columns must be > 0 or autoWidth must be true");
        }

        if (autoWidth || mColumns <= 0) {
            // ===== 保持你原来的流式换行测量逻辑（不支持span）=====
            measureAsFlow(widthMeasureSpec, heightMeasureSpec);
            // 尺寸由 measureAsFlow 内 setMeasuredDimension 处理
            return;
        }

        // ===== 固定列网格 + 支持跨行跨列 =====
        int contentWidth = widthSize - getPaddingLeft() - getPaddingRight();
        int cellW = (contentWidth - spanH * (mColumns - 1)) / mColumns;
        if (cellW < 0) cellW = 0;

        int topOffset = getPaddingTop() + (mFirstTopMargin ? spanV : 0);
        int leftOffset = getPaddingLeft();

        // 用占位表记录哪些网格单元已被占用
        // 行数若不限制，就动态扩容；若限制则最多 mRows 行
        final boolean limitRows = (mRows != Integer.MAX_VALUE);
        int maxRowsForOccupy = limitRows ? mRows : 64; // 初始
        boolean[][] occupied = new boolean[maxRowsForOccupy][mColumns];

        int usedMaxRowExclusive = 0; // 记录使用到的最大行（开区间）
        int childCount = getChildCount();

        int measuredHeight = topOffset; // 累计高度（底部在最后算）
        int[] rowHeights = new int[limitRows ? mRows : 64]; // 每行高度（动态）

        int laidOutCount = 0;

        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                // 仍保持位置列表索引对齐：不记录rect
                continue;
            }
            Pair<Integer, Integer> spanSize = spanSizeLookup.getSpanSize(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            int colSpan = Math.max(1, Math.min(spanSize.getFirst(), mColumns));
            int rowSpan = Math.max(1, spanSize.getSecond());

            // 如果有限制行数，span 不能超过限制，否则永远放不下
            if (limitRows) rowSpan = Math.min(rowSpan, mRows);

            // 找一个能放下该item的起始(row,col)
            int[] pos = findPositionForSpan(occupied, mColumns, colSpan, rowSpan, limitRows ? mRows : -1);
            if (pos == null) {
                // 放不下：直接停止（保持“超出限制不显示”的语义）
                break;
            }

            int startRow = pos[0];
            int startCol = pos[1];

            // 如果行数受限且 startRow+rowSpan 超出，停止
            if (limitRows && startRow + rowSpan > mRows) break;

            // 动态扩容 occupied/rowHeights（不限行时）
            int needRows = startRow + rowSpan;
            if (!limitRows && needRows > occupied.length) {
                int newRows = Math.max(needRows, occupied.length * 2);
                occupied = resizeOccupied(occupied, newRows, mColumns);
            }
            if (!limitRows && needRows > rowHeights.length) {
                rowHeights = resizeInt(rowHeights, Math.max(needRows, rowHeights.length * 2));
            }

            // 标记占用
            for (int rr = startRow; rr < startRow + rowSpan; rr++) {
                for (int cc = startCol; cc < startCol + colSpan; cc++) {
                    occupied[rr][cc] = true;
                }
            }

            // 先测量 child：宽度按 colSpan * cellW + 间隔
            int childContentW = colSpan * cellW + spanH * (colSpan - 1);
            int childWSpec = MeasureSpec.makeMeasureSpec(
                    Math.max(0, childContentW - lp.leftMargin - lp.rightMargin),
                    MeasureSpec.EXACTLY
            );
            int childHSpec = getChildMeasureSpec(heightMeasureSpec,
                    getPaddingTop() + getPaddingBottom(), lp.height);
            child.measure(childWSpec, childHSpec);

            int childMeasuredH = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

            // 行高策略：把该 item 的高度均摊到它跨的行中会比较复杂；
            // 这里采用“把该 item 的高度分配给起始行的行高”，并保证容器高度足够：
            // 实际布局时使用行顶累计 + item 高度即可。
            // 为避免跨行时后续行顶计算不够，我们用“行基线高度”方案：
            // 每行基线高度取该行中所有 item(不论是否跨行)的最大“占用高度/rowSpan”。
            int perRowH = (int) Math.ceil(childMeasuredH * 1f / rowSpan);
            for (int rr = startRow; rr < startRow + rowSpan; rr++) {
                rowHeights[rr] = Math.max(rowHeights[rr], perRowH);
            }

            usedMaxRowExclusive = Math.max(usedMaxRowExclusive, startRow + rowSpan);

            // 计算该 item 左上角坐标：依赖“每行高度累计”
            int childLeft = leftOffset + startCol * (cellW + spanH) + lp.leftMargin;
            int childTop = topOffset + sumRowsHeight(rowHeights, 0, startRow) + startRow * spanV + lp.topMargin;

            // item 实际高度为测量高度，跨多行时会覆盖下方行区域
            int childRight = childLeft + (childContentW - lp.leftMargin - lp.rightMargin);
            int childBottom = childTop + (child.getMeasuredHeight());

            mChildrenPositionList.add(new Rect(childLeft, childTop, childRight, childBottom));
            laidOutCount++;
        }

        // 计算总高度：按使用到的行数，把行高+行间距累加
        int rowsToUse = usedMaxRowExclusive;
        if (limitRows) rowsToUse = Math.min(rowsToUse, mRows);

        int totalRowsHeight = sumRowsHeight(rowHeights, 0, rowsToUse);
        measuredHeight = topOffset + totalRowsHeight + Math.max(0, rowsToUse - 1) * spanV;

        if (mLastBottomMargin) measuredHeight += spanV;
        measuredHeight += getPaddingBottom();

        int measuredWidth = widthMode == MeasureSpec.EXACTLY ? widthSize
                : getPaddingLeft() + getPaddingRight() + contentWidth;

        setMeasuredDimension(
                measuredWidth,
                heightMode == MeasureSpec.EXACTLY ? heightSize : measuredHeight
        );
    }

    private void measureAsFlow(int widthMeasureSpec, int heightMeasureSpec) {
        // 基本保留你原始逻辑，只做了轻微修正：padding参与计算更一致
        mChildrenPositionList.clear();

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int maxLineWidth = widthSize - getPaddingLeft() - getPaddingRight();
        int x = 0;
        int y = getPaddingTop();
        int lineMaxH = 0;

        int rowCount = 0;

        if (mFirstTopMargin) y += spanV;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;

            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);

            int childW = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            int childH = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

            int neededW = (x == 0 ? childW : x + spanH + childW);

            if (neededW > maxLineWidth) {
                rowCount++;
                if (mRows != Integer.MAX_VALUE && rowCount >= mRows) break;

                y += lineMaxH + spanV;
                x = 0;
                lineMaxH = 0;
            }

            int left = getPaddingLeft() + x + lp.leftMargin;
            int top = y + lp.topMargin;
            int right = left + child.getMeasuredWidth();
            int bottom = top + child.getMeasuredHeight();

            mChildrenPositionList.add(new Rect(left, top, right, bottom));

            x = (x == 0 ? childW : x + spanH + childW);
            lineMaxH = Math.max(lineMaxH, childH);
        }

        int h = y + lineMaxH + getPaddingBottom() + (mLastBottomMargin ? spanV : 0);

        setMeasuredDimension(
                widthMode == MeasureSpec.EXACTLY ? widthSize : (getPaddingLeft() + getPaddingRight() + x),
                heightMode == MeasureSpec.EXACTLY ? heightSize : h
        );
    }

    private static int sumRowsHeight(int[] rowHeights, int start, int endExclusive) {
        int sum = 0;
        for (int i = start; i < endExclusive; i++) sum += rowHeights[i];
        return sum;
    }

    private static boolean[][] resizeOccupied(boolean[][] old, int newRows, int cols) {
        boolean[][] n = new boolean[newRows][cols];
        for (int i = 0; i < old.length; i++) {
            System.arraycopy(old[i], 0, n[i], 0, cols);
        }
        return n;
    }

    private static int[] resizeInt(int[] old, int newLen) {
        int[] n = new int[newLen];
        System.arraycopy(old, 0, n, 0, old.length);
        return n;
    }

    /**
     * 在 occupied 中找一个能放下 colSpan*rowSpan 的位置（从第0行第0列开始扫描）
     * @param maxRowsLimit  -1 表示不限行；否则最多多少行（不可越界）
     */
    private static int[] findPositionForSpan(boolean[][] occupied, int columns, int colSpan, int rowSpan, int maxRowsLimit) {
        int maxRows = (maxRowsLimit > 0) ? maxRowsLimit : occupied.length;

        for (int r = 0; r < maxRows; r++) {
            for (int c = 0; c <= columns - colSpan; c++) {
                if (canPlace(occupied, r, c, rowSpan, colSpan, maxRowsLimit)) {
                    return new int[]{r, c};
                }
            }
        }
        return null;
    }

    private static boolean canPlace(boolean[][] occupied, int startRow, int startCol, int rowSpan, int colSpan, int maxRowsLimit) {
        int maxRows = (maxRowsLimit > 0) ? maxRowsLimit : Integer.MAX_VALUE;
        if (startRow + rowSpan > maxRows) return false;

        for (int r = startRow; r < startRow + rowSpan; r++) {
            if (r >= occupied.length) return true; // 不限行且需要扩容：认为可放（外层会扩容）
            for (int c = startCol; c < startCol + colSpan; c++) {
                if (occupied[r][c]) return false;
            }
        }
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int visibleIndex = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;

            if (visibleIndex < mChildrenPositionList.size()) {
                Rect rect = mChildrenPositionList.get(visibleIndex);
                child.layout(rect.left, rect.top, rect.right, rect.bottom);
            }
            visibleIndex++;
        }
    }

    // ====== LayoutParams：新增跨行跨列能力 ======
    public abstract static class SpanSizeLookup {
        public abstract Pair<Integer, Integer> getSpanSize(int position);
    }

    public static class LayoutParams extends MarginLayoutParams {
        public int colSpan = 1; // 跨列
        public int rowSpan = 1; // 跨行

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    private final RecyclerViewDataObserver mObserver = new RecyclerViewDataObserver();

    // ===== Adapter/点击：保持原逻辑 =====
    public <T extends RecyclerView.Adapter> void setAdapter(T adapter) {
        mAdapter = adapter;
        adapter.registerAdapterDataObserver(mObserver);
        updateItem();
    }

    private final SparseArray<RecyclerView.ViewHolder> vhPool = new SparseArray<>();

    private void updateItem() {
        removeAllViews();
        int n = mAdapter.getItemCount();

        // 这里不再按 mRows*mColumns 直接截断（因为有span，能放多少取决于占位）
        // 但为了保持原有“固定行列时的截断”语义：仍然给一个上限，避免无意义 addView 太多
        if (!autoWidth && mColumns > 0 && mRows != Integer.MAX_VALUE) {
            n = Math.min(n, mRows * mColumns * 2); // 给个宽松上限，真实能显示多少由测量占位决定
        }

        for (int i = 0; i < n; i++) {
            RecyclerView.ViewHolder holder = vhPool.get(i);
            if (holder == null) {
                int itemType = mAdapter.getItemViewType(i);
                holder = mAdapter.onCreateViewHolder(this, itemType);
                vhPool.put(i, holder);
            }
            mAdapter.onBindViewHolder(holder, i);

            View child = holder.itemView;
            addView(child);
        }

        // 重新绑定点击
        if (onItemClickListener != null) {
            for (int i = 0; i < getChildCount(); i++) {
                getChildAt(i).setOnClickListener(this);
            }
        }
        requestLayout();
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

    private class RecyclerViewDataObserver extends RecyclerView.AdapterDataObserver {
        RecyclerViewDataObserver() {
        }

        @Override
        public void onChanged() {
            updateItem();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            onChanged();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            onChanged();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            onChanged();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            onChanged();
        }


        @Override
        public void onStateRestorationPolicyChanged() {
            onChanged();
        }
    }

//    public abstract static class Adapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
//        public abstract VH onCreateViewHolder(ViewGroup parent);
//
//        @Override
//        public abstract void onBindViewHolder(VH holder, int position);
//
//        @Override
//        public abstract int getItemCount();

//        private final DataSetObservable mDataSetObservable = new DataSetObservable();
//        public void registerObserver(DataSetObserver observer) { mDataSetObservable.registerObserver(observer); }
//        @Override
//        public void notifyDataSetChanged() { mDataSetObservable.notifyChanged(); }
//    }

//    public abstract static class ViewHolder {
//        public static final int TEXT_COLOR_RED = Color.parseColor("#FD4D4A");
//        public static final int TEXT_COLOR_GREY = Color.parseColor("#7282A3");
//        public final View itemView;
//
//        public ViewHolder(View itemView) {
//            if (itemView == null) throw new IllegalArgumentException("itemView may not be null");
//            this.itemView = itemView;
//        }
//    }

    private FlowLayoutClickListener onItemClickListener;

    public FlowLayoutClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(FlowLayoutClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).setOnClickListener(this);
        }
    }

    public interface FlowLayoutClickListener {
        void onItemClick(View view, int position);
    }
}

