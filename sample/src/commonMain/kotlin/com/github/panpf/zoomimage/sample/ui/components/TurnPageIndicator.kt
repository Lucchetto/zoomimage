package com.github.panpf.zoomimage.sample.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import com.github.panpf.sketch.LocalPlatformContext
import com.github.panpf.zoomimage.sample.EventBus
import com.github.panpf.zoomimage.sample.appSettings
import com.github.panpf.zoomimage.sample.image.PhotoPalette
import com.github.panpf.zoomimage.sample.resources.Res
import com.github.panpf.zoomimage.sample.resources.ic_arrow_down
import com.github.panpf.zoomimage.sample.resources.ic_arrow_left
import com.github.panpf.zoomimage.sample.resources.ic_arrow_right
import com.github.panpf.zoomimage.sample.resources.ic_arrow_up
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource


@Composable
@OptIn(ExperimentalFoundationApi::class)
fun BoxScope.TurnPageIndicator(
    pagerState: PagerState,
    photoPaletteState: MutableState<PhotoPalette>? = null
) {
    val turnPage = remember { MutableSharedFlow<Boolean>() }
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        EventBus.keyEvent.collect { keyEvent ->
            if (keyEvent.type == KeyEventType.KeyUp && !keyEvent.isMetaPressed) {
                when (keyEvent.key) {
                    Key.PageUp, Key.DirectionLeft -> turnPage.emit(true)
                    Key.PageDown, Key.DirectionRight -> turnPage.emit(false)
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        turnPage.collect { previousPage ->
            if (previousPage) {
                val nextPageIndex = (pagerState.currentPage + 1) % pagerState.pageCount
                pagerState.animateScrollToPage(nextPageIndex)
            } else {
                val nextPageIndex =
                    (pagerState.currentPage - 1).let { if (it < 0) pagerState.pageCount + it else it }
                pagerState.animateScrollToPage(nextPageIndex)
            }
        }
    }
    val turnPageIconModifier = Modifier
        .padding(50.dp)
        .size(50.dp)
        .clip(CircleShape)
    val appSettings = LocalPlatformContext.current.appSettings
    val colorScheme = MaterialTheme.colorScheme
    val horizontalLayout by appSettings.horizontalPagerLayout.collectAsState(initial = true)
    val photoPalette by photoPaletteState ?: remember { mutableStateOf(PhotoPalette(colorScheme)) }
    if (horizontalLayout) {
        IconButton(
            onClick = { coroutineScope.launch { turnPage.emit(false) } },
            modifier = turnPageIconModifier.align(Alignment.CenterStart),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = photoPalette.containerColor,
                contentColor = photoPalette.contentColor
            ),
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_left),
                contentDescription = "Previous",
            )
        }
        IconButton(
            onClick = { coroutineScope.launch { turnPage.emit(true) } },
            modifier = turnPageIconModifier.align(Alignment.CenterEnd),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = photoPalette.containerColor,
                contentColor = photoPalette.contentColor
            ),
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_right),
                contentDescription = "Next",
            )
        }
    } else {
        IconButton(
            onClick = { coroutineScope.launch { turnPage.emit(false) } },
            modifier = turnPageIconModifier.align(Alignment.TopCenter),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = photoPalette.containerColor,
                contentColor = photoPalette.contentColor
            ),
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_up),
                contentDescription = "Previous",
            )
        }
        IconButton(
            onClick = { coroutineScope.launch { turnPage.emit(true) } },
            modifier = turnPageIconModifier.align(Alignment.BottomCenter),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = photoPalette.containerColor,
                contentColor = photoPalette.contentColor
            ),
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_down),
                contentDescription = "Next",
            )
        }
    }
}