package com.notia.app.features.drawing

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.notia.app.features.drawing.logic.UndoRedoManager
import com.notia.app.features.drawing.model.ToolConfig
import com.notia.app.features.drawing.ui.DrawingCanvas

@Composable
actual fun DrawingView(
    modifier: Modifier,
    toolConfig: ToolConfig,
    undoRedoManager: UndoRedoManager,
    onDraw: (Any) -> Unit
) {
    DrawingCanvas(
        currentColor = androidx.compose.ui.graphics.Color(toolConfig.color),
        currentWidth = toolConfig.strokeWidth,
        modifier = modifier
    )
}
