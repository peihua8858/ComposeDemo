package com.android.composedemo

import android.app.Activity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.android.composedemo.data.viewmodel.DemoHomeViewModel
import com.android.composedemo.ui.theme.DemoFontFamily

class MainActivity : BaseActivity() {
    private val mViewModel by viewModels<DemoHomeViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.app_name)
        mViewModel.requestHomeData2(1)
    }

    @Composable
    override fun ContentView(modifier: Modifier) {
        MarketListView(modifier, mViewModel.modelState3)
    }
}

object Constants {
    const val TYPE_BANNER = 0
    const val TYPE_TITLE = 1
    const val TYPE_AI_BANNER = 2
    const val TYPE_POST_BANNER = 3
    const val TYPE_ITEM = 4

    /**
     * 竖屏时列数
     */
    const val PORTRAIT_COLUMN_COUNT = 2

    /**
     * 横屏时列数
     */
    const val LANDSCAPE_COLUMN_COUNT = 3

    /**
     * 竖屏时item数量
     */
    const val PORTRAIT_ITEM_COUNT = 10

    /**
     * 横屏时item数量
     */
    const val LANDSCAPE_ITEM_COUNT = 9
}


@Composable
fun Activity.ToolbarView(modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen.dp_56))
            .statusBarsPadding()
            .padding(
                start = dimensionResource(id = R.dimen.dp_14),
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            contentScale = ContentScale.Crop,
            modifier = modifier
                .align(Alignment.CenterVertically)
                .size(dimensionResource(id = R.dimen.dp_32))
                .clip(shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_8)))
                .clickable { finish() },
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_back_arrow),
            contentDescription = ""
        )
        Text(
            text = stringResource(id = R.string.app_name),
            textAlign = TextAlign.Center,
            modifier = modifier
                .weight(1f) // 占据一定空间
                .wrapContentWidth(Alignment.CenterHorizontally) // 水平居中
                .fillMaxWidth(),
            fontFamily = DemoFontFamily.NotoSansSc500,
            fontSize = dimensionResource(id = R.dimen.sp_20).value.sp,
        )
    }
}