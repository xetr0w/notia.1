package com.notia.app.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

import kotlinx.coroutines.Dispatchers
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

actual fun provideDatabase(context: Any?): NotiaDatabase {
    val ctx = context as Context
    val dbFile = ctx.getDatabasePath("notia.db")
    return Room.databaseBuilder<NotiaDatabase>(
        context = ctx.applicationContext,
        name = dbFile.absolutePath
    )
    .setDriver(BundledSQLiteDriver())
    .fallbackToDestructiveMigration(true)
    .setQueryCoroutineContext(Dispatchers.IO)
    .build()
}


