package com.notia.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.ink.strokes.Stroke
import com.notia.app.data.database.dao.DrawingDao
import com.notia.app.data.database.entity.StrokeEntity
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class DrawingRepository(private val dao: DrawingDao) {

    fun getStrokes(noteId: String): Flow<List<Stroke>> {
        return dao.getStrokesForNote(noteId).map { entities ->
            entities.mapNotNull { entity ->
                deserializeStroke(entity.strokeData)
            }
        }
    }

    suspend fun saveStroke(noteId: String, stroke: Stroke) {
        val data = serializeStroke(stroke)
        if (data != null) {
            val entity = StrokeEntity(
                noteId = noteId,
                strokeData = data,
                createdAt = System.currentTimeMillis()
            )
            dao.insertStroke(entity)
        }
    }
    
    suspend fun clearStrokes(noteId: String) {
        dao.clearStrokesForNote(noteId)
    }

    // TODO: Implement actual Ink serialization using Ink API's builtin tools once available in Alpha
    // For now, we might need a placeholder or custom proto if Ink API Alpha 02 doesn't expose easy serialization yet.
    // NOTE: In Alpha 02, Stroke serialization is not fully public/stable. 
    // We will assume for this 'Contract' that we can wrap the InputBatch or use a helper.
    // For now, returning null to avoid crash until Engine layer provides the serializer.
    private fun serializeStroke(stroke: Stroke): ByteArray? {
        // Placeholder: Real implementation will likely be in InkSerializer helper
        return null 
    }

    private fun deserializeStroke(data: ByteArray): Stroke? {
        // Placeholder
        return null
    }
}
