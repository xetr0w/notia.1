package com.notia.app.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.notia.app.data.database.dao.NoteDao
import com.notia.app.data.database.dao.SessionDao
import com.notia.app.data.database.entity.NoteEntity
import com.notia.app.data.database.entity.SessionEntity

import com.notia.app.data.database.dao.DrawingDao
import com.notia.app.data.database.entity.StrokeEntity

@Database(entities = [NoteEntity::class, SessionEntity::class, StrokeEntity::class], version = 2)
@ConstructedBy(NotiaDatabaseConstructor::class)
abstract class NotiaDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun sessionDao(): SessionDao
    abstract fun drawingDao(): DrawingDao
}

// Expect actual function to get database builder
expect fun provideDatabase(context: Any?): NotiaDatabase
