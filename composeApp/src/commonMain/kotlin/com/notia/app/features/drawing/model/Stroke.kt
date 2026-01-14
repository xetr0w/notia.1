package com.notia.app.features.drawing.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
// import kotlinx.serialization.Serializable
// import kotlinx.serialization.Transient

// Represents a complete stroke
// @Serializable
data class Stroke(
    val points: List<Point>,
    // Store as Long (ULong converted) to avoid toArgb issues and platform dependencies
    val colorValue: Long, 
    val width: Float
) {
    // Cached Path for rendering (avoid rebuilding on every frame if static)
    // @Transient
    var path: Path? = null
    
    // Helper to get Color object
    val color: Color
        get() = Color(colorValue.toULong())
}
