package com.android.composedemo.GitHub

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.android.composedemo.BaseActivity
import com.android.composedemo.GitHub.model.Repo
import com.android.composedemo.GitHub.viewmodel.GithubViewModel
import com.android.composedemo.GitHub.viewmodel.UiAction
import com.android.composedemo.GitHub.viewmodel.UiModel
import com.android.composedemo.R
import com.android.composedemo.ui.theme.DemoFontFamily
import com.android.composedemo.utils.LaunchedLoadMore
import com.android.composedemo.utils.dLog
import com.android.composedemo.utils.forEach
import com.android.composedemo.utils.showToast
import com.android.composedemo.widgets.pullrefreshlayout.PullToRefresh
import com.android.composedemo.widgets.pullrefreshlayout.rememberPullToRefreshState

/**
 * use [PullToRefresh]
 */
class PullToRefreshByGithubPagerActivity : BaseActivity() {
    private val mViewModel by viewModels<GithubViewModel> { GithubViewModel.Factory(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Compose Demo"
    }

    @Composable
    override fun ContentView(modifier: Modifier) {
        GithubContentView(modifier, mViewModel)
    }
}


@Composable
//@ExperimentalMaterial3Api
fun GithubContentView(modifier: Modifier, mViewModel: GithubViewModel) {
    val state = mViewModel.mUiState
    val action = mViewModel.userAction
    val items = mViewModel.pagingDataFlow.collectAsLazyPagingItems()
    val refreshing =
        rememberPullToRefreshState(isRefreshing = items.loadState.refresh is LoadState.Loading)
    val dp32 = dimensionResource(R.dimen.dp_32)
    val value = remember { mutableStateOf(state.value.query) }
    Column(
        modifier
            .fillMaxSize()
            .height(dimensionResource(R.dimen.dp_56))
    ) {
        OutlinedTextField(value.value, modifier = Modifier
            .padding(start = dp32, end = dp32)
            .fillMaxWidth(), singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            keyboardActions = KeyboardActions(
                onDone = {
                    action(UiAction.Search(value.value))
                }
            ),
            label = {
                Text("Github")
            },
            onValueChange = {
                value.value = it
            })
        PullToRefresh(
            state = refreshing,
            onRefresh = {
                items.refresh()
            },
            modifier = Modifier
        ) {

            val refreshState = items.loadState.refresh
            if (refreshState is LoadState.Loading) {
                if (items.itemCount == 0) {
                    LoadingView(Modifier)
                }
            } else if (refreshState is LoadState.Error) {
                if (items.itemCount == 0) {
                    ErrorView(Modifier, refreshState) {
                        items.retry()
                    }
                } else {
                    showToast("刷新失败")
                    ShowContent(Modifier, items)
                }
            } else {
                ShowContent(Modifier, items)
            }
        }
    }
}

@Composable
private fun ErrorView(
    modifier: Modifier,
    loadState: LoadState.Error, click: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val painter = rememberAsyncImagePainter(
            ImageRequest.Builder(LocalContext.current)
                .data(data = R.mipmap.ic_no_data_found)
                .build()
        )
        Image(
            painter = painter,
            contentDescription = "",
            modifier = modifier
        )
        Text(
            "加载失败:${loadState.error.localizedMessage}",
        )
        Text(
            "点击重试",
            color = colorResource(id = R.color.light_blue_600),
            modifier = Modifier
                .clickable {
                    click()
                }
        )
    }
}

@Composable
fun ShowContent(
    modifier: Modifier,
    items: LazyPagingItems<UiModel>,
    state: LazyListState = rememberLazyListState(),
) {
    val loadMoreState = items.loadState.append
    ListView(modifier, items, state) {
        item {
            if (loadMoreState is LoadState.Error) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.dp_16)),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "加载失败:${loadMoreState.error.localizedMessage}",
                        modifier = Modifier
                    )
                    Text(
                        "点击重试",
                        color = colorResource(id = R.color.light_blue_600),
                        modifier = Modifier
                            .clickable {
                                items.retry()
                            }
                    )
                }
            } else if (loadMoreState.endOfPaginationReached.not()) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(dimensionResource(R.dimen.dp_16))
                    )
                }
            }
        }
    }
    // 自动加载下一页逻辑
    state.LaunchedLoadMore(items)
}

