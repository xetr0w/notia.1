package com.notia.app.features.timer

import com.notia.app.data.database.entity.SessionEntity
import com.notia.app.di.ServiceLocator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

enum class TimerState {
    IDLE, RUNNING, PAUSED
}

class TimerViewModel(
    private val scope: CoroutineScope
) {
    private val sessionDao = ServiceLocator.sessionDao
    
    private val _timerState = MutableStateFlow(TimerState.IDLE)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()
    
    private val _elapsedMillis = MutableStateFlow(0L)
    val elapsedMillis: StateFlow<Long> = _elapsedMillis.asStateFlow()
    
    private var timerJob: Job? = null
    private var startTime = 0L
    private var accumulatedTime = 0L

    fun toggleTimer() {
        when (_timerState.value) {
            TimerState.IDLE, TimerState.PAUSED -> startTimer()
            TimerState.RUNNING -> pauseTimer()
        }
    }
    
    fun stopTimer() {
        if (_timerState.value == TimerState.IDLE) return
        
        timerJob?.cancel()
        val finalDetails = _elapsedMillis.value
        
        // Save Session
        if (finalDetails > 1000) { // Only save if > 1 second
            scope.launch(Dispatchers.Default) {
                val session = SessionEntity(
                    id = com.notia.app.core.utils.randomUUID(),
                    type = "FOCUS",
                    durationMs = finalDetails,
                    startedAt = Clock.System.now().toEpochMilliseconds() - finalDetails, // Approx
                    endedAt = Clock.System.now().toEpochMilliseconds()
                )
                sessionDao.insertSession(session)
            }
        }
        
        reset()
    }

    private fun startTimer() {
        startTime = Clock.System.now().toEpochMilliseconds()
        _timerState.value = TimerState.RUNNING
        
        timerJob?.cancel()
        timerJob = scope.launch(Dispatchers.Default) {
             while (true) {
                 val now = Clock.System.now().toEpochMilliseconds()
                 val currentRun = now - startTime
                 _elapsedMillis.value = accumulatedTime + currentRun
                 delay(50) // Update every 50ms
             }
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        accumulatedTime = _elapsedMillis.value
        _timerState.value = TimerState.PAUSED
    }
    
    private fun reset() {
        _timerState.value = TimerState.IDLE
        _elapsedMillis.value = 0L
        accumulatedTime = 0L
    }
}
