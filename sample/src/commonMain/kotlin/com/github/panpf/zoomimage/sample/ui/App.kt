package com.github.panpf.zoomimage.sample.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.ScaleTransition
import com.github.panpf.sketch.fetch.newComposeResourceUri
import com.github.panpf.zoomimage.CoilZoomAsyncImage
import com.github.panpf.zoomimage.CoilZoomState
import com.github.panpf.zoomimage.compose.ZoomState
import com.github.panpf.zoomimage.compose.coil.CoilComposeSubsamplingImageGenerator
import com.github.panpf.zoomimage.compose.rememberZoomImageLogger
import com.github.panpf.zoomimage.compose.rememberZoomState
import com.github.panpf.zoomimage.compose.subsampling.rememberSubsamplingState
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.compose.zoom.rememberProxyZoomableState
import com.github.panpf.zoomimage.compose.zoom.rememberZoomableState
import com.github.panpf.zoomimage.rememberCoilZoomState
import com.github.panpf.zoomimage.sample.EventBus
import com.github.panpf.zoomimage.sample.resources.Res
import com.github.panpf.zoomimage.sample.ui.theme.AppTheme
import com.github.panpf.zoomimage.sample.util.Platform
import com.github.panpf.zoomimage.sample.util.current
import com.github.panpf.zoomimage.sample.util.isMobile
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.Logger.Level
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.ExperimentalResourceApi

@Composable
fun rememberCoilZoomState(
    zoomableState: ZoomableState,
    subsamplingImageGenerators: ImmutableList<CoilComposeSubsamplingImageGenerator>? = null,
    logLevel: Level? = null,
): CoilZoomState {
    val logger: Logger = rememberZoomImageLogger(tag = "CoilZoomAsyncImage", level = logLevel)
    val subsamplingState = rememberSubsamplingState(zoomableState)
    return remember(logger, zoomableState, subsamplingState, subsamplingImageGenerators) {
        CoilZoomState(logger, zoomableState, subsamplingState, subsamplingImageGenerators)
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App(onContentChanged: ((Navigator) -> Unit)? = null) {
    AppTheme {
        Scaffold {
            BoxWithConstraints {
                val parentMaxWidth = maxWidth
                val parentMaxHeight = maxHeight
                val afterZoomableState = rememberZoomableState()
                val beforeZoomableState = rememberProxyZoomableState(afterZoomableState)

                val beforeZoomState = rememberCoilZoomState(beforeZoomableState)
                val afterZoomState = rememberCoilZoomState(afterZoomableState)

                Row(
                    modifier = Modifier.fillMaxHeight()
                ) {
                    BoxWithConstraints(
                        modifier = Modifier.weight(1f).clipToBounds()
                    ) {
                        CoilZoomAsyncImage(
                            model = newComposeResourceUri(Res.getUri("files/pexels-taryn-elliott-4253835_400.jpg")),
                            contentDescription = null,
                            modifier = Modifier.padding(start = parentMaxWidth - maxWidth).requiredSize(parentMaxWidth, parentMaxHeight),
                            zoomState = beforeZoomState,
                        )
                    }
                    BoxWithConstraints(
                        modifier = Modifier.weight(1f).clipToBounds()
                    ) {
                        CoilZoomAsyncImage(
                            model = newComposeResourceUri(Res.getUri("files/pexels-taryn-elliott-4253835.jpg")),
                            contentDescription = null,
                            modifier = Modifier.padding(end = parentMaxWidth - maxWidth).requiredSize(parentMaxWidth, parentMaxHeight),
                            zoomState = afterZoomState,
                        )
                    }

                }
            }
        }
    }
}