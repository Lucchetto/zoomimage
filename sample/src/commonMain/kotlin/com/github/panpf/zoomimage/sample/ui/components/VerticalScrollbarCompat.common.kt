package com.github.panpf.zoomimage.sample.ui.components

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun VerticalScrollbarCompat(
    modifier: Modifier,
    gridState: LazyGridState
)