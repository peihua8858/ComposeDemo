package com.android.composedemo

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.android.composedemo.ui.theme.ComposeDemoTheme
import com.android.composedemo.ui.theme.MarketFontFamily
import com.android.composedemo.utils.isLandScape

abstract class BaseActivity : ComponentActivity() {
    private val mTitle = mutableStateOf<String>("")
    override fun setTitle(titleId: Int) {
        mTitle.value = getString(titleId)
    }

    override fun setTitle(title: CharSequence) {
        mTitle.value = title.toString()
    }

    val bundle: Bundle
        get() = intent?.extras ?: Bundle()

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        window.setBackgroundDrawableResource(if (isLandScape) R.mipmap.bg_window_hor else R.mipmap.bg_window_ver)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawableResource(if (isLandScape) R.mipmap.bg_window_hor else R.mipmap.bg_window_ver)
        enableEdgeToEdge()
        setContent {
            ComposeDemoTheme {
                val isShowDialog = remember { mutableStateOf(false) }
                Scaffold(modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.Transparent),
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent
                            ),
                            navigationIcon = {
                                Image(
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(dimensionResource(id = R.dimen.dp_32))
                                        .clip(shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_8)))
                                        .clickable { finish() },
                                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_back_arrow),
                                    contentDescription = ""
                                )
                            },
                            title = {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = mTitle.value,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .wrapContentWidth(Alignment.CenterHorizontally) // 水平居中
                                            .clickable(onClick = { isShowDialog.value = true })
                                            .align(Alignment.Center),
                                        fontFamily = MarketFontFamily.NotoSansSc500,
                                        fontSize = dimensionResource(id = R.dimen.sp_20).value.sp,
                                    )
                                }
                            },
                        )
                    }) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        ContentView(Modifier.padding(innerPadding))
                        if (isShowDialog.value) {
                            MarketAlertDialog(
                                dialogTitle = "标题",
                                dialogText = "弹窗内容",
                                onDismissRequest = {
                                    isShowDialog.value = false
                                },
                                onConfirmation = {
                                    isShowDialog.value = false
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    abstract fun ContentView(modifier: Modifier)
}

@Composable
fun MarketAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
) {
    AlertDialog(
        title = {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = dialogTitle,
                    modifier = Modifier
                        .wrapContentSize(Alignment.Center)
                        .align(Alignment.Center),
                    fontFamily = MarketFontFamily.NotoSansSc500,
                    fontSize = dimensionResource(id = R.dimen.sp_20).value.sp,
                )
            }
        },
        text = {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = dialogText,
                    modifier = Modifier
                        .wrapContentSize(Alignment.Center)
                        .align(Alignment.Center),
                    fontFamily = MarketFontFamily.NotoSansSc400,
                    fontSize = dimensionResource(id = R.dimen.sp_16).value.sp,
                )
            }
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            Row(modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_8)),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onDismissRequest()
                    }
                ) {
                    Text(
                        stringResource(id = R.string.text_cancel),
                        fontFamily = MarketFontFamily.NotoSansSc400,
                        fontSize = dimensionResource(id = R.dimen.sp_16).value.sp,
                    )
                }
                TextButton(
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_8)),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onConfirmation()
                    }
                ) {
                    Text(
                        stringResource(id = R.string.text_config),
                        fontFamily = MarketFontFamily.NotoSansSc500,
                        fontSize = dimensionResource(id = R.dimen.sp_16).value.sp,
                    )
                }
            }
        },
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_8)),
    )
}