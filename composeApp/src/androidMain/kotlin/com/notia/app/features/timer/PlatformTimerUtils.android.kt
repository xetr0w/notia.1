package com.notia.app.features.timer

import android.content.Intent
import com.notia.app.AppContextProvider

actual fun startTimerService() {
    val context = AppContextProvider.context ?: return
    val intent = Intent(context, TimerService::class.java)
    intent.action = TimerService.ACTION_START
    context.startForegroundService(intent)
}

actual fun stopTimerService() {
    val context = AppContextProvider.context ?: return
    val intent = Intent(context, TimerService::class.java)
    intent.action = TimerService.ACTION_STOP
    context.startService(intent)
}