@Composable
fun LoadingView(modifier: Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        val painter = rememberAsyncImagePainter(
            ImageRequest.Builder(LocalContext.current)
                .data(data = R.mipmap.ic_loading1)
                .build()
        )
        val transition = rememberInfiniteTransition(label = "")
        val progress by transition.animateValue(
            0f,
            1f,
            Float.VectorConverter,
            infiniteRepeatable(
                animation = tween(
                    durationMillis = 1332,
                    easing = LinearEasing
                )
            ), label = ""
        )
        Image(
            painter = painter,
            contentDescription = "",
            modifier = modifier
                .align(Alignment.Center)
                .rotate(progress * 360),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ListView(
    modifier: Modifier,
    data: LazyPagingItems<UiModel>,
    state: LazyListState = rememberLazyListState(),
    composeLoadMore: LazyListScope.() -> Unit
) {
    LazyColumn(modifier = modifier.fillMaxSize(), state = state) {
        data.forEach { item ->
            when (item) {
                is UiModel.RepoItem -> {
                    item {
                        RepoItemView(repo = item.repo)
                    }
                }

                is UiModel.SeparatorItem -> {
                    stickyHeader {
                        SeparatorView(item = item)
                    }
                }
            }
        }
        composeLoadMore()
    }
}

@Composable
private fun RepoItemView(repo: Repo) {
    val context = LocalContext.current
    val raidus = dimensionResource(R.dimen.dp_16)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.dp_16))
            .clip(RoundedCornerShape(raidus))
            .background(colorResource(id = R.color.white))
            .clickable {
                repo.url.let { url ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.dp_16))

        ) {
            Text(
                repo.fullName, fontFamily = DemoFontFamily.Monospace500,
                color = colorResource(id = R.color.light_blue_600),
                fontSize = dimensionResource(R.dimen.dp_20).value.sp
            )
            Text(
                repo.description ?: "", fontFamily = DemoFontFamily.Monospace400,
                fontSize = dimensionResource(R.dimen.dp_18).value.sp
            )
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = dimensionResource(R.dimen.dp_16))
            ) {
                val (language, stars, forks) = createRefs()
                Text(
                    stringResource(id = R.string.language, repo.language ?: ""),
                    modifier = Modifier
                        .constrainAs(language) {
                            start.linkTo(parent.start)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        },
                    fontFamily = DemoFontFamily.Monospace400,
                    color = colorResource(id = R.color.color_1f1f1f_72),
                    fontSize = dimensionResource(R.dimen.dp_14).value.sp
                )
                TextButton(onClick = { }, modifier = Modifier
                    .wrapContentSize(Alignment.Center)
                    .background(colorResource(id = R.color.white))
                    .width(IntrinsicSize.Max)
                    .constrainAs(forks) {
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }) {
                    Row {
                        Image(
                            painter = painterResource(id = R.drawable.ic_git_branch),
                            contentDescription = "",
                        )
                        Text(
                            repo.forks.toString(), fontFamily = DemoFontFamily.Monospace400,
                            color = colorResource(id = R.color.color_1f1f1f_72),
                            maxLines = 1,
                            fontSize = dimensionResource(R.dimen.dp_14).value.sp
                        )
                    }
                }
                TextButton(onClick = { },
                    modifier = Modifier
                        .background(colorResource(id = R.color.white))
                        .wrapContentSize(Alignment.Center)
                        .width(IntrinsicSize.Max)
                        .constrainAs(stars) {
                            end.linkTo(forks.start)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        }) {
                    Row {
                        Image(
                            painter = painterResource(id = R.drawable.ic_star),
                            contentDescription = "",
                        )
                        Text(
                            repo.stars.toString(), fontFamily = DemoFontFamily.Monospace400,
                            color = colorResource(id = R.color.color_1f1f1f_72),
                            maxLines = 1,
                            fontSize = dimensionResource(R.dimen.dp_14).value.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SeparatorView(item: UiModel.SeparatorItem) {
    Text(
        text = item.description, fontFamily = DemoFontFamily.Monospace500,
        fontSize = dimensionResource(R.dimen.dp_24).value.sp,
        color = colorResource(id = R.color.white),
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.black))
            .padding(dimensionResource(R.dimen.dp_16))
    )
}