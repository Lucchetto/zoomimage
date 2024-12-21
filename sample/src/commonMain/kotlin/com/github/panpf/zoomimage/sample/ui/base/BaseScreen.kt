package com.github.panpf.zoomimage.sample.ui.base

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen

abstract class BaseScreen : Screen {

    @Composable
    override fun Content() {
        Surface {
            DrawContent()
        }
    }

    @Composable
    abstract fun DrawContent()
}