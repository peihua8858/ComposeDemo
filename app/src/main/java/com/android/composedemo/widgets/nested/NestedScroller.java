package com.android.composedemo.widgets.nested;

import androidx.recyclerview.widget.RecyclerView;

/**
 * @author jingyou
 */
public class NestedScroller implements INestedScroller {

    private ParentRecyclerView mParent;
    private ChildRecyclerView mChild;

    public NestedScroller(ParentRecyclerView parent) {
        this.mParent = parent;
    }

    @Override
    public void setChild(ChildRecyclerView child) {
        if (this.mChild != null) {
            this.mChild.setNestedScroller(null);
        }

        if (child == null) {
            this.mChild = null;
            return;
        }

        this.mChild = child;
        if (mParent != null) {
            this.mChild.setNestedScroller(mParent.getNestedScroller());
        }
    }

    @Override
    public ParentRecyclerView getParent() {
        return mParent;
    }

    @Override
    public ChildRecyclerView getChild() {
        return mChild;
    }

    @Override
    public void scroll(RecyclerView recyclerView, int x, int y) {
        if (recyclerView == null || recyclerView.getChildCount() <= 0) {
            return;
        }
        try {
            recyclerView.scrollBy(x, y);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void fling(RecyclerView recyclerView, int x, int y) {
        if (recyclerView == null
                || recyclerView.getChildCount() <= 0
                || (y > 0 && !recyclerView.canScrollVertically(1))
                || (y < 0 && !recyclerView.canScrollVertically(-1))) {
            return;
        }
        try {
            recyclerView.fling(x, y);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
