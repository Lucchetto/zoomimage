package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.panpf.zoomimage.sample.ui.photoalbum.compose.CoilListImage
import com.github.panpf.zoomimage.sample.ui.photoalbum.compose.GlideListImage
import com.github.panpf.zoomimage.sample.ui.photoalbum.compose.SketchListImage

enum class ZoomImageType(
    val title: String,
    val subtitle: String?,
    val drawListContent: @Composable (sketchImageUri: String, modifier: Modifier) -> Unit,
    val drawContent: @Composable (sketchImageUri: String) -> Unit,
    val my: Boolean,
    val supportIgnoreExifOrientation: Boolean,
) {
    MyZoomImage(
        title = "ZoomImage",
        subtitle = null,
        drawListContent = { sketchImageUri, modifier ->
            SketchListImage(sketchImageUri, modifier)
        },
        drawContent = { sketchImageUri ->
            ZoomImageSample(sketchImageUri)
        },
        my = true,
        supportIgnoreExifOrientation = true,
    ),

    SketchZoomAsyncImage(
        title = "SketchZoomAsyncImage",
        subtitle = null,
        drawListContent = { sketchImageUri, modifier ->
            SketchListImage(sketchImageUri, modifier)
        },
        drawContent = { sketchImageUri ->
            SketchZoomAsyncImageSample(sketchImageUri)
        },
        my = true,
        supportIgnoreExifOrientation = true,
    ),

    CoilZoomAsyncImage(
        title = "CoilZoomAsyncImage",
        subtitle = null,
        drawListContent = { sketchImageUri, modifier ->
            CoilListImage(sketchImageUri, modifier)
        },
        drawContent = { sketchImageUri ->
            CoilZoomAsyncImageSample(sketchImageUri)
        },
        my = true,
        supportIgnoreExifOrientation = false,
    ),

    GlideZoomAsyncImage(
        title = "GlideZoomAsyncImage",
        subtitle = null,
        drawListContent = { sketchImageUri, modifier ->
            GlideListImage(sketchImageUri, modifier)
        },
        drawContent = { sketchImageUri ->
            GlideZoomAsyncImageSample(sketchImageUri)
        },
        my = true,
        supportIgnoreExifOrientation = false,
    ),

    TelephotoZoomableAsyncImage(
        title = "ZoomableAsyncImage",
        subtitle = "Telephoto",
        drawListContent = { sketchImageUri, modifier ->
            CoilListImage(sketchImageUri, modifier)
        },
        drawContent = { sketchImageUri ->
            TelephotoZoomableAsyncImageSample(sketchImageUri)
        },
        my = false,
        supportIgnoreExifOrientation = false,
    ),
}