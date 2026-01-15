package com.notia.app.features.drawing.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
expect fun DrawingCanvas(
    currentColor: Color = Color.Black,
    currentWidth: Float = 5f,
    scale: Float = 1f,
    offset: androidx.compose.ui.geometry.Offset = androidx.compose.ui.geometry.Offset.Zero,
    modifier: Modifier = Modifier
)
