package com.android.composedemo.widgets.nested.middle;

import androidx.recyclerview.widget.RecyclerView;

import com.android.composedemo.widgets.nested.ChildRecyclerView;


public interface INestedMiddleScroller {

    void setChild(ChildRecyclerView child);

    MiddleRecyclerView getParent();

    ChildRecyclerView getChild();

    void scroll(RecyclerView recyclerView, int x, int y);

    void fling(RecyclerView recyclerView, int x, int y);
}
