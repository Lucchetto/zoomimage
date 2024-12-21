package com.github.panpf.zoomimage.sample

import androidx.compose.ui.input.key.KeyEvent
import kotlinx.coroutines.flow.MutableSharedFlow

object EventBus {
    val keyEvent = MutableSharedFlow<KeyEvent>()
    val toastFlow = MutableSharedFlow<String>()
}