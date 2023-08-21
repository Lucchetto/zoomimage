package com.github.panpf.zoomimage.compose.subsampling

import android.content.res.Resources
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntRect
import com.github.panpf.zoomimage.compose.internal.isEmpty
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.subsampling.Tile
import kotlin.math.ceil
import kotlin.math.floor

fun Modifier.subsampling(
    subsamplingState: SubsamplingState,
    zoomableState: ZoomableState?,  // todo 删除 zoomableState
): Modifier = this.drawWithContent {
    drawContent()
    val imageInfo = subsamplingState.imageInfo ?: return@drawWithContent
    val contentSize = subsamplingState.contentSize
        .takeIf { !it.isEmpty() } ?: return@drawWithContent
    val tileList = subsamplingState.tileList
        .takeIf { it.isNotEmpty() } ?: return@drawWithContent
    val imageLoadRect = subsamplingState.imageLoadRect
        .takeIf { !it.isEmpty } ?: return@drawWithContent

    val widthScale: Float = imageInfo.width / (contentSize.width.toFloat())
    val heightScale: Float = imageInfo.height / (contentSize.height.toFloat())
    var insideLoadCount = 0
    var outsideLoadCount = 0
    var realDrawCount = 0
    tileList.forEach { tile ->
        if (tile.srcRect.overlaps(imageLoadRect)) {
            insideLoadCount++
            val tileSrcRect = tile.srcRect
            val tileDrawRect = IntRect(
                left = floor(tileSrcRect.left / widthScale).toInt(),
                top = floor(tileSrcRect.top / heightScale).toInt(),
                right = floor(tileSrcRect.right / widthScale).toInt(),
                bottom = floor(tileSrcRect.bottom / heightScale).toInt()
            )

            val tileBitmap = tile.bitmap
            if (tileBitmap != null && !tileBitmap.isRecycled) {
                realDrawCount++
                val srcRect = IntRect(0, 0, tileBitmap.width, tileBitmap.height)
                drawImage(
                    image = tileBitmap.asImageBitmap(),
                    srcOffset = srcRect.topLeft,
                    srcSize = srcRect.size,
                    dstOffset = tileDrawRect.topLeft,
                    dstSize = tileDrawRect.size,
//                        alpha = 0.5f,
                )
            }

            if (subsamplingState.showTileBounds) {
                val boundsColor = when (tile.state) {
                    Tile.STATE_LOADED -> Color.Green
                    Tile.STATE_LOADING -> Color.Yellow
                    else -> Color.Red
                }
                val boundsStrokeWidth = 1f * Resources.getSystem().displayMetrics.density
                val boundsStrokeHalfWidth = boundsStrokeWidth / 2
                val tileBoundsRect = Rect(
                    left = floor(tileDrawRect.left + boundsStrokeHalfWidth),
                    top = floor(tileDrawRect.top + boundsStrokeHalfWidth),
                    right = ceil(tileDrawRect.right - boundsStrokeHalfWidth),
                    bottom = ceil(tileDrawRect.bottom - boundsStrokeHalfWidth)
                )
                drawRect(
                    color = boundsColor,
                    topLeft = tileBoundsRect.topLeft,
                    size = tileBoundsRect.size,
                    style = Stroke(width = boundsStrokeWidth),
                    alpha = 0.5f,
                )
            }
        } else {
            outsideLoadCount++
        }
    }
    subsamplingState.logger.d {
        "drawTiles. tiles=${tileList.size}, " +
                "insideLoadCount=${insideLoadCount}, " +
                "outsideLoadCount=${outsideLoadCount}, " +
                "realDrawCount=${realDrawCount}. " +
                "'${subsamplingState.imageKey}'"
    }
}