package com.notia.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey
    val id: String,
    val type: String, // "POMODORO", "CUSTOM"
    val durationMs: Long,
    val startedAt: Long,
    val endedAt: Long
)
