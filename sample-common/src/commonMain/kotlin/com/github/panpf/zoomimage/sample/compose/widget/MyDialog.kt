package com.github.panpf.zoomimage.sample.compose.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog


@Composable
fun rememberMyDialogState(showing: Boolean = false): MyDialogState =
    remember { MyDialogState(showing) }

class MyDialogState(showing: Boolean = false) {
    var showing by mutableStateOf(showing)
}

@Composable
fun MyDialog(
    state: MyDialogState,
    transparentBackground: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    if (state.showing) {
        Dialog(onDismissRequest = { state.showing = false }) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .let {
                        if (!transparentBackground) {
                            it.background(Color.White, shape = RoundedCornerShape(20.dp))
                        } else {
                            it
                        }
                    }
                    .padding(20.dp)
            ) {
                content()
            }
        }
    }
}