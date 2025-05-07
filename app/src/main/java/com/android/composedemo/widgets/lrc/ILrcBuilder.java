package com.android.composedemo.widgets.lrc;

/**
 * douzifly @Aug 10, 2013
 * github.com/douzifly
 * douzifly@gmail.com
 */

import java.util.List;

/**
 * @author douzifly
 *
 */
public interface ILrcBuilder {
    List<LrcRow> getLrcRows(String rawLrc);
}