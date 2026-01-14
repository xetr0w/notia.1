package com.notia.app.data.database

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import platform.Foundation.NSHomeDirectory
import androidx.room.DatabaseConfiguration
import androidx.room.InvalidationTracker
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.SQLiteStatement
import com.notia.app.data.database.dao.NoteDao
import com.notia.app.data.database.dao.SessionDao
import com.notia.app.data.database.entity.NoteEntity
import com.notia.app.data.database.entity.SessionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.emptyFlow
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

actual fun provideDatabase(context: Any?): NotiaDatabase {
    return MockNotiaDatabase()
}

actual object NotiaDatabaseConstructor : RoomDatabaseConstructor<NotiaDatabase> {
    override fun initialize(): NotiaDatabase = MockNotiaDatabase()
}

// Stub implementation for iOS
class MockNotiaDatabase : NotiaDatabase() {
    override fun noteDao(): NoteDao = MockNoteDao()
    override fun sessionDao(): SessionDao = MockSessionDao()

    override fun createInvalidationTracker(): InvalidationTracker {
        return InvalidationTracker(this, emptyMap(), emptyMap(), *emptyArray())
    }


}

class MockNoteDao : NoteDao {
    override fun getAllNotes(): Flow<List<NoteEntity>> = flowOf(emptyList())
    override suspend fun getNoteById(id: String): NoteEntity? = null
    override suspend fun insertNote(note: NoteEntity) {}
    override suspend fun updateNote(note: NoteEntity) {}
    override suspend fun deleteNote(note: NoteEntity) {}
}

class MockSessionDao : SessionDao {
    override fun getAllSessions(): Flow<List<SessionEntity>> = flowOf(emptyList())
    override suspend fun insertSession(session: SessionEntity) {}
}
