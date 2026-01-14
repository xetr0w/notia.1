package com.notia.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.notia.app.data.database.entity.StrokeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DrawingDao {
    @Query("SELECT * FROM strokes WHERE noteId = :noteId ORDER BY createdAt ASC")
    fun getStrokesForNote(noteId: String): Flow<List<StrokeEntity>>

    @Insert
    suspend fun insertStroke(stroke: StrokeEntity)
    
    @Query("DELETE FROM strokes WHERE noteId = :noteId")
    suspend fun clearStrokesForNote(noteId: String)
}
