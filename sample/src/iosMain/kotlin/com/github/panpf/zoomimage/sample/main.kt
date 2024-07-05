package com.github.panpf.zoomimage.sample

import androidx.compose.ui.window.ComposeUIViewController
import com.github.panpf.sketch.SingletonSketch
import com.github.panpf.zoomimage.sample.ui.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    initials()
    return ComposeUIViewController {
        App()
    }
}

private fun initials() {
    SingletonSketch.setSafe { newSketch(it) }
    cleanImageLoaderMemoryCache()
}