package com.github.panpf.zoomimage.subsampling

import android.util.Log
import androidx.annotation.MainThread
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.ZoomableState
import com.github.panpf.zoomimage.compose.Transform
import com.github.panpf.zoomimage.compose.internal.format
import com.github.panpf.zoomimage.compose.internal.isNotEmpty
import com.github.panpf.zoomimage.compose.internal.toCompatIntRect
import com.github.panpf.zoomimage.compose.internal.toCompatIntSize
import com.github.panpf.zoomimage.compose.internal.toShortString
import com.github.panpf.zoomimage.core.IntRectCompat
import com.github.panpf.zoomimage.subsampling.internal.applyExifOrientation
import com.github.panpf.zoomimage.subsampling.internal.readImageInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@Composable
fun rememberSubsamplingState(
    tileMemoryCache: TileMemoryCache? = null,
    tileBitmapPool: TileBitmapPool? = null,
    debugMode: Boolean = false,
): SubsamplingState {
    val subsamplingState = remember { SubsamplingState() }
    subsamplingState.debugMode = debugMode
    subsamplingState.tileMemoryCache = tileMemoryCache
    subsamplingState.tileBitmapPool = tileBitmapPool
    // todo 销毁时调用 state.clean()
    return subsamplingState
}

@Composable
fun BindZoomableStateAndSubsamplingState(
    zoomableState: ZoomableState,
    subsamplingState: SubsamplingState
){
    LaunchedEffect(subsamplingState.imageInfo) {
        zoomableState.contentOriginSize =
            subsamplingState.imageInfo?.let { IntSize(it.width, it.height) } ?: IntSize.Zero
        subsamplingState.refreshTiles(
            transform = zoomableState.transform,
            displayTransform = zoomableState.displayTransform,
            minScale = zoomableState.minScale,
            contentVisibleRect = zoomableState.contentVisibleRect
        )
    }
    LaunchedEffect(zoomableState.containerSize, zoomableState.contentSize) {
        subsamplingState.containerSize = zoomableState.containerSize
        subsamplingState.contentSize = zoomableState.contentSize
        subsamplingState.reset("sizeChanged")
    }
    LaunchedEffect(zoomableState.displayTransform) {
        subsamplingState.refreshTiles(
            transform = zoomableState.transform,
            displayTransform = zoomableState.displayTransform,
            minScale = zoomableState.minScale,
            contentVisibleRect = zoomableState.contentVisibleRect
        )
    }
}

class SubsamplingState {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var initJob: Job? = null
    private var imageSource: ImageSource? = null
    private var tileManager: TileManager? = null

    var containerSize: IntSize by mutableStateOf(IntSize.Zero)
    var contentSize: IntSize by mutableStateOf(IntSize.Zero)
    var imageInfo by mutableStateOf<ImageInfo?>(null)
    var debugMode: Boolean = false
    var ignoreExifOrientation: Boolean = false
    var disallowReuseBitmap: Boolean = false
    var disableMemoryCache: Boolean = false
    var tileBitmapPool: TileBitmapPool? = null
    var tileMemoryCache: TileMemoryCache? = null

    var tilesChanged by mutableStateOf(0)
        private set

    val rowTileList: List<Tile>
        get() = tileManager?.rowTileList ?: emptyList()
    val tileList: List<Tile>
        get() = tileManager?.tileList ?: emptyList()
    val imageLoadRect: IntRectCompat
        get() = tileManager?.imageLoadRect ?: IntRectCompat.Zero
    val imageVisibleRect: IntRectCompat
        get() = tileManager?.imageVisibleRect ?: IntRectCompat.Zero

    fun setImageSource(imageSource: ImageSource?) {
        val oldImageSource = this.imageSource
        if (oldImageSource == imageSource) return
        this.imageSource = imageSource
        clean("setImageSource")
        notifyTileChanged()
        reset("setImageSource")
    }

    private fun notifyTileChanged() {
        if (tilesChanged < Int.MAX_VALUE) {
            tilesChanged++
        } else {
            tilesChanged = 0
        }
    }

