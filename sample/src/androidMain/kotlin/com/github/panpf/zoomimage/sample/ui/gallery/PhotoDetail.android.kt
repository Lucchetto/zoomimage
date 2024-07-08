package com.github.panpf.zoomimage.sample.ui.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.github.panpf.sketch.LocalPlatformContext
import com.github.panpf.zoomimage.sample.appSettings
import com.github.panpf.zoomimage.sample.image.PhotoPalette
import com.github.panpf.zoomimage.sample.ui.examples.BasicZoomImageSample
import com.github.panpf.zoomimage.sample.ui.examples.CoilZoomAsyncImageSample
import com.github.panpf.zoomimage.sample.ui.examples.SketchZoomAsyncImageSample
import com.github.panpf.zoomimage.sample.ui.examples.GlideZoomAsyncImageSample

@Composable
actual fun PhotoDetail(sketchImageUri: String, photoPaletteState: MutableState<PhotoPalette>) {
    val appSettings = LocalPlatformContext.current.appSettings
    val composeImageLoader by appSettings.composeImageLoader.collectAsState()
    when (composeImageLoader) {
        "Sketch" -> SketchZoomAsyncImageSample(sketchImageUri, photoPaletteState)
        "Coil" -> CoilZoomAsyncImageSample(sketchImageUri, photoPaletteState)
        "Glide" -> GlideZoomAsyncImageSample(sketchImageUri, photoPaletteState)
        "Basic" -> BasicZoomImageSample(sketchImageUri, photoPaletteState)
        else -> throw IllegalArgumentException("Unsupported composeImageLoader: $composeImageLoader")
    }
}