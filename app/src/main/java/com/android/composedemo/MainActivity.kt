package com.android.composedemo

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.android.composedemo.data.bean.AdapterBean
import com.android.composedemo.data.bean.Data
import com.android.composedemo.data.bean.ModuleBean
import com.android.composedemo.data.viewmodel.DemoHomeViewModel
import com.android.composedemo.ui.theme.DemoFontFamily
import com.android.composedemo.utils.dimensionResourceByPx
import com.android.composedemo.utils.ellipsize
import com.android.composedemo.utils.getDimension
import com.android.composedemo.utils.loadCircleAvatar
import com.android.composedemo.utils.subListToLines
import com.android.composedemo.widgets.CircleImageView
import kotlin.math.min
import kotlin.math.roundToInt

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
fun Activity.MarketListView(
    modifier: Modifier = Modifier,
    modelState3: MutableList<AdapterBean<*>>,
    viewmodel: DemoHomeViewModel = viewModel()
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(modelState3.toMutableList()) { message ->
            val itemData = message.data as ModuleBean
            when (message.itemType) {
                Constants.TYPE_TITLE -> {
                    TitleItemView(module = itemData)
                }

                Constants.TYPE_ITEM -> {
                    HomeItemView(module = itemData)
                }

                Constants.TYPE_BANNER -> {
                    BannerView(module = itemData)
                }

                Constants.TYPE_AI_BANNER -> {
                    AiBannerView(module = itemData)
                }

                Constants.TYPE_POST_BANNER -> {
                    PostBannerView(module = itemData)
                }
            }
        }
    }
}

/**
 * 标题栏
 */
@Composable
fun Activity.TitleItemView(module: ModuleBean) {
    val configuration = LocalConfiguration.current
    val isLandScape =
        configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = dimensionResource(id = R.dimen.dp_32),
                end = dimensionResource(id = R.dimen.dp_32),
                top = dimensionResource(id = R.dimen.dp_16),
                bottom = dimensionResource(id = R.dimen.dp_14)
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = module.moduleName,
            modifier = Modifier.align(Alignment.CenterVertically),
            fontSize = dimensionResource(id = R.dimen.sp_16).value.sp,
            fontFamily = DemoFontFamily.NotoSansSc500,
            color = colorResource(id = R.color.color_1f1f1f)
        )
        val isVisibleMore =
            module.totalCount > if (isLandScape) Constants.LANDSCAPE_ITEM_COUNT else Constants.PORTRAIT_ITEM_COUNT
        if (isVisibleMore) {
            Text(text = stringResource(id = R.string.text_more),
                fontSize = dimensionResource(id = R.dimen.sp_12).value.sp,
                fontFamily = DemoFontFamily.NotoSansSc400,
                color = colorResource(id = R.color.color_1f1f1f_25),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .clickable {
                        //点击more
                        val intent =
                            Intent(this@TitleItemView, SecondActivity::class.java)
                        intent.putExtra(SecondActivity.KEY_MODULE, module)
                        startActivity(intent)
                    })
        }
    }
}

/**
 * 模块子项
 */
