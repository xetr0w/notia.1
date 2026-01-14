package com.notia.app.features.drawing.model

// import kotlinx.serialization.Serializable

// @Serializable
data class Point(
    val x: Float,
    val y: Float,
    val pressure: Float = 1.0f,
    val timestamp: Long = 0L
)
