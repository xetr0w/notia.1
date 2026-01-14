package com.notia.app.features.timer

actual fun startTimerService() {
    // iOS doesn't use Foreground Services.
    // Timer runs in ViewModel (App Lifetime) or Background Task (limited).
    // For MVP we just let ViewModel run.
}

actual fun stopTimerService() {
    // No-op
}
