package com.github.panpf.zoomimage.sample.ui.photoalbum.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.github.panpf.sketch.compose.AsyncImage
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.resize.LongImageClipPrecisionDecider
import com.github.panpf.sketch.resize.LongImageScaleDecider
import com.github.panpf.sketch.resize.Precision.SAME_ASPECT_RATIO
import com.github.panpf.sketch.stateimage.IconStateImage
import com.github.panpf.sketch.stateimage.ResColor
import com.github.panpf.zoomimage.sample.R.color
import com.github.panpf.zoomimage.sample.R.drawable
import com.github.panpf.zoomimage.sample.settingsService

@Composable
fun SketchListImage(sketchImageUri: String, modifier: Modifier) {
    val context = LocalContext.current
    AsyncImage(
        request = DisplayRequest(context, sketchImageUri) {
            placeholder(
                IconStateImage(
                    drawable.ic_image_outline,
                    ResColor(color.placeholder_bg)
                )
            )
            error(
                IconStateImage(
                    drawable.ic_error,
                    ResColor(color.placeholder_bg)
                )
            )
            crossfade()
            resizeApplyToDrawable()
            ignoreExifOrientation(context.settingsService.ignoreExifOrientation.value)
            resizePrecision(LongImageClipPrecisionDecider(SAME_ASPECT_RATIO))
            resizeScale(LongImageScaleDecider())
        },
        modifier = modifier,
        contentScale = ContentScale.Crop,
        contentDescription = "photo",
    )
}