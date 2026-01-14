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
        topBar = {
             EditorTopBar(onBackClick = onNavigateBack)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. Paper Lines Background (Draw manually for infinite scalability)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val lineHeight = 40.dp.toPx()
                val lineCount = (size.height / lineHeight).toInt()
                
                for (i in 1..lineCount) {
                    val y = i * lineHeight
                    drawLine(
                        color = PaperLine,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                
                // Optional: Vertical Margin Line (Red/Pink) like real paper
                // drawLine(
                //    color = Color(0xFFFFCDD2),
                //    start = Offset(60.dp.toPx(), 0f),
                //    end = Offset(60.dp.toPx(), size.height),
                //    strokeWidth = 1.dp.toPx()
                // )
            }

            // 2. Content Layer (Drawing Engine)
             val density = androidx.compose.ui.platform.LocalDensity.current
             Box(modifier = Modifier.fillMaxSize()) {
                 com.notia.app.features.drawing.ui.DrawingCanvas(
                     currentColor = com.notia.app.ui.theme.AccentBlack,
                     currentWidth = with(density) { 3.dp.toPx() } // Correct density usage
                 )
             }
             
             // 3. Bottom Toolbar Overlay
             Box(
                 modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                 contentAlignment = Alignment.Center
             ) {
                 EditorBottomBar()
             }
        }
    }
}