@Composable
fun HomeItemView(modifier: Modifier = Modifier, module: ModuleBean) {
    val dataList = module.dataList ?: return
    val configuration = LocalConfiguration.current
    val isLandScape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val maxCount =
        if (isLandScape) Constants.LANDSCAPE_ITEM_COUNT else Constants.PORTRAIT_ITEM_COUNT
    val spanCount =
        if (isLandScape) Constants.LANDSCAPE_COLUMN_COUNT else Constants.PORTRAIT_COLUMN_COUNT

    /**
     * 列表左右间距
     */
    val mMarin = dimensionResource(R.dimen.dp_32)

    /**
     * 列表item之间的间距
     */
    val mGap = dimensionResource(R.dimen.dp_8)
    val rowsData = dataList.subListToLines(spanCount, min(maxCount, dataList.size))
    Column(modifier = modifier.fillMaxWidth()) {
        for ((index, items) in rowsData.withIndex()) {
            Row(modifier = modifier.fillMaxWidth()) {
                for ((i, item) in items.withIndex()) {
                    ItemView(
                        modifier = Modifier
                            .weight(1f)
                            .wrapContentHeight(Alignment.CenterVertically)
                            .padding(
                                start = (if (isLandScape && i > 0) mGap else if (i == 0) mMarin else mMarin / 2),
                                bottom = mGap * 2,
                                end = if (i == spanCount - 1) mMarin else if (isLandScape && i < spanCount - 1) mGap else if (isLandScape) 0.dp else mMarin / 2
                            ), item = item
                    )
                }
                //将不足一行的数据用空白组件补充
                if (items.size < spanCount) {
                    for (j in items.size until spanCount) {
                        Spacer(modifier = modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * 模块子项
 */
@Composable
fun ItemView(modifier: Modifier = Modifier, item: Data) {
    Row(modifier = modifier
        .height(dimensionResource(id = R.dimen.dp_68))
        .clip(shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_10)))
        .background(color = colorResource(id = R.color.white))
        .clickable {

        }) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(
                    start = dimensionResource(id = R.dimen.dp_10),
                    top = dimensionResource(id = R.dimen.dp_10),
                    bottom = dimensionResource(id = R.dimen.dp_10)
                )
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.icon)
//                    .error(R.drawable.ic_avatar_placeholder)
//                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .build(),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_10)))
                    .size(
                        dimensionResource(id = R.dimen.dp_48),
                        dimensionResource(id = R.dimen.dp_48)
                    )
                    .border(
                        width = dimensionResource(id = R.dimen.dp_0_25),
                        color = colorResource(id = R.color.color_1f1f1f_20),
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_10)) // 设置圆角边框
                    ),
                contentDescription = ""
            )
        }
        Column(
            modifier = Modifier
                .padding(start = dimensionResource(id = R.dimen.dp_12))
                .align(Alignment.CenterVertically)
                .weight(1f)
                .padding(
                    end = dimensionResource(id = R.dimen.dp_10),
                    top = dimensionResource(id = R.dimen.dp_10),
                    bottom = dimensionResource(id = R.dimen.dp_10)
                )
        ) {
            Text(
                text = item.title.ellipsize(10) ?: "",
                color = colorResource(id = R.color.color_1f1f1f),
                fontSize = dimensionResource(id = R.dimen.sp_16).value.sp,
                fontFamily = DemoFontFamily.NotoSansSc400,
                maxLines = 1
            )
            Text(
                text = item.subTitle.ellipsize(19) ?: "",
                color = colorResource(id = R.color.color_1f1f1f_50),
                fontSize = dimensionResource(id = R.dimen.sp_10).value.sp,
                fontFamily = DemoFontFamily.NotoSansSc400,
                modifier = Modifier.padding(top = dimensionResource(id = R.dimen.dp_3)),
                maxLines = 1
            )
        }
    }
}

