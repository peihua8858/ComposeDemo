package  com.android.composedemo.widgets.nested

import android.content.Context
import android.graphics.PointF
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.util.SparseIntArray
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.android.composedemo.utils.isLandScape
import java.util.*
import kotlin.math.roundToInt

class FloorLayoutManager(private val context: Context,
                         // 列表行数 （仅支持横向列表）
                         private val spanCount: Int,
                         // 横屏列数 （仅支持横向列表）
                         private val landscapeColum: Int,
                         // 竖屏列数 （仅支持横向列表）
                         private val portraitColum: Int) :
    RecyclerView.LayoutManager(),
    ItemTouchHelper.ViewDropHandler,
    RecyclerView.SmoothScroller.ScrollVectorProvider {

    companion object {

        const val TAG = "FloorLayoutManager"

        const val HORIZONTAL = RecyclerView.HORIZONTAL

        const val VERTICAL = RecyclerView.VERTICAL

        const val ITEM_DIRECTION_HEAD = -1

        const val ITEM_DIRECTION_TAIL = 1

        const val SINGLE = "single"

        const val DOUBLE = "double"
    }

    private var orientation = HORIZONTAL

    private var mPendingPosition = RecyclerView.NO_POSITION

    private var mShouldReverseLayout = false

    private var mCachedBorders: IntArray = IntArray(spanCount + 1)

    private val mPreLayoutSpanSizeCache = SparseIntArray()

    private val mPreLayoutSpanIndexCache = SparseIntArray()

    private var mSpanSizeLookup: FloorSpanSizeLookup = DefaultFloorSpanSizeLookup()

    private var mSet: Array<View?> = arrayOfNulls(spanCount+1)

    private var mOrientationHelper: OrientationHelper = OrientationHelper.createOrientationHelper(this, orientation);

    private val mLayoutState: LayoutState = LayoutState()

    private val mLayoutChunkResult = LayoutChunkResult()

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return floorLayoutParams()
    }

    override fun generateLayoutParams(lp: ViewGroup.LayoutParams?): RecyclerView.LayoutParams {
        return floorLayoutParams()
    }

    override fun checkLayoutParams(lp: RecyclerView.LayoutParams?): Boolean {
        return lp is LayoutParams
    }

    private fun floorLayoutParams(): RecyclerView.LayoutParams {
        return if (orientation == HORIZONTAL) {
            LayoutParams(
                RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.MATCH_PARENT
            )
        } else {
            LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun guessMeasurement(maxSizeInOther: Float, currentOtherDirSize: Int, isMerge: Boolean) {
        var count = if (isMerge)  1 else spanCount
        val contentSize = (maxSizeInOther * count * getFloorSize()).roundToInt()
        calculateItemBorders(Math.max(contentSize, currentOtherDirSize))
    }

    private fun updateMeasurements() {
        val totalSpace: Int = if (getOrientation() == VERTICAL) {
            width - paddingRight - paddingLeft
        } else {
            height - paddingBottom - paddingTop
        }
        calculateItemBorders(totalSpace)
    }

    private fun calculateItemBorders(totalSpace: Int) {
        mCachedBorders = calculateItemBorders(mCachedBorders, spanCount, totalSpace)
    }

    private fun calculateItemBorders(cachedBorders: IntArray?, spanCount: Int, totalSpace: Int): IntArray {
//        LogUtil.d(TAG, "calculateItemBorders totalSpace: $totalSpace")
        var cachedBorders = cachedBorders
        if (cachedBorders == null || cachedBorders.size != spanCount + 1 || cachedBorders[cachedBorders.size - 1] != totalSpace) {
            cachedBorders = IntArray(spanCount + 1)
        }
        cachedBorders[0] = 0
        val sizePerSpan = totalSpace / spanCount / getFloorSize()
        val sizePerSpanRemainder = totalSpace % spanCount
        var consumedPixels = 0
        var additionalSize = 0
        for (i in 1..spanCount) {
            var itemSize = sizePerSpan
            additionalSize += sizePerSpanRemainder
            if (additionalSize > 0 && spanCount - additionalSize < sizePerSpanRemainder) {
                itemSize += 1
                additionalSize -= spanCount
            }
            consumedPixels += itemSize
            cachedBorders[i] = consumedPixels
        }
        return cachedBorders
    }

    private fun assignSpans(
        recycler: Recycler, state: RecyclerView.State, count: Int,
        layingOutInPrimaryDirection: Boolean
    ) {
        var span: Int
        val start: Int
        val end: Int
        val diff: Int
        if (layingOutInPrimaryDirection) {
            start = 0
            end = count
            diff = 1
        } else {
            start = count - 1
            end = -1
            diff = -1
        }
        span = 0
        var i = start
        while (i != end) {
            val view = mSet[i]!!
            val params = (view.layoutParams as LayoutParams)
            params.spanSize = getSpanSize(recycler, state, getPosition(view))
            params.spanIndex = span
            params.floorSpanSize = getFloorColumSize(recycler, state, getPosition(view))
            span += params.spanSize
            i += diff
        }
    }

    private fun measureChild(view: View, otherDirParentSpecMode: Int, isMerge: Boolean, alreadyMeasured: Boolean) {
        val lp = view.layoutParams as LayoutParams
        val decorInsets = lp.mDecorInsets
        val verticalInsets = (decorInsets.top + decorInsets.bottom
                + lp.topMargin + lp.bottomMargin)
        val horizontalInsets = (decorInsets.left + decorInsets.right
                + lp.leftMargin + lp.rightMargin)
        val availableSpaceInOther: Int = getSpaceForSpanRange(lp.spanIndex, lp.spanSize, isMerge)
//        LogUtil.d(TAG, "measureChild SpaceInOther: $availableSpaceInOther mCachedBorders: ${mCachedBorders.contentToString()}")
        val wSpec: Int
        val hSpec: Int
        if (orientation == VERTICAL) {
            wSpec = getChildMeasureSpec(
                availableSpaceInOther, otherDirParentSpecMode,
                horizontalInsets, lp.width, false
            )
            hSpec = getChildMeasureSpec(
                mOrientationHelper.totalSpace,
                heightMode,
                verticalInsets, lp.height, true
            )
        } else {
            hSpec = getChildMeasureSpec(
                availableSpaceInOther, otherDirParentSpecMode,
                verticalInsets, lp.height, false
            )
            wSpec = getChildMeasureSpec(
                mOrientationHelper.totalSpace,
                widthMode,
                horizontalInsets, lp.width, true
            )
        }
        measureChildWithDecorationsAndMargin(view, wSpec, hSpec, alreadyMeasured)
    }

    fun getSpaceForSpanRange(startSpan: Int, spanSize: Int, isMerge: Boolean): Int {
        return if (orientation == VERTICAL) {
            (mCachedBorders[spanCount - startSpan]
                    - mCachedBorders[spanCount - startSpan - spanSize])
        } else {
            if (isMerge) {
                mCachedBorders[spanCount] - mCachedBorders[startSpan]
            } else {
                mCachedBorders[startSpan + spanSize] - mCachedBorders[startSpan]
            }
        }
    }

    private fun measureChildWithDecorationsAndMargin(
        child: View, widthSpec: Int, heightSpec: Int,
        alreadyMeasured: Boolean
    ) {
        val lp = child.layoutParams as RecyclerView.LayoutParams
        val measure: Boolean = if (alreadyMeasured) {
            shouldReMeasureChild(child, widthSpec, heightSpec, lp)
        } else {
            shouldMeasureChild(child, widthSpec, heightSpec, lp)
        }
        if (measure) {
            child.measure(widthSpec, heightSpec)
        }
    }

    fun shouldReMeasureChild(
        child: View,
        widthSpec: Int,
        heightSpec: Int,
        lp: RecyclerView.LayoutParams
    ): Boolean {
        return (!isMeasurementCacheEnabled
                || !isMeasurementUpToDate(child.measuredWidth, widthSpec, lp.width)
                || !isMeasurementUpToDate(child.measuredHeight, heightSpec, lp.height))
    }

    fun shouldMeasureChild(
        child: View,
        widthSpec: Int,
        heightSpec: Int,
        lp: RecyclerView.LayoutParams
    ): Boolean {
        return (child.isLayoutRequested
                || !isMeasurementCacheEnabled
                || !isMeasurementUpToDate(child.width, widthSpec, lp.width)
                || !isMeasurementUpToDate(child.height, heightSpec, lp.height))
    }

    private fun isMeasurementUpToDate(childSize: Int, spec: Int, dimension: Int): Boolean {
        val specMode = View.MeasureSpec.getMode(spec)
        val specSize = View.MeasureSpec.getSize(spec)
        if (dimension > 0 && childSize != dimension) {
            return false
        }
        when (specMode) {
            View.MeasureSpec.UNSPECIFIED -> return true
            View.MeasureSpec.AT_MOST -> return specSize >= childSize
            View.MeasureSpec.EXACTLY -> return specSize == childSize
        }
        return false
    }

    private fun getSpanSize(recycler: Recycler, state: RecyclerView.State, pos: Int): Int {
        if (!state.isPreLayout) {
            return mSpanSizeLookup.getSpanSize(pos)
        }
        val cached: Int = mPreLayoutSpanSizeCache.get(pos, -1)
        if (cached != -1) {
            return cached
        }
        val adapterPosition = recycler.convertPreLayoutPositionToPostLayout(pos)
        if (adapterPosition == -1) {
            Log.w(TAG, ("Cannot find span size for pre layout position. It is"
                        + " not cached, not in the adapter. Pos:" + pos)
            )
            return 1
        }
        return mSpanSizeLookup.getSpanSize(adapterPosition)
    }

    private fun getFloorColumSize(recycler: Recycler, state: RecyclerView.State, pos: Int): Int {
        if (!state.isPreLayout) {
            return mSpanSizeLookup.getMergeColumSize(pos)
        }
        val adapterPosition = recycler.convertPreLayoutPositionToPostLayout(pos)
        if (adapterPosition == -1) {
            Log.w(TAG, ("Cannot find span size for pre layout position. It is"
                    + " not cached, not in the adapter. Pos:" + pos)
            )
            return 0
        }
        return mSpanSizeLookup.getMergeColumSize(adapterPosition)
    }

    override fun onLayoutChildren(recycler: Recycler, state: RecyclerView.State) {
        try {
            if (state.isPreLayout) {
                cachePreLayoutSpanMapping()
            }
            if (state.itemCount <= 0) {
                removeAndRecycleAllViews(recycler)
                return
            }
            detachAndScrapAttachedViews(recycler)
            fill(recycler, state)
            clearPreLayoutSpanMappingCache()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun fill(recycler: Recycler, state: RecyclerView.State): Int {
        val resultDelta = fillHorizontal(recycler, state)
        recycleChildren(recycler)
        return resultDelta
    }

    override fun canScrollHorizontally(): Boolean {
        return false
    }

    override fun canScrollVertically(): Boolean {
        return false
    }

    override fun scrollHorizontallyBy(
        delta: Int,
        recycler: Recycler,
        state: RecyclerView.State
    ): Int {
        return super.scrollHorizontallyBy(delta, recycler, state)
    }

    private fun fillHorizontal(
        recycler: Recycler,
        state: RecyclerView.State
    ): Int {
        orientation = HORIZONTAL
        // 分离全部的view，加入到临时缓存
        detachAndScrapAttachedViews(recycler)

        mLayoutState.mOffset = 0
        mLayoutState.mCurrentPosition = 0
        mLayoutState.mItemDirection = ITEM_DIRECTION_TAIL
        if (getLayoutType() == DOUBLE) {
            layoutPortraitScreen(recycler, state)
        } else {
            layoutLandscape(recycler, state)
        }
        return 0
    }

    private fun layoutLandscape(recycler: Recycler, state: RecyclerView.State) {
        mLayoutState.isDoubleFloor = false
        mLayoutState.isNextFloor = false
        mLayoutState.mFloorOffset = 0f
        mLayoutState.mLayoutIndex = 0
        for (i in 0 until landscapeColum) {
            layoutChunk(recycler, state, mLayoutState, mLayoutChunkResult, i)
        }
    }

    private fun layoutPortraitScreen(recycler: Recycler, state: RecyclerView.State) {
        mLayoutState.isDoubleFloor = true
        mLayoutState.isNextFloor = false
        mLayoutState.mFloorOffset = 0f
        mLayoutState.mLayoutIndex = 0
        for (i in 0 until portraitColum) {
            layoutChunk(recycler, state, mLayoutState, mLayoutChunkResult, i)
        }

        mLayoutState.isNextFloor = true
        mLayoutState.mOffset = 0
        mLayoutState.mLayoutIndex = 0
        for (i in 0 until portraitColum) {
            layoutChunk(recycler, state, mLayoutState, mLayoutChunkResult, i)
        }
    }

    private fun checkLayoutParams(p: ViewGroup.LayoutParams?): Boolean {
        return p is LayoutParams
    }

    fun layoutChunk(recycler: Recycler, state: RecyclerView.State,
                    layoutState: LayoutState, result: LayoutChunkResult, chunkIndex: Int) {
        // 控制单行绘制列数
        if (chunkIndex < mLayoutState.mLayoutIndex) {
            return
        }
        val otherDirSpecMode = mOrientationHelper.modeInOther
        val flexibleInOtherDir = otherDirSpecMode != View.MeasureSpec.EXACTLY
        val currentOtherDirSize = if (childCount > 0) mCachedBorders[spanCount] else 0
        updateMeasurements()
        val layingOutInPrimaryDirection =
            layoutState.mItemDirection == LayoutState.ITEM_DIRECTION_TAIL
        var count = 0
        var remainingSpan = spanCount
        while (count < spanCount && remainingSpan > 0) {
            val pos: Int = layoutState.mCurrentPosition
//            LogUtil.d(TAG, "mCurrentPosition: $pos  ---------------------------------------")
//            val spanSize: Int = getSpanSize(recycler, state, pos)
            // 屏蔽spanSize，切换使用floorColumSize
            val spanSize = 1
            val floorColumSize: Int = getFloorColumSize(recycler, state, pos)
//            require(floorColumSize >= 0) {
//                ("floorColumSize must be greater than zero")
//            }
            remainingSpan -= if (floorColumSize > 0) {
                spanCount
            } else {
                spanSize
            }
            if (remainingSpan < 0) {
                break
            }
            val view: View = layoutState.next(recycler, state) ?: break
            mSet[count] = view
            count++
            mLayoutState.mLayoutIndex = chunkIndex + floorColumSize
//            LogUtil.d(TAG, "chunkIndex: $chunkIndex mLayoutIndex: ${mLayoutState.mLayoutIndex}")
        }

        if (count == 0) {
            result.mFinished = true
            return
        }

        var maxSize = 0
        var maxSizeInOther = 0f

        assignSpans(recycler, state, count, layingOutInPrimaryDirection)

        for (i in 0 until count) {
            val view = mSet[i]!!
            if (layoutState.mScrapList == null) {
                if (layingOutInPrimaryDirection) {
                    addView(view)
                } else {
                    addView(view, 0)
                }
            } else {
                if (layingOutInPrimaryDirection) {
                    addDisappearingView(view)
                } else {
                    addDisappearingView(view, 0)
                }
            }
            val lp = view.layoutParams as LayoutParams
            calculateItemDecorationsForChild(view, lp.mDecorInsets)
            measureChild(view, otherDirSpecMode, isNeedMerge(chunkIndex), false)
            val size: Int = mOrientationHelper.getDecoratedMeasurement(view)
            if (size > maxSize) {
                maxSize = size
            }
            val otherSize: Float = (1f * mOrientationHelper.getDecoratedMeasurementInOther(view)
                    / lp.spanSize)
            if (otherSize > maxSizeInOther) {
                maxSizeInOther = otherSize
            }
        }

        if (flexibleInOtherDir) {
            // re-distribute columns
            guessMeasurement(maxSizeInOther, currentOtherDirSize, isNeedMerge(chunkIndex))
            // now we should re-measure any item that was match parent.
            maxSize = 0
            for (i in 0 until count) {
                val view = mSet[i]!!
                measureChild(view, View.MeasureSpec.EXACTLY, isNeedMerge(chunkIndex), true)
                val size = mOrientationHelper.getDecoratedMeasurement(view)
                if (size > maxSize) {
                    maxSize = size
                }
            }
        }

        for (i in 0 until count) {
            val view = mSet[i]!!
            if (mOrientationHelper.getDecoratedMeasurement(view) != maxSize) {
                val lp = view.layoutParams as LayoutParams
                val decorInsets = lp.mDecorInsets
                val verticalInsets = (decorInsets.top + decorInsets.bottom
                        + lp.topMargin + lp.bottomMargin)
                val horizontalInsets = (decorInsets.left + decorInsets.right
                        + lp.leftMargin + lp.rightMargin)
                val totalSpaceInOther = getSpaceForSpanRange(lp.spanIndex, lp.spanSize, isNeedMerge(chunkIndex))
                val wSpec: Int
                val hSpec: Int
                if (orientation == LinearLayoutManager.VERTICAL) {
                    wSpec = getChildMeasureSpec(
                        totalSpaceInOther, View.MeasureSpec.EXACTLY,
                        horizontalInsets, lp.width, false
                    )
                    hSpec = View.MeasureSpec.makeMeasureSpec(
                        maxSize - verticalInsets,
                        View.MeasureSpec.EXACTLY
                    )
                } else {
                    wSpec = View.MeasureSpec.makeMeasureSpec(
                        maxSize - horizontalInsets,
                        View.MeasureSpec.EXACTLY
                    )
                    hSpec = getChildMeasureSpec(
                        totalSpaceInOther, View.MeasureSpec.EXACTLY,
                        verticalInsets, lp.height, false
                    )
                }
                measureChildWithDecorationsAndMargin(view, wSpec, hSpec, true)
            }
        }

        if (mLayoutState.isNextFloor) {
            var count = if (isNeedMerge(chunkIndex)) 1 else spanCount
            mLayoutState.mFloorOffset = maxSizeInOther * count
        } else {
            mLayoutState.mFloorOffset = 0f
        }
        result.mConsumed = maxSize

        var left = 0
        var right = 0
        var top = 0
        var bottom = 0

        for (i in 0 until count) {
            if (orientation == VERTICAL) {
                top = layoutState.mOffset
                bottom = top + maxSize
            } else {
                left = layoutState.mOffset
                right = left + maxSize
            }

            val view = mSet[i]!!
            val params = view.layoutParams as LayoutParams
            if (orientation == VERTICAL) {
                left = paddingLeft + mCachedBorders!![params.spanIndex]
                right = left + mOrientationHelper.getDecoratedMeasurementInOther(view)
            } else {
                top = paddingTop + mCachedBorders!![params.spanIndex] +
                        if (mLayoutState.isNextFloor) layoutState.mFloorOffset.toInt() else 0
                bottom = top + mOrientationHelper.getDecoratedMeasurementInOther(view)
            }
//            LogUtil.d(TAG, "index in set: $i  left: $left right: $right top: $top bottom: $bottom mFloorOffset ${layoutState.mFloorOffset}")

            layoutDecoratedWithMargins(view, left, top, right, bottom)
            // Consume the available space if the view is not removed OR changed
            if (params.isItemRemoved || params.isItemChanged) {
                result.mIgnoreConsumed = true
            }
            result.mFocusable = result.mFocusable or view.hasFocusable()
        }

        Arrays.fill(mSet, null)
        mLayoutState.mOffset += maxSize
    }

    override fun onLayoutCompleted(state: RecyclerView.State) {
        mPendingPosition = RecyclerView.NO_POSITION
    }

    override fun isAutoMeasureEnabled(): Boolean {
        return true
    }

    private fun clearPreLayoutSpanMappingCache() {
        mPreLayoutSpanSizeCache.clear()
        mPreLayoutSpanIndexCache.clear()
    }

    private fun cachePreLayoutSpanMapping() {
        val childCount = childCount
        for (i in 0 until childCount) {
            val lp = getChildAt(i)!!.layoutParams as GridLayoutManager.LayoutParams
            val viewPosition = lp.viewLayoutPosition
            mPreLayoutSpanSizeCache.put(viewPosition, lp.spanSize)
            mPreLayoutSpanIndexCache.put(viewPosition, lp.spanIndex)
        }
    }

    fun setFloorSpanSizeLookup(spanSizeLookup: FloorSpanSizeLookup) {
        mSpanSizeLookup = spanSizeLookup
    }

    fun getLayoutType(): String {
        return if (context.isLandScape) {
            SINGLE
        } else {
            DOUBLE
        }
    }

    private fun isNeedMerge(chunkIndex: Int): Boolean {
        return chunkIndex < mLayoutState.mLayoutIndex
    }

    private fun getFloorSize(): Int {
        return if (mLayoutState.isDoubleFloor) 2 else 1
    }

    /**
     * 回收需回收的item
     */
    private fun recycleChildren(recycler: Recycler) {
        val scrapList = recycler.scrapList
        for (i in scrapList.indices) {
            val holder = scrapList[i]
            removeAndRecycleView(holder.itemView, recycler)
        }
    }

    override fun onAdapterChanged(
        oldAdapter: RecyclerView.Adapter<*>?,
        newAdapter: RecyclerView.Adapter<*>?
    ) {
        super.onAdapterChanged(oldAdapter, newAdapter)
        removeAllViews()
    }

    override fun prepareForDrop(view: View, target: View, x: Int, y: Int) {

    }

    override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
        if (childCount == 0) {
            return null
        }
        val firstChildPos = getPosition(getChildAt(0)!!)
        val direction = if (targetPosition < firstChildPos != mShouldReverseLayout) -1 else 1
        return if (orientation == HORIZONTAL) {
            PointF(direction.toFloat(), 0f)
        } else {
            PointF(0f, direction.toFloat())
        }
    }

    private fun getOrientation(): Int {
        return orientation
    }

    open class LayoutChunkResult {
        var mConsumed = 0
        var mFinished = false
        var mIgnoreConsumed = false
        var mFocusable = false
        fun resetInternal() {
            mConsumed = 0
            mFinished = false
            mIgnoreConsumed = false
            mFocusable = false
        }
    }

    open class LayoutState {

        companion object {
            val TAG = "LLM#LayoutState"

            val LAYOUT_START = -1

            val LAYOUT_END = 1

            val INVALID_LAYOUT = Int.MIN_VALUE

            val ITEM_DIRECTION_HEAD = -1

            val ITEM_DIRECTION_TAIL = 1

            val SCROLLING_OFFSET_NaN = Int.MIN_VALUE
        }


        /**
         * We may not want to recycle children in some cases (e.g. layout)
         */
        var mRecycle = true

        /**
         * Pixel offset where layout should start
         */
        var mOffset = 0

        var isDoubleFloor = false

        var isNextFloor = false

        var mFloorOffset = 0f

        var mLayoutIndex = 0

        /**
         * Number of pixels that we should fill, in the layout direction.
         */
        var mAvailable = 0

        /**
         * Current position on the adapter to get the next item.
         */
        var mCurrentPosition = 0

        /**
         * Defines the direction in which the data adapter is traversed.
         * Should be [.ITEM_DIRECTION_HEAD] or [.ITEM_DIRECTION_TAIL]
         */
        var mItemDirection = 0

        /**
         * Defines the direction in which the layout is filled.
         * Should be [.LAYOUT_START] or [.LAYOUT_END]
         */
        var mLayoutDirection = 0

        /**
         * Used when LayoutState is constructed in a scrolling state.
         * It should be set the amount of scrolling we can make without creating a new view.
         * Settings this is required for efficient view recycling.
         */
        var mScrollingOffset = 0

        /**
         * Used if you want to pre-layout items that are not yet visible.
         * The difference with [.mAvailable] is that, when recycling, distance laid out for
         * [.mExtraFillSpace] is not considered to avoid recycling visible children.
         */
        var mExtraFillSpace = 0

        /**
         * Contains the [.calculateExtraLayoutSpace]  extra layout
         * space} that should be excluded for recycling when cleaning up the tail of the list during
         * a smooth scroll.
         */
        var mNoRecycleSpace = 0

        /**
         * Equal to [RecyclerView.State.isPreLayout]. When consuming scrap, if this value
         * is set to true, we skip removed views since they should not be laid out in post layout
         * step.
         */
        var mIsPreLayout = false

        /**
         * The most recent [.scrollBy]
         * amount.
         */
        var mLastScrollDelta = 0

        /**
         * When LLM needs to layout particular views, it sets this list in which case, LayoutState
         * will only return views from this list and return null if it cannot find an item.
         */
        var mScrapList: List<RecyclerView.ViewHolder>? = null

        /**
         * Used when there is no limit in how many views can be laid out.
         */
        var mInfinite = false

        /**
         * @return true if there are more items in the data adapter
         */
        fun hasMore(state: RecyclerView.State): Boolean {
            return mCurrentPosition >= 0 && mCurrentPosition < state.itemCount
        }


        fun next(recycler: Recycler, state: RecyclerView.State): View? {
            if (mScrapList != null) {
                return nextViewFromScrapList()
            }
            if (mCurrentPosition < 0 || mCurrentPosition >= state.itemCount) {
                return null
            }
            val view = recycler.getViewForPosition(mCurrentPosition)
            mCurrentPosition += mItemDirection
            return view
        }

        private fun nextViewFromScrapList(): View? {
            val size = mScrapList!!.size
            for (i in 0 until size) {
                val view = mScrapList!![i].itemView
                val lp = view.layoutParams as RecyclerView.LayoutParams
                if (lp.isItemRemoved) {
                    continue
                }
                if (mCurrentPosition == lp.viewLayoutPosition) {
                    assignPositionFromScrapList(view)
                    return view
                }
            }
            return null
        }

        fun assignPositionFromScrapList() {
            assignPositionFromScrapList(null)
        }

        fun assignPositionFromScrapList(ignore: View?) {
            val closest = nextViewInLimitedList(ignore)
            mCurrentPosition = if (closest == null) {
                RecyclerView.NO_POSITION
            } else {
                (closest.layoutParams as RecyclerView.LayoutParams)
                    .viewLayoutPosition
            }
        }

        fun nextViewInLimitedList(ignore: View?): View? {
            val size = mScrapList!!.size
            var closest: View? = null
            var closestDistance = Int.MAX_VALUE
            for (i in 0 until size) {
                val view = mScrapList!![i].itemView
                val lp = view.layoutParams as RecyclerView.LayoutParams
                if (view === ignore || lp.isItemRemoved) {
                    continue
                }
                val distance = ((lp.viewLayoutPosition - mCurrentPosition)
                        * mItemDirection)
                if (distance < 0) {
                    continue  // item is not in current direction
                }
                if (distance < closestDistance) {
                    closest = view
                    closestDistance = distance
                    if (distance == 0) {
                        break
                    }
                }
            }
            return closest
        }
    }

    open class DefaultFloorSpanSizeLookup : FloorSpanSizeLookup() {
        override fun getMergeColumSize(position: Int): Int {
            return 0
        }
    }

    open abstract class FloorSpanSizeLookup : SpanSizeLookup() {

        abstract fun getMergeColumSize(position: Int):Int

        override fun getSpanSize(position: Int): Int {
            return 1
        }

        override fun getSpanIndex(position: Int, spanCount: Int): Int {
            return position % spanCount
        }
    }

    open class LayoutParams : RecyclerView.LayoutParams {
        /**
         * Returns the current span index of this View. If the View is not laid out yet, the return
         * value is `undefined`.
         *
         *
         * Starting with RecyclerView **24.2.0**, span indices are always indexed from position 0
         * even if the layout is RTL. In a vertical GridLayoutManager, **leftmost** span is span
         * 0 if the layout is **LTR** and **rightmost** span is span 0 if the layout is
         * **RTL**. Prior to 24.2.0, it was the opposite which was conflicting with
         * [SpanSizeLookup.getSpanIndex].
         *
         *
         * If the View occupies multiple spans, span with the minimum index is returned.
         *
         * @return The span index of the View.
         */
        var spanIndex = INVALID_SPAN_ID

        /**
         * Returns the number of spans occupied by this View. If the View not laid out yet, the
         * return value is `undefined`.
         *
         * @return The number of spans occupied by this View.
         */
        var spanSize = 0

        var floorSpanSize = 0

        val mDecorInsets = Rect()

        constructor(c: Context?, attrs: AttributeSet?) : super(c, attrs) {}
        constructor(width: Int, height: Int) : super(width, height) {}
        constructor(source: ViewGroup.MarginLayoutParams?) : super(source) {}
        constructor(source: ViewGroup.LayoutParams?) : super(source) {}
        constructor(source: RecyclerView.LayoutParams?) : super(source) {}

        companion object {
            /**
             * Span Id for Views that are not laid out yet.
             */
            const val INVALID_SPAN_ID = -1
        }
    }
}