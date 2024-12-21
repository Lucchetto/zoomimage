package com.github.panpf.zoomimage.sample.ui

import androidx.annotation.FloatRange
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import cafe.adriel.voyager.navigator.Navigator
import com.github.panpf.sketch.fetch.newComposeResourceUri
import com.github.panpf.zoomimage.CoilZoomAsyncImage
import com.github.panpf.zoomimage.CoilZoomState
import com.github.panpf.zoomimage.compose.coil.CoilComposeSubsamplingImageGenerator
import com.github.panpf.zoomimage.compose.rememberZoomImageLogger
import com.github.panpf.zoomimage.compose.subsampling.rememberSubsamplingState
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.compose.zoom.rememberProxyZoomableState
import com.github.panpf.zoomimage.compose.zoom.rememberZoomableState
import com.github.panpf.zoomimage.sample.resources.Res
import com.github.panpf.zoomimage.sample.ui.theme.AppTheme
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

@Immutable
private data class ClipWidthShape(
    @FloatRange(from = 0.0, to = 1.0) private val start: Float,
    @FloatRange(from = 0.0, to = 1.0) private val end: Float
): Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val leftClipFraction: Float
        val rightClipFraction: Float

        when (layoutDirection) {
            LayoutDirection.Ltr -> {
                leftClipFraction = start
                rightClipFraction = end
            }
            LayoutDirection.Rtl -> {
                leftClipFraction = end
                rightClipFraction = start
            }
        }

        val rect = Rect(
            left = size.width * leftClipFraction,
            top = 0f,
            right = size.width - size.width * rightClipFraction,
            bottom = size.height
        )

        return Outline.Rectangle(rect)
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App(onContentChanged: ((Navigator) -> Unit)? = null) {
    AppTheme {
        Scaffold {
            Box(modifier = Modifier.fillMaxSize()) {
                val afterZoomableState = rememberZoomableState()
                val beforeZoomableState = rememberProxyZoomableState(afterZoomableState)

                val beforeZoomState = rememberCoilZoomState(beforeZoomableState)
                val afterZoomState = rememberCoilZoomState(afterZoomableState)

                CoilZoomAsyncImage(
                    model = newComposeResourceUri(Res.getUri("files/pexels-taryn-elliott-4253835_400.jpg")),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(ClipWidthShape(0f, 0.5f)),
                    zoomState = beforeZoomState,
                )

                CoilZoomAsyncImage(
                    model = newComposeResourceUri(Res.getUri("files/pexels-taryn-elliott-4253835.jpg")),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(ClipWidthShape(0.5f, 0f)),
                    zoomState = afterZoomState,
                )
            }
        }
    }
}