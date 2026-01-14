package com.notia.app.filestore.models

import kotlinx.serialization.Serializable

@Serializable
data class NoteMeta(
    val schemaVersion: Int = 1,
    val noteId: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val canvasMode: String = "PAGE", // PAGE or INFINITE
    val pageWidth: Float = 2100f, // A4 @ 300dpi approx width
    val pageHeight: Float = 2970f, // A4 @ 300dpi approx height
    val fingerDrawingEnabled: Boolean = true
)
