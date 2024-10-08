package com.github.panpf.zoomimage.sample.ui.screen.base

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.github.panpf.zoomimage.sample.ui.navigation.Navigation

@Composable
fun ToolbarScreen(
    navigation: Navigation,
    menus: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        val theme = MaterialTheme.colorScheme
        Row(Modifier.fillMaxWidth().height(50.dp).background(theme.primary)) {
            ToolbarIcon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                onClick = { navigation.back() }
            )
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                menus?.invoke(this)
            }
        }
        Box(Modifier.fillMaxWidth().weight(1f)) {
            content()
        }
    }
}

@Composable
fun ToolbarIcon(imageVector: ImageVector, contentDescription: String, onClick: () -> Unit) {
    val theme = MaterialTheme.colorScheme
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = Modifier.size(50.dp).clickable(onClick = onClick).padding(14.dp),
        tint = theme.onPrimary
    )
}