    fun reset(caller: String) {
        initJob?.cancel("reset:$caller")
        initJob = null
        tileManager?.destroy()
        tileManager = null
        val imageSource = this.imageSource ?: return
        val viewSize = containerSize.takeIf { it.isNotEmpty() } ?: return
        val drawableSize = contentSize.takeIf { it.isNotEmpty() } ?: return

        initJob = scope.launch(Dispatchers.Main) {
            val imageInfo = imageSource.readImageInfo()
                ?.let { if (!ignoreExifOrientation) it.applyExifOrientation() else it }
            val result =
                imageInfo?.let { canUseSubsampling(it, drawableSize.toCompatIntSize()) } ?: -10
            if (imageInfo != null && result >= 0) {
                log {
                    "setImageSource success. $caller. " +
                            "viewSize=$viewSize, " +
                            "drawableSize: ${drawableSize.toShortString()}, " +
                            "imageInfo: ${imageInfo.toShortString()}. " +
                            "'${imageSource.key}'"
                }
                tileManager = TileManager(
                    logger = Logger(),
                    imageSource = imageSource,
                    viewSize = viewSize.toCompatIntSize(),
                    tileBitmapPool = if (disallowReuseBitmap) null else tileBitmapPool,
                    tileMemoryCache = if (disableMemoryCache) null else tileMemoryCache,
                    imageInfo = imageInfo,
                    onTileChanged = {
                        notifyTileChanged()
                    }
                )
                this@SubsamplingState.imageInfo = imageInfo
            } else {
                val cause = when (result) {
                    -1 -> "The Drawable size is greater than or equal to the original image"
                    -2 -> "The drawable aspect ratio is inconsistent with the original image"
                    -3 -> "Image type not support subsampling"
                    -10 -> "Can't decode image bounds or exif orientation"
                    else -> "Unknown"
                }
                log {
                    "setImageSource failed. $caller. $cause. " +
                            "viewSize=$viewSize, " +
                            "drawableSize: ${drawableSize.toShortString()}, " +
                            "imageInfo: ${imageInfo?.toShortString()}. " +
                            "'${imageSource.key}'"
                }
            }
            initJob = null
        }
    }

    fun refreshTiles(
        transform: Transform,
        displayTransform: Transform,
        minScale: Float,
        contentVisibleRect: IntRect
    ) {
        val imageSource = imageSource ?: return
        val manager = tileManager ?: return
        val viewSize = containerSize.takeIf { it.isNotEmpty() } ?: return
        val drawableSize = contentSize.takeIf { it.isNotEmpty() } ?: return
        // todo 支持 paused
//        if (paused) {
//            log { "refreshTiles. interrupted. paused. '${imageSource.key}'" }
//            return
//        }
        if (transform.rotation % 90 != 0f) {
            log { "refreshTiles. interrupted. rotate degrees must be in multiples of 90. '${imageSource.key}'" }
            return
        }

        // todo 支持 scaling
//        val scaling = zoomEngine.isScaling
//        if (scaling) {
//            log {
//                "refreshTiles. interrupted. scaling. '${imageSource.key}'"
//            }
//            return
//        }
//        val displayMatrix = tempDisplayMatrix.apply {
//            zoomEngine.getDisplayMatrix(this)
//        }
//        val drawableVisibleRect = tempDrawableVisibleRect.apply {
//            zoomEngine.getVisibleRect(this)
//        }

        if (contentVisibleRect.isEmpty) {
            log {
                "refreshTiles. interrupted. drawableVisibleRect is empty. " +
                        "contentVisibleRect=${contentVisibleRect.toShortString()}. '${imageSource.key}'"
            }
            tileManager?.clean()
            notifyTileChanged()
            return
        }

        if (transform.scaleX.format(2) <= minScale.format(2)) {
            log { "refreshTiles. interrupted. minScale. '${imageSource.key}'" }
            tileManager?.clean()
            notifyTileChanged()
            return
        }

        tileManager?.refreshTiles(
            drawableSize.toCompatIntSize(),
            contentVisibleRect.toCompatIntRect(),
            displayTransform.scaleX.format(2)
        )
    }

    @MainThread
    fun clean(caller: String) {
        if (imageInfo == null) return
        log { "clean. $caller. '${imageSource?.key}'" }
        initJob?.cancel("destroy")
        tileManager?.destroy()
        tileManager = null
        imageSource = null
        imageInfo = null
    }

    private fun log(message: () -> String) {
        if (debugMode) {
            Log.d("SubsamplingState", message())
        }
    }
}