@Composable
fun BannerView(modifier: Modifier = Modifier, module: ModuleBean) {
    val raidus = dimensionResource(id = R.dimen.dp_10)
    val mGap = dimensionResource(id = R.dimen.dp_16)
    val dp12 = dimensionResource(id = R.dimen.dp_12)
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = mGap),
    ) {
        val dataList = module.dataList ?: arrayListOf()
        itemsIndexed(dataList) { index, item ->
            Box(modifier = modifier
                .padding(
                    start = if (index == 0) dimensionResource(id = R.dimen.dp_32) else 0.dp,
                    top = dp12,
                    end = if (index == dataList.size - 1) dimensionResource(id = R.dimen.dp_32) else mGap
                )
                .size(
                    dimensionResource(id = R.dimen.dp_288),
                    dimensionResource(id = R.dimen.dp_180)
                )
                .clickable {}) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.picUrl)
                        .error(R.drawable.ic_banner_placeholder)
                        .placeholder(R.drawable.ic_banner_placeholder)
                        .build(),
                    contentDescription = "",
                    modifier = modifier
                        .clip(RoundedCornerShape(raidus))
                        .size(
                            dimensionResource(id = R.dimen.dp_288),
                            dimensionResource(id = R.dimen.dp_180)
                        ),
                    contentScale = ContentScale.Crop
                )
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .clip(shape = RoundedCornerShape(bottomStart = raidus, bottomEnd = raidus))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    colorResource(id = R.color.color_000000_50)
                                ), start = Offset.Zero,
                                end = Offset(0f, Float.POSITIVE_INFINITY)
                            )
                        )
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.icon)
                            .error(R.drawable.ic_avatar_placeholder)
                            .placeholder(R.drawable.ic_avatar_placeholder)
                            .build(),
                        modifier = modifier
                            .padding(
                                start = raidus,
                                top = raidus,
                                bottom = raidus
                            )
                            .border(
                                width = dimensionResource(id = R.dimen.dp_0_25),
                                color = colorResource(id = R.color.color_1f1f1f_20),
                                shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_24))
                            )
                            .align(Alignment.CenterVertically)
                            .clip(shape = CircleShape)
                            .size(
                                dimensionResource(id = R.dimen.dp_48),
                                dimensionResource(id = R.dimen.dp_48)
                            ),
                        contentDescription = ""
                    )
                    Column(
                        modifier = modifier
                            .weight(1f)
                            .padding(start = dimensionResource(id = R.dimen.dp_11))
                            .align(Alignment.CenterVertically)
                    ) {
                        Text(
                            modifier = modifier.fillMaxWidth(),
                            text = item.title.ellipsize(10) ?: "",
                            fontSize = dimensionResource(id = R.dimen.sp_16).value.sp,
                            fontFamily = DemoFontFamily.NotoSansSc400,
                            color = colorResource(id = R.color.white)
                        )
                        Text(
                            modifier = modifier
                                .fillMaxWidth()
                                .padding(top = dimensionResource(id = R.dimen.dp_3)),
                            text = "@ ${item.author}".ellipsize(14) ?: "",
                            fontSize = dimensionResource(id = R.dimen.sp_10).value.sp,
                            fontFamily = DemoFontFamily.NotoSansSc400,
                            color = colorResource(id = R.color.white_72)
                        )
                    }
                }
                Box(
                    modifier = modifier
                        .align(Alignment.BottomEnd)
                        .padding(
                            end = dimensionResource(id = R.dimen.dp_16),
                            bottom = dimensionResource(id = R.dimen.dp_14)
                        )
                ) {
                    Image(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_banner_arrow),
                        contentScale = ContentScale.FillBounds,
                        contentDescription = "", modifier = modifier
                            .size(
                                dimensionResource(id = R.dimen.dp_33),
                                dimensionResource(id = R.dimen.dp_33)
                            )
                            .clip(shape = CircleShape)

                    )
                }
            }
        }
    }
}

