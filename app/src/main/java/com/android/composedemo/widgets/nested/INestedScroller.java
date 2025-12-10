package com.android.composedemo.widgets.nested;

import androidx.recyclerview.widget.RecyclerView;

public interface INestedScroller {

    void setChild(ChildRecyclerView child);

    ParentRecyclerView getParent();

    ChildRecyclerView getChild();

    void scroll(RecyclerView recyclerView, int x, int y);

    void fling(RecyclerView recyclerView, int x, int y);
}
