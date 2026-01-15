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
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.ink.brush.Brush
import androidx.ink.brush.StockBrushes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import com.notia.app.data.DrawingRepository
import com.notia.app.data.database.provideDatabase
import com.notia.app.drawing.engine.DrawingEngine
import com.notia.app.drawing.vm.DrawingViewModel

@Composable
actual fun DrawingCanvas(
    currentColor: Color,
    currentWidth: Float,
    scale: Float, // Hoisted state
    offset: androidx.compose.ui.geometry.Offset, // Hoisted state
    modifier: Modifier
) {
    val context = LocalContext.current
    
    // Manual DI
    val db = remember { provideDatabase(context) }
    val repository = remember { DrawingRepository(db.drawingDao()) }
    val viewModel = remember { DrawingViewModel(repository) }
    
    // Transformations (REMOVED - using hoisted state)
    // var scale by remember { mutableStateOf(1f) }
    // var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    
    // Tools State
    val activeTool by viewModel.activeTool.collectAsState()
    val isStylusOnly by viewModel.isStylusOnly.collectAsState()
    
    val penColor by viewModel.penColor.collectAsState()
    val penWidth by viewModel.penWidth.collectAsState()
    val highlighterColor by viewModel.highlighterColor.collectAsState()
    val highlighterWidth by viewModel.highlighterWidth.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            // .pointerInput(Unit) { ... } REMOVED - Gestures handled by parent (EditorScreen)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                DrawingEngine(ctx).apply {
                    setOnStrokeCreatedListener { stroke ->
                        viewModel.onStrokeAdded(stroke)
                    }
                    setDebugListener { rate, points ->
                        viewModel.updateDebugStats(rate)
                        viewModel.updateRawPoints(points)
                    }
                }
            },
            update = { engine ->
                // Apply Transform
                val matrix = android.graphics.Matrix()
                matrix.postScale(scale, scale)
                matrix.postTranslate(offset.x, offset.y)
                engine.setMatrix(matrix)
                
                // Set Stylus Mode
                engine.setStylusOnlyMode(isStylusOnly)

                // Update Brush / Eraser based on ACTIVE TOOL
                when (activeTool) {
                    com.notia.app.drawing.vm.DrawingTool.ERASER -> {
                         engine.setEraserActive(true)
                    }
                    com.notia.app.drawing.vm.DrawingTool.PEN -> {
                         engine.setEraserActive(false)
                         // Create Pen Brush
                         val brush = createBrush(penColor.toArgb(), penWidth, "marker")
                         engine.updateTool(brush, false)
                    }
                    com.notia.app.drawing.vm.DrawingTool.HIGHLIGHTER -> {
                         engine.setEraserActive(false)
                         // Create Highlighter Brush
                         val brush = createBrush(highlighterColor.toArgb(), highlighterWidth, "highlighter")
                         engine.updateTool(brush, false)
                    }
                }
            }
        )
        
        DrawingToolbar(
            viewModel = viewModel,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        
        // DEBUG OVERLAY
        // DEBUG OVERLAY
        val strokes by viewModel.strokes.collectAsState()
        val isDebug by viewModel.isDebugMode.collectAsState()
        val debugRate by viewModel.debugSampleRate.collectAsState()
        val debugPoints by viewModel.debugRawPoints.collectAsState()
        val isFreeze by viewModel.debugFreezeMode.collectAsState()
        
        // Debug Visualization Layer
        if (isDebug) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                // Apply same transform as content
                withTransform({
                    scale(scaleX = scale, scaleY = scale, pivot = androidx.compose.ui.geometry.Offset.Zero)
                    translate(left = offset.x, top = offset.y)
                }) {
                     debugPoints.forEach { point ->
                         val color = when {
                             !point.isAccepted -> Color.Gray.copy(alpha=0.5f) // Raw/Rejected
                             point.velocity < 200f -> Color.Blue // Slow
                             point.velocity < 800f -> Color.Green // Medium
                             else -> Color.Red // Fast
                         }
                         val radius = if (point.isAccepted) 4f / scale else 2f / scale
                         drawCircle(color, radius = radius, center = androidx.compose.ui.geometry.Offset(point.x, point.y))
                     }
                }
            }
        }

        val context = LocalContext.current
        androidx.compose.material3.Surface(
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha=0.7f),
            shape = RoundedCornerShape(4.dp)
        ) {
            Column(modifier = Modifier.padding(4.dp)) {
                androidx.compose.material3.Text(
                    text = "Stylus Only: ${if(isStylusOnly) "ON" else "OFF"}",
                    style = MaterialTheme.typography.labelSmall
                )
                androidx.compose.material3.Text(
                    text = "Active Tool: $activeTool",
                    style = MaterialTheme.typography.labelSmall
                )
                androidx.compose.material3.Text(
                    text = "Strokes: ${strokes.size}",
                    style = MaterialTheme.typography.labelSmall
                )
                if (isStylusOnly) {
                     androidx.compose.material3.Text(
                        text = "(Finger drawing disabled)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Red
                    )
                }
                
                // Debug Controls
                androidx.compose.material3.Button(
                    onClick = { viewModel.toggleDebugMode() },
                    modifier = Modifier.padding(top = 4.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
                ) {
                    androidx.compose.material3.Text(if (isDebug) "Hide Debug" else "Show Debug", style = MaterialTheme.typography.labelSmall)
                }
                
                if (isDebug) {
                    androidx.compose.material3.Text(text = debugRate, style = MaterialTheme.typography.labelSmall)
                    
                    androidx.compose.material3.Button(
                        onClick = { viewModel.toggleFreezeMode() },
                        modifier = Modifier.padding(top = 4.dp),
                         contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
                    ) {
                        androidx.compose.material3.Text(if (isFreeze) "UNFREEZE" else "FREEZE (After Stroke)", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

private fun createBrush(colorInt: Int, width: Float, family: String): Brush {
    val brushFamily = if (family == "highlighter") StockBrushes.highlighterV1 else StockBrushes.pressurePenV1
    
    // MVP Hack: Increase minimum pen thickness to 2f for better visibility
    val effectiveWidth = if (family == "highlighter") width else if (width < 2f) 2f else width
    
    return Brush.createWithColorIntArgb(
        family = brushFamily,
        colorIntArgb = colorInt,
        size = effectiveWidth,
        epsilon = 0.01f
    )
}
