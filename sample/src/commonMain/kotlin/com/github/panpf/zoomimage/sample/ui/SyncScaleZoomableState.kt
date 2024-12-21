package com.github.panpf.zoomimage.sample.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntSize
import com.github.panpf.zoomimage.compose.zoom.TransformZoomableState
import com.github.panpf.zoomimage.compose.zoom.ZoomableState

@Composable
fun rememberSyncScaleZoomableState(source: ZoomableState) = remember {
    SyncScaleZoomableState(source)
}

class SyncScaleZoomableState(
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
}