@Composable
fun AiBannerView(modifier: Modifier = Modifier, module: ModuleBean) {
    val raidus = dimensionResource(id = R.dimen.dp_10)
    val mGap = dimensionResource(id = R.dimen.dp_16)
    val configuration = LocalConfiguration.current
    val isLandScape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
    ) {
        val dataList = module.dataList ?: arrayListOf()
        itemsIndexed(dataList) { index, item ->
            Box(modifier = modifier
                .padding(
                    start = if (index == 0) dimensionResource(id = R.dimen.dp_32) else 0.dp,
                    end = if (index == dataList.size - 1) dimensionResource(id = R.dimen.dp_32) else mGap
                )
                .clip(shape = RoundedCornerShape(raidus))
                .width(dimensionResource(id = R.dimen.dp_212))
                .height(dimensionResource(id = R.dimen.dp_180))
                .clickable {}) {
                var blurredBitmap by remember {
                    mutableStateOf(
                        Bitmap.createBitmap(
                            1,
                            1,
                            Bitmap.Config.ARGB_8888
                        )
                    )
                }
                val dp68 = dimensionResourceByPx(id = R.dimen.dp_60)
                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.picUrl)
                        .error(R.drawable.ic_banner_placeholder)
                        .placeholder(R.drawable.ic_banner_placeholder)
                        .build(),
                    imageLoader = ImageLoader.Builder(LocalContext.current)
                        .allowHardware(false)
                        .build(),
                    onSuccess = {
                        val oriBitmap = it.result.drawable.toBitmap()
                        val bitmap = createBlurredBitmap(oriBitmap, dp68)
                        blurredBitmap = bitmap
                    })
                Image(
                    painter = painter,
                    contentDescription = "",
                    modifier = modifier
                        .clip(RoundedCornerShape(raidus))
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                ConstraintLayout(
                    modifier = modifier
                        .align(Alignment.BottomCenter)
                        .clip(shape = RoundedCornerShape(bottomStart = raidus, bottomEnd = raidus))
                        .width(dimensionResource(id = R.dimen.dp_212))
                        .fillMaxHeight()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    colorResource(id = R.color.color_000000_50)
                                ), start = Offset.Zero,
                                end = Offset(0f, Float.POSITIVE_INFINITY)
                            )
                        )
                ) {
                    val (column, image, avatar) = createRefs()
                    val dp18 = dimensionResource(id = R.dimen.dp_18)
                    val dp16 = dimensionResource(id = R.dimen.dp_16)
                    AndroidView(modifier = Modifier
                        .constrainAs(avatar) {
                            top.linkTo(parent.top, dp18)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            bottom.linkTo(column.top)
                        }
                        .size(
                            dimensionResource(id = R.dimen.dp_80),
                            dimensionResource(id = R.dimen.dp_80)
                        ), factory = {
                        val imageView = CircleImageView(it).apply {
                            loadCircleAvatar(item.icon)
                            mBorderWidth = getDimension(id = R.dimen.dp_5)
                            mBorderColor = "#13FFFFFF".toColorInt()
                            mInnerBorderWidth = getDimension(id = R.dimen.dp_5)
                            mInnerBorderColor = "#32FFFFFF".toColorInt()
                        }
                        imageView
                    })
                    Image(
                        bitmap = blurredBitmap.asImageBitmap(),
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .constrainAs(image) {
                                bottom.linkTo(parent.bottom)
                                top.linkTo(column.top)
                                verticalChainWeight = 1f
                                height = Dimension.fillToConstraints
                            }
                            .blur(radius = 20.dp) // 应用模糊
                    )
                    Column(
                        modifier = Modifier
                            .constrainAs(column) {
                                top.linkTo(avatar.bottom, dp16)
                                bottom.linkTo(parent.bottom)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            }
                            .height(dimensionResource(id = R.dimen.dp_68))
                    ) {
                        Text(
                            modifier = modifier
                                .padding(top = dimensionResource(id = R.dimen.dp_16))
                                .align(Alignment.CenterHorizontally),
                            text = item.title.ellipsize(10) ?: "",
                            fontSize = dimensionResource(id = if (isLandScape) R.dimen.sp_16 else R.dimen.sp_14).value.sp,
                            fontFamily = DemoFontFamily.NotoSansSc400,
                            color = colorResource(id = R.color.white)
                        )
                        Text(
                            modifier = modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(
                                    top = dimensionResource(id = R.dimen.dp_3),
                                ),
                            text = item.subTitle,
                            fontSize = dimensionResource(id = R.dimen.sp_10).value.sp,
                            fontFamily = DemoFontFamily.NotoSansSc400,
                            color = colorResource(id = R.color.white_72)
                        )
                    }
                }
            }
        }
    }
}

fun createBlurredBitmap(original: Bitmap, blurAreaHeightPx: Float): Bitmap {
    // 确保我们只处理底部区域
    val srcRect = android.graphics.Rect(
        0,
        (original.height - blurAreaHeightPx).roundToInt(),
        original.width,
        original.height
    )
    return Bitmap.createBitmap(
        original,
        srcRect.left,
        srcRect.top,
        srcRect.width(),
        srcRect.height()
    )
}

