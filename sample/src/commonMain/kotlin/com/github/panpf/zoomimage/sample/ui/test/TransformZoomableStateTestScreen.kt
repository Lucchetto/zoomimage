package com.github.panpf.zoomimage.sample.ui.test

import androidx.annotation.FloatRange
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.github.panpf.sketch.fetch.newComposeResourceUri
import com.github.panpf.zoomimage.CoilZoomAsyncImage
import com.github.panpf.zoomimage.CoilZoomState
import com.github.panpf.zoomimage.compose.coil.CoilComposeSubsamplingImageGenerator
import com.github.panpf.zoomimage.compose.rememberZoomImageLogger
import com.github.panpf.zoomimage.compose.subsampling.rememberSubsamplingState
import com.github.panpf.zoomimage.compose.zoom.TransformZoomableState
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.compose.zoom.rememberZoomableState
import com.github.panpf.zoomimage.sample.resources.Res
import com.github.panpf.zoomimage.sample.ui.base.BaseScreen
import com.github.panpf.zoomimage.sample.ui.base.ToolbarScaffold
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.Logger.Level
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.ExperimentalResourceApi

class TransformZoomableStateTestScreen : BaseScreen() {

    @OptIn(ExperimentalResourceApi::class)
    @Composable
    override fun DrawContent() = ToolbarScaffold(
        "TransformZoomableState",
        ignoreNavigationBarInsets = false
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val afterZoomableState = rememberZoomableState()
            val beforeZoomableState = remember(afterZoomableState) {
                SyncSizeZoomableState(afterZoomableState)
            }
            LaunchedEffect(afterZoomableState) {
                afterZoomableState.logger.level = Level.Debug
            }
            LaunchedEffect(beforeZoomableState) {
                beforeZoomableState.logger.level = Level.Debug
            }

            val beforeZoomState = rememberCoilZoomState(beforeZoomableState, logLevel = Level.Debug)
            val afterZoomState = rememberCoilZoomState(afterZoomableState, logLevel = Level.Debug)

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

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxHeight()
                    .width(2.dp)
                    .background(Color.White)
            )
        }
    }

    /**
     * [Shape] where it's width is clipped to given [start] and [end] fractions of width
     */
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

    /**
     * Custom [ZoomableState] implementation that syncs the state with source [ZoomableState]
     * and ensures content size is scaled to match the source as well
     *
     * @param source the [ZoomableState] to sync with
     */
    private class SyncSizeZoomableState(
        source: ZoomableState
    ): TransformZoomableState(source) {
        private var realContentSize by mutableStateOf(IntSize.Zero)
        private val scale: Float by derivedStateOf {
            if (realContentSize == IntSize.Zero) {
                1f
            } else {
                source.contentSize.let { it.width + it.height }.toFloat() / realContentSize.let { it.width + it.height }
            }
        }
        override var contentSize: IntSize
            get() = super.contentSize
            set(value) {
                realContentSize = value
            }
        override val transform by derivedStateOf {
            super.transform.let {
                it.copy(scale = it.scale * scale)
            }
        }

        override fun toString() = "SyncSizeZoomableState(" +
                "source=${source}" +
                ")"
    }

    private companion object {

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
    }
}