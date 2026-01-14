package com.notia.app.di

import com.notia.app.data.database.NotiaDatabase
import com.notia.app.data.database.provideDatabase

object ServiceLocator {
    private var database: NotiaDatabase? = null

    fun init(context: Any? = null) {
        if (database == null) {
            database = provideDatabase(context)
        }
    }

    val noteDao by lazy {
        database?.noteDao() ?: throw IllegalStateException("Database not initialized")
    }

    val sessionDao by lazy {
        database?.sessionDao() ?: throw IllegalStateException("Database not initialized")
    }
    
    val noteRepository by lazy {
        com.notia.app.data.repository.NoteRepository(noteDao)
    }
}
