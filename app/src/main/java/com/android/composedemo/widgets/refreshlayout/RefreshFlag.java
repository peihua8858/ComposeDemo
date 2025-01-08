package com.android.composedemo.widgets.refreshlayout;

/**
 * 加载状态
 *
 * @author dingpeihua
 * @date 2024/7/25 09:53
 **/
public @interface RefreshFlag {
    /**
     * 无
     */
    int NONE = 0;
    /**
     * 无网络
     */
    int NO_NET_WORK = 1;
    /**
     * 失败
     */
    int FAILURE = 2;
    /**
     * 成功
     */
    int SUCCESS = 3;
}
