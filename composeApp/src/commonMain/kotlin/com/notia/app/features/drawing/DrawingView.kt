package com.notia.app.features.drawing

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import com.notia.app.features.drawing.logic.UndoRedoManager
import com.notia.app.features.drawing.model.ToolConfig

@Composable
expect fun DrawingView(
    modifier: Modifier = Modifier,
    toolConfig: ToolConfig,
    undoRedoManager: UndoRedoManager,
    onDraw: (Any) -> Unit = {}
)
