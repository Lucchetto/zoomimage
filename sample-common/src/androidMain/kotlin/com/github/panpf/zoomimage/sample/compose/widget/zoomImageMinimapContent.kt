package com.github.panpf.zoomimage.sample.compose.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.github.panpf.sketch.compose.AsyncImage
import com.github.panpf.sketch.request.DisplayRequest

@Composable
actual fun ZoomImageMinimapContent(
    imageUri: String,
    modifier: Modifier,
    ignoreExifOrientation: Boolean
) {
    AsyncImage(
        request = DisplayRequest(LocalContext.current, imageUri) {
            crossfade()
            ignoreExifOrientation(ignoreExifOrientation)
        },
        contentDescription = "Minimap",
        modifier = modifier
    )
}