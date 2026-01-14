package com.notia.app.features.drawing.model

import com.notia.app.features.drawing.model.ToolType

data class ToolConfig(
    val type: ToolType = ToolType.PEN,
    val color: Long = 0xFF000000, // ARGB (Black)
    val strokeWidth: Float = 5f,
    val alpha: Float = 1.0f
)