@Composable
fun PostBannerView(modifier: Modifier = Modifier, module: ModuleBean) {
    val raidus = dimensionResource(id = R.dimen.dp_10)
    val mGap = dimensionResource(id = R.dimen.dp_16)
    val configuration = LocalConfiguration.current
    val isLandScape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
    ) {
        val dataList = module.dataList ?: arrayListOf()
        itemsIndexed(dataList) { index, item ->
            Box(modifier = modifier
                .padding(
                    start = if (index == 0) dimensionResource(id = R.dimen.dp_32) else 0.dp,
                    end = if (index == dataList.size - 1) dimensionResource(id = R.dimen.dp_32) else mGap
                )
                .clip(shape = RoundedCornerShape(raidus))
                .width(dimensionResource(id = R.dimen.dp_212))
                .height(dimensionResource(id = R.dimen.dp_200))
                .clickable {}) {
                var blurredBitmap by remember {
                    mutableStateOf(
                        Bitmap.createBitmap(
                            1,
                            1,
                            Bitmap.Config.ARGB_8888
                        )
                    )
                }
                val dp68 = dimensionResourceByPx(id = R.dimen.dp_60)
                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.picUrl)
                        .error(R.drawable.ic_banner_placeholder)
                        .placeholder(R.drawable.ic_banner_placeholder)
                        .build(),
                    imageLoader = ImageLoader.Builder(LocalContext.current)
                        .allowHardware(false)
                        .build(),
                    onSuccess = {
                        val oriBitmap = it.result.drawable.toBitmap()
                        val bitmap = createBlurredBitmap(oriBitmap, dp68)
                        blurredBitmap = bitmap
                    })
                Image(
                    painter = painter,
                    contentDescription = "",
                    modifier = modifier
                        .clip(RoundedCornerShape(raidus))
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                ConstraintLayout(
                    modifier = modifier
                        .align(Alignment.BottomCenter)
                        .clip(shape = RoundedCornerShape(bottomStart = raidus, bottomEnd = raidus))
                        .width(dimensionResource(id = R.dimen.dp_212))
                        .fillMaxHeight()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    colorResource(id = R.color.color_000000_50)
                                ), start = Offset.Zero,
                                end = Offset(0f, Float.POSITIVE_INFINITY)
                            )
                        )
                ) {
                    val (column, image, avatar) = createRefs()
                    val dp16 = dimensionResource(id = R.dimen.dp_16)
                    Image(
                        bitmap = blurredBitmap.asImageBitmap(),
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .constrainAs(image) {
                                bottom.linkTo(parent.bottom)
                                top.linkTo(column.top)
                                verticalChainWeight = 1f
                                height = Dimension.fillToConstraints
                            }
                            .blur(radius = 20.dp) // 应用模糊
                    )
                    Column(
                        modifier = Modifier
                            .constrainAs(column) {
                                top.linkTo(avatar.bottom, dp16)
                                bottom.linkTo(parent.bottom)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            }
                            .height(dimensionResource(id = R.dimen.dp_68))
                    ) {
                        Text(
                            modifier = modifier
                                .padding(top = dimensionResource(id = R.dimen.dp_16))
                                .align(Alignment.CenterHorizontally),
                            text = item.title.ellipsize(10) ?: "",
                            fontSize = dimensionResource(id = if (isLandScape) R.dimen.sp_16 else R.dimen.sp_14).value.sp,
                            fontFamily = DemoFontFamily.NotoSansSc400,
                            color = colorResource(id = R.color.white)
                        )
                        Text(
                            modifier = modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(
                                    top = dimensionResource(id = R.dimen.dp_3),
                                ),
                            text = item.subTitle,
                            fontSize = dimensionResource(id = R.dimen.sp_10).value.sp,
                            fontFamily = DemoFontFamily.NotoSansSc400,
                            color = colorResource(id = R.color.white_72)
                        )
                    }
                }
            }
        }
    }
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