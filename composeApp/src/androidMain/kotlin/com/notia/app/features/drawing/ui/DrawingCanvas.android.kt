package com.notia.app.features.drawing.ui

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.ink.brush.Brush
import androidx.ink.brush.StockBrushes
import com.notia.app.data.DrawingRepository
import com.notia.app.data.database.provideDatabase
import com.notia.app.drawing.engine.DrawingEngine
import com.notia.app.drawing.vm.DrawingViewModel

@Composable
actual fun DrawingCanvas(
    currentColor: Color,
    currentWidth: Float,
    modifier: Modifier
) {
    val context = LocalContext.current
    
    // Manual DI
    val db = remember { provideDatabase(context) }
    val repository = remember { DrawingRepository(db.drawingDao()) }
    val viewModel = remember { DrawingViewModel(repository) }
    
    // UI State
    var selectedColor by remember { mutableStateOf(currentColor) }
    var selectedWidth by remember { mutableStateOf(currentWidth) }
    var isEraserActive by remember { mutableStateOf(false) }
    
    // Transformations
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale *= zoom
                        offset += pan
                    }
                },
            factory = { ctx ->
                DrawingEngine(ctx).apply {
                    setOnStrokeCreatedListener { stroke ->
                        viewModel.onStrokeAdded(stroke)
                    }
                }
            },
            update = { engine ->
                // Apply Transform
                val matrix = android.graphics.Matrix()
                matrix.postScale(scale, scale)
                matrix.postTranslate(offset.x, offset.y)
                engine.setMatrix(matrix)

                // Update Brush / Eraser
                if (isEraserActive) {
                     engine.setEraserActive(true)
                } else {
                     engine.setEraserActive(false)
                     val brush = createBrush(selectedColor.toArgb(), selectedWidth)
                     engine.updateBrush(brush)
                }
            }
        )
        
        DrawingToolbar(
            currentWidth = selectedWidth,
            currentColor = selectedColor,
            onWidthChange = { selectedWidth = it },
            onColorChange = { selectedColor = it },
            onClear = { viewModel.clearCanvas() },
            onEraserClick = { isEraserActive = !isEraserActive },
            isEraserActive = isEraserActive,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        )
    }
}

private fun createBrush(colorInt: Int, width: Float): Brush {
    val family = StockBrushes.markerV1
    return Brush.createWithColorIntArgb(
        family = family,
        colorIntArgb = colorInt,
        size = width,
        epsilon = 0.1f
    )
}
