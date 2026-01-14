package com.notia.app.features.timer

import androidx.compose.runtime.Composable
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.notia.app.features.timer.TimerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    onNavigateBack: () -> Unit
) {
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val viewModel = androidx.compose.runtime.remember { TimerViewModel(scope) }
    val timerState by viewModel.timerState.collectAsState()
    val elapsedMillis by viewModel.elapsedMillis.collectAsState()
    
    // Side effect for service
    androidx.compose.runtime.LaunchedEffect(timerState) {
        if (timerState == com.notia.app.features.timer.TimerState.RUNNING) {
            com.notia.app.features.timer.startTimerService()
        } else {
            com.notia.app.features.timer.stopTimerService()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Odaklanma Zamanı") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Timer Display
            val seconds = (elapsedMillis / 1000) % 60
            val minutes = (elapsedMillis / 1000) / 60
            val timeString = "${if (minutes < 10) "0" else ""}$minutes:${if (seconds < 10) "0" else ""}$seconds"

            Text(
                text = timeString,
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Native View Integration
            Box(modifier = Modifier.size(200.dp).padding(16.dp)) {
                // Here we show the NativeTimerView for iOS or custom Compose circle for Android
                NativeTimerView(
                    modifier = Modifier.fillMaxSize(),
                    isRunning = timerState == com.notia.app.features.timer.TimerState.RUNNING,
                    progress = (seconds / 60f) // Simple progress
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Controls
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = { viewModel.toggleTimer() }) {
                    Text(if (timerState == com.notia.app.features.timer.TimerState.RUNNING) "Duraklat" else "Başla")
                }
                
                Button(
                    onClick = { viewModel.stopTimer() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Bitir & Kaydet")
                }
            }
        }
    }
}
