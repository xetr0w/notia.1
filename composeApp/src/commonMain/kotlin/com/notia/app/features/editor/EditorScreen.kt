package com.notia.app.features.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import com.notia.app.features.editor.components.EditorBottomBar
import com.notia.app.features.editor.components.EditorTopBar
import com.notia.app.ui.theme.PaperBackground
import com.notia.app.ui.theme.PaperLine

@Composable
fun EditorScreen(
    noteId: String,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        containerColor = PaperBackground,
        // topBar = { EditorTopBar(...) } REMOVED for clean Drawing UI
    ) { paddingValues ->
        // Zoom/Pan State
        var scale by remember { mutableStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                // Capture Gestures for BOTH Background and Drawing
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 3f)
                        offset += pan
                    }
                }
        ) {
            // 1. Paper Lines Background
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    }
            ) {
                val lineHeight = 40.dp.toPx()
                val lineCount = (size.height / lineHeight).toInt() // Note: This might need adjustment if infinite scrolling is desired
                
                for (i in 1..lineCount) {
                    val y = i * lineHeight
                    drawLine(
                        color = PaperLine,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }

            // 2. Content Layer (Drawing Engine)
             val density = androidx.compose.ui.platform.LocalDensity.current
             Box(modifier = Modifier.fillMaxSize()) {
                 com.notia.app.features.drawing.ui.DrawingCanvas(
                     currentColor = com.notia.app.ui.theme.AccentBlack,
                     currentWidth = with(density) { 3.dp.toPx() },
                     scale = scale,
                     offset = offset
                 )
             }
             
             // 3. Bottom Toolbar Overlay REMOVED
        }
    }
}
