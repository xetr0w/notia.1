package com.notia.app.data.repository

import com.notia.app.data.database.dao.SessionDao
import com.notia.app.data.database.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

class TimerRepository(private val sessionDao: SessionDao) {
    fun getAllSessions(): Flow<List<SessionEntity>> = sessionDao.getAllSessions()

    suspend fun saveSession(session: SessionEntity) = sessionDao.insertSession(session)
}
