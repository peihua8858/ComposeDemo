package com.android.composedemo.widgets.nested.middle;

import androidx.recyclerview.widget.RecyclerView;

import com.android.composedemo.widgets.nested.ChildRecyclerView;


/**
 * @author jingyou
 */
public class NestedMiddleScroller implements INestedMiddleScroller {

    private MiddleRecyclerView mParent;
    private ChildRecyclerView mChild;

    public NestedMiddleScroller(MiddleRecyclerView parent) {
        this.mParent = parent;
    }

    @Override
    public void setChild(ChildRecyclerView child) {
        if (this.mChild != null) {
            this.mChild.setNestedMiddleScroller(null);
        }

        if (child == null) {
            this.mChild = null;
            return;
        }

        this.mChild = child;
        if (mParent != null) {
            this.mChild.setNestedMiddleScroller(mParent.getNestedMiddleScroller());
        }
    }

    @Override
    public MiddleRecyclerView getParent() {
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
        if (recyclerView == null || recyclerView.getChildCount() <= 0) {
            return;
        }
        try {
            recyclerView.fling(x, y);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
