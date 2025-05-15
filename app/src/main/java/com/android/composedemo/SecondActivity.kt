package com.android.composedemo

import android.content.res.Configuration
import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.android.composedemo.compose.ItemView
import com.android.composedemo.data.bean.ModuleBean
import com.android.composedemo.utils.getParcelableCompat
import com.cormor.overscroll.core.overScrollVertical

class SecondActivity : BaseActivity() {
    companion object {
        const val KEY_MODULE = "MODULE"
    }

    private var mModuleBean: ModuleBean? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val module = bundle.getParcelableCompat(KEY_MODULE, ModuleBean::class.java)
        title = module?.moduleName ?: ""
        mModuleBean = module
    }

    @Composable
    override fun ContentView(modifier: Modifier) {
        val configuration = LocalConfiguration.current
        val isLandScape =
            configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val dataList = mModuleBean?.dataList ?: arrayListOf()
        val screenWidth = configuration.screenWidthDp.dp

        /**
         * 列表左右间距
         */
        val mMarin = dimensionResource(R.dimen.dp_32)

        /**
         * 列表item之间的间距
         */
        val mGap = dimensionResource(R.dimen.dp_16)
        val spanCount =
            if (isLandScape) Constants.LANDSCAPE_COLUMN_COUNT else Constants.PORTRAIT_COLUMN_COUNT
        val horGap = if (isLandScape) mGap else mMarin
        val maxWidth = (screenWidth - mMarin * 2 - ((spanCount - 1) * horGap)) / spanCount
        LazyVerticalGrid(
            GridCells.Fixed(spanCount),
            modifier = modifier
                .padding(start = mMarin, end = mMarin)
                .fillMaxSize()
                .overScrollVertical(),
            verticalArrangement = Arrangement.spacedBy(mGap), // 垂直间距
            horizontalArrangement = Arrangement.spacedBy(horGap),
        ) {
            this.itemsIndexed(dataList) { index, item ->
                ItemView(modifier = Modifier.width(maxWidth), item = item)
            }
        }
    }
}
